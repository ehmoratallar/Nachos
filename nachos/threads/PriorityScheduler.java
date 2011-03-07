package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

import java.util.*;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);
	
	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
   
















protected class PriorityQueue extends ThreadQueue {
	PriorityQueue(boolean transferPriority) {
	    this.transferPriority = transferPriority;
	}

	public void waitForAccess(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).waitForAccess(this);
	}

	public void acquire(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).acquire(this);
	}

	public KThread nextThread() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me

		
		ThreadState temp =(this.pickNextThread());
		
		
		if(temp == lockHolder){
			lockHolder = null;
			for(int i = 0; i < waitLine.size(); i++){
				waitLine.get(i).useDefaultPriority = true;
			}
		}
		
		waitLine.remove(temp);
		if(temp == null){
			return null;
		}
		else{
		
			return temp.thread;
		}
	}

	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	protected ThreadState pickNextThread() {
	    // implement me
		
		float timerCompare = Machine.timer().getTime();
		ThreadState aux = null;
		int index = 0;
		
		//~ System.out.println("Debug-->"+waitLine.toString());
		
		if(waitLine.size() > 0){
			aux = (ThreadState)waitLine.getFirst();
		}
		else{
			return null;
			
		}
		
		
		
		for(int i = 0; i < waitLine.size(); i++){
			
			if(aux.useDefaultPriority){
				if( waitLine.get(i).useDefaultPriority && waitLine.get(i).getPriority() > aux.getPriority()){
					aux = waitLine.get(i);
					index = i;
				}
				else if( waitLine.get(i).useDefaultPriority == false && waitLine.get(i).getEffectivePriority() > aux.getPriority()){
					aux = waitLine.get(i);
					index = i;
				}
				else if( waitLine.get(i).useDefaultPriority && waitLine.get(i).getEffectivePriority() == aux.getPriority()){
					
					if( (timerCompare - aux.timeQueued) > (timerCompare - waitLine.get(i).timeQueued)){
						aux = waitLine.get(i);
						index = i;
					}
				}
				else if( waitLine.get(i).useDefaultPriority == false && waitLine.get(i).getEffectivePriority() == aux.getPriority()){
					
					if( (timerCompare - aux.timeQueued) > (timerCompare - waitLine.get(i).timeQueued)){
						aux = waitLine.get(i);
						index = i;
					}
				}
			}
			else{
				
				if( waitLine.get(i).useDefaultPriority && waitLine.get(i).getPriority() > aux.getEffectivePriority()){
					aux = waitLine.get(i);
					index = i;
				}
				else if( waitLine.get(i).useDefaultPriority == false && waitLine.get(i).getEffectivePriority() > aux.getEffectivePriority()){
					aux = waitLine.get(i);
					index = i;
				}
				
				else if( waitLine.get(i).useDefaultPriority && waitLine.get(i).getEffectivePriority() == aux.getEffectivePriority()){
					
					if( (timerCompare - aux.timeQueued) > (timerCompare - waitLine.get(i).timeQueued)){
						aux = waitLine.get(i);
						index = i;
					}
				}
				
				else if( waitLine.get(i).useDefaultPriority == false && waitLine.get(i).getEffectivePriority() == aux.getEffectivePriority()){
					
					if( (timerCompare - aux.timeQueued) > (timerCompare - waitLine.get(i).timeQueued)){
						aux = waitLine.get(i);
						index = i;
					}
				}
				
			}
			
		}
		
		
		
		
		
		lockHolder = aux;
		return aux;
	}
	
	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;
	
	public ThreadState lockHolder;
	public LinkedList<ThreadState> waitLine = new LinkedList<ThreadState>();
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
   

protected class ThreadState {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread) {
	    this.thread = thread;
	    
	    setPriority(priorityDefault);
	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
	    return priority;
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
	    // implement me
	    return effectivePriority;
	}

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {
	    if (this.priority == priority)
		return;
	    
	    this.priority = priority;
	    
	    // implement me
	    
	    if(this.priority < priorityMinimum){
			
			this.priority = priorityMinimum;
			
		    }
		else if(this.priority > priorityMaximum){
			
			this.priority = priorityMaximum;
			
		}
	    
	}
	
	
	
	
	public void setEffectivePriority(int priority) {
		if (this.effectivePriority == effectivePriority)
			return;
		    
		this.effectivePriority = effectivePriority;
		    
		if(this.effectivePriority < priorityMinimum){
			
			this.effectivePriority = priorityMinimum;
			
		    }
		else if(this.effectivePriority > priorityMaximum){
			
			this.effectivePriority = priorityMaximum;
			
		}
	   
	}
	
	

	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	waitQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	public void waitForAccess(PriorityQueue waitQueue) {
	    // implement me
		
		if(waitQueue.waitLine.isEmpty()){
			
			waitQueue.lockHolder = this;
			timeQueued = Machine.timer().getTime();
			waitQueue.waitLine.add(this);
			
		}else{
			
			if(waitQueue.transferPriority){
				this.useDefaultPriority = false;
				this.setEffectivePriority(priorityMinimum);
				waitQueue.lockHolder.setPriority(waitQueue.lockHolder.effectivePriority+this.getPriority());
			}
			timeQueued = Machine.timer().getTime();
			waitQueue.waitLine.add(this);
			
			
		}
		
		
		
	}
	
	

	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */
	public void acquire(PriorityQueue waitQueue) {
	    // implement me
		
		Lib.assertTrue(Machine.interrupt().disabled());
		       
		Lib.assertTrue(waitQueue.waitLine.isEmpty());
		
		this.useDefaultPriority = false;
		waitQueue.lockHolder = this;
		
		
	}	

	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;
	
	protected int effectivePriority;
	
	protected float timeQueued;
	
	private boolean useDefaultPriority = true;
	
    }
}
