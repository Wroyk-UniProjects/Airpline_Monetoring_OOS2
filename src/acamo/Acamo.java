package acamo;

import javafx.application.Application;
import javafx.stage.Stage;
import messer.BasicAircraft;
import observer.Observable;
import observer.Observer;

public class Acamo extends Application implements Observer<BasicAircraft> {

    @Override
    public void start(Stage stage) throws Exception {

    }

    @Override
    public void update(Observable<BasicAircraft> observable, BasicAircraft newValue) {

    }

}
