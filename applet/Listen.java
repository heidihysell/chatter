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
  String [] words = {"RT", "rt:", "RT:", "rt", "xo", "XO", "Lol", "LOL", "lol", "um", "<3", "hehe", "w/", ":D", ":P", "tbh", "pff", "hah", "ppl", "D:", "XD", "lmao", "LMAO", "ugh", "haha"}; 

  Recognizer recognizer;
  Microphone microphone;
  Utterance utterance;
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
    //System.out.println("Microphone off test");
    try {
      listenEventMethod = parent.getClass().getMethod("listenEvent", new Class[] { Listen.class });
      
      // Initialse the voice recognition
      System.out.println("path: " + path);
      System.out.println("config: " + config);
      URL url = new URL("file:///" + path + config);
      ConfigurationManager cm = new ConfigurationManager(url);
      recognizer = (Recognizer) cm.lookup("recognizer");
      microphone = (Microphone) cm.lookup("microphone");
      recognizer.allocate();     
      
      // initialize the speech synthesizer
//      speech = new NSSpeechSynthesizer();

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
    // start listening and recording
    microphone.startRecording(); // we only want to call the startRecording function once before calling stop - otherwise each spawns it's own thread and we will run out of memory from threads taking over
    while (true) {
      if (microphone.isRecording()) { // we use isRecord to check if the recording is active so that we don't spawn a new thread and hog memory
        System.out.println("Microphone on");
        
        String recorded = "";
        if (microphone.getData() != null) {
          recorded = microphone.getData().toString();
        }
  
        //recognizer.getBestFinalResultNoFiller();
        System.out.println("recorded: " + recorded);
        if (recorded != "")
        {
          Result result = recognizer.recognize(); // ask the recognizer if it recognizes the text in any part of the string already recorded from the microphone
          String pass_result = result.toString();
          parent.println("RECOGNISED: " + pass_result);
          microphone.stopRecording();  
    
          //System.out.println("Microphone off test");
          System.out.println("Preforming twitter feed lookup");
          String s = pass_result;
                            
          try {
            Query query = new Query(s);
            //println(query);
            query.setRpp(100);
            QueryResult qresult = myTwitter.search(query);
    
            ArrayList tweets = (ArrayList) qresult.getTweets();
            
            Pattern userStr = Pattern.compile("\\B\\@([a-zA-Z0-9_]{1,20})");
            Pattern hashStr = Pattern.compile("\\B\\#([a-zA-Z0-9_]{1,20})");
            Pattern urlStr = Pattern.compile("((www\\.|(http|https|ftp|news|file|)+\\:\\/\\/)[&#95;.a-z0-9-]+\\.[a-z0-9\\/&#95;:@=.+?,##%&~-]*[^.|\\'|\\# |!|\\(|?|,| |>|<|;|\\)])");
            qresult = null; 
            System.out.println(tweets.size());
            Random r = new Random();
          
            int randint = r.nextInt(tweets.size());
            Tweet x = (Tweet) tweets.get(randint);
          
            String user = x.getFromUser();
            s = x.getText();
        
            Matcher patternMatch = userStr.matcher(s);
            if (patternMatch.find()) {
              s = patternMatch.replaceAll("");
            }
    
            patternMatch = hashStr.matcher(s);
            if (patternMatch.find()) {
              s = patternMatch.replaceAll("");
            }
    
            patternMatch = urlStr.matcher(s);
            if (patternMatch.find()) {
              s = patternMatch.replaceAll("");
            }
                      
            for (int i = 0; i < words.length; i++)
            {
              Pattern wordStr = Pattern.compile("\\b" + words[i]);
              Matcher patternWords = wordStr.matcher(s);
              if (patternWords.find()) {
                s = patternWords.replaceAll("");
              }
            }
                   
            z_feed = s;
          }
          catch (TwitterException te) {
            System.out.println("Couldn't connect: " + te);
          } 
    
          System.out.println(z_feed);
                            
          //speech.startSpeakingString(z_feed);
                            
          //myVM = VoiceManager.getInstance();
          //if (myVM==null) {
          //  System.err.println("Voice manager not available.");
          // System.exit(1);
          //}
    		
          //myVoice = myVM.getVoice("kevin16");
          //String voices = myVM.toString();
          //System.out.println(voices);
          //if (myVoice==null) {
          //  System.err.println("Default voice not available.");
          //System.exit(1);
          //}
    
          //myVoice.allocate();
          //myVoice.speak(z_feed);
          //myVoice.deallocate();
          z_feed = "";
          s = "";
                           
          resultText = result.getBestFinalResultNoFiller();
          if (resultText.length() > 0) {
            makeEvent();
          }
        }
      }
    }
  }

  public String readString() {
    return resultText;
  }

  public void makeEvent() {
    if (listenEventMethod != null) {
      try {
        listenEventMethod.invoke(parent, new Object[] { this });
      } 
      catch (Exception e) {
        e.printStackTrace();
        listenEventMethod = null;
      }
    }
  }

  public void dispose() {
    recognizer.deallocate();
  }
}





