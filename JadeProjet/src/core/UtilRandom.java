package core;

import java.util.Random;

/**
 * 
 * Util for simulating gaussian float distribution and obtaining integers
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
