package org.managarm.aurora.lang;

import java.util.Arrays;

import org.managarm.aurora.util.TermMap;

public final class AuOperator extends AuTerm {
	public static abstract class Descriptor {
		private int arity;
		private AuTerm signature;
		
		public Descriptor(AuTerm type, int arity) {
			this.arity = arity;
			this.signature = type;
		}
		public int getArity() {
			return arity;
		}
		public AuTerm getSignature() {
			return signature;
		}
		
		protected abstract boolean primitive(AuTerm[] args);
		protected abstract AuTerm reduce(AuTerm[] args);
	}

	public static abstract class EvalDescriptor extends Descriptor {
		public EvalDescriptor(AuTerm type, int arity) {
			super(type, arity);
		}
		
		protected boolean primitive(AuTerm[] args) {
			return false;
		}
	}
	public static abstract class GroundDescriptor extends Descriptor {
		public GroundDescriptor(AuTerm type, int arity) {
			super(type, arity);
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
	
	private Descriptor descriptor;
	private AuTerm[] arguments;
	
	public AuOperator(AuTerm annotation,
			Descriptor descriptor, AuTerm[] arguments) {
		super(annotation);
		this.descriptor = descriptor;
		this.arguments = arguments;
	}
	public Descriptor getDescriptor() {
		return descriptor;
	}
	public int numArguments() {
		return arguments.length;
	}
	public AuTerm getArgument(int i) {
		return arguments[i];
	}
	
	@Override public String toString() {
		String string = descriptor.toString() + "[";
		for(int i = 0; i < arguments.length; i++) {
			if(i > 0)
				string += ", ";
			string += arguments[i];
		}
		return string + "]";
	}
	@Override public int hashCode() {
		return descriptor.hashCode()
				+ Arrays.hashCode(arguments);
	}
	@Override public boolean equals(Object object) {
		if(!(object instanceof AuOperator))
			return false;
		AuOperator other = (AuOperator)object;
		return descriptor.equals(other.descriptor)
				&& Arrays.equals(arguments, other.arguments);
	}

	@Override public AuTerm type() {
		AuTerm derived = descriptor.signature;
		for(int i = 0; i < descriptor.arity; i++) {
			AuPi pi = (AuPi)derived.reduce();
			AuTerm codomain = pi.getCodomain();
			derived = codomain.apply(0, arguments[i]);
		}
		return derived;
	}
	@Override public boolean primitive() {
		return descriptor.primitive(arguments);
	}
	@Override public AuTerm reduce() {
		AuTerm[] red_args = new AuTerm[descriptor.arity];
		for(int i = 0; i < descriptor.arity; i++)
			red_args[i] = arguments[i].reduce();
		return descriptor.reduce(red_args);
	}
	@Override public boolean wellformed() {
		if(arguments.length != descriptor.arity)
			return false;
		
		for(AuTerm argument : arguments)
			if(!argument.wellformed())
				return false;
		
		AuTerm derived = descriptor.signature;
		for(int i = 0; i < descriptor.arity; i++) {
			AuPi pi = (AuPi)derived.reduce();
			if(!AuTerm.congruent(pi.getBound(), arguments[i].type()))
				return false;
			
			AuTerm codomain = pi.getCodomain();
			derived = codomain.apply(0, arguments[i]);
		}
		return true;
	}
	@Override public AuTerm apply(int depth, AuTerm term) {
		AuTerm[] new_args = new AuTerm[arguments.length];
		for(int i = 0; i < arguments.length; i++)
			new_args[i] = arguments[i].apply(depth, term);
		return new AuOperator(this.getAnnotation(), descriptor, new_args);
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		AuTerm[] new_args = new AuTerm[arguments.length];
		for(int i = 0; i < arguments.length; i++)
			new_args[i] = arguments[i].embed(embed_depth, limit);
		return new AuOperator(this.getAnnotation(), descriptor, new_args);
	}
	@Override public AuTerm map(TermMap fun) {
		AuTerm[] new_args = new AuTerm[arguments.length];
		for(int i = 0; i < arguments.length; i++)
			new_args[i] = fun.map(arguments[i]);
		return new AuOperator(this.getAnnotation(),
				descriptor, new_args);
	}
	@Override public AuTerm replace(AuTerm subterm, AuTerm replacement) {
		if(this.equals(subterm))
			return replacement;
		AuTerm[] new_args = new AuTerm[arguments.length];
		for(int i = 0; i < arguments.length; i++)
			new_args[i] = arguments[i].replace(subterm, replacement);
		return new AuOperator(this.getAnnotation(), descriptor, new_args);
	}
	@Override public boolean verifyVariable(int depth, AuTerm type) {
		for(AuTerm argument : arguments)
			if(!argument.verifyVariable(depth, type))
				return false;
		return true;
	}
	@Override public boolean verifyClosed(int max_depth) {
		for(AuTerm argument : arguments)
			if(!argument.verifyClosed(max_depth))
				return false;
		return true;
	}
}
