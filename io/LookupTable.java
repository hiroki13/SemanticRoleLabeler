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
public class LookupTable {
    static public HashMap<String, double[]> token_dict = new HashMap<>();
    static public int weight_length;
    static public Random rnd = new Random(0);
    
    final static public double[] get(final String token) {
        if (!token_dict.containsKey(token))
            token_dict.put(token, vector());
        return token_dict.get(token);
    }
    
    final static public double[] vector() {
        final double[] vector = new double[weight_length];
        for (int i=0; i<weight_length; ++i) vector[i] = rnd.nextDouble() - 0.5;
        return vector;
    }
    
}
