package acamo;

import de.saring.leafletmap.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import jsonstream.PlaneDataServer;
import messer.BasicAircraft;
import messer.Messer;
import observer.Observable;
import observer.Observer;
import senser.Senser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class Acamo extends Application implements Observer<BasicAircraft> {

    private static Messer MESSER;

    private static LatLong position = new LatLong(48.7433425,9.3201122);
    private ActiveAircrafts activeAircrafts;
    private ConcurrentHashMap<String, Marker> markerHashMap = new ConcurrentHashMap<>();

    private boolean scheduled = false;

    private LeafletMapView mapView;
    private CompletableFuture<Worker.State> loadState;
    private ObservableList<BasicAircraft> aircraftTableItems;
    private TitledPane detailPane;
    private BasicAircraft currentSelection;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.activeAircrafts = new ActiveAircrafts();
        MESSER.addObserver(activeAircrafts);
        MESSER.addObserver(this);

        int width = 1280;
        int height = 720;

        AnchorPane root = new AnchorPane();

        /*
        SplitPane splitPane = new SplitPane();
        AnchorPane.setTopAnchor(splitPane,0.);
        AnchorPane.setLeftAnchor(splitPane,0.);
        AnchorPane.setBottomAnchor(splitPane,0.);
        AnchorPane.setRightAnchor(splitPane,0.);
        splitPane.setPadding(new Insets(8));
        splitPane.setDividerPositions(0.65);
        */

        /*
            Configure map
         */
        int mapWidthDiferens = -10; // Magic value to fit the hole aircraft table if window width is 1280
        this.mapView = new LeafletMapView();
        AnchorPane.setTopAnchor(mapView,0.);
        AnchorPane.setLeftAnchor(mapView,0.);
        AnchorPane.setBottomAnchor(mapView,0.);
        mapView.setPrefWidth(height+mapWidthDiferens);
        mapView.setPadding(new Insets(8));
        //mapView.setBorder(new Border( new BorderStroke( Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(4), BorderWidths.DEFAULT)));

        List<MapLayer> mapLayerList = new LinkedList<>();
        mapLayerList.add(MapLayer.OPENSTREETMAP);

        MapConfig mapViewConfig = new MapConfig(mapLayerList, new ZoomControlConfig(), new ScaleControlConfig(), position);


        /*
            Create container for Aircraft Table and Details
        */
        AnchorPane infoContainer = new AnchorPane();
        AnchorPane.setTopAnchor(infoContainer,0.);
        AnchorPane.setRightAnchor(infoContainer,0.);
        AnchorPane.setBottomAnchor(infoContainer,0.);
        infoContainer.setPrefWidth(width-height-mapWidthDiferens);
        infoContainer.setPadding(new Insets(8, 8, 8, 0));


        /*
            Create the Aircraft Table
        */
        TableView<BasicAircraft> aircraftTable = new TableView<>();
        //aircraftTable.setBorder( new Border( new BorderStroke( Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(2), BorderWidths.DEFAULT)));
        AnchorPane.setLeftAnchor(aircraftTable,0.);
        AnchorPane.setRightAnchor(aircraftTable,0.);
        AnchorPane.setBottomAnchor(aircraftTable,0.);
        aircraftTable.setPrefHeight(height * 0.54);// set table height to % of window height

        for (String attribute:BasicAircraft.getAttributesNames()) {
            if(!Objects.equals(attribute, "lastCon")){
                TableColumn<BasicAircraft, String> column = new TableColumn<>(attribute.substring(0,1).toUpperCase() + attribute.substring(1));
                column.setCellValueFactory(new PropertyValueFactory<>(attribute));
                aircraftTable.getColumns().add(column);
            }
        }

        this.aircraftTableItems = FXCollections.observableArrayList();
        this.aircraftTableItems.addAll(this.activeAircrafts.values());
        aircraftTable.setItems(this.aircraftTableItems);

        aircraftTable.setOnMousePressed(this::onColumnSelect);


        /*
            Create the detail UI
        */
        AnchorPane detailAnchor = new AnchorPane();
        AnchorPane.setTopAnchor(detailAnchor,0.);
        AnchorPane.setLeftAnchor(detailAnchor,0.);

        this.detailPane = new TitledPane();
        //detailPane.setBorder( new Border( new BorderStroke( Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(4), BorderWidths.DEFAULT)));
        this.detailPane.setCollapsible(false);
        this.detailPane.setMinWidth(0);
        this.detailPane.setText("No Aircraft selected");
        this.detailPane.setPadding(new Insets(8,0,16,16));
        AnchorPane.setTopAnchor(this.detailPane,0.);
        AnchorPane.setRightAnchor(this.detailPane,0.);
        //AnchorPane.setBottomAnchor(this.detailPane,0.);
        AnchorPane.setLeftAnchor(this.detailPane,0.);

        VBox detailContent = new VBox();
        detailContent.setFillWidth(true);

        Label detailPlaceholderLabel = new Label("Please select an aircraft from the table");


        //Scene hierarchy setup
        Scene scene = new Scene(root, width, height);
        root.getChildren().add(mapView);
        root.getChildren().add(infoContainer);

        infoContainer.getChildren().add(aircraftTable);

        infoContainer.getChildren().add(detailAnchor);
        detailAnchor.getChildren().add(this.detailPane);
        this.detailPane.setContent(detailContent);
        detailContent.getChildren().add(detailPlaceholderLabel);

        primaryStage.setOnCloseRequest(this::onExit);
        primaryStage.setTitle("Acamo by Rudolf Baun");
        primaryStage.setScene(scene);

        this.loadState = mapView.displayMap(mapViewConfig);

        this.loadState.whenComplete((state, throwable) -> {
            mapView.addCustomMarker("radar", "icons/outline_radar_black_24dp.png");// url != path
            mapView.addCustomMarker("planeTest", "icons/plane00.png");

            Marker marker = new Marker(position, "center", "radar", -1);
            markerHashMap.put("center", marker);
            mapView.addMarker(marker);
        });

        primaryStage.show();

    }

    //private void launchSenserAndMesserServices(){

    //}

    private void populateAircraftDetails(BasicAircraft aircraft){
        if(aircraft == null){
            this.detailPane.setText("No Aircraft selected");
            VBox pContent = new VBox();

            Label pLabel = new Label("Please select an aircraft from the table");

            pContent.getChildren().add(pLabel);
            this.detailPane.setContent(pContent);
            return;
        }

        this.detailPane.setText("Aircraft: "+ aircraft.getIcao());
        GridPane detailContent = new GridPane();

        currentSelection = aircraft;

        try {
            List<String> attributeName = BasicAircraft.getAttributesNames();
            List<Object> attributeValues = BasicAircraft.getAttributesValues(aircraft);

            for (int i=0; i < attributeName.size(); i++){
                if (!Objects.equals(attributeName.get(i), "lastCon")){

                    Label name = new Label(attributeName.get(i).substring(0,1).toUpperCase() + attributeName.get(i).substring(1) + ": ");
                    name.setPadding(new Insets(0,16,0,0));
                    name.setMinWidth(100.);

                    Label value = new Label(attributeValues.get(i).toString());
                    value.setPadding(new Insets(0,0,0,16));

                    detailContent.add(name,0, i);
                    detailContent.add(value, 1,i);
                }
            }
        }catch (Exception e){
            System.out.println(e);//TODO better handling
        }

        this.detailPane.setContent(detailContent);
    }

    private void onColumnSelect(Event event){
        // I trust that only MouseEvents from my aircraftTable will be past to this function.
        TableView<BasicAircraft> source = (TableView<BasicAircraft>) event.getSource();
        BasicAircraft aircraft = source.getSelectionModel().getSelectedItem();

        if(aircraft == null)
            return;

        populateAircraftDetails(aircraft);
    }



    @Override
    public void update(Observable<BasicAircraft> observable, BasicAircraft newValue) {
        //System.out.println(this.activeAircrafts.retrieve(newValue.getIcao()));

        //Update Aircraft Details for the selected Aircraft
        if(this.currentSelection !=null && Objects.equals(newValue.getIcao(), this.currentSelection.getIcao())){
            this.currentSelection = newValue;
        }

        if(!this.scheduled){
            this.scheduled = true;
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(this::updateAircraft, 1, TimeUnit.SECONDS);
        }
    }

    private void updateMarker(){
        for (BasicAircraft aircraft: this.activeAircrafts.values()) {
            Marker marker = this.markerHashMap.get(aircraft.getIcao());

            LatLong latLongAircraft = new LatLong(aircraft.getCoordinate().getLatitude(), aircraft.getCoordinate().getLongitude());

            if (marker == null){
                marker = new Marker(latLongAircraft, aircraft.getIcao(),"planeTest",0);
                this.markerHashMap.put(aircraft.getIcao(), marker);
                mapView.addMarker(marker);

            }else {
                marker.move(latLongAircraft);
            }

        }
    }

    private void updateAircraft(){

        if(true){// Removing Airplanes that haven't been updated for a while.
            for (BasicAircraft aircraft : this.activeAircrafts.values()){
                if(System.currentTimeMillis()-aircraft.getLastCon().getTime() > 10000){//if older than 5 mints remove
                    System.out.println("Removed: "+aircraft.getIcao()+", Seconds: "+ (System.currentTimeMillis()-aircraft.getLastCon().getTime())/1000);
                    System.out.println(aircraft);
                    this.activeAircrafts.remove(aircraft.getIcao());

                    Platform.runLater(() -> {
                        this.loadState.whenComplete((state, throwable) -> {
                            if (markerHashMap.containsKey(aircraft.getIcao())){
                                mapView.removeMarker(this.markerHashMap.get(aircraft.getIcao()));
                                this.markerHashMap.remove(aircraft.getIcao());
                            }
                        });
                    });

                    if(this.currentSelection !=null && Objects.equals(this.currentSelection.getIcao(), aircraft.getIcao())){
                        this.currentSelection = null;
                    }
                }
            }
        }

        Platform.runLater(()-> this.populateAircraftDetails(this.currentSelection));



        this.aircraftTableItems.clear();
        this.aircraftTableItems.addAll(this.activeAircrafts.values());
        System.out.println("ActiveAircraft: " + aircraftTableItems.size());

        Platform.runLater(this::updateMarker);

        this.scheduled = false;
    }



    public static void main(String[] args) {
        String urlString = "https://opensky-network.org/api/states/all";
        PlaneDataServer server;

        boolean hasConnection = true;

        if(hasConnection)
            server = new PlaneDataServer(urlString, position.getLatitude(), position.getLongitude(), 150);
        else
            server = new PlaneDataServer(position.getLatitude(), position.getLongitude(), 100);

        Senser senser = new Senser(server);
        new Thread(server).start();
        new Thread(senser).start();

        Acamo.MESSER = new Messer();
        senser.addObserver(MESSER);
        new Thread(MESSER).start();

        launch();
    }

    private void onExit(Event event){
        Platform.exit();
        System.exit(0);
    }

}
