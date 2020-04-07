
/**
 * Author: Carter Mooring
 * Homework: #4
 * File: Parser.java
 * 
 * Recursive descent parser implementation for MyPL. The parser
 * requires a lexer. Once a parser is created, the parse() method
 * ensures the given program is syntactically correct. 
 */

import java.lang.invoke.CallSite;
import java.util.*;

public class Parser {

  private Lexer lexer;
  private Token currToken = null;
  private boolean debug_flag = false; // set to false to remove debug comments

  /**
   * Create a new parser over the given lexer.
   */
  public Parser(Lexer lexer) {
    this.lexer = lexer;
  }

  //Ensures program is syntactically correct. On error, throws a MyPLException.
  public StmtList parse() throws MyPLException {
    StmtList stmtListNode = new StmtList(); //create StmtList node 
    advance();        //initialize the lexer
    stmts(stmtListNode); //descend into stmts pass in node 
    eat(TokenType.EOS, "End of file expected");
    return stmtListNode; //return AST root node, returns the object 
  }

  /* Helper Functions */

  // sets current token to next token in stream
  private void advance() throws MyPLException {
    currToken = lexer.nextToken();
  }

  // checks that current token matches given type and advances,
  // otherwise creates an error with the given error message
  private void eat(TokenType t, String errmsg) throws MyPLException {
    if (currToken.type() == t) {
      advance();
    } else {
      error(errmsg);
    }
  }

  // generates an error message from the given message and throws a
  // corresponding MyPLException
  private void error(String errmsg) throws MyPLException {
    String s = errmsg + " found '" + currToken.lexeme() + "'";
    int row = currToken.row();
    int col = currToken.column();
    throw new MyPLException("Parser", errmsg, row, col);
  }

  // function to print a debug string if the debug_flag is set for
  // helping to diagnose/test the parser
  private void debug(String msg) {
    if (debug_flag)
      System.out.println(msg);
  }

  /* Recursive Descent Functions */
  /*AST Functions*/

  // <stmts> ::= <stmt> <stmts> | epsilon
  private void stmts(StmtList stmtListNode) throws MyPLException {
    debug("<stmts>");
    if (currToken.type() != TokenType.EOS){
      stmt(stmtListNode);
      stmts(stmtListNode);
    }
  }

  // <bstmts> ::= <bstmt> <bstmts> | epsilon
  private void bstmts(StmtList stmtListNode) throws MyPLException {
    debug("<bstmts>");
    if (currToken.type() == TokenType.VAR || currToken.type() == TokenType.SET || currToken.type() == TokenType.IF
        || currToken.type() == TokenType.WHILE || currToken.type() == TokenType.FOR
        || currToken.type() == TokenType.RETURN || currToken.type() == TokenType.NOT
        || currToken.type() == TokenType.LPAREN || isRPVal(currToken.type())) {
      stmtListNode.stmts.add(bstmt());
      bstmts(stmtListNode);
    }
  }

  // <stmt> ::= ⟨tdecl⟩ | ⟨fdecl⟩ | ⟨bstmt⟩
  private void stmt(StmtList stmtListNode) throws MyPLException {
    debug("<stmt>");
    if (currToken.type() == TokenType.TYPE) {
      tdecl(stmtListNode);
    } else if (currToken.type() == TokenType.FUN) {
      fdecl(stmtListNode);
    } else {
      stmtListNode.stmts.add(bstmt());
    }
  }

  // <bstmt> ::= ⟨vdecl⟩ | ⟨assign⟩ | ⟨cond⟩ | ⟨while⟩ | ⟨for⟩ | ⟨expr⟩ | ⟨exit⟩
  private Stmt bstmt() throws MyPLException {
    debug("<bstmt>");
    if (currToken.type() == TokenType.VAR) {
      return vdecl();
    } else if (currToken.type() == TokenType.SET) {
      return assign();
    } else if (currToken.type() == TokenType.IF) {
      return cond();
    } else if (currToken.type() == TokenType.WHILE) {
      return whileLoop();
    } else if (currToken.type() == TokenType.FOR) {
      return forLoop();
    } else if (currToken.type() == TokenType.RETURN) {
      return exit();
    } else {
      // Expr exprNode = new Expr();
      // exprNode.rest = expr();
      return expr();
    }
  }

  // <tdecl> ::= TYPE ID ⟨vdecls⟩ END
  private void tdecl(StmtList stmtListNode) throws MyPLException {
    debug("<tdecl>");
    TypeDeclStmt typeNode = new TypeDeclStmt(); //create typedecl node
    eat(TokenType.TYPE, "A Type (string, etc) is expected");
    typeNode.typeId = currToken;
    eat(TokenType.ID, "Identifier (ID) expected");
    vdecls(typeNode);
    eat(TokenType.END, "End (end) is expected");
    stmtListNode.stmts.add(typeNode);
  }

  // <vdecls> ::= ⟨vdecl⟩ ⟨vdecls⟩ | ε
  private void vdecls(TypeDeclStmt typeNode) throws MyPLException {
    debug("<vdecls>");
    if (currToken.type() == TokenType.VAR) {
      typeNode.fields.add(vdecl());
      vdecls(typeNode);
    }
  }

  // <fdecl> ::= FUN ( ⟨dtype ⟩ | NIL ) ID LPAREN ⟨params⟩ RPAREN ⟨bstmts ⟩ END
  private void fdecl(StmtList stmtListNode) throws MyPLException {
    debug("<fdecl>");
    FunDeclStmt funDeclNode = new FunDeclStmt();
    eat(TokenType.FUN, "Function (fun) expected");
    if (isType(currToken.type())) {
      funDeclNode.returnType = currToken;
      advance();
    } else{
      funDeclNode.returnType = currToken;
      eat(TokenType.NIL, "Function type ('int', 'nil', etc) expected");
    } 
    funDeclNode.funName = currToken;
    eat(TokenType.ID, "Identifier (ID) is expected");
    eat(TokenType.LPAREN, "Left parthesis '(' expected");
    params(funDeclNode);
    eat(TokenType.RPAREN, "Right parethesis ')' expected");
    bstmts(funDeclNode.stmtList);
    eat(TokenType.END, "End (end) is expected");
    stmtListNode.stmts.add(funDeclNode);
  }

  // <params> ::= ⟨dtype⟩ ID ( COMMA ⟨dtype⟩ ID )∗ | ε
  private void params(FunDeclStmt funDeclNode) throws MyPLException {
    debug("<params>");
    if (isType(currToken.type())) {
      FunParam funNode = new FunParam();
      funNode.paramType = currToken;
      dtype();
      funNode.paramName = currToken;
      eat(TokenType.ID, "Identifier (ID) expected");
      funDeclNode.params.add(funNode);
      while (currToken.type() == TokenType.COMMA) {
        FunParam funNode2 = new FunParam();
        eat(TokenType.COMMA, "Comma (,) expected");
        funNode2.paramType = currToken;
        dtype();
        funNode2.paramName = currToken;
        eat(TokenType.ID, "Identifier (ID) expected");
        funDeclNode.params.add(funNode2);
      }
    }
  }

  // <dtype> ::= INT_TYPE | DOUBLE_TYPE | BOOL_TYPE | CHAR_TYPE | STRING_TYPE | ID
  private void dtype() throws MyPLException {
    debug("<dtype>");
    if (isType(currToken.type())){
      advance();
    }else{
      error("A Type (string, etc) is expected");
    }
  }

  // <exit> ::= RETURN ( ⟨expr⟩ | ε )
  private Stmt exit() throws MyPLException {
    debug("<exit>");
    ReturnStmt returnNode = new ReturnStmt();
    returnNode.returnToken = currToken;
    eat(TokenType.RETURN, "A Return (return) is expected");
    if (currToken.type() != TokenType.END) {
      if(currToken.type() != TokenType.RETURN){
        returnNode.returnExpr = expr();
      }
    }
    return returnNode;
  }

  // <vdecl> ::= VAR (⟨dtype⟩ | ε) ID ASSIGN ⟨expr⟩
  private VarDeclStmt vdecl() throws MyPLException {
    debug("<vdecl>");
    VarDeclStmt varDeclNode = new VarDeclStmt();
    eat(TokenType.VAR, "Variable (13, etc) expected");
    Token save = currToken;
    advance();
    if(currToken.type() != TokenType.ID){
      varDeclNode.varId = save;
    }else{
      varDeclNode.varId = currToken;
      varDeclNode.varType = save;
      eat(TokenType.ID, "Identifier (ID) expected");
    }
    eat(TokenType.ASSIGN, "Assignment operator (:=) expected"); // ASSIGN
    varDeclNode.varExpr = expr(); // <expr>
    return varDeclNode;
  }

  // <assign> ::= SET ⟨lvalue ⟩ ASSIGN ⟨expr ⟩
  private AssignStmt assign() throws MyPLException {
    debug("<assign>");
    AssignStmt assignNode = new AssignStmt(); //create assign node 
    eat(TokenType.SET, "Set (set) is expected");  //dont care 
    lvalue(assignNode);  //lhs contains return of lvalue
    eat(TokenType.ASSIGN, "Assignment operator (:=) expected");
    assignNode.rhs = expr();      //add expr return to the rhs
    return assignNode;
  }

  // <lvalue> ::= ID(DOT ID)∗
  private void lvalue(AssignStmt assignNode) throws MyPLException {
    debug("<lvalue>");
    LValue lvalNode = new LValue();
    lvalNode.path.add(currToken);
    eat(TokenType.ID, "Identifier (ID) expected");
    while (currToken.type() == TokenType.DOT) {
      advance();
      lvalNode.path.add(currToken);
      eat(TokenType.ID, "Identifier (ID) expected");
    }
    assignNode.lhs = lvalNode;
  }

  // <cond> ::= IF ⟨expr ⟩ THEN ⟨bstmts ⟩ ⟨condt ⟩ END
  private IfStmt cond() throws MyPLException {
    debug("<cond>");
    IfStmt ifNode = new IfStmt();
    eat(TokenType.IF, "Star of if loop (if) expected"); //ignore if
    ifNode.ifPart.boolExpr = expr();  
    eat(TokenType.THEN, "Then (then) is expected after the expression");  //ignore then
    bstmts(ifNode.ifPart.stmtList);
    condt(ifNode);
    eat(TokenType.END, "End (end) is expected to close the loop");
    return ifNode;
  }

  // <condt> ::= ELIF ⟨expr ⟩ THEN ⟨bstmts ⟩ ⟨condt ⟩ | ELSE ⟨bstmts ⟩ | ε
  private void condt(IfStmt ifNode) throws MyPLException {
    debug("<condt>");
    BasicIf basicNode = new BasicIf();
    if (currToken.type() == TokenType.ELIF) {
      eat(TokenType.ELIF, "Else if (elif) is expected");
      basicNode.boolExpr = expr();
      eat(TokenType.THEN, "Then (then) is expected after the expression");
      bstmts(basicNode.stmtList);
      ifNode.elsifs.add(basicNode);
      condt(ifNode);
    } else if (currToken.type() == TokenType.ELSE) {
      ifNode.hasElse = true;
      advance();
      bstmts(ifNode.elseStmtList);
    }
  }

  // <whileLoop> ::= WHILE ⟨expr ⟩ DO ⟨bstmts ⟩ END
  private WhileStmt whileLoop() throws MyPLException {
    debug("<whileLoop>");
    WhileStmt whileNode = new WhileStmt();
    eat(TokenType.WHILE, "Start to While loop (while) is expected");
    whileNode.boolExpr = expr();
    eat(TokenType.DO, "Do (do) is expected after the expression");
    bstmts(whileNode.stmtList);
    eat(TokenType.END, "End (end) is expected to close while loop");
    return whileNode;
  }

  // <forLoop> ::= FOR ID ASSIGN ⟨expr ⟩ TO ⟨expr ⟩ DO ⟨bstmts ⟩ END
  private ForStmt forLoop() throws MyPLException {
    debug("<forLoop>");
    ForStmt forNode = new ForStmt();
    eat(TokenType.FOR, "For (for) is expected to start for loop");
    forNode.var = currToken;
    eat(TokenType.ID, "Identifier (ID) expected");
    eat(TokenType.ASSIGN, "Assignment operator (:=) expected");
    forNode.startExpr = expr();
    eat(TokenType.TO, "To (to) is expected after the expression");
    forNode.endExpr = expr();
    eat(TokenType.DO, "Do (do) is expected after the expression");
    bstmts(forNode.stmtList);
    eat(TokenType.END, "End (end) is expected to close for loop");
    return forNode;
  }

  // <expr> ::= ( ⟨rvalue⟩ | NOT ⟨expr⟩ | LPAREN ⟨expr⟩ RPAREN ) (
  // ⟨operator⟩⟨expr⟩ | ε )
  private Expr expr() throws MyPLException {
    debug("<expr>");
    Expr expr = new Expr();
    ComplexTerm comp = new ComplexTerm();
    if (currToken.type() == TokenType.NOT) {
      advance();
      expr = expr();
      expr.negated = true;
      return expr;
    } else if (currToken.type() == TokenType.LPAREN) {
      advance();
      comp.expr = expr(); //complex if parenthesis
      expr.first = comp;
      eat(TokenType.RPAREN, "Right parethesis ')' is expected");
    } else {
      SimpleTerm simple = new SimpleTerm();
      simple.rvalue = rvalue(); //simple case
      expr.first = simple;
    }
    if (isOp(currToken.type())) {
      expr.operator = currToken;
      advance();
      expr.rest = expr();
    }
    return expr;
  }

  // <isOp> ::= PLUS | MINUS | DIVIDE | MULTIPLY | MODULO | AND | OR | EQUAL |
  // LESS_THAN | GREATER_THAN | LESS_THAN_EQUAL | GREATER_THAN_EQUAL | NOT_EQUAL
  private boolean isOp(TokenType t) {
    debug("<isOp>");
    Set<TokenType> s = Set.of(TokenType.PLUS, TokenType.MINUS, TokenType.DIVIDE, TokenType.MULTIPLY, TokenType.MODULO,
        TokenType.AND, TokenType.OR, TokenType.EQUAL, TokenType.LESS_THAN, TokenType.GREATER_THAN,
        TokenType.LESS_THAN_EQUAL, TokenType.GREATER_THAN_EQUAL, TokenType.NOT_EQUAL);
    return s.contains(t);
  }

  // <rvalue> ::= ⟨pval⟩ | NIL | NEW ID | ⟨idrval⟩ | NEG ⟨expr⟩
  private RValue rvalue() throws MyPLException {
    debug("<rvalue>");
    SimpleRValue simpleNode = new SimpleRValue();
    if (currToken.type() == TokenType.NIL) {
      simpleNode.val = currToken;
      advance();
      return simpleNode;
    } else if (currToken.type() == TokenType.NEW) {
      NewRValue newRValNode = new NewRValue();
      advance();
      newRValNode.typeId = currToken;
      eat(TokenType.ID, "Identifier (ID) expected");
      return newRValNode;
    } else if (currToken.type() == TokenType.NEG) {
      NegatedRValue negNode = new NegatedRValue();
      advance();
      negNode.expr = expr();
      return negNode;
    } else if(isVal(currToken.type())) {
      simpleNode.val = currToken;
      pval();
      return simpleNode;
    } else {
      return idrval();
    }
  }

  // <pval> ::= INT_VAL | DOUBLE_VAL | BOOL_VAL | CHAR_VAL | STRING_VAL
  private void pval() throws MyPLException {
    debug("<pval>");
    Set<TokenType> s = Set.of(TokenType.INT_VAL, TokenType.DOUBLE_VAL, TokenType.BOOL_VAL, TokenType.CHAR_VAL,
        TokenType.STRING_VAL);

    if (s.contains(currToken.type())) {
      advance();
    }
  }

  private boolean isVal(TokenType t){
    Set <TokenType> s = new HashSet<TokenType>();

    s.add(TokenType.INT_VAL);
    s.add(TokenType.DOUBLE_VAL);
    s.add(TokenType.BOOL_VAL);
    s.add(TokenType.CHAR_VAL);
    s.add(TokenType.STRING_VAL);

    return s.contains(t);
  }


  // <idrval> ::= ID ( DOT ID )∗ | ID LPAREN ⟨exprlist⟩ RPAREN
  private RValue idrval() throws MyPLException {
    debug("<idrval>");
    CallRValue callNode = new CallRValue();
    IDRValue idNode = new IDRValue();
    callNode.funName = currToken;
    idNode.path.add(currToken);
    eat(TokenType.ID, "Identifier (ID) expected");
    if (currToken.type() == TokenType.LPAREN) {
      advance();
      exprlist(callNode);
      eat(TokenType.RPAREN, "Right parenthesis ')' expected");
      return callNode;
    } else {
      while (currToken.type() == TokenType.DOT) {
        advance();
        idNode.path.add(currToken);
        eat(TokenType.ID, "Identifier (ID) expected");
      }
      return idNode;
    }
  }

  // <exprlist> ::= ⟨expr⟩ ( COMMA ⟨expr⟩ )∗ | ε
  private void exprlist(CallRValue callNode) throws MyPLException {
    debug("<exprlist>");
    if (currToken.type() == TokenType.NOT || currToken.type() == TokenType.LPAREN || isRPVal(currToken.type())) {
      callNode.argList.add(expr());
      while (currToken.type() == TokenType.COMMA) {
        advance();
        callNode.argList.add(expr());
      }
    }
  }

  private boolean isRPVal(TokenType t) {
    debug("<isRPVal>");
    Set<TokenType> s = Set.of(TokenType.NIL, TokenType.NEW, TokenType.NEG, TokenType.ID, TokenType.INT_VAL,
        TokenType.DOUBLE_VAL, TokenType.BOOL_VAL, TokenType.CHAR_VAL, TokenType.STRING_VAL);
    return s.contains(t);
  }

  private boolean isType(TokenType t) {
    debug("<isType>");
    Set<TokenType> s = Set.of(TokenType.INT_TYPE, TokenType.DOUBLE_TYPE, TokenType.BOOL_TYPE, TokenType.CHAR_TYPE,
        TokenType.STRING_TYPE, TokenType.ID);
    return s.contains(t);
  }
}