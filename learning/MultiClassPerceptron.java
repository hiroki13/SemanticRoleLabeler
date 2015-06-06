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
public class MultiClassPerceptron {
    public float[][] weight;
    public float[][] aweight;
//    public float[][][] w;
//    public float[][][] aw;
    public float t = 1.0f;

    public MultiClassPerceptron(final int label_length, final int weight_length) {
        this.weight = new float[label_length][weight_length];
        this.aweight = new float[label_length][weight_length];
    }

/*    
    public MultiClassPerceptron(final int label_length1, final int label_length2, final int weight_length) {
        this.weight = new float[label_length1][weight_length];
        this.aweight = new float[label_length1][weight_length];
        this.w = new float[label_length1][label_length2][weight_length];
        this.aw = new float[label_length1][label_length2][weight_length];
    }
*/
    
    
    final public float calcScore(final int[] feature,
                                  final int label) {
        float score = 0.0f;
        final float[] tmp_weight = weight[label];
        for(int i=0; i<feature.length; ++i)
            score += tmp_weight[feature[i]];
        return score;
    }
/*        
    final public float calcScore(final int[] feature,
                                  final int label1,
                                  final int label2) {
        float score = 0.0f;
        final float[] tmp_weight = w[label1][label2];
        for(int i=0; i<feature.length; ++i)
            score += tmp_weight[feature[i]];
        return score;
    }
*/        
    final public void updateWeights(final int o_label, final int p_label,
                                      final int[] feature){
        for(int i=0; i<feature.length; ++i){
            final int phi_id = feature[i];
            weight[o_label][phi_id] += 1.0f;
            weight[p_label][phi_id] -= 1.0f;
            aweight[o_label][phi_id] += this.t;
            aweight[p_label][phi_id] -= this.t;
        }
        
        this.t += 1.0f;
    }

    final public void updateWeights(final int o_label, final int p_label,
                                      final int[] feature, final boolean second){
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
    
/*
    final public void updateWeights(final int o_label1, final int o_label2,
                                      final int p_label1, final int p_label2,
                                      final int[] feature){
        for(int i=0; i<feature.length; ++i){
            final int phi_id = feature[i];
            w[o_label1][o_label2][phi_id] += 1.0f;
            w[p_label1][p_label2][phi_id] -= 1.0f;
            aw[o_label1][o_label2][phi_id] += this.t;
            aw[p_label1][p_label2][phi_id] -= this.t;
        }
        
        this.t += 1.0f;
    }
*/    
    
}
