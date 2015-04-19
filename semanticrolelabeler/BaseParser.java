/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author hiroki
 */
public class BaseParser {
    public MultiClassPerceptron perceptron;
    public FeatureExtracter feature_extracter;
    public float correct, total, r_total, p_total;
    
    public BaseParser(final int label_length, final int weight_length) {
        this.perceptron = new MultiClassPerceptron(label_length, weight_length);
        this.feature_extracter = new FeatureExtracter(weight_length);
        this.feature_extracter.g_cache = new ArrayList();
    }
    
    final public void train(final ArrayList<Sentence> sentencelist) {
        correct = 0.0f;
        total = 0.0f;

        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);
            final ArrayList<Token> tokens = sentence.tokens;
            final int[] preds = sentence.preds;
                        
            if (feature_extracter.g_cache.size() < i+1)
                feature_extracter.g_cache.add(new String[preds.length][sentence.size()][]);

            if (preds.length == 0) continue;
            
            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
                final Token pred = sentence.tokens.get(preds[prd_i]);
                final ArrayList<Integer> arguments = pred.arguments;
                final String pos = feature_extracter.pos(tokens.get(preds[prd_i]));

                for (int arg_i=1; arg_i<arguments.size(); ++arg_i) {
                    final Token arg = sentence.tokens.get(arguments.get(arg_i));
                    final String[] i_feature = feature_extracter.instantiateFirstOrdFeature(sentence, prd_i, arg_i);
                    final String[] c_feature = feature_extracter.conjoin(i_feature, pos);
                    final int[] feature = feature_extracter.encodeFeature2(c_feature);
                    final int label = decode(pred, feature);
                    final int o_label = arg.apred[prd_i];
            
                    perceptron.updateWeights(o_label, label, feature);
                    
                    if (o_label == label) correct += 1.0;
                    total += 1.0;
                }
            }

            if (i%1000 == 0 && i != 0) System.out.print(String.format("%d ", i));
            
        }
        
        System.out.println("\tCorrect: " + correct);                        
        System.out.println("\tTotal: " + total);                        
        System.out.println("\tAccuracy: " + correct/total);                
        
    }
    
    final public int decode(final Token pred, final int[] feature) {
        final int roleset = pred.pred;        
//        final ArrayList<Integer> possible_roles = FrameDict.get(pred.plemma, roleset);
        final ArrayList<Integer> possible_roles = RoleDict.rolearray;
        int best_role = -1;
        float best_score = -1000000.0f;
        
        for (int i=0; i<possible_roles.size(); ++i) {
            final int role = possible_roles.get(i);
            final float score = calcScore(feature, role);
            
            if (score > best_score) {
                best_score = score;
                best_role = role;
            }
        }
        
        return best_role;
    }
    
    final public int[] extractFirstOrdFeature(final Sentence sentence,
                                                final int prd,
                                                final int arg) {
        return feature_extracter.extractFirstOrdFeature(sentence, prd, arg);
    }
    
    final private int[] extractSecondOrdFeature(final Sentence sentence,
                                                   final HashMap<String, Integer>[] graph){
        return feature_extracter.extractSecondOrdFeature(sentence, graph);
    }
        
    final private float calcScore(final int[] feature, final int role){
        return perceptron.calcScore(feature, role);
    }
    
    final public boolean checkArguments(final Sentence sentence) {
        for (int j=0; j<sentence.preds.length; ++j) {        
            final Token pred = sentence.tokens.get(sentence.preds[j]);
            if (pred.arguments.isEmpty()) return true;
        }
        return false;
    }    
    
}
