package org.managarm.aurora.builtin;

import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkVar;

import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class References {
	public static AuOperator.Descriptor refType = new AuOperator.GroundDescriptor(
			mkPi(mkMeta(),
			 mkMeta()), 1) {
		@Override public String toString() { return "Ref"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	
	private static class RefReadClass extends AuOperator.GroundDescriptor {
		RefReadClass() {
			super(mkPi(mkMeta(),
			 mkPi(mkOperator(refType, mkVar(0, mkMeta())),
			  mkOperator(Mutation.mutatorType, mkVar(1, mkMeta())))), 2);
		}
		@Override public String toString() { return "refRead"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	public static AuOperator.Descriptor refRead = new RefReadClass();
	
	private static class RefWriteClass extends AuOperator.GroundDescriptor {
		public RefWriteClass() {
			super(mkPi(mkMeta(),
			 mkPi(mkOperator(refType, mkVar(0, mkMeta())),
			  mkPi(mkVar(1, mkMeta()),
			   mkOperator(Mutation.mutatorType, mkConst(Nil.nilType))))), 3);
		}
		@Override public String toString() { return "refWrite"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	public static AuOperator.Descriptor refWrite = new RefWriteClass();
	
	public static class ImmMemory extends AuConstant.Descriptor {
		public static ImmMemory extract(AuTerm term) {
			return (ImmMemory)((AuConstant)term).getDescriptor(); 
		}
		
		private AuTerm p_value;
		
		public ImmMemory(AuTerm type, AuTerm value) {
			super(type);
			p_value = value;
		}
		public AuTerm read() {
			return p_value;
		}
		public void write(AuTerm value) {
			p_value = value;
		}
	}
	
	public static class ImmRef extends AuConstant.Descriptor {
		public static ImmRef extract(AuTerm term) {
			return (ImmRef)((AuConstant)term).getDescriptor(); 
		}
		
		private ImmMemory p_memory;
		
		public ImmRef(ImmMemory memory) {
			super(mkOperator(refType, memory.getSignature()));
			p_memory = memory;
		}
		public ImmMemory deref() {
			return p_memory;
		}
	}
}
