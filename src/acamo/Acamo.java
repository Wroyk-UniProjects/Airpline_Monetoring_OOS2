package acamo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import jsonstream.PlaneDataServer;
import messer.BasicAircraft;
import messer.Messer;
import observer.Observable;
import observer.Observer;
import senser.Senser;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Acamo extends Application implements Observer<BasicAircraft> {

    private static Messer MESSER;
    private ActiveAircrafts activeAircrafts;
    private boolean scheduled = false;
    private ObservableList<BasicAircraft> aircraftTableItems;
    private TitledPane detailPane;
    private BasicAircraft currenSelection;

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
        splitPane.setDividerPositions(0.65);


        /*
            Create the Aircraft Table
        */
        TableView<BasicAircraft> aircraftTable = new TableView<>();
        //aircraftTable.setBorder( new Border( new BorderStroke( Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(2), BorderWidths.DEFAULT)));

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

        this.detailPane = new TitledPane();
        //detailPane.setBorder( new Border( new BorderStroke( Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(4), BorderWidths.DEFAULT)));
        this.detailPane.setCollapsible(false);
        this.detailPane.setMinWidth(0);
        this.detailPane.setText("No Aircraft selected");
        this.detailPane.setPadding(new Insets(16,4,16,4));
        AnchorPane.setTopAnchor(this.detailPane,0.);
        AnchorPane.setRightAnchor(this.detailPane,0.);
        //AnchorPane.setBottomAnchor(this.detailPane,0.);
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

        currenSelection = aircraft;

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

        if(this.currenSelection !=null && Objects.equals(newValue.getIcao(), this.currenSelection.getIcao())){
            currenSelection = newValue;
        }

        if(!this.scheduled){
            this.scheduled = true;
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(this::updateTableItems, 1, TimeUnit.SECONDS);
        }
    }

    private void updateTableItems(){
        if(true){
            for (BasicAircraft aircraft : this.activeAircrafts.values()){
                if(System.currentTimeMillis()-aircraft.getLastCon().getTime() > 300000){//if older than 5 mints remove
                    System.out.println("Removed: "+aircraft.getIcao()+", Seconds: "+ (System.currentTimeMillis()-aircraft.getLastCon().getTime())/1000);
                    System.out.println(aircraft);
                    this.activeAircrafts.remove(aircraft.getIcao());

                    if(this.currenSelection !=null && Objects.equals(this.currenSelection.getIcao(), aircraft.getIcao())){
                        this.currenSelection = null;
                    }
                }
            }
        }

        Platform.runLater(()-> this.populateAircraftDetails(this.currenSelection));

        this.aircraftTableItems.clear();
        this.aircraftTableItems.addAll(this.activeAircrafts.values());
        System.out.println("ActiveAircraft: " + aircraftTableItems.size());
        this.scheduled = false;
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
