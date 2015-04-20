/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import java.util.HashMap;

/**
 *
 * @author hiroki
 */
public class SenseDict {
    static public HashMap<String, HashMap<Integer, Integer>> sensedict = new HashMap<>();

    static public HashMap addAndGet(String key) {
        if (!sensedict.containsKey(key))
            sensedict.put(key, new HashMap());        
        return sensedict.get(key);        
    }
    
    static public int addAndGet(String lemma, int sense_raw) {
        if (!sensedict.containsKey(lemma))
            sensedict.put(lemma, new HashMap());        
        HashMap<Integer, Integer> tmp = sensedict.get(lemma);

        if (!tmp.containsKey(sense_raw))
            tmp.put(sense_raw, tmp.size());
        return tmp.get(sense_raw);
    }

    static public void put(String key, HashMap value) {
        sensedict.put(key, value);    
    }    
    
}
