/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package feature;

import io.LookupTable;
import io.PathLookupTable;
import io.RoleDict;
import io.Sentence;
import io.Token;
import java.util.ArrayList;

/**
 *
 * @author hiroki
 */
public class FeatureExtractor {
    final int weight_size, role_size;
    int k;
    int total;
    public ArrayList<String[][][]> g_cache;
    public ArrayList<String[][][][][]> second_cache;
    public ArrayList<String[][]> pd_cache;    
    
    public FeatureExtractor(final int weight_size) {
        this.weight_size = weight_size;
        this.role_size = RoleDict.rolearray.size();
        this.g_cache = new ArrayList();
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

    final public double[] lookupFeature(final Sentence sentence, final int[] graph, final int prd_i) {        
        final double[] f_vector = new double[weight_size*(2*role_size+1)];
        final ArrayList<Token> tokens = sentence.tokens;
        final Token prd = tokens.get(sentence.preds[prd_i]);
//        final int[] input_args = inputArgs(graph);
        final int[] input_args = graph;

        final double[] prd_vec = prd.vec;
        for (int i=0; i<weight_size; ++i) f_vector[i] = prd_vec[i];
                
        for (int role=0; role<input_args.length; ++role) {
            final int arg_i = input_args[role];
            final double[] vec;
            final double[] path_vec;

            if (arg_i > -1) vec = tokens.get(prd.arguments.get(arg_i)).vec;
            else vec = LookupTable.get("*UNKNOWN*");

            if (arg_i > -1) path_vec = PathLookupTable.get(sentence.dep_path[prd_i][prd.arguments.get(arg_i)]);
            else path_vec = PathLookupTable.get("NULL");

//            for (int j=0; j<weight_size; ++j) f_vector[j+weight_size*(role+1)] = vec[j];
            for (int j=0; j<weight_size; ++j) f_vector[j+weight_size*(2*role+1)] = vec[j];
            for (int j=0; j<weight_size; ++j) f_vector[j+weight_size*(2*role+2)] = path_vec[j];
        }
        
        return f_vector;
    }
    
    final private int[] inputArgs(final int[] graph) {
        final int[] input = new int[role_size];
        for (int i=0; i<input.length; ++i) input[i] = -1;
        for (int arg_i=0; arg_i<graph.length; ++arg_i) {
            final int role = graph[arg_i];
            if (role > 0) input[role-1] = arg_i;
        }
        return input;
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
        String[] feature = new String[13];

        final ArrayList<Token> tokens = sentence.tokens;        
        final Token prd1 = tokens.get(sentence.preds[prd_i]);
        final Token prd2 = tokens.get(sentence.preds[prd_j]);
        final Token arg1 = tokens.get(prd1.arguments.get(arg_i));
        final Token arg2 = tokens.get(prd2.arguments.get(arg_j));

        final String pos = pos(prd1) + pos(prd2);        
        final String p_posit = position(prd1.id, prd2.id);
        final String a_posit = position(arg1.id, arg2.id);
//        final String p1p2a1_posit = position(prd1.id, prd2.id, arg1.id);
//        final String p1p2a2_posit = position(prd1.id, prd2.id, arg2.id);
//        final String p1a1_posit = dist(prd1.id, arg1.id);
//        final String p1a2_posit = dist(prd1.id, arg2.id);
//        final String p2a1_posit = dist(prd2.id, arg1.id);
//        final String p2a2_posit = dist(prd2.id, arg2.id);

//        final String[][] dpath = sentence.dep_path;
//        final String pdpath = dpath[prd_i][prd2.id];        
//        final String[][] deprel_path = sentence.dep_r_path;
//        final String p1a1_dpath = deprel_path[prd_i][arg1.id];        
//        final String p2a1_dpath = deprel_path[prd_j][arg1.id];        
//        final String p1a2_dpath = deprel_path[prd_i][arg2.id];        
//        final String p2a2_dpath = deprel_path[prd_j][arg2.id];        
//        final String p1p2_dpath = deprel_path[prd_i][prd2.id];        
        
        
        final String pw1 = prd1.form;
        final String pw2 = prd2.form;
        final String aw1 = arg1.form;
        final String aw2 = arg2.form;
//        final String ad1 = arg1.pdeprel;
//        final String ad2 = arg2.pdeprel;

        feature[k++] = "BiPredWord" + pw1 + pw2;
        feature[k++] = "BiPredPOS" + prd1.ppos + prd2.ppos;
        feature[k++] = "BiArgWord" + aw1 + aw2;
//        feature[k++] = "PredWord1" + pw1;
//        feature[k++] = "PredWord2" + pw2;
//        feature[k++] = "ArgWord1" + aw1;
//        feature[k++] = "ArgWord2" + aw2;
//        feature[k++] = "BiArgDeprel" + ad1 + ad2;

        feature[k++] = "UniPredWord+BiArgWord1" + pw1 + aw1 + aw2;
        feature[k++] = "UniPredWord+BiArgWord2" + pw2 + aw1 + aw2;
//        feature[k++] = "UniPredWord+BiArgDeprel1" + pw1 + ad1 + ad2;
//        feature[k++] = "UniPredWord+BiArgDeprel2" + pw2 + ad1 + ad2;

//        feature[k++] = "UniPredPOS+UniArgWord1" + prd1.ppos + aw1;
//        feature[k++] = "UniPredPOS+UniArgWord2" + prd2.ppos + aw1;
//        feature[k++] = "UniPredPOS+UniArgWord3" + prd1.ppos + aw2;
//        feature[k++] = "UniPredPOS+UniArgWord4" + prd2.ppos + aw2;

        feature[k++] = "UniPredPOS+UniArgPOS1" + prd1.ppos + arg1.ppos;
        feature[k++] = "UniPredPOS+UniArgPOS2" + prd2.ppos + arg1.ppos;
        feature[k++] = "UniPredPOS+UniArgPOS3" + prd1.ppos + arg2.ppos;
        feature[k++] = "UniPredPOS+UniArgPOS4" + prd2.ppos + arg2.ppos;
//        feature[k++] = "BiPredWord+UniArgWord2" + pw1 + pw2 + aw2;
//        feature[k++] = "BiPredWord+UniArgDeprel1" + pw1 + pw2 + ad1;
//        feature[k++] = "BiPredWord+UniArgDeprel2" + pw1 + pw2 + ad2;

        feature[k++] = "BiPredPOS+UniArgPOS1" + prd1.ppos + prd2.ppos + arg1.ppos;
        feature[k++] = "BiPredPOS+UniArgPOS2" + prd1.ppos + prd2.ppos + arg2.ppos;

        feature[k++] = "UniPredPOS+BiArgPOS1" + prd1.ppos + arg1.ppos + arg2.ppos;
        feature[k++] = "UniPredPOS+BiArgPOS2" + prd2.ppos + arg1.ppos + arg2.ppos;
        
//        feature[k++] = "BiPredWord+BiArgWord2" + pw1 + pw2 + aw1 + aw2 + p_posit;
//        feature[k++] = "Second_";

//        feature = conjoin(feature, pdpath, pos);
//        feature = conjoin(feature, p1p2_dpath, pos);
        feature = conjoin(feature, pos, p_posit+a_posit);
        
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

    final private String dist(final int prd, final int arg) {
        int dist = prd - arg;
        
        if (dist > 5) return "A";
        else if (dist == 4) return "B";
        else if (dist == 3) return "C";
        else if (dist == 2) return "D";
        else if (dist == 1) return "E";
        else if (dist == 0) return "F";
        else if (dist == -4) return "G";
        else if (dist == -3) return "H";
        else if (dist == -2) return "I";
        else if (dist == -1) return "J";
        else return "K";
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
/*
    final public String[] conjoin2(final String[] feature, final String role,
                                    final String pos) {
        final String[] new_feature = new String[k];
        for (int i=0; i<new_feature.length; ++i)
            new_feature[i] = feature[i] + role + pos;
        return new_feature;
    }
*/    
    
}
