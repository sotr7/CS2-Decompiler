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

public class IfElseNode extends AbstractCodeNode {

    /**
     * Contains expressions for each scope.
     */
	private ExpressionNode[] expressions;
	/**
	 * Contains scopes for each expression.
	 */
	private ScopeNode[] scopes;
	/**
	 * Contains else scope if all expressions conditions 
	 * were not met.
	 */
	private ScopeNode elseScope;
	
    
    public IfElseNode(ExpressionNode[] expressions, ScopeNode[] scopes,ScopeNode elseScope) {
    	this.expressions = expressions;
    	this.scopes = scopes;
    	this.elseScope = elseScope;
    	for (int i = 0; i < expressions.length; i++) {
    		this.write(expressions[i]);
    		expressions[i].setParent(this);
    	}
    	for (int i = 0; i < scopes.length; i++) {
    		this.write(scopes[i]);
    		scopes[i].setParent(this);
    	}
    	this.write(elseScope);
    	elseScope.setParent(this);
    }
   
    
    public boolean hasElseScope() {
    	return !this.elseScope.isEmpty();
    }
    
    public ExpressionNode[] getExpressions() {
    	return expressions;
    }
    
    public ScopeNode[] getScopes() {
    	return scopes;
    }
    
    public ScopeNode getElseScope() {
    	return elseScope;
    }


	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		for (int i = 0; i < expressions.length; i++) {
			boolean first = i == 0;
			printer.print(first ? "if (" : "\nelse if (");
			expressions[i].print(printer);
			printer.print(") ");
			scopes[i].print(printer);
		}
		if (hasElseScope()) {
			printer.print("\nelse ");
			elseScope.print(printer);
		}
		printer.endPrinting(this);
	}
}
