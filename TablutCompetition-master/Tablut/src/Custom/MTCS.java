package Custom;

import java.util.List;

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
	    boolean hasSimulatedAtLeastOnce = false;

	    while (System.currentTimeMillis() - startTime < max_time) {
	        TreeNode selected = select(root);
	        if (selected == null) continue;

	        List<TreeNode> children = expand(selected);
	        if (children == null || children.isEmpty()) continue;

	        TreeNode child = this.getChildWithMaxUCT(children);
	        if (child == null) continue;

	        double result = simulate(child);
	        backpropagate(child, result);
	        hasSimulatedAtLeastOnce = true;
	    }

	    if (!hasSimulatedAtLeastOnce) {
	        throw new IllegalStateException("Monte Carlo failed: no valid simulations were performed.");
	    }
	}

	
	

	
	private TreeNode select(TreeNode starting_node) {//Si scende lungo l'albero fino a un nodo foglia
		TreeNode node = starting_node;
		while(!node.isTerminal() && node.isFullyExpanded()) {//finché non trova un nodo foglia/terminale e non ha espanso tutto il nodo... 
			List<TreeNode> children = node.getChildren();
			node = this.getChildWithMaxUCT(children); //cerca il nodo con il max UCT
			if(node==null) break; //se non c'è un nodo sotto, vuol dire chiamo arrivati al capolinea. Si ferma.
		}
		return node; //ATTEZIONE, può essere nullo!
	}

	
	private List<TreeNode> expand(TreeNode node) {//si espande il nodo foglia aggiungendo uno o più figli
		if (node.isTerminal() || node.isFullyExpanded()) {
			return node.getChildren();
			
        }
		
		List<MoveResult> legalMoves = this.getLegalActionsAndResultingStates(node.getState());
		node.ExpandNode(legalMoves, rules);
		
		return node.getChildren();
	}

	
	private double simulate(TreeNode node) {
	    State simulating_state = node.getState().clone();
	    State.Turn starting_player = node.getState().getTurn(); // use original turn

	    while (!isTerminal(simulating_state)) {
	        List<MoveResult> legalMoves = this.getLegalActionsAndResultingStates(simulating_state);
	        if (legalMoves.isEmpty()) break;

	        MoveResult move = this.getRandomMove(legalMoves);
	        simulating_state = move.resultingState;
	    }

	    State.Turn outcome = simulating_state.getTurn(); // This now holds WHITEWIN, BLACKWIN, or DRAW

	    if (outcome == State.Turn.DRAW) {
	        return 0.5;
	    } else if ((outcome == State.Turn.WHITEWIN && starting_player == State.Turn.WHITE) ||
	               (outcome == State.Turn.BLACKWIN && starting_player == State.Turn.BLACK)) {
	        return 1.0;
	    } else {
	        return 0.0;
	    }
	}


	
	private void backpropagate(TreeNode node, double value) { //si propagano i risultati della simulazione lungo il percorso
		while(node != null) {
			node.VisitNode(value); //fa anche l'update già che visita
			node = node.getParent();
		}
	}
	
	//////////////////////////////////////////////////////////////////
	///
    
	
}