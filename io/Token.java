/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import java.util.ArrayList;

/**
 *
 * @author hiroki
 */
public class Token {
    final public int id;
    final public String form;
    final public String lemma;
    final public String plemma;
    final public String pos;
    final public String ppos;
    final public String feat;
    final public String pfeat;
    final public int head;
    final public int phead;
    final public String deprel;
    final public String pdeprel;
    final public String fillpred;
    public int pred;
    public int[] apred;
    final public String cpos;
    final public double[] vec;
    
    final public ArrayList<Integer> children;
    public String leftmostw;
    public String leftmostpos;
    public String rightmostw;
    public String rightmostpos;
    public String subcat;
    public String childdepset;
    public String childposset;
    public String childwordset;
    final public ArrayList<String> leftsiblingw;
    final public ArrayList<String> rightsiblingw;
    final public ArrayList<String> leftsiblingpos;
    final public ArrayList<String> rightsiblingpos;    
    public ArrayList<Integer> arguments;

    
    public Token(final String[] line, final boolean test) {
        id = Integer.parseInt(line[0]);
        form = line[1];
        lemma = line[2];
        plemma = line[3];
        pos = line[4];
        ppos = line[5];
        cpos = setCpos();
        feat = line[6];
        pfeat = line[7];
        head = Integer.parseInt(line[8]);
        phead = Integer.parseInt(line[9]);
        deprel = line[10];
        pdeprel = line[11];
        fillpred = line[12];
        pred = getSenseID(cpos, line[13]);
        apred = setApred(line, test);
        vec = LookupTable.get(form);
        
        children = new ArrayList<>();
        leftmostw = "";
        leftmostpos = "";
        rightmostw = "";
        rightmostpos = "";
        subcat = "";
        childdepset = "";
        childposset = "";
        childwordset = "";
        leftsiblingw = new ArrayList<>();
        rightsiblingw = new ArrayList<>();
        leftsiblingpos = new ArrayList<>();
        rightsiblingpos = new ArrayList<>();
        arguments = new ArrayList<>();
    }

/*    
    final private int getRolesetID(String line) {
        if (!"_".equals(line)) {
            final String[] tmp = line.split("\\.");
            String pred_lemma = tmp[0];
            int tmp_roleset = Integer.parseInt(tmp[1]);
            
            int roleset;

            if (!RolesetDict.rolesetdict.containsKey(lemma)) {

                if (RolesetDict.rolesetdict.containsKey(pred_lemma)) {
                    HashMap tmp_rolesetdict = RolesetDict.get(pred_lemma);
                    RolesetDict.put(lemma, tmp_rolesetdict);

                    HashMap tmp_framedict = FrameDict.get(pred_lemma);
                    FrameDict.put(lemma, tmp_framedict);
                }

                roleset = RolesetDict.get(lemma, tmp_roleset);
                FrameDict.put(lemma, roleset);                
            }
            else {
                roleset = RolesetDict.get(lemma, tmp_roleset);
            }
            
            return roleset;
        }
        else
            return -1;        
    }
*/    

    final private int[] setApred(final String[] line, final boolean test) {
        final int[] apred = new int[line.length-14];
        
        if (!test) {
            for (int i=0; i<apred.length; ++i) {
                if (!"_".equals(line[14+i])) {
                    final int role = RoleDict.addAndGet(line[14+i]);
                    if (!RoleDict.core) apred[i] = role;
                    else if (role > 0) apred[i] = role;
                    else apred[i] = -1;
//                    else apred[i] = role;
                }
                else apred[i] = -1;
            }
        }
        else {
            for (int i=0; i<apred.length; ++i) {
                if ("1".equals(line[14+i])) apred[i] = 1000;
                else apred[i] = -1;
            }
        }
        
        return apred;
    }
    
    final public static int getSenseID(final String plemma, final String line) {
        if (!"_".equals(line)) {
            final String[] tmp = line.split("\\.");
            int raw_sense = Integer.parseInt(tmp[1]);
            return SenseDict.addAndGet(plemma, raw_sense);
        }
        else return -1;
    }
    
    final public String setCpos() {
        if (ppos.startsWith("V")) return plemma + "V";
        return plemma + "N";
    }    
        
    
}
