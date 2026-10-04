# Robocode Team Project – Amaterasu

Amaterasu is a Robocode robot developed as part of a university team project. The goal of the project was to design, develop, and test a robot capable of competing against different numbers and types of opponents.

The project was written in Java and involved collaborative development using Git, alongside repeated testing and analysis of the robot's performance.

## My Contributions

Alongside contributing to the development of the robot, I focused heavily on testing and analysing its behaviour.

During testing, I noticed that the robot's performance changed significantly as the number of opponents increased. Rather than relying on assumptions about what was causing this, I decided to test several of the robot's event-driven behaviours individually.

I conducted 100-round Robocode battles under different configurations and exported the results as CSV files. I then organised and compared the results in Excel to investigate how different event handlers affected the robot's performance.

The behaviours I investigated included:

- `onHitRobot()` – controls the robot's response after colliding with another robot.
- `onBulletHit()` – controls behaviour after successfully hitting an opponent.
- `onHitByBullet()` – controls the robot's response after being hit.

The testing showed that some behaviours that worked reasonably well in smaller battles became detrimental when more opponents were introduced. Based on the results, I proposed changes to the robot's movement, firing, collision behaviour, and dodge timing.

This part of the project gave me useful experience with systematic software testing and showed me the importance of using data to validate whether a behaviour is actually improving a program rather than relying solely on how the implementation is expected to behave.

## Technologies

- Java
- Robocode
- Git
- CSV data
- Microsoft Excel

## Repository Structure

`src/pillars/Amaterasu.java` contains the source code for the robot.

Robocode is required to run and test the robot.

## Team Project Notice

This was originally developed as a university team project. This repository is a sanitised portfolio version of the project, with personal information relating to other team members removed.
