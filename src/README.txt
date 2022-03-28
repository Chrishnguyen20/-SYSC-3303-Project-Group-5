Authors: 
Billal Ghadie - 100888260
Minh Nguyen - 101154921
Christopher Nguyen - 101167689
Leo Xu - 101149896

Source files:
- Elevator.java
- ElevatorRequest.java
- ElevatorState.java
- ElevatorSystemTest.java
- Floor.java
- FloorRequest.java
- Main.java
- Scheduler.java
- TraceFile.java


Documentation:
- Class Diagram:  a UML class representation of the program 
- Sequence Diagram: a UML representation of a single program loop. That is, the floor sends a floor request to the scheduler, 
  the scheduler relays the information to the elevator, the elevator picks up the passengers, then the elevator takes the passengers to their destination floor.
- State Diagrams: there are two UML state diagrams, one for the scheduler and another for the elevator.
- Timing Diagrams: shows the timing interaction between the elevator and scheduler when a fault occurs

Instructions:
The program's usage is fairly straightforward, however, requires the use of the Eclipse IDE. Note, the input file takes the following form and is tab delimited:
<Time Stamp>	<floor number>	<direction>	<destination floor>	<number of occurrence before fault>		<fault type>
Simply unzip the source file, open the project in Eclipse, then run the three classes in the following order:
1. Scheduler
2. Floor
3. Elevator

JUnit testing:
This program comes with a JUnit package for quick and seemless testing. 
To execute the tests, first you must successfully run the program once (see Instructions). Then simply right click the "ElevatorSystemTest.java" file in Eclipse and run the program as a  JUnit test. 
There are a total of 39 tests which are all documented in the source code via comments.  

Breakdown or responsibilities: 
- Billal:  the JUnit testing + timing 
- Minh:  smart scheduler + implementation for multiple elevators  
- Leo:    Timing + documentation  
- Chris:  design documentations

Distance between floor is 4 meters.
Average time it takes per floor: 9.5 seconds
Average time from floor 1-7: 19.825 seconds
Speed per floor: 4/9.5 = 0.42105 m/s
Speed from floor 1-7: 28/19.825 = 1.412358 m/s
Rate of acceleration per floor: 0.42105/9.5 = 0.04432 m/s^2  
Rate of acceleration floor 1-7: 1.412358/19.825 = 0.07124 m/s^2 
Average loading/unloading time per floor: 9.51428 seconds
Average loading/unloading time from floor 1-7: 9.175 seconds
