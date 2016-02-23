package core;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/** Simulateur qui cr�e les agents et g�rent les sorties et entr�es des agents.
 * Un tour est �quivalent � un mois.
 * Chaque ann�e, des individus sortent de la simulation, et des individus rentrent dans la simulation.
 * 
 * */
public class Simulateur {
	/** Nombre d'individus au d�but de la simulation. */
	private int individusDebut;
	/** Nombre d'individus entrants dans la simulation chaque ann�e. */
	private int individusEntrants;
	/** Nombre d'individus sortants de la simulation chaque ann�e. */
	private int individusSortants;
	/** Temps que prend un tour dans la simulation */
	private int tempsTour;
	Object[] parametresHorloge;
	
	//Param�tres individus
	/** Temps Libre Minimum Moyen des individus */
	private int tempsLibreMoyen;
	/** Revenu Minimum Moyen des individus */
	private int revenuMoyen;
	/** Param�tres individus */
	private int x;
	private int y;
	private int z;
	/** Niveau de qualification moyen des individus */
	private int niveauQualif;
	Object[] parametresIndividu;
	
	//Param�tres Etat
	/** Nombre d'emplois par niveau de qualif */
	int[] emploisParQualif;
	/** Revenu des emplois de Etat par niveau de qualif */
	int[] revenusParQualif;
	/** Temps libre moyen des emplois de Etat par niveau de qualif */
	int[] tempsLibreParQualif;
	/** Arguments � donner � l'agent Etat */
	Object[] parametresEtat;
	
	public Simulateur(){
		//Param�tres simulateur
		individusDebut = 5;
		individusEntrants = 1;
		individusSortants = 1;
		tempsTour = 5000;
		parametresHorloge = new Object[5];
		parametresHorloge[0] = tempsTour;
		parametresHorloge[1] = individusEntrants;
		parametresHorloge[2] = individusSortants;
		parametresHorloge[3] = 1 + individusDebut;
		
		//Individus
		tempsLibreMoyen = 5;
		revenuMoyen = 10;
		x = 2;
		y = 2;
		z = 2;
		niveauQualif = 2;
		
		parametresIndividu = new Object[6];
		parametresIndividu[0] = x;
		parametresIndividu[1] = y;
		parametresIndividu[2] = z;
		/*parametresIndividu[3] = niveauQualif;
		parametresIndividu[4] = tempsLibreMoyen;
		parametresIndividu[5] = revenuMoyen;*/
		
		
		//Etat
		emploisParQualif = new int[]{2,3,1};
		revenusParQualif = new int[]{20,20,20};
		tempsLibreParQualif = new int[]{5,6,6};
		
		parametresEtat = new Object[9];
		for (int i = 0; i < 3; i++){
			parametresEtat[i] = emploisParQualif[i];
			parametresEtat[3+i] = revenusParQualif[i];
			parametresEtat[6+i] = tempsLibreParQualif[i];
		}
	}
	
	/** M�thode � appeller une seule fois par test. Cr�e tous les agents initiaux, le reste est 
	 *  g�r� par Horloge. */
	public void commenceSimulation() throws StaleProxyException{
		/*Creation du Runtime*/
		Runtime rt = Runtime.instance();
		rt.setCloseVM(true);
		
		/*Lancement de la plate-forme*/
		Profile pMain = new ProfileImpl("localhost", 8888, null);
		AgentContainer mc = rt.createMainContainer(pMain);
		
		/*Lancement des agents */
		for (int i = 1; i <= individusDebut; i++){
			//Faire les lois normales ici
			parametresIndividu[3] = niveauQualif;
			parametresIndividu[4] = tempsLibreMoyen;
			parametresIndividu[5] = revenuMoyen;
			
			mc.createNewAgent("Individu" + i, Individu.class.getName(), parametresIndividu).start();
		}
		
		parametresHorloge[4] = parametresIndividu;
		
		mc.createNewAgent("Etat", Etat.class.getName(), parametresEtat).start();
		mc.createNewAgent("PoleEmploi", PoleEmploi.class.getName(), null).start();
		AgentController test = mc.createNewAgent("Horloge", Horloge.class.getName(), parametresHorloge);


		test.start();
		/*Ces deux derni�res m�thodes peuvent lancer l'exception*/
	}

}
