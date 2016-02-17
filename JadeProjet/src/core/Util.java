package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

/**
 * 
 * Heavily inspired by jade tutorial and 
 * http://www.iro.umontreal.ca/~vaucher/Agents/Jade/primer5.html
 * for improved register method and get services methods.
 */
public class Util {
	static Random rand = new Random(); 
	
	/** Improved register function. Allows to register if the agent forgot to deregister previously.*/
	static void register(Agent agent, ServiceDescription sd)
    {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());
        

        try {  
        	DFAgentDescription list[] = DFService.search(agent, dfd);
        	if (list.length > 0) DFService.deregister(agent);
        	
        	dfd.addServices(sd);
            DFService.register(agent, dfd );  
        }
        catch (FIPAException fe) { fe.printStackTrace(); }
    }
	
	/** Return only one AID found, corresponding to service*/
    static AID getFirstService(Agent agent, String service )
    {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType( service );
        dfd.addServices(sd);
        try
        {
            DFAgentDescription[] result = DFService.search(agent, dfd);
            if (result.length>0)
                return result[0].getName() ;
        }
        catch (FIPAException fe) { fe.printStackTrace(); }
        return null;
    }
    
	/** Return only one AID found randomly, corresponding to service*/
    static AID getRandomService(Agent agent, String service )
    {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType( service );
        dfd.addServices(sd);
        try
        {
            DFAgentDescription[] result = DFService.search(agent, dfd);
            if (result.length>0)
                return result[rand.nextInt(result.length)].getName() ;
        }
        catch (FIPAException fe) { fe.printStackTrace(); }
        return null;
    }

    /** Return all AID found corresponding to service*/
    static AID [] searchDF(Agent agent, String service )
    {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType( service );
        dfd.addServices(sd);
        
        SearchConstraints ALL = new SearchConstraints();
        ALL.setMaxResults(new Long(-1));

        try
        {
            DFAgentDescription[] result = DFService.search(agent, dfd, ALL);
            AID[] agents = new AID[result.length];
            for (int i=0; i<result.length; i++) 
                agents[i] = result[i].getName() ;
            return agents;

        }
        catch (FIPAException fe) { fe.printStackTrace(); }
        
        return null;
    }
    
    /** Return all individu instances registered on the DF*/
    static AID[] getAllIndividus(Agent agent){
		AID[] individusEmployes = Util.searchDF(agent,"employe");
		AID[] individusNiv1 = Util.searchDF(agent,"nivQualif1");
    	AID[] individusNiv2 = Util.searchDF(agent,"nivQualif2");
		AID[] individusNiv3 = Util.searchDF(agent,"nivQualif3");
		
		ArrayList<AID> allIndividusList = new ArrayList<AID>();
		allIndividusList.addAll(Arrays.asList(individusEmployes));
		allIndividusList.addAll(Arrays.asList(individusNiv1));
		allIndividusList.addAll(Arrays.asList(individusNiv2));
		allIndividusList.addAll(Arrays.asList(individusNiv3));
		AID[] allIndividus = allIndividusList.toArray(new AID[
		                             individusEmployes.length + individusNiv1.length +
		                             individusNiv2.length + individusNiv3.length]);
		
		return allIndividus;
    }
    
	/** Return numerous AID found randomly(contiguous), corresponding to service*/
    static AID[] getMultipleRandomIndividu(Agent agent, int nombre )
    {	
		AID[] allIndividus = getAllIndividus(agent);

    	AID[] agents = new AID[nombre];
        if (allIndividus.length > nombre && nombre > 0){
            int randomStartIndex = rand.nextInt(allIndividus.length-nombre);
            for (int i = 0; i < nombre; i++) agents[i] = allIndividus[randomStartIndex+i];
        }
        else if(allIndividus.length == nombre){
        	return allIndividus;
        }
        return agents;
        
    }
    
    /** Ajoute tous les individus, Etat et PoleEmploi à receiver. */
    static ACLMessage createBroadcastMessage(Agent agent, ACLMessage message){
    	//Ajouter Etat et Pole Emploi
    	message.addReceiver(new AID("Etat", AID.ISLOCALNAME));
    	message.addReceiver(new AID("PoleEmploi", AID.ISLOCALNAME));

    	//Ajouter tous les individus(enregistrés sous employe, nivQualif1, nivQualif2 ou nivQualif3)
		AID[] allIndividus = Util.getAllIndividus(agent);
		for(int i = 0; i < allIndividus.length; i++) message.addReceiver(allIndividus[i]);
		
		return message;
    }
}
