# Tubes1_EnteGoalKiperPalingBaikDiDunia
<h2 align="center">
  🌌 Galaxio Bot with Greedy Algorithm 🌌<br/>
</h2>
<hr>

## Table of Contents
1. [General Info](#general-information)
2. [Creator Info](#creator-information)
3. [Features](#features)
4. [Technologies Used](#technologies-used)
5. [Setup](#setup)
6. [Usage](#usage)
7. [Screenshots](#screenshots)
7. [Structure](#structure)
8. [Project Status](#project-status)
9. [Room for Improvement](#room-for-improvement)
10. [Acknowledgements](#acknowledgements)
11. [Contact](#contact)

<a name="general-information"></a>

## General Information
A simple Galaxio Bot with Greedy Algorithm. A greedy algorithm is a method for solving optimization problems by making the locally choice at each step with the hope of finding a global optimum. In this project, the greedy algorithm is applied to solve a particular problem that Galaxio Bot was designed for. This project is made for the Tubes 1 of IF2211 - Strategi Algoritma course in ITB. 

<a name="creator-information"></a>

## Creator Information

| Nama                        | NIM      | E-Mail                      |
| --------------------------- | -------- | --------------------------- |
| Saddam Annais Shaquile      | 13521121 | 13521121@std.stei.itb.ac.id |
| M. Dimas Sakti Widyatmaja   | 13521160 | 13521160@std.stei.itb.ac.id |
| Mohammad Rifqi Farhansyah   | 13521166 | 13521166@std.stei.itb.ac.id |

<a name="features"></a>

## Features
- The Galaxio Bot can solve the problem of finding the best path to the goal (win the game).

<a name="technologies-used"></a>

## Technologies Used
- Java - version 11
- IntelliJ IDEA - version 2022.2.2
- NodeJS - version 18
- .Net Core - version 3.1
- Maven - version 3.9.0

> Note: The version of the libraries above is the version that we used in this project. You can use the latest version of the libraries.

<a name="setup"></a>

## Setup
1. Download all the required libraries.
2. Open the project in IntelliJ IDEA.
3. Run the project.

<a name="usage"></a>

## Usage
1. Clone this repository
```bash
git clone https://github.com/rifqifarhansyah/Tubes1_EnteGoalKiperPalingBaikDiDunia.githttps://github.com/rifqifarhansyah/Tubes1_EnteGoalKiperPalingBaikDiDunia.git
```
2. Open the file directory
```bash
cd Tubes1_EnteGoalKiperPalingBaikDiDunia
```
3. Go to the target directory that contains the java compiled file
```bash
cd target
```
4. Run the java file
```bash
java -jar Tubes1_EnteGoalKiperPalingBaikDiDunia.jar
```

<a name="screenshots"></a>

## Screenshots
<p>
  <img src="/image/Torpedo1.png/">
  <p>Figure 1. Launch Torpedo (Attack Phase)</p>
  <nl>
  <img src="/image/Torpedo2.png/">
  <p>Figure 2. Torpedo was Successfully Landed </p>
  <nl>
  <img src="/image/Teleport1.png/">
  <p>Figure 3. Launch Teleporter (Attack Phase)</p>
  <nl>
  <img src="/image/Teleport2.png/">
  <p>Figure 4. Teleporter was Successfully Landed</p>
  <nl>
  <img src="/image/Shield.png/">
  <p>Figure 5. Shield Activated (Defence Phase)</p>
  <nl>
  <img src="/image/Default.png/">
  <p>Figure 6. Catch Food (Default Phase)</p>
  <nl>
</p>

<a name="structure"></a>

## Structure
```bash
│   .gitignore
│   README.md
│   run.bat
│
├───.vscode
│       settings.json
│
├───doc
│       Get Started Galaxio.pdf
│       Tugas Besar 1 IF2211 Strategi Algoritma 2023.pdf
│
├───image
│       Default.png
│       Shield.png
│       Teleport1.png
│       Teleport2.png
│       Torpedo1.png
│       Torpedo2.png
│
└───src
    └───JavaBot
        │   Dockerfile
        │   pom.xml
        │   target.zip
        │
        ├───.github
        │   └───workflows
        │           .gitkeep
        │
        ├───src
        │   └───main
        │       └───java
        │           │   Main.java
        │           │
        │           ├───Enums
        │           │       Effects.java
        │           │       ObjectTypes.java
        │           │       PlayerActions.java
        │           │
        │           ├───Models
        │           │       GameObject.java
        │           │       GameState.java
        │           │       GameStateDto.java
        │           │       PlayerAction.java
        │           │       Position.java
        │           │       World.java
        │           │
        │           └───Services
        │                   BotService.java
        │                   GreedyCommand.java
        │                   PanduanTeleportSupernova.txt
        │                   Radar.java
        │                   TeleSuperNova.java
        │
        └───target
            │   JavaBot.jar
            │
            ├───classes
            │   │   Main.class
            │   │
            │   ├───Enums
            │   │       Effects.class
            │   │       ObjectTypes.class
            │   │       PlayerActions.class
            │   │
            │   ├───Models
            │   │       GameObject.class
            │   │       GameState.class
            │   │       GameStateDto.class
            │   │       PlayerAction.class
            │   │       Position.class
            │   │       World.class
            │   │
            │   └───Services
            │           BotService.class
            │           GreedyCommand.class
            │
            ├───libs
            │       azure-core-1.13.0.jar
            │       gson-2.8.5.jar
            │       jackson-annotations-2.11.3.jar
            │       jackson-core-2.11.3.jar
            │       jackson-databind-2.11.3.jar
            │       jackson-dataformat-xml-2.11.3.jar
            │       jackson-datatype-jsr310-2.11.3.jar
            │       jackson-module-jaxb-annotations-2.11.3.jar
            │       jakarta.activation-api-1.2.1.jar
            │       jakarta.xml.bind-api-2.3.2.jar
            │       netty-tcnative-boringssl-static-2.0.35.Final.jar
            │       okhttp-3.11.0.jar
            │       okio-1.14.0.jar
            │       reactive-streams-1.0.2.jar
            │       reactor-core-3.3.12.RELEASE.jar
            │       rxjava-2.2.2.jar
            │       signalr-1.0.0.jar
            │       slf4j-api-1.7.25.jar
            │       slf4j-simple-1.7.25.jar
            │       stax2-api-4.2.1.jar
            │       woodstox-core-6.2.1.jar
            │
            ├───maven-archiver
            │       pom.properties
            │
            └───maven-status
                └───maven-compiler-plugin
                    └───compile
                        └───default-compile
                                createdFiles.lst
                                inputFiles.lst
```

<a name="project-status">

## Project Status
Project is: _complete_

<a name="room-for-improvement">

## Room for Improvement
Room for Improvement:
- Find the most optimal path to the goal.
- Make the Galaxio Bot more efficient.

<a name="acknowledgements">

## Acknowledgements
- Thanks To Allah SWT
- This project was inspired by this [article](
https://bit.ly/SpekTubes1Stima)

<a name="contact"></a>

## Contact
<h4 align="center">
  Made by EnteGoalKiperPalingBaikDiDunia<br/>
  2023
</h4>
<hr>
