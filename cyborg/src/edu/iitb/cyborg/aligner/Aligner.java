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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import edu.iitb.cyborg.performance.Performance;

public class Aligner {

	
	/**
	 * This function takes the state number and the feature vector and
	 * calculates the log likelihood value.
	 * @param s - State number
	 * @param x - Feature vector (39 dimensions)
	 */
	
	public static double b(int state,float x[]) {
		int gaussian = FilesLoader.gaussian;
		int d = 39;
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
		return log_likelihood;
		
	}
	
	/**
	 * This function returns the transition probability.  
	 * @param s - state number
	 * @param from - transition from
	 * @param to - transition to
	 * 
	 */
	public static double a(int s, int from, int to) {
		float temp;
		//This condition is if the transition is from one HMM 
		//to another HMM
		if((from != to) && (to % 3 == 0))
			temp = FilesLoader.tmat[s][2][3];
		else
		// this condition is for transition from one state to another
		// of same HMM
		temp = FilesLoader.tmat[s][from%3][to%3];
		
		//double val = Math.log(temp)/Math.log(1.003);
		return Math.log(temp);
	}
	
	public static void main(String[] args) throws IOException {
		Performance.logStartTime();
		
		String models = null;
		String audioInput = null;
		String transcription = null;
		String dictionary = null;
		
		for(int i = 0; i < args.length ; i++){
			if(args[i].equals("-models")){
				models = args[++i];
			}
			if(args[i].equals("-i")){
			audioInput = args[++i];
			}
			if(args[i].equals("-t")){
				transcription = args[++i];
			}
			if(args[i].equals("-dict")){
				dictionary = args[++i];
			}
			
		}
		if(models == null || audioInput == null){
			System.out.println("Insuffient arguments\n Usage ---> \n" +
			"java -jar <> -models <folder path which has all model files> -i <input audio file> -t <transcription file> -dict <dictionary file>");
			System.exit(0);
		}
		
		// load and initialize the models in the array
		FilesLoader filesLoader = new FilesLoader();
		filesLoader.initialize(models);
		
		//--------------------//
		filesLoader.loadDict(dictionary);
		filesLoader.loadMdef(models+"\\mdef_tab");
		
		
		BufferedReader brTrans = new BufferedReader(new FileReader(transcription));
		String trans = brTrans.readLine();
		brTrans.close();
		//-------------------//
		
		//loads the feature file in the array feat
		filesLoader.readFeat("features/suryaphuula_d_dd.mfc");
		
		int totalTimeFrames = FilesLoader.feat.length;
		float x[][] = FilesLoader.feat;
		
		// Get the sequence of states.
		int states[][] = filesLoader.getStatesOfTrans(trans);
		
		System.out.println();
		System.out.println("State Array : "+states.length+" X "+states[0].length);
		for(int indexI = 0; indexI < states.length; indexI++){
			for(int indexJ = 0; indexJ < states[indexI].length; indexJ++){
				System.out.print(states[indexI][indexJ]+"\t");
			}
			System.out.println();
		}
		
		int N = states[0].length;
		System.out.println("Total number of states = "+N);
		
		double cost[][] = new double[N][totalTimeFrames];
		int backPtr[][] = new int[N][totalTimeFrames];
//-------- Viterbi algorithm ---------------------------------------------------------
		
		cost[0][0] = b(states[0][0],x[0]);
		backPtr[0][0] = 1;
		
		for(int t = 1; t < totalTimeFrames ; t++){
			cost[0][t] = b(states[0][0],x[t]) + a(states[1][0],0,0) + cost[0][t-1];
			backPtr[0][t] = 1;
			
			for(int s = 1; s <= N-1 ;s++){
				if((t < N && s < t) || (t >= N)){
					
					double a1 = cost[s][t-1] + a(states[1][s],s,s);
					double a2 = cost[s-1][t-1] + a(states[1][s-1],s-1,s);
					if(a1 >= a2){
						cost[s][t] = a1;
						backPtr[s][t] = 1;
					}
					else{
						cost[s][t] = a2;
						backPtr[s][t] = 2;
					}
					cost[s][t] += b(states[0][s],x[t]);
				}
				
				if(t< N && s==t){
					cost[s][t] = cost[s-1][t-1] + a(states[1][s-1],s-1,s) + b(states[0][s],x[t]);
					backPtr[s][t] = 2;
					}
				}
		}
		
		int stateDuration[] = new int[N];
		int s = N - 1;
		for(int t = totalTimeFrames - 1; t >= 0; t--){
			if(backPtr[s][t] == 1){
				stateDuration[s]++;
			}
			else if(backPtr[s][t] == 2){
				stateDuration[s]++;
				s--;
			}
			else System.out.println("error in force alignment--> backPtr value = 0");
		}
		
		
		System.out.println();
//		for(int i = 0; i < N; i++){
//			System.out.println(stateDuration[i]);
//		}
		
        printResults(stateDuration);
        Performance.logEndTime();
        Performance.memInfo();
        
		
	}	// end of main
	
	static void printResults(int stateDuration[])
	{
		System.out.println();
		System.out.println("****************** Phone segmentation *****************");
		System.out.println();
		System.out.println("SFrm\tEFrm\tNoOfFrames\tTriPhone");
		int SFrm = 0;
		int EFrm = 0;
		int noOfFrames = 0;
		int totalNoOfFrames = 0;
		
		for(int i = 0; i < FilesLoader.triPhoneList.length; i++){
			
			noOfFrames 	= stateDuration[i*3] + stateDuration[i*3+1] + stateDuration[i*3+2];
			totalNoOfFrames += noOfFrames;
			EFrm = totalNoOfFrames - 1;
			
			System.out.println(SFrm+"\t"+EFrm+"\t"+noOfFrames+"\t\t"+FilesLoader.triPhoneList[i].replaceAll("\t", " "));
			SFrm = totalNoOfFrames;
		}
		
		System.out.println();
		System.out.println("Total no of frames : "+totalNoOfFrames);
		System.out.println();
		
	}
}
