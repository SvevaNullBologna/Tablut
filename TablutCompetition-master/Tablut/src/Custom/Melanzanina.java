package Custom;

import java.io.IOException;
import java.net.UnknownHostException;
import it.unibo.ai.didattica.competition.tablut.domain.*;


public class Melanzanina extends it.unibo.ai.didattica.competition.tablut.client.TablutClient {
	public static final String PLAYER_NAME = "Melanzanina";
	
	Game tablut;
	TreeNode current;
	TreeNode last;
	
	public Melanzanina(String color , String ipAddress) throws UnknownHostException, IOException {
		super(color, PLAYER_NAME, 60 , ipAddress);
		// TODO Auto-generated constructor stub
	}
	
	public Melanzanina(String player, int timeout, String ipAddress) throws UnknownHostException, IOException {
		super(player.toUpperCase(), PLAYER_NAME, timeout, ipAddress);
		// TODO Auto-generated constructor stub
	}

	//setName(String)
	//String getName
	//declareName() = write the name to the server
	//write(Action) = write to the server an action
	//read() = gets state from server

	public static void main(String[] args) throws IOException {
		String ip = "localhost";
        int timeout = 60;
		
		if (args.length != 3) {
			System.out.printf("Usage: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip>\\n\")");
			System.exit(0);
		} else if (args[0].toUpperCase()!="WHITE" && args[0].toUpperCase()!="BLACK") {
			System.out.println("You must specify which player you are [WHITE | BLACK]");
		}
		try {
            timeout = Integer.parseInt(args[1]);
            ip = args[2];
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.out.printf("ERROR: Timeout must be an integer representing seconds\n" +
                    "USAGE: ./runmyplayer <black|white> <timeout-in-seconds> <server-ip>\n");
            System.exit(1);
        }
		Melanzanina player = new Melanzanina(args[0].toUpperCase(), timeout, ip);
		player.run();
	}
	

	
	@Override
	public void run() {
		try {
            this.declareName();
        } catch (Exception e) {
            e.printStackTrace();
        }
		State state = new StateTablut();
        state.setTurn(State.Turn.WHITE); // WHITE makes the first move
        while(true) {
        	try {
        		this.read();
        	} catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
                System.exit(2);
            }
        	
        	// Update the state received
            state = this.getCurrentState();
            System.out.println(state.toString()); //TODO remove
            
            if(this.getPlayer().equals(State.Turn.WHITE)) {
            	if(state.getTurn().equals(State.Turn.WHITE)) {
            		
            		Action a; //TODO add white turn logic to get best action
            		
            		try {
                        this.write(a);
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
            		
            	}else if(state.getTurn().equals(State.Turn.BLACK)){
            		//opponent turn. wait...
            	}else {
            		//la partita è finita, indipendentemente dal risultato
            		System.exit(0);
            	}
            }else{
            	if(state.getTurn().equals(State.Turn.BLACK)) {
            		
            		Action a; //TODO add black turn logic to get best action
            		
            		try {
                        this.write(a);
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
            		
            	}else if(state.getTurn().equals(State.Turn.WHITE)){
            		//opponent turn. wait...
            	}else {
            		//la partita è finita, indipendentemente dal risultato
            		System.exit(0);
            	}
            }
        }
	}
}
