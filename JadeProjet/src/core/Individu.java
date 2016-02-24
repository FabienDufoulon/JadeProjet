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
import jade.lang.acl.UnreadableException;
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
	private enum StatutIndividu {Employe, Chomage};
	
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
	
	/** Compte le nombre d'offres cons�cutives en dessous du revenu minimum personnel*/
	int compteOffresConsecutives;
	/** Compte le nombre de mois avec temps libre insuffisant cons�cutifs. */
	int compteMoisTLInsuffisant;
	
	/** Etat(actif ou chomage) */
	StatutIndividu statut;
	/** Instance d'emploi pour obtenir le revenu et le temps libre � chaque tour. */
	Emploi emploiCourant;
	
	/** Agent init */
	protected void setup() {
		// Printout a welcome message
		//System.out.println("Hello! Individu-agent"+ getAID().getName()+ " is ready.");
		
		compteOffresConsecutives = 0;
		compteMoisTLInsuffisant = 0;
	   	
		//Get title of book to buy as start-up argument
		Object[] args = getArguments();
		if(args != null && args.length >= 6){
			x = (int) args[0];
			y = (int) args[1];
			z = (int) args[2];
			//Faire le tirage probabiliste ici ou dans le simulateur?
			//Pour l'instant, c'est fait comme si le tirage �tait dans le simulateur.
			nivQualif = (int) args[3];
			tempsLibreMin = (int) args[4];
			revenuMin = (int) args[5];
			
			//Register Service : service depends on niveauQualif
			ServiceDescription sd  = new ServiceDescription();
	        sd.setType( "nivQualif" + nivQualif );
	        sd.setName( getLocalName() );
	        Util.register( this,sd );
	        
			//Cr�er message Inscription
			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
			inform.addReceiver(new AID("PoleEmploi", AID.ISLOCALNAME));
			inform.setContent("Inscription:"+nivQualif);
			send(inform);
			
			
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
	
	/** Agent clean-up */
	protected void takeDown(){
		//Deregister from DF
		try { DFService.deregister(this); }
        catch (Exception e) {System.out.println("Exception Takedown");}
		
		//System.out.println("Takedown Done");
		//Dismissal message
		//System.out.println("Individu-agent " + getAID().getName() + " terminating.");
	}
	
	/** Fonction qui enl�ve proprement un agent. D�mission pour sa liaison avec les autres objets,
	 *  et envoie d'un message � PoleEmploi, Takedown pour lui-m�me.
	 *  */
	private void retire(){
		//System.out.println("Retire");
		//Demission de l'emploi.
		faireDemission();
		
		//Envoyer message retraite � Pole Emploi
		ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
		inform.addReceiver(new AID("PoleEmploi", AID.ISLOCALNAME));
		inform.setContent("Retraite");
		send(inform);
		
		//System.out.println("Before Takedown");
		takeDown();
	}
	
	/** Fonction pour faire tous les d�tails li�s � la d�mission. */
	private void faireDemission(){
		if (emploiCourant != null){
			AID employeur = emploiCourant.getEmployeur();
			
			//Cr�er message Demission pour Employeur et PoleEmploi
			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
			inform.addReceiver(employeur);
			inform.addReceiver(new AID("PoleEmploi", AID.ISLOCALNAME));
			inform.setContent("Demission:" + emploiCourant.getRefEmploi());
			send(inform);
			
			//Changer ses infos
			emploiCourant = null;
			statut = StatutIndividu.Chomage;
			
			//Deregister from DF
			try { DFService.deregister(this); }
	        catch (Exception e) {System.out.println("Exception d�mission");}
			
			//DF : enregistrer avec le niveau de qualification
			ServiceDescription sd  = new ServiceDescription();
	        sd.setType( "nivQualif" + nivQualif );
	        sd.setName( getLocalName() );
	        Util.register( this,sd );
			
		}
	}
	
	/** Comportement qui consiste � la lecture de message de performative INFORM.
	 *  Re�oit entre autre les messages horloge qui l'informe du d�but du tour. */
	private class AttenteMessage extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				
				//On v�rifie si le message n'est pas une proposition d'emploi d'abord
				//Dans ce cas le contenu est un objet.
				if (msg.getConversationId() != null && msg.getConversationId().equals("ProposeEmploi")){
					//System.out.println("RecoitEmploi");
					if (statut == StatutIndividu.Chomage){
						etudierEmploi(msg);
					}
					else{
						//renvoyer un refus (histoire de ne pas deadlocker le message
						envoyerRefusEmploi();
					}
				}
				
				else{
					//Individu apprend que c'est le d�but du tour
					String content = msg.getContent();
					if (content.equals("Turn")){
						if (statut == StatutIndividu.Employe){
							addBehaviour(new VieEmploye());
						}
						
						//Envoyer message temps libre min, revenu min : informationTour
						ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
						inform.addReceiver(new AID("PoleEmploi", AID.ISLOCALNAME));
						inform.setContent("InformationTour:"+tempsLibreMin+":"+revenuMin);
						send(inform);
					}
					
					//L'individu part � la retraite
					else if (content.equals("Retraite")){
						//Gerer deregister registre mais aussi demission de emploi.
						retire();
					}
				}
			}
			else {
				block();
			}
		}
	}
	
	/** Fonction qui g�re le cas quand l'individu re�oit un message de proposition d'emploi,
	 *  il v�rifie alors si cet emploi le satisfait, et si oui r�pond � PoleEmploi, se met � jour. */
	private void etudierEmploi(ACLMessage msg){
		//System.out.println("etudierEmploi");
		//Lecture de l'instance emploi dans le message.
		Emploi content = null;
		try {
			content = (Emploi) msg.getContentObject();
		} catch (UnreadableException e) {
			e.printStackTrace();
		}
		
		if (content != null){
			//Si le revenu n'est pas suffisant.
			if (revenuMin > content.getRevenu()){
				if (compteOffresConsecutives > y){
					compteOffresConsecutives = 0;
					if (revenuMin - z < 0 ) revenuMin = 0;
					else revenuMin -= z;
				}
				else{
					compteOffresConsecutives++;
				}
				envoyerRefusEmploi();
			}

			//Le revenu est suffisant et on accepte l'emploi.
			else{
				//Cr�er m�me message pour PoleEmploi et Employeur
				ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
				inform.addReceiver(content.getEmployeur());
				inform.addReceiver(new AID("PoleEmploi", AID.ISLOCALNAME));
				inform.setContent("EmploiAccepte:" + content.getRefEmploi());
				send(inform);
				
				//Deregister from DF
				try { DFService.deregister(this); }
		        catch (Exception e) {}
				
				//Register to DF : employe
				ServiceDescription sd  = new ServiceDescription();
		        sd.setType("employe");
		        sd.setName( getLocalName() );
		        Util.register( this,sd );
		        
		        //Se mettre � jour
		        statut = StatutIndividu.Employe;
		        emploiCourant = content;
			}
		}

		
	}
	
	/** Fonction pour envoyer un refus � la proposition d'emploi de Pole Emploi. */
	private void envoyerRefusEmploi(){
		//Cr�er message
		ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
		inform.addReceiver(new AID("PoleEmploi", AID.ISLOCALNAME));
		inform.setContent("EmploiRefuse");
		send(inform);
	}
	
	/** Comportement appel� quand l'individu est employe, il regarde s'il dispose d'assez de temps libre, sinon d�mission. */
	private class VieEmploye extends OneShotBehaviour {

		public void action() {
			if (emploiCourant == null) System.out.println("Gros probl�me ! Employ� sans emploi correct.");
			else{
				int tempsLibreTour = emploiCourant.getTempsLibre();
				int revenuTour = emploiCourant.getRevenu();
				//Envoyer message temps libre tour, revenu tour : informationEmploye
				ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
				inform.addReceiver(new AID("PoleEmploi", AID.ISLOCALNAME));
				inform.setContent("InformationEmploye:"+tempsLibreTour+":"+revenuTour);
				send(inform);
				
				if (tempsLibreTour >= tempsLibreMin) compteMoisTLInsuffisant = 0;
				else{
					compteMoisTLInsuffisant++;
					if (compteMoisTLInsuffisant > x){
						faireDemission();
						compteMoisTLInsuffisant = 0;
					}
				}
			}
			
		}
		
	}
}
