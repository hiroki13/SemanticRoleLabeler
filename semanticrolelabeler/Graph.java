/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import Jama.Matrix;

/**
 *
 * @author hiroki
 */
public class Graph {
    public int[] graph;
    public double score;
    public double[] scores;
    public Matrix feature, h;
    public Matrix[] features, hs;
    
    public Graph() {}
    
    public Graph(final int arg_length) {
        scores = new double[arg_length];
        features = new Matrix[arg_length];
        hs = new Matrix[arg_length];
    }
}
