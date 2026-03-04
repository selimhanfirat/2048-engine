package game.rules;

import game.core.Board;
import game.core.Move;
import game.core.MoveResult;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class ClassicRules2048Test {

    private final ClassicRules2048 rules = new ClassicRules2048();

    // ---- helpers ----

    private static Board b(int n, int... flat) {
        assertEquals(n * n, flat.length, "flat length must be n*n");
        return Board.wrapTrustedCells(n, flat);
    }

    private static void assertBoardEquals(Board actual, int n, int... expectedFlat) {
        assertEquals(n, actual.getDimension(), "dimension changed");
        assertEquals(n * n, expectedFlat.length);
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                int exp = expectedFlat[r * n + c];
                int got = actual.get(r, c);
                if (exp != got) {
                    fail("Cell mismatch at (" + r + "," + c + "): expected " + exp + " but got " + got);
                }
            }
        }
    }

    private static int sum(Board b) {
        int n = b.getDimension();
        int s = 0;
        for (int r = 0; r < n; r++) for (int c = 0; c < n; c++) s += b.get(r, c);
        return s;
    }

    // =========================================================
    // Mid-game boards: mixed tiles, multiple merges, multi-rows
    // =========================================================

    @Test
    void midGame_moveLeft_mixedRows_multipleMergesScoreCorrect() {
        Board start = b(4,
                2,  0,  2,  4,
                4,  4,  8,  8,
                0,  2,  2,  2,
                16, 0, 16, 16
        );

        MoveResult res = rules.makeMove(start, Move.LEFT);

        // Row1: 2 0 2 4 -> 4 4 0 0 (score +4)
        // Row2: 4 4 8 8 -> 8 16 0 0 (score +8+16)
        // Row3: 0 2 2 2 -> 4 2 0 0 (score +4)
        // Row4: 16 0 16 16 -> 32 16 0 0 (score +32)
        assertBoardEquals(res.board(), 4,
                4,  4,  0,  0,
                8,  16, 0,  0,
                4,  2,  0,  0,
                32, 16, 0,  0
        );
        assertEquals(4 + (8 + 16) + 4 + 32, res.scoreGained());
        assertEquals(sum(start), sum(res.board()), "tile sum should be conserved by merges");
    }

    @Test
    void midGame_moveRight_mixedRows_multipleMergesScoreCorrect() {
        Board start = b(4,
                2,  0,  2,  4,
                4,  4,  8,  8,
                0,  2,  2,  2,
                16, 0, 16, 16
        );

        MoveResult res = rules.makeMove(start, Move.RIGHT);

        // Mirror of LEFT expectations:
        // Row1 -> 0 0 4 4 (score +4)
        // Row2 -> 0 0 8 16 (score +8+16)
        // Row3 -> 0 0 2 4 (score +4)
        // Row4 -> 0 0 16 32 (score +32)
        assertBoardEquals(res.board(), 4,
                0,  0,  4,  4,
                0,  0,  8,  16,
                0,  0,  2,  4,
                0,  0,  16, 32
        );
        assertEquals(4 + (8 + 16) + 4 + 32, res.scoreGained());
        assertEquals(sum(start), sum(res.board()));
    }

    @Test
    void midGame_moveUp_columnsInteractCorrectly() {
        Board start = b(4,
                2,  4,  0,  2,
                2,  0,  4,  2,
                8,  4,  4,  0,
                8,  4,  0,  2
        );

        MoveResult res = rules.makeMove(start, Move.UP);

        // Consider columns:
        // Col0: 2,2,8,8 -> 4,16,0,0 (score +4 +16)
        // Col1: 4,0,4,4 -> 8,4,0,0 (score +8)
        // Col2: 0,4,4,0 -> 8,0,0,0 (score +8)
        // Col3: 2,2,0,2 -> 4,2,0,0 (score +4)
        assertBoardEquals(res.board(), 4,
                4,  8,  8,  4,
                16, 4,  0,  2,
                0,  0,  0,  0,
                0,  0,  0,  0
        );
        assertEquals((4 + 16) + 8 + 8 + 4, res.scoreGained());
        assertEquals(sum(start), sum(res.board()));
    }

    @Test
    void midGame_moveDown_columnsInteractCorrectly() {
        Board start = b(4,
                2,  4,  0,  2,
                2,  0,  4,  2,
                8,  4,  4,  0,
                8,  4,  0,  2
        );

        MoveResult res = rules.makeMove(start, Move.DOWN);

        // DOWN results are UP results pushed to bottom:
        assertBoardEquals(res.board(), 4,
                0,  0,  0,  0,
                0,  0,  0,  0,
                4,  4,  0,  2,
                16, 8,  8,  4
        );
        assertEquals((4 + 16) + 8 + 8 + 4, res.scoreGained());
        assertEquals(sum(start), sum(res.board()));
    }

    // =========================================================
    // “Breakable” merge patterns in different orientations
    // =========================================================

    @Test
    void merges_doNotCascadeWithinSameMove_vertical2222_upBecomes44() {
        Board start = b(4,
                2, 0, 0, 0,
                2, 0, 0, 0,
                2, 0, 0, 0,
                2, 0, 0, 0
        );

        MoveResult res = rules.makeMove(start, Move.UP);

        assertBoardEquals(res.board(), 4,
                4, 0, 0, 0,
                4, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
        );
        assertEquals(4 + 4, res.scoreGained());
    }

    @Test
    void merges_respectOrder_2_2_4_0_upOnColumn() {
        // Column 1: [2,2,4,0] moving UP => [4,4,0,0]
        Board start = b(4,
                0, 2, 0, 0,
                0, 2, 0, 0,
                0, 4, 0, 0,
                0, 0, 0, 0
        );

        MoveResult res = rules.makeMove(start, Move.UP);

        assertBoardEquals(res.board(), 4,
                0, 4, 0, 0,
                0, 4, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
        );
        assertEquals(4, res.scoreGained());
    }

    @Test
    void merges_handleGaps_thenMerge_2_0_2_0_downOnColumn() {
        // Column 2: [2,0,2,0] moving DOWN => [0,0,0,4]
        Board start = b(4,
                0, 0, 2, 0,
                0, 0, 0, 0,
                0, 0, 2, 0,
                0, 0, 0, 0
        );

        MoveResult res = rules.makeMove(start, Move.DOWN);

        assertBoardEquals(res.board(), 4,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 4, 0
        );
        assertEquals(4, res.scoreGained());
    }

    @Test
    void merges_twoSeparatePairs_inSameRow() {
        Board start = b(4,
                2, 2, 4, 4,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
        );

        MoveResult res = rules.makeMove(start, Move.RIGHT);

        assertBoardEquals(res.board(), 4,
                0, 0, 4, 8,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
        );
        assertEquals(4 + 8, res.scoreGained());
    }

    // =========================================================
    // End-game positions: “almost stuck” and “fully stuck”
    // =========================================================

    @Test
    void endGame_onlyOneLegalMove_dueToSingleMerge() {
        // Full board, only one merge exists: last row ends with [8,8].
        // Check which moves are legal: LEFT/RIGHT should be legal; UP/DOWN should be illegal.
        Board start = b(4,
                2,   4,   8,   16,
                32,  64,  128, 256,
                512, 1024,2,   4,
                16,  32,  8,   8
        );

        assertFalse(rules.canMove(start, Move.UP));
        assertFalse(rules.canMove(start, Move.DOWN));
        assertTrue(rules.canMove(start, Move.LEFT));
        assertTrue(rules.canMove(start, Move.RIGHT));

        EnumSet<Move> legal = rules.getLegalMoves(start);
        assertEquals(EnumSet.of(Move.LEFT, Move.RIGHT), legal);

        assertFalse(rules.isGameOver(start));
    }

    @Test
    void endGame_fullNoMoves_gameOver_true() {
        Board stuck = b(4,
                2, 4, 2, 4,
                4, 2, 4, 2,
                2, 4, 2, 4,
                4, 2, 4, 2
        );

        assertTrue(rules.getLegalMoves(stuck).isEmpty());
        assertTrue(rules.isGameOver(stuck));
        for (Move m : Move.values()) assertFalse(rules.canMove(stuck, m));
    }

    @Test
    void endGame_fullButHasVerticalMerge_notGameOver() {
        // Two adjacent equals vertically in column 0: (row1,row2) are both 8.
        Board start = b(4,
                2, 4, 2, 4,
                8, 2, 4, 2,
                8, 4, 2, 4,
                4, 2, 4, 2
        );

        assertTrue(rules.canMove(start, Move.UP));
        assertTrue(rules.canMove(start, Move.DOWN));
        assertFalse(rules.isGameOver(start));

        // Make the merging move and verify expected column result.
        MoveResult res = rules.makeMove(start, Move.UP);
        // Column 0: [2,8,8,4] -> [2,16,4,0]
        assertBoardEquals(res.board(), 4,
                2, 4, 2, 4,
                16,2, 4, 2,
                4, 4, 2, 4,
                0, 2, 4, 2
        );
        assertEquals(16, res.scoreGained());
    }

    @Test
    void endGame_almostStuck_hasShiftMove_dueToSingleZero() {
        // Only one empty cell, no merges available, but shifts are possible.
        Board start = b(4,
                2,   4,   8,   16,
                32,  64,  128, 256,
                512, 1024,2,   4,
                16,  32,  0,   8
        );

        assertTrue(rules.canMove(start, Move.LEFT));
        assertTrue(rules.canMove(start, Move.RIGHT));
        assertFalse(rules.canMove(start, Move.UP));
        assertTrue(rules.canMove(start, Move.DOWN));
        assertFalse(rules.isGameOver(start));

        MoveResult res = rules.makeMove(start, Move.LEFT);
        assertBoardEquals(res.board(), 4,
                2,   4,   8,   16,
                32,  64,  128, 256,
                512, 1024,2,   4,
                16,  32,  8,   0
        );
        assertEquals(0, res.scoreGained());
    }

    // =========================================================
    // Consistency checks between canMove and makeMove
    // =========================================================

    @Test
    void ifCanMoveTrue_thenMakeMoveChangesBoard() {
        Board start = b(4,
                0, 2, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
        );

        assertTrue(rules.canMove(start, Move.LEFT));
        Board after = rules.makeMove(start, Move.LEFT).board();
        assertNotEquals(start, after, "board should change when canMove is true");
    }

    @Test
    void ifCanMoveFalse_thenMakeMoveIsNoOp() {
        Board stuck = b(4,
                2, 4, 2, 4,
                4, 2, 4, 2,
                2, 4, 2, 4,
                4, 2, 4, 2
        );

        for (Move m : Move.values()) {
            assertFalse(rules.canMove(stuck, m));
            MoveResult res = rules.makeMove(stuck, m);
            assertEquals(stuck, res.board(), "should be no-op when cannot move: " + m);
            assertEquals(0, res.scoreGained(), "score should be 0 on no-op: " + m);
        }
    }

    @Test
    void makeMove_preservesDimension_andDoesNotIntroduceNewNonZerosBeyondPossible() {
        Board start = b(4,
                2, 0, 0, 0,
                0, 2, 0, 0,
                0, 0, 2, 0,
                0, 0, 0, 2
        );

        MoveResult res = rules.makeMove(start, Move.LEFT);
        assertEquals(4, res.board().getDimension());
        // Sum invariant is a clean “no weird tile creation” check.
        assertEquals(sum(start), sum(res.board()));
    }
}