import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.*;

/**
* This class handles the assembling of any code written by user.
**/
public class AssembleLine {
    public JTextArea assembleArea, output;
    public String[][] labelAr = new String[50][2]; // labels defined in the program
    public String[][] usedAr = new String[50][20]; // for labels used before definition
    boolean err = false;
    boolean status = false;
    int linecnt=1;    // line number in assembly source program
    int assLineCnt=0; // line count in assembled output area
    
    final int ADD_OP = 1, MOV_OP = 2, SUB_OP = 3, AND_OP = 4, OR_OP = 5,
    XOR_OP = 6, NOT_OP = 7, COMP_OP = 8;
    final int LOAD_OP = 11, STORE_OP = 12;
    final int IN_OP = 21, OUT_OP = 22;
    final int ASM_FORMAT = 1, BIN_FORMAT = 0, HEX_FORMAT = 2;

    public AssembleLine(JTextArea assmbleArea, JTextArea out) {
        assembleArea = assmbleArea;
        output = out;
    }

    /**
    * This method checks correctness of binary code written before being loaded to memory.
    *
    * @param (String) binary instruction,
    *        (Sting) current instruction address,
    *        (int) line count.
    *
    * @return String value representing the address of next instruction
    */
    public String processBinLine( String instruct, String instAddress, int cnt) {
        linecnt = cnt;
        final String BINPATTERN= "[01]*";
        int len = instruct.length();

        if (instruct.charAt(0)=='.') {
            String pseudoOp = instruct.substring(1,len);
            String retString;
            if(pseudoOp.equalsIgnoreCase("ASM")) {
                LC.codeFormat = ASM_FORMAT;
                retString = instAddress;
            } // At present only one pseudo-op allowed in binary mode, so ...
            else
            retString = error("Error:@ Line " + linecnt+ ": Illegal pseudo-op.\n",instAddress);
            return retString;
        }

        StringTokenizer st = new StringTokenizer(instruct);
        Vector vec = new Vector();
        while (st.hasMoreTokens()) {
            String temp = st.nextToken();
            vec.add(temp);
        }
        String [] breakStr;
        String [] tempStr = new String[vec.size()];
        len = tempStr.length;
        for(int i=0;i<len;i++)
        tempStr[i]=(String)vec.elementAt(i);
        String check = tempStr[0];        // to check for label in first token

        if (check.charAt(check.length()-1)==':') {
            String retValue = callIntLabel(check,instAddress);
            // regardless of return value ...
            // ... strip instr upto ':' - i.e. drop breakStr[0]
            String [] stripped = new String[len-1];
            for(int i=0;i<len-1;i++) {
                stripped[i]=(String)vec.elementAt(i+1);
            }
            breakStr = stripped;
            // and then go on as though nothing happened
        }
        else
        breakStr = tempStr; // original array of tokens

        len = breakStr[0].length();
        if(breakStr[0].matches(BINPATTERN)) {
            if ( len==8 ) {
                String [] binInstr = new String[1]; // need standard format to append
                binInstr[0] = breakStr[0];
                instAddress = Append( binInstr, 1, 1, instAddress );
            }
            else
            return error("Error:@ Line " + linecnt+ ": Wrong byte length.\n", instAddress);
        }
        else
        return error("Error:@ Line " + linecnt+ ": Input is not binary.\n", instAddress);

        return instAddress;
    }

    /**
    * This method assembles any instruction to binary code.
    *
    * @param: instruction,
    *         instruction address,
    *         label array,
    *         line count.
    *
    * @return String value representing the address of next instruction
    */
    public String processLine( String instruct, String instAddress, String [][] lab, int cnt) {
        linecnt = cnt;
        labelAr = lab;

        StringTokenizer st = new StringTokenizer(instruct);
        Vector vec = new Vector();
        while (st.hasMoreTokens()) {
            String temp = st.nextToken();
            vec.add(temp);
        }

        String [] breakStr;

        String [] tempStr = new String[vec.size()];
        int len = tempStr.length;
        for(int i=0;i<len;i++)
        tempStr[i]=(String)vec.elementAt(i);
        String check = tempStr[0];        // to check for label or pseudo-op

        if (check.charAt(check.length()-1)==':') {
            String retValue = callIntLabel(tempStr[0],instAddress);
            // regardless of return value ...
            // ... strip instr upto ':' - i.e. drop breakStr[0]
            String [] stripped = new String[len-1];
            for(int i=0;i<len-1;i++) {
                stripped[i]=(String)vec.elementAt(i+1);
            }
            breakStr = stripped;
            // and then go on as though nothing happened
        }
        else if (check.charAt(0)=='.') {
            instAddress=callPseudoOp(tempStr,instAddress);
            return instAddress;
        }
        else
        breakStr = tempStr; // original array of tokens

        // re-define check with remaining instruction
        check = breakStr[0];

        if (check.equalsIgnoreCase("LOAD"))
        instAddress=callLoadStore(breakStr,instAddress,LOAD_OP);

        else if (check.equalsIgnoreCase("STORE"))
        instAddress=callLoadStore(breakStr,instAddress,STORE_OP);

        else if (check.equalsIgnoreCase("IN"))
        instAddress = callInOut( breakStr, instAddress, IN_OP );

        else if (check.equalsIgnoreCase("OUT"))
        instAddress = callInOut( breakStr, instAddress, OUT_OP );

        else if (check.equalsIgnoreCase("MOV"))
        instAddress = callALU( breakStr, instAddress, MOV_OP );

        else if (check.equalsIgnoreCase("ADD"))
        instAddress = callALU( breakStr, instAddress, ADD_OP );

        else if (check.equalsIgnoreCase("SUB"))
        instAddress = callALU( breakStr, instAddress, SUB_OP );

        else if (check.equalsIgnoreCase("AND"))
        instAddress = callALU( breakStr, instAddress, AND_OP );

        else if (check.equalsIgnoreCase("OR"))
        instAddress = callALU( breakStr, instAddress, OR_OP );

        else if (check.equalsIgnoreCase("XOR"))
        instAddress = callALU( breakStr, instAddress, XOR_OP );

        else if (check.equalsIgnoreCase("NOT"))
        instAddress = callNot(breakStr,instAddress);

        else if (check.equalsIgnoreCase("SHIFT"))
        instAddress = callShift(breakStr,instAddress);

        else if (check.equalsIgnoreCase("COMP"))
        instAddress = callALU( breakStr, instAddress, COMP_OP );

        else if (check.equalsIgnoreCase("JMP"))
        instAddress = callJmp( breakStr, instAddress );

        else if ( check.equalsIgnoreCase("JGT") || check.equalsIgnoreCase("JGE") ||
        check.equalsIgnoreCase("JEQ") || check.equalsIgnoreCase("JNE") ||
        check.equalsIgnoreCase("JLE") ||check.equalsIgnoreCase("JLT"))
        instAddress = callJcond( breakStr, instAddress );

        else if ( check.equalsIgnoreCase("JZ") || check.equalsIgnoreCase("JN") ||
        check.equalsIgnoreCase("JO") || check.equalsIgnoreCase("JC") ||
        check.equalsIgnoreCase("JP"))
        instAddress = callJflag( breakStr, instAddress );

        else if ( check.equalsIgnoreCase("JNZ") || check.equalsIgnoreCase("JNN") ||
        check.equalsIgnoreCase("JNO") || check.equalsIgnoreCase("JNC") ||
        check.equalsIgnoreCase("JNP"))
        instAddress = callJnflag( breakStr, instAddress );

        else if (check.equalsIgnoreCase("CALL"))
        instAddress = callCall( breakStr, instAddress );

        else if(check.equalsIgnoreCase("RET"))
        instAddress = callRet( breakStr, instAddress );

        else if(check.equalsIgnoreCase("HALT"))
        instAddress=callHalt(breakStr,instAddress);

        else
        return error("Error:@ Line "+linecnt+": Instruction opcode or syntax error. " +check+ " \n",instAddress);

        if(err) {
            err=false;
            return instAddress;
        }
        return instAddress;
    }

    /**
    * This method handles any LABEL defined in the program
    *
    * @param string containing zeroth (label) token of the instruction
    *        string containing current instruction address
    *
    **/
    public String callIntLabel(String tryLab, String instAddress) {
        String retString="OK";
        for(int j=0;j<labelAr.length;j++) {
            if(labelAr[j][0]!=null && // has label been seen before?
            (labelAr[j][0].equals(tryLab) || labelAr[j][0].equals(tryLab.substring(0,tryLab.length()-1)))) {
                retString = error("Error:@ Line "+linecnt+":Duplicate label found: "+labelAr[j][0]+".",instAddress);
                break;
            }
            if(labelAr[j][0]==null) {
                // insert label & address in array
                labelAr[j][0]=tryLab.substring(0,tryLab.length()-1);
                labelAr[j][1]=instAddress;
                break;
            }
        }
        return retString;    // either "OK" or error message
    }

    /**
    * This method handles the assembling process of any pseudo-operation.
    * Then it transfers control to individual function defined for each pseudo-op.
    *
    * @param: string array containing tokens of the instruction,
    *         current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public String callPseudoOp(String [] inst,String instAddress) {
        String [] binInst=new String[10];
        int count=0;
        String pseudoOp = inst[0].substring(1);
        if(pseudoOp.equalsIgnoreCase("START")) {
            instAddress=callStart(inst,instAddress);
        }
        else if(pseudoOp.equalsIgnoreCase("ORIG")) {
            instAddress=callOrig(inst,instAddress);
        }
        else if(pseudoOp.equalsIgnoreCase("ASM")) {
            LC.codeFormat = ASM_FORMAT;
        }
        else if(pseudoOp.equalsIgnoreCase("BIN")) {
            LC.codeFormat = BIN_FORMAT;
        }
        else if(pseudoOp.equalsIgnoreCase("END")) {
            instAddress=callEnd(inst,instAddress);
        }
        else if(pseudoOp.equalsIgnoreCase("CLEAR")) {
            LC.clearMemory();
        }
        else
        return error("Error:@ Line " + linecnt+ ": Illegal pseudo-op.\n",instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of .START pseudo-instruction.
    *
    * @param a string array containing the arguments of the instruction and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public String callStart(String [] token,String instAddress) {
        boolean done = false;
        if(token[1].charAt(0)=='X'||token[1].charAt(0)=='x') {
            instAddress = token[1].substring(1);   // recall 'x' in front
            instAddress = pad( instAddress, -6, "0" );
            LC.lastPC = LC.pc;
            LC.pc = instAddress;
            LC.moveHighlight();
            done = true;
        }
        if (!done) {
            for(int i=0;i<labelAr.length;i++) 
                if((labelAr[i][0]!=null)&&(labelAr[i][0].equals(token[1]))) {
                    // take 24 bit address of previously seen label
                    instAddress = labelAr[i][1];   
                    instAddress = pad( instAddress, -6, "0" );
                    LC.lastPC = LC.pc;
                    LC.pc = instAddress;
                    LC.moveHighlight();
                    done = true;
                }    
        }
        if (!done)
        return error("Error:@ Line " + linecnt+ ": Illegal memory address format.\n",instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of .ORIG pseudo-instruction.
    *
    * @param a string array containing the arguments of the instruction and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public String callOrig(String [] token,String instAddress) {
        if(token[1].charAt(0)=='X'||token[1].charAt(0)=='x') {
            String hex=token[1].substring(1,token[1].length());
            instAddress=pad((Object)hex,-6,"0");
        }
        else
        return error("Error:@ Line " + linecnt+ ": Illegal memory address format.",instAddress);
        return instAddress;
    }

    /*
    * This method handles the .End pseudo instruction
    */
    public String callEnd(String [] token, String instAddress) {
        return instAddress;
    }

    /**
    * This method handles the assembling process of LOAD and STORE instructions.
    *
    * @param: string array containing tokens of the instruction,
    *         current instruction address,
    *         integer indicating LOAD or STORE.
    *
    * @return String value representing the address of next instruction
    **/
    public String callLoadStore(String [] inst,String instAddress,int whichOp) {
        String tempOpcode="";
        if((inst.length!=4)&&(inst.length!=5))
        return error("Error:@ Line "+linecnt+": Syntax error - argument count.\n",instAddress);
        if (whichOp==LOAD_OP) tempOpcode = "00";
        if (whichOp==STORE_OP) tempOpcode = "01";
        if(inst.length==5)
            instAddress=callIndexed(inst,tempOpcode,instAddress);
        else if(inst[2].charAt(0)=='R'||inst[2].charAt(0)=='r')
            instAddress=callIndirect(inst,tempOpcode,instAddress);
        else
            instAddress=callDirect(inst,tempOpcode,instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of the Direct Load/Store instruction.
    *
    * @param: string array containing tokens of the instruction,
    *         string with correct bits for load or store,
    *         and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public String callDirect(String [] inst, String loadOrStore, String instAddress) {
        String [] binInst=new String[10];
        int count=0,size=0;
        String rval;

        binInst[count++]=loadOrStore;
        if(inst[1].equalsIgnoreCase("byte")||inst[1].equalsIgnoreCase("b"))
        binInst[count++]="0";
        else if(inst[1].equalsIgnoreCase("word")||inst[1].equalsIgnoreCase("w"))
        binInst[count++]="1";
        else
        return error("Error:@ Line " + linecnt+ ": Syntax error - byte(b) or word(w) specifier needed.\n",instAddress);

        // putting in addressing mode
        binInst[count++]="0";

        // now register specifier and then label or direct address
        if(inst[3].charAt(0)=='R'||inst[3].charAt(0)=='r') {
            // stuff in reg specifier - NJ
            rval=inst[3].substring(1);
            if(!registerError(rval))
                binInst[count++]=DecToBinary(rval,4);
            else
            return error("Error:@ Line " + linecnt+ ": Illegal register specifier.\n ",instAddress);
        }
        else
        return error("Error:@ Line " + linecnt+ ": Illegal register specifier.\n",instAddress);

        boolean flag=false;
        rval=inst[2].substring(1); // recall 'x' in front for hex values
        if(inst[2].charAt(0)=='X'||inst[2].charAt(0)=='x') {
            if(!hexError(rval,12)) // recall simulator memory only 2^12 bytes
                binInst[count++]=HexToBinary(rval,24);
            else
            return error("Error:@ Line "+linecnt+": Available memory upto FFF (hex).\n ",instAddress);
            flag=true;
        }
        if (!flag) {
            for(int i=0;i<labelAr.length;i++) {
                if((labelAr[i][0]!=null)&&(labelAr[i][0].equals(inst[2]))) {
                    // take 24 bit address of previously seen label
                    binInst[count++] = HexToBinary(labelAr[i][1],24); 
                    flag=true;
                }
            }
        }
        if(!flag) {
            // save label in usedAr - used before definition
            insertUsedLabel( inst[2], instAddress, 1 );
            binInst[count++]=DecToBinary("0",24);
            // zeros are stuffed in where label address will later be inserted
        }
        instAddress = Append(binInst,count,4,instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling of the indexed Load/Store instruction.
    *
    * @param: string array containing tokens of the instruction,
    *         string with correct bits for Load or Store,
    *         and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public String callIndexed(String [] inst, String loadOrStore,String instAddress) {
        String [] binInst=new String[10];
        int count=0,size=0;
        String rval;
        binInst[count++]=loadOrStore;
        if(inst[1].equalsIgnoreCase("byte")||inst[1].equalsIgnoreCase("b"))
            binInst[count++]="0";
        else if(inst[1].equalsIgnoreCase("word")||inst[1].equalsIgnoreCase("w"))
            binInst[count++]="1";
        else
            return error("Error:@ Line "+linecnt+": Syntax error - byte(b)/word(w) specifier needed.\n",instAddress);
        // setting mode and then offset & index register
        binInst[count++]="11";
        // binInst[count++]="1";          // To be checked - why two SEPARATE '1's here

        rval=inst[2].substring(1);              // offset value, drop the 'x'
        if(!hexError(rval,24)) {
            String binSt=HexToBinary(rval,24);
            if (!binSt.substring(19).equals("00000")) 
                return error("Error:@ Line "+linecnt+": Index value not multiple of x20.\n",instAddress);
            binSt=binSt.substring(0,19);        // only high order 19 bits needed
            binInst[count++]=binSt;
        }
        else
        return error("Error:@ Line "+linecnt+": Index value out of range.\n",instAddress);

        if(inst[3].charAt(0)=='R'||inst[3].charAt(0)=='r') {
            // index register
            rval=inst[3].substring(1);
            if(!registerError(rval))
                binInst[count++]= DecToBinary(rval,4);
            else
            return error("Error:@ Line "+linecnt+": Illegal register specifier.\n",instAddress);
        }
        else
        return error("Error:@ Line "+linecnt+": Register specifier required.\n",instAddress);
        
        if(inst[4].charAt(0)=='R'||inst[4].charAt(0)=='r') {
            // source/dest register
            rval=inst[4].substring(1);
            if(!registerError(rval))
                binInst[count++]=DecToBinary(rval,4);
            else
            return error("Error:@ Line "+linecnt+": Illegal register specifier.\n",instAddress);
        }
        else
        return error("Error:@ Line "+linecnt+": Register specifier required.\n",instAddress);
        instAddress = Append(binInst,count,4,instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of the InDirect Load/Store instruction.
    *
    * @param: string array containing tokens of the instruction,
    *         string with correct bits for load or store,
    *         and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public String callIndirect(String [] inst, String loadOrStore,String instAddress) {
        String [] binInst=new String[10];
        int count=0,size=0;
        String rval;
        binInst[count++]=loadOrStore;
        if(inst[1].equalsIgnoreCase("byte")||inst[1].equalsIgnoreCase("b"))
            binInst[count++]="0";
        else if(inst[1].equalsIgnoreCase("word")||inst[1].equalsIgnoreCase("w"))
            binInst[count++]="1";
        else
        return error("Error:@ Line "+linecnt+": Syntax error - word(w)/byte(b) specifier.\n",instAddress);
        // setting mode
        binInst[count++]="10";     // indirect mode
        // binInst[count++]="0";
        
        binInst[count++]="000";    // unused bits
        
        if(inst[2].charAt(0)=='R'||inst[2].charAt(0)=='r') {
            rval=inst[2].substring(1);
            if(!registerError(rval))
            binInst[count++]=DecToBinary(rval,4);
            else
            return error("Error:@ Line "+linecnt+": Illegal register specifier.\n",instAddress);
        }
        else
        return error("Error:@ Line "+linecnt+": Register specifier required.\n",instAddress);
        if(inst[3].charAt(0)=='R'||inst[3].charAt(0)=='r') {
            rval=inst[3].substring(1);
            if(!registerError(rval))
            binInst[count++]=DecToBinary(rval,4);
            else
            return error("Error:@ Line "+linecnt+": Illegal register specifier.\n",instAddress);
        }
        else
        return error("Error:@ Line "+linecnt+": Register specifier required.\n",instAddress);
        instAddress = Append(binInst,count,2,instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of the ALU instructions.
    *
    * @param string array containing the arguments of the instruction,
    *        current instruction address,
    *        integer representing the ALU operation.
    *
    * @return String value representing the address of next instruction
    **/
    public String callALU( String [] inst, String instAddress, int ALUop ) {
        String [] binInst = new String[10];
        int count=0, size=0;
        switch ( ALUop ) {
            case ADD_OP:    binInst[count++]="110001";
            break;
            case MOV_OP:    binInst[count++]="110000";
            break;
            case SUB_OP:    binInst[count++]="110010";
            break;
            case AND_OP:    binInst[count++]="110011";
            break;
            case OR_OP:     binInst[count++]="110100";
            break;
            case XOR_OP:    binInst[count++]="110101";
            break;
            case COMP_OP:   binInst[count++]="110111";
            break;
        }
        instAddress = ALUcommon(inst, binInst, count, instAddress);
        return instAddress;
    }

    /**
    * This method handles the common portion of assembling process of ALU instructions.
    *
    * @param: string array containing tokens of the instruction,
    *         string array containing binary instruction being assembled,
    *         count of number of binary fields,
    *         and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public String  ALUcommon(String[] inst, String [] binInst, int count, String instAddress) {
        int size;
        if(inst.length!=4)
        return error("Error:@ Line " + linecnt+ ": Syntax error. Number of tokens.\n ",instAddress);
        //checking for byte or word
        if(inst[1].equalsIgnoreCase("byte")||inst[1].equalsIgnoreCase("b"))
        binInst[count++]="0";
        else if(inst[1].equalsIgnoreCase("word")||inst[1].equalsIgnoreCase("w"))
        binInst[count++]="1";
        else
        return error("Error:@ Line " + linecnt+ ": Syntax error. Byte(b) or word(w) specifier needed.\n ",instAddress);
        //checking for register or immediate mode
        if(inst[2].charAt(0)=='R'||inst[2].charAt(0)=='r') {
            size=2;
            String rval=inst[2].substring(1);
            if(!registerError(rval)) {
                binInst[count++]="0";
                binInst[count++]=DecToBinary(rval,4);
            }
            else
            return error("Error:@ Line " + linecnt+ ": Illegal register specifier. \n ",instAddress);
        }
        else if(inst[2].charAt(0)=='X'||inst[2].charAt(0)=='x') {
            size=4;
            String rval=inst[2].substring(1);
            if(!hexError(rval,20)) {
                binInst[count++]="1";
                binInst[count++]=HexToBinary(rval,20);
            }
            else
            return error("Error:@ Line " + linecnt+ ": Max immediate value can be " + Math.pow(2,20) +". \n ",instAddress);
        }
        /* Note - only hex constants for now
        ** else if( inst[2].charAt(0)=='#') {
            **  size=4;
            **  String rval=inst[2].substring(1);
            **  if(!decError(rval,19)) {
                **      binInst[count++]="1";
                **      String binSt=Integer.toBinaryString(Integer.parseInt(rval));
                **      if(binSt.length()>20)
                **      binInst[count++]=binSt.substring(binSt.length()-20);
                **      else
                **      binInst[count++]= pad((Object)Integer.toBinaryString(Integer.parseInt(rval)),-20,"0");
                **
            }
            ** else
            **    return error("Error:@ Line " + linecnt+ ": Max immediate value can be " + Math.pow(2,20) +". \n ",instAddress);
            **
        }
        */
        else
        return error("Error:@ Line " + linecnt+ ": Source operand needed. \n",instAddress);

        if(inst[3].charAt(0)=='R'||inst[3].charAt(0)=='r') {
            String rval=inst[3].substring(1);
            if(!registerError(rval))
                binInst[count++]=DecToBinary(rval,4);
            else
            return error("Error:@ Line " + linecnt+ ": Illegal register specifier. \n ",instAddress);
        }
        else
        return error("Error:@ Line " + linecnt+ ": Desination register required. \n",instAddress);
        return Append( binInst, count, size, instAddress );
    }

    /**
    * This method handles the assembling process of the logical NOT instruction.
    *
    * @param a string array containing the arguments of the instruction and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public  String callNot(String [] inst,String instAddress) {
        //done
        String [] binInst=new String[10];
        int count=0,size=0;
        binInst[count++]="110110";
        size=2;
        //checking for byte or word
        if(inst[1].equalsIgnoreCase("byte")||inst[1].equalsIgnoreCase("b"))
        binInst[count++]="0";
        else if(inst[1].equalsIgnoreCase("word")||inst[1].equalsIgnoreCase("w"))
        binInst[count++]="1";
        else
        return error("Error:@ Line " + linecnt+ ": Byte(b) or word(w) specifier needed.\n ",instAddress);
        binInst[count++]="00000";
        if (inst[2].charAt(0)=='R'||inst[2].charAt(0)=='r') {
            String rval=inst[2].substring(1);
            if(!registerError(rval))
                binInst[count++]=DecToBinary(rval,4);
            else
            return error("Error:@ Line " + linecnt+ ": Illegal register specifier. \n ",instAddress);
        }
        else
        return error("Error:@ Line " + linecnt+ ": Desination register required. \n",instAddress);
        return Append(binInst,count,size,instAddress);
    }

    /**
    * This method handles the common portion of assembling process of Jump,
    * JFlag, JCondition etc. instructions.
    *
    * @param: string array containing tokens of the instruction,
    *         binary version of the instruction,
    *         count in binary version, and
    *         current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public String common2(String [] inst,String [] binInst,int count,String instAddress) {
        boolean flag=false;
        if(inst[1].charAt(0)=='X'||inst[1].charAt(0)=='x') {
            // LC.output.append("here ...\n");
            String rval=inst[1].substring(1);
            if(!hexError(rval,12)) // recall simulator memory only 2^12 bytes
                binInst[count++]=HexToBinary(rval,24);
            else
            return error("Error:@ Line "+linecnt+": Available memory upto FFF (hex).\n ",instAddress);
            flag=true;
        }
        /* Note - for the present, only hex values
        ** else if( inst[1].charAt(0)=='#') {
            **    String rval=inst[1].substring(1);
            **    if(!decError(rval,12))
            **    binInst[count++]= pad((Object)Integer.toBinaryString(Integer.parseInt(rval)),-24,"0");
            **    else
            **    return error("Error:@ Line " + linecnt+ ": Available  memory upto  " + Math.pow(2,12) +" \n ",instAddress);
            **    flag =true;
            **
            **
        }
        */
        else {
            // LC.output.append(inst[1]+"\n");
            for(int i=0;i<labelAr.length;i++) {
                if((labelAr[i][0]!=null)&&(labelAr[i][0].equals(inst[1]))) {
                    binInst[count++]=HexToBinary(labelAr[i][1],24);
                    flag=true;
                }
            }
        }
        if(!flag) {
            // save label in usedAr - used before definition
            insertUsedLabel( inst[1], instAddress, 1 );
            binInst[count++]=DecToBinary("0",24);
            // stuffed in zeros where label will later be inserted
        }
        instAddress = Append(binInst,count,4,instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of the Jump instruction.
    *
    * @param a string array containing the arguments of the instruction and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public  String callJmp(String [] inst,String instAddress) {
        String [] binInst=new String[10];
        int count=0;
        if(inst.length!=2)
        return error("Error:@ Line " + linecnt+ ": Syntax error. Number of tokens. \n ",instAddress);
        binInst[count++]="10000";
        binInst[count++]="000";
        instAddress=common2(inst, binInst, count,instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of the JCondition instruction.
    *
    * @param a string array containing the arguments of the instruction and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public  String callJcond(String [] inst,String instAddress) {
        String [] binInst=new String[10];
        int count=0;
        if(inst.length!=2)
        return error("Error:@ Line " + linecnt+ ": Syntax error. Number of tokens.\n ",instAddress);
        binInst[count++]="10001";
        String local =inst[0].substring(1);
        if(local.equalsIgnoreCase("GT"))
        binInst[count++]="000";
        else if(local.equalsIgnoreCase("GE"))
        binInst[count++]="001";
        else if(local.equalsIgnoreCase("LT"))
        binInst[count++]="010";
        else if(local.equalsIgnoreCase("LE"))
        binInst[count++]="011";
        else if(local.equalsIgnoreCase("EQ"))
        binInst[count++]="100";
        else  if(local.equalsIgnoreCase("NE"))
        binInst[count++]="101";
        instAddress=common2(inst, binInst, count,instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of the JFlag instruction.
    *
    * @param a string array containing the arguments of the instruction and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public String callJflag(String [] inst,String instAddress) {
        String [] binInst=new String[10];
        int count=0;
        if(inst.length!=2)
        return error("Error:@ Line " + linecnt+ ": Syntax error. Number of tokens. \n ",instAddress);
        binInst[count++]="10010";
        String local =inst[0].substring(1);
        if(local.equalsIgnoreCase("Z"))
        binInst[count++]="000";
        else if(local.equalsIgnoreCase("N"))
        binInst[count++]="001";
        else if(local.equalsIgnoreCase("O"))
        binInst[count++]="010";
        else if(local.equalsIgnoreCase("C"))
        binInst[count++]="011";
        else if(local.equalsIgnoreCase("P"))
        binInst[count++]="100";
        instAddress=common2(inst, binInst, count,instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of the JNFlag instruction.
    *
    * @param a string array containing the arguments of the instruction and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public String callJnflag(String [] inst,String instAddress) {
        boolean flag=false;
        if(inst.length!=2)
        return error("Error:@ Line " + linecnt+ ": Syntax error. Number of tokens. \n ",instAddress);
        String [] binInst=new String[10];
        int count=0;
        binInst[count++]="10011";
        String local =inst[0].substring(1);
        if(local.equalsIgnoreCase("NZ"))
        binInst[count++]="000";
        else if(local.equalsIgnoreCase("NN"))
        binInst[count++]="001";
        else if(local.equalsIgnoreCase("NO"))
        binInst[count++]="010";
        else if(local.equalsIgnoreCase("NC"))
        binInst[count++]="011";
        else if(local.equalsIgnoreCase("NP"))
        binInst[count++]="100";
        instAddress =common2(inst, binInst, count,instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of the Call instruction.
    *
    * @param a string array containing the arguments of the instruction and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public  String callCall(String [] inst,String instAddress) {
        String [] binInst=new String[10];
        int count=0,size=0;
        if(inst.length!=2)
        return error("Error:@ Line " + linecnt+ ": Syntax error. Number of tokens. \n ",instAddress);
        binInst[count++]="10100";
        binInst[count++]="000";
        instAddress=common2(inst, binInst, count,instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of IN and OUT instruction.
    *
    * @param: string array containing tokens of the instruction,
    *         current instruction address,
    *         integer indicating operation.
    *
    * @return String value representing the address of next instruction
    **/
    public String callInOut(String [] inst,String instAddress,int whichOp) {
        String [] binInst=new String[10];
        int count=0;
        if(inst.length!=3)
        return error("Error:@ Line " + linecnt+ ": Syntax error. Number of tokens. \n ",instAddress);
        if (whichOp==IN_OP) binInst[count++]="1110";
        if (whichOp==OUT_OP) binInst[count++]="1111";
        String bin;
        if(inst[2].charAt(0)=='R'||inst[2].charAt(0)=='r') {
            String rval=inst[2].substring(1);
            if(!registerError(rval))
                binInst[count++]=DecToBinary(rval,4);
            else
            return error("Error:@ Line " + linecnt+ " Illegal register specifier. \n ",instAddress);
        }
        else
        return error("Error:@ Line " + linecnt+ "Register specidfier needed. \n ",instAddress);
        if(inst[1].charAt(0)=='X'||inst[1].charAt(0)=='x') {
            String rval=inst[1].substring(1);
            if(!hexError(rval,8))    // 8-bit port numbers
                binInst[count++]=HexToBinary(rval,8);
            else
            return error("Error:@ Line " + linecnt+ ": Ports are avilable upto " +( Math.pow(2,16)-1) +". \n ",instAddress);
        }
        /* Note only hex constants for now
        ** else if( inst[1].charAt(0)=='#') {
            **    String rval=inst[1].substring(1);
            **    if(!decError(rval,16))
            **    // if((Long.parseLong(rval)<=(Math.pow(2,16)-1))&&(Long.parseLong(rval)>=0))
            **    binInst[count++]= pad((Object)Integer.toBinaryString(Integer.parseInt(rval)),-16,"0");
            **    else
            **    return error("Error:@ Line " + linecnt+ ": Ports available upto " +( Math.pow(2,16)-1) +". \n ",instAddress);
            **
        }
        */
        else
        return error("Error:@ Line " + linecnt+ ": Port number required. \n",instAddress);
        instAddress = Append(binInst,count,2,instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of the Shift instruction.
    *
    * @param a string array containing the arguments of the instruction and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public  String callShift(String [] inst,String instAddress) {
        String [] binInst=new String[10];
        int count=0;
        if(inst.length!=6)
        return error("Error:@ Line " + linecnt+ ": Syntax error. Number of tokens.\n ",instAddress);
        binInst[count++]="10111";
        String bin;
        int j=0;
        //for byte or word
        if(inst[1].equalsIgnoreCase("b")||inst[1].equalsIgnoreCase("byte"))
        binInst[count++]="0";
        else if(inst[1].equalsIgnoreCase("w")||inst[1].equalsIgnoreCase("word"))
        binInst[count++]="1";
        else
        return error("Error:@ Line " + linecnt+ ": Byte(b) or word(w) specifier needed.\n",instAddress);
        //for left or right
        if(inst[2].equalsIgnoreCase("l")||inst[2].equalsIgnoreCase("left"))
        binInst[count++]="0";
        else if(inst[2].equalsIgnoreCase("r")||inst[2].equalsIgnoreCase("right"))
        binInst[count++]="1";
        else
        return error("Error:@ Line " + linecnt+ ": Left(l) or right(r) specifier needed. \n",instAddress);
        //for carry or without carry
        if(inst[3].equalsIgnoreCase("with-carry")||inst[3].equalsIgnoreCase("C"))
        binInst[count++]="0";
        else if(inst[3].equalsIgnoreCase("without-carry")||inst[3].equalsIgnoreCase("NC"))
        binInst[count++]="1";
        else
        return error("Error:@ Line " + linecnt+ ": Carry(C) or without carry(NC) specifier needed. \n",instAddress);
        //check whether register specified
        if(inst[4].charAt(0)=='R'||inst[4].charAt(0)=='r') {
            String rval=inst[4].substring(1);
            if(!registerError(rval))
                binInst[count++]=DecToBinary(rval,4);
            else
            return error("Error:@ Line " + linecnt+ ": Illegal register specifier. \n ",instAddress);
        }
        else
        return error("Error:@ Line " + linecnt+ ": Register specifier missing. \n",instAddress);
        //cheking for no. of bits
        if(inst[5].charAt(0)=='X'||inst[5].charAt(0)=='x') {
            String rval=inst[5].substring(1);
            if(!hexError(rval,4))
                binInst[count++]=HexToBinary(rval,4);
            else
            return error("Error:@ Line " + linecnt+ ": Illegal shift count - 0 to 15 needed. \n ",instAddress);
        }
        /* Note - only hex counts for now
        ** else if( inst[5].charAt(0)=='#') {
            **  String rval=inst[5].substring(1);
            **  if(!decError(rval,4))
            **  binInst[count++]= pad((Object)Integer.toBinaryString(Integer.parseInt(rval)),-4,"0");
            **  else
            **  return error("Error:@ Line " + linecnt+ ": Illegal shift count - 0 to 15 needed. \n",instAddress);
            **
        }
        */
        else
        return error ("Error:@ Line " + linecnt+ ": In shift instruction, Register should be followed by number\n",instAddress);
        instAddress = Append(binInst,count,2,instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of the Return instruction.
    *
    * @param a string array containing the arguments of the instruction and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public  String callRet(String [] inst,String instAddress) {
        String [] binInst=new String[10];
        int count=0;
        if(inst.length!=1)
        return error("Error:@ Line " + linecnt+ ": Syntax error. Number of tokens. \n ",instAddress);
        binInst[count++]="10101000";
        instAddress = Append(binInst,count,1,instAddress);
        return instAddress;
    }

    /**
    * This method handles the assembling process of the Halt instruction.
    *
    * @param a string array containing the arguments of the instruction and current instruction address.
    *
    * @return String value representing the address of next instruction
    **/
    public  String callHalt(String [] inst,String instAddress) {
        String [] binInst=new String[10];
        int count=0;
        if(inst.length!=1)
        return error("Error:@ Line " + linecnt+ ": Syntax error. Number of tokens. \n ",instAddress);
        binInst[count++]="10101100";
        instAddress = Append(binInst,count,1,instAddress);
        return instAddress;
    }

    /*
    * To insert a used but undefined label in its array, with address used
    * params - label to insert
    *          instr address where referred
    *          offset within instr
    */
    public void insertUsedLabel( String lab, String instAddress, int offset ) {
        int loc = Integer.parseInt(instAddress,16)+offset;
        String ref = Integer.toHexString(loc);
        ref = pad((Object)ref.toUpperCase(),-6,"0");
        for(int j=0;j<usedAr.length;j++) {
            if(usedAr[j][0]==null) {
                // insert used label & address in array
                usedAr[j][0]=lab;
                usedAr[j][1]=ref;
                break;
            }
        }
        // LC.output.append("Used label "+lab+" at "+ref+"\n");
    }

    /*
    * To patch all used previously undefined labels after first assembly pass
    */
    public void patchUsedLabels() {
        String labUsed, labUsedAdr, labSaved, labSavedAdr;
        Boolean wroteMsg=false;
        for (int j=0;j<usedAr.length;j++) {
            if (usedAr[j][0]!=null) {
                // pick up used label & address
                labUsed=usedAr[j][0];
                labUsedAdr=usedAr[j][1];
                // LC.output.append("Found "+labUsed+" at "+ref+"\n");
                for (int k=0;k<labelAr.length;k++) {
                    labSaved = labelAr[k][0];
                    labSavedAdr = labelAr[k][1];
                    if ( labSaved!=null && labSaved.equals(labUsed)) {
                        // show patched labels in assembly area &
                        // append at mem[ref] the three bytes of saved address
                        int locSaved = Integer.parseInt(labSavedAdr,16);
                        int locUsed = Integer.parseInt(labUsedAdr,16);
                        if (!wroteMsg) {
                            wroteMsg=true;
                            assembleArea.append("\nPatching labels:\n");
                        }    
                        assembleArea.append(labUsed+" = x");
                        assembleArea.append(pad((Object)labSavedAdr.toUpperCase(),-6,"0"));
                        assembleArea.append(" at x");
                        assembleArea.append(pad((Object)labUsedAdr.toUpperCase(),-6,"0")+"\n");
                        String str = HexToBinary(labSavedAdr,24);
                        LC.memPanel.table.setValueAt(str.substring(0,8),locUsed,2);
                        LC.memPanel.table.setValueAt(str.substring(8,16),locUsed+1,2);
                        LC.memPanel.table.setValueAt(str.substring(16,24),locUsed+2,2);
                    }
                }
            }
        }
    }

    /**
    * This method is is used to pad a string to make it of certain fixed length.
    *
    * @param string to be padded - str,length of to be padded string - padlen and string
    * with which padding has to be done - pad
    **/
    private static String pad(Object str, int padlen, String pad) {
        String padding = new String();
        int len = Math.abs(padlen) - str.toString().length();
        if (len < 1)
        return str.toString();
        for (int i = 0 ; i < len ; ++i)
        padding = padding + pad;
        return (padlen < 0 ? padding + str : str + padding);
    }

    /**
    * This method adds the assembled code to assemblCode area and loads it in the memory.
    * It also calculates the address at which next instruction has to be located.
    *
    * @param string array containing the binary representation of components of the instruction,
    *        number of elements in the string array,
    *        size of current instruction,
    *        current instruction address.
    *
    * @ return String value representing the address of next instruction
    **/
    private String Append(String [] binary,int count,int size, String instAddress) {
        String str="";
        if (assLineCnt==0) {
            assembleArea.append("Line  Mem Add  Content\n");
            assembleArea.append("----------------------\n");
        }            
        if(LC.error=="false") {
            int loc = Integer.parseInt(instAddress,16);
            instAddress= Integer.toHexString(loc);
            assembleArea.append("["+pad(Integer.toString(linecnt),-2," ")+"]  ");
            assembleArea.append("x"+pad(Integer.toHexString(loc).toUpperCase(),-6,"0")+"  ");
            int k=0;
            while(k < count)
                str=str.concat(binary[k++]);
            // LC.output.append(str+" : "+str.length()+"\n");
            if(size==1) {
                LC.memPanel.table.setValueAt(str,loc,2);
            }
            else if (size==2) {
                LC.memPanel.table.setValueAt(str.substring(0,8),loc,2);
                LC.memPanel.table.setValueAt(str.substring(8),loc+1,2);
            }
            else if (size==3) {
                LC.memPanel.table.setValueAt(str.substring(0,8),loc,2);
                LC.memPanel.table.setValueAt(str.substring(8,16),loc +1 ,2);
                LC.memPanel.table.setValueAt(str.substring(16,24),loc+2,2);
            }
            else if (size==4) {
                LC.memPanel.table.setValueAt(str.substring(0,8),loc,2);
                LC.memPanel.table.setValueAt(str.substring(8,16),loc+1,2);
                LC.memPanel.table.setValueAt(str.substring(16,24),loc+2,2);
                LC.memPanel.table.setValueAt(str.substring(24),loc+3,2);
            }
            loc=loc+size;
            instAddress= Integer.toHexString(loc);
            if(LC.format==0) {
                int j=0;
                while(j < count) {
                    assembleArea.append(binary[j]+" ");
                    j++;
                }
            }
            else {
                String strg="";
                strg=strg.concat(Long.toHexString(Long.parseLong(str,2)));
                assembleArea.append("x"+pad(strg,-(size*2),"0").toUpperCase());
            }
        }
        assembleArea.append("\n");
        assLineCnt++;
        return instAddress;
    }

    /**
    * This method is used to display an error message in the test-area meant for displaying
    * error messages..
    *
    * @param out - the string to be displayed and instAddress - The address,at which the instruction
    * located causes the error to occur
    *
    * @ return the current instruction address
    **/
    private String error( String out, String instAddress) {
        LC.error="true";
        LC.assembleError=true;
        output.append(out);
        return instAddress;
    }

    /**
    * This method checks whether a register specified exists or not
    *
    * @param reg - The register number to be checked
    *
    * @ return boolean value for whether the error occurs or not
    **/
    private boolean registerError(String reg) {
        int p;
        try {
            p=Integer.parseInt(reg);
        } catch(Exception e) {
            return true;
        }
        if(p<0 ||p>15)
        return true;
        return false;
    }

    /**
    * This method checks whether the hex number specified lies in the range.
    *
    * @param reg - The hex string to be checked for and max - the maximum possible length
    * of the hex string
    *
    * @ return boolean value for whether the error occurs or not
    */
    private boolean hexError(String reg,int max) {
        int p;
        try {
            if(Integer.toBinaryString(Integer.parseInt(reg,16)).length()>max)
            return true;
        } catch(Exception e) {
            return true;
        }
        return false;
    }

    /**
    * This method checks whether the decimal number specified lies in the range.
    *
    * @param reg - string respresenting the decimal number to be checked,
    *        max - maximum possible length of binary representation of string.
    *
    * @ return boolean value for whether the error occurs or not
    **/
    private boolean decError(String reg,int max) {
        int p;
        try {
            p=Integer.parseInt(reg);
        } catch(Exception e) {
            return true;
        }
        if((p > Math.pow(2,max)-1 )||(p < 1- Math.pow(2,max)))
        return true;
        return false;
    }

    /*
    *  Decimal integer to n-bit binary
    */
    public String DecToBinary( String dec, int len ) {
        return pad((Object)Integer.toBinaryString(Integer.parseInt(dec)),-len,"0");
    }

    /*
    *  Hexadecimal integer to n-bit binary
    */
    public String HexToBinary( String hex, int len ) {
        return pad((Object)Integer.toBinaryString(Integer.parseInt(hex,16)),-len,"0");
    }
}