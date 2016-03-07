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

public class ConditionalExpressionNode extends ExpressionNode {

	/**
	 * Contains left expression node. 
	 */
	private ExpressionNode left;
	/**
	 * Contains right expression node.
	 */
	private ExpressionNode right;
	/**
	 * Contains expression conditional.
	 * 0 means !=
	 * 1 means ==
	 * 2 means >
	 * 3 means <
	 * 4 means >=
	 * 5 means <=
	 * 6 means ||
	 * 7 means &&
	 */
	private int conditional;
	
	public ConditionalExpressionNode(ExpressionNode left,ExpressionNode right,int conditional) {
		this.left = left;
		this.right = right;
		this.conditional = conditional;
		
		this.write(left);
		this.write(right);
		this.left.setParent(this);
		this.right.setParent(this);
	}
	
	@Override
	public int getPriority() {
		if (conditional == 0 || conditional == 1)
			return ExpressionNode.PRIORITY_EQNE;
		else if (conditional < 6)
			return ExpressionNode.PRIORITY_LELTGEGTINSTANCEOF;
		else if (conditional == 6)
			return ExpressionNode.PRIORITY_LOGICALOR;
		else if (conditional == 7)
			return ExpressionNode.PRIORITY_LOGICALAND;
		else
			return super.getPriority();
	}
	
	
	@Override
	public CS2Type getType() {
		return CS2Type.BOOLEAN;
	}

	private String conditionalToString() {
		switch (this.conditional) {
		case 0:
			return "!=";
		case 1:
			return "==";
		case 2:
			return ">";
		case 3:
			return "<";
		case 4:
			return ">=";
		case 5:
			return "<=";
		case 6:
			return "||";
		case 7:
			return "&&";
		default:
			return "??";
		}
	}
	
	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		boolean needsLeftParen = left.getPriority() > this.getPriority();
		boolean needsRightParen = right.getPriority() > this.getPriority();
		if (needsLeftParen)
			printer.print("(");
		this.left.print(printer);
		if (needsLeftParen)
			printer.print(")");	
		printer.print(" " + this.conditionalToString() + " ");
		if (needsRightParen)
			printer.print("(");
		this.right.print(printer);
		if (needsRightParen)
			printer.print(")");
		printer.endPrinting(this);
	}


	@Override
	public ExpressionNode copy() {
		return new ConditionalExpressionNode(left.copy(),right.copy(),conditional);
	}

	public ExpressionNode getLeft() {
		return left;
	}

	public ExpressionNode getRight() {
		return right;
	}
	
	public int getConditional() {
		return conditional;
	}




}
