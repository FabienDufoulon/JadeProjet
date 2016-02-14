package core;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

/**
 * 
 * Prend en entr�e six entiers.
 * Ils sont � entrer dans le m�me ordre que les d�clarations ci-dessous.
 * C'est-�-dire x, y, z, nivQualif, tempsLibreMin, revenuMin .
 *
 */
public class Individu extends Agent {
	/** Statut des individus. Permet d'avoir un comportement � �tat. */
	private enum StatutIndividu {Actif, Chomage};
	
	//Param�tres donn�s en entr�e � la cr�ation de l'agent.
	/** Nombre de mois avec temps libre insuffisant avant qu'il d�missionne. */
	private int x;
	/** Nombre d'offres cons�cutives en dessous de son revenu minimum qu'il peut refuser. */
	private int y;
	/** Diminution du revenu minimum qu'il peut accepter apr�s y refus. */
	private int z;
	/** Niveau de Qualification de l'individu (entre 1 et 3)*/
	private int nivQualif;
	/** Temps libre minimum personnel */
	private int tempsLibreMin;
	/** Revenu minimum personnel */
	private int revenuMin;
	
	/** Etat(actif ou chomage) */
	StatutIndividu statut;
	/** Instance d'emploi pour obtenir le revenu et le temps libre � chaque tour. */
	Emploi emploiCourant;
	
	//Agent init
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! Individu-agent"+ getAID().getName()+ " is ready.");
	   	
		//Get title of book to buy as start-up argument
		Object[] args = getArguments();
		if(args != null && args.length > 6){
			x = (int) args[0];
			y = (int) args[1];
			z = (int) args[2];
			//Faire le tirage probabiliste ici ou dans le simulateur?
			//Pour l'instant, c'est fait comme si le tirage �tait dans le simulateur.
			nivQualif = (int) args[3];
			tempsLibreMin = (int) args[4];
			revenuMin = (int) args[5];
			
			
			//Ajout des comportements.
			
		}
		else{
			//Kill agent if he does not receive enough arguments
			System.out.println("Not enough input args");
			doDelete(); 
		}
		
		Emploi emploiCourant = null;
		statut = StatutIndividu.Chomage;
	}
	
	//Agent clean-up
	protected void takeDown(){
		//Dismissal message
		System.out.println("Individu-agent " + getAID().getName() + " terminating.");
	}
}
