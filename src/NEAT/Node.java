/*
 * Copyright (c) 2021.  Michael Harris
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * 'rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


/*
 * This class represents the nodes of a neural network. They store values for the inputs and outputs. Outputs are
 * calculated as a sigmoid of the inputs so as to provide a value between 0 and 1. This is not true for input nodes
 * to the network, which take their raw values. Each node is given a unique ID (no two nodes in the
 * network should have the same geneID), they are given a layer to describe where they lie within the network, and
 * they are given a list of genes to which they should provide an output.
 */

package NEAT;

import java.util.ArrayList;

public class Node {
    private int nodeID;
    private double inputs;
    private double outputs;
    private int layer;
    private ArrayList<Gene> outputGenes;
    
    /**
     * Constructor function for a new node
     * @param l is the layer of the node
     * @param n is the unique nodeID
     */
    public Node(int l, int n) {
        nodeID = n;
        layer = l;
        outputGenes = new ArrayList<>();
        inputs = 0D;
        outputs = 0D;
    }
    
    /**
     * Add a provided value to the input total
     * @param i input that comes from a gene
     */
    public void addInput(double i) {
        inputs += i;
    }
    
    /**
     * compute the sigmoid of the outputs and feed it forward through the network if each gene is activated
     */
    public void computeOut() {
        if (layer == 0) {
            outputs = inputs;
        } else {
            outputs = 1.0 / (1.0 + Math.exp(-5 * inputs));
        }
        for (Gene g : outputGenes) {
            if (g.isEnabled()) {
                g.getToNode().addInput(outputs * g.getWeight());
            }
        }
        reset();
    }
    
    /**
     * reset the inputs and outputs of the node so that it is ready for the next set of inputs
     */
    public void reset() {
        inputs = 0D;
        outputs = 0D;
    }
    
    /**
     * Add a provided gene to this node's list of output genes
     * @param g the gene that represents a connection to a new node
     */
    public void addGeneConnection(Gene g) {
        outputGenes.add(g);
    }
    
    public Node copy() {
        Node n = new Node(layer, nodeID);
        n.setOutputGenes(outputGenes);
        return n;
    }
    
    /**
     * Determine whether this node is connected to or in the same layer as a provided node
     * @param n the provided node to check against
     * @return a boolean value
     */
    public boolean isConnected(Node n) {
        if (n.getLayer() == layer) {
            return true;
        }
        for (Gene g : outputGenes) {
            if (g.getToNode().equals(n)) {
                return true;
            }
        }
        for (Gene g : n.getOutputGenes()) {
            if (g.getToNode().equals(this)) {
                return true;
            }
        }
        return false;
    }
    
    public int getLayer() {
        return layer;
    }
    
    public void setLayer(int l) {
        layer = l;
    }
    
    public int getID() {
        return nodeID;
    }
    
    public ArrayList<Gene> getOutputGenes() {
        return outputGenes;
    }
    
    public void setOutputGenes(ArrayList<Gene> og) {
        outputGenes = og;
    }
    
    public double getOutputs() {
        return outputs;
    }
    
    public double getInputs() {
        return inputs;
    }
    
    public void setNodeID(int i) { nodeID = i;}
}
