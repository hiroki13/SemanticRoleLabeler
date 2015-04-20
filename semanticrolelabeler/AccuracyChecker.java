/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import argumentidentifier.ArgumentIdentifier;
import java.util.ArrayList;
import java.util.HashMap;
import predicatedisambiguator.PredicateDisambiguator;

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
    
    final public void testHill(final ArrayList<Sentence> testsentencelist,
                                final ArrayList<Sentence> evalsentencelist,
                                final HillClimbParser p){
        h_parser = new HillClimbParser(p.perceptron.weight[0].length);
        h_parser.perceptron.weight = averagingWeights(p.perceptron);
        h_parser.test(testsentencelist);
        h_parser.eval(testsentencelist, evalsentencelist);                
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
                              final ArgumentIdentifier tmp_ai){
        ArgumentIdentifier ai = new ArgumentIdentifier(tmp_ai.perceptron.weight.length);
        ai.perceptron.weight = averagingWeights(tmp_ai.perceptron);
        ai.test(testsentencelist);
        ai.eval(testsentencelist, evalsentencelist);
    }
    
    final public void testPD(final ArrayList<Sentence> testsentencelist,
                              final ArrayList<Sentence> evalsentencelist,
                              final PredicateDisambiguator tmp_pd){
        PredicateDisambiguator pd = new PredicateDisambiguator(tmp_pd.weight_length);
        pd.perceptrons = getAvePerceptrons(tmp_pd);
        pd.test(testsentencelist);
        pd.eval(testsentencelist, evalsentencelist);
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

    
    final private HashMap<String, MultiClassPerceptron> getAvePerceptrons(final PredicateDisambiguator pd) {
        final HashMap<String, MultiClassPerceptron> new_ps = new HashMap();
        
        for (String lemma:pd.perceptrons.keySet()) {
            final MultiClassPerceptron p = pd.perceptrons.get(lemma);
            final MultiClassPerceptron tmp_new_p = new MultiClassPerceptron(p.weight.length, p.weight[0].length);
            tmp_new_p.weight = averagingWeights(p);
            new_ps.put(lemma, tmp_new_p);
        }
        
        return new_ps;
    }
    
    final private float[][] averagingWeights(final MultiClassPerceptron p){
        final float[][] new_weight = new float[p.weight.length][p.weight[0].length];
        
        for (int i = 0; i<p.weight.length; i++) {
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
