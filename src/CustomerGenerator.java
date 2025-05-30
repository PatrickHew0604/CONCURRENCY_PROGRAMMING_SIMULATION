import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CustomerGenerator extends Thread {
    GoGoCoffeeCafe goGoCoffeeCafe;
    ArrayList<Thread> customerThreads = new ArrayList<>();

    public CustomerGenerator(GoGoCoffeeCafe goGoCoffeeCafe) {
        this.goGoCoffeeCafe = goGoCoffeeCafe;
    }

    @Override
    public void run() {
        for (int i = 0; i < 20; i++) {
            Thread customerThread = new Customer(goGoCoffeeCafe);
            customerThread.start();
            customerThreads.add(customerThread);
            customerThread.setName("Customer " + (i + 1));
            try {
                TimeUnit.SECONDS.sleep(new Random().nextInt(3, 5));
//                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        goGoCoffeeCafe.isCafeClose = true;
        System.out.println("================ Put on Cafe Closing Sign ================");
        if (goGoCoffeeCafe.customerOrderingQueue.isEmpty()) {
            try {
                TimeUnit.SECONDS.sleep(1);
                if (goGoCoffeeCafe.baristaSemaphore.availablePermits() == 3) {
                    System.out.println("+ No one left in the cafe, seems like a pretty bad day.");
                }
                goGoCoffeeCafe.orderingQueueLock.lock();
                try {
                    if (goGoCoffeeCafe.customerOrderingQueue.isEmpty()) {
                        goGoCoffeeCafe.orderingQueueCondition.signalAll();
                    }
                } finally {
                    goGoCoffeeCafe.orderingQueueLock.unlock();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (goGoCoffeeCafe.customerWaitingSeatQueue.isEmpty()) {
            goGoCoffeeCafe.waitingSeatQueueLock.lock();
            try {
                if (goGoCoffeeCafe.customerOrderingQueue.isEmpty()) {
                    goGoCoffeeCafe.waitingSeatQueueCondition.signal();
                }
            } finally {
                goGoCoffeeCafe.waitingSeatQueueLock.unlock();
            }
        }
    }
}
