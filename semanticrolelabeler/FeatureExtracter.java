/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package semanticrolelabeler;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author hiroki
 */
final public class FeatureExtracter implements Serializable{
    final int weight_size;
    int k;
    int total;
    public ArrayList<String[][][]> g_cache;
    public ArrayList<String[][]> pd_cache;
    
    public FeatureExtracter(final int weight_size) {
        this.weight_size = weight_size;
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

    final public int[] extractPredAIFeature(final Sentence sentence,
                                            final int prd, final int arg) {
        final String[] feature = instantiatePredAIFeature(sentence, prd, arg);
        final int[] encoded_feature = encodeFeature2(feature);
        return encoded_feature;        
    }    
    
    final public int[] extractPDFeature(final Sentence sentence,
                                          final int prd_i) {
        final String[] feature = instantiatePDFeature(sentence, prd_i);
        final int[] encoded_feature = encodeFeature2(feature);
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
        // prd.ppred is different from instantiateFirstOrdFeature
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


    final public String[] instantiatePredAIFeature(final Sentence sentence,
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

    
    
    final public String[] instantiatePDFeature(final Sentence sentence,
                                                  final int prd_i) {
        k = 0;        
        String[] feature = new String[7];

        final ArrayList<Token> tokens = sentence.tokens;
        String[][] cache = pd_cache.get(sentence.index);

        if (cache[prd_i] != null) return cache[prd_i];
        
        final Token prd = tokens.get(sentence.preds[prd_i]);
        final String pos = pos(prd);        
        final Token pparent = tokens.get(prd.phead);            
        final String subcat = prd.subcat;        
        final String childdepset = prd.childdepset;        
        final String childposset = prd.childposset;
                        
        feature[k++] = "PredW_" + prd.form;        
        feature[k++] = "PredPOS_" + prd.ppos;        
        feature[k++] = "PredParentW_" + pparent.form;        
        feature[k++] = "PredParentPOS_" + pparent.ppos;        
        feature[k++] = "DepSubCat_" + subcat;        
        feature[k++] = "ChildDepSet_" + childdepset;        
        feature[k++] = "ChildPOSSet_" + childposset;                
        
        feature = conjoin(feature, pos);

        if (cache[prd_i] == null) cache[prd_i] = feature;
        
        return feature;
    }
    
        
    final private String[] combineFeatures(final String[] feature1,
                                             final String[] feature2) {
       final String[] feature = new String[feature1.length+feature2.length];
       System.arraycopy(feature1, 0, feature, 0, feature1.length);
       System.arraycopy(feature2, 0, feature, feature1.length, feature2.length);
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
    
}
