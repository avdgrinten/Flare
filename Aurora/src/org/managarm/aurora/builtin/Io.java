package org.managarm.aurora.builtin;

import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkVar;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkConst;

public class Io {
	public static class Print extends AuOperator.GroundDescriptor {
		public Print() {
			super(mkPi(mkMeta(),
					 mkPi(mkVar(0, mkMeta()),
					  mkOperator(Mutation.mutatorType, mkConst(Nil.nilType)))), 2);
		}
		@Override public String toString() { return "print"; }
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	}
	public static AuOperator.Descriptor print = new Print();
}
