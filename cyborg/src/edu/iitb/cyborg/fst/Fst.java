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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.iitb.cyborg.Fileloader.FilesLoader;

public class Fst {
	/**
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
public static void main(String[] args) throws IOException, InterruptedException {
		
		//-------- stores the dictionary file in hashmap ---------//
		FilesLoader filesLoader = new FilesLoader();
		filesLoader.loadDict("resources/Dictionary.dic");
		
		File file = new File("fst");
		if(!file.exists()){
			if(file.mkdir()) System.out.println("fst directory created");
		}else{System.out.println("fst directory exists.");}
		
		long time1 = System.currentTimeMillis();
		FstGrammar fg = new FstGrammar();
		fg.getFstTxtFiles("resources/Transcription.trans");
		
		//creating the fst file for wordGraph..
		ProcessBuilder builder = new ProcessBuilder("cmd.exe","/c","fstcompile " +
				"--isymbols=fst\\lexicon.syms --osymbols=fst\\lexicon.syms fst\\wordGraph.stxt" +
				" | fstrmepsilon | fstarcsort > fst\\wordGraph_dir.fst");
		builder.redirectErrorStream(true);
		Process p = builder.start();
		int status = p.waitFor();
		if(status == 1){
			//reading the command lines
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        String line;
	        while (true) {
	            line = br.readLine();
	            if (line == null) { 
	            	
	            	break; }
	            System.out.println(line);
	        }
			System.out.println("ERROR: fst file for wordGraph not created..!!!");
			System.exit(0);
		}       
        System.out.println("wordGraph_dir.fst file created sucessfully :)\n");
		
      //creating the fst file for lexicon..
      		builder = new ProcessBuilder("cmd.exe","/c","fstcompile --isymbols=fst\\lexicon.syms --osymbols=fst\\lexicon.syms " +
      				"fst\\lexicon.stxt | fstclosure | fstarcsort > fst\\lexicon_dir.fst");
      		builder.redirectErrorStream(true);
      		p = builder.start();
      		status = p.waitFor();
      		if(status == 1){
      			//reading the command lines
      			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      	        String line;
      	        while (true) {
      	            line = br.readLine();
      	            if (line == null) { 
      	            	
      	            	break; }
      	            System.out.println(line);
      	        }
      			System.out.println("ERROR: fst file for lexicon not created..!!!");
      			System.exit(0);
      		}       
              System.out.println("lexicon_dir.fst file created sucessfully :)\n");
              
              
            //composing the fst file for lexicon..
        		builder = new ProcessBuilder("cmd.exe","/c","fstcompose fst\\lexicon_dir.fst fst\\wordGraph_dir.fst | " +
        				"fstrmepsilon | fstdeterminize | fstminimize > fst\\final_dir.fst");
        		builder.redirectErrorStream(true);
        		p = builder.start();
        		status = p.waitFor();
        		if(status == 1){
        			//reading the command lines
        			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        	        String line;
        	        while (true) {
        	            line = br.readLine();
        	            if (line == null) { 
        	            	
        	            	break; }
        	            System.out.println(line);
        	        }
        			System.out.println("ERROR: composed final fst not created..!!!");
        			System.exit(0);
        		}       
                System.out.println("final_dir.fst file created sucessfully :)\n");
                
           //print the final  fst file
                builder = new ProcessBuilder("cmd.exe","/c","fstprint --isymbols=fst\\lexicon.syms " +
                		"--osymbols=fst\\lexicon.syms fst\\final_dir.fst fst\\final_dir.txt");
        		builder.redirectErrorStream(true);
        		p = builder.start();
        		p.waitFor();
        		System.out.println("final_dir.txt file created sucessfully :)");
        		 System.out.println(System.currentTimeMillis() - time1);
	}
}

