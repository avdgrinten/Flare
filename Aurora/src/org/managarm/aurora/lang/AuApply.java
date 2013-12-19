package org.managarm.aurora.lang;


public final class AuApply extends AuTerm {
	private AuTerm function;
	private AuTerm argument;
	
	AuApply(AuTerm annotation, AuTerm function, AuTerm argument) {
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
		AuPi pi = (AuPi)function.type();
		AuTerm codomain = pi.getCodomain();
		return codomain.apply(0, argument);
	}
	@Override public boolean primitive() {
		return false;
	}
	@Override public AuTerm apply(int depth, AuTerm term) {
		return mkApplyExt(this.getAnnotation(),
				function.apply(depth, term),
				argument.apply(depth, term));
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		return mkApplyExt(this.getAnnotation(),
				function.embed(embed_depth, limit),
				argument.embed(embed_depth, limit));
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
