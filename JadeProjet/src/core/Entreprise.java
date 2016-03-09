package core;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.IntSupplier;

/**
 * 
 * Prend en entrée 14 entiers.
 * Les trois premiers correspondent à la production individuelle par niveau de qualification.
 * Les trois suivants correspondent au revenu par niveau de qualification.
 * Les trois suivants correspondent au temps libre moyen par niveau de qualification.
 * Les deux suivants correspondent au seuil d'emplois requis du niveau 1 et du niveau 2.
 * Les trois derniers correspondent à la demande moyen, au nombre de mois successifs et à la durée limitée pour chaque offre d'emploi
 *
 */
public class Entreprise extends Agent {
	//Paramètres donnés en entrée à la création de l'agent.
	/** Liste des emplois libres à envoyer à PoleEmploi. */
	private ArrayList<Emploi> emploisLibres;
	/** < Références distinctes, Emploi > */
//	private HashMap<Integer, Emploi> emplois;
	
	/** < Références distinctes, Emploi > */
	private HashMap<Integer, Emploi> emploisCDI;
	private HashMap<Integer, Emploi> emploisCDD;
	private HashMap<Integer, Integer> emploisCDDTempsDepuisAccepte;
	
	/** Sert à s'assurer que l'on créé des emplois avec des références distinctes. */
	int derniereReferenceEmploi;
	
	int[] nombreEmploisSelonQualif;
	int[] nombreEmploisSelonQualifCDI;
	int[] nombreEmploisSelonQualifCDD;
	int[] prodParQualif;
	int demandeMoyen;
	int seuil1;
	int seuil2;
	int k;
	int dureeOffreEmploi;
	

	
	/** Agent init */
	protected void setup() {
		
		derniereReferenceEmploi = 0;
//		emplois = new HashMap<Integer, Emploi>();
		emploisCDI = new HashMap<Integer, Emploi>();
		emploisCDD = new HashMap<Integer, Emploi>();
		emploisCDDTempsDepuisAccepte = new HashMap<Integer, Integer>();
		emploisLibres = new ArrayList<Emploi>();
		nombreEmploisSelonQualif = new int[]{0,0,0};
		nombreEmploisSelonQualifCDI = new int[]{0,0,0};
		nombreEmploisSelonQualifCDD = new int[]{0,0,0};
	   	
		//Crée les emplois selon les argument donnés.
		Object[] args = getArguments();
		if(args != null && args.length >= 14){
			for (int nivQualif = 1; nivQualif <= 3; nivQualif++){
				prodParQualif[nivQualif-1] = (int) args[nivQualif-1];
			}
			seuil1 = (int)args[9];
			seuil2 = (int)args[10];
			demandeMoyen = (int)args[11];
			k = (int)args[12];
			dureeOffreEmploi = (int)args[13];
			
			IntSupplier _demande = (IntSupplier & Serializable)() 
					-> UtilRandom.discreteNextGaussian(demandeMoyen, demandeMoyen/3, 1, demandeMoyen*2);
			
			// Optimisation
					
			// creation emplois
					

			
			//Ajout des comportements
			addBehaviour( new AttenteMessage());
			addBehaviour( new PublierEmplois());
		}
		else{
			//Kill agent if he does not receive enough arguments
			System.out.println("Not enough input args");
			doDelete(); 
		}
	}
	
	/** Agent clean-up */
	protected void takeDown(){
		//Dismissal message
		System.out.println("Etat-agent " + getAID().getName() + " terminating.");
	}
	
	/** Comportement pour lire les messages de performatif INFORM. */
	private class AttenteMessage extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				
				String content = msg.getContent();
				if (content.equals("Turn")){
					//System.out.println("Etat starting turn");
				}
				
				else if (content.startsWith("Demission:")){
					TraiteReponseDemission(msg);
				}
				
				else if (content.startsWith("EmploiAccepte:")){
					TraiteReponseEmploi(msg);
				}
			}
			else {
				block();
			}
		}
	}
	
	/** Gère la transmission des emplois libres(non donnés à PoleEmploi) à PoleEmploi*/
	private class PublierEmplois extends OneShotBehaviour {
		public void action() {
			if(emploisLibres.size() == 0) return;
			//Sending message
			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
			inform.addReceiver(new AID("PoleEmploi", AID.ISLOCALNAME));
			for(int i = 0; i < emploisLibres.size(); i++){
				inform.setConversationId("PublierEmploisEntreprise:" + dureeOffreEmploi);
				try {
					inform.setContentObject(emploisLibres.get(i));
				} catch (IOException e) {
					e.printStackTrace();
				}
				myAgent.send(inform);
			}
			emploisLibres.clear();
		}
	}
	
	/** Appelé quand AttenteMessage obtient un message d'acceptation d'un emploi.
	 *  Utilise la référence contenue dans le message pour mettre à jour 
	 *  l'individu dans l'emploi correspondant. */
	private void TraiteReponseEmploi(ACLMessage rempli) {
		//System.out.println("Reception réponse emploi");
		String [] content = rempli.getContent().split(":");
		emploisCDD.get(Integer.parseInt(content[1])).setEmploye(rempli.getSender());
	}
	
	/** Appelé quand AttenteMessage obtient un message de démission d'un emploi.
	 *  Utilise la référence contenue dans le message pour mettre à null 
	 *  l'individu dans l'emploi correspondant et publie "directement" l'emploi libéré. */
	private void TraiteReponseDemission(ACLMessage demission) {
		//System.out.println("Reception Démission");
		String [] content = demission.getContent().split(":");
		Emploi emploiDemission = emplois.get(Integer.parseInt(content[1]));
		emploiDemission.setEmploye(null);
		emploisLibres.add(emploiDemission);
		
		addBehaviour( new PublierEmplois());
	}
	
	private int[] optimisationDemandeProduction(int[] nombreEmploisSelonQualifCDD, int[] nombreEmploisSelonQualifCDI, demande) {
		int production = prod1*nombreEmploisSelonQualif[0] + prod2*nombreEmploisSelonQualif[1] + prod3*nombreEmploisSelonQualif[2];
		
		int[] nombreEmploisSelonQualif;
		
		if (production < demande) ajouterIndividus(nombreEmploisSelonQualif)
		else licensierIndividus(nombreEmploisSelonQualifCDD, nombreEmploisSelonQualifCDI)
		
	}

	private int[] licensierIndividus(int[] nombreEmploisSelonQualifCDD,int[] nombreEmploisSelonQualifCDI, demande, production) {
		nombre1CDD = nombreEmploisSelonQualifCDD[0];
		nombre2CDD = nombreEmploisSelonQualifCDD[1];
		nombre3CDD = nombreEmploisSelonQualifCDD[2];
		nombre1CDI = nombreEmploisSelonQualifCDI[0];
		nombre2CDI = nombreEmploisSelonQualifCDI[1];
		nombre3CDI = nombreEmploisSelonQualifCDI[2];
		
		nombre1 = nombre1CDD+nombre1CDI;
		nombre2 = nombre2CDD+nombre2CDI;
		nombre3 = nombre3CDD+nombre3CDI;
		
		while (production > demande){
			if (production - demande > prod3 && nombre3CDD > 0){
				nombre3CDD--;
			}
			else if (production - demande > prod2){
				if (nombre2 > nombre3*seuil2 && nombre2CDD > 0){
					nombre2CDD--;
				}
				else if (nombre1 > nombre2*seuil1 && nombre1CDD > 0) {
					nombre1CDD--;
				}
				else break;
			}
			else if (production - demande > prod1){
				if (nombre1 > nombre2*seuil1 && nombre1CDD > 0) {
					nombre1CDD--;
				}
			}
			else{
				break;
			}

			nombre1 = nombre1CDD+nombre1CDI;
			nombre2 = nombre2CDD+nombre2CDI;
			nombre3 = nombre3CDD+nombre3CDI;
			production = prod1*nombre1 + prod2*nombre2 + prod3*nombre3;
		}
		
		return int[]{nombreEmploisSelonQualifCDD[0]-nombre1CDD, nombreEmploisSelonQualifCDD[1]-nombre2CDD, nombreEmploisSelonQualifCDD[2]-nombre3CDD}}


	private int[] ajouterIndividus(int[] nombreEmploisSelonQualif, demande, production) {

		nombre1 = nombreEmploisSelonQualif[0];
		nombre2 = nombreEmploisSelonQualif[1];
		nombre3 = nombreEmploisSelonQualif[2];
		while (production < demande){
			if (demande - production > prod2){
				if ((nombre3+1)*seuil2 <= nombre2) {
					nombre3++;
				}
				else if ((nombre2+1)*seuil1 <= nombre1) {
					nombre2++;
				}
				else {
					nombre1++;
				}
			}
			else if (demande - production > prod1){
				if ((nombre2+1)*seuil1 <= nombre1) {
					nombre2++;
				}
				else {
					nombre1++;
				}
			}
			else{
				nombre1++;
			}

			production = prod1*nombre1 + prod2*nombre2 + prod3*nombre3;
		}
	}
}
