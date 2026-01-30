package game;

import game.ui.BoardRenderer;

import java.util.Random;
import java.util.Scanner;

public class Game {

    Rules rules;
    Spawner spawner;
    Board board;
    int score;
    Random random;
    private final Scanner input = new Scanner(System.in);

    public Game(Rules rules, Spawner spawner, Board board, Random random) {
        this.rules = rules;
        this.spawner = spawner;
        this.board = board;
        this.random = random;
        this.score = 0;
    }

    public void gameLoop() {
        SpawnDecision decision = spawner.pickRandomTile(board, random);
        board = board.placeTile(decision);

        System.out.println(BoardRenderer.pretty(board));

        while(!rules.isGameOver(board)) {
            Move move = Move.random();
            System.out.println(move);
            MoveResult result = rules.makeMove(board, move);
            score += result.scoreGained();
            board = result.board();
            decision = spawner.pickRandomTile(board, random);
            board = board.placeTile(decision);

            System.out.println(BoardRenderer.pretty(board));
            waitForEnter();
        }
    }

    private void waitForEnter() {
        System.out.print("Press Enter to continue...");
        input.nextLine();
    }






}
