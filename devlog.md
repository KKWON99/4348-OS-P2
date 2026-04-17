#start of project  4/17/2026

### Thoughts so far
This project is a bank simulation using Java threads and semaphores. There are 3 tellers
and 50 customers. The tellers and customers need to synchronize through a series of
handshake steps. The shared resources that need protection are: the safe (max 2 tellers),
the manager (only 1 teller at a time), and the customer line. The bank cannot open until
all 3 tellers are ready, so customers must block until that happens.

The trickiest part will be the per-pair signaling between a specific customer and a
specific teller — they need to signal each other back and forth multiple times through
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