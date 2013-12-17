package org.managarm.korona.lang;

import org.managarm.aurora.lang.AuTerm;

public class Scope {
	public static class BindScope extends Scope {
		private String p_name;
		private KorUid p_uid;
		private AuTerm p_type;
		
		public BindScope(String name, KorUid uid, AuTerm type) {
			p_name = name;
			p_uid = uid;
			p_type = type;
		}
		public String getName() {
			return p_name;
		}
		public KorUid getUid() {
			return p_uid;
		}
		public AuTerm getType() {
			return p_type;
		}
	}
}
