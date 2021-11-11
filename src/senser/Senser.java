package senser;

import messer.Messer;
import observer.Observable;
import observer.Observer;

import jsonstream.*;

import java.util.*;

public class Senser implements Runnable, Observable
{
	PlaneDataServer server;
	ArrayList<Observer> observers;
	Vector<AircraftSentence> aircraftSentences;
	boolean changed = false;
	boolean debug = true;


	public Senser(PlaneDataServer server)
	{
		this.server = server;
		this.observers = new ArrayList<Observer>();
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
			System.out.println("run");
			aircraftSentences = sentenceFactory.createAircraftSentenceVectorFromString(server.getPlaneListAsString());
			setChanged();
			if(debug) displayAircraftSentences(aircraftSentences, display);
			notifyObservers();

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
		observers.clear();
	}

	@Override
	public void setChanged() {
		changed = true;
	}

	@Override
	public void clearChanged() {
		changed = false;
	}

	@Override
	public boolean hasChanged() {
		return changed;
	}

	@Override
	public int countObservers() {
		return observers.size();
	}
}