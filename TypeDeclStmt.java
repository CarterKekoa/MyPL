
import java.util.ArrayList;

public class TypeDeclStmt implements Stmt {

  public Token typeId = null;
  public ArrayList<VarDeclStmt> fields = new ArrayList<>();
  
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

