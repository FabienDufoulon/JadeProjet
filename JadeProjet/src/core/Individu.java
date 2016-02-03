package core;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

public class Individu extends Agent {
	// Title of book to buy
	private String targetBookTitle;
	
	//Agent init
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! Buyer-agent"+ getAID().getName()+ " is ready.");
	   	
		//Get title of book to buy as start-up argument
		Object[] args = getArguments();
		if(args != null && args.length > 0){
			targetBookTitle = (String) args[0];
			System.out.println("Trying to buy " + targetBookTitle);			
		}
		else{
			//Kill agent if he receives no book title at start-up
			System.out.println("No book title specified");
			doDelete(); 
		}
	}
	
	//Agent clean-up
	protected void takeDown(){
		//Dismissal message
		System.out.println("Buyer-agent " + getAID().getName() + " terminating.");
	}
}
