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
