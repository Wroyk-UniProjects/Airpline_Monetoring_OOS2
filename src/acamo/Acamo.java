package acamo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jsonstream.PlaneDataServer;
import messer.BasicAircraft;
import messer.Messer;
import observer.Observable;
import observer.Observer;
import senser.Senser;

public class Acamo extends Application implements Observer<BasicAircraft> {

    private static Messer messer;
    private ActiveAircrafts activeAircrafts;

    @Override
    public void start(Stage stage) throws Exception {
        this.activeAircrafts = new ActiveAircrafts();
        messer.addObserver(activeAircrafts);
        messer.addObserver(this);

        int width = 640;
        int height = 480;

        AnchorPane root = new AnchorPane();

        Scene scene = new Scene(root, width, height);

        stage.setTitle("Acamo by Rudolf Baun");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void update(Observable<BasicAircraft> observable, BasicAircraft newValue) {
        System.out.println(this.activeAircrafts.retrieve(newValue.getIcao()));
    }

    public static void main(String[] args) {
        String urlString = "https://opensky-network.org/api/states/all";
        PlaneDataServer server;

        double latitude = 48.7433425;
        double longitude = 9.3201122;
        boolean hasConnection = true;

        if(hasConnection)
            server = new PlaneDataServer(urlString, latitude, longitude, 150);
        else
            server = new PlaneDataServer(latitude, longitude, 100);

        Senser senser = new Senser(server);
        new Thread(server).start();
        new Thread(senser).start();

        Acamo.messer = new Messer();
        senser.addObserver(messer);
        new Thread(messer).start();

        launch();
    }

}
