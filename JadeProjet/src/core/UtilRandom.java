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
public class UtilRandom {
	static Random rand = new Random(); 
	
	/** Improved register function. Allows to register if the agent forgot to deregister previously.*/
	static int discreteNextGaussian(int mean, int deviation, int min, int max)
    {
		int result = (int) (rand.nextGaussian()*deviation+mean);
		
		//Bounds
		if (result < min){
			result = min;
		}
		else if (result > max){
			result = max;
		}
		
		return result;
    }

}
