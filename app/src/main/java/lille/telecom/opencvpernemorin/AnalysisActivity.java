package lille.telecom.opencvpernemorin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacpp.opencv_ml;
import org.bytedeco.javacpp.opencv_nonfree;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.bytedeco.javacpp.opencv_highgui.imread;

public class AnalysisActivity extends Activity {

    static final String tag = MainActivity.class.getName();

    // volley
    static final String SERVER_TELECOM = "http://www-rech.telecom-lille.fr/nonfreesift/";
    RequestQueue requestQueue;
    ImageLoader imageLoader; // pour gérer la mise en cache des objets téléchargés


    private List<Brand> listBrands;
    private opencv_core.Mat MatVocabulary;

    //create SIFT feature point extracter // default parameters ""opencv2/features2d/features2d.hpp""
    final opencv_nonfree.SIFT detector = new opencv_nonfree.SIFT(0, 3, 0.04, 10, 1.6);
    //create a matcher with FlannBased Euclidien distance (possible also with BruteForce-Hamming)
    final opencv_features2d.FlannBasedMatcher matcher = new opencv_features2d.FlannBasedMatcher();
    //create BoF (or BoW) descriptor extractor
    final opencv_features2d.BOWImgDescriptorExtractor bowide = new opencv_features2d.BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);

    /******* comparaison *******/
    opencv_core.Mat response_hist = new opencv_core.Mat();
    opencv_features2d.KeyPoint keypoints = new opencv_features2d.KeyPoint();
    opencv_core.Mat inputDescriptors = new opencv_core.Mat();
    opencv_core.Mat imageTest;
    // Finding best match
    float minf = Float.MAX_VALUE;
    String bestMatch = null;

    // to store the position of the brand in listBrands
    Integer posInList;

    // pour attendre la fin des traitements de volley
    private final CountDownLatch mCountDownLatch = new CountDownLatch(5);

    /** singleton requestQueue pour optimiser les requetes volley ****/
    RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }
        return requestQueue;
    }

    /** singleton imageLoader, pas besoin de plusieurs imageLoader pour gérer la mise en cache **/
    ImageLoader getImageLoader(){
        if(imageLoader == null){
            ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {

                LruCache<String, Bitmap> cache = new LruCache<>(50);

                @Override
                public Bitmap getBitmap(String url) {
                    return cache.get(url);
                }

                @Override
                public void putBitmap(String url, Bitmap bitmap) {
                    cache.put(url, bitmap);
                }
            };

            imageLoader = new ImageLoader(getRequestQueue(), imageCache);
        }

        return imageLoader;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        Loader.load(opencv_core.class);

        Bundle extra = getIntent().getExtras();
        Log.d("pathtoimage", (String) extra.get("pathToImage"));
        Log.d("pathtoexterne", Environment.getExternalStorageDirectory().getAbsolutePath());
        try {
            Log.d("pathtopepsi", getPathFile("Pepsi_13", "jpg").getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(extra != null) {
            // récupération de la photo
            String pathToImage = (String) extra.get("pathToImage");

            // lancement des traitements volley
            launchSearch("index.json", pathToImage);

            // attente des résultats de volley pour obtenir le bestmatch
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        mCountDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // probleme de droit pour ajouter des view au layout (uniquement le thread qui l'a créé le peut) -> imbrication et utilisation de runOnUiThread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (posInList != null) {
                                // ajout des networkimageview pour voir les résultats
                                Brand br = listBrands.get(posInList);
                                for (String imgUrl : br.getImages()) {
                                    Log.d("urlimage", SERVER_TELECOM + "train-images/" + imgUrl);
                                    // ajout d'une networkImageView
                                    NetworkImageView networkImageView = new NetworkImageView(getApplicationContext());
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(250, 250);
                                    layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                                    networkImageView.setLayoutParams(layoutParams);
                                    LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearlayout);
                                    networkImageView.setImageUrl(SERVER_TELECOM + "train-images/" + imgUrl, getImageLoader());
                                    linearLayout.addView(networkImageView);
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Aucune cohérence d'image trouvée", Toast.LENGTH_LONG).show();
                            }
                        }
                    });


                }
            }).start();
        }

    }

    private void launchSearch(String adresse, String pathToImage) {

        // test avec image enregistrée dans l'appli
        String pathToImageTest = null;
        try {
            pathToImageTest = getPathFile("Pepsi_13", "jpg").getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /** l'image de test à 225 rows alors que l'image capturée avec l'appareil photo en 2250 (surement trop qui cause surcharge memoire DeadObjectException) **/
        /** => redimenssionne donc dans mainactivity **/
        /** fonctionne si je choisis l'image coca_13 **/
        // chargement du Mat avec l'image à analyser
        imageTest = imread(pathToImage);
        Log.d("mattestcree", String.valueOf(imageTest.rows()));

        String url = SERVER_TELECOM + adresse;

        // récupération de l'index.json
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, jsonRequestListener, errorListener);

        getRequestQueue().add(request);
    }

    // 1ere requete volley : récupération de l'index.json
    private Response.Listener<JSONObject> jsonRequestListener = new Response.Listener<JSONObject>() {

        @Override
        public void onResponse(JSONObject response) {
            try {

                mCountDownLatch.countDown();// décompte pour CountDownLatch

                // récupération de l'adresse vers le vocabulary
                final JSONArray jsonArray = response.getJSONArray("brands");
                String vocabulary = (String) response.get("vocabulary");

                // 2nde requete volley : recupérer le contenu du vocabulary
                String mUrl = SERVER_TELECOM + vocabulary;
                InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, mUrl,
                        new Response.Listener<byte[]>() {

                            @Override
                            public void onResponse(byte[] response) {
                                try {
                                    if (response != null) {

                                        mCountDownLatch.countDown(); // décompte pour CountDownLatch

                                        // enregistrement dans un fichier (pas d'autre moyen pour le charger avec opencv)
                                        FileOutputStream outputStream;
                                        outputStream = openFileOutput("vocabulary", Context.MODE_MULTI_PROCESS);
                                        outputStream.write(response);
                                        outputStream.close();
                                        Log.d("telechargement terminé", getApplicationContext().getFilesDir().getPath());

                                        // chargement du vocabulary
                                        opencv_core.CvFileStorage storage = opencv_core.CvFileStorage.open(getApplicationContext().getFilesDir().getPath() + "/vocabulary", null, 0);
                                        Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList());
                                        opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
                                        MatVocabulary = new opencv_core.Mat(cvMat);
                                        opencv_core.cvReleaseFileStorage(storage);

                                        // set the dictionnary with the matVocabulary created
                                        bowide.setVocabulary(MatVocabulary);
                                        Log.d("matvocabulary", String.valueOf(MatVocabulary.rows()));

                                        // récupération des brands depuis le json
                                        listBrands = new ArrayList<>();
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

                                            listBrands.add(brand);
                                        }

                                        // 3e requete volley : récupération des classifiers
                                        for (final Brand br : listBrands) {
                                            String urlBrand = SERVER_TELECOM + "classifiers/" + br.getClassifier();

                                            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlBrand,

                                                    new Response.Listener<String>() {

                                                        @Override
                                                        public void onResponse(String response) {

                                                            try {

                                                                // enregistrement des classifiers dans un fichier (pas trouvé d'autre moyen pour le charger avec opencv)
                                                                FileOutputStream outputStream;
                                                                outputStream = openFileOutput(br.getClassifier(), Context.MODE_PRIVATE);
                                                                outputStream.write(response.getBytes());
                                                                outputStream.close();

                                                                // création d'un classifier avec le fichier créé
                                                                opencv_ml.CvSVM cvSVM = new opencv_ml.CvSVM();
                                                                cvSVM.load(getApplicationContext().getFilesDir() + "/" + br.getClassifier());

                                                                // test avant de lancer le predict
                                                                int in = cvSVM.get_support_vector_count();
                                                                Log.d("cvsvmtest", String.valueOf(in)); // bien loadé  car != 0 !!!!!!!!!!

                                                                // analyse avec la technique du bagOfWord
                                                                detector.detectAndCompute(imageTest, opencv_core.Mat.EMPTY, keypoints, inputDescriptors);
                                                                Log.d("keypoint", String.format("Number of Key Points for Source: %s", keypoints.capacity()));
                                                                bowide.compute(imageTest, keypoints, response_hist);

                                                                // enregistrement du meilleure résultat (minimum)
                                                                float res = cvSVM.predict(response_hist, true);
                                                                if (res < minf) {
                                                                    minf = res;
                                                                    bestMatch = br.getBrandName();
                                                                    posInList = listBrands.indexOf(br);
                                                                }

                                                                mCountDownLatch.countDown(); // décompte pour CountDownLatch

                                                            }catch (Exception e){
                                                                e.printStackTrace();
                                                            }

                                                            // todo : rendre le code plus propre au niveau de volley, voir doc google sur le singleton et produire des fonction (code redondant)

                                                            // todo : afficher de belles erreurs non bloquantes

                                                            // todo : intégrer une barre de progression pour les traitements opencv

                                                            // todo : régler le probleme de fatal signal 11 sysgev (memoire) mail envoyé (marche tout le temps sur samsung glaxy s4)

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
