
/**
 * Author: S. Bowers
 * Assign: 4
 * File: Visitor.java
 *
 * Visitor interface for MyPL AST.
 */


public interface Visitor {

  // statement list
  public void visit(StmtList node) throws MyPLException;

  // statements
  public void visit(VarDeclStmt node) throws MyPLException;
  public void visit(AssignStmt node) throws MyPLException;
  public void visit(ReturnStmt node) throws MyPLException;
  public void visit(IfStmt node) throws MyPLException;
  public void visit(WhileStmt node) throws MyPLException;
  public void visit(ForStmt node) throws MyPLException;
  public void visit(TypeDeclStmt node) throws MyPLException;
  public void visit(FunDeclStmt node) throws MyPLException;

  // expressions
  public void visit(Expr node) throws MyPLException;
  public void visit(LValue node) throws MyPLException;
  public void visit(SimpleTerm node) throws MyPLException;
  public void visit(ComplexTerm node) throws MyPLException;

  // rvalues
  public void visit(SimpleRValue node) throws MyPLException;
  public void visit(NewRValue node) throws MyPLException;
  public void visit(CallRValue node) throws MyPLException;
  public void visit(IDRValue node) throws MyPLException;
  public void visit(NegatedRValue node) throws MyPLException;

}    

