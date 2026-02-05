package game.rules;

import game.core.Board;
import game.core.Move;
import game.core.MoveResult;
import org.junit.jupiter.api.Test;

import static game.rules.RulesTestHelper.*;
import static org.assertj.core.api.Assertions.*;

class ClassicRules2048ExamplesTest {

    private final ClassicRules2048 rules = new ClassicRules2048();

    // ============================================================
    // EARLY GAME – sparse boards
    // ============================================================

    @Test
    void early_game_slide_left_no_merge_no_score() {
        Board before = boardOf(new int[][]{
                {0, 2, 0, 0},
                {0, 0, 2, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        MoveResult result = rules.makeMove(before, Move.LEFT);

        assertThat(toMatrix(result.board())).isEqualTo(new int[][]{
                {2, 0, 0, 0},
                {2, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });
        assertThat(result.scoreGained()).isZero();
    }

    @Test
    void early_game_slide_right_no_merge_no_score() {
        Board before = boardOf(new int[][]{
                {0, 0, 2, 0},
                {0, 2, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        MoveResult result = rules.makeMove(before, Move.RIGHT);

        assertThat(toMatrix(result.board())).isEqualTo(new int[][]{
                {0, 0, 0, 2},
                {0, 0, 0, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });
        assertThat(result.scoreGained()).isZero();
    }

    // ============================================================
    // MIDGAME – cramped boards (no empty cells, merges exist)
    // ============================================================

    @Test
    void midgame_cramped_board_horizontal_merge_only() {
        // No empty cells, but one horizontal merge exists
        Board before = boardOf(new int[][]{
                {2, 4, 8, 16},
                {32, 64, 128, 256},
                {512, 1024, 1024, 2},
                {4, 8, 16, 32}
        });

        MoveResult result = rules.makeMove(before, Move.LEFT);

        assertThat(toMatrix(result.board())).isEqualTo(new int[][]{
                {2, 4, 8, 16},
                {32, 64, 128, 256},
                {512, 2048, 2, 0},
                {4, 8, 16, 32}
        });

        assertThat(result.scoreGained()).isEqualTo(2048);
    }

    @Test
    void midgame_cramped_board_vertical_merge_only() {
        Board before = boardOf(new int[][]{
                {2, 4, 8, 16},
                {32, 64, 8, 256},
                {512, 1024, 8, 2},
                {4, 8, 16, 32}
        });

        MoveResult result = rules.makeMove(before, Move.UP);

        assertThat(toMatrix(result.board())).isEqualTo(new int[][]{
                {2, 4, 16, 16},
                {32, 64, 8, 256},
                {512, 1024, 16, 2},
                {4, 8, 0, 32}
        });

        assertThat(result.scoreGained()).isEqualTo(16);
    }

    // ============================================================
    // LATE GAME – near death (exactly one escape merge)
    // ============================================================

    @Test
    void late_game_single_escape_merge_saves_game() {
        Board before = boardOf(new int[][]{
                {2, 4, 8, 16},
                {32, 64, 128, 256},
                {512, 1024, 512, 256},
                {2, 4, 8, 8}
        });

        MoveResult result = rules.makeMove(before, Move.RIGHT);

        assertThat(toMatrix(result.board())).isEqualTo(new int[][]{
                {2, 4, 8, 16},
                {32, 64, 128, 256},
                {512, 1024, 512, 256},
                {0, 2, 4, 16}
        });

        assertThat(result.scoreGained()).isEqualTo(16);
    }

    // ============================================================
    // TRUE DEAD BOARD – no empty space, no merges
    // ============================================================

    @Test
    void late_game_fully_blocked_board_is_no_op_for_all_moves() {
        Board before = boardOf(new int[][]{
                {2, 4, 8, 16},
                {32, 64, 128, 256},
                {512, 1024, 2, 4},
                {8, 16, 32, 64}
        });

        int[][] snapshot = toMatrix(before);

        for (Move move : Move.values()) {
            MoveResult result = rules.makeMove(before, move);

            assertThat(toMatrix(result.board()))
                    .as("board must not change for move " + move)
                    .isEqualTo(snapshot);

            assertThat(result.scoreGained())
                    .as("score must be zero for move " + move)
                    .isZero();
        }
    }

    // ============================================================
    // BASIC MERGES + SCORING (HORIZONTAL)
    // ============================================================

    @Test
    void horizontal_merge_left_scores_correctly() {
        Board before = boardOf(new int[][]{
                {2, 0, 2, 0},
                {4, 4, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        MoveResult result = rules.makeMove(before, Move.LEFT);

        assertThat(toMatrix(result.board())).isEqualTo(new int[][]{
                {4, 0, 0, 0},
                {8, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        assertThat(result.scoreGained()).isEqualTo(4 + 8);
    }

    @Test
    void horizontal_merge_right_scores_correctly() {
        Board before = boardOf(new int[][]{
                {0, 2, 0, 2},
                {0, 0, 4, 4},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        MoveResult result = rules.makeMove(before, Move.RIGHT);

        assertThat(toMatrix(result.board())).isEqualTo(new int[][]{
                {0, 0, 0, 4},
                {0, 0, 0, 8},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        assertThat(result.scoreGained()).isEqualTo(4 + 8);
    }

    // ============================================================
    // BASIC MERGES + SCORING (VERTICAL)
    // ============================================================

    @Test
    void vertical_merge_up_scores_correctly() {
        Board before = boardOf(new int[][]{
                {2, 0, 4, 0},
                {2, 0, 4, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        MoveResult result = rules.makeMove(before, Move.UP);

        assertThat(toMatrix(result.board())).isEqualTo(new int[][]{
                {4, 0, 8, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        assertThat(result.scoreGained()).isEqualTo(4 + 8);
    }

    @Test
    void vertical_merge_down_scores_correctly() {
        Board before = boardOf(new int[][]{
                {0, 0, 0, 0},
                {2, 0, 4, 0},
                {2, 0, 4, 0},
                {0, 0, 0, 0}
        });

        MoveResult result = rules.makeMove(before, Move.DOWN);

        assertThat(toMatrix(result.board())).isEqualTo(new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {4, 0, 8, 0}
        });

        assertThat(result.scoreGained()).isEqualTo(4 + 8);
    }

    // ============================================================
    // CLASSIC RULES – no double merges
    // ============================================================

    @Test
    void no_double_merge_left() {
        // [2,2,2,2] LEFT => [4,4,0,0]
        Board before = boardOf(new int[][]{
                {2, 2, 2, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        MoveResult result = rules.makeMove(before, Move.LEFT);

        assertThat(toMatrix(result.board())).isEqualTo(new int[][]{
                {4, 4, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });

        assertThat(result.scoreGained()).isEqualTo(8);
    }

    @Test
    void no_double_merge_down() {
        // Column [2,2,2,2] DOWN => [0,0,4,4]
        Board before = boardOf(new int[][]{
                {2, 0, 0, 0},
                {2, 0, 0, 0},
                {2, 0, 0, 0},
                {2, 0, 0, 0}
        });

        MoveResult result = rules.makeMove(before, Move.DOWN);

        assertThat(toMatrix(result.board())).isEqualTo(new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {4, 0, 0, 0},
                {4, 0, 0, 0}
        });

        assertThat(result.scoreGained()).isEqualTo(8);
    }

    // ============================================================
    // ILLEGAL / NO-OP MOVES VIA makeMove
    // ============================================================

    @Test
    void illegal_move_does_not_change_board_or_score() {
        Board before = boardOf(new int[][]{
                {2, 4, 8, 16},
                {32, 64, 128, 256},
                {512, 1024, 2, 4},
                {8, 16, 32, 64}
        });

        MoveResult result = rules.makeMove(before, Move.LEFT);

        assertThat(toMatrix(result.board())).isEqualTo(toMatrix(before));
        assertThat(result.scoreGained()).isZero();
    }

    // ============================================================
    // IMMUTABILITY
    // ============================================================

    @Test
    void makeMove_never_mutates_original_board() {
        Board before = boardOf(new int[][]{
                {2, 0, 2, 0},
                {4, 4, 0, 0},
                {0, 0, 8, 0},
                {0, 0, 0, 0}
        });

        int[][] snapshot = toMatrix(before);

        for (Move m : Move.values()) {
            rules.makeMove(before, m);
        }

        assertThat(toMatrix(before)).isEqualTo(snapshot);
    }
}
