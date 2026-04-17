import java.util.concurrent.Semaphore;
import java.util.Random;

public class BankSimulation {

    static final int NUM_TELLERS = 3;
    static final int NUM_CUSTOMERS = 50;

    //Teller Thread
    public static class Teller extends Thread {
        int id;

        Teller(int id) {
            this.id = id;
        }

        public void run() {
            System.out.println("Teller " + id + " [Teller " + id + "]: is ready to serve");
        }
    }

    // customer Thread
    public static class Customer extends Thread {
        int id;

        Customer(int id) {
            this.id = id;
        }

         public void run() {
            System.out.println("Customer " + id + " [Customer " + id + "]: is entering the bank");
        }
    }

    // main
    public static void main(String[] args) throws InterruptedException {

        Teller[] tellers = new Teller[NUM_TELLERS];
        Customer[] customers = new Customer[NUM_CUSTOMERS];

        // Start tellers first
        for (int i = 0; i < NUM_TELLERS; i++) {
            tellers[i] = new Teller(i);
            tellers[i].start();
        }

        // Start tellers first
        for (int i = 0; i < NUM_TELLERS; i++) {
            tellers[i] = new Teller(i);
            tellers[i].start();
        }

        // Start customers
        for (int i = 0; i < NUM_CUSTOMERS; i++) {
            customers[i] = new Customer(i);
            customers[i].start();
        }

        // Wait for everyone to finish
        for (int i = 0; i < NUM_TELLERS; i++)    tellers[i].join();
        for (int i = 0; i < NUM_CUSTOMERS; i++)  customers[i].join();

        System.out.println("Bank is now closed.");
    }
}