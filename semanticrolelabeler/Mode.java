/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import argumentidentifier.ArgumentIdentifier;
import java.io.IOException;
import java.util.ArrayList;
import predicatedisambiguator.PredicateDisambiguator;

/**
 *
 * @author hiroki
 */
final public class Mode {
    
    final OptionParser optionparser;
    String modeselect, parserselect;    
    String trainfile, testfile, evalfile, outfile, modelfile, framefile;
    boolean train, test, eval, output, model, frame, check_accuracy, pd, ai, ac, core;
    int iteration, restart, weight_length, prune;
    ArrayList<Sentence> trainsentence, testsentence, evalsentence;
    
    
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
        pd = optionparser.isExsist("pd");
        ai = optionparser.isExsist("ai");
        ac = optionparser.isExsist("ac");
        train = optionparser.isExsist("train");
        test = optionparser.isExsist("test");
        eval = optionparser.isExsist("eval");
        output = optionparser.isExsist("output");
        model = optionparser.isExsist("model");
//        frame = optionparser.isExsist("frame");
        core = optionparser.isExsist("core");
        check_accuracy = optionparser.isExsist("check");
        weight_length = optionparser.getInt("weight", 1000);
        iteration = optionparser.getInt("iter", 10);
        restart = optionparser.getInt("restart", 1);
        prune = optionparser.getInt("prune", 100000);
    }    
    
    final public void execute() throws Exception{
        setParameter();
        ParameterChecker p_checker = new ParameterChecker(this);
        p_checker.check();
        System.out.println("\nSemantic Role Labeling START");        
        System.out.println("PARSER: " + parserselect);        

        if ("train".equals(modeselect)) {
            System.out.println("\nFiles Loaded...");

//            core = true;
            RoleDict.core = core;
            if (core) RoleDict.add("NULL");
            
            trainsentence = Reader.read(trainfile, false);
            if (ai) testsentence = Reader.read(testfile, true);
            else testsentence = Reader.read(testfile, true, true);
            evalsentence = Reader.read(evalfile);
            
            System.out.println(String.format("Train Sents: %d\nTest Sents: %d", trainsentence.size(), testsentence.size()));
            
            for (int i=0; i<RoleDict.rolearray.size(); ++i) {
                final int role1 = RoleDict.rolearray.get(i);
                RoleDict.biroledict.put(String.valueOf(role1), RoleDict.biroledict.size());
                
                for (int j=0; j<RoleDict.rolearray.size(); ++j) {
                    final int role2 = RoleDict.rolearray.get(j);
                    RoleDict.biroledict.put(String.valueOf(role1) + "-" + String.valueOf(role2), RoleDict.biroledict.size());
                }            
            }
            
            System.out.println("Framedict: " + FrameDict.framedict.size());
            System.out.println("Roles: " + RoleDict.roledict.size());
            System.out.println("BiRoles: " + RoleDict.biroledict.size());
            
            ArrayList<String> a = RoleDict.roledict;
            
            if (pd) predicateDisambiguation();            
            if (ai) argumentIdentification();            
            if (ac) argumentClassification();

            
        }
        else if ("statistics".equals(modeselect)) {
            System.out.println("\nFiles Loaded...");
            
            trainsentence = Reader.read(trainfile, false);
            testsentence = Reader.read(testfile, true, true);
            evalsentence = Reader.read(evalfile);
            
            System.out.println(String.format(
                "Train Sents: %d\nTest Sents: %d",                        
                trainsentence.size(), testsentence.size()));

            System.out.println("Framedict: " + FrameDict.framedict.size());
            System.out.println("Roles: " + RoleDict.roledict.size());
            
            if (ai) {
                ArgumentIdentifier ai = new ArgumentIdentifier(weight_length);
                ai.confusionMatrix(testsentence, evalsentence);
            }
        }
    }
    
    final private void predicateDisambiguation() {
        System.out.println("\nPredicate Disambiguator Learning START");        
        PredicateDisambiguator pd = new PredicateDisambiguator(weight_length);

        for (int i=0; i<iteration; ++i) {        
            System.out.println("\nIteration: " + (i+1));            
            pd.train(trainsentence);            
            System.out.println();                            
            AccuracyChecker checker = new AccuracyChecker();            
            checker.testPD(testsentence, evalsentence, pd);                                
        }          
        weight_length = weight_length * 100;        
    }
    
    final private void argumentIdentification() throws IOException {
        System.out.println("\nArgument Identifier Learning START");        
        ArgumentIdentifier ai = new ArgumentIdentifier(weight_length);
        for (int i=0; i<iteration; ++i) {
            System.out.println("\nIteration: " + (i+1));
            ai.train(trainsentence);
            System.out.println();                                    
            AccuracyChecker checker = new AccuracyChecker();                    
            checker.testAI(testsentence, evalsentence, ai);                    
            
            if (i==iteration-1 && output) {
                ai.confusionMatrix(testsentence, evalsentence);                
                checker.outputAI(testsentence, outfile);                                    
            }                
        }        
    }
    
    final private void argumentClassification() throws IOException {
        System.out.println("\nArgument Classifier Learning START");        

        final Trainer trainer;

        if ("hill".equals(parserselect)) {
            trainer = new Trainer(trainsentence, weight_length, restart);
            trainer.hillparser.prune = prune;
        }        
        else {
            trainer = new Trainer(trainsentence, weight_length);
            trainer.baseparser.prune = prune;
        }

        for (int i=0; i<iteration; ++i) {        
            System.out.println("\nIteration: " + (i+1));            
            trainer.train();            
            System.out.println();            
            AccuracyChecker checker = new AccuracyChecker();
            
            if ("hill".equals(parserselect)) {
//                checker.testHill(testsentence, evalsentence, trainer.hillparser);
                checker.testSecondHill(testsentence, evalsentence, trainer.hillparser);
                if (i==iteration-1 && output) checker.output(testsentence, outfile);
            }
            else {            
                checker.testBase(testsentence, evalsentence, trainer.baseparser);                
                if (i==iteration-1 && output) checker.outputBase(testsentence, outfile);                
            }            
        }        
    }
    
}
