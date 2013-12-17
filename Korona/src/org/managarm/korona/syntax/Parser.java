package org.managarm.korona.syntax;

import static org.managarm.util.peg.PegParser.choice;
import static org.managarm.util.peg.PegParser.not;
import static org.managarm.util.peg.PegParser.optional;
import static org.managarm.util.peg.PegParser.repeat;
import static org.managarm.util.peg.PegParser.sequence;
import static org.managarm.util.peg.PegParser.transform;

import java.math.BigInteger;
import java.util.Arrays;

import org.managarm.util.peg.PegGrammar;
import org.managarm.util.peg.PegItem;
import org.managarm.util.peg.PegParser;
import org.managarm.util.peg.PegResult;
import org.managarm.util.peg.PegTransform;

public class Parser {
	private StNode applyOperator(String operator, StNode... args) {
		StNode res = new StIdent(operator);
		for(int i = 0; i < args.length; i++)
			res = new StApply(res, args[i]);
		return res;
	}
	
	private enum Tag {
		space, whitespace, lineComment, blockComment,
		number,
		ident, charIdent, operatorIdent,
		string,
		specSemicolon, specEqual, specColonEqual, specDoubleEqual,
			specPlus, specMinus, specTimes, specSlash, specPercent,
			specParL, specParR, specCurlyL, specCurlyR,
			specDot, specColon, specDoubleColon, specQm,
			specDoubleBar, specDoubleAnd, specLt, specDoubleLt, specDoubleGt,
			specArrow, specDoubleArrow,
		kwImport, kwModule, kwExport, kwExtern,
		kwMeta, kwVar, kwIf, kwThen, kwElse, kwWhile, whileStm, kwReturn,
			kwEmbed, kwImplicit,
		argumentDecl, localDecl,
		expr, assignExpr, applyExpr, connectiveExpr, compareExpr,
			shiftExpr, addExpr, multExpr, accessExpr,
			tailExpr,
			parenExpr, ifExpr, blockExpr,
			functionExpr, metaExpr, litNumberExpr, litStringExpr,
			identExpr,
		file, root, importRoot, moduleRoot, exportRoot, externRoot
	}
	private static PegGrammar<Tag> g = new PegGrammar<Tag>();
	
	{
		g.setRule(Tag.space, repeat(choice(g.ref(Tag.whitespace),
				g.ref(Tag.lineComment), g.ref(Tag.blockComment))));
		g.setRule(Tag.whitespace, new PegItem() {
			public PegResult parse(PegParser p) {
				if(p.eof())
					return PegResult.failure();
				char c = p.read();
				if(!(c == ' ' || c == '\t' || c == '\n'))
					return PegResult.failure();
				
				do {
					if(!(c == ' ' || c == '\t' || c == '\n'))
						break;
					p.consume();
					if(p.eof())
						break;
					c = p.read();
				}while(c == ' ' || c == '\t' || c == '\n');
				return PegResult.success();
			}
		});
		g.setRule(Tag.lineComment, sequence(PegParser.string("//"),
				repeat(not(PegParser.singleChar('\n'),
						PegParser.any(1))), PegParser.any(1)));
		g.setRule(Tag.blockComment, sequence(PegParser.string("/*"),
				repeat(not(PegParser.string("*/"),
						PegParser.any(1))), PegParser.any(2)));
		
		g.setRule(Tag.number, new PegItem() {
			public PegResult parse(PegParser p) {
				p.parse(g.ref(Tag.space));

				if(p.eof())
					return PegResult.failure();
				char c = p.read();
				if(!(c >= '0' && c <= '9'))
					return PegResult.failure();

				StringBuffer s = new StringBuffer();
				do {
					s.append(c);
					p.consume();
					if(p.eof())
						break;
					c = p.read();
				} while(c >= '0' && c <= '9');
				return PegResult.success(new BigInteger(s.toString()));
			}
		});
		g.setRule(Tag.ident, choice(g.ref(Tag.charIdent),
				g.ref(Tag.operatorIdent)));
		g.setRule(Tag.charIdent, new PegItem() {
			public PegResult parse(PegParser p) {
				p.parse(g.ref(Tag.space));
				
				if(p.eof())
					return PegResult.failure();
				char c = p.read();
				if(!(c >= 'a' && c <= 'z')
						&& !(c >= 'A' && c <= 'Z')
						&& c != '_')
					return PegResult.failure();

				StringBuffer s = new StringBuffer();
				do {
					s.append(c);
					p.consume();
					if(p.eof())
						break;
					c = p.read();
				} while((c >= 'a' && c <= 'z')
						|| (c >= 'A' && c <= 'Z')
						|| (c >= '0' && c <= '9')
						|| c == '_');
				return PegResult.success(s.toString());
			}
		});
		g.setRule(Tag.operatorIdent, new PegItem() {
			public PegResult parse(PegParser p) {
				p.parse(g.ref(Tag.space));
				
				if(p.eof())
					return PegResult.failure();
				char c = p.read();
				if(c != '(')
					return PegResult.failure();

				StringBuffer s = new StringBuffer();
				do {
					s.append(c);
					p.consume();
					if(p.eof())
						return PegResult.failure();;
					c = p.read();
					if(c == ' ')
						return PegResult.failure();
				} while(c != ')');
				p.consume();
				s.append(')');
				return PegResult.success(s.toString());
			}
		});

		g.setRule(Tag.string, new PegItem() {
			public PegResult parse(PegParser p) {
				p.parse(g.ref(Tag.space));
				
				if(p.eof())
					return PegResult.failure();
				char c = p.read();
				if(c != '"')
					return PegResult.failure();
				
				StringBuffer s = new StringBuffer();
				while(true) {
					p.consume();
					if(p.eof())
						return PegResult.failure();
					c = p.read();
					if(c == '"')
						break;
					s.append(c);					
				};
				p.consume();
				return PegResult.success(s.toString());
			}
		});

		g.setRule(Tag.specSemicolon, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar(';')), PegParser.forgetLeft));
		g.setRule(Tag.specPlus, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar('+')), PegParser.forgetLeft));
		g.setRule(Tag.specMinus, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar('-')), PegParser.forgetLeft));
		g.setRule(Tag.specTimes, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar('*')), PegParser.forgetLeft));
		g.setRule(Tag.specSlash, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar('/')), PegParser.forgetLeft));
		g.setRule(Tag.specPercent, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar('%')), PegParser.forgetLeft));
		g.setRule(Tag.specEqual, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar('=')), PegParser.forgetLeft));
		g.setRule(Tag.specColonEqual, transform(sequence(g.ref(Tag.space),
				PegParser.string(":=")), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleEqual, transform(sequence(g.ref(Tag.space),
				PegParser.string("==")), PegParser.forgetLeft));
		g.setRule(Tag.specParL, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar('(')), PegParser.forgetLeft));
		g.setRule(Tag.specParR, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar(')')), PegParser.forgetLeft));
		g.setRule(Tag.specCurlyL, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar('{')), PegParser.forgetLeft));
		g.setRule(Tag.specCurlyR, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar('}')), PegParser.forgetLeft));
		g.setRule(Tag.specDot, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar('.')), PegParser.forgetLeft));
		g.setRule(Tag.specColon, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar(':')), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleColon, transform(sequence(g.ref(Tag.space),
				PegParser.string("::")), PegParser.forgetLeft));
		g.setRule(Tag.specQm, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar('?')), PegParser.forgetLeft));
		g.setRule(Tag.specLt, transform(sequence(g.ref(Tag.space),
				PegParser.singleChar('<')), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleLt, transform(sequence(g.ref(Tag.space),
				PegParser.string("<<")), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleGt, transform(sequence(g.ref(Tag.space),
				PegParser.string(">>")), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleBar, transform(sequence(g.ref(Tag.space),
				PegParser.string("||")), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleAnd, transform(sequence(g.ref(Tag.space),
				PegParser.string("||")), PegParser.forgetLeft));
		g.setRule(Tag.specArrow, transform(sequence(g.ref(Tag.space),
				PegParser.string("->")), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleArrow, transform(sequence(g.ref(Tag.space),
				PegParser.string("=>")), PegParser.forgetLeft));

		g.setRule(Tag.kwImport, transform(sequence(g.ref(Tag.space),
				PegParser.string("import")), PegParser.forgetLeft));
		g.setRule(Tag.kwModule, transform(sequence(g.ref(Tag.space),
				PegParser.string("module")), PegParser.forgetLeft));
		g.setRule(Tag.kwExport, transform(sequence(g.ref(Tag.space),
				PegParser.string("export")), PegParser.forgetLeft));
		g.setRule(Tag.kwExtern, transform(sequence(g.ref(Tag.space),
				PegParser.string("extern")), PegParser.forgetLeft));

		g.setRule(Tag.kwMeta, transform(sequence(g.ref(Tag.space),
				PegParser.string("Meta")), PegParser.forgetLeft));
		g.setRule(Tag.kwVar, transform(sequence(g.ref(Tag.space),
				PegParser.string("var")), PegParser.forgetLeft));
		g.setRule(Tag.kwIf, transform(sequence(g.ref(Tag.space),
				PegParser.string("if")), PegParser.forgetLeft));
		g.setRule(Tag.kwThen, transform(sequence(g.ref(Tag.space),
				PegParser.string("then")), PegParser.forgetLeft));
		g.setRule(Tag.kwElse, transform(sequence(g.ref(Tag.space),
				PegParser.string("else")), PegParser.forgetLeft));
		g.setRule(Tag.kwWhile, transform(sequence(g.ref(Tag.space),
				PegParser.string("while")), PegParser.forgetLeft));
		g.setRule(Tag.kwReturn, transform(sequence(g.ref(Tag.space),
				PegParser.string("return")), PegParser.forgetLeft));

		g.setRule(Tag.kwEmbed, transform(sequence(g.ref(Tag.space),
				PegParser.string("@embed")), PegParser.forgetLeft));
		g.setRule(Tag.kwImplicit, transform(sequence(g.ref(Tag.space),
				PegParser.string("@implicit")), PegParser.forgetLeft));

		g.setRule(Tag.argumentDecl, transform(sequence(g.ref(Tag.ident),
				g.ref(Tag.specColon), g.ref(Tag.expr)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					String ident = (String)in[0];
					StNode type = (StNode)in[2];
					return new StArgumentDecl(ident, type);
				}}));
		g.setRule(Tag.localDecl, transform(sequence(g.ref(Tag.kwVar), g.ref(Tag.ident),
				optional(sequence(g.ref(Tag.specColon), g.ref(Tag.expr))),
				optional(sequence(g.ref(Tag.specEqual), g.ref(Tag.expr))),
				g.ref(Tag.specSemicolon)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					String ident = (String)in[1];
					Object[] type_seq = (Object[])in[2];
					Object[] init_seq = (Object[])in[3];
					StNode type = type_seq == null ? null : (StNode)type_seq[1];
					StNode initializer = init_seq == null ? null : (StNode)init_seq[1];
					return new StLocalDecl(ident, type, initializer);
				}}));
		
		/*g.setRule(Tag.stm, choice(g.ref(Tag.blockStm),
				g.ref(Tag.whileStm),
				g.ref(Tag.returnStm), g.ref(Tag.exprStm)));
		g.setRule(Tag.whileStm, transform(sequence(g.ref(Tag.kwWhile), g.ref(Tag.specParL),
				g.ref(Tag.expr), g.ref(Tag.specParR), g.ref(Tag.stm)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode expr = (StNode)in[2];
					StNode stm = (StNode)in[4];
					return new StWhile(expr, stm);
				}}));
		g.setRule(Tag.blockStm, transform(sequence(g.ref(Tag.specCurlyL),
				repeat(choice(g.ref(Tag.localDecl), g.ref(Tag.stm))),
				g.ref(Tag.specCurlyR)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					Object[] statements = (Object[])in[1];
					return new StBlock(Arrays.copyOf(statements,
							statements.length, StNode[].class));
				}}));
		g.setRule(Tag.returnStm, transform(sequence(g.ref(Tag.kwReturn),
				g.ref(Tag.expr), g.ref(Tag.specSemicolon)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode result = (StNode)in[1];
					return new StReturn(result);
				}}));
		
		g.setRule(Tag.exprStm, transform(sequence(g.ref(Tag.expr), g.ref(Tag.specSemicolon)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode expr = (StNode)in[0];
					return new StExprStm(expr);
				}}));*/
		
		g.setRule(Tag.expr, g.ref(Tag.assignExpr));
		g.setRule(Tag.assignExpr, transform(sequence(g.ref(Tag.applyExpr),
				repeat(sequence(choice(g.ref(Tag.specEqual)), g.ref(Tag.applyExpr)))),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode left = (StNode)in[0];
					Object[] tail = (Object[])in[1];
					
					StNode res = left;
					for(int i = 0; i < tail.length; i++) {
						String operator = (String)((Object[])tail[i])[0];
						StNode right = (StNode)((Object[])tail[i])[1];
						if(operator.equals("=")) {
							res = applyOperator("(=)", res, right);
						}else throw new AssertionError("Illegal operator " + operator);
					}
					return res;
				}}));
		g.setRule(Tag.applyExpr, transform(sequence(g.ref(Tag.connectiveExpr),
				repeat(g.ref(Tag.connectiveExpr))),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode left = (StNode)in[0];
					Object[] tail = (Object[])in[1];
					
					StNode res = left;
					for(int i = 0; i < tail.length; i++) {
						StNode argument = (StNode)tail[i];
						res = new StApply(res, argument);
					}
					return res;
				}}));
		g.setRule(Tag.connectiveExpr, transform(sequence(g.ref(Tag.compareExpr),
				repeat(sequence(choice(g.ref(Tag.specDoubleBar),
						g.ref(Tag.specDoubleAnd)
					), g.ref(Tag.compareExpr)))),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode left = (StNode)in[0];
					Object[] tail = (Object[])in[1];
					
					StNode res = left;
					for(int i = 0; i < tail.length; i++) {
						String operator = (String)((Object[])tail[i])[0];
						StNode right = (StNode)((Object[])tail[i])[1];
						if(operator.equals("||")) {
							res = applyOperator("(||)", res, right);
						}else if(operator.equals("&&")) {
							res = applyOperator("(&&)", res, right);
						}else throw new AssertionError("Illegal operator " + operator);
					}
					return res;
				}}));
		g.setRule(Tag.compareExpr, transform(sequence(g.ref(Tag.shiftExpr),
				repeat(sequence(choice(g.ref(Tag.specDoubleEqual),
						g.ref(Tag.specLt)
					), g.ref(Tag.shiftExpr)))),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode left = (StNode)in[0];
					Object[] tail = (Object[])in[1];
					
					StNode res = left;
					for(int i = 0; i < tail.length; i++) {
						String operator = (String)((Object[])tail[i])[0];
						StNode right = (StNode)((Object[])tail[i])[1];
						if(operator.equals("==")) {
							res = applyOperator("(==)", res, right);
						}else if(operator.equals("<")) {
							res = applyOperator("(<)", res, right);
						}else throw new AssertionError("Illegal operator " + operator);
					}
					return res;
				}}));
		g.setRule(Tag.shiftExpr, transform(sequence(g.ref(Tag.addExpr),
				repeat(sequence(choice(g.ref(Tag.specDoubleLt),
					g.ref(Tag.specDoubleGt)), g.ref(Tag.addExpr)))),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode left = (StNode)in[0];
					Object[] tail = (Object[])in[1];
					
					StNode res = left;
					for(int i = 0; i < tail.length; i++) {
						String operator = (String)((Object[])tail[i])[0];
						StNode right = (StNode)((Object[])tail[i])[1];
						if(operator.equals("<<")) {
							res = applyOperator("(<<)", res, right);
						}else if(operator.equals(">>")) {
							res = applyOperator("(>>)", res, right);
						}else throw new AssertionError("Illegal operator " + operator);
					}
					return res;
				}}));
		g.setRule(Tag.addExpr, transform(sequence(g.ref(Tag.multExpr),
				repeat(sequence(choice(g.ref(Tag.specPlus),
						g.ref(Tag.specMinus)), g.ref(Tag.multExpr)))),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode left = (StNode)in[0];
					Object[] tail = (Object[])in[1];
					
					StNode res = left;
					for(int i = 0; i < tail.length; i++) {
						String operator = (String)((Object[])tail[i])[0];
						StNode right = (StNode)((Object[])tail[i])[1];
						if(operator.equals("+")) {
							res = applyOperator("(+)", res, right);
						}else if(operator.equals("-")) {
							res = applyOperator("(-)", res, right);
						}else throw new AssertionError("Illegal operator " + operator);
					}
					return res;
				}}));
		g.setRule(Tag.multExpr, transform(sequence(g.ref(Tag.accessExpr),
				repeat(sequence(choice(g.ref(Tag.specTimes),
						g.ref(Tag.specSlash), g.ref(Tag.specPercent)),
					g.ref(Tag.accessExpr)))),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode left = (StNode)in[0];
					Object[] tail = (Object[])in[1];
					
					StNode res = left;
					for(int i = 0; i < tail.length; i++) {
						Object operator = ((Object[])tail[i])[0];
						StNode right = (StNode)((Object[])tail[i])[1];
						if(operator.equals("*")) {
							res = applyOperator("(*)", res, right);
						}else if(operator.equals("/")) {
							res = applyOperator("(/)", res, right);
						}else if(operator.equals("%")) {
							res = applyOperator("(%)", res, right);
						}else throw new AssertionError("Illegal operator " + operator);
					}
					return res;
				}}));
		g.setRule(Tag.accessExpr, transform(sequence(g.ref(Tag.tailExpr),
				repeat(sequence(g.ref(Tag.specDot), g.ref(Tag.ident)))),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode left = (StNode)in[0];
					Object[] tail = (Object[])in[1];
					
					StNode res = left;
					for(int i = 0; i < tail.length; i++) {
						String right = (String)((Object[])tail[i])[1];
						res = new StAccess(res, right);
					}
					return res;
				}}));
		g.setRule(Tag.tailExpr, choice(g.ref(Tag.parenExpr),
				g.ref(Tag.ifExpr), g.ref(Tag.blockExpr), g.ref(Tag.functionExpr),
				g.ref(Tag.metaExpr),
				g.ref(Tag.litNumberExpr), g.ref(Tag.litStringExpr),
				g.ref(Tag.identExpr)));
		
		g.setRule(Tag.parenExpr, transform(sequence(g.ref(Tag.specParL),
				g.ref(Tag.expr), g.ref(Tag.specParR)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					return (StNode)in[1];
				}}));
		g.setRule(Tag.ifExpr, transform(sequence(g.ref(Tag.kwIf),
				g.ref(Tag.expr), g.ref(Tag.kwThen), g.ref(Tag.expr),
				g.ref(Tag.kwElse), g.ref(Tag.expr)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode expr = (StNode)in[1];
					StNode then_case = (StNode)in[3];
					StNode else_case = (StNode)in[5];
					return applyOperator("(ite)", expr, then_case, else_case);
				}}));
		g.setRule(Tag.blockExpr, transform(sequence(g.ref(Tag.specCurlyL),
				repeat(transform(sequence(g.ref(Tag.expr), g.ref(Tag.specSemicolon)),
						PegParser.forgetRight)),
				g.ref(Tag.specCurlyR)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					Object[] tail = (Object[])in[1];
					
					StNode res = (StNode)tail[tail.length - 1];
					for(int i = tail.length - 2; i >= 0; i--) {
						StNode left = (StNode)tail[i];
						res = applyOperator("(chain)", left, res);
					}
					return res;
				}}));
		
		g.setRule(Tag.functionExpr, transform(sequence(
				optional(g.ref(Tag.kwImplicit)),
				g.ref(Tag.argumentDecl),
				choice(g.ref(Tag.specArrow), g.ref(Tag.specDoubleArrow)),
				g.ref(Tag.expr)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					boolean implicit = in[0] != null;
					StArgumentDecl arg = (StArgumentDecl)in[1];
					String operator = (String)in[2];
					StNode expr = (StNode)in[3];
					if(operator.equals("->")) {
						return new StPi(arg, expr);
					}else if(operator.equals("=>")) {
						return new StLambda(arg, expr, implicit);
					}else throw new AssertionError("Illegal operator " + operator);
				}}));

		g.setRule(Tag.metaExpr, transform(g.ref(Tag.kwMeta),
			new PegTransform<String>() {
				@Override public Object transform(String in) {
					return new StMeta();
				}}));
		g.setRule(Tag.litNumberExpr, transform(g.ref(Tag.number),
			new PegTransform<BigInteger>() {
				@Override public Object transform(BigInteger in) {
					return new StLiteral.LitInt(in);
				}}));
		g.setRule(Tag.litStringExpr, transform(g.ref(Tag.string),
			new PegTransform<String>() {
				@Override public Object transform(String in) {
					return new StLiteral.LitString(in);
				}}));
		g.setRule(Tag.identExpr, transform(not(choice(
					g.ref(Tag.kwIf),
					g.ref(Tag.kwThen),
					g.ref(Tag.kwElse),
					g.ref(Tag.kwReturn)
				), g.ref(Tag.ident)),
				new PegTransform<String>() {
					@Override public Object transform(String in) {
						return new StIdent(in);
					}}));
		
		g.setRule(Tag.file, transform(repeat(g.ref(Tag.root)),
				new PegTransform<Object[]>() {
			@Override public Object transform(Object[] in) {
				StNode[] elements = Arrays.copyOf(in, in.length, StNode[].class);
				return new StFile(elements);
			}}));
		g.setRule(Tag.root, choice(g.ref(Tag.importRoot),
				g.ref(Tag.moduleRoot),
				g.ref(Tag.exportRoot), g.ref(Tag.externRoot)));
		g.setRule(Tag.importRoot, transform(sequence(g.ref(Tag.kwImport),
					g.ref(Tag.ident), g.ref(Tag.specSemicolon)),
				new PegTransform<Object[]>() {
			@Override public Object transform(Object[] in) {
				String module = (String)in[1];
				return new StRoot.Import(module);
			}}));
		g.setRule(Tag.moduleRoot, transform(sequence(g.ref(Tag.kwModule),
				g.ref(Tag.ident), g.ref(Tag.specCurlyL),
				repeat(g.ref(Tag.root)), g.ref(Tag.specCurlyR)),
				new PegTransform<Object[]>() {
			@Override public Object transform(Object[] in) {
				String module = (String)in[1];
				Object[] members = (Object[])in[3];
				return new StRoot.Module(module, Arrays.copyOf(members,
						members.length, StNode[].class));
			}}));
		g.setRule(Tag.exportRoot, transform(sequence(optional(g.ref(Tag.kwEmbed)),
				g.ref(Tag.kwExport), g.ref(Tag.ident),
				g.ref(Tag.specColonEqual), g.ref(Tag.expr), g.ref(Tag.specSemicolon)),
				new PegTransform<Object[]>() {
			@Override public Object transform(Object[] in) {
				int flags = StRoot.Symbol.kFlagExport;
				if(in[0] != null)
					flags |= StRoot.Symbol.kFlagEmbed;
				String ident = (String)in[2];
				StNode value = (StNode)in[4];
				return new StRoot.Symbol(ident, null, value, flags);
			}}));
		g.setRule(Tag.externRoot, transform(sequence(g.ref(Tag.kwExtern), g.ref(Tag.ident),
				g.ref(Tag.specDoubleColon), g.ref(Tag.expr), g.ref(Tag.specSemicolon)),
				new PegTransform<Object[]>() {
			@Override public Object transform(Object[] in) {
				String ident = (String)in[1];
				StNode type = (StNode)in[3];
				return new StRoot.Symbol(ident, type, null,
						StRoot.Symbol.kFlagExtern);
			}}));
	}
	
	public StFile parse(String input) {
		PegParser parser = new PegParser(input);
		PegResult res = parser.parse(g.getRule(Tag.file));
		if(!res.okay())
			return null;
		parser.parse(g.getRule(Tag.space));
		if(!parser.eof())
			return null;
		return res.<StFile>object();
	}
}
