
import java.util.ArrayList;

public class IfStmt implements Stmt {

  public BasicIf ifPart = new BasicIf();
  public ArrayList<BasicIf> elsifs = new ArrayList<>();
  public boolean hasElse = false;
  public StmtList elseStmtList = new StmtList();
  
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

