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
 * This class represents the population-level evolutionary history of the network. It really is just an arrayList of
 * genes, but it contains a variety of functions for detecting whether a new mutation already exists in the network.
 * Therefore, this class's list of genes contains EVERY gene ever created in the network as well as every Node (as
 * each gene contains a from- and to-Node.
 */

package NEAT;

import java.util.ArrayList;

public class History {
    private ArrayList<Gene> history;
    
    
    /**
     * Empty constructor, utilized during initial population creation
     */
    public History() {
        history = new ArrayList<>();
    }
    
    /**
     * Constructor with provided history, used for cloning
     */
    public History(ArrayList<Gene> h) {
        history = h;
    }
    
    /**
     * Copy function, probably not needed
     * @return a copy of this listing of genes
     */
    public History copy() {
        return new History(history);
    }
    
    /**
     * Add a new gene to the evolutionary history
     * @param g the new gene to be added, typically a new connection between 2 existing nodes
     */
    public void addGeneInnovation(Gene g) {
        history.add(g);
    }
    
    /**
     * Add two new genes to the evolutionary history, corresponding to the input and output genes of a new node
     * @param n  the newly created node used to verify self-consistency
     * @param g1 the gene leading into the new node
     * @param g2 the gene leading out of the new node
     */
    public void addNodeInnovation(Node n, Gene g1, Gene g2) {
        if (g1.getToNode().getID() != n.getID()) {
            System.out.println("The provided gene \"g1\" does not connect with the provided node n");
            return;
        }
        if (g2.getFromNode().getID() != n.getID()) {
            System.out.println("The provided gene \"g2\" does not connect with the provided node n");
        }
        disableLargestNode();
        g1.setLargestNode(true);
        history.add(g1);
        history.add(g2);
    }
    
    /**
     * Go through the list of genes and make sure the largestNode flag is disabled for all genes. Used as a new
     * node innovation has been created and want to set that newNode to have the largest nodeID. In the entire
     * history array, there should only be a single gene that has the largestNode flag enabled
     */
    public void disableLargestNode() {
        for (Gene g : history) {
            g.setLargestNode(false);
        }
    }
    
    /**
     * Check if a "new" gene connection mutation is actually a unique new gene connection for the population. If it
     * truly is unique, return -1, otherwise return the geneID of the existing innovation.
     * @param a the fromNode for the new gene
     * @param b the toNode for the new gene
     * @return -1 if unique, the geneID of the exiting innovation if not.
     */
    public int containsGeneInnovation(Node a, Node b) {
        for (Gene g : history) {
            if (g.getFromNode().getID() == a.getID() && g.getToNode().getID() == b.getID()) return g.getGeneID();
        }
        return -1;
    }
    
    /**
     * Provided a randomly selected gene in an existing network, look through the nodes of each gene to see if any of
     * those nodes replaced the selected gene. Note that we only need to look through toNodes, as the layer0 nodes
     * don't have a replaced value (all -1). If there is a match, return that node's ID, otherwise if "will be" a new
     * unique node innovation, return -1;
     * @param g the randomly selected gene to be replaced
     * @return the nodeID if this is not a unique new node, or -1 if it is unique
     */
    public int containsNodeInnovation(Gene g) {
        Node temp;
        for (Gene g1 : history) {
            temp = g1.getToNode();
            if (temp.getReplacedGeneID() == g.getGeneID()) {
                return temp.getID();
            }
        }
        return -1;
    }
    
    /**
     * Used when a "new" Node is not unique to assign values to the gene feeding that "new" node
     * @param n the "new" node
     * @return the geneID feeding that node
     */
    
    //TODO need to rework this as many genes could be feeding into the same "new" node (just based on ID), but want to
    // isolate specifically the gene that was replaced
    public int getIDBeforeNode(Node n) {
        for (Gene g : history) {
            if (g.getToNode().getID() == n.getID()) return g.getGeneID();
        }
        return -1;
    }
    
    /**
     * Used when a "new" Node is not unique to assign values to the gene leaving that "new" node
     * @param n the "new" node
     * @return the geneID leaving that node
     */
    
    //TODO need to rework this, as many genes could be leaving the same "new" node (just based on ID), but want to
    // isolate specifically the gene that connects to the previous to-Node
    public int getIDAfterNode(Node n) {
        for (Gene g : history) {
            if (g.getFromNode().getID() == n.getID()) return g.getGeneID();
        }
        return -1;
    }
    
    @Override
    
    public int hashCode() {
        return getHistory().hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        History history1 = (History) o;
        
        return getHistory().equals(history1.getHistory());
    }
    
    public ArrayList<Gene> getHistory() {
        return history;
    }
    
    public void setHistory(ArrayList<Gene> history) {
        this.history = history;
    }
    
    public int getNextGeneID() {
        return history.size();
    }
    
    public int getNextNodeID() {
        for (Gene g : history) {
            if (g.isLargestNode()) return g.getToNode().getID() + 1;
        }
        return -1;
    }
}
