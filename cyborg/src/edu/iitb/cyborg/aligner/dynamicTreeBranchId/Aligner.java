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
 * @author  : Nicool
 * @contact : nicool@iitb.ac.in
 */
/**
 *
 * @author  : Jigar Gada
 * @contact : jigargada23@yahoo.com
 */

package edu.iitb.cyborg.aligner.dynamicTreeBranchId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import edu.iitb.cyborg.Fileloader.FilesLoader;
import edu.iitb.cyborg.aligner.basic.Probability;
import edu.iitb.cyborg.aligner.util.checkPoints.CheckPoint;
import edu.iitb.cyborg.aligner.util.results.Results;
import edu.iitb.cyborg.fst.Fst;
import edu.iitb.cyborg.fst.Fst2Triphones;
import edu.iitb.cyborg.fst.Triphone;
import edu.iitb.cyborg.performance.Performance;
import edu.iitb.frontend.audio.feature.FeatureFileExtractor;

public class Aligner {

	static HashMap<String, String> hashMapStates = new HashMap<String, String>(); 
	
	public static void main(String[] args) throws IOException, IllegalArgumentException, UnsupportedAudioFileException, InterruptedException {
		
		Performance.logStartTime();
		
		String models = null;
		String fileName = null;
		String transcription = null;
		String dictionary = null;
		String inputFolder = null;
		String featureFolder = null;
		
		CheckPoint checkPoint = new CheckPoint();
		
		if(checkPoint.checkArgs(args)){
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
		}
		else{
			System.exit(0);
		}
	
		
		FilesLoader filesLoader = new FilesLoader();
		
		// load and initialize all the models (mean,variance, mixture weights and transition matrix) in the array
		filesLoader.initialize(models);
		filesLoader.loadDict(dictionary);
		filesLoader.loadMdef(models+"\\mdef_tab");
		
		
		//extracting mfcc features
		FeatureFileExtractor.computeFeatures(fileName, inputFolder, featureFolder);
		String featureFile = featureFolder + "/" + fileName + ".mfc";
		
		//loads the feature file in the array feat
		filesLoader.readFeat(featureFile);
		
		int totalTimeFrames = FilesLoader.FEAT.length;
		
		//Storing the features in a 2-dimensional array x[][]
		float x[][] = FilesLoader.FEAT;
	
		//Creates /Overwrite fst directory and store all the fst files
		Fst.getFST(transcription);
		
		Probability p = new Probability();
		
		/*
		 * Viterbi algo with Tree structure starts here
		 */

		//HashMap for branchId's
		HashMap<String, ArrayList<Integer>> branchingHashMap = new HashMap<String,ArrayList<Integer>>();
		
		//makes the required table for context dependency
		Fst2Triphones.makeTable();
		
		//Global branch Id
		int branchId = 1;
		
		//Creating and setting root node
		Node nodeRoot = new Node();
		
		//Creating triPhone object as a temporary container
		Triphone triphoneObj = new Triphone();
		
		//Lists to keep track of last two levels nodes
		ArrayList<Node> parentListLevelN = new ArrayList<Node>();
		ArrayList<Node> parentListLevelNplus1 = new ArrayList<Node>();
		
		//Configuring root node
		nodeRoot.setParent(null);
		
		int stateInfoTemp[] = new int[3];
		stateInfoTemp[0] = 0;
		stateInfoTemp[1] = triphoneObj.getStartState().get(0).getObsState();
		stateInfoTemp[2] = triphoneObj.getStartState().get(0).getTmatState();
		
		nodeRoot.setTriPhone(triphoneObj.getStartState().get(0).getTriphone());
		nodeRoot.setStateInfo(stateInfoTemp);
		nodeRoot.setScore(p.b(nodeRoot.getStateInfo()[1], x[0]));
		nodeRoot.setCost(nodeRoot.getScore());		
		nodeRoot.setBranchId(0);
		//System.out.println("0 ");
		
		parentListLevelN.add(nodeRoot);
		parentListLevelNplus1.add(nodeRoot);
				
		for(int indexI = 1; indexI < totalTimeFrames; indexI++){
			
			parentListLevelN.clear();
			parentListLevelN.addAll(parentListLevelNplus1);
			parentListLevelNplus1.clear();
			
			Iterator<Node> iteratorParent = parentListLevelN.iterator();
		
			while(iteratorParent.hasNext()){
				
				Node parent = iteratorParent.next(); //Getting parent from parent list
				
				triphoneObj.setTriphone(parent.getTriPhone());
				triphoneObj.setId(parent.getStateInfo()[0]);
				triphoneObj.setObsState(parent.getStateInfo()[1]);
				triphoneObj.setTmatState(parent.getStateInfo()[2]);
				
	
				ArrayList<Triphone> triPhoneList = new ArrayList<>();
				triPhoneList = triphoneObj.getNextState(triphoneObj);
				
//				System.out.print("\nInput : "+triphoneObj.getTriphone());
//				System.out.print("\tOutput : ");
//				for(Triphone tri : triPhoneList)
//					System.out.print("\t"+tri.getTriphone());
//				System.out.println();
//				System.out.print("-----------------");

				if(branchingHashMap.get(String.valueOf(parent.getBranchId())) == null && triPhoneList.size() > 1){
					for(int i = 0; i< triPhoneList.size();i++)
						parent.siblingsBranchIds.add(branchId++);
					
					branchingHashMap.put(String.valueOf(parent.getBranchId()), parent.siblingsBranchIds);
				}
				
				
				Node node[] = new Node[triPhoneList.size()+1]; // Creating child nodes

				//Configuring left child node
				node[0] = new Node();
				
				node[0].setParent(parent);
				
				node[0].setTriPhone(parent.getTriPhone());
				node[0].setStateInfo(parent.getStateInfo());
				node[0].setScore(p.b(node[0].getStateInfo()[1], x[indexI])+p.a(parent.getStateInfo()[2], parent.getStateInfo()[0], node[0].getStateInfo()[0]));
				node[0].setCost(node[0].getScore()+parent.getCost());
				node[0].setBranchId(parent.getBranchId());
				node[0].setSiblingsBranchIds(parent.getSiblingsBranchIds());
				parentListLevelNplus1.add(node[0]);
				

					
				
//				if(triPhoneList.size() == 0)
//					System.out.println("\ntriPhoneList with size 0 "+node[0].getTriPhone()+" Id"+node[0].getStateInfo()[0]);
				
				//Configuring child nodes in left to right order
				for(int i=1; i< (triPhoneList.size()+1); i++){
					node[i] = new Node();
					
					node[i].setParent(parent);
					
					int stateInfoTempInside[] = new int[3];
					stateInfoTempInside[0] = node[0].getStateInfo()[0]+1;
					stateInfoTempInside[1] = triPhoneList.get(i-1).getObsState();
					stateInfoTempInside[2] = triPhoneList.get(i-1).getTmatState();
					
					node[i].setTriPhone(triPhoneList.get(i-1).getTriphone());
					node[i].setStateInfo(stateInfoTempInside);
					node[i].setScore(p.b(node[i].getStateInfo()[1], x[indexI])+p.a(parent.getStateInfo()[2], parent.getStateInfo()[0], node[i].getStateInfo()[0]));
					node[i].setCost(node[i].getScore()+parent.getCost());				
					//System.out.println("Cost : "+node[i].getCost());
					
					
					//Assign different branch Id in case of total more then two branches
					if(triPhoneList.size() > 1 && branchingHashMap.get(String.valueOf(parent.getBranchId()))!=null){	
						node[i].setBranchId(branchingHashMap.get(String.valueOf(parent.getBranchId())).get(i-1));
						//node[i].setBranchId(parent.siblingsBranchIds.get(i-1));	
						//System.out.print(node[i].getBranchId()+" ");
						
					}
					else{
						node[i].setBranchId(parent.getBranchId());
						//System.out.print(node[i].getBranchId()+" ");
					}
					
					parentListLevelNplus1.add(node[i]);
					
				}			
				
			}
			
			//Merging the nodes
			ArrayList<Node> parentListFinal = new ArrayList<>();
			ArrayList<String> stateSeqAndObjId = new ArrayList<>();
			
			for(int i = 0; i < parentListLevelNplus1.size(); i++){
				ArrayList<Node> temp = new ArrayList<>();
				List<Node> bList = parentListLevelNplus1.subList(i, parentListLevelNplus1.size());
				if(!(stateSeqAndObjId.contains(parentListLevelNplus1.get(i).getStateInfo()[0]+"\t"+parentListLevelNplus1.get(i).getStateInfo()[1]+"\t"+parentListLevelNplus1.get(i).getBranchId()))){
					stateSeqAndObjId.add(parentListLevelNplus1.get(i).getStateInfo()[0]+"\t"+parentListLevelNplus1.get(i).getStateInfo()[1]+"\t"+parentListLevelNplus1.get(i).getBranchId());
					for(int j = 0; j < bList.size(); j++)			
						if((bList.get(j).getStateInfo()[0] == parentListLevelNplus1.get(i).getStateInfo()[0]) && 
								(bList.get(j).getStateInfo()[1] == parentListLevelNplus1.get(i).getStateInfo()[1]))
								temp.add(bList.get(j));
						
					if(temp.size() > 1)
						parentListFinal.add(Aligner.max(temp));
					else
						parentListFinal.add(temp.get(0));

				}
				
			
			}
			
//			System.out.println();
//			System.out.println("Parent list levelNplus1");
//			for(Node child : parentListLevelNplus1)
//				System.out.println(child.getTriPhone()+"\t"+child.getStateInfo()[0]+"\t"+child.getStateInfo()[1]+"\t"+child.getBranchId());
//			
//			System.out.println();
//			System.out.println("Parent list final");
//			for(Node child : parentListFinal)
//				System.out.println(child.getTriPhone()+"\t"+child.getStateInfo()[0]+"\t"+child.getStateInfo()[1]+"\t"+child.getBranchId());

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
		
//		System.out.println("last Level");
//		for(Node n : parentListLevelNplus1)
//			System.out.print(n.branchId+" ");
		
		//Back tracing
		//ArrayList<Integer> stateDuration = new ArrayList<>();
		//int stateDuration[] = new int[N];
		//int s = N - 1;
		//Node child = parentListLevelNplus1.get(parentListLevelNplus1.size()-1);
		
		//Assuming silence as a last state and backtracing it from silence.  
		int states[] = filesLoader.getStates("SIL\t-\t-\t-");
		//int states[] = filesLoader.getStates("s\ta\tSIL\te");
//		int count=0;
		Node backTraceChild = new Node();
		double max=0; 
		
		for(Node child : parentListLevelNplus1)
			if(states[4] == child.getStateInfo()[1]){
				max = child.getCost();
//				System.out.println("Max cost : "+max);
				break;
			}
		
		for(Node child: parentListLevelNplus1){
			if(states[4] == child.getStateInfo()[1]){
			    if(max <= child.getCost()){
			    	max = child.getCost();
			    	backTraceChild = child;
			    }
//			    System.out.println("Max cost : "+max);
//				count++;
//				if(count == 4){
//					backTraceChild = child;
//					break;
//				}
					
			}
		}
		
		
		
//		System.out.println("Last level child");
//		for(Node child : parentListLevelNplus1)
//			System.out.println(child.getTriPhone());
	
		//for(Node child: parentListLevelNplus1)
//        System.out.println("Counttt : "+count);
			
        //Backtracing path
		int N = backTraceChild.getStateInfo()[0]+1;
		System.out.println("Value of N : "+N);
		int stateDuration[] = new int[N];
		double scoreArray[] = new double[N];
		String triPhoneSeq[] = new String[N];
		
		while(backTraceChild.getParent() != null){
			Node parent = backTraceChild.getParent();

			//System.out.println("State index : "+backTraceChild.getStateInfo()[0]);
			
			stateDuration[backTraceChild.getStateInfo()[0]]++;
			//stateDuration[backTraceChild.getStateInfo()[0]][1] = backTraceChild.getStateInfo()[1];
			scoreArray[backTraceChild.getStateInfo()[0]] = backTraceChild.getScore();
			triPhoneSeq[backTraceChild.getStateInfo()[0]] = backTraceChild.getTriPhone();
			backTraceChild = parent;
		}
		

		stateDuration[backTraceChild.getStateInfo()[0]]++;
		//stateDuration[backTraceChild.getStateInfo()[0]][1] = backTraceChild.getStateInfo()[1];
		scoreArray[backTraceChild.getStateInfo()[0]] = backTraceChild.getScore();
		triPhoneSeq[backTraceChild.getStateInfo()[0]] = backTraceChild.getTriPhone();

		String phonemSeq[] = new String[N];
		String triphoneSeq[] = new String[N];
		int j=0;
		for(int i = 0; i<N ;i++){
//			System.out.println(stateDuration[i][0]+" "+stateDuration[i][1]+"\t"+numberFormator.format(scoreArray[i])+"\t"+triPhoneSeq[i]);
			if(i%3 == 0){
			 phonemSeq[j] = triPhoneSeq[i].split("\t")[0];
			 triphoneSeq[j] = triPhoneSeq[i];
			 j++;
			}
			//System.out.println(triPhoneSeq[i].split("\t")[0]+" ");
		}

		System.out.println();
		System.out.println("Phonem sequence");
		for(int i = 0; i<N/3 ;i++)
					System.out.println(phonemSeq[i]+" ");
		
//		System.out.println();
//		System.out.println("FST file content");
//		for(Fst2Triphones obj : Fst2Triphones.arrayObj)
//		    System.out.println(obj.getIn()+" "+obj.getOp()); 
		
		Results results = new Results();
		results.printPhoneSegInConsole(stateDuration, phonemSeq, triphoneSeq, scoreArray);
		//results.printPhoneSegInFile(stateDuration, phonemSeq, triphoneSeq, scoreArray);
		results.printWordSegInConsole(stateDuration, phonemSeq, triphoneSeq, scoreArray);
		//results.printWordSegInFile(stateDuration, phonemSeq, triphoneSeq, scoreArray);
		
//		results.printResultsSphinx(stateDuration, phonemSeq, triphoneSeq, scoreArray);
//		results.printPhoneSegInConsole(stateDuration, phonemSeq, scoreArray);
//		results.printPhoneSegInFile(stateDuration, phonemSeq, scoreArray);
		
//stateDuration[s]++;
		
//		while(child.getParent() != null){
//			Node parent = child.getParent();
//
//			if(parent.getStateInfo()[1] != child.getStateInfo()[1]) s--;
//			stateDuration[s]++;
//			child = parent;
//
//		}
//
//		printResults(stateDuration);	
		
		/*
		 * Tree structure ends
		 */
		Performance.logEndTime();
		Performance.logEndTimeInFile();
		Performance.memInfo();
		Performance.memInfoInFile();
	}	// end of main
	
	

	public static Node max(ArrayList<Node> elements) {
		Node m = elements.get(0);
		for(int i = 1 ; i < elements.size() ; i++){
			if(elements.get(i).getCost() > m.getCost()) m = elements.get(i);
		}
		return m;
	}
}


//Structure of the tree node
class Node{

	//Reference to parent node
	Node parent;
	
	/* stateInfo[0] : State ID index
	 * stateInfo[1] : Observation id
	 * stateInfo[2] : State tmat value 
	 */
	int stateInfo[];
	
	int branchId;
	
	//Corresponding triphone
	String triPhone;
	
	//Stores likelihood of each frame
	double score;
	
	double cost;
	
	boolean active;
	
	//Stores list of children in left to right order.
	ArrayList<Node> children = new ArrayList<Node>();
	
	//Stores branchIds in case
	ArrayList<Integer> siblingsBranchIds = new ArrayList<>();
	
	Node(){
		stateInfo = new int[3];
		active = true;
	}
	

	public ArrayList<Integer> getSiblingsBranchIds() {
		return siblingsBranchIds;
	}

	public void setSiblingsBranchIds(ArrayList<Integer> siblingsBranchIds) {
		this.siblingsBranchIds = siblingsBranchIds;
	}

	
	public int getBranchId() {
		return branchId;
	}

	public void setBranchId(int branchId) {
		this.branchId = branchId;
	}


	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
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
