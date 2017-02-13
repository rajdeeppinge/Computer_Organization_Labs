// Program 4.4
//
// Program to find the sum of integers from 1 to 100
// 
MOV #0, R1       // Init R1 to 0
MOV #1, R2       // Init R2 to 1
LOOP: ADD R2, R1 // Add R2 into R1
ADD #1, R2       // Incr R2 by 1
COMP R2, x100    // Compare value in R2 to 100
JLE LOOP         // If less-or-equal, loop  					
HALT             // Halt 
