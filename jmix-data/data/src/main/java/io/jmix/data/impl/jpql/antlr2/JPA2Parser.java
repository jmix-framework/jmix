// $ANTLR 3.5.2 JPA2.g 2025-09-04 11:54:16

package io.jmix.data.impl.jpql.antlr2;

import io.jmix.data.impl.jpql.tree.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.RewriteRuleSubtreeStream;
import org.antlr.runtime.tree.RewriteRuleTokenStream;
import org.antlr.runtime.tree.TreeAdaptor;


@SuppressWarnings("all")
public class JPA2Parser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "AS", "ASC", "AVG", "BY", 
		"CASE", "COMMENT", "COUNT", "DESC", "DISTINCT", "ELSE", "END", "ESCAPE_CHARACTER", 
		"FETCH", "GROUP", "HAVING", "IN", "INNER", "INT_NUMERAL", "JOIN", "LEFT", 
		"LINE_COMMENT", "LOWER", "LPAREN", "MAX", "MIN", "NAMED_PARAMETER", "NOT", 
		"OR", "ORDER", "OUTER", "RPAREN", "RUSSIAN_SYMBOLS", "SET", "STRING_LITERAL", 
		"SUM", "THEN", "TRIM_CHARACTER", "T_AGGREGATE_EXPR", "T_COLLECTION_MEMBER", 
		"T_CONDITION", "T_ENUM_MACROS", "T_GROUP_BY", "T_ID_VAR", "T_JOIN_VAR", 
		"T_ORDER_BY", "T_ORDER_BY_FIELD", "T_PARAMETER", "T_QUERY", "T_SELECTED_ENTITY", 
		"T_SELECTED_FIELD", "T_SELECTED_ITEM", "T_SELECTED_ITEMS", "T_SIMPLE_CONDITION", 
		"T_SOURCE", "T_SOURCES", "WHEN", "WORD", "WS", "'${'", "'*'", "'+'", "','", 
		"'-'", "'.'", "'/'", "'0x'", "'<'", "'<='", "'<>'", "'='", "'>'", "'>='", 
		"'?'", "'@BETWEEN'", "'@DATEAFTER'", "'@DATEBEFORE'", "'@DATEBETWEEN'", 
		"'@DATEEQUALS'", "'@ENUM'", "'@TODAY'", "'ABS('", "'ALL'", "'ANY'", "'BETWEEN'", 
		"'BOTH'", "'CAST('", "'COALESCE('", "'CONCAT('", "'CURRENT_DATE'", "'CURRENT_TIME'", 
		"'CURRENT_TIMESTAMP'", "'DAY'", "'DELETE'", "'EMPTY'", "'ENTRY('", "'EPOCH'", 
		"'ESCAPE'", "'EXISTS'", "'EXTRACT('", "'FROM'", "'FUNCTION('", "'HOUR'", 
		"'INDEX('", "'IS'", "'KEY('", "'LEADING'", "'LENGTH('", "'LIKE'", "'LOCATE('", 
		"'MEMBER'", "'MINUTE'", "'MOD('", "'MONTH'", "'NEW'", "'NOW'", "'NULL'", 
		"'NULLIF('", "'NULLS FIRST'", "'NULLS LAST'", "'OBJECT'", "'OF'", "'ON'", 
		"'QUARTER'", "'REGEXP'", "'SECOND'", "'SELECT'", "'SIZE('", "'SOME'", 
		"'SQRT('", "'SUBSTRING('", "'TRAILING'", "'TREAT('", "'TRIM('", "'TYPE('", 
		"'UPDATE'", "'UPPER('", "'USER_TIMEZONE'", "'VALUE('", "'WEEK'", "'WHERE'", 
		"'YEAR'", "'false'", "'true'", "'}'"
	};
	public static final int EOF=-1;
	public static final int T__63=63;
	public static final int T__64=64;
	public static final int T__65=65;
	public static final int T__66=66;
	public static final int T__67=67;
	public static final int T__68=68;
	public static final int T__69=69;
	public static final int T__70=70;
	public static final int T__71=71;
	public static final int T__72=72;
	public static final int T__73=73;
	public static final int T__74=74;
	public static final int T__75=75;
	public static final int T__76=76;
	public static final int T__77=77;
	public static final int T__78=78;
	public static final int T__79=79;
	public static final int T__80=80;
	public static final int T__81=81;
	public static final int T__82=82;
	public static final int T__83=83;
	public static final int T__84=84;
	public static final int T__85=85;
	public static final int T__86=86;
	public static final int T__87=87;
	public static final int T__88=88;
	public static final int T__89=89;
	public static final int T__90=90;
	public static final int T__91=91;
	public static final int T__92=92;
	public static final int T__93=93;
	public static final int T__94=94;
	public static final int T__95=95;
	public static final int T__96=96;
	public static final int T__97=97;
	public static final int T__98=98;
	public static final int T__99=99;
	public static final int T__100=100;
	public static final int T__101=101;
	public static final int T__102=102;
	public static final int T__103=103;
	public static final int T__104=104;
	public static final int T__105=105;
	public static final int T__106=106;
	public static final int T__107=107;
	public static final int T__108=108;
	public static final int T__109=109;
	public static final int T__110=110;
	public static final int T__111=111;
	public static final int T__112=112;
	public static final int T__113=113;
	public static final int T__114=114;
	public static final int T__115=115;
	public static final int T__116=116;
	public static final int T__117=117;
	public static final int T__118=118;
	public static final int T__119=119;
	public static final int T__120=120;
	public static final int T__121=121;
	public static final int T__122=122;
	public static final int T__123=123;
	public static final int T__124=124;
	public static final int T__125=125;
	public static final int T__126=126;
	public static final int T__127=127;
	public static final int T__128=128;
	public static final int T__129=129;
	public static final int T__130=130;
	public static final int T__131=131;
	public static final int T__132=132;
	public static final int T__133=133;
	public static final int T__134=134;
	public static final int T__135=135;
	public static final int T__136=136;
	public static final int T__137=137;
	public static final int T__138=138;
	public static final int T__139=139;
	public static final int T__140=140;
	public static final int T__141=141;
	public static final int T__142=142;
	public static final int T__143=143;
	public static final int T__144=144;
	public static final int T__145=145;
	public static final int T__146=146;
	public static final int T__147=147;
	public static final int T__148=148;
	public static final int AND=4;
	public static final int AS=5;
	public static final int ASC=6;
	public static final int AVG=7;
	public static final int BY=8;
	public static final int CASE=9;
	public static final int COMMENT=10;
	public static final int COUNT=11;
	public static final int DESC=12;
	public static final int DISTINCT=13;
	public static final int ELSE=14;
	public static final int END=15;
	public static final int ESCAPE_CHARACTER=16;
	public static final int FETCH=17;
	public static final int GROUP=18;
	public static final int HAVING=19;
	public static final int IN=20;
	public static final int INNER=21;
	public static final int INT_NUMERAL=22;
	public static final int JOIN=23;
	public static final int LEFT=24;
	public static final int LINE_COMMENT=25;
	public static final int LOWER=26;
	public static final int LPAREN=27;
	public static final int MAX=28;
	public static final int MIN=29;
	public static final int NAMED_PARAMETER=30;
	public static final int NOT=31;
	public static final int OR=32;
	public static final int ORDER=33;
	public static final int OUTER=34;
	public static final int RPAREN=35;
	public static final int RUSSIAN_SYMBOLS=36;
	public static final int SET=37;
	public static final int STRING_LITERAL=38;
	public static final int SUM=39;
	public static final int THEN=40;
	public static final int TRIM_CHARACTER=41;
	public static final int T_AGGREGATE_EXPR=42;
	public static final int T_COLLECTION_MEMBER=43;
	public static final int T_CONDITION=44;
	public static final int T_ENUM_MACROS=45;
	public static final int T_GROUP_BY=46;
	public static final int T_ID_VAR=47;
	public static final int T_JOIN_VAR=48;
	public static final int T_ORDER_BY=49;
	public static final int T_ORDER_BY_FIELD=50;
	public static final int T_PARAMETER=51;
	public static final int T_QUERY=52;
	public static final int T_SELECTED_ENTITY=53;
	public static final int T_SELECTED_FIELD=54;
	public static final int T_SELECTED_ITEM=55;
	public static final int T_SELECTED_ITEMS=56;
	public static final int T_SIMPLE_CONDITION=57;
	public static final int T_SOURCE=58;
	public static final int T_SOURCES=59;
	public static final int WHEN=60;
	public static final int WORD=61;
	public static final int WS=62;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public JPA2Parser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public JPA2Parser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return JPA2Parser.tokenNames; }
	@Override public String getGrammarFileName() { return "JPA2.g"; }


	public static class ql_statement_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ql_statement"
	// JPA2.g:88:1: ql_statement : ( select_statement | update_statement | delete_statement );
	public final JPA2Parser.ql_statement_return ql_statement() throws RecognitionException {
		JPA2Parser.ql_statement_return retval = new JPA2Parser.ql_statement_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope select_statement1 =null;
		ParserRuleReturnScope update_statement2 =null;
		ParserRuleReturnScope delete_statement3 =null;


		try {
			// JPA2.g:89:5: ( select_statement | update_statement | delete_statement )
			int alt1=3;
			switch ( input.LA(1) ) {
			case 130:
				{
				alt1=1;
				}
				break;
			case 139:
				{
				alt1=2;
				}
				break;
			case 97:
				{
				alt1=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 1, 0, input);
				throw nvae;
			}
			switch (alt1) {
				case 1 :
					// JPA2.g:89:7: select_statement
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_select_statement_in_ql_statement511);
					select_statement1=select_statement();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, select_statement1.getTree());

					}
					break;
				case 2 :
					// JPA2.g:89:26: update_statement
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_update_statement_in_ql_statement515);
					update_statement2=update_statement();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, update_statement2.getTree());

					}
					break;
				case 3 :
					// JPA2.g:89:45: delete_statement
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_delete_statement_in_ql_statement519);
					delete_statement3=delete_statement();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, delete_statement3.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ql_statement"


	public static class select_statement_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "select_statement"
	// JPA2.g:91:1: select_statement : sl= 'SELECT' select_clause from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? ( orderby_clause )? EOF -> ^( T_QUERY[$sl] ( select_clause )? from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? ( orderby_clause )? ) ;
	public final JPA2Parser.select_statement_return select_statement() throws RecognitionException {
		JPA2Parser.select_statement_return retval = new JPA2Parser.select_statement_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token sl=null;
		Token EOF10=null;
		ParserRuleReturnScope select_clause4 =null;
		ParserRuleReturnScope from_clause5 =null;
		ParserRuleReturnScope where_clause6 =null;
		ParserRuleReturnScope groupby_clause7 =null;
		ParserRuleReturnScope having_clause8 =null;
		ParserRuleReturnScope orderby_clause9 =null;

		Object sl_tree=null;
		Object EOF10_tree=null;
		RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
		RewriteRuleTokenStream stream_130=new RewriteRuleTokenStream(adaptor,"token 130");
		RewriteRuleSubtreeStream stream_where_clause=new RewriteRuleSubtreeStream(adaptor,"rule where_clause");
		RewriteRuleSubtreeStream stream_select_clause=new RewriteRuleSubtreeStream(adaptor,"rule select_clause");
		RewriteRuleSubtreeStream stream_from_clause=new RewriteRuleSubtreeStream(adaptor,"rule from_clause");
		RewriteRuleSubtreeStream stream_groupby_clause=new RewriteRuleSubtreeStream(adaptor,"rule groupby_clause");
		RewriteRuleSubtreeStream stream_having_clause=new RewriteRuleSubtreeStream(adaptor,"rule having_clause");
		RewriteRuleSubtreeStream stream_orderby_clause=new RewriteRuleSubtreeStream(adaptor,"rule orderby_clause");

		try {
			// JPA2.g:92:6: (sl= 'SELECT' select_clause from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? ( orderby_clause )? EOF -> ^( T_QUERY[$sl] ( select_clause )? from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? ( orderby_clause )? ) )
			// JPA2.g:92:8: sl= 'SELECT' select_clause from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? ( orderby_clause )? EOF
			{
			sl=(Token)match(input,130,FOLLOW_130_in_select_statement534); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_130.add(sl);

			pushFollow(FOLLOW_select_clause_in_select_statement536);
			select_clause4=select_clause();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_select_clause.add(select_clause4.getTree());
			pushFollow(FOLLOW_from_clause_in_select_statement538);
			from_clause5=from_clause();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_from_clause.add(from_clause5.getTree());
			// JPA2.g:92:46: ( where_clause )?
			int alt2=2;
			int LA2_0 = input.LA(1);
			if ( (LA2_0==144) ) {
				alt2=1;
			}
			switch (alt2) {
				case 1 :
					// JPA2.g:92:47: where_clause
					{
					pushFollow(FOLLOW_where_clause_in_select_statement541);
					where_clause6=where_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_where_clause.add(where_clause6.getTree());
					}
					break;

			}

			// JPA2.g:92:62: ( groupby_clause )?
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==GROUP) ) {
				alt3=1;
			}
			switch (alt3) {
				case 1 :
					// JPA2.g:92:63: groupby_clause
					{
					pushFollow(FOLLOW_groupby_clause_in_select_statement546);
					groupby_clause7=groupby_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_groupby_clause.add(groupby_clause7.getTree());
					}
					break;

			}

			// JPA2.g:92:80: ( having_clause )?
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0==HAVING) ) {
				alt4=1;
			}
			switch (alt4) {
				case 1 :
					// JPA2.g:92:81: having_clause
					{
					pushFollow(FOLLOW_having_clause_in_select_statement551);
					having_clause8=having_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_having_clause.add(having_clause8.getTree());
					}
					break;

			}

			// JPA2.g:92:97: ( orderby_clause )?
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( (LA5_0==ORDER) ) {
				alt5=1;
			}
			switch (alt5) {
				case 1 :
					// JPA2.g:92:98: orderby_clause
					{
					pushFollow(FOLLOW_orderby_clause_in_select_statement556);
					orderby_clause9=orderby_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_orderby_clause.add(orderby_clause9.getTree());
					}
					break;

			}

			EOF10=(Token)match(input,EOF,FOLLOW_EOF_in_select_statement560); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_EOF.add(EOF10);

			// AST REWRITE
			// elements: groupby_clause, having_clause, select_clause, from_clause, where_clause, orderby_clause
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 93:6: -> ^( T_QUERY[$sl] ( select_clause )? from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? ( orderby_clause )? )
			{
				// JPA2.g:93:9: ^( T_QUERY[$sl] ( select_clause )? from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? ( orderby_clause )? )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new QueryNode(T_QUERY, sl), root_1);
				// JPA2.g:93:35: ( select_clause )?
				if ( stream_select_clause.hasNext() ) {
					adaptor.addChild(root_1, stream_select_clause.nextTree());
				}
				stream_select_clause.reset();

				adaptor.addChild(root_1, stream_from_clause.nextTree());
				// JPA2.g:93:64: ( where_clause )?
				if ( stream_where_clause.hasNext() ) {
					adaptor.addChild(root_1, stream_where_clause.nextTree());
				}
				stream_where_clause.reset();

				// JPA2.g:93:80: ( groupby_clause )?
				if ( stream_groupby_clause.hasNext() ) {
					adaptor.addChild(root_1, stream_groupby_clause.nextTree());
				}
				stream_groupby_clause.reset();

				// JPA2.g:93:98: ( having_clause )?
				if ( stream_having_clause.hasNext() ) {
					adaptor.addChild(root_1, stream_having_clause.nextTree());
				}
				stream_having_clause.reset();

				// JPA2.g:93:115: ( orderby_clause )?
				if ( stream_orderby_clause.hasNext() ) {
					adaptor.addChild(root_1, stream_orderby_clause.nextTree());
				}
				stream_orderby_clause.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "select_statement"


	public static class update_statement_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "update_statement"
	// JPA2.g:95:1: update_statement : up= 'UPDATE' update_clause ( where_clause )? -> ^( T_QUERY[$up] update_clause ( where_clause )? ) ;
	public final JPA2Parser.update_statement_return update_statement() throws RecognitionException {
		JPA2Parser.update_statement_return retval = new JPA2Parser.update_statement_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token up=null;
		ParserRuleReturnScope update_clause11 =null;
		ParserRuleReturnScope where_clause12 =null;

		Object up_tree=null;
		RewriteRuleTokenStream stream_139=new RewriteRuleTokenStream(adaptor,"token 139");
		RewriteRuleSubtreeStream stream_where_clause=new RewriteRuleSubtreeStream(adaptor,"rule where_clause");
		RewriteRuleSubtreeStream stream_update_clause=new RewriteRuleSubtreeStream(adaptor,"rule update_clause");

		try {
			// JPA2.g:96:5: (up= 'UPDATE' update_clause ( where_clause )? -> ^( T_QUERY[$up] update_clause ( where_clause )? ) )
			// JPA2.g:96:7: up= 'UPDATE' update_clause ( where_clause )?
			{
			up=(Token)match(input,139,FOLLOW_139_in_update_statement616); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_139.add(up);

			pushFollow(FOLLOW_update_clause_in_update_statement618);
			update_clause11=update_clause();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_update_clause.add(update_clause11.getTree());
			// JPA2.g:96:33: ( where_clause )?
			int alt6=2;
			int LA6_0 = input.LA(1);
			if ( (LA6_0==144) ) {
				alt6=1;
			}
			switch (alt6) {
				case 1 :
					// JPA2.g:96:34: where_clause
					{
					pushFollow(FOLLOW_where_clause_in_update_statement621);
					where_clause12=where_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_where_clause.add(where_clause12.getTree());
					}
					break;

			}

			// AST REWRITE
			// elements: update_clause, where_clause
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 97:5: -> ^( T_QUERY[$up] update_clause ( where_clause )? )
			{
				// JPA2.g:97:8: ^( T_QUERY[$up] update_clause ( where_clause )? )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new QueryNode(T_QUERY, up), root_1);
				adaptor.addChild(root_1, stream_update_clause.nextTree());
				// JPA2.g:97:48: ( where_clause )?
				if ( stream_where_clause.hasNext() ) {
					adaptor.addChild(root_1, stream_where_clause.nextTree());
				}
				stream_where_clause.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "update_statement"


	public static class delete_statement_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "delete_statement"
	// JPA2.g:98:1: delete_statement : dl= 'DELETE' delete_clause ( where_clause )? -> ^( T_QUERY[$dl] delete_clause ( where_clause )? ) ;
	public final JPA2Parser.delete_statement_return delete_statement() throws RecognitionException {
		JPA2Parser.delete_statement_return retval = new JPA2Parser.delete_statement_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token dl=null;
		ParserRuleReturnScope delete_clause13 =null;
		ParserRuleReturnScope where_clause14 =null;

		Object dl_tree=null;
		RewriteRuleTokenStream stream_97=new RewriteRuleTokenStream(adaptor,"token 97");
		RewriteRuleSubtreeStream stream_delete_clause=new RewriteRuleSubtreeStream(adaptor,"rule delete_clause");
		RewriteRuleSubtreeStream stream_where_clause=new RewriteRuleSubtreeStream(adaptor,"rule where_clause");

		try {
			// JPA2.g:99:5: (dl= 'DELETE' delete_clause ( where_clause )? -> ^( T_QUERY[$dl] delete_clause ( where_clause )? ) )
			// JPA2.g:99:7: dl= 'DELETE' delete_clause ( where_clause )?
			{
			dl=(Token)match(input,97,FOLLOW_97_in_delete_statement657); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_97.add(dl);

			pushFollow(FOLLOW_delete_clause_in_delete_statement659);
			delete_clause13=delete_clause();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_delete_clause.add(delete_clause13.getTree());
			// JPA2.g:99:33: ( where_clause )?
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0==144) ) {
				alt7=1;
			}
			switch (alt7) {
				case 1 :
					// JPA2.g:99:34: where_clause
					{
					pushFollow(FOLLOW_where_clause_in_delete_statement662);
					where_clause14=where_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_where_clause.add(where_clause14.getTree());
					}
					break;

			}

			// AST REWRITE
			// elements: delete_clause, where_clause
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 100:5: -> ^( T_QUERY[$dl] delete_clause ( where_clause )? )
			{
				// JPA2.g:100:8: ^( T_QUERY[$dl] delete_clause ( where_clause )? )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new QueryNode(T_QUERY, dl), root_1);
				adaptor.addChild(root_1, stream_delete_clause.nextTree());
				// JPA2.g:100:48: ( where_clause )?
				if ( stream_where_clause.hasNext() ) {
					adaptor.addChild(root_1, stream_where_clause.nextTree());
				}
				stream_where_clause.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "delete_statement"


	public static class from_clause_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "from_clause"
	// JPA2.g:102:1: from_clause : fr= 'FROM' identification_variable_declaration ( ',' identification_variable_declaration_or_collection_member_declaration )* -> ^( T_SOURCES[$fr] identification_variable_declaration ( identification_variable_declaration_or_collection_member_declaration )* ) ;
	public final JPA2Parser.from_clause_return from_clause() throws RecognitionException {
		JPA2Parser.from_clause_return retval = new JPA2Parser.from_clause_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token fr=null;
		Token char_literal16=null;
		ParserRuleReturnScope identification_variable_declaration15 =null;
		ParserRuleReturnScope identification_variable_declaration_or_collection_member_declaration17 =null;

		Object fr_tree=null;
		Object char_literal16_tree=null;
		RewriteRuleTokenStream stream_66=new RewriteRuleTokenStream(adaptor,"token 66");
		RewriteRuleTokenStream stream_104=new RewriteRuleTokenStream(adaptor,"token 104");
		RewriteRuleSubtreeStream stream_identification_variable_declaration=new RewriteRuleSubtreeStream(adaptor,"rule identification_variable_declaration");
		RewriteRuleSubtreeStream stream_identification_variable_declaration_or_collection_member_declaration=new RewriteRuleSubtreeStream(adaptor,"rule identification_variable_declaration_or_collection_member_declaration");

		try {
			// JPA2.g:103:6: (fr= 'FROM' identification_variable_declaration ( ',' identification_variable_declaration_or_collection_member_declaration )* -> ^( T_SOURCES[$fr] identification_variable_declaration ( identification_variable_declaration_or_collection_member_declaration )* ) )
			// JPA2.g:103:8: fr= 'FROM' identification_variable_declaration ( ',' identification_variable_declaration_or_collection_member_declaration )*
			{
			fr=(Token)match(input,104,FOLLOW_104_in_from_clause700); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_104.add(fr);

			pushFollow(FOLLOW_identification_variable_declaration_in_from_clause702);
			identification_variable_declaration15=identification_variable_declaration();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_identification_variable_declaration.add(identification_variable_declaration15.getTree());
			// JPA2.g:103:54: ( ',' identification_variable_declaration_or_collection_member_declaration )*
			loop8:
			while (true) {
				int alt8=2;
				int LA8_0 = input.LA(1);
				if ( (LA8_0==66) ) {
					alt8=1;
				}

				switch (alt8) {
				case 1 :
					// JPA2.g:103:55: ',' identification_variable_declaration_or_collection_member_declaration
					{
					char_literal16=(Token)match(input,66,FOLLOW_66_in_from_clause705); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_66.add(char_literal16);

					pushFollow(FOLLOW_identification_variable_declaration_or_collection_member_declaration_in_from_clause707);
					identification_variable_declaration_or_collection_member_declaration17=identification_variable_declaration_or_collection_member_declaration();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_identification_variable_declaration_or_collection_member_declaration.add(identification_variable_declaration_or_collection_member_declaration17.getTree());
					}
					break;

				default :
					break loop8;
				}
			}

			// AST REWRITE
			// elements: identification_variable_declaration_or_collection_member_declaration, identification_variable_declaration
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 104:6: -> ^( T_SOURCES[$fr] identification_variable_declaration ( identification_variable_declaration_or_collection_member_declaration )* )
			{
				// JPA2.g:104:9: ^( T_SOURCES[$fr] identification_variable_declaration ( identification_variable_declaration_or_collection_member_declaration )* )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new FromNode(T_SOURCES, fr), root_1);
				adaptor.addChild(root_1, stream_identification_variable_declaration.nextTree());
				// JPA2.g:104:72: ( identification_variable_declaration_or_collection_member_declaration )*
				while ( stream_identification_variable_declaration_or_collection_member_declaration.hasNext() ) {
					adaptor.addChild(root_1, stream_identification_variable_declaration_or_collection_member_declaration.nextTree());
				}
				stream_identification_variable_declaration_or_collection_member_declaration.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "from_clause"


	public static class identification_variable_declaration_or_collection_member_declaration_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "identification_variable_declaration_or_collection_member_declaration"
	// JPA2.g:105:1: identification_variable_declaration_or_collection_member_declaration : ( identification_variable_declaration | collection_member_declaration -> ^( T_SOURCE collection_member_declaration ) );
	public final JPA2Parser.identification_variable_declaration_or_collection_member_declaration_return identification_variable_declaration_or_collection_member_declaration() throws RecognitionException {
		JPA2Parser.identification_variable_declaration_or_collection_member_declaration_return retval = new JPA2Parser.identification_variable_declaration_or_collection_member_declaration_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope identification_variable_declaration18 =null;
		ParserRuleReturnScope collection_member_declaration19 =null;

		RewriteRuleSubtreeStream stream_collection_member_declaration=new RewriteRuleSubtreeStream(adaptor,"rule collection_member_declaration");

		try {
			// JPA2.g:106:6: ( identification_variable_declaration | collection_member_declaration -> ^( T_SOURCE collection_member_declaration ) )
			int alt9=2;
			int LA9_0 = input.LA(1);
			if ( (LA9_0==WORD) ) {
				alt9=1;
			}
			else if ( (LA9_0==IN) ) {
				alt9=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 9, 0, input);
				throw nvae;
			}

			switch (alt9) {
				case 1 :
					// JPA2.g:106:8: identification_variable_declaration
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_identification_variable_declaration_in_identification_variable_declaration_or_collection_member_declaration741);
					identification_variable_declaration18=identification_variable_declaration();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable_declaration18.getTree());

					}
					break;
				case 2 :
					// JPA2.g:107:8: collection_member_declaration
					{
					pushFollow(FOLLOW_collection_member_declaration_in_identification_variable_declaration_or_collection_member_declaration750);
					collection_member_declaration19=collection_member_declaration();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_collection_member_declaration.add(collection_member_declaration19.getTree());
					// AST REWRITE
					// elements: collection_member_declaration
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 107:38: -> ^( T_SOURCE collection_member_declaration )
					{
						// JPA2.g:107:41: ^( T_SOURCE collection_member_declaration )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot(new SelectionSourceNode(T_SOURCE), root_1);
						adaptor.addChild(root_1, stream_collection_member_declaration.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "identification_variable_declaration_or_collection_member_declaration"


	public static class identification_variable_declaration_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "identification_variable_declaration"
	// JPA2.g:109:1: identification_variable_declaration : range_variable_declaration ( joined_clause )* -> ^( T_SOURCE range_variable_declaration ( joined_clause )* ) ;
	public final JPA2Parser.identification_variable_declaration_return identification_variable_declaration() throws RecognitionException {
		JPA2Parser.identification_variable_declaration_return retval = new JPA2Parser.identification_variable_declaration_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope range_variable_declaration20 =null;
		ParserRuleReturnScope joined_clause21 =null;

		RewriteRuleSubtreeStream stream_range_variable_declaration=new RewriteRuleSubtreeStream(adaptor,"rule range_variable_declaration");
		RewriteRuleSubtreeStream stream_joined_clause=new RewriteRuleSubtreeStream(adaptor,"rule joined_clause");

		try {
			// JPA2.g:110:6: ( range_variable_declaration ( joined_clause )* -> ^( T_SOURCE range_variable_declaration ( joined_clause )* ) )
			// JPA2.g:110:8: range_variable_declaration ( joined_clause )*
			{
			pushFollow(FOLLOW_range_variable_declaration_in_identification_variable_declaration774);
			range_variable_declaration20=range_variable_declaration();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_range_variable_declaration.add(range_variable_declaration20.getTree());
			// JPA2.g:110:35: ( joined_clause )*
			loop10:
			while (true) {
				int alt10=2;
				int LA10_0 = input.LA(1);
				if ( (LA10_0==INNER||(LA10_0 >= JOIN && LA10_0 <= LEFT)) ) {
					alt10=1;
				}

				switch (alt10) {
				case 1 :
					// JPA2.g:110:35: joined_clause
					{
					pushFollow(FOLLOW_joined_clause_in_identification_variable_declaration776);
					joined_clause21=joined_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_joined_clause.add(joined_clause21.getTree());
					}
					break;

				default :
					break loop10;
				}
			}

			// AST REWRITE
			// elements: range_variable_declaration, joined_clause
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 111:6: -> ^( T_SOURCE range_variable_declaration ( joined_clause )* )
			{
				// JPA2.g:111:9: ^( T_SOURCE range_variable_declaration ( joined_clause )* )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new SelectionSourceNode(T_SOURCE), root_1);
				adaptor.addChild(root_1, stream_range_variable_declaration.nextTree());
				// JPA2.g:111:68: ( joined_clause )*
				while ( stream_joined_clause.hasNext() ) {
					adaptor.addChild(root_1, stream_joined_clause.nextTree());
				}
				stream_joined_clause.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "identification_variable_declaration"


	public static class join_section_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "join_section"
	// JPA2.g:112:1: join_section : ( joined_clause )* ;
	public final JPA2Parser.join_section_return join_section() throws RecognitionException {
		JPA2Parser.join_section_return retval = new JPA2Parser.join_section_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope joined_clause22 =null;


		try {
			// JPA2.g:112:14: ( ( joined_clause )* )
			// JPA2.g:113:5: ( joined_clause )*
			{
			root_0 = (Object)adaptor.nil();


			// JPA2.g:113:5: ( joined_clause )*
			loop11:
			while (true) {
				int alt11=2;
				int LA11_0 = input.LA(1);
				if ( (LA11_0==INNER||(LA11_0 >= JOIN && LA11_0 <= LEFT)) ) {
					alt11=1;
				}

				switch (alt11) {
				case 1 :
					// JPA2.g:113:5: joined_clause
					{
					pushFollow(FOLLOW_joined_clause_in_join_section807);
					joined_clause22=joined_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, joined_clause22.getTree());

					}
					break;

				default :
					break loop11;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "join_section"


	public static class joined_clause_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "joined_clause"
	// JPA2.g:114:1: joined_clause : ( join | fetch_join );
	public final JPA2Parser.joined_clause_return joined_clause() throws RecognitionException {
		JPA2Parser.joined_clause_return retval = new JPA2Parser.joined_clause_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope join23 =null;
		ParserRuleReturnScope fetch_join24 =null;


		try {
			// JPA2.g:114:15: ( join | fetch_join )
			int alt12=2;
			switch ( input.LA(1) ) {
			case LEFT:
				{
				int LA12_1 = input.LA(2);
				if ( (LA12_1==OUTER) ) {
					int LA12_4 = input.LA(3);
					if ( (LA12_4==JOIN) ) {
						int LA12_3 = input.LA(4);
						if ( (LA12_3==GROUP||LA12_3==WORD||LA12_3==136) ) {
							alt12=1;
						}
						else if ( (LA12_3==FETCH) ) {
							alt12=2;
						}

						else {
							if (state.backtracking>0) {state.failed=true; return retval;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 12, 3, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 12, 4, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA12_1==JOIN) ) {
					int LA12_3 = input.LA(3);
					if ( (LA12_3==GROUP||LA12_3==WORD||LA12_3==136) ) {
						alt12=1;
					}
					else if ( (LA12_3==FETCH) ) {
						alt12=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 12, 3, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 12, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case INNER:
				{
				int LA12_2 = input.LA(2);
				if ( (LA12_2==JOIN) ) {
					int LA12_3 = input.LA(3);
					if ( (LA12_3==GROUP||LA12_3==WORD||LA12_3==136) ) {
						alt12=1;
					}
					else if ( (LA12_3==FETCH) ) {
						alt12=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 12, 3, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 12, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case JOIN:
				{
				int LA12_3 = input.LA(2);
				if ( (LA12_3==GROUP||LA12_3==WORD||LA12_3==136) ) {
					alt12=1;
				}
				else if ( (LA12_3==FETCH) ) {
					alt12=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 12, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 12, 0, input);
				throw nvae;
			}
			switch (alt12) {
				case 1 :
					// JPA2.g:114:17: join
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_join_in_joined_clause815);
					join23=join();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, join23.getTree());

					}
					break;
				case 2 :
					// JPA2.g:114:24: fetch_join
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_fetch_join_in_joined_clause819);
					fetch_join24=fetch_join();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, fetch_join24.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "joined_clause"


	public static class range_variable_declaration_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "range_variable_declaration"
	// JPA2.g:115:1: range_variable_declaration : entity_name ( AS )? identification_variable -> ^( T_ID_VAR[$identification_variable.text] entity_name ) ;
	public final JPA2Parser.range_variable_declaration_return range_variable_declaration() throws RecognitionException {
		JPA2Parser.range_variable_declaration_return retval = new JPA2Parser.range_variable_declaration_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token AS26=null;
		ParserRuleReturnScope entity_name25 =null;
		ParserRuleReturnScope identification_variable27 =null;

		Object AS26_tree=null;
		RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
		RewriteRuleSubtreeStream stream_entity_name=new RewriteRuleSubtreeStream(adaptor,"rule entity_name");
		RewriteRuleSubtreeStream stream_identification_variable=new RewriteRuleSubtreeStream(adaptor,"rule identification_variable");

		try {
			// JPA2.g:116:6: ( entity_name ( AS )? identification_variable -> ^( T_ID_VAR[$identification_variable.text] entity_name ) )
			// JPA2.g:116:8: entity_name ( AS )? identification_variable
			{
			pushFollow(FOLLOW_entity_name_in_range_variable_declaration831);
			entity_name25=entity_name();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_entity_name.add(entity_name25.getTree());
			// JPA2.g:116:20: ( AS )?
			int alt13=2;
			int LA13_0 = input.LA(1);
			if ( (LA13_0==AS) ) {
				alt13=1;
			}
			switch (alt13) {
				case 1 :
					// JPA2.g:116:21: AS
					{
					AS26=(Token)match(input,AS,FOLLOW_AS_in_range_variable_declaration834); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_AS.add(AS26);

					}
					break;

			}

			pushFollow(FOLLOW_identification_variable_in_range_variable_declaration838);
			identification_variable27=identification_variable();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_identification_variable.add(identification_variable27.getTree());
			// AST REWRITE
			// elements: entity_name
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 117:6: -> ^( T_ID_VAR[$identification_variable.text] entity_name )
			{
				// JPA2.g:117:9: ^( T_ID_VAR[$identification_variable.text] entity_name )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new IdentificationVariableNode(T_ID_VAR, (identification_variable27!=null?input.toString(identification_variable27.start,identification_variable27.stop):null)), root_1);
				adaptor.addChild(root_1, stream_entity_name.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "range_variable_declaration"


	public static class join_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "join"
	// JPA2.g:118:1: join : join_spec join_association_path_expression ( AS )? identification_variable ( 'ON' conditional_expression )? -> ^( T_JOIN_VAR[$join_spec.text, $identification_variable.text] join_association_path_expression ( conditional_expression )? ) ;
	public final JPA2Parser.join_return join() throws RecognitionException {
		JPA2Parser.join_return retval = new JPA2Parser.join_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token AS30=null;
		Token string_literal32=null;
		ParserRuleReturnScope join_spec28 =null;
		ParserRuleReturnScope join_association_path_expression29 =null;
		ParserRuleReturnScope identification_variable31 =null;
		ParserRuleReturnScope conditional_expression33 =null;

		Object AS30_tree=null;
		Object string_literal32_tree=null;
		RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
		RewriteRuleTokenStream stream_126=new RewriteRuleTokenStream(adaptor,"token 126");
		RewriteRuleSubtreeStream stream_conditional_expression=new RewriteRuleSubtreeStream(adaptor,"rule conditional_expression");
		RewriteRuleSubtreeStream stream_join_association_path_expression=new RewriteRuleSubtreeStream(adaptor,"rule join_association_path_expression");
		RewriteRuleSubtreeStream stream_identification_variable=new RewriteRuleSubtreeStream(adaptor,"rule identification_variable");
		RewriteRuleSubtreeStream stream_join_spec=new RewriteRuleSubtreeStream(adaptor,"rule join_spec");

		try {
			// JPA2.g:119:6: ( join_spec join_association_path_expression ( AS )? identification_variable ( 'ON' conditional_expression )? -> ^( T_JOIN_VAR[$join_spec.text, $identification_variable.text] join_association_path_expression ( conditional_expression )? ) )
			// JPA2.g:119:8: join_spec join_association_path_expression ( AS )? identification_variable ( 'ON' conditional_expression )?
			{
			pushFollow(FOLLOW_join_spec_in_join867);
			join_spec28=join_spec();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_join_spec.add(join_spec28.getTree());
			pushFollow(FOLLOW_join_association_path_expression_in_join869);
			join_association_path_expression29=join_association_path_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_join_association_path_expression.add(join_association_path_expression29.getTree());
			// JPA2.g:119:51: ( AS )?
			int alt14=2;
			int LA14_0 = input.LA(1);
			if ( (LA14_0==AS) ) {
				alt14=1;
			}
			switch (alt14) {
				case 1 :
					// JPA2.g:119:52: AS
					{
					AS30=(Token)match(input,AS,FOLLOW_AS_in_join872); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_AS.add(AS30);

					}
					break;

			}

			pushFollow(FOLLOW_identification_variable_in_join876);
			identification_variable31=identification_variable();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_identification_variable.add(identification_variable31.getTree());
			// JPA2.g:119:81: ( 'ON' conditional_expression )?
			int alt15=2;
			int LA15_0 = input.LA(1);
			if ( (LA15_0==126) ) {
				alt15=1;
			}
			switch (alt15) {
				case 1 :
					// JPA2.g:119:82: 'ON' conditional_expression
					{
					string_literal32=(Token)match(input,126,FOLLOW_126_in_join879); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_126.add(string_literal32);

					pushFollow(FOLLOW_conditional_expression_in_join881);
					conditional_expression33=conditional_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_conditional_expression.add(conditional_expression33.getTree());
					}
					break;

			}

			// AST REWRITE
			// elements: join_association_path_expression, conditional_expression
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 120:6: -> ^( T_JOIN_VAR[$join_spec.text, $identification_variable.text] join_association_path_expression ( conditional_expression )? )
			{
				// JPA2.g:120:9: ^( T_JOIN_VAR[$join_spec.text, $identification_variable.text] join_association_path_expression ( conditional_expression )? )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new JoinVariableNode(T_JOIN_VAR, (join_spec28!=null?input.toString(join_spec28.start,join_spec28.stop):null), (identification_variable31!=null?input.toString(identification_variable31.start,identification_variable31.stop):null)), root_1);
				adaptor.addChild(root_1, stream_join_association_path_expression.nextTree());
				// JPA2.g:120:121: ( conditional_expression )?
				if ( stream_conditional_expression.hasNext() ) {
					adaptor.addChild(root_1, stream_conditional_expression.nextTree());
				}
				stream_conditional_expression.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "join"


	public static class fetch_join_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "fetch_join"
	// JPA2.g:121:1: fetch_join : join_spec 'FETCH' join_association_path_expression ;
	public final JPA2Parser.fetch_join_return fetch_join() throws RecognitionException {
		JPA2Parser.fetch_join_return retval = new JPA2Parser.fetch_join_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal35=null;
		ParserRuleReturnScope join_spec34 =null;
		ParserRuleReturnScope join_association_path_expression36 =null;

		Object string_literal35_tree=null;

		try {
			// JPA2.g:122:6: ( join_spec 'FETCH' join_association_path_expression )
			// JPA2.g:122:8: join_spec 'FETCH' join_association_path_expression
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_join_spec_in_fetch_join915);
			join_spec34=join_spec();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, join_spec34.getTree());

			string_literal35=(Token)match(input,FETCH,FOLLOW_FETCH_in_fetch_join917); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal35_tree = (Object)adaptor.create(string_literal35);
			adaptor.addChild(root_0, string_literal35_tree);
			}

			pushFollow(FOLLOW_join_association_path_expression_in_fetch_join919);
			join_association_path_expression36=join_association_path_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, join_association_path_expression36.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "fetch_join"


	public static class join_spec_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "join_spec"
	// JPA2.g:123:1: join_spec : ( ( 'LEFT' ) ( 'OUTER' )? | 'INNER' )? 'JOIN' ;
	public final JPA2Parser.join_spec_return join_spec() throws RecognitionException {
		JPA2Parser.join_spec_return retval = new JPA2Parser.join_spec_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal37=null;
		Token string_literal38=null;
		Token string_literal39=null;
		Token string_literal40=null;

		Object string_literal37_tree=null;
		Object string_literal38_tree=null;
		Object string_literal39_tree=null;
		Object string_literal40_tree=null;

		try {
			// JPA2.g:124:6: ( ( ( 'LEFT' ) ( 'OUTER' )? | 'INNER' )? 'JOIN' )
			// JPA2.g:124:8: ( ( 'LEFT' ) ( 'OUTER' )? | 'INNER' )? 'JOIN'
			{
			root_0 = (Object)adaptor.nil();


			// JPA2.g:124:8: ( ( 'LEFT' ) ( 'OUTER' )? | 'INNER' )?
			int alt17=3;
			int LA17_0 = input.LA(1);
			if ( (LA17_0==LEFT) ) {
				alt17=1;
			}
			else if ( (LA17_0==INNER) ) {
				alt17=2;
			}
			switch (alt17) {
				case 1 :
					// JPA2.g:124:9: ( 'LEFT' ) ( 'OUTER' )?
					{
					// JPA2.g:124:9: ( 'LEFT' )
					// JPA2.g:124:10: 'LEFT'
					{
					string_literal37=(Token)match(input,LEFT,FOLLOW_LEFT_in_join_spec933); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal37_tree = (Object)adaptor.create(string_literal37);
					adaptor.addChild(root_0, string_literal37_tree);
					}

					}

					// JPA2.g:124:18: ( 'OUTER' )?
					int alt16=2;
					int LA16_0 = input.LA(1);
					if ( (LA16_0==OUTER) ) {
						alt16=1;
					}
					switch (alt16) {
						case 1 :
							// JPA2.g:124:19: 'OUTER'
							{
							string_literal38=(Token)match(input,OUTER,FOLLOW_OUTER_in_join_spec937); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							string_literal38_tree = (Object)adaptor.create(string_literal38);
							adaptor.addChild(root_0, string_literal38_tree);
							}

							}
							break;

					}

					}
					break;
				case 2 :
					// JPA2.g:124:31: 'INNER'
					{
					string_literal39=(Token)match(input,INNER,FOLLOW_INNER_in_join_spec943); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal39_tree = (Object)adaptor.create(string_literal39);
					adaptor.addChild(root_0, string_literal39_tree);
					}

					}
					break;

			}

			string_literal40=(Token)match(input,JOIN,FOLLOW_JOIN_in_join_spec948); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal40_tree = (Object)adaptor.create(string_literal40);
			adaptor.addChild(root_0, string_literal40_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "join_spec"


	public static class join_association_path_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "join_association_path_expression"
	// JPA2.g:127:1: join_association_path_expression : ( identification_variable '.' ( field '.' )* ( field )? -> ^( T_SELECTED_FIELD[$identification_variable.text] ( field )* ) | 'TREAT(' identification_variable '.' ( field '.' )* ( field )? AS subtype ')' -> ^( T_SELECTED_FIELD[$identification_variable.text, $subtype.text] ( field )* ) | entity_name );
	public final JPA2Parser.join_association_path_expression_return join_association_path_expression() throws RecognitionException {
		JPA2Parser.join_association_path_expression_return retval = new JPA2Parser.join_association_path_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal42=null;
		Token char_literal44=null;
		Token string_literal46=null;
		Token char_literal48=null;
		Token char_literal50=null;
		Token AS52=null;
		Token char_literal54=null;
		ParserRuleReturnScope identification_variable41 =null;
		ParserRuleReturnScope field43 =null;
		ParserRuleReturnScope field45 =null;
		ParserRuleReturnScope identification_variable47 =null;
		ParserRuleReturnScope field49 =null;
		ParserRuleReturnScope field51 =null;
		ParserRuleReturnScope subtype53 =null;
		ParserRuleReturnScope entity_name55 =null;

		Object char_literal42_tree=null;
		Object char_literal44_tree=null;
		Object string_literal46_tree=null;
		Object char_literal48_tree=null;
		Object char_literal50_tree=null;
		Object AS52_tree=null;
		Object char_literal54_tree=null;
		RewriteRuleTokenStream stream_68=new RewriteRuleTokenStream(adaptor,"token 68");
		RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
		RewriteRuleTokenStream stream_136=new RewriteRuleTokenStream(adaptor,"token 136");
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleSubtreeStream stream_field=new RewriteRuleSubtreeStream(adaptor,"rule field");
		RewriteRuleSubtreeStream stream_subtype=new RewriteRuleSubtreeStream(adaptor,"rule subtype");
		RewriteRuleSubtreeStream stream_identification_variable=new RewriteRuleSubtreeStream(adaptor,"rule identification_variable");

		try {
			// JPA2.g:128:6: ( identification_variable '.' ( field '.' )* ( field )? -> ^( T_SELECTED_FIELD[$identification_variable.text] ( field )* ) | 'TREAT(' identification_variable '.' ( field '.' )* ( field )? AS subtype ')' -> ^( T_SELECTED_FIELD[$identification_variable.text, $subtype.text] ( field )* ) | entity_name )
			int alt22=3;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA22_1 = input.LA(2);
				if ( (LA22_1==68) ) {
					alt22=1;
				}
				else if ( (LA22_1==EOF||LA22_1==AS||(LA22_1 >= GROUP && LA22_1 <= HAVING)||LA22_1==INNER||(LA22_1 >= JOIN && LA22_1 <= LEFT)||LA22_1==ORDER||LA22_1==RPAREN||LA22_1==SET||LA22_1==WORD||LA22_1==66||LA22_1==108||LA22_1==144) ) {
					alt22=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 22, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 136:
				{
				alt22=2;
				}
				break;
			case GROUP:
				{
				alt22=1;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 22, 0, input);
				throw nvae;
			}
			switch (alt22) {
				case 1 :
					// JPA2.g:128:8: identification_variable '.' ( field '.' )* ( field )?
					{
					pushFollow(FOLLOW_identification_variable_in_join_association_path_expression962);
					identification_variable41=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_identification_variable.add(identification_variable41.getTree());
					char_literal42=(Token)match(input,68,FOLLOW_68_in_join_association_path_expression964); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_68.add(char_literal42);

					// JPA2.g:128:36: ( field '.' )*
					loop18:
					while (true) {
						int alt18=2;
						switch ( input.LA(1) ) {
						case WORD:
							{
							int LA18_1 = input.LA(2);
							if ( (LA18_1==68) ) {
								alt18=1;
							}

							}
							break;
						case 130:
							{
							int LA18_2 = input.LA(2);
							if ( (LA18_2==68) ) {
								alt18=1;
							}

							}
							break;
						case 104:
							{
							int LA18_3 = input.LA(2);
							if ( (LA18_3==68) ) {
								alt18=1;
							}

							}
							break;
						case GROUP:
							{
							int LA18_4 = input.LA(2);
							if ( (LA18_4==68) ) {
								alt18=1;
							}

							}
							break;
						case ORDER:
							{
							int LA18_5 = input.LA(2);
							if ( (LA18_5==68) ) {
								alt18=1;
							}

							}
							break;
						case MAX:
							{
							int LA18_6 = input.LA(2);
							if ( (LA18_6==68) ) {
								alt18=1;
							}

							}
							break;
						case MIN:
							{
							int LA18_7 = input.LA(2);
							if ( (LA18_7==68) ) {
								alt18=1;
							}

							}
							break;
						case SUM:
							{
							int LA18_8 = input.LA(2);
							if ( (LA18_8==68) ) {
								alt18=1;
							}

							}
							break;
						case AVG:
							{
							int LA18_9 = input.LA(2);
							if ( (LA18_9==68) ) {
								alt18=1;
							}

							}
							break;
						case COUNT:
							{
							int LA18_10 = input.LA(2);
							if ( (LA18_10==68) ) {
								alt18=1;
							}

							}
							break;
						case AS:
							{
							int LA18_11 = input.LA(2);
							if ( (LA18_11==68) ) {
								alt18=1;
							}

							}
							break;
						case 114:
							{
							int LA18_12 = input.LA(2);
							if ( (LA18_12==68) ) {
								alt18=1;
							}

							}
							break;
						case CASE:
							{
							int LA18_13 = input.LA(2);
							if ( (LA18_13==68) ) {
								alt18=1;
							}

							}
							break;
						case 124:
							{
							int LA18_14 = input.LA(2);
							if ( (LA18_14==68) ) {
								alt18=1;
							}

							}
							break;
						case SET:
							{
							int LA18_15 = input.LA(2);
							if ( (LA18_15==68) ) {
								alt18=1;
							}

							}
							break;
						case DESC:
							{
							int LA18_16 = input.LA(2);
							if ( (LA18_16==68) ) {
								alt18=1;
							}

							}
							break;
						case ASC:
							{
							int LA18_17 = input.LA(2);
							if ( (LA18_17==68) ) {
								alt18=1;
							}

							}
							break;
						case 96:
						case 100:
						case 106:
						case 115:
						case 117:
						case 127:
						case 129:
						case 143:
						case 145:
							{
							int LA18_18 = input.LA(2);
							if ( (LA18_18==68) ) {
								alt18=1;
							}

							}
							break;
						}
						switch (alt18) {
						case 1 :
							// JPA2.g:128:37: field '.'
							{
							pushFollow(FOLLOW_field_in_join_association_path_expression967);
							field43=field();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_field.add(field43.getTree());
							char_literal44=(Token)match(input,68,FOLLOW_68_in_join_association_path_expression968); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_68.add(char_literal44);

							}
							break;

						default :
							break loop18;
						}
					}

					// JPA2.g:128:48: ( field )?
					int alt19=2;
					switch ( input.LA(1) ) {
						case WORD:
							{
							int LA19_1 = input.LA(2);
							if ( (synpred21_JPA2()) ) {
								alt19=1;
							}
							}
							break;
						case ASC:
						case AVG:
						case CASE:
						case COUNT:
						case DESC:
						case MAX:
						case MIN:
						case SUM:
						case 96:
						case 100:
						case 104:
						case 106:
						case 114:
						case 115:
						case 117:
						case 124:
						case 127:
						case 129:
						case 130:
						case 143:
						case 145:
							{
							alt19=1;
							}
							break;
						case GROUP:
							{
							int LA19_3 = input.LA(2);
							if ( (synpred21_JPA2()) ) {
								alt19=1;
							}
							}
							break;
						case ORDER:
							{
							int LA19_4 = input.LA(2);
							if ( (LA19_4==EOF||LA19_4==AS||(LA19_4 >= GROUP && LA19_4 <= HAVING)||LA19_4==INNER||(LA19_4 >= JOIN && LA19_4 <= LEFT)||LA19_4==ORDER||LA19_4==RPAREN||LA19_4==SET||LA19_4==WORD||LA19_4==66||LA19_4==108||LA19_4==144) ) {
								alt19=1;
							}
							}
							break;
						case AS:
							{
							int LA19_5 = input.LA(2);
							if ( (synpred21_JPA2()) ) {
								alt19=1;
							}
							}
							break;
						case SET:
							{
							switch ( input.LA(2) ) {
								case EOF:
								case AS:
								case HAVING:
								case INNER:
								case JOIN:
								case LEFT:
								case ORDER:
								case RPAREN:
								case SET:
								case 66:
								case 108:
								case 144:
									{
									alt19=1;
									}
									break;
								case GROUP:
									{
									int LA19_8 = input.LA(3);
									if ( (LA19_8==EOF||LA19_8==BY||(LA19_8 >= GROUP && LA19_8 <= HAVING)||LA19_8==INNER||(LA19_8 >= JOIN && LA19_8 <= LEFT)||LA19_8==ORDER||LA19_8==RPAREN||LA19_8==SET||LA19_8==66||LA19_8==126||LA19_8==144) ) {
										alt19=1;
									}
									}
									break;
								case WORD:
									{
									int LA19_9 = input.LA(3);
									if ( (LA19_9==EOF||(LA19_9 >= GROUP && LA19_9 <= HAVING)||LA19_9==INNER||(LA19_9 >= JOIN && LA19_9 <= LEFT)||LA19_9==ORDER||LA19_9==RPAREN||LA19_9==SET||LA19_9==66||LA19_9==126||LA19_9==144) ) {
										alt19=1;
									}
									}
									break;
							}
							}
							break;
					}
					switch (alt19) {
						case 1 :
							// JPA2.g:128:48: field
							{
							pushFollow(FOLLOW_field_in_join_association_path_expression972);
							field45=field();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_field.add(field45.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: field
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 129:10: -> ^( T_SELECTED_FIELD[$identification_variable.text] ( field )* )
					{
						// JPA2.g:129:13: ^( T_SELECTED_FIELD[$identification_variable.text] ( field )* )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot(new PathNode(T_SELECTED_FIELD, (identification_variable41!=null?input.toString(identification_variable41.start,identification_variable41.stop):null)), root_1);
						// JPA2.g:129:73: ( field )*
						while ( stream_field.hasNext() ) {
							adaptor.addChild(root_1, stream_field.nextTree());
						}
						stream_field.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// JPA2.g:130:9: 'TREAT(' identification_variable '.' ( field '.' )* ( field )? AS subtype ')'
					{
					string_literal46=(Token)match(input,136,FOLLOW_136_in_join_association_path_expression1007); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_136.add(string_literal46);

					pushFollow(FOLLOW_identification_variable_in_join_association_path_expression1009);
					identification_variable47=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_identification_variable.add(identification_variable47.getTree());
					char_literal48=(Token)match(input,68,FOLLOW_68_in_join_association_path_expression1011); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_68.add(char_literal48);

					// JPA2.g:130:46: ( field '.' )*
					loop20:
					while (true) {
						int alt20=2;
						switch ( input.LA(1) ) {
						case WORD:
							{
							int LA20_1 = input.LA(2);
							if ( (LA20_1==68) ) {
								alt20=1;
							}

							}
							break;
						case 130:
							{
							int LA20_2 = input.LA(2);
							if ( (LA20_2==68) ) {
								alt20=1;
							}

							}
							break;
						case 104:
							{
							int LA20_3 = input.LA(2);
							if ( (LA20_3==68) ) {
								alt20=1;
							}

							}
							break;
						case GROUP:
							{
							int LA20_4 = input.LA(2);
							if ( (LA20_4==68) ) {
								alt20=1;
							}

							}
							break;
						case ORDER:
							{
							int LA20_5 = input.LA(2);
							if ( (LA20_5==68) ) {
								alt20=1;
							}

							}
							break;
						case MAX:
							{
							int LA20_6 = input.LA(2);
							if ( (LA20_6==68) ) {
								alt20=1;
							}

							}
							break;
						case MIN:
							{
							int LA20_7 = input.LA(2);
							if ( (LA20_7==68) ) {
								alt20=1;
							}

							}
							break;
						case SUM:
							{
							int LA20_8 = input.LA(2);
							if ( (LA20_8==68) ) {
								alt20=1;
							}

							}
							break;
						case AVG:
							{
							int LA20_9 = input.LA(2);
							if ( (LA20_9==68) ) {
								alt20=1;
							}

							}
							break;
						case COUNT:
							{
							int LA20_10 = input.LA(2);
							if ( (LA20_10==68) ) {
								alt20=1;
							}

							}
							break;
						case AS:
							{
							int LA20_11 = input.LA(2);
							if ( (LA20_11==68) ) {
								alt20=1;
							}

							}
							break;
						case 114:
							{
							int LA20_12 = input.LA(2);
							if ( (LA20_12==68) ) {
								alt20=1;
							}

							}
							break;
						case CASE:
							{
							int LA20_13 = input.LA(2);
							if ( (LA20_13==68) ) {
								alt20=1;
							}

							}
							break;
						case 124:
							{
							int LA20_14 = input.LA(2);
							if ( (LA20_14==68) ) {
								alt20=1;
							}

							}
							break;
						case SET:
							{
							int LA20_15 = input.LA(2);
							if ( (LA20_15==68) ) {
								alt20=1;
							}

							}
							break;
						case DESC:
							{
							int LA20_16 = input.LA(2);
							if ( (LA20_16==68) ) {
								alt20=1;
							}

							}
							break;
						case ASC:
							{
							int LA20_17 = input.LA(2);
							if ( (LA20_17==68) ) {
								alt20=1;
							}

							}
							break;
						case 96:
						case 100:
						case 106:
						case 115:
						case 117:
						case 127:
						case 129:
						case 143:
						case 145:
							{
							int LA20_18 = input.LA(2);
							if ( (LA20_18==68) ) {
								alt20=1;
							}

							}
							break;
						}
						switch (alt20) {
						case 1 :
							// JPA2.g:130:47: field '.'
							{
							pushFollow(FOLLOW_field_in_join_association_path_expression1014);
							field49=field();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_field.add(field49.getTree());
							char_literal50=(Token)match(input,68,FOLLOW_68_in_join_association_path_expression1015); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_68.add(char_literal50);

							}
							break;

						default :
							break loop20;
						}
					}

					// JPA2.g:130:58: ( field )?
					int alt21=2;
					int LA21_0 = input.LA(1);
					if ( ((LA21_0 >= ASC && LA21_0 <= AVG)||LA21_0==CASE||(LA21_0 >= COUNT && LA21_0 <= DESC)||LA21_0==GROUP||(LA21_0 >= MAX && LA21_0 <= MIN)||LA21_0==ORDER||LA21_0==SET||LA21_0==SUM||LA21_0==WORD||LA21_0==96||LA21_0==100||LA21_0==104||LA21_0==106||(LA21_0 >= 114 && LA21_0 <= 115)||LA21_0==117||LA21_0==124||LA21_0==127||(LA21_0 >= 129 && LA21_0 <= 130)||LA21_0==143||LA21_0==145) ) {
						alt21=1;
					}
					else if ( (LA21_0==AS) ) {
						int LA21_2 = input.LA(2);
						if ( (LA21_2==AS) ) {
							alt21=1;
						}
					}
					switch (alt21) {
						case 1 :
							// JPA2.g:130:58: field
							{
							pushFollow(FOLLOW_field_in_join_association_path_expression1019);
							field51=field();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_field.add(field51.getTree());
							}
							break;

					}

					AS52=(Token)match(input,AS,FOLLOW_AS_in_join_association_path_expression1022); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_AS.add(AS52);

					pushFollow(FOLLOW_subtype_in_join_association_path_expression1024);
					subtype53=subtype();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_subtype.add(subtype53.getTree());
					char_literal54=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_join_association_path_expression1026); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RPAREN.add(char_literal54);

					// AST REWRITE
					// elements: field
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 131:10: -> ^( T_SELECTED_FIELD[$identification_variable.text, $subtype.text] ( field )* )
					{
						// JPA2.g:131:13: ^( T_SELECTED_FIELD[$identification_variable.text, $subtype.text] ( field )* )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot(new TreatPathNode(T_SELECTED_FIELD, (identification_variable47!=null?input.toString(identification_variable47.start,identification_variable47.stop):null), (subtype53!=null?input.toString(subtype53.start,subtype53.stop):null)), root_1);
						// JPA2.g:131:93: ( field )*
						while ( stream_field.hasNext() ) {
							adaptor.addChild(root_1, stream_field.nextTree());
						}
						stream_field.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// JPA2.g:132:8: entity_name
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_entity_name_in_join_association_path_expression1059);
					entity_name55=entity_name();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, entity_name55.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "join_association_path_expression"


	public static class collection_member_declaration_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "collection_member_declaration"
	// JPA2.g:135:1: collection_member_declaration : 'IN' '(' path_expression ')' ( AS )? identification_variable -> ^( T_COLLECTION_MEMBER[$identification_variable.text] path_expression ) ;
	public final JPA2Parser.collection_member_declaration_return collection_member_declaration() throws RecognitionException {
		JPA2Parser.collection_member_declaration_return retval = new JPA2Parser.collection_member_declaration_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal56=null;
		Token char_literal57=null;
		Token char_literal59=null;
		Token AS60=null;
		ParserRuleReturnScope path_expression58 =null;
		ParserRuleReturnScope identification_variable61 =null;

		Object string_literal56_tree=null;
		Object char_literal57_tree=null;
		Object char_literal59_tree=null;
		Object AS60_tree=null;
		RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
		RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleSubtreeStream stream_identification_variable=new RewriteRuleSubtreeStream(adaptor,"rule identification_variable");
		RewriteRuleSubtreeStream stream_path_expression=new RewriteRuleSubtreeStream(adaptor,"rule path_expression");

		try {
			// JPA2.g:136:5: ( 'IN' '(' path_expression ')' ( AS )? identification_variable -> ^( T_COLLECTION_MEMBER[$identification_variable.text] path_expression ) )
			// JPA2.g:136:7: 'IN' '(' path_expression ')' ( AS )? identification_variable
			{
			string_literal56=(Token)match(input,IN,FOLLOW_IN_in_collection_member_declaration1072); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_IN.add(string_literal56);

			char_literal57=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_collection_member_declaration1073); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LPAREN.add(char_literal57);

			pushFollow(FOLLOW_path_expression_in_collection_member_declaration1075);
			path_expression58=path_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_path_expression.add(path_expression58.getTree());
			char_literal59=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_collection_member_declaration1077); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RPAREN.add(char_literal59);

			// JPA2.g:136:35: ( AS )?
			int alt23=2;
			int LA23_0 = input.LA(1);
			if ( (LA23_0==AS) ) {
				alt23=1;
			}
			switch (alt23) {
				case 1 :
					// JPA2.g:136:36: AS
					{
					AS60=(Token)match(input,AS,FOLLOW_AS_in_collection_member_declaration1080); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_AS.add(AS60);

					}
					break;

			}

			pushFollow(FOLLOW_identification_variable_in_collection_member_declaration1084);
			identification_variable61=identification_variable();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_identification_variable.add(identification_variable61.getTree());
			// AST REWRITE
			// elements: path_expression
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 137:5: -> ^( T_COLLECTION_MEMBER[$identification_variable.text] path_expression )
			{
				// JPA2.g:137:8: ^( T_COLLECTION_MEMBER[$identification_variable.text] path_expression )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new CollectionMemberNode(T_COLLECTION_MEMBER, (identification_variable61!=null?input.toString(identification_variable61.start,identification_variable61.stop):null)), root_1);
				adaptor.addChild(root_1, stream_path_expression.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "collection_member_declaration"


	public static class qualified_identification_variable_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "qualified_identification_variable"
	// JPA2.g:139:1: qualified_identification_variable : ( map_field_identification_variable | 'ENTRY(' identification_variable ')' );
	public final JPA2Parser.qualified_identification_variable_return qualified_identification_variable() throws RecognitionException {
		JPA2Parser.qualified_identification_variable_return retval = new JPA2Parser.qualified_identification_variable_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal63=null;
		Token char_literal65=null;
		ParserRuleReturnScope map_field_identification_variable62 =null;
		ParserRuleReturnScope identification_variable64 =null;

		Object string_literal63_tree=null;
		Object char_literal65_tree=null;

		try {
			// JPA2.g:140:5: ( map_field_identification_variable | 'ENTRY(' identification_variable ')' )
			int alt24=2;
			int LA24_0 = input.LA(1);
			if ( (LA24_0==109||LA24_0==142) ) {
				alt24=1;
			}
			else if ( (LA24_0==99) ) {
				alt24=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 24, 0, input);
				throw nvae;
			}

			switch (alt24) {
				case 1 :
					// JPA2.g:140:7: map_field_identification_variable
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_map_field_identification_variable_in_qualified_identification_variable1113);
					map_field_identification_variable62=map_field_identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, map_field_identification_variable62.getTree());

					}
					break;
				case 2 :
					// JPA2.g:141:7: 'ENTRY(' identification_variable ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal63=(Token)match(input,99,FOLLOW_99_in_qualified_identification_variable1121); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal63_tree = (Object)adaptor.create(string_literal63);
					adaptor.addChild(root_0, string_literal63_tree);
					}

					pushFollow(FOLLOW_identification_variable_in_qualified_identification_variable1122);
					identification_variable64=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable64.getTree());

					char_literal65=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_qualified_identification_variable1123); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal65_tree = (Object)adaptor.create(char_literal65);
					adaptor.addChild(root_0, char_literal65_tree);
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "qualified_identification_variable"


	public static class map_field_identification_variable_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "map_field_identification_variable"
	// JPA2.g:142:1: map_field_identification_variable : ( 'KEY(' identification_variable ')' | 'VALUE(' identification_variable ')' );
	public final JPA2Parser.map_field_identification_variable_return map_field_identification_variable() throws RecognitionException {
		JPA2Parser.map_field_identification_variable_return retval = new JPA2Parser.map_field_identification_variable_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal66=null;
		Token char_literal68=null;
		Token string_literal69=null;
		Token char_literal71=null;
		ParserRuleReturnScope identification_variable67 =null;
		ParserRuleReturnScope identification_variable70 =null;

		Object string_literal66_tree=null;
		Object char_literal68_tree=null;
		Object string_literal69_tree=null;
		Object char_literal71_tree=null;

		try {
			// JPA2.g:142:35: ( 'KEY(' identification_variable ')' | 'VALUE(' identification_variable ')' )
			int alt25=2;
			int LA25_0 = input.LA(1);
			if ( (LA25_0==109) ) {
				alt25=1;
			}
			else if ( (LA25_0==142) ) {
				alt25=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 25, 0, input);
				throw nvae;
			}

			switch (alt25) {
				case 1 :
					// JPA2.g:142:37: 'KEY(' identification_variable ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal66=(Token)match(input,109,FOLLOW_109_in_map_field_identification_variable1130); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal66_tree = (Object)adaptor.create(string_literal66);
					adaptor.addChild(root_0, string_literal66_tree);
					}

					pushFollow(FOLLOW_identification_variable_in_map_field_identification_variable1131);
					identification_variable67=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable67.getTree());

					char_literal68=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_map_field_identification_variable1132); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal68_tree = (Object)adaptor.create(char_literal68);
					adaptor.addChild(root_0, char_literal68_tree);
					}

					}
					break;
				case 2 :
					// JPA2.g:142:72: 'VALUE(' identification_variable ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal69=(Token)match(input,142,FOLLOW_142_in_map_field_identification_variable1136); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal69_tree = (Object)adaptor.create(string_literal69);
					adaptor.addChild(root_0, string_literal69_tree);
					}

					pushFollow(FOLLOW_identification_variable_in_map_field_identification_variable1137);
					identification_variable70=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable70.getTree());

					char_literal71=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_map_field_identification_variable1138); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal71_tree = (Object)adaptor.create(char_literal71);
					adaptor.addChild(root_0, char_literal71_tree);
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "map_field_identification_variable"


	public static class path_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "path_expression"
	// JPA2.g:145:1: path_expression : identification_variable '.' ( field '.' )* ( field )? -> ^( T_SELECTED_FIELD[$identification_variable.text] ( field )* ) ;
	public final JPA2Parser.path_expression_return path_expression() throws RecognitionException {
		JPA2Parser.path_expression_return retval = new JPA2Parser.path_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal73=null;
		Token char_literal75=null;
		ParserRuleReturnScope identification_variable72 =null;
		ParserRuleReturnScope field74 =null;
		ParserRuleReturnScope field76 =null;

		Object char_literal73_tree=null;
		Object char_literal75_tree=null;
		RewriteRuleTokenStream stream_68=new RewriteRuleTokenStream(adaptor,"token 68");
		RewriteRuleSubtreeStream stream_field=new RewriteRuleSubtreeStream(adaptor,"rule field");
		RewriteRuleSubtreeStream stream_identification_variable=new RewriteRuleSubtreeStream(adaptor,"rule identification_variable");

		try {
			// JPA2.g:146:5: ( identification_variable '.' ( field '.' )* ( field )? -> ^( T_SELECTED_FIELD[$identification_variable.text] ( field )* ) )
			// JPA2.g:146:8: identification_variable '.' ( field '.' )* ( field )?
			{
			pushFollow(FOLLOW_identification_variable_in_path_expression1152);
			identification_variable72=identification_variable();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_identification_variable.add(identification_variable72.getTree());
			char_literal73=(Token)match(input,68,FOLLOW_68_in_path_expression1154); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_68.add(char_literal73);

			// JPA2.g:146:36: ( field '.' )*
			loop26:
			while (true) {
				int alt26=2;
				switch ( input.LA(1) ) {
				case WORD:
					{
					int LA26_1 = input.LA(2);
					if ( (LA26_1==68) ) {
						alt26=1;
					}

					}
					break;
				case 130:
					{
					int LA26_2 = input.LA(2);
					if ( (LA26_2==68) ) {
						alt26=1;
					}

					}
					break;
				case 104:
					{
					int LA26_3 = input.LA(2);
					if ( (LA26_3==68) ) {
						alt26=1;
					}

					}
					break;
				case GROUP:
					{
					int LA26_4 = input.LA(2);
					if ( (LA26_4==68) ) {
						alt26=1;
					}

					}
					break;
				case ORDER:
					{
					int LA26_5 = input.LA(2);
					if ( (LA26_5==68) ) {
						alt26=1;
					}

					}
					break;
				case MAX:
					{
					int LA26_6 = input.LA(2);
					if ( (LA26_6==68) ) {
						alt26=1;
					}

					}
					break;
				case MIN:
					{
					int LA26_7 = input.LA(2);
					if ( (LA26_7==68) ) {
						alt26=1;
					}

					}
					break;
				case SUM:
					{
					int LA26_8 = input.LA(2);
					if ( (LA26_8==68) ) {
						alt26=1;
					}

					}
					break;
				case AVG:
					{
					int LA26_9 = input.LA(2);
					if ( (LA26_9==68) ) {
						alt26=1;
					}

					}
					break;
				case COUNT:
					{
					int LA26_10 = input.LA(2);
					if ( (LA26_10==68) ) {
						alt26=1;
					}

					}
					break;
				case AS:
					{
					int LA26_11 = input.LA(2);
					if ( (LA26_11==68) ) {
						alt26=1;
					}

					}
					break;
				case 114:
					{
					int LA26_12 = input.LA(2);
					if ( (LA26_12==68) ) {
						alt26=1;
					}

					}
					break;
				case CASE:
					{
					int LA26_13 = input.LA(2);
					if ( (LA26_13==68) ) {
						alt26=1;
					}

					}
					break;
				case 124:
					{
					int LA26_14 = input.LA(2);
					if ( (LA26_14==68) ) {
						alt26=1;
					}

					}
					break;
				case SET:
					{
					int LA26_15 = input.LA(2);
					if ( (LA26_15==68) ) {
						alt26=1;
					}

					}
					break;
				case DESC:
					{
					int LA26_16 = input.LA(2);
					if ( (LA26_16==68) ) {
						alt26=1;
					}

					}
					break;
				case ASC:
					{
					int LA26_17 = input.LA(2);
					if ( (LA26_17==68) ) {
						alt26=1;
					}

					}
					break;
				case 96:
				case 100:
				case 106:
				case 115:
				case 117:
				case 127:
				case 129:
				case 143:
				case 145:
					{
					int LA26_18 = input.LA(2);
					if ( (LA26_18==68) ) {
						alt26=1;
					}

					}
					break;
				}
				switch (alt26) {
				case 1 :
					// JPA2.g:146:37: field '.'
					{
					pushFollow(FOLLOW_field_in_path_expression1157);
					field74=field();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_field.add(field74.getTree());
					char_literal75=(Token)match(input,68,FOLLOW_68_in_path_expression1158); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_68.add(char_literal75);

					}
					break;

				default :
					break loop26;
				}
			}

			// JPA2.g:146:48: ( field )?
			int alt27=2;
			switch ( input.LA(1) ) {
				case WORD:
					{
					int LA27_1 = input.LA(2);
					if ( (synpred30_JPA2()) ) {
						alt27=1;
					}
					}
					break;
				case AVG:
				case CASE:
				case COUNT:
				case MAX:
				case MIN:
				case SUM:
				case 96:
				case 100:
				case 106:
				case 115:
				case 117:
				case 124:
				case 127:
				case 129:
				case 130:
				case 143:
				case 145:
					{
					alt27=1;
					}
					break;
				case 104:
					{
					switch ( input.LA(2) ) {
						case EOF:
						case AND:
						case AS:
						case ASC:
						case DESC:
						case ELSE:
						case END:
						case GROUP:
						case HAVING:
						case INNER:
						case JOIN:
						case LEFT:
						case NOT:
						case OR:
						case ORDER:
						case RPAREN:
						case SET:
						case THEN:
						case WHEN:
						case 64:
						case 65:
						case 66:
						case 67:
						case 69:
						case 71:
						case 72:
						case 73:
						case 74:
						case 75:
						case 76:
						case 88:
						case 101:
						case 104:
						case 108:
						case 112:
						case 114:
						case 122:
						case 123:
						case 128:
						case 144:
							{
							alt27=1;
							}
							break;
						case WORD:
							{
							int LA27_12 = input.LA(3);
							if ( (LA27_12==EOF||LA27_12==LPAREN||LA27_12==RPAREN||LA27_12==66||LA27_12==104) ) {
								alt27=1;
							}
							}
							break;
						case IN:
							{
							int LA27_13 = input.LA(3);
							if ( (LA27_13==LPAREN||LA27_13==NAMED_PARAMETER||LA27_13==63||LA27_13==77) ) {
								alt27=1;
							}
							}
							break;
					}
					}
					break;
				case GROUP:
					{
					int LA27_4 = input.LA(2);
					if ( (LA27_4==EOF||(LA27_4 >= AND && LA27_4 <= ASC)||LA27_4==DESC||(LA27_4 >= ELSE && LA27_4 <= END)||(LA27_4 >= GROUP && LA27_4 <= INNER)||(LA27_4 >= JOIN && LA27_4 <= LEFT)||(LA27_4 >= NOT && LA27_4 <= ORDER)||LA27_4==RPAREN||LA27_4==SET||LA27_4==THEN||(LA27_4 >= WHEN && LA27_4 <= WORD)||(LA27_4 >= 64 && LA27_4 <= 67)||LA27_4==69||(LA27_4 >= 71 && LA27_4 <= 76)||LA27_4==88||LA27_4==101||LA27_4==104||LA27_4==108||LA27_4==112||LA27_4==114||(LA27_4 >= 122 && LA27_4 <= 123)||LA27_4==128||LA27_4==144) ) {
						alt27=1;
					}
					}
					break;
				case ORDER:
					{
					int LA27_5 = input.LA(2);
					if ( (LA27_5==EOF||(LA27_5 >= AND && LA27_5 <= ASC)||LA27_5==DESC||(LA27_5 >= ELSE && LA27_5 <= END)||(LA27_5 >= GROUP && LA27_5 <= INNER)||(LA27_5 >= JOIN && LA27_5 <= LEFT)||(LA27_5 >= NOT && LA27_5 <= ORDER)||LA27_5==RPAREN||LA27_5==SET||LA27_5==THEN||(LA27_5 >= WHEN && LA27_5 <= WORD)||(LA27_5 >= 64 && LA27_5 <= 67)||LA27_5==69||(LA27_5 >= 71 && LA27_5 <= 76)||LA27_5==88||LA27_5==101||LA27_5==104||LA27_5==108||LA27_5==112||LA27_5==114||(LA27_5 >= 122 && LA27_5 <= 123)||LA27_5==128||LA27_5==144) ) {
						alt27=1;
					}
					}
					break;
				case AS:
					{
					int LA27_6 = input.LA(2);
					if ( (synpred30_JPA2()) ) {
						alt27=1;
					}
					}
					break;
				case 114:
					{
					switch ( input.LA(2) ) {
						case EOF:
						case AND:
						case AS:
						case ASC:
						case DESC:
						case ELSE:
						case END:
						case HAVING:
						case IN:
						case INNER:
						case JOIN:
						case LEFT:
						case NOT:
						case OR:
						case ORDER:
						case RPAREN:
						case SET:
						case THEN:
						case WHEN:
						case 64:
						case 65:
						case 66:
						case 67:
						case 69:
						case 71:
						case 72:
						case 73:
						case 74:
						case 75:
						case 76:
						case 88:
						case 101:
						case 104:
						case 108:
						case 112:
						case 114:
						case 122:
						case 123:
						case 128:
						case 144:
							{
							alt27=1;
							}
							break;
						case WORD:
							{
							int LA27_14 = input.LA(3);
							if ( (LA27_14==EOF||LA27_14==LPAREN||LA27_14==RPAREN||LA27_14==66||LA27_14==104) ) {
								alt27=1;
							}
							}
							break;
						case GROUP:
							{
							int LA27_15 = input.LA(3);
							if ( (LA27_15==BY) ) {
								alt27=1;
							}
							}
							break;
					}
					}
					break;
				case SET:
					{
					switch ( input.LA(2) ) {
						case EOF:
						case AND:
						case AS:
						case ASC:
						case DESC:
						case ELSE:
						case END:
						case HAVING:
						case IN:
						case INNER:
						case JOIN:
						case LEFT:
						case NOT:
						case OR:
						case ORDER:
						case RPAREN:
						case SET:
						case THEN:
						case WHEN:
						case 64:
						case 65:
						case 66:
						case 67:
						case 69:
						case 71:
						case 72:
						case 73:
						case 74:
						case 75:
						case 76:
						case 88:
						case 101:
						case 104:
						case 108:
						case 112:
						case 114:
						case 122:
						case 123:
						case 128:
						case 144:
							{
							alt27=1;
							}
							break;
						case WORD:
							{
							int LA27_16 = input.LA(3);
							if ( (LA27_16==EOF||LA27_16==LPAREN||LA27_16==RPAREN||LA27_16==66||LA27_16==104) ) {
								alt27=1;
							}
							}
							break;
						case GROUP:
							{
							int LA27_17 = input.LA(3);
							if ( (LA27_17==BY) ) {
								alt27=1;
							}
							}
							break;
					}
					}
					break;
				case DESC:
					{
					int LA27_9 = input.LA(2);
					if ( (synpred30_JPA2()) ) {
						alt27=1;
					}
					}
					break;
				case ASC:
					{
					int LA27_10 = input.LA(2);
					if ( (synpred30_JPA2()) ) {
						alt27=1;
					}
					}
					break;
			}
			switch (alt27) {
				case 1 :
					// JPA2.g:146:48: field
					{
					pushFollow(FOLLOW_field_in_path_expression1162);
					field76=field();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_field.add(field76.getTree());
					}
					break;

			}

			// AST REWRITE
			// elements: field
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 147:5: -> ^( T_SELECTED_FIELD[$identification_variable.text] ( field )* )
			{
				// JPA2.g:147:8: ^( T_SELECTED_FIELD[$identification_variable.text] ( field )* )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new PathNode(T_SELECTED_FIELD, (identification_variable72!=null?input.toString(identification_variable72.start,identification_variable72.stop):null)), root_1);
				// JPA2.g:147:68: ( field )*
				while ( stream_field.hasNext() ) {
					adaptor.addChild(root_1, stream_field.nextTree());
				}
				stream_field.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "path_expression"


	public static class general_identification_variable_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "general_identification_variable"
	// JPA2.g:152:1: general_identification_variable : ( identification_variable | map_field_identification_variable );
	public final JPA2Parser.general_identification_variable_return general_identification_variable() throws RecognitionException {
		JPA2Parser.general_identification_variable_return retval = new JPA2Parser.general_identification_variable_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope identification_variable77 =null;
		ParserRuleReturnScope map_field_identification_variable78 =null;


		try {
			// JPA2.g:153:5: ( identification_variable | map_field_identification_variable )
			int alt28=2;
			int LA28_0 = input.LA(1);
			if ( (LA28_0==GROUP||LA28_0==WORD) ) {
				alt28=1;
			}
			else if ( (LA28_0==109||LA28_0==142) ) {
				alt28=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 28, 0, input);
				throw nvae;
			}

			switch (alt28) {
				case 1 :
					// JPA2.g:153:7: identification_variable
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_identification_variable_in_general_identification_variable1201);
					identification_variable77=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable77.getTree());

					}
					break;
				case 2 :
					// JPA2.g:154:7: map_field_identification_variable
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_map_field_identification_variable_in_general_identification_variable1209);
					map_field_identification_variable78=map_field_identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, map_field_identification_variable78.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "general_identification_variable"


	public static class update_clause_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "update_clause"
	// JPA2.g:157:1: update_clause : identification_variable_declaration SET update_item ( ',' update_item )* -> ^( T_SOURCES identification_variable_declaration SET update_item ( ',' update_item )* ) ;
	public final JPA2Parser.update_clause_return update_clause() throws RecognitionException {
		JPA2Parser.update_clause_return retval = new JPA2Parser.update_clause_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token SET80=null;
		Token char_literal82=null;
		ParserRuleReturnScope identification_variable_declaration79 =null;
		ParserRuleReturnScope update_item81 =null;
		ParserRuleReturnScope update_item83 =null;

		Object SET80_tree=null;
		Object char_literal82_tree=null;
		RewriteRuleTokenStream stream_66=new RewriteRuleTokenStream(adaptor,"token 66");
		RewriteRuleTokenStream stream_SET=new RewriteRuleTokenStream(adaptor,"token SET");
		RewriteRuleSubtreeStream stream_update_item=new RewriteRuleSubtreeStream(adaptor,"rule update_item");
		RewriteRuleSubtreeStream stream_identification_variable_declaration=new RewriteRuleSubtreeStream(adaptor,"rule identification_variable_declaration");

		try {
			// JPA2.g:158:5: ( identification_variable_declaration SET update_item ( ',' update_item )* -> ^( T_SOURCES identification_variable_declaration SET update_item ( ',' update_item )* ) )
			// JPA2.g:158:7: identification_variable_declaration SET update_item ( ',' update_item )*
			{
			pushFollow(FOLLOW_identification_variable_declaration_in_update_clause1222);
			identification_variable_declaration79=identification_variable_declaration();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_identification_variable_declaration.add(identification_variable_declaration79.getTree());
			SET80=(Token)match(input,SET,FOLLOW_SET_in_update_clause1224); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_SET.add(SET80);

			pushFollow(FOLLOW_update_item_in_update_clause1226);
			update_item81=update_item();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_update_item.add(update_item81.getTree());
			// JPA2.g:158:59: ( ',' update_item )*
			loop29:
			while (true) {
				int alt29=2;
				int LA29_0 = input.LA(1);
				if ( (LA29_0==66) ) {
					alt29=1;
				}

				switch (alt29) {
				case 1 :
					// JPA2.g:158:60: ',' update_item
					{
					char_literal82=(Token)match(input,66,FOLLOW_66_in_update_clause1229); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_66.add(char_literal82);

					pushFollow(FOLLOW_update_item_in_update_clause1231);
					update_item83=update_item();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_update_item.add(update_item83.getTree());
					}
					break;

				default :
					break loop29;
				}
			}

			// AST REWRITE
			// elements: update_item, 66, update_item, identification_variable_declaration, SET
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 159:5: -> ^( T_SOURCES identification_variable_declaration SET update_item ( ',' update_item )* )
			{
				// JPA2.g:159:8: ^( T_SOURCES identification_variable_declaration SET update_item ( ',' update_item )* )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new SelectionSourceNode(T_SOURCES), root_1);
				adaptor.addChild(root_1, stream_identification_variable_declaration.nextTree());
				adaptor.addChild(root_1, new UpdateSetNode(stream_SET.nextToken()));
				adaptor.addChild(root_1, stream_update_item.nextTree());
				// JPA2.g:159:108: ( ',' update_item )*
				while ( stream_update_item.hasNext()||stream_66.hasNext() ) {
					adaptor.addChild(root_1, stream_66.nextNode());
					adaptor.addChild(root_1, stream_update_item.nextTree());
				}
				stream_update_item.reset();
				stream_66.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "update_clause"


	public static class update_item_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "update_item"
	// JPA2.g:160:1: update_item : path_expression '=' new_value ;
	public final JPA2Parser.update_item_return update_item() throws RecognitionException {
		JPA2Parser.update_item_return retval = new JPA2Parser.update_item_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal85=null;
		ParserRuleReturnScope path_expression84 =null;
		ParserRuleReturnScope new_value86 =null;

		Object char_literal85_tree=null;

		try {
			// JPA2.g:161:5: ( path_expression '=' new_value )
			// JPA2.g:161:7: path_expression '=' new_value
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_path_expression_in_update_item1273);
			path_expression84=path_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression84.getTree());

			char_literal85=(Token)match(input,74,FOLLOW_74_in_update_item1275); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal85_tree = (Object)adaptor.create(char_literal85);
			adaptor.addChild(root_0, char_literal85_tree);
			}

			pushFollow(FOLLOW_new_value_in_update_item1277);
			new_value86=new_value();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, new_value86.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "update_item"


	public static class new_value_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "new_value"
	// JPA2.g:162:1: new_value : ( scalar_expression | simple_entity_expression | 'NULL' );
	public final JPA2Parser.new_value_return new_value() throws RecognitionException {
		JPA2Parser.new_value_return retval = new JPA2Parser.new_value_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal89=null;
		ParserRuleReturnScope scalar_expression87 =null;
		ParserRuleReturnScope simple_entity_expression88 =null;

		Object string_literal89_tree=null;

		try {
			// JPA2.g:163:5: ( scalar_expression | simple_entity_expression | 'NULL' )
			int alt30=3;
			switch ( input.LA(1) ) {
			case AVG:
			case CASE:
			case COUNT:
			case INT_NUMERAL:
			case LOWER:
			case LPAREN:
			case MAX:
			case MIN:
			case STRING_LITERAL:
			case SUM:
			case 65:
			case 67:
			case 70:
			case 83:
			case 85:
			case 90:
			case 91:
			case 92:
			case 93:
			case 94:
			case 95:
			case 103:
			case 105:
			case 107:
			case 111:
			case 113:
			case 116:
			case 121:
			case 131:
			case 133:
			case 134:
			case 137:
			case 138:
			case 140:
			case 146:
			case 147:
				{
				alt30=1;
				}
				break;
			case WORD:
				{
				int LA30_2 = input.LA(2);
				if ( (synpred33_JPA2()) ) {
					alt30=1;
				}
				else if ( (synpred34_JPA2()) ) {
					alt30=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 30, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 77:
				{
				int LA30_3 = input.LA(2);
				if ( (LA30_3==70) ) {
					int LA30_9 = input.LA(3);
					if ( (LA30_9==INT_NUMERAL) ) {
						int LA30_10 = input.LA(4);
						if ( (synpred33_JPA2()) ) {
							alt30=1;
						}
						else if ( (synpred34_JPA2()) ) {
							alt30=2;
						}

						else {
							if (state.backtracking>0) {state.failed=true; return retval;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 30, 10, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 30, 9, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA30_3==INT_NUMERAL) ) {
					int LA30_10 = input.LA(3);
					if ( (synpred33_JPA2()) ) {
						alt30=1;
					}
					else if ( (synpred34_JPA2()) ) {
						alt30=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 30, 10, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 30, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case NAMED_PARAMETER:
				{
				int LA30_4 = input.LA(2);
				if ( (synpred33_JPA2()) ) {
					alt30=1;
				}
				else if ( (synpred34_JPA2()) ) {
					alt30=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 30, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 63:
				{
				int LA30_5 = input.LA(2);
				if ( (LA30_5==WORD) ) {
					int LA30_11 = input.LA(3);
					if ( (LA30_11==148) ) {
						int LA30_12 = input.LA(4);
						if ( (synpred33_JPA2()) ) {
							alt30=1;
						}
						else if ( (synpred34_JPA2()) ) {
							alt30=2;
						}

						else {
							if (state.backtracking>0) {state.failed=true; return retval;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 30, 12, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 30, 11, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 30, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case GROUP:
				{
				int LA30_6 = input.LA(2);
				if ( (LA30_6==68) ) {
					alt30=1;
				}
				else if ( (LA30_6==EOF||LA30_6==66||LA30_6==144) ) {
					alt30=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 30, 6, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 120:
				{
				alt30=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 30, 0, input);
				throw nvae;
			}
			switch (alt30) {
				case 1 :
					// JPA2.g:163:7: scalar_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_scalar_expression_in_new_value1288);
					scalar_expression87=scalar_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression87.getTree());

					}
					break;
				case 2 :
					// JPA2.g:164:7: simple_entity_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_simple_entity_expression_in_new_value1296);
					simple_entity_expression88=simple_entity_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, simple_entity_expression88.getTree());

					}
					break;
				case 3 :
					// JPA2.g:165:7: 'NULL'
					{
					root_0 = (Object)adaptor.nil();


					string_literal89=(Token)match(input,120,FOLLOW_120_in_new_value1304); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal89_tree = (Object)adaptor.create(string_literal89);
					adaptor.addChild(root_0, string_literal89_tree);
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "new_value"


	public static class delete_clause_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "delete_clause"
	// JPA2.g:167:1: delete_clause : fr= 'FROM' identification_variable_declaration -> ^( T_SOURCES[$fr] identification_variable_declaration ) ;
	public final JPA2Parser.delete_clause_return delete_clause() throws RecognitionException {
		JPA2Parser.delete_clause_return retval = new JPA2Parser.delete_clause_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token fr=null;
		ParserRuleReturnScope identification_variable_declaration90 =null;

		Object fr_tree=null;
		RewriteRuleTokenStream stream_104=new RewriteRuleTokenStream(adaptor,"token 104");
		RewriteRuleSubtreeStream stream_identification_variable_declaration=new RewriteRuleSubtreeStream(adaptor,"rule identification_variable_declaration");

		try {
			// JPA2.g:168:5: (fr= 'FROM' identification_variable_declaration -> ^( T_SOURCES[$fr] identification_variable_declaration ) )
			// JPA2.g:168:7: fr= 'FROM' identification_variable_declaration
			{
			fr=(Token)match(input,104,FOLLOW_104_in_delete_clause1318); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_104.add(fr);

			pushFollow(FOLLOW_identification_variable_declaration_in_delete_clause1320);
			identification_variable_declaration90=identification_variable_declaration();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_identification_variable_declaration.add(identification_variable_declaration90.getTree());
			// AST REWRITE
			// elements: identification_variable_declaration
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 169:5: -> ^( T_SOURCES[$fr] identification_variable_declaration )
			{
				// JPA2.g:169:8: ^( T_SOURCES[$fr] identification_variable_declaration )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new FromNode(T_SOURCES, fr), root_1);
				adaptor.addChild(root_1, stream_identification_variable_declaration.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "delete_clause"


	public static class select_clause_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "select_clause"
	// JPA2.g:170:1: select_clause : ( 'DISTINCT' )? select_item ( ',' select_item )* -> ^( T_SELECTED_ITEMS[] ( 'DISTINCT' )? ( ^( T_SELECTED_ITEM[] select_item ) )* ) ;
	public final JPA2Parser.select_clause_return select_clause() throws RecognitionException {
		JPA2Parser.select_clause_return retval = new JPA2Parser.select_clause_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal91=null;
		Token char_literal93=null;
		ParserRuleReturnScope select_item92 =null;
		ParserRuleReturnScope select_item94 =null;

		Object string_literal91_tree=null;
		Object char_literal93_tree=null;
		RewriteRuleTokenStream stream_66=new RewriteRuleTokenStream(adaptor,"token 66");
		RewriteRuleTokenStream stream_DISTINCT=new RewriteRuleTokenStream(adaptor,"token DISTINCT");
		RewriteRuleSubtreeStream stream_select_item=new RewriteRuleSubtreeStream(adaptor,"rule select_item");

		try {
			// JPA2.g:171:5: ( ( 'DISTINCT' )? select_item ( ',' select_item )* -> ^( T_SELECTED_ITEMS[] ( 'DISTINCT' )? ( ^( T_SELECTED_ITEM[] select_item ) )* ) )
			// JPA2.g:171:7: ( 'DISTINCT' )? select_item ( ',' select_item )*
			{
			// JPA2.g:171:7: ( 'DISTINCT' )?
			int alt31=2;
			int LA31_0 = input.LA(1);
			if ( (LA31_0==DISTINCT) ) {
				alt31=1;
			}
			switch (alt31) {
				case 1 :
					// JPA2.g:171:8: 'DISTINCT'
					{
					string_literal91=(Token)match(input,DISTINCT,FOLLOW_DISTINCT_in_select_clause1348); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DISTINCT.add(string_literal91);

					}
					break;

			}

			pushFollow(FOLLOW_select_item_in_select_clause1352);
			select_item92=select_item();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_select_item.add(select_item92.getTree());
			// JPA2.g:171:33: ( ',' select_item )*
			loop32:
			while (true) {
				int alt32=2;
				int LA32_0 = input.LA(1);
				if ( (LA32_0==66) ) {
					alt32=1;
				}

				switch (alt32) {
				case 1 :
					// JPA2.g:171:34: ',' select_item
					{
					char_literal93=(Token)match(input,66,FOLLOW_66_in_select_clause1355); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_66.add(char_literal93);

					pushFollow(FOLLOW_select_item_in_select_clause1357);
					select_item94=select_item();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_select_item.add(select_item94.getTree());
					}
					break;

				default :
					break loop32;
				}
			}

			// AST REWRITE
			// elements: DISTINCT, select_item
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 172:5: -> ^( T_SELECTED_ITEMS[] ( 'DISTINCT' )? ( ^( T_SELECTED_ITEM[] select_item ) )* )
			{
				// JPA2.g:172:8: ^( T_SELECTED_ITEMS[] ( 'DISTINCT' )? ( ^( T_SELECTED_ITEM[] select_item ) )* )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new SelectedItemsNode(T_SELECTED_ITEMS), root_1);
				// JPA2.g:172:48: ( 'DISTINCT' )?
				if ( stream_DISTINCT.hasNext() ) {
					adaptor.addChild(root_1, stream_DISTINCT.nextNode());
				}
				stream_DISTINCT.reset();

				// JPA2.g:172:62: ( ^( T_SELECTED_ITEM[] select_item ) )*
				while ( stream_select_item.hasNext() ) {
					// JPA2.g:172:62: ^( T_SELECTED_ITEM[] select_item )
					{
					Object root_2 = (Object)adaptor.nil();
					root_2 = (Object)adaptor.becomeRoot(new SelectedItemNode(T_SELECTED_ITEM), root_2);
					adaptor.addChild(root_2, stream_select_item.nextTree());
					adaptor.addChild(root_1, root_2);
					}

				}
				stream_select_item.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "select_clause"


	public static class select_item_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "select_item"
	// JPA2.g:173:1: select_item : select_expression ( ( AS )? result_variable )? ;
	public final JPA2Parser.select_item_return select_item() throws RecognitionException {
		JPA2Parser.select_item_return retval = new JPA2Parser.select_item_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token AS96=null;
		ParserRuleReturnScope select_expression95 =null;
		ParserRuleReturnScope result_variable97 =null;

		Object AS96_tree=null;

		try {
			// JPA2.g:174:5: ( select_expression ( ( AS )? result_variable )? )
			// JPA2.g:174:7: select_expression ( ( AS )? result_variable )?
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_select_expression_in_select_item1400);
			select_expression95=select_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, select_expression95.getTree());

			// JPA2.g:174:25: ( ( AS )? result_variable )?
			int alt34=2;
			int LA34_0 = input.LA(1);
			if ( (LA34_0==AS||LA34_0==WORD) ) {
				alt34=1;
			}
			switch (alt34) {
				case 1 :
					// JPA2.g:174:26: ( AS )? result_variable
					{
					// JPA2.g:174:26: ( AS )?
					int alt33=2;
					int LA33_0 = input.LA(1);
					if ( (LA33_0==AS) ) {
						alt33=1;
					}
					switch (alt33) {
						case 1 :
							// JPA2.g:174:27: AS
							{
							AS96=(Token)match(input,AS,FOLLOW_AS_in_select_item1404); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							AS96_tree = (Object)adaptor.create(AS96);
							adaptor.addChild(root_0, AS96_tree);
							}

							}
							break;

					}

					pushFollow(FOLLOW_result_variable_in_select_item1408);
					result_variable97=result_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, result_variable97.getTree());

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "select_item"


	public static class select_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "select_expression"
	// JPA2.g:175:1: select_expression : ( path_expression ( ( '+' | '-' | '*' | '/' ) scalar_expression )? | identification_variable -> ^( T_SELECTED_ENTITY[$identification_variable.text] ) | scalar_expression | aggregate_expression | 'OBJECT' '(' identification_variable ')' | constructor_expression );
	public final JPA2Parser.select_expression_return select_expression() throws RecognitionException {
		JPA2Parser.select_expression_return retval = new JPA2Parser.select_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set99=null;
		Token string_literal104=null;
		Token char_literal105=null;
		Token char_literal107=null;
		ParserRuleReturnScope path_expression98 =null;
		ParserRuleReturnScope scalar_expression100 =null;
		ParserRuleReturnScope identification_variable101 =null;
		ParserRuleReturnScope scalar_expression102 =null;
		ParserRuleReturnScope aggregate_expression103 =null;
		ParserRuleReturnScope identification_variable106 =null;
		ParserRuleReturnScope constructor_expression108 =null;

		Object set99_tree=null;
		Object string_literal104_tree=null;
		Object char_literal105_tree=null;
		Object char_literal107_tree=null;
		RewriteRuleSubtreeStream stream_identification_variable=new RewriteRuleSubtreeStream(adaptor,"rule identification_variable");

		try {
			// JPA2.g:176:5: ( path_expression ( ( '+' | '-' | '*' | '/' ) scalar_expression )? | identification_variable -> ^( T_SELECTED_ENTITY[$identification_variable.text] ) | scalar_expression | aggregate_expression | 'OBJECT' '(' identification_variable ')' | constructor_expression )
			int alt36=6;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA36_1 = input.LA(2);
				if ( (synpred43_JPA2()) ) {
					alt36=1;
				}
				else if ( (synpred44_JPA2()) ) {
					alt36=2;
				}
				else if ( (synpred45_JPA2()) ) {
					alt36=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 36, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case CASE:
			case INT_NUMERAL:
			case LOWER:
			case LPAREN:
			case NAMED_PARAMETER:
			case STRING_LITERAL:
			case 63:
			case 65:
			case 67:
			case 70:
			case 77:
			case 83:
			case 85:
			case 90:
			case 91:
			case 92:
			case 93:
			case 94:
			case 95:
			case 103:
			case 107:
			case 111:
			case 113:
			case 116:
			case 121:
			case 131:
			case 133:
			case 134:
			case 137:
			case 138:
			case 140:
			case 146:
			case 147:
				{
				alt36=3;
				}
				break;
			case COUNT:
				{
				int LA36_16 = input.LA(2);
				if ( (synpred45_JPA2()) ) {
					alt36=3;
				}
				else if ( (synpred46_JPA2()) ) {
					alt36=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 36, 16, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
				{
				int LA36_17 = input.LA(2);
				if ( (synpred45_JPA2()) ) {
					alt36=3;
				}
				else if ( (synpred46_JPA2()) ) {
					alt36=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 36, 17, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 105:
				{
				int LA36_18 = input.LA(2);
				if ( (synpred45_JPA2()) ) {
					alt36=3;
				}
				else if ( (synpred46_JPA2()) ) {
					alt36=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 36, 18, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case GROUP:
				{
				int LA36_31 = input.LA(2);
				if ( (synpred43_JPA2()) ) {
					alt36=1;
				}
				else if ( (synpred44_JPA2()) ) {
					alt36=2;
				}
				else if ( (synpred45_JPA2()) ) {
					alt36=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 36, 31, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 124:
				{
				alt36=5;
				}
				break;
			case 118:
				{
				alt36=6;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 36, 0, input);
				throw nvae;
			}
			switch (alt36) {
				case 1 :
					// JPA2.g:176:7: path_expression ( ( '+' | '-' | '*' | '/' ) scalar_expression )?
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_select_expression1421);
					path_expression98=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression98.getTree());

					// JPA2.g:176:23: ( ( '+' | '-' | '*' | '/' ) scalar_expression )?
					int alt35=2;
					int LA35_0 = input.LA(1);
					if ( ((LA35_0 >= 64 && LA35_0 <= 65)||LA35_0==67||LA35_0==69) ) {
						alt35=1;
					}
					switch (alt35) {
						case 1 :
							// JPA2.g:176:24: ( '+' | '-' | '*' | '/' ) scalar_expression
							{
							set99=input.LT(1);
							if ( (input.LA(1) >= 64 && input.LA(1) <= 65)||input.LA(1)==67||input.LA(1)==69 ) {
								input.consume();
								if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set99));
								state.errorRecovery=false;
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								throw mse;
							}
							pushFollow(FOLLOW_scalar_expression_in_select_expression1440);
							scalar_expression100=scalar_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression100.getTree());

							}
							break;

					}

					}
					break;
				case 2 :
					// JPA2.g:177:7: identification_variable
					{
					pushFollow(FOLLOW_identification_variable_in_select_expression1450);
					identification_variable101=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_identification_variable.add(identification_variable101.getTree());
					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 177:31: -> ^( T_SELECTED_ENTITY[$identification_variable.text] )
					{
						// JPA2.g:177:34: ^( T_SELECTED_ENTITY[$identification_variable.text] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot(new PathNode(T_SELECTED_ENTITY, (identification_variable101!=null?input.toString(identification_variable101.start,identification_variable101.stop):null)), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// JPA2.g:178:7: scalar_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_scalar_expression_in_select_expression1468);
					scalar_expression102=scalar_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression102.getTree());

					}
					break;
				case 4 :
					// JPA2.g:179:7: aggregate_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_aggregate_expression_in_select_expression1476);
					aggregate_expression103=aggregate_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, aggregate_expression103.getTree());

					}
					break;
				case 5 :
					// JPA2.g:180:7: 'OBJECT' '(' identification_variable ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal104=(Token)match(input,124,FOLLOW_124_in_select_expression1484); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal104_tree = (Object)adaptor.create(string_literal104);
					adaptor.addChild(root_0, string_literal104_tree);
					}

					char_literal105=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_select_expression1486); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal105_tree = (Object)adaptor.create(char_literal105);
					adaptor.addChild(root_0, char_literal105_tree);
					}

					pushFollow(FOLLOW_identification_variable_in_select_expression1487);
					identification_variable106=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable106.getTree());

					char_literal107=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_select_expression1488); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal107_tree = (Object)adaptor.create(char_literal107);
					adaptor.addChild(root_0, char_literal107_tree);
					}

					}
					break;
				case 6 :
					// JPA2.g:181:7: constructor_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_constructor_expression_in_select_expression1496);
					constructor_expression108=constructor_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, constructor_expression108.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "select_expression"


	public static class constructor_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "constructor_expression"
	// JPA2.g:182:1: constructor_expression : 'NEW' constructor_name '(' constructor_item ( ',' constructor_item )* ')' ;
	public final JPA2Parser.constructor_expression_return constructor_expression() throws RecognitionException {
		JPA2Parser.constructor_expression_return retval = new JPA2Parser.constructor_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal109=null;
		Token char_literal111=null;
		Token char_literal113=null;
		Token char_literal115=null;
		ParserRuleReturnScope constructor_name110 =null;
		ParserRuleReturnScope constructor_item112 =null;
		ParserRuleReturnScope constructor_item114 =null;

		Object string_literal109_tree=null;
		Object char_literal111_tree=null;
		Object char_literal113_tree=null;
		Object char_literal115_tree=null;

		try {
			// JPA2.g:183:5: ( 'NEW' constructor_name '(' constructor_item ( ',' constructor_item )* ')' )
			// JPA2.g:183:7: 'NEW' constructor_name '(' constructor_item ( ',' constructor_item )* ')'
			{
			root_0 = (Object)adaptor.nil();


			string_literal109=(Token)match(input,118,FOLLOW_118_in_constructor_expression1507); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal109_tree = (Object)adaptor.create(string_literal109);
			adaptor.addChild(root_0, string_literal109_tree);
			}

			pushFollow(FOLLOW_constructor_name_in_constructor_expression1509);
			constructor_name110=constructor_name();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, constructor_name110.getTree());

			char_literal111=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_constructor_expression1511); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal111_tree = (Object)adaptor.create(char_literal111);
			adaptor.addChild(root_0, char_literal111_tree);
			}

			pushFollow(FOLLOW_constructor_item_in_constructor_expression1513);
			constructor_item112=constructor_item();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, constructor_item112.getTree());

			// JPA2.g:183:51: ( ',' constructor_item )*
			loop37:
			while (true) {
				int alt37=2;
				int LA37_0 = input.LA(1);
				if ( (LA37_0==66) ) {
					alt37=1;
				}

				switch (alt37) {
				case 1 :
					// JPA2.g:183:52: ',' constructor_item
					{
					char_literal113=(Token)match(input,66,FOLLOW_66_in_constructor_expression1516); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal113_tree = (Object)adaptor.create(char_literal113);
					adaptor.addChild(root_0, char_literal113_tree);
					}

					pushFollow(FOLLOW_constructor_item_in_constructor_expression1518);
					constructor_item114=constructor_item();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, constructor_item114.getTree());

					}
					break;

				default :
					break loop37;
				}
			}

			char_literal115=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_constructor_expression1522); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal115_tree = (Object)adaptor.create(char_literal115);
			adaptor.addChild(root_0, char_literal115_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "constructor_expression"


	public static class constructor_item_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "constructor_item"
	// JPA2.g:184:1: constructor_item : ( path_expression | scalar_expression | aggregate_expression | identification_variable );
	public final JPA2Parser.constructor_item_return constructor_item() throws RecognitionException {
		JPA2Parser.constructor_item_return retval = new JPA2Parser.constructor_item_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope path_expression116 =null;
		ParserRuleReturnScope scalar_expression117 =null;
		ParserRuleReturnScope aggregate_expression118 =null;
		ParserRuleReturnScope identification_variable119 =null;


		try {
			// JPA2.g:185:5: ( path_expression | scalar_expression | aggregate_expression | identification_variable )
			int alt38=4;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA38_1 = input.LA(2);
				if ( (synpred49_JPA2()) ) {
					alt38=1;
				}
				else if ( (synpred50_JPA2()) ) {
					alt38=2;
				}
				else if ( (true) ) {
					alt38=4;
				}

				}
				break;
			case CASE:
			case INT_NUMERAL:
			case LOWER:
			case LPAREN:
			case NAMED_PARAMETER:
			case STRING_LITERAL:
			case 63:
			case 65:
			case 67:
			case 70:
			case 77:
			case 83:
			case 85:
			case 90:
			case 91:
			case 92:
			case 93:
			case 94:
			case 95:
			case 103:
			case 107:
			case 111:
			case 113:
			case 116:
			case 121:
			case 131:
			case 133:
			case 134:
			case 137:
			case 138:
			case 140:
			case 146:
			case 147:
				{
				alt38=2;
				}
				break;
			case COUNT:
				{
				int LA38_16 = input.LA(2);
				if ( (synpred50_JPA2()) ) {
					alt38=2;
				}
				else if ( (synpred51_JPA2()) ) {
					alt38=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 38, 16, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
				{
				int LA38_17 = input.LA(2);
				if ( (synpred50_JPA2()) ) {
					alt38=2;
				}
				else if ( (synpred51_JPA2()) ) {
					alt38=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 38, 17, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 105:
				{
				int LA38_18 = input.LA(2);
				if ( (synpred50_JPA2()) ) {
					alt38=2;
				}
				else if ( (synpred51_JPA2()) ) {
					alt38=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 38, 18, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case GROUP:
				{
				int LA38_31 = input.LA(2);
				if ( (synpred49_JPA2()) ) {
					alt38=1;
				}
				else if ( (synpred50_JPA2()) ) {
					alt38=2;
				}
				else if ( (true) ) {
					alt38=4;
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 38, 0, input);
				throw nvae;
			}
			switch (alt38) {
				case 1 :
					// JPA2.g:185:7: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_constructor_item1533);
					path_expression116=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression116.getTree());

					}
					break;
				case 2 :
					// JPA2.g:186:7: scalar_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_scalar_expression_in_constructor_item1541);
					scalar_expression117=scalar_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression117.getTree());

					}
					break;
				case 3 :
					// JPA2.g:187:7: aggregate_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_aggregate_expression_in_constructor_item1549);
					aggregate_expression118=aggregate_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, aggregate_expression118.getTree());

					}
					break;
				case 4 :
					// JPA2.g:188:7: identification_variable
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_identification_variable_in_constructor_item1557);
					identification_variable119=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable119.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "constructor_item"


	public static class aggregate_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "aggregate_expression"
	// JPA2.g:189:1: aggregate_expression : ( aggregate_expression_function_name '(' ( DISTINCT )? arithmetic_expression ')' -> ^( T_AGGREGATE_EXPR[] aggregate_expression_function_name '(' ( 'DISTINCT' )? arithmetic_expression ')' ) | 'COUNT' '(' ( DISTINCT )? count_argument ')' -> ^( T_AGGREGATE_EXPR[] 'COUNT' '(' ( 'DISTINCT' )? count_argument ')' ) | function_invocation );
	public final JPA2Parser.aggregate_expression_return aggregate_expression() throws RecognitionException {
		JPA2Parser.aggregate_expression_return retval = new JPA2Parser.aggregate_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal121=null;
		Token DISTINCT122=null;
		Token char_literal124=null;
		Token string_literal125=null;
		Token char_literal126=null;
		Token DISTINCT127=null;
		Token char_literal129=null;
		ParserRuleReturnScope aggregate_expression_function_name120 =null;
		ParserRuleReturnScope arithmetic_expression123 =null;
		ParserRuleReturnScope count_argument128 =null;
		ParserRuleReturnScope function_invocation130 =null;

		Object char_literal121_tree=null;
		Object DISTINCT122_tree=null;
		Object char_literal124_tree=null;
		Object string_literal125_tree=null;
		Object char_literal126_tree=null;
		Object DISTINCT127_tree=null;
		Object char_literal129_tree=null;
		RewriteRuleTokenStream stream_DISTINCT=new RewriteRuleTokenStream(adaptor,"token DISTINCT");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleTokenStream stream_COUNT=new RewriteRuleTokenStream(adaptor,"token COUNT");
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleSubtreeStream stream_arithmetic_expression=new RewriteRuleSubtreeStream(adaptor,"rule arithmetic_expression");
		RewriteRuleSubtreeStream stream_count_argument=new RewriteRuleSubtreeStream(adaptor,"rule count_argument");
		RewriteRuleSubtreeStream stream_aggregate_expression_function_name=new RewriteRuleSubtreeStream(adaptor,"rule aggregate_expression_function_name");

		try {
			// JPA2.g:190:5: ( aggregate_expression_function_name '(' ( DISTINCT )? arithmetic_expression ')' -> ^( T_AGGREGATE_EXPR[] aggregate_expression_function_name '(' ( 'DISTINCT' )? arithmetic_expression ')' ) | 'COUNT' '(' ( DISTINCT )? count_argument ')' -> ^( T_AGGREGATE_EXPR[] 'COUNT' '(' ( 'DISTINCT' )? count_argument ')' ) | function_invocation )
			int alt41=3;
			alt41 = dfa41.predict(input);
			switch (alt41) {
				case 1 :
					// JPA2.g:190:7: aggregate_expression_function_name '(' ( DISTINCT )? arithmetic_expression ')'
					{
					pushFollow(FOLLOW_aggregate_expression_function_name_in_aggregate_expression1568);
					aggregate_expression_function_name120=aggregate_expression_function_name();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_aggregate_expression_function_name.add(aggregate_expression_function_name120.getTree());
					char_literal121=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_aggregate_expression1570); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LPAREN.add(char_literal121);

					// JPA2.g:190:45: ( DISTINCT )?
					int alt39=2;
					int LA39_0 = input.LA(1);
					if ( (LA39_0==DISTINCT) ) {
						alt39=1;
					}
					switch (alt39) {
						case 1 :
							// JPA2.g:190:46: DISTINCT
							{
							DISTINCT122=(Token)match(input,DISTINCT,FOLLOW_DISTINCT_in_aggregate_expression1572); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_DISTINCT.add(DISTINCT122);

							}
							break;

					}

					pushFollow(FOLLOW_arithmetic_expression_in_aggregate_expression1576);
					arithmetic_expression123=arithmetic_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_arithmetic_expression.add(arithmetic_expression123.getTree());
					char_literal124=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_aggregate_expression1577); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RPAREN.add(char_literal124);

					// AST REWRITE
					// elements: arithmetic_expression, LPAREN, RPAREN, aggregate_expression_function_name, DISTINCT
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 191:5: -> ^( T_AGGREGATE_EXPR[] aggregate_expression_function_name '(' ( 'DISTINCT' )? arithmetic_expression ')' )
					{
						// JPA2.g:191:8: ^( T_AGGREGATE_EXPR[] aggregate_expression_function_name '(' ( 'DISTINCT' )? arithmetic_expression ')' )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot(new AggregateExpressionNode(T_AGGREGATE_EXPR), root_1);
						adaptor.addChild(root_1, stream_aggregate_expression_function_name.nextTree());
						adaptor.addChild(root_1, stream_LPAREN.nextNode());
						// JPA2.g:191:93: ( 'DISTINCT' )?
						if ( stream_DISTINCT.hasNext() ) {
							adaptor.addChild(root_1, (Object)adaptor.create(DISTINCT, "DISTINCT"));
						}
						stream_DISTINCT.reset();

						adaptor.addChild(root_1, stream_arithmetic_expression.nextTree());
						adaptor.addChild(root_1, stream_RPAREN.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// JPA2.g:192:7: 'COUNT' '(' ( DISTINCT )? count_argument ')'
					{
					string_literal125=(Token)match(input,COUNT,FOLLOW_COUNT_in_aggregate_expression1611); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_COUNT.add(string_literal125);

					char_literal126=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_aggregate_expression1613); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_LPAREN.add(char_literal126);

					// JPA2.g:192:18: ( DISTINCT )?
					int alt40=2;
					int LA40_0 = input.LA(1);
					if ( (LA40_0==DISTINCT) ) {
						alt40=1;
					}
					switch (alt40) {
						case 1 :
							// JPA2.g:192:19: DISTINCT
							{
							DISTINCT127=(Token)match(input,DISTINCT,FOLLOW_DISTINCT_in_aggregate_expression1615); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_DISTINCT.add(DISTINCT127);

							}
							break;

					}

					pushFollow(FOLLOW_count_argument_in_aggregate_expression1619);
					count_argument128=count_argument();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_count_argument.add(count_argument128.getTree());
					char_literal129=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_aggregate_expression1621); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_RPAREN.add(char_literal129);

					// AST REWRITE
					// elements: RPAREN, LPAREN, COUNT, count_argument, DISTINCT
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 193:5: -> ^( T_AGGREGATE_EXPR[] 'COUNT' '(' ( 'DISTINCT' )? count_argument ')' )
					{
						// JPA2.g:193:8: ^( T_AGGREGATE_EXPR[] 'COUNT' '(' ( 'DISTINCT' )? count_argument ')' )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot(new AggregateExpressionNode(T_AGGREGATE_EXPR), root_1);
						adaptor.addChild(root_1, stream_COUNT.nextNode());
						adaptor.addChild(root_1, stream_LPAREN.nextNode());
						// JPA2.g:193:66: ( 'DISTINCT' )?
						if ( stream_DISTINCT.hasNext() ) {
							adaptor.addChild(root_1, (Object)adaptor.create(DISTINCT, "DISTINCT"));
						}
						stream_DISTINCT.reset();

						adaptor.addChild(root_1, stream_count_argument.nextTree());
						adaptor.addChild(root_1, stream_RPAREN.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// JPA2.g:194:7: function_invocation
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_function_invocation_in_aggregate_expression1656);
					function_invocation130=function_invocation();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, function_invocation130.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "aggregate_expression"


	public static class aggregate_expression_function_name_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "aggregate_expression_function_name"
	// JPA2.g:195:1: aggregate_expression_function_name : ( 'AVG' | 'MAX' | 'MIN' | 'SUM' | 'COUNT' );
	public final JPA2Parser.aggregate_expression_function_name_return aggregate_expression_function_name() throws RecognitionException {
		JPA2Parser.aggregate_expression_function_name_return retval = new JPA2Parser.aggregate_expression_function_name_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set131=null;

		Object set131_tree=null;

		try {
			// JPA2.g:196:5: ( 'AVG' | 'MAX' | 'MIN' | 'SUM' | 'COUNT' )
			// JPA2.g:
			{
			root_0 = (Object)adaptor.nil();


			set131=input.LT(1);
			if ( input.LA(1)==AVG||input.LA(1)==COUNT||(input.LA(1) >= MAX && input.LA(1) <= MIN)||input.LA(1)==SUM ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set131));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "aggregate_expression_function_name"


	public static class count_argument_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "count_argument"
	// JPA2.g:197:1: count_argument : ( identification_variable | path_expression );
	public final JPA2Parser.count_argument_return count_argument() throws RecognitionException {
		JPA2Parser.count_argument_return retval = new JPA2Parser.count_argument_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope identification_variable132 =null;
		ParserRuleReturnScope path_expression133 =null;


		try {
			// JPA2.g:198:5: ( identification_variable | path_expression )
			int alt42=2;
			int LA42_0 = input.LA(1);
			if ( (LA42_0==GROUP||LA42_0==WORD) ) {
				int LA42_1 = input.LA(2);
				if ( (LA42_1==RPAREN) ) {
					alt42=1;
				}
				else if ( (LA42_1==68) ) {
					alt42=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 42, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 42, 0, input);
				throw nvae;
			}

			switch (alt42) {
				case 1 :
					// JPA2.g:198:7: identification_variable
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_identification_variable_in_count_argument1693);
					identification_variable132=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable132.getTree());

					}
					break;
				case 2 :
					// JPA2.g:198:33: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_count_argument1697);
					path_expression133=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression133.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "count_argument"


	public static class where_clause_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "where_clause"
	// JPA2.g:199:1: where_clause : wh= 'WHERE' conditional_expression -> ^( T_CONDITION[$wh] conditional_expression ) ;
	public final JPA2Parser.where_clause_return where_clause() throws RecognitionException {
		JPA2Parser.where_clause_return retval = new JPA2Parser.where_clause_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token wh=null;
		ParserRuleReturnScope conditional_expression134 =null;

		Object wh_tree=null;
		RewriteRuleTokenStream stream_144=new RewriteRuleTokenStream(adaptor,"token 144");
		RewriteRuleSubtreeStream stream_conditional_expression=new RewriteRuleSubtreeStream(adaptor,"rule conditional_expression");

		try {
			// JPA2.g:200:5: (wh= 'WHERE' conditional_expression -> ^( T_CONDITION[$wh] conditional_expression ) )
			// JPA2.g:200:7: wh= 'WHERE' conditional_expression
			{
			wh=(Token)match(input,144,FOLLOW_144_in_where_clause1710); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_144.add(wh);

			pushFollow(FOLLOW_conditional_expression_in_where_clause1712);
			conditional_expression134=conditional_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_conditional_expression.add(conditional_expression134.getTree());
			// AST REWRITE
			// elements: conditional_expression
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 200:40: -> ^( T_CONDITION[$wh] conditional_expression )
			{
				// JPA2.g:200:43: ^( T_CONDITION[$wh] conditional_expression )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new WhereNode(T_CONDITION, wh), root_1);
				adaptor.addChild(root_1, stream_conditional_expression.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "where_clause"


	public static class groupby_clause_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "groupby_clause"
	// JPA2.g:201:1: groupby_clause : 'GROUP' 'BY' groupby_item ( ',' groupby_item )* -> ^( T_GROUP_BY[] 'GROUP' 'BY' ( groupby_item )* ) ;
	public final JPA2Parser.groupby_clause_return groupby_clause() throws RecognitionException {
		JPA2Parser.groupby_clause_return retval = new JPA2Parser.groupby_clause_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal135=null;
		Token string_literal136=null;
		Token char_literal138=null;
		ParserRuleReturnScope groupby_item137 =null;
		ParserRuleReturnScope groupby_item139 =null;

		Object string_literal135_tree=null;
		Object string_literal136_tree=null;
		Object char_literal138_tree=null;
		RewriteRuleTokenStream stream_66=new RewriteRuleTokenStream(adaptor,"token 66");
		RewriteRuleTokenStream stream_GROUP=new RewriteRuleTokenStream(adaptor,"token GROUP");
		RewriteRuleTokenStream stream_BY=new RewriteRuleTokenStream(adaptor,"token BY");
		RewriteRuleSubtreeStream stream_groupby_item=new RewriteRuleSubtreeStream(adaptor,"rule groupby_item");

		try {
			// JPA2.g:202:5: ( 'GROUP' 'BY' groupby_item ( ',' groupby_item )* -> ^( T_GROUP_BY[] 'GROUP' 'BY' ( groupby_item )* ) )
			// JPA2.g:202:7: 'GROUP' 'BY' groupby_item ( ',' groupby_item )*
			{
			string_literal135=(Token)match(input,GROUP,FOLLOW_GROUP_in_groupby_clause1734); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_GROUP.add(string_literal135);

			string_literal136=(Token)match(input,BY,FOLLOW_BY_in_groupby_clause1736); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_BY.add(string_literal136);

			pushFollow(FOLLOW_groupby_item_in_groupby_clause1738);
			groupby_item137=groupby_item();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_groupby_item.add(groupby_item137.getTree());
			// JPA2.g:202:33: ( ',' groupby_item )*
			loop43:
			while (true) {
				int alt43=2;
				int LA43_0 = input.LA(1);
				if ( (LA43_0==66) ) {
					alt43=1;
				}

				switch (alt43) {
				case 1 :
					// JPA2.g:202:34: ',' groupby_item
					{
					char_literal138=(Token)match(input,66,FOLLOW_66_in_groupby_clause1741); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_66.add(char_literal138);

					pushFollow(FOLLOW_groupby_item_in_groupby_clause1743);
					groupby_item139=groupby_item();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_groupby_item.add(groupby_item139.getTree());
					}
					break;

				default :
					break loop43;
				}
			}

			// AST REWRITE
			// elements: BY, GROUP, groupby_item
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 203:5: -> ^( T_GROUP_BY[] 'GROUP' 'BY' ( groupby_item )* )
			{
				// JPA2.g:203:8: ^( T_GROUP_BY[] 'GROUP' 'BY' ( groupby_item )* )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new GroupByNode(T_GROUP_BY), root_1);
				adaptor.addChild(root_1, stream_GROUP.nextNode());
				adaptor.addChild(root_1, stream_BY.nextNode());
				// JPA2.g:203:49: ( groupby_item )*
				while ( stream_groupby_item.hasNext() ) {
					adaptor.addChild(root_1, stream_groupby_item.nextTree());
				}
				stream_groupby_item.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "groupby_clause"


	public static class groupby_item_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "groupby_item"
	// JPA2.g:204:1: groupby_item : ( path_expression | identification_variable | extract_function );
	public final JPA2Parser.groupby_item_return groupby_item() throws RecognitionException {
		JPA2Parser.groupby_item_return retval = new JPA2Parser.groupby_item_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope path_expression140 =null;
		ParserRuleReturnScope identification_variable141 =null;
		ParserRuleReturnScope extract_function142 =null;


		try {
			// JPA2.g:205:5: ( path_expression | identification_variable | extract_function )
			int alt44=3;
			int LA44_0 = input.LA(1);
			if ( (LA44_0==GROUP||LA44_0==WORD) ) {
				int LA44_1 = input.LA(2);
				if ( (LA44_1==68) ) {
					alt44=1;
				}
				else if ( (LA44_1==EOF||LA44_1==HAVING||LA44_1==ORDER||LA44_1==RPAREN||LA44_1==66) ) {
					alt44=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 44, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA44_0==103) ) {
				alt44=3;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 44, 0, input);
				throw nvae;
			}

			switch (alt44) {
				case 1 :
					// JPA2.g:205:7: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_groupby_item1777);
					path_expression140=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression140.getTree());

					}
					break;
				case 2 :
					// JPA2.g:205:25: identification_variable
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_identification_variable_in_groupby_item1781);
					identification_variable141=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable141.getTree());

					}
					break;
				case 3 :
					// JPA2.g:205:51: extract_function
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_extract_function_in_groupby_item1785);
					extract_function142=extract_function();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, extract_function142.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "groupby_item"


	public static class having_clause_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "having_clause"
	// JPA2.g:206:1: having_clause : 'HAVING' conditional_expression ;
	public final JPA2Parser.having_clause_return having_clause() throws RecognitionException {
		JPA2Parser.having_clause_return retval = new JPA2Parser.having_clause_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal143=null;
		ParserRuleReturnScope conditional_expression144 =null;

		Object string_literal143_tree=null;

		try {
			// JPA2.g:207:5: ( 'HAVING' conditional_expression )
			// JPA2.g:207:7: 'HAVING' conditional_expression
			{
			root_0 = (Object)adaptor.nil();


			string_literal143=(Token)match(input,HAVING,FOLLOW_HAVING_in_having_clause1796); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal143_tree = (Object)adaptor.create(string_literal143);
			adaptor.addChild(root_0, string_literal143_tree);
			}

			pushFollow(FOLLOW_conditional_expression_in_having_clause1798);
			conditional_expression144=conditional_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, conditional_expression144.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "having_clause"


	public static class orderby_clause_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "orderby_clause"
	// JPA2.g:208:1: orderby_clause : 'ORDER' 'BY' orderby_item ( ',' orderby_item )* -> ^( T_ORDER_BY[] 'ORDER' 'BY' ( orderby_item )* ) ;
	public final JPA2Parser.orderby_clause_return orderby_clause() throws RecognitionException {
		JPA2Parser.orderby_clause_return retval = new JPA2Parser.orderby_clause_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal145=null;
		Token string_literal146=null;
		Token char_literal148=null;
		ParserRuleReturnScope orderby_item147 =null;
		ParserRuleReturnScope orderby_item149 =null;

		Object string_literal145_tree=null;
		Object string_literal146_tree=null;
		Object char_literal148_tree=null;
		RewriteRuleTokenStream stream_66=new RewriteRuleTokenStream(adaptor,"token 66");
		RewriteRuleTokenStream stream_ORDER=new RewriteRuleTokenStream(adaptor,"token ORDER");
		RewriteRuleTokenStream stream_BY=new RewriteRuleTokenStream(adaptor,"token BY");
		RewriteRuleSubtreeStream stream_orderby_item=new RewriteRuleSubtreeStream(adaptor,"rule orderby_item");

		try {
			// JPA2.g:209:5: ( 'ORDER' 'BY' orderby_item ( ',' orderby_item )* -> ^( T_ORDER_BY[] 'ORDER' 'BY' ( orderby_item )* ) )
			// JPA2.g:209:7: 'ORDER' 'BY' orderby_item ( ',' orderby_item )*
			{
			string_literal145=(Token)match(input,ORDER,FOLLOW_ORDER_in_orderby_clause1809); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_ORDER.add(string_literal145);

			string_literal146=(Token)match(input,BY,FOLLOW_BY_in_orderby_clause1811); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_BY.add(string_literal146);

			pushFollow(FOLLOW_orderby_item_in_orderby_clause1813);
			orderby_item147=orderby_item();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_orderby_item.add(orderby_item147.getTree());
			// JPA2.g:209:33: ( ',' orderby_item )*
			loop45:
			while (true) {
				int alt45=2;
				int LA45_0 = input.LA(1);
				if ( (LA45_0==66) ) {
					alt45=1;
				}

				switch (alt45) {
				case 1 :
					// JPA2.g:209:34: ',' orderby_item
					{
					char_literal148=(Token)match(input,66,FOLLOW_66_in_orderby_clause1816); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_66.add(char_literal148);

					pushFollow(FOLLOW_orderby_item_in_orderby_clause1818);
					orderby_item149=orderby_item();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_orderby_item.add(orderby_item149.getTree());
					}
					break;

				default :
					break loop45;
				}
			}

			// AST REWRITE
			// elements: BY, ORDER, orderby_item
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 210:5: -> ^( T_ORDER_BY[] 'ORDER' 'BY' ( orderby_item )* )
			{
				// JPA2.g:210:8: ^( T_ORDER_BY[] 'ORDER' 'BY' ( orderby_item )* )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new OrderByNode(T_ORDER_BY), root_1);
				adaptor.addChild(root_1, stream_ORDER.nextNode());
				adaptor.addChild(root_1, stream_BY.nextNode());
				// JPA2.g:210:49: ( orderby_item )*
				while ( stream_orderby_item.hasNext() ) {
					adaptor.addChild(root_1, stream_orderby_item.nextTree());
				}
				stream_orderby_item.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "orderby_clause"


	public static class orderby_item_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "orderby_item"
	// JPA2.g:211:1: orderby_item : orderby_variable ( sort )? ( sortNulls )? -> ^( T_ORDER_BY_FIELD[] orderby_variable ( sort )? ( sortNulls )? ) ;
	public final JPA2Parser.orderby_item_return orderby_item() throws RecognitionException {
		JPA2Parser.orderby_item_return retval = new JPA2Parser.orderby_item_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope orderby_variable150 =null;
		ParserRuleReturnScope sort151 =null;
		ParserRuleReturnScope sortNulls152 =null;

		RewriteRuleSubtreeStream stream_sortNulls=new RewriteRuleSubtreeStream(adaptor,"rule sortNulls");
		RewriteRuleSubtreeStream stream_orderby_variable=new RewriteRuleSubtreeStream(adaptor,"rule orderby_variable");
		RewriteRuleSubtreeStream stream_sort=new RewriteRuleSubtreeStream(adaptor,"rule sort");

		try {
			// JPA2.g:212:5: ( orderby_variable ( sort )? ( sortNulls )? -> ^( T_ORDER_BY_FIELD[] orderby_variable ( sort )? ( sortNulls )? ) )
			// JPA2.g:212:7: orderby_variable ( sort )? ( sortNulls )?
			{
			pushFollow(FOLLOW_orderby_variable_in_orderby_item1852);
			orderby_variable150=orderby_variable();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_orderby_variable.add(orderby_variable150.getTree());
			// JPA2.g:212:24: ( sort )?
			int alt46=2;
			int LA46_0 = input.LA(1);
			if ( (LA46_0==ASC||LA46_0==DESC) ) {
				alt46=1;
			}
			switch (alt46) {
				case 1 :
					// JPA2.g:212:24: sort
					{
					pushFollow(FOLLOW_sort_in_orderby_item1854);
					sort151=sort();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_sort.add(sort151.getTree());
					}
					break;

			}

			// JPA2.g:212:30: ( sortNulls )?
			int alt47=2;
			int LA47_0 = input.LA(1);
			if ( ((LA47_0 >= 122 && LA47_0 <= 123)) ) {
				alt47=1;
			}
			switch (alt47) {
				case 1 :
					// JPA2.g:212:30: sortNulls
					{
					pushFollow(FOLLOW_sortNulls_in_orderby_item1857);
					sortNulls152=sortNulls();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_sortNulls.add(sortNulls152.getTree());
					}
					break;

			}

			// AST REWRITE
			// elements: orderby_variable, sortNulls, sort
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 213:6: -> ^( T_ORDER_BY_FIELD[] orderby_variable ( sort )? ( sortNulls )? )
			{
				// JPA2.g:213:9: ^( T_ORDER_BY_FIELD[] orderby_variable ( sort )? ( sortNulls )? )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new OrderByFieldNode(T_ORDER_BY_FIELD), root_1);
				adaptor.addChild(root_1, stream_orderby_variable.nextTree());
				// JPA2.g:213:65: ( sort )?
				if ( stream_sort.hasNext() ) {
					adaptor.addChild(root_1, stream_sort.nextTree());
				}
				stream_sort.reset();

				// JPA2.g:213:71: ( sortNulls )?
				if ( stream_sortNulls.hasNext() ) {
					adaptor.addChild(root_1, stream_sortNulls.nextTree());
				}
				stream_sortNulls.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "orderby_item"


	public static class orderby_variable_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "orderby_variable"
	// JPA2.g:214:1: orderby_variable : ( path_expression | general_identification_variable | result_variable | scalar_expression | aggregate_expression );
	public final JPA2Parser.orderby_variable_return orderby_variable() throws RecognitionException {
		JPA2Parser.orderby_variable_return retval = new JPA2Parser.orderby_variable_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope path_expression153 =null;
		ParserRuleReturnScope general_identification_variable154 =null;
		ParserRuleReturnScope result_variable155 =null;
		ParserRuleReturnScope scalar_expression156 =null;
		ParserRuleReturnScope aggregate_expression157 =null;


		try {
			// JPA2.g:215:5: ( path_expression | general_identification_variable | result_variable | scalar_expression | aggregate_expression )
			int alt48=5;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA48_1 = input.LA(2);
				if ( (synpred67_JPA2()) ) {
					alt48=1;
				}
				else if ( (synpred68_JPA2()) ) {
					alt48=2;
				}
				else if ( (synpred69_JPA2()) ) {
					alt48=3;
				}
				else if ( (synpred70_JPA2()) ) {
					alt48=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 48, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 109:
			case 142:
				{
				alt48=2;
				}
				break;
			case GROUP:
				{
				int LA48_4 = input.LA(2);
				if ( (synpred67_JPA2()) ) {
					alt48=1;
				}
				else if ( (synpred68_JPA2()) ) {
					alt48=2;
				}
				else if ( (synpred70_JPA2()) ) {
					alt48=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 48, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case CASE:
			case INT_NUMERAL:
			case LOWER:
			case LPAREN:
			case NAMED_PARAMETER:
			case STRING_LITERAL:
			case 63:
			case 65:
			case 67:
			case 70:
			case 77:
			case 83:
			case 85:
			case 90:
			case 91:
			case 92:
			case 93:
			case 94:
			case 95:
			case 103:
			case 107:
			case 111:
			case 113:
			case 116:
			case 121:
			case 131:
			case 133:
			case 134:
			case 137:
			case 138:
			case 140:
			case 146:
			case 147:
				{
				alt48=4;
				}
				break;
			case COUNT:
				{
				int LA48_19 = input.LA(2);
				if ( (synpred70_JPA2()) ) {
					alt48=4;
				}
				else if ( (true) ) {
					alt48=5;
				}

				}
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
				{
				int LA48_20 = input.LA(2);
				if ( (synpred70_JPA2()) ) {
					alt48=4;
				}
				else if ( (true) ) {
					alt48=5;
				}

				}
				break;
			case 105:
				{
				int LA48_21 = input.LA(2);
				if ( (synpred70_JPA2()) ) {
					alt48=4;
				}
				else if ( (true) ) {
					alt48=5;
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 48, 0, input);
				throw nvae;
			}
			switch (alt48) {
				case 1 :
					// JPA2.g:215:7: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_orderby_variable1892);
					path_expression153=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression153.getTree());

					}
					break;
				case 2 :
					// JPA2.g:215:25: general_identification_variable
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_general_identification_variable_in_orderby_variable1896);
					general_identification_variable154=general_identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, general_identification_variable154.getTree());

					}
					break;
				case 3 :
					// JPA2.g:215:59: result_variable
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_result_variable_in_orderby_variable1900);
					result_variable155=result_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, result_variable155.getTree());

					}
					break;
				case 4 :
					// JPA2.g:215:77: scalar_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_scalar_expression_in_orderby_variable1904);
					scalar_expression156=scalar_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression156.getTree());

					}
					break;
				case 5 :
					// JPA2.g:215:97: aggregate_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_aggregate_expression_in_orderby_variable1908);
					aggregate_expression157=aggregate_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, aggregate_expression157.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "orderby_variable"


	public static class sort_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "sort"
	// JPA2.g:216:1: sort : ( 'ASC' | 'DESC' ) ;
	public final JPA2Parser.sort_return sort() throws RecognitionException {
		JPA2Parser.sort_return retval = new JPA2Parser.sort_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set158=null;

		Object set158_tree=null;

		try {
			// JPA2.g:217:5: ( ( 'ASC' | 'DESC' ) )
			// JPA2.g:
			{
			root_0 = (Object)adaptor.nil();


			set158=input.LT(1);
			if ( input.LA(1)==ASC||input.LA(1)==DESC ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set158));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "sort"


	public static class sortNulls_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "sortNulls"
	// JPA2.g:218:1: sortNulls : ( 'NULLS FIRST' | 'NULLS LAST' ) ;
	public final JPA2Parser.sortNulls_return sortNulls() throws RecognitionException {
		JPA2Parser.sortNulls_return retval = new JPA2Parser.sortNulls_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set159=null;

		Object set159_tree=null;

		try {
			// JPA2.g:219:5: ( ( 'NULLS FIRST' | 'NULLS LAST' ) )
			// JPA2.g:
			{
			root_0 = (Object)adaptor.nil();


			set159=input.LT(1);
			if ( (input.LA(1) >= 122 && input.LA(1) <= 123) ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set159));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "sortNulls"


	public static class subquery_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "subquery"
	// JPA2.g:220:1: subquery : lp= '(' 'SELECT' simple_select_clause subquery_from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? rp= ')' -> ^( T_QUERY[$lp,$rp] 'SELECT' simple_select_clause subquery_from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? ) ;
	public final JPA2Parser.subquery_return subquery() throws RecognitionException {
		JPA2Parser.subquery_return retval = new JPA2Parser.subquery_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token lp=null;
		Token rp=null;
		Token string_literal160=null;
		ParserRuleReturnScope simple_select_clause161 =null;
		ParserRuleReturnScope subquery_from_clause162 =null;
		ParserRuleReturnScope where_clause163 =null;
		ParserRuleReturnScope groupby_clause164 =null;
		ParserRuleReturnScope having_clause165 =null;

		Object lp_tree=null;
		Object rp_tree=null;
		Object string_literal160_tree=null;
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_130=new RewriteRuleTokenStream(adaptor,"token 130");
		RewriteRuleSubtreeStream stream_where_clause=new RewriteRuleSubtreeStream(adaptor,"rule where_clause");
		RewriteRuleSubtreeStream stream_subquery_from_clause=new RewriteRuleSubtreeStream(adaptor,"rule subquery_from_clause");
		RewriteRuleSubtreeStream stream_simple_select_clause=new RewriteRuleSubtreeStream(adaptor,"rule simple_select_clause");
		RewriteRuleSubtreeStream stream_groupby_clause=new RewriteRuleSubtreeStream(adaptor,"rule groupby_clause");
		RewriteRuleSubtreeStream stream_having_clause=new RewriteRuleSubtreeStream(adaptor,"rule having_clause");

		try {
			// JPA2.g:221:5: (lp= '(' 'SELECT' simple_select_clause subquery_from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? rp= ')' -> ^( T_QUERY[$lp,$rp] 'SELECT' simple_select_clause subquery_from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? ) )
			// JPA2.g:221:7: lp= '(' 'SELECT' simple_select_clause subquery_from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? rp= ')'
			{
			lp=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_subquery1955); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LPAREN.add(lp);

			string_literal160=(Token)match(input,130,FOLLOW_130_in_subquery1957); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_130.add(string_literal160);

			pushFollow(FOLLOW_simple_select_clause_in_subquery1959);
			simple_select_clause161=simple_select_clause();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_simple_select_clause.add(simple_select_clause161.getTree());
			pushFollow(FOLLOW_subquery_from_clause_in_subquery1961);
			subquery_from_clause162=subquery_from_clause();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_subquery_from_clause.add(subquery_from_clause162.getTree());
			// JPA2.g:221:65: ( where_clause )?
			int alt49=2;
			int LA49_0 = input.LA(1);
			if ( (LA49_0==144) ) {
				alt49=1;
			}
			switch (alt49) {
				case 1 :
					// JPA2.g:221:66: where_clause
					{
					pushFollow(FOLLOW_where_clause_in_subquery1964);
					where_clause163=where_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_where_clause.add(where_clause163.getTree());
					}
					break;

			}

			// JPA2.g:221:81: ( groupby_clause )?
			int alt50=2;
			int LA50_0 = input.LA(1);
			if ( (LA50_0==GROUP) ) {
				alt50=1;
			}
			switch (alt50) {
				case 1 :
					// JPA2.g:221:82: groupby_clause
					{
					pushFollow(FOLLOW_groupby_clause_in_subquery1969);
					groupby_clause164=groupby_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_groupby_clause.add(groupby_clause164.getTree());
					}
					break;

			}

			// JPA2.g:221:99: ( having_clause )?
			int alt51=2;
			int LA51_0 = input.LA(1);
			if ( (LA51_0==HAVING) ) {
				alt51=1;
			}
			switch (alt51) {
				case 1 :
					// JPA2.g:221:100: having_clause
					{
					pushFollow(FOLLOW_having_clause_in_subquery1974);
					having_clause165=having_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_having_clause.add(having_clause165.getTree());
					}
					break;

			}

			rp=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_subquery1980); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RPAREN.add(rp);

			// AST REWRITE
			// elements: subquery_from_clause, groupby_clause, having_clause, where_clause, 130, simple_select_clause
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 222:6: -> ^( T_QUERY[$lp,$rp] 'SELECT' simple_select_clause subquery_from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? )
			{
				// JPA2.g:222:9: ^( T_QUERY[$lp,$rp] 'SELECT' simple_select_clause subquery_from_clause ( where_clause )? ( groupby_clause )? ( having_clause )? )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new QueryNode(T_QUERY, lp, rp), root_1);
				adaptor.addChild(root_1, stream_130.nextNode());
				adaptor.addChild(root_1, stream_simple_select_clause.nextTree());
				adaptor.addChild(root_1, stream_subquery_from_clause.nextTree());
				// JPA2.g:222:90: ( where_clause )?
				if ( stream_where_clause.hasNext() ) {
					adaptor.addChild(root_1, stream_where_clause.nextTree());
				}
				stream_where_clause.reset();

				// JPA2.g:222:106: ( groupby_clause )?
				if ( stream_groupby_clause.hasNext() ) {
					adaptor.addChild(root_1, stream_groupby_clause.nextTree());
				}
				stream_groupby_clause.reset();

				// JPA2.g:222:124: ( having_clause )?
				if ( stream_having_clause.hasNext() ) {
					adaptor.addChild(root_1, stream_having_clause.nextTree());
				}
				stream_having_clause.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "subquery"


	public static class subquery_from_clause_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "subquery_from_clause"
	// JPA2.g:223:1: subquery_from_clause : fr= 'FROM' subselect_identification_variable_declaration ( ',' subselect_identification_variable_declaration )* -> ^( T_SOURCES[$fr] ( ^( T_SOURCE subselect_identification_variable_declaration ) )* ) ;
	public final JPA2Parser.subquery_from_clause_return subquery_from_clause() throws RecognitionException {
		JPA2Parser.subquery_from_clause_return retval = new JPA2Parser.subquery_from_clause_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token fr=null;
		Token char_literal167=null;
		ParserRuleReturnScope subselect_identification_variable_declaration166 =null;
		ParserRuleReturnScope subselect_identification_variable_declaration168 =null;

		Object fr_tree=null;
		Object char_literal167_tree=null;
		RewriteRuleTokenStream stream_66=new RewriteRuleTokenStream(adaptor,"token 66");
		RewriteRuleTokenStream stream_104=new RewriteRuleTokenStream(adaptor,"token 104");
		RewriteRuleSubtreeStream stream_subselect_identification_variable_declaration=new RewriteRuleSubtreeStream(adaptor,"rule subselect_identification_variable_declaration");

		try {
			// JPA2.g:224:5: (fr= 'FROM' subselect_identification_variable_declaration ( ',' subselect_identification_variable_declaration )* -> ^( T_SOURCES[$fr] ( ^( T_SOURCE subselect_identification_variable_declaration ) )* ) )
			// JPA2.g:224:7: fr= 'FROM' subselect_identification_variable_declaration ( ',' subselect_identification_variable_declaration )*
			{
			fr=(Token)match(input,104,FOLLOW_104_in_subquery_from_clause2030); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_104.add(fr);

			pushFollow(FOLLOW_subselect_identification_variable_declaration_in_subquery_from_clause2032);
			subselect_identification_variable_declaration166=subselect_identification_variable_declaration();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_subselect_identification_variable_declaration.add(subselect_identification_variable_declaration166.getTree());
			// JPA2.g:224:63: ( ',' subselect_identification_variable_declaration )*
			loop52:
			while (true) {
				int alt52=2;
				int LA52_0 = input.LA(1);
				if ( (LA52_0==66) ) {
					alt52=1;
				}

				switch (alt52) {
				case 1 :
					// JPA2.g:224:64: ',' subselect_identification_variable_declaration
					{
					char_literal167=(Token)match(input,66,FOLLOW_66_in_subquery_from_clause2035); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_66.add(char_literal167);

					pushFollow(FOLLOW_subselect_identification_variable_declaration_in_subquery_from_clause2037);
					subselect_identification_variable_declaration168=subselect_identification_variable_declaration();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_subselect_identification_variable_declaration.add(subselect_identification_variable_declaration168.getTree());
					}
					break;

				default :
					break loop52;
				}
			}

			// AST REWRITE
			// elements: subselect_identification_variable_declaration
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 225:5: -> ^( T_SOURCES[$fr] ( ^( T_SOURCE subselect_identification_variable_declaration ) )* )
			{
				// JPA2.g:225:8: ^( T_SOURCES[$fr] ( ^( T_SOURCE subselect_identification_variable_declaration ) )* )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new FromNode(T_SOURCES, fr), root_1);
				// JPA2.g:225:35: ( ^( T_SOURCE subselect_identification_variable_declaration ) )*
				while ( stream_subselect_identification_variable_declaration.hasNext() ) {
					// JPA2.g:225:35: ^( T_SOURCE subselect_identification_variable_declaration )
					{
					Object root_2 = (Object)adaptor.nil();
					root_2 = (Object)adaptor.becomeRoot(new SelectionSourceNode(T_SOURCE), root_2);
					adaptor.addChild(root_2, stream_subselect_identification_variable_declaration.nextTree());
					adaptor.addChild(root_1, root_2);
					}

				}
				stream_subselect_identification_variable_declaration.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "subquery_from_clause"


	public static class subselect_identification_variable_declaration_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "subselect_identification_variable_declaration"
	// JPA2.g:227:1: subselect_identification_variable_declaration : ( identification_variable_declaration | derived_path_expression ( AS )? identification_variable ( join )* | derived_collection_member_declaration );
	public final JPA2Parser.subselect_identification_variable_declaration_return subselect_identification_variable_declaration() throws RecognitionException {
		JPA2Parser.subselect_identification_variable_declaration_return retval = new JPA2Parser.subselect_identification_variable_declaration_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token AS171=null;
		ParserRuleReturnScope identification_variable_declaration169 =null;
		ParserRuleReturnScope derived_path_expression170 =null;
		ParserRuleReturnScope identification_variable172 =null;
		ParserRuleReturnScope join173 =null;
		ParserRuleReturnScope derived_collection_member_declaration174 =null;

		Object AS171_tree=null;

		try {
			// JPA2.g:228:5: ( identification_variable_declaration | derived_path_expression ( AS )? identification_variable ( join )* | derived_collection_member_declaration )
			int alt55=3;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA55_1 = input.LA(2);
				if ( (LA55_1==AS||LA55_1==GROUP||LA55_1==WORD) ) {
					alt55=1;
				}
				else if ( (LA55_1==68) ) {
					alt55=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 55, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 136:
				{
				alt55=2;
				}
				break;
			case IN:
				{
				alt55=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 55, 0, input);
				throw nvae;
			}
			switch (alt55) {
				case 1 :
					// JPA2.g:228:7: identification_variable_declaration
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_identification_variable_declaration_in_subselect_identification_variable_declaration2075);
					identification_variable_declaration169=identification_variable_declaration();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable_declaration169.getTree());

					}
					break;
				case 2 :
					// JPA2.g:229:7: derived_path_expression ( AS )? identification_variable ( join )*
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_derived_path_expression_in_subselect_identification_variable_declaration2083);
					derived_path_expression170=derived_path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, derived_path_expression170.getTree());

					// JPA2.g:229:31: ( AS )?
					int alt53=2;
					int LA53_0 = input.LA(1);
					if ( (LA53_0==AS) ) {
						alt53=1;
					}
					switch (alt53) {
						case 1 :
							// JPA2.g:229:32: AS
							{
							AS171=(Token)match(input,AS,FOLLOW_AS_in_subselect_identification_variable_declaration2086); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							AS171_tree = (Object)adaptor.create(AS171);
							adaptor.addChild(root_0, AS171_tree);
							}

							}
							break;

					}

					pushFollow(FOLLOW_identification_variable_in_subselect_identification_variable_declaration2090);
					identification_variable172=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable172.getTree());

					// JPA2.g:229:61: ( join )*
					loop54:
					while (true) {
						int alt54=2;
						int LA54_0 = input.LA(1);
						if ( (LA54_0==INNER||(LA54_0 >= JOIN && LA54_0 <= LEFT)) ) {
							alt54=1;
						}

						switch (alt54) {
						case 1 :
							// JPA2.g:229:62: join
							{
							pushFollow(FOLLOW_join_in_subselect_identification_variable_declaration2093);
							join173=join();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, join173.getTree());

							}
							break;

						default :
							break loop54;
						}
					}

					}
					break;
				case 3 :
					// JPA2.g:230:7: derived_collection_member_declaration
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_derived_collection_member_declaration_in_subselect_identification_variable_declaration2103);
					derived_collection_member_declaration174=derived_collection_member_declaration();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, derived_collection_member_declaration174.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "subselect_identification_variable_declaration"


	public static class derived_path_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "derived_path_expression"
	// JPA2.g:231:1: derived_path_expression : ( general_derived_path '.' single_valued_object_field | general_derived_path '.' collection_valued_field );
	public final JPA2Parser.derived_path_expression_return derived_path_expression() throws RecognitionException {
		JPA2Parser.derived_path_expression_return retval = new JPA2Parser.derived_path_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal176=null;
		Token char_literal179=null;
		ParserRuleReturnScope general_derived_path175 =null;
		ParserRuleReturnScope single_valued_object_field177 =null;
		ParserRuleReturnScope general_derived_path178 =null;
		ParserRuleReturnScope collection_valued_field180 =null;

		Object char_literal176_tree=null;
		Object char_literal179_tree=null;

		try {
			// JPA2.g:232:5: ( general_derived_path '.' single_valued_object_field | general_derived_path '.' collection_valued_field )
			int alt56=2;
			int LA56_0 = input.LA(1);
			if ( (LA56_0==WORD) ) {
				int LA56_1 = input.LA(2);
				if ( (synpred81_JPA2()) ) {
					alt56=1;
				}
				else if ( (true) ) {
					alt56=2;
				}

			}
			else if ( (LA56_0==136) ) {
				int LA56_2 = input.LA(2);
				if ( (synpred81_JPA2()) ) {
					alt56=1;
				}
				else if ( (true) ) {
					alt56=2;
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 56, 0, input);
				throw nvae;
			}

			switch (alt56) {
				case 1 :
					// JPA2.g:232:7: general_derived_path '.' single_valued_object_field
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_general_derived_path_in_derived_path_expression2114);
					general_derived_path175=general_derived_path();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, general_derived_path175.getTree());

					char_literal176=(Token)match(input,68,FOLLOW_68_in_derived_path_expression2115); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal176_tree = (Object)adaptor.create(char_literal176);
					adaptor.addChild(root_0, char_literal176_tree);
					}

					pushFollow(FOLLOW_single_valued_object_field_in_derived_path_expression2116);
					single_valued_object_field177=single_valued_object_field();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, single_valued_object_field177.getTree());

					}
					break;
				case 2 :
					// JPA2.g:233:7: general_derived_path '.' collection_valued_field
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_general_derived_path_in_derived_path_expression2124);
					general_derived_path178=general_derived_path();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, general_derived_path178.getTree());

					char_literal179=(Token)match(input,68,FOLLOW_68_in_derived_path_expression2125); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal179_tree = (Object)adaptor.create(char_literal179);
					adaptor.addChild(root_0, char_literal179_tree);
					}

					pushFollow(FOLLOW_collection_valued_field_in_derived_path_expression2126);
					collection_valued_field180=collection_valued_field();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, collection_valued_field180.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "derived_path_expression"


	public static class general_derived_path_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "general_derived_path"
	// JPA2.g:234:1: general_derived_path : ( simple_derived_path | treated_derived_path ( '.' single_valued_object_field )* );
	public final JPA2Parser.general_derived_path_return general_derived_path() throws RecognitionException {
		JPA2Parser.general_derived_path_return retval = new JPA2Parser.general_derived_path_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal183=null;
		ParserRuleReturnScope simple_derived_path181 =null;
		ParserRuleReturnScope treated_derived_path182 =null;
		ParserRuleReturnScope single_valued_object_field184 =null;

		Object char_literal183_tree=null;

		try {
			// JPA2.g:235:5: ( simple_derived_path | treated_derived_path ( '.' single_valued_object_field )* )
			int alt58=2;
			int LA58_0 = input.LA(1);
			if ( (LA58_0==WORD) ) {
				alt58=1;
			}
			else if ( (LA58_0==136) ) {
				alt58=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 58, 0, input);
				throw nvae;
			}

			switch (alt58) {
				case 1 :
					// JPA2.g:235:7: simple_derived_path
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_simple_derived_path_in_general_derived_path2137);
					simple_derived_path181=simple_derived_path();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, simple_derived_path181.getTree());

					}
					break;
				case 2 :
					// JPA2.g:236:7: treated_derived_path ( '.' single_valued_object_field )*
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_treated_derived_path_in_general_derived_path2145);
					treated_derived_path182=treated_derived_path();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, treated_derived_path182.getTree());

					// JPA2.g:236:27: ( '.' single_valued_object_field )*
					loop57:
					while (true) {
						int alt57=2;
						int LA57_0 = input.LA(1);
						if ( (LA57_0==68) ) {
							int LA57_1 = input.LA(2);
							if ( (LA57_1==WORD) ) {
								int LA57_3 = input.LA(3);
								if ( (LA57_3==AS) ) {
									int LA57_4 = input.LA(4);
									if ( (LA57_4==WORD) ) {
										int LA57_6 = input.LA(5);
										if ( (LA57_6==RPAREN) ) {
											int LA57_7 = input.LA(6);
											if ( (LA57_7==AS) ) {
												int LA57_8 = input.LA(7);
												if ( (LA57_8==WORD) ) {
													int LA57_9 = input.LA(8);
													if ( (LA57_9==RPAREN) ) {
														alt57=1;
													}

												}

											}
											else if ( (LA57_7==68) ) {
												alt57=1;
											}

										}

									}

								}
								else if ( (LA57_3==68) ) {
									alt57=1;
								}

							}

						}

						switch (alt57) {
						case 1 :
							// JPA2.g:236:28: '.' single_valued_object_field
							{
							char_literal183=(Token)match(input,68,FOLLOW_68_in_general_derived_path2147); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							char_literal183_tree = (Object)adaptor.create(char_literal183);
							adaptor.addChild(root_0, char_literal183_tree);
							}

							pushFollow(FOLLOW_single_valued_object_field_in_general_derived_path2148);
							single_valued_object_field184=single_valued_object_field();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, single_valued_object_field184.getTree());

							}
							break;

						default :
							break loop57;
						}
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "general_derived_path"


	public static class simple_derived_path_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "simple_derived_path"
	// JPA2.g:238:1: simple_derived_path : superquery_identification_variable ;
	public final JPA2Parser.simple_derived_path_return simple_derived_path() throws RecognitionException {
		JPA2Parser.simple_derived_path_return retval = new JPA2Parser.simple_derived_path_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope superquery_identification_variable185 =null;


		try {
			// JPA2.g:239:5: ( superquery_identification_variable )
			// JPA2.g:239:7: superquery_identification_variable
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_superquery_identification_variable_in_simple_derived_path2166);
			superquery_identification_variable185=superquery_identification_variable();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, superquery_identification_variable185.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "simple_derived_path"


	public static class treated_derived_path_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "treated_derived_path"
	// JPA2.g:241:1: treated_derived_path : 'TREAT(' general_derived_path AS subtype ')' ;
	public final JPA2Parser.treated_derived_path_return treated_derived_path() throws RecognitionException {
		JPA2Parser.treated_derived_path_return retval = new JPA2Parser.treated_derived_path_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal186=null;
		Token AS188=null;
		Token char_literal190=null;
		ParserRuleReturnScope general_derived_path187 =null;
		ParserRuleReturnScope subtype189 =null;

		Object string_literal186_tree=null;
		Object AS188_tree=null;
		Object char_literal190_tree=null;

		try {
			// JPA2.g:242:5: ( 'TREAT(' general_derived_path AS subtype ')' )
			// JPA2.g:242:7: 'TREAT(' general_derived_path AS subtype ')'
			{
			root_0 = (Object)adaptor.nil();


			string_literal186=(Token)match(input,136,FOLLOW_136_in_treated_derived_path2183); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal186_tree = (Object)adaptor.create(string_literal186);
			adaptor.addChild(root_0, string_literal186_tree);
			}

			pushFollow(FOLLOW_general_derived_path_in_treated_derived_path2184);
			general_derived_path187=general_derived_path();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, general_derived_path187.getTree());

			AS188=(Token)match(input,AS,FOLLOW_AS_in_treated_derived_path2186); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			AS188_tree = (Object)adaptor.create(AS188);
			adaptor.addChild(root_0, AS188_tree);
			}

			pushFollow(FOLLOW_subtype_in_treated_derived_path2188);
			subtype189=subtype();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, subtype189.getTree());

			char_literal190=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_treated_derived_path2190); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal190_tree = (Object)adaptor.create(char_literal190);
			adaptor.addChild(root_0, char_literal190_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "treated_derived_path"


	public static class derived_collection_member_declaration_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "derived_collection_member_declaration"
	// JPA2.g:243:1: derived_collection_member_declaration : 'IN' superquery_identification_variable '.' ( single_valued_object_field '.' )* collection_valued_field ;
	public final JPA2Parser.derived_collection_member_declaration_return derived_collection_member_declaration() throws RecognitionException {
		JPA2Parser.derived_collection_member_declaration_return retval = new JPA2Parser.derived_collection_member_declaration_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal191=null;
		Token char_literal193=null;
		Token char_literal195=null;
		ParserRuleReturnScope superquery_identification_variable192 =null;
		ParserRuleReturnScope single_valued_object_field194 =null;
		ParserRuleReturnScope collection_valued_field196 =null;

		Object string_literal191_tree=null;
		Object char_literal193_tree=null;
		Object char_literal195_tree=null;

		try {
			// JPA2.g:244:5: ( 'IN' superquery_identification_variable '.' ( single_valued_object_field '.' )* collection_valued_field )
			// JPA2.g:244:7: 'IN' superquery_identification_variable '.' ( single_valued_object_field '.' )* collection_valued_field
			{
			root_0 = (Object)adaptor.nil();


			string_literal191=(Token)match(input,IN,FOLLOW_IN_in_derived_collection_member_declaration2201); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal191_tree = (Object)adaptor.create(string_literal191);
			adaptor.addChild(root_0, string_literal191_tree);
			}

			pushFollow(FOLLOW_superquery_identification_variable_in_derived_collection_member_declaration2203);
			superquery_identification_variable192=superquery_identification_variable();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, superquery_identification_variable192.getTree());

			char_literal193=(Token)match(input,68,FOLLOW_68_in_derived_collection_member_declaration2204); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal193_tree = (Object)adaptor.create(char_literal193);
			adaptor.addChild(root_0, char_literal193_tree);
			}

			// JPA2.g:244:49: ( single_valued_object_field '.' )*
			loop59:
			while (true) {
				int alt59=2;
				int LA59_0 = input.LA(1);
				if ( (LA59_0==WORD) ) {
					int LA59_1 = input.LA(2);
					if ( (LA59_1==68) ) {
						alt59=1;
					}

				}

				switch (alt59) {
				case 1 :
					// JPA2.g:244:50: single_valued_object_field '.'
					{
					pushFollow(FOLLOW_single_valued_object_field_in_derived_collection_member_declaration2206);
					single_valued_object_field194=single_valued_object_field();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, single_valued_object_field194.getTree());

					char_literal195=(Token)match(input,68,FOLLOW_68_in_derived_collection_member_declaration2208); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal195_tree = (Object)adaptor.create(char_literal195);
					adaptor.addChild(root_0, char_literal195_tree);
					}

					}
					break;

				default :
					break loop59;
				}
			}

			pushFollow(FOLLOW_collection_valued_field_in_derived_collection_member_declaration2211);
			collection_valued_field196=collection_valued_field();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, collection_valued_field196.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "derived_collection_member_declaration"


	public static class simple_select_clause_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "simple_select_clause"
	// JPA2.g:246:1: simple_select_clause : ( 'DISTINCT' )? simple_select_expression -> ^( T_SELECTED_ITEMS[] ^( T_SELECTED_ITEM[] ( 'DISTINCT' )? simple_select_expression ) ) ;
	public final JPA2Parser.simple_select_clause_return simple_select_clause() throws RecognitionException {
		JPA2Parser.simple_select_clause_return retval = new JPA2Parser.simple_select_clause_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal197=null;
		ParserRuleReturnScope simple_select_expression198 =null;

		Object string_literal197_tree=null;
		RewriteRuleTokenStream stream_DISTINCT=new RewriteRuleTokenStream(adaptor,"token DISTINCT");
		RewriteRuleSubtreeStream stream_simple_select_expression=new RewriteRuleSubtreeStream(adaptor,"rule simple_select_expression");

		try {
			// JPA2.g:247:5: ( ( 'DISTINCT' )? simple_select_expression -> ^( T_SELECTED_ITEMS[] ^( T_SELECTED_ITEM[] ( 'DISTINCT' )? simple_select_expression ) ) )
			// JPA2.g:247:7: ( 'DISTINCT' )? simple_select_expression
			{
			// JPA2.g:247:7: ( 'DISTINCT' )?
			int alt60=2;
			int LA60_0 = input.LA(1);
			if ( (LA60_0==DISTINCT) ) {
				alt60=1;
			}
			switch (alt60) {
				case 1 :
					// JPA2.g:247:8: 'DISTINCT'
					{
					string_literal197=(Token)match(input,DISTINCT,FOLLOW_DISTINCT_in_simple_select_clause2224); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DISTINCT.add(string_literal197);

					}
					break;

			}

			pushFollow(FOLLOW_simple_select_expression_in_simple_select_clause2228);
			simple_select_expression198=simple_select_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_simple_select_expression.add(simple_select_expression198.getTree());
			// AST REWRITE
			// elements: DISTINCT, simple_select_expression
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 248:5: -> ^( T_SELECTED_ITEMS[] ^( T_SELECTED_ITEM[] ( 'DISTINCT' )? simple_select_expression ) )
			{
				// JPA2.g:248:8: ^( T_SELECTED_ITEMS[] ^( T_SELECTED_ITEM[] ( 'DISTINCT' )? simple_select_expression ) )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new SelectedItemsNode(T_SELECTED_ITEMS), root_1);
				// JPA2.g:248:48: ^( T_SELECTED_ITEM[] ( 'DISTINCT' )? simple_select_expression )
				{
				Object root_2 = (Object)adaptor.nil();
				root_2 = (Object)adaptor.becomeRoot(new SelectedItemNode(T_SELECTED_ITEM), root_2);
				// JPA2.g:248:86: ( 'DISTINCT' )?
				if ( stream_DISTINCT.hasNext() ) {
					adaptor.addChild(root_2, stream_DISTINCT.nextNode());
				}
				stream_DISTINCT.reset();

				adaptor.addChild(root_2, stream_simple_select_expression.nextTree());
				adaptor.addChild(root_1, root_2);
				}

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "simple_select_clause"


	public static class simple_select_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "simple_select_expression"
	// JPA2.g:249:1: simple_select_expression : ( path_expression | scalar_expression | aggregate_expression | identification_variable );
	public final JPA2Parser.simple_select_expression_return simple_select_expression() throws RecognitionException {
		JPA2Parser.simple_select_expression_return retval = new JPA2Parser.simple_select_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope path_expression199 =null;
		ParserRuleReturnScope scalar_expression200 =null;
		ParserRuleReturnScope aggregate_expression201 =null;
		ParserRuleReturnScope identification_variable202 =null;


		try {
			// JPA2.g:250:5: ( path_expression | scalar_expression | aggregate_expression | identification_variable )
			int alt61=4;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA61_1 = input.LA(2);
				if ( (synpred86_JPA2()) ) {
					alt61=1;
				}
				else if ( (synpred87_JPA2()) ) {
					alt61=2;
				}
				else if ( (true) ) {
					alt61=4;
				}

				}
				break;
			case CASE:
			case INT_NUMERAL:
			case LOWER:
			case LPAREN:
			case NAMED_PARAMETER:
			case STRING_LITERAL:
			case 63:
			case 65:
			case 67:
			case 70:
			case 77:
			case 83:
			case 85:
			case 90:
			case 91:
			case 92:
			case 93:
			case 94:
			case 95:
			case 103:
			case 107:
			case 111:
			case 113:
			case 116:
			case 121:
			case 131:
			case 133:
			case 134:
			case 137:
			case 138:
			case 140:
			case 146:
			case 147:
				{
				alt61=2;
				}
				break;
			case COUNT:
				{
				int LA61_16 = input.LA(2);
				if ( (synpred87_JPA2()) ) {
					alt61=2;
				}
				else if ( (synpred88_JPA2()) ) {
					alt61=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 61, 16, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
				{
				int LA61_17 = input.LA(2);
				if ( (synpred87_JPA2()) ) {
					alt61=2;
				}
				else if ( (synpred88_JPA2()) ) {
					alt61=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 61, 17, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 105:
				{
				int LA61_18 = input.LA(2);
				if ( (synpred87_JPA2()) ) {
					alt61=2;
				}
				else if ( (synpred88_JPA2()) ) {
					alt61=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 61, 18, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case GROUP:
				{
				int LA61_31 = input.LA(2);
				if ( (synpred86_JPA2()) ) {
					alt61=1;
				}
				else if ( (synpred87_JPA2()) ) {
					alt61=2;
				}
				else if ( (true) ) {
					alt61=4;
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 61, 0, input);
				throw nvae;
			}
			switch (alt61) {
				case 1 :
					// JPA2.g:250:7: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_simple_select_expression2268);
					path_expression199=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression199.getTree());

					}
					break;
				case 2 :
					// JPA2.g:251:7: scalar_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_scalar_expression_in_simple_select_expression2276);
					scalar_expression200=scalar_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression200.getTree());

					}
					break;
				case 3 :
					// JPA2.g:252:7: aggregate_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_aggregate_expression_in_simple_select_expression2284);
					aggregate_expression201=aggregate_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, aggregate_expression201.getTree());

					}
					break;
				case 4 :
					// JPA2.g:253:7: identification_variable
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_identification_variable_in_simple_select_expression2292);
					identification_variable202=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable202.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "simple_select_expression"


	public static class scalar_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "scalar_expression"
	// JPA2.g:254:1: scalar_expression : ( arithmetic_expression | string_expression | enum_expression | datetime_expression | boolean_expression | case_expression | entity_type_expression );
	public final JPA2Parser.scalar_expression_return scalar_expression() throws RecognitionException {
		JPA2Parser.scalar_expression_return retval = new JPA2Parser.scalar_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope arithmetic_expression203 =null;
		ParserRuleReturnScope string_expression204 =null;
		ParserRuleReturnScope enum_expression205 =null;
		ParserRuleReturnScope datetime_expression206 =null;
		ParserRuleReturnScope boolean_expression207 =null;
		ParserRuleReturnScope case_expression208 =null;
		ParserRuleReturnScope entity_type_expression209 =null;


		try {
			// JPA2.g:255:5: ( arithmetic_expression | string_expression | enum_expression | datetime_expression | boolean_expression | case_expression | entity_type_expression )
			int alt62=7;
			switch ( input.LA(1) ) {
			case INT_NUMERAL:
			case 65:
			case 67:
			case 70:
			case 85:
			case 107:
			case 111:
			case 113:
			case 116:
			case 131:
			case 133:
				{
				alt62=1;
				}
				break;
			case WORD:
				{
				int LA62_2 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred91_JPA2()) ) {
					alt62=3;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}
				else if ( (synpred93_JPA2()) ) {
					alt62=5;
				}
				else if ( (true) ) {
					alt62=7;
				}

				}
				break;
			case LPAREN:
				{
				int LA62_5 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred91_JPA2()) ) {
					alt62=3;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}
				else if ( (synpred93_JPA2()) ) {
					alt62=5;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 62, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 77:
				{
				int LA62_6 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred91_JPA2()) ) {
					alt62=3;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}
				else if ( (synpred93_JPA2()) ) {
					alt62=5;
				}
				else if ( (true) ) {
					alt62=7;
				}

				}
				break;
			case NAMED_PARAMETER:
				{
				int LA62_7 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred91_JPA2()) ) {
					alt62=3;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}
				else if ( (synpred93_JPA2()) ) {
					alt62=5;
				}
				else if ( (true) ) {
					alt62=7;
				}

				}
				break;
			case 63:
				{
				int LA62_8 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred91_JPA2()) ) {
					alt62=3;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}
				else if ( (synpred93_JPA2()) ) {
					alt62=5;
				}
				else if ( (true) ) {
					alt62=7;
				}

				}
				break;
			case COUNT:
				{
				int LA62_16 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 62, 16, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
				{
				int LA62_17 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 62, 17, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 105:
				{
				int LA62_18 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}
				else if ( (synpred93_JPA2()) ) {
					alt62=5;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 62, 18, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case CASE:
				{
				int LA62_19 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred91_JPA2()) ) {
					alt62=3;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}
				else if ( (synpred93_JPA2()) ) {
					alt62=5;
				}
				else if ( (synpred94_JPA2()) ) {
					alt62=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 62, 19, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 91:
				{
				int LA62_20 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred91_JPA2()) ) {
					alt62=3;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}
				else if ( (synpred93_JPA2()) ) {
					alt62=5;
				}
				else if ( (synpred94_JPA2()) ) {
					alt62=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 62, 20, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 121:
				{
				int LA62_21 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred91_JPA2()) ) {
					alt62=3;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}
				else if ( (synpred93_JPA2()) ) {
					alt62=5;
				}
				else if ( (synpred94_JPA2()) ) {
					alt62=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 62, 21, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 90:
				{
				int LA62_22 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}
				else if ( (synpred93_JPA2()) ) {
					alt62=5;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 62, 22, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 103:
				{
				int LA62_23 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}
				else if ( (synpred93_JPA2()) ) {
					alt62=5;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 62, 23, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 83:
				{
				int LA62_24 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}
				else if ( (synpred93_JPA2()) ) {
					alt62=5;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 62, 24, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LOWER:
			case STRING_LITERAL:
			case 92:
			case 134:
			case 137:
			case 140:
				{
				alt62=2;
				}
				break;
			case GROUP:
				{
				int LA62_31 = input.LA(2);
				if ( (synpred89_JPA2()) ) {
					alt62=1;
				}
				else if ( (synpred90_JPA2()) ) {
					alt62=2;
				}
				else if ( (synpred91_JPA2()) ) {
					alt62=3;
				}
				else if ( (synpred92_JPA2()) ) {
					alt62=4;
				}
				else if ( (synpred93_JPA2()) ) {
					alt62=5;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 62, 31, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 93:
			case 94:
			case 95:
				{
				alt62=4;
				}
				break;
			case 146:
			case 147:
				{
				alt62=5;
				}
				break;
			case 138:
				{
				alt62=7;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 62, 0, input);
				throw nvae;
			}
			switch (alt62) {
				case 1 :
					// JPA2.g:255:7: arithmetic_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_arithmetic_expression_in_scalar_expression2303);
					arithmetic_expression203=arithmetic_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression203.getTree());

					}
					break;
				case 2 :
					// JPA2.g:256:7: string_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_string_expression_in_scalar_expression2311);
					string_expression204=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression204.getTree());

					}
					break;
				case 3 :
					// JPA2.g:257:7: enum_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_enum_expression_in_scalar_expression2319);
					enum_expression205=enum_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, enum_expression205.getTree());

					}
					break;
				case 4 :
					// JPA2.g:258:7: datetime_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_datetime_expression_in_scalar_expression2327);
					datetime_expression206=datetime_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, datetime_expression206.getTree());

					}
					break;
				case 5 :
					// JPA2.g:259:7: boolean_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_boolean_expression_in_scalar_expression2335);
					boolean_expression207=boolean_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, boolean_expression207.getTree());

					}
					break;
				case 6 :
					// JPA2.g:260:7: case_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_case_expression_in_scalar_expression2343);
					case_expression208=case_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, case_expression208.getTree());

					}
					break;
				case 7 :
					// JPA2.g:261:7: entity_type_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_entity_type_expression_in_scalar_expression2351);
					entity_type_expression209=entity_type_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, entity_type_expression209.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "scalar_expression"


	public static class conditional_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "conditional_expression"
	// JPA2.g:262:1: conditional_expression : ( conditional_term ) ( 'OR' conditional_term )* ;
	public final JPA2Parser.conditional_expression_return conditional_expression() throws RecognitionException {
		JPA2Parser.conditional_expression_return retval = new JPA2Parser.conditional_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal211=null;
		ParserRuleReturnScope conditional_term210 =null;
		ParserRuleReturnScope conditional_term212 =null;

		Object string_literal211_tree=null;

		try {
			// JPA2.g:263:5: ( ( conditional_term ) ( 'OR' conditional_term )* )
			// JPA2.g:263:7: ( conditional_term ) ( 'OR' conditional_term )*
			{
			root_0 = (Object)adaptor.nil();


			// JPA2.g:263:7: ( conditional_term )
			// JPA2.g:263:8: conditional_term
			{
			pushFollow(FOLLOW_conditional_term_in_conditional_expression2363);
			conditional_term210=conditional_term();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, conditional_term210.getTree());

			}

			// JPA2.g:263:26: ( 'OR' conditional_term )*
			loop63:
			while (true) {
				int alt63=2;
				int LA63_0 = input.LA(1);
				if ( (LA63_0==OR) ) {
					alt63=1;
				}

				switch (alt63) {
				case 1 :
					// JPA2.g:263:27: 'OR' conditional_term
					{
					string_literal211=(Token)match(input,OR,FOLLOW_OR_in_conditional_expression2367); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal211_tree = (Object)adaptor.create(string_literal211);
					adaptor.addChild(root_0, string_literal211_tree);
					}

					pushFollow(FOLLOW_conditional_term_in_conditional_expression2369);
					conditional_term212=conditional_term();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, conditional_term212.getTree());

					}
					break;

				default :
					break loop63;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "conditional_expression"


	public static class conditional_term_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "conditional_term"
	// JPA2.g:264:1: conditional_term : ( conditional_factor ) ( 'AND' conditional_factor )* ;
	public final JPA2Parser.conditional_term_return conditional_term() throws RecognitionException {
		JPA2Parser.conditional_term_return retval = new JPA2Parser.conditional_term_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal214=null;
		ParserRuleReturnScope conditional_factor213 =null;
		ParserRuleReturnScope conditional_factor215 =null;

		Object string_literal214_tree=null;

		try {
			// JPA2.g:265:5: ( ( conditional_factor ) ( 'AND' conditional_factor )* )
			// JPA2.g:265:7: ( conditional_factor ) ( 'AND' conditional_factor )*
			{
			root_0 = (Object)adaptor.nil();


			// JPA2.g:265:7: ( conditional_factor )
			// JPA2.g:265:8: conditional_factor
			{
			pushFollow(FOLLOW_conditional_factor_in_conditional_term2383);
			conditional_factor213=conditional_factor();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, conditional_factor213.getTree());

			}

			// JPA2.g:265:28: ( 'AND' conditional_factor )*
			loop64:
			while (true) {
				int alt64=2;
				int LA64_0 = input.LA(1);
				if ( (LA64_0==AND) ) {
					alt64=1;
				}

				switch (alt64) {
				case 1 :
					// JPA2.g:265:29: 'AND' conditional_factor
					{
					string_literal214=(Token)match(input,AND,FOLLOW_AND_in_conditional_term2387); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal214_tree = (Object)adaptor.create(string_literal214);
					adaptor.addChild(root_0, string_literal214_tree);
					}

					pushFollow(FOLLOW_conditional_factor_in_conditional_term2389);
					conditional_factor215=conditional_factor();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, conditional_factor215.getTree());

					}
					break;

				default :
					break loop64;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "conditional_term"


	public static class conditional_factor_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "conditional_factor"
	// JPA2.g:266:1: conditional_factor : ( 'NOT' )? conditional_primary ;
	public final JPA2Parser.conditional_factor_return conditional_factor() throws RecognitionException {
		JPA2Parser.conditional_factor_return retval = new JPA2Parser.conditional_factor_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal216=null;
		ParserRuleReturnScope conditional_primary217 =null;

		Object string_literal216_tree=null;

		try {
			// JPA2.g:267:5: ( ( 'NOT' )? conditional_primary )
			// JPA2.g:267:7: ( 'NOT' )? conditional_primary
			{
			root_0 = (Object)adaptor.nil();


			// JPA2.g:267:7: ( 'NOT' )?
			int alt65=2;
			int LA65_0 = input.LA(1);
			if ( (LA65_0==NOT) ) {
				int LA65_1 = input.LA(2);
				if ( (synpred97_JPA2()) ) {
					alt65=1;
				}
			}
			switch (alt65) {
				case 1 :
					// JPA2.g:267:8: 'NOT'
					{
					string_literal216=(Token)match(input,NOT,FOLLOW_NOT_in_conditional_factor2403); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal216_tree = (Object)adaptor.create(string_literal216);
					adaptor.addChild(root_0, string_literal216_tree);
					}

					}
					break;

			}

			pushFollow(FOLLOW_conditional_primary_in_conditional_factor2407);
			conditional_primary217=conditional_primary();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, conditional_primary217.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "conditional_factor"


	public static class conditional_primary_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "conditional_primary"
	// JPA2.g:268:1: conditional_primary : ( simple_cond_expression -> ^( T_SIMPLE_CONDITION[] simple_cond_expression ) | '(' conditional_expression ')' );
	public final JPA2Parser.conditional_primary_return conditional_primary() throws RecognitionException {
		JPA2Parser.conditional_primary_return retval = new JPA2Parser.conditional_primary_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal219=null;
		Token char_literal221=null;
		ParserRuleReturnScope simple_cond_expression218 =null;
		ParserRuleReturnScope conditional_expression220 =null;

		Object char_literal219_tree=null;
		Object char_literal221_tree=null;
		RewriteRuleSubtreeStream stream_simple_cond_expression=new RewriteRuleSubtreeStream(adaptor,"rule simple_cond_expression");

		try {
			// JPA2.g:269:5: ( simple_cond_expression -> ^( T_SIMPLE_CONDITION[] simple_cond_expression ) | '(' conditional_expression ')' )
			int alt66=2;
			int LA66_0 = input.LA(1);
			if ( (LA66_0==AVG||LA66_0==CASE||LA66_0==COUNT||LA66_0==GROUP||LA66_0==INT_NUMERAL||LA66_0==LOWER||(LA66_0 >= MAX && LA66_0 <= NOT)||(LA66_0 >= STRING_LITERAL && LA66_0 <= SUM)||LA66_0==WORD||LA66_0==63||LA66_0==65||LA66_0==67||LA66_0==70||(LA66_0 >= 77 && LA66_0 <= 85)||(LA66_0 >= 90 && LA66_0 <= 95)||(LA66_0 >= 102 && LA66_0 <= 103)||LA66_0==105||LA66_0==107||LA66_0==111||LA66_0==113||LA66_0==116||LA66_0==121||LA66_0==131||(LA66_0 >= 133 && LA66_0 <= 134)||(LA66_0 >= 136 && LA66_0 <= 138)||LA66_0==140||(LA66_0 >= 146 && LA66_0 <= 147)) ) {
				alt66=1;
			}
			else if ( (LA66_0==LPAREN) ) {
				int LA66_20 = input.LA(2);
				if ( (synpred98_JPA2()) ) {
					alt66=1;
				}
				else if ( (true) ) {
					alt66=2;
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 66, 0, input);
				throw nvae;
			}

			switch (alt66) {
				case 1 :
					// JPA2.g:269:7: simple_cond_expression
					{
					pushFollow(FOLLOW_simple_cond_expression_in_conditional_primary2418);
					simple_cond_expression218=simple_cond_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_simple_cond_expression.add(simple_cond_expression218.getTree());
					// AST REWRITE
					// elements: simple_cond_expression
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 270:5: -> ^( T_SIMPLE_CONDITION[] simple_cond_expression )
					{
						// JPA2.g:270:8: ^( T_SIMPLE_CONDITION[] simple_cond_expression )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot(new SimpleConditionNode(T_SIMPLE_CONDITION), root_1);
						adaptor.addChild(root_1, stream_simple_cond_expression.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// JPA2.g:271:7: '(' conditional_expression ')'
					{
					root_0 = (Object)adaptor.nil();


					char_literal219=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_conditional_primary2442); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal219_tree = (Object)adaptor.create(char_literal219);
					adaptor.addChild(root_0, char_literal219_tree);
					}

					pushFollow(FOLLOW_conditional_expression_in_conditional_primary2443);
					conditional_expression220=conditional_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, conditional_expression220.getTree());

					char_literal221=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_conditional_primary2444); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal221_tree = (Object)adaptor.create(char_literal221);
					adaptor.addChild(root_0, char_literal221_tree);
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "conditional_primary"


	public static class simple_cond_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "simple_cond_expression"
	// JPA2.g:272:1: simple_cond_expression : ( comparison_expression | between_expression | in_expression | like_expression | null_comparison_expression | empty_collection_comparison_expression | collection_member_expression | exists_expression | date_macro_expression );
	public final JPA2Parser.simple_cond_expression_return simple_cond_expression() throws RecognitionException {
		JPA2Parser.simple_cond_expression_return retval = new JPA2Parser.simple_cond_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope comparison_expression222 =null;
		ParserRuleReturnScope between_expression223 =null;
		ParserRuleReturnScope in_expression224 =null;
		ParserRuleReturnScope like_expression225 =null;
		ParserRuleReturnScope null_comparison_expression226 =null;
		ParserRuleReturnScope empty_collection_comparison_expression227 =null;
		ParserRuleReturnScope collection_member_expression228 =null;
		ParserRuleReturnScope exists_expression229 =null;
		ParserRuleReturnScope date_macro_expression230 =null;


		try {
			// JPA2.g:273:5: ( comparison_expression | between_expression | in_expression | like_expression | null_comparison_expression | empty_collection_comparison_expression | collection_member_expression | exists_expression | date_macro_expression )
			int alt67=9;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA67_1 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred101_JPA2()) ) {
					alt67=3;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}
				else if ( (synpred103_JPA2()) ) {
					alt67=5;
				}
				else if ( (synpred104_JPA2()) ) {
					alt67=6;
				}
				else if ( (synpred105_JPA2()) ) {
					alt67=7;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case STRING_LITERAL:
				{
				int LA67_2 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 77:
				{
				int LA67_3 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}
				else if ( (synpred103_JPA2()) ) {
					alt67=5;
				}
				else if ( (synpred105_JPA2()) ) {
					alt67=7;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case NAMED_PARAMETER:
				{
				int LA67_4 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}
				else if ( (synpred103_JPA2()) ) {
					alt67=5;
				}
				else if ( (synpred105_JPA2()) ) {
					alt67=7;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 63:
				{
				int LA67_5 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}
				else if ( (synpred103_JPA2()) ) {
					alt67=5;
				}
				else if ( (synpred105_JPA2()) ) {
					alt67=7;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 92:
				{
				int LA67_6 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 6, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 134:
				{
				int LA67_7 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 7, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 137:
				{
				int LA67_8 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 8, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LOWER:
				{
				int LA67_9 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 9, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 140:
				{
				int LA67_10 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 10, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case COUNT:
				{
				int LA67_11 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 11, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
				{
				int LA67_12 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 12, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 105:
				{
				int LA67_13 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 13, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case CASE:
				{
				int LA67_14 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 14, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 91:
				{
				int LA67_15 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 15, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 121:
				{
				int LA67_16 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 16, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 90:
				{
				int LA67_17 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 17, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 103:
				{
				int LA67_18 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred101_JPA2()) ) {
					alt67=3;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 18, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 83:
				{
				int LA67_19 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 19, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LPAREN:
				{
				int LA67_20 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}
				else if ( (synpred105_JPA2()) ) {
					alt67=7;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 20, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 146:
			case 147:
				{
				alt67=1;
				}
				break;
			case GROUP:
				{
				int LA67_22 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}
				else if ( (synpred101_JPA2()) ) {
					alt67=3;
				}
				else if ( (synpred102_JPA2()) ) {
					alt67=4;
				}
				else if ( (synpred103_JPA2()) ) {
					alt67=5;
				}
				else if ( (synpred104_JPA2()) ) {
					alt67=6;
				}
				else if ( (synpred105_JPA2()) ) {
					alt67=7;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 22, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 93:
			case 94:
			case 95:
				{
				int LA67_23 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 23, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 138:
				{
				int LA67_24 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred101_JPA2()) ) {
					alt67=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 24, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 65:
			case 67:
				{
				int LA67_25 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 25, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case INT_NUMERAL:
				{
				int LA67_26 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 26, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 70:
				{
				int LA67_27 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 27, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 111:
				{
				int LA67_28 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 28, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 113:
				{
				int LA67_29 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 29, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 85:
				{
				int LA67_30 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 30, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 133:
				{
				int LA67_31 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 31, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 116:
				{
				int LA67_32 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 32, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 131:
				{
				int LA67_33 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 33, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 107:
				{
				int LA67_34 = input.LA(2);
				if ( (synpred99_JPA2()) ) {
					alt67=1;
				}
				else if ( (synpred100_JPA2()) ) {
					alt67=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 67, 34, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 136:
				{
				alt67=5;
				}
				break;
			case NOT:
			case 102:
				{
				alt67=8;
				}
				break;
			case 78:
			case 79:
			case 80:
			case 81:
			case 82:
			case 84:
				{
				alt67=9;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 67, 0, input);
				throw nvae;
			}
			switch (alt67) {
				case 1 :
					// JPA2.g:273:7: comparison_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_comparison_expression_in_simple_cond_expression2455);
					comparison_expression222=comparison_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, comparison_expression222.getTree());

					}
					break;
				case 2 :
					// JPA2.g:274:7: between_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_between_expression_in_simple_cond_expression2463);
					between_expression223=between_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, between_expression223.getTree());

					}
					break;
				case 3 :
					// JPA2.g:275:7: in_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_in_expression_in_simple_cond_expression2471);
					in_expression224=in_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, in_expression224.getTree());

					}
					break;
				case 4 :
					// JPA2.g:276:7: like_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_like_expression_in_simple_cond_expression2479);
					like_expression225=like_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, like_expression225.getTree());

					}
					break;
				case 5 :
					// JPA2.g:277:7: null_comparison_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_null_comparison_expression_in_simple_cond_expression2487);
					null_comparison_expression226=null_comparison_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, null_comparison_expression226.getTree());

					}
					break;
				case 6 :
					// JPA2.g:278:7: empty_collection_comparison_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_empty_collection_comparison_expression_in_simple_cond_expression2495);
					empty_collection_comparison_expression227=empty_collection_comparison_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, empty_collection_comparison_expression227.getTree());

					}
					break;
				case 7 :
					// JPA2.g:279:7: collection_member_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_collection_member_expression_in_simple_cond_expression2503);
					collection_member_expression228=collection_member_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, collection_member_expression228.getTree());

					}
					break;
				case 8 :
					// JPA2.g:280:7: exists_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_exists_expression_in_simple_cond_expression2511);
					exists_expression229=exists_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, exists_expression229.getTree());

					}
					break;
				case 9 :
					// JPA2.g:281:7: date_macro_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_date_macro_expression_in_simple_cond_expression2519);
					date_macro_expression230=date_macro_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, date_macro_expression230.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "simple_cond_expression"


	public static class date_macro_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "date_macro_expression"
	// JPA2.g:284:1: date_macro_expression : ( date_between_macro_expression | date_before_macro_expression | date_after_macro_expression | date_equals_macro_expression | date_today_macro_expression | custom_date_between_macro_expression );
	public final JPA2Parser.date_macro_expression_return date_macro_expression() throws RecognitionException {
		JPA2Parser.date_macro_expression_return retval = new JPA2Parser.date_macro_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope date_between_macro_expression231 =null;
		ParserRuleReturnScope date_before_macro_expression232 =null;
		ParserRuleReturnScope date_after_macro_expression233 =null;
		ParserRuleReturnScope date_equals_macro_expression234 =null;
		ParserRuleReturnScope date_today_macro_expression235 =null;
		ParserRuleReturnScope custom_date_between_macro_expression236 =null;


		try {
			// JPA2.g:285:5: ( date_between_macro_expression | date_before_macro_expression | date_after_macro_expression | date_equals_macro_expression | date_today_macro_expression | custom_date_between_macro_expression )
			int alt68=6;
			switch ( input.LA(1) ) {
			case 78:
				{
				alt68=1;
				}
				break;
			case 80:
				{
				alt68=2;
				}
				break;
			case 79:
				{
				alt68=3;
				}
				break;
			case 82:
				{
				alt68=4;
				}
				break;
			case 84:
				{
				alt68=5;
				}
				break;
			case 81:
				{
				alt68=6;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 68, 0, input);
				throw nvae;
			}
			switch (alt68) {
				case 1 :
					// JPA2.g:285:7: date_between_macro_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_date_between_macro_expression_in_date_macro_expression2532);
					date_between_macro_expression231=date_between_macro_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, date_between_macro_expression231.getTree());

					}
					break;
				case 2 :
					// JPA2.g:286:7: date_before_macro_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_date_before_macro_expression_in_date_macro_expression2540);
					date_before_macro_expression232=date_before_macro_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, date_before_macro_expression232.getTree());

					}
					break;
				case 3 :
					// JPA2.g:287:7: date_after_macro_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_date_after_macro_expression_in_date_macro_expression2548);
					date_after_macro_expression233=date_after_macro_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, date_after_macro_expression233.getTree());

					}
					break;
				case 4 :
					// JPA2.g:288:7: date_equals_macro_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_date_equals_macro_expression_in_date_macro_expression2556);
					date_equals_macro_expression234=date_equals_macro_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, date_equals_macro_expression234.getTree());

					}
					break;
				case 5 :
					// JPA2.g:289:7: date_today_macro_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_date_today_macro_expression_in_date_macro_expression2564);
					date_today_macro_expression235=date_today_macro_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, date_today_macro_expression235.getTree());

					}
					break;
				case 6 :
					// JPA2.g:290:7: custom_date_between_macro_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_custom_date_between_macro_expression_in_date_macro_expression2572);
					custom_date_between_macro_expression236=custom_date_between_macro_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, custom_date_between_macro_expression236.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "date_macro_expression"


	public static class date_between_macro_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "date_between_macro_expression"
	// JPA2.g:292:1: date_between_macro_expression : '@BETWEEN' '(' path_expression ',' 'NOW' ( ( '+' | '-' ) numeric_literal )? ',' 'NOW' ( ( '+' | '-' ) numeric_literal )? ',' ( 'YEAR' | 'MONTH' | 'DAY' | 'HOUR' | 'MINUTE' | 'SECOND' ) ( ',' 'USER_TIMEZONE' )? ')' ;
	public final JPA2Parser.date_between_macro_expression_return date_between_macro_expression() throws RecognitionException {
		JPA2Parser.date_between_macro_expression_return retval = new JPA2Parser.date_between_macro_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal237=null;
		Token char_literal238=null;
		Token char_literal240=null;
		Token string_literal241=null;
		Token set242=null;
		Token char_literal244=null;
		Token string_literal245=null;
		Token set246=null;
		Token char_literal248=null;
		Token set249=null;
		Token char_literal250=null;
		Token string_literal251=null;
		Token char_literal252=null;
		ParserRuleReturnScope path_expression239 =null;
		ParserRuleReturnScope numeric_literal243 =null;
		ParserRuleReturnScope numeric_literal247 =null;

		Object string_literal237_tree=null;
		Object char_literal238_tree=null;
		Object char_literal240_tree=null;
		Object string_literal241_tree=null;
		Object set242_tree=null;
		Object char_literal244_tree=null;
		Object string_literal245_tree=null;
		Object set246_tree=null;
		Object char_literal248_tree=null;
		Object set249_tree=null;
		Object char_literal250_tree=null;
		Object string_literal251_tree=null;
		Object char_literal252_tree=null;

		try {
			// JPA2.g:293:5: ( '@BETWEEN' '(' path_expression ',' 'NOW' ( ( '+' | '-' ) numeric_literal )? ',' 'NOW' ( ( '+' | '-' ) numeric_literal )? ',' ( 'YEAR' | 'MONTH' | 'DAY' | 'HOUR' | 'MINUTE' | 'SECOND' ) ( ',' 'USER_TIMEZONE' )? ')' )
			// JPA2.g:293:7: '@BETWEEN' '(' path_expression ',' 'NOW' ( ( '+' | '-' ) numeric_literal )? ',' 'NOW' ( ( '+' | '-' ) numeric_literal )? ',' ( 'YEAR' | 'MONTH' | 'DAY' | 'HOUR' | 'MINUTE' | 'SECOND' ) ( ',' 'USER_TIMEZONE' )? ')'
			{
			root_0 = (Object)adaptor.nil();


			string_literal237=(Token)match(input,78,FOLLOW_78_in_date_between_macro_expression2584); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal237_tree = (Object)adaptor.create(string_literal237);
			adaptor.addChild(root_0, string_literal237_tree);
			}

			char_literal238=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_date_between_macro_expression2586); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal238_tree = (Object)adaptor.create(char_literal238);
			adaptor.addChild(root_0, char_literal238_tree);
			}

			pushFollow(FOLLOW_path_expression_in_date_between_macro_expression2588);
			path_expression239=path_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression239.getTree());

			char_literal240=(Token)match(input,66,FOLLOW_66_in_date_between_macro_expression2590); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal240_tree = (Object)adaptor.create(char_literal240);
			adaptor.addChild(root_0, char_literal240_tree);
			}

			string_literal241=(Token)match(input,119,FOLLOW_119_in_date_between_macro_expression2592); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal241_tree = (Object)adaptor.create(string_literal241);
			adaptor.addChild(root_0, string_literal241_tree);
			}

			// JPA2.g:293:48: ( ( '+' | '-' ) numeric_literal )?
			int alt69=2;
			int LA69_0 = input.LA(1);
			if ( (LA69_0==65||LA69_0==67) ) {
				alt69=1;
			}
			switch (alt69) {
				case 1 :
					// JPA2.g:293:49: ( '+' | '-' ) numeric_literal
					{
					set242=input.LT(1);
					if ( input.LA(1)==65||input.LA(1)==67 ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set242));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_numeric_literal_in_date_between_macro_expression2603);
					numeric_literal243=numeric_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, numeric_literal243.getTree());

					}
					break;

			}

			char_literal244=(Token)match(input,66,FOLLOW_66_in_date_between_macro_expression2607); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal244_tree = (Object)adaptor.create(char_literal244);
			adaptor.addChild(root_0, char_literal244_tree);
			}

			string_literal245=(Token)match(input,119,FOLLOW_119_in_date_between_macro_expression2609); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal245_tree = (Object)adaptor.create(string_literal245);
			adaptor.addChild(root_0, string_literal245_tree);
			}

			// JPA2.g:293:89: ( ( '+' | '-' ) numeric_literal )?
			int alt70=2;
			int LA70_0 = input.LA(1);
			if ( (LA70_0==65||LA70_0==67) ) {
				alt70=1;
			}
			switch (alt70) {
				case 1 :
					// JPA2.g:293:90: ( '+' | '-' ) numeric_literal
					{
					set246=input.LT(1);
					if ( input.LA(1)==65||input.LA(1)==67 ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set246));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_numeric_literal_in_date_between_macro_expression2620);
					numeric_literal247=numeric_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, numeric_literal247.getTree());

					}
					break;

			}

			char_literal248=(Token)match(input,66,FOLLOW_66_in_date_between_macro_expression2624); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal248_tree = (Object)adaptor.create(char_literal248);
			adaptor.addChild(root_0, char_literal248_tree);
			}

			set249=input.LT(1);
			if ( input.LA(1)==96||input.LA(1)==106||input.LA(1)==115||input.LA(1)==117||input.LA(1)==129||input.LA(1)==145 ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set249));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			// JPA2.g:293:181: ( ',' 'USER_TIMEZONE' )?
			int alt71=2;
			int LA71_0 = input.LA(1);
			if ( (LA71_0==66) ) {
				alt71=1;
			}
			switch (alt71) {
				case 1 :
					// JPA2.g:293:182: ',' 'USER_TIMEZONE'
					{
					char_literal250=(Token)match(input,66,FOLLOW_66_in_date_between_macro_expression2650); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal250_tree = (Object)adaptor.create(char_literal250);
					adaptor.addChild(root_0, char_literal250_tree);
					}

					string_literal251=(Token)match(input,141,FOLLOW_141_in_date_between_macro_expression2652); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal251_tree = (Object)adaptor.create(string_literal251);
					adaptor.addChild(root_0, string_literal251_tree);
					}

					}
					break;

			}

			char_literal252=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_date_between_macro_expression2656); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal252_tree = (Object)adaptor.create(char_literal252);
			adaptor.addChild(root_0, char_literal252_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "date_between_macro_expression"


	public static class date_before_macro_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "date_before_macro_expression"
	// JPA2.g:295:1: date_before_macro_expression : '@DATEBEFORE' '(' path_expression ',' ( path_expression | input_parameter | 'NOW' ( ( '+' | '-' ) numeric_literal )? ) ( ',' 'USER_TIMEZONE' )? ')' ;
	public final JPA2Parser.date_before_macro_expression_return date_before_macro_expression() throws RecognitionException {
		JPA2Parser.date_before_macro_expression_return retval = new JPA2Parser.date_before_macro_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal253=null;
		Token char_literal254=null;
		Token char_literal256=null;
		Token string_literal259=null;
		Token set260=null;
		Token char_literal262=null;
		Token string_literal263=null;
		Token char_literal264=null;
		ParserRuleReturnScope path_expression255 =null;
		ParserRuleReturnScope path_expression257 =null;
		ParserRuleReturnScope input_parameter258 =null;
		ParserRuleReturnScope numeric_literal261 =null;

		Object string_literal253_tree=null;
		Object char_literal254_tree=null;
		Object char_literal256_tree=null;
		Object string_literal259_tree=null;
		Object set260_tree=null;
		Object char_literal262_tree=null;
		Object string_literal263_tree=null;
		Object char_literal264_tree=null;

		try {
			// JPA2.g:296:5: ( '@DATEBEFORE' '(' path_expression ',' ( path_expression | input_parameter | 'NOW' ( ( '+' | '-' ) numeric_literal )? ) ( ',' 'USER_TIMEZONE' )? ')' )
			// JPA2.g:296:7: '@DATEBEFORE' '(' path_expression ',' ( path_expression | input_parameter | 'NOW' ( ( '+' | '-' ) numeric_literal )? ) ( ',' 'USER_TIMEZONE' )? ')'
			{
			root_0 = (Object)adaptor.nil();


			string_literal253=(Token)match(input,80,FOLLOW_80_in_date_before_macro_expression2668); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal253_tree = (Object)adaptor.create(string_literal253);
			adaptor.addChild(root_0, string_literal253_tree);
			}

			char_literal254=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_date_before_macro_expression2670); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal254_tree = (Object)adaptor.create(char_literal254);
			adaptor.addChild(root_0, char_literal254_tree);
			}

			pushFollow(FOLLOW_path_expression_in_date_before_macro_expression2672);
			path_expression255=path_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression255.getTree());

			char_literal256=(Token)match(input,66,FOLLOW_66_in_date_before_macro_expression2674); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal256_tree = (Object)adaptor.create(char_literal256);
			adaptor.addChild(root_0, char_literal256_tree);
			}

			// JPA2.g:296:45: ( path_expression | input_parameter | 'NOW' ( ( '+' | '-' ) numeric_literal )? )
			int alt73=3;
			switch ( input.LA(1) ) {
			case GROUP:
			case WORD:
				{
				alt73=1;
				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt73=2;
				}
				break;
			case 119:
				{
				alt73=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 73, 0, input);
				throw nvae;
			}
			switch (alt73) {
				case 1 :
					// JPA2.g:296:46: path_expression
					{
					pushFollow(FOLLOW_path_expression_in_date_before_macro_expression2677);
					path_expression257=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression257.getTree());

					}
					break;
				case 2 :
					// JPA2.g:296:64: input_parameter
					{
					pushFollow(FOLLOW_input_parameter_in_date_before_macro_expression2681);
					input_parameter258=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter258.getTree());

					}
					break;
				case 3 :
					// JPA2.g:296:82: 'NOW' ( ( '+' | '-' ) numeric_literal )?
					{
					string_literal259=(Token)match(input,119,FOLLOW_119_in_date_before_macro_expression2685); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal259_tree = (Object)adaptor.create(string_literal259);
					adaptor.addChild(root_0, string_literal259_tree);
					}

					// JPA2.g:296:88: ( ( '+' | '-' ) numeric_literal )?
					int alt72=2;
					int LA72_0 = input.LA(1);
					if ( (LA72_0==65||LA72_0==67) ) {
						alt72=1;
					}
					switch (alt72) {
						case 1 :
							// JPA2.g:296:89: ( '+' | '-' ) numeric_literal
							{
							set260=input.LT(1);
							if ( input.LA(1)==65||input.LA(1)==67 ) {
								input.consume();
								if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set260));
								state.errorRecovery=false;
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								throw mse;
							}
							pushFollow(FOLLOW_numeric_literal_in_date_before_macro_expression2696);
							numeric_literal261=numeric_literal();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, numeric_literal261.getTree());

							}
							break;

					}

					}
					break;

			}

			// JPA2.g:296:121: ( ',' 'USER_TIMEZONE' )?
			int alt74=2;
			int LA74_0 = input.LA(1);
			if ( (LA74_0==66) ) {
				alt74=1;
			}
			switch (alt74) {
				case 1 :
					// JPA2.g:296:122: ',' 'USER_TIMEZONE'
					{
					char_literal262=(Token)match(input,66,FOLLOW_66_in_date_before_macro_expression2703); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal262_tree = (Object)adaptor.create(char_literal262);
					adaptor.addChild(root_0, char_literal262_tree);
					}

					string_literal263=(Token)match(input,141,FOLLOW_141_in_date_before_macro_expression2705); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal263_tree = (Object)adaptor.create(string_literal263);
					adaptor.addChild(root_0, string_literal263_tree);
					}

					}
					break;

			}

			char_literal264=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_date_before_macro_expression2709); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal264_tree = (Object)adaptor.create(char_literal264);
			adaptor.addChild(root_0, char_literal264_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "date_before_macro_expression"


	public static class date_after_macro_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "date_after_macro_expression"
	// JPA2.g:298:1: date_after_macro_expression : '@DATEAFTER' '(' path_expression ',' ( path_expression | input_parameter | 'NOW' ( ( '+' | '-' ) numeric_literal )? ) ( ',' 'USER_TIMEZONE' )? ')' ;
	public final JPA2Parser.date_after_macro_expression_return date_after_macro_expression() throws RecognitionException {
		JPA2Parser.date_after_macro_expression_return retval = new JPA2Parser.date_after_macro_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal265=null;
		Token char_literal266=null;
		Token char_literal268=null;
		Token string_literal271=null;
		Token set272=null;
		Token char_literal274=null;
		Token string_literal275=null;
		Token char_literal276=null;
		ParserRuleReturnScope path_expression267 =null;
		ParserRuleReturnScope path_expression269 =null;
		ParserRuleReturnScope input_parameter270 =null;
		ParserRuleReturnScope numeric_literal273 =null;

		Object string_literal265_tree=null;
		Object char_literal266_tree=null;
		Object char_literal268_tree=null;
		Object string_literal271_tree=null;
		Object set272_tree=null;
		Object char_literal274_tree=null;
		Object string_literal275_tree=null;
		Object char_literal276_tree=null;

		try {
			// JPA2.g:299:5: ( '@DATEAFTER' '(' path_expression ',' ( path_expression | input_parameter | 'NOW' ( ( '+' | '-' ) numeric_literal )? ) ( ',' 'USER_TIMEZONE' )? ')' )
			// JPA2.g:299:7: '@DATEAFTER' '(' path_expression ',' ( path_expression | input_parameter | 'NOW' ( ( '+' | '-' ) numeric_literal )? ) ( ',' 'USER_TIMEZONE' )? ')'
			{
			root_0 = (Object)adaptor.nil();


			string_literal265=(Token)match(input,79,FOLLOW_79_in_date_after_macro_expression2721); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal265_tree = (Object)adaptor.create(string_literal265);
			adaptor.addChild(root_0, string_literal265_tree);
			}

			char_literal266=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_date_after_macro_expression2723); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal266_tree = (Object)adaptor.create(char_literal266);
			adaptor.addChild(root_0, char_literal266_tree);
			}

			pushFollow(FOLLOW_path_expression_in_date_after_macro_expression2725);
			path_expression267=path_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression267.getTree());

			char_literal268=(Token)match(input,66,FOLLOW_66_in_date_after_macro_expression2727); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal268_tree = (Object)adaptor.create(char_literal268);
			adaptor.addChild(root_0, char_literal268_tree);
			}

			// JPA2.g:299:44: ( path_expression | input_parameter | 'NOW' ( ( '+' | '-' ) numeric_literal )? )
			int alt76=3;
			switch ( input.LA(1) ) {
			case GROUP:
			case WORD:
				{
				alt76=1;
				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt76=2;
				}
				break;
			case 119:
				{
				alt76=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 76, 0, input);
				throw nvae;
			}
			switch (alt76) {
				case 1 :
					// JPA2.g:299:45: path_expression
					{
					pushFollow(FOLLOW_path_expression_in_date_after_macro_expression2730);
					path_expression269=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression269.getTree());

					}
					break;
				case 2 :
					// JPA2.g:299:63: input_parameter
					{
					pushFollow(FOLLOW_input_parameter_in_date_after_macro_expression2734);
					input_parameter270=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter270.getTree());

					}
					break;
				case 3 :
					// JPA2.g:299:81: 'NOW' ( ( '+' | '-' ) numeric_literal )?
					{
					string_literal271=(Token)match(input,119,FOLLOW_119_in_date_after_macro_expression2738); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal271_tree = (Object)adaptor.create(string_literal271);
					adaptor.addChild(root_0, string_literal271_tree);
					}

					// JPA2.g:299:87: ( ( '+' | '-' ) numeric_literal )?
					int alt75=2;
					int LA75_0 = input.LA(1);
					if ( (LA75_0==65||LA75_0==67) ) {
						alt75=1;
					}
					switch (alt75) {
						case 1 :
							// JPA2.g:299:88: ( '+' | '-' ) numeric_literal
							{
							set272=input.LT(1);
							if ( input.LA(1)==65||input.LA(1)==67 ) {
								input.consume();
								if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set272));
								state.errorRecovery=false;
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								throw mse;
							}
							pushFollow(FOLLOW_numeric_literal_in_date_after_macro_expression2749);
							numeric_literal273=numeric_literal();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, numeric_literal273.getTree());

							}
							break;

					}

					}
					break;

			}

			// JPA2.g:299:120: ( ',' 'USER_TIMEZONE' )?
			int alt77=2;
			int LA77_0 = input.LA(1);
			if ( (LA77_0==66) ) {
				alt77=1;
			}
			switch (alt77) {
				case 1 :
					// JPA2.g:299:121: ',' 'USER_TIMEZONE'
					{
					char_literal274=(Token)match(input,66,FOLLOW_66_in_date_after_macro_expression2756); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal274_tree = (Object)adaptor.create(char_literal274);
					adaptor.addChild(root_0, char_literal274_tree);
					}

					string_literal275=(Token)match(input,141,FOLLOW_141_in_date_after_macro_expression2758); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal275_tree = (Object)adaptor.create(string_literal275);
					adaptor.addChild(root_0, string_literal275_tree);
					}

					}
					break;

			}

			char_literal276=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_date_after_macro_expression2762); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal276_tree = (Object)adaptor.create(char_literal276);
			adaptor.addChild(root_0, char_literal276_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "date_after_macro_expression"


	public static class date_equals_macro_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "date_equals_macro_expression"
	// JPA2.g:301:1: date_equals_macro_expression : '@DATEEQUALS' '(' path_expression ',' ( path_expression | input_parameter | 'NOW' ( ( '+' | '-' ) numeric_literal )? ) ( ',' 'USER_TIMEZONE' )? ')' ;
	public final JPA2Parser.date_equals_macro_expression_return date_equals_macro_expression() throws RecognitionException {
		JPA2Parser.date_equals_macro_expression_return retval = new JPA2Parser.date_equals_macro_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal277=null;
		Token char_literal278=null;
		Token char_literal280=null;
		Token string_literal283=null;
		Token set284=null;
		Token char_literal286=null;
		Token string_literal287=null;
		Token char_literal288=null;
		ParserRuleReturnScope path_expression279 =null;
		ParserRuleReturnScope path_expression281 =null;
		ParserRuleReturnScope input_parameter282 =null;
		ParserRuleReturnScope numeric_literal285 =null;

		Object string_literal277_tree=null;
		Object char_literal278_tree=null;
		Object char_literal280_tree=null;
		Object string_literal283_tree=null;
		Object set284_tree=null;
		Object char_literal286_tree=null;
		Object string_literal287_tree=null;
		Object char_literal288_tree=null;

		try {
			// JPA2.g:302:5: ( '@DATEEQUALS' '(' path_expression ',' ( path_expression | input_parameter | 'NOW' ( ( '+' | '-' ) numeric_literal )? ) ( ',' 'USER_TIMEZONE' )? ')' )
			// JPA2.g:302:7: '@DATEEQUALS' '(' path_expression ',' ( path_expression | input_parameter | 'NOW' ( ( '+' | '-' ) numeric_literal )? ) ( ',' 'USER_TIMEZONE' )? ')'
			{
			root_0 = (Object)adaptor.nil();


			string_literal277=(Token)match(input,82,FOLLOW_82_in_date_equals_macro_expression2774); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal277_tree = (Object)adaptor.create(string_literal277);
			adaptor.addChild(root_0, string_literal277_tree);
			}

			char_literal278=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_date_equals_macro_expression2776); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal278_tree = (Object)adaptor.create(char_literal278);
			adaptor.addChild(root_0, char_literal278_tree);
			}

			pushFollow(FOLLOW_path_expression_in_date_equals_macro_expression2778);
			path_expression279=path_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression279.getTree());

			char_literal280=(Token)match(input,66,FOLLOW_66_in_date_equals_macro_expression2780); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal280_tree = (Object)adaptor.create(char_literal280);
			adaptor.addChild(root_0, char_literal280_tree);
			}

			// JPA2.g:302:45: ( path_expression | input_parameter | 'NOW' ( ( '+' | '-' ) numeric_literal )? )
			int alt79=3;
			switch ( input.LA(1) ) {
			case GROUP:
			case WORD:
				{
				alt79=1;
				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt79=2;
				}
				break;
			case 119:
				{
				alt79=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 79, 0, input);
				throw nvae;
			}
			switch (alt79) {
				case 1 :
					// JPA2.g:302:46: path_expression
					{
					pushFollow(FOLLOW_path_expression_in_date_equals_macro_expression2783);
					path_expression281=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression281.getTree());

					}
					break;
				case 2 :
					// JPA2.g:302:64: input_parameter
					{
					pushFollow(FOLLOW_input_parameter_in_date_equals_macro_expression2787);
					input_parameter282=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter282.getTree());

					}
					break;
				case 3 :
					// JPA2.g:302:82: 'NOW' ( ( '+' | '-' ) numeric_literal )?
					{
					string_literal283=(Token)match(input,119,FOLLOW_119_in_date_equals_macro_expression2791); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal283_tree = (Object)adaptor.create(string_literal283);
					adaptor.addChild(root_0, string_literal283_tree);
					}

					// JPA2.g:302:88: ( ( '+' | '-' ) numeric_literal )?
					int alt78=2;
					int LA78_0 = input.LA(1);
					if ( (LA78_0==65||LA78_0==67) ) {
						alt78=1;
					}
					switch (alt78) {
						case 1 :
							// JPA2.g:302:89: ( '+' | '-' ) numeric_literal
							{
							set284=input.LT(1);
							if ( input.LA(1)==65||input.LA(1)==67 ) {
								input.consume();
								if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set284));
								state.errorRecovery=false;
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								throw mse;
							}
							pushFollow(FOLLOW_numeric_literal_in_date_equals_macro_expression2802);
							numeric_literal285=numeric_literal();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, numeric_literal285.getTree());

							}
							break;

					}

					}
					break;

			}

			// JPA2.g:302:121: ( ',' 'USER_TIMEZONE' )?
			int alt80=2;
			int LA80_0 = input.LA(1);
			if ( (LA80_0==66) ) {
				alt80=1;
			}
			switch (alt80) {
				case 1 :
					// JPA2.g:302:122: ',' 'USER_TIMEZONE'
					{
					char_literal286=(Token)match(input,66,FOLLOW_66_in_date_equals_macro_expression2809); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal286_tree = (Object)adaptor.create(char_literal286);
					adaptor.addChild(root_0, char_literal286_tree);
					}

					string_literal287=(Token)match(input,141,FOLLOW_141_in_date_equals_macro_expression2811); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal287_tree = (Object)adaptor.create(string_literal287);
					adaptor.addChild(root_0, string_literal287_tree);
					}

					}
					break;

			}

			char_literal288=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_date_equals_macro_expression2815); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal288_tree = (Object)adaptor.create(char_literal288);
			adaptor.addChild(root_0, char_literal288_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "date_equals_macro_expression"


	public static class date_today_macro_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "date_today_macro_expression"
	// JPA2.g:304:1: date_today_macro_expression : '@TODAY' '(' path_expression ( ',' 'USER_TIMEZONE' )? ')' ;
	public final JPA2Parser.date_today_macro_expression_return date_today_macro_expression() throws RecognitionException {
		JPA2Parser.date_today_macro_expression_return retval = new JPA2Parser.date_today_macro_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal289=null;
		Token char_literal290=null;
		Token char_literal292=null;
		Token string_literal293=null;
		Token char_literal294=null;
		ParserRuleReturnScope path_expression291 =null;

		Object string_literal289_tree=null;
		Object char_literal290_tree=null;
		Object char_literal292_tree=null;
		Object string_literal293_tree=null;
		Object char_literal294_tree=null;

		try {
			// JPA2.g:305:5: ( '@TODAY' '(' path_expression ( ',' 'USER_TIMEZONE' )? ')' )
			// JPA2.g:305:7: '@TODAY' '(' path_expression ( ',' 'USER_TIMEZONE' )? ')'
			{
			root_0 = (Object)adaptor.nil();


			string_literal289=(Token)match(input,84,FOLLOW_84_in_date_today_macro_expression2827); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal289_tree = (Object)adaptor.create(string_literal289);
			adaptor.addChild(root_0, string_literal289_tree);
			}

			char_literal290=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_date_today_macro_expression2829); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal290_tree = (Object)adaptor.create(char_literal290);
			adaptor.addChild(root_0, char_literal290_tree);
			}

			pushFollow(FOLLOW_path_expression_in_date_today_macro_expression2831);
			path_expression291=path_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression291.getTree());

			// JPA2.g:305:36: ( ',' 'USER_TIMEZONE' )?
			int alt81=2;
			int LA81_0 = input.LA(1);
			if ( (LA81_0==66) ) {
				alt81=1;
			}
			switch (alt81) {
				case 1 :
					// JPA2.g:305:37: ',' 'USER_TIMEZONE'
					{
					char_literal292=(Token)match(input,66,FOLLOW_66_in_date_today_macro_expression2834); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal292_tree = (Object)adaptor.create(char_literal292);
					adaptor.addChild(root_0, char_literal292_tree);
					}

					string_literal293=(Token)match(input,141,FOLLOW_141_in_date_today_macro_expression2836); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal293_tree = (Object)adaptor.create(string_literal293);
					adaptor.addChild(root_0, string_literal293_tree);
					}

					}
					break;

			}

			char_literal294=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_date_today_macro_expression2840); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal294_tree = (Object)adaptor.create(char_literal294);
			adaptor.addChild(root_0, char_literal294_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "date_today_macro_expression"


	public static class custom_date_between_macro_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "custom_date_between_macro_expression"
	// JPA2.g:307:1: custom_date_between_macro_expression : '@DATEBETWEEN' '(' path_expression ',' ( path_expression | input_parameter | string_literal ) ',' ( path_expression | input_parameter | string_literal ) ( ',' 'USER_TIMEZONE' )? ')' ;
	public final JPA2Parser.custom_date_between_macro_expression_return custom_date_between_macro_expression() throws RecognitionException {
		JPA2Parser.custom_date_between_macro_expression_return retval = new JPA2Parser.custom_date_between_macro_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal295=null;
		Token char_literal296=null;
		Token char_literal298=null;
		Token char_literal302=null;
		Token char_literal306=null;
		Token string_literal307=null;
		Token char_literal308=null;
		ParserRuleReturnScope path_expression297 =null;
		ParserRuleReturnScope path_expression299 =null;
		ParserRuleReturnScope input_parameter300 =null;
		ParserRuleReturnScope string_literal301 =null;
		ParserRuleReturnScope path_expression303 =null;
		ParserRuleReturnScope input_parameter304 =null;
		ParserRuleReturnScope string_literal305 =null;

		Object string_literal295_tree=null;
		Object char_literal296_tree=null;
		Object char_literal298_tree=null;
		Object char_literal302_tree=null;
		Object char_literal306_tree=null;
		Object string_literal307_tree=null;
		Object char_literal308_tree=null;

		try {
			// JPA2.g:308:5: ( '@DATEBETWEEN' '(' path_expression ',' ( path_expression | input_parameter | string_literal ) ',' ( path_expression | input_parameter | string_literal ) ( ',' 'USER_TIMEZONE' )? ')' )
			// JPA2.g:308:7: '@DATEBETWEEN' '(' path_expression ',' ( path_expression | input_parameter | string_literal ) ',' ( path_expression | input_parameter | string_literal ) ( ',' 'USER_TIMEZONE' )? ')'
			{
			root_0 = (Object)adaptor.nil();


			string_literal295=(Token)match(input,81,FOLLOW_81_in_custom_date_between_macro_expression2852); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal295_tree = (Object)adaptor.create(string_literal295);
			adaptor.addChild(root_0, string_literal295_tree);
			}

			char_literal296=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_custom_date_between_macro_expression2854); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal296_tree = (Object)adaptor.create(char_literal296);
			adaptor.addChild(root_0, char_literal296_tree);
			}

			pushFollow(FOLLOW_path_expression_in_custom_date_between_macro_expression2856);
			path_expression297=path_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression297.getTree());

			char_literal298=(Token)match(input,66,FOLLOW_66_in_custom_date_between_macro_expression2858); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal298_tree = (Object)adaptor.create(char_literal298);
			adaptor.addChild(root_0, char_literal298_tree);
			}

			// JPA2.g:308:46: ( path_expression | input_parameter | string_literal )
			int alt82=3;
			switch ( input.LA(1) ) {
			case GROUP:
			case WORD:
				{
				alt82=1;
				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt82=2;
				}
				break;
			case STRING_LITERAL:
				{
				alt82=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 82, 0, input);
				throw nvae;
			}
			switch (alt82) {
				case 1 :
					// JPA2.g:308:47: path_expression
					{
					pushFollow(FOLLOW_path_expression_in_custom_date_between_macro_expression2861);
					path_expression299=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression299.getTree());

					}
					break;
				case 2 :
					// JPA2.g:308:65: input_parameter
					{
					pushFollow(FOLLOW_input_parameter_in_custom_date_between_macro_expression2865);
					input_parameter300=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter300.getTree());

					}
					break;
				case 3 :
					// JPA2.g:308:83: string_literal
					{
					pushFollow(FOLLOW_string_literal_in_custom_date_between_macro_expression2869);
					string_literal301=string_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_literal301.getTree());

					}
					break;

			}

			char_literal302=(Token)match(input,66,FOLLOW_66_in_custom_date_between_macro_expression2872); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal302_tree = (Object)adaptor.create(char_literal302);
			adaptor.addChild(root_0, char_literal302_tree);
			}

			// JPA2.g:308:103: ( path_expression | input_parameter | string_literal )
			int alt83=3;
			switch ( input.LA(1) ) {
			case GROUP:
			case WORD:
				{
				alt83=1;
				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt83=2;
				}
				break;
			case STRING_LITERAL:
				{
				alt83=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 83, 0, input);
				throw nvae;
			}
			switch (alt83) {
				case 1 :
					// JPA2.g:308:104: path_expression
					{
					pushFollow(FOLLOW_path_expression_in_custom_date_between_macro_expression2875);
					path_expression303=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression303.getTree());

					}
					break;
				case 2 :
					// JPA2.g:308:122: input_parameter
					{
					pushFollow(FOLLOW_input_parameter_in_custom_date_between_macro_expression2879);
					input_parameter304=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter304.getTree());

					}
					break;
				case 3 :
					// JPA2.g:308:140: string_literal
					{
					pushFollow(FOLLOW_string_literal_in_custom_date_between_macro_expression2883);
					string_literal305=string_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_literal305.getTree());

					}
					break;

			}

			// JPA2.g:308:156: ( ',' 'USER_TIMEZONE' )?
			int alt84=2;
			int LA84_0 = input.LA(1);
			if ( (LA84_0==66) ) {
				alt84=1;
			}
			switch (alt84) {
				case 1 :
					// JPA2.g:308:157: ',' 'USER_TIMEZONE'
					{
					char_literal306=(Token)match(input,66,FOLLOW_66_in_custom_date_between_macro_expression2887); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal306_tree = (Object)adaptor.create(char_literal306);
					adaptor.addChild(root_0, char_literal306_tree);
					}

					string_literal307=(Token)match(input,141,FOLLOW_141_in_custom_date_between_macro_expression2889); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal307_tree = (Object)adaptor.create(string_literal307);
					adaptor.addChild(root_0, string_literal307_tree);
					}

					}
					break;

			}

			char_literal308=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_custom_date_between_macro_expression2893); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal308_tree = (Object)adaptor.create(char_literal308);
			adaptor.addChild(root_0, char_literal308_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "custom_date_between_macro_expression"


	public static class between_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "between_expression"
	// JPA2.g:312:1: between_expression : ( arithmetic_expression ( 'NOT' )? 'BETWEEN' arithmetic_expression 'AND' arithmetic_expression | string_expression ( 'NOT' )? 'BETWEEN' string_expression 'AND' string_expression | datetime_expression ( 'NOT' )? 'BETWEEN' datetime_expression 'AND' datetime_expression );
	public final JPA2Parser.between_expression_return between_expression() throws RecognitionException {
		JPA2Parser.between_expression_return retval = new JPA2Parser.between_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal310=null;
		Token string_literal311=null;
		Token string_literal313=null;
		Token string_literal316=null;
		Token string_literal317=null;
		Token string_literal319=null;
		Token string_literal322=null;
		Token string_literal323=null;
		Token string_literal325=null;
		ParserRuleReturnScope arithmetic_expression309 =null;
		ParserRuleReturnScope arithmetic_expression312 =null;
		ParserRuleReturnScope arithmetic_expression314 =null;
		ParserRuleReturnScope string_expression315 =null;
		ParserRuleReturnScope string_expression318 =null;
		ParserRuleReturnScope string_expression320 =null;
		ParserRuleReturnScope datetime_expression321 =null;
		ParserRuleReturnScope datetime_expression324 =null;
		ParserRuleReturnScope datetime_expression326 =null;

		Object string_literal310_tree=null;
		Object string_literal311_tree=null;
		Object string_literal313_tree=null;
		Object string_literal316_tree=null;
		Object string_literal317_tree=null;
		Object string_literal319_tree=null;
		Object string_literal322_tree=null;
		Object string_literal323_tree=null;
		Object string_literal325_tree=null;

		try {
			// JPA2.g:313:5: ( arithmetic_expression ( 'NOT' )? 'BETWEEN' arithmetic_expression 'AND' arithmetic_expression | string_expression ( 'NOT' )? 'BETWEEN' string_expression 'AND' string_expression | datetime_expression ( 'NOT' )? 'BETWEEN' datetime_expression 'AND' datetime_expression )
			int alt88=3;
			switch ( input.LA(1) ) {
			case INT_NUMERAL:
			case 65:
			case 67:
			case 70:
			case 85:
			case 107:
			case 111:
			case 113:
			case 116:
			case 131:
			case 133:
				{
				alt88=1;
				}
				break;
			case WORD:
				{
				int LA88_2 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case LPAREN:
				{
				int LA88_5 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case 77:
				{
				int LA88_6 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case NAMED_PARAMETER:
				{
				int LA88_7 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case 63:
				{
				int LA88_8 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case COUNT:
				{
				int LA88_16 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
				{
				int LA88_17 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case 105:
				{
				int LA88_18 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case CASE:
				{
				int LA88_19 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case 91:
				{
				int LA88_20 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case 121:
				{
				int LA88_21 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case 90:
				{
				int LA88_22 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case 103:
				{
				int LA88_23 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case 83:
				{
				int LA88_24 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			case LOWER:
			case STRING_LITERAL:
			case 92:
			case 134:
			case 137:
			case 140:
				{
				alt88=2;
				}
				break;
			case 93:
			case 94:
			case 95:
				{
				alt88=3;
				}
				break;
			case GROUP:
				{
				int LA88_32 = input.LA(2);
				if ( (synpred144_JPA2()) ) {
					alt88=1;
				}
				else if ( (synpred146_JPA2()) ) {
					alt88=2;
				}
				else if ( (true) ) {
					alt88=3;
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 88, 0, input);
				throw nvae;
			}
			switch (alt88) {
				case 1 :
					// JPA2.g:313:7: arithmetic_expression ( 'NOT' )? 'BETWEEN' arithmetic_expression 'AND' arithmetic_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_arithmetic_expression_in_between_expression2907);
					arithmetic_expression309=arithmetic_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression309.getTree());

					// JPA2.g:313:29: ( 'NOT' )?
					int alt85=2;
					int LA85_0 = input.LA(1);
					if ( (LA85_0==NOT) ) {
						alt85=1;
					}
					switch (alt85) {
						case 1 :
							// JPA2.g:313:30: 'NOT'
							{
							string_literal310=(Token)match(input,NOT,FOLLOW_NOT_in_between_expression2910); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							string_literal310_tree = (Object)adaptor.create(string_literal310);
							adaptor.addChild(root_0, string_literal310_tree);
							}

							}
							break;

					}

					string_literal311=(Token)match(input,88,FOLLOW_88_in_between_expression2914); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal311_tree = (Object)adaptor.create(string_literal311);
					adaptor.addChild(root_0, string_literal311_tree);
					}

					pushFollow(FOLLOW_arithmetic_expression_in_between_expression2916);
					arithmetic_expression312=arithmetic_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression312.getTree());

					string_literal313=(Token)match(input,AND,FOLLOW_AND_in_between_expression2918); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal313_tree = (Object)adaptor.create(string_literal313);
					adaptor.addChild(root_0, string_literal313_tree);
					}

					pushFollow(FOLLOW_arithmetic_expression_in_between_expression2920);
					arithmetic_expression314=arithmetic_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression314.getTree());

					}
					break;
				case 2 :
					// JPA2.g:314:7: string_expression ( 'NOT' )? 'BETWEEN' string_expression 'AND' string_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_string_expression_in_between_expression2928);
					string_expression315=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression315.getTree());

					// JPA2.g:314:25: ( 'NOT' )?
					int alt86=2;
					int LA86_0 = input.LA(1);
					if ( (LA86_0==NOT) ) {
						alt86=1;
					}
					switch (alt86) {
						case 1 :
							// JPA2.g:314:26: 'NOT'
							{
							string_literal316=(Token)match(input,NOT,FOLLOW_NOT_in_between_expression2931); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							string_literal316_tree = (Object)adaptor.create(string_literal316);
							adaptor.addChild(root_0, string_literal316_tree);
							}

							}
							break;

					}

					string_literal317=(Token)match(input,88,FOLLOW_88_in_between_expression2935); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal317_tree = (Object)adaptor.create(string_literal317);
					adaptor.addChild(root_0, string_literal317_tree);
					}

					pushFollow(FOLLOW_string_expression_in_between_expression2937);
					string_expression318=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression318.getTree());

					string_literal319=(Token)match(input,AND,FOLLOW_AND_in_between_expression2939); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal319_tree = (Object)adaptor.create(string_literal319);
					adaptor.addChild(root_0, string_literal319_tree);
					}

					pushFollow(FOLLOW_string_expression_in_between_expression2941);
					string_expression320=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression320.getTree());

					}
					break;
				case 3 :
					// JPA2.g:315:7: datetime_expression ( 'NOT' )? 'BETWEEN' datetime_expression 'AND' datetime_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_datetime_expression_in_between_expression2949);
					datetime_expression321=datetime_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, datetime_expression321.getTree());

					// JPA2.g:315:27: ( 'NOT' )?
					int alt87=2;
					int LA87_0 = input.LA(1);
					if ( (LA87_0==NOT) ) {
						alt87=1;
					}
					switch (alt87) {
						case 1 :
							// JPA2.g:315:28: 'NOT'
							{
							string_literal322=(Token)match(input,NOT,FOLLOW_NOT_in_between_expression2952); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							string_literal322_tree = (Object)adaptor.create(string_literal322);
							adaptor.addChild(root_0, string_literal322_tree);
							}

							}
							break;

					}

					string_literal323=(Token)match(input,88,FOLLOW_88_in_between_expression2956); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal323_tree = (Object)adaptor.create(string_literal323);
					adaptor.addChild(root_0, string_literal323_tree);
					}

					pushFollow(FOLLOW_datetime_expression_in_between_expression2958);
					datetime_expression324=datetime_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, datetime_expression324.getTree());

					string_literal325=(Token)match(input,AND,FOLLOW_AND_in_between_expression2960); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal325_tree = (Object)adaptor.create(string_literal325);
					adaptor.addChild(root_0, string_literal325_tree);
					}

					pushFollow(FOLLOW_datetime_expression_in_between_expression2962);
					datetime_expression326=datetime_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, datetime_expression326.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "between_expression"


	public static class in_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "in_expression"
	// JPA2.g:316:1: in_expression : ( path_expression | type_discriminator | identification_variable | extract_function ) ( NOT )? IN ( '(' in_item ( ',' in_item )* ')' | subquery | collection_valued_input_parameter | '(' path_expression ')' ) ;
	public final JPA2Parser.in_expression_return in_expression() throws RecognitionException {
		JPA2Parser.in_expression_return retval = new JPA2Parser.in_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token NOT331=null;
		Token IN332=null;
		Token char_literal333=null;
		Token char_literal335=null;
		Token char_literal337=null;
		Token char_literal340=null;
		Token char_literal342=null;
		ParserRuleReturnScope path_expression327 =null;
		ParserRuleReturnScope type_discriminator328 =null;
		ParserRuleReturnScope identification_variable329 =null;
		ParserRuleReturnScope extract_function330 =null;
		ParserRuleReturnScope in_item334 =null;
		ParserRuleReturnScope in_item336 =null;
		ParserRuleReturnScope subquery338 =null;
		ParserRuleReturnScope collection_valued_input_parameter339 =null;
		ParserRuleReturnScope path_expression341 =null;

		Object NOT331_tree=null;
		Object IN332_tree=null;
		Object char_literal333_tree=null;
		Object char_literal335_tree=null;
		Object char_literal337_tree=null;
		Object char_literal340_tree=null;
		Object char_literal342_tree=null;

		try {
			// JPA2.g:317:5: ( ( path_expression | type_discriminator | identification_variable | extract_function ) ( NOT )? IN ( '(' in_item ( ',' in_item )* ')' | subquery | collection_valued_input_parameter | '(' path_expression ')' ) )
			// JPA2.g:317:7: ( path_expression | type_discriminator | identification_variable | extract_function ) ( NOT )? IN ( '(' in_item ( ',' in_item )* ')' | subquery | collection_valued_input_parameter | '(' path_expression ')' )
			{
			root_0 = (Object)adaptor.nil();


			// JPA2.g:317:7: ( path_expression | type_discriminator | identification_variable | extract_function )
			int alt89=4;
			switch ( input.LA(1) ) {
			case GROUP:
			case WORD:
				{
				int LA89_1 = input.LA(2);
				if ( (LA89_1==68) ) {
					alt89=1;
				}
				else if ( (LA89_1==IN||LA89_1==NOT) ) {
					alt89=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 89, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 138:
				{
				alt89=2;
				}
				break;
			case 103:
				{
				alt89=4;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 89, 0, input);
				throw nvae;
			}
			switch (alt89) {
				case 1 :
					// JPA2.g:317:8: path_expression
					{
					pushFollow(FOLLOW_path_expression_in_in_expression2974);
					path_expression327=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression327.getTree());

					}
					break;
				case 2 :
					// JPA2.g:317:26: type_discriminator
					{
					pushFollow(FOLLOW_type_discriminator_in_in_expression2978);
					type_discriminator328=type_discriminator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, type_discriminator328.getTree());

					}
					break;
				case 3 :
					// JPA2.g:317:47: identification_variable
					{
					pushFollow(FOLLOW_identification_variable_in_in_expression2982);
					identification_variable329=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable329.getTree());

					}
					break;
				case 4 :
					// JPA2.g:317:73: extract_function
					{
					pushFollow(FOLLOW_extract_function_in_in_expression2986);
					extract_function330=extract_function();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, extract_function330.getTree());

					}
					break;

			}

			// JPA2.g:317:91: ( NOT )?
			int alt90=2;
			int LA90_0 = input.LA(1);
			if ( (LA90_0==NOT) ) {
				alt90=1;
			}
			switch (alt90) {
				case 1 :
					// JPA2.g:317:92: NOT
					{
					NOT331=(Token)match(input,NOT,FOLLOW_NOT_in_in_expression2990); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					NOT331_tree = (Object)adaptor.create(NOT331);
					adaptor.addChild(root_0, NOT331_tree);
					}

					}
					break;

			}

			IN332=(Token)match(input,IN,FOLLOW_IN_in_in_expression2994); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			IN332_tree = (Object)adaptor.create(IN332);
			adaptor.addChild(root_0, IN332_tree);
			}

			// JPA2.g:318:13: ( '(' in_item ( ',' in_item )* ')' | subquery | collection_valued_input_parameter | '(' path_expression ')' )
			int alt92=4;
			int LA92_0 = input.LA(1);
			if ( (LA92_0==LPAREN) ) {
				switch ( input.LA(2) ) {
				case 130:
					{
					alt92=2;
					}
					break;
				case INT_NUMERAL:
				case NAMED_PARAMETER:
				case STRING_LITERAL:
				case 63:
				case 70:
				case 77:
				case 83:
					{
					alt92=1;
					}
					break;
				case GROUP:
				case WORD:
					{
					alt92=4;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 92, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
			}
			else if ( (LA92_0==NAMED_PARAMETER||LA92_0==63||LA92_0==77) ) {
				alt92=3;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 92, 0, input);
				throw nvae;
			}

			switch (alt92) {
				case 1 :
					// JPA2.g:318:15: '(' in_item ( ',' in_item )* ')'
					{
					char_literal333=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_in_expression3010); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal333_tree = (Object)adaptor.create(char_literal333);
					adaptor.addChild(root_0, char_literal333_tree);
					}

					pushFollow(FOLLOW_in_item_in_in_expression3012);
					in_item334=in_item();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, in_item334.getTree());

					// JPA2.g:318:27: ( ',' in_item )*
					loop91:
					while (true) {
						int alt91=2;
						int LA91_0 = input.LA(1);
						if ( (LA91_0==66) ) {
							alt91=1;
						}

						switch (alt91) {
						case 1 :
							// JPA2.g:318:28: ',' in_item
							{
							char_literal335=(Token)match(input,66,FOLLOW_66_in_in_expression3015); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							char_literal335_tree = (Object)adaptor.create(char_literal335);
							adaptor.addChild(root_0, char_literal335_tree);
							}

							pushFollow(FOLLOW_in_item_in_in_expression3017);
							in_item336=in_item();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, in_item336.getTree());

							}
							break;

						default :
							break loop91;
						}
					}

					char_literal337=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_in_expression3021); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal337_tree = (Object)adaptor.create(char_literal337);
					adaptor.addChild(root_0, char_literal337_tree);
					}

					}
					break;
				case 2 :
					// JPA2.g:319:15: subquery
					{
					pushFollow(FOLLOW_subquery_in_in_expression3037);
					subquery338=subquery();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, subquery338.getTree());

					}
					break;
				case 3 :
					// JPA2.g:320:15: collection_valued_input_parameter
					{
					pushFollow(FOLLOW_collection_valued_input_parameter_in_in_expression3053);
					collection_valued_input_parameter339=collection_valued_input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, collection_valued_input_parameter339.getTree());

					}
					break;
				case 4 :
					// JPA2.g:321:15: '(' path_expression ')'
					{
					char_literal340=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_in_expression3069); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal340_tree = (Object)adaptor.create(char_literal340);
					adaptor.addChild(root_0, char_literal340_tree);
					}

					pushFollow(FOLLOW_path_expression_in_in_expression3071);
					path_expression341=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression341.getTree());

					char_literal342=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_in_expression3073); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal342_tree = (Object)adaptor.create(char_literal342);
					adaptor.addChild(root_0, char_literal342_tree);
					}

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "in_expression"


	public static class in_item_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "in_item"
	// JPA2.g:327:1: in_item : ( string_literal | numeric_literal | single_valued_input_parameter | enum_function );
	public final JPA2Parser.in_item_return in_item() throws RecognitionException {
		JPA2Parser.in_item_return retval = new JPA2Parser.in_item_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope string_literal343 =null;
		ParserRuleReturnScope numeric_literal344 =null;
		ParserRuleReturnScope single_valued_input_parameter345 =null;
		ParserRuleReturnScope enum_function346 =null;


		try {
			// JPA2.g:328:5: ( string_literal | numeric_literal | single_valued_input_parameter | enum_function )
			int alt93=4;
			switch ( input.LA(1) ) {
			case STRING_LITERAL:
				{
				alt93=1;
				}
				break;
			case INT_NUMERAL:
			case 70:
				{
				alt93=2;
				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt93=3;
				}
				break;
			case 83:
				{
				alt93=4;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 93, 0, input);
				throw nvae;
			}
			switch (alt93) {
				case 1 :
					// JPA2.g:328:7: string_literal
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_string_literal_in_in_item3101);
					string_literal343=string_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_literal343.getTree());

					}
					break;
				case 2 :
					// JPA2.g:328:24: numeric_literal
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_numeric_literal_in_in_item3105);
					numeric_literal344=numeric_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, numeric_literal344.getTree());

					}
					break;
				case 3 :
					// JPA2.g:328:42: single_valued_input_parameter
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_single_valued_input_parameter_in_in_item3109);
					single_valued_input_parameter345=single_valued_input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, single_valued_input_parameter345.getTree());

					}
					break;
				case 4 :
					// JPA2.g:328:74: enum_function
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_enum_function_in_in_item3113);
					enum_function346=enum_function();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, enum_function346.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "in_item"


	public static class like_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "like_expression"
	// JPA2.g:329:1: like_expression : string_expression ( 'NOT' )? 'LIKE' ( string_expression | pattern_value | input_parameter ) ( 'ESCAPE' escape_character )? ;
	public final JPA2Parser.like_expression_return like_expression() throws RecognitionException {
		JPA2Parser.like_expression_return retval = new JPA2Parser.like_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal348=null;
		Token string_literal349=null;
		Token string_literal353=null;
		ParserRuleReturnScope string_expression347 =null;
		ParserRuleReturnScope string_expression350 =null;
		ParserRuleReturnScope pattern_value351 =null;
		ParserRuleReturnScope input_parameter352 =null;
		ParserRuleReturnScope escape_character354 =null;

		Object string_literal348_tree=null;
		Object string_literal349_tree=null;
		Object string_literal353_tree=null;

		try {
			// JPA2.g:330:5: ( string_expression ( 'NOT' )? 'LIKE' ( string_expression | pattern_value | input_parameter ) ( 'ESCAPE' escape_character )? )
			// JPA2.g:330:7: string_expression ( 'NOT' )? 'LIKE' ( string_expression | pattern_value | input_parameter ) ( 'ESCAPE' escape_character )?
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_string_expression_in_like_expression3124);
			string_expression347=string_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression347.getTree());

			// JPA2.g:330:25: ( 'NOT' )?
			int alt94=2;
			int LA94_0 = input.LA(1);
			if ( (LA94_0==NOT) ) {
				alt94=1;
			}
			switch (alt94) {
				case 1 :
					// JPA2.g:330:26: 'NOT'
					{
					string_literal348=(Token)match(input,NOT,FOLLOW_NOT_in_like_expression3127); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal348_tree = (Object)adaptor.create(string_literal348);
					adaptor.addChild(root_0, string_literal348_tree);
					}

					}
					break;

			}

			string_literal349=(Token)match(input,112,FOLLOW_112_in_like_expression3131); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal349_tree = (Object)adaptor.create(string_literal349);
			adaptor.addChild(root_0, string_literal349_tree);
			}

			// JPA2.g:330:41: ( string_expression | pattern_value | input_parameter )
			int alt95=3;
			switch ( input.LA(1) ) {
			case AVG:
			case CASE:
			case COUNT:
			case GROUP:
			case LOWER:
			case LPAREN:
			case MAX:
			case MIN:
			case SUM:
			case WORD:
			case 83:
			case 90:
			case 91:
			case 92:
			case 103:
			case 105:
			case 121:
			case 134:
			case 137:
			case 140:
				{
				alt95=1;
				}
				break;
			case STRING_LITERAL:
				{
				int LA95_2 = input.LA(2);
				if ( (synpred160_JPA2()) ) {
					alt95=1;
				}
				else if ( (synpred161_JPA2()) ) {
					alt95=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 95, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 77:
				{
				int LA95_3 = input.LA(2);
				if ( (LA95_3==70) ) {
					int LA95_7 = input.LA(3);
					if ( (LA95_7==INT_NUMERAL) ) {
						int LA95_11 = input.LA(4);
						if ( (synpred160_JPA2()) ) {
							alt95=1;
						}
						else if ( (true) ) {
							alt95=3;
						}

					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 95, 7, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA95_3==INT_NUMERAL) ) {
					int LA95_8 = input.LA(3);
					if ( (synpred160_JPA2()) ) {
						alt95=1;
					}
					else if ( (true) ) {
						alt95=3;
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 95, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case NAMED_PARAMETER:
				{
				int LA95_4 = input.LA(2);
				if ( (synpred160_JPA2()) ) {
					alt95=1;
				}
				else if ( (true) ) {
					alt95=3;
				}

				}
				break;
			case 63:
				{
				int LA95_5 = input.LA(2);
				if ( (LA95_5==WORD) ) {
					int LA95_10 = input.LA(3);
					if ( (LA95_10==148) ) {
						int LA95_12 = input.LA(4);
						if ( (synpred160_JPA2()) ) {
							alt95=1;
						}
						else if ( (true) ) {
							alt95=3;
						}

					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 95, 10, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 95, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 95, 0, input);
				throw nvae;
			}
			switch (alt95) {
				case 1 :
					// JPA2.g:330:42: string_expression
					{
					pushFollow(FOLLOW_string_expression_in_like_expression3134);
					string_expression350=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression350.getTree());

					}
					break;
				case 2 :
					// JPA2.g:330:62: pattern_value
					{
					pushFollow(FOLLOW_pattern_value_in_like_expression3138);
					pattern_value351=pattern_value();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, pattern_value351.getTree());

					}
					break;
				case 3 :
					// JPA2.g:330:78: input_parameter
					{
					pushFollow(FOLLOW_input_parameter_in_like_expression3142);
					input_parameter352=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter352.getTree());

					}
					break;

			}

			// JPA2.g:330:94: ( 'ESCAPE' escape_character )?
			int alt96=2;
			int LA96_0 = input.LA(1);
			if ( (LA96_0==101) ) {
				alt96=1;
			}
			switch (alt96) {
				case 1 :
					// JPA2.g:330:95: 'ESCAPE' escape_character
					{
					string_literal353=(Token)match(input,101,FOLLOW_101_in_like_expression3145); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal353_tree = (Object)adaptor.create(string_literal353);
					adaptor.addChild(root_0, string_literal353_tree);
					}

					pushFollow(FOLLOW_escape_character_in_like_expression3147);
					escape_character354=escape_character();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, escape_character354.getTree());

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "like_expression"


	public static class null_comparison_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "null_comparison_expression"
	// JPA2.g:331:1: null_comparison_expression : ( path_expression | input_parameter | join_association_path_expression ) 'IS' ( 'NOT' )? 'NULL' ;
	public final JPA2Parser.null_comparison_expression_return null_comparison_expression() throws RecognitionException {
		JPA2Parser.null_comparison_expression_return retval = new JPA2Parser.null_comparison_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal358=null;
		Token string_literal359=null;
		Token string_literal360=null;
		ParserRuleReturnScope path_expression355 =null;
		ParserRuleReturnScope input_parameter356 =null;
		ParserRuleReturnScope join_association_path_expression357 =null;

		Object string_literal358_tree=null;
		Object string_literal359_tree=null;
		Object string_literal360_tree=null;

		try {
			// JPA2.g:332:5: ( ( path_expression | input_parameter | join_association_path_expression ) 'IS' ( 'NOT' )? 'NULL' )
			// JPA2.g:332:7: ( path_expression | input_parameter | join_association_path_expression ) 'IS' ( 'NOT' )? 'NULL'
			{
			root_0 = (Object)adaptor.nil();


			// JPA2.g:332:7: ( path_expression | input_parameter | join_association_path_expression )
			int alt97=3;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA97_1 = input.LA(2);
				if ( (LA97_1==68) ) {
					int LA97_5 = input.LA(3);
					if ( (synpred163_JPA2()) ) {
						alt97=1;
					}
					else if ( (true) ) {
						alt97=3;
					}

				}
				else if ( (LA97_1==108) ) {
					alt97=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 97, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt97=2;
				}
				break;
			case 136:
				{
				alt97=3;
				}
				break;
			case GROUP:
				{
				int LA97_4 = input.LA(2);
				if ( (LA97_4==68) ) {
					int LA97_6 = input.LA(3);
					if ( (synpred163_JPA2()) ) {
						alt97=1;
					}
					else if ( (true) ) {
						alt97=3;
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 97, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 97, 0, input);
				throw nvae;
			}
			switch (alt97) {
				case 1 :
					// JPA2.g:332:8: path_expression
					{
					pushFollow(FOLLOW_path_expression_in_null_comparison_expression3161);
					path_expression355=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression355.getTree());

					}
					break;
				case 2 :
					// JPA2.g:332:26: input_parameter
					{
					pushFollow(FOLLOW_input_parameter_in_null_comparison_expression3165);
					input_parameter356=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter356.getTree());

					}
					break;
				case 3 :
					// JPA2.g:332:44: join_association_path_expression
					{
					pushFollow(FOLLOW_join_association_path_expression_in_null_comparison_expression3169);
					join_association_path_expression357=join_association_path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, join_association_path_expression357.getTree());

					}
					break;

			}

			string_literal358=(Token)match(input,108,FOLLOW_108_in_null_comparison_expression3172); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal358_tree = (Object)adaptor.create(string_literal358);
			adaptor.addChild(root_0, string_literal358_tree);
			}

			// JPA2.g:332:83: ( 'NOT' )?
			int alt98=2;
			int LA98_0 = input.LA(1);
			if ( (LA98_0==NOT) ) {
				alt98=1;
			}
			switch (alt98) {
				case 1 :
					// JPA2.g:332:84: 'NOT'
					{
					string_literal359=(Token)match(input,NOT,FOLLOW_NOT_in_null_comparison_expression3175); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal359_tree = (Object)adaptor.create(string_literal359);
					adaptor.addChild(root_0, string_literal359_tree);
					}

					}
					break;

			}

			string_literal360=(Token)match(input,120,FOLLOW_120_in_null_comparison_expression3179); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal360_tree = (Object)adaptor.create(string_literal360);
			adaptor.addChild(root_0, string_literal360_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "null_comparison_expression"


	public static class empty_collection_comparison_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "empty_collection_comparison_expression"
	// JPA2.g:333:1: empty_collection_comparison_expression : path_expression 'IS' ( 'NOT' )? 'EMPTY' ;
	public final JPA2Parser.empty_collection_comparison_expression_return empty_collection_comparison_expression() throws RecognitionException {
		JPA2Parser.empty_collection_comparison_expression_return retval = new JPA2Parser.empty_collection_comparison_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal362=null;
		Token string_literal363=null;
		Token string_literal364=null;
		ParserRuleReturnScope path_expression361 =null;

		Object string_literal362_tree=null;
		Object string_literal363_tree=null;
		Object string_literal364_tree=null;

		try {
			// JPA2.g:334:5: ( path_expression 'IS' ( 'NOT' )? 'EMPTY' )
			// JPA2.g:334:7: path_expression 'IS' ( 'NOT' )? 'EMPTY'
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_path_expression_in_empty_collection_comparison_expression3190);
			path_expression361=path_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression361.getTree());

			string_literal362=(Token)match(input,108,FOLLOW_108_in_empty_collection_comparison_expression3192); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal362_tree = (Object)adaptor.create(string_literal362);
			adaptor.addChild(root_0, string_literal362_tree);
			}

			// JPA2.g:334:28: ( 'NOT' )?
			int alt99=2;
			int LA99_0 = input.LA(1);
			if ( (LA99_0==NOT) ) {
				alt99=1;
			}
			switch (alt99) {
				case 1 :
					// JPA2.g:334:29: 'NOT'
					{
					string_literal363=(Token)match(input,NOT,FOLLOW_NOT_in_empty_collection_comparison_expression3195); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal363_tree = (Object)adaptor.create(string_literal363);
					adaptor.addChild(root_0, string_literal363_tree);
					}

					}
					break;

			}

			string_literal364=(Token)match(input,98,FOLLOW_98_in_empty_collection_comparison_expression3199); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal364_tree = (Object)adaptor.create(string_literal364);
			adaptor.addChild(root_0, string_literal364_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "empty_collection_comparison_expression"


	public static class collection_member_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "collection_member_expression"
	// JPA2.g:335:1: collection_member_expression : entity_or_value_expression ( 'NOT' )? 'MEMBER' ( 'OF' )? path_expression ;
	public final JPA2Parser.collection_member_expression_return collection_member_expression() throws RecognitionException {
		JPA2Parser.collection_member_expression_return retval = new JPA2Parser.collection_member_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal366=null;
		Token string_literal367=null;
		Token string_literal368=null;
		ParserRuleReturnScope entity_or_value_expression365 =null;
		ParserRuleReturnScope path_expression369 =null;

		Object string_literal366_tree=null;
		Object string_literal367_tree=null;
		Object string_literal368_tree=null;

		try {
			// JPA2.g:336:5: ( entity_or_value_expression ( 'NOT' )? 'MEMBER' ( 'OF' )? path_expression )
			// JPA2.g:336:7: entity_or_value_expression ( 'NOT' )? 'MEMBER' ( 'OF' )? path_expression
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_entity_or_value_expression_in_collection_member_expression3210);
			entity_or_value_expression365=entity_or_value_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, entity_or_value_expression365.getTree());

			// JPA2.g:336:35: ( 'NOT' )?
			int alt100=2;
			int LA100_0 = input.LA(1);
			if ( (LA100_0==NOT) ) {
				alt100=1;
			}
			switch (alt100) {
				case 1 :
					// JPA2.g:336:36: 'NOT'
					{
					string_literal366=(Token)match(input,NOT,FOLLOW_NOT_in_collection_member_expression3214); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal366_tree = (Object)adaptor.create(string_literal366);
					adaptor.addChild(root_0, string_literal366_tree);
					}

					}
					break;

			}

			string_literal367=(Token)match(input,114,FOLLOW_114_in_collection_member_expression3218); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal367_tree = (Object)adaptor.create(string_literal367);
			adaptor.addChild(root_0, string_literal367_tree);
			}

			// JPA2.g:336:53: ( 'OF' )?
			int alt101=2;
			int LA101_0 = input.LA(1);
			if ( (LA101_0==125) ) {
				alt101=1;
			}
			switch (alt101) {
				case 1 :
					// JPA2.g:336:54: 'OF'
					{
					string_literal368=(Token)match(input,125,FOLLOW_125_in_collection_member_expression3221); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal368_tree = (Object)adaptor.create(string_literal368);
					adaptor.addChild(root_0, string_literal368_tree);
					}

					}
					break;

			}

			pushFollow(FOLLOW_path_expression_in_collection_member_expression3225);
			path_expression369=path_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression369.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "collection_member_expression"


	public static class entity_or_value_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "entity_or_value_expression"
	// JPA2.g:337:1: entity_or_value_expression : ( path_expression | simple_entity_or_value_expression | subquery );
	public final JPA2Parser.entity_or_value_expression_return entity_or_value_expression() throws RecognitionException {
		JPA2Parser.entity_or_value_expression_return retval = new JPA2Parser.entity_or_value_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope path_expression370 =null;
		ParserRuleReturnScope simple_entity_or_value_expression371 =null;
		ParserRuleReturnScope subquery372 =null;


		try {
			// JPA2.g:338:5: ( path_expression | simple_entity_or_value_expression | subquery )
			int alt102=3;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA102_1 = input.LA(2);
				if ( (LA102_1==68) ) {
					alt102=1;
				}
				else if ( (LA102_1==NOT||LA102_1==114) ) {
					alt102=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 102, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt102=2;
				}
				break;
			case GROUP:
				{
				int LA102_3 = input.LA(2);
				if ( (LA102_3==68) ) {
					alt102=1;
				}
				else if ( (LA102_3==NOT||LA102_3==114) ) {
					alt102=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 102, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LPAREN:
				{
				alt102=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 102, 0, input);
				throw nvae;
			}
			switch (alt102) {
				case 1 :
					// JPA2.g:338:7: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_entity_or_value_expression3236);
					path_expression370=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression370.getTree());

					}
					break;
				case 2 :
					// JPA2.g:339:7: simple_entity_or_value_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_simple_entity_or_value_expression_in_entity_or_value_expression3244);
					simple_entity_or_value_expression371=simple_entity_or_value_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, simple_entity_or_value_expression371.getTree());

					}
					break;
				case 3 :
					// JPA2.g:340:7: subquery
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_subquery_in_entity_or_value_expression3252);
					subquery372=subquery();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, subquery372.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "entity_or_value_expression"


	public static class simple_entity_or_value_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "simple_entity_or_value_expression"
	// JPA2.g:341:1: simple_entity_or_value_expression : ( identification_variable | input_parameter | literal );
	public final JPA2Parser.simple_entity_or_value_expression_return simple_entity_or_value_expression() throws RecognitionException {
		JPA2Parser.simple_entity_or_value_expression_return retval = new JPA2Parser.simple_entity_or_value_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope identification_variable373 =null;
		ParserRuleReturnScope input_parameter374 =null;
		ParserRuleReturnScope literal375 =null;


		try {
			// JPA2.g:342:5: ( identification_variable | input_parameter | literal )
			int alt103=3;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA103_1 = input.LA(2);
				if ( (synpred171_JPA2()) ) {
					alt103=1;
				}
				else if ( (true) ) {
					alt103=3;
				}

				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt103=2;
				}
				break;
			case GROUP:
				{
				alt103=1;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 103, 0, input);
				throw nvae;
			}
			switch (alt103) {
				case 1 :
					// JPA2.g:342:7: identification_variable
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_identification_variable_in_simple_entity_or_value_expression3263);
					identification_variable373=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable373.getTree());

					}
					break;
				case 2 :
					// JPA2.g:343:7: input_parameter
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_input_parameter_in_simple_entity_or_value_expression3271);
					input_parameter374=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter374.getTree());

					}
					break;
				case 3 :
					// JPA2.g:344:7: literal
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_literal_in_simple_entity_or_value_expression3279);
					literal375=literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, literal375.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "simple_entity_or_value_expression"


	public static class exists_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "exists_expression"
	// JPA2.g:345:1: exists_expression : ( 'NOT' )? 'EXISTS' subquery ;
	public final JPA2Parser.exists_expression_return exists_expression() throws RecognitionException {
		JPA2Parser.exists_expression_return retval = new JPA2Parser.exists_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal376=null;
		Token string_literal377=null;
		ParserRuleReturnScope subquery378 =null;

		Object string_literal376_tree=null;
		Object string_literal377_tree=null;

		try {
			// JPA2.g:346:5: ( ( 'NOT' )? 'EXISTS' subquery )
			// JPA2.g:346:7: ( 'NOT' )? 'EXISTS' subquery
			{
			root_0 = (Object)adaptor.nil();


			// JPA2.g:346:7: ( 'NOT' )?
			int alt104=2;
			int LA104_0 = input.LA(1);
			if ( (LA104_0==NOT) ) {
				alt104=1;
			}
			switch (alt104) {
				case 1 :
					// JPA2.g:346:8: 'NOT'
					{
					string_literal376=(Token)match(input,NOT,FOLLOW_NOT_in_exists_expression3291); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal376_tree = (Object)adaptor.create(string_literal376);
					adaptor.addChild(root_0, string_literal376_tree);
					}

					}
					break;

			}

			string_literal377=(Token)match(input,102,FOLLOW_102_in_exists_expression3295); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal377_tree = (Object)adaptor.create(string_literal377);
			adaptor.addChild(root_0, string_literal377_tree);
			}

			pushFollow(FOLLOW_subquery_in_exists_expression3297);
			subquery378=subquery();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, subquery378.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "exists_expression"


	public static class all_or_any_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "all_or_any_expression"
	// JPA2.g:347:1: all_or_any_expression : ( 'ALL' | 'ANY' | 'SOME' ) subquery ;
	public final JPA2Parser.all_or_any_expression_return all_or_any_expression() throws RecognitionException {
		JPA2Parser.all_or_any_expression_return retval = new JPA2Parser.all_or_any_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set379=null;
		ParserRuleReturnScope subquery380 =null;

		Object set379_tree=null;

		try {
			// JPA2.g:348:5: ( ( 'ALL' | 'ANY' | 'SOME' ) subquery )
			// JPA2.g:348:7: ( 'ALL' | 'ANY' | 'SOME' ) subquery
			{
			root_0 = (Object)adaptor.nil();


			set379=input.LT(1);
			if ( (input.LA(1) >= 86 && input.LA(1) <= 87)||input.LA(1)==132 ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set379));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			pushFollow(FOLLOW_subquery_in_all_or_any_expression3321);
			subquery380=subquery();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, subquery380.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "all_or_any_expression"


	public static class comparison_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "comparison_expression"
	// JPA2.g:349:1: comparison_expression : ( string_expression ( comparison_operator | 'REGEXP' ) ( string_expression | all_or_any_expression ) | boolean_expression ( '=' | '<>' ) ( boolean_expression | all_or_any_expression ) | enum_expression ( '=' | '<>' ) ( enum_expression | all_or_any_expression ) | datetime_expression comparison_operator ( datetime_expression | all_or_any_expression ) | entity_expression ( '=' | '<>' ) ( entity_expression | all_or_any_expression ) | entity_type_expression ( '=' | '<>' ) entity_type_expression | arithmetic_expression comparison_operator ( arithmetic_expression | all_or_any_expression ) );
	public final JPA2Parser.comparison_expression_return comparison_expression() throws RecognitionException {
		JPA2Parser.comparison_expression_return retval = new JPA2Parser.comparison_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal383=null;
		Token set387=null;
		Token set391=null;
		Token set399=null;
		Token set403=null;
		ParserRuleReturnScope string_expression381 =null;
		ParserRuleReturnScope comparison_operator382 =null;
		ParserRuleReturnScope string_expression384 =null;
		ParserRuleReturnScope all_or_any_expression385 =null;
		ParserRuleReturnScope boolean_expression386 =null;
		ParserRuleReturnScope boolean_expression388 =null;
		ParserRuleReturnScope all_or_any_expression389 =null;
		ParserRuleReturnScope enum_expression390 =null;
		ParserRuleReturnScope enum_expression392 =null;
		ParserRuleReturnScope all_or_any_expression393 =null;
		ParserRuleReturnScope datetime_expression394 =null;
		ParserRuleReturnScope comparison_operator395 =null;
		ParserRuleReturnScope datetime_expression396 =null;
		ParserRuleReturnScope all_or_any_expression397 =null;
		ParserRuleReturnScope entity_expression398 =null;
		ParserRuleReturnScope entity_expression400 =null;
		ParserRuleReturnScope all_or_any_expression401 =null;
		ParserRuleReturnScope entity_type_expression402 =null;
		ParserRuleReturnScope entity_type_expression404 =null;
		ParserRuleReturnScope arithmetic_expression405 =null;
		ParserRuleReturnScope comparison_operator406 =null;
		ParserRuleReturnScope arithmetic_expression407 =null;
		ParserRuleReturnScope all_or_any_expression408 =null;

		Object string_literal383_tree=null;
		Object set387_tree=null;
		Object set391_tree=null;
		Object set399_tree=null;
		Object set403_tree=null;

		try {
			// JPA2.g:350:5: ( string_expression ( comparison_operator | 'REGEXP' ) ( string_expression | all_or_any_expression ) | boolean_expression ( '=' | '<>' ) ( boolean_expression | all_or_any_expression ) | enum_expression ( '=' | '<>' ) ( enum_expression | all_or_any_expression ) | datetime_expression comparison_operator ( datetime_expression | all_or_any_expression ) | entity_expression ( '=' | '<>' ) ( entity_expression | all_or_any_expression ) | entity_type_expression ( '=' | '<>' ) entity_type_expression | arithmetic_expression comparison_operator ( arithmetic_expression | all_or_any_expression ) )
			int alt112=7;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA112_1 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred181_JPA2()) ) {
					alt112=2;
				}
				else if ( (synpred184_JPA2()) ) {
					alt112=3;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (synpred189_JPA2()) ) {
					alt112=5;
				}
				else if ( (synpred191_JPA2()) ) {
					alt112=6;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case LOWER:
			case STRING_LITERAL:
			case 92:
			case 134:
			case 137:
			case 140:
				{
				alt112=1;
				}
				break;
			case 77:
				{
				int LA112_3 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred181_JPA2()) ) {
					alt112=2;
				}
				else if ( (synpred184_JPA2()) ) {
					alt112=3;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (synpred189_JPA2()) ) {
					alt112=5;
				}
				else if ( (synpred191_JPA2()) ) {
					alt112=6;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case NAMED_PARAMETER:
				{
				int LA112_4 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred181_JPA2()) ) {
					alt112=2;
				}
				else if ( (synpred184_JPA2()) ) {
					alt112=3;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (synpred189_JPA2()) ) {
					alt112=5;
				}
				else if ( (synpred191_JPA2()) ) {
					alt112=6;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case 63:
				{
				int LA112_5 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred181_JPA2()) ) {
					alt112=2;
				}
				else if ( (synpred184_JPA2()) ) {
					alt112=3;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (synpred189_JPA2()) ) {
					alt112=5;
				}
				else if ( (synpred191_JPA2()) ) {
					alt112=6;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case COUNT:
				{
				int LA112_11 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
				{
				int LA112_12 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case 105:
				{
				int LA112_13 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred181_JPA2()) ) {
					alt112=2;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case CASE:
				{
				int LA112_14 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred181_JPA2()) ) {
					alt112=2;
				}
				else if ( (synpred184_JPA2()) ) {
					alt112=3;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case 91:
				{
				int LA112_15 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred181_JPA2()) ) {
					alt112=2;
				}
				else if ( (synpred184_JPA2()) ) {
					alt112=3;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case 121:
				{
				int LA112_16 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred181_JPA2()) ) {
					alt112=2;
				}
				else if ( (synpred184_JPA2()) ) {
					alt112=3;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case 90:
				{
				int LA112_17 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred181_JPA2()) ) {
					alt112=2;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case 103:
				{
				int LA112_18 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred181_JPA2()) ) {
					alt112=2;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case 83:
				{
				int LA112_19 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred181_JPA2()) ) {
					alt112=2;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case LPAREN:
				{
				int LA112_20 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred181_JPA2()) ) {
					alt112=2;
				}
				else if ( (synpred184_JPA2()) ) {
					alt112=3;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case 146:
			case 147:
				{
				alt112=2;
				}
				break;
			case GROUP:
				{
				int LA112_22 = input.LA(2);
				if ( (synpred178_JPA2()) ) {
					alt112=1;
				}
				else if ( (synpred181_JPA2()) ) {
					alt112=2;
				}
				else if ( (synpred184_JPA2()) ) {
					alt112=3;
				}
				else if ( (synpred186_JPA2()) ) {
					alt112=4;
				}
				else if ( (synpred189_JPA2()) ) {
					alt112=5;
				}
				else if ( (true) ) {
					alt112=7;
				}

				}
				break;
			case 93:
			case 94:
			case 95:
				{
				alt112=4;
				}
				break;
			case 138:
				{
				alt112=6;
				}
				break;
			case INT_NUMERAL:
			case 65:
			case 67:
			case 70:
			case 85:
			case 107:
			case 111:
			case 113:
			case 116:
			case 131:
			case 133:
				{
				alt112=7;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 112, 0, input);
				throw nvae;
			}
			switch (alt112) {
				case 1 :
					// JPA2.g:350:7: string_expression ( comparison_operator | 'REGEXP' ) ( string_expression | all_or_any_expression )
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_string_expression_in_comparison_expression3332);
					string_expression381=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression381.getTree());

					// JPA2.g:350:25: ( comparison_operator | 'REGEXP' )
					int alt105=2;
					int LA105_0 = input.LA(1);
					if ( ((LA105_0 >= 71 && LA105_0 <= 76)) ) {
						alt105=1;
					}
					else if ( (LA105_0==128) ) {
						alt105=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 105, 0, input);
						throw nvae;
					}

					switch (alt105) {
						case 1 :
							// JPA2.g:350:26: comparison_operator
							{
							pushFollow(FOLLOW_comparison_operator_in_comparison_expression3335);
							comparison_operator382=comparison_operator();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, comparison_operator382.getTree());

							}
							break;
						case 2 :
							// JPA2.g:350:48: 'REGEXP'
							{
							string_literal383=(Token)match(input,128,FOLLOW_128_in_comparison_expression3339); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							string_literal383_tree = (Object)adaptor.create(string_literal383);
							adaptor.addChild(root_0, string_literal383_tree);
							}

							}
							break;

					}

					// JPA2.g:350:58: ( string_expression | all_or_any_expression )
					int alt106=2;
					int LA106_0 = input.LA(1);
					if ( (LA106_0==AVG||LA106_0==CASE||LA106_0==COUNT||LA106_0==GROUP||(LA106_0 >= LOWER && LA106_0 <= NAMED_PARAMETER)||(LA106_0 >= STRING_LITERAL && LA106_0 <= SUM)||LA106_0==WORD||LA106_0==63||LA106_0==77||LA106_0==83||(LA106_0 >= 90 && LA106_0 <= 92)||LA106_0==103||LA106_0==105||LA106_0==121||LA106_0==134||LA106_0==137||LA106_0==140) ) {
						alt106=1;
					}
					else if ( ((LA106_0 >= 86 && LA106_0 <= 87)||LA106_0==132) ) {
						alt106=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 106, 0, input);
						throw nvae;
					}

					switch (alt106) {
						case 1 :
							// JPA2.g:350:59: string_expression
							{
							pushFollow(FOLLOW_string_expression_in_comparison_expression3343);
							string_expression384=string_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression384.getTree());

							}
							break;
						case 2 :
							// JPA2.g:350:79: all_or_any_expression
							{
							pushFollow(FOLLOW_all_or_any_expression_in_comparison_expression3347);
							all_or_any_expression385=all_or_any_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, all_or_any_expression385.getTree());

							}
							break;

					}

					}
					break;
				case 2 :
					// JPA2.g:351:7: boolean_expression ( '=' | '<>' ) ( boolean_expression | all_or_any_expression )
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_boolean_expression_in_comparison_expression3356);
					boolean_expression386=boolean_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, boolean_expression386.getTree());

					set387=input.LT(1);
					if ( (input.LA(1) >= 73 && input.LA(1) <= 74) ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set387));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					// JPA2.g:351:39: ( boolean_expression | all_or_any_expression )
					int alt107=2;
					int LA107_0 = input.LA(1);
					if ( (LA107_0==CASE||LA107_0==GROUP||LA107_0==LPAREN||LA107_0==NAMED_PARAMETER||LA107_0==WORD||LA107_0==63||LA107_0==77||LA107_0==83||(LA107_0 >= 90 && LA107_0 <= 91)||LA107_0==103||LA107_0==105||LA107_0==121||(LA107_0 >= 146 && LA107_0 <= 147)) ) {
						alt107=1;
					}
					else if ( ((LA107_0 >= 86 && LA107_0 <= 87)||LA107_0==132) ) {
						alt107=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 107, 0, input);
						throw nvae;
					}

					switch (alt107) {
						case 1 :
							// JPA2.g:351:40: boolean_expression
							{
							pushFollow(FOLLOW_boolean_expression_in_comparison_expression3367);
							boolean_expression388=boolean_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, boolean_expression388.getTree());

							}
							break;
						case 2 :
							// JPA2.g:351:61: all_or_any_expression
							{
							pushFollow(FOLLOW_all_or_any_expression_in_comparison_expression3371);
							all_or_any_expression389=all_or_any_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, all_or_any_expression389.getTree());

							}
							break;

					}

					}
					break;
				case 3 :
					// JPA2.g:352:7: enum_expression ( '=' | '<>' ) ( enum_expression | all_or_any_expression )
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_enum_expression_in_comparison_expression3380);
					enum_expression390=enum_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, enum_expression390.getTree());

					set391=input.LT(1);
					if ( (input.LA(1) >= 73 && input.LA(1) <= 74) ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set391));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					// JPA2.g:352:34: ( enum_expression | all_or_any_expression )
					int alt108=2;
					int LA108_0 = input.LA(1);
					if ( (LA108_0==CASE||LA108_0==GROUP||LA108_0==LPAREN||LA108_0==NAMED_PARAMETER||LA108_0==WORD||LA108_0==63||LA108_0==77||LA108_0==91||LA108_0==121) ) {
						alt108=1;
					}
					else if ( ((LA108_0 >= 86 && LA108_0 <= 87)||LA108_0==132) ) {
						alt108=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 108, 0, input);
						throw nvae;
					}

					switch (alt108) {
						case 1 :
							// JPA2.g:352:35: enum_expression
							{
							pushFollow(FOLLOW_enum_expression_in_comparison_expression3389);
							enum_expression392=enum_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, enum_expression392.getTree());

							}
							break;
						case 2 :
							// JPA2.g:352:53: all_or_any_expression
							{
							pushFollow(FOLLOW_all_or_any_expression_in_comparison_expression3393);
							all_or_any_expression393=all_or_any_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, all_or_any_expression393.getTree());

							}
							break;

					}

					}
					break;
				case 4 :
					// JPA2.g:353:7: datetime_expression comparison_operator ( datetime_expression | all_or_any_expression )
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_datetime_expression_in_comparison_expression3402);
					datetime_expression394=datetime_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, datetime_expression394.getTree());

					pushFollow(FOLLOW_comparison_operator_in_comparison_expression3404);
					comparison_operator395=comparison_operator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, comparison_operator395.getTree());

					// JPA2.g:353:47: ( datetime_expression | all_or_any_expression )
					int alt109=2;
					int LA109_0 = input.LA(1);
					if ( (LA109_0==AVG||LA109_0==CASE||LA109_0==COUNT||LA109_0==GROUP||(LA109_0 >= LPAREN && LA109_0 <= NAMED_PARAMETER)||LA109_0==SUM||LA109_0==WORD||LA109_0==63||LA109_0==77||LA109_0==83||(LA109_0 >= 90 && LA109_0 <= 91)||(LA109_0 >= 93 && LA109_0 <= 95)||LA109_0==103||LA109_0==105||LA109_0==121) ) {
						alt109=1;
					}
					else if ( ((LA109_0 >= 86 && LA109_0 <= 87)||LA109_0==132) ) {
						alt109=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 109, 0, input);
						throw nvae;
					}

					switch (alt109) {
						case 1 :
							// JPA2.g:353:48: datetime_expression
							{
							pushFollow(FOLLOW_datetime_expression_in_comparison_expression3407);
							datetime_expression396=datetime_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, datetime_expression396.getTree());

							}
							break;
						case 2 :
							// JPA2.g:353:70: all_or_any_expression
							{
							pushFollow(FOLLOW_all_or_any_expression_in_comparison_expression3411);
							all_or_any_expression397=all_or_any_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, all_or_any_expression397.getTree());

							}
							break;

					}

					}
					break;
				case 5 :
					// JPA2.g:354:7: entity_expression ( '=' | '<>' ) ( entity_expression | all_or_any_expression )
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_entity_expression_in_comparison_expression3420);
					entity_expression398=entity_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, entity_expression398.getTree());

					set399=input.LT(1);
					if ( (input.LA(1) >= 73 && input.LA(1) <= 74) ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set399));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					// JPA2.g:354:38: ( entity_expression | all_or_any_expression )
					int alt110=2;
					int LA110_0 = input.LA(1);
					if ( (LA110_0==GROUP||LA110_0==NAMED_PARAMETER||LA110_0==WORD||LA110_0==63||LA110_0==77) ) {
						alt110=1;
					}
					else if ( ((LA110_0 >= 86 && LA110_0 <= 87)||LA110_0==132) ) {
						alt110=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 110, 0, input);
						throw nvae;
					}

					switch (alt110) {
						case 1 :
							// JPA2.g:354:39: entity_expression
							{
							pushFollow(FOLLOW_entity_expression_in_comparison_expression3431);
							entity_expression400=entity_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, entity_expression400.getTree());

							}
							break;
						case 2 :
							// JPA2.g:354:59: all_or_any_expression
							{
							pushFollow(FOLLOW_all_or_any_expression_in_comparison_expression3435);
							all_or_any_expression401=all_or_any_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, all_or_any_expression401.getTree());

							}
							break;

					}

					}
					break;
				case 6 :
					// JPA2.g:355:7: entity_type_expression ( '=' | '<>' ) entity_type_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_entity_type_expression_in_comparison_expression3444);
					entity_type_expression402=entity_type_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, entity_type_expression402.getTree());

					set403=input.LT(1);
					if ( (input.LA(1) >= 73 && input.LA(1) <= 74) ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set403));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_entity_type_expression_in_comparison_expression3454);
					entity_type_expression404=entity_type_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, entity_type_expression404.getTree());

					}
					break;
				case 7 :
					// JPA2.g:356:7: arithmetic_expression comparison_operator ( arithmetic_expression | all_or_any_expression )
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_arithmetic_expression_in_comparison_expression3462);
					arithmetic_expression405=arithmetic_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression405.getTree());

					pushFollow(FOLLOW_comparison_operator_in_comparison_expression3464);
					comparison_operator406=comparison_operator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, comparison_operator406.getTree());

					// JPA2.g:356:49: ( arithmetic_expression | all_or_any_expression )
					int alt111=2;
					int LA111_0 = input.LA(1);
					if ( (LA111_0==AVG||LA111_0==CASE||LA111_0==COUNT||LA111_0==GROUP||LA111_0==INT_NUMERAL||(LA111_0 >= LPAREN && LA111_0 <= NAMED_PARAMETER)||LA111_0==SUM||LA111_0==WORD||LA111_0==63||LA111_0==65||LA111_0==67||LA111_0==70||LA111_0==77||LA111_0==83||LA111_0==85||(LA111_0 >= 90 && LA111_0 <= 91)||LA111_0==103||LA111_0==105||LA111_0==107||LA111_0==111||LA111_0==113||LA111_0==116||LA111_0==121||LA111_0==131||LA111_0==133) ) {
						alt111=1;
					}
					else if ( ((LA111_0 >= 86 && LA111_0 <= 87)||LA111_0==132) ) {
						alt111=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 111, 0, input);
						throw nvae;
					}

					switch (alt111) {
						case 1 :
							// JPA2.g:356:50: arithmetic_expression
							{
							pushFollow(FOLLOW_arithmetic_expression_in_comparison_expression3467);
							arithmetic_expression407=arithmetic_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression407.getTree());

							}
							break;
						case 2 :
							// JPA2.g:356:74: all_or_any_expression
							{
							pushFollow(FOLLOW_all_or_any_expression_in_comparison_expression3471);
							all_or_any_expression408=all_or_any_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, all_or_any_expression408.getTree());

							}
							break;

					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "comparison_expression"


	public static class comparison_operator_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "comparison_operator"
	// JPA2.g:358:1: comparison_operator : ( '=' | '>' | '>=' | '<' | '<=' | '<>' );
	public final JPA2Parser.comparison_operator_return comparison_operator() throws RecognitionException {
		JPA2Parser.comparison_operator_return retval = new JPA2Parser.comparison_operator_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set409=null;

		Object set409_tree=null;

		try {
			// JPA2.g:359:5: ( '=' | '>' | '>=' | '<' | '<=' | '<>' )
			// JPA2.g:
			{
			root_0 = (Object)adaptor.nil();


			set409=input.LT(1);
			if ( (input.LA(1) >= 71 && input.LA(1) <= 76) ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set409));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "comparison_operator"


	public static class arithmetic_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "arithmetic_expression"
	// JPA2.g:365:1: arithmetic_expression : ( arithmetic_term ( ( '+' | '-' ) arithmetic_term )+ | arithmetic_term );
	public final JPA2Parser.arithmetic_expression_return arithmetic_expression() throws RecognitionException {
		JPA2Parser.arithmetic_expression_return retval = new JPA2Parser.arithmetic_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set411=null;
		ParserRuleReturnScope arithmetic_term410 =null;
		ParserRuleReturnScope arithmetic_term412 =null;
		ParserRuleReturnScope arithmetic_term413 =null;

		Object set411_tree=null;

		try {
			// JPA2.g:366:5: ( arithmetic_term ( ( '+' | '-' ) arithmetic_term )+ | arithmetic_term )
			int alt114=2;
			switch ( input.LA(1) ) {
			case 65:
			case 67:
				{
				int LA114_1 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case GROUP:
			case WORD:
				{
				int LA114_2 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case INT_NUMERAL:
				{
				int LA114_3 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 70:
				{
				int LA114_4 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case LPAREN:
				{
				int LA114_5 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 77:
				{
				int LA114_6 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case NAMED_PARAMETER:
				{
				int LA114_7 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 63:
				{
				int LA114_8 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 111:
				{
				int LA114_9 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 113:
				{
				int LA114_10 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 85:
				{
				int LA114_11 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 133:
				{
				int LA114_12 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 116:
				{
				int LA114_13 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 131:
				{
				int LA114_14 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 107:
				{
				int LA114_15 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case COUNT:
				{
				int LA114_16 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
				{
				int LA114_17 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 105:
				{
				int LA114_18 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case CASE:
				{
				int LA114_19 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 91:
				{
				int LA114_20 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 121:
				{
				int LA114_21 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 90:
				{
				int LA114_22 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 103:
				{
				int LA114_23 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			case 83:
				{
				int LA114_24 = input.LA(2);
				if ( (synpred200_JPA2()) ) {
					alt114=1;
				}
				else if ( (true) ) {
					alt114=2;
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 114, 0, input);
				throw nvae;
			}
			switch (alt114) {
				case 1 :
					// JPA2.g:366:7: arithmetic_term ( ( '+' | '-' ) arithmetic_term )+
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_arithmetic_term_in_arithmetic_expression3535);
					arithmetic_term410=arithmetic_term();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_term410.getTree());

					// JPA2.g:366:23: ( ( '+' | '-' ) arithmetic_term )+
					int cnt113=0;
					loop113:
					while (true) {
						int alt113=2;
						int LA113_0 = input.LA(1);
						if ( (LA113_0==65||LA113_0==67) ) {
							alt113=1;
						}

						switch (alt113) {
						case 1 :
							// JPA2.g:366:24: ( '+' | '-' ) arithmetic_term
							{
							set411=input.LT(1);
							if ( input.LA(1)==65||input.LA(1)==67 ) {
								input.consume();
								if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set411));
								state.errorRecovery=false;
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								throw mse;
							}
							pushFollow(FOLLOW_arithmetic_term_in_arithmetic_expression3546);
							arithmetic_term412=arithmetic_term();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_term412.getTree());

							}
							break;

						default :
							if ( cnt113 >= 1 ) break loop113;
							if (state.backtracking>0) {state.failed=true; return retval;}
							EarlyExitException eee = new EarlyExitException(113, input);
							throw eee;
						}
						cnt113++;
					}

					}
					break;
				case 2 :
					// JPA2.g:367:7: arithmetic_term
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_arithmetic_term_in_arithmetic_expression3556);
					arithmetic_term413=arithmetic_term();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_term413.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "arithmetic_expression"


	public static class arithmetic_term_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "arithmetic_term"
	// JPA2.g:368:1: arithmetic_term : ( arithmetic_factor ( ( '*' | '/' ) arithmetic_factor )+ | arithmetic_factor );
	public final JPA2Parser.arithmetic_term_return arithmetic_term() throws RecognitionException {
		JPA2Parser.arithmetic_term_return retval = new JPA2Parser.arithmetic_term_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set415=null;
		ParserRuleReturnScope arithmetic_factor414 =null;
		ParserRuleReturnScope arithmetic_factor416 =null;
		ParserRuleReturnScope arithmetic_factor417 =null;

		Object set415_tree=null;

		try {
			// JPA2.g:369:5: ( arithmetic_factor ( ( '*' | '/' ) arithmetic_factor )+ | arithmetic_factor )
			int alt116=2;
			switch ( input.LA(1) ) {
			case 65:
			case 67:
				{
				int LA116_1 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case GROUP:
			case WORD:
				{
				int LA116_2 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case INT_NUMERAL:
				{
				int LA116_3 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 70:
				{
				int LA116_4 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case LPAREN:
				{
				int LA116_5 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 77:
				{
				int LA116_6 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case NAMED_PARAMETER:
				{
				int LA116_7 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 63:
				{
				int LA116_8 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 111:
				{
				int LA116_9 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 113:
				{
				int LA116_10 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 85:
				{
				int LA116_11 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 133:
				{
				int LA116_12 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 116:
				{
				int LA116_13 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 131:
				{
				int LA116_14 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 107:
				{
				int LA116_15 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case COUNT:
				{
				int LA116_16 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
				{
				int LA116_17 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 105:
				{
				int LA116_18 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case CASE:
				{
				int LA116_19 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 91:
				{
				int LA116_20 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 121:
				{
				int LA116_21 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 90:
				{
				int LA116_22 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 103:
				{
				int LA116_23 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			case 83:
				{
				int LA116_24 = input.LA(2);
				if ( (synpred203_JPA2()) ) {
					alt116=1;
				}
				else if ( (true) ) {
					alt116=2;
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 116, 0, input);
				throw nvae;
			}
			switch (alt116) {
				case 1 :
					// JPA2.g:369:7: arithmetic_factor ( ( '*' | '/' ) arithmetic_factor )+
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_arithmetic_factor_in_arithmetic_term3567);
					arithmetic_factor414=arithmetic_factor();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_factor414.getTree());

					// JPA2.g:369:25: ( ( '*' | '/' ) arithmetic_factor )+
					int cnt115=0;
					loop115:
					while (true) {
						int alt115=2;
						int LA115_0 = input.LA(1);
						if ( (LA115_0==64||LA115_0==69) ) {
							alt115=1;
						}

						switch (alt115) {
						case 1 :
							// JPA2.g:369:26: ( '*' | '/' ) arithmetic_factor
							{
							set415=input.LT(1);
							if ( input.LA(1)==64||input.LA(1)==69 ) {
								input.consume();
								if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set415));
								state.errorRecovery=false;
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								throw mse;
							}
							pushFollow(FOLLOW_arithmetic_factor_in_arithmetic_term3579);
							arithmetic_factor416=arithmetic_factor();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_factor416.getTree());

							}
							break;

						default :
							if ( cnt115 >= 1 ) break loop115;
							if (state.backtracking>0) {state.failed=true; return retval;}
							EarlyExitException eee = new EarlyExitException(115, input);
							throw eee;
						}
						cnt115++;
					}

					}
					break;
				case 2 :
					// JPA2.g:370:7: arithmetic_factor
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_arithmetic_factor_in_arithmetic_term3589);
					arithmetic_factor417=arithmetic_factor();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_factor417.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "arithmetic_term"


	public static class arithmetic_factor_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "arithmetic_factor"
	// JPA2.g:371:1: arithmetic_factor : ( ( '+' | '-' ) )? arithmetic_primary ;
	public final JPA2Parser.arithmetic_factor_return arithmetic_factor() throws RecognitionException {
		JPA2Parser.arithmetic_factor_return retval = new JPA2Parser.arithmetic_factor_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set418=null;
		ParserRuleReturnScope arithmetic_primary419 =null;

		Object set418_tree=null;

		try {
			// JPA2.g:372:5: ( ( ( '+' | '-' ) )? arithmetic_primary )
			// JPA2.g:372:7: ( ( '+' | '-' ) )? arithmetic_primary
			{
			root_0 = (Object)adaptor.nil();


			// JPA2.g:372:7: ( ( '+' | '-' ) )?
			int alt117=2;
			int LA117_0 = input.LA(1);
			if ( (LA117_0==65||LA117_0==67) ) {
				alt117=1;
			}
			switch (alt117) {
				case 1 :
					// JPA2.g:
					{
					set418=input.LT(1);
					if ( input.LA(1)==65||input.LA(1)==67 ) {
						input.consume();
						if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set418));
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					}
					break;

			}

			pushFollow(FOLLOW_arithmetic_primary_in_arithmetic_factor3612);
			arithmetic_primary419=arithmetic_primary();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_primary419.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "arithmetic_factor"


	public static class arithmetic_primary_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "arithmetic_primary"
	// JPA2.g:373:1: arithmetic_primary : ( path_expression | decimal_literal | numeric_literal | '(' arithmetic_expression ')' | input_parameter | functions_returning_numerics | aggregate_expression | case_expression | function_invocation | extension_functions | subquery );
	public final JPA2Parser.arithmetic_primary_return arithmetic_primary() throws RecognitionException {
		JPA2Parser.arithmetic_primary_return retval = new JPA2Parser.arithmetic_primary_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal423=null;
		Token char_literal425=null;
		ParserRuleReturnScope path_expression420 =null;
		ParserRuleReturnScope decimal_literal421 =null;
		ParserRuleReturnScope numeric_literal422 =null;
		ParserRuleReturnScope arithmetic_expression424 =null;
		ParserRuleReturnScope input_parameter426 =null;
		ParserRuleReturnScope functions_returning_numerics427 =null;
		ParserRuleReturnScope aggregate_expression428 =null;
		ParserRuleReturnScope case_expression429 =null;
		ParserRuleReturnScope function_invocation430 =null;
		ParserRuleReturnScope extension_functions431 =null;
		ParserRuleReturnScope subquery432 =null;

		Object char_literal423_tree=null;
		Object char_literal425_tree=null;

		try {
			// JPA2.g:374:5: ( path_expression | decimal_literal | numeric_literal | '(' arithmetic_expression ')' | input_parameter | functions_returning_numerics | aggregate_expression | case_expression | function_invocation | extension_functions | subquery )
			int alt118=11;
			switch ( input.LA(1) ) {
			case GROUP:
			case WORD:
				{
				alt118=1;
				}
				break;
			case INT_NUMERAL:
				{
				int LA118_2 = input.LA(2);
				if ( (synpred207_JPA2()) ) {
					alt118=2;
				}
				else if ( (synpred208_JPA2()) ) {
					alt118=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 118, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 70:
				{
				alt118=3;
				}
				break;
			case LPAREN:
				{
				int LA118_4 = input.LA(2);
				if ( (synpred209_JPA2()) ) {
					alt118=4;
				}
				else if ( (true) ) {
					alt118=11;
				}

				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt118=5;
				}
				break;
			case 85:
			case 107:
			case 111:
			case 113:
			case 116:
			case 131:
			case 133:
				{
				alt118=6;
				}
				break;
			case AVG:
			case COUNT:
			case MAX:
			case MIN:
			case SUM:
				{
				alt118=7;
				}
				break;
			case 105:
				{
				int LA118_17 = input.LA(2);
				if ( (synpred212_JPA2()) ) {
					alt118=7;
				}
				else if ( (synpred214_JPA2()) ) {
					alt118=9;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 118, 17, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case CASE:
			case 91:
			case 121:
				{
				alt118=8;
				}
				break;
			case 83:
			case 90:
			case 103:
				{
				alt118=10;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 118, 0, input);
				throw nvae;
			}
			switch (alt118) {
				case 1 :
					// JPA2.g:374:7: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_arithmetic_primary3623);
					path_expression420=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression420.getTree());

					}
					break;
				case 2 :
					// JPA2.g:375:7: decimal_literal
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_decimal_literal_in_arithmetic_primary3631);
					decimal_literal421=decimal_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, decimal_literal421.getTree());

					}
					break;
				case 3 :
					// JPA2.g:376:7: numeric_literal
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_numeric_literal_in_arithmetic_primary3639);
					numeric_literal422=numeric_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, numeric_literal422.getTree());

					}
					break;
				case 4 :
					// JPA2.g:377:7: '(' arithmetic_expression ')'
					{
					root_0 = (Object)adaptor.nil();


					char_literal423=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_arithmetic_primary3647); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal423_tree = (Object)adaptor.create(char_literal423);
					adaptor.addChild(root_0, char_literal423_tree);
					}

					pushFollow(FOLLOW_arithmetic_expression_in_arithmetic_primary3648);
					arithmetic_expression424=arithmetic_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression424.getTree());

					char_literal425=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_arithmetic_primary3649); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal425_tree = (Object)adaptor.create(char_literal425);
					adaptor.addChild(root_0, char_literal425_tree);
					}

					}
					break;
				case 5 :
					// JPA2.g:378:7: input_parameter
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_input_parameter_in_arithmetic_primary3657);
					input_parameter426=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter426.getTree());

					}
					break;
				case 6 :
					// JPA2.g:379:7: functions_returning_numerics
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_functions_returning_numerics_in_arithmetic_primary3665);
					functions_returning_numerics427=functions_returning_numerics();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, functions_returning_numerics427.getTree());

					}
					break;
				case 7 :
					// JPA2.g:380:7: aggregate_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_aggregate_expression_in_arithmetic_primary3673);
					aggregate_expression428=aggregate_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, aggregate_expression428.getTree());

					}
					break;
				case 8 :
					// JPA2.g:381:7: case_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_case_expression_in_arithmetic_primary3681);
					case_expression429=case_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, case_expression429.getTree());

					}
					break;
				case 9 :
					// JPA2.g:382:7: function_invocation
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_function_invocation_in_arithmetic_primary3689);
					function_invocation430=function_invocation();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, function_invocation430.getTree());

					}
					break;
				case 10 :
					// JPA2.g:383:7: extension_functions
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_extension_functions_in_arithmetic_primary3697);
					extension_functions431=extension_functions();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, extension_functions431.getTree());

					}
					break;
				case 11 :
					// JPA2.g:384:7: subquery
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_subquery_in_arithmetic_primary3705);
					subquery432=subquery();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, subquery432.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "arithmetic_primary"


	public static class string_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "string_expression"
	// JPA2.g:385:1: string_expression : ( path_expression | string_literal | input_parameter | functions_returning_strings | aggregate_expression | case_expression | function_invocation | extension_functions | subquery );
	public final JPA2Parser.string_expression_return string_expression() throws RecognitionException {
		JPA2Parser.string_expression_return retval = new JPA2Parser.string_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope path_expression433 =null;
		ParserRuleReturnScope string_literal434 =null;
		ParserRuleReturnScope input_parameter435 =null;
		ParserRuleReturnScope functions_returning_strings436 =null;
		ParserRuleReturnScope aggregate_expression437 =null;
		ParserRuleReturnScope case_expression438 =null;
		ParserRuleReturnScope function_invocation439 =null;
		ParserRuleReturnScope extension_functions440 =null;
		ParserRuleReturnScope subquery441 =null;


		try {
			// JPA2.g:386:5: ( path_expression | string_literal | input_parameter | functions_returning_strings | aggregate_expression | case_expression | function_invocation | extension_functions | subquery )
			int alt119=9;
			switch ( input.LA(1) ) {
			case GROUP:
			case WORD:
				{
				alt119=1;
				}
				break;
			case STRING_LITERAL:
				{
				alt119=2;
				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt119=3;
				}
				break;
			case LOWER:
			case 92:
			case 134:
			case 137:
			case 140:
				{
				alt119=4;
				}
				break;
			case AVG:
			case COUNT:
			case MAX:
			case MIN:
			case SUM:
				{
				alt119=5;
				}
				break;
			case 105:
				{
				int LA119_13 = input.LA(2);
				if ( (synpred220_JPA2()) ) {
					alt119=5;
				}
				else if ( (synpred222_JPA2()) ) {
					alt119=7;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 119, 13, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case CASE:
			case 91:
			case 121:
				{
				alt119=6;
				}
				break;
			case 83:
			case 90:
			case 103:
				{
				alt119=8;
				}
				break;
			case LPAREN:
				{
				alt119=9;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 119, 0, input);
				throw nvae;
			}
			switch (alt119) {
				case 1 :
					// JPA2.g:386:7: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_string_expression3716);
					path_expression433=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression433.getTree());

					}
					break;
				case 2 :
					// JPA2.g:387:7: string_literal
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_string_literal_in_string_expression3724);
					string_literal434=string_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_literal434.getTree());

					}
					break;
				case 3 :
					// JPA2.g:388:7: input_parameter
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_input_parameter_in_string_expression3732);
					input_parameter435=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter435.getTree());

					}
					break;
				case 4 :
					// JPA2.g:389:7: functions_returning_strings
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_functions_returning_strings_in_string_expression3740);
					functions_returning_strings436=functions_returning_strings();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, functions_returning_strings436.getTree());

					}
					break;
				case 5 :
					// JPA2.g:390:7: aggregate_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_aggregate_expression_in_string_expression3748);
					aggregate_expression437=aggregate_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, aggregate_expression437.getTree());

					}
					break;
				case 6 :
					// JPA2.g:391:7: case_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_case_expression_in_string_expression3756);
					case_expression438=case_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, case_expression438.getTree());

					}
					break;
				case 7 :
					// JPA2.g:392:7: function_invocation
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_function_invocation_in_string_expression3764);
					function_invocation439=function_invocation();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, function_invocation439.getTree());

					}
					break;
				case 8 :
					// JPA2.g:393:7: extension_functions
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_extension_functions_in_string_expression3772);
					extension_functions440=extension_functions();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, extension_functions440.getTree());

					}
					break;
				case 9 :
					// JPA2.g:394:7: subquery
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_subquery_in_string_expression3780);
					subquery441=subquery();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, subquery441.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "string_expression"


	public static class datetime_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "datetime_expression"
	// JPA2.g:395:1: datetime_expression : ( path_expression | input_parameter | functions_returning_datetime | aggregate_expression | case_expression | function_invocation | extension_functions | date_time_timestamp_literal | subquery );
	public final JPA2Parser.datetime_expression_return datetime_expression() throws RecognitionException {
		JPA2Parser.datetime_expression_return retval = new JPA2Parser.datetime_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope path_expression442 =null;
		ParserRuleReturnScope input_parameter443 =null;
		ParserRuleReturnScope functions_returning_datetime444 =null;
		ParserRuleReturnScope aggregate_expression445 =null;
		ParserRuleReturnScope case_expression446 =null;
		ParserRuleReturnScope function_invocation447 =null;
		ParserRuleReturnScope extension_functions448 =null;
		ParserRuleReturnScope date_time_timestamp_literal449 =null;
		ParserRuleReturnScope subquery450 =null;


		try {
			// JPA2.g:396:5: ( path_expression | input_parameter | functions_returning_datetime | aggregate_expression | case_expression | function_invocation | extension_functions | date_time_timestamp_literal | subquery )
			int alt120=9;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA120_1 = input.LA(2);
				if ( (synpred224_JPA2()) ) {
					alt120=1;
				}
				else if ( (synpred231_JPA2()) ) {
					alt120=8;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 120, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt120=2;
				}
				break;
			case 93:
			case 94:
			case 95:
				{
				alt120=3;
				}
				break;
			case AVG:
			case COUNT:
			case MAX:
			case MIN:
			case SUM:
				{
				alt120=4;
				}
				break;
			case 105:
				{
				int LA120_8 = input.LA(2);
				if ( (synpred227_JPA2()) ) {
					alt120=4;
				}
				else if ( (synpred229_JPA2()) ) {
					alt120=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 120, 8, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case CASE:
			case 91:
			case 121:
				{
				alt120=5;
				}
				break;
			case 83:
			case 90:
			case 103:
				{
				alt120=7;
				}
				break;
			case GROUP:
				{
				alt120=1;
				}
				break;
			case LPAREN:
				{
				alt120=9;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 120, 0, input);
				throw nvae;
			}
			switch (alt120) {
				case 1 :
					// JPA2.g:396:7: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_datetime_expression3791);
					path_expression442=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression442.getTree());

					}
					break;
				case 2 :
					// JPA2.g:397:7: input_parameter
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_input_parameter_in_datetime_expression3799);
					input_parameter443=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter443.getTree());

					}
					break;
				case 3 :
					// JPA2.g:398:7: functions_returning_datetime
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_functions_returning_datetime_in_datetime_expression3807);
					functions_returning_datetime444=functions_returning_datetime();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, functions_returning_datetime444.getTree());

					}
					break;
				case 4 :
					// JPA2.g:399:7: aggregate_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_aggregate_expression_in_datetime_expression3815);
					aggregate_expression445=aggregate_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, aggregate_expression445.getTree());

					}
					break;
				case 5 :
					// JPA2.g:400:7: case_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_case_expression_in_datetime_expression3823);
					case_expression446=case_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, case_expression446.getTree());

					}
					break;
				case 6 :
					// JPA2.g:401:7: function_invocation
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_function_invocation_in_datetime_expression3831);
					function_invocation447=function_invocation();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, function_invocation447.getTree());

					}
					break;
				case 7 :
					// JPA2.g:402:7: extension_functions
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_extension_functions_in_datetime_expression3839);
					extension_functions448=extension_functions();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, extension_functions448.getTree());

					}
					break;
				case 8 :
					// JPA2.g:403:7: date_time_timestamp_literal
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_date_time_timestamp_literal_in_datetime_expression3847);
					date_time_timestamp_literal449=date_time_timestamp_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, date_time_timestamp_literal449.getTree());

					}
					break;
				case 9 :
					// JPA2.g:404:7: subquery
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_subquery_in_datetime_expression3855);
					subquery450=subquery();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, subquery450.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "datetime_expression"


	public static class boolean_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "boolean_expression"
	// JPA2.g:405:1: boolean_expression : ( path_expression | boolean_literal | input_parameter | case_expression | function_invocation | extension_functions | subquery );
	public final JPA2Parser.boolean_expression_return boolean_expression() throws RecognitionException {
		JPA2Parser.boolean_expression_return retval = new JPA2Parser.boolean_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope path_expression451 =null;
		ParserRuleReturnScope boolean_literal452 =null;
		ParserRuleReturnScope input_parameter453 =null;
		ParserRuleReturnScope case_expression454 =null;
		ParserRuleReturnScope function_invocation455 =null;
		ParserRuleReturnScope extension_functions456 =null;
		ParserRuleReturnScope subquery457 =null;


		try {
			// JPA2.g:406:5: ( path_expression | boolean_literal | input_parameter | case_expression | function_invocation | extension_functions | subquery )
			int alt121=7;
			switch ( input.LA(1) ) {
			case GROUP:
			case WORD:
				{
				alt121=1;
				}
				break;
			case 146:
			case 147:
				{
				alt121=2;
				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt121=3;
				}
				break;
			case CASE:
			case 91:
			case 121:
				{
				alt121=4;
				}
				break;
			case 105:
				{
				alt121=5;
				}
				break;
			case 83:
			case 90:
			case 103:
				{
				alt121=6;
				}
				break;
			case LPAREN:
				{
				alt121=7;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 121, 0, input);
				throw nvae;
			}
			switch (alt121) {
				case 1 :
					// JPA2.g:406:7: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_boolean_expression3866);
					path_expression451=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression451.getTree());

					}
					break;
				case 2 :
					// JPA2.g:407:7: boolean_literal
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_boolean_literal_in_boolean_expression3874);
					boolean_literal452=boolean_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, boolean_literal452.getTree());

					}
					break;
				case 3 :
					// JPA2.g:408:7: input_parameter
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_input_parameter_in_boolean_expression3882);
					input_parameter453=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter453.getTree());

					}
					break;
				case 4 :
					// JPA2.g:409:7: case_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_case_expression_in_boolean_expression3890);
					case_expression454=case_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, case_expression454.getTree());

					}
					break;
				case 5 :
					// JPA2.g:410:7: function_invocation
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_function_invocation_in_boolean_expression3898);
					function_invocation455=function_invocation();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, function_invocation455.getTree());

					}
					break;
				case 6 :
					// JPA2.g:411:7: extension_functions
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_extension_functions_in_boolean_expression3906);
					extension_functions456=extension_functions();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, extension_functions456.getTree());

					}
					break;
				case 7 :
					// JPA2.g:412:7: subquery
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_subquery_in_boolean_expression3914);
					subquery457=subquery();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, subquery457.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "boolean_expression"


	public static class enum_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "enum_expression"
	// JPA2.g:413:1: enum_expression : ( path_expression | enum_literal | input_parameter | case_expression | subquery );
	public final JPA2Parser.enum_expression_return enum_expression() throws RecognitionException {
		JPA2Parser.enum_expression_return retval = new JPA2Parser.enum_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope path_expression458 =null;
		ParserRuleReturnScope enum_literal459 =null;
		ParserRuleReturnScope input_parameter460 =null;
		ParserRuleReturnScope case_expression461 =null;
		ParserRuleReturnScope subquery462 =null;


		try {
			// JPA2.g:414:5: ( path_expression | enum_literal | input_parameter | case_expression | subquery )
			int alt122=5;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA122_1 = input.LA(2);
				if ( (LA122_1==68) ) {
					alt122=1;
				}
				else if ( (LA122_1==EOF||(LA122_1 >= AND && LA122_1 <= ASC)||LA122_1==DESC||(LA122_1 >= ELSE && LA122_1 <= END)||(LA122_1 >= GROUP && LA122_1 <= HAVING)||LA122_1==INNER||(LA122_1 >= JOIN && LA122_1 <= LEFT)||(LA122_1 >= OR && LA122_1 <= ORDER)||LA122_1==RPAREN||LA122_1==SET||LA122_1==THEN||(LA122_1 >= WHEN && LA122_1 <= WORD)||LA122_1==66||(LA122_1 >= 73 && LA122_1 <= 74)||LA122_1==104||(LA122_1 >= 122 && LA122_1 <= 123)||LA122_1==144) ) {
					alt122=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 122, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case GROUP:
				{
				alt122=1;
				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt122=3;
				}
				break;
			case CASE:
			case 91:
			case 121:
				{
				alt122=4;
				}
				break;
			case LPAREN:
				{
				alt122=5;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 122, 0, input);
				throw nvae;
			}
			switch (alt122) {
				case 1 :
					// JPA2.g:414:7: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_enum_expression3925);
					path_expression458=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression458.getTree());

					}
					break;
				case 2 :
					// JPA2.g:415:7: enum_literal
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_enum_literal_in_enum_expression3933);
					enum_literal459=enum_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, enum_literal459.getTree());

					}
					break;
				case 3 :
					// JPA2.g:416:7: input_parameter
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_input_parameter_in_enum_expression3941);
					input_parameter460=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter460.getTree());

					}
					break;
				case 4 :
					// JPA2.g:417:7: case_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_case_expression_in_enum_expression3949);
					case_expression461=case_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, case_expression461.getTree());

					}
					break;
				case 5 :
					// JPA2.g:418:7: subquery
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_subquery_in_enum_expression3957);
					subquery462=subquery();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, subquery462.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enum_expression"


	public static class entity_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "entity_expression"
	// JPA2.g:419:1: entity_expression : ( path_expression | simple_entity_expression );
	public final JPA2Parser.entity_expression_return entity_expression() throws RecognitionException {
		JPA2Parser.entity_expression_return retval = new JPA2Parser.entity_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope path_expression463 =null;
		ParserRuleReturnScope simple_entity_expression464 =null;


		try {
			// JPA2.g:420:5: ( path_expression | simple_entity_expression )
			int alt123=2;
			int LA123_0 = input.LA(1);
			if ( (LA123_0==GROUP||LA123_0==WORD) ) {
				int LA123_1 = input.LA(2);
				if ( (LA123_1==68) ) {
					alt123=1;
				}
				else if ( (LA123_1==EOF||LA123_1==AND||(LA123_1 >= GROUP && LA123_1 <= HAVING)||LA123_1==INNER||(LA123_1 >= JOIN && LA123_1 <= LEFT)||(LA123_1 >= OR && LA123_1 <= ORDER)||LA123_1==RPAREN||LA123_1==SET||LA123_1==THEN||LA123_1==66||(LA123_1 >= 73 && LA123_1 <= 74)||LA123_1==144) ) {
					alt123=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 123, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA123_0==NAMED_PARAMETER||LA123_0==63||LA123_0==77) ) {
				alt123=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 123, 0, input);
				throw nvae;
			}

			switch (alt123) {
				case 1 :
					// JPA2.g:420:7: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_entity_expression3968);
					path_expression463=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression463.getTree());

					}
					break;
				case 2 :
					// JPA2.g:421:7: simple_entity_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_simple_entity_expression_in_entity_expression3976);
					simple_entity_expression464=simple_entity_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, simple_entity_expression464.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "entity_expression"


	public static class simple_entity_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "simple_entity_expression"
	// JPA2.g:422:1: simple_entity_expression : ( identification_variable | input_parameter );
	public final JPA2Parser.simple_entity_expression_return simple_entity_expression() throws RecognitionException {
		JPA2Parser.simple_entity_expression_return retval = new JPA2Parser.simple_entity_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope identification_variable465 =null;
		ParserRuleReturnScope input_parameter466 =null;


		try {
			// JPA2.g:423:5: ( identification_variable | input_parameter )
			int alt124=2;
			int LA124_0 = input.LA(1);
			if ( (LA124_0==GROUP||LA124_0==WORD) ) {
				alt124=1;
			}
			else if ( (LA124_0==NAMED_PARAMETER||LA124_0==63||LA124_0==77) ) {
				alt124=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 124, 0, input);
				throw nvae;
			}

			switch (alt124) {
				case 1 :
					// JPA2.g:423:7: identification_variable
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_identification_variable_in_simple_entity_expression3987);
					identification_variable465=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable465.getTree());

					}
					break;
				case 2 :
					// JPA2.g:424:7: input_parameter
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_input_parameter_in_simple_entity_expression3995);
					input_parameter466=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter466.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "simple_entity_expression"


	public static class entity_type_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "entity_type_expression"
	// JPA2.g:425:1: entity_type_expression : ( type_discriminator | entity_type_literal | input_parameter );
	public final JPA2Parser.entity_type_expression_return entity_type_expression() throws RecognitionException {
		JPA2Parser.entity_type_expression_return retval = new JPA2Parser.entity_type_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope type_discriminator467 =null;
		ParserRuleReturnScope entity_type_literal468 =null;
		ParserRuleReturnScope input_parameter469 =null;


		try {
			// JPA2.g:426:5: ( type_discriminator | entity_type_literal | input_parameter )
			int alt125=3;
			switch ( input.LA(1) ) {
			case 138:
				{
				alt125=1;
				}
				break;
			case WORD:
				{
				alt125=2;
				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt125=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 125, 0, input);
				throw nvae;
			}
			switch (alt125) {
				case 1 :
					// JPA2.g:426:7: type_discriminator
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_type_discriminator_in_entity_type_expression4006);
					type_discriminator467=type_discriminator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, type_discriminator467.getTree());

					}
					break;
				case 2 :
					// JPA2.g:427:7: entity_type_literal
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_entity_type_literal_in_entity_type_expression4014);
					entity_type_literal468=entity_type_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, entity_type_literal468.getTree());

					}
					break;
				case 3 :
					// JPA2.g:428:7: input_parameter
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_input_parameter_in_entity_type_expression4022);
					input_parameter469=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter469.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "entity_type_expression"


	public static class type_discriminator_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "type_discriminator"
	// JPA2.g:429:1: type_discriminator : 'TYPE(' ( general_identification_variable | path_expression | input_parameter ) ')' ;
	public final JPA2Parser.type_discriminator_return type_discriminator() throws RecognitionException {
		JPA2Parser.type_discriminator_return retval = new JPA2Parser.type_discriminator_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal470=null;
		Token char_literal474=null;
		ParserRuleReturnScope general_identification_variable471 =null;
		ParserRuleReturnScope path_expression472 =null;
		ParserRuleReturnScope input_parameter473 =null;

		Object string_literal470_tree=null;
		Object char_literal474_tree=null;

		try {
			// JPA2.g:430:5: ( 'TYPE(' ( general_identification_variable | path_expression | input_parameter ) ')' )
			// JPA2.g:430:7: 'TYPE(' ( general_identification_variable | path_expression | input_parameter ) ')'
			{
			root_0 = (Object)adaptor.nil();


			string_literal470=(Token)match(input,138,FOLLOW_138_in_type_discriminator4033); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal470_tree = (Object)adaptor.create(string_literal470);
			adaptor.addChild(root_0, string_literal470_tree);
			}

			// JPA2.g:430:15: ( general_identification_variable | path_expression | input_parameter )
			int alt126=3;
			switch ( input.LA(1) ) {
			case GROUP:
			case WORD:
				{
				int LA126_1 = input.LA(2);
				if ( (LA126_1==RPAREN) ) {
					alt126=1;
				}
				else if ( (LA126_1==68) ) {
					alt126=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 126, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 109:
			case 142:
				{
				alt126=1;
				}
				break;
			case NAMED_PARAMETER:
			case 63:
			case 77:
				{
				alt126=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 126, 0, input);
				throw nvae;
			}
			switch (alt126) {
				case 1 :
					// JPA2.g:430:16: general_identification_variable
					{
					pushFollow(FOLLOW_general_identification_variable_in_type_discriminator4036);
					general_identification_variable471=general_identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, general_identification_variable471.getTree());

					}
					break;
				case 2 :
					// JPA2.g:430:50: path_expression
					{
					pushFollow(FOLLOW_path_expression_in_type_discriminator4040);
					path_expression472=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression472.getTree());

					}
					break;
				case 3 :
					// JPA2.g:430:68: input_parameter
					{
					pushFollow(FOLLOW_input_parameter_in_type_discriminator4044);
					input_parameter473=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter473.getTree());

					}
					break;

			}

			char_literal474=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_type_discriminator4047); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal474_tree = (Object)adaptor.create(char_literal474);
			adaptor.addChild(root_0, char_literal474_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "type_discriminator"


	public static class functions_returning_numerics_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "functions_returning_numerics"
	// JPA2.g:431:1: functions_returning_numerics : ( 'LENGTH(' string_expression ')' | 'LOCATE(' string_expression ',' string_expression ( ',' arithmetic_expression )? ')' | 'ABS(' arithmetic_expression ')' | 'SQRT(' arithmetic_expression ')' | 'MOD(' arithmetic_expression ',' arithmetic_expression ')' | 'SIZE(' path_expression ')' | 'INDEX(' identification_variable ')' );
	public final JPA2Parser.functions_returning_numerics_return functions_returning_numerics() throws RecognitionException {
		JPA2Parser.functions_returning_numerics_return retval = new JPA2Parser.functions_returning_numerics_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal475=null;
		Token char_literal477=null;
		Token string_literal478=null;
		Token char_literal480=null;
		Token char_literal482=null;
		Token char_literal484=null;
		Token string_literal485=null;
		Token char_literal487=null;
		Token string_literal488=null;
		Token char_literal490=null;
		Token string_literal491=null;
		Token char_literal493=null;
		Token char_literal495=null;
		Token string_literal496=null;
		Token char_literal498=null;
		Token string_literal499=null;
		Token char_literal501=null;
		ParserRuleReturnScope string_expression476 =null;
		ParserRuleReturnScope string_expression479 =null;
		ParserRuleReturnScope string_expression481 =null;
		ParserRuleReturnScope arithmetic_expression483 =null;
		ParserRuleReturnScope arithmetic_expression486 =null;
		ParserRuleReturnScope arithmetic_expression489 =null;
		ParserRuleReturnScope arithmetic_expression492 =null;
		ParserRuleReturnScope arithmetic_expression494 =null;
		ParserRuleReturnScope path_expression497 =null;
		ParserRuleReturnScope identification_variable500 =null;

		Object string_literal475_tree=null;
		Object char_literal477_tree=null;
		Object string_literal478_tree=null;
		Object char_literal480_tree=null;
		Object char_literal482_tree=null;
		Object char_literal484_tree=null;
		Object string_literal485_tree=null;
		Object char_literal487_tree=null;
		Object string_literal488_tree=null;
		Object char_literal490_tree=null;
		Object string_literal491_tree=null;
		Object char_literal493_tree=null;
		Object char_literal495_tree=null;
		Object string_literal496_tree=null;
		Object char_literal498_tree=null;
		Object string_literal499_tree=null;
		Object char_literal501_tree=null;

		try {
			// JPA2.g:432:5: ( 'LENGTH(' string_expression ')' | 'LOCATE(' string_expression ',' string_expression ( ',' arithmetic_expression )? ')' | 'ABS(' arithmetic_expression ')' | 'SQRT(' arithmetic_expression ')' | 'MOD(' arithmetic_expression ',' arithmetic_expression ')' | 'SIZE(' path_expression ')' | 'INDEX(' identification_variable ')' )
			int alt128=7;
			switch ( input.LA(1) ) {
			case 111:
				{
				alt128=1;
				}
				break;
			case 113:
				{
				alt128=2;
				}
				break;
			case 85:
				{
				alt128=3;
				}
				break;
			case 133:
				{
				alt128=4;
				}
				break;
			case 116:
				{
				alt128=5;
				}
				break;
			case 131:
				{
				alt128=6;
				}
				break;
			case 107:
				{
				alt128=7;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 128, 0, input);
				throw nvae;
			}
			switch (alt128) {
				case 1 :
					// JPA2.g:432:7: 'LENGTH(' string_expression ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal475=(Token)match(input,111,FOLLOW_111_in_functions_returning_numerics4058); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal475_tree = (Object)adaptor.create(string_literal475);
					adaptor.addChild(root_0, string_literal475_tree);
					}

					pushFollow(FOLLOW_string_expression_in_functions_returning_numerics4059);
					string_expression476=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression476.getTree());

					char_literal477=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_functions_returning_numerics4060); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal477_tree = (Object)adaptor.create(char_literal477);
					adaptor.addChild(root_0, char_literal477_tree);
					}

					}
					break;
				case 2 :
					// JPA2.g:433:7: 'LOCATE(' string_expression ',' string_expression ( ',' arithmetic_expression )? ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal478=(Token)match(input,113,FOLLOW_113_in_functions_returning_numerics4068); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal478_tree = (Object)adaptor.create(string_literal478);
					adaptor.addChild(root_0, string_literal478_tree);
					}

					pushFollow(FOLLOW_string_expression_in_functions_returning_numerics4070);
					string_expression479=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression479.getTree());

					char_literal480=(Token)match(input,66,FOLLOW_66_in_functions_returning_numerics4071); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal480_tree = (Object)adaptor.create(char_literal480);
					adaptor.addChild(root_0, char_literal480_tree);
					}

					pushFollow(FOLLOW_string_expression_in_functions_returning_numerics4073);
					string_expression481=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression481.getTree());

					// JPA2.g:433:55: ( ',' arithmetic_expression )?
					int alt127=2;
					int LA127_0 = input.LA(1);
					if ( (LA127_0==66) ) {
						alt127=1;
					}
					switch (alt127) {
						case 1 :
							// JPA2.g:433:56: ',' arithmetic_expression
							{
							char_literal482=(Token)match(input,66,FOLLOW_66_in_functions_returning_numerics4075); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							char_literal482_tree = (Object)adaptor.create(char_literal482);
							adaptor.addChild(root_0, char_literal482_tree);
							}

							pushFollow(FOLLOW_arithmetic_expression_in_functions_returning_numerics4076);
							arithmetic_expression483=arithmetic_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression483.getTree());

							}
							break;

					}

					char_literal484=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_functions_returning_numerics4079); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal484_tree = (Object)adaptor.create(char_literal484);
					adaptor.addChild(root_0, char_literal484_tree);
					}

					}
					break;
				case 3 :
					// JPA2.g:434:7: 'ABS(' arithmetic_expression ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal485=(Token)match(input,85,FOLLOW_85_in_functions_returning_numerics4087); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal485_tree = (Object)adaptor.create(string_literal485);
					adaptor.addChild(root_0, string_literal485_tree);
					}

					pushFollow(FOLLOW_arithmetic_expression_in_functions_returning_numerics4088);
					arithmetic_expression486=arithmetic_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression486.getTree());

					char_literal487=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_functions_returning_numerics4089); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal487_tree = (Object)adaptor.create(char_literal487);
					adaptor.addChild(root_0, char_literal487_tree);
					}

					}
					break;
				case 4 :
					// JPA2.g:435:7: 'SQRT(' arithmetic_expression ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal488=(Token)match(input,133,FOLLOW_133_in_functions_returning_numerics4097); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal488_tree = (Object)adaptor.create(string_literal488);
					adaptor.addChild(root_0, string_literal488_tree);
					}

					pushFollow(FOLLOW_arithmetic_expression_in_functions_returning_numerics4098);
					arithmetic_expression489=arithmetic_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression489.getTree());

					char_literal490=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_functions_returning_numerics4099); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal490_tree = (Object)adaptor.create(char_literal490);
					adaptor.addChild(root_0, char_literal490_tree);
					}

					}
					break;
				case 5 :
					// JPA2.g:436:7: 'MOD(' arithmetic_expression ',' arithmetic_expression ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal491=(Token)match(input,116,FOLLOW_116_in_functions_returning_numerics4107); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal491_tree = (Object)adaptor.create(string_literal491);
					adaptor.addChild(root_0, string_literal491_tree);
					}

					pushFollow(FOLLOW_arithmetic_expression_in_functions_returning_numerics4108);
					arithmetic_expression492=arithmetic_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression492.getTree());

					char_literal493=(Token)match(input,66,FOLLOW_66_in_functions_returning_numerics4109); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal493_tree = (Object)adaptor.create(char_literal493);
					adaptor.addChild(root_0, char_literal493_tree);
					}

					pushFollow(FOLLOW_arithmetic_expression_in_functions_returning_numerics4111);
					arithmetic_expression494=arithmetic_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression494.getTree());

					char_literal495=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_functions_returning_numerics4112); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal495_tree = (Object)adaptor.create(char_literal495);
					adaptor.addChild(root_0, char_literal495_tree);
					}

					}
					break;
				case 6 :
					// JPA2.g:437:7: 'SIZE(' path_expression ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal496=(Token)match(input,131,FOLLOW_131_in_functions_returning_numerics4120); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal496_tree = (Object)adaptor.create(string_literal496);
					adaptor.addChild(root_0, string_literal496_tree);
					}

					pushFollow(FOLLOW_path_expression_in_functions_returning_numerics4121);
					path_expression497=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression497.getTree());

					char_literal498=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_functions_returning_numerics4122); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal498_tree = (Object)adaptor.create(char_literal498);
					adaptor.addChild(root_0, char_literal498_tree);
					}

					}
					break;
				case 7 :
					// JPA2.g:438:7: 'INDEX(' identification_variable ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal499=(Token)match(input,107,FOLLOW_107_in_functions_returning_numerics4130); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal499_tree = (Object)adaptor.create(string_literal499);
					adaptor.addChild(root_0, string_literal499_tree);
					}

					pushFollow(FOLLOW_identification_variable_in_functions_returning_numerics4131);
					identification_variable500=identification_variable();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, identification_variable500.getTree());

					char_literal501=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_functions_returning_numerics4132); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal501_tree = (Object)adaptor.create(char_literal501);
					adaptor.addChild(root_0, char_literal501_tree);
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "functions_returning_numerics"


	public static class functions_returning_datetime_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "functions_returning_datetime"
	// JPA2.g:439:1: functions_returning_datetime : ( 'CURRENT_DATE' | 'CURRENT_TIME' | 'CURRENT_TIMESTAMP' );
	public final JPA2Parser.functions_returning_datetime_return functions_returning_datetime() throws RecognitionException {
		JPA2Parser.functions_returning_datetime_return retval = new JPA2Parser.functions_returning_datetime_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set502=null;

		Object set502_tree=null;

		try {
			// JPA2.g:440:5: ( 'CURRENT_DATE' | 'CURRENT_TIME' | 'CURRENT_TIMESTAMP' )
			// JPA2.g:
			{
			root_0 = (Object)adaptor.nil();


			set502=input.LT(1);
			if ( (input.LA(1) >= 93 && input.LA(1) <= 95) ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set502));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "functions_returning_datetime"


	public static class functions_returning_strings_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "functions_returning_strings"
	// JPA2.g:443:1: functions_returning_strings : ( 'CONCAT(' string_expression ',' string_expression ( ',' string_expression )* ')' | 'SUBSTRING(' string_expression ',' arithmetic_expression ( ',' arithmetic_expression )? ')' | 'TRIM(' ( ( trim_specification )? ( trim_character )? 'FROM' )? string_expression ')' | 'LOWER' '(' string_expression ')' | 'UPPER(' string_expression ')' );
	public final JPA2Parser.functions_returning_strings_return functions_returning_strings() throws RecognitionException {
		JPA2Parser.functions_returning_strings_return retval = new JPA2Parser.functions_returning_strings_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal503=null;
		Token char_literal505=null;
		Token char_literal507=null;
		Token char_literal509=null;
		Token string_literal510=null;
		Token char_literal512=null;
		Token char_literal514=null;
		Token char_literal516=null;
		Token string_literal517=null;
		Token string_literal520=null;
		Token char_literal522=null;
		Token string_literal523=null;
		Token char_literal524=null;
		Token char_literal526=null;
		Token string_literal527=null;
		Token char_literal529=null;
		ParserRuleReturnScope string_expression504 =null;
		ParserRuleReturnScope string_expression506 =null;
		ParserRuleReturnScope string_expression508 =null;
		ParserRuleReturnScope string_expression511 =null;
		ParserRuleReturnScope arithmetic_expression513 =null;
		ParserRuleReturnScope arithmetic_expression515 =null;
		ParserRuleReturnScope trim_specification518 =null;
		ParserRuleReturnScope trim_character519 =null;
		ParserRuleReturnScope string_expression521 =null;
		ParserRuleReturnScope string_expression525 =null;
		ParserRuleReturnScope string_expression528 =null;

		Object string_literal503_tree=null;
		Object char_literal505_tree=null;
		Object char_literal507_tree=null;
		Object char_literal509_tree=null;
		Object string_literal510_tree=null;
		Object char_literal512_tree=null;
		Object char_literal514_tree=null;
		Object char_literal516_tree=null;
		Object string_literal517_tree=null;
		Object string_literal520_tree=null;
		Object char_literal522_tree=null;
		Object string_literal523_tree=null;
		Object char_literal524_tree=null;
		Object char_literal526_tree=null;
		Object string_literal527_tree=null;
		Object char_literal529_tree=null;

		try {
			// JPA2.g:444:5: ( 'CONCAT(' string_expression ',' string_expression ( ',' string_expression )* ')' | 'SUBSTRING(' string_expression ',' arithmetic_expression ( ',' arithmetic_expression )? ')' | 'TRIM(' ( ( trim_specification )? ( trim_character )? 'FROM' )? string_expression ')' | 'LOWER' '(' string_expression ')' | 'UPPER(' string_expression ')' )
			int alt134=5;
			switch ( input.LA(1) ) {
			case 92:
				{
				alt134=1;
				}
				break;
			case 134:
				{
				alt134=2;
				}
				break;
			case 137:
				{
				alt134=3;
				}
				break;
			case LOWER:
				{
				alt134=4;
				}
				break;
			case 140:
				{
				alt134=5;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 134, 0, input);
				throw nvae;
			}
			switch (alt134) {
				case 1 :
					// JPA2.g:444:7: 'CONCAT(' string_expression ',' string_expression ( ',' string_expression )* ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal503=(Token)match(input,92,FOLLOW_92_in_functions_returning_strings4170); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal503_tree = (Object)adaptor.create(string_literal503);
					adaptor.addChild(root_0, string_literal503_tree);
					}

					pushFollow(FOLLOW_string_expression_in_functions_returning_strings4171);
					string_expression504=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression504.getTree());

					char_literal505=(Token)match(input,66,FOLLOW_66_in_functions_returning_strings4172); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal505_tree = (Object)adaptor.create(char_literal505);
					adaptor.addChild(root_0, char_literal505_tree);
					}

					pushFollow(FOLLOW_string_expression_in_functions_returning_strings4174);
					string_expression506=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression506.getTree());

					// JPA2.g:444:55: ( ',' string_expression )*
					loop129:
					while (true) {
						int alt129=2;
						int LA129_0 = input.LA(1);
						if ( (LA129_0==66) ) {
							alt129=1;
						}

						switch (alt129) {
						case 1 :
							// JPA2.g:444:56: ',' string_expression
							{
							char_literal507=(Token)match(input,66,FOLLOW_66_in_functions_returning_strings4177); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							char_literal507_tree = (Object)adaptor.create(char_literal507);
							adaptor.addChild(root_0, char_literal507_tree);
							}

							pushFollow(FOLLOW_string_expression_in_functions_returning_strings4179);
							string_expression508=string_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression508.getTree());

							}
							break;

						default :
							break loop129;
						}
					}

					char_literal509=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_functions_returning_strings4182); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal509_tree = (Object)adaptor.create(char_literal509);
					adaptor.addChild(root_0, char_literal509_tree);
					}

					}
					break;
				case 2 :
					// JPA2.g:445:7: 'SUBSTRING(' string_expression ',' arithmetic_expression ( ',' arithmetic_expression )? ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal510=(Token)match(input,134,FOLLOW_134_in_functions_returning_strings4190); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal510_tree = (Object)adaptor.create(string_literal510);
					adaptor.addChild(root_0, string_literal510_tree);
					}

					pushFollow(FOLLOW_string_expression_in_functions_returning_strings4192);
					string_expression511=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression511.getTree());

					char_literal512=(Token)match(input,66,FOLLOW_66_in_functions_returning_strings4193); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal512_tree = (Object)adaptor.create(char_literal512);
					adaptor.addChild(root_0, char_literal512_tree);
					}

					pushFollow(FOLLOW_arithmetic_expression_in_functions_returning_strings4195);
					arithmetic_expression513=arithmetic_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression513.getTree());

					// JPA2.g:445:63: ( ',' arithmetic_expression )?
					int alt130=2;
					int LA130_0 = input.LA(1);
					if ( (LA130_0==66) ) {
						alt130=1;
					}
					switch (alt130) {
						case 1 :
							// JPA2.g:445:64: ',' arithmetic_expression
							{
							char_literal514=(Token)match(input,66,FOLLOW_66_in_functions_returning_strings4198); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							char_literal514_tree = (Object)adaptor.create(char_literal514);
							adaptor.addChild(root_0, char_literal514_tree);
							}

							pushFollow(FOLLOW_arithmetic_expression_in_functions_returning_strings4200);
							arithmetic_expression515=arithmetic_expression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, arithmetic_expression515.getTree());

							}
							break;

					}

					char_literal516=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_functions_returning_strings4203); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal516_tree = (Object)adaptor.create(char_literal516);
					adaptor.addChild(root_0, char_literal516_tree);
					}

					}
					break;
				case 3 :
					// JPA2.g:446:7: 'TRIM(' ( ( trim_specification )? ( trim_character )? 'FROM' )? string_expression ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal517=(Token)match(input,137,FOLLOW_137_in_functions_returning_strings4211); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal517_tree = (Object)adaptor.create(string_literal517);
					adaptor.addChild(root_0, string_literal517_tree);
					}

					// JPA2.g:446:14: ( ( trim_specification )? ( trim_character )? 'FROM' )?
					int alt133=2;
					int LA133_0 = input.LA(1);
					if ( (LA133_0==TRIM_CHARACTER||LA133_0==89||LA133_0==104||LA133_0==110||LA133_0==135) ) {
						alt133=1;
					}
					switch (alt133) {
						case 1 :
							// JPA2.g:446:15: ( trim_specification )? ( trim_character )? 'FROM'
							{
							// JPA2.g:446:15: ( trim_specification )?
							int alt131=2;
							int LA131_0 = input.LA(1);
							if ( (LA131_0==89||LA131_0==110||LA131_0==135) ) {
								alt131=1;
							}
							switch (alt131) {
								case 1 :
									// JPA2.g:446:16: trim_specification
									{
									pushFollow(FOLLOW_trim_specification_in_functions_returning_strings4214);
									trim_specification518=trim_specification();
									state._fsp--;
									if (state.failed) return retval;
									if ( state.backtracking==0 ) adaptor.addChild(root_0, trim_specification518.getTree());

									}
									break;

							}

							// JPA2.g:446:37: ( trim_character )?
							int alt132=2;
							int LA132_0 = input.LA(1);
							if ( (LA132_0==TRIM_CHARACTER) ) {
								alt132=1;
							}
							switch (alt132) {
								case 1 :
									// JPA2.g:446:38: trim_character
									{
									pushFollow(FOLLOW_trim_character_in_functions_returning_strings4219);
									trim_character519=trim_character();
									state._fsp--;
									if (state.failed) return retval;
									if ( state.backtracking==0 ) adaptor.addChild(root_0, trim_character519.getTree());

									}
									break;

							}

							string_literal520=(Token)match(input,104,FOLLOW_104_in_functions_returning_strings4223); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							string_literal520_tree = (Object)adaptor.create(string_literal520);
							adaptor.addChild(root_0, string_literal520_tree);
							}

							}
							break;

					}

					pushFollow(FOLLOW_string_expression_in_functions_returning_strings4227);
					string_expression521=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression521.getTree());

					char_literal522=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_functions_returning_strings4229); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal522_tree = (Object)adaptor.create(char_literal522);
					adaptor.addChild(root_0, char_literal522_tree);
					}

					}
					break;
				case 4 :
					// JPA2.g:447:7: 'LOWER' '(' string_expression ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal523=(Token)match(input,LOWER,FOLLOW_LOWER_in_functions_returning_strings4237); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal523_tree = (Object)adaptor.create(string_literal523);
					adaptor.addChild(root_0, string_literal523_tree);
					}

					char_literal524=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_functions_returning_strings4239); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal524_tree = (Object)adaptor.create(char_literal524);
					adaptor.addChild(root_0, char_literal524_tree);
					}

					pushFollow(FOLLOW_string_expression_in_functions_returning_strings4240);
					string_expression525=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression525.getTree());

					char_literal526=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_functions_returning_strings4241); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal526_tree = (Object)adaptor.create(char_literal526);
					adaptor.addChild(root_0, char_literal526_tree);
					}

					}
					break;
				case 5 :
					// JPA2.g:448:7: 'UPPER(' string_expression ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal527=(Token)match(input,140,FOLLOW_140_in_functions_returning_strings4249); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal527_tree = (Object)adaptor.create(string_literal527);
					adaptor.addChild(root_0, string_literal527_tree);
					}

					pushFollow(FOLLOW_string_expression_in_functions_returning_strings4250);
					string_expression528=string_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, string_expression528.getTree());

					char_literal529=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_functions_returning_strings4251); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal529_tree = (Object)adaptor.create(char_literal529);
					adaptor.addChild(root_0, char_literal529_tree);
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "functions_returning_strings"


	public static class trim_specification_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "trim_specification"
	// JPA2.g:449:1: trim_specification : ( 'LEADING' | 'TRAILING' | 'BOTH' );
	public final JPA2Parser.trim_specification_return trim_specification() throws RecognitionException {
		JPA2Parser.trim_specification_return retval = new JPA2Parser.trim_specification_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set530=null;

		Object set530_tree=null;

		try {
			// JPA2.g:450:5: ( 'LEADING' | 'TRAILING' | 'BOTH' )
			// JPA2.g:
			{
			root_0 = (Object)adaptor.nil();


			set530=input.LT(1);
			if ( input.LA(1)==89||input.LA(1)==110||input.LA(1)==135 ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set530));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "trim_specification"


	public static class function_invocation_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "function_invocation"
	// JPA2.g:451:1: function_invocation : 'FUNCTION(' function_name ( ',' function_arg )* ')' ;
	public final JPA2Parser.function_invocation_return function_invocation() throws RecognitionException {
		JPA2Parser.function_invocation_return retval = new JPA2Parser.function_invocation_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal531=null;
		Token char_literal533=null;
		Token char_literal535=null;
		ParserRuleReturnScope function_name532 =null;
		ParserRuleReturnScope function_arg534 =null;

		Object string_literal531_tree=null;
		Object char_literal533_tree=null;
		Object char_literal535_tree=null;

		try {
			// JPA2.g:452:5: ( 'FUNCTION(' function_name ( ',' function_arg )* ')' )
			// JPA2.g:452:7: 'FUNCTION(' function_name ( ',' function_arg )* ')'
			{
			root_0 = (Object)adaptor.nil();


			string_literal531=(Token)match(input,105,FOLLOW_105_in_function_invocation4281); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal531_tree = (Object)adaptor.create(string_literal531);
			adaptor.addChild(root_0, string_literal531_tree);
			}

			pushFollow(FOLLOW_function_name_in_function_invocation4282);
			function_name532=function_name();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, function_name532.getTree());

			// JPA2.g:452:32: ( ',' function_arg )*
			loop135:
			while (true) {
				int alt135=2;
				int LA135_0 = input.LA(1);
				if ( (LA135_0==66) ) {
					alt135=1;
				}

				switch (alt135) {
				case 1 :
					// JPA2.g:452:33: ',' function_arg
					{
					char_literal533=(Token)match(input,66,FOLLOW_66_in_function_invocation4285); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal533_tree = (Object)adaptor.create(char_literal533);
					adaptor.addChild(root_0, char_literal533_tree);
					}

					pushFollow(FOLLOW_function_arg_in_function_invocation4287);
					function_arg534=function_arg();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, function_arg534.getTree());

					}
					break;

				default :
					break loop135;
				}
			}

			char_literal535=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_function_invocation4291); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal535_tree = (Object)adaptor.create(char_literal535);
			adaptor.addChild(root_0, char_literal535_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "function_invocation"


	public static class function_arg_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "function_arg"
	// JPA2.g:453:1: function_arg : ( literal | path_expression | input_parameter | scalar_expression );
	public final JPA2Parser.function_arg_return function_arg() throws RecognitionException {
		JPA2Parser.function_arg_return retval = new JPA2Parser.function_arg_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope literal536 =null;
		ParserRuleReturnScope path_expression537 =null;
		ParserRuleReturnScope input_parameter538 =null;
		ParserRuleReturnScope scalar_expression539 =null;


		try {
			// JPA2.g:454:5: ( literal | path_expression | input_parameter | scalar_expression )
			int alt136=4;
			switch ( input.LA(1) ) {
			case WORD:
				{
				int LA136_1 = input.LA(2);
				if ( (LA136_1==68) ) {
					alt136=2;
				}
				else if ( (synpred269_JPA2()) ) {
					alt136=1;
				}
				else if ( (true) ) {
					alt136=4;
				}

				}
				break;
			case GROUP:
				{
				int LA136_2 = input.LA(2);
				if ( (LA136_2==68) ) {
					int LA136_9 = input.LA(3);
					if ( (synpred270_JPA2()) ) {
						alt136=2;
					}
					else if ( (true) ) {
						alt136=4;
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 136, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 77:
				{
				int LA136_3 = input.LA(2);
				if ( (LA136_3==70) ) {
					int LA136_10 = input.LA(3);
					if ( (LA136_10==INT_NUMERAL) ) {
						int LA136_14 = input.LA(4);
						if ( (synpred271_JPA2()) ) {
							alt136=3;
						}
						else if ( (true) ) {
							alt136=4;
						}

					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 136, 10, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA136_3==INT_NUMERAL) ) {
					int LA136_11 = input.LA(3);
					if ( (synpred271_JPA2()) ) {
						alt136=3;
					}
					else if ( (true) ) {
						alt136=4;
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 136, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case NAMED_PARAMETER:
				{
				int LA136_4 = input.LA(2);
				if ( (synpred271_JPA2()) ) {
					alt136=3;
				}
				else if ( (true) ) {
					alt136=4;
				}

				}
				break;
			case 63:
				{
				int LA136_5 = input.LA(2);
				if ( (LA136_5==WORD) ) {
					int LA136_13 = input.LA(3);
					if ( (LA136_13==148) ) {
						int LA136_15 = input.LA(4);
						if ( (synpred271_JPA2()) ) {
							alt136=3;
						}
						else if ( (true) ) {
							alt136=4;
						}

					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 136, 13, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 136, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case AVG:
			case CASE:
			case COUNT:
			case INT_NUMERAL:
			case LOWER:
			case LPAREN:
			case MAX:
			case MIN:
			case STRING_LITERAL:
			case SUM:
			case 65:
			case 67:
			case 70:
			case 83:
			case 85:
			case 90:
			case 91:
			case 92:
			case 93:
			case 94:
			case 95:
			case 103:
			case 105:
			case 107:
			case 111:
			case 113:
			case 116:
			case 121:
			case 131:
			case 133:
			case 134:
			case 137:
			case 138:
			case 140:
			case 146:
			case 147:
				{
				alt136=4;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 136, 0, input);
				throw nvae;
			}
			switch (alt136) {
				case 1 :
					// JPA2.g:454:7: literal
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_literal_in_function_arg4302);
					literal536=literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, literal536.getTree());

					}
					break;
				case 2 :
					// JPA2.g:455:7: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_function_arg4310);
					path_expression537=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression537.getTree());

					}
					break;
				case 3 :
					// JPA2.g:456:7: input_parameter
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_input_parameter_in_function_arg4318);
					input_parameter538=input_parameter();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter538.getTree());

					}
					break;
				case 4 :
					// JPA2.g:457:7: scalar_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_scalar_expression_in_function_arg4326);
					scalar_expression539=scalar_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression539.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "function_arg"


	public static class case_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "case_expression"
	// JPA2.g:458:1: case_expression : ( general_case_expression | simple_case_expression | coalesce_expression | nullif_expression );
	public final JPA2Parser.case_expression_return case_expression() throws RecognitionException {
		JPA2Parser.case_expression_return retval = new JPA2Parser.case_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope general_case_expression540 =null;
		ParserRuleReturnScope simple_case_expression541 =null;
		ParserRuleReturnScope coalesce_expression542 =null;
		ParserRuleReturnScope nullif_expression543 =null;


		try {
			// JPA2.g:459:5: ( general_case_expression | simple_case_expression | coalesce_expression | nullif_expression )
			int alt137=4;
			switch ( input.LA(1) ) {
			case CASE:
				{
				int LA137_1 = input.LA(2);
				if ( (LA137_1==WHEN) ) {
					alt137=1;
				}
				else if ( (LA137_1==GROUP||LA137_1==WORD||LA137_1==138) ) {
					alt137=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 137, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 91:
				{
				alt137=3;
				}
				break;
			case 121:
				{
				alt137=4;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 137, 0, input);
				throw nvae;
			}
			switch (alt137) {
				case 1 :
					// JPA2.g:459:7: general_case_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_general_case_expression_in_case_expression4337);
					general_case_expression540=general_case_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, general_case_expression540.getTree());

					}
					break;
				case 2 :
					// JPA2.g:460:7: simple_case_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_simple_case_expression_in_case_expression4345);
					simple_case_expression541=simple_case_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, simple_case_expression541.getTree());

					}
					break;
				case 3 :
					// JPA2.g:461:7: coalesce_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_coalesce_expression_in_case_expression4353);
					coalesce_expression542=coalesce_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, coalesce_expression542.getTree());

					}
					break;
				case 4 :
					// JPA2.g:462:7: nullif_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_nullif_expression_in_case_expression4361);
					nullif_expression543=nullif_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, nullif_expression543.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "case_expression"


	public static class general_case_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "general_case_expression"
	// JPA2.g:463:1: general_case_expression : CASE when_clause ( when_clause )* ELSE scalar_expression END ;
	public final JPA2Parser.general_case_expression_return general_case_expression() throws RecognitionException {
		JPA2Parser.general_case_expression_return retval = new JPA2Parser.general_case_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token CASE544=null;
		Token ELSE547=null;
		Token END549=null;
		ParserRuleReturnScope when_clause545 =null;
		ParserRuleReturnScope when_clause546 =null;
		ParserRuleReturnScope scalar_expression548 =null;

		Object CASE544_tree=null;
		Object ELSE547_tree=null;
		Object END549_tree=null;

		try {
			// JPA2.g:464:5: ( CASE when_clause ( when_clause )* ELSE scalar_expression END )
			// JPA2.g:464:7: CASE when_clause ( when_clause )* ELSE scalar_expression END
			{
			root_0 = (Object)adaptor.nil();


			CASE544=(Token)match(input,CASE,FOLLOW_CASE_in_general_case_expression4372); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			CASE544_tree = (Object)adaptor.create(CASE544);
			adaptor.addChild(root_0, CASE544_tree);
			}

			pushFollow(FOLLOW_when_clause_in_general_case_expression4374);
			when_clause545=when_clause();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, when_clause545.getTree());

			// JPA2.g:464:24: ( when_clause )*
			loop138:
			while (true) {
				int alt138=2;
				int LA138_0 = input.LA(1);
				if ( (LA138_0==WHEN) ) {
					alt138=1;
				}

				switch (alt138) {
				case 1 :
					// JPA2.g:464:25: when_clause
					{
					pushFollow(FOLLOW_when_clause_in_general_case_expression4377);
					when_clause546=when_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, when_clause546.getTree());

					}
					break;

				default :
					break loop138;
				}
			}

			ELSE547=(Token)match(input,ELSE,FOLLOW_ELSE_in_general_case_expression4381); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			ELSE547_tree = (Object)adaptor.create(ELSE547);
			adaptor.addChild(root_0, ELSE547_tree);
			}

			pushFollow(FOLLOW_scalar_expression_in_general_case_expression4383);
			scalar_expression548=scalar_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression548.getTree());

			END549=(Token)match(input,END,FOLLOW_END_in_general_case_expression4385); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			END549_tree = (Object)adaptor.create(END549);
			adaptor.addChild(root_0, END549_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "general_case_expression"


	public static class when_clause_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "when_clause"
	// JPA2.g:465:1: when_clause : WHEN conditional_expression THEN ( scalar_expression | 'NULL' ) ;
	public final JPA2Parser.when_clause_return when_clause() throws RecognitionException {
		JPA2Parser.when_clause_return retval = new JPA2Parser.when_clause_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WHEN550=null;
		Token THEN552=null;
		Token string_literal554=null;
		ParserRuleReturnScope conditional_expression551 =null;
		ParserRuleReturnScope scalar_expression553 =null;

		Object WHEN550_tree=null;
		Object THEN552_tree=null;
		Object string_literal554_tree=null;

		try {
			// JPA2.g:466:5: ( WHEN conditional_expression THEN ( scalar_expression | 'NULL' ) )
			// JPA2.g:466:7: WHEN conditional_expression THEN ( scalar_expression | 'NULL' )
			{
			root_0 = (Object)adaptor.nil();


			WHEN550=(Token)match(input,WHEN,FOLLOW_WHEN_in_when_clause4396); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WHEN550_tree = (Object)adaptor.create(WHEN550);
			adaptor.addChild(root_0, WHEN550_tree);
			}

			pushFollow(FOLLOW_conditional_expression_in_when_clause4398);
			conditional_expression551=conditional_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, conditional_expression551.getTree());

			THEN552=(Token)match(input,THEN,FOLLOW_THEN_in_when_clause4400); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			THEN552_tree = (Object)adaptor.create(THEN552);
			adaptor.addChild(root_0, THEN552_tree);
			}

			// JPA2.g:466:40: ( scalar_expression | 'NULL' )
			int alt139=2;
			int LA139_0 = input.LA(1);
			if ( (LA139_0==AVG||LA139_0==CASE||LA139_0==COUNT||LA139_0==GROUP||LA139_0==INT_NUMERAL||(LA139_0 >= LOWER && LA139_0 <= NAMED_PARAMETER)||(LA139_0 >= STRING_LITERAL && LA139_0 <= SUM)||LA139_0==WORD||LA139_0==63||LA139_0==65||LA139_0==67||LA139_0==70||LA139_0==77||LA139_0==83||LA139_0==85||(LA139_0 >= 90 && LA139_0 <= 95)||LA139_0==103||LA139_0==105||LA139_0==107||LA139_0==111||LA139_0==113||LA139_0==116||LA139_0==121||LA139_0==131||(LA139_0 >= 133 && LA139_0 <= 134)||(LA139_0 >= 137 && LA139_0 <= 138)||LA139_0==140||(LA139_0 >= 146 && LA139_0 <= 147)) ) {
				alt139=1;
			}
			else if ( (LA139_0==120) ) {
				alt139=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 139, 0, input);
				throw nvae;
			}

			switch (alt139) {
				case 1 :
					// JPA2.g:466:41: scalar_expression
					{
					pushFollow(FOLLOW_scalar_expression_in_when_clause4403);
					scalar_expression553=scalar_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression553.getTree());

					}
					break;
				case 2 :
					// JPA2.g:466:61: 'NULL'
					{
					string_literal554=(Token)match(input,120,FOLLOW_120_in_when_clause4407); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal554_tree = (Object)adaptor.create(string_literal554);
					adaptor.addChild(root_0, string_literal554_tree);
					}

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "when_clause"


	public static class simple_case_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "simple_case_expression"
	// JPA2.g:467:1: simple_case_expression : CASE case_operand simple_when_clause ( simple_when_clause )* ELSE ( scalar_expression | 'NULL' ) END ;
	public final JPA2Parser.simple_case_expression_return simple_case_expression() throws RecognitionException {
		JPA2Parser.simple_case_expression_return retval = new JPA2Parser.simple_case_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token CASE555=null;
		Token ELSE559=null;
		Token string_literal561=null;
		Token END562=null;
		ParserRuleReturnScope case_operand556 =null;
		ParserRuleReturnScope simple_when_clause557 =null;
		ParserRuleReturnScope simple_when_clause558 =null;
		ParserRuleReturnScope scalar_expression560 =null;

		Object CASE555_tree=null;
		Object ELSE559_tree=null;
		Object string_literal561_tree=null;
		Object END562_tree=null;

		try {
			// JPA2.g:468:5: ( CASE case_operand simple_when_clause ( simple_when_clause )* ELSE ( scalar_expression | 'NULL' ) END )
			// JPA2.g:468:7: CASE case_operand simple_when_clause ( simple_when_clause )* ELSE ( scalar_expression | 'NULL' ) END
			{
			root_0 = (Object)adaptor.nil();


			CASE555=(Token)match(input,CASE,FOLLOW_CASE_in_simple_case_expression4419); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			CASE555_tree = (Object)adaptor.create(CASE555);
			adaptor.addChild(root_0, CASE555_tree);
			}

			pushFollow(FOLLOW_case_operand_in_simple_case_expression4421);
			case_operand556=case_operand();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, case_operand556.getTree());

			pushFollow(FOLLOW_simple_when_clause_in_simple_case_expression4423);
			simple_when_clause557=simple_when_clause();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, simple_when_clause557.getTree());

			// JPA2.g:468:44: ( simple_when_clause )*
			loop140:
			while (true) {
				int alt140=2;
				int LA140_0 = input.LA(1);
				if ( (LA140_0==WHEN) ) {
					alt140=1;
				}

				switch (alt140) {
				case 1 :
					// JPA2.g:468:45: simple_when_clause
					{
					pushFollow(FOLLOW_simple_when_clause_in_simple_case_expression4426);
					simple_when_clause558=simple_when_clause();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, simple_when_clause558.getTree());

					}
					break;

				default :
					break loop140;
				}
			}

			ELSE559=(Token)match(input,ELSE,FOLLOW_ELSE_in_simple_case_expression4430); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			ELSE559_tree = (Object)adaptor.create(ELSE559);
			adaptor.addChild(root_0, ELSE559_tree);
			}

			// JPA2.g:468:71: ( scalar_expression | 'NULL' )
			int alt141=2;
			int LA141_0 = input.LA(1);
			if ( (LA141_0==AVG||LA141_0==CASE||LA141_0==COUNT||LA141_0==GROUP||LA141_0==INT_NUMERAL||(LA141_0 >= LOWER && LA141_0 <= NAMED_PARAMETER)||(LA141_0 >= STRING_LITERAL && LA141_0 <= SUM)||LA141_0==WORD||LA141_0==63||LA141_0==65||LA141_0==67||LA141_0==70||LA141_0==77||LA141_0==83||LA141_0==85||(LA141_0 >= 90 && LA141_0 <= 95)||LA141_0==103||LA141_0==105||LA141_0==107||LA141_0==111||LA141_0==113||LA141_0==116||LA141_0==121||LA141_0==131||(LA141_0 >= 133 && LA141_0 <= 134)||(LA141_0 >= 137 && LA141_0 <= 138)||LA141_0==140||(LA141_0 >= 146 && LA141_0 <= 147)) ) {
				alt141=1;
			}
			else if ( (LA141_0==120) ) {
				alt141=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 141, 0, input);
				throw nvae;
			}

			switch (alt141) {
				case 1 :
					// JPA2.g:468:72: scalar_expression
					{
					pushFollow(FOLLOW_scalar_expression_in_simple_case_expression4433);
					scalar_expression560=scalar_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression560.getTree());

					}
					break;
				case 2 :
					// JPA2.g:468:92: 'NULL'
					{
					string_literal561=(Token)match(input,120,FOLLOW_120_in_simple_case_expression4437); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal561_tree = (Object)adaptor.create(string_literal561);
					adaptor.addChild(root_0, string_literal561_tree);
					}

					}
					break;

			}

			END562=(Token)match(input,END,FOLLOW_END_in_simple_case_expression4440); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			END562_tree = (Object)adaptor.create(END562);
			adaptor.addChild(root_0, END562_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "simple_case_expression"


	public static class case_operand_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "case_operand"
	// JPA2.g:469:1: case_operand : ( path_expression | type_discriminator );
	public final JPA2Parser.case_operand_return case_operand() throws RecognitionException {
		JPA2Parser.case_operand_return retval = new JPA2Parser.case_operand_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope path_expression563 =null;
		ParserRuleReturnScope type_discriminator564 =null;


		try {
			// JPA2.g:470:5: ( path_expression | type_discriminator )
			int alt142=2;
			int LA142_0 = input.LA(1);
			if ( (LA142_0==GROUP||LA142_0==WORD) ) {
				alt142=1;
			}
			else if ( (LA142_0==138) ) {
				alt142=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 142, 0, input);
				throw nvae;
			}

			switch (alt142) {
				case 1 :
					// JPA2.g:470:7: path_expression
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_path_expression_in_case_operand4451);
					path_expression563=path_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, path_expression563.getTree());

					}
					break;
				case 2 :
					// JPA2.g:471:7: type_discriminator
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_type_discriminator_in_case_operand4459);
					type_discriminator564=type_discriminator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, type_discriminator564.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "case_operand"


	public static class simple_when_clause_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "simple_when_clause"
	// JPA2.g:472:1: simple_when_clause : WHEN scalar_expression THEN ( scalar_expression | 'NULL' ) ;
	public final JPA2Parser.simple_when_clause_return simple_when_clause() throws RecognitionException {
		JPA2Parser.simple_when_clause_return retval = new JPA2Parser.simple_when_clause_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WHEN565=null;
		Token THEN567=null;
		Token string_literal569=null;
		ParserRuleReturnScope scalar_expression566 =null;
		ParserRuleReturnScope scalar_expression568 =null;

		Object WHEN565_tree=null;
		Object THEN567_tree=null;
		Object string_literal569_tree=null;

		try {
			// JPA2.g:473:5: ( WHEN scalar_expression THEN ( scalar_expression | 'NULL' ) )
			// JPA2.g:473:7: WHEN scalar_expression THEN ( scalar_expression | 'NULL' )
			{
			root_0 = (Object)adaptor.nil();


			WHEN565=(Token)match(input,WHEN,FOLLOW_WHEN_in_simple_when_clause4470); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WHEN565_tree = (Object)adaptor.create(WHEN565);
			adaptor.addChild(root_0, WHEN565_tree);
			}

			pushFollow(FOLLOW_scalar_expression_in_simple_when_clause4472);
			scalar_expression566=scalar_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression566.getTree());

			THEN567=(Token)match(input,THEN,FOLLOW_THEN_in_simple_when_clause4474); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			THEN567_tree = (Object)adaptor.create(THEN567);
			adaptor.addChild(root_0, THEN567_tree);
			}

			// JPA2.g:473:35: ( scalar_expression | 'NULL' )
			int alt143=2;
			int LA143_0 = input.LA(1);
			if ( (LA143_0==AVG||LA143_0==CASE||LA143_0==COUNT||LA143_0==GROUP||LA143_0==INT_NUMERAL||(LA143_0 >= LOWER && LA143_0 <= NAMED_PARAMETER)||(LA143_0 >= STRING_LITERAL && LA143_0 <= SUM)||LA143_0==WORD||LA143_0==63||LA143_0==65||LA143_0==67||LA143_0==70||LA143_0==77||LA143_0==83||LA143_0==85||(LA143_0 >= 90 && LA143_0 <= 95)||LA143_0==103||LA143_0==105||LA143_0==107||LA143_0==111||LA143_0==113||LA143_0==116||LA143_0==121||LA143_0==131||(LA143_0 >= 133 && LA143_0 <= 134)||(LA143_0 >= 137 && LA143_0 <= 138)||LA143_0==140||(LA143_0 >= 146 && LA143_0 <= 147)) ) {
				alt143=1;
			}
			else if ( (LA143_0==120) ) {
				alt143=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 143, 0, input);
				throw nvae;
			}

			switch (alt143) {
				case 1 :
					// JPA2.g:473:36: scalar_expression
					{
					pushFollow(FOLLOW_scalar_expression_in_simple_when_clause4477);
					scalar_expression568=scalar_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression568.getTree());

					}
					break;
				case 2 :
					// JPA2.g:473:56: 'NULL'
					{
					string_literal569=(Token)match(input,120,FOLLOW_120_in_simple_when_clause4481); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal569_tree = (Object)adaptor.create(string_literal569);
					adaptor.addChild(root_0, string_literal569_tree);
					}

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "simple_when_clause"


	public static class coalesce_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "coalesce_expression"
	// JPA2.g:474:1: coalesce_expression : 'COALESCE(' scalar_expression ( ',' scalar_expression )+ ')' ;
	public final JPA2Parser.coalesce_expression_return coalesce_expression() throws RecognitionException {
		JPA2Parser.coalesce_expression_return retval = new JPA2Parser.coalesce_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal570=null;
		Token char_literal572=null;
		Token char_literal574=null;
		ParserRuleReturnScope scalar_expression571 =null;
		ParserRuleReturnScope scalar_expression573 =null;

		Object string_literal570_tree=null;
		Object char_literal572_tree=null;
		Object char_literal574_tree=null;

		try {
			// JPA2.g:475:5: ( 'COALESCE(' scalar_expression ( ',' scalar_expression )+ ')' )
			// JPA2.g:475:7: 'COALESCE(' scalar_expression ( ',' scalar_expression )+ ')'
			{
			root_0 = (Object)adaptor.nil();


			string_literal570=(Token)match(input,91,FOLLOW_91_in_coalesce_expression4493); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal570_tree = (Object)adaptor.create(string_literal570);
			adaptor.addChild(root_0, string_literal570_tree);
			}

			pushFollow(FOLLOW_scalar_expression_in_coalesce_expression4494);
			scalar_expression571=scalar_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression571.getTree());

			// JPA2.g:475:36: ( ',' scalar_expression )+
			int cnt144=0;
			loop144:
			while (true) {
				int alt144=2;
				int LA144_0 = input.LA(1);
				if ( (LA144_0==66) ) {
					alt144=1;
				}

				switch (alt144) {
				case 1 :
					// JPA2.g:475:37: ',' scalar_expression
					{
					char_literal572=(Token)match(input,66,FOLLOW_66_in_coalesce_expression4497); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal572_tree = (Object)adaptor.create(char_literal572);
					adaptor.addChild(root_0, char_literal572_tree);
					}

					pushFollow(FOLLOW_scalar_expression_in_coalesce_expression4499);
					scalar_expression573=scalar_expression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression573.getTree());

					}
					break;

				default :
					if ( cnt144 >= 1 ) break loop144;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(144, input);
					throw eee;
				}
				cnt144++;
			}

			char_literal574=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_coalesce_expression4502); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal574_tree = (Object)adaptor.create(char_literal574);
			adaptor.addChild(root_0, char_literal574_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "coalesce_expression"


	public static class nullif_expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "nullif_expression"
	// JPA2.g:476:1: nullif_expression : 'NULLIF(' scalar_expression ',' scalar_expression ')' ;
	public final JPA2Parser.nullif_expression_return nullif_expression() throws RecognitionException {
		JPA2Parser.nullif_expression_return retval = new JPA2Parser.nullif_expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal575=null;
		Token char_literal577=null;
		Token char_literal579=null;
		ParserRuleReturnScope scalar_expression576 =null;
		ParserRuleReturnScope scalar_expression578 =null;

		Object string_literal575_tree=null;
		Object char_literal577_tree=null;
		Object char_literal579_tree=null;

		try {
			// JPA2.g:477:5: ( 'NULLIF(' scalar_expression ',' scalar_expression ')' )
			// JPA2.g:477:7: 'NULLIF(' scalar_expression ',' scalar_expression ')'
			{
			root_0 = (Object)adaptor.nil();


			string_literal575=(Token)match(input,121,FOLLOW_121_in_nullif_expression4513); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal575_tree = (Object)adaptor.create(string_literal575);
			adaptor.addChild(root_0, string_literal575_tree);
			}

			pushFollow(FOLLOW_scalar_expression_in_nullif_expression4514);
			scalar_expression576=scalar_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression576.getTree());

			char_literal577=(Token)match(input,66,FOLLOW_66_in_nullif_expression4516); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal577_tree = (Object)adaptor.create(char_literal577);
			adaptor.addChild(root_0, char_literal577_tree);
			}

			pushFollow(FOLLOW_scalar_expression_in_nullif_expression4518);
			scalar_expression578=scalar_expression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, scalar_expression578.getTree());

			char_literal579=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_nullif_expression4519); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal579_tree = (Object)adaptor.create(char_literal579);
			adaptor.addChild(root_0, char_literal579_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "nullif_expression"


	public static class extension_functions_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "extension_functions"
	// JPA2.g:479:1: extension_functions : ( 'CAST(' function_arg WORD ( '(' INT_NUMERAL ( ',' INT_NUMERAL )* ')' )* ')' | extract_function | enum_function );
	public final JPA2Parser.extension_functions_return extension_functions() throws RecognitionException {
		JPA2Parser.extension_functions_return retval = new JPA2Parser.extension_functions_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal580=null;
		Token WORD582=null;
		Token char_literal583=null;
		Token INT_NUMERAL584=null;
		Token char_literal585=null;
		Token INT_NUMERAL586=null;
		Token char_literal587=null;
		Token char_literal588=null;
		ParserRuleReturnScope function_arg581 =null;
		ParserRuleReturnScope extract_function589 =null;
		ParserRuleReturnScope enum_function590 =null;

		Object string_literal580_tree=null;
		Object WORD582_tree=null;
		Object char_literal583_tree=null;
		Object INT_NUMERAL584_tree=null;
		Object char_literal585_tree=null;
		Object INT_NUMERAL586_tree=null;
		Object char_literal587_tree=null;
		Object char_literal588_tree=null;

		try {
			// JPA2.g:480:5: ( 'CAST(' function_arg WORD ( '(' INT_NUMERAL ( ',' INT_NUMERAL )* ')' )* ')' | extract_function | enum_function )
			int alt147=3;
			switch ( input.LA(1) ) {
			case 90:
				{
				alt147=1;
				}
				break;
			case 103:
				{
				alt147=2;
				}
				break;
			case 83:
				{
				alt147=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 147, 0, input);
				throw nvae;
			}
			switch (alt147) {
				case 1 :
					// JPA2.g:480:7: 'CAST(' function_arg WORD ( '(' INT_NUMERAL ( ',' INT_NUMERAL )* ')' )* ')'
					{
					root_0 = (Object)adaptor.nil();


					string_literal580=(Token)match(input,90,FOLLOW_90_in_extension_functions4531); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal580_tree = (Object)adaptor.create(string_literal580);
					adaptor.addChild(root_0, string_literal580_tree);
					}

					pushFollow(FOLLOW_function_arg_in_extension_functions4533);
					function_arg581=function_arg();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, function_arg581.getTree());

					WORD582=(Token)match(input,WORD,FOLLOW_WORD_in_extension_functions4535); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					WORD582_tree = (Object)adaptor.create(WORD582);
					adaptor.addChild(root_0, WORD582_tree);
					}

					// JPA2.g:480:33: ( '(' INT_NUMERAL ( ',' INT_NUMERAL )* ')' )*
					loop146:
					while (true) {
						int alt146=2;
						int LA146_0 = input.LA(1);
						if ( (LA146_0==LPAREN) ) {
							alt146=1;
						}

						switch (alt146) {
						case 1 :
							// JPA2.g:480:34: '(' INT_NUMERAL ( ',' INT_NUMERAL )* ')'
							{
							char_literal583=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_extension_functions4538); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							char_literal583_tree = (Object)adaptor.create(char_literal583);
							adaptor.addChild(root_0, char_literal583_tree);
							}

							INT_NUMERAL584=(Token)match(input,INT_NUMERAL,FOLLOW_INT_NUMERAL_in_extension_functions4539); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							INT_NUMERAL584_tree = (Object)adaptor.create(INT_NUMERAL584);
							adaptor.addChild(root_0, INT_NUMERAL584_tree);
							}

							// JPA2.g:480:49: ( ',' INT_NUMERAL )*
							loop145:
							while (true) {
								int alt145=2;
								int LA145_0 = input.LA(1);
								if ( (LA145_0==66) ) {
									alt145=1;
								}

								switch (alt145) {
								case 1 :
									// JPA2.g:480:50: ',' INT_NUMERAL
									{
									char_literal585=(Token)match(input,66,FOLLOW_66_in_extension_functions4542); if (state.failed) return retval;
									if ( state.backtracking==0 ) {
									char_literal585_tree = (Object)adaptor.create(char_literal585);
									adaptor.addChild(root_0, char_literal585_tree);
									}

									INT_NUMERAL586=(Token)match(input,INT_NUMERAL,FOLLOW_INT_NUMERAL_in_extension_functions4544); if (state.failed) return retval;
									if ( state.backtracking==0 ) {
									INT_NUMERAL586_tree = (Object)adaptor.create(INT_NUMERAL586);
									adaptor.addChild(root_0, INT_NUMERAL586_tree);
									}

									}
									break;

								default :
									break loop145;
								}
							}

							char_literal587=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_extension_functions4549); if (state.failed) return retval;
							if ( state.backtracking==0 ) {
							char_literal587_tree = (Object)adaptor.create(char_literal587);
							adaptor.addChild(root_0, char_literal587_tree);
							}

							}
							break;

						default :
							break loop146;
						}
					}

					char_literal588=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_extension_functions4553); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal588_tree = (Object)adaptor.create(char_literal588);
					adaptor.addChild(root_0, char_literal588_tree);
					}

					}
					break;
				case 2 :
					// JPA2.g:481:7: extract_function
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_extract_function_in_extension_functions4561);
					extract_function589=extract_function();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, extract_function589.getTree());

					}
					break;
				case 3 :
					// JPA2.g:482:7: enum_function
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_enum_function_in_extension_functions4569);
					enum_function590=enum_function();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, enum_function590.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "extension_functions"


	public static class extract_function_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "extract_function"
	// JPA2.g:484:1: extract_function : 'EXTRACT(' date_part 'FROM' function_arg ')' ;
	public final JPA2Parser.extract_function_return extract_function() throws RecognitionException {
		JPA2Parser.extract_function_return retval = new JPA2Parser.extract_function_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal591=null;
		Token string_literal593=null;
		Token char_literal595=null;
		ParserRuleReturnScope date_part592 =null;
		ParserRuleReturnScope function_arg594 =null;

		Object string_literal591_tree=null;
		Object string_literal593_tree=null;
		Object char_literal595_tree=null;

		try {
			// JPA2.g:485:5: ( 'EXTRACT(' date_part 'FROM' function_arg ')' )
			// JPA2.g:485:7: 'EXTRACT(' date_part 'FROM' function_arg ')'
			{
			root_0 = (Object)adaptor.nil();


			string_literal591=(Token)match(input,103,FOLLOW_103_in_extract_function4581); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal591_tree = (Object)adaptor.create(string_literal591);
			adaptor.addChild(root_0, string_literal591_tree);
			}

			pushFollow(FOLLOW_date_part_in_extract_function4583);
			date_part592=date_part();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, date_part592.getTree());

			string_literal593=(Token)match(input,104,FOLLOW_104_in_extract_function4585); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			string_literal593_tree = (Object)adaptor.create(string_literal593);
			adaptor.addChild(root_0, string_literal593_tree);
			}

			pushFollow(FOLLOW_function_arg_in_extract_function4587);
			function_arg594=function_arg();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, function_arg594.getTree());

			char_literal595=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_extract_function4589); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal595_tree = (Object)adaptor.create(char_literal595);
			adaptor.addChild(root_0, char_literal595_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "extract_function"


	public static class enum_function_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "enum_function"
	// JPA2.g:487:1: enum_function : '@ENUM' '(' enum_value_literal ')' -> ^( T_ENUM_MACROS[$enum_value_literal.text] ) ;
	public final JPA2Parser.enum_function_return enum_function() throws RecognitionException {
		JPA2Parser.enum_function_return retval = new JPA2Parser.enum_function_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal596=null;
		Token char_literal597=null;
		Token char_literal599=null;
		ParserRuleReturnScope enum_value_literal598 =null;

		Object string_literal596_tree=null;
		Object char_literal597_tree=null;
		Object char_literal599_tree=null;
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_83=new RewriteRuleTokenStream(adaptor,"token 83");
		RewriteRuleSubtreeStream stream_enum_value_literal=new RewriteRuleSubtreeStream(adaptor,"rule enum_value_literal");

		try {
			// JPA2.g:488:5: ( '@ENUM' '(' enum_value_literal ')' -> ^( T_ENUM_MACROS[$enum_value_literal.text] ) )
			// JPA2.g:488:7: '@ENUM' '(' enum_value_literal ')'
			{
			string_literal596=(Token)match(input,83,FOLLOW_83_in_enum_function4601); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_83.add(string_literal596);

			char_literal597=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_enum_function4603); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LPAREN.add(char_literal597);

			pushFollow(FOLLOW_enum_value_literal_in_enum_function4605);
			enum_value_literal598=enum_value_literal();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_enum_value_literal.add(enum_value_literal598.getTree());
			char_literal599=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_enum_function4607); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RPAREN.add(char_literal599);

			// AST REWRITE
			// elements: 
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 488:42: -> ^( T_ENUM_MACROS[$enum_value_literal.text] )
			{
				// JPA2.g:488:45: ^( T_ENUM_MACROS[$enum_value_literal.text] )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(new EnumConditionNode(T_ENUM_MACROS, (enum_value_literal598!=null?input.toString(enum_value_literal598.start,enum_value_literal598.stop):null)), root_1);
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enum_function"


	public static class date_part_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "date_part"
	// JPA2.g:490:10: fragment date_part : ( 'EPOCH' | 'YEAR' | 'QUARTER' | 'MONTH' | 'WEEK' | 'DAY' | 'HOUR' | 'MINUTE' | 'SECOND' );
	public final JPA2Parser.date_part_return date_part() throws RecognitionException {
		JPA2Parser.date_part_return retval = new JPA2Parser.date_part_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set600=null;

		Object set600_tree=null;

		try {
			// JPA2.g:491:5: ( 'EPOCH' | 'YEAR' | 'QUARTER' | 'MONTH' | 'WEEK' | 'DAY' | 'HOUR' | 'MINUTE' | 'SECOND' )
			// JPA2.g:
			{
			root_0 = (Object)adaptor.nil();


			set600=input.LT(1);
			if ( input.LA(1)==96||input.LA(1)==100||input.LA(1)==106||input.LA(1)==115||input.LA(1)==117||input.LA(1)==127||input.LA(1)==129||input.LA(1)==143||input.LA(1)==145 ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set600));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "date_part"


	public static class input_parameter_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "input_parameter"
	// JPA2.g:494:1: input_parameter : ( '?' numeric_literal -> ^( T_PARAMETER[] '?' numeric_literal ) | NAMED_PARAMETER -> ^( T_PARAMETER[] NAMED_PARAMETER ) | '${' WORD '}' -> ^( T_PARAMETER[] '${' WORD '}' ) );
	public final JPA2Parser.input_parameter_return input_parameter() throws RecognitionException {
		JPA2Parser.input_parameter_return retval = new JPA2Parser.input_parameter_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal601=null;
		Token NAMED_PARAMETER603=null;
		Token string_literal604=null;
		Token WORD605=null;
		Token char_literal606=null;
		ParserRuleReturnScope numeric_literal602 =null;

		Object char_literal601_tree=null;
		Object NAMED_PARAMETER603_tree=null;
		Object string_literal604_tree=null;
		Object WORD605_tree=null;
		Object char_literal606_tree=null;
		RewriteRuleTokenStream stream_77=new RewriteRuleTokenStream(adaptor,"token 77");
		RewriteRuleTokenStream stream_WORD=new RewriteRuleTokenStream(adaptor,"token WORD");
		RewriteRuleTokenStream stream_148=new RewriteRuleTokenStream(adaptor,"token 148");
		RewriteRuleTokenStream stream_63=new RewriteRuleTokenStream(adaptor,"token 63");
		RewriteRuleTokenStream stream_NAMED_PARAMETER=new RewriteRuleTokenStream(adaptor,"token NAMED_PARAMETER");
		RewriteRuleSubtreeStream stream_numeric_literal=new RewriteRuleSubtreeStream(adaptor,"rule numeric_literal");

		try {
			// JPA2.g:495:5: ( '?' numeric_literal -> ^( T_PARAMETER[] '?' numeric_literal ) | NAMED_PARAMETER -> ^( T_PARAMETER[] NAMED_PARAMETER ) | '${' WORD '}' -> ^( T_PARAMETER[] '${' WORD '}' ) )
			int alt148=3;
			switch ( input.LA(1) ) {
			case 77:
				{
				alt148=1;
				}
				break;
			case NAMED_PARAMETER:
				{
				alt148=2;
				}
				break;
			case 63:
				{
				alt148=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 148, 0, input);
				throw nvae;
			}
			switch (alt148) {
				case 1 :
					// JPA2.g:495:7: '?' numeric_literal
					{
					char_literal601=(Token)match(input,77,FOLLOW_77_in_input_parameter4674); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_77.add(char_literal601);

					pushFollow(FOLLOW_numeric_literal_in_input_parameter4676);
					numeric_literal602=numeric_literal();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_numeric_literal.add(numeric_literal602.getTree());
					// AST REWRITE
					// elements: 77, numeric_literal
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 495:27: -> ^( T_PARAMETER[] '?' numeric_literal )
					{
						// JPA2.g:495:30: ^( T_PARAMETER[] '?' numeric_literal )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot(new ParameterNode(T_PARAMETER), root_1);
						adaptor.addChild(root_1, stream_77.nextNode());
						adaptor.addChild(root_1, stream_numeric_literal.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// JPA2.g:496:7: NAMED_PARAMETER
					{
					NAMED_PARAMETER603=(Token)match(input,NAMED_PARAMETER,FOLLOW_NAMED_PARAMETER_in_input_parameter4699); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_NAMED_PARAMETER.add(NAMED_PARAMETER603);

					// AST REWRITE
					// elements: NAMED_PARAMETER
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 496:23: -> ^( T_PARAMETER[] NAMED_PARAMETER )
					{
						// JPA2.g:496:26: ^( T_PARAMETER[] NAMED_PARAMETER )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot(new ParameterNode(T_PARAMETER), root_1);
						adaptor.addChild(root_1, stream_NAMED_PARAMETER.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// JPA2.g:497:7: '${' WORD '}'
					{
					string_literal604=(Token)match(input,63,FOLLOW_63_in_input_parameter4720); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_63.add(string_literal604);

					WORD605=(Token)match(input,WORD,FOLLOW_WORD_in_input_parameter4722); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_WORD.add(WORD605);

					char_literal606=(Token)match(input,148,FOLLOW_148_in_input_parameter4724); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_148.add(char_literal606);

					// AST REWRITE
					// elements: 63, WORD, 148
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 497:21: -> ^( T_PARAMETER[] '${' WORD '}' )
					{
						// JPA2.g:497:24: ^( T_PARAMETER[] '${' WORD '}' )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot(new ParameterNode(T_PARAMETER), root_1);
						adaptor.addChild(root_1, stream_63.nextNode());
						adaptor.addChild(root_1, stream_WORD.nextNode());
						adaptor.addChild(root_1, stream_148.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "input_parameter"


	public static class literal_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "literal"
	// JPA2.g:499:1: literal : WORD ;
	public final JPA2Parser.literal_return literal() throws RecognitionException {
		JPA2Parser.literal_return retval = new JPA2Parser.literal_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD607=null;

		Object WORD607_tree=null;

		try {
			// JPA2.g:500:5: ( WORD )
			// JPA2.g:500:7: WORD
			{
			root_0 = (Object)adaptor.nil();


			WORD607=(Token)match(input,WORD,FOLLOW_WORD_in_literal4752); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD607_tree = (Object)adaptor.create(WORD607);
			adaptor.addChild(root_0, WORD607_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "literal"


	public static class constructor_name_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "constructor_name"
	// JPA2.g:502:1: constructor_name : WORD ( '.' WORD )* ;
	public final JPA2Parser.constructor_name_return constructor_name() throws RecognitionException {
		JPA2Parser.constructor_name_return retval = new JPA2Parser.constructor_name_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD608=null;
		Token char_literal609=null;
		Token WORD610=null;

		Object WORD608_tree=null;
		Object char_literal609_tree=null;
		Object WORD610_tree=null;

		try {
			// JPA2.g:503:5: ( WORD ( '.' WORD )* )
			// JPA2.g:503:7: WORD ( '.' WORD )*
			{
			root_0 = (Object)adaptor.nil();


			WORD608=(Token)match(input,WORD,FOLLOW_WORD_in_constructor_name4764); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD608_tree = (Object)adaptor.create(WORD608);
			adaptor.addChild(root_0, WORD608_tree);
			}

			// JPA2.g:503:12: ( '.' WORD )*
			loop149:
			while (true) {
				int alt149=2;
				int LA149_0 = input.LA(1);
				if ( (LA149_0==68) ) {
					alt149=1;
				}

				switch (alt149) {
				case 1 :
					// JPA2.g:503:13: '.' WORD
					{
					char_literal609=(Token)match(input,68,FOLLOW_68_in_constructor_name4767); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal609_tree = (Object)adaptor.create(char_literal609);
					adaptor.addChild(root_0, char_literal609_tree);
					}

					WORD610=(Token)match(input,WORD,FOLLOW_WORD_in_constructor_name4770); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					WORD610_tree = (Object)adaptor.create(WORD610);
					adaptor.addChild(root_0, WORD610_tree);
					}

					}
					break;

				default :
					break loop149;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "constructor_name"


	public static class enum_literal_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "enum_literal"
	// JPA2.g:505:1: enum_literal : WORD ;
	public final JPA2Parser.enum_literal_return enum_literal() throws RecognitionException {
		JPA2Parser.enum_literal_return retval = new JPA2Parser.enum_literal_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD611=null;

		Object WORD611_tree=null;

		try {
			// JPA2.g:506:5: ( WORD )
			// JPA2.g:506:7: WORD
			{
			root_0 = (Object)adaptor.nil();


			WORD611=(Token)match(input,WORD,FOLLOW_WORD_in_enum_literal4784); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD611_tree = (Object)adaptor.create(WORD611);
			adaptor.addChild(root_0, WORD611_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enum_literal"


	public static class boolean_literal_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "boolean_literal"
	// JPA2.g:508:1: boolean_literal : ( 'true' | 'false' );
	public final JPA2Parser.boolean_literal_return boolean_literal() throws RecognitionException {
		JPA2Parser.boolean_literal_return retval = new JPA2Parser.boolean_literal_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set612=null;

		Object set612_tree=null;

		try {
			// JPA2.g:509:5: ( 'true' | 'false' )
			// JPA2.g:
			{
			root_0 = (Object)adaptor.nil();


			set612=input.LT(1);
			if ( (input.LA(1) >= 146 && input.LA(1) <= 147) ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set612));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "boolean_literal"


	public static class field_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "field"
	// JPA2.g:513:1: field : ( WORD | 'SELECT' | 'FROM' | 'GROUP' | 'ORDER' | 'MAX' | 'MIN' | 'SUM' | 'AVG' | 'COUNT' | 'AS' | 'MEMBER' | 'CASE' | 'OBJECT' | 'SET' | 'DESC' | 'ASC' | date_part );
	public final JPA2Parser.field_return field() throws RecognitionException {
		JPA2Parser.field_return retval = new JPA2Parser.field_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD613=null;
		Token string_literal614=null;
		Token string_literal615=null;
		Token string_literal616=null;
		Token string_literal617=null;
		Token string_literal618=null;
		Token string_literal619=null;
		Token string_literal620=null;
		Token string_literal621=null;
		Token string_literal622=null;
		Token string_literal623=null;
		Token string_literal624=null;
		Token string_literal625=null;
		Token string_literal626=null;
		Token string_literal627=null;
		Token string_literal628=null;
		Token string_literal629=null;
		ParserRuleReturnScope date_part630 =null;

		Object WORD613_tree=null;
		Object string_literal614_tree=null;
		Object string_literal615_tree=null;
		Object string_literal616_tree=null;
		Object string_literal617_tree=null;
		Object string_literal618_tree=null;
		Object string_literal619_tree=null;
		Object string_literal620_tree=null;
		Object string_literal621_tree=null;
		Object string_literal622_tree=null;
		Object string_literal623_tree=null;
		Object string_literal624_tree=null;
		Object string_literal625_tree=null;
		Object string_literal626_tree=null;
		Object string_literal627_tree=null;
		Object string_literal628_tree=null;
		Object string_literal629_tree=null;

		try {
			// JPA2.g:514:5: ( WORD | 'SELECT' | 'FROM' | 'GROUP' | 'ORDER' | 'MAX' | 'MIN' | 'SUM' | 'AVG' | 'COUNT' | 'AS' | 'MEMBER' | 'CASE' | 'OBJECT' | 'SET' | 'DESC' | 'ASC' | date_part )
			int alt150=18;
			switch ( input.LA(1) ) {
			case WORD:
				{
				alt150=1;
				}
				break;
			case 130:
				{
				alt150=2;
				}
				break;
			case 104:
				{
				alt150=3;
				}
				break;
			case GROUP:
				{
				alt150=4;
				}
				break;
			case ORDER:
				{
				alt150=5;
				}
				break;
			case MAX:
				{
				alt150=6;
				}
				break;
			case MIN:
				{
				alt150=7;
				}
				break;
			case SUM:
				{
				alt150=8;
				}
				break;
			case AVG:
				{
				alt150=9;
				}
				break;
			case COUNT:
				{
				alt150=10;
				}
				break;
			case AS:
				{
				alt150=11;
				}
				break;
			case 114:
				{
				alt150=12;
				}
				break;
			case CASE:
				{
				alt150=13;
				}
				break;
			case 124:
				{
				alt150=14;
				}
				break;
			case SET:
				{
				alt150=15;
				}
				break;
			case DESC:
				{
				alt150=16;
				}
				break;
			case ASC:
				{
				alt150=17;
				}
				break;
			case 96:
			case 100:
			case 106:
			case 115:
			case 117:
			case 127:
			case 129:
			case 143:
			case 145:
				{
				alt150=18;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 150, 0, input);
				throw nvae;
			}
			switch (alt150) {
				case 1 :
					// JPA2.g:514:7: WORD
					{
					root_0 = (Object)adaptor.nil();


					WORD613=(Token)match(input,WORD,FOLLOW_WORD_in_field4817); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					WORD613_tree = (Object)adaptor.create(WORD613);
					adaptor.addChild(root_0, WORD613_tree);
					}

					}
					break;
				case 2 :
					// JPA2.g:514:14: 'SELECT'
					{
					root_0 = (Object)adaptor.nil();


					string_literal614=(Token)match(input,130,FOLLOW_130_in_field4821); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal614_tree = (Object)adaptor.create(string_literal614);
					adaptor.addChild(root_0, string_literal614_tree);
					}

					}
					break;
				case 3 :
					// JPA2.g:514:25: 'FROM'
					{
					root_0 = (Object)adaptor.nil();


					string_literal615=(Token)match(input,104,FOLLOW_104_in_field4825); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal615_tree = (Object)adaptor.create(string_literal615);
					adaptor.addChild(root_0, string_literal615_tree);
					}

					}
					break;
				case 4 :
					// JPA2.g:514:34: 'GROUP'
					{
					root_0 = (Object)adaptor.nil();


					string_literal616=(Token)match(input,GROUP,FOLLOW_GROUP_in_field4829); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal616_tree = (Object)adaptor.create(string_literal616);
					adaptor.addChild(root_0, string_literal616_tree);
					}

					}
					break;
				case 5 :
					// JPA2.g:514:44: 'ORDER'
					{
					root_0 = (Object)adaptor.nil();


					string_literal617=(Token)match(input,ORDER,FOLLOW_ORDER_in_field4833); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal617_tree = (Object)adaptor.create(string_literal617);
					adaptor.addChild(root_0, string_literal617_tree);
					}

					}
					break;
				case 6 :
					// JPA2.g:514:54: 'MAX'
					{
					root_0 = (Object)adaptor.nil();


					string_literal618=(Token)match(input,MAX,FOLLOW_MAX_in_field4837); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal618_tree = (Object)adaptor.create(string_literal618);
					adaptor.addChild(root_0, string_literal618_tree);
					}

					}
					break;
				case 7 :
					// JPA2.g:514:62: 'MIN'
					{
					root_0 = (Object)adaptor.nil();


					string_literal619=(Token)match(input,MIN,FOLLOW_MIN_in_field4841); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal619_tree = (Object)adaptor.create(string_literal619);
					adaptor.addChild(root_0, string_literal619_tree);
					}

					}
					break;
				case 8 :
					// JPA2.g:514:70: 'SUM'
					{
					root_0 = (Object)adaptor.nil();


					string_literal620=(Token)match(input,SUM,FOLLOW_SUM_in_field4845); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal620_tree = (Object)adaptor.create(string_literal620);
					adaptor.addChild(root_0, string_literal620_tree);
					}

					}
					break;
				case 9 :
					// JPA2.g:514:78: 'AVG'
					{
					root_0 = (Object)adaptor.nil();


					string_literal621=(Token)match(input,AVG,FOLLOW_AVG_in_field4849); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal621_tree = (Object)adaptor.create(string_literal621);
					adaptor.addChild(root_0, string_literal621_tree);
					}

					}
					break;
				case 10 :
					// JPA2.g:514:86: 'COUNT'
					{
					root_0 = (Object)adaptor.nil();


					string_literal622=(Token)match(input,COUNT,FOLLOW_COUNT_in_field4853); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal622_tree = (Object)adaptor.create(string_literal622);
					adaptor.addChild(root_0, string_literal622_tree);
					}

					}
					break;
				case 11 :
					// JPA2.g:514:96: 'AS'
					{
					root_0 = (Object)adaptor.nil();


					string_literal623=(Token)match(input,AS,FOLLOW_AS_in_field4857); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal623_tree = (Object)adaptor.create(string_literal623);
					adaptor.addChild(root_0, string_literal623_tree);
					}

					}
					break;
				case 12 :
					// JPA2.g:514:103: 'MEMBER'
					{
					root_0 = (Object)adaptor.nil();


					string_literal624=(Token)match(input,114,FOLLOW_114_in_field4861); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal624_tree = (Object)adaptor.create(string_literal624);
					adaptor.addChild(root_0, string_literal624_tree);
					}

					}
					break;
				case 13 :
					// JPA2.g:514:114: 'CASE'
					{
					root_0 = (Object)adaptor.nil();


					string_literal625=(Token)match(input,CASE,FOLLOW_CASE_in_field4865); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal625_tree = (Object)adaptor.create(string_literal625);
					adaptor.addChild(root_0, string_literal625_tree);
					}

					}
					break;
				case 14 :
					// JPA2.g:515:7: 'OBJECT'
					{
					root_0 = (Object)adaptor.nil();


					string_literal626=(Token)match(input,124,FOLLOW_124_in_field4873); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal626_tree = (Object)adaptor.create(string_literal626);
					adaptor.addChild(root_0, string_literal626_tree);
					}

					}
					break;
				case 15 :
					// JPA2.g:515:18: 'SET'
					{
					root_0 = (Object)adaptor.nil();


					string_literal627=(Token)match(input,SET,FOLLOW_SET_in_field4877); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal627_tree = (Object)adaptor.create(string_literal627);
					adaptor.addChild(root_0, string_literal627_tree);
					}

					}
					break;
				case 16 :
					// JPA2.g:515:26: 'DESC'
					{
					root_0 = (Object)adaptor.nil();


					string_literal628=(Token)match(input,DESC,FOLLOW_DESC_in_field4881); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal628_tree = (Object)adaptor.create(string_literal628);
					adaptor.addChild(root_0, string_literal628_tree);
					}

					}
					break;
				case 17 :
					// JPA2.g:515:35: 'ASC'
					{
					root_0 = (Object)adaptor.nil();


					string_literal629=(Token)match(input,ASC,FOLLOW_ASC_in_field4885); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal629_tree = (Object)adaptor.create(string_literal629);
					adaptor.addChild(root_0, string_literal629_tree);
					}

					}
					break;
				case 18 :
					// JPA2.g:515:43: date_part
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_date_part_in_field4889);
					date_part630=date_part();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, date_part630.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "field"


	public static class identification_variable_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "identification_variable"
	// JPA2.g:517:1: identification_variable : ( WORD | 'GROUP' );
	public final JPA2Parser.identification_variable_return identification_variable() throws RecognitionException {
		JPA2Parser.identification_variable_return retval = new JPA2Parser.identification_variable_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set631=null;

		Object set631_tree=null;

		try {
			// JPA2.g:518:5: ( WORD | 'GROUP' )
			// JPA2.g:
			{
			root_0 = (Object)adaptor.nil();


			set631=input.LT(1);
			if ( input.LA(1)==GROUP||input.LA(1)==WORD ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set631));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "identification_variable"


	public static class parameter_name_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "parameter_name"
	// JPA2.g:520:1: parameter_name : WORD ( '.' WORD )* ;
	public final JPA2Parser.parameter_name_return parameter_name() throws RecognitionException {
		JPA2Parser.parameter_name_return retval = new JPA2Parser.parameter_name_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD632=null;
		Token char_literal633=null;
		Token WORD634=null;

		Object WORD632_tree=null;
		Object char_literal633_tree=null;
		Object WORD634_tree=null;

		try {
			// JPA2.g:521:5: ( WORD ( '.' WORD )* )
			// JPA2.g:521:7: WORD ( '.' WORD )*
			{
			root_0 = (Object)adaptor.nil();


			WORD632=(Token)match(input,WORD,FOLLOW_WORD_in_parameter_name4917); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD632_tree = (Object)adaptor.create(WORD632);
			adaptor.addChild(root_0, WORD632_tree);
			}

			// JPA2.g:521:12: ( '.' WORD )*
			loop151:
			while (true) {
				int alt151=2;
				int LA151_0 = input.LA(1);
				if ( (LA151_0==68) ) {
					alt151=1;
				}

				switch (alt151) {
				case 1 :
					// JPA2.g:521:13: '.' WORD
					{
					char_literal633=(Token)match(input,68,FOLLOW_68_in_parameter_name4920); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal633_tree = (Object)adaptor.create(char_literal633);
					adaptor.addChild(root_0, char_literal633_tree);
					}

					WORD634=(Token)match(input,WORD,FOLLOW_WORD_in_parameter_name4923); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					WORD634_tree = (Object)adaptor.create(WORD634);
					adaptor.addChild(root_0, WORD634_tree);
					}

					}
					break;

				default :
					break loop151;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "parameter_name"


	public static class escape_character_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "escape_character"
	// JPA2.g:524:1: escape_character : ( '\\'.\\'' | STRING_LITERAL );
	public final JPA2Parser.escape_character_return escape_character() throws RecognitionException {
		JPA2Parser.escape_character_return retval = new JPA2Parser.escape_character_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set635=null;

		Object set635_tree=null;

		try {
			// JPA2.g:525:5: ( '\\'.\\'' | STRING_LITERAL )
			// JPA2.g:
			{
			root_0 = (Object)adaptor.nil();


			set635=input.LT(1);
			if ( input.LA(1)==STRING_LITERAL||input.LA(1)==TRIM_CHARACTER ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(set635));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "escape_character"


	public static class trim_character_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "trim_character"
	// JPA2.g:526:1: trim_character : TRIM_CHARACTER ;
	public final JPA2Parser.trim_character_return trim_character() throws RecognitionException {
		JPA2Parser.trim_character_return retval = new JPA2Parser.trim_character_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token TRIM_CHARACTER636=null;

		Object TRIM_CHARACTER636_tree=null;

		try {
			// JPA2.g:527:5: ( TRIM_CHARACTER )
			// JPA2.g:527:7: TRIM_CHARACTER
			{
			root_0 = (Object)adaptor.nil();


			TRIM_CHARACTER636=(Token)match(input,TRIM_CHARACTER,FOLLOW_TRIM_CHARACTER_in_trim_character4953); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			TRIM_CHARACTER636_tree = (Object)adaptor.create(TRIM_CHARACTER636);
			adaptor.addChild(root_0, TRIM_CHARACTER636_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "trim_character"


	public static class string_literal_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "string_literal"
	// JPA2.g:528:1: string_literal : STRING_LITERAL ;
	public final JPA2Parser.string_literal_return string_literal() throws RecognitionException {
		JPA2Parser.string_literal_return retval = new JPA2Parser.string_literal_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token STRING_LITERAL637=null;

		Object STRING_LITERAL637_tree=null;

		try {
			// JPA2.g:529:5: ( STRING_LITERAL )
			// JPA2.g:529:7: STRING_LITERAL
			{
			root_0 = (Object)adaptor.nil();


			STRING_LITERAL637=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_string_literal4964); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			STRING_LITERAL637_tree = (Object)adaptor.create(STRING_LITERAL637);
			adaptor.addChild(root_0, STRING_LITERAL637_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "string_literal"


	public static class numeric_literal_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "numeric_literal"
	// JPA2.g:530:1: numeric_literal : ( '0x' )? INT_NUMERAL ;
	public final JPA2Parser.numeric_literal_return numeric_literal() throws RecognitionException {
		JPA2Parser.numeric_literal_return retval = new JPA2Parser.numeric_literal_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal638=null;
		Token INT_NUMERAL639=null;

		Object string_literal638_tree=null;
		Object INT_NUMERAL639_tree=null;

		try {
			// JPA2.g:531:5: ( ( '0x' )? INT_NUMERAL )
			// JPA2.g:531:7: ( '0x' )? INT_NUMERAL
			{
			root_0 = (Object)adaptor.nil();


			// JPA2.g:531:7: ( '0x' )?
			int alt152=2;
			int LA152_0 = input.LA(1);
			if ( (LA152_0==70) ) {
				alt152=1;
			}
			switch (alt152) {
				case 1 :
					// JPA2.g:531:8: '0x'
					{
					string_literal638=(Token)match(input,70,FOLLOW_70_in_numeric_literal4976); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal638_tree = (Object)adaptor.create(string_literal638);
					adaptor.addChild(root_0, string_literal638_tree);
					}

					}
					break;

			}

			INT_NUMERAL639=(Token)match(input,INT_NUMERAL,FOLLOW_INT_NUMERAL_in_numeric_literal4980); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			INT_NUMERAL639_tree = (Object)adaptor.create(INT_NUMERAL639);
			adaptor.addChild(root_0, INT_NUMERAL639_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "numeric_literal"


	public static class decimal_literal_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "decimal_literal"
	// JPA2.g:532:1: decimal_literal : INT_NUMERAL '.' INT_NUMERAL ;
	public final JPA2Parser.decimal_literal_return decimal_literal() throws RecognitionException {
		JPA2Parser.decimal_literal_return retval = new JPA2Parser.decimal_literal_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token INT_NUMERAL640=null;
		Token char_literal641=null;
		Token INT_NUMERAL642=null;

		Object INT_NUMERAL640_tree=null;
		Object char_literal641_tree=null;
		Object INT_NUMERAL642_tree=null;

		try {
			// JPA2.g:533:5: ( INT_NUMERAL '.' INT_NUMERAL )
			// JPA2.g:533:7: INT_NUMERAL '.' INT_NUMERAL
			{
			root_0 = (Object)adaptor.nil();


			INT_NUMERAL640=(Token)match(input,INT_NUMERAL,FOLLOW_INT_NUMERAL_in_decimal_literal4992); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			INT_NUMERAL640_tree = (Object)adaptor.create(INT_NUMERAL640);
			adaptor.addChild(root_0, INT_NUMERAL640_tree);
			}

			char_literal641=(Token)match(input,68,FOLLOW_68_in_decimal_literal4994); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			char_literal641_tree = (Object)adaptor.create(char_literal641);
			adaptor.addChild(root_0, char_literal641_tree);
			}

			INT_NUMERAL642=(Token)match(input,INT_NUMERAL,FOLLOW_INT_NUMERAL_in_decimal_literal4996); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			INT_NUMERAL642_tree = (Object)adaptor.create(INT_NUMERAL642);
			adaptor.addChild(root_0, INT_NUMERAL642_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "decimal_literal"


	public static class single_valued_object_field_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "single_valued_object_field"
	// JPA2.g:534:1: single_valued_object_field : WORD ;
	public final JPA2Parser.single_valued_object_field_return single_valued_object_field() throws RecognitionException {
		JPA2Parser.single_valued_object_field_return retval = new JPA2Parser.single_valued_object_field_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD643=null;

		Object WORD643_tree=null;

		try {
			// JPA2.g:535:5: ( WORD )
			// JPA2.g:535:7: WORD
			{
			root_0 = (Object)adaptor.nil();


			WORD643=(Token)match(input,WORD,FOLLOW_WORD_in_single_valued_object_field5007); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD643_tree = (Object)adaptor.create(WORD643);
			adaptor.addChild(root_0, WORD643_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "single_valued_object_field"


	public static class single_valued_embeddable_object_field_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "single_valued_embeddable_object_field"
	// JPA2.g:536:1: single_valued_embeddable_object_field : WORD ;
	public final JPA2Parser.single_valued_embeddable_object_field_return single_valued_embeddable_object_field() throws RecognitionException {
		JPA2Parser.single_valued_embeddable_object_field_return retval = new JPA2Parser.single_valued_embeddable_object_field_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD644=null;

		Object WORD644_tree=null;

		try {
			// JPA2.g:537:5: ( WORD )
			// JPA2.g:537:7: WORD
			{
			root_0 = (Object)adaptor.nil();


			WORD644=(Token)match(input,WORD,FOLLOW_WORD_in_single_valued_embeddable_object_field5018); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD644_tree = (Object)adaptor.create(WORD644);
			adaptor.addChild(root_0, WORD644_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "single_valued_embeddable_object_field"


	public static class collection_valued_field_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "collection_valued_field"
	// JPA2.g:538:1: collection_valued_field : WORD ;
	public final JPA2Parser.collection_valued_field_return collection_valued_field() throws RecognitionException {
		JPA2Parser.collection_valued_field_return retval = new JPA2Parser.collection_valued_field_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD645=null;

		Object WORD645_tree=null;

		try {
			// JPA2.g:539:5: ( WORD )
			// JPA2.g:539:7: WORD
			{
			root_0 = (Object)adaptor.nil();


			WORD645=(Token)match(input,WORD,FOLLOW_WORD_in_collection_valued_field5029); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD645_tree = (Object)adaptor.create(WORD645);
			adaptor.addChild(root_0, WORD645_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "collection_valued_field"


	public static class entity_name_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "entity_name"
	// JPA2.g:540:1: entity_name : WORD ;
	public final JPA2Parser.entity_name_return entity_name() throws RecognitionException {
		JPA2Parser.entity_name_return retval = new JPA2Parser.entity_name_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD646=null;

		Object WORD646_tree=null;

		try {
			// JPA2.g:541:5: ( WORD )
			// JPA2.g:541:7: WORD
			{
			root_0 = (Object)adaptor.nil();


			WORD646=(Token)match(input,WORD,FOLLOW_WORD_in_entity_name5040); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD646_tree = (Object)adaptor.create(WORD646);
			adaptor.addChild(root_0, WORD646_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "entity_name"


	public static class subtype_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "subtype"
	// JPA2.g:542:1: subtype : WORD ;
	public final JPA2Parser.subtype_return subtype() throws RecognitionException {
		JPA2Parser.subtype_return retval = new JPA2Parser.subtype_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD647=null;

		Object WORD647_tree=null;

		try {
			// JPA2.g:543:5: ( WORD )
			// JPA2.g:543:7: WORD
			{
			root_0 = (Object)adaptor.nil();


			WORD647=(Token)match(input,WORD,FOLLOW_WORD_in_subtype5051); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD647_tree = (Object)adaptor.create(WORD647);
			adaptor.addChild(root_0, WORD647_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "subtype"


	public static class entity_type_literal_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "entity_type_literal"
	// JPA2.g:544:1: entity_type_literal : WORD ;
	public final JPA2Parser.entity_type_literal_return entity_type_literal() throws RecognitionException {
		JPA2Parser.entity_type_literal_return retval = new JPA2Parser.entity_type_literal_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD648=null;

		Object WORD648_tree=null;

		try {
			// JPA2.g:545:5: ( WORD )
			// JPA2.g:545:7: WORD
			{
			root_0 = (Object)adaptor.nil();


			WORD648=(Token)match(input,WORD,FOLLOW_WORD_in_entity_type_literal5062); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD648_tree = (Object)adaptor.create(WORD648);
			adaptor.addChild(root_0, WORD648_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "entity_type_literal"


	public static class function_name_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "function_name"
	// JPA2.g:546:1: function_name : STRING_LITERAL ;
	public final JPA2Parser.function_name_return function_name() throws RecognitionException {
		JPA2Parser.function_name_return retval = new JPA2Parser.function_name_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token STRING_LITERAL649=null;

		Object STRING_LITERAL649_tree=null;

		try {
			// JPA2.g:547:5: ( STRING_LITERAL )
			// JPA2.g:547:7: STRING_LITERAL
			{
			root_0 = (Object)adaptor.nil();


			STRING_LITERAL649=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_function_name5073); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			STRING_LITERAL649_tree = (Object)adaptor.create(STRING_LITERAL649);
			adaptor.addChild(root_0, STRING_LITERAL649_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "function_name"


	public static class state_field_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "state_field"
	// JPA2.g:548:1: state_field : WORD ;
	public final JPA2Parser.state_field_return state_field() throws RecognitionException {
		JPA2Parser.state_field_return retval = new JPA2Parser.state_field_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD650=null;

		Object WORD650_tree=null;

		try {
			// JPA2.g:549:5: ( WORD )
			// JPA2.g:549:7: WORD
			{
			root_0 = (Object)adaptor.nil();


			WORD650=(Token)match(input,WORD,FOLLOW_WORD_in_state_field5084); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD650_tree = (Object)adaptor.create(WORD650);
			adaptor.addChild(root_0, WORD650_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "state_field"


	public static class result_variable_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "result_variable"
	// JPA2.g:550:1: result_variable : WORD ;
	public final JPA2Parser.result_variable_return result_variable() throws RecognitionException {
		JPA2Parser.result_variable_return retval = new JPA2Parser.result_variable_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD651=null;

		Object WORD651_tree=null;

		try {
			// JPA2.g:551:5: ( WORD )
			// JPA2.g:551:7: WORD
			{
			root_0 = (Object)adaptor.nil();


			WORD651=(Token)match(input,WORD,FOLLOW_WORD_in_result_variable5095); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD651_tree = (Object)adaptor.create(WORD651);
			adaptor.addChild(root_0, WORD651_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "result_variable"


	public static class superquery_identification_variable_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "superquery_identification_variable"
	// JPA2.g:552:1: superquery_identification_variable : WORD ;
	public final JPA2Parser.superquery_identification_variable_return superquery_identification_variable() throws RecognitionException {
		JPA2Parser.superquery_identification_variable_return retval = new JPA2Parser.superquery_identification_variable_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD652=null;

		Object WORD652_tree=null;

		try {
			// JPA2.g:553:5: ( WORD )
			// JPA2.g:553:7: WORD
			{
			root_0 = (Object)adaptor.nil();


			WORD652=(Token)match(input,WORD,FOLLOW_WORD_in_superquery_identification_variable5106); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD652_tree = (Object)adaptor.create(WORD652);
			adaptor.addChild(root_0, WORD652_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "superquery_identification_variable"


	public static class date_time_timestamp_literal_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "date_time_timestamp_literal"
	// JPA2.g:554:1: date_time_timestamp_literal : WORD ;
	public final JPA2Parser.date_time_timestamp_literal_return date_time_timestamp_literal() throws RecognitionException {
		JPA2Parser.date_time_timestamp_literal_return retval = new JPA2Parser.date_time_timestamp_literal_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD653=null;

		Object WORD653_tree=null;

		try {
			// JPA2.g:555:5: ( WORD )
			// JPA2.g:555:7: WORD
			{
			root_0 = (Object)adaptor.nil();


			WORD653=(Token)match(input,WORD,FOLLOW_WORD_in_date_time_timestamp_literal5117); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD653_tree = (Object)adaptor.create(WORD653);
			adaptor.addChild(root_0, WORD653_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "date_time_timestamp_literal"


	public static class pattern_value_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "pattern_value"
	// JPA2.g:556:1: pattern_value : string_literal ;
	public final JPA2Parser.pattern_value_return pattern_value() throws RecognitionException {
		JPA2Parser.pattern_value_return retval = new JPA2Parser.pattern_value_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope string_literal654 =null;


		try {
			// JPA2.g:557:5: ( string_literal )
			// JPA2.g:557:7: string_literal
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_string_literal_in_pattern_value5128);
			string_literal654=string_literal();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, string_literal654.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "pattern_value"


	public static class collection_valued_input_parameter_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "collection_valued_input_parameter"
	// JPA2.g:558:1: collection_valued_input_parameter : input_parameter ;
	public final JPA2Parser.collection_valued_input_parameter_return collection_valued_input_parameter() throws RecognitionException {
		JPA2Parser.collection_valued_input_parameter_return retval = new JPA2Parser.collection_valued_input_parameter_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope input_parameter655 =null;


		try {
			// JPA2.g:559:5: ( input_parameter )
			// JPA2.g:559:7: input_parameter
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_input_parameter_in_collection_valued_input_parameter5139);
			input_parameter655=input_parameter();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter655.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "collection_valued_input_parameter"


	public static class single_valued_input_parameter_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "single_valued_input_parameter"
	// JPA2.g:560:1: single_valued_input_parameter : input_parameter ;
	public final JPA2Parser.single_valued_input_parameter_return single_valued_input_parameter() throws RecognitionException {
		JPA2Parser.single_valued_input_parameter_return retval = new JPA2Parser.single_valued_input_parameter_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope input_parameter656 =null;


		try {
			// JPA2.g:561:5: ( input_parameter )
			// JPA2.g:561:7: input_parameter
			{
			root_0 = (Object)adaptor.nil();


			pushFollow(FOLLOW_input_parameter_in_single_valued_input_parameter5150);
			input_parameter656=input_parameter();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, input_parameter656.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "single_valued_input_parameter"


	public static class enum_value_field_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "enum_value_field"
	// JPA2.g:562:1: enum_value_field : ( WORD | 'SELECT' | 'FROM' | 'GROUP' | 'ORDER' | 'MAX' | 'MIN' | 'SUM' | 'AVG' | 'COUNT' | 'AS' | 'MEMBER' | 'CASE' | 'OBJECT' | 'SET' | 'DESC' | 'ASC' | 'NEW' | date_part );
	public final JPA2Parser.enum_value_field_return enum_value_field() throws RecognitionException {
		JPA2Parser.enum_value_field_return retval = new JPA2Parser.enum_value_field_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD657=null;
		Token string_literal658=null;
		Token string_literal659=null;
		Token string_literal660=null;
		Token string_literal661=null;
		Token string_literal662=null;
		Token string_literal663=null;
		Token string_literal664=null;
		Token string_literal665=null;
		Token string_literal666=null;
		Token string_literal667=null;
		Token string_literal668=null;
		Token string_literal669=null;
		Token string_literal670=null;
		Token string_literal671=null;
		Token string_literal672=null;
		Token string_literal673=null;
		Token string_literal674=null;
		ParserRuleReturnScope date_part675 =null;

		Object WORD657_tree=null;
		Object string_literal658_tree=null;
		Object string_literal659_tree=null;
		Object string_literal660_tree=null;
		Object string_literal661_tree=null;
		Object string_literal662_tree=null;
		Object string_literal663_tree=null;
		Object string_literal664_tree=null;
		Object string_literal665_tree=null;
		Object string_literal666_tree=null;
		Object string_literal667_tree=null;
		Object string_literal668_tree=null;
		Object string_literal669_tree=null;
		Object string_literal670_tree=null;
		Object string_literal671_tree=null;
		Object string_literal672_tree=null;
		Object string_literal673_tree=null;
		Object string_literal674_tree=null;

		try {
			// JPA2.g:563:5: ( WORD | 'SELECT' | 'FROM' | 'GROUP' | 'ORDER' | 'MAX' | 'MIN' | 'SUM' | 'AVG' | 'COUNT' | 'AS' | 'MEMBER' | 'CASE' | 'OBJECT' | 'SET' | 'DESC' | 'ASC' | 'NEW' | date_part )
			int alt153=19;
			switch ( input.LA(1) ) {
			case WORD:
				{
				alt153=1;
				}
				break;
			case 130:
				{
				alt153=2;
				}
				break;
			case 104:
				{
				alt153=3;
				}
				break;
			case GROUP:
				{
				alt153=4;
				}
				break;
			case ORDER:
				{
				alt153=5;
				}
				break;
			case MAX:
				{
				alt153=6;
				}
				break;
			case MIN:
				{
				alt153=7;
				}
				break;
			case SUM:
				{
				alt153=8;
				}
				break;
			case AVG:
				{
				alt153=9;
				}
				break;
			case COUNT:
				{
				alt153=10;
				}
				break;
			case AS:
				{
				alt153=11;
				}
				break;
			case 114:
				{
				alt153=12;
				}
				break;
			case CASE:
				{
				alt153=13;
				}
				break;
			case 124:
				{
				alt153=14;
				}
				break;
			case SET:
				{
				alt153=15;
				}
				break;
			case DESC:
				{
				alt153=16;
				}
				break;
			case ASC:
				{
				alt153=17;
				}
				break;
			case 118:
				{
				alt153=18;
				}
				break;
			case 96:
			case 100:
			case 106:
			case 115:
			case 117:
			case 127:
			case 129:
			case 143:
			case 145:
				{
				alt153=19;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 153, 0, input);
				throw nvae;
			}
			switch (alt153) {
				case 1 :
					// JPA2.g:563:7: WORD
					{
					root_0 = (Object)adaptor.nil();


					WORD657=(Token)match(input,WORD,FOLLOW_WORD_in_enum_value_field5161); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					WORD657_tree = (Object)adaptor.create(WORD657);
					adaptor.addChild(root_0, WORD657_tree);
					}

					}
					break;
				case 2 :
					// JPA2.g:563:14: 'SELECT'
					{
					root_0 = (Object)adaptor.nil();


					string_literal658=(Token)match(input,130,FOLLOW_130_in_enum_value_field5165); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal658_tree = (Object)adaptor.create(string_literal658);
					adaptor.addChild(root_0, string_literal658_tree);
					}

					}
					break;
				case 3 :
					// JPA2.g:563:25: 'FROM'
					{
					root_0 = (Object)adaptor.nil();


					string_literal659=(Token)match(input,104,FOLLOW_104_in_enum_value_field5169); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal659_tree = (Object)adaptor.create(string_literal659);
					adaptor.addChild(root_0, string_literal659_tree);
					}

					}
					break;
				case 4 :
					// JPA2.g:563:34: 'GROUP'
					{
					root_0 = (Object)adaptor.nil();


					string_literal660=(Token)match(input,GROUP,FOLLOW_GROUP_in_enum_value_field5173); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal660_tree = (Object)adaptor.create(string_literal660);
					adaptor.addChild(root_0, string_literal660_tree);
					}

					}
					break;
				case 5 :
					// JPA2.g:563:44: 'ORDER'
					{
					root_0 = (Object)adaptor.nil();


					string_literal661=(Token)match(input,ORDER,FOLLOW_ORDER_in_enum_value_field5177); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal661_tree = (Object)adaptor.create(string_literal661);
					adaptor.addChild(root_0, string_literal661_tree);
					}

					}
					break;
				case 6 :
					// JPA2.g:563:54: 'MAX'
					{
					root_0 = (Object)adaptor.nil();


					string_literal662=(Token)match(input,MAX,FOLLOW_MAX_in_enum_value_field5181); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal662_tree = (Object)adaptor.create(string_literal662);
					adaptor.addChild(root_0, string_literal662_tree);
					}

					}
					break;
				case 7 :
					// JPA2.g:563:62: 'MIN'
					{
					root_0 = (Object)adaptor.nil();


					string_literal663=(Token)match(input,MIN,FOLLOW_MIN_in_enum_value_field5185); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal663_tree = (Object)adaptor.create(string_literal663);
					adaptor.addChild(root_0, string_literal663_tree);
					}

					}
					break;
				case 8 :
					// JPA2.g:563:70: 'SUM'
					{
					root_0 = (Object)adaptor.nil();


					string_literal664=(Token)match(input,SUM,FOLLOW_SUM_in_enum_value_field5189); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal664_tree = (Object)adaptor.create(string_literal664);
					adaptor.addChild(root_0, string_literal664_tree);
					}

					}
					break;
				case 9 :
					// JPA2.g:563:78: 'AVG'
					{
					root_0 = (Object)adaptor.nil();


					string_literal665=(Token)match(input,AVG,FOLLOW_AVG_in_enum_value_field5193); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal665_tree = (Object)adaptor.create(string_literal665);
					adaptor.addChild(root_0, string_literal665_tree);
					}

					}
					break;
				case 10 :
					// JPA2.g:563:86: 'COUNT'
					{
					root_0 = (Object)adaptor.nil();


					string_literal666=(Token)match(input,COUNT,FOLLOW_COUNT_in_enum_value_field5197); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal666_tree = (Object)adaptor.create(string_literal666);
					adaptor.addChild(root_0, string_literal666_tree);
					}

					}
					break;
				case 11 :
					// JPA2.g:563:96: 'AS'
					{
					root_0 = (Object)adaptor.nil();


					string_literal667=(Token)match(input,AS,FOLLOW_AS_in_enum_value_field5201); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal667_tree = (Object)adaptor.create(string_literal667);
					adaptor.addChild(root_0, string_literal667_tree);
					}

					}
					break;
				case 12 :
					// JPA2.g:563:103: 'MEMBER'
					{
					root_0 = (Object)adaptor.nil();


					string_literal668=(Token)match(input,114,FOLLOW_114_in_enum_value_field5205); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal668_tree = (Object)adaptor.create(string_literal668);
					adaptor.addChild(root_0, string_literal668_tree);
					}

					}
					break;
				case 13 :
					// JPA2.g:563:114: 'CASE'
					{
					root_0 = (Object)adaptor.nil();


					string_literal669=(Token)match(input,CASE,FOLLOW_CASE_in_enum_value_field5209); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal669_tree = (Object)adaptor.create(string_literal669);
					adaptor.addChild(root_0, string_literal669_tree);
					}

					}
					break;
				case 14 :
					// JPA2.g:564:7: 'OBJECT'
					{
					root_0 = (Object)adaptor.nil();


					string_literal670=(Token)match(input,124,FOLLOW_124_in_enum_value_field5217); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal670_tree = (Object)adaptor.create(string_literal670);
					adaptor.addChild(root_0, string_literal670_tree);
					}

					}
					break;
				case 15 :
					// JPA2.g:564:18: 'SET'
					{
					root_0 = (Object)adaptor.nil();


					string_literal671=(Token)match(input,SET,FOLLOW_SET_in_enum_value_field5221); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal671_tree = (Object)adaptor.create(string_literal671);
					adaptor.addChild(root_0, string_literal671_tree);
					}

					}
					break;
				case 16 :
					// JPA2.g:564:26: 'DESC'
					{
					root_0 = (Object)adaptor.nil();


					string_literal672=(Token)match(input,DESC,FOLLOW_DESC_in_enum_value_field5225); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal672_tree = (Object)adaptor.create(string_literal672);
					adaptor.addChild(root_0, string_literal672_tree);
					}

					}
					break;
				case 17 :
					// JPA2.g:564:35: 'ASC'
					{
					root_0 = (Object)adaptor.nil();


					string_literal673=(Token)match(input,ASC,FOLLOW_ASC_in_enum_value_field5229); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal673_tree = (Object)adaptor.create(string_literal673);
					adaptor.addChild(root_0, string_literal673_tree);
					}

					}
					break;
				case 18 :
					// JPA2.g:564:43: 'NEW'
					{
					root_0 = (Object)adaptor.nil();


					string_literal674=(Token)match(input,118,FOLLOW_118_in_enum_value_field5233); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					string_literal674_tree = (Object)adaptor.create(string_literal674);
					adaptor.addChild(root_0, string_literal674_tree);
					}

					}
					break;
				case 19 :
					// JPA2.g:564:51: date_part
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_date_part_in_enum_value_field5237);
					date_part675=date_part();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, date_part675.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enum_value_field"


	public static class enum_value_literal_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "enum_value_literal"
	// JPA2.g:565:1: enum_value_literal : WORD ( '.' enum_value_field )* ;
	public final JPA2Parser.enum_value_literal_return enum_value_literal() throws RecognitionException {
		JPA2Parser.enum_value_literal_return retval = new JPA2Parser.enum_value_literal_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token WORD676=null;
		Token char_literal677=null;
		ParserRuleReturnScope enum_value_field678 =null;

		Object WORD676_tree=null;
		Object char_literal677_tree=null;

		try {
			// JPA2.g:566:5: ( WORD ( '.' enum_value_field )* )
			// JPA2.g:566:7: WORD ( '.' enum_value_field )*
			{
			root_0 = (Object)adaptor.nil();


			WORD676=(Token)match(input,WORD,FOLLOW_WORD_in_enum_value_literal5248); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			WORD676_tree = (Object)adaptor.create(WORD676);
			adaptor.addChild(root_0, WORD676_tree);
			}

			// JPA2.g:566:12: ( '.' enum_value_field )*
			loop154:
			while (true) {
				int alt154=2;
				int LA154_0 = input.LA(1);
				if ( (LA154_0==68) ) {
					alt154=1;
				}

				switch (alt154) {
				case 1 :
					// JPA2.g:566:13: '.' enum_value_field
					{
					char_literal677=(Token)match(input,68,FOLLOW_68_in_enum_value_literal5251); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					char_literal677_tree = (Object)adaptor.create(char_literal677);
					adaptor.addChild(root_0, char_literal677_tree);
					}

					pushFollow(FOLLOW_enum_value_field_in_enum_value_literal5254);
					enum_value_field678=enum_value_field();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, enum_value_field678.getTree());

					}
					break;

				default :
					break loop154;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enum_value_literal"

	// $ANTLR start synpred21_JPA2
	public final void synpred21_JPA2_fragment() throws RecognitionException {
		// JPA2.g:128:48: ( field )
		// JPA2.g:128:48: field
		{
		pushFollow(FOLLOW_field_in_synpred21_JPA2972);
		field();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred21_JPA2

	// $ANTLR start synpred30_JPA2
	public final void synpred30_JPA2_fragment() throws RecognitionException {
		// JPA2.g:146:48: ( field )
		// JPA2.g:146:48: field
		{
		pushFollow(FOLLOW_field_in_synpred30_JPA21162);
		field();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred30_JPA2

	// $ANTLR start synpred33_JPA2
	public final void synpred33_JPA2_fragment() throws RecognitionException {
		// JPA2.g:163:7: ( scalar_expression )
		// JPA2.g:163:7: scalar_expression
		{
		pushFollow(FOLLOW_scalar_expression_in_synpred33_JPA21288);
		scalar_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred33_JPA2

	// $ANTLR start synpred34_JPA2
	public final void synpred34_JPA2_fragment() throws RecognitionException {
		// JPA2.g:164:7: ( simple_entity_expression )
		// JPA2.g:164:7: simple_entity_expression
		{
		pushFollow(FOLLOW_simple_entity_expression_in_synpred34_JPA21296);
		simple_entity_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred34_JPA2

	// $ANTLR start synpred43_JPA2
	public final void synpred43_JPA2_fragment() throws RecognitionException {
		// JPA2.g:176:7: ( path_expression ( ( '+' | '-' | '*' | '/' ) scalar_expression )? )
		// JPA2.g:176:7: path_expression ( ( '+' | '-' | '*' | '/' ) scalar_expression )?
		{
		pushFollow(FOLLOW_path_expression_in_synpred43_JPA21421);
		path_expression();
		state._fsp--;
		if (state.failed) return;

		// JPA2.g:176:23: ( ( '+' | '-' | '*' | '/' ) scalar_expression )?
		int alt161=2;
		int LA161_0 = input.LA(1);
		if ( ((LA161_0 >= 64 && LA161_0 <= 65)||LA161_0==67||LA161_0==69) ) {
			alt161=1;
		}
		switch (alt161) {
			case 1 :
				// JPA2.g:176:24: ( '+' | '-' | '*' | '/' ) scalar_expression
				{
				if ( (input.LA(1) >= 64 && input.LA(1) <= 65)||input.LA(1)==67||input.LA(1)==69 ) {
					input.consume();
					state.errorRecovery=false;
					state.failed=false;
				}
				else {
					if (state.backtracking>0) {state.failed=true; return;}
					MismatchedSetException mse = new MismatchedSetException(null,input);
					throw mse;
				}
				pushFollow(FOLLOW_scalar_expression_in_synpred43_JPA21440);
				scalar_expression();
				state._fsp--;
				if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred43_JPA2

	// $ANTLR start synpred44_JPA2
	public final void synpred44_JPA2_fragment() throws RecognitionException {
		// JPA2.g:177:7: ( identification_variable )
		// JPA2.g:177:7: identification_variable
		{
		pushFollow(FOLLOW_identification_variable_in_synpred44_JPA21450);
		identification_variable();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred44_JPA2

	// $ANTLR start synpred45_JPA2
	public final void synpred45_JPA2_fragment() throws RecognitionException {
		// JPA2.g:178:7: ( scalar_expression )
		// JPA2.g:178:7: scalar_expression
		{
		pushFollow(FOLLOW_scalar_expression_in_synpred45_JPA21468);
		scalar_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred45_JPA2

	// $ANTLR start synpred46_JPA2
	public final void synpred46_JPA2_fragment() throws RecognitionException {
		// JPA2.g:179:7: ( aggregate_expression )
		// JPA2.g:179:7: aggregate_expression
		{
		pushFollow(FOLLOW_aggregate_expression_in_synpred46_JPA21476);
		aggregate_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred46_JPA2

	// $ANTLR start synpred49_JPA2
	public final void synpred49_JPA2_fragment() throws RecognitionException {
		// JPA2.g:185:7: ( path_expression )
		// JPA2.g:185:7: path_expression
		{
		pushFollow(FOLLOW_path_expression_in_synpred49_JPA21533);
		path_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred49_JPA2

	// $ANTLR start synpred50_JPA2
	public final void synpred50_JPA2_fragment() throws RecognitionException {
		// JPA2.g:186:7: ( scalar_expression )
		// JPA2.g:186:7: scalar_expression
		{
		pushFollow(FOLLOW_scalar_expression_in_synpred50_JPA21541);
		scalar_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred50_JPA2

	// $ANTLR start synpred51_JPA2
	public final void synpred51_JPA2_fragment() throws RecognitionException {
		// JPA2.g:187:7: ( aggregate_expression )
		// JPA2.g:187:7: aggregate_expression
		{
		pushFollow(FOLLOW_aggregate_expression_in_synpred51_JPA21549);
		aggregate_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred51_JPA2

	// $ANTLR start synpred53_JPA2
	public final void synpred53_JPA2_fragment() throws RecognitionException {
		// JPA2.g:190:7: ( aggregate_expression_function_name '(' ( DISTINCT )? arithmetic_expression ')' )
		// JPA2.g:190:7: aggregate_expression_function_name '(' ( DISTINCT )? arithmetic_expression ')'
		{
		pushFollow(FOLLOW_aggregate_expression_function_name_in_synpred53_JPA21568);
		aggregate_expression_function_name();
		state._fsp--;
		if (state.failed) return;

		match(input,LPAREN,FOLLOW_LPAREN_in_synpred53_JPA21570); if (state.failed) return;

		// JPA2.g:190:45: ( DISTINCT )?
		int alt162=2;
		int LA162_0 = input.LA(1);
		if ( (LA162_0==DISTINCT) ) {
			alt162=1;
		}
		switch (alt162) {
			case 1 :
				// JPA2.g:190:46: DISTINCT
				{
				match(input,DISTINCT,FOLLOW_DISTINCT_in_synpred53_JPA21572); if (state.failed) return;

				}
				break;

		}

		pushFollow(FOLLOW_arithmetic_expression_in_synpred53_JPA21576);
		arithmetic_expression();
		state._fsp--;
		if (state.failed) return;

		match(input,RPAREN,FOLLOW_RPAREN_in_synpred53_JPA21577); if (state.failed) return;

		}

	}
	// $ANTLR end synpred53_JPA2

	// $ANTLR start synpred55_JPA2
	public final void synpred55_JPA2_fragment() throws RecognitionException {
		// JPA2.g:192:7: ( 'COUNT' '(' ( DISTINCT )? count_argument ')' )
		// JPA2.g:192:7: 'COUNT' '(' ( DISTINCT )? count_argument ')'
		{
		match(input,COUNT,FOLLOW_COUNT_in_synpred55_JPA21611); if (state.failed) return;

		match(input,LPAREN,FOLLOW_LPAREN_in_synpred55_JPA21613); if (state.failed) return;

		// JPA2.g:192:18: ( DISTINCT )?
		int alt163=2;
		int LA163_0 = input.LA(1);
		if ( (LA163_0==DISTINCT) ) {
			alt163=1;
		}
		switch (alt163) {
			case 1 :
				// JPA2.g:192:19: DISTINCT
				{
				match(input,DISTINCT,FOLLOW_DISTINCT_in_synpred55_JPA21615); if (state.failed) return;

				}
				break;

		}

		pushFollow(FOLLOW_count_argument_in_synpred55_JPA21619);
		count_argument();
		state._fsp--;
		if (state.failed) return;

		match(input,RPAREN,FOLLOW_RPAREN_in_synpred55_JPA21621); if (state.failed) return;

		}

	}
	// $ANTLR end synpred55_JPA2

	// $ANTLR start synpred67_JPA2
	public final void synpred67_JPA2_fragment() throws RecognitionException {
		// JPA2.g:215:7: ( path_expression )
		// JPA2.g:215:7: path_expression
		{
		pushFollow(FOLLOW_path_expression_in_synpred67_JPA21892);
		path_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred67_JPA2

	// $ANTLR start synpred68_JPA2
	public final void synpred68_JPA2_fragment() throws RecognitionException {
		// JPA2.g:215:25: ( general_identification_variable )
		// JPA2.g:215:25: general_identification_variable
		{
		pushFollow(FOLLOW_general_identification_variable_in_synpred68_JPA21896);
		general_identification_variable();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred68_JPA2

	// $ANTLR start synpred69_JPA2
	public final void synpred69_JPA2_fragment() throws RecognitionException {
		// JPA2.g:215:59: ( result_variable )
		// JPA2.g:215:59: result_variable
		{
		pushFollow(FOLLOW_result_variable_in_synpred69_JPA21900);
		result_variable();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred69_JPA2

	// $ANTLR start synpred70_JPA2
	public final void synpred70_JPA2_fragment() throws RecognitionException {
		// JPA2.g:215:77: ( scalar_expression )
		// JPA2.g:215:77: scalar_expression
		{
		pushFollow(FOLLOW_scalar_expression_in_synpred70_JPA21904);
		scalar_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred70_JPA2

	// $ANTLR start synpred81_JPA2
	public final void synpred81_JPA2_fragment() throws RecognitionException {
		// JPA2.g:232:7: ( general_derived_path '.' single_valued_object_field )
		// JPA2.g:232:7: general_derived_path '.' single_valued_object_field
		{
		pushFollow(FOLLOW_general_derived_path_in_synpred81_JPA22114);
		general_derived_path();
		state._fsp--;
		if (state.failed) return;

		match(input,68,FOLLOW_68_in_synpred81_JPA22115); if (state.failed) return;

		pushFollow(FOLLOW_single_valued_object_field_in_synpred81_JPA22116);
		single_valued_object_field();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred81_JPA2

	// $ANTLR start synpred86_JPA2
	public final void synpred86_JPA2_fragment() throws RecognitionException {
		// JPA2.g:250:7: ( path_expression )
		// JPA2.g:250:7: path_expression
		{
		pushFollow(FOLLOW_path_expression_in_synpred86_JPA22268);
		path_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred86_JPA2

	// $ANTLR start synpred87_JPA2
	public final void synpred87_JPA2_fragment() throws RecognitionException {
		// JPA2.g:251:7: ( scalar_expression )
		// JPA2.g:251:7: scalar_expression
		{
		pushFollow(FOLLOW_scalar_expression_in_synpred87_JPA22276);
		scalar_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred87_JPA2

	// $ANTLR start synpred88_JPA2
	public final void synpred88_JPA2_fragment() throws RecognitionException {
		// JPA2.g:252:7: ( aggregate_expression )
		// JPA2.g:252:7: aggregate_expression
		{
		pushFollow(FOLLOW_aggregate_expression_in_synpred88_JPA22284);
		aggregate_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred88_JPA2

	// $ANTLR start synpred89_JPA2
	public final void synpred89_JPA2_fragment() throws RecognitionException {
		// JPA2.g:255:7: ( arithmetic_expression )
		// JPA2.g:255:7: arithmetic_expression
		{
		pushFollow(FOLLOW_arithmetic_expression_in_synpred89_JPA22303);
		arithmetic_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred89_JPA2

	// $ANTLR start synpred90_JPA2
	public final void synpred90_JPA2_fragment() throws RecognitionException {
		// JPA2.g:256:7: ( string_expression )
		// JPA2.g:256:7: string_expression
		{
		pushFollow(FOLLOW_string_expression_in_synpred90_JPA22311);
		string_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred90_JPA2

	// $ANTLR start synpred91_JPA2
	public final void synpred91_JPA2_fragment() throws RecognitionException {
		// JPA2.g:257:7: ( enum_expression )
		// JPA2.g:257:7: enum_expression
		{
		pushFollow(FOLLOW_enum_expression_in_synpred91_JPA22319);
		enum_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred91_JPA2

	// $ANTLR start synpred92_JPA2
	public final void synpred92_JPA2_fragment() throws RecognitionException {
		// JPA2.g:258:7: ( datetime_expression )
		// JPA2.g:258:7: datetime_expression
		{
		pushFollow(FOLLOW_datetime_expression_in_synpred92_JPA22327);
		datetime_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred92_JPA2

	// $ANTLR start synpred93_JPA2
	public final void synpred93_JPA2_fragment() throws RecognitionException {
		// JPA2.g:259:7: ( boolean_expression )
		// JPA2.g:259:7: boolean_expression
		{
		pushFollow(FOLLOW_boolean_expression_in_synpred93_JPA22335);
		boolean_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred93_JPA2

	// $ANTLR start synpred94_JPA2
	public final void synpred94_JPA2_fragment() throws RecognitionException {
		// JPA2.g:260:7: ( case_expression )
		// JPA2.g:260:7: case_expression
		{
		pushFollow(FOLLOW_case_expression_in_synpred94_JPA22343);
		case_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred94_JPA2

	// $ANTLR start synpred97_JPA2
	public final void synpred97_JPA2_fragment() throws RecognitionException {
		// JPA2.g:267:8: ( 'NOT' )
		// JPA2.g:267:8: 'NOT'
		{
		match(input,NOT,FOLLOW_NOT_in_synpred97_JPA22403); if (state.failed) return;

		}

	}
	// $ANTLR end synpred97_JPA2

	// $ANTLR start synpred98_JPA2
	public final void synpred98_JPA2_fragment() throws RecognitionException {
		// JPA2.g:269:7: ( simple_cond_expression )
		// JPA2.g:269:7: simple_cond_expression
		{
		pushFollow(FOLLOW_simple_cond_expression_in_synpred98_JPA22418);
		simple_cond_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred98_JPA2

	// $ANTLR start synpred99_JPA2
	public final void synpred99_JPA2_fragment() throws RecognitionException {
		// JPA2.g:273:7: ( comparison_expression )
		// JPA2.g:273:7: comparison_expression
		{
		pushFollow(FOLLOW_comparison_expression_in_synpred99_JPA22455);
		comparison_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred99_JPA2

	// $ANTLR start synpred100_JPA2
	public final void synpred100_JPA2_fragment() throws RecognitionException {
		// JPA2.g:274:7: ( between_expression )
		// JPA2.g:274:7: between_expression
		{
		pushFollow(FOLLOW_between_expression_in_synpred100_JPA22463);
		between_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred100_JPA2

	// $ANTLR start synpred101_JPA2
	public final void synpred101_JPA2_fragment() throws RecognitionException {
		// JPA2.g:275:7: ( in_expression )
		// JPA2.g:275:7: in_expression
		{
		pushFollow(FOLLOW_in_expression_in_synpred101_JPA22471);
		in_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred101_JPA2

	// $ANTLR start synpred102_JPA2
	public final void synpred102_JPA2_fragment() throws RecognitionException {
		// JPA2.g:276:7: ( like_expression )
		// JPA2.g:276:7: like_expression
		{
		pushFollow(FOLLOW_like_expression_in_synpred102_JPA22479);
		like_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred102_JPA2

	// $ANTLR start synpred103_JPA2
	public final void synpred103_JPA2_fragment() throws RecognitionException {
		// JPA2.g:277:7: ( null_comparison_expression )
		// JPA2.g:277:7: null_comparison_expression
		{
		pushFollow(FOLLOW_null_comparison_expression_in_synpred103_JPA22487);
		null_comparison_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred103_JPA2

	// $ANTLR start synpred104_JPA2
	public final void synpred104_JPA2_fragment() throws RecognitionException {
		// JPA2.g:278:7: ( empty_collection_comparison_expression )
		// JPA2.g:278:7: empty_collection_comparison_expression
		{
		pushFollow(FOLLOW_empty_collection_comparison_expression_in_synpred104_JPA22495);
		empty_collection_comparison_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred104_JPA2

	// $ANTLR start synpred105_JPA2
	public final void synpred105_JPA2_fragment() throws RecognitionException {
		// JPA2.g:279:7: ( collection_member_expression )
		// JPA2.g:279:7: collection_member_expression
		{
		pushFollow(FOLLOW_collection_member_expression_in_synpred105_JPA22503);
		collection_member_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred105_JPA2

	// $ANTLR start synpred144_JPA2
	public final void synpred144_JPA2_fragment() throws RecognitionException {
		// JPA2.g:313:7: ( arithmetic_expression ( 'NOT' )? 'BETWEEN' arithmetic_expression 'AND' arithmetic_expression )
		// JPA2.g:313:7: arithmetic_expression ( 'NOT' )? 'BETWEEN' arithmetic_expression 'AND' arithmetic_expression
		{
		pushFollow(FOLLOW_arithmetic_expression_in_synpred144_JPA22907);
		arithmetic_expression();
		state._fsp--;
		if (state.failed) return;

		// JPA2.g:313:29: ( 'NOT' )?
		int alt166=2;
		int LA166_0 = input.LA(1);
		if ( (LA166_0==NOT) ) {
			alt166=1;
		}
		switch (alt166) {
			case 1 :
				// JPA2.g:313:30: 'NOT'
				{
				match(input,NOT,FOLLOW_NOT_in_synpred144_JPA22910); if (state.failed) return;

				}
				break;

		}

		match(input,88,FOLLOW_88_in_synpred144_JPA22914); if (state.failed) return;

		pushFollow(FOLLOW_arithmetic_expression_in_synpred144_JPA22916);
		arithmetic_expression();
		state._fsp--;
		if (state.failed) return;

		match(input,AND,FOLLOW_AND_in_synpred144_JPA22918); if (state.failed) return;

		pushFollow(FOLLOW_arithmetic_expression_in_synpred144_JPA22920);
		arithmetic_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred144_JPA2

	// $ANTLR start synpred146_JPA2
	public final void synpred146_JPA2_fragment() throws RecognitionException {
		// JPA2.g:314:7: ( string_expression ( 'NOT' )? 'BETWEEN' string_expression 'AND' string_expression )
		// JPA2.g:314:7: string_expression ( 'NOT' )? 'BETWEEN' string_expression 'AND' string_expression
		{
		pushFollow(FOLLOW_string_expression_in_synpred146_JPA22928);
		string_expression();
		state._fsp--;
		if (state.failed) return;

		// JPA2.g:314:25: ( 'NOT' )?
		int alt167=2;
		int LA167_0 = input.LA(1);
		if ( (LA167_0==NOT) ) {
			alt167=1;
		}
		switch (alt167) {
			case 1 :
				// JPA2.g:314:26: 'NOT'
				{
				match(input,NOT,FOLLOW_NOT_in_synpred146_JPA22931); if (state.failed) return;

				}
				break;

		}

		match(input,88,FOLLOW_88_in_synpred146_JPA22935); if (state.failed) return;

		pushFollow(FOLLOW_string_expression_in_synpred146_JPA22937);
		string_expression();
		state._fsp--;
		if (state.failed) return;

		match(input,AND,FOLLOW_AND_in_synpred146_JPA22939); if (state.failed) return;

		pushFollow(FOLLOW_string_expression_in_synpred146_JPA22941);
		string_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred146_JPA2

	// $ANTLR start synpred160_JPA2
	public final void synpred160_JPA2_fragment() throws RecognitionException {
		// JPA2.g:330:42: ( string_expression )
		// JPA2.g:330:42: string_expression
		{
		pushFollow(FOLLOW_string_expression_in_synpred160_JPA23134);
		string_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred160_JPA2

	// $ANTLR start synpred161_JPA2
	public final void synpred161_JPA2_fragment() throws RecognitionException {
		// JPA2.g:330:62: ( pattern_value )
		// JPA2.g:330:62: pattern_value
		{
		pushFollow(FOLLOW_pattern_value_in_synpred161_JPA23138);
		pattern_value();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred161_JPA2

	// $ANTLR start synpred163_JPA2
	public final void synpred163_JPA2_fragment() throws RecognitionException {
		// JPA2.g:332:8: ( path_expression )
		// JPA2.g:332:8: path_expression
		{
		pushFollow(FOLLOW_path_expression_in_synpred163_JPA23161);
		path_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred163_JPA2

	// $ANTLR start synpred171_JPA2
	public final void synpred171_JPA2_fragment() throws RecognitionException {
		// JPA2.g:342:7: ( identification_variable )
		// JPA2.g:342:7: identification_variable
		{
		pushFollow(FOLLOW_identification_variable_in_synpred171_JPA23263);
		identification_variable();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred171_JPA2

	// $ANTLR start synpred178_JPA2
	public final void synpred178_JPA2_fragment() throws RecognitionException {
		// JPA2.g:350:7: ( string_expression ( comparison_operator | 'REGEXP' ) ( string_expression | all_or_any_expression ) )
		// JPA2.g:350:7: string_expression ( comparison_operator | 'REGEXP' ) ( string_expression | all_or_any_expression )
		{
		pushFollow(FOLLOW_string_expression_in_synpred178_JPA23332);
		string_expression();
		state._fsp--;
		if (state.failed) return;

		// JPA2.g:350:25: ( comparison_operator | 'REGEXP' )
		int alt169=2;
		int LA169_0 = input.LA(1);
		if ( ((LA169_0 >= 71 && LA169_0 <= 76)) ) {
			alt169=1;
		}
		else if ( (LA169_0==128) ) {
			alt169=2;
		}

		else {
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 169, 0, input);
			throw nvae;
		}

		switch (alt169) {
			case 1 :
				// JPA2.g:350:26: comparison_operator
				{
				pushFollow(FOLLOW_comparison_operator_in_synpred178_JPA23335);
				comparison_operator();
				state._fsp--;
				if (state.failed) return;

				}
				break;
			case 2 :
				// JPA2.g:350:48: 'REGEXP'
				{
				match(input,128,FOLLOW_128_in_synpred178_JPA23339); if (state.failed) return;

				}
				break;

		}

		// JPA2.g:350:58: ( string_expression | all_or_any_expression )
		int alt170=2;
		int LA170_0 = input.LA(1);
		if ( (LA170_0==AVG||LA170_0==CASE||LA170_0==COUNT||LA170_0==GROUP||(LA170_0 >= LOWER && LA170_0 <= NAMED_PARAMETER)||(LA170_0 >= STRING_LITERAL && LA170_0 <= SUM)||LA170_0==WORD||LA170_0==63||LA170_0==77||LA170_0==83||(LA170_0 >= 90 && LA170_0 <= 92)||LA170_0==103||LA170_0==105||LA170_0==121||LA170_0==134||LA170_0==137||LA170_0==140) ) {
			alt170=1;
		}
		else if ( ((LA170_0 >= 86 && LA170_0 <= 87)||LA170_0==132) ) {
			alt170=2;
		}

		else {
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 170, 0, input);
			throw nvae;
		}

		switch (alt170) {
			case 1 :
				// JPA2.g:350:59: string_expression
				{
				pushFollow(FOLLOW_string_expression_in_synpred178_JPA23343);
				string_expression();
				state._fsp--;
				if (state.failed) return;

				}
				break;
			case 2 :
				// JPA2.g:350:79: all_or_any_expression
				{
				pushFollow(FOLLOW_all_or_any_expression_in_synpred178_JPA23347);
				all_or_any_expression();
				state._fsp--;
				if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred178_JPA2

	// $ANTLR start synpred181_JPA2
	public final void synpred181_JPA2_fragment() throws RecognitionException {
		// JPA2.g:351:7: ( boolean_expression ( '=' | '<>' ) ( boolean_expression | all_or_any_expression ) )
		// JPA2.g:351:7: boolean_expression ( '=' | '<>' ) ( boolean_expression | all_or_any_expression )
		{
		pushFollow(FOLLOW_boolean_expression_in_synpred181_JPA23356);
		boolean_expression();
		state._fsp--;
		if (state.failed) return;

		if ( (input.LA(1) >= 73 && input.LA(1) <= 74) ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		// JPA2.g:351:39: ( boolean_expression | all_or_any_expression )
		int alt171=2;
		int LA171_0 = input.LA(1);
		if ( (LA171_0==CASE||LA171_0==GROUP||LA171_0==LPAREN||LA171_0==NAMED_PARAMETER||LA171_0==WORD||LA171_0==63||LA171_0==77||LA171_0==83||(LA171_0 >= 90 && LA171_0 <= 91)||LA171_0==103||LA171_0==105||LA171_0==121||(LA171_0 >= 146 && LA171_0 <= 147)) ) {
			alt171=1;
		}
		else if ( ((LA171_0 >= 86 && LA171_0 <= 87)||LA171_0==132) ) {
			alt171=2;
		}

		else {
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 171, 0, input);
			throw nvae;
		}

		switch (alt171) {
			case 1 :
				// JPA2.g:351:40: boolean_expression
				{
				pushFollow(FOLLOW_boolean_expression_in_synpred181_JPA23367);
				boolean_expression();
				state._fsp--;
				if (state.failed) return;

				}
				break;
			case 2 :
				// JPA2.g:351:61: all_or_any_expression
				{
				pushFollow(FOLLOW_all_or_any_expression_in_synpred181_JPA23371);
				all_or_any_expression();
				state._fsp--;
				if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred181_JPA2

	// $ANTLR start synpred184_JPA2
	public final void synpred184_JPA2_fragment() throws RecognitionException {
		// JPA2.g:352:7: ( enum_expression ( '=' | '<>' ) ( enum_expression | all_or_any_expression ) )
		// JPA2.g:352:7: enum_expression ( '=' | '<>' ) ( enum_expression | all_or_any_expression )
		{
		pushFollow(FOLLOW_enum_expression_in_synpred184_JPA23380);
		enum_expression();
		state._fsp--;
		if (state.failed) return;

		if ( (input.LA(1) >= 73 && input.LA(1) <= 74) ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		// JPA2.g:352:34: ( enum_expression | all_or_any_expression )
		int alt172=2;
		int LA172_0 = input.LA(1);
		if ( (LA172_0==CASE||LA172_0==GROUP||LA172_0==LPAREN||LA172_0==NAMED_PARAMETER||LA172_0==WORD||LA172_0==63||LA172_0==77||LA172_0==91||LA172_0==121) ) {
			alt172=1;
		}
		else if ( ((LA172_0 >= 86 && LA172_0 <= 87)||LA172_0==132) ) {
			alt172=2;
		}

		else {
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 172, 0, input);
			throw nvae;
		}

		switch (alt172) {
			case 1 :
				// JPA2.g:352:35: enum_expression
				{
				pushFollow(FOLLOW_enum_expression_in_synpred184_JPA23389);
				enum_expression();
				state._fsp--;
				if (state.failed) return;

				}
				break;
			case 2 :
				// JPA2.g:352:53: all_or_any_expression
				{
				pushFollow(FOLLOW_all_or_any_expression_in_synpred184_JPA23393);
				all_or_any_expression();
				state._fsp--;
				if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred184_JPA2

	// $ANTLR start synpred186_JPA2
	public final void synpred186_JPA2_fragment() throws RecognitionException {
		// JPA2.g:353:7: ( datetime_expression comparison_operator ( datetime_expression | all_or_any_expression ) )
		// JPA2.g:353:7: datetime_expression comparison_operator ( datetime_expression | all_or_any_expression )
		{
		pushFollow(FOLLOW_datetime_expression_in_synpred186_JPA23402);
		datetime_expression();
		state._fsp--;
		if (state.failed) return;

		pushFollow(FOLLOW_comparison_operator_in_synpred186_JPA23404);
		comparison_operator();
		state._fsp--;
		if (state.failed) return;

		// JPA2.g:353:47: ( datetime_expression | all_or_any_expression )
		int alt173=2;
		int LA173_0 = input.LA(1);
		if ( (LA173_0==AVG||LA173_0==CASE||LA173_0==COUNT||LA173_0==GROUP||(LA173_0 >= LPAREN && LA173_0 <= NAMED_PARAMETER)||LA173_0==SUM||LA173_0==WORD||LA173_0==63||LA173_0==77||LA173_0==83||(LA173_0 >= 90 && LA173_0 <= 91)||(LA173_0 >= 93 && LA173_0 <= 95)||LA173_0==103||LA173_0==105||LA173_0==121) ) {
			alt173=1;
		}
		else if ( ((LA173_0 >= 86 && LA173_0 <= 87)||LA173_0==132) ) {
			alt173=2;
		}

		else {
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 173, 0, input);
			throw nvae;
		}

		switch (alt173) {
			case 1 :
				// JPA2.g:353:48: datetime_expression
				{
				pushFollow(FOLLOW_datetime_expression_in_synpred186_JPA23407);
				datetime_expression();
				state._fsp--;
				if (state.failed) return;

				}
				break;
			case 2 :
				// JPA2.g:353:70: all_or_any_expression
				{
				pushFollow(FOLLOW_all_or_any_expression_in_synpred186_JPA23411);
				all_or_any_expression();
				state._fsp--;
				if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred186_JPA2

	// $ANTLR start synpred189_JPA2
	public final void synpred189_JPA2_fragment() throws RecognitionException {
		// JPA2.g:354:7: ( entity_expression ( '=' | '<>' ) ( entity_expression | all_or_any_expression ) )
		// JPA2.g:354:7: entity_expression ( '=' | '<>' ) ( entity_expression | all_or_any_expression )
		{
		pushFollow(FOLLOW_entity_expression_in_synpred189_JPA23420);
		entity_expression();
		state._fsp--;
		if (state.failed) return;

		if ( (input.LA(1) >= 73 && input.LA(1) <= 74) ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		// JPA2.g:354:38: ( entity_expression | all_or_any_expression )
		int alt174=2;
		int LA174_0 = input.LA(1);
		if ( (LA174_0==GROUP||LA174_0==NAMED_PARAMETER||LA174_0==WORD||LA174_0==63||LA174_0==77) ) {
			alt174=1;
		}
		else if ( ((LA174_0 >= 86 && LA174_0 <= 87)||LA174_0==132) ) {
			alt174=2;
		}

		else {
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 174, 0, input);
			throw nvae;
		}

		switch (alt174) {
			case 1 :
				// JPA2.g:354:39: entity_expression
				{
				pushFollow(FOLLOW_entity_expression_in_synpred189_JPA23431);
				entity_expression();
				state._fsp--;
				if (state.failed) return;

				}
				break;
			case 2 :
				// JPA2.g:354:59: all_or_any_expression
				{
				pushFollow(FOLLOW_all_or_any_expression_in_synpred189_JPA23435);
				all_or_any_expression();
				state._fsp--;
				if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred189_JPA2

	// $ANTLR start synpred191_JPA2
	public final void synpred191_JPA2_fragment() throws RecognitionException {
		// JPA2.g:355:7: ( entity_type_expression ( '=' | '<>' ) entity_type_expression )
		// JPA2.g:355:7: entity_type_expression ( '=' | '<>' ) entity_type_expression
		{
		pushFollow(FOLLOW_entity_type_expression_in_synpred191_JPA23444);
		entity_type_expression();
		state._fsp--;
		if (state.failed) return;

		if ( (input.LA(1) >= 73 && input.LA(1) <= 74) ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_entity_type_expression_in_synpred191_JPA23454);
		entity_type_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred191_JPA2

	// $ANTLR start synpred200_JPA2
	public final void synpred200_JPA2_fragment() throws RecognitionException {
		// JPA2.g:366:7: ( arithmetic_term ( ( '+' | '-' ) arithmetic_term )+ )
		// JPA2.g:366:7: arithmetic_term ( ( '+' | '-' ) arithmetic_term )+
		{
		pushFollow(FOLLOW_arithmetic_term_in_synpred200_JPA23535);
		arithmetic_term();
		state._fsp--;
		if (state.failed) return;

		// JPA2.g:366:23: ( ( '+' | '-' ) arithmetic_term )+
		int cnt175=0;
		loop175:
		while (true) {
			int alt175=2;
			int LA175_0 = input.LA(1);
			if ( (LA175_0==65||LA175_0==67) ) {
				alt175=1;
			}

			switch (alt175) {
			case 1 :
				// JPA2.g:366:24: ( '+' | '-' ) arithmetic_term
				{
				if ( input.LA(1)==65||input.LA(1)==67 ) {
					input.consume();
					state.errorRecovery=false;
					state.failed=false;
				}
				else {
					if (state.backtracking>0) {state.failed=true; return;}
					MismatchedSetException mse = new MismatchedSetException(null,input);
					throw mse;
				}
				pushFollow(FOLLOW_arithmetic_term_in_synpred200_JPA23546);
				arithmetic_term();
				state._fsp--;
				if (state.failed) return;

				}
				break;

			default :
				if ( cnt175 >= 1 ) break loop175;
				if (state.backtracking>0) {state.failed=true; return;}
				EarlyExitException eee = new EarlyExitException(175, input);
				throw eee;
			}
			cnt175++;
		}

		}

	}
	// $ANTLR end synpred200_JPA2

	// $ANTLR start synpred203_JPA2
	public final void synpred203_JPA2_fragment() throws RecognitionException {
		// JPA2.g:369:7: ( arithmetic_factor ( ( '*' | '/' ) arithmetic_factor )+ )
		// JPA2.g:369:7: arithmetic_factor ( ( '*' | '/' ) arithmetic_factor )+
		{
		pushFollow(FOLLOW_arithmetic_factor_in_synpred203_JPA23567);
		arithmetic_factor();
		state._fsp--;
		if (state.failed) return;

		// JPA2.g:369:25: ( ( '*' | '/' ) arithmetic_factor )+
		int cnt176=0;
		loop176:
		while (true) {
			int alt176=2;
			int LA176_0 = input.LA(1);
			if ( (LA176_0==64||LA176_0==69) ) {
				alt176=1;
			}

			switch (alt176) {
			case 1 :
				// JPA2.g:369:26: ( '*' | '/' ) arithmetic_factor
				{
				if ( input.LA(1)==64||input.LA(1)==69 ) {
					input.consume();
					state.errorRecovery=false;
					state.failed=false;
				}
				else {
					if (state.backtracking>0) {state.failed=true; return;}
					MismatchedSetException mse = new MismatchedSetException(null,input);
					throw mse;
				}
				pushFollow(FOLLOW_arithmetic_factor_in_synpred203_JPA23579);
				arithmetic_factor();
				state._fsp--;
				if (state.failed) return;

				}
				break;

			default :
				if ( cnt176 >= 1 ) break loop176;
				if (state.backtracking>0) {state.failed=true; return;}
				EarlyExitException eee = new EarlyExitException(176, input);
				throw eee;
			}
			cnt176++;
		}

		}

	}
	// $ANTLR end synpred203_JPA2

	// $ANTLR start synpred207_JPA2
	public final void synpred207_JPA2_fragment() throws RecognitionException {
		// JPA2.g:375:7: ( decimal_literal )
		// JPA2.g:375:7: decimal_literal
		{
		pushFollow(FOLLOW_decimal_literal_in_synpred207_JPA23631);
		decimal_literal();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred207_JPA2

	// $ANTLR start synpred208_JPA2
	public final void synpred208_JPA2_fragment() throws RecognitionException {
		// JPA2.g:376:7: ( numeric_literal )
		// JPA2.g:376:7: numeric_literal
		{
		pushFollow(FOLLOW_numeric_literal_in_synpred208_JPA23639);
		numeric_literal();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred208_JPA2

	// $ANTLR start synpred209_JPA2
	public final void synpred209_JPA2_fragment() throws RecognitionException {
		// JPA2.g:377:7: ( '(' arithmetic_expression ')' )
		// JPA2.g:377:7: '(' arithmetic_expression ')'
		{
		match(input,LPAREN,FOLLOW_LPAREN_in_synpred209_JPA23647); if (state.failed) return;

		pushFollow(FOLLOW_arithmetic_expression_in_synpred209_JPA23648);
		arithmetic_expression();
		state._fsp--;
		if (state.failed) return;

		match(input,RPAREN,FOLLOW_RPAREN_in_synpred209_JPA23649); if (state.failed) return;

		}

	}
	// $ANTLR end synpred209_JPA2

	// $ANTLR start synpred212_JPA2
	public final void synpred212_JPA2_fragment() throws RecognitionException {
		// JPA2.g:380:7: ( aggregate_expression )
		// JPA2.g:380:7: aggregate_expression
		{
		pushFollow(FOLLOW_aggregate_expression_in_synpred212_JPA23673);
		aggregate_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred212_JPA2

	// $ANTLR start synpred214_JPA2
	public final void synpred214_JPA2_fragment() throws RecognitionException {
		// JPA2.g:382:7: ( function_invocation )
		// JPA2.g:382:7: function_invocation
		{
		pushFollow(FOLLOW_function_invocation_in_synpred214_JPA23689);
		function_invocation();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred214_JPA2

	// $ANTLR start synpred220_JPA2
	public final void synpred220_JPA2_fragment() throws RecognitionException {
		// JPA2.g:390:7: ( aggregate_expression )
		// JPA2.g:390:7: aggregate_expression
		{
		pushFollow(FOLLOW_aggregate_expression_in_synpred220_JPA23748);
		aggregate_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred220_JPA2

	// $ANTLR start synpred222_JPA2
	public final void synpred222_JPA2_fragment() throws RecognitionException {
		// JPA2.g:392:7: ( function_invocation )
		// JPA2.g:392:7: function_invocation
		{
		pushFollow(FOLLOW_function_invocation_in_synpred222_JPA23764);
		function_invocation();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred222_JPA2

	// $ANTLR start synpred224_JPA2
	public final void synpred224_JPA2_fragment() throws RecognitionException {
		// JPA2.g:396:7: ( path_expression )
		// JPA2.g:396:7: path_expression
		{
		pushFollow(FOLLOW_path_expression_in_synpred224_JPA23791);
		path_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred224_JPA2

	// $ANTLR start synpred227_JPA2
	public final void synpred227_JPA2_fragment() throws RecognitionException {
		// JPA2.g:399:7: ( aggregate_expression )
		// JPA2.g:399:7: aggregate_expression
		{
		pushFollow(FOLLOW_aggregate_expression_in_synpred227_JPA23815);
		aggregate_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred227_JPA2

	// $ANTLR start synpred229_JPA2
	public final void synpred229_JPA2_fragment() throws RecognitionException {
		// JPA2.g:401:7: ( function_invocation )
		// JPA2.g:401:7: function_invocation
		{
		pushFollow(FOLLOW_function_invocation_in_synpred229_JPA23831);
		function_invocation();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred229_JPA2

	// $ANTLR start synpred231_JPA2
	public final void synpred231_JPA2_fragment() throws RecognitionException {
		// JPA2.g:403:7: ( date_time_timestamp_literal )
		// JPA2.g:403:7: date_time_timestamp_literal
		{
		pushFollow(FOLLOW_date_time_timestamp_literal_in_synpred231_JPA23847);
		date_time_timestamp_literal();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred231_JPA2

	// $ANTLR start synpred269_JPA2
	public final void synpred269_JPA2_fragment() throws RecognitionException {
		// JPA2.g:454:7: ( literal )
		// JPA2.g:454:7: literal
		{
		pushFollow(FOLLOW_literal_in_synpred269_JPA24302);
		literal();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred269_JPA2

	// $ANTLR start synpred270_JPA2
	public final void synpred270_JPA2_fragment() throws RecognitionException {
		// JPA2.g:455:7: ( path_expression )
		// JPA2.g:455:7: path_expression
		{
		pushFollow(FOLLOW_path_expression_in_synpred270_JPA24310);
		path_expression();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred270_JPA2

	// $ANTLR start synpred271_JPA2
	public final void synpred271_JPA2_fragment() throws RecognitionException {
		// JPA2.g:456:7: ( input_parameter )
		// JPA2.g:456:7: input_parameter
		{
		pushFollow(FOLLOW_input_parameter_in_synpred271_JPA24318);
		input_parameter();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred271_JPA2

	// Delegated rules

	public final boolean synpred220_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred220_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred69_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred69_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred46_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred46_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred94_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred94_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred103_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred103_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred203_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred203_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred99_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred99_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred33_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred33_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred86_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred86_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred93_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred93_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred55_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred55_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred181_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred181_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred212_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred212_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred104_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred104_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred81_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred81_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred98_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred98_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred144_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred144_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred224_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred224_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred227_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred227_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred208_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred208_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred70_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred70_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred30_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred30_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred68_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred68_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred160_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred160_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred163_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred163_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred171_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred171_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred92_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred92_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred87_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred87_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred21_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred21_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred231_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred231_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred49_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred49_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred43_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred43_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred88_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred88_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred229_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred229_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred91_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred91_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred100_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred100_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred200_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred200_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred51_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred51_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred207_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred207_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred90_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred90_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred184_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred184_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred44_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred44_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred89_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred89_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred97_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred97_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred270_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred270_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred101_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred101_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred146_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred146_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred105_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred105_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred186_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred186_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred189_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred189_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred45_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred45_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred53_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred53_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred102_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred102_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred161_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred161_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred67_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred67_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred269_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred269_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred271_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred271_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred222_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred222_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred214_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred214_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred191_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred191_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred178_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred178_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred209_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred209_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred34_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred34_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred50_JPA2() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred50_JPA2_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}


	protected DFA41 dfa41 = new DFA41(this);
	static final String DFA41_eotS =
		"\35\uffff";
	static final String DFA41_eofS =
		"\35\uffff";
	static final String DFA41_minS =
		"\1\7\1\33\2\uffff\2\7\1\43\1\uffff\1\5\22\43\1\0\1\5";
	static final String DFA41_maxS =
		"\1\151\1\33\2\uffff\2\u0085\1\104\1\uffff\1\u0091\22\105\1\0\1\u0091";
	static final String DFA41_acceptS =
		"\2\uffff\1\1\1\3\3\uffff\1\2\25\uffff";
	static final String DFA41_specialS =
		"\33\uffff\1\0\1\uffff}>";
	static final String[] DFA41_transitionS = {
			"\1\2\3\uffff\1\1\20\uffff\2\2\11\uffff\1\2\101\uffff\1\3",
			"\1\4",
			"",
			"",
			"\1\2\1\uffff\1\2\1\uffff\1\2\1\uffff\1\5\4\uffff\1\6\3\uffff\1\2\4\uffff"+
			"\4\2\10\uffff\1\2\25\uffff\1\6\1\uffff\1\2\1\uffff\1\2\1\uffff\1\2\2"+
			"\uffff\1\2\6\uffff\1\2\5\uffff\1\2\1\uffff\1\2\4\uffff\2\2\13\uffff\1"+
			"\2\1\uffff\1\2\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\2\uffff\1\2\4\uffff"+
			"\1\2\11\uffff\1\2\1\uffff\1\2",
			"\1\2\1\uffff\1\2\1\uffff\1\2\6\uffff\1\6\3\uffff\1\2\4\uffff\4\2\10"+
			"\uffff\1\2\25\uffff\1\6\1\uffff\1\2\1\uffff\1\2\1\uffff\1\2\2\uffff\1"+
			"\2\6\uffff\1\2\5\uffff\1\2\1\uffff\1\2\4\uffff\2\2\13\uffff\1\2\1\uffff"+
			"\1\2\1\uffff\1\2\3\uffff\1\2\1\uffff\1\2\2\uffff\1\2\4\uffff\1\2\11\uffff"+
			"\1\2\1\uffff\1\2",
			"\1\7\40\uffff\1\10",
			"",
			"\1\23\1\31\1\21\1\uffff\1\25\1\uffff\1\22\1\30\5\uffff\1\14\11\uffff"+
			"\1\16\1\17\3\uffff\1\15\1\uffff\1\33\1\uffff\1\27\1\uffff\1\20\25\uffff"+
			"\1\11\2\uffff\2\2\1\uffff\1\2\1\uffff\1\2\32\uffff\1\32\3\uffff\1\32"+
			"\3\uffff\1\13\1\uffff\1\32\7\uffff\1\24\1\32\1\uffff\1\32\6\uffff\1\26"+
			"\2\uffff\1\32\1\uffff\1\32\1\12\14\uffff\1\32\1\uffff\1\32",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\33\34\uffff\2\2\1\uffff\1\2\1\34\1\2",
			"\1\uffff",
			"\1\23\1\31\1\21\1\uffff\1\25\1\uffff\1\22\1\30\5\uffff\1\14\11\uffff"+
			"\1\16\1\17\3\uffff\1\15\1\uffff\1\33\1\uffff\1\27\1\uffff\1\20\25\uffff"+
			"\1\11\2\uffff\2\2\1\uffff\1\2\1\uffff\1\2\32\uffff\1\32\3\uffff\1\32"+
			"\3\uffff\1\13\1\uffff\1\32\7\uffff\1\24\1\32\1\uffff\1\32\6\uffff\1\26"+
			"\2\uffff\1\32\1\uffff\1\32\1\12\14\uffff\1\32\1\uffff\1\32"
	};

	static final short[] DFA41_eot = DFA.unpackEncodedString(DFA41_eotS);
	static final short[] DFA41_eof = DFA.unpackEncodedString(DFA41_eofS);
	static final char[] DFA41_min = DFA.unpackEncodedStringToUnsignedChars(DFA41_minS);
	static final char[] DFA41_max = DFA.unpackEncodedStringToUnsignedChars(DFA41_maxS);
	static final short[] DFA41_accept = DFA.unpackEncodedString(DFA41_acceptS);
	static final short[] DFA41_special = DFA.unpackEncodedString(DFA41_specialS);
	static final short[][] DFA41_transition;

	static {
		int numStates = DFA41_transitionS.length;
		DFA41_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA41_transition[i] = DFA.unpackEncodedString(DFA41_transitionS[i]);
		}
	}

	protected class DFA41 extends DFA {

		public DFA41(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 41;
			this.eot = DFA41_eot;
			this.eof = DFA41_eof;
			this.min = DFA41_min;
			this.max = DFA41_max;
			this.accept = DFA41_accept;
			this.special = DFA41_special;
			this.transition = DFA41_transition;
		}
		@Override
		public String getDescription() {
			return "189:1: aggregate_expression : ( aggregate_expression_function_name '(' ( DISTINCT )? arithmetic_expression ')' -> ^( T_AGGREGATE_EXPR[] aggregate_expression_function_name '(' ( 'DISTINCT' )? arithmetic_expression ')' ) | 'COUNT' '(' ( DISTINCT )? count_argument ')' -> ^( T_AGGREGATE_EXPR[] 'COUNT' '(' ( 'DISTINCT' )? count_argument ')' ) | function_invocation );";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			TokenStream input = (TokenStream)_input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA41_27 = input.LA(1);
						 
						int index41_27 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred53_JPA2()) ) {s = 2;}
						else if ( (synpred55_JPA2()) ) {s = 7;}
						 
						input.seek(index41_27);
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 41, _s, input);
			error(nvae);
			throw nvae;
		}
	}

	public static final BitSet FOLLOW_select_statement_in_ql_statement511 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_update_statement_in_ql_statement515 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_delete_statement_in_ql_statement519 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_130_in_select_statement534 = new BitSet(new long[]{0xA00000C07C442A80L,0x12528A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_select_clause_in_select_statement536 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_from_clause_in_select_statement538 = new BitSet(new long[]{0x00000002000C0000L,0x0000000000000000L,0x0000000000010000L});
	public static final BitSet FOLLOW_where_clause_in_select_statement541 = new BitSet(new long[]{0x00000002000C0000L});
	public static final BitSet FOLLOW_groupby_clause_in_select_statement546 = new BitSet(new long[]{0x0000000200080000L});
	public static final BitSet FOLLOW_having_clause_in_select_statement551 = new BitSet(new long[]{0x0000000200000000L});
	public static final BitSet FOLLOW_orderby_clause_in_select_statement556 = new BitSet(new long[]{0x0000000000000000L});
	public static final BitSet FOLLOW_EOF_in_select_statement560 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_139_in_update_statement616 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_update_clause_in_update_statement618 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000010000L});
	public static final BitSet FOLLOW_where_clause_in_update_statement621 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_97_in_delete_statement657 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_delete_clause_in_delete_statement659 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000010000L});
	public static final BitSet FOLLOW_where_clause_in_delete_statement662 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_104_in_from_clause700 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_identification_variable_declaration_in_from_clause702 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_from_clause705 = new BitSet(new long[]{0x2000000000100000L});
	public static final BitSet FOLLOW_identification_variable_declaration_or_collection_member_declaration_in_from_clause707 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_identification_variable_declaration_in_identification_variable_declaration_or_collection_member_declaration741 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_collection_member_declaration_in_identification_variable_declaration_or_collection_member_declaration750 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_range_variable_declaration_in_identification_variable_declaration774 = new BitSet(new long[]{0x0000000001A00002L});
	public static final BitSet FOLLOW_joined_clause_in_identification_variable_declaration776 = new BitSet(new long[]{0x0000000001A00002L});
	public static final BitSet FOLLOW_joined_clause_in_join_section807 = new BitSet(new long[]{0x0000000001A00002L});
	public static final BitSet FOLLOW_join_in_joined_clause815 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fetch_join_in_joined_clause819 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_entity_name_in_range_variable_declaration831 = new BitSet(new long[]{0x2000000000040020L});
	public static final BitSet FOLLOW_AS_in_range_variable_declaration834 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_identification_variable_in_range_variable_declaration838 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_join_spec_in_join867 = new BitSet(new long[]{0x2000000000040000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_join_association_path_expression_in_join869 = new BitSet(new long[]{0x2000000000040020L});
	public static final BitSet FOLLOW_AS_in_join872 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_identification_variable_in_join876 = new BitSet(new long[]{0x0000000000000002L,0x4000000000000000L});
	public static final BitSet FOLLOW_126_in_join879 = new BitSet(new long[]{0xA00000C0FC440A80L,0x02128AC0FC3FE04AL,0x00000000000C1768L});
	public static final BitSet FOLLOW_conditional_expression_in_join881 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_join_spec_in_fetch_join915 = new BitSet(new long[]{0x0000000000020000L});
	public static final BitSet FOLLOW_FETCH_in_fetch_join917 = new BitSet(new long[]{0x2000000000040000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_join_association_path_expression_in_fetch_join919 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LEFT_in_join_spec933 = new BitSet(new long[]{0x0000000400800000L});
	public static final BitSet FOLLOW_OUTER_in_join_spec937 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_INNER_in_join_spec943 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_JOIN_in_join_spec948 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identification_variable_in_join_association_path_expression962 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_join_association_path_expression964 = new BitSet(new long[]{0x200000A230041AE2L,0x902C051100000000L,0x0000000000028006L});
	public static final BitSet FOLLOW_field_in_join_association_path_expression967 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_join_association_path_expression968 = new BitSet(new long[]{0x200000A230041AE2L,0x902C051100000000L,0x0000000000028006L});
	public static final BitSet FOLLOW_field_in_join_association_path_expression972 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_136_in_join_association_path_expression1007 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_identification_variable_in_join_association_path_expression1009 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_join_association_path_expression1011 = new BitSet(new long[]{0x200000A230041AE0L,0x902C051100000000L,0x0000000000028006L});
	public static final BitSet FOLLOW_field_in_join_association_path_expression1014 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_join_association_path_expression1015 = new BitSet(new long[]{0x200000A230041AE0L,0x902C051100000000L,0x0000000000028006L});
	public static final BitSet FOLLOW_field_in_join_association_path_expression1019 = new BitSet(new long[]{0x0000000000000020L});
	public static final BitSet FOLLOW_AS_in_join_association_path_expression1022 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_subtype_in_join_association_path_expression1024 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_join_association_path_expression1026 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_entity_name_in_join_association_path_expression1059 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IN_in_collection_member_declaration1072 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_collection_member_declaration1073 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_path_expression_in_collection_member_declaration1075 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_collection_member_declaration1077 = new BitSet(new long[]{0x2000000000040020L});
	public static final BitSet FOLLOW_AS_in_collection_member_declaration1080 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_identification_variable_in_collection_member_declaration1084 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_map_field_identification_variable_in_qualified_identification_variable1113 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_99_in_qualified_identification_variable1121 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_identification_variable_in_qualified_identification_variable1122 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_qualified_identification_variable1123 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_109_in_map_field_identification_variable1130 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_identification_variable_in_map_field_identification_variable1131 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_map_field_identification_variable1132 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_142_in_map_field_identification_variable1136 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_identification_variable_in_map_field_identification_variable1137 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_map_field_identification_variable1138 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identification_variable_in_path_expression1152 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_path_expression1154 = new BitSet(new long[]{0x200000A230041AE2L,0x902C051100000000L,0x0000000000028006L});
	public static final BitSet FOLLOW_field_in_path_expression1157 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_path_expression1158 = new BitSet(new long[]{0x200000A230041AE2L,0x902C051100000000L,0x0000000000028006L});
	public static final BitSet FOLLOW_field_in_path_expression1162 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identification_variable_in_general_identification_variable1201 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_map_field_identification_variable_in_general_identification_variable1209 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identification_variable_declaration_in_update_clause1222 = new BitSet(new long[]{0x0000002000000000L});
	public static final BitSet FOLLOW_SET_in_update_clause1224 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_update_item_in_update_clause1226 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_update_clause1229 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_update_item_in_update_clause1231 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_path_expression_in_update_item1273 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_74_in_update_item1275 = new BitSet(new long[]{0xA00000C07C440A80L,0x03128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_new_value_in_update_item1277 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_scalar_expression_in_new_value1288 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simple_entity_expression_in_new_value1296 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_120_in_new_value1304 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_104_in_delete_clause1318 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_identification_variable_declaration_in_delete_clause1320 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DISTINCT_in_select_clause1348 = new BitSet(new long[]{0xA00000C07C440A80L,0x12528A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_select_item_in_select_clause1352 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_select_clause1355 = new BitSet(new long[]{0xA00000C07C440A80L,0x12528A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_select_item_in_select_clause1357 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_select_expression_in_select_item1400 = new BitSet(new long[]{0x2000000000000022L});
	public static final BitSet FOLLOW_AS_in_select_item1404 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_result_variable_in_select_item1408 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_select_expression1421 = new BitSet(new long[]{0x0000000000000002L,0x000000000000002BL});
	public static final BitSet FOLLOW_set_in_select_expression1424 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_scalar_expression_in_select_expression1440 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identification_variable_in_select_expression1450 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_scalar_expression_in_select_expression1468 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_in_select_expression1476 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_124_in_select_expression1484 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_select_expression1486 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_identification_variable_in_select_expression1487 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_select_expression1488 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_constructor_expression_in_select_expression1496 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_118_in_constructor_expression1507 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_constructor_name_in_constructor_expression1509 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_constructor_expression1511 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_constructor_item_in_constructor_expression1513 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_constructor_expression1516 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_constructor_item_in_constructor_expression1518 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_RPAREN_in_constructor_expression1522 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_constructor_item1533 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_scalar_expression_in_constructor_item1541 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_in_constructor_item1549 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identification_variable_in_constructor_item1557 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_function_name_in_aggregate_expression1568 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_aggregate_expression1570 = new BitSet(new long[]{0xA000008078442A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_DISTINCT_in_aggregate_expression1572 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_aggregate_expression1576 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_aggregate_expression1577 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COUNT_in_aggregate_expression1611 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_aggregate_expression1613 = new BitSet(new long[]{0x2000000000042000L});
	public static final BitSet FOLLOW_DISTINCT_in_aggregate_expression1615 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_count_argument_in_aggregate_expression1619 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_aggregate_expression1621 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_function_invocation_in_aggregate_expression1656 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identification_variable_in_count_argument1693 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_count_argument1697 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_144_in_where_clause1710 = new BitSet(new long[]{0xA00000C0FC440A80L,0x02128AC0FC3FE04AL,0x00000000000C1768L});
	public static final BitSet FOLLOW_conditional_expression_in_where_clause1712 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GROUP_in_groupby_clause1734 = new BitSet(new long[]{0x0000000000000100L});
	public static final BitSet FOLLOW_BY_in_groupby_clause1736 = new BitSet(new long[]{0x2000000000040000L,0x0000008000000000L});
	public static final BitSet FOLLOW_groupby_item_in_groupby_clause1738 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_groupby_clause1741 = new BitSet(new long[]{0x2000000000040000L,0x0000008000000000L});
	public static final BitSet FOLLOW_groupby_item_in_groupby_clause1743 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_path_expression_in_groupby_item1777 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identification_variable_in_groupby_item1781 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_extract_function_in_groupby_item1785 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_HAVING_in_having_clause1796 = new BitSet(new long[]{0xA00000C0FC440A80L,0x02128AC0FC3FE04AL,0x00000000000C1768L});
	public static final BitSet FOLLOW_conditional_expression_in_having_clause1798 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ORDER_in_orderby_clause1809 = new BitSet(new long[]{0x0000000000000100L});
	public static final BitSet FOLLOW_BY_in_orderby_clause1811 = new BitSet(new long[]{0xA00000C07C440A80L,0x0212AA80FC28204AL,0x00000000000C5668L});
	public static final BitSet FOLLOW_orderby_item_in_orderby_clause1813 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_orderby_clause1816 = new BitSet(new long[]{0xA00000C07C440A80L,0x0212AA80FC28204AL,0x00000000000C5668L});
	public static final BitSet FOLLOW_orderby_item_in_orderby_clause1818 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_orderby_variable_in_orderby_item1852 = new BitSet(new long[]{0x0000000000001042L,0x0C00000000000000L});
	public static final BitSet FOLLOW_sort_in_orderby_item1854 = new BitSet(new long[]{0x0000000000000002L,0x0C00000000000000L});
	public static final BitSet FOLLOW_sortNulls_in_orderby_item1857 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_orderby_variable1892 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_general_identification_variable_in_orderby_variable1896 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_result_variable_in_orderby_variable1900 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_scalar_expression_in_orderby_variable1904 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_in_orderby_variable1908 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_subquery1955 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_130_in_subquery1957 = new BitSet(new long[]{0xA00000C07C442A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_simple_select_clause_in_subquery1959 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_subquery_from_clause_in_subquery1961 = new BitSet(new long[]{0x00000008000C0000L,0x0000000000000000L,0x0000000000010000L});
	public static final BitSet FOLLOW_where_clause_in_subquery1964 = new BitSet(new long[]{0x00000008000C0000L});
	public static final BitSet FOLLOW_groupby_clause_in_subquery1969 = new BitSet(new long[]{0x0000000800080000L});
	public static final BitSet FOLLOW_having_clause_in_subquery1974 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_subquery1980 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_104_in_subquery_from_clause2030 = new BitSet(new long[]{0x2000000000100000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_subselect_identification_variable_declaration_in_subquery_from_clause2032 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_subquery_from_clause2035 = new BitSet(new long[]{0x2000000000100000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_subselect_identification_variable_declaration_in_subquery_from_clause2037 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_identification_variable_declaration_in_subselect_identification_variable_declaration2075 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_derived_path_expression_in_subselect_identification_variable_declaration2083 = new BitSet(new long[]{0x2000000000040020L});
	public static final BitSet FOLLOW_AS_in_subselect_identification_variable_declaration2086 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_identification_variable_in_subselect_identification_variable_declaration2090 = new BitSet(new long[]{0x0000000001A00002L});
	public static final BitSet FOLLOW_join_in_subselect_identification_variable_declaration2093 = new BitSet(new long[]{0x0000000001A00002L});
	public static final BitSet FOLLOW_derived_collection_member_declaration_in_subselect_identification_variable_declaration2103 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_general_derived_path_in_derived_path_expression2114 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_derived_path_expression2115 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_single_valued_object_field_in_derived_path_expression2116 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_general_derived_path_in_derived_path_expression2124 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_derived_path_expression2125 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_collection_valued_field_in_derived_path_expression2126 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simple_derived_path_in_general_derived_path2137 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_treated_derived_path_in_general_derived_path2145 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_general_derived_path2147 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_single_valued_object_field_in_general_derived_path2148 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
	public static final BitSet FOLLOW_superquery_identification_variable_in_simple_derived_path2166 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_136_in_treated_derived_path2183 = new BitSet(new long[]{0x2000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_general_derived_path_in_treated_derived_path2184 = new BitSet(new long[]{0x0000000000000020L});
	public static final BitSet FOLLOW_AS_in_treated_derived_path2186 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_subtype_in_treated_derived_path2188 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_treated_derived_path2190 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IN_in_derived_collection_member_declaration2201 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_superquery_identification_variable_in_derived_collection_member_declaration2203 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_derived_collection_member_declaration2204 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_single_valued_object_field_in_derived_collection_member_declaration2206 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_derived_collection_member_declaration2208 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_collection_valued_field_in_derived_collection_member_declaration2211 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DISTINCT_in_simple_select_clause2224 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_simple_select_expression_in_simple_select_clause2228 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_simple_select_expression2268 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_scalar_expression_in_simple_select_expression2276 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_in_simple_select_expression2284 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identification_variable_in_simple_select_expression2292 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arithmetic_expression_in_scalar_expression2303 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_expression_in_scalar_expression2311 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enum_expression_in_scalar_expression2319 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_datetime_expression_in_scalar_expression2327 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_boolean_expression_in_scalar_expression2335 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_case_expression_in_scalar_expression2343 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_entity_type_expression_in_scalar_expression2351 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_conditional_term_in_conditional_expression2363 = new BitSet(new long[]{0x0000000100000002L});
	public static final BitSet FOLLOW_OR_in_conditional_expression2367 = new BitSet(new long[]{0xA00000C0FC440A80L,0x02128AC0FC3FE04AL,0x00000000000C1768L});
	public static final BitSet FOLLOW_conditional_term_in_conditional_expression2369 = new BitSet(new long[]{0x0000000100000002L});
	public static final BitSet FOLLOW_conditional_factor_in_conditional_term2383 = new BitSet(new long[]{0x0000000000000012L});
	public static final BitSet FOLLOW_AND_in_conditional_term2387 = new BitSet(new long[]{0xA00000C0FC440A80L,0x02128AC0FC3FE04AL,0x00000000000C1768L});
	public static final BitSet FOLLOW_conditional_factor_in_conditional_term2389 = new BitSet(new long[]{0x0000000000000012L});
	public static final BitSet FOLLOW_NOT_in_conditional_factor2403 = new BitSet(new long[]{0xA00000C0FC440A80L,0x02128AC0FC3FE04AL,0x00000000000C1768L});
	public static final BitSet FOLLOW_conditional_primary_in_conditional_factor2407 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simple_cond_expression_in_conditional_primary2418 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_conditional_primary2442 = new BitSet(new long[]{0xA00000C0FC440A80L,0x02128AC0FC3FE04AL,0x00000000000C1768L});
	public static final BitSet FOLLOW_conditional_expression_in_conditional_primary2443 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_conditional_primary2444 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_comparison_expression_in_simple_cond_expression2455 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_between_expression_in_simple_cond_expression2463 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_in_expression_in_simple_cond_expression2471 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_like_expression_in_simple_cond_expression2479 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_null_comparison_expression_in_simple_cond_expression2487 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_empty_collection_comparison_expression_in_simple_cond_expression2495 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_collection_member_expression_in_simple_cond_expression2503 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_exists_expression_in_simple_cond_expression2511 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_date_macro_expression_in_simple_cond_expression2519 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_date_between_macro_expression_in_date_macro_expression2532 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_date_before_macro_expression_in_date_macro_expression2540 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_date_after_macro_expression_in_date_macro_expression2548 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_date_equals_macro_expression_in_date_macro_expression2556 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_date_today_macro_expression_in_date_macro_expression2564 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_custom_date_between_macro_expression_in_date_macro_expression2572 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_78_in_date_between_macro_expression2584 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_date_between_macro_expression2586 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_path_expression_in_date_between_macro_expression2588 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_date_between_macro_expression2590 = new BitSet(new long[]{0x0000000000000000L,0x0080000000000000L});
	public static final BitSet FOLLOW_119_in_date_between_macro_expression2592 = new BitSet(new long[]{0x0000000000000000L,0x000000000000000EL});
	public static final BitSet FOLLOW_set_in_date_between_macro_expression2595 = new BitSet(new long[]{0x0000000000400000L,0x0000000000000040L});
	public static final BitSet FOLLOW_numeric_literal_in_date_between_macro_expression2603 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_date_between_macro_expression2607 = new BitSet(new long[]{0x0000000000000000L,0x0080000000000000L});
	public static final BitSet FOLLOW_119_in_date_between_macro_expression2609 = new BitSet(new long[]{0x0000000000000000L,0x000000000000000EL});
	public static final BitSet FOLLOW_set_in_date_between_macro_expression2612 = new BitSet(new long[]{0x0000000000400000L,0x0000000000000040L});
	public static final BitSet FOLLOW_numeric_literal_in_date_between_macro_expression2620 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_date_between_macro_expression2624 = new BitSet(new long[]{0x0000000000000000L,0x0028040100000000L,0x0000000000020002L});
	public static final BitSet FOLLOW_set_in_date_between_macro_expression2626 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_date_between_macro_expression2650 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_141_in_date_between_macro_expression2652 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_date_between_macro_expression2656 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_80_in_date_before_macro_expression2668 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_date_before_macro_expression2670 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_path_expression_in_date_before_macro_expression2672 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_date_before_macro_expression2674 = new BitSet(new long[]{0xA000000040040000L,0x0080000000002000L});
	public static final BitSet FOLLOW_path_expression_in_date_before_macro_expression2677 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_input_parameter_in_date_before_macro_expression2681 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_119_in_date_before_macro_expression2685 = new BitSet(new long[]{0x0000000800000000L,0x000000000000000EL});
	public static final BitSet FOLLOW_set_in_date_before_macro_expression2688 = new BitSet(new long[]{0x0000000000400000L,0x0000000000000040L});
	public static final BitSet FOLLOW_numeric_literal_in_date_before_macro_expression2696 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_date_before_macro_expression2703 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_141_in_date_before_macro_expression2705 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_date_before_macro_expression2709 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_79_in_date_after_macro_expression2721 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_date_after_macro_expression2723 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_path_expression_in_date_after_macro_expression2725 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_date_after_macro_expression2727 = new BitSet(new long[]{0xA000000040040000L,0x0080000000002000L});
	public static final BitSet FOLLOW_path_expression_in_date_after_macro_expression2730 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_input_parameter_in_date_after_macro_expression2734 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_119_in_date_after_macro_expression2738 = new BitSet(new long[]{0x0000000800000000L,0x000000000000000EL});
	public static final BitSet FOLLOW_set_in_date_after_macro_expression2741 = new BitSet(new long[]{0x0000000000400000L,0x0000000000000040L});
	public static final BitSet FOLLOW_numeric_literal_in_date_after_macro_expression2749 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_date_after_macro_expression2756 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_141_in_date_after_macro_expression2758 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_date_after_macro_expression2762 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_82_in_date_equals_macro_expression2774 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_date_equals_macro_expression2776 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_path_expression_in_date_equals_macro_expression2778 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_date_equals_macro_expression2780 = new BitSet(new long[]{0xA000000040040000L,0x0080000000002000L});
	public static final BitSet FOLLOW_path_expression_in_date_equals_macro_expression2783 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_input_parameter_in_date_equals_macro_expression2787 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_119_in_date_equals_macro_expression2791 = new BitSet(new long[]{0x0000000800000000L,0x000000000000000EL});
	public static final BitSet FOLLOW_set_in_date_equals_macro_expression2794 = new BitSet(new long[]{0x0000000000400000L,0x0000000000000040L});
	public static final BitSet FOLLOW_numeric_literal_in_date_equals_macro_expression2802 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_date_equals_macro_expression2809 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_141_in_date_equals_macro_expression2811 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_date_equals_macro_expression2815 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_84_in_date_today_macro_expression2827 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_date_today_macro_expression2829 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_path_expression_in_date_today_macro_expression2831 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_date_today_macro_expression2834 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_141_in_date_today_macro_expression2836 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_date_today_macro_expression2840 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_81_in_custom_date_between_macro_expression2852 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_custom_date_between_macro_expression2854 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_path_expression_in_custom_date_between_macro_expression2856 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_custom_date_between_macro_expression2858 = new BitSet(new long[]{0xA000004040040000L,0x0000000000002000L});
	public static final BitSet FOLLOW_path_expression_in_custom_date_between_macro_expression2861 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_input_parameter_in_custom_date_between_macro_expression2865 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_string_literal_in_custom_date_between_macro_expression2869 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_custom_date_between_macro_expression2872 = new BitSet(new long[]{0xA000004040040000L,0x0000000000002000L});
	public static final BitSet FOLLOW_path_expression_in_custom_date_between_macro_expression2875 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_input_parameter_in_custom_date_between_macro_expression2879 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_string_literal_in_custom_date_between_macro_expression2883 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_custom_date_between_macro_expression2887 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_141_in_custom_date_between_macro_expression2889 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_custom_date_between_macro_expression2893 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arithmetic_expression_in_between_expression2907 = new BitSet(new long[]{0x0000000080000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_NOT_in_between_expression2910 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_88_in_between_expression2914 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_between_expression2916 = new BitSet(new long[]{0x0000000000000010L});
	public static final BitSet FOLLOW_AND_in_between_expression2918 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_between_expression2920 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_expression_in_between_expression2928 = new BitSet(new long[]{0x0000000080000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_NOT_in_between_expression2931 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_88_in_between_expression2935 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_between_expression2937 = new BitSet(new long[]{0x0000000000000010L});
	public static final BitSet FOLLOW_AND_in_between_expression2939 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_between_expression2941 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_datetime_expression_in_between_expression2949 = new BitSet(new long[]{0x0000000080000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_NOT_in_between_expression2952 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_88_in_between_expression2956 = new BitSet(new long[]{0xA000008078040A80L,0x02000280EC082000L});
	public static final BitSet FOLLOW_datetime_expression_in_between_expression2958 = new BitSet(new long[]{0x0000000000000010L});
	public static final BitSet FOLLOW_AND_in_between_expression2960 = new BitSet(new long[]{0xA000008078040A80L,0x02000280EC082000L});
	public static final BitSet FOLLOW_datetime_expression_in_between_expression2962 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_in_expression2974 = new BitSet(new long[]{0x0000000080100000L});
	public static final BitSet FOLLOW_type_discriminator_in_in_expression2978 = new BitSet(new long[]{0x0000000080100000L});
	public static final BitSet FOLLOW_identification_variable_in_in_expression2982 = new BitSet(new long[]{0x0000000080100000L});
	public static final BitSet FOLLOW_extract_function_in_in_expression2986 = new BitSet(new long[]{0x0000000080100000L});
	public static final BitSet FOLLOW_NOT_in_in_expression2990 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_IN_in_in_expression2994 = new BitSet(new long[]{0x8000000048000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_LPAREN_in_in_expression3010 = new BitSet(new long[]{0x8000004040400000L,0x0000000000082040L});
	public static final BitSet FOLLOW_in_item_in_in_expression3012 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_in_expression3015 = new BitSet(new long[]{0x8000004040400000L,0x0000000000082040L});
	public static final BitSet FOLLOW_in_item_in_in_expression3017 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_RPAREN_in_in_expression3021 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subquery_in_in_expression3037 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_collection_valued_input_parameter_in_in_expression3053 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_in_expression3069 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_path_expression_in_in_expression3071 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_in_expression3073 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_literal_in_in_item3101 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numeric_literal_in_in_item3105 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_single_valued_input_parameter_in_in_item3109 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enum_function_in_in_item3113 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_expression_in_like_expression3124 = new BitSet(new long[]{0x0000000080000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_NOT_in_like_expression3127 = new BitSet(new long[]{0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_112_in_like_expression3131 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_like_expression3134 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_pattern_value_in_like_expression3138 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_input_parameter_in_like_expression3142 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
	public static final BitSet FOLLOW_101_in_like_expression3145 = new BitSet(new long[]{0x0000024000000000L});
	public static final BitSet FOLLOW_escape_character_in_like_expression3147 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_null_comparison_expression3161 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_input_parameter_in_null_comparison_expression3165 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_join_association_path_expression_in_null_comparison_expression3169 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_108_in_null_comparison_expression3172 = new BitSet(new long[]{0x0000000080000000L,0x0100000000000000L});
	public static final BitSet FOLLOW_NOT_in_null_comparison_expression3175 = new BitSet(new long[]{0x0000000000000000L,0x0100000000000000L});
	public static final BitSet FOLLOW_120_in_null_comparison_expression3179 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_empty_collection_comparison_expression3190 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_108_in_empty_collection_comparison_expression3192 = new BitSet(new long[]{0x0000000080000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_NOT_in_empty_collection_comparison_expression3195 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_98_in_empty_collection_comparison_expression3199 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_entity_or_value_expression_in_collection_member_expression3210 = new BitSet(new long[]{0x0000000080000000L,0x0004000000000000L});
	public static final BitSet FOLLOW_NOT_in_collection_member_expression3214 = new BitSet(new long[]{0x0000000000000000L,0x0004000000000000L});
	public static final BitSet FOLLOW_114_in_collection_member_expression3218 = new BitSet(new long[]{0x2000000000040000L,0x2000000000000000L});
	public static final BitSet FOLLOW_125_in_collection_member_expression3221 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_path_expression_in_collection_member_expression3225 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_entity_or_value_expression3236 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simple_entity_or_value_expression_in_entity_or_value_expression3244 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subquery_in_entity_or_value_expression3252 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identification_variable_in_simple_entity_or_value_expression3263 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_input_parameter_in_simple_entity_or_value_expression3271 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_literal_in_simple_entity_or_value_expression3279 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NOT_in_exists_expression3291 = new BitSet(new long[]{0x0000000000000000L,0x0000004000000000L});
	public static final BitSet FOLLOW_102_in_exists_expression3295 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_subquery_in_exists_expression3297 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_all_or_any_expression3308 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_subquery_in_all_or_any_expression3321 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_expression_in_comparison_expression3332 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001F80L,0x0000000000000001L});
	public static final BitSet FOLLOW_comparison_operator_in_comparison_expression3335 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801CC82000L,0x0000000000001250L});
	public static final BitSet FOLLOW_128_in_comparison_expression3339 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801CC82000L,0x0000000000001250L});
	public static final BitSet FOLLOW_string_expression_in_comparison_expression3343 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_all_or_any_expression_in_comparison_expression3347 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_boolean_expression_in_comparison_expression3356 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000600L});
	public static final BitSet FOLLOW_set_in_comparison_expression3358 = new BitSet(new long[]{0xA000000048040200L,0x020002800CC82000L,0x00000000000C0010L});
	public static final BitSet FOLLOW_boolean_expression_in_comparison_expression3367 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_all_or_any_expression_in_comparison_expression3371 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enum_expression_in_comparison_expression3380 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000600L});
	public static final BitSet FOLLOW_set_in_comparison_expression3382 = new BitSet(new long[]{0xA000000048040200L,0x0200000008C02000L,0x0000000000000010L});
	public static final BitSet FOLLOW_enum_expression_in_comparison_expression3389 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_all_or_any_expression_in_comparison_expression3393 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_datetime_expression_in_comparison_expression3402 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001F80L});
	public static final BitSet FOLLOW_comparison_operator_in_comparison_expression3404 = new BitSet(new long[]{0xA000008078040A80L,0x02000280ECC82000L,0x0000000000000010L});
	public static final BitSet FOLLOW_datetime_expression_in_comparison_expression3407 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_all_or_any_expression_in_comparison_expression3411 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_entity_expression_in_comparison_expression3420 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000600L});
	public static final BitSet FOLLOW_set_in_comparison_expression3422 = new BitSet(new long[]{0xA000000040040000L,0x0000000000C02000L,0x0000000000000010L});
	public static final BitSet FOLLOW_entity_expression_in_comparison_expression3431 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_all_or_any_expression_in_comparison_expression3435 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_entity_type_expression_in_comparison_expression3444 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000600L});
	public static final BitSet FOLLOW_set_in_comparison_expression3446 = new BitSet(new long[]{0xA000000040000000L,0x0000000000002000L,0x0000000000000400L});
	public static final BitSet FOLLOW_entity_type_expression_in_comparison_expression3454 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arithmetic_expression_in_comparison_expression3462 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001F80L});
	public static final BitSet FOLLOW_comparison_operator_in_comparison_expression3464 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800CE8204AL,0x0000000000000038L});
	public static final BitSet FOLLOW_arithmetic_expression_in_comparison_expression3467 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_all_or_any_expression_in_comparison_expression3471 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arithmetic_term_in_arithmetic_expression3535 = new BitSet(new long[]{0x0000000000000000L,0x000000000000000AL});
	public static final BitSet FOLLOW_set_in_arithmetic_expression3538 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_term_in_arithmetic_expression3546 = new BitSet(new long[]{0x0000000000000002L,0x000000000000000AL});
	public static final BitSet FOLLOW_arithmetic_term_in_arithmetic_expression3556 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arithmetic_factor_in_arithmetic_term3567 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000021L});
	public static final BitSet FOLLOW_set_in_arithmetic_term3570 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_factor_in_arithmetic_term3579 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000021L});
	public static final BitSet FOLLOW_arithmetic_factor_in_arithmetic_term3589 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arithmetic_primary_in_arithmetic_factor3612 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_arithmetic_primary3623 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_decimal_literal_in_arithmetic_primary3631 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numeric_literal_in_arithmetic_primary3639 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_arithmetic_primary3647 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_arithmetic_primary3648 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_arithmetic_primary3649 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_input_parameter_in_arithmetic_primary3657 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_functions_returning_numerics_in_arithmetic_primary3665 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_in_arithmetic_primary3673 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_case_expression_in_arithmetic_primary3681 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_function_invocation_in_arithmetic_primary3689 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_extension_functions_in_arithmetic_primary3697 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subquery_in_arithmetic_primary3705 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_string_expression3716 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_literal_in_string_expression3724 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_input_parameter_in_string_expression3732 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_functions_returning_strings_in_string_expression3740 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_in_string_expression3748 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_case_expression_in_string_expression3756 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_function_invocation_in_string_expression3764 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_extension_functions_in_string_expression3772 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subquery_in_string_expression3780 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_datetime_expression3791 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_input_parameter_in_datetime_expression3799 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_functions_returning_datetime_in_datetime_expression3807 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_in_datetime_expression3815 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_case_expression_in_datetime_expression3823 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_function_invocation_in_datetime_expression3831 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_extension_functions_in_datetime_expression3839 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_date_time_timestamp_literal_in_datetime_expression3847 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subquery_in_datetime_expression3855 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_boolean_expression3866 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_boolean_literal_in_boolean_expression3874 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_input_parameter_in_boolean_expression3882 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_case_expression_in_boolean_expression3890 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_function_invocation_in_boolean_expression3898 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_extension_functions_in_boolean_expression3906 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subquery_in_boolean_expression3914 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_enum_expression3925 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enum_literal_in_enum_expression3933 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_input_parameter_in_enum_expression3941 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_case_expression_in_enum_expression3949 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subquery_in_enum_expression3957 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_entity_expression3968 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simple_entity_expression_in_entity_expression3976 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identification_variable_in_simple_entity_expression3987 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_input_parameter_in_simple_entity_expression3995 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_discriminator_in_entity_type_expression4006 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_entity_type_literal_in_entity_type_expression4014 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_input_parameter_in_entity_type_expression4022 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_138_in_type_discriminator4033 = new BitSet(new long[]{0xA000000040040000L,0x0000200000002000L,0x0000000000004000L});
	public static final BitSet FOLLOW_general_identification_variable_in_type_discriminator4036 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_path_expression_in_type_discriminator4040 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_input_parameter_in_type_discriminator4044 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_type_discriminator4047 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_111_in_functions_returning_numerics4058 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_functions_returning_numerics4059 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_functions_returning_numerics4060 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_113_in_functions_returning_numerics4068 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_functions_returning_numerics4070 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_functions_returning_numerics4071 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_functions_returning_numerics4073 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_functions_returning_numerics4075 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_functions_returning_numerics4076 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_functions_returning_numerics4079 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_85_in_functions_returning_numerics4087 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_functions_returning_numerics4088 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_functions_returning_numerics4089 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_133_in_functions_returning_numerics4097 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_functions_returning_numerics4098 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_functions_returning_numerics4099 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_116_in_functions_returning_numerics4107 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_functions_returning_numerics4108 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_functions_returning_numerics4109 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_functions_returning_numerics4111 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_functions_returning_numerics4112 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_131_in_functions_returning_numerics4120 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_path_expression_in_functions_returning_numerics4121 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_functions_returning_numerics4122 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_107_in_functions_returning_numerics4130 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_identification_variable_in_functions_returning_numerics4131 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_functions_returning_numerics4132 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_92_in_functions_returning_strings4170 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_functions_returning_strings4171 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_functions_returning_strings4172 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_functions_returning_strings4174 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_functions_returning_strings4177 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_functions_returning_strings4179 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_RPAREN_in_functions_returning_strings4182 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_134_in_functions_returning_strings4190 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_functions_returning_strings4192 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_functions_returning_strings4193 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_functions_returning_strings4195 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_functions_returning_strings4198 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_functions_returning_strings4200 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_functions_returning_strings4203 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_137_in_functions_returning_strings4211 = new BitSet(new long[]{0xA00002C07C040A80L,0x020043801E082000L,0x00000000000012C0L});
	public static final BitSet FOLLOW_trim_specification_in_functions_returning_strings4214 = new BitSet(new long[]{0x0000020000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_trim_character_in_functions_returning_strings4219 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_104_in_functions_returning_strings4223 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_functions_returning_strings4227 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_functions_returning_strings4229 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LOWER_in_functions_returning_strings4237 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_functions_returning_strings4239 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_functions_returning_strings4240 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_functions_returning_strings4241 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_140_in_functions_returning_strings4249 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_functions_returning_strings4250 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_functions_returning_strings4251 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_105_in_function_invocation4281 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_function_name_in_function_invocation4282 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_function_invocation4285 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_function_arg_in_function_invocation4287 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_RPAREN_in_function_invocation4291 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_literal_in_function_arg4302 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_function_arg4310 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_input_parameter_in_function_arg4318 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_scalar_expression_in_function_arg4326 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_general_case_expression_in_case_expression4337 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simple_case_expression_in_case_expression4345 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_coalesce_expression_in_case_expression4353 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_nullif_expression_in_case_expression4361 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CASE_in_general_case_expression4372 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_when_clause_in_general_case_expression4374 = new BitSet(new long[]{0x1000000000004000L});
	public static final BitSet FOLLOW_when_clause_in_general_case_expression4377 = new BitSet(new long[]{0x1000000000004000L});
	public static final BitSet FOLLOW_ELSE_in_general_case_expression4381 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_scalar_expression_in_general_case_expression4383 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_END_in_general_case_expression4385 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WHEN_in_when_clause4396 = new BitSet(new long[]{0xA00000C0FC440A80L,0x02128AC0FC3FE04AL,0x00000000000C1768L});
	public static final BitSet FOLLOW_conditional_expression_in_when_clause4398 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_THEN_in_when_clause4400 = new BitSet(new long[]{0xA00000C07C440A80L,0x03128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_scalar_expression_in_when_clause4403 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_120_in_when_clause4407 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CASE_in_simple_case_expression4419 = new BitSet(new long[]{0x2000000000040000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_case_operand_in_simple_case_expression4421 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_simple_when_clause_in_simple_case_expression4423 = new BitSet(new long[]{0x1000000000004000L});
	public static final BitSet FOLLOW_simple_when_clause_in_simple_case_expression4426 = new BitSet(new long[]{0x1000000000004000L});
	public static final BitSet FOLLOW_ELSE_in_simple_case_expression4430 = new BitSet(new long[]{0xA00000C07C440A80L,0x03128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_scalar_expression_in_simple_case_expression4433 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_120_in_simple_case_expression4437 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_END_in_simple_case_expression4440 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_case_operand4451 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_discriminator_in_case_operand4459 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WHEN_in_simple_when_clause4470 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_scalar_expression_in_simple_when_clause4472 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_THEN_in_simple_when_clause4474 = new BitSet(new long[]{0xA00000C07C440A80L,0x03128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_scalar_expression_in_simple_when_clause4477 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_120_in_simple_when_clause4481 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_91_in_coalesce_expression4493 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_scalar_expression_in_coalesce_expression4494 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_coalesce_expression4497 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_scalar_expression_in_coalesce_expression4499 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_RPAREN_in_coalesce_expression4502 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_121_in_nullif_expression4513 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_scalar_expression_in_nullif_expression4514 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_nullif_expression4516 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_scalar_expression_in_nullif_expression4518 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_nullif_expression4519 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_90_in_extension_functions4531 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_function_arg_in_extension_functions4533 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_WORD_in_extension_functions4535 = new BitSet(new long[]{0x0000000808000000L});
	public static final BitSet FOLLOW_LPAREN_in_extension_functions4538 = new BitSet(new long[]{0x0000000000400000L});
	public static final BitSet FOLLOW_INT_NUMERAL_in_extension_functions4539 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_66_in_extension_functions4542 = new BitSet(new long[]{0x0000000000400000L});
	public static final BitSet FOLLOW_INT_NUMERAL_in_extension_functions4544 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_RPAREN_in_extension_functions4549 = new BitSet(new long[]{0x0000000808000000L});
	public static final BitSet FOLLOW_RPAREN_in_extension_functions4553 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_extract_function_in_extension_functions4561 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enum_function_in_extension_functions4569 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_103_in_extract_function4581 = new BitSet(new long[]{0x0000000000000000L,0x8028041100000000L,0x0000000000028002L});
	public static final BitSet FOLLOW_date_part_in_extract_function4583 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_104_in_extract_function4585 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_function_arg_in_extract_function4587 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_extract_function4589 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_83_in_enum_function4601 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_enum_function4603 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_enum_value_literal_in_enum_function4605 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_enum_function4607 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_77_in_input_parameter4674 = new BitSet(new long[]{0x0000000000400000L,0x0000000000000040L});
	public static final BitSet FOLLOW_numeric_literal_in_input_parameter4676 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NAMED_PARAMETER_in_input_parameter4699 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_63_in_input_parameter4720 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_WORD_in_input_parameter4722 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000100000L});
	public static final BitSet FOLLOW_148_in_input_parameter4724 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_literal4752 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_constructor_name4764 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_constructor_name4767 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_WORD_in_constructor_name4770 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
	public static final BitSet FOLLOW_WORD_in_enum_literal4784 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_field4817 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_130_in_field4821 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_104_in_field4825 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GROUP_in_field4829 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ORDER_in_field4833 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_MAX_in_field4837 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_MIN_in_field4841 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUM_in_field4845 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AVG_in_field4849 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COUNT_in_field4853 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AS_in_field4857 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_114_in_field4861 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CASE_in_field4865 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_124_in_field4873 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SET_in_field4877 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DESC_in_field4881 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ASC_in_field4885 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_date_part_in_field4889 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_parameter_name4917 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_parameter_name4920 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_WORD_in_parameter_name4923 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
	public static final BitSet FOLLOW_TRIM_CHARACTER_in_trim_character4953 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_string_literal4964 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_70_in_numeric_literal4976 = new BitSet(new long[]{0x0000000000400000L});
	public static final BitSet FOLLOW_INT_NUMERAL_in_numeric_literal4980 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INT_NUMERAL_in_decimal_literal4992 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_decimal_literal4994 = new BitSet(new long[]{0x0000000000400000L});
	public static final BitSet FOLLOW_INT_NUMERAL_in_decimal_literal4996 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_single_valued_object_field5007 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_single_valued_embeddable_object_field5018 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_collection_valued_field5029 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_entity_name5040 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_subtype5051 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_entity_type_literal5062 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_function_name5073 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_state_field5084 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_result_variable5095 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_superquery_identification_variable5106 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_date_time_timestamp_literal5117 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_literal_in_pattern_value5128 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_input_parameter_in_collection_valued_input_parameter5139 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_input_parameter_in_single_valued_input_parameter5150 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_enum_value_field5161 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_130_in_enum_value_field5165 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_104_in_enum_value_field5169 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GROUP_in_enum_value_field5173 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ORDER_in_enum_value_field5177 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_MAX_in_enum_value_field5181 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_MIN_in_enum_value_field5185 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUM_in_enum_value_field5189 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AVG_in_enum_value_field5193 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COUNT_in_enum_value_field5197 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AS_in_enum_value_field5201 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_114_in_enum_value_field5205 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CASE_in_enum_value_field5209 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_124_in_enum_value_field5217 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SET_in_enum_value_field5221 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DESC_in_enum_value_field5225 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ASC_in_enum_value_field5229 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_118_in_enum_value_field5233 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_date_part_in_enum_value_field5237 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WORD_in_enum_value_literal5248 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_enum_value_literal5251 = new BitSet(new long[]{0x200000A230041AE0L,0x906C051100000000L,0x0000000000028006L});
	public static final BitSet FOLLOW_enum_value_field_in_enum_value_literal5254 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
	public static final BitSet FOLLOW_field_in_synpred21_JPA2972 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_field_in_synpred30_JPA21162 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_scalar_expression_in_synpred33_JPA21288 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simple_entity_expression_in_synpred34_JPA21296 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_synpred43_JPA21421 = new BitSet(new long[]{0x0000000000000002L,0x000000000000002BL});
	public static final BitSet FOLLOW_set_in_synpred43_JPA21424 = new BitSet(new long[]{0xA00000C07C440A80L,0x02128A80FC28204AL,0x00000000000C1668L});
	public static final BitSet FOLLOW_scalar_expression_in_synpred43_JPA21440 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identification_variable_in_synpred44_JPA21450 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_scalar_expression_in_synpred45_JPA21468 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_in_synpred46_JPA21476 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_synpred49_JPA21533 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_scalar_expression_in_synpred50_JPA21541 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_in_synpred51_JPA21549 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_function_name_in_synpred53_JPA21568 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_synpred53_JPA21570 = new BitSet(new long[]{0xA000008078442A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_DISTINCT_in_synpred53_JPA21572 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_synpred53_JPA21576 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_synpred53_JPA21577 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COUNT_in_synpred55_JPA21611 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_LPAREN_in_synpred55_JPA21613 = new BitSet(new long[]{0x2000000000042000L});
	public static final BitSet FOLLOW_DISTINCT_in_synpred55_JPA21615 = new BitSet(new long[]{0x2000000000040000L});
	public static final BitSet FOLLOW_count_argument_in_synpred55_JPA21619 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_synpred55_JPA21621 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_synpred67_JPA21892 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_general_identification_variable_in_synpred68_JPA21896 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_result_variable_in_synpred69_JPA21900 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_scalar_expression_in_synpred70_JPA21904 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_general_derived_path_in_synpred81_JPA22114 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_68_in_synpred81_JPA22115 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_single_valued_object_field_in_synpred81_JPA22116 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_synpred86_JPA22268 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_scalar_expression_in_synpred87_JPA22276 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_in_synpred88_JPA22284 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arithmetic_expression_in_synpred89_JPA22303 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_expression_in_synpred90_JPA22311 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enum_expression_in_synpred91_JPA22319 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_datetime_expression_in_synpred92_JPA22327 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_boolean_expression_in_synpred93_JPA22335 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_case_expression_in_synpred94_JPA22343 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NOT_in_synpred97_JPA22403 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simple_cond_expression_in_synpred98_JPA22418 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_comparison_expression_in_synpred99_JPA22455 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_between_expression_in_synpred100_JPA22463 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_in_expression_in_synpred101_JPA22471 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_like_expression_in_synpred102_JPA22479 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_null_comparison_expression_in_synpred103_JPA22487 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_empty_collection_comparison_expression_in_synpred104_JPA22495 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_collection_member_expression_in_synpred105_JPA22503 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arithmetic_expression_in_synpred144_JPA22907 = new BitSet(new long[]{0x0000000080000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_NOT_in_synpred144_JPA22910 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_88_in_synpred144_JPA22914 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_synpred144_JPA22916 = new BitSet(new long[]{0x0000000000000010L});
	public static final BitSet FOLLOW_AND_in_synpred144_JPA22918 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_synpred144_JPA22920 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_expression_in_synpred146_JPA22928 = new BitSet(new long[]{0x0000000080000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_NOT_in_synpred146_JPA22931 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_88_in_synpred146_JPA22935 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_synpred146_JPA22937 = new BitSet(new long[]{0x0000000000000010L});
	public static final BitSet FOLLOW_AND_in_synpred146_JPA22939 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801C082000L,0x0000000000001240L});
	public static final BitSet FOLLOW_string_expression_in_synpred146_JPA22941 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_expression_in_synpred160_JPA23134 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_pattern_value_in_synpred161_JPA23138 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_synpred163_JPA23161 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identification_variable_in_synpred171_JPA23263 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_expression_in_synpred178_JPA23332 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001F80L,0x0000000000000001L});
	public static final BitSet FOLLOW_comparison_operator_in_synpred178_JPA23335 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801CC82000L,0x0000000000001250L});
	public static final BitSet FOLLOW_128_in_synpred178_JPA23339 = new BitSet(new long[]{0xA00000C07C040A80L,0x020002801CC82000L,0x0000000000001250L});
	public static final BitSet FOLLOW_string_expression_in_synpred178_JPA23343 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_all_or_any_expression_in_synpred178_JPA23347 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_boolean_expression_in_synpred181_JPA23356 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000600L});
	public static final BitSet FOLLOW_set_in_synpred181_JPA23358 = new BitSet(new long[]{0xA000000048040200L,0x020002800CC82000L,0x00000000000C0010L});
	public static final BitSet FOLLOW_boolean_expression_in_synpred181_JPA23367 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_all_or_any_expression_in_synpred181_JPA23371 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enum_expression_in_synpred184_JPA23380 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000600L});
	public static final BitSet FOLLOW_set_in_synpred184_JPA23382 = new BitSet(new long[]{0xA000000048040200L,0x0200000008C02000L,0x0000000000000010L});
	public static final BitSet FOLLOW_enum_expression_in_synpred184_JPA23389 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_all_or_any_expression_in_synpred184_JPA23393 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_datetime_expression_in_synpred186_JPA23402 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001F80L});
	public static final BitSet FOLLOW_comparison_operator_in_synpred186_JPA23404 = new BitSet(new long[]{0xA000008078040A80L,0x02000280ECC82000L,0x0000000000000010L});
	public static final BitSet FOLLOW_datetime_expression_in_synpred186_JPA23407 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_all_or_any_expression_in_synpred186_JPA23411 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_entity_expression_in_synpred189_JPA23420 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000600L});
	public static final BitSet FOLLOW_set_in_synpred189_JPA23422 = new BitSet(new long[]{0xA000000040040000L,0x0000000000C02000L,0x0000000000000010L});
	public static final BitSet FOLLOW_entity_expression_in_synpred189_JPA23431 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_all_or_any_expression_in_synpred189_JPA23435 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_entity_type_expression_in_synpred191_JPA23444 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000600L});
	public static final BitSet FOLLOW_set_in_synpred191_JPA23446 = new BitSet(new long[]{0xA000000040000000L,0x0000000000002000L,0x0000000000000400L});
	public static final BitSet FOLLOW_entity_type_expression_in_synpred191_JPA23454 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arithmetic_term_in_synpred200_JPA23535 = new BitSet(new long[]{0x0000000000000000L,0x000000000000000AL});
	public static final BitSet FOLLOW_set_in_synpred200_JPA23538 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_term_in_synpred200_JPA23546 = new BitSet(new long[]{0x0000000000000002L,0x000000000000000AL});
	public static final BitSet FOLLOW_arithmetic_factor_in_synpred203_JPA23567 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000021L});
	public static final BitSet FOLLOW_set_in_synpred203_JPA23570 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_factor_in_synpred203_JPA23579 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000021L});
	public static final BitSet FOLLOW_decimal_literal_in_synpred207_JPA23631 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numeric_literal_in_synpred208_JPA23639 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_synpred209_JPA23647 = new BitSet(new long[]{0xA000008078440A80L,0x02128A800C28204AL,0x0000000000000028L});
	public static final BitSet FOLLOW_arithmetic_expression_in_synpred209_JPA23648 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_RPAREN_in_synpred209_JPA23649 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_in_synpred212_JPA23673 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_function_invocation_in_synpred214_JPA23689 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_in_synpred220_JPA23748 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_function_invocation_in_synpred222_JPA23764 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_synpred224_JPA23791 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aggregate_expression_in_synpred227_JPA23815 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_function_invocation_in_synpred229_JPA23831 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_date_time_timestamp_literal_in_synpred231_JPA23847 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_literal_in_synpred269_JPA24302 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_path_expression_in_synpred270_JPA24310 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_input_parameter_in_synpred271_JPA24318 = new BitSet(new long[]{0x0000000000000002L});

    // CAUTION: inserted manually, when regenerating the lexer, do not forget to insert
    @Override
    public void emitErrorMessage(String msg) {
        //do nothing
    }

    @Override
    protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
        throw new MismatchedTokenException(ttype, input);
    }
}
