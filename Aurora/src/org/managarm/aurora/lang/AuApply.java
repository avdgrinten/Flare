package org.managarm.aurora.lang;


public final class AuApply extends AuTerm {
	private AuTerm p_function;
	private AuTerm p_argument;
	
	AuApply(AuTerm annotation, AuTerm function, AuTerm argument) {
		super(annotation);
		this.p_function = function;
		this.p_argument = argument;
	}
	public AuTerm getFunction() {
		return p_function;
	}
	public AuTerm getArgument() {
		return p_argument;
	}
	
	public String toStringExt() {
		StringBuilder str = new StringBuilder();
		if(this.getAnnotation() != null) {
			str.append("@");
			str.append(this.getAnnotation());
			str.append(' ');
		}
		if(p_function instanceof AuApply) {
			str.append(((AuApply)p_function).toStringExt());
		}else{
			str.append(p_function);
		}
		str.append(' ');
		str.append(p_argument);
		return str.toString();
	}
	@Override public String toString() {
		return "(" + toStringExt() + ")";
	}
	@Override public int hashCode() {
		return p_function.hashCode() + p_argument.hashCode();
	}
	@Override public boolean equals(Object object) {
		if(!(object instanceof AuApply))
			return false;
		AuApply other = (AuApply)object;
		return p_function.equals(other.p_function)
				&& p_argument.equals(other.p_argument);
	}
	
	@Override public AuTerm type() {
		AuPi pi = (AuPi)p_function.type();
		AuTerm codomain = pi.getCodomain();
		return codomain.apply(0, p_argument);
	}
	@Override public boolean primitive() {
		return false;
	}
	@Override public AuTerm apply(int depth, AuTerm term) {
		return mkApplyExt(this.getAnnotation(),
				p_function.apply(depth, term),
				p_argument.apply(depth, term));
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		return mkApplyExt(this.getAnnotation(),
				p_function.embed(embed_depth, limit),
				p_argument.embed(embed_depth, limit));
	}
	@Override public boolean verifyVariable(int depth, AuTerm type) {
		return p_function.verifyVariable(depth, type)
				&& p_argument.verifyVariable(depth, type);
	}
	@Override public boolean verifyClosed(int max_depth) {
		return p_function.verifyClosed(max_depth)
				&& p_argument.verifyClosed(max_depth);
	}
}
