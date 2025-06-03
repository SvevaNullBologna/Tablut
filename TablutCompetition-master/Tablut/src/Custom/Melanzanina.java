package Custom;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class Melanzanina extends it.unibo.ai.didattica.competition.tablut.client.TablutClient {

	private boolean first_time = true;
	private final Game tablut;
	private final State.Turn turn;
	private final MCTS mcts;
	private TreeNode tree;

	public Melanzanina(String player, int timeout, String ipAddress) throws UnknownHostException, IOException {
		super(player.toUpperCase(), "Melanzanin", timeout, ipAddress);
		this.turn = Turn.valueOf(player.toUpperCase());
        this.tablut = new GameAshtonTablut(0, -1, "logs", "white_ai", "black_ai");
        this.mcts = new MCTS(timeout,  tablut);

	}

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.out.println("Usage: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip>\\n\")");
			System.exit(0);
		}
		try {
			int timeout = Integer.parseInt(args[1])*1000;
			String ip = args[2];
			Melanzanina player = new Melanzanina(args[0], timeout, ip);
			player.run();

		} catch (NumberFormatException e) {
			writeLogs.handleCriticalError("ERROR: Timeout must be an integer representing seconds\n"
					+ "USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip>\n", e);
		}

	}

	private void updateTreeFromServer() {
		try {
			this.read();
			State current = this.getCurrentState();

			if (!current.getTurn().equals(Turn.BLACK) && !current.getTurn().equals(Turn.WHITE)) {
				return; // Game over
			}

			if (this.tree == null) {
				this.tree = new TreeNode(current, null, null);
				writeLogs.write("Tree initialized.\n");
				return;
			}

			writeLogs.write("Reading and updating tree...\n");

			// Ensure the tree is expanded before trying to match
			if (!tree.hasBeenExpanded()) {
				List<MoveResult> legalMoves = MoveResult.getLegalActionsAndResultingStates(tree.getState(), tablut);
				tree.ExpandNode(legalMoves);
			}

			// Try to match the current state with an existing child
			TreeNode matchingChild = this.tree.findChildWithState(current);
			if (matchingChild != null) {
				matchingChild.cutParent();
				this.tree = matchingChild;
				writeLogs.write("Tree reused: matched existing child.\n");
				return;
			}

			// Try to reconstruct the path from legal moves
			List<MoveResult> legalMoves = MoveResult.getLegalActionsAndResultingStates(this.tree.getState(), tablut);
			for (MoveResult move : legalMoves) {
				if (move.resultingState.equals(current)) {
					TreeNode newChild = new TreeNode(move.resultingState, this.tree, move.action);
					this.tree.addChild(newChild);
					newChild.cutParent();
					this.tree = newChild;
					writeLogs.write("Tree reused: reconstructed from legal move.\n");
					return;
				}
			}

			// If all else fails, reset the tree
			Action latestAction = Action.getPreviousAction(this.tree.getState(), current);
			this.tree = new TreeNode(current, null, latestAction);
			writeLogs.write("Tree reset: no match found.\n");

		} catch (IOException | ClassNotFoundException e) {
			writeLogs.handleCriticalError("Errore critico durante la lettura dal server: ", e);
		}
	}



	private void send_result_to_server(TreeNode result){
		try {
			this.write(result.getOriginAction());
		}
		catch(IOException | ClassNotFoundException e){
			writeLogs.handleCriticalError("Errore critico durante l'invio della mossa: ", e);
		}
	}

	@Override
	public void run() {
		if(first_time){
			try {
				this.declareName();
			} catch(IOException | ClassNotFoundException e){
				writeLogs.handleCriticalError("Errore critico durante la dichiarazione del nome: ", e);
			}
			first_time = false;
		}

		while (true) {
			updateTreeFromServer();
			if(tree!=null && isGameOver(this.tree.getState())){
				System.out.println("Game over: " + tree.getState().getTurn());
				break;
			}
			if (tree != null && tree.getState().getTurn().equals(this.turn)) {
				try {
					TreeNode bestNode = mcts.montecarlo(tree , this.turn);
					if (bestNode == null || bestNode.getOriginAction() == null) {
						throw new IllegalStateException("MCTS returned no valid move.");
					}
					send_result_to_server(bestNode);
					do{
						Thread.sleep(100);
						updateTreeFromServer();
					}
					while (tree.getState().getTurn().equals(this.turn));
				}
				catch (Exception e) {
					writeLogs.handleCriticalError("Errore critico durante lo svolgimento della logica dell'algoritmo: ", e);
				}
			}
		}
	}

	private boolean isGameOver(State state){
		Turn t = state.getTurn();
		return t == Turn.WHITEWIN || t == Turn.BLACKWIN || t == Turn.DRAW;
	}

}
