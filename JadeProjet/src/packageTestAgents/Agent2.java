package packageTestAgents;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

//Simule individu : reçoit emploi

public class Agent2 extends Agent {

	//Agent init
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! "+ getAID().getName()+ " is ready.");
		
		//Add Message receiver
		/*
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				
				//MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage msg = myAgent.receive();
				if(msg != null){
					System.out.println("Receiving actual message");
					String title = msg.getContent();

					System.out.println(title);
				}
				else{
					block();
				}
			}
		});*/
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				
				//MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage msg = myAgent.receive();
				if(msg != null){
					System.out.println("Receiving actual message");
					Emploi emploiRecu = new Emploi(0,0);
					try {
						emploiRecu = (Emploi) msg.getContentObject();
					} catch (UnreadableException e) {
						e.printStackTrace();
					}

					//System.out.println("Instance emploi : " + emploiRecu.revenu + " " + emploiRecu.id);
					//emploiRecu.setId(2);
					//System.out.println("Instance emploi : " + emploiRecu.revenu + " " + emploiRecu.id);
					
					ACLMessage reply = msg.createReply();
                    reply.setPerformative( ACLMessage.INFORM );
                    try {
						reply.setContentObject((Emploi) msg.getContentObject());
					} catch (IOException e) {
						e.printStackTrace();
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
                    send(reply);
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
