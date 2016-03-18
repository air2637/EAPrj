
public class Edge {
	public String id;
	public Node source;
	public Node destination;
	public double weight;
	
	public Edge(String id, Node source, Node destination, double weight){
		this.id=id;
		this.source = source;
		this.destination = destination;
		this.weight = weight;
	}
	
	public Edge(String id, Node source, Node destination){
		this.id=id;
		this.source = source;
		this.destination = destination;
	}
	
	
	public String toString(){
		//return "Edge: " + this.id + " source: " + this.source.id + " desti: " + this.destination.id + " weight: " + this.weight;
		return this.id;
	}
}
