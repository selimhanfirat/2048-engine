package game;

import game.ui.BoardRenderer;

import java.util.Random;

public class Main {

    public static void main(String[] args) {
        Random random = new Random();

        Board board = new Board(4);

        Rules rules = new BaseRules();          // assuming default ctor
        Spawner spawner = new BaseSpawner(0.9);    // assuming default ctor

        Game game = new Game(
                rules,
                spawner,
                board,
                random
        );

        game.gameLoop();
    }
}
