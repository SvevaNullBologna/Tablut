package Custom;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Logger;

import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import aima.core.search.adversarial.*;
import aima.core.search.adversarial.Game;

public class MelanzaninaAIMA extends it.unibo.ai.didattica.competition.tablut.client.TablutClient {

	AIMAGameAshtonTablut tablut;
	TreeNode current;
	TreeNode last;
	MonteCarloTreeSearch<State,Action,State.Turn> mcts;

	public MelanzaninaAIMA(String player, int timeout, String ipAddress) throws UnknownHostException, IOException {
		super(player.toUpperCase(), "Melanzanin", timeout, ipAddress);
        this.tablut = new AIMAGameAshtonTablut(0, -1, "logs", "white_ai", "black_ai");;
        this.mcts = new MonteCarloTreeSearch<State, Action, Turn>(tablut,0,timeout);
		// TODO Auto-generated constructor stub
	}

	// setName(String)
	// String getName
	// declareName() = write the name to the server
	// write(Action) = write to the server an action
	// read() = gets state from server

	public static void main(String[] args) throws IOException {
		String ip = "localhost";
		int timeout = 60000;

		if (args.length != 3) {
			System.out.printf("Usage: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip>\\n\")");
			System.exit(0);
		}
		try {
			timeout = Integer.parseInt(args[1])*1000;
			ip = args[2];
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.printf("ERROR: Timeout must be an integer representing seconds\n"
					+ "USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip>\n");
			System.exit(1);
		}
		MelanzaninaAIMA player = new MelanzaninaAIMA(args[0], timeout, ip);
		player.run();
	}

	@Override
	public void run() {
	    try {
	        this.declareName();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    Random rand = new Random();
	    State state = new StateTablut();
	    PrintStream originalOut = System.out;
	    PrintStream nullStream = new PrintStream(new OutputStream() {
	        @Override
	        public void write(int b) {
	            // Non fa nulla
	        }
	    });
	    while (true) {
	        try {
	            this.read();
	        } catch (ClassNotFoundException | IOException e) {
	            e.printStackTrace();
	            System.exit(2);
	        }

	        state = this.getCurrentState();
	        if (!state.getTurn().equals(Turn.BLACK) && !state.getTurn().equals(Turn.WHITE))
	            break;

	        if (this.getPlayer().equals(state.getTurn())) {

	            System.setOut(nullStream); // Java 11+
	            Logger gameLogger = Logger.getLogger("GameLog");
	            gameLogger.setUseParentHandlers(false);	            
	        	Action best=mcts.makeDecision(state);
	            System.setOut(originalOut); // ripristina output
	            try {
	                this.write(best);
	            } catch (ClassNotFoundException | IOException e) {
	                e.printStackTrace();
	            }
	        	/*last = current;
	            current = new TreeNode(state, last, null);
	            System.setOut(nullStream); // Java 11+
	            Logger gameLogger = Logger.getLogger("GameLog");
	            gameLogger.setUseParentHandlers(false);	    
	            
	            //mcts.montecarlo(current);
	            
	            System.setOut(originalOut); // ripristina output
	            TreeNode favorite = null;
	            double max = Double.NEGATIVE_INFINITY;
	            for (TreeNode child : current.getChildren()) { //volendo pesare con totalValue/visitCount
	                if (child.totalValue > max) {
	                    max = child.totalValue;
	                    favorite = child;
	                } else if (child.totalValue == max) {
	                    if (favorite == null || rand.nextInt(2)>0) {
	                        favorite = child;
	                    }
	                }
	            }

	            if (favorite == null) {
	                throw new IllegalStateException("Nessuna mossa valida trovata dopo MCTS.");
	            }

	            System.out.println("Mossa scelta: " + favorite.getOriginAction());
				
	            try {
	                this.write(favorite.getOriginAction());
	            } catch (ClassNotFoundException | IOException e) {
	                e.printStackTrace();
	            }*/
	            
	        }
	    }
	}

}
