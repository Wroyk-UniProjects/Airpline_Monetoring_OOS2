package acamo;

import messer.BasicAircraft;

import java.util.HashMap;

public class ActiveAircrafts implements ActiveAircraftsInterface {

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
}
