
public class NewRValue implements RValue {

  public Token typeId = null;

  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }
  
}

