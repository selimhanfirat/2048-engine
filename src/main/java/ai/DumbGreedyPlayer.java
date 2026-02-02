package ai;

import game.core.Board;
import game.core.Move;
import game.core.MoveResult;
import game.rules.Rules;

public class DumbGreedyPlayer implements Player {

    private final Rules rules;

    public DumbGreedyPlayer(Rules rules) {
        this.rules = rules;
    }

    @Override
    public Move chooseMove(Board board) {
        var possibleMoves = rules.getLegalMoves(board);

        int maxScoreGained = Integer.MIN_VALUE / 2;
        Move bestMove = null;

        for (Move move : possibleMoves) {
            MoveResult mr = rules.makeMove(board, move);
            if (mr.scoreGained() > maxScoreGained) {
                maxScoreGained = mr.scoreGained();
                bestMove = move;
            }
        }

        return bestMove;
    }
}
