package game;

import game.core.Board;
import game.core.Move;
import game.core.MoveResult;
import game.rules.ClassicRules2048;
import game.rules.Rules;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassicRules2048Test {

    private final Rules rules = new ClassicRules2048();

    // -------- LEFT move semantics --------

    @ParameterizedTest(name = "LEFT::{0}")
    @MethodSource("leftMoveCases")
    void leftMove_updatesBoardAndScoreCorrectly(
            String description,
            int[][] initialGrid,
            int[][] expectedGrid,
            int expectedScore
    ) {
        MoveResult result = rules.makeMove(new Board(initialGrid), Move.LEFT);

        assertThat(result.board().getGrid())
                .as("FAIL_BOARD_LEFT: " + description)
                .isDeepEqualTo(expectedGrid);

        assertThat(result.scoreGained())
                .as("FAIL_SCORE_LEFT: " + description)
                .isEqualTo(expectedScore);
    }

    static Stream<Arguments> leftMoveCases() {
        return Stream.of(
                // compress-only (movableIndex must not advance on empties)
                Arguments.of(
                        "compress only (no merges)",
                        new int[][]{
                                {0, 2, 0, 4},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {2, 4, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        0
                ),

                // core merge correctness (gaps)
                Arguments.of(
                        "gap merge 2..2 -> 4",
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

                // must prevent double merge
                Arguments.of(
                        "no double merge: 2 2 2 2 -> 4 4",
                        new int[][]{
                                {2, 2, 2, 2},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {4, 4, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        8
                ),
                Arguments.of(
                        "no double merge with gaps: 2 0 2 2 -> 4 2",
                        new int[][]{
                                {2, 0, 2, 2},
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
                        "no double merge high: 8 8 8 0 -> 16 8",
                        new int[][]{
                                {8, 8, 8, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {16, 8, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        16
                ),

                // branch correctness
                Arguments.of(
                        "non-merge pushes to next slot: 2 4 2 0 stays 2 4 2",
                        new int[][]{
                                {2, 4, 2, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {2, 4, 2, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        0
                ),
                Arguments.of(
                        "non-merge after merge uses next slot: 2 2 4 2 -> 4 4 2",
                        new int[][]{
                                {2, 2, 4, 2},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {4, 4, 2, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        4
                ),

                // multi-row score accumulation
                Arguments.of(
                        "multi-row scoring sums all merged tile values",
                        new int[][]{
                                {2, 2, 2, 2},
                                {4, 0, 4, 0},
                                {0, 8, 0, 8},
                                {16, 16, 32, 32}
                        },
                        new int[][]{
                                {4, 4, 0, 0},
                                {8, 0, 0, 0},
                                {16, 0, 0, 0},
                                {32, 64, 0, 0}
                        },
                        8 + 8 + 16 + (32 + 64)
                ),

                // “merged tile cannot merge again” but with a pattern that catches wrong movableIndex handling
                Arguments.of(
                        "merge then block: 4 4 4 4 -> 8 8 (not 16)",
                        new int[][]{
                                {4, 4, 4, 4},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {8, 8, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        16
                ),

                // stable/no-change
                Arguments.of(
                        "no effect (already left-packed, no merges)",
                        new int[][]{
                                {2, 4, 8, 16},
                                {32, 64, 128, 256},
                                {2, 4, 8, 16},
                                {32, 64, 128, 256}
                        },
                        new int[][]{
                                {2, 4, 8, 16},
                                {32, 64, 128, 256},
                                {2, 4, 8, 16},
                                {32, 64, 128, 256}
                        },
                        0
                )
        );
    }

    // -------- Minimal sanity for transformations --------

    @ParameterizedTest(name = "SANITY::{0}")
    @MethodSource("nonLeftSanityCases")
    void otherMoves_sanity_updatesBoardAndScoreCorrectly(
            String description,
            Move move,
            int[][] initialGrid,
            int[][] expectedGrid,
            int expectedScore
    ) {
        MoveResult result = rules.makeMove(new Board(initialGrid), move);

        assertThat(result.board().getGrid())
                .as("FAIL_BOARD_" + move + "_SANITY: " + description)
                .isDeepEqualTo(expectedGrid);

        assertThat(result.scoreGained())
                .as("FAIL_SCORE_" + move + "_SANITY: " + description)
                .isEqualTo(expectedScore);
    }

    static Stream<Arguments> nonLeftSanityCases() {
        return Stream.of(
                Arguments.of(
                        "RIGHT simple merge through gap",
                        Move.RIGHT,
                        new int[][]{
                                {2, 0, 0, 2},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        new int[][]{
                                {0, 0, 0, 4},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0}
                        },
                        4
                ),
                Arguments.of(
                        "UP simple merge",
                        Move.UP,
                        new int[][]{
                                {2, 0, 0, 0},
                                {2, 0, 0, 0},
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
                        "DOWN simple merge",
                        Move.DOWN,
                        new int[][]{
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {2, 0, 0, 0},
                                {2, 0, 0, 0}
                        },
                        new int[][]{
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {0, 0, 0, 0},
                                {4, 0, 0, 0}
                        },
                        4
                )
        );
    }

    // -------- getLegalMoves + isGameOver --------
    @Test
    void legalMoves_emptyOnFullCheckerboard_andGameOverTrue() {
        Board board = new Board(new int[][]{
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 2}
        });

        EnumSet<Move> legal = rules.getLegalMoves(board);

        assertThat(legal)
                .as("FAIL_LEGAL_MOVES_EXPECT_EMPTY: full board w/ no adjacent equals")
                .isEmpty();

        assertThat(rules.isGameOver(board))
                .as("FAIL_GAMEOVER_EXPECT_TRUE: must be game over when no legal moves")
                .isTrue();
    }

    @Test
    void legalMoves_nonEmptyWhenMergeExists_andGameOverFalse() {
        Board board = new Board(new int[][]{
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 4}
        });

        EnumSet<Move> legal = rules.getLegalMoves(board);

        assertThat(legal)
                .as("FAIL_LEGAL_MOVES_EXPECT_NON_EMPTY: full board but merge exists")
                .isNotEmpty();

        assertThat(rules.isGameOver(board))
                .as("FAIL_GAMEOVER_EXPECT_FALSE: merge exists so not game over")
                .isFalse();
    }

    @Test
    void legalMoves_containsLeftWhenLeftChangesBoard() {
        Board board = new Board(new int[][]{
                {0, 2, 0, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        EnumSet<Move> legal = rules.getLegalMoves(board);

        assertThat(legal)
                .as("FAIL_LEGAL_MOVES_MISSING_LEFT: LEFT should be legal when it changes state")
                .contains(Move.LEFT);
    }
}
