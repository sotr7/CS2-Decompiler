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

public class InstructionInfo {
	
	private String name;
	private int scrampledOpcode;
	private int originalOpcode;
	private boolean hasIntConstant;
	
	public InstructionInfo(String name, int scrampledop, int originalop, boolean hasIntConstant) {
		this.name = name;
		this.scrampledOpcode = scrampledop;
		this.originalOpcode = originalop;
		this.hasIntConstant = hasIntConstant;
	}
	
	public String getName() {
		return name;
	}

	public int getScrampledOpcode() {
		return scrampledOpcode;
	}

	public int getOpcode() {
		return originalOpcode;
	}

	public boolean hasIntConstant() {
		return hasIntConstant;
	}
	
	@Override
	public String toString() {
		return name + "[" + originalOpcode + "," + scrampledOpcode + ":" + hasIntConstant + "]";
	}
	
}
