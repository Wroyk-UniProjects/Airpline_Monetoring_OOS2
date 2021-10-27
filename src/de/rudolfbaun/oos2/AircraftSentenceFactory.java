package de.rudolfbaun.oos2;

import jdk.jshell.spi.ExecutionControl;
import org.json.JSONArray;

import java.util.List;

public class AircraftSentenceFactory {

    public AircraftSentence createAircraftSentenceFromJSONArray(String sentence){
        return new AircraftSentence(sentence);
    }

    public AircraftSentence createAircraftSentenceFromJSONArray(JSONArray sentence){

        return new AircraftSentence(sentence.toString());
    }
    /*
    public List<AircraftSentence> createAircraftSentencesFromJSONArray(JSONArray planeArray){

        List<AircraftSentence> sentences;
        return sentences;
    }

    public List<AircraftSentence> createAircraftSentencesFromString(String planeString){

        List<AircraftSentence> sentences;
        return sentences;
    }
     */
}
