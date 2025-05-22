package Custom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public abstract class MCTSBase {

	protected Game rules;

	public MCTSBase(Game rules) {
		this.rules = rules;
	}


	public Action getPreviousAction(State previous, State current) {
		String from = null;
		String to = null;

		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				State.Pawn prevPawn = previous.getPawn(i, j);
				State.Pawn currPawn = current.getPawn(i, j);

				if ((prevPawn == State.Pawn.WHITE || prevPawn == State.Pawn.BLACK || prevPawn == State.Pawn.KING)
						&& currPawn == State.Pawn.EMPTY) {
					from = previous.getBox(i, j);
				} else if (prevPawn == State.Pawn.EMPTY &&
						(currPawn == State.Pawn.WHITE || currPawn == State.Pawn.BLACK || currPawn == State.Pawn.KING)) {
					to = current.getBox(i, j);
				}

				if (from != null && to != null) {
					try {
						return new Action(from, to, previous.getTurn());
					} catch (IOException e) {
						return null;
					}
				}
			}
		}

		return null; // No move detected
	}

    ////////////////////////////////////////////////////


	protected TreeNode getChildWithBestUCT(List<TreeNode> children, State.Turn turn){
		// 1. Priorità ai nodi non visitati
		for (TreeNode child : children) {
			if (child.getVisitCount() == 0) {
				return child;
			}
		}
		// 2. Se tutti sono stati visitati, usa UCB
		if(turn == State.Turn.BLACK) {
			// Tocca a noi: massimizziamo
			return getChildWithMaxUCT(children);
		} else {
			// Tocca all'avversario: minimizziamo
			return getChildWithMinUCT(children);
		}
	}

    private TreeNode getChildWithMaxUCT(List<TreeNode> children) {
    	double maxUCT = Double.NEGATIVE_INFINITY;
    	List<TreeNode> bestNodes = new ArrayList<>(); //si possono avere pareggi tra i nodi figli
		for(TreeNode child: children) {
    		double uctValue = child.UCB();
    		if(uctValue > maxUCT) { //se un nodo è migliore, sostituiamo il max
    			maxUCT = uctValue;
    			bestNodes.clear();
    			bestNodes.add(child);
    		}
    		else if(uctValue == maxUCT) { //se c'è un pareggio, aggiungiamo il figlio alla lista
    			bestNodes.add(child);
    		}
    	}
    	
    	if(bestNodes.isEmpty()) {
    		return null;
    	}
    	else {
    		return TreeNode.getRandomNode(bestNodes); //risolviamo i pareggi scegliendone uno randomicamente
    	}
    }


	private TreeNode getChildWithMinUCT(List<TreeNode> children){
		double minUCT = Double.POSITIVE_INFINITY;
		List<TreeNode> bestNodes = new ArrayList<>(); //si possono avere pareggi tra i nodi figli
		for(TreeNode child: children) {
			double uctValue = child.UCB();
			if(uctValue < minUCT) { //se un nodo è migliore, sostituiamo il max
				minUCT = uctValue;
				bestNodes.clear();
				bestNodes.add(child);
			}
			else if(uctValue == minUCT) { //se c'è un pareggio, aggiungiamo il figlio alla lista
				bestNodes.add(child);
			}
		}

		if(bestNodes.isEmpty()) {
			return null;
		}
		else {
			return TreeNode.getRandomNode(bestNodes); //risolviamo i pareggi scegliendone uno randomicamente
		}
	}
    

    

    
    

    
    ///////////////////////////////////////////////////////////// 
    
    protected boolean isTerminal(State state) {
        State.Turn turn = state.getTurn();
        return turn == State.Turn.WHITEWIN || turn == State.Turn.BLACKWIN || turn == State.Turn.DRAW;
    }
    
    

    
}
