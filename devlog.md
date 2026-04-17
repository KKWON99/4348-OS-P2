#start of project  4/16/2026

### Thoughts about the project 11:59
Project 2 is a bank simulation using threads and semaphores in Java.
There are 3 tellers and 50 customers. Key shared resources are the safe
(max 2 tellers at once) and the manager .
The trickiest part will be the back-and-forth signaling between each
teller-customer pair. Planning to get the skeleton up tonight and
finish the full simulation in the morning session.

### Plan for this session
- Write the README
- Write this devlog entry and commit
- Build the skeleton: BankSimulation.java with a Teller class and Customer class,
  both extending Thread, with IDs assigned
- Get all threads launching and printing a simple "Teller X ready" / "Customer Y entering"
  message so I can confirm the thread structure works
- Commit the skeleton
