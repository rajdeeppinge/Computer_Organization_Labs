// Program 5.1
//
// Program to find the sum of ten integers
// using indexed addressing
// 
.ORIG x100
.BIN
00000000
00000000
00000000
00000001
00000000
00000000
00000000
00000010
00000000
00000000
00000000
00000011
00000000
00000000
00000000
00000100
00000000
00000000
00000000
00000101
00000000
00000000
00000000
00000110
00000000
00000000
00000000
00000111
00000000
00000000
00000000
00001000
00000000
00000000
00000000
00001001
00000000
00000000
00000000
00001010
.ASM
.ORIG x10
       MOV w x00 R1       // Init R1 to 0
       MOV w x01 R2       // Init R2 to 1
       MOV w x00 R3       // Init R3 to 0
//
// Base address has 19 bits in instruction,
// to which five '0'a are appended.
// So now we supply only the 19 bits.
// 
LOOP:  LOAD W x08[R3] R4  // Load word from to R4
       ADD W R4 R1        // Add it into R1
       ADD W x04 R3	  // Incr memory address 
       ADD W x01 R2       // Increment R2 by 1
       COMP W x0A R2      // Compare final value
       JGE LOOP           // If greater, loop
       HALT
.START x10
.END
