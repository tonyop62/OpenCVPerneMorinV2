package lille.telecom.opencvpernemorin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;;
import org.opencv.calib3d.Calib3d;
import org.opencv.imgproc.Imgproc;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class PhotoMatchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_match);

        // réciupération de la photo envoyée par MainActivity
//        if(getIntent().hasExtra("byteArray")) {
//            Bitmap b = BitmapFactory.decodeByteArray(
//                    getIntent().getByteArrayExtra("byteArray"), 0, getIntent().getByteArrayExtra("byteArray").length);
//            ImageView imageRecup = (ImageView)findViewById(R.id.imageRecup);
//            imageRecup.setImageBitmap(b);
//        }

        if(getIntent().hasExtra("uriFound")){
            Bundle extra = getIntent().getExtras();
            Bitmap imageBitmap = (Bitmap)extra.get("uriFound");
            ImageView imageRecup = (ImageView)findViewById(R.id.imageRecup);
            imageRecup.setImageBitmap(imageBitmap);
        }

        // todo : tester recupération image depuis dossier drawable

        // test
//        Vector<KeyPoint> objectKeyPoint = new Vector<KeyPoint>();
//
//        Mat img_object = Highgui.imread("@drawable/frame_18", Highgui.IMREAD_GRAYSCALE);

//        List<FeatureDetector> detector = new ArrayList<FeatureDetector>(new SIFT());
//        detector.add(FeatureDetector.create(FeatureDetector.ORB));
//        detector.detect(img_object, objectKeyPoint);
//        Toast.makeText(getApplicationContext(), objectKeyPoint.size().toString(), Toast.LENGTH_LONG).show();

// todo : on accède à drawable par R.drawable.nom_du_fichier_sans_extension ?

        // todo : ajouter photo dans un coin pour rappeler la photo qu'on vient de capturer qu'on veut faire analyser
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_match, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
