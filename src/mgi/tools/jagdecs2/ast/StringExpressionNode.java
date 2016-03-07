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

public class StringExpressionNode extends ExpressionNode implements IConstantNode {

    /**
     * Contains string which this expression holds.
     */
    private String data;
    
    public StringExpressionNode(String data) {
    	this.data = data;
    }

    
    public String getData() {
    	return data;
    }

    @Override
    public CS2Type getType() {
    	return CS2Type.STRING;
    }

	@Override
	public Object getConst() {
		return this.data;
	}

	@Override
	public ExpressionNode copy() {
		return new StringExpressionNode(this.data);
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		getType().printConstant(printer, data);
		printer.endPrinting(this);
	}

}
