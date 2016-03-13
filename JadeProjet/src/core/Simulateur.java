package core;

import java.io.Serializable;
import java.util.function.IntSupplier;

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
	/** Nombre d'entreprises au d�but de la simulation. */
	private int entreprisesDebut;
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
	
	//Param�tres Entreprise
	/** Production par individu par niveau de qualif */
	int[] productionIndividuParQualif;
	/** Demande moyen des entreprises */
	int demandeMoyen;
	/** Param�tre d�finissant le seuil d'emplois requis */
	int seuilNiv1EmploisRequis;
	int seuilNiv2EmploisRequis;
	/** Arguments � donner � l'agent Entreprise */
	Object[] parametresEntreprise;
	/** Un emploi en CDD plus de k mois successifs automatiquement transform� en CDI */
	int k;
	/** Dur�e limit�e dans le temps pour chaque offre d'emploi */
	int dureeOffreEmploi;
	
	public Simulateur(){
		//Param�tres simulateur
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
	
	/** M�thode � appeller une seule fois par test. Cr�e tous les agents initiaux, le reste est 
	 *  g�r� par Horloge. */
	public AgentContainer commenceSimulation() throws StaleProxyException{
		/*Creation du Runtime*/
		Runtime rt = Runtime.instance();
		rt.setCloseVM(true);
		
		/*Lancement de la plate-forme*/
		Profile pMain = new ProfileImpl("localhost", 8888, null);
		// La taille des search du DF �tant limit� � 100 sinon
		String property_dx_maxresult = "10000";
		pMain.setParameter("jade_domain_df_maxresult", property_dx_maxresult); 
		//
		AgentContainer mc = rt.createMainContainer(pMain);
		
		IntSupplier _tempsLibre = () -> UtilRandom.discreteNextGaussian(tempsLibreMoyen, tempsLibreMoyen/3, 1, tempsLibreMoyen*2);
		IntSupplier _revenu = () -> UtilRandom.discreteNextGaussian(revenuMoyen, revenuMoyen/3, 1, revenuMoyen*2);
		IntSupplier _nivQualif = () -> UtilRandom.discreteNextGaussian(niveauQualif, 1, 1, 3);		
		
		//Ordre probablement important pour la cr�ation des agents
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
	
	/** M�thode � appeller une seule fois par test. Cr�e tous les agents initiaux, y compris des entreprises, le reste est 
	 *  g�r� par Horloge. */
	public void commenceSimulationAvecEntreprises() throws StaleProxyException{
		AgentContainer mc = commenceSimulation();
		
		/* Lancement des entreprises */
		for (int i = 1; i <= entreprisesDebut; i++){	
			mc.createNewAgent("Entreprise" + i, Entreprise.class.getName(), parametresEntreprise).start();
		}
	}

}
