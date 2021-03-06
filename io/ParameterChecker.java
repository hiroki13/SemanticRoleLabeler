/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import main.Mode;


/**
 *
 * @author hiroki
 */
public class ParameterChecker {
    Mode mode;
    OptionParser optionparser;
    
    public ParameterChecker(Mode mode) {
        this.mode = mode;
        this.optionparser = mode.optionparser;
    }
    
    final public void check() {
        String modeselect = mode.modeselect;
        String parserselect = mode.parserselect;
        
        if ("train".equals(modeselect)) {
            setTrainFile();
            setTestFile();
            setEvalFile();
            setOutputFile();
            setEmbedFile();
            
            if (!isParser(parserselect)) {
                System.out.println("Enter -parser base/hill/nn");
                System.exit(0);
            }
        }

        else if ("test".equals(modeselect)) {
            setTestFile();
            setModelFile();
            setOutputFile();
            setEmbedFile();
            
            if (!isParser(parserselect)) {
                System.out.println("Enter -parser base/hill/nn");
                System.exit(0);
            }
        }

        else if ("statistics".equals(modeselect)) {
            setTrainFile();
            setTestFile();
            setEvalFile();
        }

        else System.out.println("Enter -mode X");        
 
    }
    
    final public void setTrainFile() {
        if(mode.train) mode.trainfile = optionparser.getString("train");
        else {
            System.out.println("Enter -train filename");
            System.exit(0);
        }
    }
    
    final public void setTestFile() {
        if(mode.test) mode.testfile = optionparser.getString("test");
        else {
            System.out.println("Enter -test filename");
            System.exit(0);
        }
    }

    final public void setEvalFile() {
        if(mode.eval) mode.evalfile = optionparser.getString("eval");
    }
    
    final public void setOutputFile() {
        if(mode.output) mode.outfile = optionparser.getString("output");
    }
    
    final public void setModelFile() {
        if(mode.model) mode.modelfile = optionparser.getString("model");
        else {
            System.out.println("Enter -model filename");
            System.exit(0);
        }        
    }

    final public void setEmbedFile() {
        if(mode.embeddings) mode.embedfile = optionparser.getString("embeddings");
        else {
            System.out.println("Enter -embeddings filename");
            System.exit(0);
        }        
    }
    
/*    
    final public void setFrameFile() {
        if(mode.frame) mode.framefile = optionparser.getString("frame");
        else {
            System.out.println("Enter -frame filename");
            System.exit(0);
        }        
    }    
*/
    
    final public boolean isParser(String parserselect) {
        String[] possible_parser = {"base", "hill", "nn"};
        
        for (int i=0; i<possible_parser.length; i++) {
            if (parserselect.equals(possible_parser[i])) return true;
        }
        return false;
    }
    
    
}
