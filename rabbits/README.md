# Implementing a first Application in RePast: A Rabbits Grass Simulation

Link to OverLeaf edit [here](https://www.overleaf.com/7847813694cmqbmfbxpyfk).

## The Rabbits Grass simulation

The Rabbits Grass simulation is a simulation of an ecosystem: rabbits wander around randomly on a discrete grid environment on which grass is growing randomly. Rabbits have an initial amount of energy and with each move they lose a part of it. Once their energy is 0, they die. But when an alive rabbit bumps into some grass, it eats the grass and gains some energy. If a rabbit gains enough energy, it reproduces. The reproduction takes some energy so the rabbit can not reproduce twice within the same simulation step. The grass can be adjusted to grow at different rates and give the rabbits differing amounts of energy. It has to be possible to fully control the total amount of grass being grown at each simulation step. The model can be used to explore the competitive advantages of these variables.

This model has been described at [link](http://ccl.northwestern.edu/netlogo/models/RabbitsGrassWeeds) for the NetLogo simulation toolkit.

You have to program the Rabbits Grass Simulation in RePast, using the following requirements:
- **Grid:** the size of the world should be changeable. The default is a 20x20 grid. The world has no borders on the edges (thus, it is a torus).
- **Collisions:** different rabbits cannot stay on the same cell.
- **Legal moves:** only one-step moves to adjacent cells (north, south, east and west) are allowed.
- **Eat condition:** a rabbit can eat grass when it occupies the same cell.
- **Communication:** we assume that agents can not communicate with one another.
- **Visible range and directions:** all rabbits are blind and move randomly.
- **Creation:** at their births, rabbits are created at random places.


Implement sliders for the following variables of the simulation:
- Grid size of the world. We assume a square world of size (GridSize * GridSize)
- The number of rabbits defines the initial number of rabbits spread in the world
- The number of grass defines the initial number of grass spread in the world
- The birth threshold of rabbits defines the energy level at which the rabbit reproduces.
- The grass growth rate controls the rate at which grass grows (total amount of grass added to the whole world within one simulation tick). If it is set to 50, 50 units of grasses are spread to the world in total at every simulation tick and you should randomly put them into a number of cells.

You should not change the variable names of the above variables in the skeleton code we provide. You can add new variables if you want. Furthermore, remember to complete all "set" and "get" functions for all variables.

## Report

Explain your important model assumptions for the simulation your implement.
Create a population plot to observe the evolution of the rabbits and the grass.


## Running simulation

The main function accepts two arguments:
- ```args[0]:``` the parameter file to specify the variable values in the slider bar. By default, we set it to "" such that you can manually modify them in the GUI.
- ```args[1]:``` whether to use batch mode to run a simulation. By default, we set it to false to use the GUI mode.
- Remark: we only require you to implement the GUI model in this project. If you want to play with the parameter file and the batch model, feel free to so. In this case, you can use your IDE to pass these to arguments. For example, in Eclipse, you can edit them through "Run Configuration". 
Before you submit your final version, please make sure that your runnable jar file can also be executed with passing the two arguments. for example, try the following command:
```
java -jar ***.jar "" false
```

## Instructions
1.	Rename the folder lastname1-lastname2-in.
2.	Import the project in Eclipse (or another IDE, for example Netbeans).
3.	Reference the library repast.
4.	Other libraries such as colt.jar and plot.jar might be needed, so you can reference them as well. They can be downloaded on moodle (Additional JAR libraries for the programming exercises).
5.	Link the javadoc for the repast library which can be found on moodle (Repast javadoc).
6.	Write your code in RabbitsGrassSimulationAgent, RabbitsGrassSimulationModel and RabbitsGrassSimulationSpace (do not rename these files and do not put them into packages!).
7.	Run the simulation by running MainRabbit.java.
8.	Make sure that you fulfill the requirements for the solution given in the exercise description.
9.	Write documentation using the latex template and place it into the doc folder. The pdf should be named lastname1-lastname2-in.pdf.
10.	Create a runnable jar file and place it in the folder lastname1-lastname2-in.
11. Package required libraries in the package.
12.	Zip the folder lastname1-lastname2-in (without the libraries) and submit it on moodle.

## Delivarable

Deliverable (Due on Tuesday 01.10.2019 at 23:55):
- Your source code, compiled code, runnable jar, and report. All in one (!) zip file
- The report is a short description of your code and results, must be in PDF, maximum of three pages
- Follow the instructions given above.

# TODOs

- [x] make square grid; initial size 20x20; torus
- [x] collisions prevention; rabbits cannot stay on the same cell
- [x] legal agent moves 1 step in NSEW direction, with every move they losse part of energy (CHECK: what number exactly?) unless they bump into the grass cell, in that case they increase the energy (CHECK: what number exactly? different grass cell can give different amount of energy)
- [x] eat condition; rabbit eats the grass when it occupies the same cell
- [x] no communication between agents
- [x] agents are blind and move randomly
- [x] creation of agent and grass at random places
- [x] initializing: grid size, num of rabbits in the beginning, number of grass in the beginning
- [ ] implement birth threshold, when rabbit reaches certain energy level it reproduces; makes one new rabbit; it cannot reproduce twice in one thick since reproduction takes some energy too (CHECK: what amount?)
- [x] implement the death of the agent, when energy is 0
- [x] grass grows with every tick, total amount with every thick is initialized; CHECK: total 50 or added 50? It has to be possible to fully control the total amount of grass being grown at each simulation step.
- [ ] check that all setters/getters are there 
- [ ] write down the assuptions for the model we concluded are the best
- [ ] model can be used to explore the competitive advantages of these variables we have
- [ ] create population plot to observe the evolution of the rabbits and the grass. CHECK: is that the plot with two different color lines that are changing with every simulation tick?
- [ ] check if ```java -jar *.jar "" false``` command works fine
- [ ] write down java docs for every method implemented
- [ ] make sure Instructions and Delivarable sections are correctly completed in the end

