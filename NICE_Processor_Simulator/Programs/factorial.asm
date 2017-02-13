.orig x10
.start x10

mov w x3 r1

comp w x0 r1
jeq limit
comp w x1 r1
jeq limit

mov w r1 r2
mov w x0 r3

factorial: sub w x1 r1
		jz done
		mov w r1 r4
multiply: add w r2 r3
	sub w x1 r4
	jnz multiply

mov w r3 r2
mov w x0 r3
jmp factorial

limit: mov w #1 r2
done: halt

.end