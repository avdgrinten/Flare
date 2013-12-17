package org.managarm.korona.syntax;

public class StFile {
	private StNode[] p_elements;
	
	public StFile(StNode[] elements) {
		p_elements = elements;
	}
	public int numElements() {
		return p_elements.length;
	}
	public StNode getElement(int i) {
		return p_elements[i];
	}
	
	@Override public String toString() {
		String string = "file{";
		for(StNode element : p_elements)
			string += element;
		string += "}";
		return string;
	}
}
