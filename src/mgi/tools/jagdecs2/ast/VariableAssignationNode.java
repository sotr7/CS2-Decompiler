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
import mgi.tools.jagdecs2.CodePrinter;

public class VariableAssignationNode extends ExpressionNode {


    private LocalVariable variable;
    private ExpressionNode expression;
    private boolean isDeclaration = false;
    
    public VariableAssignationNode(LocalVariable variable,ExpressionNode expr) {
    	this.variable = variable;
    	this.expression = expr;
    	this.write(expr);
    	expr.setParent(this);
    }
    
    @Override
    public int getPriority() {
    	return ExpressionNode.PRIORITY_ASSIGNMENT;
    }

    @Override
    public CS2Type getType() {
    	return this.expression.getType();
    }

	@Override
	public ExpressionNode copy() {
		return new VariableAssignationNode(this.variable,this.expression.copy());
	}

	public LocalVariable getVariable() {
		return variable;
	}
	
	public void setVariable(LocalVariable v) {
		this.variable = v;
	}

	public ExpressionNode getExpression() {
		return expression;
	}
	
	public boolean isDeclaration() {
		return isDeclaration;
	}
	
	public void setIsDeclaration(boolean is) {
		isDeclaration = is;
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		boolean needsParen = expression.getPriority() > getPriority();
		if (isDeclaration)
			printer.print(variable.getType() + " ");
		printer.print(variable.getName() + " = ");
		if (needsParen)
			printer.print('(');
		expression.print(printer);
		if (needsParen)
			printer.print(')');
		printer.endPrinting(this);
	}

}
