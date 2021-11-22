package senser;

import messer.Messer;
import observer.Observable;
import observer.Observer;

import jsonstream.*;

import java.util.*;

public class Senser implements Runnable, Observable<AircraftSentence>
{
	PlaneDataServer server;
	private List<Observer<AircraftSentence>> observers;
	private boolean changed = false;
	private boolean debug = false;


	public Senser(PlaneDataServer server)
	{
		this.server = server;
		this.observers = new ArrayList<Observer<AircraftSentence>>();
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
			List<AircraftSentence> aircraftSentences = sentenceFactory.createAircraftSentenceVectorFromString(server.getPlaneListAsString());
			if(debug) displayAircraftSentences(aircraftSentences, display);
			for(AircraftSentence aircraftSentence: aircraftSentences){
				setChanged();
				notifyObservers(aircraftSentence);
			}

		}
	}

	@Override
	public void addObserver(Observer<AircraftSentence> o) {
		observers.add(o);
	}

	@Override
	public void deleteObserver(Observer<AircraftSentence> o) {
		observers.remove( 0);
	}

	@Override
	public void notifyObservers() {
		notifyObservers(null);
	}

	@Override
	public void notifyObservers(AircraftSentence arg) {
		if(!changed)
			return;
		clearChanged();
		for (Observer<AircraftSentence> o: observers) {
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