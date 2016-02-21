package core;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
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
import jade.domain.FIPAAgentManagement.SearchConstraints;
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

	
	//Toujours retraite à faire.
	//Il faut enlever individu de statutIndividus lors qu'il prend sa retraite.
	
	/** Agent init */
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! PoleEmploi-agent"+ getAID().getName()+ " is ready.");
		
		statutIndividus = new HashMap<AID, StatutEmploye>();
		statutEmplois = new HashMap<Emploi, StatutEmploi>();
		emploisEnvoyes = new HashMap<AID, Emploi>();
		
		//Subscribing
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		SearchConstraints sc = new SearchConstraints();
		sc.setMaxResults(new Long(1));
		
		for ( int i = 1; i <= 3; i++ ) {
			sd.setType("nivQualif" + i);
			dfd.addServices(sd);
			send(DFService.createSubscriptionMessage(this, getDefaultDF(), dfd, sc));
			dfd.removeServices(sd);
		}
		
		//Ajout des comportements
		addBehaviour(new AttenteMessage());
	}
	
	/** Agent clean-up */
	protected void takeDown(){
		//Dismissal message
		System.out.println("PoleEmploi-agent " + getAID().getName() + " terminating.");
	}
	
	/** Comportement pour la lecture des messages INFORM et leurs traitements. */
	private class AttenteMessage extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				
				if (msg.getConversationId() != null && msg.getConversationId().equals("PublierEmplois")){
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
					else if (content.equals("EmploiRefuse")){
						Emploi emploiRepropose = emploisEnvoyes.get(msg.getSender());
						statutEmplois.put(emploiRepropose, StatutEmploi.Disponible);
						proposerEmploi(emploiRepropose);
						emploisEnvoyes.remove(msg.getSender());
					}
					else if (content.equals("Retraite")){
						statutIndividus.remove(msg.getSender());
					}
					else {
						DFAgentDescription[] dfds;
						try {
							dfds = DFService.decodeNotification(msg.getContent());
							if (dfds.length > 0)
								statutIndividus.put(dfds[0].getName(), StatutEmploye.Chomage);
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
	
	/** Gère l'envoi des messages de PoleEmploi à un individu qualifié aléatoire, qui ne dispose 
	 *  pas déjà d'un proposition d'emploi.*/
	private void proposerEmploi(Emploi emploi) {
		//Si tous les individus ont déjà reçu une proposition d'emploi?		
		//Créer message
		ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
		AID individuDestine = null;
		
		individuDestine = Util.getRandomService(this, "nivQualif"+emploi.getNiveauQualificationNecessaire());
		if (!emploisEnvoyes.containsKey(individuDestine)){
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
		else {
			addBehaviour(new ProposerEmploi(this,emploi));
		}		
	}

	/** Comportement qui dispose d'un délai pour permettre à PoleEmploi de lire les messages.*/
	private class ProposerEmploi extends OneShotBehaviour {
		Emploi emploi;
		
		public ProposerEmploi(Agent a, Emploi emp){
			super(a);
			emploi = emp;
		}

		public void action() {
			myAgent.addBehaviour(
				new WakerBehaviour(myAgent, 2000){
					public void handleElapsedTimeout() { 
						proposerEmploi(emploi);
					}
				}		
			);
			
		}
		
	}
}
