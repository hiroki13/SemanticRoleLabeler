/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import java.util.ArrayList;

/**
 *
 * @author hiroki
 */
public class RoleDict {

    static public ArrayList<String> roledict = new ArrayList();
    static public ArrayList<Integer> rolearray = new ArrayList();
    
    final static public void add(String role) {
        if (roledict.contains(role)) return;
        roledict.add(role);
        rolearray.add(roledict.indexOf(role));
    }

    final static public int get(String role) {
        if (!roledict.contains(role)) {
            roledict.add(role);
            rolearray.add(roledict.indexOf(role));
        }
        return roledict.indexOf(role);
    }
    
    final static public int size() {
        return roledict.size();
    }
}
