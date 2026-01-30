package game.core;

public enum Move {
    LEFT(1),
    RIGHT(2),
    UP(3),
    DOWN(4);

    private final int code;
    private static final Move[] VALUES = values();

    Move(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static Move random() {
        return VALUES[java.util.concurrent.ThreadLocalRandom
                .current()
                .nextInt(VALUES.length)];
    }
}
