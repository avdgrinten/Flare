
import static org.managarm.aurora.lang.AuTerm.mkConst;
import static org.managarm.aurora.lang.AuTerm.mkOperator;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.managarm.aurora.builtin.Mutation;
import org.managarm.aurora.builtin.Nil;
import org.managarm.aurora.lang.AuTerm;
import org.managarm.aurora.link.Interpreter;
import org.managarm.aurora.link.Linker;
import org.managarm.aurora.util.Descriptor;
import org.managarm.korona.CompilerHelper;
import org.managarm.korona.lang.ResolveMsg;

public class TestKorona {
	public static void main(String[] args) throws IOException {
		CompilerHelper helper = new CompilerHelper() {
			@Override public void onParseError(String message) {
				System.err.println("Could not parse source");
				System.err.println(message);
			}
			@Override public void onResolveSuccess(int num_messages) {
				System.out.println("Resolve success. "
						+ num_messages + " warnings!");
			}
			@Override public void onResolveFailure(int num_messages) {
				System.err.println("Resolve failure. "
						+ num_messages + " errors/warnings!");
			}
			@Override public void onResolveMessage(ResolveMsg msg) {
				System.err.println(msg.getLevel() + ": " + msg);
			}
		};
		if(!helper.compile(new File("libkorona/Base.kor")))
			return;
		if(!helper.compile(new File("examples/Test.kor")))
			return;
		
		Linker linker = new Linker();
		Iterator<AuTerm> it = helper.symbolIterator();
		while(it.hasNext())
			linker.linkSymbol(it.next());
		
		AuTerm main = linker.getSymbol("Test", "main",
				mkOperator(Mutation.mutatorType, mkConst(Nil.nilType)));
		if(main == null)
			return;
		Descriptor desc = new Descriptor(main);
		AuTerm defn = desc.get(new Descriptor.RecordPath("defn"));
		System.out.println(defn);
		
		Interpreter.World world = new Interpreter.World() {
			@Override public void print(String string) {
				System.out.println(string);
			}
		};
		
		Interpreter interpreter = new Interpreter();
		interpreter.execute(world, defn);
		
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
