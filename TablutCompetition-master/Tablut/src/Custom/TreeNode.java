package Custom;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;

import java.util.ArrayList;
import java.util.List;

public class TreeNode{
    private State state;
    private TreeNode parent;
    private List<TreeNode> children;
    private char operator; //operatore applicato per ottenere il nodo
    private int depth;
    private int path_cost; //costo del cammino stato iniziale - nodo. Può essere aggiornato
    private int visitCount;
    public  double EstimatedValue;
    private boolean hasBeenExpanded;
    

    public TreeNode() {
    	//DEBUGGGG
    }
    
    public TreeNode(State state, TreeNode parent, char operator, int depth, int path_cost) {
    	this.state = state;
    	this.parent = parent;
    	this.operator = operator;
    	this.depth = depth;
    	this.path_cost = path_cost;
    	this.hasBeenExpanded = false;
    	this.visitCount = 0;
    	this.EstimatedValue = 0.0;
    	this.children = new ArrayList<TreeNode>();
    }
    
    public TreeNode(State state, char operator, int depth, int path_cost) { //is root
    	this.state = state;
    	this.parent = null;
    	this.operator = operator;
    	this.depth = depth;
    	this.path_cost = path_cost;
    	this.hasBeenExpanded = false;
    	this.visitCount = 0;
    	this.EstimatedValue = 0.0;
    	this.children = new ArrayList<TreeNode>();
    }
    
    


    
    public State getState() { //we can read it differently
    	return this.state;
    }
    
    
    public void updateState(State state) {
    	//checks to do about state before updating it
    	this.state = state;
    }
    
    public TreeNode getParent(){
    	return parent;
    }
    
    public List<TreeNode> getChildren(){
    	return this.children;
    }
    
    public int getDepth() {
    	return depth;
    }
    
    public int getPathCost(){
    	return path_cost;
    }
    
    public void updatePathCost(int path_cost){
    	//checks to do about path_cost before updating it
    	this.path_cost = path_cost;
    }
    
    public char operator() {
    	return operator;
    }
   
    public int getVisitCount() {
    	return this.visitCount;
    }
    
    
    public void addChild(TreeNode node) {
    	node.parent = this;
    	children.add(node);
    }
    
    public void removeChild(TreeNode node) throws Exception{
    	if(!children.remove(node)) {
    		throw new Exception(node + " NOT FOUND ");
    	}
    }
    
    public void VisitNode(Double value) { 
    	if(value != null) {
    		this.EstimatedValue += value;
    	}
    	this.visitCount++;
    }
    

    public void ExpandNode() {
    	if(this.isFullyExpanded()) return;
    	for(Action action : legalActions) {
    		State newState = applyMove(this.state, action);
    		TreeNode child = new TreeNode(newState, this);
    		this.children.add(child);
    	}
    	
    	this.hasBeenExpanded = true;
    }

    public boolean isFullyExpanded() {
    	return children.stream().allMatch(child -> child.visitCount>0);
    }
    
    public boolean isTerminal() {
    	return this.children.size() == 0;
    }
}