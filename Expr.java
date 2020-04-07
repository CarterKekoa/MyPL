
public class Expr implements Stmt {

  public boolean negated = false;
  public ExprTerm first = null;
  public Token operator = null;
  public Expr rest = null;

  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}
