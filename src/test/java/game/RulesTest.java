package game;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class RulesTest {

    private final Rules rules = new BaseRules();

    @ParameterizedTest(name = "{0}")
    @MethodSource("leftCases")
    void leftMoveCases(String name, int[][] start, int[][] expected, int expectedScore) {
        assertMove(name, start, expected, expectedScore, Move.LEFT);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rightCases")
    void rightMoveCases(String name, int[][] start, int[][] expected, int expectedScore) {
        assertMove(name, start, expected, expectedScore, Move.RIGHT);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("upCases")
    void upMoveCases(String name, int[][] start, int[][] expected, int expectedScore) {
        assertMove(name, start, expected, expectedScore, Move.UP);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("downCases")
    void downMoveCases(String name, int[][] start, int[][] expected, int expectedScore) {
        assertMove(name, start, expected, expectedScore, Move.DOWN);
    }

    private void assertMove(String name, int[][] start, int[][] expected, int expectedScore, Move move) {
        MoveResult r = rules.makeMove(new Board(start), move);

        assertThat(r.board().getGrid())
                .as("board after " + move + " — " + name)
                .isDeepEqualTo(new Board(expected).getGrid());

        assertThat(r.scoreGained())
                .as("score after " + move + " — " + name)
                .isEqualTo(expectedScore);
    }

    static Stream<Arguments> leftCases() {
        return Stream.of(
                Arguments.of(
                        "one element shifts",
                        new int[][]{
                                {0, 0, 2, 0},
                                {0, 0, 2, 0},
                                {0, 0, 2, 0},
                                {0, 0, 2, 0}
                        },
                        new int[][]{
                                {2, 0, 0, 0},
                                {2, 0, 0, 0},
                                {2, 0, 0, 0},
                                {2, 0, 0, 0}
                        },
                        0
                ),
                Arguments.of(
                        "single merge",
                        new int[][]{
                                {0, 0, 2, 0},
                                {0, 2, 2, 0},
                                {0, 0, 2, 0},
                                {4, 0, 2, 0}
                        },
                        new int[][]{
                                {2, 0, 0, 0},
                                {4, 0, 0, 0},
                                {2, 0, 0, 0},
                                {4, 2, 0, 0}
                        },
                        4
                ),
                Arguments.of(
                        "double merge row",
                        new int[][]{
                                {2, 2, 2, 2},
                                {0, 2, 2, 0},
                                {0, 0, 2, 0},
                                {4, 0, 2, 0}
                        },
                        new int[][]{
                                {4, 4, 0, 0},
                                {4, 0, 0, 0},
                                {2, 0, 0, 0},
                                {4, 2, 0, 0}
                        },
                        12
                )
        );
    }

    static Stream<Arguments> rightCases() {
        return Stream.of(
                Arguments.of(
                        "one element shifts right",
                        new int[][]{
                                {0, 2, 0, 0},
                                {0, 0, 0, 4},
                                {8, 0, 0, 0},
                                {0, 0, 16, 0}
                        },
                        new int[][]{
                                {0, 0, 0, 2},
                                {0, 0, 0, 4},
                                {0, 0, 0, 8},
                                {0, 0, 0, 16}
                        },
                        0
                ),
                Arguments.of(
                        "single merge to the right",
                        new int[][]{
                                {2, 0, 2, 0},
                                {0, 4, 0, 4},
                                {0, 0, 0, 0},
                                {2, 2, 0, 0}
                        },
                        new int[][]{
                                {0, 0, 0, 4},
                                {0, 0, 0, 8},
                                {0, 0, 0, 0},
                                {0, 0, 0, 4}
                        },
                        16
                )
        );
    }

    static Stream<Arguments> upCases() {
        return Stream.of(
                Arguments.of(
                        "vertical shift up",
                        new int[][]{
                                {0, 0, 0, 0},
                                {2, 0, 0, 0},
                                {0, 4, 0, 0},
                                {0, 0, 8, 0}
                        },
                        new int[][]{
                                {2, 4, 8, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        0
                ),
                Arguments.of(
                        "single vertical merge up",
                        new int[][]{
                                {0, 2, 0, 0},
                                {0, 2, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {0, 4, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        4
                ),
                Arguments.of(
                        "double vertical merge in one column (2,2,2,2)",
                        new int[][]{
                                {2, 0, 0, 0},
                                {2, 0, 0, 0},
                                {2, 0, 0, 0},
                                {2, 0, 0, 0}
                        },
                        new int[][]{
                                {4, 0, 0, 0},
                                {4, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        8
                )
        );
    }

    static Stream<Arguments> downCases() {
        return Stream.of(
                Arguments.of(
                        "vertical shift down",
                        new int[][]{
                                {2, 4, 8, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {2, 4, 8, 0}
                        },
                        0
                ),
                Arguments.of(
                        "single vertical merge down",
                        new int[][]{
                                {0, 0, 0, 0},
                                {0, 2, 0, 0},
                                {0, 2, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 4, 0, 0}
                        },
                        4
                ),
                Arguments.of(
                        "two independent merges down in a column (2,2,4,4)",
                        new int[][]{
                                {2, 0, 0, 0},
                                {2, 0, 0, 0},
                                {4, 0, 0, 0},
                                {4, 0, 0, 0}
                        },
                        new int[][]{
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {4, 0, 0, 0},
                                {8, 0, 0, 0}
                        },
                        12
                )
        );
    }
}
