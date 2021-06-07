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
 * This class represents the neural network itself. It contains all of the information needed to determine how a
 * network will perform. Each network has a constant number of inputs, outputs, and a single bias node. The number of
 *  layers can be updated as new nodes are added to the network.
 */

package NEAT;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

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
    private ArrayList<NodeHistory> history;
    
    /**
     * Inputs are created with NodeIDs ranging from 0 to inputs+1 (N.I.), with the biasNode being the extra node.
     * Therefore, outputs range from inputs+1 to inputs + outputs+1 (N.I.)
     * @param in  total number of inputs to the network
     * @param out total number of outputs from the network
     */
    public Genome(int in, int out) {
        inputs = in;
        outputs = out;
        layers = 2;
        nextNodeID = 0;
        nextGeneID = 0;
        nodes = new ArrayList<>();
        genes = new ArrayList<>();
        network = new ArrayList<>();
        history = new ArrayList<>();
        
        for (int i = 0; i < inputs + outputs + 1; i++) {
            if (i < inputs) {
                nodes.add(new Node(0, nextNodeID));
            } else if (i < inputs + 1) {
                nodes.add(new Node(0, nextNodeID));
                biasNodeID = nextNodeID;
            } else {
                nodes.add(new Node(1, nextNodeID));
            }
            nextNodeID++;
        }
        
        // build the network for the first time, should be the only time this function is called
        initializeNetwork();
    }
    
    /**
     * build the network with randomized genes, connecting every input+bias to each output
     */
    public void initializeNetwork() {
        Gene g;
        for (int i = 0; i < inputs + 1; i++) {
            for (int j = inputs + 1; j < inputs + outputs + 1; j++) {
                g = new Gene(nodes.get(i), nodes.get(j), nextGeneID, ThreadLocalRandom.current().nextDouble(-1, 1));
                nodes.get(i).addGeneConnection(g);
                genes.add(g);
            }
        }
    }
    
    /**
     * create a genome that is intended to be used for Genome crossover. Therefore, do not initialize anything except
     * the inputs and outputs
     */
    public Genome(int in, int out, boolean crossover) {
        inputs = in;
        outputs = out;
        
        nodes = new ArrayList<>();
        genes = new ArrayList<>();
        network = new ArrayList<>();
        history = new ArrayList<>();
    }
    
    /**
     * Determine if the newly created node is an existing mutation. If it is, update the new nodeID and the new
     * geneIDs. If it is actually a new mutation, create a new history of that mutation and add it to the arrayList
     * history.
     * @param g  the gene being replaced
     * @param g1 the new "input" gene
     * @param g2 the new "output" gene
     * @param nf the original "from node"
     * @param nt the original "to node"
     * @param n  the newly created node
     */
    private void checkNodes(Gene g, Gene g1, Gene g2, Node nf, Node nt, Node n) {
        //check each NodeHistory in the history list
        for (NodeHistory h : history) {
            Node hfn = h.getFromNode();
            Node htn = h.getToNode();
            
            // because the network is fully connected at the start, we only need to test if the new node's to and from
            // match a previous mutations to and from. If they do match, set the IDs for the newly created node and
            // genes to those matching the existing mutation. Then return out, because we're only able to do a single
            // mutation at a time
            if (hfn.getID() == nf.getID() && htn.getID() == nt.getID()) {
                n.setNodeID(h.getNewNode().getID());
                g1.setGeneID(h.getEarly().getGeneID());
                g2.setGeneID(h.getLate().getGeneID());
                return;
            }
        }
        
        // if the mutation doesn't already exist, create a new mutation record and add it to the history
        NodeHistory a = new NodeHistory(g, g1, g2, nf, nt, n);
        history.add(a);
        // make sure to deprecate the nodeID and geneID counters just in case. Since mutation only happens once per
        // generation, and these ID's are likely fed from the population level, this probably isn't strictly required.
        nextNodeID--;
        nextGeneID -= 2;
    }
    
    /**
     * add a new node between two currently existing nodes. Select a random gene to turn off. The existing node to
     * new node should have weight of 1, the new node to existing node should have weight equal to the old weight.
     * Don't disconnect a bias node, given that it just outputs 1
     */
    private void addNode() {
        // add a connection if there is only a single existing connection, but do not add a node because the network
        // does not currently support enough complexity.
        int count = 0;
        for (Gene g : genes) {
            if (g.isEnabled()) {
                count++;
            }
        }
        if (count < 2) {
            newGeneConnection();
            return;
        }
        
        int temp = ThreadLocalRandom.current().nextInt(genes.size());
        Gene g = genes.get(temp);
        // determine if the selected gene is the only biasNode output
        boolean bias = (g.getFromNode().getID() == biasNodeID) && (nodes.get(biasNodeID).getOutputGenes().size() == 1);
        
        // Select a new random gene if the current gene is disabled or the only bias output
        while (!g.isEnabled() || bias) {
            temp = ThreadLocalRandom.current().nextInt(genes.size());
            g = genes.get(temp);
            bias = (g.getFromNode().getID() == biasNodeID) && (nodes.get(biasNodeID).getOutputGenes().size() == 1);
        }
        
        // create a new node that is at the appropriate layer
        Node n = new Node(g.getFromNode().getLayer() + 1, nextNodeID);
        nextNodeID++;
        // create two new genes to connect with this node (one in, one out) and use the appropriate weights
        Gene g1 = new Gene(g.getFromNode(), n, nextGeneID, 1.0d);
        nextGeneID++;
        Gene g2 = new Gene(n, g.getToNode(), nextGeneID, g.getWeight());
        nextGeneID++;
        
        // verify if the total number of layers needs to be incremented and whether certain nodes in the network need
        // to have their layer incremented. Do this before adding the new node to the arrayList to make sure it isn't
        // actually incremented
        if (n.getLayer() == g.getToNode().getLayer()) {
            incrementLayers(n.getLayer());
        }
        
        // check if this mutation has already occurred within the population
        checkNodes(g, g1, g2, g.getFromNode(), g.getToNode(), n);
        
        // add the new node and connections, and disable the old node. Return the new node. Note that the new node,
        // and the new genes are at the end of the nodes and genes array lists.
        nodes.add(n);
        genes.add(g1);
        genes.add(g2);
        g.toggleEnabled();
    }
    
    /**
     * Increment the layer value for all nodes that have layer value equal to l or above
     * @param l minimum layer level that needs to be incremented
     */
    private void incrementLayers(int l) {
        for (Node n : nodes) {
            if (n.getLayer() >= l) {
                n.setLayer(n.getLayer() + 1);
            }
        }
        layers++;
    }
    
    /**
     * Calculate the number of active genes in the network
     * @return the number of active genes in the network
     */
    private int geneSize() {
        int count = 0;
        for (Gene g : genes) {
            if (g.isEnabled()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * create a new connection between two nodes that currently do not have a connection.
     */
    private void newGeneConnection() {
        if (isFull()) { // add a node if there isn't any available space to create a new connection
            addNode();
        } else {
            //select two nodes to try to connect
            Node a = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
            Node b = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
            //ensure connections don't occur between previously connected nodes, or those on the same layer level
            while (a.isConnected(b)) {
                a = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
                b = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
            }
            
            // once two valid nodes have been found, need to create a new connection
            Gene g;
            // build the to and from nodes appropriately based on layer level
            if (a.getLayer() > b.getLayer()) {
                g = new Gene(b, a, nextGeneID, ThreadLocalRandom.current().nextDouble(-1, 1));
                b.addGeneConnection(g);
            } else {
                g = new Gene(a, b, nextGeneID, ThreadLocalRandom.current().nextDouble(-1, 1));
                a.addGeneConnection(g);
            }
            
            boolean exists = checkGenes(g);
            if (!exists) nextGeneID++;
            genes.add(g);
        }
    }
    
    /**
     * mutate the genome
     * x% chance of creating a new node
     * y% chance of creating a new connection
     * z% chance of just mutating an existing connection weight
     */
    public void mutateGenome() {
        double x = 0.02d;
        double y = 0.08d;
        double rand = ThreadLocalRandom.current().nextDouble();
        
        // Need to check if the created node already exists as a Map innovation. If it does, need to change that
        // Nodes unique ID and the related genes before adding it to the network.
        if (rand < x) {
            addNode();
            buildNetwork();
            
            // Need to check if the created gene already exists as a Map innovation. If it does, need to change tha
            // gene's unique ID
            
        } else if (rand < y) {
            newGeneConnection();
            buildNetwork();
        } else {
            int geneTemp = ThreadLocalRandom.current().nextInt(genes.size());
            Gene g = genes.get(geneTemp);
            g.mutateWeight();
        }
    }
    
    /**
     * Determine if the newly created gene is not a new mutation, i.e. it connects two nodes in the network where
     * another individual in the population also connects those same nodes in the network.
     * @param g the new gene to test
     * @return true if the mutation already exists, false if it doesn't exist
     */
    private boolean checkGenes(Gene g) {
        int a = g.getFromNode().getID();
        int b = g.getToNode().getID();
        
        for (NodeHistory h : history) {
            if (a == h.getFromNode().getID() && b == h.getToNode().getID()) {
                g.setGeneID(h.getEarly().getGeneID());
                return true;
            }
        }
        NodeHistory n = new NodeHistory(g.getFromNode(), g.getToNode(), g);
        history.add(n);
        return false;
    }
    
    /**
     * @return an array where each index corresponds to a layer, and each value is the number of nodes in that layer
     * AND all earlier layers
     */
    private int[] cumulativeNodesPerLayer() {
        int[] num = nodesPerLayer();
        for (int i = 1; i < num.length; i++) {
            num[i] += num[i - 1];
        }
        return num;
    }
    
    /**
     * @return an array where each index corresponds to a layer, and each value is the number of nodes in that layer
     */
    private int[] nodesPerLayer() {
        int[] total = new int[layers];
        for (Node n : nodes) {
            total[n.getLayer()] += 1;
        }
        return total;
    }
    
    /**
     * Create a new offspring Genome based on Genome A and Genome B. Copy the nodes from parent A as it is the more
     * fit parent. If parent B lacks genes connecting these nodes, then use the genes from parent A
     * @param a is the MORE FIT Parent of the two
     * @param b is the LESS FIT Parent of the two
     * @return a mixing of both parents where possible
     */
    // TODO create the cross-over method
    public static Genome crossover(Genome a, Genome b) {
        Genome child = new Genome(a.inputs, a.outputs, true);
        int temp;
        Gene gt;
        
        for (Node n : a.getNodes()) {
            child.getNodes().add(n.copy());
        }
        
        for (Gene g : a.getGenes()) {
            temp = b.findGeneID(g.getGeneID());
            if (temp != -1) {
                gt = b.getGenes().get(temp);
                gt = randomGene(g, gt).copy();
            } else {
                gt = g.copy();
            }
            child.getGenes().add(gt);
        }
        return child;
    }
    
    private static Gene randomGene(Gene a, Gene b) {
        double rand = ThreadLocalRandom.current().nextDouble();
        if (rand <= 0.5) {
            return a;
        } else {
            return b;
        }
    }
    
    /**
     * Perform the calculations for each node and gene in the genome based on the inputs. return the outputs
     * @param ins input array to feed to the input nodes
     * @return return the values from the output nodes
     */
    public double[] executeNetwork(double[] ins) {
        double[] result = new double[outputs];
        for (int i = 0; i < inputs; i++) {
            nodes.get(i).addInput(ins[i]);
        }
        nodes.get(biasNodeID).addInput(1d);
        buildNetwork();
        for (Node n : network) {
            n.computeOut();
        }
        
        // fill the output array with the value from the output nodes
        for (int i = inputs + 1; i < inputs + outputs + 1; i++) {
            result[i - (inputs + 1)] = nodes.get(i).getOutputs();
        }
        return result;
    }
    
    /**
     * build the network arrayList in the order that each node should operate, i.e. in layer order
     */
    public void buildNetwork() {
        // reset the network entirely to ensure all genes are correctly connected
        network.clear();
        clearConnections();
        buildConnections();
        
        // rebuild the network, which represents the order of operations.
        for (int i = 0; i < layers; i++) {
            for (Node n : nodes) {
                if (n.getLayer() == i) {
                    network.add(n);
                }
            }
        }
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
    
    /**
     * @param i the geneID of the desired gene
     * @return the index of that gene in the "genes" array.
     */
    public int findGeneID(int i) {
        for (int j = 0; j < genes.size(); j++) {
            if (genes.get(j).getGeneID() == i) {
                return j;
            }
        }
        return -1;
    }
    
    /**
     * Find the index of a particular node with the provided ID inside the "node" arrayList
     * @param i the nodeID of the desired node
     * @return the index of that node in the "node" arrayList
     */
    public int findNodeID(int i) {
        for (int j = 0; j < nodes.size(); j++) {
            if (nodes.get(j).getID() == i) {
                return j;
            }
        }
        return -1;
    }
    
    /**
     * see above but for the network array
     * @param i the nodeID of the desired node
     * @return the index of the desired node in the network arrayLst
     */
    public int findNetworkID(int i) {
        for (int j = 0; j < network.size(); j++) {
            if (network.get(j).getID() == i) {
                return j;
            }
        }
        return -1;
    }
    
    /**
     * @return an equivalent copy of this genome
     */
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
    
    /**
     * @return whether there are genes that can still be connected in the matrix. Assume only feed-forward behavior
     * with no backwards feeding. Include disabled genes in the count because they can be turned back on and they
     * should not be overwritten.
     */
    private boolean isFull() {
        int s = nodes.size();
        int outs;
        int[] cum;
        
        for (Node n : nodes) {
            outs = n.getOutputGenes().size();
            cum = cumulativeNodesPerLayer();
            if (outs < (s - cum[n.getLayer()])) {
                return false;
            }
        }
        return true;
    }
    
    public ArrayList<Node> getNodes() {
        return nodes;
    }
    
    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }
    
    public ArrayList<Gene> getGenes() {
        return genes;
    }
    
    public void setGenes(ArrayList<Gene> genes) {
        this.genes = genes;
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
    
    public void setNextNodeID(int nextNodeID) {
        this.nextNodeID = nextNodeID;
    }
    
    public ArrayList<NodeHistory> getHistory() {
        return history;
    }
    
    public void setHistory(ArrayList<NodeHistory> history) {
        this.history = history;
    }
    
    public int getBiasNodeID() {
        return biasNodeID;
    }
    
    public void setBiasNodeID(int biasNodeID) {
        this.biasNodeID = biasNodeID;
    }
    
    public int getLayers() {
        return layers;
    }
    
    public void setLayers(int layers) {
        this.layers = layers;
    }
    
    public int getNextGeneID() {
        return nextGeneID;
    }
    
    public void setNextGeneID(int nextGeneID) {
        this.nextGeneID = nextGeneID;
    }
    
    public ArrayList<Node> getNetwork() {
        return network;
    }
    
    public void setNetwork(ArrayList<Node> network) {
        this.network = network;
    }
}
