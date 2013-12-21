package org.managarm.util.peg;

import java.util.ArrayList;
import java.util.List;

public interface PegItem {
	public static class Eof implements PegItem {
		@Override public Object parse(PegParser p) {
			if(!p.eof())
				return new PegError.EofError(p.curSourceRef());
			return null;
		}
	}
	
	public static class Any implements PegItem {
		private int p_length;
		
		public Any(int length) {
			p_length = length;
		}
		
		@Override public Object parse(PegParser p) {
			StringBuilder res = new StringBuilder();
			for(int i = 0; i < p_length; i++) {
				if(p.eof())
					return new PegError.EofError(p.curSourceRef());
				char c = p.read();
				res.append(c);
				p.consume();
			}
			return res.toString();
		}
	}
	
	public static class SingleChar implements PegItem {
		private char p_char;
		
		public SingleChar(char c) {
			p_char = c;
		}
		
		@Override public Object parse(PegParser p) {
			if(p.eof())
				return new PegError.EofError(p.curSourceRef());
			char c = p.read();
			if(c != p_char)
				return new PegError.ExpectError(p.curSourceRef(), "'" + p_char + "'");
			p.consume();
			return Character.toString(c);
		}
	}
	
	public static class CharString implements PegItem {
		private String p_string;
		
		public CharString(String string) {
			p_string = string;
		}
		
		public Object parse(PegParser p) {
			for(int i = 0; i < p_string.length(); i++) {
				if(p.eof())
					return new PegError.EofError(p.curSourceRef());
				char c = p.read();
				if(c != p_string.charAt(i))
					return new PegError.ExpectError(p.curSourceRef(), "\"" + p_string + "\"");
				p.consume();
			}
			return p_string;
		}
	}
	
	public static class Sequence implements PegItem {
		private PegItem[] p_items;
		
		public Sequence(PegItem... items) {
			p_items = items;
		}
	
		@Override public Object parse(PegParser p) {
			Object[] array = new Object[p_items.length];
			for(int i = 0; i < p_items.length; i++) {
				Object res = p.parse(p_items[i]);
				if(res instanceof PegError)
					return res;
				array[i] = res;
			}
			return array;
		}
	}
	
	public static class Optional implements PegItem {
		private PegItem p_item;
		
		public Optional(PegItem item) {
			p_item = item;
		}
		
		@Override public Object parse(PegParser p) {
			Object res = p.parse(p_item);
			if(!(res instanceof PegError))
				return res;
			return null;
		}
	}
	
	public static class Trivial implements PegItem {
		private PegItem p_item;
		
		public Trivial(PegItem item) {
			p_item = item;
		}
		
		@Override public Object parse(PegParser p) {
			Object res = p.parse(p_item);
			if(res instanceof PegError)
				return new PegError.TrivialError((PegError)res);
			return res;
		}
	}
	
	public static class Repeat implements PegItem {
		private PegItem p_item;
		
		public Repeat(PegItem item) {
			p_item = item;
		}
		
		@Override public Object parse(PegParser p) {
			List<Object> array = new ArrayList<Object>();
			while(true) {
				p.save();
				Object res = p.parse(p_item);
				if(!(res instanceof PegError)) {
					array.add(res);
					p.forget();
				}else{
					p.restore();
					break;
				}
			}
			return array.toArray();
		}
	}
	
	public static class Choice implements PegItem {
		private PegItem[] p_items;
		
		public Choice(PegItem... items) {
			p_items = items;
		}
		public int numItems() {
			return p_items.length;
		}
		public PegItem getItem(int i) {
			return p_items[i];
		}
		
		@Override public Object parse(PegParser p) {
			if(p_items.length == 1)
				return p.parse(p_items[0]); 
				
			PegError[] errors = new PegError[p_items.length];
			for(int i = 0; i < p_items.length; i++) {
				p.save();
				Object res = p.parse(p_items[i]);
				if(!(res instanceof PegError)) {
					p.forget();
					return res;
				}
				p.restore();
				errors[i] = (PegError)res;
				
			}
			return new PegError.ChoiceError(p.curSourceRef(), this, errors);
		}
	}
	
	public static class TrivialChoice implements PegItem {
		private PegItem[] p_items;
		
		public TrivialChoice(PegItem... items) {
			p_items = items;
		}
		public int numItems() {
			return p_items.length;
		}
		public PegItem getItem(int i) {
			return p_items[i];
		}

		@Override public Object parse(PegParser p) {
			for(int i = 0; i < p_items.length; i++) {
				p.save();
				Object res = p.parse(p_items[i]);
				if(!(res instanceof PegError)) {
					p.forget();
					return res;
				}
				p.restore();
			}
			return new PegError.TrivialChoiceError(p.curSourceRef(),  this);
		}
	}
	
	public static class Until implements PegItem {
		private PegItem p_repeat;
		private PegItem p_until;
		
		public Until(PegItem repeat, PegItem until) {
			p_repeat = repeat;
			p_until = until;
		}
		
		@Override public Object parse(PegParser p) {
			List<Object> array = new ArrayList<Object>();
			while(true) {
				p.save();
				Object until_res = p.parse(p_until);
				if(!(until_res instanceof PegError)) {
					p.forget();
					break;
				}
				p.restore();
				
				Object repeat_res = p.parse(p_repeat);
				if(repeat_res instanceof PegError)
					return repeat_res;
				array.add(repeat_res);
			}
			return array.toArray();
		}
	}
	
	public static class Not implements PegItem {
		private PegItem p_notThis;
		private PegItem p_butThis;
		
		public Not(PegItem not_this, PegItem but_this) {
			p_notThis = not_this;
			p_butThis = but_this;
		}
		
		
		@Override public Object parse(PegParser p) {
			Object res = p.parse(p_notThis);
			if(!(res instanceof PegError))
				return new PegError.ExpectError(p.curSourceRef(),
						p_butThis.toString());
			return p.parse(p_butThis);
		}
	}
	
	public Object parse(PegParser p);
}
