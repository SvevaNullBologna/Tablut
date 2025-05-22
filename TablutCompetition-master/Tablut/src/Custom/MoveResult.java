package Custom;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MoveResult {
    public final Action action;
    public final State resultingState;

    public MoveResult(Action action, State resultingState) {
        this.action = action;
        this.resultingState = resultingState;
    }

    public static MoveResult getRandomMove(List<MoveResult> moves) {
        return moves.get(new Random().nextInt(moves.size()));
    }
    public static List<MoveResult> getLegalActionsAndResultingStates(State state, Game rules) {
        List<MoveResult> results = new ArrayList<>();
        State.Pawn[][] board = state.getBoard();
        State.Turn turn = state.getTurn();
        int size = board.length;

        for (int fromRow = 0; fromRow < size; fromRow++) {
            for (int fromCol = 0; fromCol < size; fromCol++) {
                State.Pawn pawn = board[fromRow][fromCol];
                if (!isPlayersPawn(turn, pawn)) continue;

                addMovesInDirection(state, fromRow, fromCol, turn,  0,  1, results, rules); // RIGHT
                addMovesInDirection(state, fromRow, fromCol, turn,  0, -1, results, rules); // LEFT
                addMovesInDirection(state, fromRow, fromCol, turn,  1,  0, results, rules); // DOWN
                addMovesInDirection(state, fromRow, fromCol, turn, -1,  0, results, rules); // UP
            }
        }

        return results;
    }

    private static void addMovesInDirection(State state, int fromRow, int fromCol, State.Turn turn,
                                     int rowStep, int colStep, List<MoveResult> results, Game rules) {
        int row = fromRow + rowStep;
        int col = fromCol + colStep;
        State.Pawn[][] board = state.getBoard();
        int size = board.length;

        while (row >= 0 && row < size && col >= 0 && col < size && board[row][col].equals(State.Pawn.EMPTY)) {
            try {
                String from = state.getBox(fromRow, fromCol);
                String to = state.getBox(row, col);
                Action action = new Action(from, to, turn);
                State cloned = state.clone();
                State next = rules.checkMove(cloned, action);
                results.add(new MoveResult(action, next));
            } catch (Exception ignored) {
                //non credo si debba far altro
            }

            row += rowStep;
            col += colStep;
        }
    }

    public static boolean isPlayersPawn(State.Turn turn, State.Pawn pawn) {
        if (turn.equals(State.Turn.WHITE)) {
            return pawn.equals(State.Pawn.WHITE) || pawn.equals(State.Pawn.KING);
        } else {
            return pawn.equals(State.Pawn.BLACK);
        }
    }

}
