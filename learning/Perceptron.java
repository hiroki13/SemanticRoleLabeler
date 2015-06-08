/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

/**
 *
 * @author hiroki
 */
public class Perceptron {

    public float[] weight;
    public float[] aweight;
    public float t = 1.0f;
    
    public Perceptron(final int weight_length){
        this.weight = new float[weight_length];
        this.aweight = new float[weight_length];
    }
        
    final public float calcScore(final int[] feature) {
        float score = 0.0f;
        for(int i=0; i<feature.length; ++i)
            score += weight[feature[i]];
        return score;
    }
    
    final public void updateWeights(final int[] o_feature, final int[] feature) {
        for (int i=0; i<o_feature.length; ++i) {
            int phi_id = o_feature[i];
            this.weight[phi_id] += 1.0f;
            this.aweight[phi_id] += this.t;
        }
        
        for (int i=0; i<feature.length; ++i) {
            int phi_id = feature[i];
            this.weight[phi_id] -= 1.0f;
            this.aweight[phi_id] -= this.t;
        }
        
        this.t += 1.0f;
    }

    final public void updateWeights(final int label, final int[] feature) {
        if (label == 0) {
            this.t += 1.0f;
            return;
        }
        
        for (int i=0; i<feature.length; ++i) {
            int phi_id = feature[i];
            this.weight[phi_id] += label * 1.0f;
            this.aweight[phi_id] += label * this.t;
        }
        
        this.t += 1.0f;
    }
        
}
