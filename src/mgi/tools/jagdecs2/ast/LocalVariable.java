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


package mgi.tools.jagdecs2.ast;

import mgi.tools.jagdecs2.CS2Type;

public class LocalVariable {

	private String name;
	private CS2Type type;
	private int identifier = -1;
	private boolean needsScopeDeclaration = true;
	private boolean isArgument;
	
	public LocalVariable(String name,CS2Type type) {
		this(name,type, false);
	}
	
	public LocalVariable(String name,CS2Type type, boolean isArgument) {
		this.name = name;
		this.type = type;
		this.isArgument = isArgument;
	}

	public CS2Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isArgument() {
		return isArgument;
	}
	
	public String toString() {
		return type + " " + name;
	}

	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}

	public int getIdentifier() {
		return identifier;
	}
	
	public boolean needsScopeDeclaration() {
		return needsScopeDeclaration;
	}
	
	public void setNeedsScopeDeclaration(boolean v) {
		needsScopeDeclaration = v;
	}

	public static int makeIdentifier(int index,int stackType) {
		return index | stackType << 16;
	}
	
	public static int makeStackDumpIdentifier(int index, int stackType) {
		return index | (stackType << 16) | 0x40000000;
	}
}
