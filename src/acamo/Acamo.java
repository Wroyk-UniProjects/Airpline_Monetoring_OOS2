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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jsonstream.PlaneDataServer;
import messer.BasicAircraft;
import messer.Messer;
import observer.Observable;
import observer.Observer;
import senser.Senser;

import javafx.scene.input.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Acamo extends Application implements Observer<BasicAircraft> {

    private static String openskyAPIUrl;
    private LatLong baseStationLocation;
    private int searchRadius;
    private PlaneDataServer planeDataServer;

    private ActiveAircrafts activeAircrafts;
    private ConcurrentHashMap<String, Marker> markerHashMap;

    private boolean scheduled = false;

    private LeafletMapView mapView;
    private CompletableFuture<Worker.State> loadState;
    private ObservableList<BasicAircraft> aircraftTableItems;
    private TitledPane detailPane;
    private BasicAircraft currentSelection;
    private TableView.TableViewSelectionModel<BasicAircraft> aircraftTableViewSelectionModel;

    @Override
    public void start(Stage primaryStage) throws Exception {

        int width = 1280;
        int height = 720;
        int mapWidthDifference = -12; // Magic value to fit the hole aircraft table if window width is 1280
        double tableProtztenOfHeight = 0.5;

        this.launchSenserAndMesserServices();


        AnchorPane root = new AnchorPane();

        this.setupMapView(height, mapWidthDifference);


        /*
            Create container for Aircraft Table and Details
        */
        AnchorPane infoContainer = new AnchorPane();
        AnchorPane.setTopAnchor(infoContainer,0.);
        AnchorPane.setRightAnchor(infoContainer,0.);
        AnchorPane.setBottomAnchor(infoContainer,0.);
        infoContainer.setPrefWidth(width-height-mapWidthDifference);
        infoContainer.setPadding(new Insets(8, 8, 8, 0));

        TableView<BasicAircraft> aircraftTable = this.setupAircraftTable(height, tableProtztenOfHeight);
        AnchorPane detailAnchor = this.setupAircraftDetail();
        AnchorPane locationInputAnchor = this.setupLocationInput(height, tableProtztenOfHeight);

        //Scene hierarchy setup
        Scene scene = new Scene(root, width, height);
        root.getChildren().add(mapView);
        root.getChildren().add(infoContainer);

        infoContainer.getChildren().add(aircraftTable);
        infoContainer.getChildren().add(detailAnchor);
        infoContainer.getChildren().add(locationInputAnchor);


        primaryStage.setOnCloseRequest(this::onExit);
        primaryStage.setTitle("Acamo by Rudolf Baun");
        primaryStage.setScene(scene);

        primaryStage.show();

    }

    private void launchSenserAndMesserServices(){
        boolean hasConnection = true;

        this.baseStationLocation = new LatLong(48.689914715424244,9.20626058572938);//Airport Stuttgart
        this.searchRadius = 100;//in km

        if(hasConnection)
            this.planeDataServer = new PlaneDataServer(Acamo.openskyAPIUrl, this.baseStationLocation.getLatitude(), this.baseStationLocation.getLongitude(), this.searchRadius);
        else
            this.planeDataServer = new PlaneDataServer(this.baseStationLocation.getLatitude(), this.baseStationLocation.getLongitude(), this.searchRadius);

        Senser senser = new Senser(this.planeDataServer);
        new Thread(this.planeDataServer).start();
        new Thread(senser).start();

        Messer messer = new Messer();
        senser.addObserver(messer);
        new Thread(messer).start();

        this.activeAircrafts = new ActiveAircrafts();
        messer.addObserver(this.activeAircrafts);
        messer.addObserver(this);
    }


    private void setupMapView(int height, int mapWidthDifference){
        /*
            Configure map
         */
        this.mapView = new LeafletMapView();
        AnchorPane.setTopAnchor(mapView,0.);
        AnchorPane.setLeftAnchor(mapView,0.);
        AnchorPane.setBottomAnchor(mapView,0.);
        mapView.setPrefWidth(height+mapWidthDifference);
        mapView.setPadding(new Insets(8));
        //mapView.setBorder(new Border( new BorderStroke( Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(4), BorderWidths.DEFAULT)));

        List<MapLayer> mapLayerList = new LinkedList<>();
        mapLayerList.add(MapLayer.OPENSTREETMAP);

        MapConfig mapViewConfig = new MapConfig(mapLayerList, new ZoomControlConfig(), new ScaleControlConfig(), baseStationLocation);

        this.loadState = mapView.displayMap(mapViewConfig);

        this.loadState.whenComplete((state, throwable) -> {
            mapView.addCustomMarker("radar", "icons/outline_radar_black_24dp.png");// url != path

            for(int i = 0; i <= 24; i++){
                String iString = String.format("%02d",i);//padding with zeros

                mapView.addCustomMarker("plane" + iString, "icons/plane"+ iString +".png");
                mapView.addCustomMarker("activePlane" + iString, "icons/active/plane"+ iString +".png");
            }

            Marker marker = new Marker(baseStationLocation, "baseStation", "radar", -1);

            markerHashMap = new ConcurrentHashMap<>();
            markerHashMap.put("baseStation", marker);
            mapView.addMarker(marker);

            mapView.onMarkerClick(this::onMapMarkerClick);

            //mapView.setZoom(1);//from 0-100
        });
    }


    private TableView<BasicAircraft> setupAircraftTable(int height, double protztenOfHeight){
        /*
            Create the Aircraft Table
        */
        TableView<BasicAircraft> aircraftTable = new TableView<>();
        //aircraftTable.setBorder( new Border( new BorderStroke( Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(2), BorderWidths.DEFAULT)));
        AnchorPane.setLeftAnchor(aircraftTable,0.);
        AnchorPane.setRightAnchor(aircraftTable,0.);
        AnchorPane.setBottomAnchor(aircraftTable,0.);
        aircraftTable.setPrefHeight(height * protztenOfHeight);// set table height to % of window height 0.5

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

        this.aircraftTableViewSelectionModel = aircraftTable.getSelectionModel();
        return aircraftTable;
    }

    private AnchorPane setupAircraftDetail(){
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


        detailAnchor.getChildren().add(this.detailPane);
        this.detailPane.setContent(detailContent);
        detailContent.getChildren().add(detailPlaceholderLabel);

        return detailAnchor;
    }

    private AnchorPane setupLocationInput(int height, double protztenOfHeight){
        AnchorPane anchor = new AnchorPane();
        AnchorPane.setLeftAnchor(anchor,0.);
        anchor.setLayoutY(height * protztenOfHeight - 140);
        anchor.setPadding(new Insets(0, 16, 0, 16));

        TextField latitudeTextField = new TextField(Double.toString(baseStationLocation.getLatitude()));

        TextField longitudeTextField = new TextField(Double.toString(baseStationLocation.getLongitude()));

        Button submit = new Button("Set as Base Station");
        submit.setOnMouseClicked(this::onPanToNewLocationSubmitted);

        GridPane container = new GridPane();
        AnchorPane.setLeftAnchor(container, 0.);
        AnchorPane.setRightAnchor(container, 0.);
        container.setPadding(new Insets(2));
        container.setBorder(new Border( new BorderStroke( Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(4), BorderWidths.DEFAULT)));
        container.setHgap(4);
        container.setVgap(4);

        container.add(latitudeTextField,0, 0);
        container.add(longitudeTextField,1,0);
        container.add(submit,0,1);

        anchor.getChildren().add(container);

        return anchor;
    }



    private void moveBaseStationLocationTo(LatLong newLocation){
        this.baseStationLocation = newLocation;

        this.planeDataServer.resetLocation(newLocation.getLatitude(), newLocation.getLongitude(), this.searchRadius);
        this.activeAircrafts.clear();

        this.aircraftTableItems.clear();

    }
    private void resetMapAndMarker(){
        this.loadState.whenComplete((state, throwable) -> {

            for (Marker marker : this.markerHashMap.values()) {
                this.mapView.removeMarker(marker);
            }
            this.markerHashMap.clear();

            Marker marker = new Marker(baseStationLocation, "baseStation", "radar", -1);
            markerHashMap.put("baseStation", marker);
            this.mapView.addMarker(marker);

            this.mapView.panTo(this.baseStationLocation);
        });
    }

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

    private void onColumnSelect(MouseEvent event){
        // I trust that only MouseEvents from my aircraftTable will be past to this function.
        TableView<BasicAircraft> source = (TableView<BasicAircraft>) event.getSource();
        BasicAircraft aircraft = source.getSelectionModel().getSelectedItem();

        if(aircraft == null)
            return;

        populateAircraftDetails(aircraft);
        updateAircraftMapMarker();
    }

    private void onMapMarkerClick(String icao) {
        BasicAircraft aircraft = this.activeAircrafts.retrieve(icao);

        if(aircraft == null)
            return;

        populateAircraftDetails(aircraft);
        updateAircraftMapMarker();
    }

    private void onPanToNewLocationSubmitted(MouseEvent event){
        Button source = (Button) event.getSource();
        TextField latitudeTextField = (TextField) source.getParent().getChildrenUnmodifiable().get(0);
        TextField longitudeTextField = (TextField) source.getParent().getChildrenUnmodifiable().get(1);

        LatLong latLong = new LatLong(Double.parseDouble(latitudeTextField.getText()), Double.parseDouble(longitudeTextField.getText()));
        System.out.println(latLong);

        moveBaseStationLocationTo(latLong);
        resetMapAndMarker();
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

    private void updateAircraftMapMarker(){
        for (BasicAircraft aircraft: this.activeAircrafts.values()) {
            Marker marker = this.markerHashMap.get(aircraft.getIcao());

            LatLong latLongAircraft = new LatLong(aircraft.getCoordinate().getLatitude(), aircraft.getCoordinate().getLongitude());

            //Trak is in Degree and ranges from 0 to 360
            //The images are labelt from 0 to 24 and the image rotation corresponds to clock positions of the name
            //By multiplying Degree with the Value 0.06667 you get Hours with after rounding can be used to address the Marker
            int trakInHours = Math.toIntExact( Math.round(aircraft.getTrak() * 0.06667));
            String trakInHoursString = String.format("%02d",trakInHours);//padding with zeros

            String markerType;
            if (aircraft.equals(this.currentSelection)){
                markerType = "activePlane" + trakInHoursString;
                this.aircraftTableViewSelectionModel.select(aircraft);
            }else {
                markerType = "plane" + trakInHoursString;
            }

            if (marker == null){

                marker = new Marker(latLongAircraft, aircraft.getIcao(), markerType,0);
                marker.setClickable();

                this.markerHashMap.put(aircraft.getIcao(), marker);
                mapView.addMarker(marker);

            }else {
                marker.move(latLongAircraft);
                marker.changeIcon(markerType);
            }

        }
    }

    private void removeAircraftOverTimeout(long timeoutInMilliseconds){
        if(true){// Removing Airplanes that haven't been updated for a while.
            for (BasicAircraft aircraft : this.activeAircrafts.values()){
                if(System.currentTimeMillis()-aircraft.getLastCon().getTime() > timeoutInMilliseconds){//if older than 5(300000) minutes remove
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
    }

    private void updateAircraft(){

        removeAircraftOverTimeout(300000);//if older than 5(300000) minutes remove

        Platform.runLater(()-> this.populateAircraftDetails(this.currentSelection));


        this.aircraftTableItems.clear();
        this.aircraftTableItems.addAll(this.activeAircrafts.values());
        //System.out.println("ActiveAircraft: " + aircraftTableItems.size());

        Platform.runLater(this::updateAircraftMapMarker);

        this.scheduled = false;
    }



    public static void main(String[] args) {

        if(!validateURL(args[0])){
            throw new IllegalArgumentException(args[0] + " is an invalid API url");
        }
        openskyAPIUrl = args[0];

        launch();
    }

    private void onExit(Event event){
        Platform.exit();
        System.exit(0);
    }

    private static boolean validateURL(String url){

        Pattern regexPattern = Pattern.compile("https:/{2}(([0-9a-zA-Z-.]*/)|[0-9a-zA-Z-.&?=_])*");

        Matcher matcher = regexPattern.matcher(url);

        return matcher.matches();
    }
}
