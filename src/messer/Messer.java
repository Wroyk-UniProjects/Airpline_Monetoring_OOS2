package messer;

import observer.Observable;
import observer.Observer;
import senser.AircraftSentence;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Messer implements Runnable, Observer<AircraftSentence> {

    private Deque<AircraftSentence> aircraftSentenceQueue;

    public Messer(){
        aircraftSentenceQueue = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void update(Observable<AircraftSentence> aircraftSentenceObservable, AircraftSentence aircraftSentence) {
            aircraftSentenceQueue.offerLast(aircraftSentence);
    }

    @Override
    public void run() {


        while (true){
            AircraftSentence aircraftSentence = aircraftSentenceQueue.pollFirst();
        }
    }
}
