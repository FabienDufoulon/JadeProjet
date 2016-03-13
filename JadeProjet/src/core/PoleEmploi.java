package core;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import jade.core.AID;


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
	
	/** Permet de compter le nombre de personnes qui ont choisis de démissionner par tour, et le nombre de fois qu'ils l'ont fait */
	private HashMap<AID, Integer> demissionParTour;
	/** Permet de compter le nombre de personnes qui ont refusé un emploi par tour, et le nombre de fois qu'ils l'ont fait */
	private HashMap<AID, Integer> refusParTour;
	
	
	//Statistiques Gui
	private StatistiquesGui statGui;
	
	//Sortie fichier
	ArrayList<Double> tauxChomageTemps;
	ArrayList<Double> proportionNiv1Temps;
	ArrayList<Double> proportionNiv2Temps;
	ArrayList<Double> proportionNiv3Temps;
	ArrayList<Double> revenuMinMoyenTemps;
	int toursOutLim = 100;
	int toursOut = 0;

	
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
		
		//Stat
		niveauQualificationsIndividus = new HashMap<AID, Integer>();
		revenuMinIndividu = new HashMap<AID, Integer>();
		tempsLibreMinIndividu = new HashMap<AID, Integer>();
		revenuMoyenIndividu = new HashMap<AID, Integer>();
		tempsLibreMoyenIndividu = new HashMap<AID, Integer>();
		demissionParTour = new HashMap<AID, Integer>();
		refusParTour = new HashMap<AID, Integer>();

		//Sortie fichier
		tauxChomageTemps = new ArrayList<Double>();
		proportionNiv1Temps = new ArrayList<Double>();
		proportionNiv2Temps = new ArrayList<Double>();
		proportionNiv3Temps = new ArrayList<Double>();
		revenuMinMoyenTemps = new ArrayList<Double>();
		
		// Create and show the GUI 
		statGui = new StatistiquesGui(this);
		statGui.showGui();
	
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
				
				/*if (msg.getConversationId() != null && msg.getConversationId().startsWith("PublierEmploisEntreprise:")){
					try {
						Emploi emp = (Emploi)msg.getContentObject();
						statutEmplois.put(emp, StatutEmploi.Disponible);
						
						String refEmploi = msg.getSender().getLocalName()+emp.getRefEmploi();
						referencesEmplois.put(refEmploi, emp);
						proposerEmploi(refEmploi);
						
						//HashMap<Emploi, Integer> 

					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}*/
				
				else {
					String content = msg.getContent();
					if (content.equals("Turn")){
						statistiques();
						toursOut++;
						
						demissionParTour.clear();
						refusParTour.clear();
						
						
						if (toursOut >= toursOutLim){
							toursOut = 0;
							System.out.println("Sortie fichier");
							try{
								FileWriter writer = new FileWriter("Out/output.txt"); 
								for(double d: tauxChomageTemps) {
									String st = d + ";";
									writer.write(st);
								}
								writer.close();
							}
							catch(Exception e){}
						}
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
						AID senderAID = msg.getSender();
						statutIndividus.put(senderAID, StatutEmploye.Chomage);
						
						//Statistique
						demissionParTour.put(senderAID, demissionParTour.containsKey(senderAID) ? demissionParTour.get(senderAID)+1 : 1);
					}
					else if (content.equals("EmploiRefuse")){
						AID senderAID = msg.getSender();
						Emploi emploiRepropose = emploisEnvoyes.get(senderAID);
						emploisEnvoyes.remove(senderAID);
						statutEmplois.put(emploiRepropose, StatutEmploi.Disponible);
						
						//Statistique
						refusParTour.put(senderAID, refusParTour.containsKey(senderAID) ? refusParTour.get(senderAID)+1 : 1);
						//
						
						String refEmploi = emploiRepropose.getEmployeur().getLocalName()+emploiRepropose.getRefEmploi();
						proposerEmploi(refEmploi);
						
					}
					else if (content.equals("Retraite")){
						//System.out.println("S " + statutIndividus.size());
						statutIndividus.remove(msg.getSender());
						niveauQualificationsIndividus.remove(msg.getSender());
						tempsLibreMinIndividu.remove(msg.getSender());
						revenuMinIndividu.remove(msg.getSender());
						tempsLibreMoyenIndividu.remove(msg.getSender());
						revenuMoyenIndividu.remove(msg.getSender());
						//System.out.println("E " + statutIndividus.size());
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
		
		//Créer message
		ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
		inform.setConversationId("ProposeEmploi");
		AID individuDestine = null;
		
		individuDestine = Util.getRandomService(this, "nivQualif"+emploi.getNiveauQualificationNecessaire());
		if (!emploisEnvoyes.containsKey(individuDestine) && individuDestine != null){
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
				new WakerBehaviour(myAgent, 1000){
					public void handleElapsedTimeout() { 
						proposerEmploi(refEmploi);
					}
				}		
			);
			
		}
		
	}
	
	
	private void statistiques(){
//		System.out.println("S");

		int employes = Collections.frequency(statutIndividus.values(), StatutEmploye.Employe);
		int rechercheEmplois = Collections.frequency(statutIndividus.values(), StatutEmploye.Chomage);
		int individus = statutIndividus.size();
		int nombreEmploisNonPourvus = statutEmplois.size();
		int nombreEmploisEnvoyes = emploisEnvoyes.size();
		//int nombreReferencesEmplois = referencesEmplois.size();
		double tauxChomage = (double) rechercheEmplois / individus;
		
		int individusQualif1 = Collections.frequency(niveauQualificationsIndividus.values(), 1);
		//int individusQualif1 = Util.searchDF(this,"nivQualif1").length;
		int individusQualif2 = Collections.frequency(niveauQualificationsIndividus.values(), 2);
		//int individusQualif2 = Util.searchDF(this,"nivQualif2").length;
		int individusQualif3 = Collections.frequency(niveauQualificationsIndividus.values(), 3);
		//int individusQualif3 = Util.searchDF(this,"nivQualif3").length;
		
		double proportionNiv1 = (double) individusQualif1 / individus;
		double proportionNiv2 = (double) individusQualif2 / individus;
		double proportionNiv3 = (double) individusQualif3 / individus;
		
		int refusParTourPersonnes = refusParTour.size();
		int demissionParTourPersonnes = demissionParTour.size();
		
		int revenuMinMoyen = average(revenuMinIndividu.values());
		int tempsLibreMinMoyen = average(tempsLibreMinIndividu.values());
		int revenuMoyenMoyen = average(revenuMoyenIndividu.values());
		int tempsLibreMoyenMoyen = average(tempsLibreMoyenIndividu.values());
		
		tauxChomageTemps.add((double) tauxChomage);
		proportionNiv1Temps.add((double) proportionNiv1);
		proportionNiv2Temps.add((double) proportionNiv2);
		proportionNiv3Temps.add((double) proportionNiv3);
		revenuMinMoyenTemps.add((double) revenuMinMoyen);
		
		//Statistiques Gui
		statGui.updateData(toursOut, toursOutLim, individus, tauxChomage, employes, rechercheEmplois,
				nombreEmploisNonPourvus, nombreEmploisEnvoyes,
				individusQualif1, individusQualif2, individusQualif3,
				refusParTourPersonnes, demissionParTourPersonnes,
				revenuMinMoyen, tempsLibreMinMoyen, revenuMoyenMoyen, tempsLibreMoyenMoyen);
/*		
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
		
		System.out.println(revenuMinMoyen + " revenu Min Moyen");	
		System.out.println(tempsLibreMinMoyen + " temps Libre Min Moyen");	
		System.out.println(revenuMoyenMoyen + " revenu Moyen Moyen");	
		System.out.println(tempsLibreMoyenMoyen + " temps libre Moyen Moyen");			*/
		
		
		
		//int individusNivQualif1 = Util.searchDF(this,"nivQualif1").length;
		//int individusNivQualif2 = Util.searchDF(this,"nivQualif2").length;
		//int individusNivQualif3 = Util.searchDF(this,"nivQualif3").length;		
		//System.out.println(individusNivQualif1 + " individus de niveau 1 au chômage.");	
		//System.out.println(individusNivQualif2 + " individus de niveau 2 au chômage.");	
		//System.out.println(individusNivQualif3 + " individus de niveau 3 au chômage.");	
		
		//System.out.println(nombreReferencesEmplois + " nombreReferencesEmplois dans le système");
	}
	
	public int average(Collection<Integer> col){
		int sum = 0;
		int number = 0;
		
		for (Integer i : col){
			sum += i;
			number++;
		}
		
		if (number != 0) return sum / number;
		else return 0;
	}
}
