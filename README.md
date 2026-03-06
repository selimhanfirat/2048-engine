# 2048 AI Engine

An experimental project exploring algorithmic decision-making in the game **2048**.

The project implements a full 2048 game engine together with automated agents that play the game using heuristic evaluation and **expectimax search**. The focus is not on building a perfect solver, but on studying how different search depths, heuristics, and implementation choices affect gameplay performance and computational cost.

The engine can run complete game simulations programmatically, making it possible to evaluate agents across **large batches of games with fixed random seeds** and collect statistical results.

The rules and mechanics follow the original 2048 game:  
https://en.wikipedia.org/wiki/2048_(video_game)

---

# Features

- Complete 2048 game engine
- Automated AI agents using **expectimax search**
- Two search variants:
  - **Default expectimax**
  - **Sampling-based expectimax optimization**
- Batch experiment runner for large-scale simulations
- **Board-state caching** using an LRU cache
- Deterministic experiments using fixed random seeds

---

# Architecture Overview

The project separates the game logic from the AI and experimentation components.
* engine/ core 2048 game mechanics
* ai/ expectimax agents and evaluation heuristics
* experiments/ experiment runners and evaluation tools


The engine handles:

- board state transitions
- tile spawning
- score updates
- termination detection

This separation allows the same engine to be used for both **human gameplay** and **automated experiments**.

---

# Search Optimizations

## Board State Caching

Board evaluations are cached using an **LRU cache implemented with `LinkedHashMap`**.

Since the same board positions frequently appear multiple times during search, caching avoids recomputing previously evaluated states and significantly improves search performance.

Key properties:

- LRU eviction policy
- implemented with `LinkedHashMap`
- reduces repeated evaluation of identical board states
- improves effective node throughput

---

## Sampling Optimization

Two expectimax variants are implemented:

### Default Search
Evaluates all possible chance outcomes during the search.

### Sampling Search
Samples a subset of chance outcomes to reduce the number of nodes explored.

This reduces computational cost while maintaining similar decision quality.

Example (depth 3):

| Agent | Avg runtime per game |
|------|----------------------|
| Default | ~5.2 s |
| Sampling | ~3.0 s |

This results in roughly **40–50% faster execution** with comparable performance.

---

# Experimental Results

Agents were evaluated by running **large batches of simulated games** with fixed seeds.

### Performance by Search Depth

| Depth | Mean Score | 2048 Rate | Notes |
|------|------|------|------|
| d=1 | ~11,400 | ~7–8% | shallow lookahead |
| d=2 | ~10,700 | ~2–3% | unstable decisions |
| d=3 | ~15,500 | ~18% | reliable baseline |
| d=4 | ~21,000 | ~40–46% | strong play |
| d=5 | ~23k–29k | up to ~80%* | limited runs |

\* small sample size

### Best Observed Results

- **Best score:** 49,588  
- **Highest tile reached:** 4096  
- **Best average score:** ~21k (depth 4)

Depth-4 search achieves strong play and reaches the **2048 tile in roughly 40–46% of games**.

---

# Computational Cost

Expectimax search grows rapidly with depth due to the large branching factor introduced by chance nodes.

Beyond **depth 4**, computation time increases significantly:

- depth-4 searches already require tens of seconds per game
- deeper searches quickly become expensive due to the exponential growth of the search tree

Because of this, depth-4 currently represents a practical balance between **search quality and runtime cost** for large experiment batches.

---
## Running the Project

### Requirements

- Java **21** or newer

The project uses the **Maven Wrapper**, so Maven does **not** need to be installed.

---

### Clone and Build

```bash
git clone https://github.com/selimhanfirat/2048-engine
cd 2048-engine
./mvnw package
```

This command will:

- download Maven automatically (if needed)
- compile the project
- run all tests
- produce the compiled classes

---

### Command Line Interface

The project includes a command line interface for running simulations and experiments.

Run the CLI:

```bash
./mvnw exec:java
```

If no arguments are provided, the program runs using **default parameters**.

---

### Getting Help

The CLI provides built-in help describing all available options.

```bash
./mvnw exec:java -Dexec.args="--help"
```

This will print the full list of supported options and their default values.

---

### Example

Run an experiment with a custom configuration:

```bash
./mvnw exec:java -Dexec.args="--ai default --depth 3 --runs 500 --seed 42"
```
This runs a 500-run experiment on depth 3 with the default ai implementation.

Multiple values can also be provided for some options to run experiment grids.

```bash
./mvnw exec:java -Dexec.args="--ai default --depth 3,4 --runs 500 --seed 42 --cache true,false"
```
This runs an experiment 2*2 = 4 times. It alternates between each configuration therefore even the experiment is interrupted there is an interpretable result. It runs the configurations (depth = 3, cache = false), (depth = 4, cache = false), (depth = 3, cache = true), (depth = 4, cache = true) 

---

### Run Tests

To execute the test suite:

```bash
./mvnw test
```
