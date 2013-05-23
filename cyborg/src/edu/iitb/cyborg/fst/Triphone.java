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

package edu.iitb.cyborg.fst;

import java.io.IOException;
import java.util.ArrayList;

import edu.iitb.cyborg.Fileloader.FilesLoader;

public class Triphone {
	
	String triphone;
	int obsState;
	int tmatState;
	int id;

	public static ArrayList<String> getNextTriphone(int id, String triphone) throws IOException {
		
		String phone[] = triphone.split("\t");
		//phone[0] -> center phone
		//phone[1] -> left phone
		//phone[2] -> right phone
		//phone[3] -> marker
		String nextMarker = "i";
		ArrayList<String> next = new ArrayList<>();
		String nextLeft = phone[0];
		String nextCenter = phone[2];
		//ArrayList<String> rightPhone = new ArrayList<>();
		int i;
		//searches for the right element starting from id and maintains its index
		// in i.
		for(i = id ; i < Fst2Triphones.arrayObj.size(); i++){
			if(phone[2].equals(Fst2Triphones.arrayObj.get(i).getIn())){
				break;
			}
		}
		try{
		//get the integer id's of the right column of table2in the 'ids'
		ArrayList<Integer> idsRight = Fst2Triphones.arraytable2.get(i).getRight();
		ArrayList<Integer> idsLeft = Fst2Triphones.arraytable2.get(i).getLeft();
		
		//to check if the previous centre triphone
		//is same as current left triphone.
		int flag = 0;
		for(int pos:idsLeft){
			if(Fst2Triphones.arrayObj.get(pos).getIn().equals(phone[0]))flag = 1;
		}
		if(flag == 0){
			System.out
					.println("Incorrect ID; previous centre triphone not matching current left triphone.");
			System.out.println("Check your ID.");
//			return null;
		}
		
		for(int pos:idsRight){
			String nextRight = Fst2Triphones.arrayObj.get(pos).getIn();
			if(nextRight.equals("SIL")){
				nextMarker = "e";
				break;
			}
		}
		
		for(int pos:idsRight){
			//pos == Fst2Triphones.finalState && Fst2Triphones.arrayObj.get(pos).getIn().equals("sila"))||
			//
			if(Fst2Triphones.arrayObj.get(pos).getIn() == null){
				//System.out.println("null; end of state reached");
				next.add(null);
				break;
			}
			//System.out.println(nextCenter + "/" + nextLeft + "_" + Fst2Triphones.arrayObj.get(pos).getIn());
			String nextRight = Fst2Triphones.arrayObj.get(pos).getIn();
			
			//For the markers
			if(nextLeft.equals("SIL"))nextMarker = "b";
			else if(nextRight.equals("SIL")) nextMarker = "e";
			else if(phone[3].equals("e"))nextMarker = "b";
			else if(nextMarker.equals("e"))nextMarker = "e";
			else nextMarker = "i";
			//------------//
			
			String a = nextCenter + "\t" + nextLeft + "\t" + nextRight + "\t" + nextMarker;
			next.add(a);
		}
		}
		catch(Exception e){
			System.out.println("Incorrect triphone, This triphone does not exixt in the " +
					"current transcription file. :(");
		}
		return next;
		
	}
	
	public ArrayList<String> getStartTriphone() {
		ArrayList<String> start = new ArrayList<>();
		ArrayList<Integer> ids_right = Fst2Triphones.arraytable2.get(1).getRight();
		ArrayList<Integer> ids_left = Fst2Triphones.arraytable2.get(1).getLeft();
		String nextLeft = Fst2Triphones.arrayObj.get(ids_left.get(0)).getIn();
		//assuming only one center triphone ***very imp
		//start center triphone will always be at index 1 as index 0 is for silence.
		String nextCenter = Fst2Triphones.arrayObj.get(1).getIn();
		for(int pos:ids_right){
			
			if(Fst2Triphones.arrayObj.get(pos).getIn() == null){
				start.add(null);
				break;
			}
			String a = nextCenter + "\t" + nextLeft + "\t" + Fst2Triphones.arrayObj.get(pos).getIn();
			start.add(a);
		}
		return start;
	}
	
	
	public ArrayList<Triphone> getNextState(Triphone t) throws IOException {
		ArrayList<Triphone> list = new ArrayList<>();
		FilesLoader fl = new FilesLoader();
		String word[];
		int states[];
		
		//splitting the string to check for silences
		word = t.getTriphone().split("\t");
		System.out.println("current triphone - " + t.getTriphone());
		if(word[0].equals("SIL")) {
			states = fl.getStates("SIL\t-\t-\t-");
		}
		else{
			states = fl.getStates(t.getTriphone());
		}
		
		
		if(t.getObsState() == states[2]){
			t.setObsState(states[3]);
			list.add(t);
			return list;
		}
		else if(t.getObsState() == states[3]){
			t.setObsState(states[4]);
			list.add(t);
			return list;
		}
		else if(t.getObsState() == states[4]){
			
			ArrayList<String> next = Triphone.getNextTriphone(t.getId(), t.getTriphone());
			String c;
			for(String s:next){
				
				Triphone tr = new Triphone();
				if(s == null){
					tr.setTriphone(null);
					break;
				}
				word = s.split("\t");
				c = s;
				//center 
				if(word[0].equals("SIL")) {
					 c = "SIL\t-\t-\t-";
				}
//				else if(word[1].equals("sil")){
//					c = word[0] + "\t" + "SIL" + "\t" + word[2] + "\t" + word[3];
//				}
//				else if(word[2].equals("sil")){
//					c = word[0] + "\t" + word[1] + "\t" + "SIL" + "\t" + word[3];
//				}
				System.out.println(c);
				int nextStates[] = fl.getStates(c);
				tr.setTriphone(s);
				tr.setObsState(nextStates[2]);
				tr.setTmatState(nextStates[1]);
				tr.setId(t.getId()%3);
				list.add(tr);
			}
			return list;
		}
		else{
			System.out.println("Error in observation ID of the triphone " + t.getTriphone());
			return list;
		}
		
	}
	
	public String getTriphone() {
		return triphone;
	}

	public void setTriphone(String triphone) {
		this.triphone = triphone;
	}

	public int getObsState() {
		return obsState;
	}

	public void setObsState(int obsState) {
		this.obsState = obsState;
	}

	public int getTmatState() {
		return tmatState;
	}

	public void setTmatState(int tmatState) {
		this.tmatState = tmatState;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public ArrayList<Triphone> getStartState(){
		
		//Dummy arraylist
		ArrayList<Triphone> dummyArraylist= new ArrayList<>();
		
		return  dummyArraylist;
	}
	

	public static void main(String[] args) throws IOException {
		
		//makes the required table for context dependency
		Fst2Triphones.makeTable();
		
		//load mdef file
		FilesLoader fl = new FilesLoader();
		fl.loadMdef("ComIrva_FA_CMN_s1000_g16.cd_cont_1000\\mdef_tab");
		//----------//
		
		Triphone tr = new Triphone();
		String triphone = "l	SIL	aa	b";
		//String triphone1 = "SIL	-	-	-";
		int states[] = fl.getStates(triphone);
		int id = 9;
		tr.setId(id);
		tr.setTriphone(triphone);
		tr.setObsState(states[2]);
		tr.setTmatState(states[1]);
		System.out.println("function called");
		
		for(int i = 0; i < 6; i++){
			ArrayList<Triphone> list = tr.getNextState(tr);
			
			for(Triphone t : list){
				System.out.println("next triphone -->" + t.getTriphone());
				System.out.println("Obs state--> " + t.getObsState() + " //tmat state--> " + t.getTmatState());
			}
			tr = list.get(0);
			System.out.println("------------------------------------");
		}
//		
		
//		String abc = "j-	nj	SIL	e";
//		ArrayList<String> next = Triphone.getNextTriphone(id, abc);
//		System.out.println(next.get(0));
	}
	
}

