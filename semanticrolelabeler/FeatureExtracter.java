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
    public ArrayList<String[][][][][]> second_cache;
    public ArrayList<String[][]> pd_cache;
    
    public FeatureExtracter(final int weight_size) {
        this.weight_size = weight_size;
    }

    final public int[] extractFirstOrdFeature(final Sentence sentence,
                                                 final int prd_i, final int arg_i) {
        final String[] feature = instantiateFirstOrdFeature(sentence, prd_i, arg_i);
        final int[] encoded_feature = encodeFeature2(feature);
        return encoded_feature;        
    }

    final public int[] extractAIFeature(final Sentence sentence,
                                          final int prd_i, final int arg_id) {
        final String[] feature = instantiateAIFeature(sentence, prd_i, arg_id);
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
//        String[][][] cache = g_cache.get(sentence.index);

//        if (cache[prd_i][arg_i] != null)
//            return cache[prd_i][arg_i];
        
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

//        if (cache[prd_i][arg_i] == null)        
//            cache[prd_i][arg_i] = feature;
        
        return feature;
    }
/*
    final public String[] instantiateFirstOrdFeature(final Sentence sentence,
                                                        final int prd_i,
                                                        final int arg_i) {
        k = 0;        

        final ArrayList<Token> tokens = sentence.tokens;
//        String[][][] cache = g_cache.get(sentence.index);

//        if (cache[prd_i][arg_i] != null)
//            return cache[prd_i][arg_i];
        
        final Token prd = tokens.get(sentence.preds[prd_i]);
        final String pos = pos(prd);        

        String[] feature;
        if ("V".equals(pos))
            feature = new String[35 + prd.rightsiblingw.size() + prd.leftsiblingpos.size() + prd.rightsiblingpos.size()];
        else
            feature = new String[31 + prd.leftsiblingw.size() + prd.leftsiblingpos.size()
                    + prd.leftsiblingpos.size() + prd.rightsiblingpos.size()];            
        
        final Token pparent = tokens.get(prd.phead);            
        final String sense = prd.plemma + prd.pred;        
        final String subcat = prd.subcat;        
        final String childdepset = prd.childdepset;        
        final String childposset = prd.childposset;
        final String childwordset = prd.childwordset;
                        
        final Token arg = tokens.get(prd.arguments.get(arg_i));
        final String dep_r_path = sentence.dep_r_path[prd_i][arg.id];        
        final String dep_pos_path = sentence.dep_pos_path[prd_i][arg.id];        
        final String position = position(prd.id, arg.id);


        // 25
        feature[k++] = "PredW_" + prd.form;        
        feature[k++] = "PredPOS_" + prd.ppos;        
        feature[k++] = "PredLemma_" + prd.plemma;        
        feature[k++] = "PredLemmaSense_" + sense;        
        feature[k++] = "PredParentW_" + pparent.form;        
        feature[k++] = "PredParentPOS_" + pparent.ppos;        
        feature[k++] = "DepSubCat_" + subcat;        
        feature[k++] = "ChildDepSet_" + childdepset;        
        feature[k++] = "ChildPOSSet_" + childposset;                
        feature[k++] = "ChildWordSet_" + childwordset;                

        feature[k++] = "ArdW_" + arg.form;        
        feature[k++] = "ArgPOS_" + arg.ppos;        
        feature[k++] = "APW_" + arg.form + prd.form;        
        feature[k++] = "APF_" + arg.plemma + prd.plemma;        
        feature[k++] = "APPL_" + arg.ppos + prd.plemma;        
        feature[k++] = "APPS_" + arg.plemma + sense;        
        feature[k++] = "DepRelPath_" + dep_r_path;        
        feature[k++] = "POSPath_" + dep_pos_path;        
        feature[k++] = "Position_" +  position;        
        feature[k++] = "Position+PredLemmaSense" +  position + sense;        
        
        feature[k++] = "ArgW+PredLemmaSense" +  arg.form + sense;        
        feature[k++] = "POSPath+PredLemmaSense" +  dep_pos_path + sense;        
        feature[k++] = "Position+ArgPOS" +  position + arg.ppos;
        feature[k++] = "ChildWordSet+PredPOS_" + childwordset + prd.ppos;                
        feature[k++] = "DepSubCat+PredParentWord_" + subcat + pparent.form;        


        for (int i=0; i<prd.leftsiblingpos.size(); ++i)        
            feature[k++] = "LeftSiblingPOS" + prd.leftsiblingpos.get(i);
        
        if ("V".equals(pos)) {
            // 10
            feature[k++] = "ArgDeprel_" + arg.pdeprel;        
            feature[k++] = "ArgDeprel+PredLemmaSense_" + arg.pdeprel + sense;        
            feature[k++] = "ArgDeprel+ArgPOS_" + arg.pdeprel + arg.ppos;        
            feature[k++] = "ArgPOS+ArgWord" +  arg.ppos + arg.form;        
            feature[k++] = "DepRelPath+ArgDeprel_" + dep_r_path + arg.pdeprel;        
            feature[k++] = "RightWord" + prd.rightmostw;            
            feature[k++] = "RightPOS" + prd.rightmostpos;            
            feature[k++] = "RightPOS+ArgDeprel" + prd.rightmostpos + arg.pdeprel;            
            feature[k++] = "LeftPOS" + prd.leftmostpos;            
            feature[k++] = "RightPOS+LeftPOS" + prd.rightmostpos + prd.leftmostpos;            
            for (int i=0; i<prd.rightsiblingw.size(); ++i)
                feature[k++] = "RightSiblingWord" + prd.rightsiblingw.get(i);
            for (int i=0; i<prd.rightsiblingpos.size(); ++i) {
                feature[k++] = "POSPath+RightSiblingPOS" + dep_pos_path + prd.rightsiblingpos.get(i);
            }
        }
        else {
            feature[k++] = "ChildWordSet+PredLemmaSense_" + childwordset + sense;                
            feature[k++] = "ArgPOS+PredLemmaSense" +  arg.ppos + sense;        
            feature[k++] = "Position+ArgW" +  position + arg.form;        
            feature[k++] = "Position+DepRelPath" +  position + dep_r_path;        
            feature[k++] = "RightWord" + prd.rightmostw;            
            feature[k++] = "RightPOS" + prd.rightmostpos;            
            for (int i=0; i<prd.leftsiblingw.size(); ++i)
                feature[k++] = "LeftSiblingWord" + prd.leftsiblingw.get(i);
            for (int i=0; i<prd.leftsiblingpos.size(); ++i)
                feature[k++] = "LeftSiblingPOS+PredLemmaSense" + prd.leftsiblingpos.get(i);
            for (int i=0; i<prd.rightsiblingpos.size(); ++i)
                feature[k++] = "RightSiblingPOS+PredLemmaSense" + prd.rightsiblingpos.get(i);
        }
        
        feature = conjoin(feature, pos);

//        if (cache[prd_i][arg_i] == null)        
//            cache[prd_i][arg_i] = feature;
        
        return feature;
    }
*/
    final public String[] instantiateSecondOrdFeature(final Sentence sentence,
                                                         final String[] feature_i,
                                                         final String[] feature_j,
                                                         final int prd_i,
                                                         final int prd_j,
                                                         final int arg_i,
                                                         final int arg_j) {
        k = 0;
        String[] feature = new String[18];

        final ArrayList<Token> tokens = sentence.tokens;        
        final Token prd1 = tokens.get(sentence.preds[prd_i]);
        final Token prd2 = tokens.get(sentence.preds[prd_j]);
        final Token arg1 = tokens.get(prd1.arguments.get(arg_i));
        final Token arg2 = tokens.get(prd2.arguments.get(arg_j));

        final String p_posit = position(prd1.id, prd2.id);
        final String p1p2a1_posit = position(prd1.id, prd2.id, arg1.id);
        final String p1p2a2_posit = position(prd1.id, prd2.id, arg2.id);
        final String p1a1_posit = position(prd1.id, arg1.id);
        final String p1a2_posit = position(prd1.id, arg2.id);
        final String p2a1_posit = position(prd2.id, arg1.id);
        final String p2a2_posit = position(prd2.id, arg2.id);

        final String[][] deprel_path = sentence.dep_r_path;
        final String p1a1_dpath = deprel_path[prd_i][arg1.id];        
        final String p2a1_dpath = deprel_path[prd_j][arg1.id];        
        final String p1a2_dpath = deprel_path[prd_i][arg2.id];        
        final String p2a2_dpath = deprel_path[prd_j][arg2.id];        
        final String p1p2_dpath = deprel_path[prd_i][prd2.id];        
        
        
        final String pw1 = feature_i[0];
        final String pw2 = feature_j[0];
        final String aw1 = feature_i[9];
        final String aw2 = feature_j[9];
        final String ad1 = feature_i[11];
        final String ad2 = feature_j[11];

        feature[k++] = "BiPredWord" + pw1 + pw2 + p_posit;
        feature[k++] = "BiPredWord+DeprelPath" + pw1 + pw2 + p1p2_dpath;
        feature[k++] = "BiArgWord" + aw1 + aw2 + p1p2a1_posit + p1p2a2_posit;
        feature[k++] = "BiArgDeprel" + ad1 + ad2 + p1p2a1_posit + p1p2a2_posit;

        feature[k++] = "UniPredWord+BiArgWord1" + pw1 + aw1 + aw2 + p1a1_posit + p1a2_posit;
        feature[k++] = "UniPredWord+BiArgWord2" + pw2 + aw1 + aw2 + p2a1_posit + p2a2_posit;
        feature[k++] = "UniPredWord+BiArgDeprel1" + pw1 + ad1 + ad2 + p1a1_posit + p1a2_posit;
        feature[k++] = "UniPredWord+BiArgDeprel2" + pw2 + ad1 + ad2 + p2a1_posit + p2a2_posit;
        feature[k++] = "UniPredWord+BiArgWord1+DeprelPath" + pw1 + aw1 + aw2 + p1a1_dpath + p1a2_dpath;
        feature[k++] = "UniPredWord+BiArgWord2+DeprelPath" + pw2 + aw1 + aw2 + p2a1_dpath + p2a2_dpath;

        feature[k++] = "BiPredWord+UniArgWord1" + pw1 + pw2 + aw1 + p_posit + p1p2a1_posit;
        feature[k++] = "BiPredWord+UniArgWord2" + pw1 + pw2 + aw2 + p_posit + p1p2a2_posit;
        feature[k++] = "BiPredWord+UniArgDeprel1" + pw1 + pw2 + ad1 + p_posit + p1p2a1_posit;
        feature[k++] = "BiPredWord+UniArgDeprel2" + pw1 + pw2 + ad2 + p_posit + p1p2a2_posit;
        feature[k++] = "BiPredWord+UniArgWord1+DeprelPath" + pw1 + pw2 + aw1 + p1a1_dpath + p2a1_dpath;
        feature[k++] = "BiPredWord+UniArgWord2+DeprelPath" + pw1 + pw2 + aw2 + p1a2_dpath + p2a2_dpath;
        
        feature[k++] = "BiPredWord+BiArgWord2" + pw1 + pw2 + aw1 + aw2 + p_posit + p1p2a1_posit + p1p2a2_posit;
        feature[k++] = "BiPredWord+BiArgWord2+DeprelPath" + pw1 + pw2 + aw1 + aw2 + p1p2_dpath + p1a1_dpath + p2a2_dpath;
        
        return feature;
    }

    
    final public String[] instantiateAIFeature(final Sentence sentence,
                                                  final int prd_i,
                                                  final int arg_i) {
        k = 0;        

        final ArrayList<Token> tokens = sentence.tokens;
        String[][][] cache = g_cache.get(sentence.index);

        if (cache[prd_i][arg_i] != null)
            return cache[prd_i][arg_i];
        
        final Token prd = tokens.get(sentence.preds[prd_i]);
        final String pos = pos(prd);        

        String[] feature;
        if ("V".equals(pos))
            feature = new String[35 + prd.rightsiblingw.size() + prd.leftsiblingpos.size() + prd.rightsiblingpos.size()];
        else
            feature = new String[31 + prd.leftsiblingw.size() + prd.leftsiblingpos.size()
                    + prd.leftsiblingpos.size() + prd.rightsiblingpos.size()];            
        
        final Token pparent = tokens.get(prd.phead);            
        final String sense = prd.plemma + prd.pred;        
        final String subcat = prd.subcat;        
        final String childdepset = prd.childdepset;        
        final String childposset = prd.childposset;
        final String childwordset = prd.childwordset;
                        
        final Token arg = tokens.get(arg_i);
        final String dep_r_path = sentence.dep_r_path[prd_i][arg.id];        
        final String dep_pos_path = sentence.dep_pos_path[prd_i][arg.id];        
        final String position = position(prd.id, arg.id);


        // 25
        feature[k++] = "PredW_" + prd.form;        
        feature[k++] = "PredPOS_" + prd.ppos;        
        feature[k++] = "PredLemma_" + prd.plemma;        
        feature[k++] = "PredLemmaSense_" + sense;        
        feature[k++] = "PredParentW_" + pparent.form;        
        feature[k++] = "PredParentPOS_" + pparent.ppos;        
        feature[k++] = "DepSubCat_" + subcat;        
        feature[k++] = "ChildDepSet_" + childdepset;        
        feature[k++] = "ChildPOSSet_" + childposset;                
        feature[k++] = "ChildWordSet_" + childwordset;                

        feature[k++] = "ArdW_" + arg.form;        
        feature[k++] = "ArgPOS_" + arg.ppos;        
        feature[k++] = "APW_" + arg.form + prd.form;        
        feature[k++] = "APF_" + arg.plemma + prd.plemma;        
        feature[k++] = "APPL_" + arg.ppos + prd.plemma;        
        feature[k++] = "APPS_" + arg.plemma + sense;        
        feature[k++] = "DepRelPath_" + dep_r_path;        
        feature[k++] = "POSPath_" + dep_pos_path;        
        feature[k++] = "Position_" +  position;        
        feature[k++] = "Position+PredLemmaSense" +  position + sense;        
        
        feature[k++] = "ArgW+PredLemmaSense" +  arg.form + sense;        
        feature[k++] = "POSPath+PredLemmaSense" +  dep_pos_path + sense;        
        feature[k++] = "Position+ArgPOS" +  position + arg.ppos;
        feature[k++] = "ChildWordSet+PredPOS_" + childwordset + prd.ppos;                
        feature[k++] = "DepSubCat+PredParentWord_" + subcat + pparent.form;        


        for (int i=0; i<prd.leftsiblingpos.size(); ++i)        
            feature[k++] = "LeftSiblingPOS" + prd.leftsiblingpos.get(i);
        
        if ("V".equals(pos)) {
            // 10
            feature[k++] = "ArgDeprel_" + arg.pdeprel;        
            feature[k++] = "ArgDeprel+PredLemmaSense_" + arg.pdeprel + sense;        
            feature[k++] = "ArgDeprel+ArgPOS_" + arg.pdeprel + arg.ppos;        
            feature[k++] = "ArgPOS+ArgWord" +  arg.ppos + arg.form;        
            feature[k++] = "DepRelPath+ArgDeprel_" + dep_r_path + arg.pdeprel;        
            feature[k++] = "RightWord" + prd.rightmostw;            
            feature[k++] = "RightPOS" + prd.rightmostpos;            
            feature[k++] = "RightPOS+ArgDeprel" + prd.rightmostpos + arg.pdeprel;            
            feature[k++] = "LeftPOS" + prd.leftmostpos;            
            feature[k++] = "RightPOS+LeftPOS" + prd.rightmostpos + prd.leftmostpos;            
            for (int i=0; i<prd.rightsiblingw.size(); ++i)
                feature[k++] = "RightSiblingWord" + prd.rightsiblingw.get(i);
            for (int i=0; i<prd.rightsiblingpos.size(); ++i) {
                feature[k++] = "POSPath+RightSiblingPOS" + dep_pos_path + prd.rightsiblingpos.get(i);
            }
        }
        else {
            feature[k++] = "ChildWordSet+PredLemmaSense_" + childwordset + sense;                
            feature[k++] = "ArgPOS+PredLemmaSense" +  arg.ppos + sense;        
            feature[k++] = "Position+ArgW" +  position + arg.form;        
            feature[k++] = "Position+DepRelPath" +  position + dep_r_path;        
            feature[k++] = "RightWord" + prd.rightmostw;            
            feature[k++] = "RightPOS" + prd.rightmostpos;            
            for (int i=0; i<prd.leftsiblingw.size(); ++i)
                feature[k++] = "LeftSiblingWord" + prd.leftsiblingw.get(i);
            for (int i=0; i<prd.leftsiblingpos.size(); ++i)
                feature[k++] = "LeftSiblingPOS+PredLemmaSense" + prd.leftsiblingpos.get(i);
            for (int i=0; i<prd.rightsiblingpos.size(); ++i)
                feature[k++] = "RightSiblingPOS+PredLemmaSense" + prd.rightsiblingpos.get(i);
        }
        
        feature = conjoin(feature, pos);

        if (cache[prd_i][arg_i] == null)        
            cache[prd_i][arg_i] = feature;
        
        return feature;
    }

    
    
    final public String[] instantiatePDFeature(final Sentence sentence,
                                                  final int prd_i) {
        k = 0;        
        String[] feature = new String[9];

        final ArrayList<Token> tokens = sentence.tokens;
        String[][] cache = pd_cache.get(sentence.index);

        if (cache[prd_i] != null) return cache[prd_i];
        
        final Token prd = tokens.get(sentence.preds[prd_i]);
        final String pos = pos(prd);        
        final Token pparent = tokens.get(prd.phead);            
        final String subcat = prd.subcat;        
        final String childdepset = prd.childdepset;        
        final String childposset = prd.childposset;
        final String childwordset = prd.childwordset;
                        
        feature[k++] = "PredW_" + prd.form;        
        feature[k++] = "PredPOS_" + prd.ppos;        
        feature[k++] = "PredDeprel_" + prd.pdeprel;        
        feature[k++] = "PredParentW_" + pparent.form;        
        feature[k++] = "PredParentPOS_" + pparent.ppos;        
        feature[k++] = "DepSubCat_" + subcat;        
        feature[k++] = "ChildDepSet_" + childdepset;        
        feature[k++] = "ChildPOSSet_" + childposset;                
        feature[k++] = "ChildWordSet_" + childwordset;                
        
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
        if (arg < prd) return "Before";
        else if (arg > prd) return "After";
        else return "On";
    }

    final private String position(final int prd1_id, final int prd2_id,
                                   final int arg_id) {
        return position(prd1_id, arg_id) + position(prd2_id, arg_id);
    }
    
    
    final public String pos(final Token prd) {
        if (prd.ppos.startsWith("V")) return "V";
        return "N";
    }
    
    final public String[] conjoin(final String[] feature, final String label) {
        final String[] new_feature = new String[feature.length];
        for (int i=0; i<new_feature.length; ++i)
            new_feature[i] = feature[i] + label;
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
