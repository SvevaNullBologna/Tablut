package Custom;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.*;


public class Melanzanina extends it.unibo.ai.didattica.competition.tablut.client.TablutClient {

	boolean firstRun = true;
	Game tablut;
	TreeNode current;
	TreeNode last;
	
	public Melanzanina(String color , String ipAddress) throws UnknownHostException, IOException {
		super(color, "MELANZANINA", 60 , ipAddress);
		// TODO Auto-generated constructor stub
	}

	//setName(String)
	//String getName
	//declareName() = write the name to the server
	//write(Action) = write to the server an action
	//read() = gets state from server
	

	public static void main(String[] args) throws IOException {
		
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
            		//TODO add white turn logic
            	}else if(state.getTurn().equals(State.Turn.BLACK)){
            		//opponent turn. wait...
            	}else {
            		//la partita è finita, indipendentemente dal risultato
            		System.exit(0);
            	}
            }else{
            	if(state.getTurn().equals(State.Turn.BLACK)) {
            		//TODO add black turn logic
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
