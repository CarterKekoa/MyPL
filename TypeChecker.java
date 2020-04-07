
/**
 * Author: Carter Mooring
 * Assign: HW5
 * File: TypeChecker.java
 *
 * Visitor implementation of Semantic Analysis Checking for the MyPL
 * AST. Note the following conventions for representing type
 * information:
 * 
 * A variable name's type is a string (varname to string)
 *
 * A structured type name is a map of var mappings (typename to Map) where
 * each variable name is mapped to its type 
 *
 * A function type name is a list of parameter types (name to
 * List) where the list consists of each formal param type ending with
 * the return type of the function.
 *
 * For more information on the general design see the lecture notes.
 */

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

public class TypeChecker implements Visitor {
  private boolean debug_flag = false; // set to false to remove debug comments
  // the symbol table
  private SymbolTable symbolTable = new SymbolTable();
  // holds last inferred type
  private String currType = null;
  // sets up the initial environment for type checking
  public TypeChecker() {
    symbolTable.pushEnvironment();
    // add return type for global scope
    symbolTable.addName("return");
    symbolTable.setInfo("return", "int");
    // print function
    symbolTable.addName("print");
    symbolTable.setInfo("print", List.of("string", "nil"));
    // read function
    symbolTable.addName("read");
    symbolTable.setInfo("read", List.of("string"));
    // length function
    symbolTable.addName("length");
    symbolTable.setInfo("length", List.of("string", "int"));
    // get function
    symbolTable.addName("get");
    symbolTable.setInfo("get", List.of("int", "string", "char"));
    // concat function
    symbolTable.addName("concat");
    symbolTable.setInfo("concat", List.of("string", "string", "string"));
    // append function 
    symbolTable.addName("append");
    symbolTable.setInfo("append", List.of("string", "char", "string")) ;
    // itos function 
    symbolTable.addName("itos");
    symbolTable.setInfo("itos", List.of("int", "string"));
    // stoi function
    symbolTable.addName("stoi");
    symbolTable.setInfo("stoi", List.of("string", "int"));
    // dtos function
    symbolTable.addName("dtos");
    symbolTable.setInfo("dtos", List.of("double", "string"));
    // stod function
    symbolTable.addName("stod");
    symbolTable.setInfo("stod", List.of("string", "double"));
  }

  // function to print a debug string if the debug_flag is set for
  // helping to diagnose/test the parser
  private void debug(String msg) {
    if (debug_flag)
      System.out.println(msg);
  }

  //------------------------
  // visitor functions
  //------------------------

  //Statement List
  public void visit(StmtList node) throws MyPLException {
    debug("StmtList");
    symbolTable.pushEnvironment();
    for (Stmt s : node.stmts){
      s.accept(this);
    }
    symbolTable.popEnvironment();    
  }

  //Assignment Statements
  public void visit(AssignStmt node) throws MyPLException {
    debug("AssignStmt");
    // check and infer rhs type
    node.rhs.accept(this);
    String rhsType = currType;
    // check and obtain lhs type
    node.lhs.accept(this);
    String lhsType = currType;
    // error if rhs and lhs types don't match
    if (!rhsType.equals("nil") && !rhsType.equals(lhsType)) {
      String msg = "mismatched types in assignment";
      error(msg, node.lhs.path.get(0));
    }
  }

  public void visit(SimpleTerm node) throws MyPLException {
    debug("SimpleTerm");
    node.rvalue.accept(this);
  }

  
  public void visit(ComplexTerm node) throws MyPLException {
    debug("Complex");
    node.expr.accept(this);
  }

  
  public void visit(SimpleRValue node) throws MyPLException {
    debug("SimpleRValue");
    if (node.val.type() == TokenType.INT_VAL)
      currType = "int";
    else if (node.val.type() == TokenType.DOUBLE_VAL)
      currType = "double";
    else if (node.val.type() == TokenType.BOOL_VAL)
      currType = "bool";
    else if (node.val.type() == TokenType.CHAR_VAL)
      currType = "char";
    else if (node.val.type() == TokenType.STRING_VAL)
      currType = "string";
    else if (node.val.type() == TokenType.NIL)
      currType = "nil";
  }

  
  public void visit(IDRValue node) throws MyPLException {
    debug("IDRValue");
    // check the first id in the path
    String varName = node.path.get(0).lexeme();
    if (!symbolTable.nameExists(varName))
      error("undefined variable '" + varName + "'", node.path.get(0));
    // make sure it isn't function or type name
    if (symbolTable.getInfo(varName) instanceof List)
      error("unexpected function name in rvalue", node.path.get(0));
    if (symbolTable.getInfo(varName) instanceof Map)
      error("unexpected type name in rvalue", node.path.get(0));
    // grab the type
    currType = (String)symbolTable.getInfo(varName);
    if (node.path.size() > 1 && !(symbolTable.getInfo(currType) instanceof Map))
      error("invalid member access for non-structured type", node.path.get(0));
    // check path
    Map<String,String> idrContent = (Map<String,String>)symbolTable.getInfo(currType);
    for(int i = 1; i < node.path.size(); i++){  //for to iterate through the entire size of node.path
      idrContent = (Map<String, String>)symbolTable.getInfo(currType);
      varName = node.path.get(i).lexeme();
      //check if the key is contained, err if not
      if(!idrContent.containsKey(varName)){
        error("variable not yet defined '" + varName + "'", node.path.get(i));
      }
      currType = idrContent.get(varName);
      if(node.path.size() > (i + 1) && !(symbolTable.getInfo(currType) instanceof Map)){
        error("invalid member access for non-structured type", node.path.get(0));
      }
    }
  }

  //Variable Declerations
  public void visit(VarDeclStmt node) throws MyPLException {
    debug("VarDeclStmt");
    node.varExpr.accept(this);
    //type can be null so check if it is
    if(node.varType != null){
      //check if the types dont match and also check nil
      if(!currType.equals(node.varType.lexeme()) && !currType.equals("nil")){
        String msg = "primitive type mismatch";
        error(msg, node.varType);
      }
      currType = node.varType.lexeme();
    }
    //if nil is there, it needs to have a type becasue it cant be assumed
    if(node.varType == null && currType.equals("nil")){
      String msg = "type missing in nil assignment";
      error(msg, node.varId);
    }
    //check if the ID has already been used
    if(symbolTable.nameExistsInCurrEnv(node.varId.lexeme())){
      String msg = "'" + node.varId.lexeme() + "' has already been declared";
      error(msg, node.varId);
    }
    symbolTable.addName(node.varId.lexeme());
    symbolTable.setInfo(node.varId.lexeme(), currType);
  }

  //Return Statements
  public void visit(ReturnStmt node) throws MyPLException {
    debug("ReturnStmt");
    if(node.returnExpr != null){
      node.returnExpr.accept(this);
    }else{
      currType = "nil";
    }
    if(!currType.equals("nil") && !currType.equals(symbolTable.getInfo("return"))){
      String msg = "return does not match";
      error(msg, node.returnToken);
    }
  }

  //If statements
  public void visit(IfStmt node) throws MyPLException {
    debug("IfStmt");
    symbolTable.pushEnvironment(); //initial if statement
    node.ifPart.boolExpr.accept(this);
    node.ifPart.stmtList.accept(this);
    symbolTable.popEnvironment();
    //while there are still basicifs, keep creating environments
    for(BasicIf item : node.elsifs){
      symbolTable.pushEnvironment();//new environment for each if statememnt block
      item.boolExpr.accept(this);
      item.stmtList.accept(this);
      symbolTable.popEnvironment();
    }
    //check if the if has a else statement, like how mine doestn :)
    if(node.hasElse){
      symbolTable.pushEnvironment();
      node.elseStmtList.accept(this);
      symbolTable.popEnvironment();
    }
  }

  //While Loop Statement
  public void visit(WhileStmt node) throws MyPLException {
    debug("WhileStmt");
    node.boolExpr.accept(this); //do this first to get first type
    symbolTable.pushEnvironment();  //loops need environments
    //if the current type isnt a bool (which it has to be) then error
    if(!currType.equals("bool")){
      String msg = "requires boolean type";
      error(msg, getFirstToken(node.boolExpr));
    }
    node.stmtList.accept(this);
    symbolTable.popEnvironment();
  }

  //For Loop Statement
  public void visit(ForStmt node) throws MyPLException {
    debug("ForStmt");
    symbolTable.pushEnvironment();  //loop so environment
    symbolTable.addName(node.var.lexeme());
    symbolTable.setInfo(node.var.lexeme(), "int");
    node.startExpr.accept(this);
    node.endExpr.accept(this);
    //while there are more statements
    for(Stmt stmt : node.stmtList.stmts){
      stmt.accept(this);
    }
    symbolTable.popEnvironment();
  }

  //Type Decleration Statement
  public void visit(TypeDeclStmt node) throws MyPLException {
    debug("TypeDeclStmt");
    symbolTable.pushEnvironment();
    //HashMap to store the types
    Map<String, String> type = new HashMap<>();
    for(VarDeclStmt var : node.fields){
      var.accept(this);
      type.put(var.varId.lexeme(), currType);
    }
    symbolTable.popEnvironment();
    symbolTable.addName(node.typeId.lexeme());
    symbolTable.setInfo(node.typeId.lexeme(), type);
  }

  //Function Decleration Statement
  public void visit(FunDeclStmt node) throws MyPLException {
    debug("FunDeclStmt");
    String fun = node.funName.lexeme();
    //check if the name of the function already exists
	  if(symbolTable.nameExists(fun)){
      error("function already declared", node.funName);
    }
    symbolTable.addName(fun);
    symbolTable.pushEnvironment();
    symbolTable.setInfo("return", node.returnType.lexeme());
    //List for the parameters and their types
	  List<String> args = new ArrayList<>();
	  for(int i = 0; i < node.params.size(); i++){
      if(symbolTable.nameExistsInCurrEnv(node.params.get(i).paramName.lexeme())){
        String msg = "the parameter '" + node.params.get(i).paramName.lexeme() + "' already exists";
        error(msg, node.params.get(i).paramType);
      }
      args.add(node.params.get(i).paramType.lexeme());
      symbolTable.addName(node.params.get(i).paramName.lexeme());
      symbolTable.setInfo(node.params.get(i).paramName.lexeme(), node.params.get(i).paramType.lexeme());
    }
    args.add(node.returnType.lexeme());
    symbolTable.setInfo(fun, args);
    node.stmtList.accept(this);
    symbolTable.popEnvironment();
	  symbolTable.setInfo(fun, args);
  }

  //------------------------
  //Expressions
  //------------------------

  public void visit(Expr node) throws MyPLException {
    debug("Expr");
    //what to check: if rest, first type = rest type, ops like and and or can only be used with booleans
    //if node is negated expr has to be boolean
    node.first.accept(this);
    String lhsType = currType;
    if(node.rest != null){
      node.rest.accept(this);
    }
    String rhsType = currType;
    if(node.operator != null){
      TokenType operatorToken = node.operator.type();

      //check if left and right side are different types
      if(!lhsType.equals(rhsType) && (!lhsType.equals("nil") && !rhsType.equals("nil"))){
        String msg = "mismatched types in expression";
        error(msg, node.operator);
      }

      //make sure modulo only deals with ints
      if(operatorToken == TokenType.MODULO){
        if(!lhsType.equals("int") || !rhsType.equals("int")){
          String msg = "invalid use of modulo";
          error(msg, node.operator);
        }
      }

      //if int or double, make sure the operator correct
      if(lhsType.equals("int") || lhsType.equals("double")){
        if(!isOp(operatorToken)){
          String msg = "invalid operator use";
          error(msg, node.operator);
        }else if(rhsType.equals("nil")){
          if(!isBoop(operatorToken)){
            String msg = "unexpected type in expression";
            error(msg, node.operator);
          }
        }
      }

      //if boolean, make sure operator is correct
      if(lhsType.equals("bool")){
        if(!isBop(operatorToken)){
          String msg = "invalid type in arithmetic expression";
          error(msg, node.operator);
        }
      }

      //makes sure string and char dont use any operators
      if(lhsType.equals("string") || lhsType.equals("char")){
        if(isOp(operatorToken) || isBop(operatorToken)){
          System.out.println("2: " + lhsType);
          String msg = "invalid type in arithmetic expression";
          error(msg, node.operator);
        }
      }

      //make sure nil operators are == or !=
      if(lhsType.equals("nil")){
        if(operatorToken != TokenType.EQUAL || operatorToken != TokenType.NOT_EQUAL){
          String msg = "invalid operator use";
          error(msg, node.operator);
        }
      }

      //if comparison operator, set current type equal to boolean
      if(isCop(operatorToken)){
        currType = "bool";
      }
    }
  }

  public void visit(LValue node) throws MyPLException {
    debug("LValue");
    // check the first id in the path
    String varName = node.path.get(0).lexeme();
    if (!symbolTable.nameExists(varName))
      error("undefined variable '" + varName + "'", node.path.get(0));
    // make sure it isn't function or type name
    if (symbolTable.getInfo(varName) instanceof List)
      error("unexpected function name in lvalue", node.path.get(0));
    if (symbolTable.getInfo(varName) instanceof Map)
      error("unexpected type name in lvalue", node.path.get(0));
    // grab the type
    currType = (String)symbolTable.getInfo(varName);
    if (node.path.size() > 1 && !(symbolTable.getInfo(currType) instanceof Map))
      error("invalid member access for non-structured type", node.path.get(0));
    // check path
    Map<String,String> idrContent = (Map<String,String>)symbolTable.getInfo(currType);
    for(int i = 1; i < node.path.size(); i++){  //for to iterate through the entire size of node.path
      idrContent = (Map<String, String>)symbolTable.getInfo(currType);
      varName = node.path.get(i).lexeme();
      //check if the key is contained, err if not
      if(!idrContent.containsKey(varName)){
        error("variable not yet defined '" + varName + "'", node.path.get(i));
      }
      currType = idrContent.get(varName);
      if(node.path.size() > (i + 1) && !(symbolTable.getInfo(currType) instanceof Map)){
        error("invalid member access for non-structured type", node.path.get(0));
      }
    }
  }

  //New R Value
  public void visit(NewRValue node) throws MyPLException {
    debug("NewRValue");
    String newR = node.typeId.lexeme();
    //just a new r val so make sure the name doest already exist
	  if(!symbolTable.nameExists(newR)){
      error(newR + " does not exist", node.typeId);
    }
    currType = node.typeId.lexeme();
  }

  //Call R Value
  public void visit(CallRValue node) throws MyPLException {
    debug("CallRValue");
    String name = node.funName.lexeme();
    //make sure the name exists
	  if(!symbolTable.nameExists(name)){
      error("function does not exist", node.funName);
    }
    //List to store the arguments
    List<String> arguments = (List<String>)symbolTable.getInfo(name);
    //if there is a mismatch in number of arguments then error
	  if(node.argList.size() < (arguments.size() - 1)){
      String msg = "not enough arguments";
      error(msg, node.funName);
    }else if(node.argList.size() > (arguments.size() - 1)){
      String msg = "too many arguments";
      error(msg, node.funName);
    }
    //while we havent gone through the whole list
	  for(int i = 0; i < node.argList.size(); i++){
      node.argList.get(i).accept(this);
      //if the current type doesnt match current param and if its not nil
      if(!currType.equals(arguments.get(i)) && !currType.equals("nil") && !currType.equals("bool")){
        error("wrong argument type", node.funName);
      }
    }
    currType = arguments.get(arguments.size()-1);
  }

  //Negated R Value
  public void visit(NegatedRValue node) throws MyPLException {
    debug("NegatedRValue");
    node.expr.accept(this);
  }

  //------------------------
  // helper functions
  //------------------------

  private void error(String msg, Token token) throws MyPLException {
    int row = token.row();
    int col = token.column();
    throw new MyPLException("Type", msg, row, col);
  }

  // gets first token of an expression
  private Token getFirstToken(Expr node) {
    return getFirstToken(node.first);
  }

  // gets first token of an expression term
  private Token getFirstToken(ExprTerm node) {
    if (node instanceof SimpleTerm)
      return getFirstToken(((SimpleTerm)node).rvalue);
    else
      return getFirstToken(((ComplexTerm)node).expr);      
  }

  // gets first token of an rvalue
  private Token getFirstToken(RValue node) {
    if (node instanceof SimpleRValue)
      return ((SimpleRValue)node).val;
    else if (node instanceof CallRValue)
      return ((CallRValue)node).funName;
    else if (node instanceof IDRValue)
      return ((IDRValue)node).path.get(0);
    else if (node instanceof NegatedRValue) 
      return getFirstToken(((NegatedRValue)node).expr);
    else 
      return ((NewRValue)node).typeId;
  }

  private boolean isType(TokenType t) {
    Set<TokenType> s = Set.of(TokenType.INT_TYPE, TokenType.DOUBLE_TYPE, TokenType.BOOL_TYPE, TokenType.CHAR_TYPE,
        TokenType.STRING_TYPE);
    return s.contains(t);
  }

  // <isOp> ::= PLUS | MINUS | DIVIDE | MULTIPLY | EQUAL |
  // LESS_THAN | GREATER_THAN | LESS_THAN_EQUAL | GREATER_THAN_EQUAL | NOT_EQUAL
  private boolean isOp(TokenType t) {
    Set<TokenType> s = Set.of(TokenType.PLUS, TokenType.MINUS, TokenType.DIVIDE, TokenType.MULTIPLY, TokenType.MODULO,
        TokenType.EQUAL, TokenType.LESS_THAN, TokenType.GREATER_THAN,
        TokenType.LESS_THAN_EQUAL, TokenType.GREATER_THAN_EQUAL, TokenType.NOT_EQUAL);
    return s.contains(t);
  }

  private boolean isBop(TokenType t) {
    Set<TokenType> s = Set.of( TokenType.AND, TokenType.OR,
        TokenType.EQUAL, TokenType.NOT_EQUAL);
    return s.contains(t);
  }

  private boolean isCop(TokenType t) {
    Set<TokenType> s = Set.of(TokenType.EQUAL, TokenType.LESS_THAN, TokenType.GREATER_THAN,
    TokenType.LESS_THAN_EQUAL, TokenType.GREATER_THAN_EQUAL, TokenType.NOT_EQUAL);
    return s.contains(t);
  }

  private boolean isBoop(TokenType t) {
    Set<TokenType> s = Set.of(TokenType.EQUAL, TokenType.NOT_EQUAL);
    return s.contains(t);
  }
}    