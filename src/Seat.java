public class Seat {
    Table table;
    int seatNumber;
    Customer occupant;

    public Seat(Table table, int seatNumber) {
        this.table = table;
        this.seatNumber = seatNumber;
    }

    public boolean isEmpty() {
        return occupant == null;
    }

    public void occupy(Customer customer) {
        this.occupant = customer;
    }

    @Override
    public String toString() {
        return "Seat " + seatNumber;
    }
}