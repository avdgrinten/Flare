package org.managarm.aurora.lang;


public final class AuMeta extends AuTerm {
	AuMeta(AuTerm annotation) {
		super(annotation);
	}
	
	@Override public String toString() {
		return "Meta";
	}
	@Override public int hashCode() {
		return 1;
	}
	@Override public boolean equals(Object object) {
		return object instanceof AuMeta;
	}

	@Override public AuTerm type() {
		return this;
	}
	@Override public boolean primitive() {
		return true;
	}
	
	@Override public AuTerm apply(int depth, AuTerm term) {
		return this;
	}
	@Override public AuTerm embed(int embed_depth, int limit) {
		return this;
	}
	@Override public boolean verifyVariable(int depth, AuTerm type) {
		return true;
	}
	@Override public boolean verifyClosed(int max_depth) {
		return true;
	}
}
