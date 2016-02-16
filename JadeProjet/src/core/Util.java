package core;

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
    
	/** Return only one AID found, corresponding to service*/
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
    
    static ACLMessage createBroadcastMessage(Agent agent, ACLMessage message){
    	//Ajouter Etat et Pole Emploi
    	message.addReceiver(new AID("Etat", AID.ISLOCALNAME));
    	message.addReceiver(new AID("PoleEmploi", AID.ISLOCALNAME));

    	//Ajouter tous les individus(enregistrés sous actif, nivQualif1, nivQualif2 ou nivQualif3)
		AID[] individusActifs = Util.searchDF(agent,"actif");
		for(int i = 0; i < individusActifs.length; i++) message.addReceiver(individusActifs[i]);
		AID[] individusNiv1 = Util.searchDF(agent,"nivQualif1");
		for(int i = 0; i < individusNiv1.length; i++) message.addReceiver(individusNiv1[i]);
    	AID[] individusNiv2 = Util.searchDF(agent,"nivQualif2");
		for(int i = 0; i < individusNiv2.length; i++) message.addReceiver(individusNiv2[i]);
		AID[] individusNiv3 = Util.searchDF(agent,"nivQualif3");
		for(int i = 0; i < individusNiv3.length; i++) message.addReceiver(individusNiv3[i]);
		
		return message;
    }
}
