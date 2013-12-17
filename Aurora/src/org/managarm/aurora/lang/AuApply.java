package org.managarm.aurora.lang;

import org.managarm.aurora.util.TermMap;

public final class AuApply extends AuTerm {
	private AuTerm function;
	private AuTerm argument;
	
	public AuApply(AuTerm annotation, AuTerm function, AuTerm argument) {
		super(annotation);
		this.function = function;
		this.argument = argument;
	}
	public AuTerm getFunction() {
		return function;
	}
	public AuTerm getArgument() {
		return argument;
	}
	
	public String toStringExt() {
		StringBuilder str = new StringBuilder();
		if(this.getAnnotation() != null) {
			str.append("@");
			str.append(this.getAnnotation());
			str.append(' ');
		}
		if(function instanceof AuApply) {
			str.append(((AuApply)function).toStringExt());
		}else{
			str.append(function);
		}
		str.append(' ');
		str.append(argument);
		return str.toString();
	}
	@Override public String toString() {
		return "(" + toStringExt() + ")";
	}
	@Override public int hashCode() {
		return function.hashCode() + argument.hashCode();
	}
	@Override public boolean equals(Object object) {
		if(!(object instanceof AuApply))
			return false;
		AuApply other = (AuApply)object;
		return function.equals(other.function)
				&& argument.equals(other.argument);
	}
	
	@Override public AuTerm type() {
		AuPi pi = (AuPi)function.type().reduce();
		AuTerm codomain = pi.getCodomain();
		return codomain.apply(0, argument);
	}
	@Override public boolean primitive() {
		return false;
	}
	@Override public AuTerm reduce() {
		AuTerm func_red = function.reduce();
		if(!func_red.primitive())
			return new AuApply(this.getAnnotation(),
					func_red, argument.reduce());
		
		AuLambda lambda = (AuLambda)func_red;
		AuTerm expr = lambda.getExpr();
		AuTerm substituted = expr.apply(0, argument);
		return substituted.reduce();
	}
	@Override public boolean wellformed() {
		if(!function.wellformed() || !argument.wellformed())
			return false;
		AuTerm func_type = function.type().reduce();
		if(!(func_type instanceof AuPi))
			return false;
		AuPi pi = (AuPi)func_type;
		if(!AuTerm.congruent(pi.getBound(), argument.type()))
			return false;
		return true;
	}
	@Override public AuTerm apply(int depth, AuTerm term) {
		return new AuApply(this.getAnnotation(),
				function.apply(depth, term),
				argument.apply(depth, term));
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		return new AuApply(this.getAnnotation(),
				function.embed(embed_depth, limit),
				argument.embed(embed_depth, limit));
	}
	@Override public AuTerm map(TermMap fun) {
		return new AuApply(this.getAnnotation(),
				fun.map(function),
				fun.map(argument));
	}
	@Override public AuTerm replace(AuTerm subterm, AuTerm replacement) {
		if(this.equals(subterm))
			return replacement;
		return new AuApply(this.getAnnotation(),
				function.replace(subterm, replacement),
				argument.replace(subterm, replacement));
	}
	@Override public boolean verifyVariable(int depth, AuTerm type) {
		return function.verifyVariable(depth, type)
				&& argument.verifyVariable(depth, type);
	}
	@Override public boolean verifyClosed(int max_depth) {
		return function.verifyClosed(max_depth)
				&& argument.verifyClosed(max_depth);
	}
}
