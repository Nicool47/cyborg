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

package edu.iitb.cyborg.extra;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import edu.iitb.cyborg.Fileloader.FilesLoader;

public class MainClass {
	
	private static final double MEGABYTE = 1024d * 1024d;

	public static double bytesToMegabytes(long bytes) {
	    return bytes / MEGABYTE;
	  }
	
	public static void main(String args[]) throws IOException {
		long startTime = System.currentTimeMillis();
	
		System.out.println(Runtime.getRuntime().totalMemory());
			
		FilesLoader filesLoader = new FilesLoader();
		filesLoader.loadMdef("resources/mdef_1000_16.txt");
		filesLoader.loadDict("resources/Dictionary.dic");
    
		int statesInt[][] = filesLoader.getStatesOfTrans("ahamadanagara");
		
		System.out.println();
		System.out.println("Length of stateInt array : "+statesInt.length);

//		for(int index = 0; index < statesInt.length; index++)
//			System.out.println(statesInt[index][0]+" "+statesInt[index][1]+" "+statesInt[index][2]+" "+statesInt[index][3]+" "+statesInt[index][4]);

		for(int indexI = 0; indexI < statesInt.length; indexI++){
			for(int indexJ = 0; indexJ < statesInt[indexI].length; indexJ++){
				System.out.print(statesInt[indexI][indexJ]+"\t");
			}
			System.out.println();
		}
			
					
		
		//System.out.println(filesLoader.getStatesInt("actually about ahamadanagara"));
		
		System.out.println();
	    System.out.println("-----------------------System Performance-----------------------");
	    System.out.println(" Total time elapsed : "+(System.currentTimeMillis()-startTime)+ " ms");
	    System.out.println();
	    memInfo();
	    System.out.println("-------------------------X-----------X--------------------------");
	}

	public static void memInfo()
	{
	    Runtime runtime = Runtime.getRuntime();
	    // Run the garbage collector
	    // runtime.gc();
	    
	    long memory = runtime.totalMemory() - runtime.freeMemory();
	    NumberFormat numberFormator = new DecimalFormat("#0.00");
	    
	    System.out.println(" Total Memory alloacted by JVM               : "+numberFormator.format(bytesToMegabytes(runtime.totalMemory()))+ " MB");
	    System.out.println(" Available free memory from allocated memory : "+numberFormator.format(bytesToMegabytes(runtime.freeMemory()))+ " MB");
	    System.out.println(" Used memory in bytes                        : " + memory+" Bytes");
	    System.out.println(" Used memory in megabytes                    : " + numberFormator.format(bytesToMegabytes(memory))+ " MB");
	   	
	}
}
  