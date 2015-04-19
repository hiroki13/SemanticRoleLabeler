/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import org.w3c.dom.*;

/**
 *
 * @author hiroki
 */
public class FrameDictBuilder {
    Node root;
    Node[] roots;
    String lemma;
    int roleset, role;

    public FrameDictBuilder(Node root) {
        this.root = root;
    }

    public FrameDictBuilder(Node[] roots) {
        this.roots = roots;
    }
    
    final public void setDict() {
        NodeList children = root.getChildNodes();
        buildDict(children);        
    }    

    final public void setWholeDict() {
        for (int i=0; i<roots.length; ++i) {
            Node root = roots[i];

            if (root == null) continue;
            
            NodeList children = root.getChildNodes();
            buildDict(children);
        }
    }    
    
    final private void buildDict(NodeList children) {
        final String delimiter = "\\.";
        for (int i=0; i<children.getLength(); ++i) {        
            Node gchild = children.item(i);            

            if ("roleset".equals(gchild.getNodeName())) {
                NamedNodeMap attributes = gchild.getAttributes();
                Node id = attributes.getNamedItem("id");
                String[] split = id.getNodeValue().split(delimiter);

                lemma = split[0];
                int tmp_roleset = Integer.parseInt(split[1]);
                roleset = (int) RolesetDict.get(lemma, tmp_roleset);
                FrameDict.put(lemma, roleset);
            }
            else if ("role".equals(gchild.getNodeName())) {
                NamedNodeMap attributes = gchild.getAttributes();
                Node id1 = attributes.getNamedItem("n");
                Node id2 = attributes.getNamedItem("f");

                if (id1 != null) {
                    role = RoleDict.get(id1.getNodeValue());
                }
                if (id2 != null) {
                    role = RoleDict.get(id2.getNodeValue());
                }
                
                FrameDict.add(lemma, roleset, role);
            }
            
            NodeList gchildren = gchild.getChildNodes();
            if (gchildren.getLength() > 0) buildDict(gchildren);
        }
    }    
}
