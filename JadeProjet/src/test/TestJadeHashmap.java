package test;
import jade.core.Runtime;
import core.Individu;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class TestJadeHashmap {
	public static void main(String[] args) throws StaleProxyException {
		/*Creation du Runtime*/
		Runtime rt = Runtime.instance();
		rt.setCloseVM(true);
		
		/*Lancement de la plate-forme*/
		Profile pMain = new ProfileImpl("localhost", 8888, null);
		AgentContainer mc = rt.createMainContainer(pMain);
		
		/*Lancement d'un agent*/
		Object[] arguments = {"Silmarillion"};
		mc.createNewAgent("Buyer1", Individu.class.getName(), arguments).start();
		AgentController test = mc.createNewAgent("Buyer2", Individu.class.getName(), arguments);
		test.start();
		/*Ces deux dernières méthodes peuvent lancer l'exception*/
	}
}
