package org.managarm.aurora.lang;

import org.managarm.aurora.util.TermMap;

public final class AuVar extends AuTerm {
	private int depth;
	private AuTerm type;
	
	public AuVar(AuTerm annotation, int depth, AuTerm type) {
		super(annotation);
		this.depth = depth;
		this.type = type;
	}
	public int getDepth() {
		return depth;
	}
	public AuTerm getType() {
		return type;
	}
	@Override public String toString() {
		return "$" + depth + ":" + type;
	}
	@Override public int hashCode() {
		return depth + type.hashCode();
	}
	@Override public boolean equals(Object object) {
		if(!(object instanceof AuVar))
			return false;
		AuVar other = (AuVar)object;
		return depth == other.depth
				&& type.equals(other.type);
	}
	
	@Override public AuTerm type() {
		return type;
	}
	@Override public boolean primitive() {
		return false;
	}
	@Override public AuTerm reduce() {
		return this;
	}
	@Override public boolean wellformed() {
		return type.wellformed();
	}
	@Override public AuTerm apply(int substitute_depth, AuTerm term) {
		if(depth < substitute_depth) {
			return new AuVar(this.getAnnotation(),
					depth, type.apply(substitute_depth, term));
		}else if(depth > substitute_depth) {
			return new AuVar(this.getAnnotation(),
					depth - 1, type.apply(substitute_depth, term));
		}else return term.embed(substitute_depth, 0);
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		if(depth >= limit) {
			return new AuVar(this.getAnnotation(),
					depth + embed_depth, type.embed(embed_depth, limit));
		}else return new AuVar(this.getAnnotation(),
					depth, type.embed(embed_depth, limit));
	}
	@Override public AuTerm map(TermMap fun) {
		return new AuVar(this.getAnnotation(),
				depth, fun.map(type));
	}
	@Override public AuTerm replace(AuTerm subterm, AuTerm replacement) {
		if(this.equals(subterm))
			return replacement;
		return new AuVar(this.getAnnotation(),
				depth, type.replace(subterm, replacement));
	}
	@Override public boolean verifyVariable(int verify_depth, AuTerm verify_type) {
		if(depth == verify_depth)
			if(!AuTerm.congruent(type, verify_type))
				return false;
		return type.verifyVariable(verify_depth, verify_type);
	}
	@Override public boolean verifyClosed(int max_depth) {
		if(depth > max_depth)
			return false;
		return type.verifyClosed(max_depth);
	}
}
