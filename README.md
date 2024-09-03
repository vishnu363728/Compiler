# Compiler

Full-fledged compiler from minijava to RISC-V, developed as a part of UCLA's compiler construction course

The compiler is developed in 4 parts:

1) Typechecker for minijava
2) Translator from minijava to Sparrow (intermediete language). The Sparrow representation removes the concept of 
classes, requiring the compiler to build internal class hierarchy representations and vtables to track class
methods. In my implementation, when an object is created, it contains a pointer to its parent (null if nonexistent),
allowing for access to parent variables or the parent object itself. 
3) Translator from Sparrow to Sparrow-V. This entailed the usage of registers and develop a stack representation 
with caller and callee saved registers. The assignment was graded on minimization of alloc/heap operations, requiring compiler optimizations that exploited register usage. 
4) Translator from Sparrow-V to RISC-V. This is the final compilation step, ensuring the logic from the Sparrow-V 
program fits with RISC-V syntax. 
