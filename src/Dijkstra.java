import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Dijkstra {
	List<Edge> edges;
	List<Node> nodes;
	Set<Node> settledNodes;
	Set<Node> unsettledNodes;
	Map<Node, Double> nodeDistance;
	Map<Node, Node> predecessor; // A tricky setting: key is the destination
									// node, while value is the source node
	Map<Node, Edge> predecessorEdge; // Destination Node is the key, shortest path Edge is the value
	Node sourceNode;

	public Dijkstra(Graph graph) {
		this.nodes = new ArrayList<Node>(graph.nodes);
		this.edges = new ArrayList<Edge>(graph.edges);
	}

	public void execute(Node sourceNode) {
		/*
		 * for (int i = 0; i < nodes.size(); i++) { System.out.println(nodes.get(i).id); }
		 * 
		 * for (int i = 0; i < edges.size(); i++) { System.out.println(edges.get(i).toString()); }
		 */

		settledNodes = new HashSet<Node>();
		unsettledNodes = new HashSet<Node>();
		nodeDistance = new HashMap<Node, Double>();
		predecessor = new HashMap<Node, Node>();
		predecessorEdge = new HashMap<Node, Edge>();
		this.sourceNode = sourceNode;
		nodeDistance.put(sourceNode, (double) 0);
		unsettledNodes.add(sourceNode);

		while (!unsettledNodes.isEmpty()) {
			// find the minimal distance node in the unsettledNodes collection
			Node currentNode = findMinimumFromUnsettledNodes(unsettledNodes);
			// find all its neighbors (except those settled ones) and calculate
			// their distance & comparing
			// with current one
			// add all those neighbors into unsettledNodes
			calculateItsNeighbors(currentNode);
			// calculate currentNode minimum distance by comparing its current
			// distance with all possible distance with source from its
			// neighbors
			getMinDistance(currentNode);
			// add current node into settledNodes & remove it from
			// unsettledNodes
			settledNodes.add(currentNode);
			unsettledNodes.remove(currentNode);
		}
		/*
		 * Iterator<Node> iter = nodeDistance.keySet().iterator(); while (iter.hasNext()) { Node cur
		 * = (Node) iter.next(); System.out.println("To node " + cur.id + " with distance: " +
		 * nodeDistance.get(cur)); }
		 */
		/*
		 * Iterator<Node> iter = predecessor.keySet().iterator(); while (iter.hasNext()) { Node cur
		 * = (Node) iter.next(); System.out.println( "To node " + cur.id + " From Node " +
		 * predecessor.get(cur)); }
		 */

	}

	private void getMinDistance(Node currentNode) {
		for (Edge edgeEle : edges) {
			if (edgeEle.destination.equals(currentNode)
					&& getNodeDistance(edgeEle.source) != Integer.MAX_VALUE) {

				if (getNodeDistance(currentNode) > getNodeDistance(edgeEle.source)
						+ edgeEle.weight) {
					nodeDistance.put(currentNode, getNodeDistance(edgeEle.source) + edgeEle.weight); //TODO: Don't understand this
					predecessor.put(currentNode, edgeEle.source);
					predecessorEdge.put(currentNode, edgeEle);
				}
			}
		}
	}

	private void calculateItsNeighbors(Node currentNode) {
		for (Edge edgeEle : edges) {
			if (edgeEle.source.equals(currentNode) && !settledNodes.contains(edgeEle.destination)) {
				nodeDistance.put(edgeEle.destination,
						edgeEle.weight + nodeDistance.get(currentNode));
				unsettledNodes.add(edgeEle.destination);
				predecessor.put(edgeEle.destination, currentNode);
				predecessorEdge.put(edgeEle.destination, edgeEle);
			}
		}
	}

	private Node findMinimumFromUnsettledNodes(Set<Node> unsettledNodes) {
		Node minimumNode = null;
		for (Node ele : unsettledNodes) {
			if (minimumNode == null) {
				minimumNode = ele;
			} else {
				if (getNodeDistance(minimumNode) > getNodeDistance(ele)) {
					minimumNode = ele;
				}
			}
		}
		return minimumNode;
	}

	private double getNodeDistance(Node minimumNode) {
		if (nodeDistance.get(minimumNode) != null) {
			return nodeDistance.get(minimumNode);
		}
		return Integer.MAX_VALUE;
	}

	public Stack<Edge> shortestPath(Node destinationNode) {
		/*
		 * LinkedList<Node> pathToReturn = new LinkedList<Node>();
		 * 
		 * pathToReturn.add(destinationNode); while (destinationNode != this.sourceNode) {
		 * 
		 * if(!predecessor.containsValue(destinationNode)){ return null; }
		 * 
		 * destinationNode = predecessor.get(destinationNode); // get the // source Node // for
		 * current // destination // Node and // update it as // new // destination // Node
		 * pathToReturn.add(destinationNode);
		 * 
		 * }
		 */

		Node tmpNode = destinationNode;
		Edge tmpPath = null;
		Stack<Edge> pathToReturn = new Stack<Edge>();

		while (!tmpNode.equals(this.sourceNode)) {
			tmpPath = predecessorEdge.get(tmpNode);
			tmpNode = tmpPath.source;
			pathToReturn.push(tmpPath);
		}

		return pathToReturn;
	}

}
