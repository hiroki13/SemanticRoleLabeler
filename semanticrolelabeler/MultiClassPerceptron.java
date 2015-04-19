/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

/**
 *
 * @author hiroki
 */
public class MultiClassPerceptron {
    public float[][] weight;
    public float[][] aweight;
    public float t = 1.0f;

    public MultiClassPerceptron(final int label_length, final int weight_length) {
        this.weight = new float[label_length][weight_length];
        this.aweight = new float[label_length][weight_length];
    }
    
    final public float calcScore(final int[] feature,
                                  final int label) {
        float score = 0.0f;
        final float[] tmp_weight = weight[label];
        for(int i=0; i<feature.length; ++i)
            score += tmp_weight[feature[i]];
        return score;
    }
        
    final public void updateWeights(final int o_label, final int p_label,
                                      final int[] feature){
        final float[] tmp_o_weight = this.weight[o_label];
        final float[] tmp_weight = this.weight[p_label];
        final float[] tmp_o_aweight = this.aweight[o_label];
        final float[] tmp_aweight = this.aweight[p_label];

        for(int i=0; i<feature.length; ++i){
            final int phi_id = feature[i];
            tmp_o_weight[phi_id] += 1.0f;
            tmp_weight[phi_id] -= 1.0f;
            tmp_o_aweight[phi_id] += this.t;
            tmp_aweight[phi_id] -= this.t;
        }
        
        this.t += 1.0f;
    }
    
    
}
