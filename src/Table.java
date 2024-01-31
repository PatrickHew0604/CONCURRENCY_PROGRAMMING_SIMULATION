public class Table {
    int tableNumber;
    Seat[] seats;

    public Table(int tableNumber) {
        this.tableNumber = tableNumber;
        this.seats = new Seat[]
                {new Seat(this, 1), new Seat(this, 2)};
    }

    @Override
    public String toString() {
        return "Table " + tableNumber;
    }

    public int getSuitableSeat(Customer customer) {
        if (seats[0].isEmpty() && seats[1].isEmpty()) {
            return 0;
        } else if ((seats[0].isEmpty() && !seats[1].isEmpty())) {
            if (customer.isWillingToShareTable && seats[1].occupant.isWillingToShareTable) {
                return 0;
            }
        } else if (!seats[0].isEmpty() && seats[1].isEmpty()) {
            if (customer.isWillingToShareTable && seats[0].occupant.isWillingToShareTable) {
                return 1;
            }
        }
        return -1;
    }
}
