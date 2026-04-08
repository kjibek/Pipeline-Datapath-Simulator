# Pipeline-Datapath-Simulator
This project simulates a 5-stage pipelined processor datapath using Java. It models how instructions move through each stage of a pipeline cycle-by-cycle, closely following concepts from computer architecture.

## Overview
This program emulates a classic pipeline with the following stages:
* IF (Instruction Fetch)
* ID (Instruction Decode)
* EX (Execute)
* MEM (Memory Access)
* WB (Write Back)

Each iteration represents one clock cycle. Multiple instructions are processed at the same time-- each in a different stage (just like a real CPU pipeline).

## Supported Instructions
* add
* sub
* lb (load byte)
* sb (store byte)
* nop (no operation)

## How it Works
The pipeline processes hexadecimal instructions provided by a .txt input file. 

Each cycle follows this process...
* Each stage reads from a READ pipeline register and writes to a WRITE version
* At the end of each cycle WRITE values are copied into READ

 After each cycle the program outputs:
 * register files
 * all pipeline registers
 * both READ and WRITE versions

At the end, there are four NOP instructions used to flush the pipeline. These don't do any real work and are only used to help the previous instructions move down all five stages of the pipeline.
