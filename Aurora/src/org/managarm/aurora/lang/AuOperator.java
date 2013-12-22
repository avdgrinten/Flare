package org.managarm.aurora.lang;

import java.util.Arrays;

public final class AuOperator extends AuTerm {
	public static abstract class Descriptor {
		private int p_arity;
		private AuTerm p_signature;
		
		public Descriptor(AuTerm type, int arity) {
			this.p_arity = arity;
			this.p_signature = type;
		}
		public int getArity() {
			return p_arity;
		}
		public AuTerm getSignature() {
			return p_signature;
		}

		protected abstract boolean primitive(AuTerm[] args);
		protected abstract boolean reducible(AuTerm[] args);
		protected abstract AuTerm reduce(AuTerm[] args);
	}
	
	public static class NotReducibleException extends RuntimeException {
		private static final long serialVersionUID = 5953173928263057491L;
	}

	public static abstract class EvalDescriptor extends Descriptor {
		public EvalDescriptor(AuTerm type, int arity) {
			super(type, arity);
		}
		
		protected boolean reducible(AuTerm[] args) {
			for(int i = 0; i < args.length; i++)
				if(!args[i].primitive())
					return false;
			return true;
		}
		protected boolean primitive(AuTerm[] args) {
			return false;
		}
	}
	public static abstract class GroundDescriptor extends Descriptor {
		public GroundDescriptor(AuTerm type, int arity) {
			super(type, arity);
		}
		
		protected boolean reducible(AuTerm[] args) {
			return false;
		}
		protected boolean primitive(AuTerm[] args) {
			for(AuTerm arg : args)
				if(!arg.primitive())
					return false;
			return true;
		}
	}
	
	public static AuTerm makeBuiltin(AuOperator.Descriptor descriptor) {
		int arity = descriptor.getArity();
		
		AuTerm[] arg_types = new AuTerm[arity];
		AuTerm rem_type = descriptor.getSignature();
		for(int i = 0; i < arity; i++) {
			AuPi item = (AuPi)rem_type; 
			arg_types[i] = item.getBound();
			rem_type = item.getCodomain();
		}
		
		AuTerm[] args = new AuTerm[arity];
		for(int i = 0; i < arity; i++)
			args[i] = mkVar(arity - i - 1,
					arg_types[i].embed(arity - i, 0));
		AuTerm defn = mkOperator(descriptor, args);
		
		for(int i = arity - 1; i >= 0; i--)
			defn = mkLambda(arg_types[i], defn);
		return defn;
	}
	
	private Descriptor p_descriptor;
	private AuTerm[] p_arguments;
	
	AuOperator(AuTerm annotation,
			Descriptor descriptor, AuTerm[] arguments) {
		super(annotation);
		this.p_descriptor = descriptor;
		this.p_arguments = arguments;
	}
	public Descriptor getDescriptor() {
		return p_descriptor;
	}
	public int numArguments() {
		return p_arguments.length;
	}
	public AuTerm getArgument(int i) {
		return p_arguments[i];
	}
	
	@Override public String toString() {
		String string = p_descriptor.toString() + "[";
		for(int i = 0; i < p_arguments.length; i++) {
			if(i > 0)
				string += ", ";
			string += p_arguments[i];
		}
		return string + "]";
	}
	@Override public int hashCode() {
		return p_descriptor.hashCode()
				+ Arrays.hashCode(p_arguments);
	}
	@Override public boolean equals(Object object) {
		if(!(object instanceof AuOperator))
			return false;
		AuOperator other = (AuOperator)object;
		return p_descriptor.equals(other.p_descriptor)
				&& Arrays.equals(p_arguments, other.p_arguments);
	}

	@Override public AuTerm type() {
		AuTerm derived = p_descriptor.p_signature;
		for(int i = 0; i < p_descriptor.p_arity; i++) {
			AuPi pi = (AuPi)derived;
			AuTerm codomain = pi.getCodomain();
			derived = codomain.apply(0, p_arguments[i]);
		}
		return derived;
	}
	@Override public boolean primitive() {
		return p_descriptor.primitive(p_arguments);
	}
	@Override public AuTerm apply(int depth, AuTerm term) {
		AuTerm[] new_args = new AuTerm[p_arguments.length];
		for(int i = 0; i < p_arguments.length; i++)
			new_args[i] = p_arguments[i].apply(depth, term);
		return mkOperatorExt(this.getAnnotation(), p_descriptor, new_args);
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		AuTerm[] new_args = new AuTerm[p_arguments.length];
		for(int i = 0; i < p_arguments.length; i++)
			new_args[i] = p_arguments[i].embed(embed_depth, limit);
		return mkOperatorExt(this.getAnnotation(), p_descriptor, new_args);
	}
	@Override public boolean verifyVariable(int depth, AuTerm type) {
		for(AuTerm argument : p_arguments)
			if(!argument.verifyVariable(depth, type))
				return false;
		return true;
	}
	@Override public boolean verifyClosed(int max_depth) {
		for(AuTerm argument : p_arguments)
			if(!argument.verifyClosed(max_depth))
				return false;
		return true;
	}
}
