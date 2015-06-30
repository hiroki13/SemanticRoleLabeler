/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import Jama.Matrix;
import io.Sentence;
import static java.lang.Math.exp;
import semanticrolelabeler.Graph;

/**
 *
 * @author hiroki
 */
public class Classifier {

    public float[][] weight;
    public float[][] aweight;
    public Matrix h;//j*1
    public float t = 1.0f;
    public double alpha;

    public Classifier() {}
    
    public float calcScore(final int[] feature, final int tag) {
        return 0.0f;
    }
    
    public void updateWeights(final int o_label, final int p_label, final int[] feature){}
    
    public void updateWeights(final int o_label, final int p_label, final int[] feature, final boolean second){}

    public double forward(final Matrix x) {
        return 0;
    }

    public double[] forward(final Matrix x, final Matrix a0, final Matrix a1) {
        return null;
    }
    
    public void backpropagation(final int o_tag, final Matrix x) {}

    public void backpropagation(final double o_tag, final double prob, final Matrix h, final Matrix x) {}
    
    public Matrix sigmoid(final Matrix x) {
        for (int j=0; j<x.getRowDimension(); ++j) {
            for (int i=0; i<x.getColumnDimension(); ++i) {            
                double score = 1.0 / (1.0 + exp(-(x.get(j, i))));
                x.set(j, i, score);
            }
        }   
        return x;
    }
        
    public Matrix delta_y (final double o_tag, final double prob) {
        return null;
    }
    
    public Matrix derivative_kj(final Matrix delta_y, final Matrix h) {
        return null;
    }

    public Matrix derivative_ji(final Matrix delta_y, final Matrix h, final Matrix x) {
        return null;
    }
    
    public Matrix derivative_x (final Matrix delta_y, final Matrix h) {
        return null;
    }
    
    public void update(final Matrix derivative_kj, final Matrix derivative_ji) {}

    public void updateVector(final Sentence sentence, final int[] graph, final int prd_i,
                               final Matrix h, final Matrix derivative_x, final Matrix x) {}

    public void update(final Sentence sentence, final int prd_i, final double delta, final Graph graph) {}
    
    public void update(final Sentence sentence, final int prd_i, final double[] delta, final Graph graph) {}

    public void update(final Sentence sentence, final int prd_i, final Graph o_graph, final Graph graph) {}

}
