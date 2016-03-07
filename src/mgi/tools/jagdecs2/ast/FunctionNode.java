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

public class FunctionNode extends AbstractCodeNode {

	private int id;
	private String name;
	private CS2Type[] argumentTypes;
	private String[] argumentNames;
	private LocalVariable[] argumentLocals;
	private CS2Type returnType;
	private ScopeNode scope;
	

	public FunctionNode(int id, String name,CS2Type[] args, String[] argnames, CS2Type returnType) {
		this.id = id;
		this.name = name;
		this.argumentTypes = args;
		this.argumentNames = argnames;
		this.returnType = returnType;
		this.argumentLocals = new LocalVariable[args.length];
		this.scope = new ScopeNode();
		this.write(scope);
		scope.setParent(this);
	}

	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public CS2Type[] getArgumentTypes() {
		return argumentTypes;
	}
	
	public String[] getArgumentNames() {
		return argumentNames;
	}
	
	public LocalVariable[] getArgumentLocals() {
		return argumentLocals;
	}

	public void setReturnType(CS2Type returnType) {
		this.returnType = returnType;
	}

	public CS2Type getReturnType() {
		return returnType;
	}


	public ScopeNode getScope() {
		return scope;
	}
	

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		int precount = 0;
		for (int i = 0; i < size(); i++) {
			if (read(i) == scope)
				break;
			read(i).print(printer);
			printer.print('\n');
			precount++;
		}
		
		if (precount > 0)
			printer.print("\n\n");
		
		printer.print(this.returnType.toString());
		printer.print(' ');
		printer.print(this.name);
		printer.print('(');
		for (int i = 0; i < argumentLocals.length; i++) {
			printer.print(argumentLocals[i].toString());
			if ((i + 1) < argumentTypes.length)
				printer.print(',');
		}
		printer.print(')');
		printer.print(' ');
		this.scope.print(printer);
		printer.endPrinting(this);
	}


	
}
