//
// Program to test shift operations.
// Run in slow or medium mode, and
// watch contents of R1 change.
//
.ORIG x10
      MOV W x1 R1         // init R1
      ADD W R1 R1         // clear carry
//
L1:   SHIFT W L C R1 x1   // left shift
                          // 1 bit, with carry
      JNC L1
//
      MOV W x80000 R1
      AND W R1 R1        // clear carry
      SHIFT W L C R1 xC  // 12 bits left
//
L2:   SHIFT W R NC R1 x1 // right shift
                         // 1 bit, NC
      JNZ L2
//
      HALT
//
      MOV B x10 R1       // one bit set        
      AND B R1 R1        // clear carry
      MOV B x04 R2       // count
//
L3:   SHIFT B R NC R1 x4 // right shift
                         // 4 bits, NC
      SHIFT B L NC R1 x4 // shift back
      SUB B x01 R2       // decrement count
      JNZ L3
//
      HALT
.START x10
.END
