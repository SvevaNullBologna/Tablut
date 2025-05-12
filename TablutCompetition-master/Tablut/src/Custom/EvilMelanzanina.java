package Custom;

import java.io.IOException;
import java.net.UnknownHostException;

public class EvilMelanzanina extends it.unibo.ai.didattica.competition.tablut.client.TablutRandomClient {

	public EvilMelanzanina(String color , String ipAddress) throws UnknownHostException, IOException {
		super(color, "EVIL_MELANZANINA", 60 , ipAddress);
		// TODO Auto-generated constructor stub
	}

	//setName(String)
	//String getName
	//declareName() = write the name to the server
	//write(Action) = write to the server an action
	//read() = gets state from server
	
}
