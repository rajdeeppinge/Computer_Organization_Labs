//Program to divide one number by another and store quotient and remainder

.ORIG x10
.START x10

MOV b x4 R1	//dividend
MOV b x8 R2	//divisor

MOV b R1 R4	//remainder. Remainder will gradually decrease due to continuous subtraction.	
MOV b x0 R3	//quotient.

LOOP: COMP b R4 R2	//comparing if dividend is less than divisor
	JLT FINISH	//if yes end the program
	SUB b R2 R4	//subtract divisor from dividend, since division means continuous subtraction. Also we are keeping dividend in R4 so that eventually when it becomes less than divisor we automatically get the remainder in R4.
	ADD b x1 R3	//incrementing quotient by 1 on each subtraction.
	JMP LOOP	//looping

FINISH: HALT		//if divident < divisor finish the program

.END