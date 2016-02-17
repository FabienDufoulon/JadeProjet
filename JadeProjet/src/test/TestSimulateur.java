package test;
import core.Simulateur;
import jade.wrapper.StaleProxyException;

/** Classe test simple. */
public class TestSimulateur {
	public static void main(String[] args) throws StaleProxyException {
		Simulateur simu = new Simulateur();
		simu.commenceSimulation();
	}
}
