/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import argumentidentifier.ArgumentIdentifier;
import java.io.IOException;
import java.util.ArrayList;
import predicatedisambiguator.PredicateDisambiguator;
import semanticrolelabeler.AccuracyChecker;
import io.FrameDict;
import io.LookupTable;
import io.OptionParser;
import io.ParameterChecker;
import io.PathLookupTable;
import io.Reader;
import io.RoleDict;
import io.Sentence;
import semanticrolelabeler.Trainer;

/**
 *
 * @author hiroki
 */
public class Mode {
    
    public OptionParser optionparser;
    public String modeselect, parserselect;    
    public String trainfile, testfile, evalfile, outfile, modelfile, framefile, embedfile;
    public boolean train, test, eval, output, model, frame, embeddings, check_accuracy, pd, ai, ac, core;
    public int iteration, restart, weight_length, prune;
    public ArrayList<Sentence> trainsentence, testsentence, evalsentence;
    
    
    Mode(String[] args) throws Exception{
        optionparser = new OptionParser(args);
        
        if (optionparser.isExsist("mode"))
            modeselect = optionparser.getString("mode");
        else {
            System.out.println("Enter -mode train/test/statistics");
            System.exit(0);
        }

        if (optionparser.isExsist("parser"))
            parserselect = optionparser.getString("parser");
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
        embeddings = optionparser.isExsist("embeddings");
//        frame = optionparser.isExsist("frame");
        RoleDict.core = optionparser.isExsist("core");
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

        if ("train".equals(modeselect) && "nn".equals(parserselect)) {
            System.out.println("\nFiles Loaded...");

//            RoleDict.add("NULL");            
            LookupTable.weight_length = 50;

            if (embedfile != null) Reader.embeddings(embedfile);

            trainsentence = Reader.read_nn(trainfile, false);
            testsentence = Reader.read_nn(testfile, true);
            evalsentence = Reader.read_nn(evalfile);
            
            System.out.println(String.format("Train Sents: %d\nTest Sents: %d", trainsentence.size(), testsentence.size()));
            
            ArrayList a = RoleDict.roledict;
            
            System.out.println("Framedict: " + FrameDict.framedict.size());
            System.out.println("Roles: " + RoleDict.roledict.size());
            
            predicateDisambiguation();            
            argumentClassification();
            
        }
        else if ("train".equals(modeselect)) {
            System.out.println("\nFiles Loaded...");

            if (RoleDict.core && !ac) RoleDict.add("NULL");
            
            iteration = 20;
            weight_length = 50;
            LookupTable.weight_length = 50;

            if (embedfile != null) Reader.embeddings(embedfile);

            trainsentence = Reader.read(trainfile, false);
            if (ai) testsentence = Reader.read(testfile, true);
            else testsentence = Reader.read(testfile, true, true);
            evalsentence = Reader.read(evalfile);
            
            System.out.println(String.format("Train Sents: %d\nTest Sents: %d", trainsentence.size(), testsentence.size()));
            
            RoleDict.setBiroledict();
            
            ArrayList a = RoleDict.roledict;
            
            System.out.println("Framedict: " + FrameDict.framedict.size());
            System.out.println("Roles: " + RoleDict.roledict.size());
            
            if (pd) predicateDisambiguation();            
            if (ai) argumentIdentification();            
            if (ac) argumentClassification();
            
        }
        else if ("statistics".equals(modeselect)) {
            System.out.println("\nFiles Loaded...");
            
            trainsentence = Reader.read(trainfile, false);
            testsentence = Reader.read(testfile, true, true);
            evalsentence = Reader.read(evalfile);
            
            System.out.println(String.format("Train Sents: %d\nTest Sents: %d", trainsentence.size(), testsentence.size()));
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

        iteration = 20;        
        
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
        weight_length = 50;
        PathLookupTable.weight_length = 50;
        final Trainer trainer = new Trainer(trainsentence, parserselect, weight_length, restart, prune);

        iteration = 200;
        for (int i=0; i<iteration; ++i) {        
            System.out.println("\nIteration: " + (i+1));            
            trainer.train();            
            System.out.println();            

//            AccuracyChecker checker = new AccuracyChecker();
//            checker.test(testsentence, evalsentence, trainer.parser, parserselect);
        }        
    }
    
    
}
