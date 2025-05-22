package aima.core.search.adversarial;

import aima.core.search.framework.GameTree;
import aima.core.search.framework.Metrics;
import aima.core.search.framework.Node;
import aima.core.search.framework.NodeFactory;
import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.RowFilter.Entry;

import Custom.AIMAGameAshtonTablut;
import Custom.SimulatedState;

/**
 * Artificial Intelligence A Modern Approach (4th Edition): page ???.<br>
 *
 * <pre>
 * <code>
 * function MONTE-CARLO-TREE-SEARCH(state) returns an action
 *   tree &larr; NODE(state)
 *   while TIME-REMAINING() do
 *   	leaf &larr; SELECT(tree)
 *   	child &larr; EXPAND(leaf)
 *   	result &larr; SIMULATE(child)
 *   	BACKPROPAGATE(result, child)
 *   return the move in ACTIONS(state) whose node has highest number of playouts
 * </code>
 * </pre>
 *
 * Figure ?.? The Monte Carlo tree search algorithm. A game tree, tree, is
 * initialized, and then we repeat the cycle of SELECT / EXPAND / SIMULATE/
 * BACKPROPAGATE until we run out of time, and return the move that led to the
 * node with the highest number of playouts.
 *
 *
 * @author Suyash Jain
 *
 * @param <S> Type which is used for states in the game.
 * @param <A> Type which is used for actions in the game.
 * @param <P> Type which is used for players in the game.
 */

public class MonteCarloTreeSearch<S, A, P> implements AdversarialSearch<S, A> {
	private int iterations = 0;
	private final Game<S, A, P> game;
	private final GameTree<S, A> tree;
	private int max_time;

	public MonteCarloTreeSearch(Game<S, A, P> game, int iterations, int max_time) {
		this.game = game;
		this.iterations = iterations;
		tree = new GameTree<>();
		this.max_time = max_time-1000;
	}

	@Override
	public A makeDecision(S state) {
		// tree <-- NODE(state)
		tree.addRoot(state);
		// while TIME-REMAINING() do
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - startTime < (max_time)) {
			// leaf <-- SELECT(tree)
			Node<S, A> leaf = select(tree);
			// child <-- EXPAND(leaf)
			Node<S, A> child = expand(leaf);
			// result <-- SIMULATE(child)
			// result = true if player of root node wins
			double result = simulate(child, startTime);
			// BACKPROPAGATE(result, child)
			backpropagate(result, child);
			// repeat the four steps for set number of iterations
		}
		System.out.println(tree);
		// return the move in ACTIONS(state) whose node has highest number of playouts
		return bestAction(tree.getRoot());
	}

	private Node<S, A> select(GameTree<S, A> gameTree) {
		Node<S, A> node = gameTree.getRoot();
		while (!game.isTerminal(node.getState()) && isNodeFullyExpanded(node)) {
			node = gameTree.getChildWithMaxUCT(node);
		}
		return node;
	}

	private Node<S, A> expand(Node<S, A> leaf) {
		if (game.isTerminal(leaf.getState()))
			return leaf;
		else
			return randomlySelectUnvisitedChild(leaf);
	}
	
	private double simulate(Node<S, A> node, long startTime) {
		if (game.isTerminal(node.getState()))
			System.out.println("a");
		int depth = 0;
	    while (!game.isTerminal(node.getState()) && depth<10 && System.currentTimeMillis() - startTime < (max_time)) {
			((AIMAGameAshtonTablut)game).setDrawConditions(findDrawConditions(node));
			List<A> actions = game.getActions(node.getState());
			S result = null;
	        double bestValue = Double.NEGATIVE_INFINITY;
	        for (A a : actions) {
	            S next = game.getResult(node.getState(), a);
	            double val = game.getUtility(next, game.getPlayer(node.getState())); // turno corrente
	            if (val >= bestValue) {
	                bestValue = val;
	                result = next;
	              }
	        }
            depth++;
			NodeFactory<S, A> nodeFactory = new NodeFactory<>();
			node = nodeFactory.createNode(result);
			depth++;
		}
		P p = game.getPlayer(tree.getRoot().getState());
		return game.getUtility(node.getState(), p);
		//return game.getUtility(node.getState(), p) > 0;
	}

	private void backpropagate(double result, Node<S, A> node) {
		tree.updateStats(result, node);
		if (tree.getParent(node) != null)
			backpropagate(result, tree.getParent(node));
	}

	private A bestAction(Node<S, A> root) {
		HashMap<A, S> actions = new HashMap<>();
		((AIMAGameAshtonTablut)game).setDrawConditions(findDrawConditions(root));
		game.getActions(root.getState()).stream().forEach((A a) -> actions.put(a, game.getResult(root.getState(), a)));
		A winnerChild = getWinnerChild(actions,
				((State) root.getState()).getTurn().equals(State.Turn.BLACK) ? State.Turn.BLACKWIN : State.Turn.WHITEWIN);
		if (winnerChild != null) {
			return winnerChild;
		}
		Node<S, A> bestChild = tree.getChildWithMaxPlayouts(root);
		for (java.util.Map.Entry<A, S> entry : actions.entrySet()) {
			if (entry.getValue().equals(bestChild.getState()))
				return entry.getKey();
		}
		return null;
	}

	private boolean isNodeFullyExpanded(Node<S, A> node) {
		List<S> visitedChildren = tree.getVisitedChildren(node);
		((AIMAGameAshtonTablut)game).setDrawConditions(findDrawConditions(node));
		for (A a : game.getActions(node.getState())) {
			S result = game.getResult(node.getState(), a);
			if (!visitedChildren.contains(result)) {
				return false;
			}
		}
		return true;
	}

	public static <S, A> List<State> findDrawConditions(Node<S, A> node) {
		State present = (State) node.getState();
		List<State> ret = new ArrayList<>();
		if (!node.isRootNode()) {
			State parent = (State) node.getParent().getState();
			if(present.getNumberOf(Pawn.BLACK) == parent.getNumberOf(Pawn.BLACK)
					&& present.getNumberOf(Pawn.WHITE) == parent.getNumberOf(Pawn.WHITE)) {
				ret.add(parent);
				if (parent instanceof SimulatedState)
					ret.addAll(((SimulatedState) parent).getDrawConditions());
				else
					ret.addAll(findDrawConditions(node.getParent()));
			}				
		}
		return ret;
	}
	private Node<S, A> randomlySelectUnvisitedChild(Node<S, A> node) {
		S bestUnvisitedChildren = null;
		List<S> visitedChildren = tree.getVisitedChildren(node);
		double bestValue = Double.NEGATIVE_INFINITY;
		((AIMAGameAshtonTablut)game).setDrawConditions(findDrawConditions(node));
		for (A a : game.getActions(node.getState())) {
			S result = game.getResult(node.getState(), a);
			double val = game.getUtility(result, game.getPlayer(node.getState())); // turno corrente
            if (!visitedChildren.contains(result) && val >= bestValue) {
                bestValue = val;
                bestUnvisitedChildren = result;
              }
		}
		return tree.addChild(node, bestUnvisitedChildren);
	}

	public A getWinnerChild(HashMap<A, S> actions, State.Turn aimed) {
		List<A> best_children = new ArrayList<>();
		for (java.util.Map.Entry<A, S> entry : actions.entrySet()) {
			if (((State) entry.getValue()).getTurn().equals(aimed))
				best_children.add(entry.getKey());
		}
		Random rand = new Random();
		if (best_children.size() == 0)
			return null;
		return best_children.get(rand.nextInt(best_children.size()));
	}

	@Override
	public Metrics getMetrics() {
		return null;
	}
}
