package completePDG;

public class cPDGEdge {

  public enum EdgeTypes {//fixed set of elements for variable Type
    CONTROL_FLOW,
    DATA_DEPENDENCE,
    CONTROL_DEPENDENCE
  }

  private EdgeTypes edgeType;
  private cPDGNode source;
  private cPDGNode dest;

  public cPDGEdge(EdgeTypes type, cPDGNode source, cPDGNode dest) {
    this.edgeType = type;
    this.source = source;
    this.dest = dest;
    if (!(this.source.getEdgesOut().contains(this))) this.source.addEdgeOut(this);
    if (!(this.dest.getEdgesIn().contains(this))) this.dest.addEdgeIn(this);
  }

  public EdgeTypes getEdgeType() {
    return this.edgeType;
  }

  public String getEdgeTypeToString() {
    return String.valueOf(this.edgeType);
  }

  public cPDGNode getSource() {
    return this.source;
  }

  public cPDGNode getDest() {
    return this.dest;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dest == null) ? 0 : dest.hashCode());
    result = prime * result + ((edgeType == null) ? 0 : edgeType.hashCode());
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
    cPDGEdge other = (cPDGEdge) obj;
    if (this.dest == null) {
      if (other.getDest() != null)
        return false;
    } else if (!this.dest.equals(other.getDest()))
      return false;
    if (this.edgeType != other.getEdgeType())
      return false;
    if (this.source == null) {
      if (other.getSource() != null)
        return false;
    } else if (!this.source.equals(other.getSource()))
      return false;
    return true;
  }
}

