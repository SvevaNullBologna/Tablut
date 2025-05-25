package Custom;

import java.io.IOException;
import java.net.UnknownHostException;

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

	private void updateTreeFromServer(){
		try {
			this.read();
			State current = this.getCurrentState();
			if (!current.getTurn().equals(Turn.BLACK) && !current.getTurn().equals(Turn.WHITE)) {//se non è il turno di nessuno
				return; //termina partita
			}

			if(this.tree == null){
				this.tree = new TreeNode(current, null, null);
				return;
			}
			TreeNode matchingChild = this.tree.findChildWithState(current);
			if(matchingChild!=null){
				matchingChild.cutParent();
				this.tree =  matchingChild;
			}
			else{
				Action latestAction = mcts.getPreviousAction(this.tree.getState(), current);
				this.tree = new TreeNode(current, this.tree, latestAction);
			}
		}
		catch (IOException | ClassNotFoundException e){
			writeLogs.handleCriticalError("Errore critico durante la lettura dal server: ", e );
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
