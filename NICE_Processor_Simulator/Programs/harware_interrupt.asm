//program for hardware interrupt
.CLEAR
.start x20

.orig x00
mov b x3 r4
jmp done

.orig x20
mov b x2 r2
loop: mov b x3 r3
	jmp loop

done: halt

.end