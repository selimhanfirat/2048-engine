package game;

import game.core.Board;
import game.core.Move;
import game.util.Coordinate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoardTest {

    // ---------- constructors / invariants ----------

    @Test
    void defaultConstructor_creates4x4AllZeros() {
        Board b = new Board();
        assertThat(b.getDimension()).as("dimension").isEqualTo(4);

        int[][] g = b.getGrid();
        assertThat(g).hasDimensions(4, 4);
        for (int i = 0; i < 4; i++) {
            assertThat(g[i]).as("row " + i).hasSize(4);
            for (int j = 0; j < 4; j++) {
                assertThat(g[i][j]).as("cell (" + i + "," + j + ")").isZero();
            }
        }
    }

    @Test
    void dimensionConstructor_createsNxNAllZeros() {
        Board b = new Board(3);

        assertThat(b.getDimension()).as("dimension").isEqualTo(3);
        assertThat(b.getGrid())
                .as("grid")
                .isDeepEqualTo(new int[][]{
                        {0, 0, 0},
                        {0, 0, 0},
                        {0, 0, 0}
                });
    }

    @Test
    void dimensionConstructor_rejectsNonPositiveDimension() {
        assertThatThrownBy(() -> new Board(0))
                .as("dimension 0 should be rejected")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dimension");

        assertThatThrownBy(() -> new Board(-1))
                .as("negative dimension should be rejected")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dimension");
    }

    @Test
    void gridConstructor_copiesInputArray_deepCopy() {
        int[][] src = {
                {1, 0},
                {0, 2}
        };
        Board b = new Board(src);

        src[0][0] = 999;
        src[1][1] = 888;

        assertThat(b.getGrid())
                .as("mutating source must not affect board")
                .isDeepEqualTo(new int[][]{
                        {1, 0},
                        {0, 2}
                });
    }

    @Test
    void getGrid_returnsDefensiveCopy_everyCallIndependent() {
        Board b = new Board(new int[][]{
                {1, 0},
                {0, 2}
        });

        int[][] g1 = b.getGrid();
        int[][] g2 = b.getGrid();

        g1[0][0] = 999;

        assertThat(g2[0][0])
                .as("each getGrid call returns independent copy")
                .isEqualTo(1);

        assertThat(b.getGrid()[0][0])
                .as("board internal state unchanged")
                .isEqualTo(1);
    }

    @Test
    void gridConstructor_rejectsNullOr0x0Grid_messageContains0x0() {
        //noinspection DataFlowIssue
        assertThatThrownBy(() -> new Board(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0x0");

        assertThatThrownBy(() -> new Board(new int[][]{}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0x0");
    }


    @Test
    void gridConstructor_rejectsNonSquareGrid_messageContainsNonSquare() {
        int[][] nonSquare = {
                {1, 2, 3},
                {4, 5, 6}
        };

        assertThatThrownBy(() -> new Board(nonSquare))
                .as("non-square grid must be rejected")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Non-square");
    }

    @Test
    void gridConstructor_rejectsJaggedGrid_messageContainsNonSquare() {
        int[][] jagged = {
                {1, 2},
                {3}
        };

        assertThatThrownBy(() -> new Board(jagged))
                .as("jagged grid must be rejected")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Non-square");
    }

    // ---------- empty cells ----------

    @Test
    void getEmptyCells_returnsAllZeroPositionsRowMajor() {
        Board b = new Board(new int[][]{
                {1, 0, 3},
                {0, 0, 6},
                {7, 8, 0}
        });

        assertThat(b.getEmptyCells())
                .as("row-major indices for zeros")
                .containsExactly(1, 3, 4, 8);
    }

    @Test
    void getEmptyCells_emptyWhenNoZeros() {
        Board b = new Board(new int[][]{
                {1, 2},
                {3, 4}
        });

        assertThat(b.getEmptyCells())
                .as("no zeros => empty list")
                .isEmpty();
    }

    @Test
    void getEmptyCells_allCellsWhenAllZeros() {
        Board b = new Board(new int[][]{
                {0, 0},
                {0, 0}
        });

        assertThat(b.getEmptyCells())
                .as("all zeros => all indices")
                .containsExactly(0, 1, 2, 3);
    }

    // ---------- placeTile ----------

    @Test
    void placeTile_returnsNewBoard_originalUnchanged_andOnlyOneCellChanged() {
        Board b = new Board(new int[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        });

        Board b2 = b.placeTile(new Coordinate(1, 0), 4);

        assertThat(b).as("original board unchanged")
                .isEqualTo(new Board(new int[][]{
                        {0, 0, 0},
                        {0, 0, 0},
                        {0, 0, 0}
                }));

        assertThat(b2.getGrid())
                .as("new board contains placed tile only")
                .isDeepEqualTo(new int[][]{
                        {0, 0, 0},
                        {4, 0, 0},
                        {0, 0, 0}
                });

        assertThat(b2).as("must be new contents").isNotEqualTo(b);
    }

    @Test
    void placeTile_overwritesExistingValue() {
        Board b = new Board(new int[][]{
                {2, 0},
                {0, 0}
        });

        Board b2 = b.placeTile(new Coordinate(0, 0), 4);

        assertThat(b2.getGrid()[0][0])
                .as("overwrite existing")
                .isEqualTo(4);

        assertThat(b.getGrid()[0][0])
                .as("original unchanged")
                .isEqualTo(2);
    }

    @Test
    void placeTile_throwsOnOutOfBounds() {
        Board b = new Board(2);

        assertThatThrownBy(() -> b.placeTile(new Coordinate(2, 0), 2))
                .as("row out of bounds")
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        assertThatThrownBy(() -> b.placeTile(new Coordinate(0, 2), 2))
                .as("col out of bounds")
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    // ---------- transpose / reverseRows ----------

    @Test
    void transpose_isInvolution_transposeTwiceEqualsOriginal() {
        Board b = new Board(new int[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });

        assertThat(b.transpose().transpose())
                .as("transpose twice should return original")
                .isEqualTo(b);
    }

    @Test
    void reverseRows_isInvolution_reverseTwiceEqualsOriginal() {
        Board b = new Board(new int[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });

        assertThat(b.reverseRows().reverseRows())
                .as("reverseRows twice should return original")
                .isEqualTo(b);
    }

    @Test
    void transpose_worksOnNonTrivialGrid() {
        Board b = new Board(new int[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });

        assertThat(b.transpose().getGrid())
                .as("transpose result")
                .isDeepEqualTo(new int[][]{
                        {1, 4, 7},
                        {2, 5, 8},
                        {3, 6, 9}
                });
    }

    @Test
    void reverseRows_worksOnNonTrivialGrid() {
        Board b = new Board(new int[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });

        assertThat(b.reverseRows().getGrid())
                .as("reverseRows result")
                .isDeepEqualTo(new int[][]{
                        {3, 2, 1},
                        {6, 5, 4},
                        {9, 8, 7}
                });
    }

    // ---------- transformations ----------

    @Test
    void applyTransformation_matchesDefinitions() {
        Board b = new Board(new int[][]{
                {1, 2},
                {3, 4}
        });

        assertThat(b.applyTransformation(Move.LEFT))
                .as("LEFT transform")
                .isSameAs(b);

        assertThat(b.applyTransformation(Move.RIGHT))
                .as("RIGHT transform")
                .isEqualTo(b.reverseRows());

        assertThat(b.applyTransformation(Move.UP))
                .as("UP transform")
                .isEqualTo(b.transpose());

        assertThat(b.applyTransformation(Move.DOWN))
                .as("DOWN transform")
                .isEqualTo(b.transpose().reverseRows());
    }

    @Test
    void applyInverseTransformation_undoesTransformation_forAllMoves() {
        Board b = new Board(new int[][]{
                {0, 2, 0, 4},
                {8, 0, 0, 0},
                {0, 0, 16, 0},
                {0, 32, 0, 64}
        });

        for (Move m : Move.values()) {
            Board back = b.applyTransformation(m).applyInverseTransformation(m);

            assertThat(back)
                    .as("inverse should undo transform for %s", m)
                    .isEqualTo(b);
        }
    }

    @Test
    void transformationAndInverse_arePure_doNotMutateOriginal() {
        Board b = new Board(new int[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });

        int[][] before = b.getGrid();

        for (Move m : Move.values()) {
            b.applyTransformation(m);
            b.applyInverseTransformation(m);

            assertThat(b.getGrid())
                    .as("board must remain unchanged after transform calls (%s)", m)
                    .isDeepEqualTo(before);
        }
    }

    // ---------- equals / hashCode ----------

    @Test
    void equals_isReflexiveSymmetricTransitive_andHandlesNullAndOtherTypes() {
        Board x = new Board(new int[][]{
                {1, 0, 2},
                {0, 4, 0},
                {8, 0, 16}
        });
        Board y = new Board(new int[][]{
                {1, 0, 2},
                {0, 4, 0},
                {8, 0, 16}
        });
        Board z = new Board(new int[][]{
                {1, 0, 2},
                {0, 4, 0},
                {8, 0, 16}
        });

        assertThat(x).as("reflexive").isEqualTo(x);
        assertThat(x).as("symmetric").isEqualTo(y);
        assertThat(y).as("symmetric").isEqualTo(x);

        assertThat(x).as("transitive").isEqualTo(y);
        assertThat(y).as("transitive").isEqualTo(z);
        assertThat(x).as("transitive").isEqualTo(z);

        assertThat(x.equals(null)).as("null").isFalse();
        assertThat(x.equals("not a board")).as("other type").isFalse();
    }

    @Test
    void hashCode_equalBoardsHaveEqualHashCodes() {
        Board x = new Board(new int[][]{
                {1, 2},
                {3, 4}
        });
        Board y = new Board(new int[][]{
                {1, 2},
                {3, 4}
        });

        assertThat(x).isEqualTo(y);
        assertThat(x.hashCode()).isEqualTo(y.hashCode());
    }

    @Test
    void equals_detectsDifferentContents() {
        Board x = new Board(new int[][]{
                {1, 2},
                {3, 4}
        });
        Board y = new Board(new int[][]{
                {1, 2},
                {3, 5}
        });

        assertThat(x).as("different contents").isNotEqualTo(y);
    }
}
