package org.managarm.aurora.builtin;

import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkVar;
import static org.managarm.aurora.lang.AuTerm.mkOperator;

import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class Symbols {
	public static AuOperator.Descriptor symbolInline = new AuOperator.Descriptor(
			mkPi(mkConst(Strings.stringType),
			 mkPi(mkConst(Strings.stringType),
			  mkPi(mkMeta(),
			   mkVar(0, mkMeta())))), 3) {
		@Override public String toString() {
			return "symbolInline";
		}
		
		@Override protected boolean reductive(AuTerm[] args) {
			return false;
		}
		@Override protected boolean primitive(AuTerm[] args) {
			return false;
		}
		@Override protected AuTerm reduce(AuTerm[] args) {
			return mkOperator(this, args);
		}
	};
}
