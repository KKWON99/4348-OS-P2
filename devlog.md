#start of project  4/17/2026 02:04 am

### Thoughts so far
This project is a bank simulation using Java threads and semaphores. There are 3 tellers
and 50 customers. The tellers and customers need to synchronize through a series of
handshake steps. The shared resources that need protection are: the safe (max 2 tellers),
the manager (only 1 teller at a time), and the customer line. The bank cannot open until
all 3 tellers are ready, so customers must block until that happens.

The trickiest part will be the per-pair signaling between a specific customer and a
specific teller they need to signal each other back and forth multiple times through
the transaction. I will need a semaphore array, one per teller, for things like
"customer has arrived", "teller asked for transaction", "customer gave transaction", etc.

### Plan for this session
- Write the README
- Write this devlog entry and commit
- Build the skeleton: BankSimulation.java with a Teller class and Customer class,
  both extending Thread, with IDs assigned
- Get all threads launching and printing a simple "Teller X ready" / "Customer Y entering"
  message so I can confirm the thread structure works
- Commit the skeleton

## Thoughts  0322
Thread skeleton is working. All 50 customers and 3 tellers launch and print.
Order is random which is expected. Added semaphore declarations for bankOpen,
door, safe, managerLock, lineLock, and per-teller arrays for the handshake steps.
Bank-open gate is in place — customers will block until all 3 tellers signal ready.

## error
Git kept rejecting pushes due to non-fast-forward errors. Remote was ahead of local.
Fixed by running git pull --no-rebase before pushing. Also got stuck in vim when
git opened the merge message editor. Escaped with :wq


## April 17, 2026 0347

**Thoughts:** Managing the direct interaction between 50 customers and 3 tellers requires precise 1-to-1 signaling. I need to ensure that when a customer walks up to a teller, the data passed between them is completely isolated from the other threads.

**Session Accomplishments:** Implemented arrays of semaphores indexed by the teller ID  This setup allows a specific customer to synchronize perfectly with a specific teller. Also added shared integer arrays to track the transaction type and a mutex-protected variable to count how many tellers are currently ready.

**Next plan:** Implement the matching logic so a customer can safely find an available teller ID, assign themselves to it, and begin the semaphore handshake.


## April 17, 2026 - 04:06

**Thoughts:** I needed a clean way to ensure the bank doesn't open until all three tellers are fully initialized and ready. I also wanted to clean up the main method by handling array initialization earlier.

**Session Accomplishments:** * Moved the initialization of the per-teller semaphore arrays into a class-level `static` block so they are ready before any threads start.
* Added a `Random` instance for future timing requirements.
* Implemented the initialization phase in the `Teller` thread's `run()` method. Using the `tellerReadyCount` mutex, the tellers now safely increment the `tellersReady` counter. 
* Added the logic for the final ready teller to print "bank is now open" and release the `bankOpen` semaphore for all 50 customers.

**next plan:** Update the `Customer` thread to wait for the `bankOpen` semaphore before trying to enter, implement the `door` capacity limit, and start building out the customer queue logic.


## April 17, 2026 - 11:40
**Thoughts:** The customer side needs to block on bankOpen before
doing anything, then respect the door limit of 2 at a time.
Finding a free teller requires a mutex around the search so two
customers don't grab the same teller simultaneously.

## error  
had the teller and the customer run mixed failed to run

**Accomplishments:** Customer now sleeps 0-100ms before arriving,
acquires bankOpen then door semaphore to enter. Uses lineLock mutex
and tryAcquire to scan for a free teller. If none found, falls back
to waiting on a teller slot. Customer prints which teller it selected.
**Next:** Implement the full teller-customer handshake so they
exchange transaction type and complete the transaction.

## error  1150

Encountered a compilation error due to accidentally nesting method signatures in the Customer class. Fixed the syntax and integrated the final synchronization logic.



**Accomplishments:**     **1203**
added so that it serves customers one at a time


## April 17, 2026 - 12:20
**Thoughts:** The handshake is a back-and-forth ping-pong between
the customer and teller semaphores. Each step must be in the right
order or both threads deadlock waiting on each other.
**Accomplishments:** Teller loops indefinitely releasing tellerReady
then waiting on customerArrived. Customer signals customerArrived,
waits for transactionReady, writes transaction type to shared array,
then signals back. Teller reads the type, processes it, signals
transactionDone. Customer sees it done, prints leaving, signals
customerLeaving so teller can serve the next one.
**Next:** Add manager permission for withdrawals

## April 17, 2026 - 13:20
Session Accomplishments:

Syntax Debugging: Fixed a structural error where the run() method was accidentally nested inside itself in the Customer class.

Scope Resolution: Declared customersServed and customersServedLock as static variables within the BankSimulation class to fix "cannot find symbol" errors during compilation.

Synchronization Verification: Verified that the safe semaphore correctly restricts access to only two tellers at once and that the bankOpen semaphore properly gates customer entry.

Graceful Exit: Added System.exit(0) to ensure all threads terminate and the program closes cleanly after the 50th customer is served.


## final thoughts
The project is nearing completion. The primary challenge was coordinating the "handshake" between 50 customer threads and 3 teller threads without creating deadlocks. I focused heavily on ensuring that shared resources like the bank manager and the safe were strictly managed by semaphores to meet the project's concurrency requirements.


## issue found

The program is hanging/freezing after customers enter — tellers aren't finishing serving them
The sample shows Customer X [Teller Y]: format but mine just shows Teller X [Teller X]:

## issue found
Keep having race condition that turns in to deadlock **1705**

## April 18, 2026 - simulation complete 1740

**Thoughts:** After several iterations fixing deadlocks and race
conditions, the simulation is now running correctly to completion.

**Final fixes that worked:** Replaced LinkedBlockingQueue with a
lineLock mutex and tellerAvailable semaphore array using tryAcquire
with a 5ms sleep fallback. Added currentCustomer[] array so the
teller knows which customer ID to print in its messages. Expanded
the handshake to 6 semaphore arrays for clean turn-taking with no
race conditions. Added proper shutdown sequence in main() joining
customers first then releasing customerArrived to wake idle tellers.

**Reflection:** This project was significantly harder than project 1
because the synchronization had to be precise in both directions.
The hardest part was the line logic — getting customers to find a
free teller without two customers grabbing the same one. Went
through three different approaches: polling with tryAcquire,
LinkedBlockingQueue, and finally lineLock with tryAcquire and sleep.

