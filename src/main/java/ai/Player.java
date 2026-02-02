package ai;

import game.core.Board;
import game.core.Move;

public interface Player {
    Move chooseMove(Board board);
}
