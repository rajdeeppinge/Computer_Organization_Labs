.ORIG x10
.START x10

MOV b x1A R5	
MOV b x1 R4	//we observe that for odd numbers LSB is always 1

AND b R5 R4	//ANDing odd number with 1 will store 1 in r4 while ANDing with even number will
		//make contents of r4 = 0 and will set the zero flag 
JZ EVEN		//if zero flag is set, the number is even
MOV b x1 R3	//if number is odd, this condition is executed

HALT		//if number is odd we don't want further code to be executed therefore HALT

EVEN: MOV b x1 R2	//if number is even 1 is stored in r2

.END
