import twitter4j.*;
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import java.util.regex.Pattern.*;
import java.util.regex.Matcher;
import edu.cmu.sphinx.linguist.flat.FlatLinguist;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.util.Random;

boolean DEBUG = true;

Twitter myTwitter;
Listen reading;
Flock flock;
Particle ball;
Particle arriver;


int max_width = 1500; 
int max_height = 900; 

String s = "";

Flock finding = new Flock();
Flock finding2 = new Flock();
Flock finding3 = new Flock();
Flock finding4 = new Flock();

void loadTwitter() {
     myTwitter = new Twitter("subcultured", "cl0ckw0rk");
   
    try {
		String searchKeys = "love OR hate OR sad OR happy";
		  
		// Search twitter for tweets matching the query

		println("Searching Twitter for " + searchKeys);
		  
		Query tquery = new Query(searchKeys);
		  
		tquery.setRpp(100);
		
		QueryResult result = myTwitter.search(tquery);
		
		for (int i = 0; i < 6; i++) 	// Loop though 4 pages of search results
		{
			ArrayList tweets = (ArrayList) result.getTweets();

			println("tweets found : " + tweets.size());
			
			// Set the next page for the search results
			tquery.setPage(tquery.getPage() + 1);
			
			// Create a wanderer class for each of the search results and add it to the array
			for (int k = 0; k < tweets.size(); k++) {
  
                                int colCount = 0;
                                int colRed = 0;
                                int colGreen = 0;
                                int colBlue = 0;
                                
				 Particle ball = new Particle(new PVector(random(max_width), random(max_height)) );   
				 
				// Get the tweet text
                                String tweetText = tweets.get(k).toString();	
				ball.setTweet(tweetText);	
				// Set the tweet text in the class
				
                                // convert the text to lowercase (for searching)
				tweetText = tweetText.toLowerCase(); 			
				if (tweetText.contains("love"))
				{
                                        colRed += 216;
                                        colGreen += 20;
                                        colBlue += 20;
                                        colCount++;
                                        finding.addParticle(ball);
				}
				if (tweetText.contains("hate"))
				{
                                        colRed += 129;
                                        colGreen += 160;
                                        colBlue += 162;
                                        colCount++;
                                        finding2.addParticle(ball);
				}
				
				if (tweetText.contains("sad"))
				{
                                        colRed += 84;
                                        colGreen += 151;
                                        colBlue += 180;
                                        colCount++;
                                        finding3.addParticle(ball);
				}
				
				if (tweetText.contains("happy"))
				{
                                        colRed += 229;
                                        colGreen += 212;
                                        colBlue += 16;
                                        colCount++;
                                        finding4.addParticle(ball);
				}

                               if (colCount > 1)
                                {
                                  colRed = colRed / colCount;
                                  colGreen = colGreen / colCount;
                                  colBlue = colBlue / colCount;
                                }
                                
                                ball.setColor(color(colRed, colGreen, colBlue));
				
			}
		}
		
		println("Number of balls: " + finding.particles.size());
	}
	catch (TwitterException te) {
		println("Couldn't connect: " + te);
	
}


}

void setup() {
 size(max_width, max_height);
 background(0);
 noStroke();
 smooth();
 
 loadTwitter();
 reading = new Listen(this,"sample.config.xml", myTwitter);
 frameRate(30);


}

void dispose() {
  reading.dispose();
}

void draw() {
  background(0);
  int findingSize = finding.particles.size();
  for (int m = 0; m < findingSize; m++) {
    Particle ball = (Particle) finding.particles.get(m);  
    
  }	
  
  finding.run();	
  
  int finding2Size = finding2.particles.size();
  for (int m = 0; m < finding2Size; m++) {
    Particle ball = (Particle) finding2.particles.get(m); 
    
  }
  finding2.run();
  
  int finding3Size = finding3.particles.size();
  for (int m = 0; m < finding3Size; m++) {
    Particle ball = (Particle) finding3.particles.get(m); 
    
  }
  finding3.run();
  
  int finding4Size = finding4.particles.size();
  for (int m = 0; m < finding4Size; m++) {
    Particle ball = (Particle) finding4.particles.get(m); 
    
  }
  finding4.run();
}

void listenEvent(Listen _l) {
   if(DEBUG) System.out.println("listenEvent");
   int k = -5;
   int minDistance = 3;
   background(0);
   s = _l.readString();//returns the tweet value of "love" "hate" "angry" "sad" etc
   if(DEBUG) System.out.println("s: " + s);
   for (int c = 0; c < finding.particles.size(); c++) {  
     if (s.equals("love")){ //if the Listen passed result = word love
       if (finding.particles.contains("love")){
         ball.seek(new PVector(width/2,height/2));
       }
     }
   }
   for (int c = 0; c < finding2.particles.size(); c++) { 
     if (s.equals("hate")){
       if (finding2.particles.contains("hate")){
         ball.arrive(new PVector(width/2,height/2));
       }
     }
   }
   for (int c = 0; c < finding3.particles.size(); c++) { 
     if (s.equals("sad")){
       if (finding3.particles.contains("sad")){
         ball.arrive(new PVector(width/2,height/2));
       }
     }
   }
   for (int c = 0; c < finding2.particles.size(); c++) { 
     if (s.equals("happy")){
       if (finding2.particles.contains("happy")){
         ball.arrive(new PVector(width/2,height/2));
       }
     }
   } 
 }
 
// The Boid class

class Particle {

  PVector loc;
  PVector vel;
  PVector acc;
  float r;
  float maxforce;    // Maximum steering force
  float maxspeed;    // Maximum speed
  float wander_theta;
  float wander_radius;
  
  color col;
  String tweet;
    
  // bigger = more edgier, hectic
  float max_wander_offset = 0.3;
 // bigger = faster turns
  float max_wander_radius = 3.5;
  

    Particle(PVector l) {
    acc = new PVector(0,0);
    vel = new PVector(random(-1,1),random(-1,1));
    loc = l.get();
    r = 2.0;
    wander_theta = random(TWO_PI);
    wander_radius = random(max_wander_radius);
  }

  void run(ArrayList particles) {
    update();
    borders();
    move();
    //flock(particles);  // We don't want them to flock.
	render();
  }
  
 void render()
  {
  	fill(col);
  	ellipse(loc.x, loc.y, 20, 20);
  
  }
  
  void move()
   {
     float wander_offset = random(-max_wander_offset, max_wander_offset);
     wander_theta += wander_offset;
     
     loc.x += cos(wander_theta);
     loc.y += sin(wander_theta);
   }
 

  public void stayInsideCanvas()
   {
  	loc.x %= width;
  	loc.y %= height;
   }

  // We accumulate a new acceleration each time based on three rules
  void flock(ArrayList particles) {
    PVector sep = separate(particles);   // Separation
    PVector ali = align(particles);      // Alignment
    PVector coh = cohesion(particles);   // Cohesion
    // Arbitrarily weight these forces
    sep.mult(1.5);
    ali.mult(1.0);
    coh.mult(1.0);
    // Add the force vectors to acceleration
    acc.add(sep);
    acc.add(ali);
    acc.add(coh);
  }


  // Method to update location
  void update() {
    // Update velocity
    vel.add(acc);
    // Limit speed
    vel.limit(maxspeed);
    loc.add(vel);
    // Reset accelertion to 0 each cycle
    acc.mult(0);
  }

  void seek(PVector target) {
    acc.add(steer(target,false));
  }

  void arrive(PVector target) {
    acc.add(steer(target,true));
  }

  // A method that calculates a steering vector towards a target
  // Takes a second argument, if true, it slows down as it approaches the target
  PVector steer(PVector target, boolean slowdown) {
    //maxspeed = ms;
    //maxforce = mf;
    PVector steer;  // The steering vector
    PVector desired = target.sub(target,loc);  // A vector pointing from the location to the target
    float d = desired.mag(); // Distance from the target is the magnitude of the vector
    // If the distance is greater than 0, calc steering (otherwise return zero vector)
    if (d > 0) {
      // Normalize desired
      desired.normalize();
      // Two options for desired vector magnitude (1 -- based on distance, 2 -- maxspeed)
      if ((slowdown) && (d < 100.0)) desired.mult(maxspeed*(d/100.0)); // This damping is somewhat arbitrary
      else desired.mult(maxspeed);
      // Steering = Desired minus Velocity
      steer = target.sub(desired,vel);
      steer.limit(maxforce);  // Limit to maximum steering force
    } 
    else {
      steer = new PVector(0,0);
    }
    return steer;
  }


  // Wraparound
  void borders() {
    if (loc.x < -r) loc.x = width+r;
    if (loc.y < -r) loc.y = height+r;
    if (loc.x > width+r) loc.x = -r;
    if (loc.y > height+r) loc.y = -r;
  }

  // Separation
  // Method checks for nearby boids and steers away
  PVector separate (ArrayList particles) {
    float desiredseparation = 20.0;
    PVector steer = new PVector(0,0,0);
    int count = 0;
    // For every boid in the system, check if it's too close
    for (int i = 0 ; i < particles.size(); i++) {
      Particle other = (Particle) particles.get(i);
      float d = PVector.dist(loc,other.loc);
      // If the distance is greater than 0 and less than an arbitrary amount (0 when you are yourself)
      if ((d > 0) && (d < desiredseparation)) {
        // Calculate vector pointing away from neighbor
        PVector diff = PVector.sub(loc,other.loc);
        diff.normalize();
        diff.div(d);        // Weight by distance
        steer.add(diff);
        count++;            // Keep track of how many
      }
    }
    // Average -- divide by how many
    if (count > 0) {
      steer.div((float)count);
    }

    // As long as the vector is greater than 0
    if (steer.mag() > 0) {
      // Implement Reynolds: Steering = Desired - Velocity
      steer.normalize();
      steer.mult(maxspeed);
      steer.sub(vel);
      steer.limit(maxforce);
    }
    return steer;
  }

  // Alignment
  // For every nearby boid in the system, calculate the average velocity
  PVector align (ArrayList particles) {
    float neighbordist = 25.0;
    PVector steer = new PVector(0,0,0);
    int count = 0;
    for (int i = 0 ; i < particles.size(); i++) {
      Particle other = (Particle) particles.get(i);
      float d = PVector.dist(loc,other.loc);
      if ((d > 0) && (d < neighbordist)) {
        steer.add(other.vel);
        count++;
      }
    }
    if (count > 0) {
      steer.div((float)count);
    }

    // As long as the vector is greater than 0
    if (steer.mag() > 0) {
      // Implement Reynolds: Steering = Desired - Velocity
      steer.normalize();
      steer.mult(maxspeed);
      steer.sub(vel);
      steer.limit(maxforce);
    }
    return steer;
  }

  // Cohesion
  // For the average location (i.e. center) of all nearby boids, calculate steering vector towards that location
  PVector cohesion (ArrayList particles) {
    float neighbordist = 25.0;
    PVector sum = new PVector(0,0);   // Start with empty vector to accumulate all locations
    int count = 0;
    for (int i = 0 ; i < particles.size(); i++) {
      Particle other = (Particle) particles.get(i);
      float d = loc.dist(other.loc);
      if ((d > 0) && (d < neighbordist)) {
        sum.add(other.loc); // Add location
        count++;
      }
    }
    if (count > 0) {
      sum.div((float)count);
      return steer(sum,false);  // Steer towards the location
    }
    return sum;
  }
  
    color getColor()
 {
	return col;
 }
 
public void setTweet(String text)
 {
	tweet = text;
 }
  
  
public void setColor(color c) 
 {
  col = c;
 }
}
  

// The Flock (a list of Boid objects)

class Flock {
  ArrayList particles; // An arraylist for all the boids

  Flock() {
    particles = new ArrayList(); // Initialize the arraylist
  }

  void run() {
    for (int i = 0; i < particles.size(); i++) {
      Particle b = (Particle) particles.get(i);  
      b.run(particles);  // Passing the entire list of boids to each boid individually
    }
  }

  void addParticle(Particle b) {
    particles.add(b);
  }

}
