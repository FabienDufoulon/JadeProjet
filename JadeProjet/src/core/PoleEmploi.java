package core;

import jade.core.Agent;

import java.util.HashMap;

import jade.core.AID;

public class PoleEmploi extends Agent {
	/** Statut des employes. Surtout utile à des fins statistiques. */
	private enum StatutEmploye {Actif, Chomage};
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
	}
	
	//Agent clean-up
	protected void takeDown(){
		//Dismissal message
		System.out.println("PoleEmploi-agent " + getAID().getName() + " terminating.");
	}
}
