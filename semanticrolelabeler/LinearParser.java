/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import Jama.Matrix;
import feature.FeatureExtractor;
import io.LookupTable;
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
public class LinearParser extends Parser{
    
    final ArrayList<Integer> proposition;
    final int prop_length;
    int arg_length;
    
    public LinearParser(final Classifier c, final int weight_length, final int restart, final int prune) {
        this.classifier = c;
        this.weight_length = weight_length;
        this.feature_extracter = new FeatureExtractor(weight_length);
        this.rnd = new Random();
        this.restart = restart;
        this.prune = prune;
        this.proposition = RoleDict.rolearray;
        this.prop_length = proposition.size();
    }
    
    @Override
    final public void train(final ArrayList<Sentence> sentencelist) {
        correct = 0.0f;
        total = 0.0f;

        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);
                        
            if (sentence.preds.length == 0) continue;
            if (checkArguments(sentence)) continue;

            train(sentence);
            if (i%100 == 0 && i != 0) System.out.print(String.format("%d ", i));            
            if (i==prune) break;            
        }
        
        System.out.println("\n\tCorrect: " + correct);                        
        System.out.println("\tTotal: " + total);                        
        System.out.println("\tAccuracy: " + correct/total);
    }

    final private void train(final Sentence sentence) {
        for (int prd_i=0; prd_i<sentence.preds.length; ++prd_i) {
//            if (prd_i > 0) break;
            
            final Graph graph = decode(sentence, prd_i);
            checkAccuracy(sentence.o_graph[prd_i], graph.graph);
            
            final Graph o_g = setOGraph(sentence, prd_i);
            update(sentence, prd_i, o_g, graph);
        }
    }

    
    final private Graph setOGraph(final Sentence sentence, final int prd_i) {
        final Graph g = new Graph();
        g.graph = copyGraph(sentence.o_graph[prd_i]);
        g.feature = new Matrix(lookupFeature(sentence, g.graph, prd_i), weight_length*(2*RoleDict.size()+1));
//        g.feature = new Matrix(lookupFeature(sentence, g.graph, prd_i), weight_length*(RoleDict.size()+1));
        g.score = classifier.forward(g.feature);
//        g.h = copyMatrix(classifier.h);
        return g;
    }

    final private void update(final Sentence sentence, final int prd_i, final Graph o_g, final Graph g) {
        classifier.update(sentence, prd_i, o_g, g);        
    }
    
    final private void update2(final Sentence sentence, final int prd_i, final Graph o_g, final Graph g) {
        final double delta[] = new double[RoleDict.size()+1];        
        if (match(o_g.graph, g.graph)) delta[0] = g.scores[0] - 1.0d;        
        else delta[0] = g.scores[0];

        if (o_g.graph[0] == g.graph[0]) delta[1] = g.scores[1] - 1.0d;        
        else delta[1] = g.scores[1];
        
        if (o_g.graph[1] == g.graph[1]) delta[2] = g.scores[2] - 1.0d;        
        else delta[2] = g.scores[2];
        
        classifier.update(sentence, prd_i, delta, g);        
    }

    @Override
    final public void test(final ArrayList<Sentence> testsentencelist) {
        time = (long) 0.0;

        for (int i=0; i<testsentencelist.size(); ++i) {
            final Sentence sentence = testsentencelist.get(i);
                        
            if (sentence.preds.length == 0) continue;
            if (checkArguments(sentence)) continue;

            sentence.p_graph = new int[sentence.preds.length][];
            
            for (int prd_i=0; prd_i<sentence.preds.length; ++prd_i) {
                long time1 = System.currentTimeMillis();
                final Graph g = decode(sentence, prd_i);
                sentence.p_graph[prd_i] = copyGraph(g.graph);
                long time2 = System.currentTimeMillis();

                time += time2 - time1;
            }
            
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

            final int[][] o_graph = evalsentence.o_graph;
            final int[][] p_graph = testsentence.p_graph;

            for (int j=0; j<p_graph.length; ++j) {
                final int[] p_args = p_graph[j];
                final int[] o_args = o_graph[j];
                
                for (int l=0; l<o_args.length; ++l) {
                    final int o_arg = o_args[l];
                    final int p_arg = p_args[l];
                    if (o_arg > -1) r_total += 1;
                    if (p_arg > -1) p_total += 1;
                    if (o_arg == p_arg && o_arg > -1) correct += 1;
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

    
    final public Graph decode(final Sentence sentence, final int prd_i) {
        arg_length = sentence.tokens.get(sentence.preds[prd_i]).arguments.size();
        Graph best_graph = new Graph();
//        best_graph.score = -1.0d;
        best_graph.score = -100000000.0d;

        for (int i=0; i<restart; ++i) {
            Graph graph = new Graph();
            graph.graph = new int[prop_length];
            
            while(true) {
                int[] prev_graph = copyGraph(graph.graph);
                graph = getBestNeighborGraph(sentence, prev_graph, prd_i);
//                graph = getBestNeighborGraph2(sentence, prev_graph, prd_i);
//                graph = getBestAllNeighborGraph(sentence, prd_i);
                if (match(prev_graph, graph.graph)) break;
            }
            if (graph.score > best_graph.score) {
                best_graph.graph = copyGraph(graph.graph);
                best_graph.score = graph.score;
//                best_graph.h = copyMatrix(graph.h);
                best_graph.feature = graph.feature.copy();
            }
        }
        
        return best_graph;
    }

/*
    final public Graph decode(final Sentence sentence, final int prd_i) {
        arg_length = sentence.tokens.get(sentence.preds[prd_i]).arguments.size();
        Graph graph = new Graph();        
        graph.graph = new int[prop_length];
            
        while(true) {
            int[] prev_graph = copyGraph(graph.graph);            
            graph = getBestNeighborGraph2(sentence, prev_graph, prd_i);            
            if (match(prev_graph, graph.graph)) break;            
        }
        
        return graph;
    }
*/    
    
    final public Graph decode2(final Sentence sentence, final int prd_i) {
        arg_length = sentence.tokens.get(sentence.preds[prd_i]).arguments.size();
        Graph graph = new Graph();        
        graph.graph = new int[prop_length];

        for (int role=0; role<prop_length; ++role) {
            int best_arg = -1;
            double best_score = -10000.0d;
            Matrix best_feature = null;
            Matrix best_h = null;
            
            for (int arg_i=-1; arg_i<arg_length; ++arg_i) {
                final Matrix feature = new Matrix(lookupFeature(sentence, prd_i, arg_i, role), weight_length*(2+1));                
                final double score = classifier.forward(feature);
                
                if (score > best_score) {
                    best_arg = arg_i;
                    best_score = score;
//                    best_feature = copyMatrix(feature);
                    best_feature = feature.copy();
//                    best_h = copyMatrix(classifier.h);
                    best_h = classifier.h.copy();
                }
            }
            
            graph.graph[role] = best_arg;
            graph.scores[role] = best_score;
            graph.features[role] = best_feature;
            graph.hs[role] = best_h;
        }
        
        return graph;
    }
    
    final private boolean match(final int[] graph1, final int[] graph2) {
        for (int i=0; i<graph1.length; ++i) if (graph1[i] != graph2[i]) return false;
        return true;
    }

    final private Graph getBestNeighborGraph(final Sentence sentence, final int[] graph, final int prd_i) {
        Graph g = new Graph();
//        g.score = -1.0d;
        g.score = -100000000.0d;
        
        for (int arg_i=-1; arg_i<arg_length; ++arg_i) {            
            for (int role_i=0; role_i<prop_length; ++role_i) {
                final int role = proposition.get(role_i);
                final int[] tmp_graph = changeGraph(graph, arg_i, role);                
                final Matrix feature = new Matrix(lookupFeature(sentence, tmp_graph, prd_i), weight_length*(2*RoleDict.size()+1));
//                final Matrix feature = new Matrix(lookupFeature(sentence, tmp_graph, prd_i), weight_length*(RoleDict.size()+1));                
                final double score = classifier.forward(feature);

                if (score > g.score) {                
                    g.score = score;                                            
                    g.feature = feature.copy();
//                    g.h = copyMatrix(classifier.h);                                            
                    g.graph = copyGraph(tmp_graph);
                }                                    
            }                            
        }
        return g;
    }

    final private Graph getBestNeighborGraph2(final Sentence sentence, final int[] graph, final int prd_i) {
        Graph g = new Graph(arg_length);
        g.score = -100000000.0d;
        final Token prd = sentence.tokens.get(sentence.preds[prd_i]);
        final double[] phi_prd = LookupTable.get(prd.form);
        
        for (int arg_i=-1; arg_i<arg_length; ++arg_i) {            
            for (int role_i=0; role_i<prop_length; ++role_i) {
                final int role = proposition.get(role_i);
                final int[] tmp_graph = changeGraph(graph, arg_i, role);                

                final double[] phi_a0 = lookupFeature(sentence, prd_i, tmp_graph[0], 0);
                final double[] phi_a1 = lookupFeature(sentence, prd_i, tmp_graph[1], 1);
                
                final double[] feature_all = new double[phi_prd.length+phi_a0.length+phi_a1.length];
                final double[] feature_a0 = new double[phi_prd.length+phi_a0.length];
                final double[] feature_a1 = new double[phi_prd.length+phi_a1.length];
                
                System.arraycopy(phi_prd, 0, feature_all, 0, phi_prd.length);
                System.arraycopy(phi_a0, 0, feature_all, phi_prd.length, phi_a0.length);
                System.arraycopy(phi_a1, 0, feature_all, phi_prd.length+phi_a0.length, phi_a1.length);

                System.arraycopy(feature_all, 0, feature_a0, 0, phi_prd.length+phi_a0.length);

                System.arraycopy(feature_all, 0, feature_a1, 0, phi_prd.length);
                System.arraycopy(feature_all, feature_a0.length, feature_a1, phi_prd.length, phi_a1.length);
                
                final Matrix x1 = new Matrix(feature_all, feature_all.length);
                final Matrix x2 = new Matrix(feature_a0, feature_a0.length);
                final Matrix x3 = new Matrix(feature_a1, feature_a1.length);
                
                final double scores[] = classifier.forward(x1, x2, x3);
                final double score = scores[0] + scores[1] + scores[2];
//                final double score = scores[1] + scores[2];

                if (score > g.score) {                
                    g.score = score;
                    g.scores[0] = scores[0];
                    g.scores[1] = scores[1];
                    g.scores[2] = scores[2];

                    g.features[0] = x1.copy();
                    g.features[1] = x2.copy();
                    g.features[2] = x3.copy();
                    g.graph = copyGraph(tmp_graph);
                }                                    
            }                            
        }
        
        return g;
    }

    final private Graph getBestAllNeighborGraph(final Sentence sentence, final int prd_i) {
        Graph g = new Graph();
        g.score = -1000000000.0d;
        
        for (int arg_i=-1; arg_i<arg_length; ++arg_i) {            
        for (int arg_j=-1; arg_j<arg_length; ++arg_j) {            
                final int[] tmp_graph = new int[]{arg_i, arg_j};
                final Matrix feature = new Matrix(lookupFeature(sentence, tmp_graph, prd_i), weight_length*(2*RoleDict.size()+1));                
//                final Matrix feature = new Matrix(lookupFeature(sentence, tmp_graph, prd_i), weight_length*(RoleDict.size()+1));                
                final double score = classifier.forward(feature);
                
                if (score > g.score) {                
                    g.score = score;                                            
//                    g.feature = copyMatrix(feature);                                            
                    g.feature = feature.copy();                                            
//                    g.h = copyMatrix(classifier.h);                                            
                    g.graph = copyGraph(tmp_graph);                    
                }                                    
            }
        }
        return g;
    }
    
    final private int[] copyGraph(final int[] graph) {
        final int[] copied_graph = new int[graph.length];
        System.arraycopy(graph, 0, copied_graph, 0, graph.length);
        return copied_graph;        
    }
    
    final private int[] changeGraph(final int[] graph, final int arg_i, final int role) {
        final int[] copied_graph = copyGraph(graph);        
        copied_graph[role] = arg_i;        
        return copied_graph;
    }
    
    final private double[] lookupFeature(final Sentence sentence, final int[] graph, final int prd_i) {
        return feature_extracter.lookupFeature(sentence, graph, prd_i);
    }
    
    final private double[] lookupFeature(final Sentence sentence, final int prd_i, final int arg_i, final int role) {
        return feature_extracter.lookupFeature(sentence, prd_i, arg_i, role);
    }
    
    final public int checkLabel(final int[] o_graph, final int[] graph) {
        for (int j=0; j<o_graph.length; ++j)
            if (o_graph[j] != graph[j]) return 0;
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
    
    final public void checkAccuracy(final int[] o_graph, final int[] graph) {
        for (int i=0; i<o_graph.length; ++i) {
            if (o_graph[i] == graph[i]) correct += 1.0f;            
            total += 1.0f;
        }
    }
    
    final private void updateWeights(final int[] o_graph, final int[] graph, final double prob, final Matrix h, final Matrix x) {
        final int label = checkLabel(o_graph, graph);        
        classifier.backpropagation((double) label, prob, h, x);
    }
    
    final private Matrix copyMatrix(final Matrix x) {
        final double[][] a = new double[x.getRowDimension()][x.getColumnDimension()];
        for (int i=0; i<a.length; ++i)
            for (int j=0; j<a[0].length; ++j) a[i][j] = x.get(i, j);
        return new Matrix(a);
    }    
    
}
