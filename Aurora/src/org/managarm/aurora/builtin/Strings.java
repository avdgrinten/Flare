package org.managarm.aurora.builtin;

import static org.managarm.aurora.lang.AuTerm.mkConst;

import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuTerm;

public class Strings {
	public static AuConstant.Descriptor stringType = new AuConstant.Descriptor(AuTerm.mkMeta()) {
		@Override public String toString() { return "String"; }
	};
	
	public static class StringLit extends AuConstant.Descriptor {
		public static StringLit extract(AuTerm term) {
			return (StringLit)((AuConstant)term).getDescriptor();
		}
		
		private String value;
		
		public StringLit(String value) {
			super(mkConst(stringType));
			this.value = value;
		}
		public String getValue() {
			return value;
		}
		
		@Override public String toString() {
			return '"' + value + '"';
		}
		@Override public boolean equals(Object object) {
			if(!(object instanceof StringLit))
				return false;
			StringLit other = (StringLit)object;
			return value.equals(other.value);
		}
	};
}
