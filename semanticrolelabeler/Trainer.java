/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package semanticrolelabeler;

import java.util.ArrayList;

/**
 *
 * @author hiroki
 */
final public class Trainer {
    final public ArrayList<Sentence> sentencelist;
    public float correct, total;
    public BaseParser baseparser;
    public HillClimbParser hillparser;
    
    public Trainer(final ArrayList<Sentence> sentencelist,
                    final int weight_length,
                    final int restart) {
        this.sentencelist = sentencelist;
        this.hillparser = new HillClimbParser(weight_length, restart);
    }

    public Trainer(final ArrayList<Sentence> sentencelist,
                    final int weight_length) {
        this.sentencelist = sentencelist;
        this.baseparser = new BaseParser(RoleDict.size(), weight_length);
    }
    
    final public void train(){
        if (baseparser != null)
            baseparser.train(sentencelist);
        else
            hillparser.trainSecond(sentencelist);
//            hillparser.train(sentencelist);
    }

}
