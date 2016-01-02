package lille.telecom.opencvpernemorin;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.ml.CvSVM;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends Activity implements View.OnClickListener{

    static final String tag = MainActivity.class.getName();
    static final String SERVER_TELECOM = "http://www-rech.telecom-lille.fr/";
    private Button connectBtn;
    private String vocabulary;
    private List<Brand> listBrands;

    RequestQueue requestQueue;

    RequestQueue getRequestQueue(){
        if(requestQueue==null){
            requestQueue = Volley.newRequestQueue(this);
        }
        return requestQueue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        connectBtn = (Button)findViewById(R.id.connectBtn);
        connectBtn.setText(getStringFromNative());
        connectBtn.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // recupération du bitmap lors de la destruction (pour la rotation)

    }

    static { // load the native library "myNativeTest"
        System.loadLibrary("myNativeTest");
    }

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d("opencv", "passe pas");
        }
    }

    class GetURL extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            try {
                String indexJson = URLReader.connect("http://www-rech.telecom-lille.fr/freeorb/index.json");

                JSONObject jsonObject = new JSONObject(indexJson);
                Log.d("jobj", jsonObject.toString(1));

                // récupération du fichier vocabulary
                vocabulary = jsonObject.getString("vocabulary");

                // liste des brands
                List<Brand> listBrands = new ArrayList<>();

                JSONArray jsonArray = (JSONArray) jsonObject.get("brands");

                // récupération des brands depuis le json
                for(int i = 0 ; i < jsonArray.length() ; i++){
                    JSONObject row = jsonArray.getJSONObject(i);
                    Brand brand = new Brand();
                    brand.setBrandName(row.getString("brandname"));
                    brand.setUrl(row.getString("url"));
                    brand.setClassifier(row.getString("classifier"));
                    listBrands.add(brand);
                }


                // récupération des classifiers pour chaque brand
                for(Brand br : listBrands){
                    String classifierXML = URLReader.connect("http://www-rech.telecom-lille.fr/freeorb/classifiers/" + br.getClassifier());
                    Log.d("classifier", classifierXML);


                    // creer un fichier pour load
                    File file = new File(Environment.DIRECTORY_DOCUMENTS, "testopencv");
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(classifierXML);

                    CvSVM classifier = new CvSVM();
                    classifier.load(file.getAbsolutePath());

                    // todo : ajouter le ndk car fonction natives non trouvées dans CascadeClassifier

//                    CascadeClassifier classifier = new CascadeClassifier();
//                    classifier.load(classifierXML);

                    Log.d("classifierObj", String.valueOf(classifier.get_var_count()));

                    // initialisation opencv

                    OpenCVLoader.initDebug();

                    //	OrbDescriptorExtractor detector;
                    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

                    //create SURF feature point extractor
                    FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);

                    //create SURF descriptor extractor
                    DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

                    //create BoF (or BoW) descriptor extractor

                }



            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

//    @Override
//    public void onClick(View view) {
//        if(view == connectBtn){
//            GetURL getUrl = new GetURL();
//            getUrl.execute();
//        }
//    }

    @Override
    public void onClick(View view) {
        if(view == connectBtn){
            launchSearch("freeorb/index.json");
        }
    }

    /***** utilisation de Volley pour gérer la communication internet et récupérer les données utiles
     Volley gère tout ce qui est tache dans l'objet requestQueue dans des thread différents ******/


    private void launchSearch(String adresse){

        String url = SERVER_TELECOM + adresse;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, jsonRequestListener, errorListener);

        getRequestQueue().add(request);
    }

    private Response.Listener<JSONObject> jsonRequestListener = new Response.Listener<JSONObject>(){

        @Override
        public void onResponse(JSONObject response) {
            try{
                JSONArray jsonArray = response.getJSONArray("brands");
                String vocabulary = (String) response.get("vocabulary");

                // liste des brands
                listBrands = new ArrayList<>();

                // récupération des brands depuis le json
                for(int i = 0 ; i < jsonArray.length() ; i++){
                    JSONObject row = jsonArray.getJSONObject(i);
                    Brand brand = new Brand();
                    brand.setBrandName(row.getString("brandname"));
                    brand.setUrl(row.getString("url"));
                    brand.setClassifier(row.getString("classifier"));

                    // cas des images du brand
                    JSONArray jsonArrayImage = row.getJSONArray("images");
                    for(int j = 0 ; j < jsonArrayImage.length() ; j++){
                        brand.getImages().add((String) jsonArrayImage.get(j));
                    }

                    // ajout du brand dans la liste des brands
                    listBrands.add(brand);
                }

                // récupération des classifiers, nouvelle requete volley
                for(Brand br : listBrands) {
                    String url = SERVER_TELECOM + "freeorb/classifiers/" + br.getClassifier();

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,

                            new Response.Listener<String>() {

                                @Override
                                public void onResponse(String response) {
                                    Log.d("volley", "hello");

                                    String xml = response;
                                    Log.d("volleyclassifier", xml); // marche pas dans android studio, indique seulement une ligne et mal. Faut lancer en ligne de commande adb logcat depuis le dossier platform-tools

                                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                    try {
                                        DocumentBuilder builder = factory.newDocumentBuilder();
                                        Document d1 = builder.parse(new InputSource(new StringReader(xml)));
                                        Log.d("volleyclassifier", String.valueOf(d1.getElementById("sv_total")));
                                    }catch (ParserConfigurationException e) {
                                        e.printStackTrace();
                                    } catch (SAXException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    /** todo : créer le code en c grace au fichier du prof pour construire le classifier et tout le tralala
                                     * 1. passer la variable xml au code C, voir aussi pour la photo prise avec le telephone (quesqu'on envoie ? on récupére le fichier sauvegardé en c ?)
                                     * 2. traiter le xml
                                     * 3. renvoyer le nom ou la photo directement
                                     */


                                    // todo : uplodaer une ou plusieurs images avec volley (plusieurs à voir c'est pour l'affichage du résultat)

                                    // todo : intégrer le layout la prise de photo et la récupération depuis la mémoire du téléphone de la photo (voir projet 1)

                                    // todo : rendre le code plus propre au niveau de volley, voir doc google sur le singleton

                                }
                            },

                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("error classifier", error.getLocalizedMessage());
                                }
                            }

                    );
                    getRequestQueue().add(stringRequest);

                }

                // les brands sont bien stockés dans la listeBrands
            }catch (JSONException e){
                Log.e("errorJSON", e.getLocalizedMessage());
            }
        }
    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("error index", error.getLocalizedMessage());
        }
    };

    public native String getStringFromNative();

}
