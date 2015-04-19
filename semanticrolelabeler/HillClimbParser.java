/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author hiroki
 */
public class HillClimbParser {
    public MultiClassPerceptron perceptron;
    final public FeatureExtracter feature_extracter;
    public Random rnd;
    public float correct, total, t_correct, t_total;

    public HillClimbParser(final int weight_length) {
        this.perceptron = new MultiClassPerceptron(RoleDict.size(), weight_length);
        this.feature_extracter = new FeatureExtracter(weight_length);
        this.feature_extracter.g_cache = new ArrayList();
        this.rnd = new Random();
    }
    
    final public void train(final ArrayList<Sentence> sentencelist) {
        correct = 0.0f;
        total = 0.0f;
        t_correct = 0.0f;
        t_total = 0.0f;        

        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);
                        
            if (feature_extracter.g_cache.size() < i+1)
                feature_extracter.g_cache.add(new String[sentence.preds.length][sentence.size()][]);

            if (sentence.preds.length == 0) continue;
            if (checkArguments(sentence)) continue;

            final int[][][] features = createFeatures(sentence);
            ArrayList<Integer>[] best_graph = decode(sentence, features, 1);
            
            updateWeights(sentence.o_graph, best_graph, features);

            checkAccuracy(sentence.o_graph, best_graph);

            if (i%1000 == 0 && i != 0) {
                System.out.print(String.format("%d ", i));
            }
            
        }
        
        System.out.println("\n\tCorrect: " + correct);                        
        System.out.println("\tTotal: " + total);                        
        System.out.println("\tAccuracy: " + correct/total);
    }
    
    final public ArrayList<Integer>[] decode(final Sentence sentence,
                                              final int[][][] features,
                                              final int restart) {
        final ArrayList<Token> tokens = sentence.tokens;
        final int[] preds = sentence.preds;
        final int prds_length = sentence.preds.length;
        final ArrayList<Integer>[] propositions = setPropositions2(sentence);
        final float[][][] scores = getScores(sentence, propositions, features);

        ArrayList<Integer>[] best_graph = new ArrayList[prds_length];
        float prev_best_score = -10000000000.0f, best_score = -10000000000.0f;
        int best_arg_i = -1, best_prd = -1, best_role = -1;
        int prev_best_arg_i = -1, prev_best_prd = -1, prev_best_role = -1;

        for (int i=0; i<restart; ++i) {
            ArrayList<Integer>[] graph = setInitGraph(sentence, propositions);

            while (true) {
                final ArrayList<Integer>[] prev_graph = copyGraph(graph);
                final float overall_score = getOverallScore(sentence, prev_graph, scores);
                
                for (int prd_i=0; prd_i<prds_length; ++prd_i) {
                    final ArrayList<Integer> proposition = propositions[prd_i];
                    final ArrayList<Integer> arguments = tokens.get(preds[prd_i]).arguments;
                    final ArrayList<Integer> tmp_prev_graph = prev_graph[prd_i];
                    final float[][] tmp_scores = scores[prd_i];

                    for (int arg_i=0; arg_i<arguments.size(); ++arg_i) {
//                        final int arg = arguments.get(arg_i);
                        final int prev_role = tmp_prev_graph.get(arg_i);
//                        final float[] tmp_scores2 = tmp_scores[arg];
                        final float[] tmp_scores2 = tmp_scores[arg_i];
                        final float tmp_overall_score = overall_score - tmp_scores2[prev_role];
                        
                        for (int role_i=0; role_i<proposition.size(); ++role_i) {
                            final int role = proposition.get(role_i);
                            float score = tmp_overall_score + tmp_scores2[role];
                            
                            if (score > best_score) {
                                best_score = score;
                                best_prd = prd_i;
                                best_role = role;
                                best_arg_i = arg_i;
                            }
                        }                      
                    }
                }
                
                graph[best_prd].set(best_arg_i, best_role);
//                graph = changeGraph(prev_graph, best_prd, best_role, best);
//                if (isGraphMatch(graph, prev_graph)) break;
                if (best_prd == prev_best_prd && best_arg_i == prev_best_arg_i && best_role == prev_best_role)
                    break;
                else {
                    prev_best_prd = best_prd;
                    prev_best_arg_i = best_arg_i;
                    prev_best_role = best_role;
                }
            }

            if (best_score > prev_best_score) {
                best_graph = copyGraph(graph);
                prev_best_score = best_score;                
            }            
        }
        
        return best_graph;
    }

    
    final private ArrayList<Integer>[] setPropositions(final Sentence sentence) {
        final ArrayList<Integer>[] preds = new ArrayList[sentence.preds.length];
        
        for (int i=0; i<sentence.preds.length; ++i) {
            final Token pred = sentence.tokens.get(sentence.preds[i]);
            final int roleset = pred.pred;
            preds[i] = FrameDict.get(pred.plemma, roleset);
        }
        
        return preds;
    }

    final private ArrayList<Integer>[] setPropositions2(final Sentence sentence) {
        final ArrayList<Integer>[] preds = new ArrayList[sentence.preds.length];
        
        for (int i=0; i<sentence.preds.length; ++i) {
            preds[i] = RoleDict.rolearray;
        }
        
        return preds;
    }
    
    
    final private ArrayList<Integer>[] setInitGraph(final Sentence sentence,
                                                      final ArrayList<Integer>[] propositions) {
        final int prds_length = sentence.preds.length;
        final ArrayList<Token> tokens = sentence.tokens;
        final int[] preds = sentence.preds;
        final ArrayList<Integer>[] graph = new ArrayList[prds_length];

        for (int prd_i=0; prd_i<graph.length; ++prd_i) {
            final ArrayList<Integer> tmp_graph = new ArrayList();
            final Token pred = tokens.get(preds[prd_i]);
            final ArrayList arguments = pred.arguments;
            final ArrayList<Integer> proposition = propositions[prd_i];
            final int prop_length = proposition.size();

            for (int j=0; j<arguments.size(); ++j)
                tmp_graph.add(proposition.get(rnd.nextInt(prop_length)));
            
            graph[prd_i] = tmp_graph;
        }
        
        return graph;
    }

    final private HashMap<String, Integer>[] setInitZeroGraph(final Sentence sentence,
                                               final ArrayList<String>[] propositions) {
        final int prds_length = sentence.preds.length;
        final HashMap<String, Integer>[] graph = new HashMap[prds_length];

        for (int i=0; i<graph.length; ++i) {
            final HashMap<String, Integer> tmp_graph = new HashMap<>();
            final ArrayList<String> proposition = propositions[i];

            for (int j=0; j<proposition.size(); ++j)
                tmp_graph.put(proposition.get(j), 0);
            
            graph[i] = tmp_graph;
        }
        
        return graph;
    }
    
    final private ArrayList<Integer>[] copyGraph(final ArrayList<Integer>[] graph) {
        final ArrayList<Integer>[] copied_graph = new ArrayList[graph.length];

        for (int i=0; i<copied_graph.length; ++i) {
            copied_graph[i] = new ArrayList();
            final ArrayList<Integer> tmp_graph = graph[i];
            final ArrayList<Integer> tmp_copied_graph = copied_graph[i];
            
            for (int j=0; j<tmp_graph.size(); ++j)
                tmp_copied_graph.add(tmp_graph.get(j));
        }
        
        return copied_graph;
    }

    final private ArrayList<Integer>[] changeGraph(final ArrayList<Integer>[] graph,
                                                    final int prd_i,
                                                    final int target_role,
                                                    final int arg_i) {
        final ArrayList<Integer>[] copied_graph = new ArrayList[graph.length];

        for (int i=0; i<copied_graph.length; ++i) {
            copied_graph[i] = new ArrayList();
            final ArrayList<Integer> tmp_graph = graph[i];
            final ArrayList<Integer> tmp_copied_graph = copied_graph[i];
            
            for (int j=0; j<tmp_graph.size(); ++j) {
                if (i==prd_i && j==arg_i)
                    tmp_copied_graph.add(target_role);
                else
                    tmp_copied_graph.add(tmp_graph.get(j));
            }
        }
        
        return copied_graph;
    }
    
    
    final private boolean isGraphMatch(final ArrayList<Integer>[] graph1,
                                        final ArrayList<Integer>[] graph2) {
        for (int i=0; i<graph1.length; ++i) {
            final ArrayList<Integer> tmp_graph1 = graph1[i];
            final ArrayList<Integer> tmp_graph2 = graph2[i];
            
            for (int j=0; j<tmp_graph1.size(); ++j) {
                final int arg1 = tmp_graph1.get(j);
                final int arg2 = tmp_graph2.get(j);
                
                if (arg1 != arg2) return false;
            }
        }
        
        return true;
    }    
                
    final private float[][][] getScores(final Sentence sentence,
                                         final ArrayList<Integer>[] propositions,
                                         final int[][][] features) {
        final float[][][] scores = new float[sentence.preds.length][sentence.size()][RoleDict.size()];
        
        for (int prd_i=0; prd_i<sentence.preds.length; ++prd_i) {
            final float[][] tmp_scores = scores[prd_i];
            final int[][] tmp_features = features[prd_i];
            final Token pred = sentence.tokens.get(sentence.preds[prd_i]);
            final ArrayList<Integer> arguments = pred.arguments;
            final ArrayList<Integer> proposition = propositions[prd_i];
            
            for (int arg_i=0; arg_i<arguments.size(); ++arg_i) {
//                final int arg = arguments.get(arg_i);
                final float[] tmp_scores2 = tmp_scores[arg_i];
                final int[] feature = tmp_features[arg_i];
                
                for (int role_i=0; role_i<proposition.size(); ++role_i) {
                    final int role = proposition.get(role_i);
                    tmp_scores2[role] = calcScore(feature, role);
                }
            }
        }

        return scores;
    }
    
    final private float getOverallScore(final Sentence sentence,
                                          final ArrayList<Integer>[] graph,
                                          final float[][][] scores) {
        float score = 0.0f;
        final ArrayList<Token> tokens = sentence.tokens;
        final int[] preds = sentence.preds;
        
        for (int prd_i=0; prd_i<graph.length; ++prd_i) {
            final ArrayList<Integer> tmp_graph = graph[prd_i];
            final float[][] tmp_scores = scores[prd_i];
            final Token pred = tokens.get(preds[prd_i]);
//            final ArrayList<Integer> arguments = pred.arguments;
            
            for (int arg_i=0; arg_i<tmp_graph.size(); ++arg_i) {
                final int role = tmp_graph.get(arg_i);
//                score += tmp_scores[arguments.get(arg_i)][role];
                score += tmp_scores[arg_i][role];
            }
        }
        
        return score;
    }
    
    final private float getHammingDistance(final HashMap<String, Integer>[] o_graph,
                                             final HashMap<String, Integer>[] graph) {
        float hamming = 0.0f;

        for (int i=0; i<graph.length; ++i) {
            final HashMap<String, Integer> tmp_o_graph = o_graph[i];
            final HashMap<String, Integer> tmp_graph = graph[i];

            for (String role:tmp_o_graph.keySet()) {
                final int arg1 = tmp_o_graph.get(role);
                final int arg2 = tmp_graph.get(role);
                
                if (arg1 != arg2) hamming += 1.0f;
            }
        }
        
        return hamming;
    }
    
    final public int[][][] createFeatures(final Sentence sentence) {
        final ArrayList<Token> tokens = sentence.tokens;
        final int[] preds = sentence.preds;
        final int[][][] features = new int[preds.length][sentence.size()][];
        
        for (int prd_i=0; prd_i<preds.length; ++prd_i) {
            final int[][] tmp_features = features[prd_i];
            final ArrayList arguments = tokens.get(preds[prd_i]).arguments;
            final String pos = feature_extracter.pos(tokens.get(preds[prd_i]));

            for (int arg_i=0; arg_i<arguments.size(); ++arg_i) {
                final String[] feature = feature_extracter.instantiateFirstOrdFeature(sentence, prd_i, arg_i);
                final String[] c_feature = feature_extracter.conjoin(feature, pos);
                tmp_features[arg_i] = feature_extracter.encodeFeature2(c_feature);
            }
        }
        
        return features;
    }
    
    final private float calcScore(final int[] feature, final int label){
        return perceptron.calcScore(feature, label);
    }

    
    final public void checkAccuracy(final ArrayList<Integer>[] o_graph,
                                      final ArrayList<Integer>[] graph) {
        for (int i=0; i<o_graph.length; ++i) {
            final ArrayList<Integer> tmp_graph1 = o_graph[i];
            final ArrayList<Integer> tmp_graph2 = graph[i];
            
            for (int j=0; j<tmp_graph1.size(); ++j) {
                final int role1 = tmp_graph1.get(j);
                final int role2 = tmp_graph2.get(j);
                
                if (role1 == role2) {
                    correct += 1.0f;
                    t_correct += 1.0f;
                }
                t_total += 1.0f;
                total += 1.0f;
            }
        }
    }
    
    final public boolean checkArguments(final Sentence sentence) {
        for (int j=0; j<sentence.preds.length; ++j) {        
            final Token pred = sentence.tokens.get(sentence.preds[j]);
            if (pred.arguments.isEmpty()) return true;
        }
        return false;
    }
    
    final private void updateWeights(final ArrayList<Integer>[] o_graph,
                                       final ArrayList<Integer>[] graph,
                                       final int[][][] features) {
        for (int i=0; i<o_graph.length; ++i) {
            final ArrayList<Integer> tmp_o_graph = o_graph[i];
            final ArrayList<Integer> tmp_graph = graph[i];
            final int[][] tmp_features = features[i];
            
            for (int j=0; j<tmp_o_graph.size(); ++j) {
                final int o_role = tmp_o_graph.get(j);
                final int role = tmp_graph.get(j);
                final int[] feature = tmp_features[j];
                
                perceptron.updateWeights(o_role, role, feature);
            }
        }
    }
    
}
