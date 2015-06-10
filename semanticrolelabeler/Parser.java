/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import feature.FeatureExtractor;
import io.Sentence;
import java.util.ArrayList;
import java.util.Random;
import learning.Classifier;

/**
 *
 * @author hiroki
 */
public class Parser {

    public Classifier classifier;
    public FeatureExtractor feature_extracter;
    public Random rnd;
    public int weight_length, restart, prune = -1;
    public float correct, total, p_total, r_total;
    public long time;

    public Parser() {}
    
    public Parser(final int label_length, final int weight_length, final int prune) {}
    
    public Parser(final int label_length, final int weight_length, final int restart, final int prune) {}
    
    public Parser(final Classifier c, final int weight_length, final int prune) {}
    
    public Parser(final Classifier c, final int weight_length, final int restart, final int prune) {}
    
    public void train(final ArrayList<Sentence> sentencelist) {}
    
    public void trainSecond(final ArrayList<Sentence> sentencelist) {}
    
    public void test(final ArrayList<Sentence> testsentencelist) {}

    public void testSecond(final ArrayList<Sentence> testsentencelist) {}

    public void eval(final ArrayList<Sentence> testsentencelist, final ArrayList<Sentence> evalsentencelist) {}
}
