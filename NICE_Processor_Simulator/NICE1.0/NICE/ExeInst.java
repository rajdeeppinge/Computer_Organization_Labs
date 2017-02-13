import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.io.*;

/**
*   This class handles the execution of code written by user
*   -- one instruction at a time.
*   Last modified: December 2008, Naresh Jotwani
*
*/
public class ExeInst extends JPanel {
    private String MA;      	/* contents of MAR */
    private String C;       	/* contents of register C connected at output of ALU */
    private String MD="";   	/* contents of MDR */
    private String CON="0"; 	/* whether a condition in JCond, JFlag etc.evaluates to true\false */
    private String IR;		/* contents of IR */
    private String A;		/* contents of register A, an input to ALU is provided through it */
    private String B;		/* contents of register B, an input to ALU is provided through it */
    private String PORTDATA;	/* data provided from a port or data provided to a port */
    private int src;		/* contents of source register for any instruction */
    private int dst;		/* contents of destination register for any instruction */
    private int portno=0;	/* port number provided */
    private int i=0;

    private String opcode;  	/* opcode (binary code) for any instruction */
    String ALUOUT="";		/* contents after execution of any ALU operation */

    Container contentPane;  	/* Container where GUI elements are displayed */
    TablePanel controlPanel;

    private int noRows = 0;

    Toolkit tk = Toolkit.getDefaultToolkit();
    Class cs = this.getClass();
  
    public ExeInst () {
    }

    /**
    * This method handles the execution of any instruction.
    * It fetches the next instruction to be excuted (through fetch cycle).
    * Then passes control to control unit for further steps.
    */
    public void controlUnit(Boolean overRide) {
        MA = CustTable.pad(LC.pc,-8,"0");
        Boolean isBPset = (Boolean) LC.memPanel.table.getValueAt(Integer.parseInt(MA,16),0);
        // Either BP is not set, or BP is being overridden for one step
        if(overRide||!isBPset) {     
            // common fetch phase 
            readMemoryWord();
            IR = MD;
            LC.regPanel.valTable.setValueAt("x"+(IR.toUpperCase()),0, 3);
            // move "<<< PC" to new location
            LC.moveHighlight();
            // decode and execute the instruction
            Decode_Execute();
        }
        else {
            LC.BPseen = true;
        }
    }


    /**
    * This method handles the decoding and execution of instruction.
    * Modified - NJ - October 2008
    */
    private void Decode_Execute() {
        String in = HexToBinary(IR,32);

        if(in.substring(0,2).equals("00")) {
            // LOAD
            opcode="00";
            if(in.charAt(3)=='0') {
                // direct mode
                dst = Integer.parseInt(in.substring(4,8),2);
                incrPC(4);
                MA = "00"+IR.substring(2,8);
                if(in.charAt(2)=='0') {
                    // byte mode
                    readMemoryByte();
                    writeRegisterByte( MD, dst );
                }
                if(in.charAt(2)=='1') {
                    // word mode
                    readMemoryWord();
                    writeRegisterWord( MD, dst );
                }
            }
            else if(in.charAt(3)=='1') {
                // indexed or indirect mode
                if(in.charAt(4)=='1') {
                    // indexed mode
                    dst = Integer.parseInt(in.substring(28,32),2);
                    incrPC(4);
                    getIndexedAddress();
                    if(in.charAt(2)=='0') {
                        // byte mode
                        readMemoryByte();
                        writeRegisterByte( MD, dst );
                    }
                    if(in.charAt(2)=='1') {
                        // word mode
                        readMemoryWord();
                        writeRegisterWord( MD, dst );
                    }
                }
                if(in.charAt(4)=='0') {
                    // indirect mode
                    src = Integer.parseInt(in.substring(8,12),2);
                    dst = Integer.parseInt(in.substring(12,16),2);
                    incrPC(2);
                    MA = (String) LC.regPanel.valTable.getValueAt((src/2)+1, 2*(src%2) +1);
                    MA = MA.substring(1);        /* drop 'x' */
                    MA = "00"+MA.substring(2);   /* drop high order byte */
                    if(in.charAt(2)=='0') {
                        // byte mode
                        readMemoryByte();
                        writeRegisterByte( MD, dst );
                    }
                    if(in.charAt(2)=='1') {
                        // word mode
                        readMemoryWord();
                        writeRegisterWord( MD, dst );
                    }
                }
            }
        }

        else if(in.substring(0,2).equals("01")) {
            // STORE
            opcode="01";
            if(in.charAt(3)=='0') {
                // direct mode
                src = Integer.parseInt(in.substring(4,8),2);
                incrPC(4);
                MA = "00"+IR.substring(2,8);
                if(in.charAt(2)=='0') {
                    // byte mode
                    MD = readRegisterByte( src );
                    writeMemoryByte();
                }
                if(in.charAt(2)=='1') {
                    // word mode
                    MD = readRegisterWord( src );
                    writeMemoryWord();
                }
            }
            else if(in.charAt(3)=='1') {
                // indexed or indirect mode
                if(in.charAt(4)=='1') {
                    // indexed mode
                    src = Integer.parseInt(in.substring(28,32),2);
                    incrPC(4);
                    getIndexedAddress();
                    if(in.charAt(2)=='0') {
                        // byte mode
                        MD = readRegisterByte( src );
                        writeMemoryByte();
                    }
                    if(in.charAt(2)=='1') {
                        // word mode
                        MD = readRegisterWord( src );
                        writeMemoryWord();
                    }
                }
                if(in.charAt(4)=='0') {
                    // indirect mode
                    dst = Integer.parseInt(in.substring(8,12),2);
                    src = Integer.parseInt(in.substring(12,16),2);
                    incrPC(2);
                    MA = (String) LC.regPanel.valTable.getValueAt((dst/2)+1, 2*(dst%2) +1);
                    MA = MA.substring(1);        /* drop 'x' */
                    MA = "00"+MA.substring(2);   /* drop high order byte */
                    if(in.charAt(2)=='0') {
                        // byte mode
                        MD = readRegisterByte( src );
                        writeMemoryByte();
                    }
                    if(in.charAt(2)=='1') {
                        // word mode
                        MD = readRegisterWord( src );
                        writeMemoryWord();
                    }
                    // LC.writeMessage( MA+":"+MD );
                }
            }
        }

        else if(in.substring(0,2).equals("10")) {
            // SHIFT, JMP, JCond, JFlag, JNFlag, CALL

            String instrRec = in.substring(2,5);
            opcode = in.substring(0,8);

            if(instrRec.equals("000")) {
                // JMP instruction
                incrPC(4);  
                LC.pc = IR.substring(2);
            }
            else if(instrRec.equals("001")) {
                // Jcond instruction
                incrPC(4);
                evalCondition();
                if(CON.equals("1")) {
                    LC.pc = IR.substring(2);
                }
            }
            else if(instrRec.equals("010")) {
                // Jflag instruction
                incrPC(4);
                evalFlag();
                if(CON.equals("1")) {
                    LC.pc = IR.substring(2);
                }
            }
            else if(instrRec.equals("011")) {
                // JNflag instruction
                incrPC(4);
                evalNotFlag();
                if(CON.equals("1")) {
                    LC.pc = IR.substring(2);
                }
            }
            else if(instrRec.equals("100")) {
                // CALL instruction
                int SPreg = 15;
                incrPC(4);
                MD = "00"+LC.pc;                   // make up 4 bytes
                MA = readRegisterWord( SPreg );
                writeMemoryWord();                 // store PC at current SP
                LC.pc = IR.substring(2);           // last six bytes
                // LC.writeMessage("new pc = "+LC.pc);
                int tempSP = Integer.parseInt(MA,16);
                String temp = CustTable.pad(Integer.toHexString(tempSP+4),-8,"0");
                writeRegisterWord( temp, SPreg );  // incremented SP 
            }
            else if (instrRec.equals("101")) {
                // RET or HALT
                if(in.charAt(5)=='0') {
                    // RET
                    int SPreg = 15;
                    incrPC(1);
                    String temp = readRegisterWord( SPreg );
                    int tempSP = Integer.parseInt(temp,16);
                    temp = CustTable.pad(Integer.toHexString(tempSP-4),-8,"0");
                    writeRegisterWord( temp, SPreg );
                    MA = temp;
                    readMemoryWord();
                    LC.pc = MD.substring(2);
                }
                else {
                    // HALT
                    incrPC(1);
                    int x;
                    LC.halt = true;
                }
            }

            // NOTE - 10110 opcode is unused

            else if(instrRec.equals("111")) {
                // SHIFT
                incrPC(2);
                dst = Integer.parseInt(IR.substring(2,3),16);
                B = readRegisterWord( dst );
                int bitsToShift = Integer.parseInt(IR.substring(3,4),16); // # of bits to shift
                if(in.charAt(5)=='0') {
                    // byte mode 
                    shiftByte( bitsToShift );
                    writeRegisterByte( ALUOUT, dst );
                }
                if(in.charAt(5)=='1') {
                    // word mode 
                    shiftWord( bitsToShift );
                    writeRegisterWord( ALUOUT, dst );
                }
            }
        }

        else {
            // Group 3 instruction
            String logical = in.substring(3,6);
            String inOut = in.substring(0,4);
            opcode = in.substring(0,8);
            boolean flag=true;

            if(inOut.equals("1110")) {
                // IN
                dst = Integer.parseInt(in.substring(4,8),2);
                portno = Integer.parseInt(in.substring(8,16),2); 
                incrPC(2);
                if (portno>=0&&portno<=23 ) {    // simulator has only ports 0 to 23
                    String val = getInputData(CustTable.pad(Integer.toHexString(portno),-2,"0").toUpperCase());
                    int intVal = Integer.parseInt(val,2);
                    MD = CustTable.pad(Integer.toHexString(intVal),-8,"0");
                    writeRegisterByte( MD, dst );
                    LC.IOdevicePanel.table.setValueAt(HexToBinary(MD.substring(6),8),portno/2,1+2*(portno%2));
                }
            }
            else if(inOut.equals("1111")) {
                // OUT 
                src = Integer.parseInt(in.substring(4,8),2);
                portno = Integer.parseInt(in.substring(8,16),2);
                incrPC(2);
                if (portno>=0&&portno<=23 ) {     // simulator has only ports 0 to 23
                    MD = readRegisterByte( src );
                    LC.IOdevicePanel.table.setValueAt(HexToBinary(MD.substring(6),8),portno/2,1+2*(portno%2));
                }
            }
            else if(!logical.equals("000") && in.charAt(2)=='0') {
                // ADD, SUB, AND, OR, XOR, NOT, COMP
                //
                flag = true;
                if(logical.equals("111")) // if COMP
                    flag = false;

                if(in.charAt(7)=='1') {
                    // immediate mode
                    dst = Integer.parseInt(in.substring(28),2);
                    incrPC(4);
                    String temp = IR.substring(2,7);
                    // if(Integer.parseInt(temp.substring(0,1),16)<8) // sign extend
                    //    A = "000"+temp;
                    // else
                    //     A = "FFF"+temp;
                    A = "000"+temp;
                    if(in.charAt(6)=='0') {
                        // byte mode 
                        B = readRegisterByte( dst );
                        ALUoperation();
                        if (flag)
                            writeRegisterByte( ALUOUT, dst );
                    }
                    if(in.charAt(6)=='1') {
                        // word mode 
                        B = readRegisterWord( dst );
                        ALUoperation();
                        if (flag)
                            writeRegisterWord( ALUOUT, dst );
                    }
                } // immediate mode

                else {
                    // register mode
                    dst = Integer.parseInt(in.substring(12,16),2);
                    src = Integer.parseInt(in.substring(8,12),2);
                    incrPC(2);
                    if(in.charAt(6)=='0') {
                        // byte mode 
                        A = readRegisterByte( src );
                        B = readRegisterByte( dst );
                        ALUoperation();
                        if (flag)
                            writeRegisterByte( ALUOUT, dst );
                    }
                    if(in.charAt(6)=='1') {
                        // word mode 
                        A = readRegisterWord( src );
                        B = readRegisterWord( dst );
                        ALUoperation();
                        if (flag)
                            writeRegisterWord( ALUOUT, dst );
                    }
                } // register mode
            }

            else if(logical.equals("000")) {
                // MOV instruction
                if(in.charAt(7)=='1') {
                    // immediate mode
                    dst = Integer.parseInt(in.substring(28),2);
                    incrPC(4);
                    String temp = IR.substring(2,7);
                    // if(Integer.parseInt(temp.substring(0,1),16)<8) // sign extend 
                    //    A = "000"+temp;
                    // else
                    //    A = "FFF"+temp;
                    A = "000"+temp;  // extend 20 bit immediate operand to 32 bits 
                    if(in.charAt(6)=='0') {
                        // byte mode 
                        writeRegisterByte( A, dst );
                    }
                    if(in.charAt(6)=='1') {
                        // word mode 
                        writeRegisterWord( A, dst );
                    }
                }
                else {
                    // register mode
                    dst = Integer.parseInt(in.substring(12,16),2);
                    src = Integer.parseInt(in.substring(8,12),2);
                    incrPC(2);
                    if(in.charAt(6)=='0') {
                        // byte mode 
                        A = readRegisterByte( src );
                        writeRegisterByte( A, dst );
                    }
                    if(in.charAt(6)=='1') {
                        // word mode 
                        A = readRegisterWord( src );
                        writeRegisterWord( A, dst );
                    }
                }
            } // mov instruction

        }
    }

    /*
    * Increment PC by n.
    *     Param Integer n should be length of instruction fetched
    */
    public void incrPC( Integer n ) {
        // show PC on screen which is being executed, and then - 
        LC.pc = CustTable.pad(LC.pc,-6,"0"); 
        int len = LC.pc.length();
        if (len>6)
            LC.pc = LC.pc.substring(len-6);
        LC.regPanel.valTable.setValueAt("x"+LC.pc.toUpperCase(), 0, 1);
        // - increment PC. Note instruction from previous PC is still to be executed
        LC.pc = CustTable.pad(Integer.toHexString(Integer.parseInt(LC.pc,16)+n),-6,"0");
    }

    /*
    * Read byte from memory address in MA into MD.
    */
    public void readMemoryByte() {
        int loc = Integer.parseInt(MA,16);
        if (loc<0||loc>4095)     // simulator has only 4k bytes memory
            return;
        MD = (String) LC.memPanel.table.getValueAt(loc,2);
        MD = CustTable.pad(Long.toHexString(Long.parseLong(MD,2)),-2,"0");
        MD = "000000"+MD;
    }

    /*
    * Write rightmost byte from MD into memory at address in MA.
    */
    public void writeMemoryByte() {
        int loc = Integer.parseInt(MA,16);
        if (loc<0||loc>4095)     // simulator has only 4k bytes memory
            return;
        String bits = Integer.toBinaryString(Integer.parseInt(MD.substring(6),16));
        bits = CustTable.pad(bits,-8,"0");
        LC.memPanel.table.setValueAt(bits,loc,2);
    }

    /*
    * Read word from memory address in MA..MA+3 into MD.
    */
    public void readMemoryWord() {
        int cnt = 0;
        int loc = Integer.parseInt(MA,16);
        if (loc<0||loc>4092)     // simulator has only 4k bytes memory
            return;
        MD = "";
        while (cnt < 4)
            MD = MD+LC.memPanel.table.getValueAt(loc+cnt++,2);
        MD = CustTable.pad(Long.toHexString(Long.parseLong(MD,2)),-8,"0");
    }

    /*
    * Write word in MD at memory address in MA..MA+3.
    */
    public void writeMemoryWord() {
        int cnt = 0;
        int loc = Integer.parseInt(MA,16);
        if (loc<0||loc>4092)     // simulator has only 4k bytes memory
            return;
        String bits = "";
        // LC.writeMessage(">>"+MD+MD.length());
        while(cnt<3) {
            bits = Integer.toBinaryString(Integer.parseInt(MD.substring(2*cnt,2*(cnt+1)),16));
            bits = CustTable.pad(bits,-8,"0");
            LC.memPanel.table.setValueAt(bits,loc+cnt++,2);
        }
        bits=Integer.toBinaryString(Integer.parseInt(MD.substring(6),16));
        bits=CustTable.pad(bits,-8,"0");
        LC.memPanel.table.setValueAt(bits,loc+cnt++,2);
    }

    /*
    * Read byte from register specified by src.
    *
    * Param:     src: Integer, specifies register.
    * Returns:   byte left-padded with '0's, hex string of length 8
    */
    public String readRegisterByte( Integer src ) {
        String P = (String) LC.regPanel.valTable.getValueAt((src/2)+1, 2*(src%2) +1);
        P = P.substring(1);
        P = "000000" + P.substring(6);
        return P;
    }

    /*
    * Write byte in register specified by dst.
    *
    * Param:    Val: String of 8 hex characters, rightmost byte to be stored in reg.
    *           dst: Integer, specifies register.
    */
    public void writeRegisterByte( String Val, Integer dst ) {
        String P = (String) LC.regPanel.valTable.getValueAt((dst/2)+1,2*(dst%2)+1);
        P = P.substring(0,7) + Val.substring(6).toUpperCase();
        LC.regPanel.valTable.setValueAt(P,(dst/2)+1,2*(dst%2)+1);
    }

    /*
    * Read word from register specified by src.
    *
    * Param:    src: Integer, specifies register.
    * Returns:  hex string of length 8
    */
    public String readRegisterWord( Integer src ) {
        String P = (String) LC.regPanel.valTable.getValueAt((src/2)+1, 2*(src%2) +1);
        P = P.substring(1);
        return P;
    }

    /*
    * Write word in register specified by dst.
    *
    * Param:    Val: String of 8 hex characters to be stored in reg.
    *           dst: Integer, specifies register.
    */
    public void writeRegisterWord(String Val,Integer dst) {
        LC.regPanel.valTable.setValueAt("x"+(Val.toUpperCase()),(dst/2)+1,2*(dst%2)+1);
    }


    /*
    * Calculate memory address in indexed mode.
    *
    */
    public void getIndexedAddress() {
        String binstr = HexToBinary(IR,32);
        String temp = binstr.substring(5,24)+"00000";          // 19-bit offset -> made to 24
        A = Integer.toHexString(Integer.parseInt(temp,2));     // convert to hex 
        int baseReg = Integer.parseInt(binstr.substring(24,28),2);
        B = (String) LC.regPanel.valTable.getValueAt((baseReg/2)+1,2*(baseReg%2)+1);
        B = B.substring(1); // drop the 'x'
        long add1, add2;
        add1 = Long.parseLong(A,16);
        add2 = Long.parseLong(B,16);
        String t="";
        add2 = add2 + add1;
        t = CustTable.pad(Long.toHexString(add2),-8,"0");
        if (t.length()==9)
            t = t.substring(1); // drop extra high order bit -- i.e. wrap-around
        MA = t;
        // LC.writeMessage("MA : "+t);
    }

    public void evalCondition() {
        // Jcond
        String condCode = opcode.substring(5);
        Boolean ZFlag, NFlag, OFlag, CFlag, negFlag;
        // negFlag indicates actually a negative/positive result
        // as opposed to NFlag which goes by sign bit alone.
        ZFlag = (String)LC.flagPanel.valTable.getValueAt(0,1)=="1" ? true : false;
        NFlag = (String)LC.flagPanel.valTable.getValueAt(1,1)=="1" ? true : false;
        OFlag = (String)LC.flagPanel.valTable.getValueAt(2,1)=="1" ? true : false;
        CFlag = (String)LC.flagPanel.valTable.getValueAt(3,1)=="1" ? true : false;
        // overflow negates the meaning of NFlag 
        negFlag = OFlag ? !NFlag : NFlag; 
        if(condCode.equals("000")&&!ZFlag&&negFlag)
            /*GT*/CON="1";
        else if(condCode.equals("001")&&(ZFlag||negFlag))
            /*GE*/CON="1";
        else if(condCode.equals("010")&&!ZFlag&&!negFlag)
            /*LT*/CON="1";
        else if(condCode.equals("011")&&(ZFlag||!negFlag))
            /*LE*/CON="1";
        else if(condCode.equals("100")&&ZFlag)
            /*EQ*/CON="1";
        else if(condCode.equals("101")&&!ZFlag)
            /*NE*/CON="1";
        else CON="0";
    }

    public void evalFlag() {
        // Jflag
        CON="H";
        String condCode=opcode.substring(5);
        String ZFlag=(String)LC.flagPanel.valTable.getValueAt(0,1);
        String NFlag=(String)LC.flagPanel.valTable.getValueAt(1,1);
        String OFlag=(String)LC.flagPanel.valTable.getValueAt(2,1);
        String CFlag=(String)LC.flagPanel.valTable.getValueAt(3,1);
        String PFlag=(String)LC.flagPanel.valTable.getValueAt(4,1);
        if(condCode.equals("000")&&ZFlag.equals("1"))
        CON="1";
        else if(condCode.equals("001")&&NFlag.equals("1"))
        CON="1";
        else if(condCode.equals("010")&&OFlag.equals("1"))
        CON="1";
        else if(condCode.equals("011")&&CFlag.equals("1"))
        CON="1";
        else if(condCode.equals("100")&&PFlag.equals("1"))
        CON="1";
        else    CON="0";
    }

    public void evalNotFlag() {
        //JNflag
        String condCode = opcode.substring(5);
        String ZFlag=(String)LC.flagPanel.valTable.getValueAt(0,1);
        String NFlag=(String)LC.flagPanel.valTable.getValueAt(1,1);
        String OFlag=(String)LC.flagPanel.valTable.getValueAt(2,1);
        String CFlag=(String)LC.flagPanel.valTable.getValueAt(3,1);
        String PFlag=(String)LC.flagPanel.valTable.getValueAt(4,1);
        if(condCode.equals("000")&&ZFlag.equals("0"))
        CON="1";
        else if(condCode.equals("001")&&NFlag.equals("0"))
        CON="1";
        else if(condCode.equals("010")&&OFlag.equals("0"))
        CON="1";
        else if(condCode.equals("011")&&CFlag.equals("0"))
        CON="1";
        else if(condCode.equals("100")&&PFlag.equals("0"))
        CON="1";
        else    CON="0";
    }

    /*
    *  Shift rightmost byte in B by specified number of bits.
    *  Result is in ALUOUT.
    *  param - number of bits
    */
    public void shiftByte( int bitsToShift ) {
        int val = Integer.parseInt(B.substring(6),16);        // content to shift
        String valToShift = "";                               // string of value to shift
        String carryOut = "";                                 // to store carry-out
        String t = "";
        if (bitsToShift==0)
            return;
        if(opcode.charAt(7)=='0') {
            //with carry
            if(opcode.charAt(6)=='0') {
                //left shift
                while(bitsToShift>0) {
                    val=2*val;
                    valToShift=CustTable.pad(Integer.toBinaryString(val),-9,"0");
                    carryOut=valToShift.substring(0,1);
                    valToShift=valToShift.substring(1,9);
                    bitsToShift--;
                    val=Integer.parseInt(valToShift,2);
                }
                LC.flagPanel.valTable.setValueAt(carryOut,3,1);
                t = CustTable.pad(Integer.toHexString(val),-2,"0");
            }
            if(opcode.charAt(6)=='1') {
                //right shift
                while(bitsToShift>0) {
                    val=val/2;
                    int rem=val%2;
                    String remain=CustTable.pad(Integer.toBinaryString(rem),-1,"0");
                    valToShift=CustTable.pad(Integer.toBinaryString(val),-8,"0");
                    carryOut=remain;
                    bitsToShift--;
                    val=Integer.parseInt(valToShift,2);
                }
                LC.flagPanel.valTable.setValueAt(carryOut,3,1);
                t = CustTable.pad(Integer.toHexString(val),-2,"0");
            }
        }
        if(opcode.charAt(7)=='1') {
            //without carry
            if(opcode.charAt(6)=='0') {
                //left shift
                while(bitsToShift>0) {
                    val=2*val;
                    valToShift=CustTable.pad(Integer.toBinaryString(val),-9,"0");
                    valToShift=valToShift.substring(1,9);
                    bitsToShift--;
                    val=Integer.parseInt(valToShift,2);
                }
                t = CustTable.pad(Integer.toHexString(val),-2,"0");
            }
            if(opcode.charAt(6)=='1') {
                //right shift
                while(bitsToShift>0) {
                    val=val/2;
                    valToShift=CustTable.pad(Integer.toBinaryString(val),-8,"0");
                    bitsToShift--;
                    val=Integer.parseInt(valToShift,2);
                }
                t = CustTable.pad(Integer.toHexString(val),-2,"0");
            }
        }
        ALUOUT="000000"+t;
        setNZPflag(t);
    }

    /*
    *  Shift word in B by specified number of bits.
    *  Result is in ALUOUT.
    *  param - number of bits
    */
    public void shiftWord( int bitsToShift ) {
        long val=Long.parseLong(B,16);          // value to shift
        String valToShift = "";                 // string of value to shift
        String carryOut = ""; 	                // to store the carry-out
        String t="";
        if (bitsToShift==0)
            return;
        if(opcode.charAt(7)=='0') {
            //with carry
            if(opcode.charAt(6)=='0') {
                //left shift
                while(bitsToShift>0) {
                    val=2*val;
                    valToShift=CustTable.pad(Long.toBinaryString(val),-33,"0");
                    carryOut=valToShift.substring(0,1);
                    valToShift=valToShift.substring(1,33);
                    bitsToShift--;
                    val=Long.parseLong(valToShift,2);
                }
                LC.flagPanel.valTable.setValueAt(carryOut,3,1);
                t = CustTable.pad(Long.toHexString(Long.parseLong(valToShift,2)),-8,"0");
            }
            if(opcode.charAt(6)=='1') {
                //right shift
                while(bitsToShift>0) {
                    val=val/2;
                    long rem=val%2;
                    String remain=CustTable.pad(Long.toBinaryString(rem),-1,"0");
                    valToShift=CustTable.pad(Long.toBinaryString(val),-32,"0");
                    carryOut=remain;
                    bitsToShift--;
                    val=Long.parseLong(valToShift,2);
                }
                LC.flagPanel.valTable.setValueAt(carryOut,3,1);
                t = CustTable.pad(Long.toHexString(val),-8,"0");
            }
        }
        if(opcode.charAt(7)=='1') {
            //without carry
            if(opcode.charAt(6)=='0') {
                //left shift
                while(bitsToShift>0) {
                    val=2*val;
                    valToShift=CustTable.pad(Long.toBinaryString(val),-33,"0");
                    valToShift=valToShift.substring(1,33);
                    bitsToShift--;
                    val=Long.parseLong(valToShift,2);
                }
                t = CustTable.pad(Long.toHexString(val),-8,"0");
            }
            if(opcode.charAt(6)=='1') {
                //right shift
                while(bitsToShift>0) {
                    val=val/2;
                    valToShift=CustTable.pad(Long.toBinaryString(val),-32,"0");
                    bitsToShift--;
                    val=Long.parseLong(valToShift,2);
                }
                t = CustTable.pad(Long.toHexString(val),-8,"0");
            }
        }
        ALUOUT=t;
        setNZPflag(t);
    }

    /**
    * ALUoperation - simulates operation of ALU
    *
    **/
    public void ALUoperation() {
        if(opcode.substring(0,2).equals("11")) {
            // Group 3 instruction
            String ALUcode = opcode.substring(2,6);
            if(opcode.charAt(6)=='0') {
                // byte mode
                int val1 = Integer.parseInt(A.substring(6),16);
                int val2 = Integer.parseInt(B.substring(6),16);
                int cval2 = val2;
                String t="";
                if (ALUcode.equals("0001")) { // ADD       
                    val2 = val1 + val2;
                    t = CustTable.pad(Integer.toHexString(val2),-2,"0");
                    t = setCOflagbyte( t, val1, cval2, false );
                    }
                if (ALUcode.equals("0010") || ALUcode.equals("0111") ) { // SUB, COMP
                    val1 = -val1;                  
                    String hex = Integer.toHexString(val1);    
                    hex = CustTable.pad(hex,-2,"0");         
                    hex = hex.substring(hex.length()-2);        // keep hex length = 2
                    val1 = Integer.parseInt(hex,16);       
                    val2 = val1 + val2;                         // subtract
                    t = CustTable.pad(Integer.toHexString(val2),-2,"0");
                    t = setCOflagbyte( t, val1, cval2, true );
                    // LC.writeMessage(val1+","+hex+","+cval2+","+val2+","+t);
                    }
                if(ALUcode.equals("0011")) { // AND
                    val2 = val1 & val2;
                    t = CustTable.pad(Integer.toHexString(val2),-2,"0");
                    resetCOflag();
                }
                if(ALUcode.equals("0100")) { // OR
                    val2 = val1 | val2;
                    t = CustTable.pad(Integer.toHexString(val2),-2,"0");
                    resetCOflag();
                }
                if(ALUcode.equals("0101")) { // XOR
                    val2 = val1 ^ val2;
                    t = CustTable.pad(Integer.toHexString(val2),-2,"0");
                    resetCOflag();
                }
                if(ALUcode.equals("0110")) { // NOT
                    val2 = 0xff ^ val2;
                    t = CustTable.pad(Integer.toHexString(val2),-2,"0");
                    resetCOflag();
                }
                ALUOUT = "000000"+ t;
                setNZPflag(t);
            }
            else {
                // word mode
                long add1, add2, cval3;
                add1 = Long.parseLong(A,16);
                add2 = Long.parseLong(B,16);
                cval3 = add2;
                String t="";
                if(ALUcode.equals("0001")) { // ADD
                    add2 = add2 + add1;
                    t= CustTable.pad(Long.toHexString(add2),-8,"0");
                    t=setCOflagword( t,add1, cval3, false);
                }
                if(ALUcode.equals("0010") || ALUcode.equals("0111") ) { // SUB, COMP
                    add1=-add1;
                    String hex = Long.toHexString(add1);
                    if(add1<0)
                        hex=hex.substring(hex.length()-8);
                    else
                        hex=CustTable.pad(hex,-8,"0");
                    add1 = Long.parseLong(hex,16);
                    add2 = add1 + add2;
                    t = CustTable.pad(Long.toHexString(add2),-8,"0");
                    t = setCOflagword( t,add1, cval3, true);
                }
                if(ALUcode.equals("0011")) { // AND
                    add2 = add1 & add2;
                    t = CustTable.pad(Long.toHexString(add2),-8,"0");
                    resetCOflag();
                }
                if(ALUcode.equals("0100")) { // OR
                    add2 = add1 | add2;
                    t = CustTable.pad(Long.toHexString(add2),-8,"0");
                    resetCOflag();
                }
                if(ALUcode.equals("0101")) { // XOR
                    add2 = add1 ^ add2;
                    t = CustTable.pad(Long.toHexString(add2),-8,"0");
                    resetCOflag();
                }
                if(ALUcode.equals("0110")) { // NOT
                    add2 = 0xffffffff ^ add2;
                    t = CustTable.pad(Long.toHexString(add2),-8,"0");
                    resetCOflag();
                }
                ALUOUT = t;
                setNZPflag(t);
            }
        }
    }

    /**
    * This method sets the N(negative),Z(zero) and P(parity) flags based on 
    * the instruction executed.
    *
    * @param a string which is value on basis of which these flags are set
    */
    public void setNZPflag(String flag) {
        if (flag.equals("00000000"))
            LC.flagPanel.valTable.setValueAt("1",0,1);
        else if(flag.equals("00"))
            LC.flagPanel.valTable.setValueAt("1",0,1);
        else
            LC.flagPanel.valTable.setValueAt("0",0,1);

        if(Integer.parseInt(flag.substring(0,1),16)>=8)
            LC.flagPanel.valTable.setValueAt("1",1,1);
        else
            LC.flagPanel.valTable.setValueAt("0",1,1);

        int j=0;
        int k=0;
        if(flag.length()==8)
        flag = Integer.toBinaryString(Integer.parseInt(flag.substring(0,4),16)) +
        Integer.toBinaryString(Integer.parseInt(flag.substring(4),16));
        else
        flag=Long.toBinaryString(Long.parseLong(flag,16));

        while(k < flag.length()) {
            if(flag.charAt(k)=='1')
            j++;
            k++;
        }
        if(j%2==1)
            LC.flagPanel.valTable.setValueAt("1",4,1);
        else
            LC.flagPanel.valTable.setValueAt("0",4,1);

    } /* end setNZPflag */


    /**
    * This method sets C(carry) and O(overflow) flags after the instruction 
    * is executed. It is for instructions executed in Byte mode.
    *
    * @param t: string which is value on basis of which these flags are set,
    * and two int type values operation on which causes the flags to be set.
    */

    public String setCOflagbyte(String t,int val1, int cval2, boolean subFlag ) {
        String set="1", cleared="0";
        if (subFlag) { // invert for borrow
            set="0";
            cleared="1";
        }    
        if(t.length()>2) {                          
            t=t.substring(t.length()-2);
            LC.flagPanel.valTable.setValueAt(set,3,1); // carry set/borrow cleared
        }
        else
            LC.flagPanel.valTable.setValueAt(cleared,3,1); // carry cleared/borrow set
            
        int fval = Integer.parseInt(t,16);
        if((val1 < 128 && cval2 < 128 && fval >=128) || (val1 >=128 && cval2 >=128 && fval <128))
            LC.flagPanel.valTable.setValueAt("1",2,1); // overflow set
        else
            LC.flagPanel.valTable.setValueAt("0",2,1); // overflow cleared
        return t;
    } /* end setcoflagbyte */



    /**
    * This method sets the C(carry) and O(offset) flags based on the instructions executed.
    * It is for instructions executed in Word mode.
    *
    * @param a string which is value on basis of which these flags are set,and two int type values
    * operation on which causes the flags to be set.
    */

    public String setCOflagword(String  t,long add1, long cval3, boolean subFlag ) {
        String set="1", cleared="0";
        if (subFlag) { // invert for borrow
            set="0";
            cleared="1";
        }    
        if(t.length()==9) {
            t=t.substring(1);
            LC.flagPanel.valTable.setValueAt(set,3,1);
        }
        else
        LC.flagPanel.valTable.setValueAt(cleared,3,1);

        long fval= Long.parseLong(t,16);
        int  power= (int)Math.pow(2,31);

        if((add1 < power && cval3 < power && fval >=power) || 
           (add1 >=power && cval3 >=power && fval <power))
            LC.flagPanel.valTable.setValueAt("1",2,1);
        else
            LC.flagPanel.valTable.setValueAt("0",2,1);
        ALUOUT= t;
        return t;
    } /* end setcoflagword */

    /**
    * To reset Carry and Overflow after logical operations
    **/
    public void resetCOflag() {
        LC.flagPanel.valTable.setValueAt("0",2,1);
        LC.flagPanel.valTable.setValueAt("0",3,1);
    }
    
    /*
    *  Hexadecimal integer to n-bit binary
    */
    public String HexToBinary( String hex, int len ) {
        String temp;
        temp = CustTable.pad(Long.toBinaryString(Long.parseLong(hex,16)),-len,"0");
        // LC.output.append(temp+"\n");
        return temp;
    }
    
    /*
    * Get input port data supplied by user 
    */
    public String getInputData(String portNo) {
        LC.writeMessage("Reading 8-bit binary input.");
        String typedText;
        while (true) {
            Object inputValue = JOptionPane.showInputDialog(null, 
                        "Port # "+portNo, "8-bit binary input",
                        JOptionPane.PLAIN_MESSAGE, null, null, null);
	    typedText = (String) inputValue;
	    if (typedText==null) {
                LC.writeMessage("Only 8-bit binary input allowed.");
	        continue;
	    }    
	    if (typedText.matches("[01]*") && typedText.length()==8)
	        break;
	    LC.writeMessage("Only 8-bit binary input allowed.");
	}        
        // LC.output.append("Read input "+typedText+".\n");
        return typedText;
    }
}