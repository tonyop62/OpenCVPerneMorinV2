package lille.telecom.opencvpernemorin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener{

    static final String tag = MainActivity.class.getName();
    private static final int IMAGE_CAPTURE = 1;
    private static final int IMAGE_PHOTOLIBRARY = 2;
    private Button captureBtn;
    private ImageView imageActivityMain;
    private Button libraryBtn;
    private Button photoMatchBtn;
    private Bitmap imageFound;
    private Uri uriFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        captureBtn = (Button)findViewById(R.id.captureBtn);
        captureBtn.setOnClickListener(this);
        
        libraryBtn = (Button)findViewById(R.id.photoLibraryBtn);
        libraryBtn.setOnClickListener(this);

        photoMatchBtn = (Button)findViewById(R.id.analysisBtn);
        photoMatchBtn.setOnClickListener(this);

        imageActivityMain = (ImageView)findViewById(R.id.imageActivityMain);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extra = data.getExtras();
            Bitmap imageBitmap = (Bitmap)extra.get("data");
            this.imageFound = imageBitmap;
            imageActivityMain.setImageBitmap(imageBitmap);
        }
        else if(requestCode == IMAGE_PHOTOLIBRARY && resultCode == RESULT_OK){
            Uri photoUri = data.getData();
            this.uriFound = photoUri;
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                imageActivityMain.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view == captureBtn){
            startCaptureActivity();
        }else if(view == libraryBtn){
            startPhotoLibraryActivity();
        }else if(view == photoMatchBtn){
            startPhotoMatchActivity();
        }
    }

    private void startPhotoMatchActivity() {
        Intent photoMatchIntent = new Intent(this, PhotoMatchActivity.class);
//        ByteArrayOutputStream bs = new ByteArrayOutputStream();
//        this.imageFound.compress(Bitmap.CompressFormat.JPEG, 100, bs); // todo : test photo non vide
//        photoMatchIntent.putExtra("byteArray", bs.toByteArray());
        photoMatchIntent.putExtra(this.uriFound.getPath(), "uriFound"); // todo : inverser les parametre (key, value), les key sont des nomdepackege.constante
        startActivity(photoMatchIntent);
    }

    protected void startCaptureActivity() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(captureIntent, IMAGE_CAPTURE);
    }

    private void startPhotoLibraryActivity() {
        Intent libraryIntent = new Intent();
        libraryIntent.setType("image/*");
        libraryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(libraryIntent, "select location picture"),IMAGE_PHOTOLIBRARY);
        // todo : si image grande, impossible upload voir photo.compress
        // todo : onRetainNonConfigurationInstance() perte de photo quand paysage/portrait voir (derniere section) : https://openclassrooms.com/courses/creez-des-applications-pour-android/preambule-quelques-concepts-avances
    }

// todo : changer la couleur du bouton quand on clic dessus (pour IHM) : https://openclassrooms.com/courses/creez-des-applications-pour-android/creation-de-vues-personnalisees
}
