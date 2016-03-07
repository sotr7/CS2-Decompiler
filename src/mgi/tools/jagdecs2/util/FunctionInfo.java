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

import mgi.tools.jagdecs2.CS2Type;

public class FunctionInfo {

	private String name;
	private CS2Type returnType;
	private CS2Type[] argumentTypes;
	private String[] argumentNames;

	
	public FunctionInfo(String name,CS2Type[] argTypes,CS2Type returnType, String[] argNames) {
		this.name = name;
		this.argumentTypes = argTypes;
		this.returnType = returnType;
		this.argumentNames = argNames;
	}
	
	
	
	public String getName() {
		return name;
	}

	public CS2Type[] getArgumentTypes() {
		return argumentTypes;
	}

	public CS2Type getReturnType() {
		return returnType;
	}

	public String[] getArgumentNames() {
		return argumentNames;
	}
	
	public String toString() {
		StringBuilder bld = new StringBuilder();
		bld.append(returnType);
		bld.append(' ');
		bld.append(name);
		bld.append('(');
		for (int i = 0; i < argumentTypes.length; i++) {
			bld.append(argumentTypes[i].toString() + " " + argumentNames[i]);
			if ((i + 1) < argumentTypes.length)
				bld.append(',');
		}
		bld.append(')');
		bld.append(';');
		return bld.toString();
	}
}
