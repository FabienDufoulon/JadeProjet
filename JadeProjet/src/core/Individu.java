package core;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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
 * Prend en entrée six entiers.
 * Ils sont à entrer dans le même ordre que les déclarations ci-dessous.
 * C'est-à-dire x, y, z, nivQualif, tempsLibreMin, revenuMin .
 *
 */
public class Individu extends Agent {
	/** Statut des individus. Permet d'avoir un comportement à état. */
	private enum StatutIndividu {Employe, Chomage};
	
	//Paramètres donnés en entrée à la création de l'agent.
	/** Nombre de mois avec temps libre insuffisant avant qu'il démissionne. */
	private int x;
	/** Nombre d'offres consécutives en dessous de son revenu minimum qu'il peut refuser. */
	private int y;
	/** Diminution du revenu minimum qu'il peut accepter après y refus. */
	private int z;
	/** Niveau de Qualification de l'individu (entre 1 et 3)*/
	private int nivQualif;
	/** Temps libre minimum personnel */
	private int tempsLibreMin;
	/** Revenu minimum personnel */
	private int revenuMin;
	
	/** Compte le nombre d'offres consécutives en dessous du revenu minimum personnel*/
	int compteOffresConsecutives;
	/** Compte le nombre de mois avec temps libre insuffisant consécutifs. */
	int compteMoisTLInsuffisant;
	
	/** Etat(actif ou chomage) */
	StatutIndividu statut;
	/** Instance d'emploi pour obtenir le revenu et le temps libre à chaque tour. */
	Emploi emploiCourant;
	
	//Agent init
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! Individu-agent"+ getAID().getName()+ " is ready.");
		
		compteOffresConsecutives = 0;
		compteMoisTLInsuffisant = 0;
	   	
		//Get title of book to buy as start-up argument
		Object[] args = getArguments();
		if(args != null && args.length >= 6){
			x = (int) args[0];
			y = (int) args[1];
			z = (int) args[2];
			//Faire le tirage probabiliste ici ou dans le simulateur?
			//Pour l'instant, c'est fait comme si le tirage était dans le simulateur.
			nivQualif = (int) args[3];
			tempsLibreMin = (int) args[4];
			revenuMin = (int) args[5];
			
			//Register Service : service depends on niveauQualif
			ServiceDescription sd  = new ServiceDescription();
	        sd.setType( "nivQualif" + nivQualif );
	        sd.setName( getLocalName() );
	        Util.register( this,sd );
			
			
			//Ajout des comportements.
			addBehaviour(new AttenteMessage());
			
		}
		else{
			//Kill agent if he does not receive enough arguments
			System.out.println("Not enough input args");
			doDelete(); 
		}
		
		emploiCourant = null;
		statut = StatutIndividu.Chomage;
	}
	
	//Agent clean-up
	protected void takeDown(){
		//Deregister from DF
		try { DFService.deregister(this); }
        catch (Exception e) {}
		
		//Dismissal message
		System.out.println("Individu-agent " + getAID().getName() + " terminating.");
	}
	
	private void retire(){
		faireDemission();
		takeDown();
		//Demission de l'emploi.
	}
	
	private void faireDemission(){
		if (emploiCourant != null){
			AID employeur = emploiCourant.getEmployeur();
			
			//Créer message
			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
			inform.addReceiver(employeur);
			inform.setContent("Demission:" + emploiCourant.getRefEmploi());
			send(inform);
			
			//Changer ses infos
			emploiCourant = null;
			statut = StatutIndividu.Chomage;
			
			//DF : enregistrer avec le niveau de qualification
			ServiceDescription sd  = new ServiceDescription();
	        sd.setType( "nivQualif" + nivQualif );
	        sd.setName( getLocalName() );
	        Util.register( this,sd );
		}
	}
	
	private class AttenteMessage extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				
				String content = msg.getContent();
				if (content.equals("Turn")){
					if (statut == StatutIndividu.Employe){
						addBehaviour(new VieActive());
					}
				}
				
				else if (content.equals("Retraite")){
					//Gerer deregister registre mais aussi demission de emploi.
					retire();
				}
				
				else if (msg.getConversationId().equals("PropositionEmploi")){
					if (statut == StatutIndividu.Chomage){
						
					}
					else{
						//renvoyer un refus (histoire de ne pas deadlocker le message
					}
				}
			}
			else {
				block();
			}
		}
	}
	
	private class VieActive extends OneShotBehaviour {

		public void action() {
			if (emploiCourant == null) System.out.println("Gros problème ! Employé sans emploi correct.");
			else{
				int tempsLibreTour = emploiCourant.getTempsLibre();
				if (tempsLibreTour >= tempsLibreMin) compteMoisTLInsuffisant = 0;
				else{
					compteMoisTLInsuffisant++;
					if (compteMoisTLInsuffisant > x){
						faireDemission();
					}
				}
			}
			
		}
		
	}
}
