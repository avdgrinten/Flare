package org.managarm.korona.syntax;

public class StRoot extends StNode {
	public static class Import extends StRoot {
		private String p_module;
		
		public Import(String module) {
			p_module = module;
		}
		public String getModule() {
			return p_module;
		}
	}
	
	public static class Module extends StRoot {
		private String p_module;
		private StNode[] p_members;
		
		public Module(String module, StNode[] members) {
			p_module = module;
			p_members = members;
		}
		public String getModule() {
			return p_module;
		}
		public int numMembers() {
			return p_members.length;
		}
		public StNode getMember(int i) {
			return p_members[i];
		}
	}
	
	public static class Symbol extends StRoot {
		public static final int kFlagExport = 1;
		public static final int kFlagExtern = 2;
		public static final int kFlagEmbed = 32;
		
		private String p_name;
		private StNode p_type;
		private StNode p_defn;
		private int p_flags;
		
		public Symbol(String name, StNode type, StNode defn, int flags) {
			p_name = name;
			p_type = type;
			p_defn = defn;
			p_flags = flags;
		}
		public String getName() {
			return p_name;
		}
		public StNode getType() {
			return p_type;
		}
		public StNode getDefn() {
			return p_defn;
		}
		public int getFlags() {
			return p_flags;
		}
	}
}
