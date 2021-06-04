package NEAT;

import java.util.concurrent.ThreadLocalRandom;

public class Gene {
    
    private boolean enabled;
    private final Node fromNode;
    private final Node toNode;
    private final int geneID;
    private double weight;
    
    public Gene(Node from, Node to, int ID, double w) {
        fromNode = from;
        toNode = to;
        geneID = ID;
        weight = w;
        enabled = true;
    }
    
    public void toggleEnabled() {
        enabled = !enabled;
    }
    
    public void setEnabled(boolean a) {
        enabled = a;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public Node getFromNode() {
        return fromNode;
    }
    
    public Node getToNode() {
        return toNode;
    }
    
    public int getGeneID() {
        return geneID;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public Gene copy() {
        return new Gene(fromNode, toNode, geneID, weight);
    }
    
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
}
