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
  This class records each of the mutations that can occur within a network.
  
  When a single connection is added, this object records the node it is from, the node it is to, and the new Gene
  itself. These can then be referenced in the future to search for existing connections when new connections are
  mutated.
  
  When a new node is created, this object records the original gene that was turned off, the two new genes created,
  and original from node, the original to node, and the new node itself. Each of these can be referenced in the
  future to search for existing node mutations.
 */

package NEAT;

public class NodeHistory {
    private Gene original;
    private Gene early;
    private Gene late;
    private Node fromNode;
    private Node toNode;
    private Node newNode;
    
    public NodeHistory(Node f, Node t, Gene g) {
        original = null;
        late = null;
        fromNode = f;
        toNode = t;
        newNode = null;
        early = g;
    }
    
    public NodeHistory(Gene o, Gene e, Gene l, Node f, Node t, Node n) {
        original = o;
        early = e;
        late = l;
        fromNode = f;
        toNode = t;
        newNode = n;
    }
    
    public Gene getOriginal() {
        return original;
    }
    
    public void setOriginal(Gene original) {
        this.original = original;
    }
    
    public Gene getEarly() {
        return early;
    }
    
    public void setEarly(Gene early) {
        this.early = early;
    }
    
    public Gene getLate() {
        return late;
    }
    
    public void setLate(Gene late) {
        this.late = late;
    }
    
    public Node getFromNode() {
        return fromNode;
    }
    
    public void setFromNode(Node fromNode) {
        this.fromNode = fromNode;
    }
    
    public Node getToNode() {
        return toNode;
    }
    
    public void setToNode(Node toNode) {
        this.toNode = toNode;
    }
    
    public Node getNewNode() {
        return newNode;
    }
    
    public void setNewNode(Node newNode) {
        this.newNode = newNode;
    }
}
