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

public class LoadNamedDataNode extends ExpressionNode {

	private String name;
    private CS2Type type;
    
    public LoadNamedDataNode(String name, CS2Type type) {
    	this.name = name;
    	this.type = type;	
    }
    
    @Override
    public int getPriority() {
    	return ExpressionNode.PRIORITY_STANDART;
    }

    @Override
    public CS2Type getType() {
    	return type;
    }

	@Override
	public ExpressionNode copy() {
		return new LoadNamedDataNode(this.name,this.type);
	}

	public String getName() {
		return name;
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		printer.print(name);
		printer.endPrinting(this);
	}

}
