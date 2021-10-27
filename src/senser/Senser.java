package senser;

import de.rudolfbaun.oos2.AircraftDisplay;
import de.rudolfbaun.oos2.AircraftSentence;
import de.rudolfbaun.oos2.AircraftSentenceFactory;
import org.json.*;

import jsonstream.*;

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
		Vector<AircraftSentence> sentences = new Vector<AircraftSentence>();

		aircraftList = getSentences();
		System.out.println(aircraftList);

		//TODO splitt aircraftList into seperad Sentences

		sentences.add(aircraftFactory.createAircraftSentenceFromString(aircraftList));


		System.out.println("\nAircraft in Range: " + sentences.size());

		Iterator<AircraftSentence> sentencesIterator = sentences.iterator();
		while (sentencesIterator.hasNext()) {
			aircraftDisplay.display(sentencesIterator.next());
		}
	}

	private void useJSONArrayOfSentences(AircraftSentenceFactory aircraftFactory, AircraftDisplay aircraftDisplay){

		JSONArray planeArray;
		Vector<AircraftSentence> sentences = new Vector<AircraftSentence>();

		planeArray = server.getPlaneArray();
		//System.out.println(planeArray);


		for(int i = 0; i < planeArray.length(); i++){
			sentences.add(aircraftFactory.createAircraftSentenceFromJSONArray(planeArray.getJSONArray(i)));
		}

		System.out.println("\nAircraft in Range: " + sentences.size());

		Iterator<AircraftSentence> sentencesIterator = sentences.iterator();
		while (sentencesIterator.hasNext()){
			aircraftDisplay.display(sentencesIterator.next());
		}
	}
	
	public void run()
	{
		AircraftSentenceFactory sentenceFactory = new AircraftSentenceFactory();
		AircraftDisplay display = new AircraftDisplay();
		
		while (true)
		{

			//this.useStringOfSentences(sentenceFactory, display);

			this.useJSONArrayOfSentences(sentenceFactory, display);

		}		
	}
}