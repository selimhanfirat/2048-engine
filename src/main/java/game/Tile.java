package game;

public record Tile(int value) {

    public boolean canMerge(Tile other) {
        if (this.value == 0 || other.value == 0) return false;
        return value == other.value;
    }

    public Tile merge(Tile other) {
        if (canMerge(other)) {
            return new Tile(value + other.value);
        }
        throw new IllegalArgumentException("Cannot merge tiles of different types");
    }

    public boolean isEmpty() {
        return value == 0;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tile tile)) return false;

        return value() == tile.value();
    }

    @Override
    public int hashCode() {
        return value();
    }
}