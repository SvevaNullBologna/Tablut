package Custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public abstract class MCTSBase {

	protected Game rules;
	protected double C = Math.sqrt(2);
	
	public MCTSBase(Game rules) {
		this.rules = rules;
	}
	
	
	public void setUCTConstant(double C) {
		this.C = C;
	}
	
	
	protected List<MoveResult> getLegalActionsAndResultingStates(State state) {
		List<MoveResult> results = new ArrayList<>();
	    State.Pawn[][] board = state.getBoard();
	    State.Turn turn = state.getTurn();
	    int size = board.length;

	    for (int fromRow = 0; fromRow < size; fromRow++) {
	        for (int fromCol = 0; fromCol < size; fromCol++) {
	            State.Pawn pawn = board[fromRow][fromCol];
	            if (!isPlayersPawn(turn, pawn)) continue;

	            addMovesInDirection(state, fromRow, fromCol, turn,  0,  1, results); // RIGHT
	            addMovesInDirection(state, fromRow, fromCol, turn,  0, -1, results); // LEFT
	            addMovesInDirection(state, fromRow, fromCol, turn,  1,  0, results); // DOWN
	            addMovesInDirection(state, fromRow, fromCol, turn, -1,  0, results); // UP
	        }
	    }

	    return results;
	}
    
	
	private void addMovesInDirection(State state, int fromRow, int fromCol, State.Turn turn,
            int rowStep, int colStep, List<MoveResult> results) {
		int row = fromRow + rowStep;
		int col = fromCol + colStep;
		State.Pawn[][] board = state.getBoard();
		int size = board.length;

		while (row >= 0 && row < size && col >= 0 && col < size && board[row][col].equals(State.Pawn.EMPTY)) {
			try {
					String from = state.getBox(fromRow, fromCol);
					String to = state.getBox(row, col);
					Action action = new Action(from, to, turn);
					State cloned = state.clone();
					State next = rules.checkMove(cloned, action);
					results.add(new MoveResult(action, next));
			} catch (Exception ignored) {
				//non credo si debba far altro 
			}

			row += rowStep;
			col += colStep;
		}
	}

    protected boolean isPlayersPawn(State.Turn turn, State.Pawn pawn) {
        if (turn.equals(State.Turn.WHITE)) {
            return pawn.equals(State.Pawn.WHITE) || pawn.equals(State.Pawn.KING);
        } else {
            return pawn.equals(State.Pawn.BLACK);
        }
    }
    
    ////////////////////////////////////////////////////
    
    protected TreeNode getChildWithMaxUCT(List<TreeNode> children) {
    	double maxUCT = Double.NEGATIVE_INFINITY;
    	List<TreeNode> bestNodes = new ArrayList<>(); //si possono avere pareggi tra i nodi figli
    	for(TreeNode child: children) {
    		double uctValue = UCB(child);
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
    		return getRandomNode(bestNodes); //risolviamo i pareggi scegliendone uno randomicamente
    	}
    }
    
    private double UCB(TreeNode node) {
        TreeNode parent = node.getParent();

        // Se il nodo non è mai stato visitato, va esplorato subito
        if (node.getVisitCount() == 0) {
            return Double.POSITIVE_INFINITY;
        }

        // Se è la radice o il genitore ha problemi, usa solo il valore stimato
        if (parent == null || parent.getVisitCount() == 0) {
            return node.totalValue;
        }

        // Aggiungiamo robustezza con valori minimi per evitare log(0) o div/0
        double parentVisits = Math.max(parent.getVisitCount(), 1e-6);
        double nodeVisits = Math.max(node.getVisitCount(), 1e-6);

        return node.getAverageValue() + C * Math.sqrt(Math.log(parentVisits) / nodeVisits);
    }
    
    protected TreeNode getRandomNode(List<TreeNode> nodes) {
    	return nodes.get(new Random().nextInt(nodes.size()));
    	/*
    	double totalWeight = 0.0;

        Heuristics heuristic;
        State state;
        List<TreeNode> validNodes = new ArrayList<>();
        List<Double> weights = new ArrayList<>();

        for (TreeNode node : nodes) {
            state = node.getState();
            heuristic = state.getTurn().equalsTurn("Black")
                ? new BlackHeuristics(state)
                : new WhiteHeuristics(state);

            double weight = heuristic.evaluateState();

            // SCARTA mosse che portano a sconfitta sicura
            if (weight < 0) {
                weight=0;
            }

            validNodes.add(node);
            weights.add(weight);
            totalWeight += weight;
        }

        if (validNodes.isEmpty()) {
            // Fallback: tutte le mosse portavano a disfatta → selezione casuale "rassegnata"
            return nodes.get(new Random().nextInt(nodes.size()));
        }

        double r = new Random().nextDouble() * totalWeight;
        double cumulative = 0.0;

        for (int i = 0; i < validNodes.size(); i++) {
            cumulative += weights.get(i);
            if (r < cumulative) {
                return validNodes.get(i);
            }
        }

        // Fallback: mai raggiunto, ma serve
        return validNodes.get(validNodes.size() - 1);
        */
    }
    
    
    protected MoveResult getRandomMove(List<MoveResult> moves) {
    	return moves.get(new Random().nextInt(moves.size()));
    }
    
    ///////////////////////////////////////////////////////////// 
    
    protected boolean isTerminal(State state) {
        State.Turn turn = state.getTurn();
        return turn == State.Turn.WHITEWIN || turn == State.Turn.BLACKWIN || turn == State.Turn.DRAW;
    }
    
    

    
}
