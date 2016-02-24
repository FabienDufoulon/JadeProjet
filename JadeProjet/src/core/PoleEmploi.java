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
	
	//Stat
	/** Permet d'obtenir le niveau de qualification d'un individu. */
	private HashMap<AID, Integer> niveauQualificationsIndividus;
	
	/** Permet d'obtenir le revenu min d'un individu. */
	private HashMap<AID, Integer> revenuMinIndividu;
	/** Permet d'obtenir le temps libre min d'un individu. */
	private HashMap<AID, Integer> tempsLibreMinIndividu;
	/** Permet d'obtenir le revenu moyen d'un individu. */
	private HashMap<AID, Integer> revenuMoyenIndividu;
	/** Permet d'obtenir le temps libre moyen d'un individu. */
	private HashMap<AID, Integer> tempsLibreMoyenIndividu;
	
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
		
		niveauQualificationsIndividus = new HashMap<AID, Integer>();
		
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
						AID employeurAID = emploiAccepte.getEmployeur(); 
						String refEmploi = employeurAID.getLocalName()+emploiAccepte.getRefEmploi();
						
						referencesEmplois.remove(refEmploi);
						statutEmplois.remove(emploiAccepte);
						emploisEnvoyes.remove(senderAID);
					}
					else if (content.startsWith("Inscription")){
						statutIndividus.put(msg.getSender(), StatutEmploye.Chomage);
						
						String[] split = msg.getContent().split(":");
						niveauQualificationsIndividus.put(msg.getSender(), Integer.parseInt(split[1]));
					}
					else if (content.startsWith("Demission")){
						statutIndividus.put(msg.getSender(), StatutEmploye.Chomage);
					}
					else if (content.equals("EmploiRefuse")){
						Emploi emploiRepropose = emploisEnvoyes.get(msg.getSender());
						statutEmplois.put(emploiRepropose, StatutEmploi.Disponible);
						
						String refEmploi = emploiRepropose.getEmployeur().getLocalName()+emploiRepropose.getRefEmploi();
						proposerEmploi(refEmploi);
					}
					else if (content.equals("Retraite")){
						statutIndividus.remove(msg.getSender());
						niveauQualificationsIndividus.remove(msg.getSender());
					}
					else if (content.startsWith("InformationTour")){
						String[] split = msg.getContent().split(":");
						tempsLibreMinIndividu.put(msg.getSender(), Integer.parseInt(split[1]));
						revenuMinIndividu.put(msg.getSender(), Integer.parseInt(split[2]));
					}
					else if (content.startsWith("InformationEmploye")){
						String[] split = msg.getContent().split(":");
						tempsLibreMoyenIndividu.put(msg.getSender(), Integer.parseInt(split[1]));
						revenuMoyenIndividu.put(msg.getSender(), Integer.parseInt(split[2]));
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
		//System.out.println("ProposeEmploi");
		Emploi emploi = referencesEmplois.get(refEmploi);
		
		//Si tous les individus ont déjà reçu une proposition d'emploi?		
		//Créer message
		ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
		inform.setConversationId("ProposeEmploi");
		AID individuDestine = null;
		
		//if emploi null, normalement pas possible
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

		int employes = Collections.frequency(statutIndividus.values(), StatutEmploye.Employe);
		int rechercheEmplois = Collections.frequency(statutIndividus.values(), StatutEmploye.Chomage);
		int individus = statutIndividus.size();
		int nombreEmploisNonPourvus = statutEmplois.size();
		int nombreEmploisEnvoyes = emploisEnvoyes.size();
		int nombreReferencesEmplois = referencesEmplois.size();
		float tauxChomage = (float) rechercheEmplois / individus;
		
		int individusQualif1 = Collections.frequency(niveauQualificationsIndividus.values(), 1);
		int individusQualif2 = Collections.frequency(niveauQualificationsIndividus.values(), 2);
		int individusQualif3 = Collections.frequency(niveauQualificationsIndividus.values(), 3);
		
		int revenuMinMoyen = Collections. (statutIndividus.values(). ,
		
		System.out.println(individus + " individus dans le système.");
		
		System.out.println(tauxChomage + " taux de chômage dans le système.");
		//System.out.println(employes + " individus employes dans le système");
		//System.out.println(rechercheEmplois + " individus en recherche d'emplois dans le système");

		System.out.println(employes+nombreEmploisNonPourvus+ " nombre d'emplois perçus par Pole Emploi en tout.");		
		
		System.out.println(nombreEmploisNonPourvus + " nombreEmploisNonPourvus dans le système");
		System.out.println(nombreEmploisEnvoyes + " nombreEmploisEnvoyes dans le système");
		
		System.out.println(individusQualif1 + " individus de niveau 1");	
		System.out.println(individusQualif2 + " individus de niveau 2");	
		System.out.println(individusQualif3 + " individus de niveau 3");		
		
		
		
		//int individusNivQualif1 = Util.searchDF(this,"nivQualif1").length;
		//int individusNivQualif2 = Util.searchDF(this,"nivQualif2").length;
		//int individusNivQualif3 = Util.searchDF(this,"nivQualif3").length;		
		//System.out.println(individusNivQualif1 + " individus de niveau 1 au chômage.");	
		//System.out.println(individusNivQualif2 + " individus de niveau 2 au chômage.");	
		//System.out.println(individusNivQualif3 + " individus de niveau 3 au chômage.");	
		
		//System.out.println(nombreReferencesEmplois + " nombreReferencesEmplois dans le système");
	}
}
