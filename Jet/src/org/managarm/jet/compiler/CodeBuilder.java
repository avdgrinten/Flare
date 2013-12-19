package org.managarm.jet.compiler;

import static org.managarm.aurora.lang.AuTerm.mkApply;
import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkOperator;

import java.util.HashMap;
import java.util.Map;

import org.managarm.aurora.builtin.Bool;
import org.managarm.aurora.builtin.IntArithmetic;
import org.managarm.aurora.builtin.Nil;
import org.managarm.aurora.lang.AuApply;
import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuLambda;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuPi;
import org.managarm.aurora.lang.AuTerm;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class CodeBuilder {
	private Namespace p_namespace;
	private Receiver p_receiver;
	private MethodVisitor p_method;
	private TypeCompiler p_typeCompiler;
	
	// next JVM register number that will be allocated
	private int p_registerPointer = -1;
	
	private Map<InternalRef, Cell> p_cells = new HashMap<InternalRef, Cell>();
	
	public CodeBuilder(Namespace namespace, Receiver receiver,
			MethodVisitor method) {
		p_namespace = namespace;
		p_receiver = receiver;
		p_method = method;
		p_typeCompiler = new TypeCompiler(namespace, receiver);
	}
	
	public void setupRegisterPointer(int pointer) {
		p_registerPointer = pointer;
	}
	public void setupExternCell(InternalRef ref, Cell cell) {
		p_cells.put(ref, cell);
	}
	
	private Cell.Register registerAlloc(Type type) {
		return new Cell.Register(type, p_registerPointer++);
	}
	private void registerFree(Cell.Register register) {
		
	}
	
	/**
	 * Reduces the given term.
	 * Places the result on the operand stack.
	 */
	public void computeTerm(AuTerm term) {
		if(term instanceof AuLambda) {
			AuLambda lambda = (AuLambda)term;
			
			String id = p_namespace.getUniqueId();
			ClassInfo implementation = new ClassInfo.LambdaClass(
					p_namespace, id, lambda.getBound(), lambda.getExpr());
			p_receiver.onDependency(implementation);
			
			p_method.visitTypeInsn(Opcodes.NEW,
					implementation.getPath());
			p_method.visitInsn(Opcodes.DUP);
			p_method.visitMethodInsn(Opcodes.INVOKESPECIAL,
					implementation.getPath(), "<init>",
					Type.getMethodDescriptor(Type.VOID_TYPE));
		}else if(term instanceof AuApply) {
			AuApply apply = (AuApply)term;
			
			AuPi ftype = (AuPi)apply.getFunction().type();
			Type jvm_arg_type = p_typeCompiler.compile(ftype.getBound());
			Type jvm_res_type = p_typeCompiler.compile(ftype.getCodomain());
			Type jvm_ftype = p_typeCompiler.compile(ftype);
			
			computeTerm(apply.getFunction());
			computeTerm(apply.getArgument());
			p_method.visitMethodInsn(Opcodes.INVOKEINTERFACE,
					jvm_ftype.getInternalName(),
					"compute", Type.getMethodDescriptor(jvm_res_type, jvm_arg_type));
		}else if(term instanceof AuOperator) {
			AuOperator operator = (AuOperator)term;
			computeOperator(operator);
		}else if(term instanceof AuConstant) {
			AuConstant constant = (AuConstant)term;
			computeConstant(constant);
		}else throw new RuntimeException("Illegal term " + term);
	}
	
	private void computeConstant(AuConstant constant) {
		AuConstant.Descriptor desc = constant.getDescriptor();
		if(desc == Nil.nilValue) {
			/* nil values are not stored on the operand stack */
		}else if(desc instanceof IntArithmetic.IntLit) {
			IntArithmetic.IntLit lit = (IntArithmetic.IntLit)desc;
			p_method.visitLdcInsn(lit.getValue().intValue());
		}else if(desc instanceof Bool.BoolLit) {
			Bool.BoolLit lit = (Bool.BoolLit)desc;
			if(lit.getValue()) {
				p_method.visitInsn(Opcodes.ICONST_1);
			}else p_method.visitInsn(Opcodes.ICONST_0);
		}else throw new RuntimeException("Illegal constant " + desc);
	}
	
	private void computeOperator(AuOperator operator) {
		AuOperator.Descriptor desc = operator.getDescriptor();
		if(desc instanceof InternalRef) {
			Cell cell = p_cells.get((InternalRef)desc);
			if(cell == null)
				throw new AssertionError("Illegal internal ref");
			cell.load(p_method);
		}else if(desc == IntArithmetic.intAdd) {
			computeTerm(operator.getArgument(0));
			computeTerm(operator.getArgument(1));
			p_method.visitInsn(Opcodes.IADD);
		}else if(desc == IntArithmetic.intLt) {
			Label then_label = new Label();

			p_method.visitInsn(Opcodes.ICONST_1);
			computeTerm(operator.getArgument(0));
			computeTerm(operator.getArgument(1));
			p_method.visitJumpInsn(Opcodes.IF_ICMPLT, then_label);
			p_method.visitInsn(Opcodes.POP);
			p_method.visitInsn(Opcodes.ICONST_0);
			p_method.visitLabel(then_label);
		}else if(desc == IntArithmetic.intFold) {
			Label loop_label = new Label();
			Label fin_label = new Label();
			
			AuTerm count = operator.getArgument(0);
			AuTerm initial = operator.getArgument(2);
			AuTerm function = operator.getArgument(3);
			
			Cell.Register counter = registerAlloc(Type.INT_TYPE);
			Cell.Register value = registerAlloc(Type.INT_TYPE);
			
			InternalRef counter_ref = new InternalRef();
			InternalRef value_ref = new InternalRef();
			p_cells.put(counter_ref, counter);
			p_cells.put(value_ref, value);
			
			// initialize counter and value
			p_method.visitInsn(Opcodes.ICONST_0);
			counter.store(p_method);
			computeTerm(initial);
			value.store(p_method);
			
			p_method.visitLabel(loop_label);
			// jump out of the loop if we finished
			counter.load(p_method);
			computeTerm(count);
			p_method.visitJumpInsn(Opcodes.IF_ICMPGE, fin_label);
			
			// compute the new value
			AuTerm applied = mkApply(mkApply(function,
					mkOperator(value_ref, mkConst(IntArithmetic.intType))),
					mkOperator(counter_ref, mkConst(IntArithmetic.intType)));
			computeTerm(applied);
			value.store(p_method);
			
			// increment the counter value
			counter.load(p_method);
			p_method.visitInsn(Opcodes.ICONST_1);
			p_method.visitInsn(Opcodes.IADD);
			counter.store(p_method);
			p_method.visitJumpInsn(Opcodes.GOTO, loop_label);
			
			// load the last value after we finished
			p_method.visitLabel(fin_label);
			value.load(p_method);
			
			p_cells.remove(counter_ref);
			p_cells.remove(value_ref);
			registerFree(counter);
			registerFree(value);
		}else throw new RuntimeException("Illegal operator " + desc);
	}
	
	/**
	 * Performs the mutation that is described by the given term.
	 * Puts the result of the mutation on the operand stack.
	 */
	/*public void mutateTerm(AuTerm term) {
		if(term instanceof AuOperator) {
			AuOperator operator = (AuOperator)term;
			mutateOperator(operator);
		}else throw new RuntimeException("Illegal term " + term);
	}*/
	
	
	/*public void mutateOperator(AuOperator operator) {
		AuOperator.Descriptor desc = operator.getDescriptor();
		if(desc == Mutation.lift) {
			computeTerm(operator.getArgument(1));
		}else if(desc == Mutation.seq) {
			Type type = TypeCompiler.compileType(operator.getArgument(0));
			
			mutateTerm(operator.getArgument(2));
			
			Cell.Register register = registerAlloc(type);
			register.store(p_method);
			
			InternalRef ref = new InternalRef();
			p_cells.put(ref, register);
			
			AuTerm applied = mkApply(operator.getArgument(3),
					mkOperator(ref, operator.getArgument(0)));
			mutateTerm(applied.reduce());
			
			p_cells.remove(ref);
			registerFree(register);
		}else if(desc == Io.print) {
			p_method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					Type.getObjectType("java/io/PrintStream").getDescriptor());
			computeTerm(operator.getArgument(1));
			p_method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println",
					Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE));	
		}else if(desc == Bool.ite) {
			Label else_label = new Label();
			Label follow_label = new Label();
			
			computeTerm(operator.getArgument(1));
			p_method.visitJumpInsn(Opcodes.IFEQ, else_label);
			mutateTerm(operator.getArgument(2));
			p_method.visitJumpInsn(Opcodes.GOTO, follow_label);
			p_method.visitLabel(else_label);
			mutateTerm(operator.getArgument(3));
			p_method.visitLabel(follow_label);
		}else throw new RuntimeException("Illegal operator " + desc);
	}*/
}
