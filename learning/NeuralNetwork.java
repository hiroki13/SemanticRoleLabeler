/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import Jama.Matrix;
import io.RoleDict;
import static java.lang.Math.exp;
import java.util.Random;

/**
 *
 * @author hiroki
 */
public class NeuralNetwork extends Classifier{
    final Matrix w_ji;
    final Matrix w_kj;
//    double y;
    final Random rnd = new Random(0);
    final double alpha = 0.075d;
    
    public NeuralNetwork(final int weight_length) {
//        w_ji = initialize(Matrix.random(weight_length*5, weight_length*3));
//        w_kj = initialize(Matrix.random(1, weight_length*5));
        w_ji = initialize(weight_length*5, weight_length*(2*RoleDict.size()+1));
        w_kj = initialize(1, weight_length*5);
    }
    
    @Override
    public double forward(final Matrix x) {
        final Matrix in_j = w_ji.times(x);
        h = relu(in_j);
        final Matrix in_k = w_kj.times(h);//1 * 1
        double y = sigmoid(in_k).get(0, 0);
        return y;
    }
    
    @Override
    public void backpropagation(final int o_tag, final double prob, final Matrix h, final Matrix x) {
        final Matrix delta_y = delta_y(o_tag, prob);
        final Matrix derivative_kj = derivative_kj(delta_y, h);
        final Matrix derivative_ji = derivative_ji(delta_y, h, x);
        update(derivative_kj, derivative_ji);
    }
    
    public Matrix delta_y (final int o_tag, final double prob) {
        final Matrix error = new Matrix(1, 1);
        error.set(0, 0, prob-o_tag);
        return error;
    }
    
    public Matrix derivative_kj(final Matrix delta_y, final Matrix h) {
        // error = 1*1, h = j*1,
        return delta_y.times(h.transpose());
    }
    
    public Matrix derivative_ji(final Matrix delta_y, final Matrix h, final Matrix x) {
        // w_ij = j * i
        final Matrix derivative = new Matrix(w_ji.getRowDimension(), w_ji.getColumnDimension());
        // w_jk = 45 * dim of j, error = 45 * 1, error_j = dim of j * 1
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
    
    public void update(final Matrix derivative_kj, final Matrix derivative_ji) {
        w_kj.minusEquals(derivative_kj.timesEquals(alpha));
        w_ji.minusEquals(derivative_ji.timesEquals(alpha));
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
    
    public Matrix sigmoid(final Matrix x) {
        for (int j=0; j<x.getRowDimension(); ++j) {
            for (int i=0; i<x.getColumnDimension(); ++i) {            
                double score = 1.0 / (1.0 + exp(-(x.get(j, i))));
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
    
    final public Matrix initialize(final Matrix x) {
        for (int i=0; i<x.getRowDimension(); ++i) {
            for (int j=0; j<x.getColumnDimension(); ++j)
                x.set(i, j, x.get(i, j)-0.5);
        }
        return x;
    }        
    
    final public Matrix initialize(final int d1, final int d2) {
        final double[][] matrix = new double[d1][d2];
        for (int i=0; i<d1; ++i) {
            for (int j=0; j<d2; ++j)
                matrix[i][j] = rnd.nextDouble() - 0.5;
        }
        return new Matrix(matrix);
    }        
}
