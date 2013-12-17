package org.managarm.aurora.lang;

import org.managarm.aurora.util.TermMap;

public final class AuPi extends AuTerm {
	private AuTerm bound;
	private AuTerm codomain; 
	
	public AuPi(AuTerm annotation, AuTerm bound, AuTerm codomain) {
		super(annotation);
		this.bound = bound;
		this.codomain = codomain;
	}
	public AuTerm getBound() {
		return bound;
	}
	public AuTerm getCodomain() {
		return codomain;
	}
	
	public String toStringExt() {
		StringBuilder str = new StringBuilder();
		if(this.getAnnotation() != null) {
			str.append("@");
			str.append(this.getAnnotation());
			str.append(' ');
		}
		str.append(bound);
		str.append(" -> ");
		if(codomain instanceof AuPi) {
			str.append(((AuPi)codomain).toStringExt());
		}else{
			str.append(codomain);
		}
		return str.toString();
	}
	@Override public String toString() {
		return "(" + toStringExt() + ")";
	}
	@Override public int hashCode() {
		return bound.hashCode() + codomain.hashCode();
	}
	@Override public boolean equals(Object object) {
		if(!(object instanceof AuPi))
			return false;
		AuPi other = (AuPi)object;
		return bound.equals(other.bound)
				&& codomain.equals(other.codomain);
	}

	@Override public AuTerm type() {
		return AuTerm.mkMeta();
	}
	@Override public boolean primitive() {
		return true;
	}
	@Override public AuTerm reduce() {
		return new AuPi(this.getAnnotation(),
				bound, codomain.reduce());
	}
	@Override public boolean wellformed() {
		if(!bound.wellformed() || !codomain.wellformed())
			return false;
		return codomain.verifyVariable(0, bound.embed(1, 0));
	}
	@Override public AuTerm apply(int depth, AuTerm term) {
		return new AuPi(this.getAnnotation(),
				bound.apply(depth, term),
				codomain.apply(depth + 1, term));
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		return new AuPi(this.getAnnotation(),
				bound.embed(embed_depth, limit),
				codomain.embed(embed_depth, limit + 1));
	}
	@Override public AuTerm map(TermMap fun) {
		return new AuPi(this.getAnnotation(),
				fun.map(bound),
				fun.map(codomain));
	}
	@Override public AuTerm replace(AuTerm subterm, AuTerm replacement) {
		if(this.equals(subterm))
			return replacement;
		return new AuPi(this.getAnnotation(),
				bound.replace(subterm, replacement),
				codomain.replace(subterm, replacement));
	}
	@Override public boolean verifyVariable(int depth, AuTerm type) {
		return bound.verifyVariable(depth, type)
				&& codomain.verifyVariable(depth + 1, type.embed(1, 0));
	}
	@Override public boolean verifyClosed(int max_depth) {
		return bound.verifyClosed(max_depth)
				&& codomain.verifyClosed(max_depth + 1);
	}
}
