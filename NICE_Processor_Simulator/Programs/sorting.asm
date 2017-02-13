.orig x10
.start x10

mov w x23 r1
store b [x200] r1

mov w x8 r2
store b [x201] r2

mov w x7 r3
store b [x202] r3

mov w x14 r4
store b [x203] r4

mov w x15 r5
store b [x204] r5

mov w x200 r6
mov w x204 r14

loop1: mov w x200 r12
	sub w #1 r14
	comp w x200 r14
	jge done

loop2: load b [r12] r9
	mov w r12 r13
	add w x1 r13
	load b [r13] r10
	comp b r9 r10
	jgt swap
	store b [r12] r9
	store b [r13] r10
	add w #1 r12
	comp w r14 r12
	jge loop2

jmp loop1

swap: mov b r9 r11
	mov b r10 r9
	mov b r11 r10
	ret

done: halt

.end