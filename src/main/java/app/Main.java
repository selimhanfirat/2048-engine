package app;

import game.runtime.Game;
import game.runtime.GameConfig;
import game.runtime.Presets;
import player.DumbGreedyPlayer;
import player.Player;

public class Main {

    public static void main(String[] args) {
        GameConfig gameConfig = Presets.standard2048(42);
        Player player = new DumbGreedyPlayer(gameConfig.rules());
        Game game = new Game(gameConfig, player);

        game.gameLoop();

    }

}
