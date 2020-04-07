
public class SimpleTerm implements ExprTerm {

  public RValue rvalue = null;

  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}
