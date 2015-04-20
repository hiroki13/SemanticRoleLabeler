/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package argumentidentifier;

import java.util.ArrayList;
import semanticrolelabeler.FeatureExtracter;
import semanticrolelabeler.Perceptron;
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
            
                for (int arg=1; arg<sentence.size(); ++arg) {            
                    final Token token = tokens.get(arg);                            
                    final int[] features = extractFeatures(sentence, prd_i, arg);
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
                    final int[] features = extractFeatures(sentence, prd_i, arg_id);
                    final float score = perceptron.calcScore(features);
                    final int label = sign(score);
                    long time2 = System.currentTimeMillis();                    
                    time += time2 - time1;
                    
                    if (label == 1) pred.parguments.add(arg_id);
                }
            }
            
            if (i%1000 == 0 && i != 0)
                System.out.print(String.format("%d ", i));
        }
        
    }

    final public void eval(final ArrayList<Sentence> testsentencelist,
                            final ArrayList<Sentence> evalsentencelist) {
        time = (long) 0.0;
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
                
                for (int j=0; j<pred.parguments.size(); ++j) {
                    final int arg_id = pred.parguments.get(j);
                    
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
