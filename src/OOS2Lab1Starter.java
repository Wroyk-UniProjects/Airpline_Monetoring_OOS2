import jsonstream.*;
import senser.Senser;

public class OOS2Lab1Starter
{
    private static double latitude = 48.7433;
    private static double longitude = 9.3201;
    private static boolean haveConnection = false;

	public static void main(String[] args)
	{
		//TODO a validation checked for args[0]
		String urlString = args[0];
		PlaneDataServer server;
		
		if(haveConnection)
			server = new PlaneDataServer(urlString, latitude, longitude, 100);
		else
			server = new PlaneDataServer(latitude, longitude, 150);

		Senser senser = new Senser(server);
		new Thread(server).start();
		new Thread(senser).start();// Why two times? Why is it not running with one?
	}
}