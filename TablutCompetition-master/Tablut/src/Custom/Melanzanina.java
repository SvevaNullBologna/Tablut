package Custom;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;


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
		//RICORDA DI PASSARE ALL'ALGORITMO 
		try {
            this.declareName();
        } catch (Exception e) {
            e.printStackTrace();
        }
		observe();
		//think();
		//act();
	}
	
	
	
	///////////E' IL PLAYER A OSSERVARE LE PEDINE E A VEDERE QUALI MOSSE SONO POSSIBILI
	
	private TreeNode observe() {
		try {
			this.read();
			State latest_enemy_turn = this.getCurrentState();
			
			if(firstRun) {
				
				//how do we get from last to received state?
				//we must recognize the state we are actually at on our treeNode?
				//do pruning while observing?
				}
			
			
			return new TreeNode(); //DEBUG
		}
		catch(ClassNotFoundException notfound){
			return null;
		}
		catch(IOException ioe) {
			return null;
		}
	}
	
	
	


    ///////////////////////////quale azione è migliore? Si possono effettivamente svolgere delle azioni?
    
    private void think(List<Action> allPossibleActions) { 
    	if(allPossibleActions.isEmpty()) {
    		//WE LOST OR WE WON?
    	}
    	else {
    		//what kind of algorithm would you like to use?
    		//return new Solution();
    	}
		
	}
    
    
    
    ///////////////////////////////////////// 
    ///
	private void act() {//INPUT = SOLUTION
		//try {
			//this.write(result.toActionToServer(getName(), "servername"));
		//}
		/*catch(ClassNotFoundException notfound){
			
		}
		catch(IOException ioe) {
			
		}*/
	}
    
	

}
