package SDG;

import completePDG.cPDG;
import soot.Unit;

public class SDGEdge {

  private cPDG source;
  private Unit unitSource;
  private cPDG dest;
  private boolean isLib;

  public SDGEdge(cPDG source, Unit unitSource, cPDG dest) {
    this.source = source;
    this.unitSource = unitSource;
    this.dest = dest;
    this.isLib = false;
  }

  public SDGEdge(cPDG source, Unit unitSource, cPDG dest, boolean isLib) {
    this.source = source;
    this.unitSource = unitSource;
    this.dest = dest;
    this.isLib = isLib;
  }

  public cPDG getSource() {
    return this.source;
  }

  public cPDG getDest() {
    return this.dest;
  }

  public Unit getUnitSource() {
    return unitSource;
  }

  public boolean isLib() {
    return isLib;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dest == null) ? 0 : dest.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
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
    SDGEdge other = (SDGEdge) obj;
    if (this.dest == null) {
      if (other.getDest() != null)
        return false;
    } else if (!this.dest.equals(other.getDest()))
      return false;
    if (this.source == null) {
      return other.getSource() == null;
    } else return this.source.equals(other.getSource());
  }

}