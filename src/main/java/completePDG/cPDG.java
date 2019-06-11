package completePDG;

import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.*;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.SmartLocalDefs;
import soot.toolkits.scalar.UnitValueBoxPair;

import java.util.*;
import java.util.regex.Pattern;

import static utility.Utility.clean4CCS;

public class cPDG {

  private int uniqueId;
  private int nodeIdIncrement;
  private boolean built = false;
  private String cPDGFullName;
  private String cPDGClassName;
  private String cPDGMethodName;
  private cPDGNode rootNode;
  private UnitGraph unitGraph;
  private ProgramDependenceGraph pdg;
  private Set<Unit> visitedStmt;
  private Map<String, Unit> invokeStmt;
  private Map<Integer, cPDGNode> cPDGNodes;


  public cPDG(UnitGraph cfg, String fname, String cname, String mname, int unId) {

    this.unitGraph = cfg;
    this.cPDGFullName = fname;
    this.cPDGClassName = cname;
    this.cPDGMethodName = mname;
    this.uniqueId=unId;
    this.nodeIdIncrement = 2; //id 0 and 1 reserved for entry and exit node
    this.cPDGNodes = new TreeMap<>();
    this.visitedStmt = new HashSet<>();
    this.invokeStmt = new HashMap<>();

    //create Entry and Exit Node
    this.cPDGNodes.put(0, new cPDGNode(0, "ENTRY_NODE", "EXTRA", null));
    this.rootNode = this.cPDGNodes.get(0);
    this.cPDGNodes.put(1, new cPDGNode(1, "EXIT_NODE", "EXTRA", null));

    if (this.unitGraph.getHeads().size() == 0) {
      System.err.println("ERROR: cfg has no entry point");
    }

    try {
      this.pdg = new HashMutablePDG(this.unitGraph);
      createcPDG();
    } catch (RuntimeException e){
      System.err.println("RUNTIME exception - ERROR in generating a PDG (Soot problem)");
    }

  }

  private void createcPDG() {
    for (int i = 0; i < this.unitGraph.getHeads().size(); i++) {
      createCFG(this.unitGraph.getHeads().get(i), this.cPDGNodes.get(0));
    }
    createPDGDataEdges();
    createPDGControlEdges();
    this.built = true;
  }

  public boolean isBuilt() { return this.built; }

  public cPDGNode getRootNode() {
    return this.rootNode;
  }

  public String getFullName() {
    return this.cPDGFullName;
  }

  public String getClassName() {
    return this.cPDGClassName;
  }

  public int getUniqueId() { return this.uniqueId; }

  public String getMethodName() {
    return this.cPDGMethodName;
  }

  public Map<String,Unit> getInvokeStmt() {
    return this.invokeStmt;
  }

  public UnitGraph getUnitGraph() {
    return this.unitGraph;
  }

  public String generateCCS() {
    String toReturn = "";
    this.cleanVisitcPDG();
    for (Map.Entry<Integer, cPDGNode> entry : this.cPDGNodes.entrySet()) {

      if (entry.getKey() == 1) //EXIT_NODE
        continue;

      if (!entry.getValue().getEdgesOut().isEmpty()) {

        toReturn = toReturn + "proc " + getProcName(entry.getValue()) + "=";
        boolean init = true;

        for (cPDGEdge edge : entry.getValue().getEdgesOut()) {

          if (init)
            init = false;
          else
            toReturn = toReturn + "+";

          if (edge.getEdgeType() == cPDGEdge.EdgeTypes.CONTROL_FLOW)
            toReturn = toReturn + "f_" + clean4CCS(edge.getDest().getStmtType())
              + "." + getProcName(edge.getDest());

          else if (edge.getEdgeType() == cPDGEdge.EdgeTypes.DATA_DEPENDENCE) {
            String varFlow = null;
            if (edge.getSource().getUnitNode() instanceof AssignStmt)
              varFlow = ((AssignStmt) edge.getSource().getUnitNode()).getLeftOp().toString().replaceAll("\\$", "DDOLLARO");
            else if (edge.getSource().getUnitNode() instanceof IdentityStmt)
              varFlow = ((IdentityStmt) edge.getSource().getUnitNode()).getLeftOp().toString().replaceAll("\\$", "DDOLLARO");
            else
              System.err.println("ERROR: Unit Node instance of " + edge.getSource().getUnitNode().getClass());
            toReturn = toReturn + "d_" + clean4CCS(varFlow) + "." + getProcName(edge.getDest());

          } else if (edge.getEdgeType() == cPDGEdge.EdgeTypes.CONTROL_DEPENDENCE)
            toReturn = toReturn + "c." + getProcName(edge.getDest());
        }

        toReturn = toReturn + "\n\n";

      }
    }

    toReturn = toReturn + "proc " + clean4CCS(this.getMethodName())
            + "1=return.nil";
    return toReturn;
  }

  private String getProcName(cPDGNode cpdg){
    return clean4CCS(this.getMethodName()) + cpdg.getId();
  }

  private int getNodeIdIncrement() {
    int toReturn = this.nodeIdIncrement;
    this.nodeIdIncrement++;
    return toReturn;
  }

  private void createCFG(Unit unitNode, cPDGNode precNode) {
    if (this.visitedStmt.contains(unitNode)) {//get into a code loop -> node already visited, only link with precnode
      precNode.newEdgeOut(this.cPDGNodes.get(findcPDGNode(unitNode)), cPDGEdge.EdgeTypes.CONTROL_FLOW);
      return;
    } else
      this.visitedStmt.add(unitNode);
    if (!(unitNode instanceof Stmt))
      System.err.println("NODE '" + unitNode + "' is not a statement!");
    cPDGNode newNode = new cPDGNode(this.getNodeIdIncrement(), unitNode.toString(), this.mapStmtType(unitNode), unitNode);
    this.cPDGNodes.put(newNode.getId(), newNode);
    newNode.newEdgeIn(precNode, cPDGEdge.EdgeTypes.CONTROL_FLOW);
    if (this.unitGraph.getSuccsOf(unitNode).isEmpty()) {
      newNode.newEdgeOut(this.cPDGNodes.get(1), cPDGEdge.EdgeTypes.CONTROL_FLOW);
    } else {
      for (Unit succNode : this.unitGraph.getSuccsOf(unitNode)) {
        createCFG(succNode, newNode);
      }
    }
  }

  private void createPDGDataEdges() {
    SimpleLiveLocals s = new SimpleLiveLocals(this.unitGraph);
    Iterator<Unit> gIt = this.unitGraph.iterator();
    // generate du-pairs
    while (gIt.hasNext()) {

      Unit defUnit = gIt.next();
      int defUnitID = findcPDGNode(defUnit);
      if (defUnitID < 0) {
        System.err.println("defUnit '" + defUnit.toString() + "' not found in cPDG!");
        continue;
      }
      SmartLocalDefs des = new SmartLocalDefs(this.unitGraph, s); // defs of local variables
      SimpleLocalUses uses = new SimpleLocalUses(this.unitGraph, des);

      List<UnitValueBoxPair> ul = uses.getUsesOf(defUnit);
      if (ul != null && ul.size() != 0) {
        Iterator<UnitValueBoxPair> iteraBoxPair = ul.iterator();
        while (iteraBoxPair.hasNext()) {
          Unit useUnit = iteraBoxPair.next().getUnit();
          int useUnitID = findcPDGNode(useUnit);
          if (useUnitID < 0)
            System.err.println("useUnit not found in cPDG!");
          this.cPDGNodes.get(defUnitID).newEdgeOut(this.cPDGNodes.get(useUnitID)
            , cPDGEdge.EdgeTypes.DATA_DEPENDENCE);
        }
      }
    }
  }

  private void createPDGControlEdges() {
    Iterator<PDGNode> iteraPDG = pdg.iterator();
    while (iteraPDG.hasNext()) {
      PDGNode node = iteraPDG.next();
      //skip REGION node and CFGnode with empty dependent set
      if (node.getDependents().isEmpty() || node.getType() == PDGNode.Type.REGION)
        continue;
      Unit fromDepUnit = null;
      if (node.getNode() instanceof Block) {
        Block blockNode = (Block) node.getNode();
        fromDepUnit = blockNode.getTail();
      } else
        System.err.println("Node " + node.toShortString() + " NOT contains a BLOCK!!!");
      int fromDepUnitID = findcPDGNode(fromDepUnit);
      for (PDGNode tempNode : node.getDependents()) {
        if (tempNode.getType() != PDGNode.Type.REGION)
          System.err.println("DEPENDENTS " + tempNode.toShortString()
            + " of " + node.toShortString() + " NOT REGION!!!");
        else {
          IRegion regionNode = null;
          if (tempNode.getNode() instanceof PDGRegion) {
            regionNode = (PDGRegion) tempNode.getNode();
          } else if (tempNode.getNode() instanceof Region) {
            regionNode = (Region) tempNode.getNode();
          } else
            System.err.println("NODE " + tempNode.toShortString()
              + " of " + node.toShortString() + " NOT CASTABLE REGION!!!");
          for (Unit toDepUnit : regionNode.getUnits()) {
            int toDepUnitID = findcPDGNode(toDepUnit);
            this.cPDGNodes.get(fromDepUnitID).newEdgeOut(this.cPDGNodes.get(toDepUnitID)
              , cPDGEdge.EdgeTypes.CONTROL_DEPENDENCE);
          }
        }
      }
    }
  }

  private int findcPDGNode(Unit unitNode) {
    int toReturn = -1;
    for (Map.Entry<Integer, cPDGNode> entry : this.cPDGNodes.entrySet()) {
      cPDGNode node = entry.getValue();
      //skip entry and exit node
      if (entry.getKey() == 0 || entry.getKey() == 1)
        continue;
      if (node.getUnitNode().equals(unitNode)) {
        toReturn = entry.getKey();
        break;
      }
    }
    return toReturn;
  }

  private String mapStmtType(Unit node) {
    String toReturn = null;
    if (node instanceof Stmt) {

      if (node instanceof AssignStmt) {
        Value rightOP = ((AssignStmt) node).getRightOp();
        if (rightOP.toString().contains("invoke"))
          toReturn = node.getClass().toString().substring(node.getClass().toString().lastIndexOf('.') + 1) + "-call";
        else
          toReturn = node.getClass().toString().substring(node.getClass().toString().lastIndexOf('.') + 1) + "-stat";
        //} else if (node instanceof IfStmt) {
        //  Value condition = ((IfStmt) node).getCondition();
        //  if (condition.toString().contains("invoke"))
        //    toReturn = node.getClass().toString().substring(node.getClass().toString().lastIndexOf('.') + 1) + "-call";
        //  else
        //    toReturn = node.getClass().toString().substring(node.getClass().toString().lastIndexOf('.') + 1) + "-stat";

      } else {

        if (node instanceof InvokeStmt) {
          //Example: virtualinvoke [$r5.]<cn.domob.android.ads.i: void a([android.content.Context])>([$r6])
          //[...] means not mandatory
          String invokeName = node.toString().replaceAll("\\$", "DDOLLARO");
          String regex = "^(interface|virtual|static|special)invoke " + //start line, invoke type
            "([a-zA-Z0-9]+\\.)?" + // nameClass. -> not mandatory, if attribute of a class but can be also just method
            "<[a-zA-Z0-9_.']+: " + // name.of.the.package: -> apparently, can also be 'name.package.'
            "[a-zA-Z0-9\\.\\[\\]]+ " + // returnType -> can be primitive (boolean, int) but also class (java.lang.String)
            "([a-zA-Z0-9_]+|<init>){1}" + // nameOfTheMethod
            "\\([a-zA-Z0-9\\.,_'\\[\\]]*\\)>" + // parametersType -> list of parameters type (int,byte[],com.some_class)
            "\\(.*\\)$"; // parameters and end of line

          if (!Pattern.matches(regex, invokeName)) {
            System.err.println("InvokeStmt does not match the regex");
            System.err.println(invokeName);
            for (int i = 0; i < invokeName.length(); i++) {
              String temp = invokeName.replaceAll(String.valueOf(invokeName.charAt(i)), "");
              //String temp = i != invokeName.length() - 1 ?
              //  invokeName.substring(0, i) + invokeName.substring(i + 1) :
              //  invokeName.substring(0, i);
              if (Pattern.matches(regex, temp)) {
                System.err.println("FAILURE CAUSED by char at position: " + i + " -> '" + invokeName.charAt(i) + "'");
                break;
              }
            }

          } else {
            this.invokeStmt.put(invokeName.split("<")[1].split(":")[0] + "_"
              + invokeName.split(":")[1].split(" ")[2].split("\\)")[0] + ")",node);
          }
        }

        toReturn = node.getClass().toString().substring(node.getClass().toString().lastIndexOf('.') + 1);
      }
    }
    return toReturn;
  }

  private void cleanVisitcPDG() {
    for (Map.Entry<Integer, cPDGNode> entry : this.cPDGNodes.entrySet()) {
      entry.getValue().setVisited(false);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((cPDGFullName == null) ? 0 : cPDGFullName.hashCode());
    result = prime * result + ((unitGraph == null) ? 0 : unitGraph.hashCode());
    result = prime * result + ((invokeStmt == null) ? 0 : invokeStmt.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    cPDG other = (cPDG) obj;
    if (this.cPDGFullName == null) {
      if (other.getFullName() != null)
        return false;
    } else if (!this.cPDGFullName.equals(other.getFullName()))
      return false;
    if (this.unitGraph == null) {
      if (other.getUnitGraph() != null)
        return false;
    } else if (!this.unitGraph.equals(other.getUnitGraph()))
      return false;
    if (this.invokeStmt == null) {
      return other.getInvokeStmt() == null;
    } else return this.invokeStmt.equals(other.getInvokeStmt());
  }

}
