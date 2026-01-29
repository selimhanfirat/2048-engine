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
        MoveResult r = rules.makeMove(new Board(start), Move.LEFT);

        assertThat(r.board().getGrid())
                .isDeepEqualTo(new Board(expected).getGrid());

        assertThat(r.scoreGained()).isEqualTo(expectedScore);
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
}
