
import java.lang.*;

/**
*   class Counter
*   This class runs the execution of instructions in a separate thread such that
*   all other actions can be performed while the program is being executed.
*   Thus simultaneous activities can take place in the whole appliction.
*/

public class Counter extends Thread {
    ExeInst einst;
    public  Counter( ExeInst exeInst ) {
        einst = exeInst;
    }

    /*
    * Implements the run method of Thread.
    */
    public void run() {
        while(!LC.halt && !LC.pause && !LC.BPseen && !LC.assembleError) {
            LC.delay((long)LC.delayTime);
            einst.controlUnit(false);   // do not override BP
        }
        if(LC.halt) {
            LC.writeMessage("Processor halted - PC = x"+LC.lastPC+".");
            LC.runAssemble.setEnabled(true); LC.Assemble.setEnabled(true);
            LC.runStep.setEnabled(true); LC.Step.setEnabled(true);
            LC.runGo.setEnabled(true); LC.Go.setEnabled(true);
            LC.runResume.setEnabled(true); LC.Resume.setEnabled(true);
            LC.runPause.setEnabled(false); LC.Pause.setEnabled(false);
            LC.halt=false;
        }
        if(LC.pause) 
            LC.writeMessage("Execution paused.");
        if(LC.BPseen) {
            LC.writeMessage("BreakPoint encountered - PC = x"+LC.pc);
            LC.runStep.setEnabled(true); LC.Step.setEnabled(true);
            LC.runGo.setEnabled(true); LC.Go.setEnabled(true);
            LC.runAssemble.setEnabled(true); LC.Assemble.setEnabled(true);
            LC.runResume.setEnabled(true); LC.Resume.setEnabled(true);
            LC.runPause.setEnabled(false); LC.Pause.setEnabled(false);
        }
        if(LC.assembleError) {
            LC.writeMessage("Error in assembly process.");
        }
    }
}