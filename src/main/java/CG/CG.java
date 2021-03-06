package CG;

import completePDG.cPDG;
import soot.Unit;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphNode;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import static utility.Utility.clean4CCS;

public class CG {

  private Map<String, cPDG> cPDGAvailable;
  private Set<String> notActiveBodycPDG;
  private Set<String> failedPDG;
  private Map<cPDG, Set<CGEdge>> sdg;
  private Map<cPDG, Set<CGEdge>> utilityEdges;
  private ArrayList<String> libraries;
  private cPDG rootNode;

  public CG() {
    this.rootNode = null;
    this.cPDGAvailable = new HashMap<>();
    this.notActiveBodycPDG = new HashSet<>();
    this.failedPDG = new HashSet<>();
    this.sdg = new HashMap<>();
    this.libraries = new ArrayList<String>(Arrays.asList("java.", "javax.", "org.apache.", "org.json.",
            "android.accessibilityservice.", "android.accounts.", "android.animation.", "android.annotation.",
            "android.app.", "android.appwidget.", "android.bluetooth.", "android.companion.", "android.content.",
            "android.database.", "android.drm.", "android.gesture.", "android.graphics.", "android.hardware.",
            "android.icu.", "android.inputmethodservice.", "android.location.", "android.media.", "android.mtp.",
            "android.net.", "android.nfc.", "android.opengl.", "android.os.", "android.preference.", "android.print.",
            "android.printservice.", "android.provider.", "android.renderscript.", "android.sax.", "android.se.",
            "android.security.", "android.service.", "android.speech.", "android.system.", "android.telecom.",
            "android.telephony.", "android.test.", "android.text.", "android.util", "android.view.",
            "android.transition.", "android.webkit.", "android.widget.")
    );
  }

  public void initConnectedGraph(){
    this.rootNode = new cPDG("ROOT", null, 0);
    this.cPDGAvailable.put("ROOT", this.rootNode);
    this.utilityEdges = new HashMap<>();
  }

  // IF return null, rootNode not set, thus not Connected Graph
  public cPDG getRootNode() { return this.rootNode; }

  public void addcPDG(cPDG cpdg) {
    this.cPDGAvailable.put(cpdg.getFullName(), cpdg);
  }

  public void addUtilityEdge(cPDG source, cPDG dest) {
    Set<CGEdge> tempCGEdgeSet;
    if (this.utilityEdges.containsKey(source))
      tempCGEdgeSet = this.utilityEdges.get(source);
    else {
      tempCGEdgeSet = new HashSet<CGEdge>();
      this.utilityEdges.put(source, tempCGEdgeSet);
    }
    tempCGEdgeSet.add(new CGEdge(source, dest));
  }

  public void addNotActiveBody(String name) {
    this.notActiveBodycPDG.add(name);
  }

  public void addFailedPDG(cPDG cpdg) { this.failedPDG.add(cpdg.getFullName()); }

  public Map<String, cPDG> getcPDGAvailable() { return this.cPDGAvailable; }

  public class CGData{
    public ArrayList<String> nodeXfeatures = new ArrayList<>();
    public ArrayList<String> edges = new ArrayList<>();

    public void addNode(String node_features){
      this.nodeXfeatures.add(node_features);
    }

    public void addEdge(CGEdge edge, String idDest){
      String e = edge.getSource().getUniqueId() + ", " + idDest;
      if (getRootNode() != null) {
        String l = edge.getTypeEdge() == -1 ? "1" : "0";
        e += ", " + l;
      }
      this.edges.add(e);
    }

    public void printNodeOnFile(String path){
      try (PrintWriter out = new PrintWriter(path)) {
        for(String s: this.nodeXfeatures){
          out.println(s);
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }

    public void printEdgeOnFile(String path){
      try (PrintWriter out = new PrintWriter(path)) {
        for(String s: this.edges){
          out.println(s);
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }

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
      Set<CGEdge> tempCGEdgeSet = new HashSet<CGEdge>();
      for (Map.Entry<String, Unit> entryInvoke : entrycPDG.getValue().getInvokeStmt().entrySet()) {
        tot++;
        if (this.cPDGAvailable.containsKey(entryInvoke.getKey())) {
          tempCGEdgeSet.add(new CGEdge(entrycPDG.getValue(), entryInvoke.getValue(), entryInvoke.getKey(), this.cPDGAvailable.get(entryInvoke.getKey())));
          matched++;
        } else if (this.libCall(entryInvoke.getKey()) != 0) {// entryInvoke is Library
          tempCGEdgeSet.add(new CGEdge(entrycPDG.getValue(), entryInvoke.getValue(), entryInvoke.getKey(), null, this.libCall(entryInvoke.getKey())));
          lib++;
        } else if (this.notActiveBodycPDG.contains(entryInvoke.getKey())) {
          tempCGEdgeSet.add(new CGEdge(entrycPDG.getValue(), entryInvoke.getValue(), entryInvoke.getKey(), null));
          notActiveBody++;
        } else {
          //System.err.println("FROM " + entrycPDG.getValue().getName() + " CANNOT MATCH CALL: " + entryInvoke.getKey());
          failed++;
        }
      }
      if (tempCGEdgeSet.isEmpty())
        this.sdg.put(entrycPDG.getValue(), null);
      else
        this.sdg.put(entrycPDG.getValue(), tempCGEdgeSet);
    }
    int identified = matched + lib + notActiveBody;
    toReturn+="MATCHED: " + matched + " LIB: " + lib + " NotActiveBody: " + notActiveBody + " FAILED: " + failed + "\n";
    float percentage = ((float) identified/ (float) tot) * 100;
    toReturn+="IDENTIFIED '" + identified + "' OUT OF '" + tot + "' --> " + percentage + "%" + "\n";
    return toReturn;
  }

  public CGData genCG(){
    CGData data = new CGData();
    for (Map.Entry<String, cPDG> entrycPDG : this.cPDGAvailable.entrySet()) {
      data.addNode(entrycPDG.getValue().getUniqueId() + ", " +
              entrycPDG.getValue().getStmtFrequency().toString().replaceAll("\\[", "").replaceAll("]", ""));
      if (this.sdg.get(entrycPDG.getValue())!=null){
        Set<CGEdge> tempCGEdgeSet = this.sdg.get(entrycPDG.getValue());
        for ( CGEdge edge : tempCGEdgeSet) {
          if ( edge.getDest() == null  && edge.isLib())
            ; // Library Node, not created yet...
          else if (edge.getDest() == null  && !edge.isLib() ){
            ; // Do nothing, because the dest has no active body, thus no features set and no node
          } else if (edge.getDest() != null){
            data.addEdge(edge, String.valueOf(edge.getDest().getUniqueId()));
          } else {
            System.err.println("Unexpected Edge null but not lib... exiting!");
            System.exit(1);
          }
        }
      } else if (this.getRootNode() != null && this.utilityEdges.get(entrycPDG.getValue())!=null){
        Set<CGEdge> tempCGEdgeSet = this.utilityEdges.get(entrycPDG.getValue());
        for ( CGEdge edge : tempCGEdgeSet)
          data.addEdge(edge, String.valueOf(edge.getDest().getUniqueId()));
      }
    }
    return data;
  }

  // IF uniqueNodePackage, all the calls to a package lead to the same node (per package), otherwise each call lead to
  //  (potentially) different nodes (because also 2 calls to the same package may call different functions/methods).
  //  When !uniqueNodePackage, the calls try to be merged by comparing 'invokeStmt'.
  //  NB: even if the package has more nodes, all the nodes of the same package share the same features array
  public CGData genCGLib(Integer uniqueIndex, boolean uniqueNodePackage){
    CGData data = new CGData();
    String libFeatures = "";
    HashMap<Integer, HashMap<String, Integer>> mapLibNode = new HashMap<>();

    for (Map.Entry<String, cPDG> entrycPDG : this.cPDGAvailable.entrySet()) {

      if (libFeatures.equals("")) {// first loop, to check num of JimpleStmt and then place libIndex at the end of features array
        for (int i = 0; i < entrycPDG.getValue().getStmtFrequency().size(); i++){
          libFeatures += "0, ";
        }
      }

      data.addNode(entrycPDG.getValue().getUniqueId() + ", " +
              entrycPDG.getValue().getStmtFrequency().toString().replaceAll("\\[", "").replaceAll("]", "")
              + ", 0"); // add one index to mark node as notLib
      if (this.sdg.get(entrycPDG.getValue())!=null){
        Set<CGEdge> tempCGEdgeSet = this.sdg.get(entrycPDG.getValue());
        for ( CGEdge edge : tempCGEdgeSet) {
          if ( edge.getDest() == null  && edge.isLib()){

            String invokeStmt = edge.getInvokeStmt();
            Integer indexLib = edge.getTypeEdge();
            Integer indexNode = null;

            if (uniqueNodePackage){
              if (mapLibNode.containsKey(indexLib))
                indexNode = mapLibNode.get(indexLib).get("unique");
              else {
                HashMap<String, Integer> newPackageNode = new HashMap<>();
                newPackageNode.put("unique", uniqueIndex);
                mapLibNode.put(indexLib, newPackageNode);
                indexNode = uniqueIndex;
                data.addNode(indexNode + ", " + libFeatures + indexLib); // add one value for lib index
                uniqueIndex++;
              }
            } else {
              if (mapLibNode.containsKey(indexLib)) {
                if (mapLibNode.get(indexLib).containsKey(invokeStmt))
                  indexNode = mapLibNode.get(indexLib).get(invokeStmt);
                else {
                  mapLibNode.get(indexLib).put(invokeStmt, uniqueIndex);
                  indexNode = uniqueIndex;
                  data.addNode(indexNode + ", " + libFeatures + indexLib); // add one value for lib index
                  uniqueIndex++;
                }
              } else {
                HashMap<String, Integer> newPackageNode = new HashMap<>();
                newPackageNode.put(invokeStmt, uniqueIndex);
                mapLibNode.put(indexLib, newPackageNode);
                indexNode = uniqueIndex;
                uniqueIndex++;
              }
            }

            data.addEdge(edge, String.valueOf(indexNode));

          }
          else if (edge.getDest() == null  && !edge.isLib() ){
            ; // Do nothing, because the dest has no active body, thus no features set and no node
          } else if (edge.getDest() != null){
            data.addEdge(edge, String.valueOf(edge.getDest().getUniqueId()));
          } else {
            System.err.println("Unexpected Edge null but not lib... exiting!");
            System.exit(1);
          }
        }
      } else if (this.getRootNode() != null && this.utilityEdges.get(entrycPDG.getValue())!=null){
        Set<CGEdge> tempCGEdgeSet = this.utilityEdges.get(entrycPDG.getValue());
        for ( CGEdge edge : tempCGEdgeSet)
          data.addEdge(edge, String.valueOf(edge.getDest().getUniqueId()));
      }
    }
    return data;
  }

  // If 'invoke' is library, return index of library Arraylist, otherwise 0
  private Integer libCall(String invoke) {
    for ( String lib : this.libraries){
      if (invoke.startsWith(lib))
        return this.libraries.indexOf(lib) + 1;
    }
    return 0;
  }

  public int getConnectedMethod(String targetMethod, StringBuilder toPrint){
    if (targetMethod == null)
      return 0;
    if (this.cPDGAvailable.containsKey(targetMethod)) {
      cleanVisit();
      return getLinkedMethods(this.cPDGAvailable.get(targetMethod), 0, toPrint);
    } else {
      System.err.println(targetMethod + " NOT FOUND!");
      return 0;
    }
  }

  private int getLinkedMethods(cPDG target, int level, StringBuilder toPrint){
    Set<CGEdge> tempCGEdgeSet = this.sdg.get(target);
    int toReturn = level;
    if ( tempCGEdgeSet != null) {
      for (CGEdge edge : tempCGEdgeSet) {
        if (level == 0) {
          toPrint.append("0  |_ M" + edge.getSource().getUniqueId() + ": THE TARGET\n");
          level++;
        }
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


  private void cleanVisit(){
    for (Map.Entry<cPDG, Set<CGEdge>> entrySDG : this.sdg.entrySet()) {
      if ( entrySDG.getValue() != null ){
        for (CGEdge edge : entrySDG.getValue()) {
          edge.setVisited(false);
        }
      }
    }
  }


}
