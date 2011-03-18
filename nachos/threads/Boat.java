package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	//~ System.out.println("\n ***Testing Boats with only 2 children***");
	//~ begin(0, 2, b);

	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
 	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;

	// Instantiate global variables here
	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.

	//~ Runnable r = new Runnable() {
	    //~ public void run() {
                //~ SampleItinerary();
            //~ }
        //~ };
        //~ KThread t = new KThread(r);
        //~ t.setName("Sample Boat Thread");
        //~ t.fork();
	
	
	
	for(int i = 0; i < adults; i++){
		
		Runnable adult = new Runnable() {
		
		
		public void run() {
			AdultItinerary();
		    }
		};
		KThread t = new KThread(adult);
		t.setName("Adult Thread");
		t.fork();
		
	}
	
	for(int i = 0; i < children; i++){
		
		Runnable child = new Runnable() {
		    public void run() {
			
			ChildItinerary();
		    }
		};
		KThread t = new KThread(child);
		t.setName("Children Thread");
		t.fork();
		
	}
	
	adultsOahu = adults;
	childrenOahu = children;
	
	

    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    static void AdultItinerary()
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
	
	lock.acquire();
	adultOahu.sleep();
	adultsOahu -= 1;
	bg.AdultRowToMolokai();
	childMolokai.wake();
	adultMolokai.sleep();
	
	lock.release();
    }

    
    
    
    
    
    
    

    
    
    
    
    
    
    static void ChildItinerary()
    {
	   
		lock.acquire();
		System.out.println(childrenOahu);
		while(boatInOahu == false){
			childOahu.sleep();
		}
		
		if(turno == 0){
			passengers += 1;

			if(passengers < 2){
				childOahu.sleep();
				bg.ChildRideToMolokai();
				childrenOahu -= 1;
				childrenMolokai += 1;
				childMolokai.wake();
			}
			else{
				childOahu.wake();
				bg.ChildRowToMolokai();
				childrenOahu -= 1;
				childrenMolokai += 1;
				
			}
			
			boatInOahu = false;
			turno = 1;
			passengers = 0;
			childMolokai.sleep();
			
					
			
		}
		
		if(turno == 1){
		
			bg.ChildRowToOahu();
			boatInOahu = true;
			childrenOahu += 1;
			childrenMolokai -=1;
			adultOahu.wake();
			//~ childOahu.wake();
			turno = 0;
		}
		
		if(adultsOahu > 0 || childrenOahu > 0){
			lock.release();
			ChildItinerary();
			lock.acquire();
		}
		
		lock.release();
		
		
		
	    
    }
    
    
    
    
    
    

    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    }
    
    
    
    private static boolean boatInOahu = true;
    private static boolean childrenRide = true;
    private static int passengers = 0;
    private static int turno = 0;
    
    
    private static int adultsOahu = 0;
    private static int childrenOahu = 0;
    private static int childrenMolokai = 0;
    public static Lock lock = new Lock();
    public static Condition adultOahu = new Condition(lock);
    public static Condition adultMolokai = new Condition(lock);
    public static Condition childOahu = new Condition(lock);
    public static Condition childMolokai = new Condition(lock);
    
    
}
