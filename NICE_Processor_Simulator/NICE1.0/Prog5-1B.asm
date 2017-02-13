// Program 5.1B
//
// Program to find the sum of ten bytes
// using indexed addressing
// 
.ORIG x100
.BIN
00000001
00000010
00000011
00000100
00000101
00000110
00000111
00001000
00001001
00001010
.ASM
.ORIG x10
       MOV w x00 R1       // Init R1 to 0
       MOV w x01 R2       // Init R2 to 1
       MOV w x00 R3       // Init R3 to 0
       MOV w x00 R4       // Init R4 to 0
//
// Base address has 19 bits in instruction,
// to which five '0'a are appended.
// So now we supply only the 19 bits.
// 
LOOP:  LOAD B x08[R3] R4  // Load word from to R4
       ADD B R4 R1        // Add it into R1
       ADD W x01 R3	  // Incr memory address 
       ADD W x01 R2       // Increment R2 by 1
       COMP W x0A R2      // Compare final value
       JGE LOOP           // If greater, loop
       HALT
.START x10
.END