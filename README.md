<<<<<<< HEAD
# 4348-OS-P2
=======
# 4348-OS-P2 04/16/2026 1203
having issues with connecting vs code and github I have recreated this file multiple times
>>>>>>> 20eec01185d9860ee9e75ffb10551b78b1bcd5a0

# CS4348 Project 2 - Bank Simulation

## Author
Kiryang Kwon

## Files
- `BankSimulation.java` - Main simulation file containing all thread logic
- `devlog.md` - Development log tracking progress and thoughts
- `SignalDemo.java` - Reference file provided by instructor
- `ThreadDemo.java` - Reference file provided by instructor

## How to Compile
javac BankSimulation.java

## How to Run
java BankSimulation

## Description
Simulates a bank with 3 teller threads and 50 customer threads.
Uses Java semaphores for synchronization of shared resources
including the safe (max 2 tellers) and manager (1 teller at a time).
