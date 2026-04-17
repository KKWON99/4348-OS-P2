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