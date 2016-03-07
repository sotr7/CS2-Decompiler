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

public class StructConstructExpr extends ExpressionNode  {


    private CS2Type type;
    private ExpressionNode[] arguments;
    
    
    public StructConstructExpr(CS2Type type,ExpressionNode[] arguments) {
    	this.type = type;
    	this.arguments = arguments;
    	for (int i = 0; i < arguments.length; i++) {
    		this.write(arguments[i]);
    		arguments[i].setParent(this);
    	}
    }
    
    @Override
    public int getPriority() {
    	return ExpressionNode.PRIORITY_CALL;
    }

    @Override
    public CS2Type getType() {
    	return type;
    }


	@Override
	public ExpressionNode copy() {
		ExpressionNode[] argsCopy = new ExpressionNode[arguments.length];
		for (int i = 0; i < arguments.length; i++)
			argsCopy[i] = arguments[i].copy();
		return new StructConstructExpr(this.type,argsCopy);
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		printer.print(type.name() + "");
		printer.print('(');
		for (int i = 0; i < arguments.length; i++) {
			arguments[i].print(printer);
			if ((i + 1) < arguments.length)
				printer.print(", ");
		}
		printer.print(')');
		printer.endPrinting(this);
		
	}

}
