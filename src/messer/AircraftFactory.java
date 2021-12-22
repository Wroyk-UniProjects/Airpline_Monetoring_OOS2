package messer;

import org.json.JSONArray;
import senser.AircraftSentence;

import java.util.Date;

import static java.lang.Double.NaN;

public class AircraftFactory {
    protected BasicAircraft newBasicAircraftFromAircraftSentence(AircraftSentence aircraftSentence){
        //System.out.println(aircraftSentence);
        JSONArray sentence = new JSONArray(aircraftSentence.getAircraftAsString());

        String icao = "null";
        if(!sentence.isNull(0))
            icao = sentence.getString(0);

        String operator = "null";
        if(!sentence.isNull(1))
            operator = sentence.getString(1);

        Date posTime = null;
        if(!sentence.isNull(3))
            posTime = new Date(sentence.getLong(3));


        Coordinate coordinate = new Coordinate(NaN, NaN);
        if(!sentence.isNull(5) && !sentence.isNull(6))
            coordinate = new Coordinate(sentence.getDouble(5), sentence.getDouble(6));

        double altitude = NaN;
        if(!sentence.isNull(7))
            altitude = sentence.getDouble(7);

        double speed = NaN;
        if(!sentence.isNull(9))
            speed = sentence.getDouble(9);

        double trak = NaN;
        if(!sentence.isNull(9))
            trak = sentence.getDouble(10);

        return new BasicAircraft(icao,operator,posTime,coordinate,speed,trak,altitude);
    }
}
