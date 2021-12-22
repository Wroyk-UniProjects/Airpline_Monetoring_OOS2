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
        SplitPane splitPane = new SplitPane();
        AnchorPane.setTopAnchor(splitPane,0.);
        AnchorPane.setLeftAnchor(splitPane,0.);
        AnchorPane.setBottomAnchor(splitPane,0.);
        AnchorPane.setRightAnchor(splitPane,0.);
        //splitPane.setPadding(new Insets(8));
        splitPane.setDividerPositions(0.6);


        TableView<BasicAircraft> aircraftTable = new TableView<>();
        //aircraftTable.setBorder( new Border( new BorderStroke( Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(2), BorderWidths.DEFAULT)));

        /*
            Create the detail UI
        */
        AnchorPane detailAnchor = new AnchorPane();

        TitledPane detailPane = new TitledPane();
        //detailPane.setBorder( new Border( new BorderStroke( Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(4), BorderWidths.DEFAULT)));
        detailPane.setCollapsible(false);
        detailPane.setText("No Aircraft selected");
        detailPane.setPadding(new Insets(16,4,16,4));
        AnchorPane.setTopAnchor(detailPane,0.);
        AnchorPane.setRightAnchor(detailPane,0.);
        AnchorPane.setBottomAnchor(detailPane,0.);
        AnchorPane.setLeftAnchor(detailPane,0.);

        VBox detailContent = new VBox();
        detailContent.setFillWidth(true);

        Label detailPlaceholderLabel = new Label("Please select an aircraft from the table");


        //Scene hierarchy setup
        Scene scene = new Scene(root, width, height);
        root.getChildren().add(splitPane);

        splitPane.getItems().add(aircraftTable);

        splitPane.getItems().add(detailAnchor);
        detailAnchor.getChildren().add(detailPane);
        detailPane.setContent(detailContent);
        detailContent.getChildren().add(detailPlaceholderLabel);


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
