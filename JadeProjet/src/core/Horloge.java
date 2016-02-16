package core;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ContainerController;

import java.util.HashMap;
import java.util.function.IntSupplier;

import jade.core.AID;

/**
 * Cette classe sert à synchroniser les agents, pour qu'ils effectuent les actions
 * à chaque début de tour en même temps.
 * Prend en entrée quatre entiers.
 * Ils sont à entrer dans le même ordre que les déclarations ci-dessous.
 * C'est-à-dire tempsTour, nombreEntrants, nombreSortants, dernierID.
 *
 */
public class Horloge extends Agent {
	/** Temps d'un tour(un mois) */
	private int tempsTour;
	/** Nombre d'individus entrants au bout de 12 tour (un an) */
	private int nombreEntrants;
	/** Nombre d'individus sortants au bout de 12 tour (un an) */
	private int nombreSortants;
	/** Dernier numéro attribué(ainsi tous les individus ont un nom simple) */
	private int dernierID;	


	
	//Agent init
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! Horloge-agent"+ getAID().getName()+ " is ready.");
		
		//Crée l'horloge selon les arguments en entrée
		Object[] args = getArguments();
		if(args != null && args.length >= 4){
			tempsTour = (int) args[0];
			nombreEntrants = (int) args[1];
			nombreSortants = (int) args[2];
			dernierID = (int) args[3];
			
			//Ajout des comportements
			//Comportement : chaque mois
			addBehaviour( new TickerBehaviour(this,tempsTour){
				protected void onTick() {
					//System.out.println("Un tour est passé.");
					
					//Sending message
					ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
					inform = Util.createBroadcastMessage(myAgent, inform);
					inform.setContent("Turn");
					
					myAgent.send(inform);
				}
			});
			
			//Comportement : chaque an (ou si on veut être sûr de la séquence, mettre un compteur au dessus et l'envoi conditionnel)
			addBehaviour( new TickerBehaviour(this,3*tempsTour){
				protected void onTick() {
					//System.out.println("Les individus tournent.");
					
					//Sending message
					ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
					inform.addReceiver(Util.getRandomService(myAgent, "nivQualif2"));
					inform.setContent("Year");
					myAgent.send(inform);
					
					//Créer un nouveau individu
					try {
						ContainerController container = getContainerController(); // get a container controller for creating new agents
						container.createNewAgent("Individu"+dernierID, "examples.thanksAgent.ThanksAgent", null).start();
						dernierID++;
					} catch (Exception any) {
						any.printStackTrace();
					}
					
				}
			});
		}
		else{
			//Kill agent if he does not receive enough arguments
			System.out.println("Not enough input args");
			doDelete(); 
		}
		
	}
	
	//Agent clean-up
	protected void takeDown(){
		//Dismissal message
		System.out.println("PoleEmploi-agent " + getAID().getName() + " terminating.");
	}
}
