import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Customer extends Thread {
    String customerID;
    static int ID = 1;
    final GoGoCoffeeCafe goGoCoffeeCafe;
    final DrinksType orderDrinks;
    Semaphore controller;

    AtomicBoolean gettingServed = new AtomicBoolean(false);
    boolean isWillingToShareTable;
    int chosenTableIndex;
    int chosenSeatIndex;
    long enteringMillisec;

    public Customer(GoGoCoffeeCafe goGoCoffeeCafe) {
        this.customerID = "" + ID++;
        this.goGoCoffeeCafe = goGoCoffeeCafe;
        this.orderDrinks = getRandomDrinks();
        this.controller = new Semaphore(0);
        this.isWillingToShareTable = new Random().nextBoolean();
    }

    public void chooseSeat(int chosenTableIndex, int chosenSeatIndex) {
        this.chosenTableIndex = chosenTableIndex;
        this.chosenSeatIndex = chosenSeatIndex;
    }

    private DrinksType getRandomDrinks() {
        int randomOrdinal = new Random().nextInt(10);
        var allDrinks = DrinksType.values();
        return switch (randomOrdinal) {
            case 0, 1, 2, 3, 4, 5, 6 -> allDrinks[0];
            case 7, 8 -> allDrinks[1];
            default -> allDrinks[2];
        };
//        return allDrinks[randomOrdinal];
    }

    @Override
    public String toString() {
        return "Customer " + customerID;
    }

    private void payForDrinks() {
        System.out.println(this + " : Paying RM " + orderDrinks.getPrice() + " for the " + orderDrinks);
    }

    @Override
    public void run() {

        try {
            boolean isLeave = goGoCoffeeCafe.placeOrder(this);
            if (isLeave) return;
            long now = System.currentTimeMillis();
            while (!gettingServed.get()) {
                if (System.currentTimeMillis() - now > new Random().nextLong(2000,6000)) {
                    System.out.println(this + " : Getting tired standing and leave the cafe");
                    goGoCoffeeCafe.orderingQueueLock.lock();
                    try {
                        goGoCoffeeCafe.customerOrderingQueue.remove(this);
                    } finally {
                        goGoCoffeeCafe.orderingQueueLock.unlock();
                    }
                    return;
                }
            }
            controller.acquire();
            payForDrinks();
            goGoCoffeeCafe.seekForAvailableSeat(this);
            controller.acquire();
            goGoCoffeeCafe.consumeDrinks(this);
            goGoCoffeeCafe.leaveCafe(this);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}