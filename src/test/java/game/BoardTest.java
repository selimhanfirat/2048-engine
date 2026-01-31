package game;

import game.core.Board;
import game.core.Move;
import game.core.SpawnDecision;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BoardTest {

    @Test
    void defaultConstructor_creates4x4AllZeros() {
        Board b = new Board();
        assertThat(b.getDimension()).isEqualTo(4);

        int[][] g = b.getGrid();
        assertThat(g.length).isEqualTo(4);
        assertThat(g[0].length).isEqualTo(4);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertThat(g[i][j]).isZero();
            }
        }
    }

    @Test
    void constructor_copiesInputArray() {
        int[][] src = {
                {1, 0},
                {0, 2}
        };
        Board b = new Board(src);

        src[0][0] = 999; // mutate original after construction
        assertThat(b.getGrid()[0][0]).isEqualTo(1);
    }

    @Test
    void getGrid_returnsDefensiveCopy() {
        int[][] src = {
                {1, 0},
                {0, 2}
        };
        Board b = new Board(src);

        int[][] g1 = b.getGrid();
        g1[0][0] = 999; // mutate returned grid

        assertThat(b.getGrid()[0][0]).isEqualTo(1);
    }

    @Test
    void constructor_rejectsNonSquareGrid() {
        int[][] nonSquare = {
                {1, 2, 3},
                {4, 5, 6}
        };

        assertThatThrownBy(() -> new Board(nonSquare))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("square");
    }

    @Test
    void getEmptyCells_returnsAllZeroPositionsRowMajor() {
        int[][] src = {
                {1, 0, 3},
                {0, 0, 6},
                {7, 8, 0}
        };
        Board b = new Board(src);

        assertThat(b.getEmptyCells()).containsExactly(1, 3, 4, 8);
    }

    @Test
    void placeTile_returnsNewBoard_andDoesNotMutateOriginal() {
        int[][] src = {
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        Board b = new Board(src);

        Board b2 = b.placeTile(new SpawnDecision(1, 0, 4));

        assertThat(b.getGrid()[1][0]).as("original unchanged").isZero();
        assertThat(b2.getGrid()[1][0]).as("new board has tile").isEqualTo(4);
        assertThat(b2).isNotEqualTo(b);
    }

    @Test
    void transpose_works() {
        int[][] src = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        Board b = new Board(src);

        int[][] expected = {
                {1, 4, 7},
                {2, 5, 8},
                {3, 6, 9}
        };

        assertThat(b.transpose().getGrid()).isDeepEqualTo(expected);
    }

    @Test
    void reverseRows_works() {
        int[][] src = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        Board b = new Board(src);

        int[][] expected = {
                {3, 2, 1},
                {6, 5, 4},
                {9, 8, 7}
        };

        assertThat(b.reverseRows().getGrid()).isDeepEqualTo(expected);
    }

    @Test
    void applyTransformation_matchesDefinitions() {
        int[][] src = {
                {1, 2},
                {3, 4}
        };
        Board b = new Board(src);

        assertThat(b.applyTransformation(Move.LEFT)).isEqualTo(b);
        assertThat(b.applyTransformation(Move.RIGHT)).isEqualTo(b.reverseRows());
        assertThat(b.applyTransformation(Move.UP)).isEqualTo(b.transpose());
        assertThat(b.applyTransformation(Move.DOWN)).isEqualTo(b.transpose().reverseRows());
    }

    @Test
    void applyInverseTransformation_undoesTransformation_forAllMoves() {
        int[][] src = {
                {0, 2, 0, 4},
                {8, 0, 0, 0},
                {0, 0, 16, 0},
                {0, 32, 0, 64}
        };
        Board b = new Board(src);

        for (Move m : Move.values()) {
            Board transformed = b.applyTransformation(m);
            Board back = transformed.applyInverseTransformation(m);

            assertThat(back)
                    .as("inverse should undo transform for %s", m)
                    .isEqualTo(b);
        }
    }

    @Test
    void equalsAndHashCode_workForSameContents() {
        int[][] a = {
                {1, 0, 2},
                {0, 4, 0},
                {8, 0, 16}
        };
        int[][] b = {
                {1, 0, 2},
                {0, 4, 0},
                {8, 0, 16}
        };

        Board x = new Board(a);
        Board y = new Board(b);

        assertThat(x).isEqualTo(y);
        assertThat(x.hashCode()).isEqualTo(y.hashCode());
    }
}
