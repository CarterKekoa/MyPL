/**
 * Author: Carter Mooring
 * Assign: 7
 * File: Interpreter.java
 *
 * Visitor implementation of a basic "Pure AST" Interpreter for MyPL. 
 */

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class Interpreter implements Visitor {
  private boolean debug_flag = false; // set to false to remove debug comments
  private final SymbolTable symbolTable = new SymbolTable();
  private Object currVal = null;
  private Map<Integer,Map<String,Object>> heap = new HashMap<>();
  
  
  public Integer run(final StmtList stmtList) throws MyPLException {
    debug("run");
    try {
      // evaluate the stmtList
      stmtList.accept(this);
      // default return
      return 0;
    }catch (MyPLException e) {
      if (!e.isReturnException())
        throw e;
      Object returnVal = e.getReturnValue();
      if (returnVal == null)
        return 0;
      return (Integer)returnVal;
      }
  }

  
  // visitor functions
  public void visit(final StmtList node) throws MyPLException {
    symbolTable.pushEnvironment();
    for (final Stmt s : node.stmts) {
      s.accept(this);
    }
    symbolTable.popEnvironment();    
  }

  
  public void visit(final VarDeclStmt node) throws MyPLException {  
    node.varExpr.accept(this);
    symbolTable.addName(node.varId.lexeme());
    symbolTable.setInfo(node.varId.lexeme(), currVal);
  }

  
  public void visit(final AssignStmt node) throws MyPLException {
    // evaluate rhs
    node.rhs.accept(this);
    // let LValue do the assignment
    node.lhs.accept(this);
  }

  
  public void visit(final ReturnStmt node) throws MyPLException {
    debug("ReturnStmt");
    node.returnExpr.accept(this);
	  Object returnVal = currVal;
	  throw new MyPLException(returnVal);
  }

  
  public void visit(final IfStmt node) throws MyPLException {
    debug("IfStmt");
    Boolean elif = true;
    node.ifPart.boolExpr.accept(this);
    if((boolean)currVal){
      node.ifPart.stmtList.accept(this);
    }else{
      for(final BasicIf elseif : node.elsifs) {
        elseif.boolExpr.accept(this);
        if((boolean)currVal && elif){
          elseif.stmtList.accept(this);
          elif = false;
        }
      }
      if(node.hasElse && elif){
        node.elseStmtList.accept(this);
      }
    }
  }

  
  public void visit(final WhileStmt node) throws MyPLException {
    debug("WhileStmt");
    node.boolExpr.accept(this);
    while((boolean)currVal){
      node.stmtList.accept(this);
      node.boolExpr.accept(this);
    }
  }
  
  public void visit(final ForStmt node) throws MyPLException {
    debug("ForStmt");
    node.startExpr.accept(this);
    final Object start = currVal;
    final int begin = (Integer)start;
    node.endExpr.accept(this);
    final Object last = currVal;
    final int end = (Integer)last;
    symbolTable.addName(node.var.lexeme());
    symbolTable.setInfo(node.var.lexeme(), begin);

    if(begin > end){
      for(int i = begin; i > end; i--){
        symbolTable.setInfo(node.var.lexeme(), i);
        node.stmtList.accept(this);
      }
    }else{
      for(int i = begin; i <= end; i++){
        symbolTable.setInfo(node.var.lexeme(), i);
        node.stmtList.accept(this);
      }
    }
  }

  public void visit(final TypeDeclStmt node) throws MyPLException {
    debug("TypeDeclStmt");
    symbolTable.addName(node.typeId.lexeme());
    symbolTable.setInfo(node.typeId.lexeme(), List.of(symbolTable.getEnvironmentId(), node));
  }


  public void visit(final FunDeclStmt node) throws MyPLException {
    debug("FunDeclStmt");
    symbolTable.addName(node.funName.lexeme());
    symbolTable.setInfo(node.funName.lexeme(), List.of(symbolTable.getEnvironmentId(), node));
  }

  
  // expressions
  
  public void visit(final Expr node) throws MyPLException {
    debug("Expr");
    // The following is a basic sketch of what you need to
    // implement. You will need to fill in the remaining code to get
    // Expr working.
     
    node.first.accept(this);
    final Object firstVal = currVal;
    
    if (node.operator != null) {
      node.rest.accept(this);
      final Object restVal = currVal;
      final String op = node.operator.lexeme();

      // Check for null values (all except == and !=)
      // if you find a null value report an error

      // basic math ops (+, -, *, /, %)
      if (op.equals("+")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal + (Integer)restVal;
        else 
          currVal = (Double)firstVal + (Double)restVal;
      }
      else if (op.equals("-")) {
        if(firstVal instanceof Integer)
          currVal = (Integer)firstVal - (Integer)restVal;
        else
          currVal = (Double)firstVal - (Double)restVal;
      }
      else if (op.equals("*")) {
        if(firstVal instanceof Integer)
          currVal = (Integer)firstVal * (Integer)restVal;
        else
          currVal = (Double)firstVal * (Double)restVal;
      }
      else if (op.equals("/")) {
        if(firstVal instanceof Integer && (Integer)restVal != 0)
          currVal = (Integer)firstVal / (Integer)restVal;
        else if(firstVal instanceof Double && (Double)restVal != 0){
          currVal = (Double)firstVal / (Double)restVal;
        }else{
          error("Can't divide by 0", node.operator);
        }
      }
      else if (op.equals("%")) {
        if(firstVal instanceof Integer && (Integer)restVal != 0){
          currVal = (Integer)firstVal % (Integer)restVal;
        }
        else if(firstVal instanceof Double && (Double)restVal != 0)
          currVal = (Double)firstVal % (Double)restVal;
        else
          error("Can't mod by 0", node.operator);
      }
      // boolean operators (and, or)
      else if (op.equals("and")) {
        currVal = (Boolean)firstVal && (Boolean)restVal;
      }
      else if (op.equals("or")) {
        currVal = (Boolean)firstVal || (Boolean)restVal;
      }
      // relational comparators (=, !=, <, >, <=, >=)
      else if (op.equals("=")) {
        if(restVal != null){
          if(firstVal.equals(restVal)){
            currVal = true;
          }else{
            currVal = false;
          }
        }else{
          if(firstVal == null){
            currVal = true;
          }else{
            currVal = false;
          }
        }
      }
      else if (op.equals("!=")){
        if(restVal != null){
          if(firstVal.equals(restVal)){
            currVal = false;
          }else{
            currVal = true;
          }
       }else{
         if(firstVal == null){
           currVal = false;
         }else{
           currVal = true;
         }
       }
      }
      else if (op.equals("<")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal < (Integer)restVal;
        else if (firstVal instanceof Double)
          currVal = (Double)firstVal < (Double)restVal;
        else
          currVal = ((String)firstVal).compareTo((String)restVal) < 0;
      }
      else if (op.equals("<=")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal <= (Integer)restVal;
        else if (firstVal instanceof Double)
          currVal = (Double)firstVal <= (Double)restVal;
        else
          currVal = ((String)firstVal).compareTo((String)restVal) <= 0;
      }
      else if (op.equals(">")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal > (Integer)restVal;
        else if (firstVal instanceof Double)
          currVal = (Double)firstVal > (Double)restVal;
        else
          currVal = ((String)firstVal).compareTo((String)restVal) > 0;
      }
      else if (op.equals(">=")) {
        if (firstVal instanceof Integer)
          currVal = (Integer)firstVal >= (Integer)restVal;
        else if (firstVal instanceof Double)
          currVal = (Double)firstVal >= (Double)restVal;
        else
          currVal = ((String)firstVal).compareTo((String)restVal) >= 0;
      }
    }
    // deal with not operator
    if (node.negated){
     if(currVal instanceof Boolean){
      currVal = !(Boolean)currVal;
     }
    }
  }


  public void visit(final LValue node) throws MyPLException {
    String varName = node.path.get(0).lexeme();
  	if(node.path.size() > 1){
		  Map<String, Object> obj = (Map<String, Object>)heap.get((Integer)symbolTable.getInfo(varName));
		  for(int i = 1; i < node.path.size() - 1; i++){
			  varName = node.path.get(i).lexeme();
			  int oid = (Integer)obj.get(varName);
			  obj = (Map<String, Object>)heap.get(oid); 
		  }
		  varName = node.path.get(node.path.size() - 1).lexeme();
		  obj.replace(varName, currVal);
	  }else{
      symbolTable.setInfo(varName, currVal);
    }
  }

  
  public void visit(final SimpleTerm node) throws MyPLException {
    node.rvalue.accept(this);
  }

  
  public void visit(final ComplexTerm node) throws MyPLException {
    node.expr.accept(this);
  }

  
  public void visit(final SimpleRValue node) throws MyPLException {
    if (node.val.type() == TokenType.INT_VAL)
      currVal = Integer.parseInt(node.val.lexeme());
    else if (node.val.type() == TokenType.DOUBLE_VAL)
      currVal = Double.parseDouble(node.val.lexeme());
    else if (node.val.type() == TokenType.BOOL_VAL)
      currVal = Boolean.parseBoolean(node.val.lexeme());
    else if (node.val.type() == TokenType.CHAR_VAL)
      currVal = node.val.lexeme(); // leave as single character string
    else if (node.val.type() == TokenType.STRING_VAL)
      currVal = node.val.lexeme();
    else if (node.val.type() == TokenType.NIL)
      currVal = null;
  }

  
  public void visit(final NewRValue node) throws MyPLException {
    debug("NewRValue");
    List<Object> typeInfo = (List<Object>)symbolTable.getInfo(node.typeId.lexeme());
	  int currEnv = symbolTable.getEnvironmentId();
	  symbolTable.setEnvironmentId((int)typeInfo.get(0));
	  Map<String,Object> obj = new HashMap<>();
	  int oid = System.identityHashCode(obj);
	  symbolTable.pushEnvironment();
	  for(VarDeclStmt varDecl : ((TypeDeclStmt)(typeInfo.get(1))).fields) {
		  varDecl.accept(this);
		  obj.put(varDecl.varId.lexeme(), currVal);
	  }
	  symbolTable.popEnvironment();
	  symbolTable.setEnvironmentId(currEnv);
	  heap.put(oid, obj);
  	currVal = oid;
  }


  public void visit(final CallRValue node) throws MyPLException {
    debug("CallRValue");
    final List<String> builtIns = List.of("print", "read", "length", "get",
                                    "concat", "append", "itos", "stoi",
                                    "dtos", "stod");
    final String funName = node.funName.lexeme();
    if (builtIns.contains(funName)){
      callBuiltInFun(node);
    }else{
      List<Object> argumentVals = new ArrayList<>();
      List<Object> functionVals = (List<Object>)symbolTable.getInfo(funName);
      int currentEnv = symbolTable.getEnvironmentId();
      for(Expr argument : node.argList) { //
        argument.accept(this);
        argumentVals.add(currVal);
      }
      symbolTable.setEnvironmentId((Integer)functionVals.get(0));
      symbolTable.pushEnvironment();
      ArrayList<FunParam> funParams = ((FunDeclStmt)(functionVals.get(1))).params;
      for(int i = 0; i < funParams.size(); i++){ //while not at the end of the size
        symbolTable.addName(((funParams.get(i)).paramName).lexeme());
        symbolTable.setInfo(((funParams.get(i)).paramName).lexeme(), argumentVals.get(i));
      }
      StmtList funStmtList = ((FunDeclStmt)(functionVals.get(1))).stmtList;
      try {
        ((FunDeclStmt)(functionVals.get(1))).stmtList.accept(this);
      }catch (MyPLException e) {
        if (!e.isReturnException()){
          throw e;
        }
        currVal = e.getReturnValue();
      }
      symbolTable.popEnvironment();
      symbolTable.setEnvironmentId(currentEnv);
    }
  }

  
  public void visit(final IDRValue node) throws MyPLException {
    String varName = node.path.get(0).lexeme();
    if(node.path.size() > 1){ //if path is larger that 1
      Map<String, Object> obj = (Map<String, Object>)heap.get((Integer)symbolTable.getInfo(varName));
      for(int i = 1; i < node.path.size() - 1; i++){ // while not at the end of the node size, minus one becasue return value
        varName = node.path.get(i).lexeme();
        int oid = (Integer)obj.get(varName);
        obj = (Map<String, Object>)heap.get(oid); 
      }
      varName = node.path.get(node.path.size() - 1).lexeme();
      currVal = obj.get(varName);
    }else{
      currVal = symbolTable.getInfo(varName);
    }
  }


  public void visit(final NegatedRValue node) throws MyPLException {
    node.expr.accept(this);
    if (currVal instanceof Integer)
      currVal = -(Integer)currVal;
    else
      currVal = -(Double)currVal;
  }

  //------------------
  // helper functions
  //------------------

  
  private void callBuiltInFun(final CallRValue node) throws MyPLException {
    final String funName = node.funName.lexeme();
    // get the function arguments
    final List<Object> argVals = new ArrayList<>();
    for (final Expr arg : node.argList) {
      arg.accept(this);
      // make sure no null values
      if (currVal == null)
        error("nil value", getFirstToken(arg));
      argVals.add(currVal);
    }
    if (funName.equals("print")) {
      // Fix '\' 'n' issue
      String msg = (String)argVals.get(0);
      msg = msg.replace("\\n", "\n");
      msg = msg.replace("\\t", "\t");
      System.out.print(msg);
      currVal = null;
    }
    else if (funName.equals("read")) {
      final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      try {
        currVal = reader.readLine();
      }
      catch(final Exception e) {
        currVal = null;
      }
    }
    else if (funName.equals("get")) {
      final int index = (Integer)argVals.get(0);
      final String str = (String)argVals.get(1);
      if(0 <= index && index < str.length()){
        currVal = str.charAt(index);
      }else{
        error("index is out of bounds", node.funName);
      }
    }
    else if (funName.equals("concat")) {
      currVal = (String)argVals.get(0) + (String)argVals.get(1);
    }
    else if (funName.equals("append")) {
      currVal = (String)argVals.get(0) + argVals.get(1).toString();
    } 
    else if (funName.equals("itos")) {
      currVal = argVals.get(0).toString();
    }
    else if (funName.equals("stoi")) {
      currVal = Integer.parseInt((String)argVals.get(0));
    }
    else if (funName.equals("dtos")) {
      currVal = argVals.get(0).toString();
    }
    else if (funName.equals("stod")) {
      currVal = Double.parseDouble((String)argVals.get(0));
    }
    else if(funName.equals("length")){
      final String str = (String)argVals.get(0);
      final int length = str.length();
      currVal = length;
    }
  }

  
  private void error(final String msg, final Token token) throws MyPLException {
    final int row = token.row();
    final int col = token.column();
    throw new MyPLException("\nRuntime", msg, row, col);
  }

  
  private Token getFirstToken(final Expr node) {
    return getFirstToken(node.first);
  }

  
  private Token getFirstToken(final ExprTerm node) {
    if (node instanceof SimpleTerm)
      return getFirstToken(((SimpleTerm)node).rvalue);
    else
      return getFirstToken(((ComplexTerm)node).expr);      
  }

  
  private Token getFirstToken(final RValue node) {
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

  private void debug(String msg) {
    if (debug_flag)
      System.out.println(msg);
  }
}    