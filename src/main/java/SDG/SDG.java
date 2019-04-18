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

  public void matchInvokecPDG() {
    int matched = 0;
    int lib = 0;
    int notActiveBody = 0;
    int failed = 0;
    int tot = 0;
    System.out.println("INFO -> available_cPDG: " + this.cPDGAvailable.size() + " notActiveBody: " + this.notActiveBodycPDG.size()
      + " failedPDG: " + this.failedPDG.size());
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
    System.out.println("MATCHED: " + matched + " LIB: " + lib + " NotActiveBody: " + notActiveBody + " FAILED: " + failed);
    float percentage = ((float) identified/ (float) tot) * 100;
    System.out.println("IDENTIFIED: " + identified + " OUT OF: " + tot + " --> " + percentage + "%");
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

}
