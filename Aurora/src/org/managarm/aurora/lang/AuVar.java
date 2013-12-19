package org.managarm.aurora.lang;


public final class AuVar extends AuTerm {
	private int p_depth;
	private AuTerm p_type;
	
	AuVar(AuTerm annotation, int depth, AuTerm type) {
		super(annotation);
		this.p_depth = depth;
		this.p_type = type;
	}
	public int getDepth() {
		return p_depth;
	}
	public AuTerm getType() {
		return p_type;
	}
	@Override public String toString() {
		return "$" + p_depth + ":" + p_type;
	}
	@Override public int hashCode() {
		return p_depth + p_type.hashCode();
	}
	@Override public boolean equals(Object object) {
		if(!(object instanceof AuVar))
			return false;
		AuVar other = (AuVar)object;
		return p_depth == other.p_depth
				&& p_type.equals(other.p_type);
	}
	
	@Override public AuTerm type() {
		return p_type;
	}
	@Override public boolean primitive() {
		return false;
	}
	@Override public AuTerm apply(int substitute_depth, AuTerm term) {
		if(p_depth < substitute_depth) {
			return mkVarExt(this.getAnnotation(),
					p_depth, p_type.apply(substitute_depth, term));
		}else if(p_depth > substitute_depth) {
			return mkVarExt(this.getAnnotation(),
					p_depth - 1, p_type.apply(substitute_depth, term));
		}else return term.embed(substitute_depth, 0);
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		if(p_depth >= limit) {
			return mkVarExt(this.getAnnotation(),
					p_depth + embed_depth, p_type.embed(embed_depth, limit));
		}else return mkVarExt(this.getAnnotation(),
					p_depth, p_type.embed(embed_depth, limit));
	}
	@Override public boolean verifyVariable(int verify_depth, AuTerm verify_type) {
		if(p_depth == verify_depth)
			if(!p_type.equals(verify_type))
				return false;
		return p_type.verifyVariable(verify_depth, verify_type);
	}
	@Override public boolean verifyClosed(int max_depth) {
		if(p_depth > max_depth)
			return false;
		return p_type.verifyClosed(max_depth);
	}
}
