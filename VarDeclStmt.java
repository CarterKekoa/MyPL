

public class VarDeclStmt implements Stmt {

  public Token varId = null;
  public Token varType = null;
  public Expr varExpr = null;
  
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }
  
}

