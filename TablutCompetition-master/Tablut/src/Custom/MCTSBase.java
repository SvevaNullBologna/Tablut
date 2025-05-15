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
	
	
	protected List<Action> getLegalActions(State state) {
        List<Action> legalActions = new ArrayList<>();
        State.Pawn[][] board = state.getBoard();
        State.Turn turn = state.getTurn();
        int size = board.length;
        for (int fromRow = 0; fromRow < size; fromRow++) {
            for (int fromCol = 0; fromCol < size; fromCol++) {

                State.Pawn pawn = board[fromRow][fromCol];

                if (!isPlayersPawn(turn, pawn)) continue;

                // Prova a muoversi in 4 direzioni finché la cella è vuota
                // RIGHT
                for (int col = fromCol + 1; col < size; col++) {
                    if (!board[fromRow][col].equals(State.Pawn.EMPTY)) break;
                    tryMove(state, rules, fromRow, fromCol, fromRow, col, turn, legalActions);
                }

                // LEFT
                for (int col = fromCol - 1; col >= 0; col--) {
                    if (!board[fromRow][col].equals(State.Pawn.EMPTY)) break;
                    tryMove(state, rules, fromRow, fromCol, fromRow, col, turn, legalActions);
                }

                // DOWN
                for (int row = fromRow + 1; row < size; row++) {
                    if (!board[row][fromCol].equals(State.Pawn.EMPTY)) break;
                    tryMove(state, rules, fromRow, fromCol, row, fromCol, turn, legalActions);
                }

                // UP
                for (int row = fromRow - 1; row >= 0; row--) {
                    if (!board[row][fromCol].equals(State.Pawn.EMPTY)) break;
                    tryMove(state, rules, fromRow, fromCol, row, fromCol, turn, legalActions);
                }
            }
        }

        return legalActions;
    }

    protected void tryMove(State state, Game rules, int fromRow, int fromCol, int toRow, int toCol,
                                State.Turn turn, List<Action> actions) {
        try {
            String from = state.getBox(fromRow, fromCol);
            String to = state.getBox(toRow, toCol);
            Action action = new Action(from, to, turn);
            rules.checkMove(state.clone(), action);
            actions.add(action);
        } catch (Exception ignored) {
        	///should we do something here?
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
    		return bestNodes.get(new Random().nextInt(bestNodes.size())); //risolviamo i pareggi scegliendone uno randomicamente
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
            return node.EstimatedValue;
        }

        // Aggiungiamo robustezza con valori minimi per evitare log(0) o div/0
        double parentVisits = Math.max(parent.getVisitCount(), 1e-6);
        double nodeVisits = Math.max(node.getVisitCount(), 1e-6);

        return node.EstimatedValue + C * Math.sqrt(Math.log(parentVisits) / nodeVisits);
    }

}
