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

public class Cmn {
	
			public float[][] cepstralMeanNormalize(float feat_s[][]) {
				
				int frames = feat_s.length;
				float mean[] = new float[13];
				// computing the mean for each of the 13 features for the current utterance
				for(int i = 0; i < 13; i++){
					float avg = 0;
					for(int j = 0; j < frames; j++){
						avg += feat_s[j][i];
					}
					mean[i] = avg/frames;
				}
				
				// subtracting the mean from each features
				
				for(int i = 0; i < 13; i++)
					for(int j = 0; j < frames; j++)
						feat_s[j][i] -= mean[i];
				
				return feat_s;
				
		}
}


