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
