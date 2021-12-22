package acamo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import jsonstream.PlaneDataServer;
import messer.BasicAircraft;
import messer.Coordinate;
import messer.Messer;
import observer.Observable;
import observer.Observer;
import senser.Senser;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Acamo extends Application implements Observer<BasicAircraft> {

    private static Messer MESSER;
    private ActiveAircrafts activeAircrafts;
    private boolean queued = false;
    private ObservableList<BasicAircraft> aircraftTableItems;
    private TitledPane detailPane;

    @Override
    public void start(Stage stage) throws Exception {
        this.activeAircrafts = new ActiveAircrafts();
        MESSER.addObserver(activeAircrafts);
        MESSER.addObserver(this);

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


        /*
            Create the Aircraft Table
        */
        TableView<BasicAircraft> aircraftTable = new TableView<>();
        //aircraftTable.setBorder( new Border( new BorderStroke( Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(2), BorderWidths.DEFAULT)));

        // Defining the columns
        TableColumn<BasicAircraft, String> colIcao = new TableColumn<>("ICAO");
        TableColumn<BasicAircraft, String> colOperator = new TableColumn<>("Operator");
        TableColumn<BasicAircraft, Date> colPosTime = new TableColumn<>("posTime");
        TableColumn<BasicAircraft, Coordinate> colCoordinate = new TableColumn<>("Coordinate");
        TableColumn<BasicAircraft, Double> colSpeed = new TableColumn<>("Speed");
        TableColumn<BasicAircraft, Double> colTrak = new TableColumn<>("Trak");
        TableColumn<BasicAircraft, Double> colAltitude = new TableColumn<>("Altitude");

        // Defining Factorys
        colIcao.setCellValueFactory( new PropertyValueFactory<>("icao"));
        colOperator.setCellValueFactory( new PropertyValueFactory<>("operator"));
        colPosTime.setCellValueFactory( new PropertyValueFactory<>("posTime"));
        colOperator.setCellValueFactory( new PropertyValueFactory<>("coordinate"));
        colSpeed.setCellValueFactory( new PropertyValueFactory<>("speed"));
        colTrak.setCellValueFactory( new PropertyValueFactory<>("trak"));
        colAltitude.setCellValueFactory( new PropertyValueFactory<>("altitude"));


        aircraftTable.getColumns().addAll(colIcao, colOperator, colPosTime, colCoordinate, colSpeed, colTrak, colAltitude);

        this.aircraftTableItems = FXCollections.observableArrayList();
        this.aircraftTableItems.addAll(this.activeAircrafts.values());
        aircraftTable.setItems(this.aircraftTableItems);

        /*
            Create the detail UI
        */
        AnchorPane detailAnchor = new AnchorPane();

        this.detailPane = new TitledPane();
        //detailPane.setBorder( new Border( new BorderStroke( Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(4), BorderWidths.DEFAULT)));
        this.detailPane.setCollapsible(false);
        this.detailPane.setMinWidth(0);
        this.detailPane.setText("No Aircraft selected");
        this.detailPane.setPadding(new Insets(16,4,16,4));
        AnchorPane.setTopAnchor(this.detailPane,0.);
        AnchorPane.setRightAnchor(this.detailPane,0.);
        AnchorPane.setBottomAnchor(this.detailPane,0.);
        AnchorPane.setLeftAnchor(this.detailPane,0.);

        VBox detailContent = new VBox();
        detailContent.setFillWidth(true);

        Label detailPlaceholderLabel = new Label("Please select an aircraft from the table");


        //Scene hierarchy setup
        Scene scene = new Scene(root, width, height);
        root.getChildren().add(splitPane);

        splitPane.getItems().add(aircraftTable);

        splitPane.getItems().add(detailAnchor);
        detailAnchor.getChildren().add(this.detailPane);
        this.detailPane.setContent(detailContent);
        detailContent.getChildren().add(detailPlaceholderLabel);


        stage.setTitle("Acamo by Rudolf Baun");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void update(Observable<BasicAircraft> observable, BasicAircraft newValue) {
        System.out.println(this.activeAircrafts.retrieve(newValue.getIcao()));
        if(!this.queued){
            this.queued = true;

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(this::updateTableItems, 1, TimeUnit.SECONDS);
        }
    }

    private void updateTableItems(){
        for (BasicAircraft aircraft : this.activeAircrafts.values()){

            if(System.currentTimeMillis()-aircraft.getPosTime().getTime() >= 15){
                activeAircrafts.remove(aircraft.getIcao());
                System.out.println(aircraft.getIcao());
            }
        }
        this.aircraftTableItems.clear();
        this.aircraftTableItems.addAll(this.activeAircrafts.values());
        this.queued = false;
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

        Acamo.MESSER = new Messer();
        senser.addObserver(MESSER);
        new Thread(MESSER).start();

        launch();
    }

}
