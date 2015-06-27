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
public class LinearNetwork extends Classifier{

    final Matrix w_ji;
    Matrix w_a0, w_a1;
    final Random rnd = new Random(0);
    final double alpha = 0.075d;
    final int weight_length;
    
    public LinearNetwork(final int weight_length) {
        this.weight_length = weight_length;
        w_ji = initialize(1, weight_length*(2*RoleDict.size()+1));
//        w_a0 = initialize(1, weight_length*(2+1));
//        w_a1 = initialize(1, weight_length*(2+1));
    }
    
    @Override
    public double forward(final Matrix x) {
//        final Matrix d = sigmoid(w_ji.times(x));
        final Matrix d = w_ji.times(x);
        return d.get(0, 0);
    }

    @Override
    public double[] forward(final Matrix x, final Matrix a0, final Matrix a1) {
        final Matrix d1 = sigmoid(w_ji.times(x));
        final Matrix d2 = sigmoid(w_a0.times(a0));
        final Matrix d3 = sigmoid(w_a1.times(a1));
        return new double[]{d1.get(0, 0), d2.get(0, 0), d3.get(0, 0)};
    }

    @Override
    public void backpropagation(final double o_tag, final double prob, final Matrix h, final Matrix x) {
        final Matrix delta_y = delta_y(o_tag, prob);
        final Matrix derivative_kj = derivative_kj(delta_y, h);
        final Matrix derivative_ji = derivative_ji(delta_y, h, x);
        update(derivative_kj, derivative_ji);
    }
    
    
    @Override
    public void update(final Sentence sentence, final int prd_i, final double delta, final Graph graph) {
        final Matrix derivative_ji = graph.feature.times(delta).transpose();
        derivative_ji.timesEquals(alpha);

        final double[] derivative_x = derivative_x(delta).getRowPackedCopy();
        w_ji.minusEquals(derivative_ji);
        updateVector(sentence, prd_i, graph, derivative_x);
    }

    @Override
    public void update(final Sentence sentence, final int prd_i, final Graph o_graph, final Graph graph) {
        final Matrix o_derivative_ji = o_graph.feature.times(-alpha).transpose();
        final Matrix derivative_ji = graph.feature.times(alpha).transpose();
        final double[] o_derivative_x = w_ji.times(-alpha).getRowPackedCopy();
        final double[] derivative_x = w_ji.times(alpha).getRowPackedCopy();

        final Token prd = sentence.tokens.get(sentence.preds[prd_i]);
        double[] v = LookupTable.get(prd.form).clone();        
        
        w_ji.minusEquals(o_derivative_ji);
        w_ji.minusEquals(derivative_ji);
//        updateVector(sentence, prd_i, o_graph, o_derivative_x);
//        updateVector(sentence, prd_i, graph, derivative_x);        

        double[] u = LookupTable.get(prd.form);
        boolean b = match(v, u);
        String s = "mo";
        String r = s;
    }

    @Override
    public void update(final Sentence sentence, final int prd_i, final double[] delta, final Graph graph) {
        final Matrix derivative_ji = graph.features[0].times(delta[0]).transpose();
        derivative_ji.timesEquals(alpha);

        final Matrix derivative_a0 = graph.features[1].times(delta[1]).transpose();
        derivative_a0.timesEquals(alpha);

        final Matrix derivative_a1 = graph.features[2].times(delta[2]).transpose();
        derivative_a1.timesEquals(alpha);

//        final double[] derivative_x = derivative_x(delta).getRowPackedCopy();
        w_ji.minusEquals(derivative_ji);
        w_a0.minusEquals(derivative_a0);
        w_a1.minusEquals(derivative_a1);
//        updateVector(sentence, prd_i, graph, derivative_x);
    }
    
    public Matrix derivative_x(final double delta) {
        final Matrix derivative_x = w_ji.times(delta);
        derivative_x.timesEquals(alpha);
        return derivative_x;
    }
        
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
    
    
    final public Matrix initialize(final int d1, final int d2) {
        final double[][] matrix = new double[d1][d2];
        for (int i=0; i<d1; ++i) {
            for (int j=0; j<d2; ++j)
//                matrix[i][j] = rnd.nextDouble() - 0.5;
                matrix[i][j] = (rnd.nextDouble() - 0.5) / 10;
        }
        return new Matrix(matrix);
    }        
}
