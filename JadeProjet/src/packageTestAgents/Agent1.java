package packageTestAgents;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

//Simule Etat : donne emploi

public class Agent1 extends Agent {
	Emploi emploiEtat;
	
	//Agent init
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! "+ getAID().getName()+ " is ready.");
		emploiEtat = new Emploi(2,0);
		
		//Add Ticker Behaviour
		/*
		addBehaviour(new TickerBehaviour(this,30000) {
			protected void onTick() {
				System.out.println("Sending message!");
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			    msg.setContent( "bla...bla...bla" );
			    msg.addReceiver( new AID( "Individu", AID.ISLOCALNAME) );
			    send(msg);
			}
		});*/
		addBehaviour(new TickerBehaviour(this,10000) {
			protected void onTick() {
				System.out.println("Sending message!");
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			    try {
					msg.setContentObject(emploiEtat);
				} catch (IOException e) {
					e.printStackTrace();
				}
			    msg.addReceiver( new AID( "Individu", AID.ISLOCALNAME) );
			    send(msg);
			}
		});
		
		
		//Add Message receiver
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				
				//MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage msg = myAgent.receive();
				if(msg != null){
					System.out.println("Receiving actual message");
					Emploi emploiRecu2 = new Emploi(0,0);
					try {
						emploiRecu2 = (Emploi) msg.getContentObject();
					} catch (UnreadableException e) {
						e.printStackTrace();
					}

					System.out.println("Instance emploi : " + emploiRecu2.revenu + " " + emploiRecu2.id);
					System.out.println(emploiEtat);
					System.out.println(emploiRecu2);
				}
				else{
					block();
				}
			}
		});
	}
	
	//Agent clean-up
	protected void takeDown(){
		//Dismissal message
		System.out.println(getAID().getName() + " terminating.");
	}
}
