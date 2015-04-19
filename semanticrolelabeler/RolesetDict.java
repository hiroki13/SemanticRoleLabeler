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
public class RolesetDict {
    static public HashMap<String, HashMap> rolesetdict = new HashMap<>();

    static public HashMap get(String key) {
        if (!rolesetdict.containsKey(key))
            rolesetdict.put(key, new HashMap());        
        return rolesetdict.get(key);        
    }
    
    static public int get(String key1, int key2) {
        if (!rolesetdict.containsKey(key1))
            rolesetdict.put(key1, new HashMap());        
        HashMap<Integer, Integer> tmp = rolesetdict.get(key1);

        if (!tmp.containsKey(key2))
            tmp.put(key2, tmp.size());
        return tmp.get(key2);
    }

    static public void put(String key, HashMap value) {
        rolesetdict.put(key, value);    
    }    
    
}
