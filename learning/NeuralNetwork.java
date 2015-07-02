/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import Jama.Matrix;
import io.LookupTable;
import io.PathLookupTable;
import io.RoleDict;
import io.Sentence;
import io.Token;
import static java.lang.Math.exp;
import java.util.ArrayList;
import java.util.Random;
import semanticrolelabeler.Graph;

/**
 *
 * @author hiroki
 */
public class NeuralNetwork extends Classifier{
    final Matrix w_ji, w_kj, b_j;
    final Random rnd = new Random(0);
    final int weight_length;
    
    public NeuralNetwork(final int weight_length) {
        this.weight_length = weight_length;
        w_ji = initialize(weight_length*3, weight_length*(2*RoleDict.size()+1));
        w_kj = initialize(1, weight_length*3);
        b_j = initialize(weight_length*3, 1);
    }
    
    public NeuralNetwork(final int weight_length, final int h_layer) {
        this.weight_length = weight_length;
//        w_ji = initialize(weight_length*h_layer, weight_length*(2*RoleDict.size()+1));
        w_ji = initialize(weight_length*h_layer, weight_length*(4*RoleDict.size()+4));
//        w_ji = initialize(weight_length*h_layer, weight_length*(RoleDict.size()+1));
        w_kj = initialize(1, weight_length*h_layer);
        b_j = initialize_b(weight_length*h_layer, 1);
    }
/*    
    @Override
    public double forward(final Matrix x) {
        final Matrix in_j = w_ji.times(x);
        h = relu(in_j);
//        h = sigmoid(in_j);
        final Matrix in_k = w_kj.times(h);//1 * 1
//        double y = sigmoid(in_k).get(0, 0);
        double y = in_k.get(0, 0);
//        double y = Math.tanh(in_k.get(0, 0));
        return y;
    }
*/
    @Override
    public double forward(final Matrix x) {
        final Matrix in_j = w_ji.times(x);
        in_j.plusEquals(b_j);
        h = relu(in_j);
//        h = sigmoid(in_j);
        final Matrix in_k = w_kj.times(h);//1 * 1
//        double y = sigmoid(in_k).get(0, 0);
        double y = in_k.get(0, 0);
//        double y = Math.tanh(in_k.get(0, 0));
        return y;
    }

    @Override
    public void backpropagation(final double o_tag, final double prob, final Matrix h, final Matrix x) {
        final Matrix delta_y = delta_y(o_tag, prob);
        final Matrix derivative_kj = derivative_kj(delta_y, h);
        final Matrix derivative_ji = derivative_ji(delta_y, h, x);
        update(derivative_kj, derivative_ji);
    }
    
    @Override
    public Matrix delta_y (final double o_tag, final double prob) {
        final Matrix error = new Matrix(1, 1);
//        error.set(0, 0, o_tag * prob * (1-prob));
//        error.set(0, 0, o_tag * (1 - prob*prob));
        error.set(0, 0, o_tag);
        return error;
    }

    @Override
    public Matrix derivative_kj(final Matrix delta_y, final Matrix h) {
        // error = 1*1, h = j*1,
        return delta_y.times(h.transpose());
    }

    
    @Override
    public Matrix derivative_ji(final Matrix delta_y, final Matrix h, final Matrix x) {
        final Matrix derivative = new Matrix(w_ji.getRowDimension(), w_ji.getColumnDimension());
        final Matrix error_j = w_kj.transpose().times(delta_y);
        
        for (int j=0; j<error_j.getRowDimension(); ++j) {
            double h_derivative = 0.0d;
            if (h.get(j, 0) > 0) h_derivative = 1.0d;
            
            final double delta_h = error_j.get(j, 0) * h_derivative;

            for (int i=0; i<x.getRowDimension(); ++i)
                derivative.set(j, i, delta_h * x.get(i, 0));
        }
        
        return derivative;
    }

/*    
    @Override
    public Matrix derivative_ji(final Matrix delta_y, final Matrix h, final Matrix x) {
        final Matrix derivative = new Matrix(w_ji.getRowDimension(), w_ji.getColumnDimension());
        final Matrix error_j = w_kj.transpose().times(delta_y);
        
        for (int j=0; j<error_j.getRowDimension(); ++j) {
            double h_j = h.get(j, 0);
            double h_derivative = h_j * (1-h_j);
            
            final double delta_h = error_j.get(j, 0) * h_derivative;

            for (int i=0; i<x.getRowDimension(); ++i)
                derivative.set(j, i, delta_h * x.get(i, 0));
        }
        
        return derivative;
    }
*/
    
    @Override
    public Matrix derivative_x (final Matrix delta_y, final Matrix h) {
        final Matrix delta_h = w_kj.times(delta_y.get(0, 0));//1 * 200
//        final Matrix derivative_x = delta_x.times(w_ji);// (1*200) * (200*250)

        for (int j=0; j<delta_h.getColumnDimension(); ++j) {
            if (h.get(j, 0) <= 0) delta_h.set(0, j, 0.0d);            
        }
        
        final Matrix derivative_x = delta_h.times(w_ji);
        return derivative_x;
    }
    
    @Override
    public Matrix derivative_b (final Matrix delta_y, final Matrix h) {
        final Matrix delta_h = w_kj.times(delta_y.get(0, 0));//1 * 200

        for (int j=0; j<delta_h.getColumnDimension(); ++j) {
            if (h.get(j, 0) <= 0) delta_h.set(0, j, 0.0d);            
        }
        return delta_h;
    }
    
    
    @Override
    public void update(final Matrix derivative_kj, final Matrix derivative_ji) {
        w_kj.minusEquals(derivative_kj.timesEquals(alpha));
        w_ji.minusEquals(derivative_ji.timesEquals(alpha));
    }
    
    @Override
    public void update_b(final Matrix derivative_b) {
        b_j.minusEquals(derivative_b.timesEquals(alpha).transpose());
    }
    
    @Override
    public void updateVector(final Sentence sentence, final int prd_i, final Graph graph, final double[] derivative_x) {
        final Token prd = sentence.tokens.get(sentence.preds[prd_i]);
        final double[] phi_vec = graph.feature.getRowPackedCopy();
        
        final double[] vec_prd = updatedVector(phi_vec, 0, derivative_x);
        LookupTable.token_dict.put(prd.form, vec_prd);
                
        for (int role=0; role<graph.graph.length; ++role) {
            int arg_i = graph.graph[role];
            
            final int begin1 = weight_length*(2*role+1);
            final double[] vec_arg = updatedVector(phi_vec, begin1, derivative_x);            
            final int begin2 = weight_length*(2*role+2);
            final double[] vec_path = updatedVector(phi_vec, begin2, derivative_x);
            
            if (arg_i > -1) {
                final int arg_id = prd.arguments.get(arg_i);
                Token arg = sentence.tokens.get(arg_id);
                
                if (role == 0) LookupTable.token_dict_a0.put(arg.form, vec_arg);
                else LookupTable.token_dict_a1.put(arg.form, vec_arg);
//                LookupTable.token_dict.put(arg.form, vec_arg);
                
                PathLookupTable.path_dict.put(sentence.dep_path[prd_i][arg_id] + "_" + role, vec_path);
            }
            else {
                LookupTable.token_dict.put("*UNKNOWN*" + role, vec_arg);
                PathLookupTable.path_dict.put("NULL_" + role, vec_path);
            }            
        }
    }

    final private boolean match(final double[] graph1, final double[] graph2) {
        for (int i=0; i<graph1.length; ++i) if (graph1[i] != graph2[i]) return false;
        return true;
    }
    
    final private double[] updatedVector(final double[] phi_vec, final int begin, final double[] derivative) {
        double[] vec = new double[weight_length];
        System.arraycopy(phi_vec, begin, vec, 0, weight_length);
        for (int i=begin; i<begin+weight_length; ++i) vec[i-begin] -= derivative[i];
        return vec;
    }
    
    @Override
    public void updateVector(final Sentence sentence, final int[] graph, final int prd_i,
                               final Matrix h, final Matrix derivative_x, final Matrix x) {
        final ArrayList<Token> tokens = sentence.tokens;
        final Token prd = tokens.get(sentence.preds[prd_i]);

        final Matrix vec = x.minusEquals(derivative_x.transpose().times(alpha));
            
        double[] tmp_vec = new double[weight_length];
        int k = 0;
        int role = 0;

        for (int i=0; i<vec.getRowDimension(); ++i) {
            tmp_vec[i-k*weight_length] = vec.get(i, 0);
            
            if (i != 0 && i % weight_length == weight_length-1) {
                if (k == 0) 
                    LookupTable.token_dict.put(prd.form, tmp_vec);
                else if (k % 2 == 1) {
                    final int arg_i = graph[role];
                    if (arg_i > -1) {
                        if (role == 0) LookupTable.token_dict_a0.put(tokens.get(prd.arguments.get(arg_i)).form+role, tmp_vec);
                        else LookupTable.token_dict_a1.put(tokens.get(prd.arguments.get(arg_i)).form+role, tmp_vec);
                    }
                    else LookupTable.token_dict.put("*UNKNOWN*"+role, tmp_vec);
                }
                else {
                    final int arg_i = graph[role];
                    if (arg_i > -1) PathLookupTable.path_dict.put(sentence.dep_path[prd_i][prd.arguments.get(arg_i)] + role, tmp_vec);
                    else PathLookupTable.path_dict.put("NULL"+role, tmp_vec);                    
                    role += 1;
                }
                k += 1;
                tmp_vec = new double[weight_length];
            }
        }
    }
    
    final public double regularizer(final double[][] w) {
        double r = 0.0d;
        for (int k=0; k<w.length; ++k) {
            for (int j=0; j<w[0].length; ++j) {
                double tmp = w[k][j];
                if (tmp >= 0.0d) r += tmp;
                else r += tmp;
            }
        }
        return r;
    }
    
    public Matrix exponential(final Matrix x) {
        for (int j=0; j<x.getRowDimension(); ++j) {
            for (int i=0; i<x.getColumnDimension(); ++i) {
                double score = exp(x.get(j, i));
                if (Double.isInfinite(score)) {
                    System.out.println("Inf:" + x.get(j, i));
                    System.exit(0);
                }
                else if (Double.isNaN(score)) {
                    System.out.println("NaN:" + x.get(j, i));
                    System.exit(0);
                }
                x.set(j, i, score);
            }
        }        
        return x;
    }
    
    public Matrix relu(final Matrix x) {
        for (int j=0; j<x.getRowDimension(); ++j) {
            for (int i=0; i<x.getColumnDimension(); ++i) {            
                double score = x.get(j, i);
                if (score < 0) score = 0;
                x.set(j, i, score);
            }
        }   
        return x;
    }
    
    public double[] softmax(final Matrix in_k) {
        final Matrix x = exponential(in_k);
        final double[] y = new double[x.getRowDimension()];
        final double z = z(in_k);
        for (int i=0; i<y.length; ++i) y[i] = x.get(i, 0) / z;
        return y;
    }
    
    public float z(final Matrix x) {
        float z = 0.0f;
        for (int i=0; i<x.getRowDimension(); ++i) z += x.get(i, 0); 
        return z;
    }
    
    final public Matrix initialize(final int d1, final int d2) {
        final double[][] matrix = new double[d1][d2];
        for (int i=0; i<d1; ++i) {
            for (int j=0; j<d2; ++j)
//                matrix[i][j] = rnd.nextDouble() - 0.5;
                matrix[i][j] = (rnd.nextDouble() - 0.5) / 10;
        }
        return new Matrix(matrix);
    }
    
    final public Matrix initialize_b(final int d1, final int d2) {
        final double[][] matrix = new double[d1][d2];
        for (int i=0; i<d1; ++i) {
            for (int j=0; j<d2; ++j)
//                matrix[i][j] = rnd.nextDouble() - 0.5;
                matrix[i][j] = 0.1;
        }
        return new Matrix(matrix);
    }        
    
}
