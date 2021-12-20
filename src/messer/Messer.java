package messer;

import observer.Observable;
import observer.Observer;
import senser.AircraftSentence;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Messer implements Runnable, Observer<AircraftSentence>, Observable<BasicAircraft> {

    private Deque<AircraftSentence> aircraftSentenceQueue;
    private List<Observer<BasicAircraft>> observers = new ArrayList<>();
    private boolean changed = false;

    public Messer(){
        aircraftSentenceQueue = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void update(Observable<AircraftSentence> aircraftSentenceObservable, AircraftSentence aircraftSentence) {
            aircraftSentenceQueue.offerLast(aircraftSentence);
    }

    @Override
    public void run() {
        AircraftFactory factory = new AircraftFactory();
        AircraftDisplay display = new AircraftDisplay();

        while (true){
            if(aircraftSentenceQueue.peekFirst()!= null){
                BasicAircraft basicAircraft = factory.newBasicAircraftFromAircraftSentence(aircraftSentenceQueue.pollFirst());
                setChanged();
                notifyObservers(basicAircraft);
                //display.display(basicAircraft);
            }

        }
    }

    @Override
    public void addObserver(Observer<BasicAircraft> o) {
        observers.add(o);
    }

    @Override
    public void deleteObserver(Observer<BasicAircraft> o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        notifyObservers(null);
    }

    @Override
    public void notifyObservers(BasicAircraft arg) {
        if(!changed)
            return;
        clearChanged();
        for (Observer<BasicAircraft> o: observers) {
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
