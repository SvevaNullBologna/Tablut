package Custom;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Logger;

import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class Melanzanina extends it.unibo.ai.didattica.competition.tablut.client.TablutClient {

	private final Game tablut;
	private final State.Turn turn;
	private final MCTS mcts;
	private TreeNode current;
	private TreeNode previous;

	private final Random rand = new Random();

	public Melanzanina(String player, int timeout, String ipAddress) throws UnknownHostException, IOException {
		super(player.toUpperCase(), "Melanzanin", timeout, ipAddress);
		this.turn = Turn.valueOf(player.toUpperCase());
        this.tablut = new GameAshtonTablut(0, -1, "logs", "white_ai", "black_ai");
        this.mcts = new MCTS(timeout, 10, tablut);

	}

	// setName(String)
	// String getName
	// declareName() = write the name to the server
	// write(Action) = write to the server an action
	// read() = gets state from server

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
			System.out.println("ERROR: Timeout must be an integer representing seconds\n"
					+ "USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip>\n");
			System.exit(1);
		}

	}

	@Override
	public void run() {
		try {
			this.declareName();
		} catch (Exception e) {
			System.err.println("Failed to declare name to server.");
		}

		State state = new StateTablut();
		PrintStream originalOut = System.out;
		PrintStream nullStream = new PrintStream(new OutputStream() {
			@Override
			public void write(int b) {
				// Suppress output
			}
		});

		while (true) {
			try {
				this.read();
			} catch (ClassNotFoundException | IOException e) {
				System.err.println("Error reading state from server.");
				System.exit(2);
			}

			state = this.getCurrentState();
			if (!state.getTurn().equals(Turn.BLACK) && !state.getTurn().equals(Turn.WHITE)) {
				break; //termina partita
			}
			if (state.getTurn().equals(this.turn)) {
				this.previous = this.current;
				this.current = new TreeNode(state, previous, null);

				Logger gameLogger = Logger.getLogger("GameLog");
				gameLogger.setUseParentHandlers(false);

				try {
					TreeNode bestNode = mcts.montecarlo(current, this.turn);
					if (bestNode == null || bestNode.getOriginAction() == null) {
						throw new IllegalStateException("MCTS returned no valid move.");
					}
					System.setOut(originalOut); // restore output
					System.out.println("Mossa scelta: " + bestNode.getOriginAction());

					this.write(bestNode.getOriginAction());
				} catch (Exception e) {
					System.setOut(originalOut); // restore output
					System.out.println("ERRORE durante l'esecuzione di MCTS:");
					e.printStackTrace();
					return;
				}
			}
		}
	}
}
