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
import mgi.tools.jagdecs2.util.ConfigInfo;
import mgi.tools.jagdecs2.util.TextUtils;

public class ConfigurationStoreNode extends ExpressionNode {

    private ConfigInfo info;
    private boolean secondaryDomainRegister;
    private ExpressionNode expression;
    
    public ConfigurationStoreNode(ConfigInfo info, boolean secondaryDomainRegister, ExpressionNode expr) {
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
    
	public ConfigInfo getInfo() {
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
		return new ConfigurationStoreNode(this.info, this.secondaryDomainRegister,this.expression.copy());
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		boolean needsParen = expression.getPriority() > getPriority();
		if (secondaryDomainRegister)
			printer.print("$.");
		printer.print(info.getDomainName());
		printer.print('[');
		printer.print(TextUtils.quote(info.getConfigName()).replace('"', '\''));
		printer.print(']');
		printer.print(" = ");
		if (needsParen)
			printer.print('(');
		expression.print(printer);
		if (needsParen)
			printer.print(')');
		printer.endPrinting(this);
	}



}
