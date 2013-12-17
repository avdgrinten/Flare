package org.managarm.aurora.io;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.managarm.aurora.builtin.Anys;
import org.managarm.aurora.builtin.IntArithmetic;
import org.managarm.aurora.builtin.Io;
import org.managarm.aurora.builtin.Lists;
import org.managarm.aurora.builtin.Mutation;
import org.managarm.aurora.builtin.Nil;
import org.managarm.aurora.builtin.Products;
import org.managarm.aurora.builtin.Strings;
import org.managarm.aurora.lang.AuConstant;
import org.managarm.aurora.lang.AuMeta;
import org.managarm.aurora.lang.AuOperator;
import org.managarm.aurora.lang.AuPi;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.lang.AuVar;

public class BinaryWriter {
	public static String toString(AuTerm term) {
		StringWriter writer = new StringWriter();
		try {
			write(writer, term);
		}catch(IOException e) {
			throw new RuntimeException("Unexpected IO exception", e);
		}
		return writer.toString();
	}
	
	public static void write(Writer output, AuTerm term) throws IOException {
		if(term.getAnnotation() != null) {
			output.write('@');
			write(output, term.getAnnotation());
		}
		
		if(term instanceof AuMeta) {
			output.write('M');
		}else if(term instanceof AuVar) {
			AuVar var = (AuVar)term;
			output.write('V');
			output.write(Integer.toString(var.getDepth()));
			write(output, var.getType());
		}else if(term instanceof AuPi) {
			AuPi pi = (AuPi)term;
			output.write('P');
			write(output, pi.getBound());
			write(output, pi.getCodomain());
		}else if(term instanceof AuConstant) {
			AuConstant constant = (AuConstant)term;
			output.write('C');
			writeConstant(output, constant.getDescriptor());
		}else if(term instanceof AuOperator) {
			AuOperator operator = (AuOperator)term;
			output.write('O');
			writeOperator(output, operator.getDescriptor());
			for(int i = 0; i < operator.numArguments(); i++)
				write(output, operator.getArgument(i));
		}else throw new RuntimeException("Illegal term " + term);
	}
	
	private static void writeConstant(Writer output,
			AuConstant.Descriptor desc) throws IOException {
		if(desc == Nil.nilType) {
			output.write("nit");
		}else if(desc == Nil.nilValue) {
			output.write("nil");
		}else if(desc == IntArithmetic.intType) {
			output.write("it");
		}else if(desc == Strings.stringType) {
			output.write("st");
		}else if(desc instanceof Strings.StringLit) {
			Strings.StringLit lit = (Strings.StringLit)desc;
			output.write("sl");
			writeString(output, lit.getValue());
		}else if(desc == Anys.anyType) {
			output.write("ayt");
		}else throw new RuntimeException("Unsupported constant " + desc);
	}
	
	private static void writeOperator(Writer output,
			AuOperator.Descriptor desc) throws IOException {
		if(desc == IntArithmetic.intAdd) {
			output.write("ia");
		}else if(desc == Products.productType) {
			output.write("prt");
		}else if(desc == Products.product) {
			output.write("prv");
		}else if(desc instanceof Lists.List) {
			Lists.List list = (Lists.List)desc;
			output.write("l");
			output.write(Integer.toString(list.getLength()));
		}else if(desc == Anys.any) {
			output.write("ayv");
		}else if(desc == Mutation.mutatorType) {
			output.write("mut");
		}else if(desc == Io.print) {
			output.write("iop");
		}else throw new RuntimeException("Unsupported operator " + desc);
	}
	
	private static void writeString(Writer output,
			String string) throws IOException {
		for(int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			
			if((c >= 'a' && c <= 'z')
					|| (c >= 'A' && c <= 'Z')
					|| (c >= '0' && c <= '9')) {
				output.write(c);
			}else if(c == ' ') {
				output.write("_s");
			}else if(c == '\t') {
				output.write("_t");
			}else if(c == ',') {
				output.write("_c");
			}else if(c == '.') {
				output.write("_d");
			}else if(c == '!') {
				output.write("_e");
			}else if(c == '?') {
				output.write("_q");
			}else{
				output.write(String.format("_%04X", (int)c));
			}
		}
		output.write("_f");
	}
}
