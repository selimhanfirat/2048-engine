package player;

import game.core.Board;
import game.core.Move;
import game.rules.Rules;
import game.core.MoveResult;
import java.util.EnumSet;

public class DumbGreedyPlayer implements Player {

    Rules rules;
    public DumbGreedyPlayer(Rules rules) {
        this.rules = rules;
    }

    @Override
    public Move chooseMove(Board board, EnumSet<Move> possibleMoves) {
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
