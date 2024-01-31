import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class GoGoCoffeeCafe {
    // Customer Waiting Time
    List<Long> customerWaitingTime = new ArrayList<>();

    // Counter
    AtomicInteger customerInCafeCount = new AtomicInteger(0);
    AtomicInteger soldCappuccino = new AtomicInteger(0);
    AtomicInteger soldEspresso = new AtomicInteger(0);
    AtomicInteger soldJuiceDrink = new AtomicInteger(0);
    AtomicInteger totalCustomers = new AtomicInteger(0);

    boolean isClose;
    List<Customer> customerOrderingQueue;
    List<Table> tableList;
    List<Customer> customerWaitingSeatQueue;

    // Locks
    ReentrantLock orderingQueueLock = new ReentrantLock();
    ReentrantLock espressoMachineLock = new ReentrantLock(true);
    ReentrantLock milkFrothingMachineLock = new ReentrantLock(true);
    ReentrantLock juiceTapLock = new ReentrantLock(true);
    ReentrantLock tableListLock = new ReentrantLock();
    ReentrantLock waitingSeatQueueLock = new ReentrantLock();

    Condition orderingQueueCondition = orderingQueueLock.newCondition();
    Condition waitingSeatQueueCondition = waitingSeatQueueLock.newCondition();

    Semaphore baristaSemaphore = new Semaphore(3);

    boolean isCafeClose;

    public GoGoCoffeeCafe() {
        this.customerOrderingQueue = new LinkedList<>();
        this.isClose = false;
        this.tableList = new ArrayList<>();
        this.customerWaitingSeatQueue = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            tableList.add(new Table(i));
        }
    }

    public void seekForAvailableSeat(Customer customer) {
        System.out.println(customer + " : Receive the " + customer.orderDrinks + " and seeking for a available seat");
        // Adding Waiting Time into the List
        customerWaitingTime.add(System.currentTimeMillis() - customer.enteringMillisec);

        boolean hasWillingToshareTableCustomerWaiting = false;

        waitingSeatQueueLock.lock();
        try {
            for (Customer queuingCustomer : customerWaitingSeatQueue) {
                if (queuingCustomer.isWillingToShareTable) {
                    hasWillingToshareTableCustomerWaiting = true;
                    break;
                }
            }
        } finally {
            waitingSeatQueueLock.unlock();
        }

        if (customerWaitingSeatQueue.isEmpty() || !hasWillingToshareTableCustomerWaiting) {
            tableListLock.lock();
            try {
                for (Table table : tableList) {
                    int availableSeatIndex = table.getSuitableSeat(customer);
                    if (availableSeatIndex != -1) {
                        System.out.println(customer + " : " + (customer.isWillingToShareTable ? "(Willing To Share Seat)" :
                                "(Not Willing To Share Seat)") +
                                " Occupied a seat at " + table + " " + table.seats[availableSeatIndex]);
                        table.seats[availableSeatIndex].occupy(customer);
                        customer.chooseSeat(table.tableNumber - 1, availableSeatIndex);
                        // Controller
                        customer.controller.release();
                        // Controller
                        return;
                    }
                }
            } finally {
                tableListLock.unlock();
            }
            queueForAvailableSeat(customer);
        } else
            queueForAvailableSeat(customer);
    }

    public void queueForAvailableSeat(Customer customer) {
        totalCustomers.incrementAndGet();
        waitingSeatQueueLock.lock();
        try {
            customerWaitingSeatQueue.add(customer);
            System.out.println(customer + (customer.isWillingToShareTable ? " : (Willing To Share Seat)" :
                    " : (Not Willing To Share Seat)") + " Queue to get a seat");
        } finally {
            waitingSeatQueueLock.unlock();
        }
    }

    public void assignCustomerToSuitableSeat() {
        boolean isCustomerChangePreferences = false;
        waitingSeatQueueLock.lock();
        try {
            while (customerWaitingSeatQueue.isEmpty()) {
                waitingSeatQueueCondition.await();
                if (isCafeClose) {
                    return;
                }
            }
            for (Customer nextQueingCustomer : customerWaitingSeatQueue) {
                for (Table table : tableList) {
                    int availableSeatIndex = table.getSuitableSeat(nextQueingCustomer);
                    // If customer found a suitable seat
                    if (availableSeatIndex != -1) {
                        System.out.println(nextQueingCustomer + " : " + (nextQueingCustomer.isWillingToShareTable ? "(Willing To Share Seat)" :
                                "(Not Willing To Share Seat)") +
                                " Occupied a seat at " + table + " " + table.seats[availableSeatIndex]);
                        table.seats[availableSeatIndex].occupy(nextQueingCustomer);
                        nextQueingCustomer.chooseSeat(table.tableNumber - 1, availableSeatIndex);
                        customerWaitingSeatQueue.remove(nextQueingCustomer);
                        // Controller
                        nextQueingCustomer.controller.release();
                        // Controller

                        return;
                    }
                }
                // Only for isWillingToShareTable = False
                if (!nextQueingCustomer.isWillingToShareTable) {
                    boolean randomBool = new Random().nextBoolean();
                    if (randomBool) {
                        System.out.println(nextQueingCustomer + " : (Not Willing To Share Seat)" + /*(nextQueingCustomer.isWillingToShareTable ? "(Willing To Share Seat)" :
                            "(Not Willing To Share Seat)") + */
                                " Still not be able to find a suitable seat, " +
                                "Thus he willing to share table now.");
                        nextQueingCustomer.isWillingToShareTable = true;
                        isCustomerChangePreferences = true;
                    } else {
                        System.out.println(nextQueingCustomer + " : (Not Willing To Share Seat) Still willing to wait for a table");
                    }
                }
            }
            if (!isCustomerChangePreferences) {
                waitingSeatQueueCondition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            waitingSeatQueueLock.unlock();
        }
    }

    public void leaveCafe(Customer customer) {
        tableListLock.lock();
        try {
            System.out.println(customer + " : Leaving the cafe from Table " + (customer.chosenTableIndex + 1) +
                    " Seat " + (customer.chosenSeatIndex + 1));
            Table table = tableList.get(customer.chosenTableIndex);
            int seatNum = customer.chosenSeatIndex;
            // Release The Seat
            table.seats[seatNum].occupy(null);
            // Customer in Cafe Leave
            customerInCafeCount.decrementAndGet();
        } finally {
            tableListLock.unlock();
        }
        // Notify the waiting customers that a seat is available
        waitingSeatQueueLock.lock();
        try {
            waitingSeatQueueCondition.signal();
        } finally {
            waitingSeatQueueLock.unlock();
        }
    }

    public void consumeDrinks(Customer customer) {
        for (int i = 25; i <= 100; i += 25) {
            try {
                TimeUnit.SECONDS.sleep(new Random().nextInt(1, 2));
//                TimeUnit.MILLISECONDS.sleep(7500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(customer + " : Consuming " + customer.orderDrinks + " " + i + " %");
        }
    }

    public void serveCustomer(Barista barista) {
        Customer customer;
        orderingQueueLock.lock();
        try {
            while (customerOrderingQueue.isEmpty()) {
                orderingQueueCondition.await();
                if (isCafeClose) {
                    return;
                }
            }
            baristaSemaphore.acquire();
            customer = (Customer) ((LinkedList<?>) customerOrderingQueue).poll();
            System.out.println(barista + " : Serving customer " + customer);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            orderingQueueLock.unlock();
        }
        prepareOrder(barista, customer);
        baristaSemaphore.release();
        Objects.requireNonNull(customer).controller.release();
    }

    private void prepareOrder(Barista barista, Customer customer) {
        switch (Objects.requireNonNull(customer).orderDrinks) {
            case CAPPUCCINO:
                prepareCappuccino(barista, customer);
                soldCappuccino.incrementAndGet();
                break;
            case ESPRESSO:
                prepareEspresso(barista, customer);
                soldEspresso.incrementAndGet();
                break;
            case JUICE:
                prepareJuice(barista, customer);
                soldJuiceDrink.incrementAndGet();
                break;
            default:
                throw new IllegalStateException("Unexpected value");
        }
    }

    private void prepareCappuccino(Barista barista, Customer customer) {
        try {
            if (espressoMachineLock.isLocked() && milkFrothingMachineLock.isLocked()) {
                System.out.println(barista + " : Waiting for the Espresso Machine " +
                        "and Milk Frothing Machine");
            } else if (espressoMachineLock.isLocked()) {
                System.out.println(barista + " : Waiting for the Espresso Machine");
            }
            espressoMachineLock.lock();
            milkFrothingMachineLock.lock();
            System.out.println(barista + " : Using Espresso Machine and Milk Frothing Machine to make Cappuccino for " + customer);
            try {
                TimeUnit.SECONDS.sleep(new Random().nextInt(3, 5));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(barista + " : Finished prepared Cappuccino for " + customer + " and release Espresso Machine and Milk Frothing Machine");
        } finally {
            espressoMachineLock.unlock();
            milkFrothingMachineLock.unlock();
        }
    }

    private void prepareEspresso(Barista barista, Customer customer) {
        try {
            if (espressoMachineLock.isLocked()) {
                System.out.println(barista + " : Waiting for the Espresso Machine");
            }
            espressoMachineLock.lock();
            System.out.println(barista + " : Using Espresso Machine to make Espresso for " + customer);
            try {
                TimeUnit.SECONDS.sleep(new Random().nextInt(2, 4));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(barista + " : Finished prepared Espresso for " + customer + " and release Espresso Machine");
        } finally {
            espressoMachineLock.unlock();
        }
    }

    private void prepareJuice(Barista barista, Customer customer) {
        try {
            if (juiceTapLock.isLocked()) {
                System.out.println(barista + " : Waiting for the Juice Tap");
            }
            juiceTapLock.lock();
            System.out.println(barista + " : Using Juice Tap to make Juice for " + customer);
            try {
                TimeUnit.SECONDS.sleep(new Random().nextInt(1, 3));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(barista + " : Finished prepared Juice for " + customer + " and release Juice Tap");
        } finally {
            juiceTapLock.unlock();
        }
    }


    public boolean placeOrder(Customer customer) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        System.out.println(customer + " : Enter the cafe at " + formattedDateTime);

        // Get statistic
        customer.enteringMillisec = System.currentTimeMillis();

        orderingQueueLock.lock();
        try {
            if (customerOrderingQueue.size() == 5) {
                System.out.println(customer + " : Leaving because more than 5 peoples are waiting.");
                return true;
            } else {
                customerOrderingQueue.add(customer);
                System.out.println(customer + " : Stands in the line at " + customerOrderingQueue.size() +
                        " position and order " + customer.orderDrinks);
                customerInCafeCount.incrementAndGet();
            }

            if (!customerOrderingQueue.isEmpty()) {
                orderingQueueCondition.signal();
            }
        } finally {
            orderingQueueLock.unlock();
        }
        return false;
    }

    private double getProfit() {
        return soldCappuccino.get() * DrinksType.CAPPUCCINO.getPrice() +
                soldEspresso.get() * DrinksType.ESPRESSO.getPrice() +
                soldJuiceDrink.get() * DrinksType.JUICE.getPrice();
    }

    public void displaySalesSummary() {
        System.out.println("==========================================");
        System.out.println("       Customer Served : " + (soldJuiceDrink.get() + soldEspresso.get() + soldCappuccino.get()));
        System.out.println("       Profit          : " + getProfit());
        System.out.println("       Cappuccino Sold : " + soldCappuccino);
        System.out.println("       Espresso Sold   : " + soldEspresso);
        System.out.println("       Juice Sold      : " + soldJuiceDrink);
        System.out.println("==========================================");
    }

    public void displayCustomerWaitingStatistic() {
        System.out.println("==========================================");
        System.out.println("       Customer Waiting Time : ");
        System.out.println("       Minimum   : " + Collections.min(customerWaitingTime) / 1000 + " sec");
        System.out.println("       Average   : " + new DecimalFormat("#.##").
                format(customerWaitingTime.stream().mapToLong(Long::longValue).average().orElse(0.0) / 1000) +
                " sec"
        );
        // Dont think why doesnt match with total execution time becuz there are three barista !
        System.out.println("       Maximum   : " + Collections.max(customerWaitingTime) / 1000 + " sec");
        System.out.println("==========================================");
    }
}