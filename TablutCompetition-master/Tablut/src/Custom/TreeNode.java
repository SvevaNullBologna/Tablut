package Custom;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TreeNode{
    private final State state;
    private final State.Turn turn;
    private TreeNode parent;
    private final List<TreeNode> children;
    private final Action action; //azione applicata per ottenere il nodo
    private final int depth;
    private int visitCount;
    private boolean hasBeenExpanded;
    public  double value;

    public TreeNode(State state, TreeNode parent, Action action) {
    	this.state = state;
    	this.turn = state.getTurn();
        if(parent==null){
            this.parent = null;
            this.depth = 0;
            this.action = null;
        }
        else{
    		this.parent = parent;
    		this.depth = parent.depth + 1;
    		this.action = action;
    	}

    	this.visitCount = 0;
    	this.value = 0.0;
        this.hasBeenExpanded = false;
    	this.children = new ArrayList<>();
    }


    public State getState() { //we can read it differently
        return this.state;
    }
    public State.Turn getTurn(){
    	return this.turn;
    }

    public TreeNode getParent(){
        return parent;
    }

    public List<TreeNode> getChildren(){
    	return this.children;
    }

    public Action getOriginAction() {
        return action;
    }

    public int getVisitCount() {
    	return this.visitCount;
    }
    
    public double getValue() {
    	return this.value;
    }
    
    public double getAverageValue()
    {
    	if(this.visitCount == 0 ) return 0.0; //MA significa che non è stato visitato e quindi va visitato!
    	return this.value / this.visitCount;
    }
    
    public void addChild(TreeNode node) {
    	node.parent = this;
    	children.add(node);
    }

    public void VisitNode(Double value) {
    	if(value != null) {
    		this.value += value;
    	}
    	this.visitCount++;
    }
    
    public boolean hasBeenExpanded(){
        return this.hasBeenExpanded;
    }

    public void ExpandNode(List<MoveResult> legalMoves) {
    	if(this.hasBeenExpanded() || legalMoves == null || legalMoves.isEmpty()){
            return;
        }
    	
    	for(MoveResult move : legalMoves) {
    		TreeNode child = new TreeNode(move.resultingState, this, move.action); //dove salviamo il value? Nel padre o nel figlio?
            this.addChild(child);

            double terminalValue = child.evaluateTerminalState();
            if(!Double.isNaN(terminalValue)){
                child.VisitNode(terminalValue);
            }

    	}
        this.hasBeenExpanded = true;
    	

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

    public Double evaluateTerminalState() {
        State.Turn result = this.state.getTurn();

        if (result == State.Turn.WHITEWIN) {
            return this.turn == State.Turn.WHITE ? Constants.WIN : Constants.LOSE;
        } else if (result == State.Turn.BLACKWIN) {
            return this.turn == State.Turn.BLACK ? Constants.WIN : Constants.LOSE;
        } else if (result == State.Turn.DRAW) {
            return Constants.DRAW(this.turn);
        }
        else{
            return  Constants.NOT_A_TERMINAL_STATE;
        }
    }

    public double UCB(State.Turn turn ) {
        TreeNode parent = this.getParent();

        Double terminalValue = this.evaluateTerminalState();
        if(terminalValue!=null && !Double.isNaN(terminalValue)){
            //se è terminale, allora restituisci i valori terminal
            return terminalValue;
        }

        // Se il nodo non è mai stato visitato, va esplorato subito
        if (this.getVisitCount() == 0) {
            return Constants.UNVISITED_NODE;
        }

        // Se è la radice o il genitore ha problemi, usa solo il valore stimato
        if (parent == null || parent.getVisitCount() == 0) {
            return this.getValue();
        }

        // Aggiungiamo robustezza con valori minimi per evitare log(0) o div/0
        double parentVisits = Math.max(parent.getVisitCount(), 1e-6);
        double nodeVisits = Math.max(this.getVisitCount(), 1e-6);

        double exploitation = this.getAverageValue();

        if(turn == State.Turn.WHITE){
            exploitation *= 1;
        }
        else{
            exploitation *= -1;
        }

        return  exploitation + Constants.C * Math.sqrt(Math.log(parentVisits) / nodeVisits);
    }

    public void cutParent(){
        this.parent = null;
    }

    public TreeNode findChildWithState(State targetState) {
        for (TreeNode child : this.children) {
            if (child.getState().equals(targetState)) {
                return child;
            }
        }
        return null;
    }


    public static TreeNode getRandomNode(List<TreeNode> nodes) {
        Collections.shuffle(nodes);
        return nodes.get(new Random().nextInt(nodes.size()));

    }

    public static TreeNode getChildWithBestUCT(List<TreeNode> children, State.Turn turn){
        // 1. Priorità ai nodi non visitati
        Collections.shuffle(children);
        return getChildWithMaxUCT(children, turn);
    }

    public static TreeNode getChildWithMaxUCT(List<TreeNode> children, State.Turn turn) {
        double maxUCT = Constants.initial_maxUCT;
        List<TreeNode> bestNodes = new ArrayList<>(); //si possono avere pareggi tra i nodi figli
        for(TreeNode child: children) {
            double uctValue = child.UCB(turn);
            if(uctValue > maxUCT) { //se un nodo è migliore, sostituiamo il max
                maxUCT = uctValue;
                bestNodes.clear();
                bestNodes.add(child);
            }
            else if(uctValue == maxUCT) { //se c'è un pareggio, aggiungiamo il figlio alla lista
                bestNodes.add(child);
            }
        }

        if(bestNodes.isEmpty()) {
            return null;
        }
        else {
            return TreeNode.getRandomNode(bestNodes); //risolviamo i pareggi scegliendone uno randomicamente
        }
    }



    @Override
    public String toString() {
        return "\n" +
                "TREENODE: \n" +
                this.turn + "\n" +
                "depth: " + this.depth + "\n" +
                "totalValue: " +  this.value + "\n" +
                "visitCount: " + this.visitCount + "\n" +
                "hasBeenExpanded: " + this.hasBeenExpanded + "\n" +
                "is_root: " + (this.parent == null) + "\n" +
                "has_children" + (!this.children.isEmpty()) + "\n\n";
    }


}