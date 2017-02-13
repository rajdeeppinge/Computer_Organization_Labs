// 
// Program to test left shift - word mode
//
.ORIG x10
     MOV W x01 R1        // initialize R1
L1:  SHIFT W L C R1 x1   // shift left 1 bit,
                         // without carry
     JNC L1              // repeat until carry
//
// watch R1 in slow/medium mode 
// until carry flag is set
     MOV W x40000  R1    // only one bit set
L2:  SHIFT W R NC R1 x1  // right shift
     JNZ L2
//
//   watch R1 in slow/medium mode
//   until zero flag is set
     HALT
.START x10
.END
