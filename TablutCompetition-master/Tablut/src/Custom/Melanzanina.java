package Custom;

import java.io.IOException;
import java.net.UnknownHostException;
import it.unibo.ai.didattica.competition.tablut.domain.State;


public class Melanzanina extends it.unibo.ai.didattica.competition.tablut.client.TablutClient {

	boolean firstRun = true;
	
	public Melanzanina(String color , String ipAddress) throws UnknownHostException, IOException {
		super(color, "MELANZANINA", 60 , ipAddress);
		// TODO Auto-generated constructor stub
	}

	//setName(String)
	//String getName
	//declareName() = write the name to the server
	//write(Action) = write to the server an action
	//read() = gets state from server
	
	private void observe() {
		try {
			this.read();
			State current = this.getCurrentState();
			//do pruning while observing?
		}
		catch(ClassNotFoundException notfound){
			
		}
		catch(IOException ioe) {
			
		}
	}
	
	private void think() {
		//what kind of algorithm would you like to use?
		//return new Solution();
	}
	
	private void act() {//INPUT = SOLUTION
		//try {
			//this.write(result.toActionToServer(getName(), "servername"));
		//}
		/*catch(ClassNotFoundException notfound){
			
		}
		catch(IOException ioe) {
			
		}*/
	}
	
	@Override
	public void run() {
		
		
		if(firstRun) {
			setName(this.getName());
		}
		observe();
		think();
		//act();
	}
	
	
	

}
