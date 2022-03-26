// Generated from Filter.g by ANTLR 4.9.3
package org.zxc.service.parse;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link FilterParser}.
 */
public interface FilterListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link FilterParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(FilterParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(FilterParser.ParseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code gtExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterGtExpr(FilterParser.GtExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code gtExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitGtExpr(FilterParser.GtExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MulDiv}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMulDiv(FilterParser.MulDivContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MulDiv}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMulDiv(FilterParser.MulDivContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAddSub(FilterParser.AddSubContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAddSub(FilterParser.AddSubContext ctx);
	/**
	 * Enter a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterOrExpr(FilterParser.OrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitOrExpr(FilterParser.OrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code float}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterFloat(FilterParser.FloatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code float}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitFloat(FilterParser.FloatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code gtEqExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterGtEqExpr(FilterParser.GtEqExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code gtEqExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitGtEqExpr(FilterParser.GtEqExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParenExpr(FilterParser.ParenExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParenExpr(FilterParser.ParenExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code int}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterInt(FilterParser.IntContext ctx);
	/**
	 * Exit a parse tree produced by the {@code int}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitInt(FilterParser.IntContext ctx);
	/**
	 * Enter a parse tree produced by the {@code eqExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterEqExpr(FilterParser.EqExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code eqExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitEqExpr(FilterParser.EqExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code leExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterLeExpr(FilterParser.LeExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code leExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitLeExpr(FilterParser.LeExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code leEqExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterLeEqExpr(FilterParser.LeEqExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code leEqExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitLeEqExpr(FilterParser.LeEqExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code id}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterId(FilterParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by the {@code id}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitId(FilterParser.IdContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(FilterParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link FilterParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(FilterParser.AndExprContext ctx);
}