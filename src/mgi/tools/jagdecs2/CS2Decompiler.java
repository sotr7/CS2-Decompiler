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


package mgi.tools.jagdecs2;

import java.util.ArrayList;
import java.util.List;

import mgi.tools.jagdecs2.ast.*;
import mgi.tools.jagdecs2.util.ConfigsDatabase;
import mgi.tools.jagdecs2.util.FunctionDatabase;
import mgi.tools.jagdecs2.util.FunctionInfo;
import mgi.tools.jagdecs2.util.InstructionsDatabase;

public class CS2Decompiler {
	
	private final String version = "jagdecs2 v3.5";
	private boolean t1LocalAnalyzer = true;
	private boolean t2LocalAnalyzer = true;
	private boolean beautifier = true;
	
	private List<Integer> decompiling = new ArrayList<Integer>();
	
	
	private InstructionsDatabase instructionsDb;
	private ConfigsDatabase configsDb;
	private FunctionDatabase opcodesDb;
	private FunctionDatabase scriptsDb;
	
	private ICS2Provider provider;
	
	public CS2Decompiler(InstructionsDatabase idb, ConfigsDatabase cdb, FunctionDatabase odb, FunctionDatabase sdb, ICS2Provider provider) {
		this.instructionsDb = idb;
		this.configsDb = cdb;
		this.opcodesDb = odb;
		this.scriptsDb = sdb;
		this.provider = provider;
	}
	
	public void setFlags(boolean t1LocalAnalyzer, boolean t2LocalAnalyzer, boolean beautifier) {
		this.t1LocalAnalyzer = t1LocalAnalyzer;
		this.t2LocalAnalyzer = t2LocalAnalyzer;
		this.beautifier = beautifier;
	}
	

	
	public FunctionNode decompile(int id) throws DecompilerException {
		if (decompiling.contains(id))
			throw new DecompilerException("stuck inside a loop");
		decompiling.add(id);
		System.out.println("Decompile:" + id);
		CS2 cs2 = provider.getCS2(instructionsDb, configsDb, scriptsDb, opcodesDb, id);
		if (cs2 == null)
			throw new DecompilerException("Couldn't find cs2:" + id);
		
		FunctionInfo info = scriptsDb.getInfo(id);
		if (info != null) {
			cs2.setName(info.getName());
			cs2.setReturnType(info.getReturnType());
			CS2Type[] arguments = new CS2Type[info.getArgumentTypes().length];
			String[] names = new String[info.getArgumentNames().length];
			System.arraycopy(info.getArgumentTypes(), 0, arguments, 0, arguments.length);
			System.arraycopy(info.getArgumentNames(), 0, names, 0, names.length);
			cs2.setArgumentTypes(arguments);
			cs2.setArgumentNames(names);
		}
		String[] names = cs2.getArgumentNames();
		if (names == null) {
			names = new String[cs2.getArgumentTypes().length];
			for (int i = 0; i < names.length; i++)
				names[i] = "arg" + i;
		}
		
		FunctionNode function = new FunctionNode(id, cs2.getName() == null ? ("script_" + id) : cs2.getName(),cs2.getArgumentTypes(), names, cs2.getReturnType());
		
		addLogo(function);
		declareAllVariables(cs2, function);
		FlowBlocksGenerator generator = new FlowBlocksGenerator(this, cs2, function);
		generator.generate();
		if (t1LocalAnalyzer) {
			LocalVariablesAnalyzerT1 analyzert1 = new LocalVariablesAnalyzerT1(this, function, generator.getBlocks());
			analyzert1.analyze();
		}
		if (t2LocalAnalyzer) {
			LocalVariablesAnalyzerT2 analyzert2 = new LocalVariablesAnalyzerT2(this, function, generator.getBlocks());
			analyzert2.analyze();
		}
		FlowBlocksSolver solver = new FlowBlocksSolver(this, function.getScope(), generator.getBlocks());
		solver.solve();
		if (beautifier) {
			Beautifier beautifier = new Beautifier(this, function);
			beautifier.beautify();
		}
		
		decompiling.remove((Integer)id);
		return function;
	}


	private void addLogo(FunctionNode function) {
		function.setCodeAddress(0);
		function.write(new CommentNode("" +
			"Script decompiled by " + version() + "\n" +
			"Decompiler opts:\n" +
			"--------------\n" +
			"t1LocalAnalyzer:" + t1LocalAnalyzer() + "\n" +
			"t2LocalAnalyzer:" + t2LocalAnalyzer() + "\n" +
			"beautifier:" + beautifier() + "\n" +
			"--------------\n" +
			"Made by mgi125 ;)", CommentNode.LOGO_STYLE
			//"Copyright © mgi125 2012-2014", CommentNode.LOGO_STYLE	
		));
	}
	
	
	private void declareAllVariables(CS2 cs2, FunctionNode function) {
		
		int ic = 0, oc = 0, lc = 0;		
		for (int i = 0; i < function.getArgumentTypes().length; i++) {
			CS2Type atype = function.getArgumentTypes()[i];
			String aname = function.getArgumentNames()[i];
			if (atype.intSS() == 1 && atype.stringSS() == 0 && atype.longSS() == 0) {
				LocalVariable var = new LocalVariable(aname, atype, true);
				var.setIdentifier(LocalVariable.makeIdentifier(ic++, 0));
				var.setNeedsScopeDeclaration(false);
				function.getScope().declare(function.getArgumentLocals()[i] = var);
			}
			else if (atype.intSS() == 0 && atype.stringSS() == 1 && atype.longSS() == 0) {
				LocalVariable var = new LocalVariable(aname, atype, true);
				var.setIdentifier(LocalVariable.makeIdentifier(oc++, 1));
				var.setNeedsScopeDeclaration(false);
				function.getScope().declare(function.getArgumentLocals()[i] = var);
			}
			else if (atype.intSS() == 0 && atype.stringSS() == 0 && atype.longSS() == 1) {
				LocalVariable var = new LocalVariable(aname, atype, true);
				var.setIdentifier(LocalVariable.makeIdentifier(lc++, 2));
				var.setNeedsScopeDeclaration(false);
				function.getScope().declare(function.getArgumentLocals()[i] = var);
			}
			else {
				throw new RuntimeException("structs in args?");
			}
		}
		if (ic != cs2.getIntArgumentsCount() || oc != cs2.getStringArgumentsCount() || lc != cs2.getLongArgumentsCount())
			throw new RuntimeException("badargs");

		for (int i = cs2.getIntArgumentsCount(); i < cs2.getIntLocalsSize(); i++) {
			LocalVariable var = new LocalVariable("ivar" + i,CS2Type.INT);
			var.setIdentifier(LocalVariable.makeIdentifier(i, 0));
			function.getScope().declare(var);
		}
		for (int i = cs2.getStringArgumentsCount(); i < cs2.getStringLocalsSize(); i++) {
			LocalVariable var = new LocalVariable("svar" + i,CS2Type.STRING);
			var.setIdentifier(LocalVariable.makeIdentifier(i, 1));
			function.getScope().declare(var);
		}
		for (int i = cs2.getLongArgumentsCount(); i < cs2.getLongLocalsSize(); i++) {
			LocalVariable var = new LocalVariable("lvar" + i,CS2Type.LONG);
			var.setIdentifier(LocalVariable.makeIdentifier(i, 2));
			function.getScope().declare(var);
		}
	}	
	
	
	public String version() {
		return version;
	}
	
	public boolean t1LocalAnalyzer() {
		return t1LocalAnalyzer;
	}
	
	public boolean t2LocalAnalyzer() {
		return t2LocalAnalyzer;
	}
	
	public boolean beautifier() {
		return beautifier;
	}
	
	
	public InstructionsDatabase getInstructionsDatabase() {
		return instructionsDb;
	}
	
	public ConfigsDatabase getConfigsDatabase() {
		return configsDb;
	}

	public FunctionDatabase getOpcodesDatabase() {
		return opcodesDb;
	}

	public FunctionDatabase getScriptsDatabase() {
		return scriptsDb;
	}








	

	
	
}
