package core;

import java.io.Serializable;
import java.util.function.IntSupplier;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/** Simulateur qui crée les agents et gèrent les sorties et entrées des agents.
 * Un tour est équivalent à un mois.
 * Chaque année, des individus sortent de la simulation, et des individus rentrent dans la simulation.
 * 
 * */
public class Simulateur {
	/** Nombre d'individus au début de la simulation. */
	private int individusDebut;
	/** Nombre d'individus entrants dans la simulation chaque année. */
	private int individusEntrants;
	/** Nombre d'individus sortants de la simulation chaque année. */
	private int individusSortants;
	/** Temps que prend un tour dans la simulation */
	private int tempsTour;
	Object[] parametresHorloge;
	
	//Paramètres individus
	/** Temps Libre Minimum Moyen des individus */
	private int tempsLibreMoyen;
	/** Revenu Minimum Moyen des individus */
	private int revenuMoyen;
	/** Paramètres individus */
	private int x;
	private int y;
	private int z;
	/** Niveau de qualification moyen des individus */
	private int niveauQualif;
	Object[] parametresIndividu;
	
	//Paramètres Etat
	/** Nombre d'emplois par niveau de qualif */
	int[] emploisParQualif;
	/** Revenu des emplois de Etat par niveau de qualif */
	int[] revenusParQualif;
	/** Temps libre moyen des emplois de Etat par niveau de qualif */
	int[] tempsLibreParQualif;
	/** Arguments à donner à l'agent Etat */
	Object[] parametresEtat;
	
	public Simulateur(){
		//Paramètres simulateur
		individusDebut = 50;
		individusEntrants = 5;
		individusSortants = 5;
		tempsTour = 5000;
		parametresHorloge = new Object[5];
		parametresHorloge[0] = tempsTour;
		parametresHorloge[1] = individusEntrants;
		parametresHorloge[2] = individusSortants;
		parametresHorloge[3] = 1 + individusDebut;
		
		//Individus
		tempsLibreMoyen = 5;
		revenuMoyen = 6;
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
		emploisParQualif = new int[]{20,30,30};
		revenusParQualif = new int[]{4,6,8};
		tempsLibreParQualif = new int[]{5,6,4};
		
		parametresEtat = new Object[9];
		for (int i = 0; i < 3; i++){
			parametresEtat[i] = emploisParQualif[i];
			parametresEtat[3+i] = revenusParQualif[i];
			parametresEtat[6+i] = tempsLibreParQualif[i];
		}
	}
	
	/** Méthode à appeller une seule fois par test. Crée tous les agents initiaux, le reste est 
	 *  géré par Horloge. */
	public void commenceSimulation() throws StaleProxyException{
		/*Creation du Runtime*/
		Runtime rt = Runtime.instance();
		rt.setCloseVM(true);
		
		/*Lancement de la plate-forme*/
		Profile pMain = new ProfileImpl("localhost", 8888, null);
		// La taille des search du DF étant limité à 100 sinon
		String property_dx_maxresult = "10000";
		pMain.setParameter("jade_domain_df_maxresult", property_dx_maxresult); 
		//
		AgentContainer mc = rt.createMainContainer(pMain);
		
		IntSupplier _tempsLibre = () -> UtilRandom.discreteNextGaussian(tempsLibreMoyen, tempsLibreMoyen/3, 1, tempsLibreMoyen*2);
		IntSupplier _revenuMoyen = () -> UtilRandom.discreteNextGaussian(revenuMoyen, revenuMoyen/3, 1, revenuMoyen*2);
		IntSupplier _nivQualif = () -> UtilRandom.discreteNextGaussian(niveauQualif, 1, 1, 3);		
		
		/*Lancement des agents */
		for (int i = 1; i <= individusDebut; i++){
			//Faire les lois normales ici
			//IntSupplier _tempsLibre = (IntSupplier & Serializable)() -> tempsLibreMoyen ;
			//Create Util for random generation?
			//donner moyenne, std_var et limite min, max puis discrétiser.
			
			parametresIndividu[3] = _nivQualif.getAsInt();
			parametresIndividu[4] = _tempsLibre.getAsInt();
			parametresIndividu[5] = _revenuMoyen.getAsInt();
			
			mc.createNewAgent("Individu" + i, Individu.class.getName(), parametresIndividu).start();
		}
		
		parametresHorloge[4] = parametresIndividu;
		
		mc.createNewAgent("Etat", Etat.class.getName(), parametresEtat).start();
		mc.createNewAgent("PoleEmploi", PoleEmploi.class.getName(), null).start();
		AgentController test = mc.createNewAgent("Horloge", Horloge.class.getName(), parametresHorloge);


		test.start();
		/*Ces deux dernières méthodes peuvent lancer l'exception*/
	}

}
