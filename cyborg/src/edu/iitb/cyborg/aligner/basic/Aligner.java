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

package edu.iitb.cyborg.aligner.basic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import edu.iitb.cyborg.Fileloader.FilesLoader;
import edu.iitb.frontend.audio.feature.FeatureFileExtractor;;

public class Aligner {

	public static void main(String[] args) throws IOException, IllegalArgumentException, UnsupportedAudioFileException {
		
		String models = null;
		String fileName = null;
		String transcription = null;
		String dictionary = null;
		String inputFolder = null;
		String featureFolder = null;
		
		for(int i = 0; i < args.length ; i++){
			if(args[i].equals("-models")){
				models = args[++i];
			}
			if(args[i].equals("-name")){
				fileName = args[++i];
			}
			if(args[i].equals("-in")){
				inputFolder = args[++i];
			}
			if(args[i].equals("-t")){
				transcription = args[++i];
			}
			if(args[i].equals("-dict")){
				dictionary = args[++i];
			}
			if(args[i].equals("-feat")){
				featureFolder = args[++i];
			}
			
			
		}
		if(models == null || fileName == null || inputFolder == null || transcription == null 
				|| dictionary == null || featureFolder == null){
			System.out.println("Insuffient arguments\n Usage ---> \n" +
			"java -jar \n -models <folder path which has all model files>\n " +
			"-name <input audio file without .wav extension>\n " +
			"-in <folder in which wav files are stored>\n " +
			"-feat <folder in which feature files will be stored>\n " +
			"-t <transcription file>\n " +
			"-dict <dictionary file>");
			System.exit(0);
		}
		
		// load and initialise all the models (mean,variance, mixture weights and transition matrix) in the array
		FilesLoader filesLoader = new FilesLoader();
		filesLoader.initialize(models);
		
		//--------------------//
		filesLoader.loadDict(dictionary);
		filesLoader.loadMdef(models+"\\mdef_tab");
		
		
		BufferedReader brTrans = new BufferedReader(new FileReader(transcription));
		String trans = brTrans.readLine();
		brTrans.close();
		//-------------------//
		
		//extracting mfcc features
		FeatureFileExtractor.computeFeatures(fileName, inputFolder, featureFolder);
		String featureFile = featureFolder + "/" + fileName + ".mfc";
		
		//loads the feature file in the array feat
		filesLoader.readFeat(featureFile);
		
		int totalTimeFrames = FilesLoader.FEAT.length;
		//Storing the features in a 2-dimensional array x[][]
		float x[][] = FilesLoader.FEAT;
		
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
		Probability p = new Probability();
//-------- Viterbi algorithm ---------------------------------------------------------
		
		cost[0][0] = p.b(states[0][0],x[0]);
		backPtr[0][0] = 1;
		
		for(int t = 1; t < totalTimeFrames ; t++){
			cost[0][t] = p.b(states[0][0],x[t]) + p.a(states[1][0],0,0) + cost[0][t-1];
			backPtr[0][t] = 1;
			
			for(int s = 1; s <= N-1 ;s++){
				if((t < N && s < t) || (t >= N)){
					
					double a1 = cost[s][t-1] + p.a(states[1][s],s,s);
					double a2 = cost[s-1][t-1] + p.a(states[1][s-1],s-1,s);
					if(a1 >= a2){
						cost[s][t] = a1;
						backPtr[s][t] = 1;
					}
					else{
						cost[s][t] = a2;
						backPtr[s][t] = 2;
					}
					cost[s][t] += p.b(states[0][s],x[t]);
				}
				
				if(t< N && s==t){
					cost[s][t] = cost[s-1][t-1] + p.a(states[1][s-1],s-1,s) + p.b(states[0][s],x[t]);
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
        printResults(stateDuration);
		
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
