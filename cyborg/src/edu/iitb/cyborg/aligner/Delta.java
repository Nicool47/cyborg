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

package edu.iitb.cyborg.aligner;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Delta {
	
	public float[][] computeDeltaFeatures(String path) throws IOException {
		
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
			
		    System.out.println(frames);
		    
			float[][] feat_s = new float[frames+6][13];
			for(int i = 3; i < frames+3; i++)
				for(int j = 0; j < 13; j++){
					dis.read(buffer);
			    	feat_s[i][j] =  ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getFloat();
				}
			
			// replicating the first 3 rows.
			for(int i = 0,n = 4; i < 3; i++,n++){
				for(int j = 0; j < 13; j++)
			    	feat_s[i][j] =  feat_s[n][j];
			}
			
			// replicating the last 3 rows.
			for(int i = frames+3 ,n = frames-1; i < frames+6; i++,n++){
				for(int j = 0; j < 13; j++)
					feat_s[i][j] =  feat_s[n][j];
				System.out.println(n);
			}
							
			// computing the delta features
			float[][] feat_d = new float[frames][13];   
			 for(int i = 0,n=3; i < frames; i++,n++){
				 for(int j = 0; j < 13; j++)
					 feat_d[i][j] =  feat_s[n+2][j] - feat_s[n-2][j];
			 }
			
			 // computing the double delta features
			 float[][] feat_dd = new float[frames][13];   
			 for(int i = 0,n=3; i < frames; i++,n++){
				 for(int j = 0; j < 13; j++)
					 feat_dd[i][j] =  (feat_s[n+3][j] - feat_s[n-1][j]) - (feat_s[n+1][j] - feat_s[n-3][j]);
			 }
			 
			 // merging delta and double-delta with single features
			 float feat_s_d_dd[][] = new float[frames][39];
			 for(int i = 0; i < frames; i++)
				 for(int j = 0; j < 13; j++){
					 feat_s_d_dd[i][j] = feat_s[i+3][j] ;
				 	 feat_s_d_dd[i][j+13] = feat_d[i][j];
				 	 feat_s_d_dd[i][j+26] = feat_dd[i][j];
				 }	 	 
			 	
		return feat_s_d_dd;
			 
		}catch(Exception e){
			System.out.println(path + " file may be missing or problems with the feature file");
			e.printStackTrace();
			System.exit(0);
		}
		finally{
			if(is != null)is.close();
			if(dis != null)dis.close();
			}
		return null;
		
		
	}
}
	
