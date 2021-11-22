package messer;

import org.json.JSONArray;
import org.json.JSONTokener;
import senser.AircraftSentence;

import java.util.Date;

public class AircraftFactory {
    protected BasicAircraft newBasicAircraftFromAircraftSentence(AircraftSentence aircraftSentence){
        System.out.println(aircraftSentence);

        JSONArray sentence = new JSONArray(aircraftSentence.getAircraftAsString());

        String icao = sentence.getString(0);
        String operator = sentence.getString(1);
        Date posTime = new Date(sentence.getLong(3));
        Coordinate coordinate = new Coordinate(sentence.getDouble(5), sentence.getDouble(6));
        double altitude = sentence.getDouble(7);
        double speed = sentence.getDouble(9);
        double trak = sentence.getDouble(10);

        return new BasicAircraft(icao,operator,posTime,coordinate,speed,trak,altitude);
    }
}
