/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import java.io.File;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 *
 * @author hiroki
 */
public class FrameLoader {
    final public DocumentBuilderFactory factory;
    final public DocumentBuilder builder;
            
    public FrameLoader() throws Exception {
        factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();
    }
    
    final public Node buildTree(String filename) throws Exception {
        return builder.parse(filename);
    }

    final public Node[] buildTrees(String filename) throws Exception {
        File file = new File(filename);
        File[] files = file.listFiles();
        Node[] nodes = new Node[files.length];
        
        for (int i=0; i<files.length; ++i) {
            if ("frameset.dtd".equals(files[i].getName())) continue;
            nodes[i] = builder.parse(files[i]);
        }
        
        return nodes;
    }
    
}
