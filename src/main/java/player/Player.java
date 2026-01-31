package player;

import game.core.Board;
import game.core.Move;

import java.util.EnumSet;

// Player is a decider, player gets access to a set of rules,
// a set of legal actions (implemented as moves enum for now),
//
public interface Player {

    // given a board(state) and a set of legal moves, return a move
    public Move chooseMove(Board board, EnumSet<Move> possibleMoves);

}
