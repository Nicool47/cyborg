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

package edu.iitb.cyborg.performance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Performance {

	private static final double MEGABYTE = 1024d * 1024d;
	private static long startTime = 0;
	private static File resultFile = new File("results/result.txt");
	private static FileWriter resultFileWriter;
	
	public static double bytesToMegabytes(long bytes) {
	    return bytes / MEGABYTE;
	}
	
	
	public static void logStartTime(){
		startTime = System.currentTimeMillis();
	}
	
	public static void logEndTime(){
		System.out.println();
	    System.out.println("****************** System Performance ********************");
	    System.out.println();
	    System.out.println(" Total time elapsed : "+(System.currentTimeMillis()-startTime)+ " ms");
	    System.out.println();
	}
	
	public static void logEndTimeInFile() throws IOException{
	
		resultFileWriter = new FileWriter(resultFile, true);
		resultFileWriter.write("\n\n\n");
	    resultFileWriter.write("****************** System Performance ********************");
		resultFileWriter.write("\n\n");
	    resultFileWriter.write(" Total time elapsed : "+(System.currentTimeMillis()-startTime)+ " ms");
	    resultFileWriter.write("\n\n");
	    resultFileWriter.close();
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
	    System.out.println();
	    System.out.println("*******************X*******************X*******************");
	   	
	}
	
	public static void memInfoInFile() throws IOException
	{
	    Runtime runtime = Runtime.getRuntime();
	    // Run the garbage collector
	    // runtime.gc();
	    
	    long memory = runtime.totalMemory() - runtime.freeMemory();
	    NumberFormat numberFormator = new DecimalFormat("#0.00");
	    
	    resultFileWriter = new FileWriter(resultFile, true);
	    resultFileWriter.write(" Total Memory allocated by JVM               : "+numberFormator.format(bytesToMegabytes(runtime.totalMemory()))+ " MB");
	    resultFileWriter.write("\n Available free memory from allocated memory : "+numberFormator.format(bytesToMegabytes(runtime.freeMemory()))+ " MB");
	    resultFileWriter.write("\n Used memory in bytes                        : " + memory+" Bytes");
	    resultFileWriter.write("\n Used memory in megabytes                    : " + numberFormator.format(bytesToMegabytes(memory))+ " MB");
	    resultFileWriter.write("\n\n");
	    resultFileWriter.write("*******************X*******************X*******************");
	   	resultFileWriter.close();
	}
	
}
