
import java.util.ArrayList;

public class CallRValue implements RValue {

  public Token funName = null;
  public ArrayList<Expr> argList = new ArrayList<>();

  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }
  
}
