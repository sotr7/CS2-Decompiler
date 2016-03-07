/*
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package mgi.tools.jagdecs2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mgi.tools.jagdecs2.instructions.BitConfigInstruction;
import mgi.tools.jagdecs2.instructions.BooleanInstruction;
import mgi.tools.jagdecs2.instructions.ConfigInstruction;
import mgi.tools.jagdecs2.instructions.IntInstruction;
import mgi.tools.jagdecs2.instructions.JumpInstruction;
import mgi.tools.jagdecs2.instructions.Label;
import mgi.tools.jagdecs2.instructions.LongInstruction;
import mgi.tools.jagdecs2.instructions.Opcodes;
import mgi.tools.jagdecs2.instructions.StringInstruction;
import mgi.tools.jagdecs2.instructions.SwitchInstruction;
import mgi.tools.jagdecs2.util.BitConfigInfo;
import mgi.tools.jagdecs2.util.ByteBuffer;
import mgi.tools.jagdecs2.util.ConfigInfo;
import mgi.tools.jagdecs2.util.ConfigsDatabase;
import mgi.tools.jagdecs2.util.InstructionInfo;
import mgi.tools.jagdecs2.util.InstructionsDatabase;

public class CS2Decoder {

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static final CS2 readScript(InstructionsDatabase idb, ConfigsDatabase cdb, File file) throws IOException {
		if (!file.exists() || !file.isFile() || !file.canRead())
			throw new FileNotFoundException("Script file " + file + " does not exist.");
		FileInputStream stream = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		int readed = stream.read(data);
		stream.close();
		if (readed != data.length)
			throw new IOException("Reading failed.");
		ByteBuffer buffer = new ByteBuffer(data);

		buffer.setPosition(data.length - 2);
		int switchBlocksSize = buffer.readUnsignedShort();
		int codeBlockEnd = data.length - switchBlocksSize - 16 - 2;
		buffer.setPosition(codeBlockEnd);
		int codeSize = buffer.readInt();
		int intLocalsCount = buffer.readUnsignedShort();
		int stringLocalsCount = buffer.readUnsignedShort();
		int longLocalsCount = buffer.readUnsignedShort();

		int intArgsCount = buffer.readUnsignedShort();
		int stringArgsCount = buffer.readUnsignedShort();
		int longArgsCount = buffer.readUnsignedShort();

		int switchesCount = buffer.readUByte();
		Map[] switches = new HashMap[switchesCount];
		for (int i = 0; i < switchesCount; i++) {
			int numCases = buffer.readUnsignedShort();
			switches[i] = new HashMap<Integer, Integer>(numCases);
			while (numCases-- > 0) {
				switches[i].put(buffer.readInt(), buffer.readInt());
			}
		}
		buffer.setPosition(0);
		String scriptName = buffer.readNullString();
		int[] intPool = new int[codeSize];
		Object[] objectPool = new Object[codeSize];
		long[] longPool = new long[codeSize];
		InstructionInfo[] instructions = new InstructionInfo[codeSize];

		int writeOffset = 0;
		while (buffer.getPosition() < codeBlockEnd) {
			int opcode = buffer.readUnsignedShort();
			InstructionInfo info = idb.getByScrampled(opcode);
			if (info == null)
				throw new RuntimeException("Unknown opcode:" + opcode);
			opcode = info.getOpcode();			

			if (opcode == Opcodes.LOAD_CONFIG || opcode == Opcodes.STORE_CONFIG) {
				int domainType = buffer.readUByte();
				int id = buffer.readUnsignedShort();
				ConfigInfo cfg = cdb.getConfigInfo(domainType, id);
				if (cfg == null)
					throw new RuntimeException("Unknown config:" + domainType + "," + id);
				intPool[writeOffset] = buffer.readUByte();
				objectPool[writeOffset] = cfg;
			}
			else if (opcode == Opcodes.PUSH_STR) {
				int baseType = buffer.readUByte();
				if (baseType == 0) {
					info = idb.getByUnscrampled(opcode = Opcodes.PUSH_INT);
					intPool[writeOffset] = buffer.readInt();
				}
				else if (baseType == 1) {
					info = idb.getByUnscrampled(opcode = Opcodes.PUSH_LONG);
					longPool[writeOffset] = buffer.readLong();
				}
				else if (baseType == 2)
					objectPool[writeOffset] = buffer.readString();
				else
					throw new RuntimeException("Unknown base type:" + baseType);
			}
			else if (opcode == Opcodes.LOAD_BITCONFIG || opcode == Opcodes.STORE_BITCONFIG) {
				int id = buffer.readUnsignedShort();
				BitConfigInfo cfg = cdb.getBitConfigInfo(id);
				if (cfg == null)
					throw new RuntimeException("Unknown bitconfig:" + id);
				intPool[writeOffset] = buffer.readUByte();
				objectPool[writeOffset] = cfg;
			}
			else {
				intPool[writeOffset] = info.hasIntConstant() ? buffer.readInt() : buffer.readUByte();
			}
				
			instructions[writeOffset++] = info;
		}

		CS2Type[] args = new CS2Type[intArgsCount + stringArgsCount + longArgsCount];
		int write = 0;
		for (int i = 0; i < intArgsCount; i++)
			args[write++] = CS2Type.INT;
		for (int i = 0; i < stringArgsCount; i++)
			args[write++] = CS2Type.STRING;
		for (int i = 0; i < longArgsCount; i++)
			args[write++] = CS2Type.LONG;

		CS2 script = new CS2(scriptName, args, intLocalsCount, stringLocalsCount, longLocalsCount, intArgsCount, stringArgsCount,longArgsCount,codeSize);
		for (int i = 0; i < codeSize; i++) {
			InstructionInfo info = instructions[i];
			int opcode = info.getOpcode();
			if (opcode == Opcodes.PUSH_STR)
				script.getInstructions()[(i * 2) + 1] = new StringInstruction(info, (String) objectPool[i]);
			else if (opcode == Opcodes.PUSH_LONG)
				script.getInstructions()[(i * 2) + 1] = new LongInstruction(info, longPool[i]);
			else if (opcode == Opcodes.SWITCH) { // switch
				Map block = switches[intPool[i]];
				int[] cases = new int[block.size()];
				Label[] targets = new Label[block.size()];
				int w = 0;
				for (Object key : block.keySet()) {
					cases[w] = (Integer) key;
					Object addr = block.get(key);
					int full = i + ((Integer) addr).intValue() + 1;
					if (script.getInstructions()[full * 2] == null)
						script.getInstructions()[full * 2] = new Label();
					targets[w++] = (Label) script.getInstructions()[full * 2];
				}
				script.getInstructions()[(i * 2) + 1] = new SwitchInstruction(info, cases, targets);
			} 
			else if (opcode >= Opcodes.GOTO && opcode <= Opcodes.LONG_GE) {
				int fullAddr = i + intPool[i] + 1;
				if (script.getInstructions()[fullAddr * 2] == null)
					script.getInstructions()[fullAddr * 2] = new Label();
				script.getInstructions()[(i * 2) + 1] = new JumpInstruction(info, (Label) script.getInstructions()[fullAddr * 2]);
			} 
			else if (opcode == Opcodes.LOAD_CONFIG || opcode == Opcodes.STORE_CONFIG)
				script.getInstructions()[(i * 2) + 1] = new ConfigInstruction(info, (ConfigInfo)objectPool[i], intPool[i] == 1);
			else if (opcode == Opcodes.LOAD_BITCONFIG || opcode == Opcodes.STORE_BITCONFIG)
				script.getInstructions()[(i * 2) + 1] = new BitConfigInstruction(info, (BitConfigInfo)objectPool[i], intPool[i] == 1);
			else if (opcode < 150)
				script.getInstructions()[(i * 2) + 1] = new IntInstruction(info, intPool[i]);
			else
				script.getInstructions()[(i * 2) + 1] = new BooleanInstruction(info, intPool[i] == 1);
		}

		script.prepareInstructions();
		return script;
	}

}
