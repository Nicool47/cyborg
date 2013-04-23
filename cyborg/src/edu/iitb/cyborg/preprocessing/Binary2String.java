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

package edu.iitb.cyborg.aligner.preprocessing;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Binary2String {
	public static void main(String[] args) throws IOException {
		String mean = null;
		String variance = null;
		String tmat = null;
		String mixwt = null;
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
		}
		
		if(flag == 0){
			System.out.println("Error: Enter atleast one argument\n" +
			"Usage: ----->\n" +
			"java -jar -mean <binary mean file> " +
			"-variance <binary variance file> "  +
			"-tmat <binary transition_matrix file> " +
			"-mixwt <binary mixture_weights file>\n");
			System.exit(0);
		}
		Binary2String ob = new Binary2String();
		if(mean != null){
			byte b = ob.mean(mean);
			if(b == 1)
			System.out.println("mean_bin.txt sucessfully created in the directory  "
					+ System.getProperty("user.dir"));
		}
		if(variance != null){
			byte b = ob.variance(variance);
			if(b == 1)
			System.out.println("variance_bin.txt sucessfully created in the directory  "
					+ System.getProperty("user.dir"));
		}
		if(tmat != null){
			byte b = ob.tmat(tmat);
			if(b == 1)
			System.out.println("tmat_bin.txt sucessfully created in the directory  "
					+ System.getProperty("user.dir"));
		}
		if(mixwt != null){
			byte b = ob.mixWt(mixwt);
			if(b == 1)
			System.out.println("mixwt_bin.txt sucessfully created in the directory  "
					+ System.getProperty("user.dir"));
		}
		
	}
	
	/**
	 * This function converts the mean binary file into text file that
	 * is readable.It takes the mean binary file as an argument.
	 * @param fileName location with name of the binary file.
	 * @return 
	 * @throws IOException
	 */
	public byte mean(String fileName) throws IOException {
		
		FileInputStream is = null;
		DataInputStream dis = null;
		BufferedWriter br = new BufferedWriter(new FileWriter("mean_bin.txt"));
		NumberFormat formatter = new DecimalFormat();
	    formatter = new DecimalFormat("0.000E00");
		try {
			is = new FileInputStream(fileName);
			dis = new DataInputStream(new BufferedInputStream(is));
			int senones = (int)dis.readFloat();
			int gaussians = (int)dis.readFloat();
			br.write("param " + senones + " 1 " + gaussians);
			br.newLine();
			for(int i = 0; i < senones; i++)
				{
				br.write("mgau " + i +" ");
				br.newLine();
				br.write("feat 0");
				br.newLine();
				for(int j = 0; j < gaussians; j++)
					{
					br.write("density " + j +" ");
					for(int k = 0 ; k < 39; k++)
						br.write(formatter.format(dis.readFloat()) + " ");
					br.newLine();
					}
				}
			
		}
			catch(Exception e){
				e.printStackTrace();
				return 0;
			}
			finally{
				if(is != null)is.close();
				if(dis != null)dis.close();
				if(br != null){
					br.flush();
					br.close();
				}
			}
		return 1;
		}
	
	/**
	 * This function converts the variance binary file into text file that
	 * is readable.It takes the variance binary file as an argument.
	 * @param fileName location with name of the binary file.
	 * @throws IOException
	 */
	public byte variance(String fileName) throws IOException {
		FileInputStream is = null;
		DataInputStream dis = null;
		BufferedWriter br = new BufferedWriter(new FileWriter("variance_bin.txt"));
		NumberFormat formatter = new DecimalFormat();
	    formatter = new DecimalFormat("0.000E00");
		try {
			is = new FileInputStream(fileName);
			dis = new DataInputStream(new BufferedInputStream(is));
			int senones = (int)dis.readFloat();
			int gaussians = (int)dis.readFloat();
			br.write("param " + senones + " 1 " + gaussians);
			br.newLine();
			for(int i = 0; i < senones; i++)
				{
				br.write("mgau " + i +" ");
				br.newLine();
				br.write("feat 0");
				br.newLine();
				for(int j = 0; j < gaussians; j++)
					{
					br.write("density " + j +" ");
					for(int k = 0 ; k < 39; k++)
						br.write(formatter.format(dis.readFloat()) + " ");
					br.newLine();
					}
				}
			
		}
			catch(Exception e){
				e.printStackTrace();
				return 0;
			}
			finally{
				if(is != null)is.close();
				if(dis != null)dis.close();
				if(br != null){
					br.flush();
					br.close();
				}
			}
		return 1;
	}
	
	/**
	 * This function converts the transition matrix binary 
	 * file into text file that
	 * is readable.It takes the tmat binary file as an argument.
	 * @param fileName location with name of the binary file.
	 * @throws IOException
	 */
	public byte tmat(String fileName) throws IOException {
		FileInputStream is = null;
		DataInputStream dis = null;
		BufferedWriter br = new BufferedWriter(new FileWriter("tmat_bin.txt"));
		NumberFormat formatter = new DecimalFormat();
	     formatter = new DecimalFormat("0.000E00");
		try {
			is = new FileInputStream(fileName);
			dis = new DataInputStream(new BufferedInputStream(is));
			int phones = (int)dis.readFloat();
			int states = (int)dis.readFloat();
			br.write("tmat " + phones + " " + states);
			br.newLine();
			for(int i = 0; i < phones; i++)
				{
				br.write("tmat [" + i +"]");
				br.newLine();
				for(int j = 0; j < 3; j++)
					{
					if(j == 0)br.write(" ");
					if(j == 1)br.write("\t");
					if(j == 2)br.write("\t\t");
					for(int k = 0 ; k < 2; k++)
					br.write(formatter.format(dis.readFloat()) + " ");
					br.newLine();
					}
				}
			
		}
			catch(Exception e){
				e.printStackTrace();
				return 0;
			}
			finally{
				if(is != null)is.close();
				if(dis != null)dis.close();
				if(br != null){
					br.flush();
					br.close();
				}
			}
		return 1;
	}
	
	
	/**
	 * This function converts the Mixture weights binary file into text file that
	 * is readable.It takes binary file as an argument.
	 * @param fileName location with name of the binary file.
	 * @throws IOException
	 */
	public byte mixWt(String fileName) throws IOException {
		FileInputStream is = null;
		DataInputStream dis = null;
		BufferedWriter br = new BufferedWriter(new FileWriter("mixWt_bin.txt"));
		NumberFormat formatter = new DecimalFormat();
	     formatter = new DecimalFormat("0.000E00");
		try {
			is = new FileInputStream(fileName);
			dis = new DataInputStream(new BufferedInputStream(is));
			int senones = (int)dis.readFloat();
			int gaussians = (int)dis.readFloat();
			int rows = gaussians/8;
			br.write("mixw " + senones + " 1 " + gaussians);
			br.newLine();
			if(gaussians > 8)gaussians = 8;
			for(int i = 0; i < senones; i++)
				{
				br.write("mixw [" + i +" 0"+"]");
				br.newLine();
				br.newLine();
				for(int j = 0; j < rows; j++)
					{
					for(int k = 0 ; k < gaussians; k++)
					br.write(formatter.format(dis.readFloat()) + " ");
					br.newLine();
					}
				}
			
		}
			catch(Exception e){
				e.printStackTrace();
				return 0;
			}
			finally{
				if(is != null)is.close();
				if(dis != null)dis.close();
				if(br != null){
					br.flush();
					br.close();
				}
			}
		return 1;
	}
}
