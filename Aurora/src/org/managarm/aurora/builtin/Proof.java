package org.managarm.aurora.builtin;

import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkConst;

import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class Proof {
	public static AuOperator.Descriptor proofType = new AuOperator.GroundDescriptor(
			mkPi(mkConst(Bool.boolType),
			 mkMeta()), 1) {
		@Override public String toString() { return "Proof"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	
	public static AuConstant.Descriptor tautology = new AuConstant.Descriptor(
			mkOperator(proofType, mkConst(new Bool.BoolLit(true))));
}
