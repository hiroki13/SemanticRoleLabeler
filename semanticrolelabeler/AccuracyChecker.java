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

    
    final public void testBase(final ArrayList<Sentence> testsentencelist,
                                final ArrayList<Sentence> evalsentencelist,
                                final BaseParser p){
        time = (long) 0.0;
        
        b_parser = new BaseParser(p.perceptron.weight.length, p.perceptron.weight[0].length);
        b_parser.perceptron.weight = averagingWeights(p.perceptron);
        b_parser.test(testsentencelist);
        b_parser.eval(testsentencelist, evalsentencelist);                
    }
    
    
    final public void testAI(final ArrayList<Sentence> testsentencelist,
                              final ArrayList<Sentence> evalsentencelist,
                              final ArgumentIdentifier ai){
        ArgumentIdentifier identifier = new ArgumentIdentifier(ai.perceptron.weight.length);
        identifier.perceptron.weight = averagingWeights(ai.perceptron);
        identifier.test(testsentencelist);
        identifier.eval(testsentencelist, evalsentencelist);
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
