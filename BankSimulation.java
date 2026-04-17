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
    static int   tellersReady    = 0;
    static int   customersServed = 0; 
    static Semaphore customersServedLock = new Semaphore(1);
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

                tellerReadyCount.acquire();
                tellersReady++;
                if (tellersReady == NUM_TELLERS) {
                    System.out.println("Teller " + id + " [Teller " + id + "]: bank is now open");
                    bankOpen.release(NUM_CUSTOMERS);
                }
                tellerReadyCount.release();

               // serve customers one at a time
                while (true) {

                    customersServedLock.acquire();
                    if (customersServed >= NUM_CUSTOMERS) {
                        customersServedLock.release();
                        break;
                    }
                    customersServedLock.release();

                    tellerReady[id].release();        // signal available
                    customerArrived[id].acquire();    // wait for customer

                    System.out.println("Teller " + id + " [Teller " + id + "]: asks for transaction");
                    transactionReady[id].release();   // ask for transaction
                    transactionReady[id].acquire();   // wait for answer

                    int type = transactionType[id];
                    String typeName = (type == 0) ? "deposit" : "withdraw";
                    System.out.println("Teller " + id + " [Teller " + id + "]: handling " + typeName);

                    // withdraw requires manager permission
                    if (type == 1) {
                        System.out.println("Teller " + id + " [Teller " + id + "]: going to manager");
                        managerLock.acquire();
                        System.out.println("Teller " + id + " [Teller " + id + "]: with manager");
                        Thread.sleep(5 + rand.nextInt(26)); // 5-30ms
                        System.out.println("Teller " + id + " [Teller " + id + "]: leaving manager");
                        managerLock.release();
                    }

                    // enter safe (max 2 tellers at once)
                    System.out.println("Teller " + id + " [Teller " + id + "]: going to safe");
                    safe.acquire();
                    System.out.println("Teller " + id + " [Teller " + id + "]: in the safe");
                    Thread.sleep(10 + rand.nextInt(41)); // 10-50ms
                    System.out.println("Teller " + id + " [Teller " + id + "]: leaving the safe");
                    safe.release();

                    transactionDone[id].release();
                    customerLeaving[id].acquire();

                    // count this customer as served
                    customersServedLock.acquire();
                    customersServed++;
                    System.out.println("Teller " + id + " [Teller " + id + "]: ready for next customer");
                    customersServedLock.release();
                }

                System.out.println("Teller " + id + " [Teller " + id + "]: going home");

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
            try {
                int type = rand.nextInt(2); // 0=deposit 1=withdraw
                String typeName = (type == 0) ? "deposit" : "withdraw";

                // wait between 0-100ms before going to bank
                Thread.sleep(rand.nextInt(101));

                // wait for bank to open, then enter through door (max 2 at once)
                bankOpen.acquire();
                door.acquire();
                System.out.println("Customer " + id + " [Customer " + id + "]: is entering the bank");
                door.release();

                // find a free teller
                lineLock.acquire();
                int myTeller = -1;
                for (int i = 0; i < NUM_TELLERS; i++) {
                    if (tellerReady[i].tryAcquire()) {
                        myTeller = i;
                        break;
                    }
                }
                lineLock.release();
            // if no teller free, wait in line
                if (myTeller == -1) {
                    tellerReady[id % NUM_TELLERS].acquire();
                    myTeller = id % NUM_TELLERS;
                }


               System.out.println("Customer " + id + " [Customer " + id + "]: selects Teller " + myTeller);

                customerArrived[myTeller].release();       // tell teller im here
                transactionReady[myTeller].acquire();      // wait for teller to ask

                transactionType[myTeller] = type;
                System.out.println("Customer " + id + " [Customer " + id + "]: requests " + typeName);
                transactionReady[myTeller].release();      // give transaction

                transactionDone[myTeller].acquire();       // wait for teller to finish
                System.out.println("Customer " + id + " [Customer " + id + "]: is leaving");
                customerLeaving[myTeller].release();       // tell teller im gone

            } catch (Exception e) { System.out.println(e); }
        }
    }

    // main
    public static void main(String[] args) throws InterruptedException {
        Teller[]   tellers   = new Teller[NUM_TELLERS];
        Customer[] customers = new Customer[NUM_CUSTOMERS];

        for (int i = 0; i < NUM_TELLERS; i++) { tellers[i] = new Teller(i); tellers[i].start(); }
        for (int i = 0; i < NUM_CUSTOMERS; i++) { customers[i] = new Customer(i); customers[i].start(); }
        for (int i = 0; i < NUM_TELLERS; i++)   tellers[i].join();
        for (int i = 0; i < NUM_CUSTOMERS; i++) customers[i].join();

        System.out.println("Bank is now closed.");
    }
}