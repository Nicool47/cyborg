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
 */

package edu.iitb.cyborg.preprocessing;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class String2Binary {
	
	public static ArrayList<String>list;
	
	public static void main(String[] args) throws IOException {
		
		String mean = null;
		String variance = null;
		String tmat = null;
		String mixwt = null;
		String mdef = null;
		byte flag = 0;
		
		for(int i = 0; i < args.length ; i++){
			if(args[i].equals("-mean")){
				mean = args[++i];
				flag = 1;
			}
			if(args[i].equals("-variance")){
				variance = args[++i];
				flag = 1;
			}
			if(args[i].equals("-tmat")){
				tmat = args[++i];
				flag = 1;
			}
			if(args[i].equals("-mixwt")){
				mixwt = args[++i];
				flag = 1;
			}
			if(args[i].equals("-mdef")){
				mdef = args[++i];
				flag = 1;
			}
			
		}
		
		if(flag == 0){
			System.out.println("Error: Enter atleast one argument\n" +
			"Usage: ----->\n" +
			"java -jar <Jar name> -mean <mean file> " +
			"-variance <variance file> "  +
			"-tmat <transition_matrix file> " +
			"-mixwt <mixture_weights file> " +
			"-mdef <mdef file>\n" );
			System.exit(0);
		}
		String2Binary ob = new String2Binary();
		if(mean != null){
			byte b = ob.mean(mean);
			if(b == 1)
			System.out.println("mean_bin sucessfully created in the directory  "
					+ System.getProperty("user.dir"));
		}
		if(variance != null){
			byte b = ob.variance(variance);
			if(b == 1)
			System.out.println("variance_bin sucessfully created in the directory  "
					+ System.getProperty("user.dir"));
		}
		if(tmat != null){
			byte b = ob.tmat(tmat);
			if(b == 1)
			System.out.println("tmat_bin sucessfully created in the directory  "
					+ System.getProperty("user.dir"));
		}
		if(mixwt != null){
			byte b = ob.mixWt(mixwt);
			if(b == 1)
			System.out.println("mixwt_bin sucessfully created in the directory  "
					+ System.getProperty("user.dir"));
		}
		if(mdef != null){
			byte b = ob.mdef(mdef);
			if(b == 1)
			System.out.println("mdef_tab sucessfully created in the directory  "
					+ System.getProperty("user.dir"));
		}
		
	}
	
/**
 * Creates a file mdef_tab(Content is in text and tab separated) in the current directory given the text 
 * format of mdef file. 
 * The location with the name of the mdef file 
 * should be specified as an argument to this function. 
 * 	
 * @param fileName
 * @return 1 if successful else 0
 * @throws IOException
 */
	private byte mdef(String fileName) throws IOException {
		
		File fileMdef = null;
		File fileMdefTab = null;
		BufferedReader brMdef = null;
		FileWriter fwMdefTab = null; 
		
		try {
			fileMdef = new File(fileName);
			fileMdefTab = new File(fileName+"_tab");
			brMdef = new BufferedReader(new FileReader(fileMdef));
			fwMdefTab = new FileWriter(fileMdefTab);
			String tempLine = null;
			
			while((tempLine = brMdef.readLine()) != null){
				tempLine = tempLine.replaceAll("^\\s+", "");
				tempLine = tempLine.replaceAll("\\s+", "\t");
				fwMdefTab.write(tempLine+"\n");
			}
		} catch (Exception e) {
			
			e.printStackTrace();
			return 0;
		} finally {
			if(brMdef!=null)
				brMdef.close();
			if(fwMdefTab!=null)
				fwMdefTab.close();
		}
		return 1;
	}


	/**
	 * Creates a file mean_bin in the current directory given the text 
	 * format of mean file.
	 * The location with the name of the mean file 
	 * should be specified as an argument to this function.
	 * @param fileName 
	 * @throws IOException
	 */
	public byte mean(String fileName) throws IOException {
		
		initialize(fileName);
		FileOutputStream fos = null;
	    DataOutputStream dos = null;
	    try{
	    	// create file output stream
	    	fos = new FileOutputStream("mean_bin");
	    	// create data output stream
	    	dos = new DataOutputStream(fos);
	    	String[] params = list.get(0).split("\\s");
	    	if(!(params[0].equals("param"))){
        	System.out.println("Incorrect mean file given as argument");
        	System.exit(0);
        }
        float c = Float.parseFloat(params[3]);
        //writing the senones and gaussians
        dos.writeFloat(Float.parseFloat(params[1]));
        dos.writeFloat(Float.parseFloat(params[3]));
        
		for (int i = 1; i < list.size(); i++) 
			{	
			if(!(i % (c+2) == 1 || i % (c+2) == 2) )
			{	
				String[] result = list.get(i).split("\\s");
				for (int j = 2; j < result.length; j++)
					dos.writeFloat(Float.parseFloat(result[j]));		
			}
			
		}
	    }catch(Exception e){
	            // if any I/O error occurs
	            e.printStackTrace();
	            return 0;
	         }finally{ if(dos != null)
	             dos.close();
	         if(fos!=null)
	            fos.close();
	         }
	    return 1;
	    }
	/**
	 *Creates a file variance_bin in the current directory given the text 
	 * format of variance file.
	 * The location with the name of the variance file 
	 * should be specified as an argument to this function.
	 * @param fileName
	 * @throws IOException
	 */
	public byte variance(String fileName) throws IOException {
		
		initialize(fileName);
		FileOutputStream fos = null;
	    DataOutputStream dos = null;
	    try{
	    	// create file output stream
	    	fos = new FileOutputStream("variance_bin");
	    	// create data output stream
	    	dos = new DataOutputStream(fos);
	    	String[] params = list.get(0).split("\\s");
	    	if(!(params[0].equals("param"))){
	    		System.out.println("Incorrect variance file given as argument");
	    		System.exit(0);
        }
        float c = Float.parseFloat(params[3]);
        dos.writeFloat(Float.parseFloat(params[1]));
        dos.writeFloat(Float.parseFloat(params[3]));
        
		for (int i = 1; i < list.size(); i++) 
			{	
			if(!(i % (c+2) == 1 || i % (c+2) == 2) )
			{
				String[] result = list.get(i).split("\\s");
				for (int j = 2; j < result.length; j++)
				dos.writeFloat(Float.parseFloat(result[j]));
			}
			
		}
	    }catch(Exception e){
	            // if any I/O error occurs
	            e.printStackTrace();
	            return 0;
	         }finally{ if(dos != null)
	             dos.close();
	         if(fos!=null)
	            fos.close();
	         }
	    return 1;
	    }
	
	/**
	 * Creates a file tmat_bin in the current directory given the text 
	 * format of transition matrix file file.
	 * The location with the name of the transition matrix file 
	 * should be specified as an argument to this function.
	 * @param fileName
	 * @throws IOException
	 */
	public byte tmat(String fileName) throws IOException {
		
		initialize(fileName);
		FileOutputStream fos = null;
	    DataOutputStream dos = null;
	    try{
	    	// create file output stream
	    	fos = new FileOutputStream("tmat_bin");
	    	// create data output stream
	    	dos = new DataOutputStream(fos);
	    	String str = list.get(0).trim();
	    	String[] params = str.split("\\s");
	    	if(!(params[0].equals("tmat"))){
	    		System.out.println("Incorrect transition file given as argument");
	    		System.exit(0);
        }
        dos.writeFloat(Float.parseFloat(params[1]));
        dos.writeFloat(Float.parseFloat(params[2]));
        
		for (int i = 1; i < list.size(); i++) 
			{	
			if(!(i % 4 == 1))
			{
				String str1 = list.get(i).trim();
				String[] result = str1.split("\\s");
				for (int j = 0; j < 2; j++)
				dos.writeFloat(Float.parseFloat(result[j]));
			}
			
		}
	    }catch(Exception e){
	            // if any I/O error occurs
	            e.printStackTrace();
	            return 0;
	         }finally{ if(dos != null)
	             dos.close();
	         if(fos!=null)
	            fos.close();
	         }
	    return 1;
	    }
		
	/**
	 * Creates a file mixWtt_bin in the current directory given the text 
	 * format of mixture weights file.
	 * The location with the name of the mixture weights file 
	 * should be specified as an argument to this function.
	 * @param fileName
	 * @throws IOException
	 */
	public byte mixWt(String fileName) throws IOException {
		
		initialize(fileName);
		FileOutputStream fos = null;
	    DataOutputStream dos = null;
	    try{
	 // create file output stream
	    fos = new FileOutputStream("mixWt_bin");
	 // create data output stream
        dos = new DataOutputStream(fos);
        String[] params = list.get(0).split("\\s");
        if(!(params[0].equals("mixw"))){
        	System.out.println("Incorrect mixture weight file given as argument");
        	System.exit(0);
        }
        int c = (int)(Float.parseFloat(params[3])/8);
        //writing the senones
        dos.writeFloat(Float.parseFloat(params[1]));
        //writing the gaussians
        dos.writeFloat(Float.parseFloat(params[3]));
        
		for (int i = 1; i < list.size(); i++) 
			{	
			if(!(i % (c+2) == 1 || i % (c+2) == 2) )
			{
				String str = list.get(i).replaceAll("\\s+$","");
				String[] result = str.split("\\s");
				for (int j = 0; j < result.length; j++)
				dos.writeFloat(Float.parseFloat(result[j]));
			}
			
		}
	    }catch(Exception e){
	            // if any I/O error occurs
	            e.printStackTrace();
	            return 0;
	         }finally{ if(dos != null)
	             dos.close();
	         if(fos!=null)
	            fos.close();
	         }
	    return 1;
	}

	/**
	 * This function reads the text file given as an argument,
	 * reads each line and stores it in a static array 'list'.
	 * So the array 'list' is accessible anywhere in this package by 
	 * just calling this function and referring to list with the className
	 * (e.g. list).
	 * Also the function trims each line and replaces multiple
	 * spaces with single space.
	 * 
	 * @param path path of the text file with name
	 */
	
	public static void initialize(String path){
		
		list = new ArrayList<String>();
		String currentLine;
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(path));
			while ((currentLine = br.readLine()) != null){
				String trimmedLine = currentLine.replaceAll(" +", " ").trim();			
				list.add(trimmedLine);
		}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				if(br != null)br.close();
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
}
