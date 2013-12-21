package org.managarm.util.peg;

public abstract class PegError {
	private static String indent(String in, int spaces) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			builder.append(c);
			if(c == '\n')
				for(int j = 0; j < spaces; j++)
					builder.append(' ');
		}
		
		return builder.toString();
	}
		
	public static class EofError extends PegError {
		public EofError(int source_ref) {
			super(source_ref);
		}

		@Override public String format(PegParser p) {
			return "Unexpected end-of-file"; 
		}
	}
	public static class ExpectError extends PegError {
		private String p_expected;
		
		public ExpectError(int source_ref, String expected) {
			super(source_ref);
			p_expected = expected;
		}
		
		@Override public String format(PegParser p) {
			return p.derefInfo(this.getSourceRef())
					+ " Expected " + p_expected;
		}
	}
	
	public static class TrivialError extends PegError {
		private PegError p_chain;
		
		public TrivialError(PegError chain) {
			super(chain.getSourceRef());
			p_chain = chain;
		}

		@Override public String format(PegParser p) {
			return p_chain.format(p);
		} 
	}
	
	public static class ChoiceError extends PegError {
		private PegItem.Choice p_choice;
		private PegError[] p_errors;
		
		public ChoiceError(int source_ref,
				PegItem.Choice choice, PegError[] errors) {
			super(source_ref);
			p_choice = choice;
			p_errors = errors;
		}
		
		@Override public String format(PegParser p) {
			int num = 0;
			for(int i = 0; i < p_choice.numItems(); i++) {
				if(p_errors[i] instanceof TrivialError)
					continue;
				num++;
			}
			if(num == 1) {
				for(int i = 0; i < p_choice.numItems(); i++) {
					if(p_errors[i] instanceof TrivialError)
						continue;
					return p_errors[i].format(p);
				}
			}
			
			StringBuilder builder = new StringBuilder();
			builder.append(p.derefInfo(this.getSourceRef()));
			builder.append(" Expected either ");
			for(int i = 0; i < p_choice.numItems(); i++) {
				if(i > 0)
					if(i == p_choice.numItems() - 1) {
						builder.append(" or ");
					}else{
						builder.append(", ");
					}
				builder.append(p_choice.getItem(i));
			}
			
			for(int i = 0; i < p_choice.numItems(); i++) {
				if(p_errors[i] instanceof TrivialError)
					continue;
				
				builder.append('\n');
				builder.append("    ");
				builder.append(p_choice.getItem(i));
				builder.append(" failed: ");
				builder.append(indent(p_errors[i].format(p), 8));
			}
			return builder.toString();
		}
	}
	
	public static class TrivialChoiceError extends PegError {
		private PegItem.TrivialChoice p_choice;
		
		public TrivialChoiceError(int source_ref,
				PegItem.TrivialChoice choice) {
			super(source_ref);
			p_choice = choice;
		}
		
		@Override public String format(PegParser p) {
			StringBuilder builder = new StringBuilder();
			builder.append(p.derefInfo(this.getSourceRef()));
			builder.append(" Expected either ");
			for(int i = 0; i < p_choice.numItems(); i++) {
				if(i > 0)
					if(i == p_choice.numItems() - 1) {
						builder.append(" or ");
					}else{
						builder.append(", ");
					}
				builder.append(p_choice.getItem(i));
			}
			return builder.toString();
		}
	}
	
	private int p_sourceRef;
	
	public PegError(int source_ref) {
		p_sourceRef = source_ref;
	}
	public int getSourceRef() {
		return p_sourceRef;
	}
	
	public abstract String format(PegParser p);
}
