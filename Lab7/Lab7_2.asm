.ORIG x10
.START x10

MOV w xA9  R5	//Storing hexasecimal number

MOV w R5 R1	//for DIVISION we need dividend in R1
MOV w x64 R2	//for DIVISION we need divisor in R2. x64 is equal to 100 in decimal we first divide by 100 to get digit at 100th's place
CALL DIVISION
MOV w R3 R6	//after DIVISION quotient is in R3 which is the digit at 100th's place

MOV w R4 R1	//Remainder is in R4 which is the remaining number
MOV w xA R2	//Now dividing by decimal 10
CALL DIVISION
MOV w R3 R7 	//digit at 10th's place

MOV w R4 R8	//remainder is the digit at unit's place

SHIFT w L NC R6 x8	//shifting by 8 bits since hexadecimal digit at 100th's place must be shifted by 8 bits to put it at 100th's place, 3rd position from right
SHIFT w L NC R7 x4	//similar to above comment.

OR w R6 R10		//ORing to put the digits in R10
OR w R7 R10
OR w R8 R10

.END

//function to divide two numbers and get their quotient and remainder
//INPUT:
//R1: contains dividend
//R2: contains divisor
//OUTPUT:
//R3: contains quotient
//R4: contains remainder

DIVISION: MOV w R1 R4	//remainder. Remainder will gradually decrease due to continuous subtraction.	
		MOV b x0 R3	//quotient.

		LOOP: COMP w R4 R2	//comparing if dividend is greater than or equal to divisor
			JGE OP		//if yes perform operation
			RET		//else return

		OP:	SUB w R2 R4	//subtract divisor from dividend, since division means continuous subtraction. Also we are keeping dividend in R4 so that eventually when it becomes less than divisor we automatically get the remainder in R4.
			ADD b x1 R3	//incrementing quotient by 1 on each subtraction.
			JMP LOOP	//looping