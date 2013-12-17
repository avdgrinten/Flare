package org.managarm.util.peg;

public class PegResult {
	public static PegResult success() {
		return new PegResult(true, null);
	}
	public static PegResult success(Object object) {
		return new PegResult(true, object);
	}
	public static PegResult failure() {
		return new PegResult(false, null);
	}
	public static PegResult failure(String text) {
		return new PegResult(false, text);
	}
	
	private boolean okay;
	private Object object;
	
	private PegResult(boolean okay, Object object) {
		this.okay = okay;
		this.object = object;
	}
	
	public boolean okay() {
		return this.okay;
	}
	@SuppressWarnings("unchecked")
	public <T> T object() {
		return (T)this.object;
	}
}
