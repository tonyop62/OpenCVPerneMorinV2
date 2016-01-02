package lille.telecom.opencvpernemorin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by E34575 on 25/11/2015.
 */
public class URLReader {

    public static String connect(String url) throws Exception {
        String indexJson = "";
// todo : faire une test sur la connexion
        URL verisign = new URL(url);
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
