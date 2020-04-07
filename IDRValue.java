
import java.util.ArrayList;

public class IDRValue implements RValue {

  public ArrayList<Token> path = new ArrayList<>();

  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }
  
}
