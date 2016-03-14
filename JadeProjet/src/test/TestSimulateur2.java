package test;
import core.Simulateur;
import jade.wrapper.StaleProxyException;

/** Classe test simple. */
public class TestSimulateur2 {
	public static void main(String[] args) throws StaleProxyException {
		Simulateur simu = new Simulateur();
		simu.commenceSimulationAvecEntreprises();
	}
}
