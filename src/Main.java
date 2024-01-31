import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Thread Declaration
        GoGoCoffeeCafe goGoCoffeeCafe = new GoGoCoffeeCafe();
        List<Barista> baristaList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            baristaList.add(new Barista(goGoCoffeeCafe));
        }
        SeatAssignation seatAssignation = new SeatAssignation(goGoCoffeeCafe);
        CustomerGenerator customerGenerator = new CustomerGenerator(goGoCoffeeCafe);
        customerGenerator.setName("Customer Generator");
        seatAssignation.setName("Seat Assignation");


        // Start Execution of the program
        long startTime = System.currentTimeMillis();

        customerGenerator.start();
        for (Barista barista : baristaList) {
            barista.start();
        }
        seatAssignation.start();

        try {
            customerGenerator.join();
            seatAssignation.join();
            for (Barista barista : baristaList) {
                barista.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("========= The cafe is closed now =========");
        // Statistic
        goGoCoffeeCafe.displaySalesSummary();
        goGoCoffeeCafe.displayCustomerWaitingStatistic();

        long endTime = System.currentTimeMillis();
        long totalTimeMilliseconds = endTime - startTime;
        double totalTimeSeconds = totalTimeMilliseconds / 1000.0;

        System.out.println("Simulation took " + totalTimeSeconds + " seconds");
    }
}
