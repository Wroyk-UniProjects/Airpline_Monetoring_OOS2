package acamo;

import java.util.ArrayList;
import java.util.HashMap;

import messer.BasicAircraft;

public interface ActiveAircraftsInterface {

	public void store(String icao, BasicAircraft ac);

	public void clear();

	public BasicAircraft retrieve(String icao);

	public HashMap<String,BasicAircraft> values ();

}
