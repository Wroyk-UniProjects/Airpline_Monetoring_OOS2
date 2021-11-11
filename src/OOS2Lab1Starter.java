import jsonstream.*;
import messer.BasicAircraft;
import messer.Coordinate;
import senser.Senser;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OOS2Lab1Starter
{
    private static double latitude = 48.7433;
    private static double longitude = 9.3201;
    private static boolean haveConnection = false;

	public static void main(String[] args)
	{


		if(!validateURL(args[0])){
			System.out.println("Invalide API URL");
			return;
		}
		String urlString = args[0];
		PlaneDataServer server;

		System.out.println(BasicAircraft.getAttributesNames());
		try{
			ArrayList<Object> arrayList = BasicAircraft.getAttributesValues(new BasicAircraft("1", "2", new Date(123333), new Coordinate(2.3,4.5),1.0, 45.6));
			for ( Object o: arrayList) {
				System.out.println(o);
			}
		}catch (Exception e){

		}

		
		if(haveConnection)
			server = new PlaneDataServer(urlString, latitude, longitude, 100);
		else
			server = new PlaneDataServer(latitude, longitude, 150);

		Senser senser = new Senser(server);
		//new Thread(server).start();
		//new Thread(senser).start();// Why two times? Why is it not running with one?
	}

	private static boolean validateURL(String url){

		Pattern regexPattern = Pattern.compile("https:/{2}(([0-9a-zA-Z-.]*/)|[0-9a-zA-Z-.&?=_])*");

		Matcher matcher = regexPattern.matcher(url);

		return matcher.matches();
	}
}