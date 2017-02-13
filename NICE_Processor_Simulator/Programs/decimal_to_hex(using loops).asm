// Program to convert DEC into HEX
// Take an input decimal num in r0 (0 to 9999)
// Watch the output HEX number in r1
.orig x10
mov w x9999,r0 
mov w r0,r4
and w xF000,r4
shift w r nc r4,xC
mov w r0,r5
and w xF00,r5
shift w r nc r5,x8
mov w r0,r6
and w xF0,r6
shift w r nc r6,x4
mov w r0,r7
and w xF,r7
// Multiplication of r4 by x3E8
mov w r4,r10
mov w x3E8,r11
call MUL
mov w r12,r4
// Multiplication of r5 by x64
mov w r5,r10
mov w x64,r11
call MUL
mov w r12,r5
// Multiplication of r6 by xA
mov w r6,r10
mov w xA,r11
call MUL
mov w r12,r6
// Final stage
add w r4 r5
add w r5 r6
add w r6 r7
mov w r7 r1
halt
.end
MUL: mov w x0,r12
or w x0,r10
jnz L0
ret
L0: add w r11,r12
    sub w x1,r10
    jnz L0
ret