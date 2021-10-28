package de.rudolfbaun.oos2.AircraftSentence;

import org.json.JSONArray;

public class AircraftSentenceFactory {

    public AircraftSentence createAircraftSentenceFromString(String sentence){
        //AircraftSentence.aircraft = sentence is an option
        return new AircraftSentence(sentence);
    }

    public AircraftSentence createAircraftSentenceFromJSONArray(JSONArray sentence){
        //AircraftSentence.aircraft = sentence.toString() is an option
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
