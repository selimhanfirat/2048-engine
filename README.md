# 2048 AI Engine

An experimental project exploring algorithmic decision-making in the game **2048**.

The project implements a full 2048 game engine together with automated agents that play the game using **heuristic evaluation and expectimax search**. The goal is not to build a perfect solver, but to study how search depth, heuristics, and implementation choices affect gameplay performance and computational cost.

The engine supports both **interactive play** and **large batch experiments** for evaluating AI performance under controlled conditions.

The rules follow the original 2048 game:  
https://en.wikipedia.org/wiki/2048_(video_game)

---

# Features

- Complete **2048 game engine**
- AI agents based on **Expectimax search**
- Two search variants
  - **Default Expectimax**
  - **Sampling Expectimax optimization**
- Interactive **terminal UI (TUI)** for watching AI play
- Batch **experiment runner**
- Deterministic experiments using **fixed seeds**
- Experiment result output to automatically generated markdown files.
- **Board-state caching** using an LRU cache

---

# Architecture Overview

The project separates the game logic, AI agents, and experiment infrastructure.

```
game/
    core/      board state and mechanics
    rules/     game rules
    spawn/     tile generation
    runtime/   game session execution

ai/
    expectimax search implementations
    heuristic evaluators

app/
    CLI entrypoint
    experiment runner
    output sinks
```

The engine handles:

- board state transitions
- tile spawning
- score updates
- termination detection

This separation allows the same engine to be used for:

- **interactive gameplay**
- **AI simulations**
- **large experimental batches**

---

# Interactive Mode (Default)

Running the program without arguments launches **interactive play mode** where the AI plays the game and renders the board in a terminal UI.

The interface shows:

- current score
- maximum tile
- AI search depth
- AI decision time
- number of nodes explored
- evaluation calls
- cache hit rate


Two pacing modes are available:

### Auto Mode
The AI plays continuously with a minimum delay between moves.

### Step Mode
Each move requires user input.

Controls:

```
SPACE / ENTER  -> next move
q              -> quit
```

---

# Search Algorithm

The AI uses **Expectimax search**, which models the randomness of tile spawning.

The search tree contains two types of nodes:

- **Decision nodes** – the AI selects a move
- **Chance nodes** – random tile spawns (2 or 4)

At the leaves of the search tree a **heuristic evaluation function** estimates board quality.

The heuristic considers factors such as:

- board smoothness
- monotonicity
- number of empty tiles
- tile positioning

---

# Search Optimizations

## Board State Caching

Board evaluations are cached using an **LRU cache implemented with `LinkedHashMap`**.

Because identical board positions often appear multiple times in the search tree, caching avoids redundant computations.

Properties:

- LRU eviction policy
- implemented with `LinkedHashMap`
- reduces repeated evaluations
- improves search throughput

The UI displays cache efficiency as:

```
Cache hit 67.6%
```

---

## Sampling Optimization

Two Expectimax variants are implemented.

### Default Search
Evaluates **all possible chance outcomes**.

### Sampling Search
Samples a subset of chance outcomes to reduce branching.

This reduces computation while maintaining similar decision quality.

Example (depth 3):

| Agent | Avg runtime per game |
|------|----------------------|
| Default | ~5.2 s |
| Sampling | ~3.0 s |

This results in roughly **40–50% faster execution**.

---

# Experimental Results

Agents were evaluated by running **large batches of games with fixed seeds**.

### Performance by Search Depth

| Depth | Mean Score | 2048 Rate |
|------|------|------|
| d=1 | ~11k | ~7–8% |
| d=2 | ~10k | ~2–3% |
| d=3 | ~15k | ~18% |
| d=4 | ~21k | ~40–46% |
| d=5 | ~23k–29k | up to ~80%* |

\* limited sample size

### Best Observed Results

- **Best score:** 49,588
- **Highest tile:** 4096
- **Best average score:** ~21k (depth 4)

Depth-4 search currently provides the best balance between **performance and runtime**.

---

# Computational Cost

Expectimax search grows exponentially due to the large branching factor created by random tile spawns.

Typical growth:

| Depth | Nodes Explored |
|------|----------------|
| 3 | ~2k |
| 4 | ~20k |
| 5 | ~200k |
| 6 | ~2M+ |

Because of this growth, depth-4 represents a practical limit for large experiment batches.

---

# Running the Project

## Requirements

- **Java 21+**

The project uses the **Maven Wrapper**, so Maven does **not** need to be installed.

---

# Build

```bash
git clone https://github.com/selimhanfirat/2048-engine
cd 2048-engine
./mvnw package
```

This will:

- download Maven if necessary
- compile the project
- run tests
- build the project

---

# Running the Program

Run the application:

```bash
./mvnw exec:java
```

By default this launches **interactive play mode**.

---

# CLI Usage

```
./mvnw exec:java -Dexec.args="..."
```

Modes:

```
play        interactive AI gameplay (default)
experiment  run experiment batches
```

---

# Play Mode Options

```
--ai default|sample       (default: default)
--depth <n>               (default: 4)
--cache <bool>            (default: true)
--ignore4 <n>             (default: 6)
--seed <n>                (default: 42)
--pace auto|step          (default: auto)
--delay-ms <n>            (default: 100)
```

Example:

```
./mvnw exec:java -Dexec.args="play --depth 4"
./mvnw exec:java -Dexec.args="play --ai sample --depth 4 --pace step"
./mvnw exec:java -Dexec.args="play --seed 42 --pace auto --delay-ms 1000"
```

---

# Experiment Mode

Runs batches of games to evaluate AI configurations.

Options:

```
--ai default,sample
--depth 2,3,4
--cache true,false
--ignore4 <n>
--runs <n>
--seed <n>
--checkpoints <n>
--warmup <fraction|percent>
```

Example:

```
./mvnw exec:java -Dexec.args="experiment --ai default --depth 3 --runs 500 --seed 42"
```

Grid experiments are supported:

```
./mvnw exec:java -Dexec.args="experiment --ai default --depth 3,4 --cache true,false --runs 500"
```

This runs multiple experiment configurations in sequence.

---

# Running Tests

```
./mvnw test
```

---

# Project Goals

This project is intended as a **research and experimentation platform** for studying AI search strategies in stochastic games.

Focus areas include:

- search depth vs performance
- heuristic design
- sampling optimizations
- caching strategies
- runtime cost vs decision quality

---

## Notes and Future Work

This repository contains the **first version** of the project. Development is currently **on hold**, but the codebase will likely be revisited in the future.

Planned improvements include:

- new **sampling strategies** for expectimax
- board representation using **packed longs**
- **Zobrist hashing** for faster state caching

### UI

The current **terminal UI (TUI)** was added mainly for debugging and observing the AI during development. It is intentionally simple and should be considered a temporary interface.

In the future, it will likely be replaced with a **proper frontend**, since the engine and AI logic are already separated from the UI layer.