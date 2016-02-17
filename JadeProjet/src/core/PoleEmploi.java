package core;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class PoleEmploi extends Agent {
	/** Statut des employes. Surtout utile à des fins statistiques. */
	private enum StatutEmploye {Employe, Chomage};
	/** Statut des emplois. Pour savoir s'il faut s'occuper de cet emploi ou non. */
	private enum StatutEmploi {Attente, Disponible};

	/** Permet de lier les individus et leur statut. */
	private HashMap<AID, StatutEmploye> statutIndividus;
	/** Permet de lier les emplois et leur statut. */
	private HashMap<Emploi, StatutEmploi> statutEmplois;
	/** Permet d'obtenir l'emploi que l'on a envoye à un certain individu. */
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
				
				if (msg.getConversationId().equals("PublierEmplois")){
					try {
						statutEmplois.put((Emploi)msg.getContentObject(), StatutEmploi.Disponible);
						proposerEmploi((Emploi)msg.getContentObject());
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
				
				else {
					String content = msg.getContent();
					if (content.equals("Turn")){
						System.out.println("PoleEmploi starting turn");
					}
					else if (content.startsWith("EmploiAccepte")){
						statutIndividus.put(msg.getSender(), StatutEmploye.Employe);
						statutEmplois.remove(emploisEnvoyes.get(msg.getSender()), StatutEmploi.Attente);
						emploisEnvoyes.remove(msg.getSender());
					}
					else if (content.startsWith("EmploiRefuse")){
						Emploi emploiRepropose = emploisEnvoyes.get(msg.getSender());
						statutEmplois.put(emploiRepropose, StatutEmploi.Disponible);
						proposerEmploi(emploiRepropose);
						emploisEnvoyes.remove(msg.getSender());
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
										statutIndividus.put(dfds[0].getName(), StatutEmploye.Chomage);
									}
								}
				            }
						} catch (FIPAException e) {
							e.printStackTrace();
						}
					}
				}
				
			}
			else {
				block();
			}
		}
	}
	
	private void proposerEmploi(Emploi emploi) {
		//Si tous les individus ont déjà reçu une proposition d'emploi?
		
		//Créer message
		ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
		AID individuDestine = null;
		do {
			individuDestine = Util.getRandomService(this, "nivQualif"+emploi.getNiveauQualificationNecessaire());
		} while (emploisEnvoyes.containsKey(individuDestine));
		inform.addReceiver(individuDestine);
		try {
			inform.setContentObject(emploi);
		} catch (IOException e) {
			e.printStackTrace();
		}
		send(inform);
		
		//Changer ses infos
		statutEmplois.put(emploi, StatutEmploi.Attente);
		emploisEnvoyes.put(individuDestine, emploi);
	}
}
