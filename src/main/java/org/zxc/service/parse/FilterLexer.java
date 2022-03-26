// Generated from Filter.g by ANTLR 4.9.3
package org.zxc.service.parse;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FilterLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		MUL=10, DIV=11, ADD=12, SUB=13, ID=14, INT=15, FLOAT=16, WS=17;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"MUL", "DIV", "ADD", "SUB", "ID", "INT", "FLOAT", "DIGIT", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'=='", "'>='", "'<='", "'>'", "'<'", "'&&'", "'||'", 
			"'*'", "'/'", "'+'", "'-'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, "MUL", "DIV", 
			"ADD", "SUB", "ID", "INT", "FLOAT", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public FilterLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Filter.g"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\23y\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\3\2\3\2\3\3\3\3\3\4\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7"+
		"\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3"+
		"\17\6\17H\n\17\r\17\16\17I\3\17\3\17\3\17\3\17\5\17P\n\17\3\17\3\17\6"+
		"\17T\n\17\r\17\16\17U\3\20\6\20Y\n\20\r\20\16\20Z\3\21\6\21^\n\21\r\21"+
		"\16\21_\3\21\3\21\7\21d\n\21\f\21\16\21g\13\21\3\21\3\21\6\21k\n\21\r"+
		"\21\16\21l\5\21o\n\21\3\22\3\22\3\23\6\23t\n\23\r\23\16\23u\3\23\3\23"+
		"\2\2\24\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17"+
		"\35\20\37\21!\22#\2%\23\3\2\6\5\2ffooyy\5\2\62;C\\c|\3\2\62;\5\2\13\f"+
		"\17\17\"\"\2\u0080\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13"+
		"\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2"+
		"\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2"+
		"!\3\2\2\2\2%\3\2\2\2\3\'\3\2\2\2\5)\3\2\2\2\7+\3\2\2\2\t.\3\2\2\2\13\61"+
		"\3\2\2\2\r\64\3\2\2\2\17\66\3\2\2\2\218\3\2\2\2\23;\3\2\2\2\25>\3\2\2"+
		"\2\27@\3\2\2\2\31B\3\2\2\2\33D\3\2\2\2\35G\3\2\2\2\37X\3\2\2\2!n\3\2\2"+
		"\2#p\3\2\2\2%s\3\2\2\2\'(\7*\2\2(\4\3\2\2\2)*\7+\2\2*\6\3\2\2\2+,\7?\2"+
		"\2,-\7?\2\2-\b\3\2\2\2./\7@\2\2/\60\7?\2\2\60\n\3\2\2\2\61\62\7>\2\2\62"+
		"\63\7?\2\2\63\f\3\2\2\2\64\65\7@\2\2\65\16\3\2\2\2\66\67\7>\2\2\67\20"+
		"\3\2\2\289\7(\2\29:\7(\2\2:\22\3\2\2\2;<\7~\2\2<=\7~\2\2=\24\3\2\2\2>"+
		"?\7,\2\2?\26\3\2\2\2@A\7\61\2\2A\30\3\2\2\2BC\7-\2\2C\32\3\2\2\2DE\7/"+
		"\2\2E\34\3\2\2\2FH\5#\22\2GF\3\2\2\2HI\3\2\2\2IG\3\2\2\2IJ\3\2\2\2JK\3"+
		"\2\2\2KO\7\60\2\2LM\7\65\2\2MP\7\62\2\2NP\t\2\2\2OL\3\2\2\2ON\3\2\2\2"+
		"PQ\3\2\2\2QS\7\60\2\2RT\t\3\2\2SR\3\2\2\2TU\3\2\2\2US\3\2\2\2UV\3\2\2"+
		"\2V\36\3\2\2\2WY\5#\22\2XW\3\2\2\2YZ\3\2\2\2ZX\3\2\2\2Z[\3\2\2\2[ \3\2"+
		"\2\2\\^\5#\22\2]\\\3\2\2\2^_\3\2\2\2_]\3\2\2\2_`\3\2\2\2`a\3\2\2\2ae\7"+
		"\60\2\2bd\5#\22\2cb\3\2\2\2dg\3\2\2\2ec\3\2\2\2ef\3\2\2\2fo\3\2\2\2ge"+
		"\3\2\2\2hj\7\60\2\2ik\5#\22\2ji\3\2\2\2kl\3\2\2\2lj\3\2\2\2lm\3\2\2\2"+
		"mo\3\2\2\2n]\3\2\2\2nh\3\2\2\2o\"\3\2\2\2pq\t\4\2\2q$\3\2\2\2rt\t\5\2"+
		"\2sr\3\2\2\2tu\3\2\2\2us\3\2\2\2uv\3\2\2\2vw\3\2\2\2wx\b\23\2\2x&\3\2"+
		"\2\2\f\2IOUZ_elnu\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}