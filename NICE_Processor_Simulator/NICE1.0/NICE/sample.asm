//
// Sample program
//
.orig x20
loop: sub w #12 r3
      jnz loop
      mov w xfffff r11
      ret;
//
lab1: add w #24 r3
      call loop
      add b r5 r4
      mov w x12 r14
      halt
.start lab1
//
