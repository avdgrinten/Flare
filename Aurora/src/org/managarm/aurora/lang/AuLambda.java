package org.managarm.aurora.lang;

import org.managarm.aurora.util.TermMap;

public final class AuLambda extends AuTerm {
	private AuTerm bound;
	private AuTerm expr;
	
	public AuLambda(AuTerm annotation, AuTerm bound, AuTerm expr) {
		super(annotation);
		this.bound = bound;
		this.expr = expr;
	}
	public AuTerm getBound() {
		return bound;
	}
	public AuTerm getExpr() {
		return expr;
	}
	
	public String toStringExt() {
		StringBuilder str = new StringBuilder();
		if(this.getAnnotation() != null) {
			str.append("@");
			str.append(this.getAnnotation());
			str.append(' ');
		}
		str.append(bound);
		str.append(" => ");
		if(expr instanceof AuLambda) {
			str.append(((AuLambda)expr).toStringExt());
		}else{
			str.append(expr);
		}
		return str.toString();
	}
	@Override public String toString() {
		return "(" + toStringExt() + ")";
	}
	@Override public int hashCode() {
		return bound.hashCode() + expr.hashCode();
	}
	@Override public boolean equals(Object object) {
		if(!(object instanceof AuLambda))
			return false;
		AuLambda other = (AuLambda)object;
		return bound.equals(other.bound)
				&& expr.equals(other.expr);
	}

	@Override public AuTerm type() {
		//FIXME:
		return AuTerm.mkPiExt(this.getAnnotation(), bound, expr.type());
	}
	@Override public boolean primitive() {
		return true;
	}
	@Override public AuTerm reduce() {
		return new AuLambda(this.getAnnotation(),
				bound.reduce(), expr.reduce());
	}
	@Override public boolean wellformed() {
		if(!bound.wellformed() || !expr.wellformed())
			return false;
		return expr.verifyVariable(0, bound.embed(1, 0));
	}
	@Override public AuTerm apply(int depth, AuTerm term) {
		return new AuLambda(this.getAnnotation(),
				bound.apply(depth, term),
				expr.apply(depth + 1, term));
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		return new AuLambda(this.getAnnotation(),
				bound.embed(embed_depth, limit),
				expr.embed(embed_depth, limit + 1));
	}
	@Override public AuTerm map(TermMap fun) {
		return new AuLambda(this.getAnnotation(),
				fun.map(bound),
				fun.map(expr));
	}
	@Override public AuTerm replace(AuTerm subterm, AuTerm replacement) {
		if(this.equals(subterm))
			return replacement;
		return new AuLambda(this.getAnnotation(),
				bound.replace(subterm, replacement),
				expr.replace(subterm, replacement));
	}
	@Override public boolean verifyVariable(int depth, AuTerm type) {
		return bound.verifyVariable(depth, type)
				&& expr.verifyVariable(depth + 1, type.embed(1, 0));
	}
	@Override public boolean verifyClosed(int max_depth) {
		return bound.verifyClosed(max_depth)
				&& expr.verifyClosed(max_depth + 1);
	}
}
