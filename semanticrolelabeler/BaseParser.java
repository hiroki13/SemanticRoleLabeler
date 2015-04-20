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
    public long time;
    
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

                for (int arg_i=0; arg_i<arguments.size(); ++arg_i) {
                    final Token arg = sentence.tokens.get(arguments.get(arg_i));
                    final String[] i_feature = feature_extracter.instantiateFirstOrdFeature(sentence, prd_i, arg_i);
//                    final String[] c_feature = feature_extracter.conjoin(i_feature, pos);
                    final int[] feature = feature_extracter.encodeFeature2(i_feature);
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
    
    final public void test(final ArrayList<Sentence> testsentencelist) {
        time = (long) 0.0;
        
        for (int i=0; i<testsentencelist.size(); i++){
            Sentence testsentence = testsentencelist.get(i);
            testsentence.initializePapred();
            
            final ArrayList<Token> tokens = testsentence.tokens;
            final int[] preds = testsentence.preds;
            
            if (feature_extracter.g_cache.size() < i+1)
                feature_extracter.g_cache.add(new String[testsentence.preds.length][testsentence.size()][]);
            
            if (testsentence.preds.length == 0) continue;

            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
                final Token pred = testsentence.tokens.get(preds[prd_i]);
                ArrayList<Integer> arguments = pred.parguments;
                
                final String pos = feature_extracter.pos(tokens.get(preds[prd_i]));

                for (int arg_i=0; arg_i<arguments.size(); ++arg_i) {
                    final Token arg = testsentence.tokens.get(arguments.get(arg_i));

                    long time1 = System.currentTimeMillis();
                    final String[] i_feature = feature_extracter.instantiatePredFirstOrdFeature(testsentence, prd_i, arg_i);
//                    final String[] c_feature = feature_extracter.conjoin(i_feature, pos);
                    final int[] feature = feature_extracter.encodeFeature2(i_feature);
                    final int label = decode(pred, feature);
                    arg.papred[prd_i] = label;
                    long time2 = System.currentTimeMillis();                    
                    time += time2 - time1;
                }
                
            }
            
            if (i%100 == 0 && i != 0)
                System.out.print(String.format("%d ", i));            
        }

    }    

    
    final public void eval(final ArrayList<Sentence> testsentencelist,
                            final ArrayList<Sentence> evalsentencelist) {
        correct = 0.0f;
        p_total = 0.0f;
        r_total = 0.0f;

        for (int i=0; i<evalsentencelist.size(); i++){
            final Sentence evalsentence = evalsentencelist.get(i);
            final Sentence testsentence = testsentencelist.get(i);
            final int[] preds = evalsentence.preds;
            
            if (evalsentence.preds.length == 0) continue;

            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
                final Token o_pred = evalsentence.tokens.get(preds[prd_i]);
                final ArrayList<Integer> arguments = o_pred.arguments;
                r_total += arguments.size();

                for (int arg_i=0; arg_i<arguments.size(); ++arg_i) {
                    final Token o_arg = evalsentence.tokens.get(arguments.get(arg_i));
                    final Token arg = getArg(testsentence, prd_i, o_arg);
                    final int o_label = o_arg.apred[prd_i];
                    final int label;
                    
                    if (arg != null) {
                        label = arg.papred[prd_i];
                    }
                    else label = -1;
                    
                    if (label > -1 && o_label == label)
                        correct += 1.0;
                }

                final Token pred = testsentence.tokens.get(preds[prd_i]);
                p_total += pred.parguments.size();                
                
            }
        }

        float p = correct/p_total;
        float r = correct/r_total;
        System.out.println("\n\tTest Correct: " + correct);
        System.out.println("\tTest R_Total: " + r_total);
        System.out.println("\tTest P_Total: " + p_total);
        System.out.println("\tTest Precision: " + p);
        System.out.println("\tTest Recall: " + r);
        System.out.println("\tTest F1: " + (2*p*r)/(p+r));
        System.out.println("\tTest Speed: " + time);                
        
    }
    
    final private Token getArg(final Sentence testsentence, final int prd_i, final Token o_arg) {
        final ArrayList<Integer> arguments = testsentence.tokens.get(testsentence.preds[prd_i]).parguments;
        for (int i=0; i<arguments.size(); ++i) {
            final Token arg = testsentence.tokens.get(arguments.get(i));
            
            if (arg.id == o_arg.id) return arg;
        }
        
        return null;
    }

    
    final public int decode(final Token pred, final int[] feature) {
//        final int roleset = pred.pred;        
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
