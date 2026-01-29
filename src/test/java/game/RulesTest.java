package game;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;


public class RulesTest {

    Rules rules = new BaseRules();

    @Test
    void testOneElement() {
        Board board = new Board(new int[][]{
                new int[]{0, 0, 2, 0},
                new int[]{0, 0, 2, 0},
                new int[]{0, 0, 2, 0},
                new int[]{0, 0, 2, 0}
        });

        Board expectedBoard = new Board(new int[][]{
                new int[]{2, 0, 0, 0},
                new int[]{2, 0, 0, 0},
                new int[]{2, 0, 0, 0},
                new int[]{2, 0, 0, 0}
        });

        MoveResult r = rules.makeMove(board, Move.LEFT);
        MoveResult expected = new MoveResult(expectedBoard, 0);

        assertThat(r).isEqualTo(expected);
    }

    void testMerge() {
        Board board = new Board(new int[][]{
                new int[]{0, 0, 2, 0},
                new int[]{0, 2, 2, 0},
                new int[]{0, 0, 2, 0},
                new int[]{4, 0, 2, 0}
        });

        Board expectedBoard = new Board(new int[][]{
                new int[]{2, 0, 0, 0},
                new int[]{4, 0, 0, 0},
                new int[]{2, 0, 0, 0},
                new int[]{4, 2, 0, 0}
        });

        MoveResult r = rules.makeMove(board, Move.LEFT);
        MoveResult expected = new MoveResult(expectedBoard, 4);

        assertThat(r).isEqualTo(expected);
    }

}
