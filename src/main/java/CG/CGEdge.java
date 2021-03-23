package CG;

import completePDG.cPDG;
import soot.Unit;

public class CGEdge {

  private cPDG source;
  private Unit unitSource;
  private String invokeStmt;
  private cPDG dest;
  // -1 special edge (link methods in classes), 0 standard edge, >0 index of lib describe in an ArrayList of CG.java
  private Integer typeEdge;
  private boolean visited = false;

  public CGEdge(cPDG source, Unit unitSource, String invokeStmt, cPDG dest) {
    this.source = source;
    this.unitSource = unitSource;
    this.invokeStmt = invokeStmt;
    this.dest = dest;
    this.typeEdge = 0;
  }

  public CGEdge(cPDG source, Unit unitSource, String invokeStmt, cPDG dest, Integer libIndex) {
    this.source = source;
    this.unitSource = unitSource;
    this.invokeStmt = invokeStmt;
    this.dest = dest;
    this.typeEdge = libIndex;
  }

  // Constructor for utility edge
  public CGEdge(cPDG source, cPDG dest) {
    this.source = source;
    this.unitSource = null;
    this.invokeStmt = null;
    this.dest = dest;
    this.typeEdge = -1;
  }

  public cPDG getSource() {
    return this.source;
  }

  public String getInvokeStmt() {
    return this.invokeStmt;
  }

  public cPDG getDest() {
    return this.dest;
  }

  public Unit getUnitSource() {
    return unitSource;
  }

  public boolean isLib() {
    return this.typeEdge > 0;
  }

  public Integer getTypeEdge() { return this.typeEdge; }

  public boolean isVisited() { return this.visited; }
  public void setVisited(boolean a) { this.visited = a; }

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
    CGEdge other = (CGEdge) obj;
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
