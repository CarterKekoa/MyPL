
public class ForStmt implements Stmt {

  public Token var = null;
  public Expr startExpr = null;
  public Expr endExpr = null;
  public StmtList stmtList = new StmtList();
  
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

