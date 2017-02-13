// Program 5.4
//
// Function MAX:
// To find the largest of ten 32-bit integers.
//
// Input: Memory address of first integer in R0.
// Output: The largest integer returned in R1.
//
// Registers used: R0, R1, R2, R3
//
.ASM
.ORIG x10
MAX:   LOAD w [R0] R1      // Load first integer in R1
       ADD  w x04 R0       // Make R0 point to second integer
       MOV  w x01 R2       // Initialize count R2 to 1
LOOP:  LOAD w [R0] R3      // Load integer from memory to R3
       COMP w R3 R1        // Compare it with R1
       JLE NEXT            // If smaller/equal, do not change
       MOV w R3 R1         // Move larger integer to R1
NEXT:  ADD w x04 R0        // Make R0 point to next integer 
       ADD w x01  R2       // Increment count R2 by 1
       COMP w x0A R2       // Compare contents of R2 to ten
       JNE LOOP            // If not equal, loop
       RET                 // Return to calling program
//
MAIN:  MOV w x80 R15       // Set up stack 
       MOV w x50 R0        // Address of data in R0
       CALL MAX
       HALT                // Upon return, answer in R1
//
.ORIG x50
.BIN
00000000
00000000
00000000
00000010
00000000
00000000
00000000
00000110
00000000
00000000
00000000
00001001
00000000
00000000
00000000
00000011
00000000
00000000
00000000
00001000
00000000
00000000
00000000
00010000
00000000
00000000
00000000
00000010
00000000
00000000
00000000
00000111
00000000
00000000
00000000
00001111
00000000
00000000
00000000
00001100
//
.ASM
.START MAIN
.END
//