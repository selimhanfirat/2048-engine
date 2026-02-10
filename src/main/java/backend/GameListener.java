package backend;

import game.core.Board;
import game.core.Move;

public interface GameListener {
    default void onInit(long seed, Board initialState, int initialScore) {}
    default void onStep(int stepIndex, Move move, Board boardState, int afterScore) {}
    default void onGameOver(Board finalState, int finalScore, int stepsCount) {}
}
