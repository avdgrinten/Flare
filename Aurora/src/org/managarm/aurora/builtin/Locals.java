package org.managarm.aurora.builtin;

import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkVar;

import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class Locals {
	public static AuOperator.Descriptor localType = new AuOperator.GroundDescriptor(
			mkPi(mkMeta(),
			 mkMeta()), 1) {
		@Override public String toString() { return "Local"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	
	public static final AuOperator.Descriptor localAlloc
		= new AuOperator.GroundDescriptor(
			mkPi(mkMeta(),
			 mkPi(mkMeta(),
			  mkPi(mkPi(mkOperator(localType, mkVar(1, mkMeta())),
					  mkOperator(Mutation.mutatorType, mkVar(1, mkMeta()))),
				mkOperator(Mutation.mutatorType, mkVar(1, mkMeta()))))), 3) {
		
		@Override public String toString() { return "localAlloc"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	
	public static final AuOperator.Descriptor localRead
		= new AuOperator.GroundDescriptor(
			mkPi(mkMeta(),
			 mkPi(mkOperator(localType, mkVar(0, mkMeta())),
			  mkOperator(Mutation.mutatorType, mkVar(1, mkMeta())))), 2) {

		@Override public String toString() { return "localRead"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	
	public static final AuOperator.Descriptor localWrite
		= new AuOperator.GroundDescriptor(
			mkPi(mkMeta(),
			 mkPi(mkOperator(localType, mkVar(0, mkMeta())),
			  mkPi(mkVar(1, mkMeta()),
			   mkOperator(Mutation.mutatorType, mkConst(Nil.nilType))))), 3) {
		
		@Override public String toString() { return "localWrite"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};	
}
