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

import java.util.Arrays;

import mgi.tools.jagdecs2.util.InstructionInfo;

public class AbstractInstruction {

	private InstructionInfo info;
	private int address;

	public AbstractInstruction(InstructionInfo info) {
		this.info = info;
		this.address = -1;
	}

	public int getOpcode() {
		return info != null ? info.getOpcode() : -1;
	}
	
	public int getScrambledOpcode() {
		return info != null ? info.getScrampledOpcode() : -1;
	}
	
	public String getName() {
		return info != null ? info.getName() : "N/A";
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public int getAddress() {
		return address;
	}
	
	@Override
	public String toString() {
		char[] pad = new char[25];
		Arrays.fill(pad, ' ');
		char[] name = (info != null ? info.getName() : "N/A").toCharArray();
		System.arraycopy(name, 0, pad, 0, Math.min(pad.length, name.length));
		if (name.length > pad.length) {
			pad[pad.length - 3] = '.';
			pad[pad.length - 2] = '.';
			pad[pad.length - 1] = '.';
		}
		
		return new String(pad);
	}
}
