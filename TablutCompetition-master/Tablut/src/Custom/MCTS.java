package Custom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.State;


public class MCTS {
	private final int max_time;
	private final long max_memory = (long) (Runtime.getRuntime().maxMemory() * 0.75/1024);
	private final Game rules;


	public MCTS(int max_time, Game rules) {
		this.rules = rules;
		this.max_time = max_time;
	}

	//Now I pass an updated tree. That means, we just go to the bottom of it and start from there, cutting previous possible branches
	public TreeNode montecarlo(TreeNode root, State.Turn turn) throws IllegalStateException, NullPointerException {
		long startTime = System.currentTimeMillis();
	    boolean hasSimulatedAtLeastOnce = false;

		while (resourcesAvailable(startTime)) {
			TreeNode selected = select(root, turn);
			if (selected == null) break;

			List<TreeNode> children = expand(selected);
			if (children == null || children.isEmpty()) continue;

			TreeNode child = TreeNode.getChildWithBestUCT(children, turn);
			if (child == null) continue;

			double result = simulate(child);
			backpropagate(child, result);
			hasSimulatedAtLeastOnce = true;
		}

	    if (!hasSimulatedAtLeastOnce) {
	        throw new IllegalStateException("Monte Carlo failed: no valid simulations were performed.");
	    }

		TreeNode best = root.getMostVisitedChild();
		if(best == null){
			throw new NullPointerException("Monte Carlo failed: no valid child");
		}
		return best;
	}


	private TreeNode select(TreeNode starting_node, State.Turn turn) {//Si scende lungo l'albero fino a un nodo foglia
		TreeNode node = starting_node;
		while(!node.isTerminal() && node.hasBeenExpanded()) {//finché non trova un nodo foglia/terminale e non ha espanso tutto il nodo...
			List<TreeNode> children = node.getChildren();
			node = TreeNode.getChildWithBestUCT(children, turn); //cerca il nodo con il max UCT
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
		double terminalValue = node.evaluateTerminalState();
		if (!Double.isNaN(terminalValue)) {
			return terminalValue;
		} else {
			State simulating_state = node.getState().clone();
			State.Turn starting_player = node.getState().getTurn();

			Set<State> visited = new HashSet<>();
			visited.add(simulating_state);

			int maxDepth = 100; // evita simulazioni infinite
			int depth = 0;

			while (!simulating_state.isTerminal() && depth < maxDepth) {
				List<MoveResult> legalMoves = MoveResult.getLegalActionsAndResultingStates(simulating_state, rules);

				// Filtra mosse che portano a stati già visti
				List<MoveResult> filteredMoves = legalMoves.stream()
						.filter(m -> !visited.contains(m.resultingState))
						.collect(Collectors.toList());

				if (filteredMoves.isEmpty()) {
					break; // siamo in loop o bloccati
				}

				MoveResult move = MoveResult.getRandomMove(filteredMoves);
				simulating_state = move.resultingState;
				visited.add(simulating_state);
				depth++;
			}

			if (depth >= maxDepth) {
				return Constants.DRAW(starting_player); // o penalizza con LOSS
			}

			State.Turn outcome = simulating_state.getTurn();

			if (outcome == State.Turn.DRAW) {
				return Constants.DRAW(starting_player);
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

	private boolean resourcesAvailable(long startTime){
		long elapsedTime = System.currentTimeMillis() - startTime;
		long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long memoryLimit = max_memory * 1024L;

		return elapsedTime < (max_time - 1500) && usedMemory < memoryLimit;
	}


}