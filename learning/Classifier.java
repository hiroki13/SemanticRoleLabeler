/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import Jama.Matrix;
import io.Sentence;

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
    
//    public void backpropagation(final int o_tag, final double prob, final Matrix h, final Matrix x) {}

    public void backpropagation(final double o_tag, final double prob, final Matrix h, final Matrix x) {}

    public Matrix delta_y (final double o_tag, final double prob) {
        return new Matrix(1,1);
    }
    
    public Matrix derivative_kj(final Matrix delta_y, final Matrix h) {
        return new Matrix(1,1);
    }

    public Matrix derivative_ji(final Matrix delta_y, final Matrix h, final Matrix x) {
        return new Matrix(1,1);
    }
    
    public Matrix derivative_x (final Matrix delta_y, final Matrix h) {
        return new Matrix(1,1);
    }
    
    public void update(final Matrix derivative_kj, final Matrix derivative_ji) {}

    public void update(final Sentence sentence, final int[] graph, final int prd_i,
                        final Matrix h, final Matrix derivative_x, final Matrix x) {}
        
}
