package org.managarm.aurora.io;

import java.io.IOException;
import java.io.PushbackReader;
import java.math.BigInteger;

import static org.managarm.aurora.lang.AuTerm.mkMetaExt;
import static org.managarm.aurora.lang.AuTerm.mkVarExt;

import org.managarm.aurora.lang.AuTerm;

public class BinaryReader {
	public static AuTerm read(PushbackReader input) throws IOException {
		int tag = input.read();
		if(tag == -1)
			throw new RuntimeException("Unexpected end-of-stream");
		
		AuTerm annotation = null;
		if(tag == '@') {
			annotation = read(input);
			tag = input.read();
			if(tag == -1)
				throw new RuntimeException("Unexpected end-of-stream");	
		}
		
		if(tag == 'M') {
			return mkMetaExt(annotation);
		}else if(tag == 'V') {
			int depth = readInt(input).intValue();
			AuTerm type = read(input);
			return mkVarExt(annotation, depth, type);
		}else throw new RuntimeException("Unexpected tag: " + (char)tag);
	}
	
	private static BigInteger readInt(PushbackReader input) throws IOException {
		StringBuilder str = new StringBuilder();
		
		int c = input.read();
		if(!(c >= '0' && c <= '9'))
			throw new RuntimeException("Illegal character " + c + " in number");
		
		while(c >= '0' && c <= '9') {
			str.append(c);
			c = input.read();
		}
		input.unread(c);
		
		return new BigInteger(str.toString());
	}
}
