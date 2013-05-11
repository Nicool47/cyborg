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

package edu.iitb.cyborg.aligner.tree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.sound.sampled.UnsupportedAudioFileException;

import edu.iitb.cyborg.Fileloader.FilesLoader;
import edu.iitb.frontend.audio.feature.FeatureFileExtractor;;
//import edu.iitb.tree.Node;

public class Aligner {

	//Tree structure starts
	static HashMap<String, String> hashMapStates = new HashMap<String, String>(); 
	//Tree structure ends
	
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
		for(int array[]: states){
			for(int state:array)
				System.out.print(state+"\t");
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
		
		
		//Tree structure starts
		createHashMap(states);

		Node nodeRoot = new Node();
		nodeRoot.setParent(null);
		int stateInfoTemp[] = new int[3];
		stateInfoTemp[0] = 0;
		stateInfoTemp[1] = states[0][0];
		stateInfoTemp[2] = states[1][0];
		
		nodeRoot.setStateInfo(stateInfoTemp);
		nodeRoot.setCost(p.b(states[0][0],x[0]));
		
		ArrayList<Node> parentList = new ArrayList<Node>();
		parentList.add(nodeRoot);
		
		
		for(int indexI=1;indexI<6;indexI++){
			
			Iterator<Node> iteratorParent = parentList.iterator();
			while(iteratorParent.hasNext()){
				Node node[] = new Node[2];
				
				Node parent = iteratorParent.next();
				node[0] = new Node();
				node[0].setParent(parent);
				
				int stateInfoTempCh1[] = new int[3];
				stateInfoTempCh1[0] = parent.getStateInfo()[0];
				stateInfoTempCh1[1] = parent.getStateInfo()[1];
				stateInfoTempCh1[2] = parent.getStateInfo()[2];
				node[0].setStateInfo(stateInfoTemp);
				node[0].setCost(p.b(node[0].getStateInfo()[1], x[indexI])+p.a(parent.getStateInfo()[2], parent.getStateInfo()[0], node[0].getStateInfo()[0])+parent.getCost());
				System.out.println("Cost of child 1: "+node[0].getCost());
				
				
				System.out.println(parent.getStateInfo()[0]+" "+parent.getStateInfo()[1]+" "+parent.getStateInfo()[2]);
				
				String childStateInfoString[] = hashMapStates.get(Integer.toString(parent.getStateInfo()[0])+" "+Integer.toString(parent.getStateInfo()[1])+" "+Integer.toString(parent.getStateInfo()[2])).split(" ");
				
				int childStateInfoTemp[] = new int[3];
				for(int i = 0;i< childStateInfoString.length;i++)
					childStateInfoTemp[i] = Integer.parseInt(childStateInfoString[i]);
				
				
				System.out.print("====>"+childStateInfoTemp[0]+" "+childStateInfoTemp[1]+" "+childStateInfoTemp[2]);
				//nodeRoot.setCost(p.b(states[0][0],x[0]));
				
				node[1] = new Node();
				node[1].setParent(parent);
				int stateInfoTempCh2[] = new int[3];
				stateInfoTempCh2[0] = childStateInfoTemp[0];
				stateInfoTempCh2[1] = childStateInfoTemp[1];
				stateInfoTempCh2[2] = childStateInfoTemp[2];
				node[1].setStateInfo(stateInfoTemp);
				System.out.println("a parameters:"+ parent.getStateInfo()[2]+" "+parent.getStateInfo()[0]+" "+ node[1].getStateInfo()[0]);
				
				node[1].setCost(p.b(node[1].getStateInfo()[1], x[indexI])+p.a(parent.getStateInfo()[2], parent.getStateInfo()[0], node[1].getStateInfo()[0]) + parent.getCost());
				System.out.println("Cost of child 2: "+node[1].getCost());
				
			}
			
				
				
				
				
				
			
			
		}
		//Tree structure ends
		
		
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
        
        System.out.println("Backpointer values:");
        for(int array[]:backPtr){
        	for(int ptr:array)
        		System.out.print(ptr+" ");
        	System.out.println();
        }
        
        System.out.println("Cost values:");
        for(double array[]:cost){
        	for(double val:array){
        		NumberFormat numberFormator = new DecimalFormat("000.0000");
        		System.out.print(numberFormator.format(val)+"\t");
        	}
        	System.out.println();
        }
		
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
	
	static void createHashMap(int [][] states){
		
		int key[] = new int[3];
		int value[] = new int[3];
		
		for(int i=0; i<states[0].length-1; i++){
			key[0]=i;
			key[1]=states[0][i];
			key[2]=states[1][i];
			
			value[0] = i+1;
			value[1] = states[0][i+1];
			value[2] = states[1][i+1];
			
			String temp1 = Integer.toString(value[0])+" "+Integer.toString(value[1])+" "+Integer.toString(value[2]);
			String temp2 = Integer.toString(value[0])+" "+Integer.toString(value[1])+" "+Integer.toString(value[2]);
			
			hashMapStates.put(Integer.toString(key[0])+" "+Integer.toString(key[1])+" "+Integer.toString(key[2]), Integer.toString(value[0])+" "+Integer.toString(value[1])+" "+Integer.toString(value[2]));
			//int x[] = hashMapStates.get(key[0]+" "+key[1]+" "+key[2]);
			//System.out.println(key[0]+" "+key[1]+" "+key[2]+"==>"+x[0]+ " "+x[1]+" "+x[2]);
		}
	}
}

class Node{

	Node parent;
	//Stores 
	//stateInfo[0] : State ID index
	//stateInfo[1] : State ID
	//stateInfo[2] : State tmat value
	
	int stateInfo[];
	double cost;
	boolean active;
	ArrayList<Node> children = new ArrayList<Node>();
	
	Node(){
		stateInfo = new int[3];
		active = true;
	}
	
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public Node getParent() {
		return parent;
	}
	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	

	public int[] getStateInfo() {
		return stateInfo;
	}

	public void setStateInfo(int[] stateInfo) {
		this.stateInfo = stateInfo;
	}

	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	
	public ArrayList<Node> getChildren() {
		return children;
	}
	public void setChildren(ArrayList<Node> children) {
		this.children = children;
	}		
	
}
