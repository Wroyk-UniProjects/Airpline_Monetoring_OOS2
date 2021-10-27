package senser;

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

	private void useStringOfSentences(){

		String aircraftList;
		//TODO splitt string into seperad Sentences
		aircraftList = getSentences();
		System.out.println(aircraftList);
	}

	private void useJSONArrayOfSentences(AircraftSentenceFactory aircraftFactory){

		JSONArray planeArray;

		planeArray = server.getPlaneArray();

		Vector<AircraftSentence> sentences = new Vector<AircraftSentence>();
		for(int i = 0; i > planeArray.length(); i++){
			sentences.add(aircraftFactory.createAircraftSentenceFromJSONArray(planeArray.getJSONArray(i)));
		}

		System.out.println("Aircraft in Range: " + sentences.size());

		Iterator<AircraftSentence> sentencesIterator = sentences.iterator();
		while (sentencesIterator.hasNext()){

		}
		System.out.println(planeArray);
	}
	
	public void run()
	{
		
		while (true)
		{

			//this.useStringOfSentences();

			this.useJSONArrayOfSentences();

		}		
	}
}