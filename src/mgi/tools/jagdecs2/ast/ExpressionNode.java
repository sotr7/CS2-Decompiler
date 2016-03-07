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


public abstract class ExpressionNode extends AbstractCodeNode {

	public static final int PRIORITY_STANDART = 0;
	public static final int PRIORITY_ARRAY_INDEX = 1;
	public static final int PRIORITY_CALL = 1;
	public static final int PRIORITY_MEMBER_ACCESS = 1;
	public static final int PRIORITY_UNARYPLUSMINUS = 2;
	public static final int PRIORITY_PLUSMINUSPREFIXPOSTFIX = 2;
	public static final int PRIORITY_UNARYLOGICALNOT = 2;
	public static final int PRIORITY_UNARYBITWISENOT = 2;
	public static final int PRIORITY_CAST = 2;
	public static final int PRIORITY_NEWOPERATOR = 2;
	public static final int PRIORITY_MULDIVREM = 3;
	public static final int PRIORITY_ADDSUB = 4;
	public static final int PRIORITY_CONTACTSTRING = 4;
	public static final int PRIORITY_BITSHIFTS = 5;
	public static final int PRIORITY_LELTGEGTINSTANCEOF = 6;
	public static final int PRIORITY_EQNE = 7;
	public static final int PRIORITY_BITAND = 8;
	public static final int PRIORITY_BITXOR = 9;
	public static final int PRIORITY_BITOR = 10;
	public static final int PRIORITY_LOGICALAND = 11;
	public static final int PRIORITY_LOGICALOR = 12;
	public static final int PRIORITY_TERNARY = 13;
	public static final int PRIORITY_ASSIGNMENT = 14;
	
	
    /**
     * Contains parent or null if this expression return is 
     * not used.
     */
    private AbstractCodeNode parent;
    
    /**
     * Get's priority of this expression.
     */
    public int getPriority() {
    	return 0;
    }
    
    /**
     * Return's type of this expression.
     */
    public abstract CS2Type getType();
    
    /**
     * Copies this expression node.
     */
    public abstract ExpressionNode copy();

    public void setParent(AbstractCodeNode parent) {
    	this.parent = parent;
    }

    public AbstractCodeNode getParent() {
    	return parent;
    }
}
