/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author hiroki
 */
public class FrameDict {
    static public HashMap<String, HashMap<Integer, ArrayList>> framedict = new HashMap<>();
    
    static public HashMap get(final String lemma) {
        return framedict.get(lemma);
    }

    static public ArrayList get(final String lemma, final int sense) {
        HashMap<Integer, ArrayList> tmp = framedict.get(lemma);
        
        if (tmp == null) return null;
        
        return tmp.get(sense);
    }
    
    static public boolean containsKey(final String lemma) {
        return framedict.containsKey(lemma);
    }

    static public HashMap addAndGet(final String lemma) {        
        if (!framedict.containsKey(lemma))
            framedict.put(lemma, new HashMap());        
        return framedict.get(lemma);        
    }

    static public ArrayList<Integer> addAndGet(final String lemma, final int sense) {
        if (!framedict.containsKey(lemma))
            framedict.put(lemma, new HashMap());        
        HashMap<Integer, ArrayList> tmp = framedict.get(lemma);

        if (!tmp.containsKey(sense))
            tmp.put(sense, new ArrayList());
        return tmp.get(sense);
    }

    static public void put(final String lemma, final HashMap value) {
        framedict.put(lemma, value);    
    }    

    static public void put(String lemma, int sense) {
        if (!framedict.containsKey(lemma))
            framedict.put(lemma, new HashMap());
        framedict.get(lemma).put(sense, new ArrayList());
    }    
    
    
    static public void add(String lemma, int sense, int role) {
        if (role < 0) return;
        
        if (!framedict.containsKey(lemma))
            framedict.put(lemma, new HashMap());        
        final HashMap<Integer, ArrayList> frames = framedict.get(lemma);

        if (!frames.containsKey(sense))
            frames.put(sense, new ArrayList());
        final ArrayList proposition = frames.get(sense);
        
        if (!proposition.contains(role))
            proposition.add(role);
    }

    static public void add(String lemma, int sense) {
        if (!framedict.containsKey(lemma))
            framedict.put(lemma, new HashMap());        
        final HashMap<Integer, ArrayList> frames = framedict.get(lemma);

        if (!frames.containsKey(sense))
            frames.put(sense, new ArrayList());
    }
    
    
}
