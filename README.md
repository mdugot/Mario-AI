# Mario AI


<p align="center">
  <img src="https://github.com/mdugot/Mario-AI/blob/master/mario.gif" />
</p>


A realtime Mario AI based on a genetic algorithm solve some procedurally generated level based on the orignal Mario levels through machine learning. 

The Mario emulation engine is based on the [Mario-AI-Framework](https://github.com/amidos2006/Mario-AI-Framework).

The genetic algorithm AI is inspired by the paper [Learning Levels of Mario AI Using Genetic Algorithms](https://core.ac.uk/download/pdf/44310211.pdf). </br>
The level generation use a n-gram model inspired by the paper [Procedural Content Generation via Machine Learning](https://arxiv.org/abs/1702.00539).

## Requirements

[JDK (Java Development Kit)](https://jdk.java.net/15/)  is required to build and run this project. </br>
It has been developed with the version 15.0.

## Usage

Clone the repository, move inside it and run `make` to build the the project into a JAR package.

```
git clone https://github.com/mdugot/Mario-AI.git
cd Mario-AI
make
```
Use jave to execute the JAR. It will generate a random level and let the AI solve it.

```
java -jar run.jar
```

The progam accept as first argument a number that will be used as the seed for the level random generation.
It allow you to play multiple time the same level.

```
java -jar run.jar 42
```
