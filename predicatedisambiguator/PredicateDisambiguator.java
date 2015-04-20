/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package predicatedisambiguator;

import java.util.ArrayList;
import java.util.HashMap;
import semanticrolelabeler.FeatureExtracter;
import semanticrolelabeler.FrameDict;
import semanticrolelabeler.MultiClassPerceptron;
import semanticrolelabeler.Sentence;
import semanticrolelabeler.Token;

/**
 *
 * @author hiroki
 */
public class PredicateDisambiguator {

    public HashMap<String, MultiClassPerceptron> perceptrons;
    final public FeatureExtracter feature_extracter;
    final public int weight_length;
    public float correct, total;
    public long time;
    
    public PredicateDisambiguator(final int weight_length) {
        perceptrons = new HashMap();
        this.weight_length = weight_length;
        feature_extracter = new FeatureExtracter(weight_length);
        feature_extracter.pd_cache = new ArrayList();        
    }

    
    final public void train(final ArrayList<Sentence> sentencelist) {
        correct = 0.0f;
        total = 0.0f;
        
        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);
            final int[] preds = sentence.preds;
            final ArrayList<Token> tokens = sentence.tokens;

            if (feature_extracter.pd_cache.size() < i+1) {
                if (sentence.preds.length > 0)
                    feature_extracter.pd_cache.add(new String[sentence.preds.length][]);
                else
                    feature_extracter.pd_cache.add(new String[1][]);
            }
            
            if (sentence.preds.length == 0) continue;
                                    
            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
                final Token pred = tokens.get(preds[prd_i]);
                final HashMap<Integer, ArrayList> frames = FrameDict.addAndGet(pred.cpos);
                
                if (frames.isEmpty()) {
                    System.out.print(String.format("%d ", i));
                    continue;
                }
                
                if (!perceptrons.containsKey(pred.cpos))
                    perceptrons.put(pred.cpos, new MultiClassPerceptron(frames.size(), weight_length));
                
                final MultiClassPerceptron perceptron = perceptrons.get(pred.cpos);
                
                final int[] feature = feature_extracter.extractPDFeature(sentence, prd_i);
                final int best_sense = getBestSense(perceptron, feature, frames);
                
                perceptron.updateWeights(pred.pred, best_sense, feature);

                if (best_sense == pred.pred) correct += 1.0;
                total += 1.0;
            }
            
            if (i%1000 == 0 && i != 0)
                System.out.print(String.format("%d ", i));
        }

        System.out.println("\n\tPD Train Correct: " + correct);
        System.out.println("\tPD Train Accuracy: " + correct/total);
    }

    
    final public void test(final ArrayList<Sentence> testsentencelist) {
        time = (long) 0.0;

        for (int i=0; i<testsentencelist.size(); ++i) {
            final Sentence sentence = testsentencelist.get(i);
            final int[] preds = sentence.preds;
            final ArrayList<Token> tokens = sentence.tokens;

            if (feature_extracter.pd_cache.size() < i+1) {
                if (sentence.preds.length > 0)
                    feature_extracter.pd_cache.add(new String[sentence.preds.length][]);
                else
                    feature_extracter.pd_cache.add(new String[1][]);
            }
            
            if (sentence.preds.length == 0) continue;
            
            sentence.initializePpred();
                                    
            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
                final Token pred = tokens.get(preds[prd_i]);
                
                if (!FrameDict.containsKey(pred.cpos)) {
                    continue;
                }
                
                final HashMap<Integer, ArrayList> frames = FrameDict.get(pred.cpos);
                
                if (!perceptrons.containsKey(pred.cpos))
                    continue;
//                    perceptrons.put(pred.plemma, new MultiClassPerceptron(frames.size(), weight_length));
                
                final MultiClassPerceptron perceptron = perceptrons.get(pred.cpos);
                
                final int[] feature = feature_extracter.extractPDFeature(sentence, prd_i);
                pred.pred = getBestSense(perceptron, feature, frames);
            }
            
            if (i%1000 == 0 && i != 0)
                System.out.print(String.format("%d ", i));
        }
    }

    
    final public void eval(final ArrayList<Sentence> testsentencelist,
                            final ArrayList<Sentence> evalsentencelist) {
        correct = 0.0f;
        total = 0.0f;
        
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
                
                if (pred.pred == o_pred.pred && pred.cpos.equals(o_pred.cpos))
                    correct += 1.0;
                total += 1.0;
            }
        }

        System.out.println("\n\tPD Test Correct: " + correct);
        System.out.println("\tPD Test Accuracy: " + correct/total);
    }
    
    final private int getBestSense(final MultiClassPerceptron perceptron,
                                     final int[] feature,
                                     final HashMap<Integer, ArrayList> frame) {
        float best_score = -1000000.0f;
        int best = -1;
        
        for (int sense:frame.keySet()) {
            final float score = perceptron.calcScore(feature, sense);
            if (score > best_score) {
                best_score = score;
                best = sense;
            }
        }
        
        return best;
    }
    
}
