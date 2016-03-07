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
import mgi.tools.jagdecs2.util.BitConfigInfo;
import mgi.tools.jagdecs2.util.TextUtils;

public class BitConfigurationStoreNode extends ExpressionNode {

    private BitConfigInfo info;
    private boolean secondaryDomainRegister;
    private ExpressionNode expression;
    
    public BitConfigurationStoreNode(BitConfigInfo info, boolean secondaryDomainRegister, ExpressionNode expr) {
    	this.info = info;
    	this.secondaryDomainRegister = secondaryDomainRegister;
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
    
	public BitConfigInfo getInfo() {
		return info;
	}
	
	public boolean isSecondaryDomainRegister() {
		return secondaryDomainRegister;
	}

	public ExpressionNode getExpression() {
		return expression;
	}
    
	@Override
	public ExpressionNode copy() {
		return new BitConfigurationStoreNode(this.info, this.secondaryDomainRegister, this.expression.copy());
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		boolean needsParen = expression.getPriority() > getPriority();
		if (secondaryDomainRegister)
			printer.print("$.");
		printer.print(info.getBase().getDomainName());
		printer.print('[');
		printer.print(TextUtils.quote(info.getBase().getConfigName()).replace('"', '\''));
		printer.print(']');
		printer.print('.');
		printer.print(info.getName());
		printer.print(" = ");
		if (needsParen)
			printer.print('(');
		expression.print(printer);
		if (needsParen)
			printer.print(')');
		printer.endPrinting(this);
	}



}
