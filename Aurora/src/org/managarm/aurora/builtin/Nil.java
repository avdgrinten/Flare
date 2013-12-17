package org.managarm.aurora.builtin;

import org.managarm.aurora.lang.AuConstant;

import static org.managarm.aurora.lang.AuTerm.mkMeta;
import static org.managarm.aurora.lang.AuTerm.mkConst;

public class Nil {
	public static AuConstant.Descriptor nilType = new AuConstant.Descriptor(mkMeta()) {
		@Override public String toString() { return "Nil"; }
	};
	public static AuConstant.Descriptor nilValue = new AuConstant.Descriptor(mkConst(nilType)) {
		@Override public String toString() { return "nil"; }
	};
}
