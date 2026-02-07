package game.core;

import game.util.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

class BoardTest {

    private static Coordinate coord(int r, int c) {
        return new Coordinate(r, c);
    }

    private static int[][] deepCopy(int[][] grid) {
        int[][] copy = new int[grid.length][];
        for (int i = 0; i < grid.length; i++) {
            copy[i] = Arrays.copyOf(grid[i], grid[i].length);
        }
        return copy;
    }

    private static void assertThrowsIndexing(Runnable r) {
        assertThatThrownBy(r::run)
                .satisfiesAnyOf(
                        ex -> assertThat(ex).isInstanceOf(IndexOutOfBoundsException.class),
                        ex -> assertThat(ex).isInstanceOf(ArrayIndexOutOfBoundsException.class),
                        ex -> assertThat(ex).isInstanceOf(IllegalArgumentException.class)
                );
    }

    @Nested
    @DisplayName("Constructors and factories")
    class ConstructorsAndFactories {

        @Test
        void noArgConstructor_creates4x4EmptyBoard() {
            Board b = new Board();
            assertThat(b.getDimension()).isEqualTo(4);
            assertThat(b.getMaxTile()).isZero();

            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    assertThat(b.get(r, c)).isZero();
                }
            }

            assertThat(b.getEmptyCells()).containsExactly(
                    0, 1, 2, 3,
                    4, 5, 6, 7,
                    8, 9, 10, 11,
                    12, 13, 14, 15
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 8})
        void dimensionConstructor_createsNxNEmptyBoard(int n) {
            Board b = new Board(n);
            assertThat(b.getDimension()).isEqualTo(n);
            assertThat(b.getMaxTile()).isZero();

            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    assertThat(b.get(r, c)).isZero();
                }
            }

            int[] expected = new int[n * n];
            for (int i = 0; i < n * n; i++) expected[i] = i;
            assertThat(b.getEmptyCells()).containsExactly(expected);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        void dimensionConstructor_rejectsNonPositive(int n) {
            assertThatThrownBy(() -> new Board(n))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void gridConstructor_rejectsNullGrid() {
            assertThatThrownBy(() -> new Board(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void gridConstructor_rejects0x0Grid() {
            assertThatThrownBy(() -> new Board(new int[0][0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void gridConstructor_rejectsNullRow() {
            int[][] g = new int[2][];
            g[0] = new int[]{1, 2};
            g[1] = null;

            assertThatThrownBy(() -> new Board(g))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void gridConstructor_rejectsNonSquareGrid_rectangular() {
            int[][] g = new int[][]{
                    {1, 2, 3},
                    {4, 5, 6}
            };
            assertThatThrownBy(() -> new Board(g))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void gridConstructor_rejectsNonSquareGrid_jagged() {
            int[][] g = new int[][]{
                    {1, 2},
                    {3}
            };
            assertThatThrownBy(() -> new Board(g))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void gridConstructor_defensivelyCopiesInputGrid() {
            int[][] input = new int[][]{
                    {1, 2},
                    {3, 4}
            };
            Board b = new Board(input);

            input[0][0] = 99;
            input[1][1] = 77;

            assertThat(b.getDimension()).isEqualTo(2);
            assertThat(b.get(0, 0)).isEqualTo(1);
            assertThat(b.get(1, 1)).isEqualTo(4);
        }

        @Test
        void wrapTrustedGrid_rejectsNullGrid() {
            assertThatThrownBy(() -> Board.wrapTrustedGrid(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void wrapTrustedGrid_rejects0x0Grid() {
            assertThatThrownBy(() -> Board.wrapTrustedGrid(new int[0][0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void wrapTrustedGrid_rejectsNullRow() {
            int[][] g = new int[2][];
            g[0] = new int[]{1, 2};
            g[1] = null;

            assertThatThrownBy(() -> Board.wrapTrustedGrid(g))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void wrapTrustedGrid_rejectsNonSquareGrid() {
            int[][] g = new int[][]{
                    {1, 2, 3},
                    {4, 5, 6}
            };
            assertThatThrownBy(() -> Board.wrapTrustedGrid(g))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void wrapTrustedGrid_doesNotDefensivelyCopy() {
            int[][] trusted = new int[][]{
                    {1, 2},
                    {3, 4}
            };
            Board b = Board.wrapTrustedGrid(trusted);

            assertThat(b.get(0, 0)).isEqualTo(1);
            trusted[0][0] = 999;
            assertThat(b.get(0, 0)).isEqualTo(999);
        }
    }

    @Nested
    @DisplayName("Accessors: get, getGrid, getMaxTile, getEmptyCells")
    class Accessors {

        @Test
        void get_returnsValuesFromGridConstructor() {
            int[][] g = new int[][]{
                    {0, 2, 0},
                    {4, 8, 16},
                    {0, 0, 32}
            };
            Board b = new Board(g);

            assertThat(b.getDimension()).isEqualTo(3);
            assertThat(b.get(0, 0)).isEqualTo(0);
            assertThat(b.get(0, 1)).isEqualTo(2);
            assertThat(b.get(1, 2)).isEqualTo(16);
            assertThat(b.get(2, 2)).isEqualTo(32);
        }

        @Test
        void get_throwsForOutOfBoundsIndices() {
            Board b = new Board(2);

            assertThrowsIndexing(() -> b.get(-1, 0));
            assertThrowsIndexing(() -> b.get(0, -1));
            assertThrowsIndexing(() -> b.get(2, 0));
            assertThrowsIndexing(() -> b.get(0, 2));
        }

        @Test
        void getGrid_returnsDefensiveCopy() {
            int[][] g = new int[][]{
                    {1, 2},
                    {3, 4}
            };
            Board b = new Board(g);

            int[][] copy1 = b.getGrid();
            int[][] copy2 = b.getGrid();

            assertThat(copy1).isNotSameAs(copy2);
            assertThat(copy1[0]).isNotSameAs(copy2[0]);

            copy1[0][0] = 999;
            assertThat(b.get(0, 0)).isEqualTo(1);

            copy2[1][1] = 777;
            assertThat(b.get(1, 1)).isEqualTo(4);
        }

        @Test
        void getGrid_isDeepCopy_notSharingRows() {
            Board b = new Board(new int[][]{
                    {10, 11},
                    {12, 13}
            });

            int[][] copy = b.getGrid();
            copy[0] = new int[]{99, 99};

            assertThat(b.get(0, 0)).isEqualTo(10);
            assertThat(b.get(0, 1)).isEqualTo(11);
        }

        @Test
        void getMaxTile_emptyBoardIsZero() {
            Board b = new Board(4);
            assertThat(b.getMaxTile()).isZero();
        }

        @Test
        void getMaxTile_returnsMaximumValue() {
            Board b = new Board(new int[][]{
                    {0, 2, 0, 4},
                    {8, 16, 32, 64},
                    {128, 256, 512, 1024},
                    {2048, 0, 0, 0}
            });
            assertThat(b.getMaxTile()).isEqualTo(2048);
        }

        @Test
        void getMaxTile_handlesNegativeValues_ifPresent() {
            Board b = new Board(new int[][]{
                    {-5, -1},
                    {-9, -3}
            });
            assertThat(b.getMaxTile()).isEqualTo(-1);
        }

        @Test
        void getEmptyCells_returnsAllZeros_asFlattenedIndices_rowMajor() {
            Board b = new Board(new int[][]{
                    {0, 2, 0},
                    {4, 0, 6},
                    {0, 0, 9}
            });

            assertThat(b.getEmptyCells()).containsExactly(0, 2, 4, 6, 7);
        }

        @Test
        void getEmptyCells_returnsEmptyArrayWhenNoZeros() {
            Board b = new Board(new int[][]{
                    {1, 2},
                    {3, 4}
            });
            assertThat(b.getEmptyCells()).isEmpty();
        }

        @Test
        void getEmptyCells_isStableAcrossCalls_andNotAffectedByCallerMutation() {
            Board b = new Board(new int[][]{
                    {0, 1},
                    {0, 2}
            });

            int[] e1 = b.getEmptyCells();
            int[] e2 = b.getEmptyCells();

            assertThat(e1).containsExactly(0, 2);
            assertThat(e2).containsExactly(0, 2);

            e1[0] = 999;

            int[] e3 = b.getEmptyCells();
            assertThat(e3).containsExactly(0, 2);
        }
    }

    @Nested
    @DisplayName("placeTile: immutability and coordinate semantics")
    class PlaceTile {

        @Test
        void placeTile_setsValueAtCoordinate_andReturnsNewBoard() {
            Board original = new Board(4);

            Board b2 = original.placeTile(coord(1, 2), 8);

            assertThat(b2).isNotSameAs(original);
            assertThat(original.get(1, 2)).isZero();
            assertThat(b2.get(1, 2)).isEqualTo(8);
            assertThat(original.getMaxTile()).isZero();
            assertThat(b2.getMaxTile()).isEqualTo(8);
        }

        @Test
        void placeTile_canPlaceZero_makingCellEmptyAgain() {
            Board b1 = new Board(2).placeTile(coord(0, 0), 4);
            assertThat(b1.getEmptyCells()).doesNotContain(0);

            Board b2 = b1.placeTile(coord(0, 0), 0);
            assertThat(b2.get(0, 0)).isZero();
            assertThat(b2.getEmptyCells()).contains(0);
        }

        @Test
        void placeTile_overwritesExistingValue() {
            Board b1 = new Board(2).placeTile(coord(0, 1), 2);
            Board b2 = b1.placeTile(coord(0, 1), 16);

            assertThat(b1.get(0, 1)).isEqualTo(2);
            assertThat(b2.get(0, 1)).isEqualTo(16);
        }

        @Test
        void placeTile_allOtherCellsRemainUnchanged() {
            int[][] g = new int[][]{
                    {0, 1, 2},
                    {3, 4, 5},
                    {6, 7, 8}
            };
            Board b1 = new Board(g);
            Board b2 = b1.placeTile(coord(0, 0), 9);

            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (r == 0 && c == 0) continue;
                    assertThat(b2.get(r, c)).as("cell (%s,%s)", r, c).isEqualTo(b1.get(r, c));
                }
            }
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void placeTile_throwsForNullCoordinate() {
            Board b = new Board(2);
            assertThatThrownBy(() -> b.placeTile(null, 2))
                    .satisfiesAnyOf(
                            ex -> assertThat(ex).isInstanceOf(NullPointerException.class),
                            ex -> assertThat(ex).isInstanceOf(IllegalArgumentException.class)
                    );
        }

        @Test
        void placeTile_throwsForOutOfBoundsCoordinate() {
            Board b = new Board(2);

            assertThrowsIndexing(() -> b.placeTile(coord(-1, 0), 2));
            assertThrowsIndexing(() -> b.placeTile(coord(0, -1), 2));
            assertThrowsIndexing(() -> b.placeTile(coord(2, 0), 2));
            assertThrowsIndexing(() -> b.placeTile(coord(0, 2), 2));
        }

        @Test
        void placeTile_worksWithArbitraryIntValues() {
            Board b = new Board(2)
                    .placeTile(coord(0, 0), Integer.MIN_VALUE)
                    .placeTile(coord(1, 1), Integer.MAX_VALUE);

            assertThat(b.get(0, 0)).isEqualTo(Integer.MIN_VALUE);
            assertThat(b.get(1, 1)).isEqualTo(Integer.MAX_VALUE);
            assertThat(b.getMaxTile()).isEqualTo(Integer.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("equals / hashCode: deep equality and contract")
    class EqualityAndHashing {

        @Test
        void equals_isReflexive_symmetric_transitive_andNullSafe() {
            int[][] g = new int[][]{
                    {0, 2},
                    {4, 8}
            };
            Board a = new Board(g);
            Board b = new Board(deepCopy(g));
            Board c = new Board(deepCopy(g));

            assertThat(a).isEqualTo(a);

            assertThat(a).isEqualTo(b);
            assertThat(b).isEqualTo(a);

            assertThat(a).isEqualTo(b);
            assertThat(b).isEqualTo(c);
            assertThat(a).isEqualTo(c);

            assertThat(a).isNotEqualTo(null);
            assertThat(a).isNotEqualTo("not a board");
        }

        @Test
        void equals_detectsDifferentDimensions() {
            Board b2 = new Board(2);
            Board b3 = new Board(3);
            assertThat(b2).isNotEqualTo(b3);
        }

        @Test
        void equals_detectsDifferentContents() {
            Board a = new Board(new int[][]{
                    {1, 2},
                    {3, 4}
            });
            Board b = new Board(new int[][]{
                    {1, 2},
                    {3, 5}
            });
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        void equals_comparesDeepGrid_notArrayIdentity() {
            int[][] g1 = new int[][]{
                    {1, 2},
                    {3, 4}
            };
            int[][] g2 = new int[][]{
                    {1, 2},
                    {3, 4}
            };

            Board a = new Board(g1);
            Board b = new Board(g2);

            assertThat(a).isEqualTo(b);
        }

        @Test
        void hashCode_isConsistentWithEquals_andStableAcrossCalls() {
            Board a1 = new Board(new int[][]{
                    {0, 2},
                    {4, 8}
            });
            Board a2 = new Board(new int[][]{
                    {0, 2},
                    {4, 8}
            });
            Board b = new Board(new int[][]{
                    {0, 2},
                    {4, 16}
            });

            assertThat(a1).isEqualTo(a2);
            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());

            assertThat(a1).isNotEqualTo(b);

            int h1 = a1.hashCode();
            int h2 = a1.hashCode();
            int h3 = a1.hashCode();
            assertThat(h1).isEqualTo(h2).isEqualTo(h3);
        }

        @Test
        void immutability_plusEquality_placeTileCreatesDifferentBoard() {
            Board a = new Board(2);
            Board b = a.placeTile(coord(0, 0), 2);
            Board c = a.placeTile(coord(0, 0), 2);

            assertThat(a).isNotEqualTo(b);
            assertThat(b).isEqualTo(c);
            assertThat(b.hashCode()).isEqualTo(c.hashCode());
        }
    }
}
