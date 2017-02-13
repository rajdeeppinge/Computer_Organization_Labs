.ORIG x10
.START x10

MOV b x7 R7		//storing 7 in r7
MOV b R7 R8		//since we want result in r8 we shift contents of r7 to r8
SHIFT b L NC R8 x3	//shifting left without carry. Shifting 1 bit to the left multiplies value by 2
			//therefore shifting 3 bits will multiply value by 8
.END