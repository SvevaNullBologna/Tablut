package Custom;

import java.util.ArrayList;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class MTCS{
	private static Game rules;
	private int max_time; 
	private int max_memory;
	private double C = Math.sqrt(2);
	
	public MTCS(int max_time, int max_memory, Game rules) {
		this.rules = rules;
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
	
	//////////////////////////////////////////////////////////////////
	///
    private List<Action> getLegalActions(State state) {
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

    private void tryMove(State state, Game rules, int fromRow, int fromCol, int toRow, int toCol,
                                State.Turn turn, List<Action> actions) {
        try {
            String from = state.getBox(fromRow, fromCol);
            String to = state.getBox(toRow, toCol);
            Action action = new Action(from, to, turn);
            rules.checkMove(state.clone(), action);
            actions.add(action);
        } catch (Exception ignored) {
        }
    }

    private boolean isPlayersPawn(State.Turn turn, State.Pawn pawn) {
        if (turn.equals(State.Turn.WHITE)) {
            return pawn.equals(State.Pawn.WHITE) || pawn.equals(State.Pawn.KING);
        } else {
            return pawn.equals(State.Pawn.BLACK);
        }
    }
	
}