package org.managarm.jet.compiler;

import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkOperator;
import static org.managarm.aurora.lang.AuTerm.mkPi;
import static org.managarm.aurora.lang.AuTerm.mkVar;

import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuTerm;

public class InternalRef extends AuOperator.Descriptor {
	public InternalRef() {
			super(mkPi(mkMeta(),
			mkVar(0, mkMeta())), 1);
	}
		
	@Override protected boolean primitive(AuTerm[] args) {
		return false;
	}
	@Override protected AuTerm reduce(AuTerm[] args) {
		return mkOperator(this, args);
	}
}
