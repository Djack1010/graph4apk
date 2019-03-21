package SDG;

import completePDG.cPDG;
import soot.Unit;

import java.util.*;

public class SDG {

  private Map<String, cPDG> cPDGAvailable;
  private Set<String> notActiveBodycPDG;
  private Map<cPDG, Set<SDGEdge>> sdg;

  public SDG() {
    this.cPDGAvailable = new HashMap<>();
    this.notActiveBodycPDG = new HashSet<>();
    this.sdg = new HashMap<>();
  }

  public void addcPDG(cPDG cpdg) {
    this.cPDGAvailable.put(cpdg.getName(), cpdg);
  }

  public void addNotActiveBody(String name) {
    this.notActiveBodycPDG.add(name);
  }

  public void matchInvokecPDG() {
    int matched = 0;
    int lib = 0;
    int notActiveBody = 0;
    int failed = 0;
    int tot = 0;
    System.err.println("INFO -> tot_cPDG: " + this.cPDGAvailable.size() + " notActiveBody: " + this.notActiveBodycPDG.size());
    for (Map.Entry<String, cPDG> entrycPDG : this.cPDGAvailable.entrySet()) {
      Set<SDGEdge> tempSDGEdgeSet = new HashSet<SDGEdge>();
      for (Map.Entry<String, Unit> entryInvoke : entrycPDG.getValue().getInvokeStmt().entrySet()) {
        tot++;
        if (this.cPDGAvailable.containsKey(entryInvoke.getKey())) {
          tempSDGEdgeSet.add(new SDGEdge(entrycPDG.getValue(), entryInvoke.getValue(), this.cPDGAvailable.get(entryInvoke.getKey())));
          matched++;
        } else if (this.libCall(entryInvoke.getKey())) {
          tempSDGEdgeSet.add(new SDGEdge(entrycPDG.getValue(), entryInvoke.getValue(), null, true));
          lib++;
        } else if (this.notActiveBodycPDG.contains(entryInvoke.getKey())) {
          tempSDGEdgeSet.add(new SDGEdge(entrycPDG.getValue(), entryInvoke.getValue(), null));
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
    System.err.println("MATCHED: " + matched + " LIB: " + lib + " NotActiveBody: " + notActiveBody + " FAILED: " + failed);
    System.err.println("IDENTIFIED: " + identified + " OUT OF: " + tot);
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
