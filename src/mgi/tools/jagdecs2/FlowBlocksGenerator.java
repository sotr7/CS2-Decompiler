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
import java.util.Arrays;
import java.util.List;

import mgi.tools.jagdecs2.ast.*;
import mgi.tools.jagdecs2.instructions.*;
import mgi.tools.jagdecs2.util.ArrayQueue;
import mgi.tools.jagdecs2.util.FunctionInfo;
import mgi.tools.jagdecs2.util.IOUtils;
import mgi.tools.jagdecs2.util.OpcodeUtils;

public class FlowBlocksGenerator {

	private CS2Decompiler decompiler;
	private CS2 cs2;
	private FunctionNode function;
	private FlowBlock[] blocks;
	private List<FlowBlock> processedBlocks;
	private int counter;
	
		
	public FlowBlocksGenerator(CS2Decompiler decompiler, CS2 cs2, FunctionNode function) {
		this.processedBlocks = new ArrayList<FlowBlock>();
		this.decompiler = decompiler;
		this.cs2 = cs2;
		this.function = function;
		this.counter = 0;
	}



	public void generate() throws DecompilerException {
		this.initGeneration();
		this.processGeneration();
		this.endGeneration();
	}
	
	private void initGeneration() {
		blocks = new FlowBlock[cs2.countOf(Label.class) + 1];
		blocks[0] = new FlowBlock();
	}
	
	private void processGeneration() {
		int numProcessed;
		do {
			numProcessed = 0;
			for (int i = 0; i < blocks.length; i++)
				if (blocks[i] != null && !processedBlocks.contains(blocks[i])) {
					processedBlocks.add(blocks[i]);
					numProcessed++;
					processFlowBlock(blocks[i]);
				}
		}
		while (numProcessed > 0);
	}
	
	private void processFlowBlock(FlowBlock block) {
		int ptr = block.getStartAddress();
		CS2Stack stack = block.getStack().copy();
		
		try {
			for (;;ptr++) {
				if (ptr >= cs2.getInstructions().length)
					throw new DecompilerException("Error:Code out bounds.");
				AbstractInstruction instruction = cs2.getInstructions()[ptr];
				int opcode = instruction.getOpcode();
				if (instruction instanceof Label) {
					// new flow block
					this.dumpStack(block, stack);
					this.generateFlowBlock((Label)instruction, stack.copy());
					break;
				}
				else if (instruction instanceof JumpInstruction) {
					JumpInstruction jmp = (JumpInstruction)instruction;
					
					if (OpcodeUtils.getTwoConditionsJumpStackType(opcode) != -1) {
						int stackType = OpcodeUtils.getTwoConditionsJumpStackType(opcode);
						CS2Type type = defaultType(stackType);
						ExpressionNode v2 = this.cast(stack.pop(stackType), type);
						ExpressionNode v1 = this.cast(stack.pop(stackType), type);
						ExpressionNode conditional = new ConditionalExpressionNode(v1,v2,OpcodeUtils.getTwoConditionsJumpConditional(opcode));
						this.dumpStack(block, stack);
						FlowBlock target = generateFlowBlock(jmp.getTarget(),stack.copy());
						target.getPredecesors().add(block);
						block.getSucessors().add(target);
						block.write(new ConditionalFlowBlockJump(conditional,target));
					}
					else if (OpcodeUtils.getOneConditionJumpStackType(opcode) != -1) {
						ExpressionNode expr = this.cast(stack.pop(0), CS2Type.BOOLEAN);
						if (opcode == Opcodes.INT_F)
							expr = new NotExpressionNode(expr);
						this.dumpStack(block, stack);
						FlowBlock target = generateFlowBlock(jmp.getTarget(),stack.copy());
						target.getPredecesors().add(block);
						block.getSucessors().add(target);
						block.write(new ConditionalFlowBlockJump(expr,target));
					}
					else {
						this.dumpStack(block, stack);
						FlowBlock target = generateFlowBlock(jmp.getTarget(),stack.copy());
						target.getPredecesors().add(block);
						block.getSucessors().add(target);
						block.write(new UnconditionalFlowBlockJump(target));
						break;
					}
				}
				else if (instruction instanceof StringInstruction) {
					stack.push(new StringExpressionNode(((StringInstruction)instruction).getConstant()),1);
				}
				else if (instruction instanceof LongInstruction) {
					stack.push(new LongExpressionNode(((LongInstruction)instruction).getConstant()),2);
				}
				else if (instruction instanceof ConfigInstruction) {
					ConfigInstruction cfg = (ConfigInstruction)instruction;
					int stackType = cfg.getConfig().getType().intSS() == 0 ? (cfg.getConfig().getType().longSS() != 0 ? 2 : 1) : 0;
					if (opcode == Opcodes.LOAD_CONFIG) {
						stack.push(new ConfigurationLoadNode(cfg.getConfig(), cfg.getConstant()), stackType);
					}
					else if (opcode == Opcodes.STORE_CONFIG) {
						ExpressionNode expr = cast(stack.pop(stackType), cfg.getConfig().getType());
						this.dumpStack(block, stack);
						block.write(new PopableNode(new ConfigurationStoreNode(cfg.getConfig(), cfg.getConstant(), expr)));
					}
					else {
						throw new DecompilerException("Unknown opcode:" + opcode);
					}
				}
				else if (instruction instanceof BitConfigInstruction) {
					BitConfigInstruction cfg = (BitConfigInstruction)instruction;
					int stackType = cfg.getConfig().getBase().getType().intSS() == 0 ? (cfg.getConfig().getBase().getType().longSS() != 0 ? 2 : 1) : 0;
					if (opcode == Opcodes.LOAD_BITCONFIG) {
						stack.push(new BitConfigurationLoadNode(cfg.getConfig(), cfg.getConstant()), stackType);
					}
					else if (opcode == Opcodes.STORE_BITCONFIG) {
						ExpressionNode expr = cast(stack.pop(stackType), cfg.getConfig().getBase().getType());
						this.dumpStack(block, stack);
						block.write(new PopableNode(new BitConfigurationStoreNode(cfg.getConfig(), cfg.getConstant(), expr)));
					}
					else {
						throw new DecompilerException("Unknown opcode:" + opcode);
					}
				}
				else if (instruction instanceof IntInstruction) {
					IntInstruction intInstr = (IntInstruction)instruction;
					if (opcode == Opcodes.PUSH_INT) { // push int.
						stack.push(new IntExpressionNode(intInstr.getConstant()),0);
					}
					else if (opcode == Opcodes.RETURN) {
						if (stack.getSize() <= 0) {
							this.function.setReturnType(CS2Type.merge(this.function.getReturnType(), CS2Type.VOID));
							block.write(new ReturnNode());
						} else if (stack.getSize() == 1) {
							this.function.setReturnType(CS2Type.merge(this.function.getReturnType(), stack.peek().getType()));
							block.write(new ReturnNode(stack.pop()));
						}
						else {
							CS2Type[] types = new CS2Type[stack.getSize()];
							ExpressionNode[] args = new ExpressionNode[stack.getSize()];
							for (int i = args.length - 1; i >= 0; i--) {
								ExpressionNode expr = stack.pop();
								args[i] = cast(expr, expr.getType());
								types[i] = args[i].getType();
							}
							
							CS2Type struct = CS2Type.makeAdvancedStruct(this.function.getName() + "_struct", false, types);
							this.function.setReturnType(CS2Type.merge(this.function.getReturnType(), struct));
							block.write(new ReturnNode(new StructConstructExpr(struct, args)));
						}
						break;
					}
					else if (opcode == Opcodes.LOAD_INT || opcode == Opcodes.LOAD_STR || opcode == Opcodes.LOAD_LONG) {
						int stackType = opcode != Opcodes.LOAD_INT ? (opcode == Opcodes.LOAD_LONG ? 2 : 1) : 0;
						LocalVariable var = function.getScope().getLocalVariable(LocalVariable.makeIdentifier(intInstr.getConstant(), stackType));
						stack.push(new VariableLoadNode(var), stackType);
					}
					else if (opcode == Opcodes.STORE_INT || opcode == Opcodes.STORE_STR || opcode == Opcodes.STORE_LONG) {
						int stackType = opcode != Opcodes.STORE_INT ? (opcode == Opcodes.STORE_LONG ? 2 : 1) : 0;
						LocalVariable var = function.getScope().getLocalVariable(LocalVariable.makeIdentifier(intInstr.getConstant(), stackType));
						ExpressionNode expr = cast(stack.pop(stackType),var.getType());
						this.dumpStack(block, stack);
						block.write(new PopableNode(new VariableAssignationNode(var,expr)));
					}
					else if (opcode == Opcodes.CONCAT_STRINGS) {
						int amount = intInstr.getConstant();
						ExpressionNode[] exprs = new ExpressionNode[amount];
						for (int i = amount - 1; i >= 0; i--)
							exprs[i] = cast(stack.pop(1),CS2Type.STRING);
						stack.push(new BuildStringNode(exprs), 1);
					}
					else if (opcode == Opcodes.POP_INT || opcode == Opcodes.POP_STR || opcode == Opcodes.POP_LONG) {
						ExpressionNode expr = stack.pop(opcode == Opcodes.POP_LONG ? 2 : (opcode == Opcodes.POP_STR ? 1 : 0));
						this.dumpStack(block, stack);
						block.write(new PopableNode(expr));
					}
					else if (opcode == Opcodes.CALL_CS2) {
						FunctionInfo info = decompiler.getScriptsDatabase().getInfo(intInstr.getConstant());
						if (info == null)
							throw new DecompilerException("No documentation for:" + instruction);
						int ret = this.analyzeCall(info, block, stack, ptr);
						if (ret != -1)
							ptr = ret;
					}
					else if (opcode == Opcodes.NEW_ARRAY) {
						int arrayID = intInstr.getConstant() >> 16;
						char type = (char)(intInstr.getConstant() & 0xFFFF);
						CS2Type array = CS2Type.forJagexChar(type).getArrayType();
						ExpressionNode length = cast(stack.pop(0),CS2Type.INT);
						this.dumpStack(block, stack);
						block.write(new PopableNode(new StoreNamedDataNode("globalarray_" + arrayID, new NewArrayNode(length,array))));
					}
					else if (opcode == Opcodes.ARRAY_LOAD) {
						stack.push(new ArrayLoadNode(new LoadNamedDataNode("globalarray_" + intInstr.getConstant(),CS2Type.INT.getArrayType()),cast(stack.pop(0),CS2Type.INT)), 0);
					}
					else if (opcode == Opcodes.ARRAY_STORE) {
						int arrayID = intInstr.getConstant() >> 16;
						ExpressionNode value = cast(stack.pop(0), CS2Type.INT);
						ExpressionNode index = cast(stack.pop(0), CS2Type.INT);
						this.dumpStack(block, stack);
						block.write(new PopableNode(new ArrayStoreNode(new LoadNamedDataNode("globalarray_" + arrayID,CS2Type.INT.getArrayType()),index,value)));
					}
					else {
						throw new DecompilerException("Unknown opcode:" + opcode);
					}
				}
				else if (instruction instanceof BooleanInstruction) {
					FunctionInfo info = decompiler.getOpcodesDatabase().getInfo(instruction.getOpcode());
					if (info == null)
						throw new DecompilerException("No documentation for:" + instruction);
					if (instruction.getOpcode() >= 20000 && instruction.getOpcode() < 30000)
						info = this.analyzeDelegate(info, block, stack.copy());
					else if (instruction.getOpcode() >= 30000 && instruction.getOpcode() < 40000)
						info = this.analyzeSpecialCall(instruction, info, block, stack.copy());
					else if (instruction.getOpcode() >= 40000)
						throw new DecompilerException("A call to disabled function: " + info);
					int ret = this.analyzeCall(info, block, stack, ptr);
					if (ret != -1)
						ptr = ret;
				}
				else if (instruction instanceof SwitchInstruction) {
					SwitchInstruction sw = (SwitchInstruction)instruction;
					ExpressionNode value = cast(stack.pop(0), CS2Type.INT);
					this.dumpStack(block, stack);
					int[] cases = sw.getCases();
					FlowBlock[] targets = new FlowBlock[cases.length];
					for (int i = 0; i < targets.length; i++) {
						targets[i] = generateFlowBlock(sw.getTargets()[i], stack.copy());
						targets[i].getPredecesors().add(block);
						block.getSucessors().add(targets[i]);
					}
					
					block.write(new SwitchFlowBlockJump(value, cases, targets, sw.getDefaultIndex()));
					break;
				}
				else
					throw new DecompilerException("Error:Unknown instruction type:" + instruction.getClass().getName());
			}
		}
		catch (DecompilerException ex) {
			this.dumpStack(block, stack);
			block.write(new CommentNode("AT " + cs2.getInstructions()[ptr] + "\n" + IOUtils.getStackTrace(ex), CommentNode.STANDART_STYLE));
		}
		catch (RuntimeException ex) {
			this.dumpStack(block, stack);
			block.write(new CommentNode("AT " + cs2.getInstructions()[ptr] + "\n" + IOUtils.getStackTrace(ex), CommentNode.STANDART_STYLE));
		}
	}


	private int analyzeCall(FunctionInfo info, FlowBlock block, CS2Stack stack, int ptr) {
		CS2Type returnType = info.getReturnType();
		for (int i = 0; i < info.getArgumentTypes().length; i++)
			if (!info.getArgumentTypes()[i].usable() || info.getArgumentTypes()[i].structure() || info.getArgumentTypes()[i].totalSS() > 1)
				throw new DecompilerException(returnType + " is not supported in function arguments");
		
		if (!returnType.usable())
			throw new DecompilerException(returnType + " is not supported in function return type");
		
		if (returnType.totalSS() <= 1) {
			ExpressionNode[] args = new ExpressionNode[info.getArgumentTypes().length];
			for (int i = args.length - 1; i >= 0; i--) {
				CS2Type type = info.getArgumentTypes()[i];
				args[i] = cast(stack.pop(type.intSS() == 0 ? (type.longSS() != 0 ? 2 : 1) : 0),type);
			}
			
			if (returnType.totalSS() <= 0) { // void				
				this.dumpStack(block, stack);
				block.write(new PopableNode(new CallExpressionNode(info,args)));
			}
			else { // standart
				int stackType = returnType.intSS() == 0 ? (returnType.longSS() != 0 ? 2 : 1) : 0;
				stack.push(new CallExpressionNode(info,args), stackType);
			}
			return -1;
		}
		else {
			this.dumpStack(block, stack);
			ExpressionNode[] args = new ExpressionNode[info.getArgumentTypes().length];
			for (int i = args.length - 1; i >= 0; i--) {
				CS2Type type = info.getArgumentTypes()[i];
				args[i] = cast(stack.pop(type.intSS() == 0 ? (type.longSS() != 0 ? 2 : 1) : 0),type);
			}
			
			ExpressionNode expr = new CallExpressionNode(info,args);
			LocalVariable dump = new LocalVariable("dmp_" + counter++,expr.getType());
			function.getScope().declare(dump);
			block.write(new PopableNode(new VariableAssignationNode(dump,expr)));
			
			VariableAssignationNode[] assignations = new VariableAssignationNode[returnType.totalSS()];
			int intsLeft = returnType.intSS();
			int stringsLeft = returnType.stringSS();
			int longsLeft = returnType.longSS();
			
			int readptr;
			int write;
			for (write = 0, readptr = ptr + 1; (intsLeft + stringsLeft + longsLeft) > 0; readptr++) {
				if (readptr >= cs2.getInstructions().length || !(cs2.getInstructions()[readptr] instanceof IntInstruction))
					break;
				IntInstruction intInstr = (IntInstruction)cs2.getInstructions()[readptr];
				if (cs2.getInstructions()[readptr].getOpcode() == Opcodes.STORE_INT) {
					if (intsLeft <= 0)
						break;
					assignations[write++] = new VariableAssignationNode(function.getScope().getLocalVariable(LocalVariable.makeIdentifier(intInstr.getConstant(), 0)),
							new StructPartLoadNode("ip_" + (--intsLeft),CS2Type.INT,new VariableLoadNode(dump)));
				}
				else if (cs2.getInstructions()[readptr].getOpcode() == Opcodes.STORE_STR) {
					if (stringsLeft <= 0)
						break;
					assignations[write++] = new VariableAssignationNode(function.getScope().getLocalVariable(LocalVariable.makeIdentifier(intInstr.getConstant(), 1)),
							new StructPartLoadNode("sp_" + (--stringsLeft),CS2Type.STRING,new VariableLoadNode(dump)));
				}
				else if (cs2.getInstructions()[readptr].getOpcode() == Opcodes.STORE_LONG) {
					if (longsLeft <= 0)
						break;
					assignations[write++] = new VariableAssignationNode(function.getScope().getLocalVariable(LocalVariable.makeIdentifier(intInstr.getConstant(), 2)),
							new StructPartLoadNode("lp_" + (--longsLeft),CS2Type.LONG,new VariableLoadNode(dump)));
				}
				if ((intsLeft + stringsLeft + longsLeft) <= 0)
					break;
			}
				
			if ((intsLeft + stringsLeft + longsLeft) <= 0) {
				for (int i = 0; i < assignations.length; i++)
					block.write(new PopableNode(assignations[i]));
				return readptr;
			}
			else {
				for (int i = 0; i < expr.getType().intSS(); i++) {
					stack.push(new StructPartLoadNode("ip_" + i,CS2Type.INT,new VariableLoadNode(dump)), 0);
				}
				for (int i = 0; i < expr.getType().stringSS(); i++) {
					stack.push(new StructPartLoadNode("sp_" + i,CS2Type.STRING,new VariableLoadNode(dump)), 1);
				}
				for (int i = 0; i < expr.getType().longSS(); i++) {
					stack.push(new StructPartLoadNode("lp_" + i,CS2Type.LONG,new VariableLoadNode(dump)), 2);
				}
				return -1;
			}
		}
	}
	
	private FunctionInfo analyzeDelegate(FunctionInfo info, FlowBlock block, CS2Stack stack) {		
		String name = info.getName();
		CS2Type[] argsBuff = new CS2Type[100];
		int argsCount = 0;
		
		if (info.getArgumentTypes().length > 1) {
			stack.pop(0);
			argsBuff[argsCount++] = CS2Type.INTERFACE;
		}
			
		ExpressionNode stringExpr = stack.pop(1);
		argsBuff[argsCount++] = CS2Type.STRING;
		if (!(stringExpr instanceof StringExpressionNode))
			throw new DecompilerException("Dynamic delegate - impossible to decompile.");
		String descriptor = ((StringExpressionNode)stringExpr).getData();
		if (descriptor.length() > 0 && descriptor.charAt(descriptor.length() - 1) == 'Y') {
			ExpressionNode length = stack.pop(0);
			if (!(length instanceof IntExpressionNode))
				throw new DecompilerException("Dynamic delegate - impossible to decompile.");
			int len = ((IntExpressionNode)length).getData();
			argsBuff[argsCount++] = CS2Type.INT;
			while (len-- > 0) {
				stack.pop(0);
				argsBuff[argsCount++] = CS2Type.INT;
			}
			descriptor = descriptor.substring(0, descriptor.length() - 1);
		}
		for (int argument = descriptor.length() - 1; argument >= 0; argument--) {
			CS2Type type = CS2Type.forJagexChar(descriptor.charAt(argument));
			CS2Type basic = CS2Type.INT;
			if (descriptor.charAt(argument) == 's')
				basic = CS2Type.STRING;
			else if (descriptor.charAt(argument) == 'l')
				basic = CS2Type.LONG;
			
			if (basicType(type) != basic)
				type = basic;
			
			stack.pop(basic == CS2Type.LONG ? 2 : (basic == CS2Type.STRING ? 1 : 0));
			argsBuff[argsCount++] = type;
		}
		stack.pop(0);
		argsBuff[argsCount++] = CS2Type.FUNCTION;
	
		CS2Type[] args = new CS2Type[argsCount];
		String[] names = new String[argsCount];
		int write = args.length - 1;
		for (int i = 0; i < argsCount; i++) {
			args[write--] = argsBuff[i];
			names[i] = "arg" + i;
		}
		return new FunctionInfo(name, args, CS2Type.VOID, names);
		
	}
	
	private int[] paramtypes = { 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 105, 0, 0, 105, 0, 0, 0, 0, 0, 0, 121, 0, 105, 0, 105, 105, 0, 0, 105, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 103, 0, 0, 0, 105, 0, 0, 105, 0, 0, 0, 0, 0, 110, 111, 0, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 111, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 171, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 100, 105, 105, 100, 100, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 73, 73, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 105, 111, 105, 111, 105, 111, 105, 111, 105, 111, 105, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 103, 111, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 0, 0, 111, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 0, 0, 105, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 105, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 115, 115, 111, 111, 111, 111, 0, 171, 0, 105, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 74, 109, 65, 105, 105, 105, 105, 105, 111, 111, 111, 111, 111, 0, 0, 100, 115, 105, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 8364, 105, 100, 100, 100, 100, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 105, 105, 115, 100, 105, 105, 0, 0, 0, 0, 73, 73, 74, 103, 103, 103, 103, 105, 105, 105, 0, 0, 105, 103, 105, 105, 74, 105, 105, 105, 115, 110, 0, 105, 105, 99, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 108, 108, 0, 0, 0, 0, 0, 115, 115, 115, 115, 115, 0, 0, 0, 0, 171, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 100, 100, 105, 115, 49, 105, 105, 105, 105, 105, 105, 105, 105, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 103, 0, 0, 0, 0, 0, 75, 105, 75, 75, 115, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 0, 105, 105, 0, 0, 0, 0, 111, 0, 111, 111, 0, 0, 0, 0, 0, 0, 105, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 105, 105, 115, 99, 99, 99, 99, 99, 105, 105, 115, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 74, 115, 115, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 109, 105, 109, 65, 105, 115, 109, 105, 65, 0, 0, 0, 115, 115, 115, 115, 100, 0, 105, 105, 110, 110, 105, 0, 0, 0, 0, 105, 0, 105, 0, 105, 105, 105, 0, 0, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 105, 105, 83, 105, 83, 105, 115, 115, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 111, 49, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 115, 115, 105, 65, 105, 105, 105, 80, 105, 100, 105, 115, 115, 115, 100, 105, 105, 116, 64, 108, 108, 115, 100, 100, 118, 65, 65, 109, 65, 65, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 100, 75, 75, 75, 75, 75, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 115, 115, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 111, 111, 111, 111, 115, 115, 115, 105, 105, 105, 105, 100, 105, 115, 115, 115, 115, 115, 115, 115, 115, 115, 99, 99, 99, 99, 99, 99, 99, 99, 105, 115, 115, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 115, 100, 0, 0, 0, 0, 0, 105, 105, 105, 0, 105, 111, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 105, 105, 105, 110, 115, 115, 105, 105, 105, 105, 100, 105, 105, 100, 105, 65, 74, 74, 74, 74, 0, 0, 105, 115, 115, 0, 0, 0, 0, 0, 105, 105, 105, 0, 0, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 74, 74, 74, 100, 100, 100, 100, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 100, 100, 105, 105, 105, 105, 105, 115, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 105, 111, 111, 111, 111, 115, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 100, 105, 0, 0, 0, 103, 103, 103, 0, 105, 105, 105, 105, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 74, 74, 74, 74, 74, 74, 74, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 100, 115, 105, 105, 105, 115, 105, 100, 105, 115, 105, 100, 105, 115, 105, 100, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 99, 99, 105, 105, 105, 99, 99, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 99, 105, 105, 99, 105, 105, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 103, 103, 103, 103, 103, 103, 103, 103, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 105, 105, 105, 99, 99, 99, 99, 99, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 0, 105, 105, 0, 105, 105, 115, 115, 100, 105, 100, 172, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 115, 115, 105, 115, 115, 105, 115, 115, 105, 115, 115, 110, 110, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 105, 105, 49, 111, 105, 105, 111, 111, 111, 111, 0, 0, 0, 0, 74, 74, 74, 74, 74, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 115, 115, 105, 105, 108, 108, 108, 108, 108, 100, 0, 115, 115, 115, 49, 111, 111, 111, 111, 105, 115, 115, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 0, 0, 0, 115, 105, 105, 65, 105, 116, 100, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 171, 171, 171, 171, 171, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 115, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 105, 111, 100, 105, 115, 49, 49, 49, 105, 105, 105, 105, 105, 115, 105, 105, 105, 105, 105, 99, 105, 105, 0, 105, 105, 105, 105, 105, 0, 111, 111, 111, 111, 111, 111, 0, 0, 0, 0, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 0, 0, 105, 111, 105, 105, 105, 105, 105, 105, 111, 83, 105, 105, 111, 111, 111, 111, 111, 111, 74, 111, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 108, 108, 108, 105, 105, 105, 105, 115, 105, 105, 105, 115, 115, 105, 115, 115, 115, 0, 115, 115, 105, 105, 115, 111, 111, 109, 105, 105, 105, 105, 105, 105, 105, 105, 115, 105, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 0, 74, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 115, 0, 115, 105, 105, 105, 105, 105, 105, 105, 105, 115, 100, 65, 65, 109, 65, 109, 105, 105, 103, 105, 0, 0, 105, 49, 115, 105, 0, 115, 0, 0, 0, 0, 0, 100, 105, 115, 115, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 109, 109, 109, 109, 109, 109, 109, 109, 109, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 105, 105, 115, 0, 0, 49, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 111, 111, 111, 105, 105, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 105, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 105, 105, 105, 105, 99, 105, 105, 105, 103, 74, 111, 105, 111, 105, 111, 105, 111, 105, 111, 105, 111, 111, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 105, 115, 115, 105, 105, 105, 105, 105, 105, 100, 64, 105, 105, 105, 105, 115, 105, 0, 105, 105, 105, 105, 105, 74, 103, 0, 105, 109, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 65, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 65, 103, 65, 65, 65, 65, 116, 103, 171, 171, 171, 171, 171, 171, 171, 171, 171, 171, 80, 116, 80, 171, 171, 171, 171, 171, 116, 99, 105, 105, 105, 105, 105, 105, 0, 0, 0, 105, 105, 105, 8364, 8364, 0, 0, 0, 0, 0, 105, 74, 105, 74, 105, 74, 105, 74, 0, 105, 0, 105, 0, 0, 105, 105, 74, 105, 0, 105, 111, 111, 111, 111, 105, 115, 105, 116, 111, 74, 74, 74, 105, 105, 105, 49, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 49, 100, 103, 105, 105, 49, 0, 115, 115, 115, 115, 115, 164, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 109, 109, 115, 105, 105, 100, 105, 105, 105, 105, 105, 105, 105, 105, 105, 115, 115, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 105, 0, 0, 0, 115, 105, 105, 105, 105, 105, 105, 115, 100, 105, 105, 105, 115, 108, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 109, 110, 109, 110, 109, 110, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 74, 105, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 49, 111, 105, 105, 105, 105, 105, 105, 105, 105, 105, 116, 105, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 105, 105, 100, 115, 115, 115, 115, 115, 115, 115, 105, 65, 116, 116, 105, 115, 74, 0, 0, 99, 99, 115, 105, 111, 100, 105, 105, 0, 116, 0, 105, 0, 105, 116, 105, 0, 0, 105, 105, 105, 105, 105, 105, 105, 99, 99, 105, 105, 105, 103, 99, 110, 105, 74, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 0, 65, 105, 105, 105, 111, 105, 105, 105, 0, 105, 0, 0, 0, 0, 105, 0, 105, 0, 0, 0, 111, 111, 111, 111, 111, 111, 105, 115, 105, 105, 105, 105, 105, 0, 0, 0, 0, 100, 100, 111, 115, 115, 115, 115, 115, 105, 105, 105, 105, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 74, 115, 83, 105, 100, 105, 76, 100, 100, 100, 74, 74, 74, 74, 74, 74, 74, 115, 76, 105, 105, 105, 105, 76, 105, 105, 105, 105, 76, 105, 105, 105, 105, 76, 105, 105, 105, 105, 76, 105, 105, 105, 105, 103, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 115, 74, 100, 100, 100, 74, 105, 105, 105, 105, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 74, 49, 105, 49, 49, 49, 49, 49, 49, 105, 49, 49, 49, 49, 105, 49, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 49, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 0, 105, 105, 115, 105, 105, 105, 105, 105, 105, 105, 105, 105, 65, 103, 105, 0, 105, 105, 107, 101, 49, 0, 105, 105, 0, 0, 0, 115, 105, 111, 100, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 105, 100, 100, 100, 100, 0, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 65, 65, 109, 65, 109, 105, 111, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 105, 74, 74, 105, 105, 100, 100, 100, 100, 100, 100, 100, 100, 100, 105, 105, 105, 102, 105, 105, 0, 105, 115, 105, 0, 118, 73, 49, 103, 105, 102, 115, 115, 105, 115, 100, 111, 105, 100, 105, 115, 115, 105, 115, 110, 103, 103, 105, 105, 83, 83, 83, 83, 83, 105, 105, 105, 105, 105, 111, 111, 111, 111, 111, 105, 105, 105, 105, 105, 115, 105, 105, 105, 105, 99, 115, 115, 105, 99, 99, 99, 99, 99, 99, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 100, 100, 100, 100, 100, 100, 100, 100, 100, 115, 115, 115, 115, 115, 115, 115, 100, 100, 100, 100, 100, 100, 105, 105, 0, 0, 0, 105, 105, 0, 0, 0, 0, 99, 0, 0, 0, 0, 111, 111, 111, 111, 111, 111, 105, 115, 105, 105, 105, 105, 105, 0, 0, 0, 0, 100, 100, 111, 115, 115, 105, 105, 105, 105, 115, 105, 105, 105, 105, 115, 105, 105, 105, 105, 115, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 0, 115, 0, 115, 105, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 115, 105, 111, 0, 0, 105, 0, 0, 0, 111, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 100, 100, 105, 105, 105, 100, 100, 100, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 105, 105, 105, 100, 100, 105, 105, 115, 105, 115, 105, 74, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 111, 0, 105, 105, 105, 105, 105, 105, 0, 0, 111, 115, 0, 105, 105, 115, 49, 49, 0, 0, 0, 0, 110, 49, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 110, 105, 103, 103, 100, 115, 105, 49, 105, 100, 100, 100, 105, 105, 105, 102, 105, 74, 105, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 105, 105, 74, 105, 99, 115, 105, 0, 0, 105, 100, 105, 105, 105, 105, 100, 100, 73, 73, 73, 73, 73, 73, 73, 105, 0, 0, 0, 0, 65, 65, 65, 116, 116, 116, 116, 116, 0, 0, 116, 116, 116, 105, 105, 115, 115, 0, 0, 0, 0, 115, 105, 0, 115, 105, 105, 105, 105, 102, 105, 105, 105, 115, 115, 100, 100, 105, 105, 105, 105, 65, 116, 65, 116, 116, 105, 0, 0, 0, 103, 83, 83, 83, 83, 105, 105, 105, 105, 111, 111, 111, 111, 105, 105, 105, 105, 105, 105, 83, 103, 105, 105, 0, 105, 105, 0, 0, 74, 105, 100, 105, 115, 105, 115, 115, 99, 115, 105, 115, 115, 65, 0, 105, 105, 116, 8364, 105, 105, 105, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 0, 105, 0, 0, 65, 109, 65, 109, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 171, 171, 115, 115, 115, 115, 105, 0, 105, 105, 105, 105, 105, 115, 115, 111, 111, 105, 111, 105, 116, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8364, 8364, 65, 65, 65, 65, 65, 65, 116, 0, 103, 100, 100, 100, 105, 74, 74, 74, 74, 74, 74, 74, 74, 105, 105, 100, 100, 100, 49, 115, 115, 74, 115, 100, 115, 115, 74, 105, 105, 105, 105, 105, 74, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 105, 105, 0, 100, 100, 100, 100, 100, 100, 100, 100, 105, 105, 105, 105, 105, 105, 105, 105, 100, 100, 100, 100, 73, 115, 0, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 73, 111, 0, 0, 83, 105, 111, 0, 0, 105, 105, 49, 105, 105, 105, 105, 105, 115, 99, 49, 103, 103, 103, 49, 49, 49, 49, 49, 73, 103, 105, 105, 105, 105, 49, 105, 105, 102, 105, 105, 105, 105, 105, 105, 102, 105, 105, 74, 102, 105, 105, 115, 105, 111, 111, 105, 105, 74, 74, 74, 0, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 74, 0, 105, 105, 0, 0, 105, 0, 0, 116, 116, 116, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 0, 0, 115, 115, 105, 105, 115, 103, 0, 0, 105, 0, 105, 0, 0, 103, 105, 115, 105, 105, 105, 110, 110, 115, 115, 115, 103, 74, 74, 74, 74, 115, 105, 115, 0, 105, 99, 105, 105, 105, 49, 99, 99, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 100, 100, 100, 100, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 49, 105, 105, 105, 105, 105, 105, 105, 105, 0, 105, 0, 0, 105, 105, 0, 0, 111, 105, 111, 105, 0, 111, 0, 0, 111, 99, 99, 110, 121, 111, 111, 105, 0, 0, 0, 105, 110, 109, 8364, 105, 0, 0, 0, 0, 0, 0, 0, 0, 111, 111, 111, 111, 111, 105, 105, 105, 105, 105, 105, 0, 0, 103, 110, 105, 115, 105, 115, 65, 65, 65, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 74, 0, 0, 105, 115, 105, 105, 115, 115, 115, 115, 115, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 105, 105, 105, 105, 105, 105, 0, 0, 0, 105, 74, 105, 0, 74, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 49, 0, 0, 0, 0, 0, 0, 105, 0, 111, 105, 0, 105, 105, 111, 105, 74, 105, 103, 115, 115, 110, 65, 65, 109, 0, 0, 105, 0, 103, 115, 115, 115, 115, 115, 115, 115, 105, 111, 100, 105, 105, 105, 105, 105, 115, 115, 105, 105, 105, 0, 0, 0, 111, 105, 105, 99, 99, 105, 111, 105, 111, 105, 111, 105, 105, 105, 0, 105, 105, 0, 115, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 105, 115, 105, 105, 105, 105, 105, 105, 105, 105, 49, 115, 115, 105, 49, 115, 105, 115, 115, 115, 115, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 0, 105, 115, 115, 105, 105, 105, 105, 115, 83, 83, 83, 83, 83, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 103, 115, 115, 115, 115, 115, 115, 115, 103, 103, 103, 103, 105, 105, 105, 105, 65, 65, 65, 115, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 105, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 73, 105, 105, 105, 100, 115, 105, 115, 105, 111, 105, 105, 115, 111, 111, 111, 111, 111, 111, 83, 105, 105, 105, 0, 0, 105, 105, 105, 105, 74, 105, 105, 105, 105, 105, 105, 115, 115, 100, 100, 105, 105, 0, 0, 115, 111, 105, 105, 115, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 0, 105, 105, 115, 115, 115, 105, 105, 115, 100, 0, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 103, 111, 0, 0, 105, 49, 103, 103, 105, 0, 105, 0, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 105, 111, 105, 0, 0, 0, 0, 0, 74, 105, 0, 0, 0, 100, 0, 74, 0, 105, 111, 105, 111, 105, 111, 105, 111, 105, 0, 115, 105, 76, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 110, 105, 108, 105, 111, 105, 65, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 111, 105, 105, 105, 115, 105, 115, 105, 105, 105, 0, 0, 0, 0, 105, 105, 115, 115, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 111, 115, 105, 105, 105, 115, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 105, 105, 49, 49, 105, 0, 0, 65, 0, 0, 171, 105, 0, 0, 105, 105, 49, 0, 105, 0, 103, 103, 103, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 0, 105, 0, 115, 105, 105, 105, 105, 105, 111, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 111, 111, 111, 111, 115, 115, 115, 115, 100, 115, 115, 115, 105, 105, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 105, 115, 82, 73, 0, 0, 0, 0, 0, 99, 99, 99, 105, 108, 105, 0, 105, 105, 105, 105, 105, 105, 105, 0, 105, 115, 105, 105, 208, 105, 105, 0, 0, 105, 105, 111, 111, 111, 111, 111, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 49, 49, 49, 49, 49, 111, 111, 111, 111, 111, 105, 105, 105, 105, 105, 100, 0, 100, 0, 0, 0, 0, 105, 105, 0, 0, 0, 105, 0, 111, 111, 111, 111, 111, 83, 105, 83, 105, 83, 105, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 111, 105, 208, 208, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 108, 115, 115, 109, 111, 105, 105, 0, 0, 0, 105, 105, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 105, 105, 105, 105, 115, 0, 105 };
	//private int[][][] db1 = { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, { 0, }, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, { 0, }, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, { 17, 0, }, null, { 0, }, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, { 0, }, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, { 0, }, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, null, { { 0, }, { 36, }, null, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, null, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, { 0, }, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, { 0, }, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, { 0, }, null, null, null, null, } };
	private int[][][] db2 = { null, null, null, null, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, { 0, }, { 17, 0, }, { 0, }, { 0, }, { 0, }, }, null, null, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, } };
	
	private FunctionInfo analyzeSpecialCall(AbstractInstruction instruction, FunctionInfo info, FlowBlock block, CS2Stack stack) {	
		if (instruction.getName().equals("_db_getfield")) {
			ExpressionNode[] args = new ExpressionNode[3];
			for (int i = 2; i >= 0; i--)
				args[i] = stack.pop(0);
			
			if (!(args[1] instanceof IntExpressionNode))
				throw new DecompilerException("Dynamic type");
			
			int target = ((IntExpressionNode)args[1]).getData();
			
			int t1 = target >>> 8;
			int t2 = target & 0xFF;
			
			if (t1 < 0 || t1 >= db2.length || db2[t1] == null)
				throw new DecompilerException("Invalid type");
			
			if (t2 < 0 || t2 >= db2[t1].length || db2[t1][t2] == null)
				throw new DecompilerException("Invalid type");
			
			CS2Type[] rtypes = new CS2Type[db2[t1][t2].length];
			for (int i = 0; i < rtypes.length; i++)
				rtypes[i] = CS2Type.forJagexId(db2[t1][t2][i]);
			
			if (rtypes.length == 0)
				return new FunctionInfo(info.getName(), info.getArgumentTypes(), CS2Type.VOID, info.getArgumentNames());
			else if (rtypes.length == 1)
				return new FunctionInfo(info.getName(), info.getArgumentTypes(), rtypes[0], info.getArgumentNames());
			else {
				String s = "(";
				for (int i = 0; i < rtypes.length; i++)
					s += rtypes[i].toString() + ((i+1)<rtypes.length?";":"");
				s += ")";
				return new FunctionInfo(info.getName(), info.getArgumentTypes(), CS2Type.forDesc("dbfield" + s), info.getArgumentNames());
			}
		}
		else if (instruction.getName().equals("_enum")) { // enum
			ExpressionNode[] args = new ExpressionNode[4];
			for (int i = 3; i >= 0; i--)
				args[i] = stack.pop(0);
			if (!(args[0] instanceof IntExpressionNode) || !(args[1] instanceof IntExpressionNode))
				throw new DecompilerException("Dynamic type");
			CS2Type[] atypes = Arrays.copyOf(info.getArgumentTypes(), info.getArgumentTypes().length);
			atypes[3] = CS2Type.forJagexId(((IntExpressionNode)args[0]).getData());
			return new FunctionInfo(info.getName(), atypes, CS2Type.forJagexId(((IntExpressionNode)args[1]).getData()), info.getArgumentNames());
		}
		else if (instruction.getName().equals("_random_sound_pitch")) { // random_pitch_sound
			ExpressionNode[] args = new ExpressionNode[2];
			for (int i = 1; i >= 0; i--)
				args[i] = stack.pop(0);
			if (!(args[0] instanceof IntExpressionNode) || !(args[1] instanceof IntExpressionNode))
				throw new DecompilerException("Dynamic type");
			if (((IntExpressionNode)args[0]).getData() > 700 || ((IntExpressionNode)args[1]).getData() > 700)
				return new FunctionInfo(info.getName(), info.getArgumentTypes(), CS2Type.forDesc("random_pitch_sound(2,0,0)"), info.getArgumentNames());
			else
				return new FunctionInfo(info.getName(), info.getArgumentTypes(), CS2Type.INT, info.getArgumentNames());
		}
		else if (instruction.getName().contains("_param")) {
			ExpressionNode arg = stack.pop(0);
			if (!(arg instanceof IntExpressionNode))
				throw new DecompilerException("Dynamic type");
			
			int paramId = ((IntExpressionNode)arg).getData();
			if (paramId < 0 || paramId >= paramtypes.length)
				throw new DecompilerException("unknown param id: " + paramId);
			
			return new FunctionInfo(info.getName(), info.getArgumentTypes(), CS2Type.forJagexChar((char)paramtypes[paramId]), info.getArgumentNames());
		}
		else if (instruction.getName().equals("_runjavascript")) {
			// TODO we don't know the return type though
			CS2Type rtype = CS2Type.VOID;
			CS2Type[] argtypes = new CS2Type[stack.getSize()];
			String[] argnames = new String[stack.getSize()];
			for (int i = argtypes.length - 1; i >= 0; i--) {
				argtypes[i] = stack.pop().getType();
				argnames[i] = "arg" + i;
			}
			
			return new FunctionInfo(info.getName(), argtypes, rtype, argnames);
		}
		else
			throw new DecompilerException("TODO unimplemented special instruction: " + instruction.getOpcode() + ", name=" + instruction.getName());
	}
	
	
	private void endGeneration() {
		List<FlowBlock> validBlocks = new ArrayList<FlowBlock>();
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null)
				validBlocks.add(blocks[i]);
		blocks = new FlowBlock[validBlocks.size()];
		int write = 0;
		for (FlowBlock block : validBlocks)
			blocks[write++] = block;
		for (int i = 1; i < blocks.length; i++) {
			blocks[i - 1].setNext(blocks[i]);
			blocks[i].setPrev(blocks[i - 1]);
		}
	}
	


	
	private FlowBlock generateFlowBlock(Label label,CS2Stack variableStack) {
		int blockID = label.getLabelID() + 1;
		if (blocks[blockID] == null)
			return (blocks[blockID] = new FlowBlock(blockID, label.getAddress() + 1,variableStack));
		if (!checkMerging(blocks[blockID].getStack(),variableStack))
			throw new DecompilerException("Can't merge two stacks (Code is invalid).");
		return blocks[blockID];
	}
	
	
	/**
	 * Dump's stack contents to local variables.
	 * Write's assignation expressions on specific block.
	 */
	private void dumpStack(FlowBlock block, CS2Stack stack) {
		ArrayQueue<ExpressionNode> s = new ArrayQueue<ExpressionNode>(stack.getSize());
		while (stack.getSize() > 0)
			s.insert(stack.pop());		
		int ic = 0,sc = 0,lc = 0;		
		while (s.size() > 0) {
			ExpressionNode value = s.take();
			int stackType = value.getType().intSS() == 0 ? (value.getType().longSS() != 0 ? 2 : 1) : 0;
			int identifier = LocalVariable.makeStackDumpIdentifier(stackType != 0 ? (stackType == 2 ? lc : sc) : ic, stackType);
			LocalVariable variable;
			if (function.getScope().isDeclared(identifier)) {
				variable = function.getScope().getLocalVariable(identifier);
			}
			else {
				variable = new LocalVariable("stack_dump" + counter++, defaultType(stackType));
				variable.setIdentifier(identifier);
				function.getScope().declare(variable);
			}
			block.write(new PopableNode(new VariableAssignationNode(variable,cast(value,variable.getType()))));
			stack.push(new VariableLoadNode(variable), stackType);
			if (stackType == 0)
				ic++;
			else if (stackType == 1)
				sc++;
			else
				lc++;
		}
	}
	
	/**
	 * Casts expression node to specific type.
	 * If expression type is same then returned value is expr,
	 * otherwise on most cases CastExpressionNode is returned with one child 
	 * which is expr.
	 */
	private ExpressionNode cast(ExpressionNode expr, CS2Type type) {
		if (type == CS2Type.FUNCTION) {
			FunctionNode n = null;
			if (expr instanceof IntExpressionNode) {
				IntExpressionNode iexpr = (IntExpressionNode)expr;
				try {
					if (iexpr.getData() != -1)
						n = decompiler.decompile(iexpr.getData());
					else
						n = new FunctionNode(-1, "none", new CS2Type[0], new String[0], CS2Type.VOID);
				}
				catch (DecompilerException ex) {
				}
			}
			return new FunctionExpressionNode(expr, n);
		}
		
		return new CastNode(type, expr);
	}
	
	
	/**
	 * Check's if two stacks can be merged.
	 * false is returned if stack sizes doesn't match or 
	 * one of the elements in the first or second stack is not dumped
	 * to same local variables.
	 */
	private boolean checkMerging(CS2Stack v0, CS2Stack v1) {
		if (v0.getSize() != v1.getSize())
			return false;
		for (int i = 0; i < 3; i++)
			if (v0.getSize(i) != v1.getSize(i))
				return false;
		CS2Stack c0 = v0.copy();
		CS2Stack c1 = v1.copy();
		for (int i = 0; i < 3; i++) {
			while (c0.getSize(i) > 0) {
				ExpressionNode e0 = c0.pop(i);
				ExpressionNode e1 = c1.pop(i);
				if (!(e0 instanceof VariableLoadNode) || !(e1 instanceof VariableLoadNode))
					return false;
				if (((VariableLoadNode)e0).getVariable() != ((VariableLoadNode)e1).getVariable())
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Find's default CS2Type for given stack type.
	 */
	private CS2Type defaultType(int stackType) {
		if (stackType == 0)
			return CS2Type.INT;
		else if (stackType == 1)
			return CS2Type.STRING;
		else if (stackType == 2)
			return CS2Type.LONG;
		throw new DecompilerException("Wrong stack type.");
	}
	
	/**
	 * Find's basic CS2Type for given advanced type.
	 */
	private CS2Type basicType(CS2Type advanced) {
		if ((advanced.intSS() + advanced.stringSS() + advanced.longSS()) == 1)
			return advanced.intSS() == 1 ? CS2Type.INT : (advanced.stringSS() == 1 ? CS2Type.STRING : CS2Type.LONG);
		throw new DecompilerException("Wrong advanced type.");
	}
	

	public void setBlocks(FlowBlock[] blocks) {
		this.blocks = blocks;
	}

	public FlowBlock[] getBlocks() {
		return blocks;
	}

	public List<FlowBlock> getProcessedBlocks() {
		return processedBlocks;
	}
	
	
}
