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

public class Population{
	
	private final int inputs;
	private final int outputs;
	private ArrayList<Species> species;
	private History history;
	private int generationNum;
	private ArrayList<Player> players;
	
	public Population(){
		players = new ArrayList<>();
		species = new ArrayList<>();
		history = new History();
		inputs = 4; // needs to change based on implementation
		outputs = 4; //change based on implementation
		for(int i = 0; i < 100; i++) {
			players.add(new Player(inputs, outputs));
			players.get(i).getBrain().buildNetwork();
		}
		generationNum = 0;
		history = players.get(0).getBrain().buildHistory();
		speciate();
	}
	
	public void speciate(){
		if(species.size() == 0) {
			Species s = new Species(0, players.get(0));
			species.add(s);
		}
		
		boolean flag = false;
		for(Player p : players) {
			for(Species s : species) {
				if(s.memberOfSpecies(p)) {
					s.getSpeciesPlayer().add(p);
					flag = true;
					break;
				}
			}
			if(!flag) {
				Species s1 = new Species(species.size(), p);
				species.add(s1);
			}
		}
	}
	
	public Population(int ins, int outs, int size){
		players = new ArrayList<>();
		species = new ArrayList<>();
		history = new History();
		inputs = ins;
		outputs = outs;
		for(int i = 0; i < size; i++) {
			players.add(new Player(ins, outs));
			players.get(i).getBrain().buildNetwork();
		}
		generationNum = 0;
		history = players.get(0).getBrain().buildHistory();
		speciate();
	}
	
	public void updateLiving(){
		for(Player p : players) {
			if(p.isLiving()) {
				p.Look();
				p.Think();
				p.Move();
				p.Update();
				p.Show();
			}
		}
	}
	
	public boolean allDead(){
		for(Player p : players) {
			if(p.isLiving()) return false;
		}
		return true;
	}
	
	public void calculateFitness(){
		for(Player p : players) {
			p.calcFitness();
		}
		sortPlayers();
	}
	
	private void sortPlayers(){
		ArrayList<Player> temp = new ArrayList<>();
		
		while (players.size() > 0) {
			double max = 0;
			int index = 0;
			for(Player p1 : players) {
				if(p1.getFitness() > max) {
					max = p1.getFitness();
					index = players.indexOf(p1);
				}
			}
			temp.add(players.get(index));
			players.remove(index);
		}
		players = temp;
	}
	
	public void cullAndRePop(){
		int size;
		Player p;
		int start;
		for(Species s : species) {
			
			s.sort();
			size = s.getSpeciesPlayer().size();
			if(s.getSpecAge() < 10) { // only cull the bottom 25% to allow species time to evolve
				start = size * 3 / 4;
			} else if(s.getSpecStale() > 15) { // if there have been no improvements, kill the whole species
				start = 0;
			} else { // cull the bottom 50%
				start = size / 2;
			}
			for(int i = start; i < size; i++) {
				p = s.getSpeciesPlayer().get(i);
				s.getSpeciesPlayer().remove(p);
				players.remove(p);
			}
			if(start == 0) { // remove the species from the list if it was entirely culled
				species.remove(s);
			} else {
				for(int i = size / 2; i < size; i++) {
					p = s.reproduce();
					s.getSpeciesPlayer().add(p);
					players.add(p);
				}
			}
		}
	}
	
	public void mutate(){
		for(Player p : players) {
			history = p.mutate(history);
		}
	}
	
	@Override
	public int hashCode(){
		int result = species.hashCode();
		result = 31 * result + history.hashCode();
		result = 31 * result + generationNum;
		result = 31 * result + inputs;
		result = 31 * result + outputs;
		result = 31 * result + players.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object o){
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		
		Population that = (Population) o;
		
		if(generationNum != that.generationNum) return false;
		if(inputs != that.inputs) return false;
		if(outputs != that.outputs) return false;
		if(!species.equals(that.species)) return false;
		if(!history.equals(that.history)) return false;
		return players.equals(that.players);
	}
	
	public int getInputs(){
		return inputs;
	}
	
	public int getOutputs(){
		return outputs;
	}
	
	public ArrayList<Species> getSpecies(){
		return species;
	}
	
	public void setSpecies(ArrayList<Species> species){
		this.species = species;
	}
	
	public History getHistory(){
		return history;
	}
	
	public void setHistory(History history){
		this.history = history;
	}
	
	public int getGenerationNum(){
		return generationNum;
	}
	
	public void setGenerationNum(int generationNum){
		this.generationNum = generationNum;
	}
	
	public ArrayList<Player> getPlayers(){
		return players;
	}
	
	public void setPlayers(ArrayList<Player> players){
		this.players = players;
	}
}
