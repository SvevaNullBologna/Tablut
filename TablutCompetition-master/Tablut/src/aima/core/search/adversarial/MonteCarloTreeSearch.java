package aima.core.search.adversarial;

import aima.core.search.framework.GameTree;
import aima.core.search.framework.Metrics;
import aima.core.search.framework.Node;
import aima.core.search.framework.NodeFactory;
import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.RowFilter.Entry;

import Custom.AIMAGameAshtonTablut;
import Custom.CanonicalState;
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
		tree.addRoot(state);
		// while TIME-REMAINING() do
		long startTime = System.currentTimeMillis();
        long time = System.currentTimeMillis();
		while (time - startTime < (max_time)) {
	        System.out.println("2:"+System.currentTimeMillis());
			time = System.currentTimeMillis();
			// leaf <-- SELECT(tree)
			Node<S, A> leaf = select(tree);
			// child <-- EXPAND(leaf)

	        System.out.println("sel:"+(System.currentTimeMillis()-time));
			time = System.currentTimeMillis();			
			Node<S, A> child = expand(leaf);

	        System.out.println("exp:"+(System.currentTimeMillis()-time));
			time = System.currentTimeMillis();			// result <-- SIMULATE(child)
			// result = true if player of root node wins
			double result = simulate(child, startTime);

	        System.out.println("sim:"+(System.currentTimeMillis()-time));
			time = System.currentTimeMillis();			// BACKPROPAGATE(result, child)
			backpropagate(result, child);

	        System.out.println("bac:"+(System.currentTimeMillis()-time));
			time = System.currentTimeMillis();			// repeat the four steps for set number of iterations
		}
        System.out.println("depth:"+depth);
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
	
	int depth;

	
	private double simulate(Node<S, A> node, long startTime) {
		P player = game.getPlayer(node.getState());
	    while (!game.isTerminal(node.getState()) && (System.currentTimeMillis() - startTime) < (max_time-1000)) {
	    	Random rand = new Random();
			A a = game.getActions(node.getState()).get(rand.nextInt(game.getActions(node.getState()).size()));
			S result = game.getResult(node.getState(), a);
			NodeFactory<S, A> nodeFactory = new NodeFactory<>();
			node = nodeFactory.createNode(result);
		}
		P p = game.getPlayer(tree.getRoot().getState());
		depth++;
		if (!game.isTerminal(node.getState())) 
			return 0.0;
		State.Turn outcome = ((State)node.getState()).getTurn();
		if (outcome == State.Turn.DRAW) {
			return 0.0;
		} else if ((outcome == State.Turn.WHITEWIN && p.equals(State.Turn.WHITE)) ||
				(outcome == State.Turn.BLACKWIN && p.equals(State.Turn.BLACK))) {
			return 1.0;
		} else {
			return -1.0;
		}
	}

	private void backpropagate(double result, Node<S, A> node) {
		tree.updateStats(result, node);
		if (tree.getParent(node) != null)
			backpropagate(result, tree.getParent(node));
	}
	
	private A bestAction(Node<S, A> root) {
		Node<S, A> bestChild = tree.getChildWithMaxPlayouts(root);
		for (A a : game.getActions(root.getState())) {
			S result = game.getResult(root.getState(), a);
			if (result.equals(bestChild.getState())) return a;
		}
		return null;
	}

	private boolean isNodeFullyExpanded(Node<S, A> node) {
		List<S> visitedChildren = tree.getVisitedChildren(node);
		for (A a : game.getActions(node.getState())) {
			S result = game.getResult(node.getState(), a);
			if (!visitedChildren.contains(result)) {
				return false;
			}
		}
		return true;
	}

	public <S, A> List<State> findDrawConditions(Node<S, A> node) {
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
		else ret.addAll((Collection<? extends State>) tree.getDrawConditions());
		return ret;
	}
	
	private Node<S, A> randomlySelectUnvisitedChild(Node<S, A> node) {
		List<S> unvisitedChildren = new ArrayList<>();
		List<S> visitedChildren = tree.getVisitedChildren(node);
		for (A a : game.getActions(node.getState())) {
			S result = game.getResult(node.getState(), a);
			if (!visitedChildren.contains(result)) unvisitedChildren.add(result);
		}
		Random rand = new Random();
		return tree.addChild(node, unvisitedChildren.get(rand.nextInt(unvisitedChildren.size())));
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
