package org.managarm.korona.syntax;

import static org.managarm.util.peg.PegParser.transform;

import java.math.BigInteger;
import java.util.Arrays;

import org.managarm.util.peg.PegError;
import org.managarm.util.peg.PegGrammar;
import org.managarm.util.peg.PegItem;
import org.managarm.util.peg.PegParser;
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
		specSemicolon, specEqual, specColonEqual, specDoubleEqual, specInEqual,
			specPlus, specMinus, specTimes, specSlash, specPercent,
			specParL, specParR, specCurlyL, specCurlyR,
			specDot, specColon, specDoubleColon, specQm,
			specDoubleBar, specDoubleAnd, specLt, specGt, specLe, specGe, specDoubleLt, specDoubleGt,
			specArrow, specDoubleArrow,
		kwImport, kwModule, kwExport, kwExtern,
		kwMeta, kwLet, kwVar, kwIf, kwThen, kwElse, kwWhile, whileStm, kwReturn,
			kwEmbed, kwImplicit,
		argumentDecl, localDecl,
		expr, assignExpr, applyExpr, connectiveExpr, compareExpr,
			shiftExpr, addExpr, multExpr, accessExpr,
			tailExpr,
			parenExpr, letExpr, ifExpr, blockExpr,
			functionExpr, metaExpr, litNumberExpr, litStringExpr,
			identExpr,
		file, root, importRoot, moduleRoot, exportRoot, externRoot
	}
	private static PegGrammar<Tag> g = new PegGrammar<Tag>();
	
	{
		g.setRule(Tag.space, new PegItem.Repeat(
				new PegItem.TrivialChoice(
					g.ref(Tag.whitespace),
					g.ref(Tag.lineComment),
					g.ref(Tag.blockComment))));
		g.setRule(Tag.whitespace, new PegItem() {
			public Object parse(PegParser p) {
				if(p.eof())
					return new PegError.EofError(p.curSourceRef());
				char c = p.read();
				if(!(c == ' ' || c == '\t' || c == '\n'))
					return new PegError.ExpectError(p.curSourceRef(), "whitespace");
				
				do {
					if(!(c == ' ' || c == '\t' || c == '\n'))
						break;
					p.consume();
					if(p.eof())
						break;
					c = p.read();
				}while(c == ' ' || c == '\t' || c == '\n');
				return null;
			}
		});
		g.setRule(Tag.lineComment, new PegItem.Sequence(
				new PegItem.CharString("//"),
				new PegItem.Repeat(
					new PegItem.Not(new PegItem.SingleChar('\n'),
						new PegItem.Any(1))
				),
				new PegItem.Any(1)));
		g.setRule(Tag.blockComment, new PegItem.Sequence(
				new PegItem.CharString("/*"),
				new PegItem.Repeat(
					new PegItem.Not(new PegItem.CharString("*/"),
						new PegItem.Any(1))
				),
				new PegItem.Any(2)));
		
		g.setRule(Tag.number, new PegItem() {
			public Object parse(PegParser p) {
				p.parse(g.ref(Tag.space));

				if(p.eof())
					return new PegError.EofError(p.curSourceRef());
				char c = p.read();
				if(!(c >= '0' && c <= '9'))
					return new PegError.ExpectError(p.curSourceRef(), "number");

				StringBuilder s = new StringBuilder();
				do {
					s.append(c);
					p.consume();
					if(p.eof())
						break;
					c = p.read();
				} while(c >= '0' && c <= '9');
				return new BigInteger(s.toString());
			}
		});
		g.setRule(Tag.ident, new PegItem.TrivialChoice(
				g.ref(Tag.charIdent),
				g.ref(Tag.operatorIdent)));
		g.setRule(Tag.charIdent, new PegItem() {
			public Object parse(PegParser p) {
				p.parse(g.ref(Tag.space));
				
				if(p.eof())
					return new PegError.EofError(p.curSourceRef());
				char c = p.read();
				if(!(c >= 'a' && c <= 'z')
						&& !(c >= 'A' && c <= 'Z')
						&& c != '_')
					return new PegError.ExpectError(p.curSourceRef(), "identifier");

				StringBuilder s = new StringBuilder();
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
				return s.toString();
			}
		});
		g.setRule(Tag.operatorIdent, new PegItem() {
			public Object parse(PegParser p) {
				p.parse(g.ref(Tag.space));
				
				if(p.eof())
					return new PegError.EofError(p.curSourceRef());
				char c = p.read();
				if(c != '(')
					return new PegError.ExpectError(p.curSourceRef(), "operator identifier");

				StringBuilder s = new StringBuilder();
				do {
					s.append(c);
					p.consume();
					if(p.eof())
						return new PegError.EofError(p.curSourceRef());
					c = p.read();
					if(c == ' ')
						return new PegError.ExpectError(p.curSourceRef(), "space");
				} while(c != ')');
				p.consume();
				s.append(')');
				return s.toString();
			}
		});

		g.setRule(Tag.string, new PegItem() {
			public Object parse(PegParser p) {
				p.parse(g.ref(Tag.space));
				
				if(p.eof())
					return new PegError.EofError(p.curSourceRef());
				char c = p.read();
				if(c != '"')
					return new PegError.ExpectError(p.curSourceRef(), "string");
				
				StringBuilder s = new StringBuilder();
				while(true) {
					p.consume();
					if(p.eof())
						return new PegError.EofError(p.curSourceRef());
					c = p.read();
					if(c == '"')
						break;
					s.append(c);					
				};
				p.consume();
				return s.toString();
			}
		});

		g.setRule(Tag.specSemicolon, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar(';')), PegParser.forgetLeft));
		g.setRule(Tag.specPlus, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar('+')), PegParser.forgetLeft));
		g.setRule(Tag.specMinus, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar('-')), PegParser.forgetLeft));
		g.setRule(Tag.specTimes, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar('*')), PegParser.forgetLeft));
		g.setRule(Tag.specSlash, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar('/')), PegParser.forgetLeft));
		g.setRule(Tag.specPercent, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar('%')), PegParser.forgetLeft));
		g.setRule(Tag.specEqual, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar('=')), PegParser.forgetLeft));
		g.setRule(Tag.specColonEqual, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString(":=")), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleEqual, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("==")), PegParser.forgetLeft));
		g.setRule(Tag.specInEqual, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("!=")), PegParser.forgetLeft));
		g.setRule(Tag.specParL, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar('(')), PegParser.forgetLeft));
		g.setRule(Tag.specParR, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar(')')), PegParser.forgetLeft));
		g.setRule(Tag.specCurlyL, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar('{')), PegParser.forgetLeft));
		g.setRule(Tag.specCurlyR, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar('}')), PegParser.forgetLeft));
		g.setRule(Tag.specDot, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar('.')), PegParser.forgetLeft));
		g.setRule(Tag.specColon, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar(':')), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleColon, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("::")), PegParser.forgetLeft));
		g.setRule(Tag.specQm, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar('?')), PegParser.forgetLeft));
		g.setRule(Tag.specLt, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar('<')), PegParser.forgetLeft));
		g.setRule(Tag.specGt, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.SingleChar('>')), PegParser.forgetLeft));
		g.setRule(Tag.specLe, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("<=")), PegParser.forgetLeft));
		g.setRule(Tag.specGe, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString(">=")), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleLt, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("<<")), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleGt, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString(">>")), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleBar, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("||")), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleAnd, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("||")), PegParser.forgetLeft));
		g.setRule(Tag.specArrow, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("->")), PegParser.forgetLeft));
		g.setRule(Tag.specDoubleArrow, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("=>")), PegParser.forgetLeft));

		g.setRule(Tag.kwImport, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("import")), PegParser.forgetLeft));
		g.setRule(Tag.kwModule, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("module")), PegParser.forgetLeft));
		g.setRule(Tag.kwExport, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("export")), PegParser.forgetLeft));
		g.setRule(Tag.kwExtern, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("extern")), PegParser.forgetLeft));

		g.setRule(Tag.kwLet, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("let")), PegParser.forgetLeft));
		g.setRule(Tag.kwMeta, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("Meta")), PegParser.forgetLeft));
		g.setRule(Tag.kwVar, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("var")), PegParser.forgetLeft));
		g.setRule(Tag.kwIf, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("if")), PegParser.forgetLeft));
		g.setRule(Tag.kwThen, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("then")), PegParser.forgetLeft));
		g.setRule(Tag.kwElse, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("else")), PegParser.forgetLeft));
		g.setRule(Tag.kwWhile, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("while")), PegParser.forgetLeft));
		g.setRule(Tag.kwReturn, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("return")), PegParser.forgetLeft));

		g.setRule(Tag.kwEmbed, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("@embed")), PegParser.forgetLeft));
		g.setRule(Tag.kwImplicit, transform(new PegItem.Sequence(g.ref(Tag.space),
				new PegItem.CharString("@implicit")), PegParser.forgetLeft));

		g.setRule(Tag.argumentDecl, transform(
				new PegItem.Sequence(
					g.ref(Tag.ident),
					g.ref(Tag.specColon),
					g.ref(Tag.expr)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					String ident = (String)in[0];
					StNode type = (StNode)in[2];
					return new StArgumentDecl(ident, type);
				}}));
		g.setRule(Tag.localDecl, transform(
				new PegItem.Sequence(
					g.ref(Tag.kwVar),
					g.ref(Tag.ident),
					new PegItem.Optional(
						new PegItem.Sequence(
							g.ref(Tag.specColon),
							g.ref(Tag.expr))),
					new PegItem.Optional(
						new PegItem.Sequence(
							g.ref(Tag.specEqual),
							g.ref(Tag.expr))),
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
		g.setRule(Tag.assignExpr, transform(
				new PegItem.Sequence(
					g.ref(Tag.applyExpr),
					new PegItem.Repeat(
						new PegItem.Sequence(
							new PegItem.TrivialChoice(
								g.ref(Tag.specEqual)),
							g.ref(Tag.applyExpr)))),
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
		g.setRule(Tag.applyExpr, transform(
				new PegItem.Sequence(
					g.ref(Tag.connectiveExpr),
					new PegItem.Repeat(
						g.ref(Tag.connectiveExpr))),
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
		g.setRule(Tag.connectiveExpr, transform(
				new PegItem.Sequence(
					g.ref(Tag.compareExpr),
					new PegItem.Repeat(
						new PegItem.Sequence(
							new PegItem.TrivialChoice(
								g.ref(Tag.specDoubleBar),
								g.ref(Tag.specDoubleAnd)),
							g.ref(Tag.compareExpr)))),
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
		g.setRule(Tag.compareExpr, transform(
				new PegItem.Sequence(
					g.ref(Tag.shiftExpr),
					new PegItem.Repeat(
						new PegItem.Sequence(
							new PegItem.TrivialChoice(
								g.ref(Tag.specDoubleEqual),
								g.ref(Tag.specInEqual), 
								g.ref(Tag.specLe),
								g.ref(Tag.specGe),
								g.ref(Tag.specLt),
								g.ref(Tag.specGt)),
							g.ref(Tag.shiftExpr)))),
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
						}else if(operator.equals("!=")) {
							res = applyOperator("(!=)", res, right);
						}else if(operator.equals("<=")) {
							res = applyOperator("(<=)", res, right);
						}else if(operator.equals(">=")) {
							res = applyOperator("(>=)", res, right);
						}else if(operator.equals("<")) {
							res = applyOperator("(<)", res, right);
						}else if(operator.equals(">")) {
							res = applyOperator("(>)", res, right);
						}else throw new AssertionError("Illegal operator " + operator);
					}
					return res;
				}}));
		g.setRule(Tag.shiftExpr, transform(
				new PegItem.Sequence(
					g.ref(Tag.addExpr),
					new PegItem.Repeat(
						new PegItem.Sequence(
							new PegItem.TrivialChoice(
								g.ref(Tag.specDoubleLt),
								g.ref(Tag.specDoubleGt)
							),
							g.ref(Tag.addExpr)))),
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
		g.setRule(Tag.addExpr, transform(
				new PegItem.Sequence(
					g.ref(Tag.multExpr),
					new PegItem.Repeat(
						new PegItem.Sequence(
							new PegItem.TrivialChoice(
								g.ref(Tag.specPlus),
								g.ref(Tag.specMinus)),
							g.ref(Tag.multExpr)))),
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
		g.setRule(Tag.multExpr, transform(
				new PegItem.Sequence(
					g.ref(Tag.accessExpr),
					new PegItem.Repeat(
						new PegItem.Sequence(
							new PegItem.TrivialChoice(
								g.ref(Tag.specTimes),
								g.ref(Tag.specSlash),
								g.ref(Tag.specPercent)),
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
		g.setRule(Tag.accessExpr, transform(
				new PegItem.Sequence(
					g.ref(Tag.tailExpr),
					new PegItem.Repeat(
						new PegItem.Sequence(
							g.ref(Tag.specDot),
							g.ref(Tag.ident)))),
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
		g.setRule(Tag.tailExpr, new PegItem.Choice(
				g.ref(Tag.parenExpr),
				g.ref(Tag.letExpr),
				g.ref(Tag.ifExpr),
				g.ref(Tag.blockExpr),
				g.ref(Tag.functionExpr),
				g.ref(Tag.metaExpr),
				g.ref(Tag.litNumberExpr),
				g.ref(Tag.litStringExpr),
				g.ref(Tag.identExpr)));
		
		g.setRule(Tag.parenExpr, transform(new PegItem.Sequence(g.ref(Tag.specParL),
				g.ref(Tag.expr), g.ref(Tag.specParR)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					return (StNode)in[1];
				}}));
		g.setRule(Tag.letExpr, transform(
				new PegItem.Sequence(
					g.ref(Tag.kwLet),
					g.ref(Tag.ident),
					g.ref(Tag.specColonEqual),
					g.ref(Tag.expr),
					g.ref(Tag.specDoubleColon),
					g.ref(Tag.expr)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					String name = (String)in[1];
					StNode defn = (StNode)in[3];
					StNode expr = (StNode)in[5];
					return new StLetExpr(name, defn, expr);
				}
			}));
		g.setRule(Tag.ifExpr, transform(
				new PegItem.Sequence(
					g.ref(Tag.kwIf),
					g.ref(Tag.expr),
					g.ref(Tag.kwThen),
					g.ref(Tag.expr),
					g.ref(Tag.kwElse),
					g.ref(Tag.expr)),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode expr = (StNode)in[1];
					StNode then_case = (StNode)in[3];
					StNode else_case = (StNode)in[5];
					return applyOperator("(ite)", expr, then_case, else_case);
				}}));
		g.setRule(Tag.blockExpr, transform(new PegItem.Sequence(g.ref(Tag.specCurlyL),
				new PegItem.Repeat(transform(new PegItem.Sequence(g.ref(Tag.expr), g.ref(Tag.specSemicolon)),
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
		
		g.setRule(Tag.functionExpr, transform(
				new PegItem.Sequence(
					new PegItem.Optional(g.ref(Tag.kwImplicit)),
					g.ref(Tag.argumentDecl),
					new PegItem.TrivialChoice(
						g.ref(Tag.specArrow),
						g.ref(Tag.specDoubleArrow)
					),
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
		g.setRule(Tag.identExpr, transform(
				new PegItem.Not(
					new PegItem.TrivialChoice(
						g.ref(Tag.kwIf),
						g.ref(Tag.kwThen),
						g.ref(Tag.kwElse),
						g.ref(Tag.kwReturn)),
					g.ref(Tag.ident)),
			new PegTransform<String>() {
				@Override public Object transform(String in) {
					return new StIdent(in);
				}}));
		
		g.setRule(Tag.file, transform(
				new PegItem.Until(g.ref(Tag.root),
					new PegItem.Sequence(
						g.ref(Tag.space),
						new PegItem.Eof())),
			new PegTransform<Object[]>() {
				@Override public Object transform(Object[] in) {
					StNode[] elements = Arrays.copyOf(in, in.length, StNode[].class);
					return new StFile(elements);
				}}));
		g.setRule(Tag.root, new PegItem.Choice(
				g.ref(Tag.importRoot),
				g.ref(Tag.moduleRoot),
				g.ref(Tag.exportRoot),
				g.ref(Tag.externRoot)));
		g.setRule(Tag.importRoot, transform(
				new PegItem.Sequence(
					new PegItem.Trivial(g.ref(Tag.kwImport)),
					g.ref(Tag.ident),
					g.ref(Tag.specSemicolon)),
				new PegTransform<Object[]>() {
			@Override public Object transform(Object[] in) {
				String module = (String)in[1];
				return new StRoot.Import(module);
			}}));
		g.setRule(Tag.moduleRoot, transform(
				new PegItem.Sequence(
					new PegItem.Trivial(g.ref(Tag.kwModule)),
					g.ref(Tag.ident),
					g.ref(Tag.specCurlyL),
					new PegItem.Until(
						g.ref(Tag.root),
						g.ref(Tag.specCurlyR))),
				new PegTransform<Object[]>() {
			@Override public Object transform(Object[] in) {
				String module = (String)in[1];
				Object[] members = (Object[])in[3];
				return new StRoot.Module(module, Arrays.copyOf(members,
						members.length, StNode[].class));
			}}));
		g.setRule(Tag.exportRoot, transform(
				new PegItem.Sequence(
					new PegItem.Optional(g.ref(Tag.kwEmbed)),
					new PegItem.Trivial(g.ref(Tag.kwExport)),
					g.ref(Tag.ident),
					g.ref(Tag.specColonEqual),
					g.ref(Tag.expr),
					g.ref(Tag.specSemicolon)),
				new PegTransform<Object[]>() {
			@Override public Object transform(Object[] in) {
				int flags = StRoot.Symbol.kFlagExport;
				if(in[0] != null)
					flags |= StRoot.Symbol.kFlagEmbed;
				String ident = (String)in[2];
				StNode value = (StNode)in[4];
				return new StRoot.Symbol(ident, null, value, flags);
			}}));
		g.setRule(Tag.externRoot, transform(
				new PegItem.Sequence(
					new PegItem.Trivial(g.ref(Tag.kwExtern)),
					g.ref(Tag.ident),
					g.ref(Tag.specDoubleColon),
					g.ref(Tag.expr),
					g.ref(Tag.specSemicolon)),
				new PegTransform<Object[]>() {
			@Override public Object transform(Object[] in) {
				String ident = (String)in[1];
				StNode type = (StNode)in[3];
				return new StRoot.Symbol(ident, type, null,
						StRoot.Symbol.kFlagExtern);
			}}));
	}
	
	private PegParser p_parser;
	private StFile p_result;
	private PegError p_error;
	
	public void parse(String input) {
		p_parser = new PegParser(input);
		Object res = p_parser.parse(g.getRule(Tag.file));
		if(res instanceof PegError) {
			p_error = (PegError)res;
		}else{
			p_result = (StFile)res;
		}
	}
	
	public boolean okay() {
		return p_error == null;
	}
	public String getError() {
		return p_error.format(p_parser);
	}
	public StFile getResult() {
		return p_result;
	}
}
