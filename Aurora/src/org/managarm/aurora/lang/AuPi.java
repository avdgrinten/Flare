package org.managarm.aurora.lang;


public final class AuPi extends AuTerm {
	private AuTerm p_bound;
	private AuTerm p_codomain; 
	
	AuPi(AuTerm annotation, AuTerm bound, AuTerm codomain) {
		super(annotation);
		this.p_bound = bound;
		this.p_codomain = codomain;
	}
	public AuTerm getBound() {
		return p_bound;
	}
	public AuTerm getCodomain() {
		return p_codomain;
	}
	
	public String toStringExt() {
		StringBuilder str = new StringBuilder();
		if(this.getAnnotation() != null) {
			str.append("@");
			str.append(this.getAnnotation());
			str.append(' ');
		}
		str.append(p_bound);
		str.append(" -> ");
		if(p_codomain instanceof AuPi) {
			str.append(((AuPi)p_codomain).toStringExt());
		}else{
			str.append(p_codomain);
		}
		return str.toString();
	}
	@Override public String toString() {
		return "(" + toStringExt() + ")";
	}
	@Override public int hashCode() {
		return p_bound.hashCode() + p_codomain.hashCode();
	}
	@Override public boolean equals(Object object) {
		if(!(object instanceof AuPi))
			return false;
		AuPi other = (AuPi)object;
		return p_bound.equals(other.p_bound)
				&& p_codomain.equals(other.p_codomain);
	}

	@Override public AuTerm type() {
		return AuTerm.mkMeta();
	}
	@Override public boolean primitive() {
		return true;
	}
	@Override public AuTerm apply(int depth, AuTerm term) {
		return mkPiExt(this.getAnnotation(),
				p_bound.apply(depth, term),
				p_codomain.apply(depth + 1, term));
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		return mkPiExt(this.getAnnotation(),
				p_bound.embed(embed_depth, limit),
				p_codomain.embed(embed_depth, limit + 1));
	}
	@Override public boolean verifyVariable(int depth, AuTerm type) {
		return p_bound.verifyVariable(depth, type)
				&& p_codomain.verifyVariable(depth + 1, type.embed(1, 0));
	}
	@Override public boolean verifyClosed(int max_depth) {
		return p_bound.verifyClosed(max_depth)
				&& p_codomain.verifyClosed(max_depth + 1);
	}
}
