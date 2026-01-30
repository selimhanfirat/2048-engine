package game;

import game.ui.BoardRenderer;

import java.util.Random;

public class Game {

    Rules rules;
    Spawner spawner;
    Board board;
    int score;
    Random random;

    public Game(Rules rules, Spawner spawner, Board board, Random random) {
        this.rules = rules;
        this.spawner = spawner;
        this.board = board;
        this.random = random;
        this.score = 0;
    }

    public void gameLoop() {
        SpawnDecision decision = spawner.pickRandomTile(board, random);
        board = board.addTile(decision);

        System.out.println(BoardRenderer.pretty(board));

        while(rules.isGameOver(board)) {
            Move move = Move.random();
            MoveResult result = rules.makeMove(board, move);
            score += result.scoreGained();
            board = result.board();
            decision = spawner.pickRandomTile(board, random);
            board = board.addTile(decision);

            System.out.println(BoardRenderer.pretty(board));
            sleep(250);
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }





}
