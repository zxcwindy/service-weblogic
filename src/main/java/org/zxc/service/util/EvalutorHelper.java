package org.zxc.service.util;

import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.zxc.service.parse.CandleEntryListener;
import org.zxc.service.parse.FilterLexer;
import org.zxc.service.parse.FilterParser;
import org.zxc.service.stock.CandleEntry;

public class EvalutorHelper {

	public static boolean eval(String condition, List<CandleEntry> m30CandleList, List<CandleEntry> dayCandleList,
			List<CandleEntry> weekCandleList, List<CandleEntry> monthCandleList) {
		FilterLexer lexer = new FilterLexer(CharStreams.fromString(condition));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		FilterParser parse = new FilterParser(tokens);

		Stack<Boolean> booleanStack = new Stack<>();
		CandleEntryListener listener = new CandleEntryListener(booleanStack, m30CandleList, dayCandleList,
				weekCandleList, monthCandleList);

		ParseTreeWalker walker = new ParseTreeWalker();
		ParseTree tree = parse.parse();
		walker.walk(listener, tree);
		return booleanStack.peek();
	}
}
