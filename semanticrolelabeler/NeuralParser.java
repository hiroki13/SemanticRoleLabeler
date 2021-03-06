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
import java.util.Collections;
import java.util.Random;
import learning.Classifier;

/**
 *
 * @author hiroki
 */
public class NeuralParser extends Parser{
        
    final ArrayList<Integer> proposition;
    final int prop_length;
    int arg_length;
    
    public NeuralParser(final Classifier c, final int weight_length, final int restart, final int prune) {
        this.classifier = c;
        this.weight_length = weight_length;
        this.feature_extracter = new FeatureExtractor(weight_length);
        this.rnd = new Random(0);
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

/*    
    final private void train(final Sentence sentence) {
        for (int prd_i=0; prd_i<sentence.preds.length; ++prd_i) {
            
            final Graph graph = decode(sentence, prd_i);
            checkAccuracy(sentence.o_graph[prd_i], graph.graph);
            
            final Graph o_g = setOGraph(sentence, prd_i);
            update(sentence, prd_i, o_g, graph);
//            update(sentence, prd_i, o_g, o_g);
        }
    }
*/    
/*    
    final private void train(final Sentence sentence) {
        for (int prd_i=0; prd_i<sentence.preds.length; ++prd_i) {
//            if (prd_i > 0) break;
            
            final Graph graph = decode(sentence, prd_i);
            final Graph o_g = setOGraph(sentence, prd_i);

            checkAccuracy(sentence.o_graph[prd_i], graph.graph);            
            updateWeights(sentence.o_graph[prd_i], graph.graph, graph.score, graph.h, graph.feature);

            if (checkLabel(o_g.graph, graph.graph) == 0)
                updateWeights(o_g.graph, o_g.graph, o_g.score, o_g.h, o_g.feature);
        }
    }
*/
/*    
    final private void train(final Sentence sentence) {
        for (int prd_i=0; prd_i<sentence.preds.length; ++prd_i) {
            final Graph graph = decode(sentence, prd_i);
            checkAccuracy(sentence.o_graph[prd_i], graph.graph);
            
            final Graph o_g = new Graph();
            o_g.graph = copyGraph(sentence.o_graph[prd_i]);
            o_g.feature = new Matrix(lookupFeature(sentence, o_g.graph, prd_i), weight_length*(2*RoleDict.size()+1));
//            o_g.feature = new Matrix(lookupFeature(sentence, o_g.graph, prd_i), weight_length*(RoleDict.size()+1));
            o_g.score = classifier.forward(o_g.feature);
            o_g.h = copyMatrix(classifier.h);
            
//            updateWeights(sentence.o_graph[prd_i], graph.graph, graph.score, graph.h, graph.feature);
            
            if ((1 - o_g.score + graph.score) <= 0) continue;
            
            final Matrix o_delta_y = classifier.delta_y(-1.0, o_g.score);
//            final Matrix o_delta_y = new Matrix(1,1,-1.0d);
            final Matrix o_derivative_kj = classifier.derivative_kj(o_delta_y, o_g.h);
            final Matrix o_derivative_ji = classifier.derivative_ji(o_delta_y, o_g.h, o_g.feature);
            final Matrix o_derivative_x = classifier.derivative_x(o_delta_y, o_g.h);

            final Matrix delta_y = classifier.delta_y(1.0, graph.score);
//            final Matrix delta_y = new Matrix(1,1,1.0d);
            final Matrix derivative_kj = classifier.derivative_kj(delta_y, graph.h);
            final Matrix derivative_ji = classifier.derivative_ji(delta_y, graph.h, graph.feature);
            final Matrix derivative_x = classifier.derivative_x(delta_y, graph.h);
            
            classifier.update(derivative_kj, derivative_ji);
            classifier.update(o_derivative_kj, o_derivative_ji);

            classifier.updateVector(sentence, o_g.graph, prd_i, o_g.h, o_derivative_x, o_g.feature);
            Matrix o_phi = new Matrix(lookupFeature(sentence, graph.graph, prd_i), weight_length*(2*RoleDict.size()+1));
            classifier.updateVector(sentence, graph.graph, prd_i, graph.h, derivative_x, o_phi);
//            classifier.update(sentence, graph.graph, prd_i, graph.h, derivative_x, graph.feature);
            
//            classifier.backpropagation(-1.0, o_g.score, o_g.h, o_g.feature);
//            classifier.backpropagation(1.0, graph.score, graph.h, graph.feature);
//            classifier.backpropagation(o_g.score, graph.score, graph.h, graph.feature);

//            if (checkLabel(o_g.graph, graph.graph) == 0)
//                updateWeights(o_g.graph, o_g.graph, o_g.score, o_g.h, o_g.feature);
        }
    }    
*/    
    final private void train(final Sentence sentence) {
        for (int prd_i=0; prd_i<sentence.preds.length; ++prd_i) {

            final Graph o_g = setOGraph(sentence, prd_i);
            final Graph graph = decode(sentence, prd_i);
            checkAccuracy(sentence.o_graph[prd_i], graph.graph);
            
            if ((1 - o_g.score + graph.score) <= 0) continue;
            
            final Matrix o_delta_y = classifier.delta_y(-1.0, o_g.score);
            final Matrix o_derivative_kj = classifier.derivative_kj(o_delta_y, o_g.h);
            final Matrix o_derivative_ji = classifier.derivative_ji(o_delta_y, o_g.h, o_g.feature);
            final Matrix o_derivative_b = classifier.derivative_b(o_delta_y, o_g.h);
//            final Matrix o_derivative_x = classifier.derivative_x(o_delta_y, o_g.h);

            final Matrix delta_y = classifier.delta_y(1.0, graph.score);
            final Matrix derivative_kj = classifier.derivative_kj(delta_y, graph.h);
            final Matrix derivative_ji = classifier.derivative_ji(delta_y, graph.h, graph.feature);
            final Matrix derivative_b = classifier.derivative_b(delta_y, graph.h);
//            final Matrix derivative_x = classifier.derivative_x(delta_y, graph.h);
            
            classifier.update(derivative_kj, derivative_ji);
            classifier.update(o_derivative_kj, o_derivative_ji);
            classifier.update_b(o_derivative_b);
            classifier.update_b(derivative_b);
//            updateVector(sentence, o_g, graph, prd_i, o_derivative_x, derivative_x);
        }
    }
    
    final private void updateVector(final Sentence sentence, final Graph o_g, final Graph g, final int prd_i,
                                      final Matrix o_derivative, final Matrix derivative) {
//        classifier.updateVector(sentence, o_g.graph, prd_i, o_g.h, o_derivative, o_g.feature);
        final double[] o_d = o_derivative.getRowPackedCopy();
        classifier.updateVector(sentence, prd_i, o_g, o_d);
//        Matrix o_phi = new Matrix(lookupFeature(sentence, g.graph, prd_i), weight_length*(2*RoleDict.size()+1));        
        final double[] d = derivative.getRowPackedCopy();
        classifier.updateVector(sentence, prd_i, g, d);
    }
    
/*    
    final private void train(final Sentence sentence) {
        for (int prd_i=0; prd_i<sentence.preds.length; ++prd_i) {
//            if (prd_i > 0) break;
            
            final Graph graph = decode(sentence, prd_i);
            checkAccuracy(sentence.o_graph[prd_i], graph.graph);
            
            final Graph o_g = setOGraph(sentence, prd_i);
            
//            updateWeights(sentence.o_graph[prd_i], graph.graph, graph.score, graph.h, graph.feature);
            
//            if ((1 - o_g.score + graph.score) <= 0) continue;
            
//            final Matrix o_delta_y = classifier.delta_y(-1.0, o_g.score);
//            final Matrix o_delta_y = new Matrix(1,1,-1.0d);
//            final Matrix o_derivative_kj = classifier.derivative_kj(o_delta_y, o_g.h);
//            final Matrix o_derivative_ji = classifier.derivative_ji(o_delta_y, o_g.h, o_g.feature);
//            final Matrix o_derivative_x = classifier.derivative_x(o_delta_y, o_g.h);

//            final Matrix delta_y = classifier.delta_y(1.0, graph.score);
//            final Matrix delta_y = new Matrix(1,1,1.0d);
//            final Matrix derivative_kj = classifier.derivative_kj(delta_y, graph.h);
//            final Matrix derivative_ji = classifier.derivative_ji(delta_y, graph.h, graph.feature);
//            final Matrix derivative_x = classifier.derivative_x(delta_y, graph.h);
            
//            classifier.update(derivative_kj, derivative_ji);
//            classifier.update(o_derivative_kj, o_derivative_ji);

//            classifier.update(-1.0d, o_g.feature);
//            double d = graph.score * (1-graph.score);
            
//            classifier.update(sentence, o_g.graph, prd_i, o_g.h, o_derivative_x, o_g.feature);
//            Matrix o_phi = new Matrix(lookupFeature(sentence, graph.graph, prd_i), weight_length*(2*RoleDict.size()+1));
//            classifier.update(sentence, graph.graph, prd_i, graph.h, derivative_x, o_phi);
//            classifier.update(sentence, graph.graph, prd_i, graph.h, derivative_x, graph.feature);
            
//            classifier.backpropagation(-1.0, o_g.score, o_g.h, o_g.feature);
//            classifier.backpropagation(1.0, graph.score, graph.h, graph.feature);
//            classifier.backpropagation(o_g.score, graph.score, graph.h, graph.feature);

//            if (checkLabel(o_g.graph, graph.graph) == 0)
//                updateWeights(o_g.graph, o_g.graph, o_g.score, o_g.h, o_g.feature);
        }
    }
*/
    
    final private Graph setOGraph(final Sentence sentence, final int prd_i) {
        final Graph g = new Graph();
        g.graph = copyGraph(sentence.o_graph[prd_i]);
        g.feature = new Matrix(lookupFeature(sentence, g.graph, prd_i), weight_length*(4*RoleDict.size()+4));
//        g.feature = new Matrix(lookupFeature(sentence, g.graph, prd_i), weight_length*(2*RoleDict.size()+1));
//        g.feature = new Matrix(lookupFeature(sentence, g.graph, prd_i), weight_length*(RoleDict.size()+1));
        g.score = classifier.forward(g.feature);
        g.h = classifier.h.copy();
        return g;
    }

    final private Graph setNegativeGraph(final Sentence sentence, final int prd_i, final Graph o_g) {
        final Graph g = new Graph();
        g.graph = genNegativeGraph(o_g);
        g.feature = new Matrix(lookupFeature(sentence, g.graph, prd_i), weight_length*(2*RoleDict.size()+1));
        g.score = classifier.forward(g.feature);
        g.h = classifier.h.copy();
        return g;
    }
    
    final private int[] genNegativeGraph(final Graph o_g) {
        final int[] g = copyGraph(o_g.graph);
        final int role = rnd.nextInt(prop_length);
        int arg_i = rnd.nextInt(arg_length+1);
        if (arg_i == arg_length) arg_i = -1;
        g[role] = arg_i;
        return g;
    }

    final private void update(final Sentence sentence, final int prd_i, final Graph o_g, final Graph g) {
        classifier.update(sentence, prd_i, o_g, g);        
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
//                graph = getBestAllNeighborGraph(sentence, prd_i);
                if (match(prev_graph, graph.graph)) break;
            }
            if (graph.score > best_graph.score) {
                best_graph.graph = copyGraph(graph.graph);
                best_graph.score = graph.score;
                best_graph.h = graph.h.copy();
                best_graph.feature = graph.feature.copy();
            }
        }
        
        return best_graph;
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
                final Matrix feature = new Matrix(lookupFeature(sentence, tmp_graph, prd_i), weight_length*(4*RoleDict.size()+4));
//                final Matrix feature = new Matrix(lookupFeature(sentence, tmp_graph, prd_i), weight_length*(2*RoleDict.size()+1));
//                final Matrix feature = new Matrix(lookupFeature(sentence, tmp_graph, prd_i), weight_length*(RoleDict.size()+1));                
                final double score = classifier.forward(feature);

                if (score > g.score) {                
                    g.score = score;                                            
                    g.feature = feature.copy();
                    g.h = classifier.h.copy();                                            
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
                    g.feature = feature.copy();                                            
                    g.h = classifier.h.copy();                                            
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
