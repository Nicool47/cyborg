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

package edu.iitb.cyborg.aligner;

import java.io.IOException;

public class GmmExample {

	public static void main(String[] args) throws IOException {
		
//		float mu[][] = {{12.58f,0.94f,-0.98f},{16.15f,0.373f,-0.853f},{10.8f,0.929f,-0.1758f}};
//		float sigma[][] = {{2.594f,0.07714f,0.286f},{2.142f,0.2068f,0.07968f},{1.373f,0.2014f,0.1226f}};
//		float c[] = {0.4f,0.3f,0.3f};
//		float x[] = {13.83f,0.734f,-1.186f};
//		int gaussian = 3;
		
		FilesLoader filesLoader = new FilesLoader();
		filesLoader.initialize("sphinx3_FA_noCMN_s1000_g16.cd_cont_1000");
		filesLoader.readFeat("resources/m300_clipped.mfc");
		//System.out.println(totalTimeFrames);
		//extracting the model file wrt the state
		int state = 299;
		int t = 3;
		int gaussian = FilesLoader.gaussian;
		int d = 39;
		
		float x[] = FilesLoader.feat[t];
		float mu[][] = FilesLoader.mean[state];
		float sigma[][] = FilesLoader.var[state];
		float c[] = FilesLoader.mixWt[state];
		
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
		//System.out.println(log_m);
		System.out.println("log likelihood value is " + log_likelihood);
	}
	
	
}
