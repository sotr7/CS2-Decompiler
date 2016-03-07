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

public class SwitchNode extends AbstractCodeNode implements IBreakableNode {

    /**
     * Contains expression which type is boolean.
     */
    private ExpressionNode expression;
    /**
     * Contains scope which should be executed if 
     * expression finds valid case.
     */
    private ScopeNode scope;
    /**
     * Contains end block of this switch node.
     */
    private FlowBlock end;
    /**
     * Contains label name.
     */
    private String labelName;
    
    public SwitchNode(FlowBlock end, ScopeNode scope, ExpressionNode expr) {
    	this.end = end;
    	this.expression = expr;
    	this.scope = scope;
		this.write(expr);
		this.write(scope);
		expr.setParent(this);
		scope.setParent(this);
    }
    

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		if (this.labelName != null)
			printer.print(this.labelName + " ");
		printer.print("switch (");
		expression.print(printer);
		printer.print(") ");
		scope.print(printer);
		printer.endPrinting(this);
	}
    
	public ScopeNode getScope() {
		return scope;
	}

	public ExpressionNode getExpression() {
		return expression;
	}
	
	@Override
	public boolean canBreak() {
		return this.end != null;
	}

	@Override
	public FlowBlock getEnd() {
		return this.end;
	}

	@Override
	public void enableLabelName() {
		this.labelName = "switch_" + this.hashCode() + ":";	
	}

	@Override
	public String getLabelName() {
		return this.labelName;
	}


}
