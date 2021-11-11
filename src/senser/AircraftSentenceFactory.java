package senser;

import org.json.JSONArray;

import java.util.Vector;

public class AircraftSentenceFactory {

    public AircraftSentence createAircraftSentenceFromString(String sentence){
        //AircraftSentence.aircraft = sentence is an option
        return new AircraftSentence(sentence);
    }

    public AircraftSentence createAircraftSentenceFromJSONArray(JSONArray sentence){
        //AircraftSentence.aircraft = sentence.toString() is an option
        return new AircraftSentence(sentence.toString());
    }

    public Vector<AircraftSentence> createAircraftSentenceVectorFromJSONArray(JSONArray planeArray){

        Vector<AircraftSentence> aircraftSentences = new Vector<AircraftSentence>();

        //Documentation says JSONArray has .iterator() different Version?
        for(int i = 0; i < planeArray.length(); i++){
            aircraftSentences.add(this.createAircraftSentenceFromJSONArray(planeArray.getJSONArray(i)));
        }

        return aircraftSentences;
    }

    public Vector<AircraftSentence> createAircraftSentenceVectorFromString(String planeString){

        Vector<AircraftSentence> aircraftSentences = new Vector<AircraftSentence>();

        String[] sentences = planeString.split("(?<=]),");

        for (String sentence : sentences){
            AircraftSentence aircraftSentence = this.createAircraftSentenceFromString(sentence);
            aircraftSentences.add(aircraftSentence);
        }

        return aircraftSentences;
    }
}
