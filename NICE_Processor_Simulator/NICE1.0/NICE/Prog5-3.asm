// Program 5.3
//
// Function to copy a string of characters.      
// Input: R0 - Memory address of source string
//        R1 - Memory address of destination string
//        R2 - Length of source string
//
// Assumes non-overlapping source & destination locations.
//
// Registers used: R0, R1, R2, R3  
// 
COPY: LOAD B [R0] R3    // Load source byte into R3
      STORE B [R1] R3   // Store it at destination address
      ADD W x01 R0      // Add 1 to source address
      ADD W x01 R1      // Add 1 to destination address
      SUB W x01 R2      // Decrement count by 1
      JNZ COPY	        // If not zero, iterate
      RET               // Return to calling program
//
MAIN: MOV W x100 R15    // Set up stack
      MOV W x200 R0     // Source
      MOV W x300 R1     // Destination
      MOV W x0A R2      // Count
      CALL COPY
      HALT
//
.ORIG x200
.BIN
01001000
01100000
01000100
01000010
01011000
01000000
01000000
11000110
01000000
01110000
.ASM
.ORIG x300
.BIN
00000000
00000000
00000000
00000000
00000000
00000000
00000000
00000000
00000000
00000000
.ASM
.START MAIN
.END
