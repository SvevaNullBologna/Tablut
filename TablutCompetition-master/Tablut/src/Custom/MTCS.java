package Custom;

import java.util.List;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class MTCS{
	private int max_time; 
	private int max_memory;
	private double C = Math.sqrt(2);
	
	public MTCS(int max_time, int max_memory) {
		this.max_time = max_time;
		this.max_memory = max_memory;
	}

	public void Montecarlo(TreeNode root) {
		long startTime = System.currentTimeMillis();
		
		while(System.currentTimeMillis() - startTime < max_time) {
			
		}
	}
	
	private double UCB(TreeNode node){ //upper confidence bounds 
		TreeNode parent = node.getParent();
		if(node.getVisitCount() == 0) {
			return Double.POSITIVE_INFINITY;//il nodo non è mai stato visitato
		}
		
		if(parent == null || parent.getVisitCount() == 0) {
			return node.EstimatedValue;//è un nodo radice
		}
		return node.EstimatedValue + C * Math.sqrt( Math.log(parent.getVisitCount()) / node.getVisitCount() );//formula UCB
	}

	
	public TreeNode select(List<TreeNode> children) {
		return children.stream()
				.max((a,b) -> Double.compare(UCB(a), UCB(b)))//seleziona il miglior figlio
				.orElse(null);
	}

	
	public List<TreeNode> expand(TreeNode node) {
		if (node.isFullyExpanded()) {
			return node.getChildren();
			
        }
		
		node.ExpandNode();
		return node.getChildren();
	}

	
	public double simulate(TreeNode node) {
		
		// TODO Auto-generated method stub
		return 0;
	}

	
	public void backpropagate(TreeNode node, double value) {
		while(node != null) {
			node.VisitNode(value);
			node = node.getParent();
		}
	}
	
	
	
}