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

package NEAT;

import java.util.Arrays;

public class Player {
    private Genome brain;
    private double fitness;
    private double scaleFit;
    private double cumFit;
    private double[] inputs;
    private double[] outputs;
    private boolean living;
    
    /**
     * Standard constructor
     * @param inSize  # of inputs provided
     * @param outSize # of possible outputs expected
     */
    public Player(int inSize, int outSize) {
        inputs = new double[inSize];
        outputs = new double[outSize];
        living = true;
        fitness = 0d;
        brain = new Genome(inSize, outSize);
    }
    
    /**
     * Constructor that feeds a provided genome, possibly useful for crossOver
     * @param g the provided genome
     */
    public Player(Genome g) {
        brain = g;
        fitness = 0;
        living = true;
        inputs = new double[g.getInputSize()];
        outputs = new double[g.getOutputSize()];
    }
    
    public void Look() {
        // set the values of inputs depending on the implementation
    }
    
    /**
     * Propagate the inputs through the network and collect the outputs
     */
    public void Think() {
        outputs = brain.runNetwork(inputs);
    }
    
    public void Move() {
        // update other fields/locations/info based on outputs
    }
    
    public void Update() {
        // update other fields as necessary
        // update "living" based on implementation
    }
    
    public void Show() {
        // draw the player as necessary
    }
    
    public double calcFitness() {
        // changes based on implementation
        return 0d;
    }
    
    public Player copy() {
        Player p = new Player(inputs.length, outputs.length);
        p.setBrain(brain.copy());
        p.setFitness(fitness);
        p.setLiving(living);
        return p;
    }
    
    public History mutate(History h) {
        return brain.mutateGenome(h);
    }
    
    public Player crossOver(Player b) {
        Genome newG = brain.crossOver(b.getBrain());
        return new Player(newG);
    }
    
    public Genome getBrain() {
        return brain;
    }
    
    public void setBrain(Genome brain) {
        this.brain = brain;
    }
    
    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getBrain().hashCode();
        temp = Double.doubleToLongBits(getFitness());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + Arrays.hashCode(getInputs());
        result = 31 * result + Arrays.hashCode(getOutputs());
        result = 31 * result + (isLiving() ? 1 : 0);
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Player player = (Player) o;
        
        if (Double.compare(player.getFitness(), getFitness()) != 0) return false;
        if (isLiving() != player.isLiving()) return false;
        if (!getBrain().equals(player.getBrain())) return false;
        if (!Arrays.equals(getInputs(), player.getInputs())) return false;
        return Arrays.equals(getOutputs(), player.getOutputs());
    }
    
    public double getFitness() {
        return fitness;
    }
    
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
    
    public double[] getInputs() {
        return inputs;
    }
    
    public void setInputs(double[] inputs) {
        this.inputs = inputs;
    }
    
    public double[] getOutputs() {
        return outputs;
    }
    
    public void setOutputs(double[] outputs) {
        this.outputs = outputs;
    }
    
    public boolean isLiving() {
        return living;
    }
    
    public void setLiving(boolean living) {
        this.living = living;
    }
    
    public double getCumFit() {
        return cumFit;
    }
    
    public void setCumFit(double cumFit) {
        this.cumFit = cumFit;
    }
    
    public double getScaleFit() {
        return scaleFit;
    }
    
    public void setScaleFit(double scaleFit) {
        this.scaleFit = scaleFit;
    }
}
