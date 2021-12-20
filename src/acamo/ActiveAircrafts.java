package acamo;

import messer.BasicAircraft;
import observer.Observable;
import observer.Observer;

import java.util.HashMap;

public class ActiveAircrafts implements ActiveAircraftsInterface, Observer<BasicAircraft> {

    HashMap<String, BasicAircraft> aircraftHashMap = new HashMap<>();

    @Override
    public void store(String icao, BasicAircraft aircraft) {
        aircraftHashMap.put(icao, aircraft);
    }

    @Override
    public void clear() {
        aircraftHashMap.clear();
    }

    @Override
    public BasicAircraft retrieve(String icao) {
        return aircraftHashMap.get(icao);
    }

    @Override
    public HashMap<String, BasicAircraft> values() {
        return aircraftHashMap;
    }

    @Override
    public void update(Observable<BasicAircraft> observable, BasicAircraft newValue) {
        store(newValue.getIcao(),newValue);
        //System.out.println(aircraftHashMap.size());
    }
}
