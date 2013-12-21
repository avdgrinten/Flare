package org.managarm.korona.lang;

import org.managarm.aurora.lang.AuTerm;
import org.managarm.korona.syntax.StIdent;

// this class describes either a warning or an error that occurred during
// the resolve process.
public abstract class ResolveMsg {
	public enum Level {
		kWarning {
			@Override public String toString() {
				return "Warning";
			}
		},
		kError {
			@Override public String toString() {
				return "Error";
			}
		}
	}
	
	public static class NoSuchImport extends ResolveMsg {
		private String p_module;
		
		public NoSuchImport(String module) {
			super(Level.kWarning);
			p_module = module;
		}
		
		@Override public String toString() {
			return "Imported module " + p_module + " does not exist"; 
		}
	}
	
	public static class IllegalAccess extends ResolveMsg {
		private AuTerm p_lhs;
		private String p_identifier;
		
		public IllegalAccess(AuTerm lhs, String identifier) {
			super(Level.kError);
			p_lhs = lhs;
			p_identifier = identifier;
		}
		
		@Override public String toString() {
			return "Could not access member " + p_identifier + " of " + p_lhs;
		}
	}
	
	public static class DuplicateSymbol extends ResolveMsg {
		private String p_module;
		private String p_name;
		private AuTerm p_type;
		
		public DuplicateSymbol(String module, String name, AuTerm type) {
			super(Level.kError);
			p_module = module;
			p_name = name;
			p_type = type;
		}
		
		@Override public String toString() {
			return "Duplicate symbol " + p_module + "." + p_name
					+ " of type " + p_type;
		}
	}
	
	public static class UnresolvedIdentifier extends ResolveMsg {
		private StIdent p_ident;
		
		public UnresolvedIdentifier(StIdent ident) {
			super(Level.kError);
			p_ident = ident;
		}
		
		@Override public String toString() {
			return "Could not resolve identifier " + p_ident.string();
		}
	}
	
	public static class NoOverload extends ResolveMsg {
		private StIdent p_ident;
		
		public NoOverload(StIdent ident) {
			super(Level.kError);
			p_ident = ident;
		}
		
		@Override public String toString() {
			return "No valid overload for " + p_ident.string();
		}
	}

	
	public static class AmbiguousOverload extends ResolveMsg {
		private StIdent p_ident;
		private AuTerm p_chosen;
		
		public AmbiguousOverload(StIdent ident, AuTerm chosen) {
			super(Level.kWarning);
			p_ident = ident;
			p_chosen = chosen;
		}
		
		@Override public String toString() {
			return "Overload for " + p_ident.string() + " is ambiguous\n"
					+ "    Chosen candidate: " + p_chosen;
		}
	}
	public static class AmbiguousIdentifier extends ResolveMsg {
		private StIdent p_ident;
		private AuTerm p_chosen;
		
		public AmbiguousIdentifier(StIdent ident, AuTerm chosen) {
			super(Level.kWarning);
			p_ident = ident;
			p_chosen = chosen;
		}
		
		@Override public String toString() {
			return "Identifier " + p_ident.string() + " is ambiguous\n"
					+ "    Chosen candidate: " + p_chosen;
		}
	}
	
	private Level p_level; 
	
	public ResolveMsg(Level level) {
		p_level = level;
	}
	public Level getLevel() {
		return p_level;
	}
}
