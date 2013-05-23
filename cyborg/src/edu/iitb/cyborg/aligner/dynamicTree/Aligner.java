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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import edu.iitb.cyborg.Fileloader.FilesLoader;
import edu.iitb.cyborg.aligner.basic.Probability;
import edu.iitb.cyborg.fst.Fst2Triphones;
import edu.iitb.cyborg.fst.Triphone;
import edu.iitb.frontend.audio.feature.FeatureFileExtractor;
import edu.iitb.cyborg.performance.Performance;

public class Aligner {

	//Tree structure starts
	static HashMap<String, String> hashMapStates = new HashMap<String, String>(); 
	//Tree structure ends
	
	public static void main(String[] args) throws IOException, IllegalArgumentException, UnsupportedAudioFileException {
			
		Performance.logStartTime();
		
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
		
		Probability p = new Probability();
		
		/*
		 * Viterbi algo with Tree structure starts
		 */
		
		//Loading state information to HashTable to get next state info based on current state.
		//createHashMap(states);

		//makes the required table for context dependency
		Fst2Triphones.makeTable();
		
		//Global index
		int index = 0;
		
		//Creating and setting root node
		Node nodeRoot = new Node();
		
		Triphone triphoneObj = new Triphone();
		
		//Temp code
		String triphone = "l	SIL	aa	b";
		//String triphone1 = "SIL	-	-	-";
		int statesTemp[] = filesLoader.getStates(triphone);

		nodeRoot.setParent(null);
		int stateInfoTemp[] = new int[3];
		stateInfoTemp[0] = index;
		stateInfoTemp[1] = statesTemp[2];
		stateInfoTemp[2] = statesTemp[1];
		
		nodeRoot.setTriPhone(triphone);
		nodeRoot.setStateInfo(stateInfoTemp);
		nodeRoot.setCost(p.b(statesTemp[2],x[0]));
		
		triphoneObj.setTriphone(triphone);
		triphoneObj.setObsState(statesTemp[4]);
		triphoneObj.setTmatState(statesTemp[1]);
		triphoneObj.setId(index%3+1);
		
		//ArrayList<Triphone> tempList = new ArrayList<>();
		//tempList = triphoneObj.getNextState(triphoneObj);
		
		for(Triphone t : triphoneObj.getNextState(triphoneObj)){
			System.out.println("next triphone -->" + t.getTriphone());
			System.out.println("Obs state--> " + t.getObsState() + " //tmat state--> " + t.getTmatState());
		}
		
		//System.exit(0);
		//Temp code
		

		/* Temporary commented */
		
//		nodeRoot.setParent(null);
//		int stateInfoTemp[] = new int[3];
//		stateInfoTemp[0] = index++;
//		stateInfoTemp[1] = triphoneObj.getStartState().get(0).getObsState();
//		stateInfoTemp[2] = triphoneObj.getStartState().get(0).getTmatState();
//		
//		nodeRoot.setTriPhone(triphoneObj.getStartState().get(0).getTriphone());
//		nodeRoot.setStateInfo(stateInfoTemp);
//		nodeRoot.setCost(p.b(states[0][0],x[0]));
		
		/*********************/
		
		ArrayList<Node> parentListLevelN = new ArrayList<Node>();
		ArrayList<Node> parentListLevelNplus1 = new ArrayList<Node>();
		
		parentListLevelN.add(nodeRoot);
		parentListLevelNplus1.add(nodeRoot);
				
		for(int indexI = 1; indexI < totalTimeFrames; indexI++){
			
			parentListLevelN.clear();
			parentListLevelN.addAll(parentListLevelNplus1);
			parentListLevelNplus1.clear();
			
			Iterator<Node> iteratorParent = parentListLevelN.iterator();
		
			while(iteratorParent.hasNext()){
				
				Node parent = iteratorParent.next(); //Getting parent from patent list
				
				triphoneObj.setTriphone(parent.getTriPhone());
				triphoneObj.setObsState(parent.getStateInfo()[1]);
				triphoneObj.setTmatState(parent.getStateInfo()[2]);
				triphoneObj.setId((parent.getStateInfo()[0])%3+1);
				
				ArrayList<Triphone> tempList = new ArrayList<>();
				tempList = triphoneObj.getNextState(triphoneObj);
				
				Node node[] = new Node[tempList.size()]; // Creating two child nodes(Left and Right)

				//Configuring left child node
				node[0] = new Node();
				node[0].setParent(parent);
				node[0].setStateInfo(parent.getStateInfo());
				node[0].setCost(p.b(node[0].getStateInfo()[1], x[indexI])+p.a(parent.getStateInfo()[2], parent.getStateInfo()[0], node[0].getStateInfo()[0])+parent.getCost());
				parentListLevelNplus1.add(node[0]);
				
				
				//Configuring child nodes in left to right order
				for(int i=1; i<tempList.size(); i++){
					
					node[i] = new Node();
					node[i].setParent(parent);
					int stateInfoTempInside[] = new int[3];
					stateInfoTempInside[0] = index;
					stateInfoTempInside[1] = triphoneObj.getStartState().get(i).getObsState();
					stateInfoTempInside[2] = triphoneObj.getStartState().get(i).getTmatState();
					
					node[i].setStateInfo(stateInfoTempInside);
					node[i].setCost(p.b(node[i].getStateInfo()[1], x[indexI])+p.a(parent.getStateInfo()[2], parent.getStateInfo()[0], node[i].getStateInfo()[0])+parent.getCost());
					parentListLevelNplus1.add(node[i]);
					
				}
				
				index++;
				
//				//Configuring left child node
//				node[0] = new Node();
//				node[0].setParent(parent);
//				node[0].setStateInfo(parent.getStateInfo());
//				node[0].setCost(p.b(node[0].getStateInfo()[1], x[indexI])+p.a(parent.getStateInfo()[2], parent.getStateInfo()[0], node[0].getStateInfo()[0])+parent.getCost());
//				parentListLevelNplus1.add(node[0]);
//				
//				
//				String childStateInfo[];
//				try{
//					childStateInfo = hashMapStates.get(Integer.toString(parent.getStateInfo()[0])+" "+Integer.toString(parent.getStateInfo()[1])+" "+Integer.toString(parent.getStateInfo()[2])).split(" ");
//				}
//				catch(NullPointerException e){
//					break;
//				}
//				int childStateInfoTemp[] = new int[3];
//				for(int i = 0;i< childStateInfo.length;i++)
//					childStateInfoTemp[i] = Integer.parseInt(childStateInfo[i]);
//				
//				
//				node[1] = new Node();
//				node[1].setParent(parent);
//				node[1].setStateInfo(childStateInfoTemp);				
//				node[1].setCost(p.b(node[1].getStateInfo()[1], x[indexI])+p.a(parent.getStateInfo()[2], parent.getStateInfo()[0], node[1].getStateInfo()[0]) + parent.getCost());
//				parentListLevelNplus1.add(node[1]);
					
				//childrenList.add(node[0]);
				//childrenList.add(node[1]);
				//parent.setChildren(childrenList);
							
			}
			
			//Merging the nodes
			ArrayList<Node> parentListFinal = new ArrayList<>();
			ArrayList<Integer> stateSeq = new ArrayList<>();
			
			for(int i = 0; i < parentListLevelNplus1.size(); i++){
				ArrayList<Node> temp = new ArrayList<>();
				List<Node> bList = parentListLevelNplus1.subList(i, parentListLevelNplus1.size());
				if(!(stateSeq.contains(parentListLevelNplus1.get(i).getStateInfo()[0]))){
					stateSeq.add(parentListLevelNplus1.get(i).getStateInfo()[0]);
					for(int j = 0; j < bList.size(); j++)			
						if(bList.get(j).getStateInfo()[0] == parentListLevelNplus1.get(i).getStateInfo()[0]) temp.add(bList.get(j));
						
					if(temp.size() > 1)
						parentListFinal.add(Aligner.max(temp));
					else
						parentListFinal.add(temp.get(0));

				}
			
			}

			//get the node with maximum cost.
			double maxCost = Aligner.max(parentListFinal).getCost();
			//System.out.println("maximum cost " + maxCost);
			double pruneLimit = -126.64; // ln(10^-55)
			double beam = maxCost + pruneLimit;
			//System.out.println("beam cost " + beam);
			ArrayList<Node> prunedList = new ArrayList<>();
				
			//System.out.println("info for pruned nodes");
			for(Node n : parentListFinal){
				//pruning 
				if(n.getCost() > beam){
					prunedList.add(n);
					Node parent = n.getParent();
					parent.setChild(n);
				}			
			}
			// assigning the final reference
			//parentListLevelNplus1 = parentListFinal;
			parentListLevelNplus1 = prunedList;
			
		}
		
		int stateDuration[] = new int[N];
		int s = N - 1;
		Node child = parentListLevelNplus1.get(parentListLevelNplus1.size()-1);
		stateDuration[s]++;
		
		while(child.getParent() != null){
			Node parent = child.getParent();

			if(parent.getStateInfo()[1] != child.getStateInfo()[1]) s--;
			stateDuration[s]++;
			child = parent;

		}

		printResults(stateDuration);		
		Performance.logEndTime();
		Performance.memInfo();
		/*
		 * Tree structure ends
		 */
		
	}	// end of main
	
	

	public static Node max(ArrayList<Node> elements) {
		Node m = elements.get(0);
		for(int i = 1 ; i < elements.size() ; i++){
			if(elements.get(i).getCost() > m.getCost()) m = elements.get(i);
		}
		return m;
	}
	
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
			
			hashMapStates.put(Integer.toString(key[0])+" "+Integer.toString(key[1])+" "+
			Integer.toString(key[2]), Integer.toString(value[0])+" "+
			Integer.toString(value[1])+" "+Integer.toString(value[2]));
		}
	}
}


//Structure of the tree node
class Node{

	//Reference to parent node
	Node parent;
	
	/* stateInfo[0] : State ID index
	 * stateInfo[1] : State ID
	 * stateInfo[2] : State tmat value 
	 */
	int stateInfo[];
	
	//Corresponding triphone
	String triPhone;
	
	double cost;
	boolean active;
	
	//Stores list of children in left to right order.
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
	
	
	public String getTriPhone() {
		return triPhone;
	}

	public void setTriPhone(String triPhone) {
		this.triPhone = triPhone;
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
	
	public void setChild(Node child) {
		this.children.add(child);
	}	
	
}
