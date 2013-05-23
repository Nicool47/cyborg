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

public class FstGrammar {
	
	static int lexiconPos = 2;
	static int symsPos = 0;
	static BufferedWriter wordGraph = null;
	static BufferedWriter lexicon = null;
	static BufferedWriter syms = null;
	static Map<String, String> hashMapSyms;
	
	/**
	 * This function creates three text files in the current directory 
	 * <b>wordGraph.stxt</b>, <b>lexicon.stxt</b> and <b>lexicon.syms</b> from the transcription and the 
	 * dictionary.<br>
	 * The function will run provided the dictionary file has been loaded in the hashmap.<br>
	 * The format of the file is in accordance with the openFST open source project.</br>
	 * For more details of the openFST library, visit the site 
	 * <a href = "http://www.openfst.org/twiki/bin/view/FST/WebHome"> OpenFST </a> <br>
	 * 
	 * 
	 * @param transFile
	 * @throws IOException
	 */
	public void getFstTxtFiles(String transFile) throws IOException {
		
		FstGrammar fg = new FstGrammar();
		hashMapSyms = new HashMap<>();
		BufferedReader br = null;
		String line,phoneme;
		try{
			br = new BufferedReader(new FileReader(transFile));
			wordGraph = new BufferedWriter(new FileWriter("fst//wordGraph.stxt"));
			lexicon = new BufferedWriter(new FileWriter("fst//lexicon.stxt"));
			syms = new BufferedWriter(new FileWriter("fst//lexicon.syms"));
			if((line = br.readLine()) == null) {
				System.out.println("empty transcription file");
				System.exit(0);
			}
			
			//write the silences and epsilon in the symbol file
			fg.printSilences();
			
			String words[] = string2array(line);	
			int totalWords = words.length;
			
			int pos = 0;
			printWordGraph(pos, "<s>");
			pos++;
			for (int i = 0; i < totalWords; i++){
				int no = 2;
				printWordGraph(pos, words[i]);
				if ((phoneme = FilesLoader.getPhonemes(words[i])) == null){
					System.out.println(words[i]
							+ " not present is the dictionary");
					System.exit(0);
				}
				fg.createHash(words[i],phoneme);
				fg.writeLexicon(phoneme,words[i]);
				//alternate pronunciations are marked as (2), (3), ...
				//e.g. hello(2), hello(3),..
				String altPronunciation = words[i] + "(" + no +")"; 
				while((phoneme = FilesLoader.getPhonemes(altPronunciation)) != null ){
					//printing the alternate pronunciation
					printWordGraph(pos, altPronunciation);
					no++;
					fg.createHash(altPronunciation,phoneme);
					fg.writeLexicon(phoneme,altPronunciation);
					altPronunciation = words[i] + "(" + no +")";
				}
				pos++;
				if(i < (totalWords-1) ){
					//printing intermediate silence/ epsilon
					printWordGraph(pos, "<eps>");
					printWordGraph(pos, "sil");
					pos++;	
				}
				
			}

			fg.printEndSilences(pos);
			//System.out.println("total time = " + (System.currentTimeMillis() - time1) + "ms");
		}
		finally{
			if(br != null)br.close();
			if(wordGraph != null) wordGraph.close();
			if(lexicon != null) lexicon.close();
			if(syms != null) syms.close();
			}
		
	}
	
	void printEndSilences(int pos) throws IOException{
		//printing the ending silences in the word graph
		printWordGraph(pos, "</s>");
		pos++;
		//printing the silences in the lexicon
		printLexicon(0, 1, "SIL", "<s>");
		printLexicon(0,1, "SIL", "</s>");
		printLexicon(0,1, "SIL", "sil");
		
		//printing the end position 
		lexicon.write(Integer.toString(1));
		wordGraph.write(Integer.toString(pos));
	}
	
	static String[] string2array(String s){
		//trim the starting the ending spaces.
		s = s.trim();
		// trim intermediate spaces to one space
		s = s.replaceAll("\\s+", " ");
		String words[] = s.split(" ");
		return words;
	}
		
	static void printWordGraph(int pos, String c) throws IOException{
		wordGraph.write(pos + " " + (pos+1) + " " + c + " " + c + "\n");
		//System.out.print(pos + " " + nextPos + " " + c + " " + d + "\n");	
	}
	
	static void printLexicon(int pos,int nextPos, String c, String d) throws IOException{
		lexicon.write(pos + " " + nextPos + " " + c + " " + d + "\n");
		//System.out.print(pos + " " + nextPos + " " + c + " " + d + "\n");	
	}
	
	static void printSyms(String c) throws IOException{
		syms.write(c + " " + symsPos + "\n");
		//System.out.print(symsPos + " " +  c + "\n");	
		symsPos++;
	}
	
	void writeLexicon(String phoneme, String word) throws IOException{
		String monophone[] = string2array(phoneme);
		int length = monophone.length;
		printLexicon(0, lexiconPos, monophone[0], word);

		for(int i = 1; i < length - 1; i++){
			printLexicon(lexiconPos, lexiconPos+1, monophone[i], "<eps>");
			lexiconPos++;
		}
		printLexicon(lexiconPos, 1, monophone[length-1], "<eps>");	
		lexiconPos++;
	}
	
	void printSilences() throws IOException {
		printSyms("<eps>");
		printSyms("<s>");
		printSyms("</s>");
		printSyms("sil");
		printSyms("SIL");
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
	
}