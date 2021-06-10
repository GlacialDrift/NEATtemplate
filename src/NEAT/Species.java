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

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Species{
	
	private int specID;
	private Player reference;
	private Player best;
	private double bestFitness;
	private int specAge;
	private int specStale;
	private ArrayList<Player> speciesPlayer;
	
	private final double excessCoeff = 1d;
	private final double weightDiff = 0.5d;
	private final double Comp = 3d;
	
	public Species(int sID, Player r){
		specID = sID;
		reference = r;
		best = r;
		bestFitness = r.getFitness();
		speciesPlayer = new ArrayList<>();
		speciesPlayer.add(r);
	}
	
	public Species(int sID){
		specID = sID;
		reference = null;
		best = null;
		bestFitness = 0;
		speciesPlayer = new ArrayList<>();
	}
	
	public boolean memberOfSpecies(Player p){
		double excess = getExcess(p, reference);
		double wDiff = getWDiff(p, reference);
		int normalizer = p.getBrain().getGenes().size() - 20;
		if(normalizer < 1) normalizer = 1;
		double compat = excessCoeff * excess / normalizer + weightDiff * wDiff;
		if(compat < Comp) {
			speciesPlayer.add(p);
			return true;
		} else {
			return false;
		}
	}
	
	private double getExcess(Player p, Player r){
		Genome pg = p.getBrain();
		Genome rg = r.getBrain();
		double excess = 0d;
		int temp;
		for(Gene g : rg.getGenes()) {
			temp = pg.matchingGene(g);
			if(temp == -1) excess++;
		}
		return excess;
	}
	
	private double getWDiff(Player p, Player r){
		Genome pg = p.getBrain();
		Genome rg = r.getBrain();
		double diff = 0;
		double matches = 0;
		for(Gene g : pg.getGenes()) {
			for(Gene g1 : rg.getGenes()) {
				if(g.getGeneID() == g1.getGeneID()) {
					matches++;
					diff += Math.abs(g.getWeight() - g1.getWeight());
					break;
				}
			}
		}
		if(matches == 0) {
			return 1000;
		} else {
			return diff / matches;
		}
	}
	
	public Player reproduce(){
		Player a = selectPlayer();
		Player b = selectPlayer();
		Player p;
		if(a.getFitness() > b.getFitness()) {
			p = a.crossOver(b);
		} else {
			p = b.crossOver(a);
		}
		return p;
	}
	
	public Player selectPlayer(){
		double rand = ThreadLocalRandom.current().nextDouble();
		int index = ThreadLocalRandom.current().nextInt(speciesPlayer.size());
		double temp = 0.07d * Math.exp(-0.07d * index);
		int counter = 0;
		while (temp < rand && counter < speciesPlayer.size() * 10) {
			index = ThreadLocalRandom.current().nextInt(speciesPlayer.size());
			temp = 0.07d * Math.exp(-0.07d * index);
			counter++;
		}
		return speciesPlayer.get(index);
	}
	
	public void sort(){
		ArrayList<Player> temp = new ArrayList<>();
		
		while (speciesPlayer.size() > 0) {
			double max = 0;
			int index = 0;
			for(Player p1 : speciesPlayer) {
				if(p1.getFitness() > max) {
					max = p1.getFitness();
					index = speciesPlayer.indexOf(p1);
				}
			}
			temp.add(speciesPlayer.get(index));
			speciesPlayer.remove(index);
		}
		speciesPlayer = temp;
		determineBest();
	}
	
	private void determineBest(){
		if(speciesPlayer.get(0).getFitness() > bestFitness) {
			specStale = 0;
			best = speciesPlayer.get(0);
			bestFitness = speciesPlayer.get(0).getFitness();
		} else {
			specStale++;
		}
	}
	
	@Override
	public int hashCode(){
		int result;
		long temp;
		result = getSpecID();
		result = 31 * result + getReference().hashCode();
		result = 31 * result + getBest().hashCode();
		temp = Double.doubleToLongBits(getBestFitness());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + getSpecAge();
		result = 31 * result + getSpecStale();
		result = 31 * result + (getSpeciesPlayer() != null ? getSpeciesPlayer().hashCode() : 0);
		temp = Double.doubleToLongBits(getExcessCoeff());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getWeightDiff());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getComp());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	@Override
	public boolean equals(Object o){
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		
		Species species = (Species) o;
		
		if(getSpecID() != species.getSpecID()) return false;
		if(Double.compare(species.getBestFitness(), getBestFitness()) != 0) return false;
		if(getSpecAge() != species.getSpecAge()) return false;
		if(getSpecStale() != species.getSpecStale()) return false;
		if(Double.compare(species.getExcessCoeff(), getExcessCoeff()) != 0) return false;
		if(Double.compare(species.getWeightDiff(), getWeightDiff()) != 0) return false;
		if(Double.compare(species.getComp(), getComp()) != 0) return false;
		if(!getReference().equals(species.getReference())) return false;
		if(!getBest().equals(species.getBest())) return false;
		return getSpeciesPlayer() != null ? getSpeciesPlayer().equals(species.getSpeciesPlayer()) : species.getSpeciesPlayer() == null;
	}
	
	public int getSpecID(){
		return specID;
	}
	
	public void setSpecID(int specID){
		this.specID = specID;
	}
	
	public Player getReference(){
		return reference;
	}
	
	public void setReference(Player reference){
		this.reference = reference;
	}
	
	public Player getBest(){
		return best;
	}
	
	public void setBest(Player best){
		this.best = best;
	}
	
	public double getBestFitness(){
		return bestFitness;
	}
	
	public void setBestFitness(double bestFitness){
		this.bestFitness = bestFitness;
	}
	
	public int getSpecAge(){
		return specAge;
	}
	
	public void setSpecAge(int specAge){
		this.specAge = specAge;
	}
	
	public int getSpecStale(){
		return specStale;
	}
	
	public void setSpecStale(int specStale){
		this.specStale = specStale;
	}
	
	public ArrayList<Player> getSpeciesPlayer(){
		return speciesPlayer;
	}
	
	public void setSpeciesPlayer(ArrayList<Player> speciesPlayer){
		this.speciesPlayer = speciesPlayer;
	}
	
	public double getExcessCoeff(){
		return excessCoeff;
	}
	
	public double getWeightDiff(){
		return weightDiff;
	}
	
	public double getComp(){
		return Comp;
	}
}
