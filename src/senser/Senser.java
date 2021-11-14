package senser;

import messer.Messer;
import observer.Observable;
import observer.Observer;

import jsonstream.*;

import java.util.*;

public class Senser implements Runnable, Observable<List<AircraftSentence>>
{
	PlaneDataServer server;
	private List<Observer<List<AircraftSentence>>> observers;
	private List<AircraftSentence> aircraftSentences;
	private boolean changed = false;
	private boolean debug = false;


	public Senser(PlaneDataServer server)
	{
		this.server = server;
		this.observers = new ArrayList<Observer<List<AircraftSentence>>>();
	}

	private void displayAircraftSentences(List<AircraftSentence> aircraftSentences, AircraftDisplay aircraftDisplay){
		System.out.println("\nThere are " + aircraftSentences.size() + " Aircraft in range.");

		for (AircraftSentence aircraftSentence : aircraftSentences) {
			aircraftDisplay.display(aircraftSentence);
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
			notifyObservers(aircraftSentences);

		}
	}

	@Override
	public void addObserver(Observer<List<AircraftSentence>> o) {
		observers.add(o);
	}

	@Override
	public void deleteObserver(Observer<List<AircraftSentence>> o) {
		observers.remove( 0);
	}

	@Override
	public void notifyObservers() {
		notifyObservers(null);
	}

	@Override
	public void notifyObservers(List<AircraftSentence> arg) {
		if(!changed)
			return;
		clearChanged();
		for (Observer<List<AircraftSentence>> o: observers) {
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