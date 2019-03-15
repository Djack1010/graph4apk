package completePDG;

import soot.Unit;

import java.util.HashSet;
import java.util.Set;

public class cPDGNode {

  private int nodeId;
  private String content;
  private Set<cPDGEdge> edgesOut;
  private Set<cPDGEdge> edgesIn;
  private Unit unitNode;
  private boolean visited;


  public cPDGNode(int nodeId, String content, Unit node) {
    this.visited = false;
    this.content = content.replaceAll("\\s", "");
    this.nodeId = nodeId;
    this.edgesOut = new HashSet<cPDGEdge>();
    this.edgesIn = new HashSet<cPDGEdge>();
    this.unitNode = node;
  }

  public cPDGNode(cPDGNode nodeToClone) {
    this.visited = false;
    this.content = nodeToClone.getContent();
    this.nodeId = nodeToClone.getId();
    this.edgesOut = new HashSet<cPDGEdge>();
    this.edgesIn = new HashSet<cPDGEdge>();
    this.unitNode = nodeToClone.getUnitNode();
  }

  public void newEdgeIn(cPDGNode source, cPDGEdge.EdgeTypes edgeType) {
    cPDGEdge newEdge = new cPDGEdge(edgeType, source, this);
    this.edgesIn.add(newEdge);
  }

  public void addEdgeIn(cPDGEdge edge) {
    this.edgesIn.add(edge);
  }

  public Set<cPDGEdge> getEdgesIn() {
    return this.edgesIn;
  }

  public void newEdgeOut(cPDGNode dest, cPDGEdge.EdgeTypes edgeType) {
    cPDGEdge newEdge = new cPDGEdge(edgeType, this, dest);
    this.edgesOut.add(newEdge);
  }

  public void addEdgeOut(cPDGEdge edge) {
    this.edgesOut.add(edge);
  }

  public Set<cPDGEdge> getEdgesOut() {
    return this.edgesOut;
  }

  public String getContent() {
    return this.content;
  }

  public int getId() {
    return this.nodeId;
  }

  public String getName() {
    return this.nodeId + "_" + this.content;
  }

  public boolean isVisited() {
    return this.visited;
  }

  public void setVisited(boolean set) {
    this.visited = set;
  }

  public Unit getUnitNode() {
    return this.unitNode;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((content == null) ? 0 : content.hashCode());
    result = 17 * result + nodeId;
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    cPDGNode other = (cPDGNode) obj;
    if (this.content == null) {
      if (other.getContent() != null)
        return false;
    } else if (!this.content.equals(other.getContent()))
      return false;
    if (this.nodeId != other.getId())
      return false;
    if (this.unitNode == null) {
      if (other.getUnitNode() != null)
        return false;
    } else if (!this.unitNode.equals(other.getUnitNode()))
      return false;
    return true;
  }

  public String toString() {
    return this.nodeId + " -> " + this.content;
  }

}
