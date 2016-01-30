package lille.telecom.opencvpernemorin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_highgui.imread;




public class MainActivity extends Activity implements View.OnClickListener {

    static final String tag = MainActivity.class.getName();
    static final String SERVER_TELECOM = "http://www-rech.telecom-lille.fr/";
    private Button connectBtn;
    private List<Brand> listBrands;
    private Map<String, CvSVM> mapClassifiers;
    private Mat MatVocabulary;
    private ImageView imageView;

    //create SIFT feature point extracter // default parameters ""opencv2/features2d/features2d.hpp""
    final SIFT detector = new SIFT(0, 3, 0.04, 10, 1.6);
    //create a matcher with FlannBased Euclidien distance (possible also with BruteForce-Hamming)
    final FlannBasedMatcher matcher = new FlannBasedMatcher();
    //create BoF (or BoW) descriptor extractor
    final BOWImgDescriptorExtractor bowide = new BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);
    /******* comparaison *******/
    Mat response_hist = new Mat();
    KeyPoint keypoints = new KeyPoint();
    Mat inputDescriptors = new Mat();
    Mat imageTest;
    // Finding best match
    float minf = Float.MAX_VALUE;
    String bestMatch = null;

    RequestQueue requestQueue;

    RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }
        return requestQueue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        connectBtn = (Button) findViewById(R.id.connectBtn);
//        connectBtn.setText(getStringFromNative());
        connectBtn.setOnClickListener(this);

        imageView = (ImageView) findViewById(R.id.imageActivityMain);

        Loader.load(opencv_core.class);

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

    @Override
    public void onClick(View view) {
        if (view == connectBtn) {

            launchSearch("nonfreesift/index.json");

        }
    }

    /*****
     * utilisation de Volley pour gérer la communication internet et récupérer les données utiles
     * Volley gère tout ce qui est tache dans l'objet requestQueue dans des thread différents
     ******/

    private void launchSearch(String adresse) {

        // todo : prendre en compte l'image photo et utiliser imageloader de volley pour afficher l'image trouvée sur internet http://developer.android.com/training/volley/request.html
        String pathToImage = null;
        try {
            pathToImage = getPathFile("Pepsi_13", "jpg").getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageTest = imread(pathToImage);

//        Bitmap bm = Bitmap.createBitmap(imageTest.cols(), imageTest.rows(), Bitmap.Config.ARGB_88880);


        InputStream ims = null;
        try {
            ims = getAssets().open("Pepsi_13.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // load image as Drawable
        Drawable d = Drawable.createFromStream(ims, null);
        // set image to ImageView
        imageView.setImageDrawable(d);


        String url = SERVER_TELECOM + adresse;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, jsonRequestListener, errorListener);

        getRequestQueue().add(request);
    }

    private Response.Listener<JSONObject> jsonRequestListener = new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject response) {
            try {
                final JSONArray jsonArray = response.getJSONArray("brands");
                String vocabulary = (String) response.get("vocabulary");

                // recupérer le vocabulary
                String mUrl = SERVER_TELECOM + "nonfreesift/" + vocabulary;
                InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, mUrl,
                        new Response.Listener<byte[]>() {

                            @Override
                            public void onResponse(byte[] response) {
                                try {
                                    if (response != null) {

                                        FileOutputStream outputStream;
                                        outputStream = openFileOutput("vocabulary", Context.MODE_MULTI_PROCESS);

                                        outputStream.write(response);
                                        outputStream.close();
                                        Log.d("telechargement terminé", getApplicationContext().getFilesDir().getPath());

                                        opencv_core.CvFileStorage storage = opencv_core.CvFileStorage.open(getApplicationContext().getFilesDir().getPath() + "/vocabulary", null, 0);
                                        Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList());
                                        opencv_core.CvMat cvMat = new opencv_core.CvMat(p);

                                        MatVocabulary = new opencv_core.Mat(cvMat);
                                        opencv_core.cvReleaseFileStorage(storage);

                                        // set the dictionnary with the matVocabulary created in 1s step
                                        bowide.setVocabulary(MatVocabulary);
                                        Log.d("matvocabulary", String.valueOf(MatVocabulary.rows()));

                                        // ajout des classifiers
                                        // liste des brands
                                        listBrands = new ArrayList<>();

                                        // récupération des brands depuis le json
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject row = jsonArray.getJSONObject(i);
                                            Brand brand = new Brand();
                                            brand.setBrandName(row.getString("brandname"));
                                            brand.setUrl(row.getString("url"));
                                            brand.setClassifier(row.getString("classifier"));

                                            // cas des images du brand
                                            JSONArray jsonArrayImage = row.getJSONArray("images");
                                            for (int j = 0; j < jsonArrayImage.length(); j++) {
                                                brand.getImages().add((String) jsonArrayImage.get(j));
                                            }

                                            // ajout du brand dans la liste des brands
                                            listBrands.add(brand);
                                        }

                                        mapClassifiers = new HashMap<>();
                                        // récupération des classifiers, nouvelle requete volley
                                        for (final Brand br : listBrands) {
                                            String urlBrand = SERVER_TELECOM + "nonfreesift/classifiers/" + br.getClassifier();

                                            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlBrand,

                                                    new Response.Listener<String>() {

                                                        @Override
                                                        public void onResponse(String response) {

//                                                            String xml = response;
//                                                            Log.d("volleyclassifier", xml); // marche pas dans android studio, indique seulement une ligne et mal. Faut lancer en ligne de commande adb logcat depuis le dossier platform-tools
                                                            Log.d("volleyclassifier", br.getBrandName());

                                                            Log.d("classifier", response.substring(0, 25));
                                                            try {
                                                                FileOutputStream outputStream;
                                                                outputStream = openFileOutput(br.getClassifier(), Context.MODE_PRIVATE);

                                                                outputStream.write(response.getBytes());
                                                                outputStream.close();

                                                                /*** marche bien le contenu du fichier est bien stocké ***/

//                                                                BufferedReader r = new BufferedReader(new InputStreamReader(openFileInput(br.getClassifier())));
//                                                                String line;
//                                                                while ((line = r.readLine()) != null) {
//                                                                    Log.d("classifierfichier", line);
//                                                                }

                                                                /****/

                                                                CvSVM cvSVM = new CvSVM();
//                                                                Log.d("cvSVM", String.valueOf(cvSVM.sizeof()));
                                                                Log.d("cheminconstruit", getApplicationContext().getFilesDir() + "/" + br.getClassifier());
                                                                cvSVM.load(getApplicationContext().getFilesDir() + "/" + br.getClassifier());

                                                                // test avant de lancer le predict
                                                                int in= cvSVM.get_support_vector_count();

                                                                Log.d("cvsvmtest", String.valueOf(in)); // donne 0 donc n'est pas bien loadé !!!!!!!!!!

                                                                Log.d("cvSVM", String.valueOf(cvSVM.sizeof())); // meme taille qu'a la création

                                                                detector.detectAndCompute(imageTest, Mat.EMPTY, keypoints, inputDescriptors);
                                                                Log.d("keypoint", String.format("Number of Key Points for Source: %s", keypoints.capacity()));
                                                                bowide.compute(imageTest, keypoints, response_hist);


                                                                float res = cvSVM.predict(response_hist, true);
                                                                if (res < minf) {
                                                                    minf = res;
                                                                    bestMatch = br.getBrandName();
                                                                }

                                                                long timePrediction = System.currentTimeMillis();

                                                                timePrediction = System.currentTimeMillis() - timePrediction;
                                                                Log.d("Rec", "detected " + bestMatch + " in " + timePrediction + " ms");


                                                            }catch (Exception e){
                                                                e.printStackTrace();
                                                            }


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

                                    }
                                } catch (Exception e) {
                                    Log.d("telechargement", "download failed");
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Log.d("telechargement", "download failed");
                            }

                        }, null);

                getRequestQueue().add(request);

            } catch (JSONException e) {
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


    @SuppressWarnings("unused")
    private File getPathFile(String part, String ext)
            throws IOException {
        File cacheFile = new File(getCacheDir(), part + "." + ext);
        try {
            InputStream inputStream = getAssets().open(part + "." + ext);
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            try {
                throw new IOException("Could not open vocab file", e);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        return cacheFile;

    }
}