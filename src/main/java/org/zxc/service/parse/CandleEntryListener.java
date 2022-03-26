package org.zxc.service.parse;

import java.util.List;
import java.util.Stack;

import org.zxc.service.parse.FilterParser.AddSubContext;
import org.zxc.service.parse.FilterParser.AndExprContext;
import org.zxc.service.parse.FilterParser.EqExprContext;
import org.zxc.service.parse.FilterParser.FloatContext;
import org.zxc.service.parse.FilterParser.GtEqExprContext;
import org.zxc.service.parse.FilterParser.GtExprContext;
import org.zxc.service.parse.FilterParser.IdContext;
import org.zxc.service.parse.FilterParser.IntContext;
import org.zxc.service.parse.FilterParser.LeEqExprContext;
import org.zxc.service.parse.FilterParser.LeExprContext;
import org.zxc.service.parse.FilterParser.MulDivContext;
import org.zxc.service.parse.FilterParser.OrExprContext;
import org.zxc.service.stock.CandleEntry;
import org.zxc.service.stock.CandleEntryFuntion;

/**
 * 对CandleEntry集合支持 类似表达式
 * <p><code>1.d.k > 3 && 1.d.m == 0 && (1.d.ma5 > 1.d.ma10 && 2.w.ma5 < 2.w.ma10)</code>
 * <p><code>13.d.ma5 < 13.d.ma10 && 12.d.ma5 > 12.d.ma10</code>
 * <p><code>1.d.macd == 0.36 && 1.w.macd < 0</code>
 * @author david 2022年3月26日
 */
public class CandleEntryListener extends FilterBaseListener {
	private Stack<Double> numStack = new Stack<>();
	private Stack<Boolean> booleanStack;
	private List<CandleEntry> m30CandleList;
	private List<CandleEntry> dayCandleList;
	private List<CandleEntry> weekCandleList;
	private List<CandleEntry> monthCandleList;

	public CandleEntryListener(Stack<Boolean> booleanStack, List<CandleEntry> m30CandleList,
			List<CandleEntry> dayCandleList, List<CandleEntry> weekCandleList, List<CandleEntry> monthCandleList) {
		this.booleanStack = booleanStack;
		this.m30CandleList = m30CandleList;
		this.dayCandleList = dayCandleList;
		this.weekCandleList = weekCandleList;
		this.monthCandleList = monthCandleList;
	}

	@Override
	public void exitEqExpr(EqExprContext ctx) {
		double rightValue = numStack.pop();
		double leftValue = numStack.pop();
		booleanStack.push(leftValue== rightValue);
	}

	@Override
	public void exitGtExpr(GtExprContext ctx) {
		double rightValue = numStack.pop();
		double leftValue = numStack.pop();
		booleanStack.push(leftValue > rightValue);
	}

	@Override
	public void exitGtEqExpr(GtEqExprContext ctx) {
		double rightValue = numStack.pop();
		double leftValue = numStack.pop();
		booleanStack.push(leftValue >= rightValue);
	}

	@Override
	public void exitLeEqExpr(LeEqExprContext ctx) {
		double rightValue = numStack.pop();
		double leftValue = numStack.pop();
		booleanStack.push(leftValue <= rightValue);
	}

	@Override
	public void exitLeExpr(LeExprContext ctx) {
		double rightValue = numStack.pop();
		double leftValue = numStack.pop();
		booleanStack.push(leftValue < rightValue);
	}

	@Override
	public void exitAndExpr(AndExprContext ctx) {
		boolean rightValue = booleanStack.pop();
		boolean leftValue = booleanStack.pop();
		booleanStack.push(leftValue && rightValue);
	}

	@Override
	public void exitOrExpr(OrExprContext ctx) {
		boolean rightValue = booleanStack.pop();
		boolean leftValue = booleanStack.pop();
		booleanStack.push(leftValue || rightValue);
	}

	@Override
	public void exitMulDiv(MulDivContext ctx) {
		if (ctx.op.getType() == FilterParser.MUL) {
			numStack.push(parseNum(ctx.expr(0).getText()) * parseNum(ctx.expr(1).getText()));
		} else {
			numStack.push(parseNum(ctx.expr(1).getText()) != 0
					? parseNum(ctx.expr(0).getText()) / parseNum(ctx.expr(1).getText()) : 0);
		}
	}

	@Override
	public void exitAddSub(AddSubContext ctx) {
		if (ctx.op.getType() == FilterParser.ADD) {
			numStack.push(Double.parseDouble(ctx.expr(0).getText()) + Double.parseDouble(ctx.expr(1).getText()));
		} else {
			numStack.push(parseNum(ctx.expr(0).getText()) - parseNum(ctx.expr(1).getText()));
		}
	}

	@Override
	public void exitId(IdContext ctx) {
		String expr = ctx.getText();
		String[] exprStrs = expr.split("\\.");
		int index = Integer.parseInt(exprStrs[0]);
		String period = exprStrs[1];
		String attr = exprStrs[2];
		switch (period) {
		case "30":
			numStack.push(CandleEntryFuntion.calc(m30CandleList.get(m30CandleList.size() - index), attr));
			break;
		case "d":
			numStack.push(CandleEntryFuntion.calc(dayCandleList.get(dayCandleList.size() - index), attr));
			break;
		case "w":
			numStack.push(CandleEntryFuntion.calc(weekCandleList.get(weekCandleList.size() - index), attr));
			break;
		case "m":
			numStack.push(CandleEntryFuntion.calc(monthCandleList.get(monthCandleList.size() - index), attr));
			break;
		}
	}

	@Override
	public void exitInt(IntContext ctx) {
		numStack.push(parseNum(ctx.getText()));
	}

	@Override
	public void exitFloat(FloatContext ctx) {
		numStack.push(parseNum(ctx.getText()));
	}

	private Double parseNum(String num) {
		return Double.parseDouble(num);
	}
}
