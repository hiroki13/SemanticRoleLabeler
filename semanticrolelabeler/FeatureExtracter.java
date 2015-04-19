/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package semanticrolelabeler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hiroki
 */
final public class FeatureExtracter implements Serializable{
    final int weight_size;
    int k;
    int total;
    public ArrayList<String[][][]> g_cache;
    
    public FeatureExtracter(final int weight_size) {
        this.weight_size = weight_size;
    }

    final public int[] extractFeature (final Sentence sentence, 
                                         final int target){
        final String[] feature = instantiateFeature(sentence.tokens, target);
        final int[] encoded_feature = encodeFeature(feature);
        return encoded_feature;
    }

    final public int[] extractFeature (final Sentence sentence,
                                         final int prd,
                                         final int arg){
        final String[] feature = instantiateFeature(sentence.tokens, prd, arg);
        final int[] encoded_feature = encodeFeature(feature);
        return encoded_feature;
    }
    
    final public int[] extractFirstOrdFeature(final Sentence sentence,
                                                 final int prd, final int arg) {
        final String[] feature = instantiateFirstOrdFeature(sentence, prd, arg);
        final int[] encoded_feature = encodeFeature2(feature);
        return encoded_feature;        
    }

    final public int[] extractAIFeature(final Sentence sentence,
                                          final int prd, final int arg) {
        final String[] feature = instantiateAIFeature(sentence, prd, arg);
        final int[] encoded_feature = encodeFeature2(feature);
        return encoded_feature;        
    }    
    
    final public int[] extractSecondOrdFeature(final Sentence sentence,
                                                  final HashMap<String, Integer>[] graph) {
        final String[] feature = instantiateSecondOrdFeature(sentence, graph);
        final int[] encoded_feature = encodeFeature(feature);
        return encoded_feature;        
    }
        
    final public String[] instantiateFirstOrdFeature(final Sentence sentence,
                                                        final int prd_i,
                                                        final int arg_i) {
        k = 0;        
        String[] feature = new String[27];

        final ArrayList<Token> tokens = sentence.tokens;
        String[][][] cache = g_cache.get(sentence.index);

        if (cache[prd_i][arg_i] != null)
            return cache[prd_i][arg_i];
        
        final Token prd = tokens.get(sentence.preds[prd_i]);
        final String pos = pos(prd);        
        final Token pparent = tokens.get(prd.phead);            
        final String sense = prd.plemma + prd.pred;        
        final String subcat = prd.subcat;        
        final String childdepset = prd.childdepset;        
        final String childposset = prd.childposset;
                        
        final Token arg = tokens.get(prd.arguments.get(arg_i));
        final String dep_r_path = sentence.dep_r_path[prd_i][arg.id];        
        final String dep_pos_path = sentence.dep_pos_path[prd_i][arg.id];        
        final String position = position(prd.id, arg.id);


        feature[k++] = "PredW_" + prd.form;        
        feature[k++] = "PredPOS_" + prd.ppos;        
        feature[k++] = "PredLemma_" + prd.plemma;        
        feature[k++] = "PredLemmaSense_" + sense;        
        feature[k++] = "PredParentW_" + pparent.form;        
        feature[k++] = "PredParentPOS_" + pparent.ppos;        
        feature[k++] = "DepSubCat_" + subcat;        
        feature[k++] = "ChildDepSet_" + childdepset;        
        feature[k++] = "ChildPOSSet_" + childposset;                
        feature[k++] = "ArdW_" + arg.form;        

        feature[k++] = "ArgPOS_" + arg.ppos;        
        feature[k++] = "ArgDeprel_" + arg.pdeprel;        
        feature[k++] = "APW_" + arg.form + prd.form;        
        feature[k++] = "APF_" + arg.plemma + prd.plemma;        
        feature[k++] = "APPL_" + arg.ppos + prd.plemma;        
        feature[k++] = "APPS_" + arg.plemma + sense;        
        feature[k++] = "DepRelPath_" + dep_r_path;        
        feature[k++] = "POSPath_" + dep_pos_path;        
        feature[k++] = "Position_" +  position;        
        feature[k++] = "Position+DepRelPath" +  position + dep_r_path;        
        
        feature[k++] = "Position+ArgW" +  position + arg.form;        
        feature[k++] = "ArgPOS+PredLemmaSense" +  arg.ppos + sense;        
        feature[k++] = "Position+PredLemmaSense" +  position + sense;        
        feature[k++] = "ArgW+PredLemmaSense" +  arg.form + sense;        
        feature[k++] = "ArgPOS+ArgW" +  arg.ppos + arg.form;        
        feature[k++] = "POSPath+PredLemmaSense" +  dep_pos_path + sense;        
        feature[k++] = "Position+ArgPOS" +  position + arg.ppos;
        
        feature = conjoin(feature, pos);

        if (cache[prd_i][arg_i] == null)        
            cache[prd_i][arg_i] = feature;
        
        return feature;
    }

    final public String[] instantiatePredFirstOrdFeature(final Sentence sentence,
                                                             final int prd_i,
                                                             final int arg_i) {
        k = 0;        
        String[] feature = new String[27];

        final ArrayList<Token> tokens = sentence.tokens;
        String[][][] cache = g_cache.get(sentence.index);

        if (cache[prd_i][arg_i] != null)
            return cache[prd_i][arg_i];
        
        final Token prd = tokens.get(sentence.preds[prd_i]);
        final String pos = pos(prd);        
        final Token pparent = tokens.get(prd.phead);            
        final String sense = prd.plemma + prd.pred;        
        final String subcat = prd.subcat;        
        final String childdepset = prd.childdepset;        
        final String childposset = prd.childposset;
                        
        final Token arg = tokens.get(prd.parguments.get(arg_i));
        final String dep_r_path = sentence.dep_r_path[prd_i][arg.id];        
        final String dep_pos_path = sentence.dep_pos_path[prd_i][arg.id];        
        final String position = position(prd.id, arg.id);


        feature[k++] = "PredW_" + prd.form;        
        feature[k++] = "PredPOS_" + prd.ppos;        
        feature[k++] = "PredLemma_" + prd.plemma;        
        feature[k++] = "PredLemmaSense_" + sense;        
        feature[k++] = "PredParentW_" + pparent.form;        
        feature[k++] = "PredParentPOS_" + pparent.ppos;        
        feature[k++] = "DepSubCat_" + subcat;        
        feature[k++] = "ChildDepSet_" + childdepset;        
        feature[k++] = "ChildPOSSet_" + childposset;                
        feature[k++] = "ArdW_" + arg.form;        

        feature[k++] = "ArgPOS_" + arg.ppos;        
        feature[k++] = "ArgDeprel_" + arg.pdeprel;        
        feature[k++] = "APW_" + arg.form + prd.form;        
        feature[k++] = "APF_" + arg.plemma + prd.plemma;        
        feature[k++] = "APPL_" + arg.ppos + prd.plemma;        
        feature[k++] = "APPS_" + arg.plemma + sense;        
        feature[k++] = "DepRelPath_" + dep_r_path;        
        feature[k++] = "POSPath_" + dep_pos_path;        
        feature[k++] = "Position_" +  position;        
        feature[k++] = "Position+DepRelPath" +  position + dep_r_path;        
        
        feature[k++] = "Position+ArgW" +  position + arg.form;        
        feature[k++] = "ArgPOS+PredLemmaSense" +  arg.ppos + sense;        
        feature[k++] = "Position+PredLemmaSense" +  position + sense;        
        feature[k++] = "ArgW+PredLemmaSense" +  arg.form + sense;        
        feature[k++] = "ArgPOS+ArgW" +  arg.ppos + arg.form;        
        feature[k++] = "POSPath+PredLemmaSense" +  dep_pos_path + sense;        
        feature[k++] = "Position+ArgPOS" +  position + arg.ppos;
        
        feature = conjoin(feature, pos);

        if (cache[prd_i][arg_i] == null)        
            cache[prd_i][arg_i] = feature;
        
        return feature;
    }

    
    final public String[] instantiateAIFeature(final Sentence sentence,
                                                  final int prd_i,
                                                  final int arg_i) {
        k = 0;        
        String[] feature = new String[27];

        final ArrayList<Token> tokens = sentence.tokens;
        String[][][] cache = g_cache.get(sentence.index);

        if (cache[prd_i][arg_i] != null)
            return cache[prd_i][arg_i];
        
        final Token prd = tokens.get(sentence.preds[prd_i]);
        final String pos = pos(prd);        
        final Token pparent = tokens.get(prd.phead);            
        final String sense = prd.plemma + prd.pred;        
        final String subcat = prd.subcat;        
        final String childdepset = prd.childdepset;        
        final String childposset = prd.childposset;
                        
        final Token arg = tokens.get(arg_i);
        final String dep_r_path = sentence.dep_r_path[prd_i][arg.id];        
        final String dep_pos_path = sentence.dep_pos_path[prd_i][arg.id];        
        final String position = position(prd.id, arg.id);


        feature[k++] = "PredW_" + prd.form;        
        feature[k++] = "PredPOS_" + prd.ppos;        
        feature[k++] = "PredLemma_" + prd.plemma;        
        feature[k++] = "PredLemmaSense_" + sense;        
        feature[k++] = "PredParentW_" + pparent.form;        
        feature[k++] = "PredParentPOS_" + pparent.ppos;        
        feature[k++] = "DepSubCat_" + subcat;        
        feature[k++] = "ChildDepSet_" + childdepset;        
        feature[k++] = "ChildPOSSet_" + childposset;                
        feature[k++] = "ArdW_" + arg.form;        

        feature[k++] = "ArgPOS_" + arg.ppos;        
        feature[k++] = "ArgDeprel_" + arg.pdeprel;        
        feature[k++] = "APW_" + arg.form + prd.form;        
        feature[k++] = "APF_" + arg.plemma + prd.plemma;        
        feature[k++] = "APPL_" + arg.ppos + prd.plemma;        
        feature[k++] = "APPS_" + arg.plemma + sense;        
        feature[k++] = "DepRelPath_" + dep_r_path;        
        feature[k++] = "POSPath_" + dep_pos_path;        
        feature[k++] = "Position_" +  position;        
        feature[k++] = "Position+DepRelPath" +  position + dep_r_path;        
        
        feature[k++] = "Position+ArgW" +  position + arg.form;        
        feature[k++] = "ArgPOS+PredLemmaSense" +  arg.ppos + sense;        
        feature[k++] = "Position+PredLemmaSense" +  position + sense;        
        feature[k++] = "ArgW+PredLemmaSense" +  arg.form + sense;        
        feature[k++] = "ArgPOS+ArgW" +  arg.ppos + arg.form;        
        feature[k++] = "POSPath+PredLemmaSense" +  dep_pos_path + sense;        
        feature[k++] = "Position+ArgPOS" +  position + arg.ppos;
        
        feature = conjoin(feature, pos);

        if (cache[prd_i][arg_i] == null)        
            cache[prd_i][arg_i] = feature;
        
        return feature;
    }
    
    
    final public String[] instantiateSecondOrdFeature(final Sentence sentence,
                                                         final HashMap<String, Integer>[] graph) {
        int p = 0;
        String[] feature = null;
        final ArrayList<Token> tokens = sentence.tokens;
        final int[] preds = sentence.preds;
        
        for (int prd_i=0; prd_i<graph.length; prd_i++) {
            final HashMap<String, Integer> tmp_graph1 = graph[prd_i];
            final Token prd1 = tokens.get(preds[prd_i]);
            final String[] p_atomic_feature1 = atomicFeature(prd1, tokens);
            
            for (Map.Entry<String, Integer> entry1 : tmp_graph1.entrySet()) {
                final String role1 = entry1.getKey();
                final int arg_i = entry1.getValue();                
                final Token arg1 = tokens.get(arg_i);
                final String[] a_atomic_feature1 = atomicFeature2(arg1, tokens);
                final String[] combination_feature1 = combinationFeature(p_atomic_feature1, a_atomic_feature1, role1);
                feature = Arrays.copyOf(combination_feature1, combination_feature1.length);
                
                for (int prd_j = prd_i+1; prd_j<graph.length; prd_j++) {
                    final HashMap<String, Integer> tmp_graph2 = graph[prd_j];
                    final Token prd2 = tokens.get(preds[prd_j]);
                    final String[] p_atomic_feature2 = atomicFeature(prd2, tokens);

                    for (Map.Entry<String, Integer> entry2 : tmp_graph2.entrySet()) {
                        final String role2 = entry2.getKey();
                        final int arg_j = entry2.getValue();                
                        final Token arg2 = tokens.get(arg_j);
                        final String[] a_atomic_feature2 = atomicFeature2(arg2, tokens);
                        final String[] combination_feature2 = combinationFeature(p_atomic_feature2, a_atomic_feature2, role2);                        
                        final String[] combination_feature3 = combinationFeature(p_atomic_feature1, p_atomic_feature2, role1+role2);
                        final String[] combination_feature4 = combinationFeature(combination_feature1, p_atomic_feature2, role2);
                        final String[] combination_feature5 = combinationFeature(combination_feature1, a_atomic_feature2, role2);
                        final String[] combination_feature6 = combinationFeature(combination_feature1, combination_feature2, "N");                        
                        
                        feature = combineFeatures(feature, combination_feature2);
                        feature = combineFeatures(feature, combination_feature3);
                        feature = combineFeatures(feature, combination_feature4);
                        feature = combineFeatures(feature, combination_feature5);
                        feature = combineFeatures(feature, combination_feature6);
                    }
                }
            }
        }
        
        return feature;
    }
    
    final private String[] atomicFeature(final Token token,
                                           final ArrayList<Token> tokens) {
        k = 0;
        final String[] feature_instance = new String[6];
        final Token pparent = tokens.get(token.phead);

        feature_instance[k++] = token.form;        
        feature_instance[k++] = token.ppos;        
        feature_instance[k++] = token.plemma;        
        feature_instance[k++] = token.plemma + token.ppred;        
        feature_instance[k++] = pparent.form;        
        feature_instance[k++] = pparent.ppos;
        
        return feature_instance;
    }

    final private String[] atomicFeature2(final Token token,
                                            final ArrayList<Token> tokens) {
        k = 0;
        final String[] feature_instance = new String[3];

        feature_instance[k++] = token.form;        
        feature_instance[k++] = token.ppos;        
        feature_instance[k++] = token.pdeprel;
        
        return feature_instance;
    }
    
    
    final private String[] combinationFeature(final String[] p_atomic,
                                                final String[] a_atomic,
                                                final String role) {
        k = 0;
        final String[] feature_instance = new String[p_atomic.length+a_atomic.length];
        
        for (int i=0; i<p_atomic.length; ++i) {
            final String p_phi = p_atomic[i];
            for (int j=0; j<a_atomic.length; ++j) {
                final String a_phi = a_atomic[j];
                feature_instance[k++] = p_phi+a_phi+role;
            }
        }
        
        return feature_instance;
    }
    
    final private String[] combineFeatures(final String[] feature1,
                                             final String[] feature2) {
       final String[] feature = new String[feature1.length+feature2.length];
       System.arraycopy(feature1, 0, feature, 0, feature1.length);
       System.arraycopy(feature2, 0, feature, feature1.length, feature2.length);
       return feature;
    }
    
    final public String[] instantiateFeature (final ArrayList<Token> tokens, 
                                                final int pred_index){
        k = 0;
        final String[] feature = new String[9];
        Token pred = tokens.get(pred_index);
        Token parent = tokens.get(pred.phead);
        ArrayList<Integer> children = pred.children;

        feature[k++] = "1_" + pred.ppos;
        feature[k++] = "2_" + pred.pdeprel;
        feature[k++] = "3_" + parent.form;
        feature[k++] = "4_" + parent.ppos;
        
        String child_dep_set = "";
        String child_word_set = "";
        String child_word_dep_set = "";
        String child_pos_set = "";
        String child_pos_dep_set = "";
        
        for (int i=0; i<children.size(); ++i) {
            Token child = tokens.get(children.get(i));
            child_dep_set += child.pdeprel;
            child_word_set += child.form;
            child_word_dep_set += child.form + child.pdeprel;
            child_pos_set += child.ppos;
            child_pos_dep_set += child.ppos + child.pdeprel;
        }

        feature[k++] = "5_" + child_dep_set;
        feature[k++] = "6_" + child_word_set;
//        feature[k++] = "7_" + child_word_dep_set;
        feature[k++] = "8_" + child_pos_set;
//        feature[k++] = "9_" + child_pos_dep_set;
        
        return feature;
    }
    
    final public String[] instantiateFeature (final ArrayList<Token> tokens, 
                                                final int pred_index,
                                                final int arg_index){
        k = 0;
        final String[] feature = new String[9];
        final Token pred = tokens.get(pred_index);
        final Token arg = tokens.get(arg_index);

        final Token parent = tokens.get(pred.phead);
        final String sense = pred.lemma + pred.pred;
        final String position = position(pred_index, arg_index);

        feature[k++] = "1_" + sense;
        feature[k++] = "2_" + parent.form;
        feature[k++] = "3_" + parent.ppos;
        feature[k++] = "4_" + arg.form;
        feature[k++] = "5_" + arg.ppos;
        feature[k++] = "6_" + arg.pdeprel;
        feature[k++] = "7_" + position;
                
        return feature;
    }
    
    
    final private int[] encodeFeature (final String[] feature) {
        final int[] encoded_feature = new int[total];
        for(int i=0; i<total; ++i)            
            encoded_feature[i] = (feature[i].hashCode() >>> 1) % weight_size;
        return encoded_feature;
    }

    final public int[] encodeFeature2 (final String[] feature) {
        final int[] encoded_feature = new int[feature.length];
        for(int i=0; i<feature.length; ++i)            
            encoded_feature[i] = (feature[i].hashCode() >>> 1) % weight_size;
        return encoded_feature;
    }
    
    final private String position(final int prd, final int arg) {
        if (arg-prd < 0) return "Before";
        else if (arg-prd > 0) return "After";
        else return "On";
    }
    
    final public String pos(final Token prd) {
        if (prd.ppos.startsWith("V")) return "V";
        return "N";
    }
    
    final public String[] conjoin(final String[] feature, final String role) {
        final String[] new_feature = new String[feature.length];
        for (int i=0; i<new_feature.length; ++i)
            new_feature[i] = feature[i] + role;
        return new_feature;
    }

    final public String[] conjoin(final String[] feature, final String role,
                                    final String pos) {
        final String[] new_feature = new String[feature.length];
        for (int i=0; i<new_feature.length; ++i)
            new_feature[i] = feature[i] + role + pos;
        return new_feature;
    }

    final public String[] conjoin2(final String[] feature, final String role,
                                    final String pos) {
        final String[] new_feature = new String[k];
        for (int i=0; i<new_feature.length; ++i)
            new_feature[i] = feature[i] + role + pos;
        return new_feature;
    }
    
    final private String[] getFeature(final String[] feature) {
        final String[] new_feature = new String[k];
        for (int i=0; i<new_feature.length; ++i)
            new_feature[i] = feature[i];
        return new_feature;
    }
    
}
