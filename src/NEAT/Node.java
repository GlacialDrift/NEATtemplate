package NEAT;

import java.util.ArrayList;

public class Node {
    private double inputs;
    private double outputs;
    private int layer;
    private final int nodeID;
    private ArrayList<Gene> outputGenes;
    
    /**
     * Constructor function for a new node
     * @param l is the layer of the node
     * @param n is the nodeID
     */
    public Node(int l, int n) {
        nodeID = n;
        layer = l;
        outputGenes = new ArrayList<Gene>();
        inputs = 0D;
        outputs = 0D;
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
}
