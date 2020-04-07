

public class SimpleRValue implements RValue {

  public Token val = null;

  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}
