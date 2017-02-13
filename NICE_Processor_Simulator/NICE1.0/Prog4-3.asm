// Program 4.3
//
// Program to add three 32-bit integers. 
// If the sum is less than 1000 (hex), 
// it is changed to 1000 (hex).
// 
.ORIG X10
    LOAD W X204 R2      // ... second operand into R2
    LOAD W X208 R3      // ... third operand into R3
    ADD W R2 R1	        // Add second operand into the first
    ADD W R3 R1	        // Add third operand into the sum
    COMP W X1000 R1     // Compare sum to hex 1000
    JGE	DONE            // If greater or equal, jump to DONE
    MOV	W X1000 R1      // Change contents of R1 to 1000
    JMP DONE
.ORIG X30
DONE: STORE W X200 R1   // Store result at address X200 
    HALT                // Halt the program