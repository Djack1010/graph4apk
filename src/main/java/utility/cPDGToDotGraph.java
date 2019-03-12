package utility;

import completePDG.cPDGEdge;
import completePDG.cPDGNode;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphEdge;
import soot.util.dot.DotGraphNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class cPDGToDotGraph {

    //Map<String, String> dotNodes;
    Set<Integer> visitedNodes;
    cPDGNode rootNode = null;
    DotGraph dotGraph = null;
    String name = null;

    /**
     * Constructor, create an instance of a CPGToDotGraph class
     * @param rootNode, the CodePropertyGraph root node
     * @param name, the PDG name
     */
    public cPDGToDotGraph(cPDGNode rootNode, String name){
        this.rootNode = rootNode;
        this.name = name;
        this.dotGraph = new DotGraph(this.name);
        this.visitedNodes = new HashSet<Integer>();
    }

    /**
     * Create a DotGraph object with the info of the PDG in DOT language
     * @return DotGraph object
     */
    public DotGraph drawcPDG(){
        this.dotGraph.setNodeShape("record");
        this.recursiveDrawCPG(this.rootNode);
        return dotGraph;
    }
    /**
     * Visit the PDG and create nodes and edges in the DotGraph for each CPG element
     * @param pdgNode, CPG node to visit
     */
    private void recursiveDrawCPG(cPDGNode pdgNode){
        if(this.visitedNodes.contains(pdgNode.getId())) return;
        else this.visitedNodes.add(pdgNode.getId());
        if(pdgNode.getEdgesIn().isEmpty() && pdgNode.getEdgesOut().isEmpty()){//Only one node
            DotGraphNode dotNode = this.dotGraph.drawNode(String.valueOf(pdgNode.getId()));
            dotNode.setLabel("{" + pdgNode.getId() + "|" + pdgNode.getContent()
                    .replace("<", "\\<").replace(">", "\\>")
                    .replace("{", "\\{").replace("}", "\\}") + "}");
            return;
        }
        if(pdgNode.getEdgesIn().isEmpty()){//Root node
            DotGraphNode dotNode = this.dotGraph.drawNode(String.valueOf(pdgNode.getId()));
            dotNode.setLabel("{" + pdgNode.getId() + "|" + pdgNode.getContent()
                    .replace("<", "\\<").replace(">", "\\>")
                    .replace("{", "\\{").replace("}", "\\}") + "}");
        }else if(pdgNode.getEdgesOut().isEmpty()){//Leaf node
            DotGraphNode dotNode = this.dotGraph.getNode(String.valueOf(pdgNode.getId()));
            dotNode.setLabel("{" + pdgNode.getId() + "|" + pdgNode.getContent()
                    .replace("<", "\\<").replace(">", "\\>")
                    .replace("{", "\\{").replace("}", "\\}") + "}");
            return;
        }else{//generic node
            DotGraphNode dotNode = this.dotGraph.getNode(String.valueOf(pdgNode.getId()));
            dotNode.setLabel("{" + pdgNode.getId() + "|" + pdgNode.getContent()
                    .replace("<", "\\<").replace(">", "\\>")
                    .replace("{", "\\{").replace("}", "\\}") + "}");
        }

        Iterator<cPDGEdge> iteraVisits = pdgNode.getEdgesOut().iterator();
        while (iteraVisits.hasNext()) {
            cPDGEdge tempEdge = iteraVisits.next();
            DotGraphEdge dotEdge = this.dotGraph.drawEdge(String.valueOf(pdgNode.getId()),
                    String.valueOf(tempEdge.getDest().getId()));

            if(tempEdge.getEdgeType() == cPDGEdge.EdgeTypes.CONTROL_FLOW)
                dotEdge.setAttribute("color","blue");

            if(tempEdge.getEdgeType() == cPDGEdge.EdgeTypes.CONTROL_DEPENDENCE) {
                dotEdge.setAttribute("color","gold");
                dotEdge.setLabel("CONTROL");
            }
            if(tempEdge.getEdgeType() == cPDGEdge.EdgeTypes.DATA_DEPENDENCE) {
                dotEdge.setAttribute("color","goldenrod4");
                dotEdge.setLabel("DATA");
            }
            recursiveDrawCPG(tempEdge.getDest());
        }
    }

}
