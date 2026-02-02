package ai;

import ai.eval.Evaluator;
import game.core.Board;
import game.core.Move;
import game.rules.Rules;
import game.runtime.Game;
import game.runtime.Presets;

public class SearchingPlayer implements Player {

    private final Rules rules;
    private final Evaluator eval;

    public SearchingPlayer(Rules rules, Evaluator eval) {
        this.rules = rules;
        this.eval = eval;
    }

    @Override
    public Move chooseMove(Board board) {
        Game game = new Game(Presets.standard2048(), 42);
        Move maxMove = Move.LEFT;
        for (Move move : Move.values()) {
            game.step(move);
        }
        return Move.UP;
    }

    private double search(Game game, boolean player, int depth) {
        throw new IllegalArgumentException("not implemented yet");
    }
}
