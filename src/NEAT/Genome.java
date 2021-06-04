package NEAT;

import java.util.ArrayList;

public class Genome {
    private final int inputs;
    private final int outputs;
    private int nextNodeID;
    private int biasNodeID;
    private int layers;
    private int nextGeneID;
    
    private ArrayList<Node> nodes;
    private ArrayList<Gene> genes;
    private ArrayList<Node> network;
    
    public void setNextNodeID(int nextNodeID) {
        this.nextNodeID = nextNodeID;
    }
    
    public void setBiasNodeID(int biasNodeID) {
        this.biasNodeID = biasNodeID;
    }
    
    public void setLayers(int layers) {
        this.layers = layers;
    }
    
    public void setNextGeneID(int nextGeneID) {
        this.nextGeneID = nextGeneID;
    }
    
    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }
    
    public void setGenes(ArrayList<Gene> genes) {
        this.genes = genes;
    }
    
    public void setNetwork(ArrayList<Node> network) {
        this.network = network;
    }
    
    public int getInputs() {
        return inputs;
    }
    
    public int getOutputs() {
        return outputs;
    }
    
    public int getNextNodeID() {
        return nextNodeID;
    }
    
    public int getBiasNodeID() {
        return biasNodeID;
    }
    
    public int getLayers() {
        return layers;
    }
    
    public int getNextGeneID() {
        return nextGeneID;
    }
    
    public ArrayList<Node> getNodes() {
        return nodes;
    }
    
    public ArrayList<Gene> getGenes() {
        return genes;
    }
    
    public ArrayList<Node> getNetwork() {
        return network;
    }
    
    public Genome(int in, int out) {
        inputs = in;
        outputs = out;
        layers = 2;
        nextNodeID = 0;
        nextGeneID = 0;
        nodes = new ArrayList<>();
        genes = new ArrayList<>();
        network = new ArrayList<>();
        
        for (int i = 0; i < inputs + outputs + 1; i++) {
            if (i < inputs) {
                nodes.add(new Node(0, nextNodeID));
            } else if (i < outputs) {
                nodes.add(new Node(1, nextNodeID));
            } else {
                nodes.add(new Node(0, nextNodeID));
                biasNodeID = nextNodeID;
            }
            nextNodeID++;
        }
    }
    
    /**
     * build the network with randomized genes, connecting every input+bias to each output
     */
    public void initializeNetwork() {
    
    }
    
    /**
     * build the network arrayList in the order that each node should operate, i.e. in layer order
     */
    public void buildNetwork() {
    
    }
    
    /**
     * Perform the calculations for each node and gene in the genome based on the inputs. return the outputs
     * @param ins input array to feed to the input nodes
     * @return return the values from the output nodes
     */
    public double[] executeNetwork(double[] ins) {
        return null;
    }
    
    /**
     * mutate the genome
     * x% chance of creating a new node
     * y% chance of creating a new connection
     * z% chance of just mutating an existing connection weight
     */
    public void mutateGenome() {
    
    }
    
    /**
     * Find the index of a particular node with the provided ID inside the "node" arrayList
     * @param i the nodeID of the desired node
     * @return the index of that node in the "node" arrayList
     */
    public int findNodeID(int i) {
        return -1;
    }
    
    /**
     * see above but for the network array
     * @param i the nodeID of the desired node
     * @return the index of the desired node in the network arrayLst
     */
    public int findNetworkID(int i) {
        return -1;
    }
    
    /**
     * @param i the geneID of the desired gene
     * @return the index of that gene in the "genes" array.
     */
    public int findGeneID(int i) {
        return -1;
    }
    
    /**
     * add a new node between two currently existing nodes. Select a random gene to turn off. The existing node to
     * new node should have weight of 1, the new node to existing node should have weight equal to the old weight.
     * Don't disconnect a bias node, given that it just outputs 1
     */
    private void addNode() {
    
    }
    
    /**
     * Increment the layer value for all nodes that have layer value equal to l or above
     * @param l minimum layer level that needs to be incremented
     */
    private void incrementLayers(int l) {
    
    }
    
    /**
     * create a new connection between two nodes that currently do not have a connection.
     */
    private void addGeneConnection() {
    
    }
    
    /**
     * @return whether there are genes that can still be connected in the matrix. Assume only feed-forward behavior
     * with no backwards feeding. Include disabled genes in the count because they can be turned back on and they
     * should not be overwritten.
     */
    private boolean isFull() {
        return false;
    }
    
    /**
     * clear all output connections if we want to rebuild the network.
     */
    private void clearConnections() {
        for (Node n : nodes) {
            n.getOutputGenes().clear();
        }
    }
    
    /**
     * connect all genes in the array to their appropriate from-node
     */
    private void buildConnections() {
        for (Gene g : genes) {
            g.getFromNode().addGeneConnection(g);
        }
    }
    
    public Genome copy() {
        Genome g = new Genome(inputs, outputs);
        g.setLayers(layers);
        g.setNextGeneID(nextGeneID);
        g.setNextNodeID(nextNodeID);
        g.setBiasNodeID(biasNodeID);
        g.setGenes(genes);
        g.setNodes(nodes);
        g.setNetwork(network);
        return g;
    }
}
