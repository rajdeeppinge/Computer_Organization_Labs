// computing x^n
//inputs : x-->r0, n-->r1
//output : x^n-->r2
mov w x3,r0 // input x
mov w xA,r1 // input n

//limiting conditions
comp w x0,r0
jeq done
comp w x0,r1
jeq L3

mov w r0,r2
mov w r0,r3
mov w r1,r4
back: sub w x1,r4
jz done
L1: add w r2,r5
    sub w x1,r3
    jnz L1
mov w r5,r2
mov w r0,r3
mov w x0,r5
jmp back 
L3: mov w x1,r2
done: mov w x0,r3
halt
.end