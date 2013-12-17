package org.managarm.util.peg;

import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;

public class PegParser {
	public static PegItem any(final int length) {
		return new PegItem() {
			public PegResult parse(PegParser p) {
				StringBuilder res = new StringBuilder();
				for(int i = 0; i < length; i++) {
					if(p.eof())
						return PegResult.failure("Unexpected end of file");
					char c = p.read();
					res.append(c);
					p.consume();
				}
				return PegResult.success(res.toString());
			}
		};
	}
	public static PegItem singleChar(final char single_char) {
		return new PegItem() {
			public PegResult parse(PegParser p) {
				if(p.eof())
					return PegResult.failure("Unexpected end of file");
				char c = p.read();
				if(c != single_char)
					return PegResult.failure("Unexpected character '" + c
							+ "', expected " + single_char);
				p.consume();
				return PegResult.success(Character.toString(c));
			}
		};
	}
	public static PegItem string(final String str) {
		return new PegItem() {
			public PegResult parse(PegParser p) {
				for(int i = 0; i < str.length(); i++) {
					if(p.eof())
						return PegResult.failure("Unexpected end of file");
					char c = p.read();
					if(c != str.charAt(i))
						return PegResult.failure("Expected " + str);
					p.consume();
				}
				return PegResult.success(str);
			}
		};
	}
	public static PegItem sequence(final PegItem... items) {
		return new PegItem() {
			@Override public PegResult parse(PegParser p) {
				Object[] result = new Object[items.length];
				for(int i = 0; i < items.length; i++) {
					PegResult r = p.parse(items[i]);
					if(!r.okay())
						return PegResult.failure();
					result[i] = r.object();
				}
				return PegResult.success(result);
			}
		};
	}
	public static PegItem optional(final PegItem item) {
		return new PegItem() {
			@Override public PegResult parse(PegParser p) {
				PegResult res = p.parse(item);
				if(res.okay())
					return res;
				return PegResult.success();
			}
		};
	}
	public static PegItem choice(final PegItem... items) {
		return new PegItem() {
			@Override public PegResult parse(PegParser p) {
				for(PegItem item : items) {
					p.save();
					PegResult res = p.parse(item);
					if(res.okay()) {
						p.forget();
						return res;
					}else{
						p.restore();
					}
				}
				return PegResult.failure();
			}
		};
	}
	public static PegItem repeat(final PegItem item) {
		return new PegItem() {
			@Override public PegResult parse(PegParser p) {
				List<Object> res_list = new ArrayList<Object>();
			
				while(true) {
					p.save();
					PegResult res = p.parse(item);
					if(res.okay()) {
						res_list.add(res.object());
						p.forget();
					}else{
						p.restore();
						break;
					}
				}
				return PegResult.success(res_list.toArray());
			}
		};
	}
	public static PegItem not(final PegItem not_this,
			final PegItem but_this) {
		return new PegItem() {
			@Override public PegResult parse(PegParser p) {
				PegResult res = p.parse(not_this);
				if(res.okay())
					return PegResult.failure();
				return p.parse(but_this);
			}
		};
	}
	public static <T> PegItem transform(final PegItem item,
			final PegTransform<T> transform) {
		return new PegItem() {
			@Override public PegResult parse(PegParser p) {
				PegResult in = p.parse(item);
				if(!in.okay())
					return in;
				Object res = transform.transform(in.<T>object());
				return PegResult.success(res);
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
	
	public PegResult parse(PegItem item) {
		return item.parse(this);
	}
	
	public PegResult[] oldRepeat(PegItem item) {
		List<PegResult> res_list = new ArrayList<PegResult>();
		
		while(true) {
			save();
			PegResult res = item.parse(this);
			if(res.okay()) {
				res_list.add(res);
				forget();
			}else{
				restore();
				break;
			}
		}
		return res_list.toArray(new PegResult[res_list.size()]);
	}
	public PegResult[] list(PegItem item, PegItem delimiter) {
		List<PegResult> res_list = new ArrayList<PegResult>();
		
		boolean first = true;
		while(true) {
			save();
			if(!first) {
				PegResult delim_res = delimiter.parse(this);
				if(!delim_res.okay()) {
					restore();
					break;
				}
			}
			PegResult res = item.parse(this);
			if(res.okay()) {
				res_list.add(res);
				forget();
			}else{
				restore();
				break;
			}
			first = false;
		}
		return res_list.toArray(new PegResult[res_list.size()]);
	}
}
