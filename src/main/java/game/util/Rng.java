package game.util;

public final class Rng {
    private long state;

    private static final long MULT = 0x5DEECE66DL;
    private static final long ADD  = 0xBL;
    private static final long MASK = (1L << 48) - 1;

    /** Public: create RNG from an external seed */
    public Rng(long seed) {
        this.state = (seed ^ MULT) & MASK;
    }

    /** Private: create RNG from internal state directly */
    private Rng(long internalState, boolean rawState) {
        this.state = internalState & MASK;
    }

    public Rng copy() {
        return new Rng(this.state, true);
    }

    private int nextBits(int bits) {
        state = (state * MULT + ADD) & MASK;
        return (int) (state >>> (48 - bits));
    }

    public int nextInt(int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be > 0");
        return nextBits(31) % bound;
    }

    public double nextDouble() {
        long hi = ((long) nextBits(26)) << 27;
        long lo = nextBits(27);
        return (hi + lo) / (double) (1L << 53);
    }
}
