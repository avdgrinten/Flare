package org.managarm.korona.syntax;

import java.math.BigInteger;

public class StLiteral {
	public static class LitInt extends StNode {
		private BigInteger p_value;
		
		public LitInt(BigInteger value) {
			p_value = value;
		}
		public BigInteger value() {
			return p_value;
		}
		@Override public String toString() {
			return p_value.toString();
		}
	}
	public static class LitString extends StNode {
		private String p_value;
		
		public LitString(String value) {
			p_value = value;
		}
		public String value() {
			return p_value;
		}
		@Override public String toString() {
			return '"' + p_value + "'";
		}
	}
}
