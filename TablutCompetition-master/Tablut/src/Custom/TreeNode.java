package Custom;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.Action;

import java.util.ArrayList;
import java.util.List;

public class TreeNode{
    private State state;
    private final State.Turn turn;
    private TreeNode parent;
    private final List<TreeNode> children;
    private final Action action; //azione applicata per ottenere il nodo
    private final int depth;
    private int visitCount;
    public  Double totalValue;
    private boolean hasBeenExpanded;

    public TreeNode(State state, TreeNode parent, Action action) {
    	this.state = state;
    	this.turn = state.getTurn();
        if(parent==null){
            this.parent = parent;
            this.depth = 0;
            this.action = null;
        }
        else{
    		this.parent = parent;
    		this.depth = parent.depth + 1;
    		this.action = action;
    	}
    	
    	this.hasBeenExpanded = false;
    	this.visitCount = 0;
    	this.totalValue = 0.0;
    	this.children = new ArrayList<>();
    }
   
    public Double evaluateTerminalState() {
        State.Turn result = this.state.getTurn();

        if (result == State.Turn.WHITEWIN) {
            return this.turn == State.Turn.WHITE ? Constants.WIN : Constants.LOSE;
        } else if (result == State.Turn.BLACKWIN) {
            return this.turn == State.Turn.BLACK ? Constants.WIN : Constants.LOSE;
        } else if (result == State.Turn.DRAW) {
            return Constants.DRAW;
        }

        return null;
    }
    
    public State.Turn getTurn(){
    	return this.turn;
    }
    
    public State getState() { //we can read it differently
    	return this.state;
    }
    
    
    public void updateState(State state) {
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
    
    public Action getOriginAction() {
    	return action;
    }
   
    public int getVisitCount() {
    	return this.visitCount;
    }
    
    public double getTotalValue() {
    	return this.totalValue;
    }
    
    public double getAverageValue()
    {
    	if(this.visitCount == 0 ) return 0.0;
    	return this.totalValue / this.visitCount;
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
    		this.totalValue += value;
    	}
    	this.visitCount++;
    }
    

    public void ExpandNode(List<MoveResult> legalMoves) {
    	if(this.isFullyExpanded()) return;
    	
    	for(MoveResult move : legalMoves) {
    		TreeNode child = new TreeNode(move.resultingState, this, move.action);
    		this.addChild(child);
    	}
    	
    	this.hasBeenExpanded = true;
    }

    public boolean isFullyExpanded() {
    	return hasBeenExpanded;
    }
    
    public boolean isTerminal() {
        return  state.getTurn() == State.Turn.WHITEWIN ||
                state.getTurn() == State.Turn.BLACKWIN ||
                state.getTurn() == State.Turn.DRAW;

    }

    public TreeNode getMostVisitedChild(){
        if(this.children == null || this.children.isEmpty()){
            return  null;
        }
        int maxVisits = -1;
        TreeNode answer = null;
        for(TreeNode child : this.children){
            if(child.visitCount > maxVisits){
                maxVisits = child.visitCount;
                answer = child;
            }
        }
        return answer;
    }


    @Override
    public String toString() {
        return "\n" +
                "TREENODE: \n" +
                this.turn + "\n" +
                "depth: " + this.depth + "\n" +
                "totalValue: " +  this.totalValue + "\n" +
                "visitCount: " + this.visitCount + "\n" +
                "hasBeenExpanded: " + this.hasBeenExpanded + "\n" +
                "is_root: " + (this.parent == null ? true : false) + "\n" +
                "has_children" + (this.children.isEmpty() ? false : true) + "\n\n";
    }
}