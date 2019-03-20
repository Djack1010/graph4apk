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

public class cPDG {

  int unId;
  String cPDGname;
  cPDGNode rootNode;
  UnitGraph unitGraph;
  Set<Unit> visitedStmt;
  Map<Integer, cPDGNode> cPDGNodes;


  public cPDG(UnitGraph cfg, String name) {

    this.unitGraph = cfg;
    this.cPDGname = name;
    this.unId = 2; //id 0 and 1 reserved for entry and exit node
    this.cPDGNodes = new TreeMap<Integer, cPDGNode>();
    this.visitedStmt = new HashSet<Unit>();

    //create Entry and Exit Node
    this.cPDGNodes.put(0, new cPDGNode(0, "ENTRY_NODE", "EXTRA", null));
    this.rootNode = this.cPDGNodes.get(0);
    this.cPDGNodes.put(1, new cPDGNode(1, "EXIT_NODE", "EXTRA", null));

    if (this.unitGraph.getHeads().size() == 0) {
      System.err.println("ERROR: cfg has no entry point");
    } else
      createcPDG();

  }

  private void createcPDG() {
    for (int i = 0; i < this.unitGraph.getHeads().size(); i++) {
      createCFG(this.unitGraph.getHeads().get(i), this.cPDGNodes.get(0));
    }
    createPDGDataEdges();
    createPDGControlEdges();
  }

  public cPDGNode getRootNode() {
    return this.rootNode;
  }

  public String getName() {
    return this.cPDGname;
  }

  public String getClassName() {
    return this.cPDGname.split("_")[0];
  }

  public String getMethodName() {
    return this.cPDGname.split("_")[1];
  }

  private int getUniqueID() {
    int toReturn = this.unId;
    this.unId++;
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
    cPDGNode newNode = new cPDGNode(this.getUniqueID(), unitNode.toString(), this.mapStmtType(unitNode), unitNode);
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
      if (defUnitID < 0)
        System.err.println("defUnit not found in cPDG!");
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
    ProgramDependenceGraph pdg = new HashMutablePDG(this.unitGraph);
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
      } else
        toReturn = node.getClass().toString().substring(node.getClass().toString().lastIndexOf('.') + 1);
    }
    return toReturn;
  }

  private void cleanVisitcPDG() {
    for (Map.Entry<Integer, cPDGNode> entry : this.cPDGNodes.entrySet()) {
      entry.getValue().setVisited(false);
    }
  }

  public void printcPDG(cPDGNode node) {
    System.out.println(node.getName());
    Set<cPDGNode> toVisit = new HashSet<cPDGNode>();
    for (cPDGEdge edge : node.getEdgesOut()) {
      System.out.println("\t -> " + edge.getDest().getId());
      toVisit.add(edge.getDest());
    }
    Iterator<cPDGNode> iter = toVisit.iterator();
    while (iter.hasNext()) {
      printcPDG(iter.next());
    }
  }

  //TODO test
  public String generateCCS() {
    String toReturn = "";
    this.cleanVisitcPDG();
    for (Map.Entry<Integer, cPDGNode> entry : this.cPDGNodes.entrySet()) {

      if (entry.getKey() == 1) //EXIT_NODE
        continue;

      if (!entry.getValue().getEdgesOut().isEmpty()) {

        toReturn = toReturn + "proc " + this.getMethodName() + entry.getValue().getId() + " = ";
        boolean init = true;

        for (cPDGEdge edge : entry.getValue().getEdgesOut()) {

          if (init)
            init = false;
          else
            toReturn = toReturn + " + ";

          if (edge.getEdgeType() == cPDGEdge.EdgeTypes.CONTROL_FLOW)
            toReturn = toReturn + "f_" + edge.getDest().getStmtType()
              + "." + this.getMethodName() + edge.getDest().getId();

          else if (edge.getEdgeType() == cPDGEdge.EdgeTypes.DATA_DEPENDENCE) {
            String varFlow = null;
            if (edge.getSource().getUnitNode() instanceof AssignStmt)
              varFlow = ((AssignStmt) edge.getSource().getUnitNode()).getLeftOp().toString().replaceAll("\\$", "");
            else if (edge.getSource().getUnitNode() instanceof IdentityStmt)
              varFlow = ((IdentityStmt) edge.getSource().getUnitNode()).getLeftOp().toString().replaceAll("\\$", "");
            else
              System.err.println("ERROR: Unit Node instance of " + edge.getSource().getUnitNode().getClass());
            toReturn = toReturn + "d_" + varFlow + "." + this.getMethodName() + edge.getDest().getId();

          } else if (edge.getEdgeType() == cPDGEdge.EdgeTypes.CONTROL_DEPENDENCE)
            toReturn = toReturn + "c." + this.getMethodName() + edge.getDest().getId();
        }

        toReturn = toReturn + "\n\n";

      }
    }

    toReturn = toReturn + "proc " + this.getMethodName() + "1=return.nil";
    return toReturn;
  }

}
