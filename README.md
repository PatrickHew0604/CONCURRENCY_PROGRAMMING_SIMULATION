# GoGo Coffee Cafe Simulation ☕️

A concurrent programming simulation of a coffee cafe, where customers, baristas, and seat assignment are managed using Java threads and synchronization primitives.

---

## 📋 Project Structure

```
src/
  Barista.java
  Customer.java
  CustomerGenerator.java
  DrinksType.java
  GoGoCoffeeCafe.java
  Main.java
  Seat.java
  SeatAssignation.java
  Table.java
```

---

## 🚦 How It Works

- **Customers** arrive at the cafe, place orders, and wait for available seats.
- **Baristas** prepare drinks (Cappuccino, Espresso, Juice) using shared resources (machines/taps).
- **SeatAssignation** manages seat allocation, considering customer preferences for sharing tables.
- The simulation runs until all customers are served or leave, then prints statistics.

---

## 🛠️ Technologies Used

- **Java 21**
- **Threads & Concurrency:** `Thread`, `Semaphore`, `ReentrantLock`, `Condition`
- **Collections:** `ArrayList`, `LinkedList`
- **Randomization:** Simulates real-world unpredictability

---

## ▶️ Running the Simulation

1. **Compile:**
   ```sh
   javac src/*.java
   ```

2. **Run:**
   ```sh
   java -cp src Main
   ```

---

## 📈 Features

- Multiple baristas serve customers concurrently.
- Customers may leave if the queue is too long or if they wait too long.
- Customers can change their willingness to share a table.
- Detailed statistics on sales and customer waiting times.

---

## 📊 Output Example

```
Customer 1 : Enter the cafe at 10:00:01
Barista 1 : Serving customer Customer 1
Customer 1 : Paying RM 9.0 for the Cappuccino
Customer 1 : Receive the Cappuccino and seeking for a available seat
Customer 1 : (Willing To Share Seat) Occupied a seat at Table 1 Seat 1
...
========= The cafe is closed now =========
==========================================
       Customer Served : 20
       Profit          : 150.0
       Cappuccino Sold : 14
       Espresso Sold   : 4
       Juice Sold      : 2
==========================================
       Customer Waiting Time : 
       Minimum   : 2 sec
       Average   : 4.5 sec
       Maximum   : 8 sec
==========================================
Simulation took 45.2 seconds
```

---

## 👨‍💻 Authors

- Patrick

---

## 📄 License

This project is for educational purposes.

---