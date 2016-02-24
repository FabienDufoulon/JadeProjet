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
 * Prend en entr�e neuf entiers.
 * Les trois premiers correspondent au nombre d'emplois par niveau de qualification.
 * Les trois suivants correspondent au revenu par niveau de qualification.
 * Les trois derniers correspondent au temps libre moyen par niveau de qualification.
 *
 */
public class Etat extends Agent {
	//Param�tres donn�s en entr�e � la cr�ation de l'agent.
	/** Liste des emplois libres � envoyer � PoleEmploi. */
	private ArrayList<Emploi> emploisLibres;
	/** Nombre d'offres cons�cutives en dessous de son revenu minimum qu'il peut refuser. */
	private HashMap<Integer, Emploi> emplois;
	/** Sert � s'assurer que l'on cr�� des emplois avec des r�f�rences distinctes. */
	int derniereReferenceEmploi;

	
	/** Agent init */
	protected void setup() {
		// Welcome message
		System.out.println("Hello! Etat-agent"+ getAID().getName()+ " is ready.");
		
		derniereReferenceEmploi = 0;
		emplois = new HashMap<Integer, Emploi>();
		emploisLibres = new ArrayList<Emploi>();
	   	
		//Cr�e les emplois selon les argument donn�s.
		Object[] args = getArguments();
		if(args != null && args.length >= 9){
			for (int nivQualif = 1; nivQualif <= 3; nivQualif++){
				//Param�tres pour chaque niveau de qualification
				int nombreEmplois = (int) args[nivQualif-1];
				int revenu = (int) args[3 + nivQualif-1];
				int tempsLibreMoyen = (int) args[6 + nivQualif-1];
				
				for(int i = 0; i < nombreEmplois; i++){
					IntSupplier _revenu = (IntSupplier & Serializable)() -> revenu ;
					//Faire une loi normale ici pour le temps libre
					IntSupplier _tempsLibre = (IntSupplier & Serializable)() 
								-> UtilRandom.discreteNextGaussian(tempsLibreMoyen, tempsLibreMoyen/3, 1, tempsLibreMoyen*2);
					
					Emploi temp = new Emploi(getAID(), nivQualif, _tempsLibre, _revenu, derniereReferenceEmploi);
					emplois.put(derniereReferenceEmploi, temp);
					emploisLibres.add(temp);
					derniereReferenceEmploi++;
				}
			}
			
			
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
	
	/** G�re la transmission des emplois libres(non donn�s � PoleEmploi) � PoleEmploi*/
	private class PublierEmplois extends OneShotBehaviour {
		public void action() {
			if(emploisLibres.size() == 0) return;
			//Sending message
			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
			inform.addReceiver(new AID("PoleEmploi", AID.ISLOCALNAME));
			for(int i = 0; i < emploisLibres.size(); i++){
				inform.setConversationId("PublierEmplois");
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
	
	/** Appel� quand AttenteMessage obtient un message d'acceptation d'un emploi.
	 *  Utilise la r�f�rence contenue dans le message pour mettre � jour 
	 *  l'individu dans l'emploi correspondant. */
	private void TraiteReponseEmploi(ACLMessage rempli) {
		//System.out.println("Reception r�ponse emploi");
		String [] content = rempli.getContent().split(":");
		emplois.get(Integer.parseInt(content[1])).setEmploye(rempli.getSender());
	}
	
	/** Appel� quand AttenteMessage obtient un message de d�mission d'un emploi.
	 *  Utilise la r�f�rence contenue dans le message pour mettre � null 
	 *  l'individu dans l'emploi correspondant et publie "directement" l'emploi lib�r�. */
	private void TraiteReponseDemission(ACLMessage demission) {
		//System.out.println("Reception D�mission");
		String [] content = demission.getContent().split(":");
		Emploi emploiDemission = emplois.get(Integer.parseInt(content[1]));
		emploiDemission.setEmploye(null);
		emploisLibres.add(emploiDemission);
		
		addBehaviour( new PublierEmplois());
	}
}
