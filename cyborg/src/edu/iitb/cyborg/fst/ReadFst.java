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

package edu.iitb.cyborg.fst;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReadFst {

	static Map<String, String> hashMapPath;
	
	public static void main(String[] args) throws IOException {
		BufferedReader br = null;
		hashMapPath =  new HashMap<>();
		try{
			br = new BufferedReader(new FileReader("fst//final_dir.txt"));
			String line = null;
	
			while((line = br.readLine()) != null)
				ReadFst.putHashMap(line);

		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(br != null)br.close();
		}
		
		//print hashmap
		for(String key: hashMapPath.keySet()){
			String value = hashMapPath.get(key);
			System.out.println("key " + key + " value " + value);
		}
		
	}
	
	static void putHashMap(String s){
		String words[] = s.split("\t",3);
			if(words.length > 1){
				hashMapPath.put(words[0] + words[1], words[2]);
			}else
				hashMapPath.put(words[0], "final");
	}
	
	static String getHashmap(String s){
		return hashMapPath.get(s);
	}
	
}


