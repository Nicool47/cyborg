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

package edu.iitb.cyborg.extra;

import java.io.IOException;

public class FeatureExtraction {
	public static void main(String[] args) throws IOException {
		
		String audioInput = "gahuu.wav";
		String audioFeature = "gahuu.mfc";
		Runtime.getRuntime().exec("java -jar wav2feat.jar -c feature_extraction.xml -name cepstraFrontEnd" +
				" -i " + audioInput + " -o " + audioFeature);
	}
}
