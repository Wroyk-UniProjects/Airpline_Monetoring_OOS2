package messer;

import observer.Observable;
import observer.Observer;
import senser.AircraftSentence;

import java.util.List;

public class Messer implements Observer<List<AircraftSentence>> {


    public Messer(){}

    @Override
    public void update(Observable<List<AircraftSentence>> observable, List<AircraftSentence> newValue) {
        System.out.println(newValue.toString());
    }
}
