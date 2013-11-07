package simpleSpeech;


import edu.cmu.sphinx.frontend.util.Utterance;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import twitter4j.*;
import java.util.regex.*;
import processing.core.*;
  import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
//import Twit.*;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import java.util.ArrayList;
import javax.swing.*; 
import java.util.Random;


//import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.lang.reflect.*;


public class Listen implements Runnable {
  boolean DEBUG = true;
  String [] words = {"RT", "rt:", "RT:", "rt", "xo", "XO", "Lol", "LOL", "lol", "um", "<3", "hehe", "w/", ":D", ":P", "tbh", "pff", "hah", "ppl", "D:", "XD", "lmao", "LMAO", "ugh", "haha"}; 

  Recognizer recognizer;
  Microphone microphone;
  PApplet parent;
  Method listenEventMethod;
  Twitter myTwitter;
  VoiceManager myVM;
  Voice myVoice;
  NSSpeechSynthesizer speech;
         
  Thread t;
  String resultText;
  String config;
  String path;
  String z_feed ="";

  public Listen(PApplet _p, String _c, Twitter myTwitter) {
    this.parent = _p;
    this.config = _c;
    resultText = "";
    init();

    this.myTwitter = myTwitter;
    
    parent.registerDispose(this);

    t = new Thread(this);
    t.start();

  }

  private void init() {
    path = parent.dataPath("");
    //if(DEBUG) System.out.println("Microphone off test");
    try {
      listenEventMethod = parent.getClass().getMethod("listenEvent", new Class[] { Listen.class });
      
      // Initialse the voice recognition
      if(DEBUG) System.out.println("path: " + path);
      if(DEBUG) System.out.println("config: " + config);
      URL url = new URL("file:///" + path + config);
      ConfigurationManager cm = new ConfigurationManager(url);
      recognizer = (Recognizer) cm.lookup("recognizer");
      microphone = (Microphone) cm.lookup("microphone");
      recognizer.allocate();     
      
      // start listening and recording
      microphone.startRecording(); // we only want to call the startRecording function once before calling stop - otherwise each spawns it's own thread and we will run out of memory from threads taking over

      // initialize the speech synthesizer
      //speech = new NSSpeechSynthesizer();
      
//      myVM = VoiceManager.getInstance(); 
//      if (myVM==null) {
//        System.err.println("Voice manager not available.");
//        System.exit(1);
//      }
//		
//      myVoice = myVM.getVoice("kevin16");
//      String voices = myVM.toString();
//      if(DEBUG) System.out.println(voices);
//      if (myVoice==null) {
//        System.err.println("Default voice not available.");
//      System.exit(1);
//      }
//      myVoice.allocate();

    } catch (IOException ioe) {
      ioe.printStackTrace();
    } catch (PropertyException pe) {
      pe.printStackTrace();
    } catch (InstantiationException ie) {
      ie.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } 

  }

  public void run() {
    while (true) {
      if (DEBUG) System.out.println("TEST-1");
      try {
        if (DEBUG) System.out.println("TEST-2");
        if (microphone.isRecording()) { // we use isRecord to check if the recording is active so that we don't spawn a new thread and hog memory
          if(DEBUG) System.out.println("Microphone on");
          
          String recorded = "";
          if (microphone.getData() != null) {
            recorded = microphone.getData().toString();
          }

          if(DEBUG) System.out.println("recorded: " + recorded);
          if (recorded != "") // if we recorded something other than silence
          {
            // ask the recognizer if it recognizes the text in any part of the string already recorded from the microphone
            Result result = recognizer.recognize(recorded); 
            if(DEBUG) System.out.println("result: " + result);
            if (result != null && result.getBestFinalResultNoFiller() != null && result.getBestFinalResultNoFiller() != "") {
              resultText = result.getBestFinalResultNoFiller();
              if(DEBUG) System.out.println("ACTIVE TOKEN: " + result.getBestFinalResultNoFiller());
              if(DEBUG) System.out.println("RECOGNIZED: " + result.toString());
              
              // since we recognized one of the terms we are looking for we can turn off the microphone to pause the recording
              //microphone.stopRecording();
              
              // retrieve the twitter search results
              // TODO: do we really need to retrieve the twitter results again? Can't we just pass them in the initialize function
              //       instead of retrieving them everytime we go through the loop?
              String speechFeed = retweet(result.getBestFinalResultNoFiller());
              if (DEBUG) System.out.println("speechFeed: " + speechFeed);

              makeEvent();

              //speech.startSpeakingString(speechFeed);
              //myVoice.speak(speechFeed);

            }
          }
        } else { // not recording
          if (DEBUG) System.out.println("TEST-3");
          //if(DEBUG) System.out.println("speaking? " + speech.isSpeaking());
          //if (!speech.isSpeaking()) {
            microphone.clear();
            microphone.startRecording();
          //}
        }
      } catch (Exception e) { 
        if(DEBUG) System.out.println("EXCEPTION Occured ");  
        e.printStackTrace(); 
        if (!microphone.isRecording()) {
          microphone.clear();
          microphone.startRecording();
        }
      }
    }
  }
  
  // get tweets by searchBy criteria and then filter out for text to speech
  protected String retweet(String searchBy) {
          if(DEBUG) System.out.println("Preforming twitter feed lookup");
          String tweet = "";                  
          try {
            Query query = new Query(searchBy);
            //println(query);
            query.setRpp(100);
            QueryResult qresult = myTwitter.search(query);
    
            ArrayList tweets = (ArrayList) qresult.getTweets();
            
            Pattern userStr = Pattern.compile("\\B\\@([a-zA-Z0-9_]{1,20})");
            Pattern hashStr = Pattern.compile("\\B\\#([a-zA-Z0-9_]{1,20})");
            Pattern urlStr = Pattern.compile("((www\\.|(http|https|ftp|news|file|)+\\:\\/\\/)[&#95;.a-z0-9-]+\\.[a-z0-9\\/&#95;:@=.+?,##%&~-]*[^.|\\'|\\# |!|\\(|?|,| |>|<|;|\\)])");
            qresult = null; 
            if(DEBUG) System.out.println(tweets.size());
            Random r = new Random();
          
            int randint = r.nextInt(tweets.size());
            Tweet x = (Tweet) tweets.get(randint);
          
            String user = x.getFromUser();
            tweet = x.getText();
        
            Matcher patternMatch = userStr.matcher(tweet);
            if (patternMatch.find()) {
              tweet = patternMatch.replaceAll("");
            }
    
            patternMatch = hashStr.matcher(tweet);
            if (patternMatch.find()) {
              tweet = patternMatch.replaceAll("");
            }
    
            patternMatch = urlStr.matcher(tweet);
            if (patternMatch.find()) {
              tweet = patternMatch.replaceAll("");
            }
                      
            for (int i = 0; i < words.length; i++)
            {
              Pattern wordStr = Pattern.compile("\\b" + words[i]);
              Matcher patternWords = wordStr.matcher(tweet);
              if (patternWords.find()) {
                tweet = patternWords.replaceAll("");
              }
            }

          }
          catch (TwitterException te) {
            if(DEBUG) System.out.println("Couldn't connect: " + te);
          } 
    
          if(DEBUG) System.out.println(tweet);
          return tweet;
  }

  public String readString() {
    return resultText;
  }

  public void makeEvent() {
    if(DEBUG) System.out.println("makeEvent");
    if (listenEventMethod != null) {
      try {
        if(DEBUG) System.out.println("invoke");
        listenEventMethod.invoke(parent, new Object[] { this });
      } 
      catch (Exception e) {
        e.printStackTrace();
        listenEventMethod = null;
      }
    }
  }

  public void dispose() {
    microphone.stopRecording();
    recognizer.deallocate();
    myVoice.deallocate();
  }
}





