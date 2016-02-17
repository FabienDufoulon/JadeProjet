package core;

import java.io.Serializable;
import java.util.function.IntSupplier;

import jade.core.AID;

public class Emploi implements Serializable{
	/** AID de l'employeur, pour pouvoir communiquer avec lui */
	private AID employeur;
	/** AID de l'employe, pour pouvoir communiquer avec lui */
	private AID employe;
	/** Niveau de qualification nécessaire pour obtenir cet emploi */
	private int niveauQualifNecessaire;
	/** Prend une expression lambda dans le constructeur. Permet de personnaliser comment on obtient le temps libre pour chaque emploi. */
	private IntSupplier tempsLibre;
	/** Prend une expression lambda dans le constructeur. Permet de personnaliser comment on obtient le revenu pour chaque emploi. */
	private IntSupplier revenu;
	
	/** Référence de l'emploi */
	private int refEmploi;
	
	public Emploi(AID _employeur, int _niveauQualifNecessaire, IntSupplier _tempsLibre, IntSupplier _revenu, int _refEmploi){
		employeur = _employeur;
		niveauQualifNecessaire = _niveauQualifNecessaire;
		tempsLibre = _tempsLibre;
		revenu = _revenu;
		employe = null;
		refEmploi = _refEmploi;
	}
	
	public void setEmploye(AID _employe){
		employe = _employe;
	}
	
	public AID getEmploye(){
		return employe;
	}
	
	public AID getEmployeur(){
		return employeur;
	}
	
	public int getNiveauQualificationNecessaire(){
		return niveauQualifNecessaire;
	}
	
	public int getTempsLibre(){
		return tempsLibre.getAsInt();
	}
	
	public int getRevenu(){
		return revenu.getAsInt();
	}
	
	public int getRefEmploi(){
		return refEmploi;
	}
	

}
