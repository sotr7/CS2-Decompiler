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

public class StoreNamedDataNode extends ExpressionNode {
	
	private String name;
    private ExpressionNode expression;
    
    public StoreNamedDataNode(String name,ExpressionNode expr) {
    	this.name = name;
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
		return new StoreNamedDataNode(this.name, this.expression.copy());
	}

	public String getName() {
		return name;
	}

	public ExpressionNode getExpression() {
		return expression;
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		boolean needsParen = expression.getPriority() > getPriority();
		printer.print(name + " = ");
		if (needsParen)
			printer.print('(');
		expression.print(printer);
		if (needsParen)
			printer.print(')');
		printer.endPrinting(this);
	}

}
