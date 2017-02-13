// Program 4.2
//
// Program to add three 32-bit integers
// 
.ORIG x200
.BIN
DAT1: 00000000
      00000000
      00000000
      00000001
DAT2: 00000000
      00000000
      00000000
      00000010
DAT3: 00000000
      00000000
      00000000
      00000011
.ASM
.ORIG x10
    LOAD  w DAT1 R1     // Load first int to R1
    LOAD  w DAT2 R2     // Second int to R2
    LOAD  w DAT3 R3     // Third int to R3
    ADD   w R2 R1       // Add second int to first
    ADD   w R3 R1       // Add third  
    STORE w DAT1 R1     // Store result
    HALT          
.START X10
.END
       
 
 