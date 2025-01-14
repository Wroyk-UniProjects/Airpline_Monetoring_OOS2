import jsonstream.*;
import messer.Messer;
import senser.Senser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OOS2Lab1Starter
{
    private static double latitude = 48.7433;
    private static double longitude = 9.3201;
    private static boolean haveConnection = true;

	public static void main(String[] args)
	{


		if(!validateURL(args[0])){
			System.out.println("Invalide API URL");
			return;
		}
		String urlString = args[0];
		PlaneDataServer server;


		if(haveConnection)
			server = new PlaneDataServer(urlString, latitude, longitude, 100);
		else
			server = new PlaneDataServer(latitude, longitude, 150);


		Senser senser = new Senser(server);
		new Thread(server).start();
		new Thread(senser).start();// Why two times? Why is it not running with one?

		Messer messer = new Messer();
		senser.addObserver(messer);

		new Thread(messer).start();

	}

	private static boolean validateURL(String url){

		Pattern regexPattern = Pattern.compile("https:/{2}(([0-9a-zA-Z-.]*/)|[0-9a-zA-Z-.&?=_])*");

		Matcher matcher = regexPattern.matcher(url);

		return matcher.matches();
	}
}