package game.runtime;

import game.core.Board;
import game.core.Move;
import game.core.MoveResult;
import game.core.SpawnDecision;
import player.Player;
import ui.BoardRenderer;

public class Game {

    private final GameConfig config;
    private final Player player;

    private Board board;
    private int score;

    public Game(GameConfig config, Player player) {
        this.config = config;
        this.player = player;
        this.board = new Board(config.gridSize());
        this.score = 0;
    }

    public void gameLoop() {
        spawn(); // initial spawn

        System.out.println(BoardRenderer.pretty(board));

        while (!config.rules().isGameOver(board)) {
            Move move = player.chooseMove(board, config.rules().getLegalMoves(board));
            System.out.println(move);

            MoveResult result = config.rules().makeMove(board, move);
            board = result.board();
            score += result.scoreGained();

            spawn();
            System.out.println(BoardRenderer.pretty(board));
        }
        System.out.println("THE GAME IS OVER AND YOUR SCORE IS " + score);
    }

    private void spawn() {
        SpawnDecision decision =
                config.spawner().pickTile(board, config.random());
        board = board.placeTile(decision);
    }
}
