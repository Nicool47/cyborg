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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Fst2Triphones {
	int start;
	int end;
	String in;
	String op;
	static int finalState;
	static ArrayList<Fst2Triphones> arrayObj;
	static ArrayList<Table> arraytable2;
public static void makeTable() throws IOException {
	
	arrayObj = new ArrayList<>();
	BufferedReader br = null;
	String line = null;
	String words[];
	try{
		br = new BufferedReader(new FileReader("fst//final_dir.txt"));
		while((line = br.readLine()) != null){
			words = line.split("\t");
			Fst2Triphones obj = new Fst2Triphones();
			if(words.length > 1){
				obj.setStart(Integer.parseInt(words[0]));
				obj.setEnd(Integer.parseInt(words[1]));
				obj.setIn(words[2]);
				obj.setOp(words[3]);
			}else{
				obj.setStart(Integer.parseInt(words[0]));
				//for the end of state
				finalState = Integer.parseInt(words[0]);
				obj.setEnd(-1);
			}
			
			arrayObj.add(obj);
		}
			
	}catch(Exception e){
		e.printStackTrace();
	}finally{
		if(br != null)br.close();
	}
	
	// creating unique set of symbols
	ArrayList<Integer> states = new ArrayList<>();
	System.out.println("unique states-->");
	for(Fst2Triphones o: arrayObj){
		if(!(states.contains(o.getStart()))){
			states.add(o.getStart());
			System.out.println(o.getStart());
		}
	}
	System.out.println("end states--->");
	for(Fst2Triphones ft : arrayObj){
		System.out.println(ft.getEnd());
	}
	System.out.println("-------------");
	//generating table1
	//size of arraytable1 will be the size of unique states.
	ArrayList<Table> arraytable1 = new ArrayList<>();

	for(int i:states){
		Table t1 = new Table();
		//int count = 0;
		//optimize it later w.r.t starting point
		//set j=i or something
		for(int j = 0 ; j < arrayObj.size(); j++){
			//max count = 2 because only silence is considered 
			//if(count > 2)break;
			if(i == arrayObj.get(j).getStart()){
				//count++;
				t1.setLeft(j);
			}
			
			if(i == arrayObj.get(j).getEnd()){
				//count++;
				t1.setRight(j);
			}
		}
		arraytable1.add(t1);
	}
	
	//display table1
	System.out.println("size of array table1 is " + arraytable1.size());
	for(int i = 0; i < arraytable1.size(); i++){
		System.out.print(states.get(i)+ " |");
		for(int j = 0 ; j < arraytable1.get(i).getLeft().size(); j++){
			System.out.print(arraytable1.get(i).getLeft().get(j) + " ");;
		}
		System.out.print("|");
		for(int j = 0 ; j < arraytable1.get(i).getRight().size(); j++){
			System.out.print(arraytable1.get(i).getRight().get(j) + " ");;
		}
		System.out.println("");
	}
	System.out.println("end of arraytable1");
	//arraytable1.clear();
	
	
	//generate table2
	arraytable2 = new ArrayList<>();
	for(Fst2Triphones  ft : arrayObj){
		Table t2 = new Table();
		t2.setLeftarray(arraytable1.get(ft.getStart()).getRight());
		if(ft.getEnd() != -1){
			t2.setRightarray(arraytable1.get(ft.getEnd()).getLeft());
		}
		
		arraytable2.add(t2);
	}
	
	//display table2
	System.out.println("size of array table2 is " + arraytable2.size());
	for(int i = 0; i < arraytable2.size(); i++){
		System.out.print(i + " |");
		for(int j = 0 ; j < arraytable2.get(i).getLeft().size(); j++){
			System.out.print(arraytable2.get(i).getLeft().get(j) + " ");;
		}
		System.out.print("|");
		for(int j = 0 ; j < arraytable2.get(i).getRight().size(); j++){
			System.out.print(arraytable2.get(i).getRight().get(j) + " ");;
		}
		System.out.println("");
	}
	
	System.out.println("end of arraytable2");
	System.out.println("--------xxxxx-----------------");

//	int pos = 6;
//	System.out.println("centre element-->");
//	System.out.println(arrayObj.get(pos).getIn());
//	System.out.println("left elements-->");
//	for(int j = 0 ; j < arraytable2.get(pos).getLeft().size(); j++){
//		System.out.print(arrayObj.get(arraytable2.get(pos).getLeft().get(j)).getIn() + " ");;
//	}
//	System.out.println("");
//	System.out.println("right elements-->");
//	for(int j = 0 ; j < arraytable2.get(pos).getRight().size(); j++){
//		System.out.print(arrayObj.get(arraytable2.get(pos).getRight().get(j)).getIn() + " ");;
//	}
	

}
public int getStart() {
	return start;
}
public void setStart(int start) {
	this.start = start;
}
public int getEnd() {
	return end;
}
public void setEnd(int end) {
	this.end = end;
}
public String getIn() {
	return in;
}
public void setIn(String in) {
	this.in = in;
}
public String getOp() {
	return op;
}
public void setOp(String op) {
	this.op = op;
}
}

class Table{
	ArrayList<Integer> left;
	ArrayList<Integer> right;
	
	Table(){
		left = new ArrayList<>();
		right = new ArrayList<>();
	}
	
	public void setLeft(int a){
		this.left.add(a);
	}
	
	public void setLeftarray(ArrayList<Integer> l){
		this.left = l;
	}
	
	public void setRightarray(ArrayList<Integer> r){
		this.right = r;
	}
	
	public void setRight(int a){
		this.right.add(a);
	}
	
	public ArrayList<Integer> getLeft(){
		return left;
	}
	
	public ArrayList<Integer> getRight(){
		return right;
	}
	
	
}
