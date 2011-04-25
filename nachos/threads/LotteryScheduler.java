package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.*;

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
   
    /* The default priority for a new thread. Do not change this value.
    */
   public static final int priorityDefault = 1;
   /**
    * The minimum priority that a thread can have. Do not change this value.
    */
   public static final int priorityMinimum = 1;
   /**
    * The maximum priority that a thread can have. Do not change this value.
    */
   public static final int priorityMaximum = Integer.MAX_VALUE;  
   
   /**
    * Tests whether this module is working.
    */
  public static void selfTest() {
        //~ Lib.debug(dbgThread, "Enter LotteryScheduler.selfTest");
	  
        KThread[] threads = new KThread[20]; 
        for (int i = 1; i < 11; i++)
        {
                threads[i-1] = new KThread(new Runnable() {
                           public void run() {
                                   int j = 0;
                                   while (j < 20)
                                   {
                                           long currentTime = Machine.timer().getTime();
                                           while (Machine.timer().getTime() < currentTime + 500)
                                           {
                                                   KThread.yield();
                                           }
                                           System.out.println(KThread.currentThread().getName() + " loop # " + j);
                                           j++;
                                   }
                                   }
                           }).setName("Thread #" + i);
        }
        for (int i = 0; i < 10; i++)
        {
                threads[i].fork();
                ((LotteryScheduler.ThreadState)threads[i].schedulingState).setPriority(50*i);
        }
        KThread.yield();
} 


     /**
     * Allocate a new lottery scheduler.
     */
    public LotteryScheduler() {
    }
    
    /**
     * Allocate a new lottery thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer tickets from waiting threads
     *					to the owning thread.
     * @return	a new lottery thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	// implement me
	return new LotteryQueue(transferPriority);
    }
    




	protected class LotteryQueue extends PriorityQueue{
		
		/*
		*
		*  Campos
		*
		*/
		
		public Hashtable<Integer, LinkedList<LotteryThreadState>> lotteryTicketSelectTable = new Hashtable<Integer, LinkedList<LotteryThreadState>>();
		public Hashtable<LotteryThreadState, LinkedList<Integer>> ownedTicketTable = new Hashtable<LotteryThreadState, LinkedList<Integer>>();
		public Hashtable<LotteryThreadState, LinkedList<Integer>> donatedTicketTable = new Hashtable<LotteryThreadState, LinkedList<Integer>>();
		
		public boolean transferPriority;	
		public LinkedList <ThreadState> esperando = new LinkedList <ThreadState>();	
		public ThreadState LockHolder = null;	
		
		/*
		*
		* Constructor
		*
		*/
		public LotteryQueue(boolean transferPriority){
				super(transferPriority);
			}
		
		
		/*
		*
		*  Metodos
		*
		*/
		
			//~ @override
			public KThread nextThread(){
				LotteryThreadState actual = (LotteryThreadState)getThreadState(KThread.currentThread());
				if(actual == LockHolder){
					int i = 0; 
					while(i < esperando.size()){
						
						int prioridad = esperando.get(i).getPriority();
						
						if(transferPriority){
							clearDonatedTickets(actual);
						}
						
						esperando.get(i).setEffectivePriority(prioridad);
						calculateTickets((LotteryThreadState)esperando.get(i),prioridad);
						i++;
					}
				}
				
				LotteryThreadState next = pickNextThread();
				
				if(next == null){
					LockHolder = null;
					return null;
				}
				else{
				
					esperando.remove(next);
					next.acquire(this);
				}
				
				return next.thread;
			}
			
			//~ @override
			public LotteryThreadState pickNextThread(){
				
				if(esperando.size() == 0){
					return null;
				}
				
				ArrayList hashKeys = new ArrayList(lotteryTicketSelectTable.keySet());
				Integer winner = (Integer) hashKeys.get((int)(Math.random() * hashKeys.size()));
				
				
				return (lotteryTicketSelectTable.get(winner)).getLast();
			}			
				
				
			public void clearDonatedTickets(LotteryThreadState ts){
			
				LinkedList<Integer> ticketList = donatedTicketTable.get(ts.thread);
				LinkedList<LotteryThreadState> aux;
				
				if(ticketList.size() >0 || ticketList != null){
					
					for(int i =0; i < ticketList.size(); i++){
					
						aux = lotteryTicketSelectTable.get(ticketList.get(i));
						while(aux.size() > 1){
							aux.removeLast();
						}
						lotteryTicketSelectTable.put((Integer)i, aux);
					}
					
					ticketList.clear();
				}	
				
				
			}

			
			public void calculateTickets(LotteryThreadState ts, int priority) {
				
				LinkedList<Integer> ticketList = ownedTicketTable.get(ts.thread);
				boolean aux;
				LinkedList<LotteryThreadState> lista = new LinkedList<LotteryThreadState>();
				
				Integer remainingTickets = priority * 20;
				
				
				if(ticketList.size() >0 || ticketList != null){
					
					for(int i = 0; i < ticketList.size(); i++){
						lotteryTicketSelectTable.remove(ticketList.get(i));
					}
					
					ticketList.clear();
					ownedTicketTable.put(ts, ticketList);
					
					
				}
				
				
				//~ if(ticketList.size() == 0 || ticketList == null){
					
					while(remainingTickets > 0){
					
						for(int i = 0; i < priorityMaximum; i++){
							aux = lotteryTicketSelectTable.containsKey((Integer)i);
							
							if(!aux){
								lista.add(ts);
								ticketList.add(i);
								lotteryTicketSelectTable.put(i,lista);
								remainingTickets -= 1;
								
							}
						}
						
					}
					
					ownedTicketTable.put(ts,ticketList);
				//~ }
				
			}
				
				
				
				
			
			
		
	}



	protected class LotteryThreadState extends ThreadState{
		
		
		public LotteryThreadState(KThread thread) {
			super(thread);
			//~ this.numberOfTickets=priorityDefault;
		}
		
		
		public void setPriority(int priority) {
			
		}
		
		
		public void waitForAccess(LotteryQueue waitQueue) {
			
			
			if(waitQueue.esperando.size()==0){
				waitQueue.LockHolder = this;
			}else{
				if(waitQueue.transferPriority){
					waitQueue.LockHolder.setPriority(this.getPriority() + waitQueue.LockHolder.getPriority());
					this.setEffectivePriority(priorityMinimum);
					this.calculateTickets(waitQueue, this.getEffectivePriority());
				}
			}
		    timeP = Machine.timer().getTime();
		    waitQueue.esperando.add(this);
				
				//~ calculateTickets(this, this.getPriority, waitQUeue.transferPriority);
			calculateTickets(waitQueue,this.getEffectivePriority());
			
			
		}
		
		
		public void clearDonatedTickets(LotteryQueue waitQueue){
			
			LinkedList<Integer> ticketList = waitQueue.donatedTicketTable.get(this.thread);
			
			
			if(ticketList.size() >0 || ticketList != null){
				ticketList.clear();
			}	
			
			
		}

		
		public void calculateTickets(LotteryQueue waitQueue,int priority) {
			
			LinkedList<Integer> ticketList = waitQueue.ownedTicketTable.get(this.thread);
			boolean aux;
			LinkedList<LotteryThreadState> lista = new LinkedList<LotteryThreadState>();
			
			Integer remainingTickets = priority * 20;
			
			
			if(ticketList.size() >0 || ticketList != null){
				
				for(int i = 0; i < ticketList.size(); i++){
					waitQueue.lotteryTicketSelectTable.remove(ticketList.get(i));
				}
				
				ticketList.clear();
				waitQueue.ownedTicketTable.put(this, ticketList);
				
				
			}
			
			
			//~ if(ticketList.size() == 0 || ticketList == null){
				
				while(remainingTickets > 0){
				
					for(int i = 0; i < priorityMaximum; i++){
						aux = waitQueue.lotteryTicketSelectTable.containsKey((Integer)i);
						
						if(!aux){
							lista.add(this);
							ticketList.add(i);
							waitQueue.lotteryTicketSelectTable.put(i,lista);
							remainingTickets -= 1;
							
						}
					}
					
				}
				
				waitQueue.ownedTicketTable.put(this,ticketList);
			//~ }
			
		}
		
		
		protected float timeP;
		
	}
}

