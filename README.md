# 2048 Engine

This is a small experimental project for exploring the mechanics of the game 2048 and different ways of making decisions in the game. The initial focus is on implementing the core game logic correctly, including move execution, tile merging, score calculation, and game termination.

At the current stage, the engine can run complete game sessions from start to finish, making it possible to simulate games programmatically and collect results. This provides a foundation for experimenting with automated players and evaluating their behavior under controlled conditions.

The project is being gradually extended with simple heuristic-based decision-making systems, starting with greedy approaches and moving toward search-based methods such as expectimax. The goal is not to build a highly optimized solver, but to understand how different modeling and evaluation choices affect gameplay quality and performance.

The rules and mechanics follow the original 2048 game, which is documented here:  
https://en.wikipedia.org/wiki/2048_(video_game)

## Experimental Results

The following results are snapshots from local experiments and are not guaranteed to be optimal or reproducible across machines.

2026-02-04 — Expectimax (depth 2, empty-cells + monotonicity heuristic, with caching)  
- Runs: 1,000
- Average score: ~10,900
- Best max tile: 2048
- Average max tile: ~800
- Reached 2048: 2.90% (29 / 1000)

