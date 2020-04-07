
/**
 * Author: S. Bowers
 * Assign: 5
 * File: SymbolTable.java
 *
 * Simple SymbolTable for MyPL.
 */

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;


public class SymbolTable {
  // stack of environments with name -> Object mappings
  private List<Map<String,Object>> environments = new LinkedList<>();
  // current environment identifier
  private Integer currEnvId = null;


  // add a new environment to environment stack
  public void pushEnvironment() {
    Map<String,Object> new_environment = new HashMap<>();
    if (environments.size() == 0)
      environments.add(new_environment);
    else {
      int index = getCurrEnvIndex();
      environments.add(index + 1, new_environment);
    }
    currEnvId = id(new_environment);
  }

  // remove last added environment from environment stack
  public void popEnvironment() {
    if (environments.size() == 0)
      return;
    int index = getCurrEnvIndex();
    environments.remove(index);
    if (index > 0)
      currEnvId = id(environments.get(index - 1));
    else
      currEnvId = null;
  }


  // get the current environment
  public Integer getEnvironmentId() {
    return currEnvId;
  }


  // set the current environment to the given environment id
  public void setEnvironmentId(Integer envId) {
    currEnvId = envId;
  }


  // add the given name to the current environment
  public void addName(String name) {
    // can't add if no environments exist
    if (environments.size() == 0)
      return;
    environments.get(getCurrEnvIndex()).put(name, null);
  }


  // set the given name to the given info object
  public void setInfo(String name, Object info) {
    Map<String,Object> env = getEnvForName(name);
    if (env != null)
      env.put(name, info);
  }


  // get the info associated with the given name
  public Object getInfo(String name) {
    Map<String,Object> env = getEnvForName(name);
    if (env != null)
      return env.get(name);
    return null;
  }

  
  // check if the name exists in the current environment or ancestor
  // environments on the stack
  public boolean nameExists(String name) {
    return getEnvForName(name) != null;
  }

  
  // check if the given name exists in the current environment
  public boolean nameExistsInCurrEnv(String name) {
    return nameExistsInEnv(name, getEnvironmentId());
  }

  
  // check if the given name exists in the given environment
  public boolean nameExistsInEnv(String name, Integer environmentId) {
    for (Map<String,Object> env : environments)
      if (id(env) == environmentId)
        return env.containsKey(name);
    return false;
  }

  
  // pretty print the symbol table for testing purposes
  public String toString() {
    String s = "";
    String r = "";
    for (int i = 0; i < environments.size(); ++i) {
      Map<String,Object> env = environments.get(i);
      Integer eid = id(env);
      s += r + eid + ":\n" + r + env + "\n";
      r += " ";
    }
    return s;
  }

  
  // ------------------------------
  // helper functions
  // ------------------------------

  
  // get the environment containing the given name starting from the
  // current environment and moving up the stack of environments
  private Map<String,Object> getEnvForName(String name) {
    int index = getCurrEnvIndex();
    for (int i = index; i >= 0; --i) {
      if (environments.get(i).containsKey(name))
        return environments.get(i);
    }
    return null;
  }

  
  // get the index of the current environment
  private int getCurrEnvIndex() {
    for (int i = 0; i < environments.size(); ++i)
      if (currEnvId == id(environments.get(i)))
        return i;
    return -1;
  }


  // get a unique identifier for a given object, which is used as the
  // current environment identifier
  private int id(Object obj) {
    return System.identityHashCode(obj);
  }


  // basic tests
  public static void main(String[] args) {
    SymbolTable t = new SymbolTable();
    t.pushEnvironment();

    // e1
    Integer e1 = t.getEnvironmentId();
    t.addName("x");
    t.setInfo("x", 20);

    // e2
    t.pushEnvironment();
    Integer e2 = t.getEnvironmentId();
    t.addName("x");
    t.setInfo("x", 30);

    // e1_2
    t.setEnvironmentId(e1);
    System.out.println("in e1: " + t.getInfo("x"));
    t.pushEnvironment();
    Integer e1_2 = t.getEnvironmentId();
    t.addName("x");
    t.setInfo("x", 25);
    System.out.println("in e1_2: " + t.getInfo("x"));
    t.setEnvironmentId(e2);
    System.out.println("in e2: " + t.getInfo("x"));

    System.out.println("Full table:\n" + t);

    // remove e1_2
    t.setEnvironmentId(e1_2);
    t.popEnvironment();
    System.out.println("Table after e1_2 removed:\n" + t);
    
    // remove e2
    t.setEnvironmentId(e2);
    t.popEnvironment();
    System.out.println("Table after e2 removed:\n" + t);

    // remove e1
    t.popEnvironment();
    System.out.println("Table after e1 removed:\n" + t);    
  }
}
