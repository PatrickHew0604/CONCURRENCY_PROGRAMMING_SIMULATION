public enum DrinksType {
    CAPPUCCINO, ESPRESSO, JUICE;

    public double getPrice() {
        return switch (this) {
            case CAPPUCCINO -> 9.0;
            case ESPRESSO -> 6.0;
            case JUICE -> 7.0;
            default -> throw new IllegalStateException("Unexpected value");
        };

    }

    @Override
    public String toString() {
        return switch (this) {
            case CAPPUCCINO -> "Cappuccino";
            case ESPRESSO -> "Espresso";
            case JUICE -> "Juice";
            default -> throw new IllegalStateException("Unexpected value");
        };
    }
}
