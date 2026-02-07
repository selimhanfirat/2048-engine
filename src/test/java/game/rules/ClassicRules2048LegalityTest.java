package game.rules;

import game.core.Board;
import game.core.Move;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static game.rules.RulesTestHelper.*;
import static org.assertj.core.api.Assertions.*;

class ClassicRules2048LegalityTest {

    private final ClassicRules2048 rules = new ClassicRules2048();

    // ============================================================
    // BASIC LEGALITY
    // ============================================================

    @Test
    void empty_board_all_moves_illegal() {
        Board empty = boardOf(new int[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        });

        for (Move move : Move.values()) {
            assertThat(rules.canMove(empty, move))
                    .as("empty board should not allow move " + move)
                    .isFalse();
        }

        assertThat(rules.getLegalMoves(empty)).isEmpty();
        assertThat(rules.isGameOver(empty)).isTrue();
    }

    @Test
    void single_tile_center_board_can_move_all_directions() {
        Board before = boardOf(new int[][]{
                {0, 0, 0},
                {0, 2, 0},
                {0, 0, 0}
        });

        assertThat(rules.getLegalMoves(before))
                .containsExactlyInAnyOrder(Move.LEFT, Move.RIGHT, Move.UP, Move.DOWN);

        assertThat(rules.isGameOver(before)).isFalse();
    }

    // ============================================================
    // DIRECTION-SPECIFIC LEGALITY
    // ============================================================

    @Test
    void tile_blocked_by_edge_cannot_move_further() {
        Board before = boardOf(new int[][]{
                {2, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        });

        assertThat(rules.canMove(before, Move.UP)).isFalse();
        assertThat(rules.canMove(before, Move.LEFT)).isFalse();

        assertThat(rules.canMove(before, Move.RIGHT)).isTrue();
        assertThat(rules.canMove(before, Move.DOWN)).isTrue();
    }

    @Test
    void merge_available_makes_move_legal_even_without_empty_space() {
        Board before = boardOf(new int[][]{
                {2, 2, 4},
                {8, 16, 32},
                {64, 128, 256}
        });

        assertThat(rules.canMove(before, Move.LEFT)).isTrue();
        assertThat(rules.canMove(before, Move.RIGHT)).isTrue();

        assertThat(rules.canMove(before, Move.UP)).isFalse();
        assertThat(rules.canMove(before, Move.DOWN)).isFalse();
    }

    // ============================================================
    // CRAMPED MIDGAME LEGALITY
    // ============================================================

    @Test
    void cramped_board_only_one_direction_is_legal() {
        Board before = boardOf(new int[][]{
                {2, 4, 8, 16},
                {32, 64, 128, 256},
                {512, 1024, 1024, 2},
                {4, 8, 16, 32}
        });

        assertThat(rules.getLegalMoves(before))
                .containsExactlyInAnyOrder(Move.LEFT, Move.RIGHT);

        assertThat(rules.canMove(before, Move.LEFT)).isTrue();
        assertThat(rules.canMove(before, Move.RIGHT)).isTrue();
        assertThat(rules.canMove(before, Move.UP)).isFalse();
        assertThat(rules.canMove(before, Move.DOWN)).isFalse();
    }

    // ============================================================
    // GAME OVER
    // ============================================================

    @Test
    void fully_blocked_board_is_game_over() {
        Board before = boardOf(new int[][]{
                {2, 4, 8, 16},
                {32, 64, 128, 256},
                {512, 1024, 2, 4},
                {8, 16, 32, 64}
        });

        assertThat(rules.getLegalMoves(before)).isEmpty();
        assertThat(rules.isGameOver(before)).isTrue();

        for (Move move : Move.values()) {
            assertThat(rules.canMove(before, move)).isFalse();
        }
    }

    @Test
    void one_escape_merge_means_not_game_over() {
        Board before = boardOf(new int[][]{
                {2, 4, 8, 16},
                {32, 64, 128, 256},
                {512, 1024, 512, 256},
                {2, 4, 8, 8}
        });

        assertThat(rules.isGameOver(before)).isFalse();
        assertThat(rules.getLegalMoves(before)).contains(Move.RIGHT);
    }

    // ============================================================
    // COHERENCE BETWEEN APIS
    // ============================================================

    @Test
    void getLegalMoves_equals_filtering_canMove() {
        Board before = boardOf(new int[][]{
                {2, 0, 2},
                {4, 4, 8},
                {16, 32, 64}
        });

        EnumSet<Move> expected = EnumSet.noneOf(Move.class);
        for (Move m : Move.values()) {
            if (rules.canMove(before, m)) {
                expected.add(m);
            }
        }

        assertThat(rules.getLegalMoves(before)).isEqualTo(expected);
    }

    // ============================================================
    // SMALL BOARDS
    // ============================================================

    @Test
    void one_by_one_board_is_always_game_over() {
        Board one = boardOf(new int[][]{{2}});

        assertThat(rules.getLegalMoves(one)).isEmpty();
        assertThat(rules.isGameOver(one)).isTrue();

        for (Move move : Move.values()) {
            assertThat(rules.canMove(one, move)).isFalse();
        }
    }

    @Test
    void two_by_two_board_legality() {
        Board before = boardOf(new int[][]{
                {2, 2},
                {4, 8}
        });

        assertThat(rules.getLegalMoves(before))
                .containsExactlyInAnyOrder(Move.LEFT, Move.RIGHT);

        assertThat(rules.canMove(before, Move.UP)).isFalse();
        assertThat(rules.canMove(before, Move.DOWN)).isFalse();
    }
}
