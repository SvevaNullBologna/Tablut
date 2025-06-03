package Custom;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class Constants{

    static final double initial_maxUCT = Double.NEGATIVE_INFINITY;
    static final double initial_minUCT = Double.POSITIVE_INFINITY;
    static final double C = Math.sqrt(2);
    static final double WIN = 1.0;
    static final double LOSE = -1.0;
    static double DRAW(State.Turn player){
        return player == State.Turn.WHITE ? -0.4 : 0.4;
    }
    static final double UNVISITED_NODE = Double.POSITIVE_INFINITY;
    static final double NOT_A_TERMINAL_STATE = Double.NaN;


}
