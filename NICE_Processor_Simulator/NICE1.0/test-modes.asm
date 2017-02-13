//
// Test of indexed addressing
//
// First add integers 1 to 10
//
.ASM
.ORIG x10
ST:   MOV w x00 R0      // Running sum
      MOV w x00 R1      // Index
      MOV b x0A R3      // Count
//
L1:   LOAD b x18[R1] R4 // Load byte
      ADD b R4 R0       // Add
      ADD b x01 R1      // Increment index
//
      SUB b x01 R3      // Decrement count
      JNZ L1            // Done?
//
// Now move them from x300 to x400
//
      MOV w x00 R0      // Re-initialize
      MOV w x00 R1
      MOV b x0A R3
//
L2:   LOAD b x18[R1] R4   // Load
      STORE b x20[R1] R4  // Store
      ADD b x01 R1        // 
//
      SUB b x01 R3
      JNZ L2
//
      HALT
//
.ORIG x300
.BIN
00001010
00001001
00001000
00000111
00000110
00000101
00000100
00000011
00000010
00000001
.ASM
.START ST
.END

