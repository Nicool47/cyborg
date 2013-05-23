package edu.iitb.cyborg.performance;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Performance {

	private static final double MEGABYTE = 1024d * 1024d;
	private static long startTime = 0;

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
}
