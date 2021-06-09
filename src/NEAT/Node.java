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
    private int replacedGeneID;
    
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
        replacedGeneID = -1;
    }
    
    /**
     * Constructor with a replacedGeneID specified, useful for creating nodes that are not unique in the population
     * @param l        the layer of the new node
     * @param n        the nodeID of the new node
     * @param replaced the geneID of the interrupted/replaced gene
     */
    public Node(int l, int n, int replaced) {
        nodeID = n;
        layer = l;
        replacedGeneID = replaced;
        outputGenes = new ArrayList<>();
        inputs = 0d;
        outputs = 0d;
    }
    
    /**
     * compute the sigmoid of the outputs and feed it forward through the network if each gene is activated
     */
    public void computeOut() {
        if (layer == 0) {
            outputs = inputs;
        } else {
            outputs = sigmoid(inputs);
        }
        for (Gene g : outputGenes) {
            if (g.isEnabled()) {
                g.getToNode().addInput(outputs * g.getWeight());
            }
        }
    }
    
    /**
     * compute a sigmoid function. Has its own method so that it can easily be changed
     * @param ins the inputs
     * @return the sigmoid-ed value
     */
    private double sigmoid(double ins) {
        return 1.0d / (1.0d + Math.exp(-5 * ins));
    }
    
    /**
     * Add a provided value to the input total
     * @param i input that comes from a gene
     */
    public void addInput(double i) {
        inputs += i;
    }
    
    /**
     * clear the output Genes list as a method for convenience
     */
    public void clearOutputGenes() {
        outputGenes.clear();
    }
    
    /**
     * Add a provided gene to this node's list of output genes
     * @param g the gene that represents a connection to a new node
     */
    public void addGeneConnection(Gene g) {
        outputGenes.add(g);
    }
    
    /**
     * @return a copy of this node, but the actual inputs and outputs don't really matter
     */
    public Node copy() {
        Node n = new Node(layer, nodeID, replacedGeneID);
        n.setOutputGenes(outputGenes);
        n.clearValues();
        return n;
    }
    
    /**
     * reset the inputs and outputs of the node so that it is ready for the next set of inputs
     */
    public void clearValues() {
        inputs = 0D;
        outputs = 0D;
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
        for (Gene g : n.getOutputGenes()) {
            if (g.getToNode().getID() == nodeID) return true;
        }
        for (Gene g : outputGenes) {
            if (g.getToNode().getID() == n.getID()) return true;
        }
        return false;
    }
    
    public int getLayer() {
        return layer;
    }
    
    public void setLayer(int l) {
        layer = l;
    }
    
    public ArrayList<Gene> getOutputGenes() {
        return outputGenes;
    }
    
    public void setOutputGenes(ArrayList<Gene> og) {
        outputGenes = og;
    }
    
    public int getID() {
        return nodeID;
    }
    
    @Override
    public int hashCode() {
        int result;
        long temp;
        result = nodeID;
        temp = Double.doubleToLongBits(getInputs());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getOutputs());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + getLayer();
        result = 31 * result + (getOutputGenes() != null ? getOutputGenes().hashCode() : 0);
        result = 31 * result + getReplacedGeneID();
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Node node = (Node) o;
        
        if (nodeID != node.nodeID) return false;
        if (Double.compare(node.getInputs(), getInputs()) != 0) return false;
        if (Double.compare(node.getOutputs(), getOutputs()) != 0) return false;
        if (getLayer() != node.getLayer()) return false;
        if (getReplacedGeneID() != node.getReplacedGeneID()) return false;
        return getOutputGenes() != null ? getOutputGenes().equals(node.getOutputGenes()) : node.getOutputGenes() == null;
    }
    
    public double getInputs() {
        return inputs;
    }
    
    public double getOutputs() {
        return outputs;
    }
    
    public int getReplacedGeneID() {
        return replacedGeneID;
    }
    
    public void setReplacedGeneID(int replacedGeneID) {
        this.replacedGeneID = replacedGeneID;
    }
    
    public void setNodeID(int i) { nodeID = i;}
}
