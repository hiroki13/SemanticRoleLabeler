/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package semanticrolelabeler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author hiroki
 */
final public class Reader {
    final public static ArrayList<Sentence> read(String fn) throws Exception{
        int i = 0;
        final String delimiter = "\t";
        final String[] root ={"0","_ROOT_","_ROOT_","_ROOT_","ROOT","ROOT",
                              "_","_","-1","-1","PAD","PAD","_","_"};
        String line;
        ArrayList<Sentence> sentenceList = new ArrayList<>();
        Sentence sentence = new Sentence(i++);
        sentence.add(new Token(root));

        try(BufferedReader br = new BufferedReader(
                                new InputStreamReader(
                                new FileInputStream(fn)))){
            while((line=br.readLine()) != null){
                
                if (line.isEmpty()) {
                    sentence.setPredicates();
                    sentence.setArguments();
                    sentence.setChildren();
                    sentence.setSubCat();
                    sentence.setChildDepSet();                    
                    sentence.setDeps();
                    sentence.setFrameDict();
                    sentence.setOracleGraph();
                    sentenceList.add(sentence);
                    sentence = new Sentence(i++);
                    sentence.add(new Token(root));
                }
                else {               
                    String[] split = line.split(delimiter);
                    sentence.add(new Token(split));
                }
            }
        }
        
        return sentenceList;
    }
    
    final public static ArrayList<Sentence> read(String fn, boolean t) throws Exception{
        int i = 0;
        final String delimiter = "\t";
        final String[] root ={"0","_ROOT_","_ROOT_","_ROOT_","ROOT","ROOT",
                              "_","_","-1","-1","PAD","PAD","_","_"};
        String line;
        ArrayList<Sentence> sentenceList = new ArrayList<>();
        Sentence sentence = new Sentence(i++);
        sentence.add(new Token(root));

        try(BufferedReader br = new BufferedReader(
                                new InputStreamReader(
                                new FileInputStream(fn)))){
            while((line=br.readLine()) != null){
                
                if (line.isEmpty()) {
                    sentence.setPredicates();
                    sentence.setArguments();
                    sentence.setChildren();
                    sentence.setSubCat();
                    sentence.setChildDepSet();                    
                    sentence.setDeps();
                    sentence.setOracleGraph();
                    sentenceList.add(sentence);
                    sentence = new Sentence(i++);
                    sentence.add(new Token(root));
                }
                else {               
                    String[] split = line.split(delimiter);
                    sentence.add(new Token(split));
                }
            }
        }
        
        return sentenceList;
    }
    
}
