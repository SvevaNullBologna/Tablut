package Custom;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class Constants{

    static final Double C = Math.sqrt(2);
    static final Double WIN = 1.0;
    static final Double LOSE = -1.0;
    static Double DRAW(State.Turn player){
        return player == State.Turn.WHITE ? -0.4 : 0.4;
    }
    static final Double UNVISITED_NODE = Double.POSITIVE_INFINITY;
    static final Double NOT_A_TERMINAL_STATE = Double.NaN;


}
