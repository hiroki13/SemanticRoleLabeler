/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package semanticrolelabeler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author hiroki
 */
final public class Token {
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
    final public int pred;
    final public int[] apred;
    
    final public ArrayList<Integer> children;
    public String subcat;
    public String childdepset;
    public String childposset;
    public ArrayList<String> leftsiblingw;
    public ArrayList<String> rightsiblingw;
    public ArrayList<String> leftsiblingpos;
    public ArrayList<String> rightsiblingpos;
    public int ppred = 0;
    public int pred_id = -1;
    public int[] papred;
    
    public ArrayList<Integer> arguments;
    public ArrayList<Integer> parguments;
    
    public Token(String[] line) {
        id = Integer.parseInt(line[0]);
        form = line[1];
        lemma = line[2];
        plemma = line[3];
        pos = line[4];
        ppos = line[5];
        feat = line[6];
        pfeat = line[7];
        head = Integer.parseInt(line[8]);
        phead = Integer.parseInt(line[9]);
        deprel = line[10];
        pdeprel = line[11];
        fillpred = line[12];
        pred = getRolesetID2(line[13]);
        apred = new int[line.length-14];
        
        for (int i=0; i<apred.length; ++i) {
            if (!"_".equals(line[14+i]))
                apred[i] = RoleDict.get(line[14+i]);
            else
                apred[i] = -1;
        }
        
        children = new ArrayList<>();
        subcat = "";
        childdepset = "";
        childposset = "";
        leftsiblingw = new ArrayList<>();
        rightsiblingw = new ArrayList<>();
        leftsiblingpos = new ArrayList<>();
        rightsiblingpos = new ArrayList<>();
        arguments = new ArrayList<>();
        parguments = new ArrayList<>();
    }
    
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
    
    final private int getRolesetID2(String line) {
        if (!"_".equals(line)) {
            final String[] tmp = line.split("\\.");
            int tmp_roleset = Integer.parseInt(tmp[1]);
            return RolesetDict.get(plemma, tmp_roleset);
        }
        else return -1;
    }
        
}
