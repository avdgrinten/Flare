package org.managarm.korona;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.managarm.aurora.builtin.Anys;
import org.managarm.aurora.builtin.Bool;
import org.managarm.aurora.builtin.IntArithmetic;
import org.managarm.aurora.builtin.Io;
import org.managarm.aurora.builtin.Lists;
import org.managarm.aurora.builtin.Locals;
import org.managarm.aurora.builtin.Mutation;
import org.managarm.aurora.builtin.Nil;
import org.managarm.aurora.builtin.Products;
import org.managarm.aurora.builtin.Proof;
import org.managarm.aurora.builtin.Strings;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.korona.lang.ResolveMsg;
import org.managarm.korona.lang.Resolver;
import org.managarm.korona.syntax.Parser;
import org.managarm.korona.syntax.StFile;

public class CompilerHelper {
	private List<AuTerm> p_symbols = new ArrayList<AuTerm>();
	
	public boolean compile(File file) throws IOException {
		return compile(new FileReader(file));
	}
	public boolean compile(InputStream stream) throws IOException {
		return compile(new InputStreamReader(stream));
	}
	public boolean compile(Reader reader) throws IOException {
		StringBuilder source = new StringBuilder();
		int c;
		while((c = reader.read()) != -1)
			source.append((char)c);
		reader.close();
		return compile(source.toString());
	}
	public boolean compile(String source) {
		Parser parser = new Parser();
		long parse_start = System.currentTimeMillis();
		parser.parse(source);
		long parse_time = System.currentTimeMillis() - parse_start;
		System.out.println("Parse time: " + parse_time + "ms");
		
		if(!parser.okay()) {
			onParseError(parser.getError());
			return false;
		}
		StFile syntax = parser.getResult();
		
		Resolver resolver = new Resolver();
		resolver.addBuiltin(Nil.nilType);
		resolver.addBuiltin(Nil.nilValue);
		resolver.addBuiltin(Bool.boolType);
		resolver.addBuiltin(Bool.boolOr);
		resolver.addBuiltin(Bool.boolAnd);
		resolver.addBuiltin(Bool.ite);
		resolver.addBuiltin(IntArithmetic.intType);
		resolver.addBuiltin(IntArithmetic.intAdd);
		resolver.addBuiltin(IntArithmetic.intSub);
		resolver.addBuiltin(IntArithmetic.intMul);
		resolver.addBuiltin(IntArithmetic.intDiv);
		resolver.addBuiltin(IntArithmetic.intMod);
		resolver.addBuiltin(IntArithmetic.intEq);
		resolver.addBuiltin(IntArithmetic.intInEq);
		resolver.addBuiltin(IntArithmetic.intFold);
		resolver.addBuiltin(IntArithmetic.intLt);
		resolver.addBuiltin(IntArithmetic.intGt);
		resolver.addBuiltin(IntArithmetic.intLe);
		resolver.addBuiltin(IntArithmetic.intGe);
		resolver.addBuiltin(Strings.stringType);
		resolver.addBuiltin(Products.productType);
		resolver.addBuiltin(Products.product);
		resolver.addBuiltin(Products.projectL);
		resolver.addBuiltin(Products.projectR);
		resolver.addBuiltin(Lists.listType);
		resolver.addBuiltin(Lists.emptyList);
		resolver.addBuiltin(Lists.singletonList);
		resolver.addBuiltin(Lists.listLen);
		resolver.addBuiltin(Lists.listAppend);
		resolver.addBuiltin(Lists.listElem);
		resolver.addBuiltin(Anys.anyType);
		resolver.addBuiltin(Anys.any);
		resolver.addBuiltin(Anys.anyMeta);
		resolver.addBuiltin(Anys.anyExtract);
		resolver.addBuiltin(Mutation.mutatorType);
		resolver.addBuiltin(Mutation.embed);
		resolver.addBuiltin(Mutation.seq);
		resolver.addBuiltin(Proof.proofType);
		resolver.addBuiltin(Proof.tautology);
		resolver.addBuiltin(Proof.assume);
		resolver.addBuiltin(Locals.localType);
		resolver.addBuiltin(Locals.localAlloc);
		resolver.addBuiltin(Locals.localRead);
		resolver.addBuiltin(Locals.localWrite);
		resolver.addBuiltin(Io.print);
		
		for(AuTerm symbol : p_symbols)
			resolver.addSymbol(symbol);
		
		long resolve_start = System.currentTimeMillis();
		resolver.buildFile(syntax);
		long resolve_time = System.currentTimeMillis() - resolve_start;
		System.out.println("Resolve time: " + resolve_time + "ms");
		
		if(!resolver.okay()) {
		onResolveFailure(resolver.numMessages());
			for(int i = 0; i < resolver.numMessages(); i++)
				onResolveMessage(resolver.getMessage(i));
			return false;
		}
		
		onResolveSuccess(resolver.numMessages());
		for(int i = 0; i < resolver.numMessages(); i++)
			onResolveMessage(resolver.getMessage(i));
		
		Iterator<AuTerm> it = resolver.symbolIterator();
		while(it.hasNext())
			p_symbols.add(it.next());
		return true;
	}
	
	protected void onParseError(String message) { }
	protected void onResolveSuccess(int num_messages) { }
	protected void onResolveFailure(int num_messages) { }
	protected void onResolveMessage(ResolveMsg msg) { }
	
	public Iterator<AuTerm> symbolIterator() {
		return p_symbols.iterator();
	}
}
