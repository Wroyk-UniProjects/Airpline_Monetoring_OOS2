package messer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public class BasicAircraft {
	private String icao;
	private String operator;
	private Date posTime;
	private Coordinate coordinate;
	private Double speed;
	private Double trak;
	private Double altitude;

	
	public BasicAircraft (String icao, String operator, Date posTime, Coordinate coordinate, double speed, double trak, double altitude) {
		this.icao = icao;
		this.operator = operator;
		this.posTime = posTime;
		this.coordinate = coordinate;
		this.speed = speed;
		this.trak = trak;
		this.altitude = altitude;
	}

	public String getIcao() {
		return icao;
	}
	
	public String getOperator() {
		return operator;
	}

	public Date getPosTime() {
		return posTime;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public double getSpeed() {
		return speed;
	}

	public double getTrak() {
		return trak;
	}

	public double getAltitude() {
		return altitude;
	}
	
	public static List<String> getAttributesNames() {

		Field[] fields = BasicAircraft.class.getDeclaredFields();
		return Arrays.stream(fields).map(Field::getName).collect(Collectors.toList());
	}

	public static List<Object> getAttributesValues(BasicAircraft ac) throws NoSuchFieldException, IllegalAccessException {
		List<Object> attributes = new ArrayList<Object>();

		List<String> attributeNames = getAttributesNames();
		for (String attributeName: attributeNames) {
			Field field = BasicAircraft.class.getDeclaredField(attributeName);

			attributes.add(field.get(ac));
		}

		return attributes;
	}

	@Override
	public String toString() {
		return "BasicAircraft [icao=" + icao + ", operator=" + operator + ", posTime=" + posTime
				+ ", " + coordinate + ", speed=" + speed + ", trak =" + trak + ", baro_altitude =" + altitude + "]";
	}
}