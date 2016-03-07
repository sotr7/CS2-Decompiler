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
import mgi.tools.jagdecs2.util.DecompilerUtils;

public class FlowBlocksSolver {

	private CS2Decompiler decompiler;
	private ScopeNode scope;
	private FlowBlock[] blocks;
	
	public FlowBlocksSolver(CS2Decompiler decompiler,ScopeNode scope, FlowBlock[] blocks) {
		this.decompiler = decompiler;
		this.scope = scope;
		this.blocks = blocks;
	}


	public void solve() throws DecompilerException {
		
		if (true) {
			int total = 0;
			
			do {
				total = 0;
				total += doStandartIfConditionsMergeCheck();
				total += doConnectionCheck();
			}
			while (total > 0);
				
			do {
				total = 0;
				total += doStandartIfCheck();
				total += doStandartIfElseCheck();
				total += doStandartLoopsCheck();
				total += doStandartFlowControlsCheck();
				total += doStandartSwitchesCheck();
				total += doConnectionCheck();
			}
			while (total > 0);
			
			do {
				total = 0;
				total += doUnexpectedGotosResolving();
				total += doConnectionCheck();
			}
			while (total > 0);
			
		}
		
		List<FlowBlock> blocks = listBlocks();
		for (FlowBlock block : blocks) {
			if (block.getSucessors().size() <= 0 && block.getPredecesors().size() <= 0) {
				List<AbstractCodeNode> childs = block.listChilds();
				for (AbstractCodeNode node : childs)
					this.scope.write(node);
				continue;
			}
			this.scope.write(block);
		}
	}
	
	private int doConnectionCheck() {
		int connected = 0;
		for (int a = 0; a < blocks.length; a++)
			for (int i = 0; i < blocks.length; i++)
				if (blocks[a] != null && blocks[i] != null && 
						blocks[a] != blocks[i] && canConnect(blocks[a],blocks[i])) {
					connected++;
					connect(blocks[a],blocks[i]);
				}
		if (connected > 0)
			System.err.println("Connected - " + connected);
		return connected;
	}
	
	private int doStandartIfCheck() {
		int solved = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && doStandartIfCheck(blocks[i]))
				solved++;
		if (solved > 0)
			System.err.println("Solved standart ifs - " + solved);
		return solved;
	}
	
	private boolean doStandartIfCheck(FlowBlock block) {
		if (block.size() < 2)
			return false;
		/**
		 * What we solve here is check last two nodes
		 * for structure like this
		 * IF (condition)
		 * 		GOTO if_start_block
		 * GOTO if_end_block
		 * if_start_block:
		 * whatever here
		 * if_end_block:
		 */
		AbstractCodeNode v0 = block.read(block.size() - 2);
		AbstractCodeNode v1 = block.read(block.size() - 1);
		if (!(v0 instanceof ConditionalFlowBlockJump) || !(v1 instanceof UnconditionalFlowBlockJump))
			return false;
		ConditionalFlowBlockJump condition = (ConditionalFlowBlockJump)v0;
		UnconditionalFlowBlockJump jumpOut = (UnconditionalFlowBlockJump)v1;
		if (condition.getTarget().getBlockID() <= block.getBlockID() || 
				jumpOut.getTarget().getBlockID() <= condition.getTarget().getBlockID())
			return false;
		List<FlowBlock> inJumps = new ArrayList<FlowBlock>();
		inJumps.add(block);
		List<FlowBlock> outJumps = new ArrayList<FlowBlock>();
		if (!canCut(block,jumpOut.getTarget(),inJumps,outJumps))
			return false;
		FlowBlock[] blocks = this.cut(block, jumpOut.getTarget());
		block.setCodeAddress(block.size() - 2);
		for (int i = 0; i < 2; i++)
			block.delete();
		block.getSucessors().remove(condition.getTarget());
		block.getSucessors().remove(jumpOut.getTarget());
		condition.getTarget().getPredecesors().remove(block);
		jumpOut.getTarget().getPredecesors().remove(block);
		IfElseNode ifElse = new IfElseNode(new ExpressionNode[] { condition.getExpression() },new ScopeNode[] { new ScopeNode(this.scope) },new ScopeNode(this.scope));
		FlowBlocksSolver solver = new FlowBlocksSolver(this.decompiler,ifElse.getScopes()[0], blocks);
		solver.solve();
		block.write(ifElse);
		return true;
	}
	
	private int doStandartIfElseCheck() {
		int solved = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && doStandartIfElseCheck(blocks[i]))
				solved++;
		if (solved > 0)
			System.err.println("Solved standart if else's - " + solved);
		return solved;
	}
	
	private boolean doStandartIfElseCheck(FlowBlock block) {
		if (block.size() < 2)
			return false;

		AbstractCodeNode v0 = block.read(block.size() - 2);
		AbstractCodeNode v1 = block.read(block.size() - 1);
		if (!(v0 instanceof ConditionalFlowBlockJump) || !(v1 instanceof UnconditionalFlowBlockJump))
			return false;
		ConditionalFlowBlockJump condition = (ConditionalFlowBlockJump)v0;
		UnconditionalFlowBlockJump jumpOut = (UnconditionalFlowBlockJump)v1;
		if (block.getNext() != condition.getTarget() || 
				jumpOut.getTarget().getBlockID() <= condition.getTarget().getBlockID())
			return false;
		
		List<FlowBlock> outJumps = getAllOutjumps(block, jumpOut.getTarget());
		if (outJumps.size() != 1)
			return false;
		FlowBlock end = outJumps.get(0);
		if (!jumpOut.getTarget().getPrev().getSucessors().contains(end))
			return false;
		if (jumpOut.getTarget().getPrev().size() < 1)
			return false;
		AbstractCodeNode v2 = jumpOut.getTarget().getPrev().read(jumpOut.getTarget().getPrev().size() - 1);
		if (!(v2 instanceof UnconditionalFlowBlockJump))
			return false;
		UnconditionalFlowBlockJump jumpEnd = (UnconditionalFlowBlockJump) v2;
		if (jumpEnd.getTarget() != end)
			return false;
		//if (this.getFirstJumpingBlock(end) != jumpOut.getTarget().getPrev())
		//	return false;
		
		{
			List<FlowBlock> allowedInJumpers = new ArrayList<FlowBlock>();
			allowedInJumpers.add(block);
			List<FlowBlock> allowedOutJumps = new ArrayList<FlowBlock>();
			allowedOutJumps.add(end);
			if (!canCut(block, jumpOut.getTarget(), allowedInJumpers, allowedOutJumps))
				return false;
		}
		
		
		int bufferWrite = 0;
		FlowBlock[] startBlocks = new FlowBlock[100];
		FlowBlock[] endBlocks = new FlowBlock[100];
		FlowBlock[] jumpEndBlocks = new FlowBlock[100];
		ConditionalFlowBlockJump[] jumpins = new ConditionalFlowBlockJump[100];
		UnconditionalFlowBlockJump[] jumpouts = new UnconditionalFlowBlockJump[100];
		UnconditionalFlowBlockJump[] jumpends = new UnconditionalFlowBlockJump[100];
		
		startBlocks[bufferWrite] = block;
		endBlocks[bufferWrite] = jumpOut.getTarget();
		jumpEndBlocks[bufferWrite] = jumpOut.getTarget().getPrev();
		jumpins[bufferWrite] = condition;
		jumpouts[bufferWrite] = jumpOut;
		jumpends[bufferWrite++] = jumpEnd;
		
		while(true) {
			block = jumpOut.getTarget();
			if (block == end) 
				return false; // we reached end block?! wtf
			
			if (block.size() < 2)
				break; // we reached end, there's else block
			AbstractCodeNode v3 = block.read(block.size() - 2);
			AbstractCodeNode v4 = block.read(block.size() - 1);
			if (!(v3 instanceof ConditionalFlowBlockJump) || !(v4 instanceof UnconditionalFlowBlockJump))
				break; // we reached end, there's else block 
			condition = (ConditionalFlowBlockJump)v3;
			jumpOut = (UnconditionalFlowBlockJump)v4;
			if (block.getNext() != condition.getTarget() || 
					jumpOut.getTarget().getBlockID() <= condition.getTarget().getBlockID())
				return false;
			outJumps = getAllOutjumps(block, jumpOut.getTarget());
			if (outJumps.size() == 0) { // there's no else block
				if (jumpOut.getTarget() != end)
					return false;
				List<FlowBlock> allowedInJumpers = new ArrayList<FlowBlock>();
				allowedInJumpers.add(block);
				List<FlowBlock> allowedOutJumps = new ArrayList<FlowBlock>();
				if (!canCut(block, jumpOut.getTarget(), allowedInJumpers, allowedOutJumps))
					return false;
				startBlocks[bufferWrite] = block;
				endBlocks[bufferWrite] = jumpOut.getTarget();
				jumpins[bufferWrite] = condition;
				jumpouts[bufferWrite] = jumpOut;
			}
			else if (outJumps.size() == 1 && outJumps.get(0) == end) {
				AbstractCodeNode v5 = jumpOut.getTarget().getPrev().read(jumpOut.getTarget().getPrev().size() - 1);
				if (!(v5 instanceof UnconditionalFlowBlockJump))
					return false;
				jumpEnd = (UnconditionalFlowBlockJump) v5;
				if (jumpEnd.getTarget() != end)
					return false;
				List<FlowBlock> allowedInJumpers = new ArrayList<FlowBlock>();
				allowedInJumpers.add(block);
				List<FlowBlock> allowedOutJumps = new ArrayList<FlowBlock>();
				allowedOutJumps.add(end);
				if (!canCut(block, jumpOut.getTarget(), allowedInJumpers, allowedOutJumps))
					return false;			
				startBlocks[bufferWrite] = block;
				endBlocks[bufferWrite] = jumpOut.getTarget();
				jumpEndBlocks[bufferWrite] = jumpOut.getTarget().getPrev();
				jumpins[bufferWrite] = condition;
				jumpouts[bufferWrite] = jumpOut;
				jumpends[bufferWrite++] = jumpEnd;
			}
			else
				return false;
		}
		
		boolean hasElse = jumpouts[bufferWrite - 1].getTarget() != end;
		List<FlowBlock> allowedElseInJumpers = new ArrayList<FlowBlock>();
		allowedElseInJumpers.add(startBlocks[bufferWrite - 1]); // last start block
		if (hasElse && !this.canCut(block.getPrev(), end, allowedElseInJumpers, new ArrayList<FlowBlock>()))
			return false;
		ExpressionNode[] conditions = new ExpressionNode[bufferWrite];
		ScopeNode[] scopes = new ScopeNode[bufferWrite];
		ScopeNode elseScope = new ScopeNode(this.scope);
		
		for (int i = 0; i < bufferWrite; i++) {
			FlowBlock[] blocks = cut(startBlocks[i], endBlocks[i]);
			if (jumpEndBlocks[i] != null) {
				jumpEndBlocks[i].setCodeAddress(jumpEndBlocks[i].addressOf(jumpends[i]));
				jumpEndBlocks[i].delete();
				jumpEndBlocks[i].getSucessors().remove(jumpends[i].getTarget());
				jumpends[i].getTarget().getPredecesors().remove(jumpEndBlocks[i]);
			}
			startBlocks[i].setCodeAddress(startBlocks[i].addressOf(jumpins[i]));
			startBlocks[i].delete();
			startBlocks[i].getSucessors().remove(jumpins[i].getTarget());
			jumpins[i].getTarget().getPredecesors().remove(startBlocks[i]);
			startBlocks[i].setCodeAddress(startBlocks[i].addressOf(jumpouts[i]));
			startBlocks[i].delete();
			startBlocks[i].getSucessors().remove(jumpouts[i].getTarget());
			jumpouts[i].getTarget().getPredecesors().remove(startBlocks[i]);
			conditions[i] = jumpins[i].getExpression();
			scopes[i] = new ScopeNode(this.scope);
			FlowBlocksSolver solver = new FlowBlocksSolver(decompiler,scopes[i],blocks);
			solver.solve();
		}
		
		if (hasElse) {
			FlowBlock[] blocks = cut(block.getPrev(),end);
			FlowBlocksSolver solver = new FlowBlocksSolver(decompiler,elseScope,blocks);
			solver.solve();
		}
		
		startBlocks[0].setCodeAddress(startBlocks[0].size());
		startBlocks[0].write(new IfElseNode(conditions,scopes,elseScope));
		
		return true;
	}
	


	
	
	private int doStandartIfConditionsMergeCheck() {
		int merged = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && (doIfANDConditionsMerge(blocks[i]) || doIfORConditionsMerge1(blocks[i]) || doIfORConditionsMerge2(blocks[i])))
				merged++;
		if (merged > 0)
			System.err.println("Merged if conditions:" + merged);
		return merged;
	}
	
	private boolean doIfORConditionsMerge1(FlowBlock block) {
		if (block.size() < 2)
			return false;
		block.setCodeAddress(0);
		for (AbstractCodeNode node = block.read(); node != null; node = block.read()) {
			if (node instanceof ConditionalFlowBlockJump) {
				ConditionalFlowBlockJump jmp = (ConditionalFlowBlockJump) node;
				int nextAddr = block.addressOf(jmp) + 1;
				if (nextAddr >= block.size())
					return false;
				if (!(block.read(nextAddr) instanceof ConditionalFlowBlockJump))
					return false;
				ConditionalFlowBlockJump jmp2 = (ConditionalFlowBlockJump) block.read(nextAddr);
				if (jmp.getTarget() == jmp2.getTarget()) {
					block.getSucessors().remove(jmp.getTarget());
					jmp.getTarget().getPredecesors().remove(block);
					block.setCodeAddress(nextAddr - 1);
					block.delete();
					block.delete();
					block.write(new ConditionalFlowBlockJump(new ConditionalExpressionNode(jmp.getExpression(),jmp2.getExpression(),6),jmp.getTarget()));
					System.err.println("AHAHAHAHAH");
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean doIfORConditionsMerge2(FlowBlock block) {
		if (block.size() < 2)
			return false;
		
		AbstractCodeNode v0 = block.read(block.size() - 2);
		AbstractCodeNode v1 = block.read(block.size() - 1);
		if (!(v0 instanceof ConditionalFlowBlockJump) || !(v1 instanceof UnconditionalFlowBlockJump))
			return false;
		
		ConditionalFlowBlockJump condition1 = (ConditionalFlowBlockJump)v0;
		UnconditionalFlowBlockJump jmp = (UnconditionalFlowBlockJump)v1;
		
		FlowBlock target = jmp.getTarget();
		if (target.getBlockID() <= block.getBlockID() || target.getPredecesors().size() != 1 || target.size() < 2)
			return false;
		
		AbstractCodeNode v3 = target.read(0);
		AbstractCodeNode v4 = target.read(1);
		if (!(v3 instanceof ConditionalFlowBlockJump) || !(v4 instanceof UnconditionalFlowBlockJump))
			return false;
		
		ConditionalFlowBlockJump condition2 = (ConditionalFlowBlockJump)v3;
		UnconditionalFlowBlockJump jmp2 = (UnconditionalFlowBlockJump)v4;
		
		FlowBlock realTarget = jmp2.getTarget();
		
		if (condition1.getTarget() != condition2.getTarget())
			return false;
		
		block.setCodeAddress(block.size() - 2);
		block.delete();
		block.delete();
		
		block.write(new ConditionalFlowBlockJump(new ConditionalExpressionNode(condition1.getExpression(), condition2.getExpression(), 6), condition1.getTarget()));
		block.write(new UnconditionalFlowBlockJump(realTarget));
		
		block.getSucessors().remove(target);
		target.getPredecesors().remove(block);
		
		block.getSucessors().add(realTarget);
		realTarget.getPredecesors().add(block);
		
		
		target.setCodeAddress(0);
		target.delete();
		target.delete();
		
		target.getSucessors().remove(condition2.getTarget());
		condition2.getTarget().getPredecesors().remove(target);
		target.getSucessors().remove(realTarget);
		realTarget.getPredecesors().remove(target);
		
		return true;
	}
	
	private boolean doIfANDConditionsMerge(FlowBlock block) {
		if (block.size() < 2)
			return false;
		AbstractCodeNode v0 = block.read(block.size() - 2);
		AbstractCodeNode v1 = block.read(block.size() - 1);
		if (!(v0 instanceof ConditionalFlowBlockJump) || !(v1 instanceof UnconditionalFlowBlockJump))
			return false;
		ConditionalFlowBlockJump conditionPart = (ConditionalFlowBlockJump)v0;
		UnconditionalFlowBlockJump jumpOut = (UnconditionalFlowBlockJump)v1;
		FlowBlock condition = conditionPart.getTarget();
		FlowBlock out = jumpOut.getTarget();
		if (condition.getBlockID() <= block.getBlockID() || out.getBlockID() <= condition.getBlockID())
			return false;	
		if (condition.getPredecesors().size() != 1 || condition.size() < 1 || condition.size() > 2)
			return false;
		
		if (condition.size() == 1) {
			if (condition.getNext() != out)
				return false;
			
			AbstractCodeNode v3 = condition.read(condition.size() - 1);
			if (!(v3 instanceof ConditionalFlowBlockJump))
				return false;
			
			ConditionalFlowBlockJump realjmp = (ConditionalFlowBlockJump)v3;
			FlowBlock target = realjmp.getTarget();
			
			block.setCodeAddress(block.size() - 2);
			block.delete();
			block.write(new ConditionalFlowBlockJump(new ConditionalExpressionNode(conditionPart.getExpression(), realjmp.getExpression(), 7), target));
			block.getSucessors().remove(condition);
			block.getSucessors().add(target);
			condition.getPredecesors().remove(block);
			target.getPredecesors().add(block);
			
			condition.setCodeAddress(condition.size() - 1);
			condition.delete();
			condition.getSucessors().remove(target);
			target.getPredecesors().remove(condition);
			return true;
			
		}
		else {
			AbstractCodeNode v3 = condition.read(condition.size() - 2);
			AbstractCodeNode v4 = condition.read(condition.size() - 1);
			if (!(v3 instanceof ConditionalFlowBlockJump) || !(v4 instanceof UnconditionalFlowBlockJump))
				return false;
			ConditionalFlowBlockJump realjmp = (ConditionalFlowBlockJump)v3;
			UnconditionalFlowBlockJump jumpOut2 = (UnconditionalFlowBlockJump)v4;
			
			if (jumpOut2.getTarget() != out)
				return false;
			
			FlowBlock target = realjmp.getTarget();
			
			block.setCodeAddress(block.size() - 2);
			block.delete();
			block.write(new ConditionalFlowBlockJump(new ConditionalExpressionNode(conditionPart.getExpression(), realjmp.getExpression(), 7), target));
			block.getSucessors().remove(condition);
			block.getSucessors().add(target);
			condition.getPredecesors().remove(block);
			target.getPredecesors().add(block);
			
			condition.setCodeAddress(condition.size() - 2);
			for (int i = 0; i < 2; i++)
				condition.delete();
			condition.getSucessors().remove(target);
			condition.getSucessors().remove(out);
			target.getPredecesors().remove(condition);
			out.getPredecesors().remove(condition);
			return true;
		}
	}
	
	private int doStandartLoopsCheck() {
		int flowsFound = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && doStandartLoopsCheck(blocks[i]))
				flowsFound++;
		if (flowsFound > 0)
			System.err.println("Loops found:" + flowsFound);
		return flowsFound;
	}
	
	private boolean doStandartLoopsCheck(FlowBlock block) {
		/**
		 * What we solve here is check the last jumper to this 
		 * block does jump unconditionally and only block that is jumped
		 * from inside the loop is the next block from the backjumper.
		 * example
		 * doSomething();
		 * flow_1:
		 * 		IF (someVariable > 0)
		 * 			GOTO flow_2
		 * 		GOTO flow_3;
		 * flow_2:
		 * 		doSomethingElse();
		 *		 GOTO flow_1
		 * flow_3:
		 * 		blah();
		 * This code could be translated to
		 * doSomething();
		 * for (;someVariable > 0;) {
		 * 		doSomethingElse();
		 * }
		 * blah();
		 */
		FlowBlock last = this.getLastJumpingBlock(block);
		if (last == null || last.getBlockID() < block.getBlockID() || last.size() < 1)
			return false;
		if (!(last.read(last.size() - 1) instanceof UnconditionalFlowBlockJump))
			return false;
		UnconditionalFlowBlockJump jumpBack = (UnconditionalFlowBlockJump) last.read(last.size() - 1);
		if (jumpBack.getTarget() != block)
			return false;
		if (block.getPrev() == null)
			this.attachSynthethicBlockBefore(block);
		if (last.getNext() == null)
			this.attachSynthethicBlockAfter(last);
		FlowBlock start = block.getPrev();
		FlowBlock end = last.getNext();
		List<FlowBlock> inJumps = new ArrayList<FlowBlock>();
		List<FlowBlock> outJumps = new ArrayList<FlowBlock>();
		outJumps.add(end);
		if (!this.canCut(start, end, inJumps, outJumps))
			return false;
		FlowBlock[] blocks = this.cut(start, end);
		LoopNode loop = new LoopNode(LoopNode.LOOPTYPE_WHILE,new ScopeNode(scope),new CastNode(CS2Type.BOOLEAN, new IntExpressionNode(1)),block,end);
		block.setCodeAddress(0);
		if (block.read() instanceof ConditionalFlowBlockJump && block.read() instanceof UnconditionalFlowBlockJump) {
			ConditionalFlowBlockJump v0 = (ConditionalFlowBlockJump)block.read(0);
			UnconditionalFlowBlockJump v1 = (UnconditionalFlowBlockJump)block.read(1);
			if (v0.getTarget() == block.getNext() && v1.getTarget() == end) {
				block.setCodeAddress(0);
				for (int i = 0; i < 2; i++)
					block.delete();
				loop = new LoopNode(LoopNode.LOOPTYPE_WHILE,new ScopeNode(scope), v0.getExpression(), block,end);
				
				block.getSucessors().remove(v0.getTarget());
				block.getSucessors().remove(v1.getTarget());
				v0.getTarget().getPredecesors().remove(block);
				v1.getTarget().getPredecesors().remove(block);
			}
		}
		start.setCodeAddress(start.size());
		start.write(loop);
		
		last.setCodeAddress(last.size() - 1);
		last.delete();
		block.getPredecesors().remove(last);
		last.getSucessors().remove(block);
		FlowBlocksSolver solver = new FlowBlocksSolver(decompiler,loop.getScope(),blocks);
		solver.solve();

		return true;
	}
	
	private int doStandartFlowControlsCheck() {
		int controlsFound = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && doStandartFlowControlsCheck(blocks[i]))
				controlsFound++;
		if (controlsFound > 0)
			System.err.println("Controls found:" + controlsFound);
		return controlsFound;
	}
	
	private boolean doStandartFlowControlsCheck(FlowBlock block) {
		if (block.size() < 1)
			return false;
		/**
		 * What we solve here is check the last node
		 * if it goes to start (continue) or end(break) of parent controllable
		 * flow node.
		 */
		AbstractCodeNode n0 = block.read(block.size() - 1);
		if (!(n0 instanceof UnconditionalFlowBlockJump))
			return false;
		UnconditionalFlowBlockJump jmp = (UnconditionalFlowBlockJump)n0;
		IControllableFlowNode node = scope.findControllableNode(jmp.getTarget());
		if (node == null)
			return false;
		if (node instanceof IContinueableNode && ((IContinueableNode)node).getStart() == jmp.getTarget()) {
			block.setCodeAddress(block.size() - 1);
			block.delete();
			block.write(new ContinueNode(scope,(IContinueableNode)node));
			block.getSucessors().remove(jmp.getTarget());
			jmp.getTarget().getPredecesors().remove(block);
			return true;
		}
		else if (node instanceof IBreakableNode && ((IBreakableNode)node).getEnd() == jmp.getTarget()) {
			block.setCodeAddress(block.size() - 1);
			block.delete();
			block.write(new BreakNode(scope,(IBreakableNode)node));
			block.getSucessors().remove(jmp.getTarget());
			jmp.getTarget().getPredecesors().remove(block);
			return true;
		}
		else
			throw new DecompilerException("Unexpected type node:" + node);
		
	}
	
	
	private int doStandartSwitchesCheck() {
		int switchesFound = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && doStandartSwitchesCheck(blocks[i]))
				switchesFound++;
		if (switchesFound > 0)
			System.err.println("Switches found:" + switchesFound);
		return switchesFound;
	}
	
	private boolean doStandartSwitchesCheck(FlowBlock block) {
		if (block.size() < 1)
			return false;
		
		AbstractCodeNode v0 = block.read(block.size() - 1);
		if (!(v0 instanceof SwitchFlowBlockJump))
			return false;
		SwitchFlowBlockJump sw = (SwitchFlowBlockJump)v0;
		if (sw.getCases().length <= 0)
			return false;
		if (sw.getTargets()[0].getBlockID() <= block.getBlockID())
			return false;
		FlowBlock start = sw.getTargets()[0];
		FlowBlock end = sw.getTargets()[sw.getTargets().length - 1];
		if (start.getPrev() == null)
			attachSynthethicBlockBefore(start);
		if (end.getNext() == null)
			attachSynthethicBlockAfter(end);
		start = start.getPrev();
		end = end.getNext();
		
		main: while (true) {
			List<FlowBlock> outJumps = this.getAllOutjumps(start, end);
			for (FlowBlock out : outJumps) {
				if (out.getBlockID() < end.getBlockID())
					return false;
				if (end != out) {
					end = out;
					continue main;
				}
			}
			break;
		}
		
		
		DecompilerUtils.SwitchCase[] cases = DecompilerUtils.makeSwitchCases(sw);
		List<FlowBlock> allowedInJumpers = new ArrayList<FlowBlock>();
		for (int i = 0; i < sw.getCases().length; i++)
			allowedInJumpers.add(block);
		
		List<FlowBlock> allowedOutJumps = this.getAllOutjumps(start, end);
		for (FlowBlock out : allowedOutJumps) {
			if (out != end)
				return false;
		}
		if (!canCut(start, end, allowedInJumpers, allowedOutJumps))
			return false;
		FlowBlock[] blocks = cut(start, end);
		start.setCodeAddress(start.size() - 1);
		start.delete();
		SwitchNode swi = new SwitchNode(end, new ScopeNode(this.scope), sw.getExpression());
		start.write(swi);
		for (int i = 0; i < sw.getCases().length; i++) {
			sw.getTargets()[i].getPredecesors().remove(block);
			block.getSucessors().remove(sw.getTargets()[i]);
		}
		
		for (int i = 0; i < cases.length; i++) {
			boolean def = false;
			for (CaseAnnotation a : cases[i].getAnnotations()) {
				if (a.isDefault()) {
					def = true;
					break;
				}
			}
			
			if (!def)
				continue;
			
			int blockIndex = -1;
			for (int x = 0; x < blocks.length; x++) {
				if (blocks[x] == cases[i].getBlock()) {
					blockIndex = x;
					break;
				}
			}
			
			if (blockIndex == -1)
				throw new DecompilerException("logic error");
			
			FlowBlock b = blocks[blockIndex];
			
			if (b.size() != 1 || b.getPredecesors().size() != 0 || b.getSucessors().size() != 1)
				continue;
			
			AbstractCodeNode b0 = b.read(0);
			if (!(b0 instanceof UnconditionalFlowBlockJump))
				continue;
			
			UnconditionalFlowBlockJump j = (UnconditionalFlowBlockJump)b0;
			if (j.getTarget() != end)
				cases[i].setBlock(j.getTarget());
			else
				cases[i] = null;
			
			b.getSucessors().remove(j.getTarget());
			j.getTarget().getPredecesors().remove(b);
			
			b.setCodeAddress(0);
			b.delete();
		}

		for (int i = 0; i < cases.length; i++) {
			if (cases[i] == null)
				continue;
			
			cases[i].getBlock().setCodeAddress(0);
			for (int a = 0; a < cases[i].getAnnotations().length; a++)
				cases[i].getBlock().write(cases[i].getAnnotations()[a]);
		}
		FlowBlocksSolver solver = new FlowBlocksSolver(decompiler,swi.getScope(),blocks);
		solver.solve();
		return true;
	}
	
	
	
	private int doUnexpectedGotosResolving() {
		int unexpectedGotosResolved = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && doUnexpectedGotosResolving(blocks[i]))
				unexpectedGotosResolved++;
		if (unexpectedGotosResolved > 0)
			System.err.println("Unexpected gotos resolved:" + unexpectedGotosResolved);
		return unexpectedGotosResolved;
	}
	
	private boolean doUnexpectedGotosResolving(FlowBlock start) {
		for (int i = 0; i < blocks.length; i++) {		
			if (blocks[i] == null || blocks[i].getPrev() == null)
				continue;
			FlowBlock firstJumper = this.getFirstJumpingBlock(blocks[i]);
			FlowBlock lastJumper = this.getLastJumpingBlock(blocks[i]);
			if (firstJumper == null || lastJumper == null || firstJumper.getBlockID() <= start.getBlockID() || lastJumper.getBlockID() >= blocks[i].getBlockID())
				continue;
			FlowBlock end = blocks[i];
			List<FlowBlock> allowedOutJumps = new ArrayList<FlowBlock>();
			for (FlowBlock current = start.getNext(); current != end && current != null; current = current.getNext()) {
				for (FlowBlock successor : current.getSucessors()) {
					if (successor == end)
						allowedOutJumps.add(end); 
				}
			}
			if (!this.canCut(start,end, new ArrayList<FlowBlock>(), allowedOutJumps ))
				continue;
			FlowBlock[] blocks = this.cut(start,end);
			LoopNode loop = new LoopNode(LoopNode.LOOPTYPE_DOWHILE,new ScopeNode(scope), new CastNode(CS2Type.BOOLEAN, new IntExpressionNode(0)),blocks[0],end);
			start.setCodeAddress(0);
			start.write(loop);
			FlowBlocksSolver solver = new FlowBlocksSolver(decompiler,loop.getScope(),blocks);
			solver.solve();
			return true;
		}
		return false;
	}
	
	/**
	 * Decides if two blocks can be connected.
	 */
	private boolean canConnect(FlowBlock b1,FlowBlock b2) {
		if (b1.getNext() != b2 || b2.getPrev() != b1)
			return false;
		if (b2.getPredecesors().size() > 0 || (b2.size() > 0 && b1.getSucessors().size() > 0))
			return false;
		return true;
	}
	
	/**
	 * Connect's b2 to b1's end.
	 */
	private void connect(FlowBlock b1,FlowBlock b2) {
		if (!canConnect(b1,b2))
			throw new RuntimeException("Unchecked connection.");
		b1.setNext(b2.getNext());
		if (b2.getNext() != null) {
			b2.getNext().setPrev(b1);
		}
		b1.setCodeAddress(0);
		do {} while (b1.read() != null);
		b2.setCodeAddress(0);
		for (AbstractCodeNode node = b2.read(); node != null; node = b2.read())
			b1.write(node);
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] == b2)
				blocks[i] = null;
		for (FlowBlock successor : b2.getSucessors()) {
			successor.getPredecesors().remove(b2);
			successor.getPredecesors().add(b1);
			b1.getSucessors().add(successor);
		}
		for (FlowBlock predecesor : b2.getPredecesors()) {
			predecesor.getSucessors().remove(b2);
			predecesor.getSucessors().add(b1);
			b1.getPredecesors().add(predecesor);
		}
	}
	
	/**
	 * Decides if flow blocks starting after from and ending before to 
	 * can be cut.
	 */
	private boolean canCut(FlowBlock from,FlowBlock to,List<FlowBlock> allowedInJumps,List<FlowBlock> allowedOutJumps) {
		if (from.getBlockID() >= to.getBlockID() || from.getNext() == to || to.getPrev() == from)
			return false;
		List<FlowBlock> leftIn = new ArrayList<FlowBlock>(allowedInJumps);
		List<FlowBlock> leftOut = new ArrayList<FlowBlock>(allowedOutJumps);
		for (FlowBlock current = from.getNext(); current != to && current != null; current = current.getNext()) {
			//if (current == null)
			//	return false;
			for (FlowBlock inJump : current.getPredecesors()) {
				if (leftIn.contains(inJump)) {
					leftIn.remove(inJump);
					continue;
				}
				if (inJump.getBlockID() <= from.getBlockID() || inJump.getBlockID() >= to.getBlockID())
					return false;
			}
			for (FlowBlock outJump : current.getSucessors()) {
				if (leftOut.contains(outJump)) {
					leftOut.remove(outJump);
					continue;
				}
				if (outJump.getBlockID() <= from.getBlockID() || outJump.getBlockID() >= to.getBlockID())
					return false;
			}
		}
		return true;
	}
	

	private FlowBlock[] cut(FlowBlock from,FlowBlock to) {
		FlowBlock[] buffer = new FlowBlock[to.getBlockID() * 2];
		int bufferWrite = 0;
		for (FlowBlock current = from.getNext(); current != to && current != null; current = current.getNext())
			buffer[bufferWrite++] = current;	
		from.setNext(to);
		to.setPrev(from);
		buffer[0].setPrev(null);
		buffer[bufferWrite - 1].setNext(null);
		
		FlowBlock[] blocks = new FlowBlock[bufferWrite];
		for (int i = 0; i < blocks.length; i++) {
			blocks[i] = buffer[i];
			for (int a = 0; a < this.blocks.length; a++)
				if (blocks[i] == this.blocks[a])
					this.blocks[a] = null;
		}
		
		return blocks;
	}
	
	/**
	 * Attache's synthethic block before given block.
	 * if (next.getPrev() != null)
	 * then this method throws IllegalArgumentException
	 * The ID of the synthethic flow block is always negative -(next.getBlockID() - 1);
	 */
	private void attachSynthethicBlockBefore(FlowBlock next) {
		if (next.getPrev() != null)
			throw new IllegalArgumentException("next.getPrev() != null");
		int blockID = next.getBlockID() - 1;
		if (blockID > 0)
			blockID = -blockID;
		FlowBlock block = new FlowBlock(blockID,-1,new CS2Stack());
		FlowBlock[] rebuff = new FlowBlock[blocks.length + 1];
		for (int i = 0,write = 0; i < blocks.length; i++) {
			if (blocks[i] != null && blocks[i].getNext() == next)
				throw new IllegalArgumentException("block " + i + " next flow block is argument!");
			if (blocks[i] == next)
				rebuff[write++] = block;
			rebuff[write++] = blocks[i];
		}
		block.setNext(next);
		next.setPrev(block);
		blocks = rebuff;
	}
	
	
	/**
	 * Attache's synthethic block after given block.
	 * if (prev.getNext() != null)
	 * then this method throws IllegalArgumentException
	 * The ID of the synthethic flow block is always higher than prev by 5000
	 */
	private void attachSynthethicBlockAfter(FlowBlock prev) {
		if (prev.getNext() != null)
			throw new IllegalArgumentException("next.getPrev() != null");
		int blockID = prev.getBlockID() + 5000;
		FlowBlock block = new FlowBlock(blockID,-1,new CS2Stack());
		FlowBlock[] rebuff = new FlowBlock[blocks.length + 1];
		for (int i = 0,write = 0; i < blocks.length; i++) {
			if (blocks[i] != null && blocks[i].getPrev() == prev)
				throw new IllegalArgumentException("block " + i + " prev flow block is argument!");
			rebuff[write++] = blocks[i];
			if (blocks[i] == prev)
				rebuff[write++] = block;
		}
		block.setPrev(prev);
		prev.setNext(block);
		blocks = rebuff;
	}
	
	private List<FlowBlock> getAllOutjumps(FlowBlock from,FlowBlock to) {
		List<FlowBlock> outJumps = new ArrayList<FlowBlock>();
		if (from.getBlockID() >= to.getBlockID() || from.getNext() == to || to.getPrev() == from)
			throw new IllegalArgumentException("from -> | nothing | <- to");
		for (FlowBlock current = from.getNext(); current != to && current != null; current = current.getNext()) {
			for (FlowBlock outJump : current.getSucessors()) {
				if (outJump.getBlockID() <= from.getBlockID() || outJump.getBlockID() >= to.getBlockID())
					outJumps.add(outJump);
			}
		}
		return outJumps;
	}
	
	
	
	
	public FlowBlock getFirstJumpingBlock(FlowBlock target) {
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && blocks[i].getSucessors().contains(target))
				return blocks[i];
		return null;
	}
	
	public FlowBlock getLastJumpingBlock(FlowBlock target) {
		int lastBlockID = -1;
		FlowBlock last = null;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && blocks[i].getBlockID() > lastBlockID && blocks[i].getSucessors().contains(target)) {
				lastBlockID = blocks[i].getBlockID();
				last = blocks[i];
			}
		return last;
	}


	
	public List<FlowBlock> listBlocks() {
		List<FlowBlock> blocks = new ArrayList<FlowBlock>();
		for (int i = 0; i < this.blocks.length; i++)
			if (this.blocks[i] != null)
				blocks.add(this.blocks[i]);
		return blocks;
	}

	

	public ScopeNode getScope() {
		return scope;
	}
	


	
	
}
