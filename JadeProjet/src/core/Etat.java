package core;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.IntSupplier;

/**
 * 
 * Prend en entrée neuf entiers.
 * Les trois premiers correspondent au nombre d'emplois par niveau de qualification.
 * Les trois suivants correspondent au revenu par niveau de qualification.
 * Les trois derniers correspondent au temps libre moyen par niveau de qualification.
 *
 */
public class Etat extends Agent {
	//Paramètres donnés en entrée à la création de l'agent.
	/** Liste des emplois libres à envoyer à PoleEmploi. */
	private ArrayList<Emploi> emploisLibres;
	/** Nombre d'offres consécutives en dessous de son revenu minimum qu'il peut refuser. */
	private HashMap<Integer, Emploi> emplois;
	/** Sert à s'assurer que l'on créé des emplois avec des références distinctes. */
	int derniereReferenceEmploi;

	
	//Agent init
	protected void setup() {
		// Welcome message
		System.out.println("Hello! Etat-agent"+ getAID().getName()+ " is ready.");
		
		derniereReferenceEmploi = 0;
		emplois = new HashMap<Integer, Emploi>();
		emploisLibres = new ArrayList<Emploi>();
	   	
		//Crée les emplois selon les argument donnés.
		Object[] args = getArguments();
		if(args != null && args.length >= 9){
			for (int nivQualif = 1; nivQualif <= 3; nivQualif++){
				//Paramètres pour chaque niveau de qualification
				int nombreEmplois = (int) args[nivQualif-1];
				int revenu = (int) args[3 + nivQualif-1];
				int tempsLibreMoyen = (int) args[6 + nivQualif-1];
				
				for(int i = 0; i < nombreEmplois; i++){
					IntSupplier _revenu = () -> revenu ;
					//Faire une loi normale ici pour le temps libre
					IntSupplier _tempsLibre = () -> tempsLibreMoyen ;
					
					Emploi temp = new Emploi(getAID(), nivQualif, _tempsLibre, _revenu, derniereReferenceEmploi);
					emplois.put(derniereReferenceEmploi, temp);
					emploisLibres.add(temp);
					derniereReferenceEmploi++;
				}
			}
			
			
			//Ajout des comportements
			addBehaviour( new AttenteHorloge());
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
		System.out.println("Etat-agent " + getAID().getName() + " terminating.");
	}
	
	private class AttenteHorloge extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String content = msg.getContent();
				if (content.equals("Turn")){
					System.out.println("Etat starting turn");
				}
				else if (content.equals("Year")){
					System.out.println("Etat Year");
				}
			}
			else {
				block();
			}
		}
	}
}
