package game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class RulesTest {

    private final Rules rules = new BaseRules();

    // -------- LEFT move semantics --------

    @ParameterizedTest(name = "{0}")
    @MethodSource("leftMoveCases")
    void leftMoveUpdatesBoardAndScoreCorrectly(
            String description,
            int[][] initialGrid,
            int[][] expectedGrid,
            int expectedScore
    ) {
        MoveResult result = rules.makeMove(new Board(initialGrid), Move.LEFT);

        assertThat(result.board().getGrid())
                .as("board after LEFT — " + description)
                .isDeepEqualTo(expectedGrid);

        assertThat(result.scoreGained())
                .as("score after LEFT — " + description)
                .isEqualTo(expectedScore);
    }

    static Stream<Arguments> leftMoveCases() {
        return Stream.of(
                Arguments.of(
                        "gap merge",
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
                        "three tiles merge once",
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
                        "two independent merges in one row",
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
                        "merged tile cannot merge again",
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
                        "left move with no effect",
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

    // -------- Game over detection --------

    @Test
    void gameIsOverWhenBoardIsFullAndNoMoveChangesState() {
        Board board = new Board(new int[][]{
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 2}
        });

        assertThat(rules.isGameOver(board)).isTrue();
    }

    @Test
    void gameIsNotOverWhenAtLeastOneMoveChangesState() {
        Board board = new Board(new int[][]{
                {2, 2, 4, 8},
                {4, 16, 32, 64},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        assertThat(rules.isGameOver(board)).isFalse();
    }
}
