package org.managarm.jet.compiler;

import static org.managarm.aurora.lang.AuTerm.mkOperator;

import java.util.ArrayList;
import java.util.List;

import org.managarm.aurora.lang.AuLambda;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.util.Descriptor;
import org.managarm.aurora.util.Descriptor.RecordPath;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MemberBuilder {
	private Namespace p_namespace;
	private Receiver p_receiver;
	private ClassVisitor p_class;
	private TypeCompiler p_typeCompiler;
	
	public MemberBuilder(Namespace namespace, Receiver receiver,
			ClassVisitor clazz) {
		p_namespace = namespace;
		p_receiver = receiver;
		p_class = clazz;
		p_typeCompiler = new TypeCompiler(namespace, receiver);
	}
	
	public void buildFunction(AuTerm symbol) {
		int arity = 1;
		
		Descriptor desc = new Descriptor(symbol);
		String symb_name = desc.getString(new RecordPath("name"));
		AuTerm symb_defn = desc.get(new RecordPath("defn"));
		
		List<AuTerm> arg_types = new ArrayList<AuTerm>();
		List<InternalRef> arg_refs = new ArrayList<InternalRef>();
		List<Type> jvm_argtypes = new ArrayList<Type>();
				
		AuTerm res_defn = symb_defn;
		for(int i = 0; i < arity; i++) {
			AuLambda lambda = (AuLambda)res_defn;
			AuTerm bound = lambda.getBound();
			arg_types.add(bound);
			jvm_argtypes.add(p_typeCompiler.compile(bound));
			
			InternalRef ref = new InternalRef();
			arg_refs.add(ref);
			
			res_defn = lambda.getExpr().apply(0, mkOperator(ref, bound));
		}
		AuTerm res_type = res_defn.type();
		
		Type type = Type.getMethodType(p_typeCompiler.compile(res_type),
				jvm_argtypes.toArray(new Type[jvm_argtypes.size()]));
		
		int access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC; 
		MethodVisitor method = p_class.visitMethod(access,
				symb_name, type.getDescriptor(), null, null);
				
		CodeBuilder builder = new CodeBuilder(p_namespace, p_receiver, method);
		builder.setupRegisterPointer(arity);
		for(int i = 0; i < arity; i++)
			builder.setupExternCell(arg_refs.get(i),
					new Cell.Register(jvm_argtypes.get(i), i));
		
		builder.computeTerm(res_defn);
		emitReturn(type.getReturnType(), method);
		
		method.visitMaxs(0, 0);
		method.visitEnd();
	}
	
	public void buildLambda(AuTerm bound, AuTerm expr) {
		Type jvm_arg_type = p_typeCompiler.compile(bound);
		Type jvm_res_type = p_typeCompiler.compile(expr.type());
		
		Type type = Type.getMethodType(jvm_res_type, jvm_arg_type);
		
		int access = Opcodes.ACC_PUBLIC; 
		MethodVisitor method = p_class.visitMethod(access,
				"compute", type.getDescriptor(), null, null);
				
		CodeBuilder builder = new CodeBuilder(p_namespace, p_receiver, method);
		builder.setupRegisterPointer(1);
		
		builder.computeTerm(expr);
		emitReturn(jvm_res_type, method);
		
		method.visitMaxs(0, 0);
		method.visitEnd();
	}
	
	private static void emitReturn(Type type, MethodVisitor method) {
		if(type == Type.INT_TYPE) {
			method.visitInsn(Opcodes.IRETURN);
		}else if(type.getSort() == Type.OBJECT) {
			method.visitInsn(Opcodes.ARETURN);
		}else throw new RuntimeException("Illegal return type");
	}
}
