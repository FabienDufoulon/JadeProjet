package core;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.HashMap;
import java.util.Iterator;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class PoleEmploi extends Agent {
	/** Statut des employes. Surtout utile � des fins statistiques. */
	private enum StatutEmploye {Employe, Chomage};
	/** Statut des emplois. Pour savoir s'il faut s'occuper de cet emploi ou non. */
	private enum StatutEmploi {Attente, Disponible};

	/** Permet de lier les individus et leur statut. */
	private HashMap<AID, StatutEmploye> statutIndividus;
	/** Permet de lier les emplois et leur statut. */
	private HashMap<Emploi, StatutEmploi> statutEmplois;
	/** Permet d'obtenir l'emploi que l'on a envoye � un certain individu. */
	private HashMap<AID, Emploi> emploisEnvoyes;

	
	//Agent init
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! PoleEmploi-agent"+ getAID().getName()+ " is ready.");
		
		statutIndividus = new HashMap<AID, StatutEmploye>();
		statutEmplois = new HashMap<Emploi, StatutEmploi>();
		emploisEnvoyes = new HashMap<AID, Emploi>();
		
		//Ajout des comportements
		addBehaviour(new AttenteMessage());
	}
	
	//Agent clean-up
	protected void takeDown(){
		//Dismissal message
		System.out.println("PoleEmploi-agent " + getAID().getName() + " terminating.");
	}
	
	private class AttenteMessage extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String content = msg.getContent();
				if (content.equals("Turn")){
					System.out.println("PoleEmploi starting turn");
				}
				else if (content.startsWith("EmploiAccepte")){
					
				}
				else if (content.startsWith("EmploiRefuse")){
					
				}
				else if (msg.getConversationId().equals("PublierEmplois")){
					try {
						statutEmplois.put((Emploi)msg.getContentObject(), StatutEmploi.Disponible);
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
				else {
					DFAgentDescription[] dfds;
					try {
						dfds = DFService.decodeNotification(msg.getContent());
						if (dfds.length > 0){
							Iterator dfservice = dfds[0].getAllServices();
							while (dfservice.hasNext()){
								ServiceDescription sd = (ServiceDescription) dfservice.next();
								if (sd.getType().startsWith("nivQualif")){
									if (statutIndividus.containsKey(dfds[0].getName())){
										statutIndividus.replace(dfds[0].getName(), StatutEmploye.Employe, StatutEmploye.Chomage);
									}
									else {
										statutIndividus.put(dfds[0].getName(), StatutEmploye.Chomage);
									}
								}
							}
			            }
					} catch (FIPAException e) {
						e.printStackTrace();
					}
				}
			}
			else {
				block();
			}
		}
	}
}
