/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 * @author hiroki
 */
public class Sentence {
    final public int index;
    final public ArrayList<Token> tokens;
    public int[] preds;
    public int[][] o_graph;
    public int[][] p_graph;

    public String[][] dep_path;
    public String[][] dep_pos_path;
    public String[][] dep_r_path;
    
    public int max_arg_length;
    
    public Sentence(int index) {
        this.index = index;
        this.tokens = new ArrayList<>();
    }

    final public void setPredicates() {
        ArrayList<Integer> tmp_preds = new ArrayList<>();
        
        for (int i=0; i<tokens.size(); ++i) {
            Token t = tokens.get(i);
            if ("Y".equals(t.fillpred)) {
                tmp_preds.add(t.id);
            }                
        }
        
        preds = new int[tmp_preds.size()];
        
        for (int i=0; i<tmp_preds.size(); ++i)
            preds[i] = tmp_preds.get(i);
    }
    
    final void setChildren() {
        for (int i=0; i<size(); ++i) {
            Token token = tokens.get(i);

            for (int j=0; j<size(); ++j) {
                if (i == j) continue;
                
                Token child = tokens.get(j);
                
                if (child.phead == token.id)
                    token.children.add(child.id);
            }    
        }
        
        for (int i=0; i<size(); ++i) {
            final Token token = tokens.get(i);
            final ArrayList<Integer> children = token.children;

            if (children.isEmpty()) continue;

            int leftmost = 1000;
            int rightmost = -1;
            for (int j=0; j<children.size(); ++j) {
                final int child_id = children.get(j);
                
                if (child_id < leftmost)
                    leftmost = child_id;
                if (child_id > rightmost)
                    rightmost = child_id;
            }
            
            if (leftmost < token.id) {
                final Token l = tokens.get(leftmost);
                token.leftmostw = l.form;
                token.leftmostpos = l.ppos;
            }
            if (rightmost > token.id) {
                final Token r = tokens.get(rightmost);
                token.rightmostw = r.form;
                token.rightmostpos = r.ppos;
            }
        }        
    }
    
    final void setSubCat() {
        for (int i=0; i<this.size(); ++i) {    
            Token token = tokens.get(i);
            ArrayList<Integer> child = token.children;
            
            for (int j=0; j<child.size(); ++j)
                token.subcat += tokens.get(child.get(j)).pdeprel;
        }
    }

    final void setChildDepSet() {
        for (int i=0; i<this.size(); ++i) {    
            Token token = tokens.get(i);
            ArrayList<Integer> child = token.children;
            final TreeSet tmp = new TreeSet();
            final TreeSet tmp2 = new TreeSet();
            final TreeSet tmp3 = new TreeSet();
            
            for (int j=0; j<child.size(); ++j) {
                tmp.add(tokens.get(child.get(j)).pdeprel);
                tmp2.add(tokens.get(child.get(j)).ppos);
                tmp3.add(tokens.get(child.get(j)).form);
            }

            Iterator<String> it = tmp.iterator();
            Iterator<String> it2 = tmp2.iterator();
            Iterator<String> it3 = tmp3.iterator();

            while (it.hasNext())
                token.childdepset += it.next();
            while (it2.hasNext())
                token.childposset += it2.next();
            while (it3.hasNext())
                token.childwordset += it3.next();
        }
    }
    
    final void setSinblings() {
        for (int i=0; i<this.size(); ++i) {    
            Token token = tokens.get(i);
            Token head = tokens.get(token.phead);
            ArrayList<Integer> child = head.children;
            
            for (int j=0; j<child.size(); ++j) {
                Token sibling = tokens.get(child.get(j));
                
                if (sibling.id == token.id) continue;
                else if (sibling.id < token.id) {
                    token.leftsiblingw.add(sibling.form);
                    token.leftsiblingpos.add(sibling.ppos);
                }
                else {
                    token.rightsiblingw.add(sibling.form);
                    token.rightsiblingpos.add(sibling.ppos);                    
                }
            }
        }
        
    }
    
    final public void initializeParguments() {
        for (int i=1; i<this.size(); ++i) {        
            Token arg = tokens.get(i);
            arg.arguments = new ArrayList();
        }
    }

    final public void initializePapred() {
        for (int i=1; i<this.size(); ++i) {        
            Token arg = tokens.get(i);
            arg.apred = new int[preds.length];
            
            for (int j=0; j<arg.apred.length; ++j) arg.apred[j] = -1;
        }
    }

    final public void initializePpred() {
        for (int i=0; i<preds.length; ++i) {        
            Token pred = tokens.get(preds[i]);
            pred.pred = 0;
        }
    }
    
    final public void setMaxArgLength() {
        for (int i=0; i<preds.length; ++i) {
            final Token pred = tokens.get(preds[i]);
            
            if (pred.arguments.size() > max_arg_length)
                max_arg_length = pred.arguments.size();
        }
    }
    
    
    final void setArguments() {
        for (int i=0; i<this.preds.length; ++i) {    
            Token pred = tokens.get(preds[i]);
            
            for (int j=1; j<this.size(); ++j) {
                Token arg = tokens.get(j);
                if (arg.apred[i] > -1) pred.arguments.add(arg.id);
            }
        }
        
    }

    final void setAllCandidateArguments() {
        for (int i=0; i<this.preds.length; ++i) {    
            final Token pred = tokens.get(preds[i]);
            
            for (int j=1; j<this.size(); ++j) {
                final Token arg = tokens.get(j);
                pred.arguments.add(arg.id);
            }
        }
        
    }

    
    final void setOracleGraph() {
        o_graph = new int[this.preds.length][max_arg_length];

        for (int i=0; i<this.preds.length; ++i) {
            final Token pred = this.tokens.get(this.preds[i]);
            final int[] tmp_graph = o_graph[i];
            final ArrayList<Integer> arguments = pred.arguments;
            final int arg_length = arguments.size();

            for (int j=0; j<max_arg_length; ++j) {
                if (j < arg_length) {
                    final int arg_i = arguments.get(j);
                    final Token arg = tokens.get(arg_i);
                    tmp_graph[j] = arg.apred[i];
                }
                else
                    tmp_graph[j] = -1;
            }
        }
    }

    final void setOraclePropGraph() {
        o_graph = new int[this.preds.length][RoleDict.rolearray.size()];

        for (int prd_i=0; prd_i<this.preds.length; ++prd_i) {
            final Token pred = this.tokens.get(this.preds[prd_i]);
            final int[] tmp_graph = o_graph[prd_i];
            
            for (int i=0; i<tmp_graph.length; ++i) tmp_graph[i] = -1;
            
            final ArrayList<Integer> arguments = pred.arguments;
            final int arg_length = arguments.size();

            for (int i=0; i<arg_length; ++i) {            
                final int arg_i = arguments.get(i);                                    
                final Token arg = tokens.get(arg_i);                
                final int role = arg.apred[prd_i];                                    

//                if (role > -1) tmp_graph[role] = arg_i;                
                if (role > -1) tmp_graph[role] = i;                
            }
        }
    }

    final void setFrameDict() {
        for (int i=0; i<this.preds.length; ++i) {
            final Token pred = this.tokens.get(this.preds[i]);
            final int sense = pred.pred;
            
            for (int j=1; j<this.size(); ++j) {
                final Token arg = this.tokens.get(j);
                final int role = arg.apred[i];
                        
                if (role > -1)
                    FrameDict.add(pred.cpos, sense, role);
                else
                    FrameDict.add(pred.cpos, sense);
            }
        }
    }
    
    final public void setDeps() {
        final int sent_length = this.size();
        final int preds_length = preds.length;
        
        dep_path = new String[preds_length][sent_length];
        dep_pos_path = new String[preds_length][sent_length];
        dep_r_path = new String[preds_length][sent_length];

        for (int i=0; i<preds.length; ++i) {
            final Token prd = tokens.get(preds[i]);
            
            for (int j=1; j<this.size(); ++j) {
                Token arg = tokens.get(j);
        
                ArrayList path = getDependencyPath(arg, prd);
                String d_path = getDepPathPhi(path, j);
                String[] dep_info_path = getDepInfoPathPhi(path, d_path);
                
                dep_path[i][j] = d_path;
                dep_pos_path[i][j] = dep_info_path[0];
                dep_r_path[i][j] = dep_info_path[1];
            }
        }
    }    
    
    final private ArrayList getDependencyPath(final Token token1,
                                                final Token token2) {
        int arg_id = token1.id;
        int prd_id = token2.id;
        
        if (arg_id < 1 || prd_id < 1) {
            ArrayList NULL = new ArrayList<>();
            NULL.add(-2);
            return NULL;
        }
        else if (arg_id == prd_id) {
            ArrayList NULL = new ArrayList<>();
            NULL.add(-1);
            return NULL;            
        }
        
        ArrayList path1 = searchRootPath(arg_id, new ArrayList<>());
        ArrayList path2 = searchRootPath(prd_id, new ArrayList<>());
        return joinTwoPath(path1, path2);
    }
    
    final private ArrayList searchRootPath(final int token_id,
                                             final ArrayList path) {
        if (token_id < 1) {
            ArrayList NULL = new ArrayList<>();
            NULL.add(-1);
            return NULL;
        }
        Token token = tokens.get(token_id);
        path.add(token.id);
        int head = token.head;
        if (head == 0) return path;
        return searchRootPath(head, path);
    }
    
    final private ArrayList<Integer> joinTwoPath(final ArrayList<Integer> arg_path,
                                                  final ArrayList<Integer> prd_path) {
        ArrayList<Integer> root = new ArrayList<>();
        
        int arg_id;
        int prd_id;
        
        for (int i=0; i<arg_path.size(); ++i) {
            arg_id = arg_path.get(i);
            
            for (int j=0; j<prd_path.size(); ++j) {
                prd_id = prd_path.get(j);

                if (arg_id == prd_id) {
                    for (int k=0; k<i+1; ++k) root.add(arg_path.get(k));
                    for (int k=j-1; k>-1; --k) root.add(prd_path.get(k));
                    return root;
                }
            }
        }
        return root;
    }
    
    final private String getDepPathPhi(final ArrayList<Integer> path,
                                         final int arg_id) {
        String dep_path = "";
        int node = path.get(0);
        int tmp_node;
        
        if (node == -2) return "NULL";
        else if (node == -1) return "SAME";

        ArrayList arg_path = searchRootPath(arg_id, new ArrayList<>());
        
        for (int i=1; i<path.size(); ++i) {
            tmp_node = path.get(i);
            if (arg_path.contains(tmp_node)) dep_path += "0";
            else dep_path += "1";
            node = tmp_node;
        }
        return dep_path;
    }

    final private String[] getDepInfoPathPhi(final ArrayList<Integer> path,
                                                final String d_path) {
        String dep_pos_path = "";
        String dep_r_path = "";
        
        final int node = path.get(0);
        
        if (node == -2) return new String[]{"NULL","NULL"};
        else if (node == -1) return new String[]{"SAME","SAME"};        
        
        for (int i=0; i<path.size()-1; ++i) {
            final int tmp_node = path.get(i);
            final String d_tmp_node = d_path.substring(i, i+1);
            
            Token token;
            if ("0".equals(d_tmp_node))
                token = tokens.get(tmp_node);
            else
                token = tokens.get(path.get(i+1));
            dep_pos_path += token.ppos;
            dep_r_path += token.pdeprel;
        }
        
        return new String[]{dep_pos_path, dep_r_path};
    }
        
    
    final public int size() {
        return tokens.size();
    }

    final public void add(Token token) {
        this.tokens.add(token);
    }
    
}
