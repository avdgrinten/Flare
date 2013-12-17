package org.managarm.jet.compiler;

import org.managarm.aurora.builtin.Bool;
import org.managarm.aurora.builtin.IntArithmetic;
import org.managarm.aurora.builtin.Nil;
import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuPi;
import org.managarm.aurora.lang.AuTerm;
import org.objectweb.asm.Type;

public class TypeCompiler {
	private Namespace p_namespace;
	private Receiver p_receiver;
	
	public static Type getArrayType(Type type) {
		return Type.getType("[" + type.getDescriptor());
	}
	
	public TypeCompiler(Namespace namespace, Receiver receiver) {
		p_namespace = namespace;
		p_receiver = receiver;
	}
	
	public Type compile(AuTerm type) {
		if(type instanceof AuPi) {
			AuPi pi = (AuPi)type;
			
			ClassInfo implementation = new ClassInfo.PiInterface(p_namespace,
					pi.getBound(), pi.getCodomain());
			p_receiver.onDependency(implementation);
			
			return Type.getObjectType(implementation.getPath());
		}else if(type instanceof AuConstant) {
			AuConstant constant = (AuConstant)type;
			AuConstant.Descriptor descriptor = constant.getDescriptor();
			if(descriptor == Nil.nilType) {
				return Type.VOID_TYPE;
			}else if(descriptor == IntArithmetic.intType) {
				return Type.INT_TYPE;
			}else if(descriptor == Bool.boolType) {
				return Type.INT_TYPE;
			}
		}
		throw new RuntimeException("Illegal type " + type);
	}
}
