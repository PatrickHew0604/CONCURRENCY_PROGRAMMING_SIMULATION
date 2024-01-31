

public class SeatAssignation extends Thread {
    GoGoCoffeeCafe goGoCoffeeCafe;

    public SeatAssignation(GoGoCoffeeCafe goGoCoffeeCafe) {
        this.goGoCoffeeCafe = goGoCoffeeCafe;
    }

    @Override
    public void run() {
        while (!goGoCoffeeCafe.isCafeClose) {
            goGoCoffeeCafe.assignCustomerToSuitableSeat();
        }
        while (!goGoCoffeeCafe.customerWaitingSeatQueue.isEmpty() ||
                goGoCoffeeCafe.customerInCafeCount.get() != 0) {
//            System.out.println("===Customer in Count : " + goGoCoffeeCafe.customerInCafeCount.get()+"===");
            goGoCoffeeCafe.assignCustomerToSuitableSeat();
        }
        ;
//        System.out.println("-----------------No Assignation Now-----------------");
    }
}
