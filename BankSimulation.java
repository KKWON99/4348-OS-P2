import java.util.concurrent.Semaphore;
import java.util.Random;

public class BankSimulation {

    static final int NUM_TELLERS = 3;
    static final int NUM_CUSTOMERS = 50;

    // Shared Resources 
    static Semaphore bankOpen      = new Semaphore(0); // opens when all 3 tellers ready
    static Semaphore door          = new Semaphore(2); 
    static Semaphore safe          = new Semaphore(2); 
    static Semaphore managerLock   = new Semaphore(1); 
    static Semaphore lineLock      = new Semaphore(1); 

    // Per-teller semaphores 
    static Semaphore[] tellerReady      = new Semaphore[NUM_TELLERS]; 
    static Semaphore[] customerArrived  = new Semaphore[NUM_TELLERS]; 
    static Semaphore[] transactionReady = new Semaphore[NUM_TELLERS]; 
    static Semaphore[] transactionDone  = new Semaphore[NUM_TELLERS]; 
    static Semaphore[] customerLeaving  = new Semaphore[NUM_TELLERS]; 

    // Shared variables for teller-customer communication
    static int[] transactionType = new int[NUM_TELLERS]; 
    static int[] assignedTeller  = new int[1];         
    static int   tellersReady    = 0;                    
    static Semaphore tellerReadyCount = new Semaphore(1);

static Random rand = new Random();

    static {
        for (int i = 0; i < NUM_TELLERS; i++) {
            tellerReady[i]      = new Semaphore(0);
            customerArrived[i]  = new Semaphore(0);
            transactionReady[i] = new Semaphore(0);
            transactionDone[i]  = new Semaphore(0);
            customerLeaving[i]  = new Semaphore(0);
        }
    }

    //Teller Thread
    public static class Teller extends Thread {
        int id;
Teller(int id) { this.id = id; }

        public void run() {
            try {
                System.out.println("Teller " + id + " [Teller " + id + "]: is ready to serve");

                // Count this teller as ready; if all 3 ready, open the bank
                tellerReadyCount.acquire();
                tellersReady++;
                if (tellersReady == NUM_TELLERS) {
                    System.out.println("Teller " + id + " [Teller " + id + "]: bank is now open");
                    bankOpen.release(NUM_CUSTOMERS); // let all customers in
                }
                tellerReadyCount.release();

                // TODO: serve customers (coming next session)

            } catch (Exception e) { System.out.println(e); }
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