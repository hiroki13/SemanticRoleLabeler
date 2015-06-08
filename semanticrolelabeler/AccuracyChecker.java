/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import argumentidentifier.ArgumentIdentifier;
import io.RoleDict;
import io.SenseDict;
import io.Sentence;
import io.Token;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import learning.Classifier;
import learning.MultiClassPerceptron;
import learning.Perceptron;
import predicatedisambiguator.PredicateDisambiguator;

/**
 *
 * @author hiroki
 */
final public class AccuracyChecker {
    public Parser parser;
    public long time;

    public AccuracyChecker() {}
        
    final public void test(final ArrayList<Sentence> testsentencelist,
                            final ArrayList<Sentence> evalsentencelist,
                            final Parser p, final String p_name){
        if ("hill".equals(p_name)) {
            final Classifier c = new MultiClassPerceptron(RoleDict.biroledict.size(), p.weight_length);
            parser = new HillClimbParser(c, p.weight_length, p.restart, p.prune);
            parser.classifier.weight = averagingWeights(p.classifier);
//            parser.test(testsentencelist);
            parser.testSecond(testsentencelist);
        }
        else {
            final Classifier c = new MultiClassPerceptron(RoleDict.roledict.size(), p.weight_length);
            parser = new BaseParser(c, p.weight_length, p.prune);
            parser.classifier.weight = averagingWeights(p.classifier);
            parser.test(testsentencelist);
        }

        parser.eval(testsentencelist, evalsentencelist);                
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
    
    
    final public void output(final ArrayList<Sentence> sentencelist, final String fn) throws IOException {
        PrintWriter pw = new PrintWriter(new BufferedWriter
                                        (new FileWriter(fn + "-output.txt")));
        
        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);

            for (int j=1; j<sentence.size(); ++j) {
                final Token t = sentence.tokens.get(j);
                String text = "";
                text += t.id + "\t";
                text += t.form + "\t";
                text += t.lemma + "\t";
                text += t.plemma + "\t";
                text += t.pos + "\t";
                text += t.ppos + "\t";
                text += t.feat + "\t";
                text += t.pfeat + "\t";
                text += t.head + "\t";
                text += t.phead + "\t";
                text += t.deprel + "\t";
                text += t.pdeprel + "\t";
                text += t.fillpred + "\t";
                text += setPred(t);
                text += setArgument(sentence, t);
                pw.println(text);
            }

            pw.println();
        }
        pw.close();
    }

    
    
    final public void outputBase(final ArrayList<Sentence> sentencelist, final String fn) throws IOException {
        PrintWriter pw = new PrintWriter(new BufferedWriter
                                        (new FileWriter(fn + "-output.txt")));
        
        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);

            for (int j=1; j<sentence.size(); ++j) {
                final Token t = sentence.tokens.get(j);
                String text = "";
                text += t.id + "\t";
                text += t.form + "\t";
                text += t.lemma + "\t";
                text += t.plemma + "\t";
                text += t.pos + "\t";
                text += t.ppos + "\t";
                text += t.feat + "\t";
                text += t.pfeat + "\t";
                text += t.head + "\t";
                text += t.phead + "\t";
                text += t.deprel + "\t";
                text += t.pdeprel + "\t";
                text += t.fillpred + "\t";
                text += setPred(t);
                text += setArgument(t);
                pw.println(text);
            }

            pw.println();
        }
        pw.close();
    }

    final public void outputAI(final ArrayList<Sentence> sentencelist, final String fn) throws IOException {
        PrintWriter pw = new PrintWriter(new BufferedWriter
                                        (new FileWriter(fn + "-ai-output.txt")));
        
        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);

            for (int j=1; j<sentence.size(); ++j) {
                final Token t = sentence.tokens.get(j);
                String text = "";
                text += t.id + "\t";
                text += t.form + "\t";
                text += t.lemma + "\t";
                text += t.plemma + "\t";
                text += t.pos + "\t";
                text += t.ppos + "\t";
                text += t.feat + "\t";
                text += t.pfeat + "\t";
                text += t.head + "\t";
                text += t.phead + "\t";
                text += t.deprel + "\t";
                text += t.pdeprel + "\t";
                text += t.fillpred + "\t";
                text += setPred(t);
                text += setArgument(sentence, j);
                pw.println(text);
            }

            pw.println();
        }
        pw.close();
    }

    
    final public String setArgument(final Sentence sentence, final Token t) {
        final int[] preds = sentence.preds;
        final int[][] graph = sentence.p_graph;
        String text = "";

        if (graph == null) {
            for (int i=0; i<preds.length; ++i) text += "_\t";
            return text;
        }
          
        
        for (int i=0; i<graph.length; ++i) {
            boolean flag = true;
            final Token pred = sentence.tokens.get(preds[i]);
            final int[] t_graph = graph[i];
            final ArrayList<Integer> arguments = pred.arguments;
            final int arg_length = arguments.size();
            
            for (int j=0; j<arg_length; ++j) {
                final int arg_id = arguments.get(j);
                
                if (t.id != arg_id) continue;
                
                final int role_id = t_graph[j];
                final String role = RoleDict.roledict.get(role_id);
                
                if (!"NULL".equals(role)) text += role + "\t";
                else text += "_\t";
                
                flag = false;
                break;
            }
            if (flag) text += "_\t";
        }
        return text;
    }

    final public String setArgument(final Token t) {
        String text = "";
        
        for (int i=0; i<t.apred.length; ++i) {
            final int role_id = t.apred[i];
            if (role_id < 0) text += "_\t";
            else {
                String role = RoleDict.roledict.get(role_id);
                
                if (!"NULL".equals(role)) text += role + "\t";
                else text += "_\t";
            }
        }
        return text;
    }
    
    final public String setArgument(final Sentence sentence, final int target_arg_id) {
        String text = "";
        final int[] preds = sentence.preds;
        
        for (int i=0; i<preds.length; ++i) {
            final Token pred = sentence.tokens.get(preds[i]);
            final ArrayList<Integer> arguments = pred.arguments;
            boolean flag = true;
            
            for (int j=0; j<arguments.size(); ++j) {
                final int arg_id = arguments.get(j);
                
                if (target_arg_id == arg_id) {
                    text += "1\t";
                    flag = false;
                    break;
                }
            }
            if (flag) text += "_\t";
        }
        return text;
    }
    
    final public String setPred(final Token t) {
        if (t.pred < 0) return "_\t";
        final HashMap<Integer, Integer> sensedict = SenseDict.sensedict.get(t.cpos);
        
        for (Map.Entry<Integer,Integer> entry:sensedict.entrySet()) {
            final int t_sense = entry.getKey();
            final int id = entry.getValue();
            if (t.pred == id) {
                String sense = t.plemma + ".0" + t_sense + "\t";
                return sense;
            }
        }
        return "error";
    }
    
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
    
    final private float[][] averagingWeights(final Classifier c){
        final float[][] new_weight = new float[c.weight.length][c.weight[0].length];
        
        for (int i = 0; i<c.weight.length; i++) {
            final float[] tmp_new_weight = new_weight[i];
            final float[] tmp_weight = c.weight[i];
            final float[] tmp_aweight = c.aweight[i];
            
            for (int j = 0; j<tmp_weight.length; ++j)
                tmp_new_weight[j] = tmp_weight[j] - tmp_aweight[j] /c.t;
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
