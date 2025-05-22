package Custom;

import java.util.List;


import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.State;


public class MCTS extends MCTSBase {
	private int max_time; 
	private int max_memory;
	
	public MCTS(int max_time, int max_memory, Game rules) {
		super(rules);
		this.max_time = max_time;
		this.max_memory = max_memory;
	}

	//Now I pass an updated tree. That means, we just go to the bottom of it and start from there, cutting previous possible branches
	public TreeNode montecarlo(TreeNode partenza, State.Turn turn) throws IllegalStateException, NullPointerException {
		long startTime = System.currentTimeMillis();
	    boolean hasSimulatedAtLeastOnce = false;

		while (System.currentTimeMillis() - startTime < (max_time-1500)) {
			TreeNode selected = select(partenza, turn);
			if (selected == null) continue;

			List<TreeNode> children = expand(selected);
			if (children == null || children.isEmpty()) continue;

			TreeNode child = this.getChildWithBestUCT(children, turn);

			if (child == null) continue;

			double result = simulate(child);
			backpropagate(child, result);
			hasSimulatedAtLeastOnce = true;
		}

	    if (!hasSimulatedAtLeastOnce) {
	        throw new IllegalStateException("Monte Carlo failed: no valid simulations were performed.");
	    }

		TreeNode answer = partenza.getMostVisitedChild();
		if(answer != null){
			return answer;
		}
		else{
			throw new NullPointerException("Monte Carlo failed: no valid child");
		}

	}


	private TreeNode select(TreeNode starting_node, State.Turn turn) {//Si scende lungo l'albero fino a un nodo foglia
		TreeNode node = starting_node;
		while(!node.isTerminal() && node.hasBeenExpanded()) {//finché non trova un nodo foglia/terminale e non ha espanso tutto il nodo...
			List<TreeNode> children = node.getChildren();
			node = this.getChildWithBestUCT(children, turn); //cerca il nodo con il max UCT
			if(node==null) break; //se non c'è un nodo sotto, vuol dire chiamo arrivati al capolinea. Si ferma.
		}
		return node; //ATTEZIONE, può essere nullo!
	}

	
	private List<TreeNode> expand(TreeNode node) {//si espande il nodo foglia aggiungendo uno o più figli
		if (node.isTerminal() || node.hasBeenExpanded()) {
			return node.getChildren();
			
        }
		
		List<MoveResult> legalMoves = MoveResult.getLegalActionsAndResultingStates(node.getState(), rules);
		node.ExpandNode(legalMoves);
		
		return node.getChildren();
	}

	
	private double simulate(TreeNode node) {
		Double terminalValue = node.evaluateTerminalState();
		if(terminalValue != Constants.NOT_A_TERMINAL_STATE){
			return terminalValue; //evitiamo una simulazione inutile. Siamo in uno stato terminale dopotutto
		}
		else {
			State simulating_state = node.getState().clone();
			State.Turn starting_player = node.getState().getTurn(); // use original turn

			while (!isTerminal(simulating_state)) {
				List<MoveResult> legalMoves = MoveResult.getLegalActionsAndResultingStates(simulating_state, rules);
				if (legalMoves.isEmpty()) break;

				MoveResult move = MoveResult.getRandomMove(legalMoves);
				simulating_state = move.resultingState;
			}

			State.Turn outcome = simulating_state.getTurn(); // This now holds WHITEWIN, BLACKWIN, or DRAW

			if (outcome == State.Turn.DRAW) {
				return Constants.DRAW;
			} else if ((outcome == State.Turn.WHITEWIN && starting_player == State.Turn.WHITE) ||
					(outcome == State.Turn.BLACKWIN && starting_player == State.Turn.BLACK)) {
				return Constants.WIN;
			} else {
				return Constants.LOSE;
			}
		}
	}


	
	private void backpropagate(TreeNode node, double value) { //si propagano i risultati della simulazione lungo il percorso
		while(node != null) {
			node.VisitNode(value); //fa anche l'update già che visita
			node = node.getParent();
		}
	}
	

	
}