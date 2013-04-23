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

import java.io.IOException;

public class Delta {
	
	/**
	 * This function takes 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public float[][] computeDeltaFeatures(float feat[][]) throws IOException {
		
			
			int frames = feat.length;
			float feat_s[][] = new float[frames+6][13];
			// Appending 3 rows in the beginning and the end of feat.
			for(int i = 0,n = 3; i < frames; i++, n++)
				for(int j = 0; j < 13; j++)
					feat_s[n][j] = feat[i][j];
			
			// replicating the first 3 rows.
			for(int i = 0,n = 4; i < 3; i++,n++){
				for(int j = 0; j < 13; j++)
			    	feat_s[i][j] =  feat_s[n][j];
			}
			
			// replicating the last 3 rows.
			for(int i = frames+3 ,n = frames-1; i < frames+6; i++,n++){
				for(int j = 0; j < 13; j++)
					feat_s[i][j] =  feat_s[n][j];
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
		
		// returning the array with delta and double delta features.
		return feat_s_d_dd;
		
		
	}
}
	
