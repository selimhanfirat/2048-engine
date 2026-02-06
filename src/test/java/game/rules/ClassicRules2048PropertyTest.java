package game.rules;

import game.core.Board;
import game.core.Move;
import net.jqwik.api.*;
import static org.assertj.core.api.Assertions.*;

public class ClassicRules2048PropertyTest {

    private final ClassicRules2048 rules = new ClassicRules2048();

    @Property(tries = 150)
    void emptyBoardHasNoLegalMoves(@ForAll("emptyBoards") Board board) {
        assertThat(rules.getLegalMoves(board)).isEmpty();
        assertThat(rules.isGameOver(board)).isTrue();
        assertThat(rules.isGameOver(board) == rules.getLegalMoves(board).isEmpty()).isTrue();
    }

    @Property(tries = 50)
    void moveLegalityIsSymmetric(@ForAll("randomBoards") Board board, @ForAll Move move) {
        if (rules.getLegalMoves(board).contains(move)) {
            assertThat(rules.getLegalMoves(board).contains(move.opposite())).isTrue();
        }

    }

    @Provide
    Arbitrary<Board> emptyBoards() {
        return Arbitraries.integers().between(1, 20).map(Board::new);
    }

    @Provide
    Arbitrary<Board> randomBoards() {
        return Arbitraries.integers()
                .between(1, 20)
                .flatMap(n ->
                        Arbitraries.integers()
                                .between(1, 17)
                                .map(x -> (Integer) (1 << x))
                                .array(int[].class)
                                .ofSize(n * n) // depends on n
                                .map(flat -> RulesTestHelper.toSquare(flat, n))   // 1D -> 2D
                                .map(Board::wrapTrustedGrid)
                );
    }

}
