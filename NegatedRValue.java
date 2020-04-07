
public class NegatedRValue implements RValue {

  public Expr expr = null;

  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }
  
}
