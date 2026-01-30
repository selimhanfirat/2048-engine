package game;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class RulesTest {

    private final Rules rules = new BaseRules();

    // 1) Left only tests

    @ParameterizedTest(name = "{0}")
    @MethodSource("leftCases")
    void leftMoveCases(String name, int[][] start, int[][] expected, int expectedScore) {
        MoveResult r = rules.makeMove(new Board(start), Move.LEFT);

        assertThat(r.board().getGrid())
                .as("board after LEFT — " + name)
                .isDeepEqualTo(new Board(expected).getGrid());

        assertThat(r.scoreGained())
                .as("score after LEFT — " + name)
                .isEqualTo(expectedScore);
    }

    // 2) Equivalence tests under rotation and transposition
    @ParameterizedTest(name = "equivalence {1} matches transformed LEFT — {0}")
    @MethodSource("equivalenceCases")
    void otherMovesAreEquivalentToTransformedLeft(String name, Move move, int[][] start) {
        Board b = new Board(start);

        // actual result from engine
        MoveResult actual = rules.makeMove(b, move);

        // expected via: transform -> LEFT -> inverse transform
        Board transformed = b.applyTransformation(move, false);
        MoveResult leftOnTransformed = rules.makeMove(transformed, Move.LEFT);
        Board expectedBoard = leftOnTransformed.board().applyTransformation(move, true);
        int expectedScore = leftOnTransformed.scoreGained();

        assertThat(actual.board().getGrid())
                .as("board equivalence for " + move + " — " + name)
                .isDeepEqualTo(expectedBoard.getGrid());

        assertThat(actual.scoreGained())
                .as("score equivalence for " + move + " — " + name)
                .isEqualTo(expectedScore);
    }

    // ----------- LEFT semantic cases (annoying ones here) -----------
    static Stream<Arguments> leftCases() {
        return Stream.of(
                Arguments.of(
                        "gap merge (2,0,0,2) -> 4",
                        new int[][]{
                                {2, 0, 0, 2},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {4, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        4
                ),
                Arguments.of(
                        "three in a row merges once (2,2,2,0) -> (4,2,0,0)",
                        new int[][]{
                                {2, 2, 2, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {4, 2, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        4
                ),
                Arguments.of(
                        "two merges in one row (2,2,4,4) -> (4,8,0,0)",
                        new int[][]{
                                {2, 2, 4, 4},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {4, 8, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        12
                ),
                Arguments.of(
                        "merged tile cannot merge again (4,4,4,0) -> (8,4,0,0)",
                        new int[][]{
                                {4, 4, 4, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {8, 4, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        8
                ),
                Arguments.of(
                        "no-op left (already packed, no merges)",
                        new int[][]{
                                {2, 4, 8, 16},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {2, 4, 8, 16},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        0
                )
        );
    }

    // ----------- Equivalence test boards (only a few, but messy) -----------

    static Stream<Arguments> equivalenceCases() {
        return Stream.of(
                Arguments.of(
                        "messy board with gaps + merges",
                        Move.RIGHT,
                        new int[][]{
                                {2, 0, 2, 4},
                                {0, 4, 4, 0},
                                {2, 2, 0, 2},
                                {0, 8, 0, 8}
                        }
                ),
                Arguments.of(
                        "messy board with vertical patterns",
                        Move.UP,
                        new int[][]{
                                {2, 2, 0, 0},
                                {2, 0, 2, 0},
                                {0, 2, 2, 2},
                                {2, 0, 0, 2}
                        }
                ),
                Arguments.of(
                        "messy board with packed row + merge row",
                        Move.DOWN,
                        new int[][]{
                                {2, 4, 8, 16},
                                {0, 0, 0, 0},
                                {2, 2, 4, 4},
                                {4, 0, 4, 0}
                        }
                )
        );
    }
}
