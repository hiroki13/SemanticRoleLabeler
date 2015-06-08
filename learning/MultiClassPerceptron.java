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
public class MultiClassPerceptron extends Classifier{

    public MultiClassPerceptron(final int label_length, final int weight_length) {
        this.weight = new float[label_length][weight_length];
        this.aweight = new float[label_length][weight_length];
    }
    
    final public float calcScore(final int[] feature, final int label) {
        float score = 0.0f;
        final float[] tmp_weight = weight[label];
        for(int i=0; i<feature.length; ++i)
            score += tmp_weight[feature[i]];
        return score;
    }

    @Override
    final public void updateWeights(final int o_label, final int p_label, final int[] feature){
        for(int i=0; i<feature.length; ++i){
            final int phi_id = feature[i];
            weight[o_label][phi_id] += 1.0f;
            weight[p_label][phi_id] -= 1.0f;
            aweight[o_label][phi_id] += this.t;
            aweight[p_label][phi_id] -= this.t;
        }
        
        this.t += 1.0f;
    }

    @Override
    final public void updateWeights(final int o_label, final int p_label, final int[] feature, final boolean second){
        if (second) this.t -= 1.0f;
        
        for(int i=0; i<feature.length; ++i){
            final int phi_id = feature[i];
            weight[o_label][phi_id] += 1.0f;
            weight[p_label][phi_id] -= 1.0f;
            aweight[o_label][phi_id] += this.t;
            aweight[p_label][phi_id] -= this.t;
        }
        
        this.t += 1.0f;
    }
        
}
