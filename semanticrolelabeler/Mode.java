/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import argumentidentifier.ArgumentIdentifier;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author hiroki
 */
final public class Mode {
    
    final OptionParser optionparser;
    String modeselect, parserselect;    
    String trainfile, testfile, evalfile, outfile, modelfile, framefile;
    boolean train, test, eval, output, model, frame, check_accuracy;
    int iteration, restart, weight_length;
    ArrayList<Sentence> trainsentence, testsentence, evalsentence;
    HashMap framedict, rolesetdict;
    String lemma, roleset, role;
    
    
    Mode(String[] args) throws Exception{
        this.optionparser = new OptionParser(args);
        boolean mode = optionparser.isExsist("mode");
        boolean parser = optionparser.isExsist("parser");
        
        if (mode) modeselect = optionparser.getString("mode");
        else {
            System.out.println("Enter -mode train/test/");
            System.exit(0);
        }

        if (parser) parserselect = optionparser.getString("parser");
        else {
            System.out.println("Enter -parser base/hill");
            System.exit(0);
        }
        
    }

    final public void setParameter(){
        train = optionparser.isExsist("train");
        test = optionparser.isExsist("test");
        eval = optionparser.isExsist("eval");
        output = optionparser.isExsist("output");
        model = optionparser.isExsist("model");
        frame = optionparser.isExsist("frame");
        check_accuracy = optionparser.isExsist("check");
        weight_length = optionparser.getInt("weight", 1000);
        iteration = optionparser.getInt("iter", 10);
    }    
    
    final public void execute() throws Exception{
        setParameter();
        ParameterChecker p_checker = new ParameterChecker(this);
        p_checker.check();
        System.out.println("\nSemantic Role Labeling START");        
        System.out.println("PARSER: " + parserselect);        

        if ("train".equals(modeselect)) {
            System.out.println("\nFiles Loaded...");        
            trainsentence = Reader.read(trainfile, false);
            testsentence = Reader.read(testfile, true);
            evalsentence = Reader.read(evalfile);
            
            System.out.println(String.format(
                "Train Sents: %d\nTest Sents: %d",                        
                trainsentence.size(), testsentence.size()));

            framedict = FrameDict.framedict;
            System.out.println("Framedict: " + framedict.size());
            ArrayList a = RoleDict.roledict;
            System.out.println("Roles: " + RoleDict.roledict.size());
            
//            if ("ai".equals(parserselect)) {    
            System.out.println("\nArgument Identifier Learning START");        
                ArgumentIdentifier ai = new ArgumentIdentifier(weight_length);
                for (int i=0; i<iteration; ++i) {
                    System.out.println("\nIteration: " + (i+1));
                    ai.train(trainsentence);
                    System.out.println();                
                    AccuracyChecker checker = new AccuracyChecker();
                    checker.testAI(testsentence, evalsentence, ai);
                    
                }
//                return;
//            }
            
            System.out.println("\nArgument Classifier Learning START");        
            final Trainer trainer;
            if ("hill".equals(parserselect))
                trainer = new Trainer(trainsentence, weight_length, true);
            else
                trainer = new Trainer(trainsentence, weight_length);
            
            for (int i=0; i<iteration; ++i) {
                System.out.println("\nIteration: " + (i+1));
                trainer.train();
                System.out.println();
                AccuracyChecker checker = new AccuracyChecker();
                if ("hill".equals(parserselect))
                    checker.testHill(testsentence, trainer.hillparser);
                else
                    checker.testBase(testsentence, evalsentence, trainer.baseparser);                    
            }
        }
    }    
}
