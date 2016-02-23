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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import jade.proto.SubscriptionInitiator;

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
	
	/** Permet d'obtenir l'emploi que l'on a envoye à un certain individu. 
	 *  La référence emploi est le nom local de l'employeur correspondant, à
	 *  laquelle on concatène la référence de l'emploi.*/
	private HashMap<String, Emploi> referencesEmplois;

	
	//Toujours retraite à faire.
	//Il faut enlever individu de statutIndividus lors qu'il prend sa retraite.
	
	/** Agent init */
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! PoleEmploi-agent"+ getAID().getName()+ " is ready.");
		
		statutIndividus = new HashMap<AID, StatutEmploye>();
		statutEmplois = new HashMap<Emploi, StatutEmploi>();
		emploisEnvoyes = new HashMap<AID, Emploi>();
		
		referencesEmplois = new HashMap<String, Emploi>();
	
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
					
						Emploi emp = (Emploi)msg.getContentObject();
						statutEmplois.put(emp, StatutEmploi.Disponible);
						
						String refEmploi = msg.getSender().getLocalName()+emp.getRefEmploi();
						referencesEmplois.put(refEmploi, emp);
						proposerEmploi(refEmploi);

					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
				
				else {
					String content = msg.getContent();
					if (content.equals("Turn")){
						statistiques();
						//System.out.println("PoleEmploi starting turn");
					}
					else if (content.startsWith("EmploiAccepte")){
						statutIndividus.put(msg.getSender(), StatutEmploye.Employe);
		
						AID senderAID = msg.getSender();
						Emploi emploiAccepte = emploisEnvoyes.get(senderAID);
						String refEmploi = senderAID.getLocalName()+emploiAccepte.getRefEmploi();
						referencesEmplois.remove(refEmploi);
						statutEmplois.remove(emploiAccepte);
						emploisEnvoyes.remove(senderAID);
					}
					else if (content.equals("Inscription")){
						statutIndividus.put(msg.getSender(), StatutEmploye.Chomage);
					}
					else if (content.startsWith("Demission")){
						statutIndividus.put(msg.getSender(), StatutEmploye.Chomage);
					}
					else if (content.equals("EmploiRefuse")){
						Emploi emploiRepropose = emploisEnvoyes.get(msg.getSender());
						statutEmplois.put(emploiRepropose, StatutEmploi.Disponible);
						
						String refEmploi = msg.getSender().getLocalName()+emploiRepropose.getRefEmploi();
						referencesEmplois.put(refEmploi, emploiRepropose);
						proposerEmploi(refEmploi);
						//emploisEnvoyes.remove(msg.getSender());
					}
					else if (content.equals("Retraite")){
						statutIndividus.remove(msg.getSender());
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
	private void proposerEmploi(String refEmploi) {
		Emploi emploi = referencesEmplois.get(refEmploi);
		
		//Si tous les individus ont déjà reçu une proposition d'emploi?		
		//Créer message
		ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
		inform.setConversationId("ProposeEmploi");
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
			referencesEmplois.remove(refEmploi);
		}
		else {
			addBehaviour(new ProposerEmploi(this,refEmploi));
		}
	}

	/** Comportement qui dispose d'un délai pour permettre à PoleEmploi de lire les messages.*/
	private class ProposerEmploi extends OneShotBehaviour {
		String refEmploi;
		
		public ProposerEmploi(Agent a, String _refEmploi){
			super(a);
			refEmploi = _refEmploi;
		}

		public void action() {
			myAgent.addBehaviour(
				new WakerBehaviour(myAgent, 2000){
					public void handleElapsedTimeout() { 
						proposerEmploi(refEmploi);
					}
				}		
			);
			
		}
		
	}
	
	
	private void statistiques(){
		System.out.println("S");
		//int count = Collections.frequency(new ArrayList<String>(HM.values()), "Red");
		int employes = Collections.frequency(statutIndividus.values(), StatutEmploye.Employe);
		int rechercheEmplois = Collections.frequency(statutIndividus.values(), StatutEmploye.Chomage);
		int individus = statutIndividus.size();
		int nombreEmploisNonPourvus = statutEmplois.size();
		int nombreEmploisEnvoyes = emploisEnvoyes.size();
		int nombreReferencesEmplois = referencesEmplois.size();
		
		System.out.println(individus + " individus dans le système");
		System.out.println(employes + " individus employes dans le système");
		System.out.println(rechercheEmplois + " individus en recherche d'emplois dans le système");
		System.out.println(nombreEmploisNonPourvus + " nombreEmploisNonPourvus dans le système");
		System.out.println(nombreEmploisEnvoyes + " nombreEmploisEnvoyes dans le système");
		
		System.out.println(nombreReferencesEmplois + " nombreReferencesEmplois dans le système");
	}
}
