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

package edu.iitb.frontend.audio.feature;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
/**
 * This program is an independent module and takes list of audio files, does front end signal processing to it,
 * and then dumps the resulting Feature into a separate folder.
 * <p/>
 * Available options:
 * <ul>
 * 
 * <li>-flag : value = 1 for single file processing, value = 2 for batch file processing</li>
 * <p> for flag = 1 </p>
 * <li>-in input Folder in which the wavFiles are stored.</li>
 * <li>-i wavFile - the name of the input file</li>
 * <li>-op outputFile - output folder in which the MFc files will be stored</li>
 * 
 * <p> for flag =2 </p>
 * <li>-in input Folder in which the wavFiles are stored.</li>
 * <li>-ctl controlFile - the name of the input file for batch processing</li>
 * <li>-op outputFile - output folder in which the MFc files will be stored</li>
 * </ul>
 * 
 * 
 * 
 * All the parameters for feature extraction have to entered manually in the file <b>FeatureFileExtractor.java</b>.<br>
 * If no parameters entered then default parameters will be used.<br>
 * Default parameters:
 * <ul>
 * <li>sampling rate = 8000</li>
 * <li>window size = 25.625ms </li>
 * <li>hopsize = 10ms </li>
 * <li>lowerFrequencyOfFilter = 133Hz </li>
 * <li>upperFrequencyOfFilter = 3500Hz </li>
 * <li> numberFilters = 31 </li>
 * </ul>
 */

public class FeatureFileDumper {
	
	public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
		
		String inputFolder = null;
		String outputFolder = null;
		String fileIds = null;
		String fileName = null;

		for(int i = 0; i < args.length ; i++){
			if(args[i].equals("-ip")){
				inputFolder = args[++i];
			}
			if(args[i].equals("-op")){
				outputFolder = args[++i];
			}
			if(args[i].equals("-ctl")){
				fileIds = args[++i];
			}
			if(args[i].equals("-i")){
				fileName = args[++i];
			}
			
		}
		
		if(inputFolder == null || outputFolder == null){
			System.out.println("Insuffient arguments\n Usage ---> \n" +
			" -ip <input Folder in which the wavFiles are stored>\n -op <output " +
			"folder in which the MFc files will be stored>\n -i <name of the input" +
			" file>\n -ctl <the name of the input" +
				" file for batch processing>");
			System.exit(0);
		}
		
		
		if(fileName != null && fileIds == null){
			System.out.println("considering single file processing");
			FeatureFileExtractor.computeFeatures(fileName,inputFolder, outputFolder);
		}
		else if(fileName == null && fileIds != null){
			System.out.println("considering batch file processing");
			BufferedReader br = null;
			String fileName1;
			
			try{
				br = new BufferedReader(new FileReader(fileIds));
				while ((fileName1 = br.readLine()) != null){				
					FeatureFileExtractor.computeFeatures(fileName1,inputFolder, outputFolder);
					System.out.println("CONVERTED successfully");
					
				}
			}
			catch(IOException e){
				e.printStackTrace();
			}finally{
				try{
					if(br != null)br.close();
					}catch(IOException ex){
					ex.printStackTrace();
					}		
				}
		}
		else if(fileName != null && fileIds != null){
			System.out
					.println("ERROR: the arguments -ctl and -i cannot be given at the same time");
		}
		
		else if(fileName == null && fileIds == null){
			System.out
					.println("ERROR: Atleast one of the argument -ctl or -i have to be given");
		}
		
		
		else{
			System.out.println("Error in the argument list");
		}
		
	}
}
