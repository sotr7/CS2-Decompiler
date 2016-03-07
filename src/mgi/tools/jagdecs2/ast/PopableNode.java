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

public class PopableNode extends AbstractCodeNode {

    private ExpressionNode expression;
    
    public PopableNode(ExpressionNode expression) {
    	this.expression = expression;
    	this.write(expression);
		expression.setParent(this);
    }
   
	public ExpressionNode getExpression() {
		return expression;
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		expression.print(printer);
		printer.print(';');
		printer.endPrinting(this);
	}


}
