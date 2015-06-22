/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author hiroki
 */
public class Reader {
    final public static ArrayList<Sentence> read(final String fn, final boolean test) throws Exception{
        int i = 0;
        final String delimiter = "\t";
        final String[] root ={"0","_ROOT_","_ROOT_","_ROOT_","ROOT","ROOT",
                              "_","_","-1","-1","PAD","PAD","_","_"};
        String line;
        ArrayList<Sentence> sentenceList = new ArrayList<>();
        Sentence sentence = new Sentence(i++);
        sentence.add(new Token(root, test));

        try(BufferedReader br = new BufferedReader(
                                new InputStreamReader(
                                new FileInputStream(fn)))){
            while((line=br.readLine()) != null){
                
                if (line.isEmpty()) {
                    sentence.setPredicates();
                    sentence.setChildren();
                    sentence.setSubCat();
                    sentence.setChildDepSet();                    
                    sentence.setDeps();

                    if (!test) {
                        sentence.setArguments();
                        sentence.setMaxArgLength();
                        sentence.setFrameDict();
                        sentence.setOracleGraph();
                    }
                    
                    sentenceList.add(sentence);
                    sentence = new Sentence(i++);
                    sentence.add(new Token(root, test));
                }
                else {               
                    String[] split = line.split(delimiter);
                    sentence.add(new Token(split, test));
                }
            }
        }
        
        return sentenceList;
    }

    final public static ArrayList<Sentence> read_nn(final String fn, final boolean test) throws Exception{
        int i = 0;
        final String delimiter = "\t";
        final String[] root ={"0","_ROOT_","_ROOT_","_ROOT_","ROOT","ROOT",
                              "_","_","-1","-1","PAD","PAD","_","_"};
        String line;
        ArrayList<Sentence> sentenceList = new ArrayList<>();
        Sentence sentence = new Sentence(i++);
        sentence.add(new Token(root, test));

        try(BufferedReader br = new BufferedReader(
                                new InputStreamReader(
                                new FileInputStream(fn)))){
            while((line=br.readLine()) != null){
                
                if (line.isEmpty()) {
                    sentence.setPredicates();
                    sentence.setChildren();
                    sentence.setSubCat();
                    sentence.setChildDepSet();                    
                    sentence.setDeps();

                    sentence.setAllCandidateArguments();                    
                    sentence.setMaxArgLength();                    

                    if (!test) {
                        sentence.setFrameDict();
                        sentence.setOraclePropGraph();
                    }
                    
                    sentenceList.add(sentence);
                    if (sentenceList.size() == 2) return sentenceList;
                    sentence = new Sentence(i++);
                    sentence.add(new Token(root, test));
                }
                else {               
                    String[] split = line.split(delimiter);
                    sentence.add(new Token(split, test));
                }
            }
        }
        
        return sentenceList;
    }

    final public static ArrayList<Sentence> read(final String fn, final boolean ai, final boolean test) throws Exception{
        int i = 0;
        final String delimiter = "\t";
        final String[] root ={"0","_ROOT_","_ROOT_","_ROOT_","ROOT","ROOT",
                              "_","_","-1","-1","PAD","PAD","_","_"};
        String line;
        ArrayList<Sentence> sentenceList = new ArrayList<>();
        Sentence sentence = new Sentence(i++);
        sentence.add(new Token(root, test));

        try(BufferedReader br = new BufferedReader(
                                new InputStreamReader(
                                new FileInputStream(fn)))){
            while((line=br.readLine()) != null){
                
                if (line.isEmpty()) {
                    sentence.setPredicates();
                    sentence.setChildren();
                    sentence.setSubCat();
                    sentence.setChildDepSet();                    
                    sentence.setDeps();

                    sentence.setArguments();
                    sentence.setMaxArgLength();
                    sentence.setFrameDict();
                    
                    sentenceList.add(sentence);
                    sentence = new Sentence(i++);
                    sentence.add(new Token(root, test));
                }
                else {               
                    String[] split = line.split(delimiter);
                    sentence.add(new Token(split, test));
                }
            }
        }
        
        return sentenceList;
    }
    
    final public static ArrayList<Sentence> read(final String fn) throws Exception{
        int i = 0;
        final String delimiter = "\t";
        final String[] root ={"0","_ROOT_","_ROOT_","_ROOT_","ROOT","ROOT",
                              "_","_","-1","-1","PAD","PAD","_","_"};
        String line;
        ArrayList<Sentence> sentenceList = new ArrayList<>();
        Sentence sentence = new Sentence(i++);
        sentence.add(new Token(root, false));

        try(BufferedReader br = new BufferedReader(
                                new InputStreamReader(
                                new FileInputStream(fn)))){
            while((line=br.readLine()) != null){
                
                if (line.isEmpty()) {
                    sentence.setPredicates();
                    sentence.setArguments();
                    sentence.setChildren();
                    sentence.setMaxArgLength();
                    sentence.setFrameDict();
//                    sentence.setOracleGraph();
                    sentence.setOraclePropGraph();
                    
                    sentenceList.add(sentence);
                    sentence = new Sentence(i++);
                    sentence.add(new Token(root, false));
                }
                else {               
                    String[] split = line.split(delimiter);
                    sentence.add(new Token(split, false));
                }
            }
        }
        
        return sentenceList;
    }

    final public static ArrayList<Sentence> read_nn(final String fn) throws Exception{
        int i = 0;
        final String delimiter = "\t";
        final String[] root ={"0","_ROOT_","_ROOT_","_ROOT_","ROOT","ROOT",
                              "_","_","-1","-1","PAD","PAD","_","_"};
        String line;
        ArrayList<Sentence> sentenceList = new ArrayList<>();
        Sentence sentence = new Sentence(i++);
        sentence.add(new Token(root, false));

        try(BufferedReader br = new BufferedReader(
                                new InputStreamReader(
                                new FileInputStream(fn)))){
            while((line=br.readLine()) != null){
                
                if (line.isEmpty()) {
                    sentence.setPredicates();
                    sentence.setAllCandidateArguments();
                    sentence.setChildren();
                    sentence.setMaxArgLength();
                    sentence.setFrameDict();
//                    sentence.setOracleGraph();
                    sentence.setOraclePropGraph();
                    
                    sentenceList.add(sentence);
                    sentence = new Sentence(i++);
                    sentence.add(new Token(root, false));
                }
                else {               
                    String[] split = line.split(delimiter);
                    sentence.add(new Token(split, false));
                }
            }
        }
        
        return sentenceList;
    }

    final public static void embeddings(String fn) throws Exception {
        try(BufferedReader br = new BufferedReader(
                                new InputStreamReader(
                                new FileInputStream(fn)))){
            final String delimiter = " ";
            String line;
            while((line=br.readLine()) != null){
                final String[] split = line.split(delimiter);
                final String w = split[0];
                final double[] embedding = new double[split.length-1];

                for (int i=1; i<split.length; ++i)
                    embedding[i-1] = Double.valueOf(split[i]);
                
                LookupTable.token_dict.put(w, embedding);
                for (int i=0; i<2; ++i) {
                    if (i == 0) LookupTable.token_dict_a0.put(w, embedding);
                    else LookupTable.token_dict_a1.put(w, embedding);
                }
            }
        }
    }
    
}
