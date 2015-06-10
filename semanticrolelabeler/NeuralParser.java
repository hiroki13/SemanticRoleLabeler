/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import Jama.Matrix;
import feature.FeatureExtractor;
import io.RoleDict;
import io.Sentence;
import io.Token;
import java.util.ArrayList;
import java.util.Random;
import learning.Classifier;

/**
 *
 * @author hiroki
 */
public class NeuralParser extends Parser{

    public NeuralParser(final Classifier c, final int weight_length, final int restart, final int prune) {
        this.classifier = c;
        this.weight_length = weight_length;
        this.feature_extracter = new FeatureExtractor(weight_length);
        this.rnd = new Random();
        this.restart = restart;
        this.prune = prune;
    }
    
    @Override
    final public void train(final ArrayList<Sentence> sentencelist) {
        correct = 0.0f;
        total = 0.0f;

        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);
                        
            if (sentence.preds.length == 0) continue;
            if (checkArguments(sentence)) continue;

            final int[][] best_graph = decodePerPAS2(sentence);
            
            checkAccuracy(sentence.o_graph, best_graph);

            if (i%100 == 0 && i != 0) {
                System.out.print(String.format("%d ", i));
            }
            
            if (i==prune) break;
            
        }
        
        System.out.println("\n\tCorrect: " + correct);                        
        System.out.println("\tTotal: " + total);                        
        System.out.println("\tAccuracy: " + correct/total);
    }
    
    @Override
    final public void test(final ArrayList<Sentence> testsentencelist) {
        time = (long) 0.0;

        for (int i=0; i<testsentencelist.size(); ++i) {
            final Sentence sentence = testsentencelist.get(i);
                        
            if (sentence.preds.length == 0) continue;
            if (checkArguments(sentence)) continue;

            long time1 = System.currentTimeMillis();
            sentence.p_graph = decodePerPAS(sentence);
            long time2 = System.currentTimeMillis();

            time += time2 - time1;
            
            if (i%100 == 0 && i != 0) {
                System.out.print(String.format("%d ", i));
            }
            
        }
    }

    @Override
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

            final int[][] o_graph = evalsentence.o_graph;
            final int[][] p_graph = testsentence.p_graph;
                        
            if (p_graph == null && o_graph != null) {
                for (int j=0; j<o_graph.length; ++j) {
                    final ArrayList<Integer> o_arguments = o_tokens.get(evalsentence.preds[j]).arguments;
//                    r_total += o_arguments.size();
                }
                continue;
            }
            else if (o_graph == null && p_graph != null) {
                for (int j=0; j<p_graph.length; ++j) {
                    final ArrayList<Integer> p_arguments = tokens.get(testsentence.preds[j]).arguments;
//                    p_total += p_arguments.size();
                }
                continue;
            }
            else if (o_graph == null && p_graph == null) {
                continue;
            }
            
            for (int j=0; j<p_graph.length; ++j) {
                final int[] p_roles = p_graph[j];
                final int[] o_roles = o_graph[j];
                
                final ArrayList<Integer> arguments = tokens.get(testsentence.preds[j]).arguments;
                final ArrayList<Integer> o_arguments = o_tokens.get(evalsentence.preds[j]).arguments;                
//                p_total += arguments.size();
//                r_total += o_arguments.size();
                for (int l=0; l<o_roles.length; ++l) {
                    final int r = o_roles[l];
                    if (r > 0) r_total += 1;
                }
                for (int l=0; l<p_roles.length; ++l) {
                    final int r = p_roles[l];
                    if (r > 0) p_total += 1;
                }
                
                for (int k=0; k<arguments.size(); ++k) {
                    final int arg_id = arguments.get(k);
                    final int role = p_roles[k];
                    
                    if (role == 0) continue;
                    
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

    final private boolean match(final int arg_id, final int role, final int[] o_roles,
                                 final ArrayList<Integer> o_arguments) {
        for (int i=0; i<o_arguments.size(); ++i) {
            final int o_arg_id = o_arguments.get(i);
            final int o_role = o_roles[i];
            
            if (arg_id == o_arg_id && role == o_role) return true;
        }
        return false;
    }
    
    final public int[][] decodePerPAS(final Sentence sentence) {
        final ArrayList<Token> tokens = sentence.tokens;
        final int[] preds = sentence.preds;
        final int prds_length = sentence.preds.length;
        final int max_arg_length = sentence.max_arg_length;
        final ArrayList<Integer> proposition = RoleDict.rolearray;
        final int prop_length = proposition.size();

        final int[][] graph = new int[prds_length][max_arg_length];

        for (int i=0; i<restart; ++i) {
            for (int prd_i=0; prd_i<prds_length; ++prd_i) {
                final ArrayList<Integer> arguments = tokens.get(preds[prd_i]).arguments;
                final int arg_length = arguments.size();
                double best_score = -1.0d;

                if (arg_length == 0) {
                    graph[prd_i] = new int[]{-1,-1};
                }
                else if (arg_length == 1) {
                    for (int role_i=1; role_i<prop_length; ++role_i) {
                        final int role1 = proposition.get(role_i);
                        final int[] tmp_graph = new int[]{role1, -1};
                        final double score = getPASScore(sentence, tmp_graph, prd_i);                            

                        if (score > best_score) {
                            best_score = score;
                            graph[prd_i] = tmp_graph;
                        }
                    }
                }
                else {
                    for (int role_i=1; role_i<prop_length; ++role_i) {
                        final int role1 = proposition.get(role_i);                        

                        for (int role_j=1; role_j<prop_length; ++role_j) {
//                            if (role_i == 0 && role_j > 0) break;
                            if (role_i == role_j) continue;
                            
                            final int role2 = proposition.get(role_j);
                            final int[] tmp_graph = new int[]{role1, role2};

                            final double score = getPASScore(sentence, tmp_graph, prd_i);                            

                            if (score > best_score) {
                                best_score = score;
                                graph[prd_i] = tmp_graph;
                            }
                        }
                    }
                }
            }
        }
        
        return graph;
    }

    final public int[][] decodePerPAS2(final Sentence sentence) {
        final ArrayList<Token> tokens = sentence.tokens;
        final int[] preds = sentence.preds;
        final int prds_length = sentence.preds.length;
        final int max_arg_length = sentence.max_arg_length;
        final ArrayList<Integer> proposition = RoleDict.rolearray;
        final int prop_length = proposition.size();

        final int[][] graph = new int[prds_length][max_arg_length];

        for (int i=0; i<restart; ++i) {
            for (int prd_i=0; prd_i<prds_length; ++prd_i) {
                final ArrayList<Integer> arguments = tokens.get(preds[prd_i]).arguments;
                final int arg_length = arguments.size();
                double best_score = -1.0d;
                Matrix best_h = new Matrix(1,1);
                Matrix best_feature = new Matrix(1,1);

                if (arg_length == 0) {
                    graph[prd_i] = new int[]{-1,-1};
                    final Matrix feature = new Matrix(lookupFeature(sentence, graph[prd_i], prd_i), weight_length*3);
                    best_feature = feature;
                    best_h = copyMatrix(classifier.h);
                    best_score = classifier.forward(feature);
                }
                else if (arg_length == 1) {
                    for (int role_i=1; role_i<prop_length; ++role_i) {
                        final int role1 = proposition.get(role_i);
                        final int[] tmp_graph = new int[]{role1, -1};

                        final Matrix feature = new Matrix(lookupFeature(sentence, tmp_graph, prd_i), weight_length*3);                        
                        final double score = classifier.forward(feature);
//                        final double score = getPASScore(sentence, tmp_graph, prd_i);                            

                        if (score > best_score) {
                            best_score = score;
                            best_feature = feature;
                            best_h = copyMatrix(classifier.h);
                            graph[prd_i] = tmp_graph;
                        }
                    }
                }
                else {
                    for (int role_i=1; role_i<prop_length; ++role_i) {
                        final int role1 = proposition.get(role_i);                        

                        for (int role_j=1; role_j<prop_length; ++role_j) {
//                            if (role_i == 0 && role_j > 0) break;
                            if (role_i == role_j) continue;
                            
                            final int role2 = proposition.get(role_j);
                            final int[] tmp_graph = new int[]{role1, role2};

                            final Matrix feature = new Matrix(lookupFeature(sentence, tmp_graph, prd_i), weight_length*3);
                            final double score = classifier.forward(feature);
//                            final double score = getPASScore(sentence, tmp_graph, prd_i);                            

                            if (score > best_score) {
                                best_score = score;
                                best_feature = feature;
                                best_h = copyMatrix(classifier.h);
                                graph[prd_i] = tmp_graph;
                            }
                        }
                    }
                }
//                updateWeights(sentence, sentence.o_graph[prd_i], graph[prd_i], prd_i);
                updateWeights(sentence.o_graph[prd_i], graph[prd_i], best_score, best_h, best_feature);
            }
        }
        
        return graph;
    }

    final private ArrayList<Integer>[] setPropositions(final Sentence sentence) {
        final ArrayList<Integer>[] preds = new ArrayList[sentence.preds.length];
        
        for (int i=0; i<sentence.preds.length; ++i) {
            preds[i] = RoleDict.rolearray;
        }
        
        return preds;
    }

    final private double getPASScore(final Sentence sentence, final int[] graph, final int prd_i) {
        final Matrix x = new Matrix(lookupFeature(sentence, graph, prd_i), weight_length*3);
        return classifier.forward(x);
    }

    final private double[] lookupFeature(final Sentence sentence, final int[] graph, final int prd_i) {
        return feature_extracter.lookupFeature(sentence, graph, prd_i);
    }
    
    final public int checkLabel(final int[] o_graph, final int[] graph) {
        for (int j=0; j<o_graph.length; ++j)
            if (o_graph[j] != graph[j])
                return 0;
        return 1;
    }
        
    final public boolean checkArguments(final Sentence sentence) {
        for (int j=0; j<sentence.preds.length; ++j) {        
            final Token pred = sentence.tokens.get(sentence.preds[j]);
            if (!pred.arguments.isEmpty()) return false;
        }
        return true;
    }
    
    final public void checkAccuracy(final int[][] o_graph, final int[][] graph) {
        for (int i=0; i<o_graph.length; ++i) {
            final int[] tmp_graph1 = o_graph[i];
            final int[] tmp_graph2 = graph[i];
            
            for (int j=0; j<tmp_graph1.length; ++j) {
                final int role1 = tmp_graph1[j];
                final int role2 = tmp_graph2[j];
                if (role2 < 0) break;
                
                if (role1 < 1) continue;
                if (role1 == role2) correct += 1.0f;
                total += 1.0f;
            }
        }
    }
    
    final private void updateWeights(final int[] o_graph, final int[] graph, final double prob, final Matrix h, final Matrix x) {
        final int label = checkLabel(o_graph, graph);        
        classifier.backpropagation(label, prob, h, x);
    }
    
    final private Matrix copyMatrix(final Matrix x) {
        final double[][] a = new double[x.getRowDimension()][x.getColumnDimension()];
        for (int i=0; i<a.length; ++i) {
            for (int j=0; j<a[0].length; ++j)
                a[i][j] = x.get(i, j);
        }
        return new Matrix(a);
    }
    
    
}
