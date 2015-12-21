package lille.telecom.opencvpernemorin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by E34575 on 25/11/2015.
 */
public class URLReader {

    public static String connect() throws Exception {
        String indexJson = "";

        URL verisign = new URL("http://www-rech.telecom-lille.fr/freeorb/index.json");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        verisign.openStream()));

        String inputLine;

        while ((inputLine = in.readLine()) != null)
            indexJson += inputLine;

        in.close();

        return indexJson;
    }
}
