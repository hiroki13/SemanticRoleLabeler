/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package argumentidentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import semanticrolelabeler.FeatureExtracter;
import semanticrolelabeler.Perceptron;
import semanticrolelabeler.RoleDict;
import semanticrolelabeler.Sentence;
import semanticrolelabeler.Token;

/**
 *
 * @author hiroki
 */
public class ArgumentIdentifier {
    
    final public Perceptron perceptron;
    final public FeatureExtracter feature_extracter;
    public float correct, p_total, r_total;
    public float correct1, correct2, correct3, total1, total2, total3;
    public long time;
    
    public ArgumentIdentifier(final int weight_length) {
        perceptron = new Perceptron(weight_length);
        feature_extracter = new FeatureExtracter(weight_length);
        feature_extracter.g_cache = new ArrayList();
    }
    
    final public void train(final ArrayList<Sentence> sentencelist) {
        correct = 0.0f;
        p_total = 0.0f;
        r_total = 0.0f;
        
        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);
            final int[] preds = sentence.preds;
            final ArrayList<Token> tokens = sentence.tokens;

            if (feature_extracter.g_cache.size() < i+1)
                feature_extracter.g_cache.add(new String[sentence.preds.length][sentence.size()][]);

            if (sentence.preds.length == 0) continue;
            if (checkArguments(sentence)) continue;
            
            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
            
                for (int arg_id=1; arg_id<sentence.size(); ++arg_id) {            
                    final Token token = tokens.get(arg_id);                            
                    final int[] features = extractFeatures(sentence, prd_i, arg_id);
                    final float score = perceptron.calcScore(features);
                    final int label = sign(score);
                    final int o_label = sign((float) token.apred[prd_i]);
                    
                    
                    if (label == o_label) {           
                        if (label == 1) {
                            correct += 1.0;
                            p_total += 1.0f;
                        }
                        perceptron.updateWeights(0, features);
                    }
                    else {
                        if (score >= 0) {
                            p_total += 1.0f;
                            perceptron.updateWeights(-1, features);
                        }
                        else perceptron.updateWeights(1, features);
                    }            

                    
                    if (o_label == 1) r_total += 1.0;
                }
            }
            
            if (i%1000 == 0 && i != 0)
                System.out.print(String.format("%d ", i));
        }

        float p = correct/p_total;
        float r = correct/r_total;
        System.out.println("\n\tAI Train Correct: " + correct);
        System.out.println("\tAI Train R_Total: " + r_total);
        System.out.println("\tAI Train P_Total: " + p_total);
        System.out.println("\tAI Train Precision: " + p);
        System.out.println("\tAI Train Recall: " + r);
        System.out.println("\tAI Train F1: " + (2*p*r)/(p+r));
    }
    
    
    final public void test(final ArrayList<Sentence> testsentencelist) {
        time = (long) 0.0;

        for (int i=0; i<testsentencelist.size(); ++i) {
            final Sentence sentence = testsentencelist.get(i);
            
            sentence.initializeParguments();
            
            final int[] preds = sentence.preds;
            final ArrayList<Token> tokens = sentence.tokens;

            if (feature_extracter.g_cache.size() < i+1)
                feature_extracter.g_cache.add(new String[sentence.preds.length][sentence.size()][]);

            if (sentence.preds.length == 0) continue;
            
            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
                final Token pred = tokens.get(preds[prd_i]);
            
                for (int arg_id=1; arg_id<sentence.size(); ++arg_id) {            
                    long time1 = System.currentTimeMillis();
                    final int[] features = feature_extracter.extractAIFeature(sentence, prd_i, arg_id);
                    final float score = perceptron.calcScore(features);
                    final int label = sign(score);
                    long time2 = System.currentTimeMillis();                    
                    time += time2 - time1;
                    
                    if (label == 1) pred.arguments.add(arg_id);
                }
            }
            
            if (i%1000 == 0 && i != 0)
                System.out.print(String.format("%d ", i));
        }
        
    }

    final public void eval(final ArrayList<Sentence> testsentencelist,
                            final ArrayList<Sentence> evalsentencelist) {
        correct = 0.0f;
        p_total = 0.0f;
        r_total = 0.0f;

        for (int i=0; i<testsentencelist.size(); ++i) {
            final Sentence testsentence = testsentencelist.get(i);
            final Sentence evalsentence = evalsentencelist.get(i);

            final int[] preds = testsentence.preds;
            final ArrayList<Token> tokens = testsentence.tokens;
            final ArrayList<Token> o_tokens = evalsentence.tokens;

            if (testsentence.preds.length == 0) continue;
            
            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
                final Token pred = tokens.get(preds[prd_i]);                            
                final Token o_pred = o_tokens.get(preds[prd_i]);
                
                for (int j=0; j<pred.arguments.size(); ++j) {
                    final int arg_id = pred.arguments.get(j);
                    
                    if (o_pred.arguments.contains(arg_id)) correct += 1.0;
                    p_total += 1.0;
                }
                
                r_total += o_pred.arguments.size();            

            }
            
        }

        float p = correct/p_total;
        float r = correct/r_total;
        System.out.println("\n\tAI Test Correct: " + correct);
        System.out.println("\tAI Test R_Total: " + r_total);
        System.out.println("\tAI Test P_Total: " + p_total);
        System.out.println("\tAI Test Precision: " + p);
        System.out.println("\tAI Test Recall: " + r);
        System.out.println("\tAI Test F1: " + (2*p*r)/(p+r));
        
    }
    
    final public void confusionMatrix(final ArrayList<Sentence> testsentencelist,
                                        final ArrayList<Sentence> evalsentencelist) {
        final HashMap<String, float[]> cm = new HashMap();
        final HashMap<String, float[]> cm_n = new HashMap();
        final HashMap<String, float[]> cm_v = new HashMap();

        for (int i=0; i<testsentencelist.size(); ++i) {
            final Sentence testsentence = testsentencelist.get(i);
            final Sentence evalsentence = evalsentencelist.get(i);

            final int[] preds = testsentence.preds;
            final ArrayList<Token> tokens = testsentence.tokens;
            final ArrayList<Token> o_tokens = evalsentence.tokens;

            if (testsentence.preds.length == 0) continue;
            
            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
                final Token pred = tokens.get(preds[prd_i]);                            
                final Token o_pred = o_tokens.get(preds[prd_i]);
                
                for (int j=0; j<pred.arguments.size(); ++j) {
                    final int arg_id = pred.arguments.get(j);
                    final Token o_arg = o_tokens.get(arg_id);
                    final int role_id = o_arg.apred[prd_i];
                    final String role;
                    
                    if (role_id > -1) role = RoleDict.roledict.get(role_id);
                    else role = "NONE";

                    if (!cm.containsKey(role)) cm.put(role, new float[2]);                    
                    if (!pred.pos.startsWith("V") && !cm_n.containsKey(role)) cm_n.put(role, new float[2]);                    
                    else if (pred.pos.startsWith("V") && !cm_v.containsKey(role)) cm_v.put(role, new float[2]);                    

                    final float[] tmp = cm.get(role);
                    final float[] tmp2 = cm_n.get(role);
                    final float[] tmp3 = cm_v.get(role);
                    
                    if (o_pred.arguments.contains(arg_id)) {
                        tmp[0] += 1.0f;
                        if (!pred.pos.startsWith("V")) tmp2[0] += 1.0f;
                        else tmp3[0] += 1.0f;
                    }
                }
            }
            
            for (int j=1; j<o_tokens.size(); ++j) {
                final Token token = o_tokens.get(j);
                final int[] apred = token.apred;

                for (int k=0; k<apred.length; ++k) {
                    final Token pred = o_tokens.get(evalsentence.preds[k]);
                    final int role_id = apred[k];
                    final String role;
                    
                    if (role_id > -1) role = RoleDict.roledict.get(role_id);
                    else role = "NONE";
                    
                    if (!cm.containsKey(role)) cm.put(role, new float[2]);                    
                    if (!pred.pos.startsWith("V") && !cm_n.containsKey(role)) cm_n.put(role, new float[2]);                    
                    else if (pred.pos.startsWith("V") && !cm_v.containsKey(role)) cm_v.put(role, new float[2]);                    

                    final float[] tmp = cm.get(role);
                    final float[] tmp2 = cm_n.get(role);
                    final float[] tmp3 = cm_v.get(role);

                    tmp[1] += 1.0f;
                    if (!pred.pos.startsWith("V")) tmp2[1] += 1.0f;                    
                    else tmp3[1] += 1.0f;
                }
            }
            
        }
        
        Collections.sort(RoleDict.roledict);
        
        for (int i=0; i<RoleDict.roledict.size(); ++i) {
            final String role = RoleDict.roledict.get(i);
            float[] value1 = new float[2];
            float[] value2 = new float[2];
            float[] value3 = new float[2];
            if (cm.containsKey(role)) value1 = cm.get(role);
            if (cm_n.containsKey(role)) value2 = cm_n.get(role);
            if (cm_v.containsKey(role)) value3 = cm_v.get(role);
            
            showAccuracy(role, value1, value2, value3);
        }
        
        System.out.println("\n\tRole:\tTOTAL");
        System.out.println("\tCorrect:\t" + (int) correct1 + "\t" + (int) correct2 + "\t" + (int) correct3);
        System.out.println("\tTotal:\t" + (int) total1 + "\t" + (int) total2 + "\t" + (int) total3);
        System.out.println("\tAccuracy:\t" + correct1/total1 + "\t" + correct2/total2 + "\t" + correct3/total3);
        
    }

    final public void showF1(final String label, final float correct,
                              final float p_total, final float r_total) {
        float p = correct/p_total;
        float r = correct/r_total;
        System.out.println("\n\tRole: " + label);
        System.out.println("\tCorrect: " + (int) correct);
        System.out.println("\tR_Total: " + (int) r_total);
        System.out.println("\tP_Total: " + (int) p_total);
        System.out.println("\tPrecision: " + p);
        System.out.println("\tRecall: " + r);
        System.out.println("\tF1: " + (2*p*r)/(p+r));
                
    }
    
    final public void showAccuracy(final String label, final float correct,
                                     final float total) {
        System.out.println("\n\tRole: " + label);
        System.out.println("\tCorrect: " + (int) correct);
        System.out.println("\tTotal: " + (int) total);
        System.out.println("\tAccuracy: " + correct/total);
                
    }
    
    final public void showAccuracy(final String label, final float[] value1,
                                     final float[] value2, final float[] value3) {
        final float c1 = value1[0];
        final float c2 = value2[0];
        final float c3 = value3[0];
        final float t1 = value1[1];
        final float t2 = value2[1];
        final float t3 = value3[1];
        System.out.println("\n\tRole:\t" + label);
        System.out.println("\tCorrect:\t" + (int) c1 + "\t" + (int) c2 + "\t" + (int) c3);
        System.out.println("\tTotal:\t" + (int) t1 + "\t" + (int) t2 + "\t" + (int) t3);
        System.out.println("\tAccuracy:\t" + c1/t1 + "\t" + c2/t2 + "\t" + c3/t3);
                
        correct1 += c1;
        correct2 += c2;
        correct3 += c3;
        total1 += t1;
        total2 += t2;
        total3 += t3;
    }
    
    final public int[] extractFeatures(final Sentence sentence, final int prd,
                                         final int arg) {
        return feature_extracter.extractAIFeature(sentence, prd, arg);        
 }
    
    final public int sign(final float score) {
        if (score >= 0) return 1;
        return -1;
    }
    
    final public boolean checkArguments(final Sentence sentence) {
        for (int j=0; j<sentence.preds.length; ++j) {        
            final Token pred = sentence.tokens.get(sentence.preds[j]);
            if (pred.arguments.isEmpty()) return true;
        }
        return false;
    }    
    
}
