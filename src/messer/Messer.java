package messer;

import observer.Observable;
import observer.Observer;

public class Messer implements Observer {


    public Messer(){}

    @Override
    public void update(Observable observable, Object newValue) {
        System.out.println("test");
    }
}
