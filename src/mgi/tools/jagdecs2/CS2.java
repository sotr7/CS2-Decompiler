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

import java.util.ArrayList;
import java.util.List;

import mgi.tools.jagdecs2.instructions.AbstractInstruction;
import mgi.tools.jagdecs2.instructions.Label;
import mgi.tools.jagdecs2.instructions.SwitchInstruction;

public class CS2 {

	private String name;
	private CS2Type[] argumentsTypes;
	private String[] argumentNames;
	private CS2Type returnType = CS2Type.UNKNOWN;
	private int intLocalsSize, stringLocalsSize, longLocalsSize;
	private int intArgumentsCount, stringArgumentsCount, longArgumentsCount;
	private AbstractInstruction[] instructions;

	public CS2(String name, CS2Type[] args, int intls, int stringls, int longls, int intac, int stringtac, int longac, int codeSize) {
		this.name = name;
		this.argumentsTypes = args;
		this.intLocalsSize = intls;
		this.stringLocalsSize = stringls;
		this.longLocalsSize = longls;
		this.intArgumentsCount = intac;
		this.stringArgumentsCount = stringtac;
		this.longArgumentsCount = longac;
		this.instructions = new AbstractInstruction[codeSize * 2];
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setReturnType(CS2Type returnType) {
		this.returnType = returnType;
	}

	public CS2Type getReturnType() {
		return returnType;
	}
	
	public void setArgumentTypes(CS2Type[] arguments) {
		this.argumentsTypes = arguments;
	}

	public CS2Type[] getArgumentTypes() {
		return argumentsTypes;
	}
	
	public void setArgumentNames(String[] names) {
		this.argumentNames = names;
	}
	
	public String[] getArgumentNames() {
		return argumentNames;
	}

	public int getIntLocalsSize() {
		return intLocalsSize;
	}

	public int getStringLocalsSize() {
		return stringLocalsSize;
	}

	public int getLongLocalsSize() {
		return longLocalsSize;
	}

	public int getIntArgumentsCount() {
		return intArgumentsCount;
	}
	
	public int getStringArgumentsCount() {
		return stringArgumentsCount;
	}

	public int getLongArgumentsCount() {
		return longArgumentsCount;
	}

	public AbstractInstruction[] getInstructions() {
		return instructions;
	}

	
	@Deprecated
	public int addressOf(AbstractInstruction instr) {
		for (int i = 0; i < instructions.length; i++)
			if (instructions[i] == instr)
				return i;
		return -1;
	}


	public void prepareInstructions() {
		int nonLabelsAmt = instructions.length / 2;
		for (int i = 0; i < nonLabelsAmt; i++) {
			if (instructions[i * 2 + 1] instanceof SwitchInstruction) {
				SwitchInstruction instr = (SwitchInstruction) instructions[i * 2 + 1];
				if (((i + 1) * 2 + 1) >= instructions.length) {
					AbstractInstruction[] buf = new AbstractInstruction[(nonLabelsAmt + 1) * 2];
					System.arraycopy(instructions, 0, buf, 0, instructions.length);
					this.instructions = buf;
				}
				
				if (instructions[(i + 1) * 2] == null)
					instructions[(i + 1) * 2] = new Label();
				instr.attachDefault((Label)instructions[(i + 1) * 2]);
			}
		}
		
		
		List<AbstractInstruction> buffer = new ArrayList<AbstractInstruction>();
		for (int i = 0; i < this.instructions.length; i++)
			if (this.instructions[i] != null)
				buffer.add(this.instructions[i]);

		this.instructions = new AbstractInstruction[buffer.size()];
		int write = 0;
		for (AbstractInstruction instr : buffer)
			this.instructions[write++] = instr;
		for (int i = 0; i < instructions.length; i++)
			instructions[i].setAddress(i);
		for (int i = 0,labelsFound = 0; i < instructions.length; i++)
			if (instructions[i] instanceof Label)
				((Label)instructions[i]).setLabelID(labelsFound++);
		for (int i = 0; i < instructions.length; i++)
			if (instructions[i] instanceof SwitchInstruction)
				((SwitchInstruction)instructions[i]).sort();
	}
	
	public int countOf(Class<? extends AbstractInstruction> type) { 
		int total = 0;
		for (int i = 0; i < instructions.length; i++)
			if (instructions[i].getClass() == type)
				total++;
		return total;
	}
}
