#start of project  4/17/2026

### Thoughts about the project
Project 2 is a bank simulation using threads and semaphores in Java.
There are 3 tellers and 50 customers. Key shared resources are the safe
(max 2 tellers at once) and the manager .
The trickiest part will be the back-and-forth signaling between each
teller-customer pair. Planning to get the skeleton up tonight and
finish the full simulation in the morning session.

### Plan for this session
- Set up BankSimulation.java with Teller and Customer inner classes
- Get threads launching and printing basic output
- No synchronization yet, just confirm threads run
- Commit skeleton, then add basic semaphore structure for bank opening
