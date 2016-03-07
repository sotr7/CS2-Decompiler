package tests;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import mgi.tools.jagdecs2.CS2Decompiler;
import mgi.tools.jagdecs2.CodePrinter;
import mgi.tools.jagdecs2.ast.FunctionNode;
import mgi.tools.jagdecs2.util.ConfigsDatabase;
import mgi.tools.jagdecs2.util.FunctionDatabase;
import mgi.tools.jagdecs2.util.InstructionsDatabase;

public class DecompileAll {

	
	
	public static void main(String[] args) throws Throwable {
		InstructionsDatabase insDatabase = new InstructionsDatabase(new File("instructions_db.ini"));
		ConfigsDatabase cfgDatabase      = new ConfigsDatabase(new File("configs_db.ini"), new File("bitconfigs_db.ini"));
		FunctionDatabase opcodesDatabase = new FunctionDatabase(new File("opcodes_db.ini"));
		FunctionDatabase scriptsDatabase = new FunctionDatabase(new File("scripts_db.ini"));
		CS2Decompiler decompiler = new CS2Decompiler(insDatabase, cfgDatabase, opcodesDatabase, scriptsDatabase, new TestProvider());

		
		
		List<Integer> scriptsde = new ArrayList<Integer>();
		List<Integer> scriptsfe = new ArrayList<Integer>();
		int count = 12346;
		System.setErr(new PrintStream(new ByteArrayOutputStream()));
		int flowErrors = 0;
		int decompilerErrors = 0;
		for (int i = 0; i < count; i++) {
			if (new File("scripts/" + i).exists()) {
				FunctionNode func = decompiler.decompile(i);
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File("sources/" + i + ".cs2")));
				CodePrinter printer = new CodePrinter();
				func.print(printer);
				String str = printer.toString();
				writer.append(str);
				writer.newLine();
				writer.close();
				boolean flowError = str.contains("flow_");
				boolean decompilerError = str.contains("DecompilerException");
				if (decompilerError)
					scriptsde.add(i);
				if (flowError)
					scriptsfe.add(i);
				System.out.println(i + "/" + (count - 1) + ", flow errors:" + (flowError ? ++flowErrors : flowErrors) + ", decompiler errors:" + (decompilerError ? ++decompilerErrors : decompilerErrors));
			}
			else {
				System.out.println(i + "/" + (count - 1) + ", not existing");
			}
		}
		
		for (Integer i : scriptsde)
			System.out.println("Decompiler error on:" + i);
		
		for (Integer i : scriptsfe)
			System.out.println("Flow error on:" + i);
	}
	
	
}
