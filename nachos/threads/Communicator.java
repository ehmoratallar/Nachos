package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
	
	conditionLock.acquire();
	    
		while(speakerReady == true){
			condLock.sleep();
		}
		//~ else{
			speakerReady = true;
			
			if(listenerReady){
			
				data = word;
				
				//~ speakerReady = false;
				
				condLock.wakeAll();
			}
			else{
				condLock.sleep();
				
				data = word;
				//~ speakerReady = false;
			}
		//~ }
		
	    
	conditionLock.release();
	    
	
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
	    
	    
	conditionLock.acquire();
		
		int aux=0;
	    
		while(listenerReady == true){
			condLock.sleep();
		}
		//~ else{
			listenerReady = true;
			condLock.wakeAll();
			
			if(speakerReady){
				
				aux = data;
				speakerReady = false;
				
				condLock.wakeAll();
			}
			else{
				condLock.sleep();
				
				aux = data;
				speakerReady = false;
				condLock.wakeAll();
				
			}
		//~ }
	
	
	listenerReady = false;   
	conditionLock.release();
	    
	return aux;
    }
    
    
	public static void selfTest(){
	    
	System.out.println("Self Test Communicator");
	
	CommunicatorTest.runTest();
		
	}
    
    private Lock conditionLock = new Lock();
    private Condition2 condLock = new Condition2(conditionLock);
    private boolean listenerReady = false;
    private boolean speakerReady = false;
    private int data;
    
}
