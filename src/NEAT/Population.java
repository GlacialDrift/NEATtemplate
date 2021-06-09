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

public class Population {
    private final ArrayList<Species> species;
    private final History history;
    private final int generationNum;
    private final int inputs;
    private final int outputs;
    private ArrayList<Player> players;
    
    public Population() {
        players = new ArrayList<>();
        species = new ArrayList<>();
        history = new History();
        inputs = 4; // needs to change based on implementation
        outputs = 4; //change based on implementation
        generationNum = 0;
    }
    
    public Population(int ins, int outs, int size) {
        players = new ArrayList<>();
        species = new ArrayList<>();
        history = new History();
        inputs = ins;
        outputs = outs;
        for (int i = 0; i < size; i++) {
            players.add(new Player(ins, outs));
            players.get(i).getBrain().buildNetwork();
        }
        generationNum = 0;
    }
    
    public void updateLiving() {
        for (Player p : players) {
            if (p.isLiving()) {
                p.Look();
                p.Think();
                p.Move();
                p.Update();
                p.Show();
            }
        }
    }
    
    public boolean allDead() {
        for (Player p : players) {
            if (p.isLiving()) return false;
        }
        return true;
    }
    
    
    public void calculateFitness() {
        for (Player p : players) {
            p.calcFitness();
        }
        sortPlayers();
    }
    
    private void sortPlayers() {
        ArrayList<Player> temp = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            double max = 0;
            int index = 0;
            for (Player p1 : players) {
                if (p1.getFitness() > max) {
                    max = p1.getFitness();
                    index = players.indexOf(p1);
                }
            }
            temp.add(players.get(index));
            players.remove(index);
            i--;
        }
        players = temp;
    }
    
    public void speciate() {
        if (species.size() == 0) {
            Species s = new Species(0, players.get(0));
            species.add(s);
        }
        
        for (Player p : players) {
            for (Species s : species) {
                boolean test = s.memberOfSpecies(p);
                if (!test) {
                    Species s1 = new Species(species.size(), p);
                    break;
                }
            }
        }
    }
    
    //TODO need to add species information to each player to keep track of species during culling
    
    // assume pre-sorted population based on fitness
    public void cullPop() {
        int size = players.size();
        while (players.size() > size / 2) {
        
        }
    }
    
    public void repopulate(int target) {
        while (players.size() < target) {
        
        }
    }
    
    public History mutate(History h) {
        for (Player p : players) {
            h = p.mutate(h);
        }
        return h;
    }
}
