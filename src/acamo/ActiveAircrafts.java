package acamo;

import messer.BasicAircraft;
import observer.Observable;
import observer.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveAircrafts implements ActiveAircraftsInterface, Observer<BasicAircraft> {

    ConcurrentHashMap<String, BasicAircraft> aircraftHashMap = new ConcurrentHashMap<>();

    @Override
    public void store(String icao, BasicAircraft aircraft) {
        aircraftHashMap.put(icao, aircraft);
    }

    @Override
    public void clear() {
        aircraftHashMap.clear();
    }

    public void remove(String icao){
        aircraftHashMap.remove(icao);
    }

    @Override
    public BasicAircraft retrieve(String icao) {
        return aircraftHashMap.get(icao);
    }

    @Override
    public List<BasicAircraft> values() {
        return new ArrayList<>(aircraftHashMap.values());
    }

    @Override
    public void update(Observable<BasicAircraft> observable, BasicAircraft newValue) {
        store(newValue.getIcao(),newValue);
        //System.out.println(aircraftHashMap.size());
    }
}
