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

public class StructPartLoadNode extends ExpressionNode {

    private String name;
    private CS2Type type;
    private ExpressionNode expression;
    
    public StructPartLoadNode(String name,CS2Type type,ExpressionNode expr) {
    	this.name = name;
    	this.type = type;
    	this.expression = expr;
    	this.write(expr);
    	expr.setParent(this);
    }

    @Override
    public CS2Type getType() {
    	return this.type;
    }
    
	public String getName() {
		return name;
	}

	public ExpressionNode getExpression() {
		return expression;
	}
    
	@Override
	public ExpressionNode copy() {
		return new StructPartLoadNode(this.name,this.type,this.expression.copy());
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		boolean needsParen = expression.getPriority() > ExpressionNode.PRIORITY_MEMBER_ACCESS;
		if (needsParen)
			printer.print('(');
		expression.print(printer);
		if (needsParen)
			printer.print(')');
		printer.print("." + name);
		printer.endPrinting(this);
	}



}
