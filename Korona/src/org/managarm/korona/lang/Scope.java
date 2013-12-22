package org.managarm.korona.lang;

import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.util.NamedTerm;

public class Scope {
	public static class BindScope extends Scope {
		private String p_name;
		private AuTerm p_type;
		private NamedTerm.Name p_descriptor;
		
		public BindScope(String name, AuTerm type,
				NamedTerm.Name descriptor) {
			p_name = name;
			p_type = type;
			p_descriptor = descriptor;
		}
		public String getName() {
			return p_name;
		}
		public AuTerm getType() {
			return p_type;
		}
		public NamedTerm.Name getDescriptor() {
			return p_descriptor;
		}
	}
	
	public static class LetScope extends Scope {
		private String p_name;
		private AuTerm p_defn;
		
		public LetScope(String name, AuTerm defn) {
			p_name = name;
			p_defn = defn;
		}
		public String getName() {
			return p_name;
		}
		public AuTerm getDefn() {
			return p_defn;
		}
	}
}
