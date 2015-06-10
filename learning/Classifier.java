/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import Jama.Matrix;

/**
 *
 * @author hiroki
 */
public class Classifier {

    public float[][] weight;
    public float[][] aweight;
    public Matrix h;//j*1
    public float t = 1.0f;

    public Classifier() {}
    
    public float calcScore(final int[] feature, final int tag) {
        return 0.0f;
    }
    
    public void updateWeights(final int o_label, final int p_label, final int[] feature){}
    
    public void updateWeights(final int o_label, final int p_label, final int[] feature, final boolean second){}

    public double forward(final Matrix x) {
        return 0.0d;
    }
    
    public void backpropagation(final int o_tag, final Matrix x) {}
    
    public void backpropagation(final int o_tag, final double prob, final Matrix h, final Matrix x) {}
    
}
