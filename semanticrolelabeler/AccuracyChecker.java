/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import argumentidentifier.ArgumentIdentifier;
import java.util.ArrayList;

/**
 *
 * @author hiroki
 */
final public class AccuracyChecker {
    public BaseParser b_parser;
    public HillClimbParser h_parser;
    public long time;

    public AccuracyChecker(){
    }
    
    final public void testHill(final ArrayList<Sentence> sentencelist,
                                final HillClimbParser p){
        time = (long) 0.0;
        
        h_parser = new HillClimbParser(p.perceptron.weight[0].length);
        h_parser.perceptron.weight = averagingWeights(p.perceptron);
        
        for (int i=0; i<sentencelist.size(); i++){
            Sentence sentence = sentencelist.get(i);
            
            if (h_parser.feature_extracter.g_cache.size() < i+1)
                h_parser.feature_extracter.g_cache.add(new String[sentence.preds.length][sentence.size()][]);
            
            if (sentence.preds.length == 0) continue;
            if (h_parser.checkArguments(sentence)) continue;

            long time1 = System.currentTimeMillis();
            final int[][][] features = h_parser.createFeatures(sentence);            
            ArrayList<Integer>[] best_graph = h_parser.decode(sentence, features, 1);
            long time2 = System.currentTimeMillis();

            time += time2 - time1;
            h_parser.checkAccuracy(sentence.o_graph, best_graph);
            
            if (i%100 == 0 && i != 0)
                System.out.print(String.format("%d ", i));            
        }
        
        System.out.println("\n\tTest Correct: " + h_parser.correct);                        
        System.out.println("\tTest Total: " + h_parser.total);                        
        System.out.println("\tTest Accuracy: " + h_parser.correct/h_parser.total);
        System.out.println("\tTest Speed: " + time);        
    }

    final public void testBase(final ArrayList<Sentence> sentencelist,
                                final BaseParser p){
        time = (long) 0.0;
        
        b_parser = new BaseParser(p.perceptron.weight.length, p.perceptron.weight[0].length);
        b_parser.perceptron.weight = averagingWeights(p.perceptron);
        b_parser.correct = 0.0f;
        b_parser.p_total = 0.0f;
        b_parser.r_total = 0.0f;

        for (int i=0; i<sentencelist.size(); i++){
            Sentence sentence = sentencelist.get(i);
            final ArrayList<Token> tokens = sentence.tokens;
            final int[] preds = sentence.preds;
            
            if (b_parser.feature_extracter.g_cache.size() < i+1)
                b_parser.feature_extracter.g_cache.add(new String[sentence.preds.length][sentence.size()][]);
            
            if (sentence.preds.length == 0) continue;
            if (b_parser.checkArguments(sentence)) continue;

            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
                final Token pred = sentence.tokens.get(preds[prd_i]);
                ArrayList<Integer> arguments = pred.parguments;
                final String pos = b_parser.feature_extracter.pos(tokens.get(preds[prd_i]));

                for (int arg_i=1; arg_i<arguments.size(); ++arg_i) {
                    final Token arg = sentence.tokens.get(arguments.get(arg_i));
                    long time1 = System.currentTimeMillis();
                    final String[] i_feature = b_parser.feature_extracter.instantiatePredFirstOrdFeature(sentence, prd_i, arg_i);
                    final String[] c_feature = b_parser.feature_extracter.conjoin(i_feature, pos);
                    final int[] feature = b_parser.feature_extracter.encodeFeature2(c_feature);
                    final int label = b_parser.decode(pred, feature);
                    long time2 = System.currentTimeMillis();
                    time += time2 - time1;
                    final int o_label = arg.apred[prd_i];
                                
                    if (o_label == label) {
                        b_parser.correct += 1.0;
                        
                    }

                    b_parser.p_total += 1.0;                        
                }
                
                arguments = pred.arguments;
                for (int arg_i=1; arg_i<arguments.size(); ++arg_i) {
                    final Token arg = sentence.tokens.get(arguments.get(arg_i));
                    final int o_label = arg.apred[prd_i];
                    if (o_label > -1) b_parser.r_total += 1.0f;
                }                
            }
            
            if (i%100 == 0 && i != 0)
                System.out.print(String.format("%d ", i));            
        }
        
        float pre = b_parser.correct/b_parser.p_total;
        float re = b_parser.correct/b_parser.r_total;
        System.out.println("\n\tTest Correct: " + b_parser.correct);
        System.out.println("\tTest R_Total: " + b_parser.r_total);
        System.out.println("\tTest P_Total: " + b_parser.p_total);
        System.out.println("\tTest Precision: " + pre);
        System.out.println("\tTest Recall: " + re);
        System.out.println("\tTest F1: " + (2*pre*re)/(pre+re));
        System.out.println("\tTest Speed: " + time);        
    }
    
    final public void testAI(final ArrayList<Sentence> sentencelist,
                              final ArgumentIdentifier ai){
        time = (long) 0.0;
        
        ArgumentIdentifier identifier = new ArgumentIdentifier(ai.perceptron.weight.length);
        identifier.perceptron.weight = averagingWeights(ai.perceptron);
        identifier.correct = 0.0f;
        identifier.p_total = 0.0f;
        identifier.r_total = 0.0f;

        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);
            final int[] preds = sentence.preds;
            final ArrayList<Token> tokens = sentence.tokens;

            if (identifier.feature_extracter.g_cache.size() < i+1)
                identifier.feature_extracter.g_cache.add(new String[sentence.preds.length][sentence.size()][]);

            if (sentence.preds.length == 0) continue;
            if (identifier.checkArguments(sentence)) continue;
            
            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
            
                for (int arg=1; arg<sentence.size(); ++arg) {            
                    final Token token = tokens.get(arg);                            
                    final int[] features = identifier.extractFeatures(sentence, prd_i, arg);
                    final float score = identifier.perceptron.calcScore(features);
                    final int label = identifier.sign(score);
                    final int o_label = identifier.sign((float) token.apred[prd_i]);
                    
                    
                    if (label == o_label) {           
                        if (label == 1) {
                            identifier.correct += 1.0;
                            identifier.p_total += 1.0f;
                        }
                    }
                    else {
                        if (score >= 0) {
                            identifier.p_total += 1.0f;
                        }
                    }            

                    if (o_label == 1) identifier.r_total += 1.0;
                }
            }
            
            if (i%1000 == 0 && i != 0)
                System.out.print(String.format("%d ", i));
        }

        float p = identifier.correct/identifier.p_total;
        float r = identifier.correct/identifier.r_total;
        System.out.println("\n\tAI Test Correct: " + identifier.correct);
        System.out.println("\tAI Test R_Total: " + identifier.r_total);
        System.out.println("\tAI Test P_Total: " + identifier.p_total);
        System.out.println("\tAI Test Precision: " + p);
        System.out.println("\tAI Test Recall: " + r);
        System.out.println("\tAI Test F1: " + (2*p*r)/(p+r));
    
    }

    final public void identify(final ArrayList<Sentence> sentencelist,
                                final ArgumentIdentifier ai){
        time = (long) 0.0;
        
        ArgumentIdentifier identifier = new ArgumentIdentifier(ai.perceptron.weight.length);
        identifier.perceptron.weight = averagingWeights(ai.perceptron);
        identifier.correct = 0.0f;
        identifier.p_total = 0.0f;
        identifier.r_total = 0.0f;

        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);
            final int[] preds = sentence.preds;
            final ArrayList<Token> tokens = sentence.tokens;

            if (identifier.feature_extracter.g_cache.size() < i+1)
                identifier.feature_extracter.g_cache.add(new String[sentence.preds.length][sentence.size()][]);

            if (sentence.preds.length == 0) continue;
            if (identifier.checkArguments(sentence)) continue;
            
            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
            
                for (int arg=1; arg<sentence.size(); ++arg) {            
                    final Token token = tokens.get(arg);                            
                    final int[] features = identifier.extractFeatures(sentence, prd_i, arg);
                    final float score = identifier.perceptron.calcScore(features);
                    final int label = identifier.sign(score);
                    
                    if (token.papred == null)
                        token.papred = new int[preds.length];
                    if (label < 0)
                        token.papred[prd_i] = label;
                    else
                        token.papred[prd_i] = arg;
                }
            }
            
            if (i%1000 == 0 && i != 0)
                System.out.print(String.format("%d ", i));
        }
    }
    
    
/*    final public void outputPerceptron(final String fn){
        try {      
            try (ObjectOutputStream objOutStream =
                    new ObjectOutputStream(
                    new FileOutputStream(fn+"_perceptron.bin"))) {
                objOutStream.writeObject(tagger.perceptron);
                objOutStream.close();
            }
      
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
*/    

    final private float[][] averagingWeights(final MultiClassPerceptron p){
        final float[][] new_weight = new float[p.weight.length][p.weight[0].length];
        
        for (int i = 0;i<p.weight.length;i++) {
            final float[] tmp_new_weight = new_weight[i];
            final float[] tmp_weight = p.weight[i];
            final float[] tmp_aweight = p.aweight[i];
            
            for (int j = 0; j<tmp_weight.length; ++j)
                tmp_new_weight[j] = tmp_weight[j] - tmp_aweight[j] /p.t;
        }

        return new_weight;
    }

    final private float[] averagingWeights(final Perceptron p){
        final float[] new_weight = new float[p.weight.length];
        
        for (int i = 0;i<p.weight.length;i++) {
            new_weight[i] = p.weight[i] - p.aweight[i] /p.t;
        }

        return new_weight;
    }    
    
}
