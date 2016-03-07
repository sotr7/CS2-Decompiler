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

public class SwitchFlowBlockJump extends AbstractCodeNode {


    private ExpressionNode expression;
    private int[] cases;
    private FlowBlock[] targets;
    private int defaultIndex;
    
    public SwitchFlowBlockJump(ExpressionNode expr,int[] cases, FlowBlock[] targets, int defaultIndex) {
    	this.expression = expr;
    	this.cases = cases;
    	this.targets = targets;
		this.defaultIndex = defaultIndex;
		this.write(expr);
		expr.setParent(this);
    }
    

	public ExpressionNode getExpression() {
		return expression;
	}
	
	public int[] getCases() {
		return cases;
	}
	
	public FlowBlock[] getTargets() {
		return targets;
	}
	
	public int getDefaultIndex() {
		return defaultIndex;
	}
	


	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		printer.print("SWITCH (");
		expression.print(printer);
		printer.print(") {");
		printer.tab();
		for (int i = 0; i < cases.length; i++) {
			if (i == defaultIndex)
				printer.print("\ndefault:\n\t GOTO flow_" + targets[i].getBlockID());
			else
				printer.print("\ncase " + cases[i] + ":\n\t GOTO flow_" + targets[i].getBlockID());
		}
		
		printer.untab();
		printer.print("\n}");
		printer.endPrinting(this);

	}
}
