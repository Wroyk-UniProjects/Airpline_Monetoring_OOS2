package senser;

import de.rudolfbaun.oos2.AircraftDisplay;
import de.rudolfbaun.oos2.AircraftSentence;
import de.rudolfbaun.oos2.AircraftSentenceFactory;
import org.json.*;

import jsonstream.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class Senser implements Runnable
{
	PlaneDataServer server;


	public Senser(PlaneDataServer server)
	{
		this.server = server;
	}

	private String getSentences()
	{
		String list = server.getPlaneListAsString();
		return list;
	}

	private void useStringOfSentences(AircraftSentenceFactory aircraftFactory, AircraftDisplay aircraftDisplay){

		String aircraftList;
		Vector<AircraftSentence> aircraft = new Vector<AircraftSentence>();

		aircraftList = getSentences();

		String[] sentences = aircraftList.split("],");

		Iterator<String> sentencesIterator = Arrays.stream(sentences).iterator();
		while(sentencesIterator.hasNext()){
			aircraft.add(aircraftFactory.createAircraftSentenceFromString(sentencesIterator.next() + "]"));
		}

		System.out.println("\nAircraft in Range: " + aircraft.size());

		Iterator<AircraftSentence> aircraftIterator = aircraft.iterator();
		while (aircraftIterator.hasNext()) {
			aircraftDisplay.display(aircraftIterator.next());
		}
	}

	private void useJSONArrayOfSentences(AircraftSentenceFactory aircraftFactory, AircraftDisplay aircraftDisplay){

		JSONArray planeArray;
		Vector<AircraftSentence> aircraft = new Vector<AircraftSentence>();

		planeArray = server.getPlaneArray();
		//System.out.println(planeArray);


		for(int i = 0; i < planeArray.length(); i++){
			aircraft.add(aircraftFactory.createAircraftSentenceFromJSONArray(planeArray.getJSONArray(i)));
		}

		System.out.println("\nAircraft in Range: " + aircraft.size());

		Iterator<AircraftSentence> aircraftIterator = aircraft.iterator();
		while (aircraftIterator.hasNext()){
			aircraftDisplay.display(aircraftIterator.next());
		}
	}
	
	public void run()
	{
		AircraftSentenceFactory sentenceFactory = new AircraftSentenceFactory();
		AircraftDisplay display = new AircraftDisplay();
		
		while (true)
		{

			this.useStringOfSentences(sentenceFactory, display);

			//this.useJSONArrayOfSentences(sentenceFactory, display);

		}		
	}
}