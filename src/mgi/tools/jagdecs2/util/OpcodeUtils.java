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


package mgi.tools.jagdecs2.util;

import mgi.tools.jagdecs2.instructions.Opcodes;

public class OpcodeUtils {

	
	public static int getTwoConditionsJumpStackType(int opcode) {
		if (opcode >= Opcodes.INT_EQ && opcode <= Opcodes.INT_GE)
			return 0;
		else if (opcode >= Opcodes.LONG_EQ && opcode <= Opcodes.LONG_GE)
			return 2;
		else
			return -1;
	}
	
	public static int getTwoConditionsJumpConditional(int opcode) {
		switch (opcode) {
			case Opcodes.INT_NE:
			case Opcodes.LONG_NE:
				return 0; // !=
			case Opcodes.INT_EQ:
			case Opcodes.LONG_EQ:
				return 1; // ==
			case Opcodes.INT_LT:
			case Opcodes.LONG_LT:
				return 3; // <
			case Opcodes.INT_GT:
			case Opcodes.LONG_GT:
				return 2; // >
			case Opcodes.INT_LE:
			case Opcodes.LONG_LE:
				return 5; // <=
			case Opcodes.INT_GE:
			case Opcodes.LONG_GE:
				return 4; // >=
			default:
				return -1;
		}
	}
	
	public static int getOneConditionJumpStackType(int opcode) {
		if (opcode == Opcodes.INT_T || opcode == Opcodes.INT_F)
			return 0;
		else 
			return -1;
	}
}
