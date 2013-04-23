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

package edu.iitb.cyborg.aligner;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;


public class FilesLoader {
	
	private static HashMap<String, String> hashMapMdef;
	private static HashMap<String, String> hashMapDict;
	
	public static String triPhones[];
	public static String triPhoneList[];
	
	static float MEAN[][][];
	static float VAR[][][];
	static float MIXWT[][];
	static float TMAT[][][];
	static float FEAT[][];
	static int GAUSSIAN;
	static int SENONES;
	static int CI_STATES;

	/**
     * Reads a 'mdef' file and loads it into HashMap. Each value is considered to be seperated by tab ('\t') <br> <br>
     * 
	 * <b>Line Format</b> : #base	lft	rt	position	attrib	tmat	stateId's	LnEnd(N)<br>
	 * <b>eg</b>: a	SIL	d'	b	n/a	11	265	296	324	N <br> <br>  
     *
     * <b>Hash Entry Example:</b><br>
     * <blockquote>
     * <b>Key</b>   : a	SIL	d'	b <br>
     * <b>Value</b> : n/a	11	265	296	324	N <br> 
     * </blockquote>
     *  
     * @return     void
     *
     * @exception  IOException  If an I/O error occurs
     * 
     */
	
	public void loadMdef(String pathToMdef) throws IOException {
		
		hashMapMdef = new HashMap<String, String>();
		File fileMdef = new File(pathToMdef);
		BufferedReader brMdef = new BufferedReader(new FileReader(fileMdef));
		brMdef.readLine();
		
		// Line read from file 
		String templineMain;		
		// Line for temporary processing
		String tempLine;		
		// Extracted triPhone portion from line
		String triPhone;		
		// Extracted states portion from line
		String states;
		
		Boolean startProcessingLines = false;
		
		try {		
			while((templineMain = brMdef.readLine()) != null) {			
				if(startProcessingLines) {					
					tempLine = templineMain.substring(templineMain.indexOf("\t")+1);
					tempLine = tempLine.substring(tempLine.indexOf("\t")+1);
					tempLine = tempLine.substring(tempLine.indexOf("\t")+1);
					tempLine = tempLine.substring(tempLine.indexOf("\t")+1);
					
					triPhone = templineMain.substring(0,templineMain.indexOf(tempLine)-1);
					states = tempLine;
					
					hashMapMdef.put(triPhone, states);
					//System.out.println(templineMain.substring(0,templineMain.indexOf(tempLine)-1)+"\tSeperator\t"+tempLine);
				}				
													
				if(templineMain.startsWith("#base"))
					startProcessingLines = true;
			}
		} catch(Exception e) {
			System.out.println(e.toString());
		} finally {
			if(brMdef != null){
				System.out.println("Closing brMdef...");
				brMdef.close();
			}
				
		}
	
	}
	
	/**
	 * Reads a Dictionary file and stores it into HashMap. Word and phoneme sequence assumed to be separated by tab (\t).<br>
	 * 
	 * <b>Line format</b> : word	phonemes
	 * <b>eg</b>: ahamadanagara 	a h a m a d n a g a r
	 * 
	 * <b>Hash Entry example</b>
	 * <blockquote>
	 * <b>Key:</b> ahamadanagara<br>
	 * <b>Value:</b> a h a m a d n a g a r<br>
	 * </blockquote>
	 * 
	 * @param pathToDict
	 * @throws IOException
	 */
	
	public void loadDict(String pathToDict) throws IOException	{
		
		hashMapDict = new HashMap<String, String>();
		File fileDict = new File(pathToDict);
		BufferedReader brDict = new BufferedReader(new FileReader(fileDict));
			
		// Line read from file
		String tempLineMain = null;		
		// Extracted word from line
		String word = null;		
		// Extracted phonemes from line
		String phonems = null;
		
		try {
			while((tempLineMain = brDict.readLine()) != null) {			
				word = tempLineMain.substring(0,tempLineMain.indexOf("\t")).trim();
				phonems = tempLineMain.substring(tempLineMain.indexOf("\t")+1, tempLineMain.length());
				hashMapDict.put(word, phonems);
				//System.out.println(word+"\tSeperator\t"+phonems);
			}
			
		} catch(StringIndexOutOfBoundsException e){
			System.out.println("Dictionary content is in bad format!!");
		} catch(Exception e){			
			System.out.println("Something went wrong with loadind Dictionary : "+e.toString()+tempLineMain+word+phonems);
		} finally {			
			if(brDict != null) {				
				System.out.println("Closing brDict...");
				brDict.close();				
			}				
		}
	}
	
	//-------------------//
	
	/**
	 * This function takes the binary mean file given as an
	 * argument and stores it in a static 3-d array 'MEAN[][][]' for fast computation.
	 * 
	 * The format of the mean array is as follows:<br>
	 * <li>First dimension - senoneID </li>
	 * <li>Second dimension - GAUSSIAN </li>
	 * <li>Third dimension - feature number </li> <br>
	 * e.g. MEAN[500][4][38] refers to 500th senone, 4th gaussian and
	 * 38th feature.<br>
	 * SenoneID, gaussian and feature vectors starts from index <b>0</b> 
	 * and goes on till the number of senones, gaussians and 
	 * feature vectors <b> minus 1.</b>
	 * @param path path of the binary file
	 * @throws IOException
	 */
	public void mean_read(String path) throws IOException {
		
		FileInputStream is = null;
		DataInputStream dis = null;
		try{
			is = new FileInputStream(path);
			dis = new DataInputStream(new BufferedInputStream(is));
			SENONES = (int)dis.readFloat();
			GAUSSIAN = (int)dis.readFloat();
			MEAN = new float[SENONES][GAUSSIAN][39];
			for(int i = 0; i < SENONES; i++)
				for(int j = 0; j < GAUSSIAN; j++)
					for(int k = 0 ; k < 39; k++)
						MEAN[i][j][k] = dis.readFloat();
			
		}catch(Exception e){
			System.out.println(path + " file missing");
			e.printStackTrace();	
		}
		finally{
			if(is != null)is.close();
			if(dis != null)dis.close();
			}
	}
		
	/**
	 * * This function takes the binary variance file given as an
	 * argument and stores it in a static 3-d array 'VAR[][][]' for
	 *  fast computation.
	 * 
	 * The format of the variance array is as follows:<br>
	 * <li>First dimension - senoneID </li>
	 * <li>Second dimension - gaussian </li>
	 * <li>Third dimension - feature number </li> <br>
	 * e.g. VAR[500][4][38] refers to 500th state, 4th gaussian and
	 * 38th feature.<br>
	 * senoneID, gaussian and feature vectors starts from index <b>0</b> 
	 * and goes on till the number of senones, gaussians and 
	 * feature vectors <b> minus 1.</b>
	 * @param path path of the binary file
	 * @throws IOException
	 */
	public void var_read(String path) throws IOException {
		FileInputStream is = null;
		DataInputStream dis = null;
		try{
			is = new FileInputStream(path);
			dis = new DataInputStream(new BufferedInputStream(is));
			SENONES = (int)dis.readFloat();
			GAUSSIAN = (int)dis.readFloat();
			VAR = new float[SENONES][GAUSSIAN][39];
			for(int i = 0; i < SENONES; i++)
				for(int j = 0; j < GAUSSIAN; j++)
					for(int k = 0 ; k < 39; k++)
						VAR[i][j][k] = dis.readFloat();
			
		}catch(Exception e){
			System.out.println(path + " file missing");
			e.printStackTrace();
		}
		finally{
			if(is != null)is.close();
			if(dis != null)dis.close();
			}
	}

	/**
	 * This function takes the binary transition matrix file given 
	 * as an argument and stores it in a static 3-d array 'TMAT[][][]' 
	 * for fast computation.
	 * 
	 * The format of the tmat array is as follows:<br>
	 * <li>First dimension - phone ID </li>
	 * <li>Second dimension - transition from </li>
	 * <li>Third dimension - transition to.</li> <br>
	 * e.g. TMAT[60][1][2] refers to 60th CI phone with a transition
	 * from first to  second state.<br>
	 * The assumption is that the HMM cant skip state. <br>
	 * So TMAT[60][1][3], TMAT[60][0][2] will be zero and so on.<br>
	 * State, transition from and transition to starts from index <b>0</b> 
	 * and goes on till the number of CI phones and the number of 
	 * states per senone/CI phone.
	 * @param path path of the binary file
	 * @throws IOException
	 */
	public void tmat_read(String path) throws IOException {
		FileInputStream is = null;
		DataInputStream dis = null;
		try{
			is = new FileInputStream(path);
			dis = new DataInputStream(new BufferedInputStream(is));
			CI_STATES = (int)dis.readFloat();
			int states_per_triphone = (int)dis.readFloat();
			TMAT = new float[CI_STATES][states_per_triphone][states_per_triphone];
			for(int i = 0; i < CI_STATES; i++)
				{
				for(int j = 0; j < states_per_triphone - 1 ; j++){
					for(int k = j ; k <= j+1; k++)
						TMAT[i][j][k] = dis.readFloat();
					}
				}
		}catch(Exception e){
			System.out.println(path + " file missing");
			e.printStackTrace();
		}
		finally{
			if(is != null)is.close();
			if(dis != null)dis.close();
			}
	}
	
	/**
	 * This function takes the binary mixture weight file given 
	 * as an argument and stores it in a static 2-d array 'MIXWT[][]' 
	 * for fast computation.
	 * 
	 * The format of the MIXWT array is as follows:<br>
	 * <li>First dimension - senoneID;</li>
	 * <li>Second dimension - Gaussian mixture number; </li><br>
	 * e.g. MIXWT[400][8] refers to 400th senone and 8th gaussian.<br>
	 * SenoneID and gaussian mix no. starts from index <b>0</b> 
	 * and goes on till the number of senones and gaussians
	 * minus 1.</b>
	 * @param path
	 * @throws IOException
	 */
	public void mixWt_read(String path) throws IOException {
		FileInputStream is = null;
		DataInputStream dis = null;
		try{
			is = new FileInputStream(path);
			dis = new DataInputStream(new BufferedInputStream(is));
			SENONES = (int)dis.readFloat();
			GAUSSIAN = (int)dis.readFloat();
			MIXWT = new float[SENONES][GAUSSIAN];
			for(int i = 0; i < SENONES; i++)
				for(int j = 0; j < GAUSSIAN; j++)
						MIXWT[i][j] = dis.readFloat();
			
		}catch(Exception e){
			System.out.println(path + " file missing");
			e.printStackTrace();
		}
		finally{
			if(is != null)is.close();
			if(dis != null)dis.close();
			}
	}
	
	/**
	 * This function read all the binary files (i.e mean,
	 * variance, transition mat and mixture weights) stored in the 
	 * directory 'dir' given as an argument. All these files have
	 * to be present in the directory.
	 * @param dir
	 * @throws IOException
	 */
	public void initialize(String dir) throws IOException {
		this.mean_read(dir + "/mean_bin");
		this.var_read(dir + "/variance_bin");
		this.tmat_read(dir + "/tmat_bin");
		this.mixWt_read(dir + "/mixWt_bin");
	}
	
	/**
	 * This function takes the path of the MFC file and
	 * stores the features in a 2-d static array FEAT[][].<br>
	 * Format of the array is as follows:
	 * <li> First dimension - time frame</li>
	 * <li> Second dimension - feature number</li> <br>
	 * Total number of features are 39.<br>
	 * e.g. FEAT[5][30] refers to the 6th time frame and 31st feature. <br>
	 * Remember the index for both the time frame and the feature number starts from 0.
	 * @param path
	 * @throws IOException
	 */
	public void readFeat(String path) throws IOException {
		
		int cmn = 1; //for cepstral mean normalization
		FileInputStream is = null;
		DataInputStream dis = null;
		try{
			is = new FileInputStream(path);
			dis = new DataInputStream( new BufferedInputStream(is));
		    byte[] buffer = new byte[4];
		    dis.read(buffer);  // Bytes are read into buffer
//		    if (bytesRead != 4) {
//		      throw new IOException("Unexpected End of Stream");
//		    }		    
		    int frames = 
		        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt()/13;
		    
			float[][] feat_s = new float[frames][13];
			for(int i = 0; i < frames; i++)
				for(int j = 0; j < 13; j++){
					dis.read(buffer);
			    	feat_s[i][j] =  ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getFloat();
				}
			
			if(cmn == 1){
				Cmn c = new Cmn();
				feat_s = c.cepstralMeanNormalize(feat_s);
			}
			
			// Computing the delta features
			Delta d = new Delta();
			FEAT = d.computeDeltaFeatures(feat_s);
			
		}catch(Exception e){
			System.out.println(path + " file may be missing or problems with the feature file");
			e.printStackTrace();
			System.exit(0);
		}
		finally{
			if(is != null)is.close();
			if(dis != null)dis.close();
			}
		
			
		}
	
	//------------------//
	
	/**
	 *  
	 * @param String of triPhones where each phone is separated by tab ('\t') <br> eg : a	SIL	d'	b
	 * @return Integer array containing corresponding attrib, tmat and states of triPhone <br>
	 * eg: <br>
	 * states[0] = 1    // attrib[0: filler and 1: n/a] <br>
	 * states[1] = 11   // tmat <br>
	 * states[2] = 265  // state no of 'a' <br>
	 * states[3] = 296  // state no of 'SIL' <br>
	 * states[4] = 324  // state no of 'b' <br>
	 * 
	 */

	public int[] getStates(String triPhones) {
		
		int states[] = new int[5];
		String statesString[] = new String[6];
		//System.out.println(hashMapMdef.get(triPhones));
		
		try {
			statesString = hashMapMdef.get(triPhones).split("\t");
			
			states[0] = statesString[0].equals("filler") ? 0 : 1 ;
			states[1] = Integer.parseInt(statesString[1]);
			states[2] = Integer.parseInt(statesString[2]);
			states[3] = Integer.parseInt(statesString[3]);
			states[4] = Integer.parseInt(statesString[4]);
		}
		catch(NullPointerException e) {
				System.out.println("'" + triPhones.replace("\t", " ")+ "'" + " : TriPhone doesn't exist in 'mdef' file.");
		}
		
		return states;
	}
	
	/**
	 * 
	 * @param word <br> <b>eg:</b> ahamadanagara 
	 * @return <b>String</b> containing phoneme sequence of the word <br>
	 *         <b>eg:</b> a h a m a d n a g a r
	 */
	
	public static String getPhonemes(String word) {
		
		return hashMapDict.get(word);
	}

	/**
	 * 
	 * @param transcription <br> <b>eg:</b> laala deshii makaa
	 * @return <b>String</b> containing phoneme sequence of the transcription with initial and end SIL added <br>
	 *         <b>eg:</b> SIL l aa l d e sh ii m a k aa SIL
	 */
	public static String getPhonemsOfTrans(String transcription) {
		
		String words[] = transcription.split(" ");
		String phonems = "";
		
		System.out.println("\nActual transcription  : " + transcription);
		for(int Index = 0; Index < words.length; Index++) {
			if(getPhonemes(words[Index]) == null){
				System.out.println("Word "+words[Index]+" doesn't exist in Dictionary!!");
				System.exit(0);
			}			
			phonems = phonems + " "+getPhonemes(words[Index])+" ";
			//System.out.println(getPhonemes(words[Index]));
		}
		
		return "SIL "+phonems.trim()+" SIL";
		//return "SIL "+phonems.replaceAll("\\s+", " ").trim()+" SIL";
	}
	
	/**
	 * 
	 * @param transcription
	 * @return <b>Integer</b> array containing states of each triPhone of transcription
	 */
	
	public int[][] getStatesOfTrans(String transcription) {
		 
		String phonems = FilesLoader.getPhonemsOfTrans(transcription);
		int states[][];		
		
	
		System.out.println("Phonem sequence       : "+phonems);
		
		String phonemsArray[]= phonems.split(" ");
		System.out.println("No of phones in the array :"+ phonems.replaceAll("\\s+", " ").trim().split(" ").length);
		
//		for(String s:phonemsArray)
//			System.out.println(s);
		
		int noOfTriPhones =  phonems.replaceAll("\\s+", " ").trim().split(" ").length - 2;
		System.out.println("No of tri Phones      : "+noOfTriPhones);
		int noOfPhones = noOfTriPhones + 2;
		System.out.println("No of phones in Trans : " + noOfPhones);
		System.out.println();
		
		states = new int[3][3*(noOfTriPhones+2)]; // Extra 2 is for beginning and ending mono silence
		triPhones = new String[noOfTriPhones+2];
		
		triPhoneList = new String[noOfTriPhones+2];
		
		// Separate phonems of words with NIL
		for(int index = 0; index < phonemsArray.length; index++){
			if(phonemsArray[index].equals("")){
				phonemsArray[index] = "NIL";
			}
		}
		
//		for(String s:phonemsArray)
//			System.out.println("System "+s);
		
		//  0 1 2 3  4   5 6 7  8  9   10  11 12 13 14  15
		//SIL d NIL o n sh NIL e b aa  w  NIL   i  s  t  r  SIL

	    int indexT = 1;
	    int indexI = 1;
	    
		triPhoneList[0] =  "SIL	-	-	-";
		
		if(phonemsArray[indexI+1].equals("NIL")){
			triPhoneList[indexT] = phonemsArray[indexI]+"\t"+phonemsArray[indexI - 1]+"\t"+phonemsArray[indexI + 2]+"\t"+"s";
			indexT++;
		}
		else{
			triPhoneList[indexT] = phonemsArray[indexI]+"\t"+phonemsArray[indexI - 1]+"\t"+phonemsArray[indexI + 1]+"\t"+"b";
			indexT++;
		}
			
					

			for(indexI = 2; indexI < phonemsArray.length -2; indexI++){
				if(phonemsArray[indexI-1].equals("NIL") && phonemsArray[indexI+1].equals("NIL")){
					triPhoneList[indexT] = phonemsArray[indexI]+"\t"+phonemsArray[indexI - 2]+"\t"+phonemsArray[indexI + 2]+"\t"+"s";  
					indexT++;
				}
				else if(phonemsArray[indexI].equals("NIL")){
					continue;
				}
				else if(phonemsArray[indexI - 1].equals("NIL")){
					triPhoneList[indexT] = phonemsArray[indexI]+"\t"+phonemsArray[indexI - 2]+"\t"+phonemsArray[indexI + 1]+"\t"+"b";  
					indexT++;
				}
				else if(phonemsArray[indexI + 1].equals("NIL")){
					triPhoneList[indexT] = phonemsArray[indexI]+"\t"+phonemsArray[indexI - 1]+"\t"+phonemsArray[indexI + 2]+"\t"+"e";  
					indexT++;
				}
				else{
					triPhoneList[indexT] = phonemsArray[indexI]+"\t"+phonemsArray[indexI - 1]+"\t"+phonemsArray[indexI + 1]+"\t"+"i";  
					indexT++;
				}
				
			}
			
							
			if(phonemsArray[indexI -1].equals("NIL")){
				triPhoneList[indexT] = phonemsArray[indexI]+"\t"+phonemsArray[indexI - 2]+"\t"+phonemsArray[indexI + 1]+" s";
				indexT++;
			}
			else{
				triPhoneList[indexT] = phonemsArray[indexI]+"\t"+phonemsArray[indexI - 1]+"\t"+phonemsArray[indexI + 1]+"\t"+"e";
				indexT++;
			}			

	
		triPhoneList[indexT] = phonemsArray[indexI]+"\t"+phonemsArray[indexI - 1]+"\t"+phonemsArray[indexI + 1]+"\t"+"e";
		triPhoneList[noOfTriPhones+1] =  "SIL	-	-	-";
		
		//////////////////////////////
		
//		for(String tiPhn: triPhoneList)
//			System.out.println("TriPhone: "+tiPhn);
		
		int statesTemp[] = new int[5];
		
		for(int indexFirst = 0; indexFirst < triPhoneList.length; indexFirst++){

			System.out.print("Tri Phone : "+triPhoneList[indexFirst]);
			statesTemp = getStates(triPhoneList[indexFirst]);
			System.out.println("\tStates : "+statesTemp[0]+" "+statesTemp[1]+" "+statesTemp[2]+" "+statesTemp[3]+" "+statesTemp[4]);
			
			states[0][((indexFirst)*3)] = statesTemp[2]; 
		    states[0][((indexFirst)*3)+1] = statesTemp[3];
		    states[0][((indexFirst)*3)+2] = statesTemp[4];
		    
		    states[1][((indexFirst)*3)] = statesTemp[1]; 
		    states[1][((indexFirst)*3)+1] = statesTemp[1];
		    states[1][((indexFirst)*3)+2] = statesTemp[1];
		    
		    states[2][((indexFirst)*3)] = statesTemp[0]; 
		    states[2][((indexFirst)*3)+1] = statesTemp[0];
		    states[2][((indexFirst)*3)+2] = statesTemp[0];
		}
				
		return states;
	}
}