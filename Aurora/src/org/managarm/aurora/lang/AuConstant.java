package org.managarm.aurora.lang;


public final class AuConstant extends AuTerm {
	public static class Descriptor {
		private AuTerm p_signature;
		
		public Descriptor(AuTerm type) {
			this.p_signature = type;
		}
		public AuTerm getSignature() {
			return p_signature;
		}
	}
	
	private Descriptor p_descriptor;
	
	AuConstant(AuTerm annotation, Descriptor descriptor) {
		super(annotation);
		this.p_descriptor = descriptor;
	}
	public Descriptor getDescriptor() {
		return p_descriptor;
	}
	@Override public String toString() {
		return p_descriptor.toString();
	}
	@Override public int hashCode() {
		return p_descriptor.hashCode();
	}
	@Override public boolean equals(Object object) {
		if(!(object instanceof AuConstant))
			return false;
		AuConstant other = (AuConstant)object;
		return p_descriptor.equals(other.p_descriptor);
	}

	@Override public AuTerm type() {
		return p_descriptor.p_signature;
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
