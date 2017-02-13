// Program 5.4B
//
// Function MAXB:
// To find the largest of 40 8-bit bytes.
//
// Input: Memory address of first byte in R0.
// Output: The largest byte returned in R1.
//
// Registers used: R0, R1, R2, R3
//
.ASM
.ORIG x10
MAXB:  LOAD b [R0] R1      // Load first byte in R1
       ADD  w x01 R0       // Make R0 point to second byte
       MOV  w x01 R2       // Initialize count R2 to 1
LOOP:  LOAD b [R0] R3      // Load byte from memory to R3
       COMP b R3 R1        // Compare it with R1
       JLE NEXT            // If smaller/equal, do not change
       MOV b R3 R1         // Move larger integer to R1
NEXT:  ADD w x01 R0        // Make R0 point to next byte 
       ADD w x01 R2        // Increment count R2 by 1
       COMP w x28 R2       // Compare contents of R2 to 40
       JNE LOOP            // If not equal, loop
       RET                 // Return to calling program
//
MAIN:  MOV w x80 R15       // Set up stack 
       MOV w x50 R0        // Address of data in R0
       CALL MAXB
       HALT                // Upon return, answer in R1
//
.ORIG x50
.BIN
00001000
00100000
00000100
00000010
00011000
00000000
01000000
10000110
00000000
01110000
00000000
00001001
00000110
00000001
00100100
00000011
00000000
01010001
00000000
00001000
11100000
11110000
00111000
00010000
00001110
00000000
00100010
00000010
00000000
10000010
00000000
00000111
00010010
00000000
00001000
00001111
00011000
11000000
00000000
00001100
//
.ASM
.START MAIN
.END
//