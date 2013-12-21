
import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkOperator;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.managarm.aurora.builtin.Anys;
import org.managarm.aurora.builtin.Bool;
import org.managarm.aurora.builtin.IntArithmetic;
import org.managarm.aurora.builtin.Io;
import org.managarm.aurora.builtin.Lists;
import org.managarm.aurora.builtin.Locals;
import org.managarm.aurora.builtin.Mutation;
import org.managarm.aurora.builtin.Nil;
import org.managarm.aurora.builtin.Products;
import org.managarm.aurora.builtin.Strings;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.link.Interpreter;
import org.managarm.aurora.link.Linker;
import org.managarm.aurora.util.Descriptor;
import org.managarm.korona.lang.ResolveMsg;
import org.managarm.korona.lang.Resolver;
import org.managarm.korona.syntax.Parser;
import org.managarm.korona.syntax.StFile;

public class TestKorona {
	public static void main(String[] args) {
		StringBuilder source = new StringBuilder();
		try {
			Reader reader = new FileReader("examples/Test.kor");
			int c;
			while((c = reader.read()) != -1)
				source.append((char)c);
			reader.close();
		}catch(IOException e) {
			e.printStackTrace();
			return;
		}
		
		Parser parser = new Parser();
		long parse_start = System.currentTimeMillis();
		parser.parse(source.toString());
		long parse_time = System.currentTimeMillis() - parse_start;
		System.out.println("Parse time: " + parse_time + "ms");
		
		if(!parser.okay()) {
			System.err.println("Could not parse source");
			System.err.println(parser.getError());
			return;
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
		resolver.addBuiltin(Products.projectL);
		resolver.addBuiltin(Lists.listType);
		resolver.addBuiltin(Lists.emptyList);
		resolver.addBuiltin(Lists.singletonList);
		resolver.addBuiltin(Lists.listAppend);
		resolver.addBuiltin(Anys.anyType);
		resolver.addBuiltin(Anys.any);
		resolver.addBuiltin(Anys.anyMeta);
		resolver.addBuiltin(Anys.anyExtract);
		resolver.addBuiltin(Mutation.mutatorType);
		resolver.addBuiltin(Mutation.embed);
		resolver.addBuiltin(Mutation.seq);
		resolver.addBuiltin(Locals.localType);
		resolver.addBuiltin(Locals.localAlloc);
		resolver.addBuiltin(Locals.localRead);
		resolver.addBuiltin(Locals.localWrite);
		resolver.addBuiltin(Io.print);
		
		long resolve_start = System.currentTimeMillis();
		resolver.buildFile(syntax);
		long resolve_time = System.currentTimeMillis() - resolve_start;
		System.out.println("Resolve time: " + resolve_time + "ms");
		
		if(!resolver.okay()) {
			System.err.println("Resolve failure: "
					+ resolver.numMessages() + " errors/warnings!");
			for(int i = 0; i < resolver.numMessages(); i++) {
				ResolveMsg msg = resolver.getMessage(i);
				System.err.println(msg.getLevel() + ": " + msg);
			}
			return;
		}
		
		System.out.println("Resolve success. "
				+ resolver.numMessages() + " warnings!");
		for(int i = 0; i < resolver.numMessages(); i++) {
			ResolveMsg msg = resolver.getMessage(i);
			System.err.println(msg.getLevel() + ": " + msg);
		}
		
		Linker linker = new Linker();
		Iterator<AuTerm> it = resolver.symbolIterator();
		while(it.hasNext())
			linker.linkSymbol(it.next());
		
		AuTerm main = linker.getSymbol("Test", "main",
				mkOperator(Mutation.mutatorType, mkConst(Nil.nilType)));
		if(main == null)
			return;
		Descriptor desc = new Descriptor(main);
		AuTerm defn = desc.get(new Descriptor.RecordPath("defn"));
		System.out.println(defn);
		
		Interpreter interpreter = new Interpreter();
		interpreter.execute(defn);
		
		/*Namespace namespace = new Namespace("testrepo");
		JvmLoader clsloader = new JvmLoader(namespace, linker);
		clsloader.addModule("Test");
		try {
			Class<?> cls = clsloader.loadClass("Test");
			Method run = cls.getMethod("main", int.class);
			System.out.println("jvm result: " + run.invoke(null, 42));
		} catch (ClassNotFoundException
				| IllegalAccessException
				| NoSuchMethodException
				| InvocationTargetException e) {
			e.printStackTrace();
		}*/
	}
}
