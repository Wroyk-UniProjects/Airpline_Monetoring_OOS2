package senser;

import de.rudolfbaun.oos2.AircraftDisplay;
import de.rudolfbaun.oos2.AircraftSentence.*;
import observer.Observable;
import observer.Observer;
import org.json.*;

import jsonstream.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class Senser implements Runnable, Observable
{
	PlaneDataServer server;
	ArrayList<Observer> observers;
	Boolean debug = true;


	public Senser(PlaneDataServer server)
	{
		this.server = server;
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

			Vector<AircraftSentence> aircraftSentencesFromString = sentenceFactory.createAircraftSentenceVectorFromString(server.getPlaneListAsString());
			Vector<AircraftSentence> aircraftSentencesFromJSONArray = sentenceFactory.createAircraftSentenceVectorFromJSONArray(server.getPlaneArray());

			if(debug) System.out.println("\n\nThies were created from String:");
			displayAircraftSentences(aircraftSentencesFromString, display);

			if(debug) System.out.println("\n\nThies were created from JSONArray:");
			if(debug) displayAircraftSentences(aircraftSentencesFromJSONArray, display);


		}
	}

	@Override
	public void addObserver(Observer o) {
		observers.add(o);
	}

	@Override
	public void deleteObserver(Observer o) {
		observers.remove( 0);
	}

	@Override
	public void notifyObservers() {
		notifyObservers(null);
	}

	@Override
	public void notifyObservers(Object arg) {
		if(!changed)
			return;
		clearChanged();
		for (Observer o: observers) {
			o.update(this,arg);
		}
	}

	@Override
	public void deleteObservers() {

	}

	@Override
	public void setChanged() {

	}

	@Override
	public void clearChanged() {

	}

	@Override
	public boolean hasChanged() {
		return false;
	}

	@Override
	public int countObservers() {
		return 0;
	}
}