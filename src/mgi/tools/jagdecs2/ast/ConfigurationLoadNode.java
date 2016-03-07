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

public class ConfigurationLoadNode extends ExpressionNode {

    private ConfigInfo info;
    private boolean secondaryDomainRegister;
    
    public ConfigurationLoadNode(ConfigInfo info, boolean secondaryDomainRegister) {
    	this.info = info;
    	this.secondaryDomainRegister = secondaryDomainRegister;
    }
    


    @Override
    public CS2Type getType() {
    	return info.getType();
    }
    
    @Override
    public int getPriority() {
    	return ExpressionNode.PRIORITY_ARRAY_INDEX;
    }

	@Override
	public ExpressionNode copy() {
		return new ConfigurationLoadNode(this.info, this.secondaryDomainRegister);
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		if (secondaryDomainRegister)
			printer.print("$.");
		printer.print(info.getDomainName());
		printer.print('[');
		printer.print(TextUtils.quote(info.getConfigName()).replace('"', '\''));
		printer.print(']');
		printer.endPrinting(this);
	}
	

	public ConfigInfo getInfo() {
		return info;
	}
	
	public boolean isSecondaryDomainRegister() {
		return secondaryDomainRegister;
	}

}
