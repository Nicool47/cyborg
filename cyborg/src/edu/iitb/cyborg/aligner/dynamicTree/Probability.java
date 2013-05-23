/*
 * 
 * Copyright 2013 Digital Audio Processing Lab, Indian Institute of Technology.  
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */

/**
 *
 * @author  : Jigar Gada
 * @contact : jigargada23@yahoo.com
 */

package edu.iitb.cyborg.aligner.dynamicTree;

import edu.iitb.cyborg.Fileloader.FilesLoader;

public class Probability {

	
	/**
	 * This function takes the state number and the feature vector and
	 * calculates the log likelihood value.
	 * @param s - State number
	 * @param x - Feature vector (39 dimensions)
	 */
	
	public double b(int state,float x[]) {
		int gaussian = FilesLoader.GAUSSIAN;
		int d = 39;
		float mu[][] = FilesLoader.MEAN[state];
		float sigma[][] = FilesLoader.VAR[state];
		float c[] = FilesLoader.MIXWT[state];
		
		double[] log_b = new double[gaussian];
		
		for(int k = 0 ; k < gaussian; k++){
			for(int i = 0; i < d; i++)
				log_b[k] += Math.log(2*Math.PI) + Math.log(sigma[k][i]) + Math.pow((x[i] - mu[k][i]), 2)/sigma[k][i];
			log_b[k] = -0.5 * log_b[k] + Math.log(c[k]);
			}
		double log_m = log_b[0];
		for(int i = 1; i < gaussian; i++){
			if(log_b[i] > log_m) log_m = log_b[i];
		}
		double log_exp = 0;
		for(int i = 0; i < gaussian; i++){
			log_exp += Math.exp(log_b[i] - log_m);
		}
		
		double log_likelihood = log_m + Math.log(log_exp);
		return log_likelihood;
		
	}
	

	
	/**
	 * This function returns the transition probability.  
	 * @param s - state number
	 * @param from - transition from
	 * @param to - transition to
	 * 
	 */
	public  double a(int s, int from, int to) {
		float temp;
		//This condition is if the transition is from one HMM 
		//to another HMM
		if((from != to) && (to % 3 == 0))
			temp = FilesLoader.TMAT[s][2][3];
		else
		// this condition is for transition from one state to another
		// of same HMM
		temp = FilesLoader.TMAT[s][from%3][to%3];
		
		//double val = Math.log(temp)/Math.log(1.003);
		return Math.log(temp);
	}
	
	
}
