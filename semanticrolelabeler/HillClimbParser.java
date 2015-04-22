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
    public float correct, total, p_total, r_total;
    public long time;
    public int restart;

    public HillClimbParser(final int weight_length, final int restart) {
//        this.perceptron = new MultiClassPerceptron(RoleDict.size(), weight_length);
        this.perceptron = new MultiClassPerceptron(RoleDict.biroledict.size(), weight_length);
//        this.perceptron = new MultiClassPerceptron(RoleDict.size(), RoleDict.size(), weight_length);
        this.feature_extracter = new FeatureExtracter(weight_length);
        this.feature_extracter.g_cache = new ArrayList();
        this.rnd = new Random();
        this.restart = restart;
    }
    
    final public void train(final ArrayList<Sentence> sentencelist) {
        correct = 0.0f;
        total = 0.0f;

        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);
                        
            if (feature_extracter.g_cache.size() < i+1)
                feature_extracter.g_cache.add(new String[sentence.preds.length][sentence.size()][]);

            if (sentence.preds.length == 0) continue;
            if (checkArguments(sentence)) continue;

            final int[][][] features = createFeatures(sentence, false);
            ArrayList<Integer>[] best_graph = decode(sentence, features, false);
            
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

    
    final public void trainSecond(final ArrayList<Sentence> sentencelist) {
        correct = 0.0f;
        total = 0.0f;

        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);
                        
            if (sentence.preds.length == 0) continue;
            if (checkArguments(sentence)) continue;

            final int[][][] features = createFeatures(sentence, false);
            final int[][][][][] features2 = createSecondFeatures(sentence, false);
            ArrayList<Integer>[] best_graph = decodeSecond(sentence, features, features2, false);
            
            updateWeights(sentence.o_graph, best_graph, features);
            updateWeights(sentence.o_graph, best_graph, features2);
            perceptron.t -= 1.0;

            checkAccuracy(sentence.o_graph, best_graph);

//            if (i%100 == 0 && i != 0) {
                System.out.print(String.format("%d ", i));
//            }
            
        }
        
        System.out.println("\n\tCorrect: " + correct);                        
        System.out.println("\tTotal: " + total);                        
        System.out.println("\tAccuracy: " + correct/total);
    }

    
    final public void test(final ArrayList<Sentence> testsentencelist) {
        time = (long) 0.0;

        for (int i=0; i<testsentencelist.size(); ++i) {
            final Sentence sentence = testsentencelist.get(i);
                        
            if (feature_extracter.g_cache.size() < i+1)
                feature_extracter.g_cache.add(new String[sentence.preds.length][sentence.size()][]);

            if (sentence.preds.length == 0) continue;
            if (checkArguments(sentence)) continue;

            long time1 = System.currentTimeMillis();
            final int[][][] features = createFeatures(sentence, true);
            sentence.p_graph = decode(sentence, features, true);
            long time2 = System.currentTimeMillis();

            time += time2 - time1;
            
            if (i%1000 == 0 && i != 0) {
                System.out.print(String.format("%d ", i));
            }
            
        }
    }

    final public void testSecond(final ArrayList<Sentence> testsentencelist) {
        time = (long) 0.0;

        for (int i=0; i<testsentencelist.size(); ++i) {
            final Sentence sentence = testsentencelist.get(i);
                        
            if (sentence.preds.length == 0) continue;
            if (checkArguments(sentence)) continue;

            long time1 = System.currentTimeMillis();
            final int[][][] features = createFeatures(sentence, true);
            final int[][][][][] features2 = createSecondFeatures(sentence, true);
            sentence.p_graph = decodeSecond(sentence, features, features2, true);
            long time2 = System.currentTimeMillis();

            time += time2 - time1;
            
//            if (i%100 == 0 && i != 0) {
                System.out.print(String.format("%d ", i));
//            }
            
        }
    }

    
    final public void eval(final ArrayList<Sentence> testsentencelist,
                            final ArrayList<Sentence> evalsentencelist) {
        correct = 0.0f;
        p_total = 0.0f;
        r_total = 0.0f;
        
        for (int i=0; i<testsentencelist.size(); ++i) {
            final Sentence testsentence = testsentencelist.get(i);
            final Sentence evalsentence = evalsentencelist.get(i);

            if (testsentence.preds.length == 0) continue;

            final ArrayList<Token> o_tokens = evalsentence.tokens;
            final ArrayList<Token> tokens = testsentence.tokens;

            final ArrayList<Integer>[] o_graph = evalsentence.o_graph;
            final ArrayList<Integer>[] p_graph = testsentence.p_graph;
                        
            if (p_graph == null && o_graph != null) {
                for (int j=0; j<o_graph.length; ++j) {
                    final ArrayList<Integer> o_arguments = o_tokens.get(evalsentence.preds[j]).arguments;
                    r_total += o_arguments.size();
                }
                continue;
            }
            else if (o_graph == null && p_graph != null) {
                for (int j=0; j<p_graph.length; ++j) {
                    final ArrayList<Integer> p_arguments = tokens.get(testsentence.preds[j]).arguments;
                    p_total += p_arguments.size();
                }
                continue;
            }
            else if (o_graph == null && p_graph == null) {
                continue;
            }
            
            for (int j=0; j<p_graph.length; ++j) {
                final ArrayList<Integer> p_roles = p_graph[j];
                final ArrayList<Integer> o_roles = o_graph[j];
                
                final ArrayList<Integer> arguments = tokens.get(testsentence.preds[j]).arguments;
                final ArrayList<Integer> o_arguments = o_tokens.get(evalsentence.preds[j]).arguments;
                p_total += arguments.size();
                r_total += o_arguments.size();
                
                for (int k=0; k<arguments.size(); ++k) {
                    final int arg_id = arguments.get(k);
                    final int role = p_roles.get(k);
                    
                    if (match(arg_id, role, o_roles, o_arguments)) correct += 1.0f;
                }
            }            
        }
        
        float p = correct/p_total;
        float r = correct/r_total;
        System.out.println("\n\tAC Test Correct: " + correct);
        System.out.println("\tAC Test R_Total: " + r_total);
        System.out.println("\tAC Test P_Total: " + p_total);
        System.out.println("\tAC Test Precision: " + p);
        System.out.println("\tAC Test Recall: " + r);
        System.out.println("\tAC Test F1: " + (2*p*r)/(p+r));
        
    }
    
    final private boolean match(final int arg_id, final int role,
                                 final ArrayList<Integer> o_roles, ArrayList<Integer> o_arguments) {
        for (int i=0; i<o_arguments.size(); ++i) {
            final int o_arg_id = o_arguments.get(i);
            final int o_role = o_roles.get(i);
            
            if (arg_id == o_arg_id && role == o_role) return true;
        }
        return false;
    }
    
    
    final public ArrayList<Integer>[] decode(final Sentence sentence,
                                              final int[][][] features,
                                              final boolean test) {
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
                final float overall_score = getOverallScore(prev_graph, scores);
                
                for (int prd_i=0; prd_i<prds_length; ++prd_i) {
                    final ArrayList<Integer> proposition = propositions[prd_i];

                    final ArrayList<Integer> arguments = tokens.get(preds[prd_i]).arguments;

                    final ArrayList<Integer> tmp_prev_graph = prev_graph[prd_i];
                    final float[][] tmp_scores = scores[prd_i];

                    for (int arg_i=0; arg_i<arguments.size(); ++arg_i) {
                        final int prev_role = tmp_prev_graph.get(arg_i);
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

    
    final public ArrayList<Integer>[] decodeSecond(final Sentence sentence,
                                                     final int[][][] features,
                                                     final int[][][][][] features2,
                                                     final boolean test) {
        final ArrayList<Token> tokens = sentence.tokens;
        final int[] preds = sentence.preds;
        final int prds_length = sentence.preds.length;
        final ArrayList<Integer>[] propositions = setPropositions2(sentence);
        final float[][][] scores1 = getScores(sentence, propositions, features);
        final float[][][][][][] scores2 = getScores(sentence, propositions, features2);
        final ArrayList<Integer> proposition = RoleDict.rolearray;
        final int prop_length = proposition.size();

        ArrayList<Integer>[] best_graph = new ArrayList[prds_length];
        float prev_best_score = -10000000000.0f, best_score = -10000000000.0f;
        int best_arg_i = -1, best_prd = -1, best_role = -1;
        int prev_best_arg_i = -1, prev_best_prd = -1, prev_best_role = -1;

        for (int i=0; i<restart; ++i) {
            ArrayList<Integer>[] graph = setInitGraph(sentence, propositions);

            while (true) {
//                final ArrayList<Integer>[] prev_graph = copyGraph(graph);
//                final float overall_score = getOverallScore(prev_graph, scores1) + getOverallScore(prev_graph, scores2);
                
                for (int prd_i=0; prd_i<prds_length; ++prd_i) {
//                    final ArrayList<Integer> proposition = propositions[prd_i];

                    final ArrayList<Integer> arguments = tokens.get(preds[prd_i]).arguments;

//                    final ArrayList<Integer> tmp_prev_graph = prev_graph[prd_i];
//                    final float[][] tmp_scores = scores1[prd_i];

                    for (int arg_i=0; arg_i<arguments.size(); ++arg_i) {
//                        final int prev_role = tmp_prev_graph.get(arg_i);
//                        final float[] tmp_scores2 = tmp_scores[arg_i];
                        
//                        final float tmp_overall_score = overall_score
//                                     - tmp_scores2[prev_role]
//                                     - getSecondOrdScores(scores2, propositions, prd_i, arg_i, prev_role);
                        
                        for (int role_i=0; role_i<prop_length; ++role_i) {
                            final int role = proposition.get(role_i);
                            final ArrayList<Integer>[] tmp_graph = changeGraph(graph, prd_i, role, arg_i);
                            final float score = getOverallScore(tmp_graph, scores1) + getOverallScore(tmp_graph, scores2);
//                            float score = tmp_overall_score + tmp_scores2[role] + getSecondOrdScores(scores2, propositions, prd_i, arg_i, role);
                            
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
            preds[i] = FrameDict.addAndGet(pred.cpos, roleset);
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
//        final float[][][] scores = new float[sentence.preds.length][sentence.size()][RoleDict.size()];
        final float[][][] scores = new float[sentence.preds.length][sentence.max_arg_length][RoleDict.size()];
        
        for (int prd_i=0; prd_i<sentence.preds.length; ++prd_i) {
            final float[][] tmp_scores = scores[prd_i];
            final int[][] tmp_features = features[prd_i];
            final Token pred = sentence.tokens.get(sentence.preds[prd_i]);
            final ArrayList<Integer> arguments = pred.arguments;
            final ArrayList<Integer> proposition = propositions[prd_i];
            
            for (int arg_i=0; arg_i<arguments.size(); ++arg_i) {
                final float[] tmp_scores2 = tmp_scores[arg_i];
                final int[] feature = tmp_features[arg_i];
                
                for (int role_i=0; role_i<proposition.size(); ++role_i) {
                    final int role = proposition.get(role_i);
                    tmp_scores2[role] = calcScore(feature, RoleDict.biroledict.get(String.valueOf(role)));
                }
            }
        }

        return scores;
    }
    
    final private float[][][][][][] getScores(final Sentence sentence,
                                               final ArrayList<Integer>[] propositions,
                                               final int[][][][][] features2) {
//        final float[][][][][][] scores = new float[sentence.preds.length][sentence.size()][RoleDict.size()][sentence.preds.length][sentence.size()][RoleDict.size()];
        final float[][][][][][] scores = new float[sentence.preds.length][sentence.max_arg_length][RoleDict.size()][sentence.preds.length][sentence.max_arg_length][RoleDict.size()];
        
        for (int prd_i=0; prd_i<sentence.preds.length; ++prd_i) {
            final float[][][][][] tmp_scores = scores[prd_i];
            final int[][][][] tmp_features = features2[prd_i];

            final Token pred = sentence.tokens.get(sentence.preds[prd_i]);
            final ArrayList<Integer> arguments1 = pred.arguments;
            final ArrayList<Integer> proposition1 = propositions[prd_i];
            
            for (int arg_i=0; arg_i<arguments1.size(); ++arg_i) {
                final float[][][][] tmp_scores2 = tmp_scores[arg_i];
                final int[][][] tmp_features2 = tmp_features[arg_i];

                for (int role_i=0; role_i<proposition1.size(); ++role_i) {                
                    final int role1 = proposition1.get(role_i);                    
                    final float[][][] tmp_scores3 = tmp_scores2[role1];
                    
                    for (int prd_j=prd_i; prd_j<sentence.preds.length; ++prd_j) {                
                        final float[][] tmp_scores4 = tmp_scores3[prd_j];                    
                        final int[][] tmp_features3 = tmp_features2[prd_j];
    
                        final Token pred2 = sentence.tokens.get(sentence.preds[prd_j]);                    
                        final ArrayList<Integer> arguments2 = pred2.arguments;                    
                        final ArrayList<Integer> proposition2 = propositions[prd_j];

                        final int p;
                        if (prd_i == prd_j) p = arg_i+1;
                        else p=0;
                                        
                        for (int arg_j=p; arg_j<arguments2.size(); ++arg_j) {
                            final float[] tmp_scores5 = tmp_scores4[arg_j];
                            final int[] tmp_features4 = tmp_features3[arg_j];
                

                            for (int role_j=0; role_j<proposition2.size(); ++role_j) {
                                final int role2 = proposition2.get(role_j);
                                tmp_scores5[role2] = calcScore(tmp_features4, RoleDict.biroledict.get(String.valueOf(role1) + "-" + String.valueOf(role2)));
//                                tmp_scores5[role2] = calcScore(tmp_features4, role1, role2);
                            }
                        }
                    }
                }
            }
        }

        return scores;
    }
    

    final private float getOverallScore(final ArrayList<Integer>[] graph,
                                          final float[][][] scores) {
        float score = 0.0f;
        
        for (int prd_i=0; prd_i<graph.length; ++prd_i) {
            final ArrayList<Integer> tmp_graph = graph[prd_i];
            final float[][] tmp_scores = scores[prd_i];
            
            for (int arg_i=0; arg_i<tmp_graph.size(); ++arg_i) {
                final int role = tmp_graph.get(arg_i);
                score += tmp_scores[arg_i][role];
            }
        }
        
        return score;
    }

    final private float getOverallScore(final ArrayList<Integer>[] graph,
                                          final float[][][][][][] scores) {
        float score = 0.0f;
        
        for (int prd_i=0; prd_i<graph.length; ++prd_i) {
            final ArrayList<Integer> tmp_graph = graph[prd_i];
            final float[][][][][] tmp_scores = scores[prd_i];
            
            for (int arg_i=0; arg_i<tmp_graph.size(); ++arg_i) {
                final int role1 = tmp_graph.get(arg_i);
                final float[][][] tmp_scores2 = tmp_scores[arg_i][role1];
                
                for (int prd_j=prd_i; prd_j<graph.length; ++prd_j) {
                    final ArrayList<Integer> tmp_graph2 = graph[prd_j];
                    final float[][] tmp_scores3 = tmp_scores2[prd_j];
            
                    final int p;
                    if (prd_i == prd_j) p = arg_i+1;
                    else p=0;
                                        
                    for (int arg_j=p; arg_j<tmp_graph2.size(); ++arg_j) {
                        final int role2 = tmp_graph2.get(arg_j);
                        score += tmp_scores3[arg_j][role2];
                    }
                }
                
            }
        }
        
        return score;
    }
    
    
    final private float getSecondOrdScores(final float[][][][][][] scores,
                                             final ArrayList<Integer>[] propositions,
                                             final int prd_i, final int arg_i,
                                             final int role) {
        float score = 0.0f;
        final float[][][] post_scores = scores[prd_i][arg_i][role];
        
        for (int prd_j=prd_i; prd_j<post_scores.length; ++prd_j) {
            final float[][] tmp_scores2 = post_scores[prd_j];
            final ArrayList<Integer> proposition = propositions[prd_j];

            final int p;            
            if (prd_i == prd_j) p = arg_i+1;            
            else p=0;
            
            for (int arg_j=p; arg_j<tmp_scores2.length; ++arg_j) {
                final float[] tmp_scores3 = tmp_scores2[arg_j];

                for (int role_j=0; role_j<tmp_scores3.length; ++role_j)
                    score += tmp_scores3[proposition.get(role_j)];
            }
        }

        for (int prd_j=0; prd_j<prd_i+1; ++prd_j) {
            final float[][][][][] tmp_scores2 = scores[prd_j];
            final ArrayList<Integer> proposition = propositions[prd_j];

            final int p;            
            if (prd_i == prd_j) p = tmp_scores2.length;            
            else p = arg_i;
            
            for (int arg_j=0; arg_j<p; ++arg_j) {
                final float[][][][] tmp_scores3 = tmp_scores2[arg_j];

                for (int role_j=0; role_j<tmp_scores3.length; ++role_j)
                    score += tmp_scores3[proposition.get(role_j)][prd_i][arg_i][role];
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
    
    final public int[][][] createFeatures(final Sentence sentence, final boolean test) {
        final ArrayList<Token> tokens = sentence.tokens;
        final int[] preds = sentence.preds;
//        final int[][][] features = new int[preds.length][sentence.size()][];
        final int[][][] features = new int[preds.length][sentence.max_arg_length][];
        
        for (int prd_i=0; prd_i<preds.length; ++prd_i) {
            final int[][] tmp_features = features[prd_i];
            final ArrayList arguments = tokens.get(preds[prd_i]).arguments;

            for (int arg_i=0; arg_i<arguments.size(); ++arg_i) {
                final String[] feature = feature_extracter.instantiateFirstOrdFeature(sentence, prd_i, arg_i);
                tmp_features[arg_i] = feature_extracter.encodeFeature2(feature);
            }
        }
        
        return features;
    }
    
    final public int[][][][][] createSecondFeatures(final Sentence sentence,
                                                       final boolean test) {
        final ArrayList<Token> tokens = sentence.tokens;
        final int[] preds = sentence.preds;
//        final int[][][][][] features = new int[preds.length][sentence.size()][preds.length][sentence.size()][];
        final int[][][][][] features = new int[preds.length][sentence.max_arg_length][preds.length][sentence.max_arg_length][];
        
        for (int prd_i=0; prd_i<preds.length; ++prd_i) {
            final int[][][][] tmp_features = features[prd_i];
            final ArrayList arguments1 = tokens.get(preds[prd_i]).arguments;

            for (int arg_i=0; arg_i<arguments1.size(); ++arg_i) {
                final int[][][] tmp_features2 = tmp_features[arg_i];
                final String[] feature_i = feature_extracter.instantiateFirstOrdFeature(sentence, prd_i, arg_i);

                for (int prd_j=prd_i; prd_j<preds.length; ++prd_j) {
                    final int[][] tmp_features3 = tmp_features2[prd_j];
                    final ArrayList arguments2 = tokens.get(preds[prd_j]).arguments;

                    final int p;
                    if (prd_i == prd_j) p = arg_i+1;
                    else p=0;
                    
                    for (int arg_j=p; arg_j<arguments2.size(); ++arg_j) {
                        final String[] feature_j = feature_extracter.instantiateFirstOrdFeature(sentence, prd_j, arg_j);
                        final String[] feature = feature_extracter.instantiateSecondOrdFeature(sentence, feature_i, feature_j, prd_i, prd_j, arg_i, arg_j);
                        tmp_features3[arg_j] = feature_extracter.encodeFeature2(feature);
                    }
                }
            }
        }
        
        return features;
    }
    
    final private float calcScore(final int[] feature, final int label){
        return perceptron.calcScore(feature, label);
    }
/*
    final private float calcScore(final int[] feature, final int label1, final int label2){
        return perceptron.calcScore(feature, label1, label2);
    }
*/
    
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
                }
                total += 1.0f;
            }
        }
    }
    
    final public boolean checkArguments(final Sentence sentence) {
        for (int j=0; j<sentence.preds.length; ++j) {        
            final Token pred = sentence.tokens.get(sentence.preds[j]);
            if (!pred.arguments.isEmpty()) return false;
        }
        return true;
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
                perceptron.updateWeights(RoleDict.biroledict.get(String.valueOf(o_role)), RoleDict.biroledict.get(String.valueOf(role)), feature);
//                perceptron.updateWeights(o_role, role, feature);
            }
        }
    }

    final private void updateWeights(final ArrayList<Integer>[] o_graph,
                                       final ArrayList<Integer>[] graph,
                                       final int[][][][][] features) {
        for (int prd_i=0; prd_i<o_graph.length; ++prd_i) {
            final ArrayList<Integer> tmp_o_graph1 = o_graph[prd_i];
            final ArrayList<Integer> tmp_graph1 = graph[prd_i];
            final int[][][][] tmp_features = features[prd_i];
            
            for (int arg_i=0; arg_i<tmp_o_graph1.size(); ++arg_i) {
                final int o_role1 = tmp_o_graph1.get(arg_i);
                final int role1 = tmp_graph1.get(arg_i);
                final int[][][] tmp_features2 = tmp_features[arg_i];
                
                for (int prd_j=prd_i; prd_j<o_graph.length; ++prd_j) {
                    final ArrayList<Integer> tmp_o_graph2 = o_graph[prd_j];
                    final ArrayList<Integer> tmp_graph2 = graph[prd_j];
                    final int[][] tmp_features3 = tmp_features2[prd_j];

                    final int p;
                    if (prd_i == prd_j) p = arg_i+1;
                    else p=0;
                    
                    for (int arg_j=p; arg_j<tmp_o_graph2.size(); ++arg_j) {
                        final int o_role2 = tmp_o_graph2.get(arg_j);
                        final int role2 = tmp_graph2.get(arg_j);
                        final int[] feature = tmp_features3[arg_j];
                
                        perceptron.updateWeights(RoleDict.biroledict.get(String.valueOf(o_role1) + "-" + String.valueOf(o_role2)),
                                                 RoleDict.biroledict.get(String.valueOf(role1) + "-" + String.valueOf(role2)),
                                                 feature);
//                        perceptron.updateWeights(o_role1, o_role2, role1, role2, feature);
                    }
                }
                
            }
        }
    }
    
    
}
