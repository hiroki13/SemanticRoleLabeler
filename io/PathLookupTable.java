/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author hiroki
 */
public class PathLookupTable {

    static public HashMap<String, double[]> path_dict = new HashMap<>();
    static public int weight_length;
    static public Random rnd = new Random(0);
    
    final static public double[] get(final String token) {
        if (!path_dict.containsKey(token))
            path_dict.put(token, vector());
        return path_dict.get(token);
    }
    
    final static public double[] vector() {
        final double[] vector = new double[weight_length];
        for (int i=0; i<weight_length; ++i) vector[i] = (rnd.nextDouble() - 0.5) / 10;
        return vector;
    }
    
}
