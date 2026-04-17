import java.util.concurrent.Semaphore;
import java.util.Random;

public class BankSimulation {

    static final int NUM_TELLERS  = 3;
    static final int NUM_CUSTOMERS = 50;

    static Semaphore bankOpen    = new Semaphore(0);
    static Semaphore door        = new Semaphore(2);
    static Semaphore safe        = new Semaphore(2);
    static Semaphore managerLock = new Semaphore(1);
    static Semaphore lineLock    = new Semaphore(1);

    // 6 distinct semaphores forperfect turn-taking
    static Semaphore[] tellerAvailable     = new Semaphore[NUM_TELLERS];
    static Semaphore[] customerArrived     = new Semaphore[NUM_TELLERS];
    static Semaphore[] tellerAsked         = new Semaphore[NUM_TELLERS];
    static Semaphore[] transactionProvided = new Semaphore[NUM_TELLERS];
    static Semaphore[] transactionFinished = new Semaphore[NUM_TELLERS];
    static Semaphore[] customerLeft        = new Semaphore[NUM_TELLERS];

    static int[] transactionType = new int[NUM_TELLERS];
    static int[] currentCustomer = new int[NUM_TELLERS];
    
    static int tellersReady    = 0;
    static int customersServed = 0;
    static Semaphore tellerReadyCount    = new Semaphore(1);
    static Semaphore customersServedLock = new Semaphore(1);
    static Random rand = new Random();

    static {
        for (int i = 0; i < NUM_TELLERS; i++) {
            tellerAvailable[i]     = new Semaphore(0);
            customerArrived[i]     = new Semaphore(0);
            tellerAsked[i]         = new Semaphore(0);
            transactionProvided[i] = new Semaphore(0);
            transactionFinished[i] = new Semaphore(0);
            customerLeft[i]        = new Semaphore(0);
        }
    }

    public static class Teller extends Thread {
        int id;
        Teller(int id) { this.id = id; }

        public void run() {
            try {
                System.out.println("Teller " + id + " []: ready to serve");

                tellerReadyCount.acquire();
                tellersReady++;
                if (tellersReady == NUM_TELLERS) {
                    bankOpen.release(NUM_CUSTOMERS);
                }
                tellerReadyCount.release();

                while (true) {
                    customersServedLock.acquire();
                    if (customersServed >= NUM_CUSTOMERS) {
                        customersServedLock.release();
                        break;
                    }
                    customersServedLock.release();

                    System.out.println("Teller " + id + " []: waiting for a customer");
                    tellerAvailable[id].release(); // Let one customer approach
                    customerArrived[id].acquire(); // Wait for them to sit down

                    // Re-check for shutdown
                    customersServedLock.acquire();
                    if (customersServed >= NUM_CUSTOMERS) {
                        customersServedLock.release();
                        break;
                    }
                    customersServedLock.release();

                    int custId = currentCustomer[id];

                    System.out.println("Teller " + id + " [Customer " + custId + "]: serving a customer");
                    System.out.println("Teller " + id + " [Customer " + custId + "]: asks for transaction");

                    tellerAsked[id].release();         // Tell customer to give transaction
                    transactionProvided[id].acquire(); // Wait for customer to answer

                    int type = transactionType[id];
                    String typeName = (type == 0) ? "deposit" : "withdrawal";
                    System.out.println("Teller " + id + " [Customer " + custId + "]: handling " + typeName + " transaction");

                    if (type == 1) {
                        System.out.println("Teller " + id + " [Customer " + custId + "]: going to the manager");
                        managerLock.acquire();
                        System.out.println("Teller " + id + " [Customer " + custId + "]: getting manager's permission");
                        Thread.sleep(5 + rand.nextInt(26));
                        System.out.println("Teller " + id + " [Customer " + custId + "]: got manager's permission");
                        managerLock.release();
                    }

                    System.out.println("Teller " + id + " [Customer " + custId + "]: going to safe");
                    safe.acquire();
                    System.out.println("Teller " + id + " [Customer " + custId + "]: enter safe");
                    Thread.sleep(10 + rand.nextInt(41));
                    System.out.println("Teller " + id + " [Customer " + custId + "]: leaving safe");
                    safe.release();

                    System.out.println("Teller " + id + " [Customer " + custId + "]: finishes " + typeName + " transaction.");
                    System.out.println("Teller " + id + " [Customer " + custId + "]: wait for customer to leave.");

                    transactionFinished[id].release(); // Tell customer we are done
                    customerLeft[id].acquire();        // Wait for customer to leave desk

                    customersServedLock.acquire();
                    customersServed++;
                    customersServedLock.release();
                }

                System.out.println("Teller " + id + " []: leaving for the day");

            } catch (Exception e) { System.out.println(e); }
        }
    }

    public static class Customer extends Thread {
        int id;
        Customer(int id) { this.id = id; }

        public void run() {
            try {
                int type = rand.nextInt(2);
                String typeName = (type == 0) ? "deposit" : "withdrawal";

                System.out.println("Customer " + id + " []: wants to perform a " + typeName + " transaction");

                Thread.sleep(rand.nextInt(101));

                bankOpen.acquire();
                System.out.println("Customer " + id + " []: going to bank.");
                
                door.acquire();
                System.out.println("Customer " + id + " []: entering bank.");
                System.out.println("Customer " + id + " []: getting in line.");
                door.release();

                System.out.println("Customer " + id + " []: selecting a teller.");

                // Safe line logic
                int myTeller = -1;
                while (myTeller == -1) {
                    lineLock.acquire();
                    for (int i = 0; i < NUM_TELLERS; i++) {
                        if (tellerAvailable[i].tryAcquire()) { 
                            myTeller = i; 
                            break; 
                        }
                    }
                    lineLock.release();
                    if (myTeller == -1) Thread.sleep(5);
                }

                System.out.println("Customer " + id + " [Teller " + myTeller + "]: selects teller");
                System.out.println("Customer " + id + " [Teller " + myTeller + "] introduces itself");

                currentCustomer[myTeller] = id;
                customerArrived[myTeller].release(); 

                tellerAsked[myTeller].acquire(); 

                transactionType[myTeller] = type;
                System.out.println("Customer " + id + " [Teller " + myTeller + "]: asks for " + typeName + " transaction");
                transactionProvided[myTeller].release();

                transactionFinished[myTeller].acquire(); 
                
                System.out.println("Customer " + id + " [Teller " + myTeller + "]: leaves teller");
                System.out.println("Customer " + id + " []: goes to door");
                System.out.println("Customer " + id + " []: leaves the bank");
                
                customerLeft[myTeller].release();

            } catch (Exception e) { System.out.println(e); }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Teller[]   tellers   = new Teller[NUM_TELLERS];
        Customer[] customers = new Customer[NUM_CUSTOMERS];

        for (int i = 0; i < NUM_TELLERS; i++)   { tellers[i]   = new Teller(i);   tellers[i].start();   }
        for (int i = 0; i < NUM_CUSTOMERS; i++) { customers[i] = new Customer(i); customers[i].start(); }

        for (int i = 0; i < NUM_CUSTOMERS; i++) customers[i].join();

        // Safe shutdown to wake up tellers
        for (int i = 0; i < NUM_TELLERS; i++) {
            customerArrived[i].release();
        }

        for (int i = 0; i < NUM_TELLERS; i++) tellers[i].join();

        System.out.println("The bank closes for the day.");
    }
}