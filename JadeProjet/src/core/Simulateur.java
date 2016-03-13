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
	/** Nombre d'entreprises au début de la simulation. */
	private int entreprisesDebut;
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
	
	//Paramètres Entreprise
	/** Production par individu par niveau de qualif */
	int[] productionIndividuParQualif;
	/** Demande moyen des entreprises */
	int demandeMoyen;
	/** Paramètre définissant le seuil d'emplois requis */
	int seuilNiv1EmploisRequis;
	int seuilNiv2EmploisRequis;
	/** Arguments à donner à l'agent Entreprise */
	Object[] parametresEntreprise;
	/** Un emploi en CDD plus de k mois successifs automatiquement transformé en CDI */
	int k;
	/** Durée limitée dans le temps pour chaque offre d'emploi */
	int dureeOffreEmploi;
	
	public Simulateur(){
		//Paramètres simulateur
		individusDebut = 50;
		individusEntrants = 0;
		individusSortants = 0;
		entreprisesDebut = 10;
		tempsTour = 250;
		parametresHorloge = new Object[8];
		parametresHorloge[0] = tempsTour;
		parametresHorloge[1] = individusEntrants;
		parametresHorloge[2] = individusSortants;
		parametresHorloge[3] = 1 + individusDebut;
		
		//Individus
		tempsLibreMoyen = 100;
		revenuMoyen = 2000;
		x = 3;
		y = 3;
		z = 100;
		niveauQualif = 2;
		
		parametresIndividu = new Object[6];
		parametresIndividu[0] = x;
		parametresIndividu[1] = y;
		parametresIndividu[2] = z;
		/*parametresIndividu[3] = niveauQualif;
		parametresIndividu[4] = tempsLibreMoyen;
		parametresIndividu[5] = revenuMoyen;*/
		
		
		//Etat
		emploisParQualif = new int[]{50,50,50};
		revenusParQualif = new int[]{2200,2200,2200};
		tempsLibreParQualif = new int[]{1500,1500,1500};
		
		parametresEtat = new Object[9];
		for (int i = 0; i < 3; i++){
			parametresEtat[i] = emploisParQualif[i];
			parametresEtat[3+i] = revenusParQualif[i];
			parametresEtat[6+i] = tempsLibreParQualif[i];
		}
		
		//Entreprise
		demandeMoyen = 80;
		productionIndividuParQualif = new int[]{1,2,3};
		seuilNiv1EmploisRequis = 10;
		seuilNiv2EmploisRequis = 5;
		k = 12;
		dureeOffreEmploi = 5;
		
		parametresEntreprise = new Object[14];
		for (int i = 0; i < 3; i++){
			parametresEntreprise[i] = productionIndividuParQualif[i];
			parametresEntreprise[3+i] = revenusParQualif[i];
			parametresEntreprise[6+i] = tempsLibreParQualif[i];
		}
		parametresEntreprise[9] = seuilNiv1EmploisRequis;
		parametresEntreprise[10] = seuilNiv2EmploisRequis;
		parametresEntreprise[11] = demandeMoyen;
		parametresEntreprise[12] = k;
		parametresEntreprise[13] = dureeOffreEmploi;
	}
	
	/** Méthode à appeller une seule fois par test. Crée tous les agents initiaux, le reste est 
	 *  géré par Horloge. */
	public AgentContainer commenceSimulation() throws StaleProxyException{
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
		IntSupplier _revenu = () -> UtilRandom.discreteNextGaussian(revenuMoyen, revenuMoyen/3, 1, revenuMoyen*2);
		IntSupplier _nivQualif = () -> UtilRandom.discreteNextGaussian(niveauQualif, 1, 1, 3);		
		
		//Ordre probablement important pour la création des agents
		mc.createNewAgent("PoleEmploi", PoleEmploi.class.getName(), null).start();
		
		
		mc.createNewAgent("Etat", Etat.class.getName(), parametresEtat).start();
		
		/* Lancement des agents */
		for (int i = 1; i <= individusDebut; i++){	
			parametresIndividu[3] = _nivQualif.getAsInt();
			parametresIndividu[4] = _tempsLibre.getAsInt();
			parametresIndividu[5] = _revenu.getAsInt();
			
			mc.createNewAgent("Individu" + i, Individu.class.getName(), parametresIndividu).start();
		}
		
		parametresHorloge[4] = parametresIndividu;
		parametresHorloge[5] = _tempsLibre;
		parametresHorloge[6] = _revenu;
		parametresHorloge[7] = _nivQualif;
		
		AgentController test = mc.createNewAgent("Horloge", Horloge.class.getName(), parametresHorloge);


		test.start();
		
		return mc;
	}
	
	/** Méthode à appeller une seule fois par test. Crée tous les agents initiaux, y compris des entreprises, le reste est 
	 *  géré par Horloge. */
	public void commenceSimulationAvecEntreprises() throws StaleProxyException{
		AgentContainer mc = commenceSimulation();
		
		/* Lancement des entreprises */
		for (int i = 1; i <= entreprisesDebut; i++){	
			mc.createNewAgent("Entreprise" + i, Entreprise.class.getName(), parametresEntreprise).start();
		}
	}

}
