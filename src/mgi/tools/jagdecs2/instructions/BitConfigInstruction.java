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

import mgi.tools.jagdecs2.util.BitConfigInfo;
import mgi.tools.jagdecs2.util.InstructionInfo;

public class BitConfigInstruction extends AbstractInstruction {

	private BitConfigInfo config;
	private boolean constant;

	public BitConfigInstruction(InstructionInfo info, BitConfigInfo config, boolean constant) {
		super(info);
		this.config = config;
		this.constant = constant;
	}
	
	public BitConfigInfo getConfig() {
		return config;
	}

	public boolean getConstant() {
		return constant;
	}

	public String toString() {
		return super.toString() + "\t" + config + "\t" + constant;
	}

}
