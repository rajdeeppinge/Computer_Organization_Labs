//
// testing ADD & SUB ops - byte mode
//
L1:     ADD b x7F R1     // init 1 > R1
	MOV b xF0 R2     // init 0 > R2
	MOV b R2 R3      //        & R3
//
L2:     ADD b R1 R2      // add
	SUB b R1 R2      // sub
	COMP b R2 R3     // comp
	JNZ ERR          // error ?
//
	ADD b x01 R2     // inc R2
	ADD b x01 R3     // inc R3
	COMP b x00 R3    // end?
	JNZ L2
	HALT
//
ERR:    HALT
