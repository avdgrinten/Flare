package org.managarm.util.peg;

import java.util.ArrayDeque;
import java.util.Deque;

public class PegParser {
	public static <T> PegItem transform(final PegItem item,
			final PegTransform<T> transform) {
		return new PegItem() {
			@Override public Object parse(PegParser p) {
				Object in = p.parse(item);
				if(in instanceof PegError)
					return in;
				@SuppressWarnings("unchecked")
				Object res = transform.transform((T)in);
				return res;
			}
		};
	}
	
	public static final PegTransform<Object[]> forgetLeft = new PegTransform<Object[]>() {
		@Override public Object transform(Object[] in) {
			return in[1];
		}
	};
	public static final PegTransform<Object[]> forgetRight = new PegTransform<Object[]>() {
		@Override public Object transform(Object[] in) {
			return in[0];
		}
	};
	
	private class State {
		int position;
	}
	
	private String input;
	private State current = new State();
	private Deque<State> stack = new ArrayDeque<State>();
	
	public PegParser(String input) {
		this.input = input;
	}
	
	public void save() {
		State state = new State();
		state.position = current.position;
		stack.push(state);
	}
	public void restore() {
		State state = stack.pop();
		current.position = state.position;
	}
	public void forget() {
		stack.pop();
	}
	
	public char read() {
		return input.charAt(current.position);
	}
	public boolean eof() {
		return current.position >= input.length();
	}
	public void consume() {
		current.position++;
	}
	public int curSourceRef() {
		return current.position;
	}
	
	public Object parse(PegItem item) {
		return item.parse(this);
	}
	
	public int derefLine(int source_ref) {
		int line = 1;
		for(int i = 0; i < source_ref; i++) {
			char c = input.charAt(i);
			if(c == '\n')
				line++;
		}
		return line;
	}
	
	public String derefInfo(int source_ref) {
		int line = derefLine(source_ref);
		return "[Line " + line + "]";
	}
}
