
import java.util.ArrayList;

public class StmtList implements ASTNode {

  public ArrayList<Stmt> stmts = new ArrayList<>();

  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}
