package senser;

import de.rudolfbaun.oos2.AircraftDisplay;
import de.rudolfbaun.oos2.AircraftSentence.*;
import org.json.*;

import jsonstream.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class Senser implements Runnable
{
	PlaneDataServer server;
	Boolean debug = true;


	public Senser(PlaneDataServer server)
	{
		this.server = server;
	}

	private String[] getSentenceStrings()
	{
		String list = server.getPlaneListAsString();

		return list.split("(?<=]),");
	}

	private Vector<AircraftSentence> createAircraftSentencesFromString(AircraftSentenceFactory aircraftFactory){

		String[] sentences;
		Vector<AircraftSentence> aircraftSentences = new Vector<AircraftSentence>();

		sentences = getSentenceStrings();

		for (String sentence : sentences){
			AircraftSentence aircraftSentence = aircraftFactory.createAircraftSentenceFromString(sentence);
			aircraftSentences.add(aircraftSentence);
		}

		return aircraftSentences;
	}

	private Vector<AircraftSentence> createAircraftSentencesFromJSONArray(AircraftSentenceFactory aircraftFactory){

		JSONArray aircraftArray;
		Vector<AircraftSentence> aircraftSentences = new Vector<AircraftSentence>();

		aircraftArray = server.getPlaneArray();

		//Documentation says JSONArray has .iterator() different Version?
		for(int i = 0; i < aircraftArray.length(); i++){
			aircraftSentences.add(aircraftFactory.createAircraftSentenceFromJSONArray(aircraftArray.getJSONArray(i)));
		}

		return aircraftSentences;
	}

	private void displayAircraftSentences(Vector<AircraftSentence> aircraftSentences, AircraftDisplay aircraftDisplay){
		System.out.println("\nThere are " + aircraftSentences.size() + " Aircraft in range.");

		Iterator<AircraftSentence> aircraftIterator = aircraftSentences.iterator();
		while (aircraftIterator.hasNext()) {
			aircraftDisplay.display(aircraftIterator.next());
		}
	}
	
	public void run()
	{
		AircraftSentenceFactory sentenceFactory = new AircraftSentenceFactory();
		AircraftDisplay display = new AircraftDisplay();
		
		while (true)
		{

			Vector<AircraftSentence> aircraftSentencesFromString = this.createAircraftSentencesFromString(sentenceFactory);
			Vector<AircraftSentence> aircraftSentencesFromJSONArray = this.createAircraftSentencesFromJSONArray(sentenceFactory);

			if(debug) System.out.println("\n\nThies were created from String:");
			displayAircraftSentences(aircraftSentencesFromString, display);

			if(debug) System.out.println("\n\nThies were created from JSONArray:");
			if(debug) displayAircraftSentences(aircraftSentencesFromJSONArray, display);


		}
	}
}