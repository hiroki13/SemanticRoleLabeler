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
public class FrameDict {
    static public HashMap<String, HashMap> framedict = new HashMap<>();
    
    static public HashMap get(String key) {
        if (!framedict.containsKey(key))
            framedict.put(key, new HashMap());        
        return framedict.get(key);        
    }

    static public ArrayList<Integer> get(String key1, int key2) {
        if (!framedict.containsKey(key1))
            framedict.put(key1, new HashMap());        
        HashMap<Integer, ArrayList> tmp = framedict.get(key1);

        if (!tmp.containsKey(key2))
            tmp.put(key2, new ArrayList());
        return tmp.get(key2);
    }

    static public void put(String key, HashMap value) {
        framedict.put(key, value);    
    }    

    static public void put(String lemma, int roleset) {
        if (!framedict.containsKey(lemma))
            framedict.put(lemma, new HashMap());
        framedict.get(lemma).put(roleset, new ArrayList());
    }    
    
    
    static public void add(String key1, int key2, int key3) {
        if (key3 < 0) return;
        
        if (!framedict.containsKey(key1))
            framedict.put(key1, new HashMap());        
        final HashMap<Integer, ArrayList> rolesets = framedict.get(key1);

        if (!rolesets.containsKey(key2))
            rolesets.put(key2, new ArrayList());
        final ArrayList proposition = rolesets.get(key2);
        
        if (!proposition.contains(key3))
            proposition.add(key3);
    }

}
