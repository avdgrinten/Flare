package org.managarm.jet.compiler;

import java.io.PrintWriter;

import org.managarm.aurora.io.BinaryWriter;
import org.managarm.aurora.lang.AuTerm;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;

public abstract class ClassInfo {
	public static class ModuleClass extends ClassInfo {
		private String p_module;
		private AuTerm[] p_symbols;
		
		public ModuleClass(Namespace namespace,
				String module, AuTerm[] symbols) {
			super(namespace);
			p_module = module;
			p_symbols = symbols;
		}

		@Override public String getPath() {
			return p_module;
		}
		@Override public ClassFile buildCode(Receiver receiver) {
			ClassWriter clazz = new ClassWriter(ClassWriter.COMPUTE_MAXS
					| ClassWriter.COMPUTE_FRAMES);
			
			clazz.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, p_module,
					null, "java/lang/Object", null);
			
			for(AuTerm symbol : p_symbols) {
				MemberBuilder builder = new MemberBuilder(this.getNamespace(),
						receiver, clazz);
				builder.buildFunction(symbol);
			}
			
			clazz.visitEnd();

			TraceClassVisitor trace = new TraceClassVisitor(new PrintWriter(System.out));
			ClassReader reader = new ClassReader(clazz.toByteArray());
			reader.accept(trace, 0);
			
			return new ClassFile(clazz.toByteArray());
		}
	}
	
	public static class PiInterface extends ClassInfo {
		private AuTerm p_bound;
		private AuTerm p_codomain;
		
		public PiInterface(Namespace namespace, AuTerm bound,
				AuTerm codomain) {
			super(namespace);
			p_bound = bound;
			p_codomain = codomain;
		}
		
		@Override public String getPath() {
			return "aurora/pi/" + BinaryWriter.toString(p_bound)
					+ BinaryWriter.toString(p_codomain);
		}
		@Override public ClassFile buildCode(Receiver receiver) {
			ClassWriter clazz = new ClassWriter(0);
			
			TypeCompiler type_compiler = new TypeCompiler(this.getNamespace(),
					receiver);
			
			int cls_access = Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE
					| Opcodes.ACC_ABSTRACT;
			clazz.visit(Opcodes.V1_5, cls_access, this.getPath(),
					null, "java/lang/Object", null);

			int mth_access = Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT; 
			clazz.visitMethod(mth_access,
					"compute", Type.getMethodDescriptor(
							type_compiler.compile(p_codomain),
							type_compiler.compile(p_bound)), null, null);
			
			return new ClassFile(clazz.toByteArray());
		}
	}
	
	public static class LambdaClass extends ClassInfo {
		private String p_uniqueId;
		private AuTerm p_bound;
		private AuTerm p_expr;
		
		public LambdaClass(Namespace namespace, String unique_id,
				AuTerm bound, AuTerm expr) {
			super(namespace);
			p_uniqueId = unique_id;
			p_bound = bound;
			p_expr = expr;
		}
		
		@Override public String getPath() {
			return "aurora/lambda/" + p_uniqueId;
		}
		@Override public ClassFile buildCode(Receiver receiver) {
			ClassWriter clazz = new ClassWriter(ClassWriter.COMPUTE_MAXS
					| ClassWriter.COMPUTE_FRAMES);
			
			String[] interfaces = new String[] {
					"aurora/pi/" + BinaryWriter.toString(p_bound)
						+ BinaryWriter.toString(p_expr.type())
				};
			
			int cls_access = Opcodes.ACC_PUBLIC;
			clazz.visit(Opcodes.V1_5, cls_access, this.getPath(),
					null, "java/lang/Object", interfaces);

			int init_access = Opcodes.ACC_PUBLIC;
			MethodVisitor init_method = clazz.visitMethod(init_access,
					"<init>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
			init_method.visitIntInsn(Opcodes.ALOAD, 0);
			init_method.visitMethodInsn(Opcodes.INVOKESPECIAL,
					"java/lang/Object", "<init>",
					Type.getMethodDescriptor(Type.VOID_TYPE));
			init_method.visitInsn(Opcodes.RETURN);
			init_method.visitMaxs(0, 0);
			init_method.visitEnd();
			
			MemberBuilder builder = new MemberBuilder(this.getNamespace(),
					receiver, clazz);
			builder.buildLambda(p_bound, p_expr);
			
			return new ClassFile(clazz.toByteArray());
		}
	}
	
	private Namespace p_namespace;
	
	public ClassInfo(Namespace namespace) {
		p_namespace = namespace;
	}
	public Namespace getNamespace() {
		return p_namespace;
	}
	
	public abstract String getPath();
	public abstract ClassFile buildCode(Receiver receiver);
}
