package acamo;

import messer.BasicAircraft;

import java.util.concurrent.ConcurrentHashMap;

public interface ActiveAircraftsInterface {

	public void store(String icao, BasicAircraft ac);

	public void clear();

	public BasicAircraft retrieve(String icao);

	public ConcurrentHashMap<String, BasicAircraft> values ();

}
