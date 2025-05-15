package Custom;

import java.util.ArrayList;
import java.util.List;

import org.junit.platform.engine.support.hierarchical.Node;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class MTCS extends MCTSBase {
	private static Game rules;
	private int max_time; 
	private int max_memory;
	
	public MTCS(int max_time, int max_memory, Game rules) {
		super(rules);
		this.max_time = max_time;
		this.max_memory = max_memory;
	}

	public void Montecarlo(TreeNode root) {
		long startTime = System.currentTimeMillis();
		
		while(System.currentTimeMillis() - startTime < max_time) {
			
		}
	}
	
	

	
	private TreeNode select(TreeNode starting_node) {
		TreeNode node = starting_node;
		while(!node.isTerminal() && node.isFullyExpanded()) {//finché non trova un nodo foglia/terminale e non ha espanso tutto il nodo... 
			List<TreeNode> children = node.getChildren();
			node = this.getChildWithMaxUCT(children); //cerca il nodo con il max UCT
			if(node==null) break; //se non c'è un nodo sotto, vuol dire chiamo arrivati al capolinea. Si ferma
		}
		return node; //ATTEZIONE, può essere nullo!
	}

	
	private List<TreeNode> expand(TreeNode node) {
		if (node.isFullyExpanded()) {
			return node.getChildren();
			
        }
		
		node.ExpandNode(this.getLegalActions(node.getState()),rules);
		return node.getChildren();
	}

	
	private double simulate(TreeNode node) {
		
		// TODO Auto-generated method stub
		return 0;
	}

	
	private void backpropagate(TreeNode node, double value) {
		while(node != null) {
			node.VisitNode(value);
			node = node.getParent();
		}
	}
	
	//////////////////////////////////////////////////////////////////
	///
    
	
}