package org.managarm.aurora.lang;


public final class AuLambda extends AuTerm {
	private AuTerm p_bound;
	private AuTerm p_expr;
	
	AuLambda(AuTerm annotation, AuTerm bound, AuTerm expr) {
		super(annotation);
		this.p_bound = bound;
		this.p_expr = expr;
	}
	public AuTerm getBound() {
		return p_bound;
	}
	public AuTerm getExpr() {
		return p_expr;
	}
	
	public String toStringExt() {
		StringBuilder str = new StringBuilder();
		if(this.getAnnotation() != null) {
			str.append("@");
			str.append(this.getAnnotation());
			str.append(' ');
		}
		str.append(p_bound);
		str.append(" => ");
		if(p_expr instanceof AuLambda) {
			str.append(((AuLambda)p_expr).toStringExt());
		}else{
			str.append(p_expr);
		}
		return str.toString();
	}
	@Override public String toString() {
		return "(" + toStringExt() + ")";
	}
	@Override public int hashCode() {
		return p_bound.hashCode() + p_expr.hashCode();
	}
	@Override public boolean equals(Object object) {
		if(!(object instanceof AuLambda))
			return false;
		AuLambda other = (AuLambda)object;
		return p_bound.equals(other.p_bound)
				&& p_expr.equals(other.p_expr);
	}

	@Override public AuTerm type() {
		//FIXME:
		return AuTerm.mkPiExt(this.getAnnotation(), p_bound, p_expr.type());
	}
	@Override public boolean primitive() {
		return true;
	}
	@Override public AuTerm apply(int depth, AuTerm term) {
		return mkLambdaExt(this.getAnnotation(),
				p_bound.apply(depth, term),
				p_expr.apply(depth + 1, term));
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		return mkLambdaExt(this.getAnnotation(),
				p_bound.embed(embed_depth, limit),
				p_expr.embed(embed_depth, limit + 1));
	}
	@Override public boolean verifyVariable(int depth, AuTerm type) {
		return p_bound.verifyVariable(depth, type)
				&& p_expr.verifyVariable(depth + 1, type.embed(1, 0));
	}
	@Override public boolean verifyClosed(int max_depth) {
		return p_bound.verifyClosed(max_depth)
				&& p_expr.verifyClosed(max_depth + 1);
	}
}
