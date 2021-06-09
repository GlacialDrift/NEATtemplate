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
 * This class represents the connections between nodes of a neural network. They store their from- and to-nodes, they
 *  are given a unique ID (no two genes/connections in the network should have the same geneID), they are given a
 * weight between -1 and 1 (double), and they can either be enabled or disabled. By default, all genes are enabled,
 * but they can become disabled in the process of mutating the genome/network (specifically, if a new node is created
 *  to fit in between the selected gene/connection).
 */

package NEAT;

import java.util.concurrent.ThreadLocalRandom;

public class Gene {
    
    /**
     * object variables
     */
    private final Node fromNode;
    private final Node toNode;
    private int geneID;
    private boolean enabled;
    private double weight;
    private boolean largestNode;
    
    /**
     * Constructor
     * @param from the node from which this connection originates
     * @param to   the node to which this connection transfers information
     * @param ID   the unique ID for this connection
     * @param w    the weight associated with this node (-1 to 1);
     */
    public Gene(Node from, Node to, int ID, double w) {
        fromNode = from;
        toNode = to;
        geneID = ID;
        weight = w;
        enabled = true;
        largestNode = false;
    }
    
    public Gene copy() {
        Gene g = new Gene(fromNode, toNode, geneID, weight);
        g.setLargestNode(largestNode);
        return g;
    }
    
    /**
     * mutate the weight of this gene.
     * 5% chance of random mutation
     * 45% chance of small mutation
     * 20% chance of re-enabling this connection IF it is currently disabled
     */
    public void mutateWeight() {
        double t = ThreadLocalRandom.current().nextDouble();
        if (t < 0.05) {
            weight = ThreadLocalRandom.current().nextDouble(-1, 1);
        } else if (t < 0.5) {
            weight += ThreadLocalRandom.current().nextGaussian() / 30;
            if (weight > 1) weight = 1D;
            if (weight < -1) weight = -1D;
        } else if (t > 0.8) {
            if (!isEnabled()) {
                toggleEnabled();
            }
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void toggleEnabled() {
        enabled = !enabled;
    }
    
    public void setEnabled(boolean a) {
        enabled = a;
    }
    
    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getFromNode().hashCode();
        result = 31 * result + getToNode().hashCode();
        result = 31 * result + getGeneID();
        result = 31 * result + (isEnabled() ? 1 : 0);
        temp = Double.doubleToLongBits(getWeight());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (isLargestNode() ? 1 : 0);
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Gene gene = (Gene) o;
        
        if (getGeneID() != gene.getGeneID()) return false;
        if (isEnabled() != gene.isEnabled()) return false;
        if (Double.compare(gene.getWeight(), getWeight()) != 0) return false;
        if (isLargestNode() != gene.isLargestNode()) return false;
        if (!getFromNode().equals(gene.getFromNode())) return false;
        return getToNode().equals(gene.getToNode());
    }
    
    public int getGeneID() {
        return geneID;
    }
    
    public void setGeneID(int i) {
        geneID = i;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public Node getFromNode() {
        return fromNode;
    }
    
    public Node getToNode() {
        return toNode;
    }
    
    public boolean isLargestNode() {
        return largestNode;
    }
    
    public void setLargestNode(boolean largestNode) {
        this.largestNode = largestNode;
    }
}
