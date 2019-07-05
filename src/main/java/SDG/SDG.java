package SDG;

import completePDG.cPDG;
import soot.Unit;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphEdge;
import soot.util.dot.DotGraphNode;
import utility.Utility;

import java.util.*;

import static utility.Utility.clean4CCS;

public class SDG {

  private Map<String, cPDG> cPDGAvailable;
  private Set<String> notActiveBodycPDG;
  private Set<String> failedPDG;
  private Map<cPDG, Set<SDGEdge>> sdg;
  private DotGraph dotGraph = null;

  public SDG() {
    this.cPDGAvailable = new HashMap<>();
    this.notActiveBodycPDG = new HashSet<>();
    this.failedPDG = new HashSet<>();
    this.sdg = new HashMap<>();
  }

  public void addcPDG(cPDG cpdg) {
    this.cPDGAvailable.put(cpdg.getFullName(), cpdg);
  }

  public void addNotActiveBody(String name) {
    this.notActiveBodycPDG.add(name);
  }

  public void addFailedPDG(cPDG cpdg) { this.failedPDG.add(cpdg.getFullName()); }

  public Map<String, cPDG> getcPDGAvailable() { return this.cPDGAvailable; }

  public DotGraph drawcSDG() {
    this.dotGraph = new DotGraph("SDG");
    this.dotGraph.setNodeShape("record");
    DotGraphNode dotNodeLib = this.dotGraph.getNode("LIBRARY CALL");
    dotNodeLib.setLabel("{ LIBRARY CALL }");
    DotGraphNode dotNodeNAB = this.dotGraph.getNode("NOT ACTIVE BODY");
    dotNodeNAB.setLabel("{ NOT ACTIVE BODY }");
    for (Map.Entry<String, cPDG> entrycPDG : this.cPDGAvailable.entrySet()) {
      if (this.sdg.get(entrycPDG.getValue())==null){
        DotGraphNode dotNode = this.dotGraph.drawNode(String.valueOf(entrycPDG.getValue().getFullName()));
        dotNode.setLabel("{" + entrycPDG.getValue().getFullName()
          .replace("<", "\\<").replace(">", "\\>")
          .replace("{", "\\{").replace("}", "\\}") + "}");
      } else {
        DotGraphNode dotNode = this.dotGraph.drawNode(String.valueOf(entrycPDG.getValue().getFullName()));
        dotNode.setLabel("{" + entrycPDG.getValue().getFullName()
          .replace("<", "\\<").replace(">", "\\>")
          .replace("{", "\\{").replace("}", "\\}") + "}");
        Set<SDGEdge> tempSDGEdgeSet = new HashSet<SDGEdge>();
        tempSDGEdgeSet = this.sdg.get(entrycPDG.getValue());
        for ( SDGEdge edge : tempSDGEdgeSet) {
          if ( edge.getDest() == null  && edge.isLib())
            this.dotGraph.drawEdge(String.valueOf(entrycPDG.getValue().getFullName()),
            "LIBRARY CALL");
          else if (edge.getDest() == null  && !edge.isLib() ){
            this.dotGraph.drawEdge(String.valueOf(entrycPDG.getValue().getFullName()),
              "NOT ACTIVE BODY");
          } else if (edge.getDest() != null){
            this.dotGraph.drawEdge(String.valueOf(entrycPDG.getValue().getFullName()),
              edge.getDest().getFullName());
          }
        }
      }
    }
    return dotGraph;
  }

  /**
  public String generateCCS() {
    String startingProc = "proc StartingNode=";
    boolean init = true;
    String toReturn = "";
    for (Map.Entry<String, cPDG> entrycPDG : this.cPDGAvailable.entrySet()) {

      if (this.sdg.get(entrycPDG.getValue())==null)
        continue;

      if (init)
        startingProc = startingProc + "e." + clean4CCS(entrycPDG.getValue().getFullName());
      else
        startingProc = startingProc + "+e." + clean4CCS(entrycPDG.getValue().getFullName());

      toReturn = toReturn + "proc " + clean4CCS(entrycPDG.getValue().getFullName()) + "=";
      init = true;

      Set<SDGEdge> tempSDGEdgeSet = new HashSet<SDGEdge>();
      tempSDGEdgeSet = this.sdg.get(entrycPDG.getValue());
      for ( SDGEdge edge : tempSDGEdgeSet) {
        if (init)
          init = false;
        else
          toReturn = toReturn + "+";

        if ( edge.getDest() == null  && edge.isLib())
          toReturn = toReturn + "e.LibCall(" + clean4CCS(edge.getInvokeStmt()) + ")";
        else if (edge.getDest() == null  && !edge.isLib() ){
          toReturn = toReturn + "e.NotActiveBody";
        } else if (edge.getDest() != null){
          toReturn = toReturn + "e." + clean4CCS(edge.getDest().getFullName());
          this.dotGraph.drawEdge(String.valueOf(clean4CCS(entrycPDG.getValue().getFullName())),
                  clean4CCS(edge.getDest().getFullName()));
        }

      }

      toReturn = toReturn + "\n\n";

    }

    startingProc = startingProc + "\n\n";
    //toReturn = toReturn + "proc " + this.getFullName() + "1=return.nil";
    return startingProc + toReturn;

  }
  public String generateSimpleCCS() {
    String startingProc = "proc StartingNode=";
    boolean supInit = true;
    String toReturn = "";
    for (Map.Entry<String, cPDG> entrycPDG : this.cPDGAvailable.entrySet()) {

      if (this.sdg.get(entrycPDG.getValue()) == null)
        continue;

      String tempProc = "proc " + clean4CCS(entrycPDG.getValue().getFullName()) + "=";
      boolean init = true;
      boolean valid = false;

      Set<SDGEdge> tempSDGEdgeSet = new HashSet<SDGEdge>();
      tempSDGEdgeSet = this.sdg.get(entrycPDG.getValue());
      for (SDGEdge edge : tempSDGEdgeSet) {
        if (init)
          init = false;
        else
          tempProc = tempProc + "+";

        if (edge.getDest() == null && edge.isLib())
          tempProc = tempProc + "e.LibCall*" + clean4CCS(edge.getInvokeStmt()) + "*";
        else if (edge.getDest() == null && !edge.isLib()) {
          valid = true;
          tempProc = tempProc + "e.NotActiveBody";
        } else if (edge.getDest() != null) {
          valid = true;
          tempProc = tempProc + "e." + clean4CCS(edge.getDest().getFullName());
          this.dotGraph.drawEdge(String.valueOf(clean4CCS(entrycPDG.getValue().getFullName())),
                  clean4CCS(edge.getDest().getFullName()));
        }

      }

      if (valid) {
        toReturn = toReturn + tempProc + "\n\n";
        if (supInit) {
          startingProc = startingProc + "e." + clean4CCS(entrycPDG.getValue().getFullName());
          supInit = false;
        } else
          startingProc = startingProc + "+e." + clean4CCS(entrycPDG.getValue().getFullName());
      }

    }

    startingProc = startingProc + "\n\n";
    //toReturn = toReturn + "proc " + this.getFullName() + "1=return.nil";
    return startingProc + toReturn;

  }
  public String generateSimpleCCS_NEW() {
    String startingProc = "proc M0=";
    boolean supInit = true;
    String toReturn = "";
    for (Map.Entry<String, cPDG> entrycPDG : this.cPDGAvailable.entrySet()) {

      if (this.sdg.get(entrycPDG.getValue()) == null)
        continue;

      String tempProc = "proc M" + entrycPDG.getValue().getUniqueId() + "=";
      boolean init = true;
      boolean valid = false;

      Set<SDGEdge> tempSDGEdgeSet = new HashSet<SDGEdge>();
      tempSDGEdgeSet = this.sdg.get(entrycPDG.getValue());
      for (SDGEdge edge : tempSDGEdgeSet) {
        if (init)
          init = false;
        else
          tempProc = tempProc + "+";

        if (edge.getDest() == null && edge.isLib())
          tempProc = tempProc + clean4CCS(edge.getInvokeStmt()) + ".LibCall";
        else if (edge.getDest() == null && !edge.isLib()) {
          valid = true;
          tempProc = tempProc + clean4CCS(edge.getInvokeStmt()) + ".NotActiveBody";
        } else if (edge.getDest() != null) {
          valid = true;
          tempProc = tempProc + clean4CCS(edge.getDest().getFullName()) + ".M" + edge.getDest().getUniqueId();
        }

      }

      if (valid) {
        toReturn = toReturn + tempProc + "\n\n";
        if (supInit) {
          supInit = false;
          startingProc = startingProc + clean4CCS(entrycPDG.getValue().getFullName()) + ".M"
                  + entrycPDG.getValue().getUniqueId();
        } else
          startingProc = startingProc + "+" + clean4CCS(entrycPDG.getValue().getFullName()) + ".M"
                  + entrycPDG.getValue().getUniqueId();
      }

    }

    startingProc = startingProc + "\n\n";
    //toReturn = toReturn + "proc " + this.getFullName() + "1=return.nil";
    return startingProc + toReturn;

  }
   **/

  public String generateCCS_NEW() {
    String startingProc = "proc M0=";
    boolean init = true;
    String toReturn = "";
    //Set<Integer> proc = new HashSet<Integer>();
    //Set<Integer> call = new HashSet<Integer>();
    for (Map.Entry<String, cPDG> entrycPDG : this.cPDGAvailable.entrySet()) {

      if (this.sdg.get(entrycPDG.getValue())==null) {
        toReturn = toReturn + "proc M" + entrycPDG.getValue().getUniqueId() + "=nil\n\n";
        //proc.add(entrycPDG.getValue().getUniqueId());
        continue;
      }

      if (init)
        startingProc = startingProc + clean4CCS(entrycPDG.getValue().getFullName()) + ".M"
                + entrycPDG.getValue().getUniqueId();
      else
        startingProc = startingProc + "+" + clean4CCS(entrycPDG.getValue().getFullName()) + ".M"
                + entrycPDG.getValue().getUniqueId();

      //proc.add(entrycPDG.getValue().getUniqueId());
      toReturn = toReturn + "proc M" + entrycPDG.getValue().getUniqueId() + "=";
      init = true;

      Set<SDGEdge> tempSDGEdgeSet = new HashSet<SDGEdge>();
      tempSDGEdgeSet = this.sdg.get(entrycPDG.getValue());
      for ( SDGEdge edge : tempSDGEdgeSet) {
        if (init)
          init = false;
        else
          toReturn = toReturn + "+";

        if ( edge.getDest() == null  && edge.isLib())
          toReturn = toReturn + clean4CCS(edge.getInvokeStmt()) + ".LibCall";
        else if (edge.getDest() == null  && !edge.isLib() ){
          toReturn = toReturn + clean4CCS(edge.getInvokeStmt()) + ".NotActiveBody";
        } else if (edge.getDest() != null) {
          toReturn = toReturn + clean4CCS(edge.getDest().getFullName()) + ".M" + edge.getDest().getUniqueId();
          //call.add(edge.getDest().getUniqueId());
        }

      }

      toReturn = toReturn + "\n\n";

    }

    startingProc = startingProc + "\n\n";
    toReturn = toReturn + "proc LibCall=nil\n\nproc NotActiveBody=nil";
    return startingProc + toReturn;

  }

  public String matchInvokecPDG() {
    int matched = 0;
    int lib = 0;
    int notActiveBody = 0;
    int failed = 0;
    int tot = 0;
    String toReturn = "INFO -> available_cPDG: " + this.cPDGAvailable.size() + " notActiveBody: " + this.notActiveBodycPDG.size()
      + " failedPDG: " + this.failedPDG.size() + "\n";
    for (Map.Entry<String, cPDG> entrycPDG : this.cPDGAvailable.entrySet()) {
      Set<SDGEdge> tempSDGEdgeSet = new HashSet<SDGEdge>();
      for (Map.Entry<String, Unit> entryInvoke : entrycPDG.getValue().getInvokeStmt().entrySet()) {
        tot++;
        if (this.cPDGAvailable.containsKey(entryInvoke.getKey())) {
          tempSDGEdgeSet.add(new SDGEdge(entrycPDG.getValue(), entryInvoke.getValue(), entryInvoke.getKey(), this.cPDGAvailable.get(entryInvoke.getKey())));
          matched++;
        } else if (this.libCall(entryInvoke.getKey())) {
          tempSDGEdgeSet.add(new SDGEdge(entrycPDG.getValue(), entryInvoke.getValue(), entryInvoke.getKey(), null, true));
          lib++;
        } else if (this.notActiveBodycPDG.contains(entryInvoke.getKey())) {
          tempSDGEdgeSet.add(new SDGEdge(entrycPDG.getValue(), entryInvoke.getValue(), entryInvoke.getKey(), null));
          notActiveBody++;
        } else {
          //System.err.println("FROM " + entrycPDG.getValue().getName() + " CANNOT MATCH CALL: " + entryInvoke.getKey());
          failed++;
        }
      }
      if (tempSDGEdgeSet.isEmpty())
        this.sdg.put(entrycPDG.getValue(), null);
      else
        this.sdg.put(entrycPDG.getValue(), tempSDGEdgeSet);
    }
    int identified = matched + lib + notActiveBody;
    toReturn+="MATCHED: " + matched + " LIB: " + lib + " NotActiveBody: " + notActiveBody + " FAILED: " + failed + "\n";
    float percentage = ((float) identified/ (float) tot) * 100;
    toReturn+="IDENTIFIED: " + identified + " OUT OF: " + tot + " --> " + percentage + "%" + "\n";
    return toReturn;
  }

  private boolean libCall(String invoke) {
    return invoke.startsWith("java.") || invoke.startsWith("javax.")
      || invoke.startsWith("org.apache.") || invoke.startsWith("org.json.")
      || invoke.startsWith("android.accessibilityservice.")
      || invoke.startsWith("android.accounts.") || invoke.startsWith("android.animation.")
      || invoke.startsWith("android.annotation.") || invoke.startsWith("android.app.")
      || invoke.startsWith("android.appwidget.") || invoke.startsWith("android.bluetooth.")
      || invoke.startsWith("android.companion.") || invoke.startsWith("android.content.")
      || invoke.startsWith("android.database.") || invoke.startsWith("android.drm.")
      || invoke.startsWith("android.gesture.") || invoke.startsWith("android.graphics.")
      || invoke.startsWith("android.hardware.") || invoke.startsWith("android.icu.")
      || invoke.startsWith("android.inputmethodservice.") || invoke.startsWith("android.location.")
      || invoke.startsWith("android.media.") || invoke.startsWith("android.mtp.")
      || invoke.startsWith("android.net.") || invoke.startsWith("android.nfc.")
      || invoke.startsWith("android.opengl.") || invoke.startsWith("android.os.")
      || invoke.startsWith("android.preference.") || invoke.startsWith("android.print.")
      || invoke.startsWith("android.printservice.") || invoke.startsWith("android.provider.")
      || invoke.startsWith("android.renderscript.") || invoke.startsWith("android.sax.")
      || invoke.startsWith("android.se.") || invoke.startsWith("android.security.")
      || invoke.startsWith("android.service.") || invoke.startsWith("android.speech.")
      || invoke.startsWith("android.system.") || invoke.startsWith("android.telecom.")
      || invoke.startsWith("android.telephony.") || invoke.startsWith("android.test.")
      || invoke.startsWith("android.text.") || invoke.startsWith("android.util")
      || invoke.startsWith("android.view.") || invoke.startsWith("android.transition.")
      || invoke.startsWith("android.webkit.") || invoke.startsWith("android.widget.");
  }

  public void completeAnalysis(){
    int base = 0;
    String name = "";
    for (Map.Entry<String, cPDG> entrycPDG : this.cPDGAvailable.entrySet()) {
      int temp = getConnectedMethod(entrycPDG.getKey(), new StringBuilder());
      if (temp > base){
        name = entrycPDG.getKey();
        base = temp;
      }
    }
    System.out.println("The winner is '" + name + "' with " + String.valueOf(base) + " levels");
  }

  public int getConnectedMethod(String targetMethod, StringBuilder toPrint){
    if (targetMethod == null)
      return 0;
    if (this.cPDGAvailable.containsKey(targetMethod)) {
      cleanVisit();
      return getLinkedMethods(this.cPDGAvailable.get(targetMethod), 1, toPrint);
    } else {
      System.err.println(targetMethod + " NOT FOUND!");
      return 0;
    }
  }

  public int getConnectedMethod_PARSER(String targ, StringBuilder toPrint){
    if (targ == null)
      return 0;
    String targetMethod = targ.toUpperCase();
    ArrayList<String> matched = new ArrayList<>();
    int matchedValue = 100000;
    for (Map.Entry<String, cPDG> entrycPDG : this.cPDGAvailable.entrySet()) {
      String toEvaluate = entrycPDG.getKey().replaceAll("\\.","").replaceAll("<","")
        .replaceAll(">","").replaceAll("\\(","").replaceAll("\\)","").toUpperCase();
      if (targetMethod.contains(toEvaluate.split("_")[0])) {
        String restTarget = targetMethod.split(toEvaluate.split("_")[0])[1];
        if ( restTarget.contains(toEvaluate.split("_")[1]) ) {
          int newMatchedVal = restTarget.length()-toEvaluate.split("_")[1].length();
          if ( newMatchedVal < matchedValue) {
            matchedValue = newMatchedVal;
            matched.clear();
            matched.add(entrycPDG.getKey());
          } else if ( newMatchedVal == matchedValue)
            matched.add(entrycPDG.getKey());
        }
      }
    }
    if ( matched.isEmpty() ) {
      toPrint.append(" NOT FOUND!");
      return 0;
    } else {
      for( String toLoop : matched) {
        toPrint.append(toLoop + "\n");
        getLinkedMethods(this.cPDGAvailable.get(toLoop), 1, toPrint);
      }
      return 1;
    }
  }

  private int getLinkedMethods(cPDG target, int level, StringBuilder toPrint){
    Set<SDGEdge> tempSDGEdgeSet = this.sdg.get(target);
    int toReturn = level;
    if ( tempSDGEdgeSet != null) {
      for (SDGEdge edge : tempSDGEdgeSet) {
        String lvl = level + "  |_";
        for(int i=0;i<level-1;i++){
          lvl=lvl+"_";
        }
        toPrint.append(lvl);

        if (edge.isVisited())
          toPrint.append(" RECURSION -> ");

        if (edge.getDest() != null)
          toPrint.append(" M" + edge.getDest().getUniqueId() + ": ");
        else if (edge.isLib())
          toPrint.append(" LIBRARY: ");
        else
          toPrint.append(" NOT_FOUND: ");
        toPrint.append(edge.getInvokeStmt() + "\n");

        if (!edge.isVisited()) {
          edge.setVisited(true);
          if(edge.getDest() != null)
            toReturn = getLinkedMethods(edge.getDest(), level+1, toPrint);
        }

      }
    } else
      toPrint.append(level + "  |-> STOP" + "\n");
    return toReturn;
  }

  public subGraph getSubGraph(cPDG target1, cPDG target2) {
    this.cleanVisit();
    subGraph subgraph = new subGraph(target1);
    RECgetSubGraph(target1, target2, subgraph);
    if (subgraph.getTarget() == null){
      this.cleanVisit();
      subGraph subgraph2 = new subGraph(target2);
      RECgetSubGraph(target2, target1, subgraph2);
      return subgraph2;
    } else
      return subgraph;
  }

  private void RECgetSubGraph(cPDG target1, cPDG target2, subGraph subgraph){
    Set<SDGEdge> tempSDGEdgeSet = this.sdg.get(target1);
    if (subgraph.target == null)
      subgraph.addPathID(target1.getUniqueId());
    Set<cPDG> toLoop = new HashSet<>();
    if ( tempSDGEdgeSet != null) {
      for (SDGEdge edge : tempSDGEdgeSet) {
        if (!edge.isVisited()) {
          edge.setVisited(true);
          subgraph.addSDGEdge(edge);
          subgraph.addcPDGNode(edge.getSource());
          if (edge.getDest() != null) {
            toLoop.add(edge.getDest());
            if (edge.getDest().equals(target2)){
              subgraph.setTarget(edge.getDest());
              subgraph.addPathID(edge.getDest().getUniqueId());
            }
          }
        }
      }
      if (toLoop.isEmpty())
        return;
      else{
        for(cPDG c: toLoop){
          RECgetSubGraph(c, target2, subgraph);
        }
      }
    }
    if (subgraph.target == null)
      subgraph.removePathID();
  }

  private void cleanVisit(){
    for (Map.Entry<cPDG, Set<SDGEdge>> entrySDG : this.sdg.entrySet()) {
      if ( entrySDG.getValue() != null ){
        for (SDGEdge edge : entrySDG.getValue()) {
          edge.setVisited(false);
        }
      }
    }
  }

  public class subGraph{
    cPDG startNode;
    cPDG target;
    List<Integer> path;
    Set<SDGEdge> subGraphEdge;
    Set<cPDG> subGraphNode;

    public subGraph(cPDG rootNode){
      this.startNode = rootNode;
      this.target = null;
      this.subGraphEdge = new HashSet<SDGEdge>();
      this.subGraphNode = new HashSet<cPDG>();
      this.path = new ArrayList<>();
    }

    public void addSDGEdge(SDGEdge edge) { this.subGraphEdge.add(edge); }
    public void addcPDGNode(cPDG node) { this.subGraphNode.add(node); }

    public void setTarget(cPDG cpdg) { this.target = cpdg; }

    public cPDG getStartNode() {
      return startNode;
    }

    public cPDG getTarget() {
      return target;
    }

    public Set<SDGEdge> getSubGraphEdge() {
      return subGraphEdge;
    }
    public Set<cPDG> getSubGraphNode() {
      return subGraphNode;
    }

    public void addPathID(int i) { this.path.add(i); }
    public void removePathID() { this.path.remove(this.path.size()-1); }

    public String genCCS(){
      String startingProc = "proc M0=";
      boolean init = true;
      String toReturn = "";
      for (cPDG node : this.subGraphNode) {

        if (init)
          startingProc = startingProc + "M" + node.getUniqueId();
        else
          startingProc = startingProc + "+M" + node.getUniqueId();

        toReturn = toReturn + "proc M" + node.getUniqueId() + "=";
        init = true;
        Set<SDGEdge> tempSDGEdgeSet = new HashSet<SDGEdge>();
        tempSDGEdgeSet = sdg.get(node);
        for (SDGEdge edge : tempSDGEdgeSet) {
          if (init)
            init = false;
          else
            toReturn = toReturn + "+";

          if (edge.getDest() == null && edge.isLib())
            toReturn = toReturn + this.cleanInvokeStmt(edge.getInvokeStmt(), 1) + ".L"
              + this.cleanInvokeStmt(edge.getInvokeStmt(), 0) + ".nil";
          else if (edge.getDest() == null && !edge.isLib()) {
            toReturn = toReturn + this.cleanInvokeStmt(edge.getInvokeStmt(), 1) + ".N"
              + this.cleanInvokeStmt(edge.getInvokeStmt(), 0) + ".nil";
          } else if (edge.getDest() != null) {
            toReturn = toReturn + this.cleanInvokeStmt(edge.getInvokeStmt(), 1) + ".M" + edge.getDest().getUniqueId();
          }

        }

        toReturn = toReturn + "\n\n";
      }


      startingProc = startingProc + "\n\n";
      return startingProc + toReturn;
    }

    public void printInfo(){
      System.out.println("STARTER: " + this.startNode.getFullName());
      if (this.target != null) {
        System.out.println("TARGET: " + this.target.getFullName());
        System.out.print("PATH:");
        for(Integer n : this.path){
          System.out.print(" " + n + " ->");
        }
        System.out.println();
      } else {
        System.out.println("TARGET: NOT FOUND!");
        return;
      }
      for(SDGEdge e : this.subGraphEdge){
        if(e.getDest() != null)
          System.out.println("EDGE from '" + e.getSource().getUniqueId() + "' to '" + e.getDest().getUniqueId() + "'");
        else if (e.isLib())
          System.out.println("EDGE from '" + e.getSource().getUniqueId() + "' to LIB");
        else
          System.out.println("EDGE from '" + e.getSource().getUniqueId() + "' to NOT FOUND");
        System.out.println("\tCalling " + e.getInvokeStmt().split("\\(")[0]
          + " and passing (" + e.getInvokeStmt().split("\\(")[1]);
      }
    }

    private String cleanInvokeStmt(String stmt, int n){
      String base = stmt.split("\\(")[n].replaceAll("\\)", "").replaceAll("\\.", "")
        .replaceAll("<", "").replaceAll(">", "").replaceAll(",", ".");
      if(base.isEmpty())
        return "-";
      else
        return base;
    }
  }

}
