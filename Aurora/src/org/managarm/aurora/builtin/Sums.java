package org.managarm.aurora.builtin;

import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkVar;

import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class Sums {
	public static AuOperator.GroundDescriptor sumType = new AuOperator.GroundDescriptor(
			mkPi(mkMeta(),
			 mkPi(mkMeta(),
			  mkMeta())), 2) {
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	
	public static AuOperator.Descriptor sumL = new AuOperator.GroundDescriptor(
			mkPi(mkMeta(),
			 mkPi(mkMeta(),
			  mkPi(mkVar(1, mkMeta()),
			   mkOperator(sumType, mkVar(2, mkMeta()), mkVar(1, mkMeta()))))), 3) {
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
	
	public static AuOperator.Descriptor sumR = new AuOperator.GroundDescriptor(
			mkPi(mkMeta(),
			 mkPi(mkMeta(),
			  mkPi(mkVar(0, mkMeta()),
			   mkOperator(sumType, mkVar(2, mkMeta()), mkVar(1, mkMeta()))))), 3) {
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
}
