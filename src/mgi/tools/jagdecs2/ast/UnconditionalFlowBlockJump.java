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

import mgi.tools.jagdecs2.CodePrinter;

public class UnconditionalFlowBlockJump extends AbstractCodeNode {

    /**
     * Contains target flow block.
     */
    private FlowBlock target;
    
    public UnconditionalFlowBlockJump(FlowBlock target) {
    	this.target = target;
    }
 
	
	public FlowBlock getTarget() {
		return target;
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		printer.print("GOTO\t" + "flow_" + target.getBlockID());
		printer.endPrinting(this);
	}
}
