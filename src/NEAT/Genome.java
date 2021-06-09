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
    private final int inputSize;
    private final int outputSize;
    private int biasNodeID;
    private int layers;
    
    private ArrayList<Node> nodes;
    private ArrayList<Gene> genes;
    private ArrayList<Node> network;
    
    /**
     * Inputs are created with NodeIDs ranging from 0 to inputs+1 (N.I.), with the biasNode being the extra node.
     * Therefore, outputs range from inputs+1 to inputs + outputs+1 (N.I.)
     * @param in  total number of inputs to the network
     * @param out total number of outputs from the network
     */
    public Genome(int in, int out) {
        inputSize = in;
        outputSize = out;
        layers = 2;
        nodes = new ArrayList<>();
        genes = new ArrayList<>();
        network = new ArrayList<>();
        
        buildNodes(in, out);
        buildGenes(in, out);
    }
    
    /**
     * create a node for every input, output, and an extra for the bias node
     * @param in  # of inputs
     * @param out # of outputs
     */
    private void buildNodes(int in, int out) {
        for (int i = 0; i < in + 1; i++) {
            nodes.add(new Node(0, i));
        }
        biasNodeID = in;
        for (int i = in + 1; i < in + out + 1; i++) {
            nodes.add(new Node(1, i));
        }
    }
    
    /**
     * Connect every single input (and bias) to every single output with a random weight
     * @param in  # of inputs
     * @param out # of outputs
     */
    private void buildGenes(int in, int out) {
        int geneCount = 0;
        double w;
        for (int i = 0; i < in + 1; i++) {
            for (int j = in + 1; j < in + out + 1; j++) {
                w = ThreadLocalRandom.current().nextDouble(-1, 1);
                genes.add(new Gene(nodes.get(i), nodes.get(j), geneCount, w));
                geneCount++;
            }
        }
        genes.get(genes.size() - 1).setLargestNode(true);
    }
    
    /**
     * create a genome that is intended to be used for Genome crossover. Therefore, do not initialize anything except
     * the inputs and outputs
     */
    public Genome(int in, int out, boolean crossover) {
        inputSize = in;
        outputSize = out;
        
        nodes = new ArrayList<>();
        genes = new ArrayList<>();
        network = new ArrayList<>();
    }
    
    public History buildHistory() {
        return new History(genes);
    }
    
    /**
     * @return an equivalent copy of this genome
     */
    public Genome copy() {
        Genome g = new Genome(inputSize, outputSize);
        g.setLayers(layers);
        g.setBiasNodeID(biasNodeID);
        g.setGenes(genes);
        g.setNodes(nodes);
        g.setNetwork(network);
        return g;
    }
    
    /**
     * Find the index of a given node
     * @param n node to search for
     * @return the index of that node in the nodes array, or -1 if it doesn't exist
     */
    public int getNode(Node n) {
        for (Node node : nodes) {
            if (node.getID() == n.getID()) return nodes.indexOf(node);
        }
        return -1;
    }
    
    /**
     * Find the index of a given Gene
     * @param g the gene to look for
     * @return the index of that gene in the genes array, or -1 if it doesn't exist
     */
    public int getGene(Gene g) {
        for (Gene gene : genes) {
            if (gene.getGeneID() == g.getGeneID()) return genes.indexOf(gene);
        }
        return -1;
    }
    
    /**
     * Read in the inputs and set those inputs to the input value for each node in the first layer, which all have a
     * nodeID of 0->inputSize inclusive (the last node is biasNode, so set its value = 1). Then propagate the network
     * and collect the outputs (nodeID of inputSize+1->inputs+outputs+1 exclusive).
     * @param ins the values to feed the network
     * @return the outputs of each output node
     */
    public double[] runNetwork(double[] ins) {
        if (ins.length != inputSize) return null;
        for (int i = 0; i < inputSize; i++) {
            nodes.get(i).addInput(ins[i]);
        }
        nodes.get(inputSize).addInput(1d);
        for (Node n : network) {
            n.computeOut();
        }
        double[] outs = new double[outputSize];
        for (int i = inputSize + 1; i < inputSize + outputSize + 1; i++) {
            outs[i - (inputSize + 1)] = nodes.get(i).getOutputs();
        }
        for (Node n : nodes) {
            n.clearValues();
        }
        return outs;
    }
    
    /**
     * mutate the genome, with the population history as reference to create new nodes or connections.
     * @param h the population history
     * @return the "new" population history, which may or may not be modified
     */
    public History mutateGenome(History h) {
        double rand = ThreadLocalRandom.current().nextDouble();
        History newH;
        if (rand < 0.03) {
            newH = addNode(h);
        } else if (rand < 0.13) {
            newH = addConnection(h);
        } else {
            int r = ThreadLocalRandom.current().nextInt(genes.size());
            genes.get(r).mutateWeight();
            newH = h;
        }
        return newH;
    }
    
    /**
     * Add a new node by randomly selecting a gene, disabling it, and creating a new node between the previous
     * fromNode and toNode. Check along the way to determine if this is a unique mutation. If it is, generate new
     * info, otherwise take existing info
     * @param h the population evolution history
     * @return the updated evolution history
     */
    private History addNode(History h) {
        Gene g = genes.get(ThreadLocalRandom.current().nextInt(genes.size()));
        while (!g.isEnabled()) {
            g = genes.get(ThreadLocalRandom.current().nextInt(genes.size()));
        }
        int ID = h.containsNodeInnovation(g);
        if (ID == -1) {
            int newID = h.getNextNodeID();
            int newLayer = g.getFromNode().getLayer() + 1;
            if (newLayer == g.getToNode().getLayer()) {
                incrementLayers(newLayer);
            }
            Node newNode = new Node(newLayer, newID);
            newNode.setReplacedGeneID(g.getGeneID());
            int newGID = h.getNextGeneID();
            double w = ThreadLocalRandom.current().nextDouble(-1, 1);
            Gene g1 = new Gene(g.getFromNode(), newNode, newGID, 1d);
            Gene g2 = new Gene(newNode, g.getToNode(), newGID + 1, g.getWeight());
            g1.setLargestNode(true);
            g.toggleEnabled();
            nodes.add(newNode);
            genes.add(g1);
            genes.add(g2);
            newNode.addGeneConnection(g2);
            h.addNodeInnovation(newNode, g1, g2);
            return h;
        } else {
            int newLayer = g.getFromNode().getLayer() + 1;
            if (newLayer == g.getToNode().getLayer()) {
                incrementLayers(newLayer);
            }
            Node n = new Node(newLayer, ID);
            n.setReplacedGeneID(g.getGeneID());
            int g1ID = h.getIDBeforeNode(n);
            int g2ID = h.getIDAfterNode(n);
            double w = ThreadLocalRandom.current().nextDouble(-1, 1);
            Gene g1 = new Gene(g.getFromNode(), n, g1ID, 1);
            Gene g2 = new Gene(n, g.getToNode(), g2ID, w);
            g.toggleEnabled();
            nodes.add(n);
            genes.add(g1);
            genes.add(g2);
            n.addGeneConnection(g2);
            return h;
        }
    }
    
    /**
     * increase the layer of all nodes in an given layer or above
     * @param l the given layer
     */
    private void incrementLayers(int l) {
        for (Node n : nodes) {
            if (n.getLayer() >= l) {
                n.setLayer(n.getLayer() + 1);
            }
        }
    }
    
    /**
     * Add a new connection by randomly selecting two nodes and creating a new connection between them. Check during
     * this process, whether this is a new innovation or if this innovation already exists
     * @param h the evolutionary history
     * @return the new/updated evolutionary history
     */
    private History addConnection(History h) {
        if (isFull()) {
            return addNode(h);
        }
        
        Node a = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
        Node b = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
        
        while (a.isConnected(b)) {
            a = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
            b = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
        }
        
        int ID = -2;
        if (a.getLayer() > b.getLayer()) {
            ID = h.containsGeneInnovation(b, a);
        } else {
            ID = h.containsGeneInnovation(a, b);
        }
        
        if (ID == -1) {
            int newGID = h.getNextGeneID();
            double w = ThreadLocalRandom.current().nextDouble(-1, 1);
            Gene g;
            if (a.getLayer() > b.getLayer()) {
                g = new Gene(b, a, newGID, w);
            } else {
                g = new Gene(a, b, newGID, w);
            }
            genes.add(g);
            g.getFromNode().addGeneConnection(g);
            h.addGeneInnovation(g);
        } else {
            double w = ThreadLocalRandom.current().nextDouble(-1, 1);
            Gene g;
            if (a.getLayer() > b.getLayer()) {
                g = new Gene(b, a, ID, w);
            } else {
                g = new Gene(a, b, ID, w);
            }
            genes.add(g);
            g.getFromNode().addGeneConnection(g);
        }
        return h;
    }
    
    /**
     * @return whether or not there is the possibility to add a new connection to the network
     */
    private boolean isFull() {
        for (Node n : nodes) {
            int outputSize = n.getOutputGenes().size();
            int possible = getPossible(n.getLayer());
            if (outputSize < possible) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * For a given layer level, determine the number of nodes that are above that layer level
     * @param l The given layer leve
     * @return the number of nodes above that layer level
     */
    private int getPossible(int l) {
        int count = 0;
        for (Node n : nodes) {
            if (n.getLayer() > l) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Create a new genome that is a crossover of the current genome and a provided genome. Slight preference is
     * given to genes from the current genome, presumed to have the higher fitness. Any non-matching genes in the
     * current genome are directly inherited from the current genome. The node map/network is inhereted from the
     * current genome.
     */
    public Genome crossOver(Genome b) {
        Genome child = new Genome(inputSize, outputSize, true);
        Node temp;
        Gene tempG;
        for (Node n : nodes) {
            temp = n.copy();
            child.getNodes().add(temp);
        }
        for (Gene g : genes) {
            int bLoc = b.matchingGene(g);
            if (bLoc != -1) {
                Gene bGene = b.getGenes().get(bLoc);
                double rand = ThreadLocalRandom.current().nextDouble();
                if (rand < 0.55) {
                    tempG = g.copy();
                } else {
                    tempG = bGene.copy();
                }
                child.getGenes().add(tempG);
            } else {
                tempG = g.copy();
                child.getGenes().add(tempG);
            }
        }
        child.clearNodeOutputs();
        child.connectNodes();
        child.buildNetwork();
        return child;
    }
    
    public ArrayList<Node> getNodes() {
        return nodes;
    }
    
    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }
    
    /**
     * If the provided gene matches a gene in the current genome, return the current genome's index of the gene.
     * Otherwise return -1
     * @param g provided gene to search for
     * @return the index of the provided gene in this genome ("genes" list), or -1 if there isn't a match.
     */
    public int matchingGene(Gene g) {
        for (Gene gene : genes) {
            if (gene.getGeneID() == g.getGeneID()) return genes.indexOf(gene);
        }
        return -1;
    }
    
    public ArrayList<Gene> getGenes() {
        return genes;
    }
    
    public void setGenes(ArrayList<Gene> genes) {
        this.genes = genes;
    }
    
    /**
     * disconnect all output genes from each node, to allow for rebuilding of the network
     */
    public void clearNodeOutputs() {
        for (Node node : nodes) {
            node.clearOutputGenes();
        }
    }
    
    /**
     * rebuild the connections for each node in the network
     */
    public void connectNodes() {
        for (Gene gene : genes) {
            gene.getFromNode().addGeneConnection(gene);
        }
    }
    
    /**
     * create the network array, which lists nodes in the order in which they need to operate through the netwrok
     * (layer 0, then layer 1, etc.)
     */
    public void buildNetwork() {
        network.clear();
        for (int i = 0; i < layers; i++) {
            for (Node n : nodes) {
                if (n.getLayer() == i) {
                    network.add(n);
                }
            }
        }
    }
    
    @Override
    public int hashCode() {
        int result = getInputSize();
        result = 31 * result + getOutputSize();
        result = 31 * result + getBiasNodeID();
        result = 31 * result + getLayers();
        result = 31 * result + getNodes().hashCode();
        result = 31 * result + getGenes().hashCode();
        result = 31 * result + (getNetwork() != null ? getNetwork().hashCode() : 0);
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Genome genome = (Genome) o;
        
        if (getInputSize() != genome.getInputSize()) return false;
        if (getOutputSize() != genome.getOutputSize()) return false;
        if (getBiasNodeID() != genome.getBiasNodeID()) return false;
        if (getLayers() != genome.getLayers()) return false;
        if (!getNodes().equals(genome.getNodes())) return false;
        if (!getGenes().equals(genome.getGenes())) return false;
        return getNetwork() != null ? getNetwork().equals(genome.getNetwork()) : genome.getNetwork() == null;
    }
    
    public int getInputSize() {
        return inputSize;
    }
    
    public int getOutputSize() {
        return outputSize;
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
    
    public ArrayList<Node> getNetwork() {
        return network;
    }
    
    public void setNetwork(ArrayList<Node> network) {
        this.network = network;
    }
}
