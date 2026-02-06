package game.core;

public enum Move {
    LEFT(1),
    RIGHT(2),
    UP(3),
    DOWN(4);

    private final int code;
    private Move opposite;

    static {
        LEFT.opposite = RIGHT;
        RIGHT.opposite = LEFT;
        UP.opposite = DOWN;
        DOWN.opposite = UP;
    }

    Move(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public Move opposite() {
        return opposite;
    }
}
