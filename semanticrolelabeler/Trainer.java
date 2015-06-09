/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package semanticrolelabeler;

import io.RoleDict;
import io.Sentence;
import java.util.ArrayList;
import learning.Classifier;
import learning.MultiClassPerceptron;
import learning.NeuralNetwork;

/**
 *
 * @author hiroki
 */
final public class Trainer {
    final public ArrayList<Sentence> sentencelist;
    final public Parser parser;
    final public String parser_name;
    
    public Trainer(final ArrayList<Sentence> sentencelist, final Parser p, final String p_name) {
        this.sentencelist = sentencelist;
        this.parser = p;
        this.parser_name = p_name;
    }
    
    public Trainer(final ArrayList<Sentence> sentencelist, final String p_name,
                    final int weight_length, final int restart, final int prune) {
        this.sentencelist = sentencelist;
        this.parser_name = p_name;

        if ("hill".equals(p_name)) {
            Classifier c = new MultiClassPerceptron(RoleDict.biroledict.size(), weight_length);
            parser = new HillClimbParser(c, weight_length, restart, prune);
        }
        else if ("nn".equals(p_name)) {
            Classifier c = new NeuralNetwork(weight_length);
            parser = new HillClimbParser(c, weight_length, restart, prune);

        }
        else {
            Classifier c = new MultiClassPerceptron(RoleDict.roledict.size(), weight_length);
            parser = new BaseParser(c, weight_length, prune);
        }
    }

    final public void train(){
        if ("hill".equals(parser_name)) parser.trainSecond(sentencelist);
        else if ("nn".equals(parser_name)) parser.trainNN(sentencelist);
        else parser.train(sentencelist);
    }

}
