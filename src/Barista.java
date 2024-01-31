import java.util.Random;

public class Barista extends Thread {
    String baristaID;
    static int ID = 1;
    final GoGoCoffeeCafe goGoCoffeeCafe;

    @Override
    public String toString() {
        return "Barista " + baristaID;
    }

    public Barista(GoGoCoffeeCafe goGoCoffeeCafe) {
        this.baristaID = "" + ID++;
        this.goGoCoffeeCafe = goGoCoffeeCafe;
    }

    @Override
    public void run() {
        System.out.println(this + " : Start working");
        while (!goGoCoffeeCafe.isCafeClose) {
            goGoCoffeeCafe.serveCustomer(this);
        }
        while (!goGoCoffeeCafe.customerOrderingQueue.isEmpty()) {
            System.out.println("+ Seems like there are some customers left in the cafe.");
            goGoCoffeeCafe.serveCustomer(this);
        }
        if (goGoCoffeeCafe.isCafeClose) {
            System.out.println(this + " : " +
                    (switch (new Random().nextInt(3)) {
                        case 0 -> "Cleaning cup.";
                        case 1 -> "Sweeping floor.";
                        case 2 -> "Wiping table.";
                        default -> throw new IllegalStateException("Unexpected value: " + new Random().nextInt(3));
                    }));
            try {
                goGoCoffeeCafe.baristaSemaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
