

public class WhileStmt implements Stmt {

  public Expr boolExpr = null;
  public StmtList stmtList = new StmtList();
  
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

