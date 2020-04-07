
import java.util.ArrayList;

public class FunDeclStmt implements Stmt {

  public Token returnType = null;
  public Token funName = null;
  public ArrayList<FunParam> params = new ArrayList<>();
  public StmtList stmtList = new StmtList();
  
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

