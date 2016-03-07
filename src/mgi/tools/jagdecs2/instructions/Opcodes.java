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


package mgi.tools.jagdecs2.instructions;

import java.lang.reflect.Field;

public class Opcodes {
	
	public static final int PUSH_INT = 0;
	public static final int PUSH_STR = 1;
	public static final int PUSH_LONG = 2;
	
	public static final int LOAD_INT = 3;
	public static final int STORE_INT = 4;
	public static final int LOAD_STR = 5;
	public static final int STORE_STR = 6;
	public static final int LOAD_LONG = 7;
	public static final int STORE_LONG = 8;
	
	public static final int POP_INT = 9;
	public static final int POP_STR = 10;
	public static final int POP_LONG = 11;
	
	public static final int NEW_ARRAY = 12;
	public static final int ARRAY_LOAD = 13;
	public static final int ARRAY_STORE = 14;
	
	public static final int CALL_CS2 = 15;
	public static final int RETURN = 16;
	
	public static final int SWITCH = 17;
	public static final int GOTO = 18;
	public static final int INT_EQ = 19;
	public static final int INT_NE = 20;
	public static final int INT_LT = 21;
	public static final int INT_GT = 22;
	public static final int INT_LE = 23;
	public static final int INT_GE = 24;
    public static final int INT_T = 25;
    public static final int INT_F = 26;
	public static final int LONG_EQ = 27;
	public static final int LONG_NE = 28;
	public static final int LONG_LT = 29;
	public static final int LONG_GT = 30;
	public static final int LONG_LE = 31;
	public static final int LONG_GE = 32;
	
	public static final int LOAD_CONFIG = 33;
	public static final int STORE_CONFIG = 34;
	public static final int LOAD_BITCONFIG = 35;
	public static final int STORE_BITCONFIG = 36;
	public static final int CONCAT_STRINGS = 37;
	
	
	public static int getOpcode(String name) {
		try {
			Field[] flds = Opcodes.class.getFields();
			for (Field f : flds) {
				if (!f.getName().equals(name)) {
					continue;
				}
				return f.getInt(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static String getOpcodeName(int opcode) {
		try {
			Field[] flds = Opcodes.class.getFields();
			for (Field f : flds) {
				if (f.getInt(null) != opcode) {
					continue;
				}
				return (f.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "n/a:" + opcode;
	}
}
