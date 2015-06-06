/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;


/**
 *
 * @author hiroki
 */
public class Mian {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Mode mode = new Mode(args);
        mode.execute();
    }
    
}
