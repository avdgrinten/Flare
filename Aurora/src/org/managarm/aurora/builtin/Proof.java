package org.managarm.aurora.builtin;

import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkVar;

import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class Proof {
	public static AuOperator.Descriptor proofType = new AuOperator.GroundDescriptor(
			mkPi(mkConst(Bool.boolType),
			 mkMeta()), 1) {
		@Override public String toString() { return "Proof"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			throw new AuOperator.NotReducibleException();
		}
	};
	
	public static AuConstant.Descriptor tautology = new AuConstant.Descriptor(
			mkOperator(proofType, mkConst(new Bool.BoolLit(true)))) {
		@Override public String toString() {
			return "tautology";
		}
	};
	
	public static AuOperator.Descriptor assume = new AuOperator.Descriptor(
			mkPi(mkConst(Bool.boolType),
			 mkOperator(proofType, mkVar(0, mkConst(Bool.boolType)))), 1) {
		@Override public String toString() {
			return "assume";
		}
			 
		@Override protected boolean primitive(AuTerm[] args) {
			return false;
		}
		@Override protected boolean reducible(AuTerm[] args) {
			return args[0].equals(mkConst(new Bool.BoolLit(true)));
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkConst(tautology);
		}
	};
}
