package org.managarm.aurora.builtin;

import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkVar;

import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class Locals {
	private static class LocalAllocClass extends AuOperator.GroundDescriptor {
		LocalAllocClass() {
			super(mkPi(mkMeta(),
			 mkPi(mkMeta(),
			  mkPi(mkPi(mkOperator(References.refType, mkVar(1, mkMeta())),
					  mkOperator(Mutation.mutatorType, mkVar(1, mkMeta()))),
				mkOperator(Mutation.mutatorType, mkVar(1, mkMeta()))))), 3);
		}
		
		@Override public String toString() { return "localAlloc"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	}
	public static AuOperator.Descriptor localAlloc = new LocalAllocClass();
}
