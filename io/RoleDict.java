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
public class RoleDict {

    static public ArrayList<String> roledict = new ArrayList();
    static public ArrayList<Integer> rolearray = new ArrayList();
    static public HashMap<String, Integer> biroledict = new HashMap();
    static public boolean core;
    
    final static public void add(String role) {
//        if (core && (role.startsWith("C") || role.startsWith("R"))) return;
        if (core && !"A0".equals(role) && !"A1".equals(role)) return;

        if (roledict.contains(role)) return;
        roledict.add(role);
        rolearray.add(roledict.indexOf(role));
    }

    final static public int addAndGet(String role) {
//        if (core && (role.startsWith("C") || role.startsWith("R"))) role = "NULL";
//        if (core && !"A0".equals(role) && !"A1".equals(role)) role = "NULL";
        if (core && !"A0".equals(role) && !"A1".equals(role)) return -1;

        if (!roledict.contains(role)) {
            roledict.add(role);
            rolearray.add(roledict.indexOf(role));
        }
        return roledict.indexOf(role);
    }
    
    final static public int size() {
        return roledict.size();
    }
    
    final static public void setBiroledict() {
        for (int i=0; i<rolearray.size(); ++i) {        
            final int role1 = rolearray.get(i);            
            biroledict.put(String.valueOf(role1), biroledict.size());
                            
            for (int j=0; j<rolearray.size(); ++j) {            
                final int role2 = rolearray.get(j);                
                biroledict.put(String.valueOf(role1) + "-" + String.valueOf(role2), biroledict.size());                
            }                        
        }        
    }
    
}
