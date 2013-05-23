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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.iitb.cyborg.Fileloader.FilesLoader;

public class MakeSymbol {
	static BufferedWriter syms = null;
	static int symsPos = 0;
	static Map<String, String> hashMapSyms;
	
	public static void main(String[] args) throws IOException {
		long time1 = System.currentTimeMillis();
		BufferedReader br = null;
		String line,phoneme;
		MakeSymbol ms = new MakeSymbol();
		hashMapSyms = new HashMap<>();
		
		try{
			br = new BufferedReader(new FileReader("resources/Transcription.trans"));
			syms = new BufferedWriter(new FileWriter("lexicon.syms"));
			if((line = br.readLine()) == null) {
				System.out.println("empty transcription file");
				System.exit(0);
			}
			
			String words[] = string2array(line);	
			int totalWords = words.length;
			
			//-------- stores the dictionary file in hashmap ---------//
			FilesLoader filesLoader = new FilesLoader();
			filesLoader.loadDict("resources/Dictionary.dic");
			
			//hashMapSyms.put("<eps>", "0");
			ms.printSilences();
					
			for(int i = 0; i < totalWords; i++){
				int no = 2;
				if ((phoneme = FilesLoader.getPhonemes(words[i])) == null){
					System.out.println(words[i]
							+ " not present is the dictionary");
					System.exit(0);
				}
				ms.createHash(words[i],phoneme);
				String altPronunciation = words[i] + "(" + no +")"; 
				while((phoneme = FilesLoader.getPhonemes(altPronunciation)) != null ){
					//printing the alternate pronunciation
					no++;
					ms.createHash(altPronunciation,phoneme);
					altPronunciation = words[i] + "(" + no +")";
				}
			}
			
			//ms.printSyms();
			System.out.println(System.currentTimeMillis() - time1);
			
		}
		finally{
			if(br != null)br.close();
		}
	}
	
	void printSilences() throws IOException {
		printSyms("<eps>");
		printSyms("<s>");
		printSyms("</s>");
		printSyms("SIL");
		printSyms("sil");
	}

	static void printSyms(String c) throws IOException{
		//syms.write(symsPos + " " +  c + "\n");
		System.out.print(symsPos + " " +  c + "\n");	
		symsPos++;
	}
	
	static String[] string2array(String s){
		//trim the starting the ending spaces.
		s = s.trim();
		// trim intermediate spaces to one space
		s = s.replaceAll("\\s+", " ");
		String words[] = s.split(" ");
		return words;
	}
	
	void createHash(String word, String phoneme) throws IOException{
		
		if(getSymbol(word) == null) {
			putSymbol(word);
			printSyms(word);
		}
		String monophone[] = string2array(phoneme);
		for(String s:monophone){
			if(getSymbol(s) == null) {
				putSymbol(s);
				printSyms(s);
			}
		}
	}
	
	static String getSymbol(String word){
		return hashMapSyms.get(word);
	}
	
	static void putSymbol(String word){
		hashMapSyms.put(word, "1");
	}
	
//	void printSyms(){
//		Iterator<E>
//	}
}

