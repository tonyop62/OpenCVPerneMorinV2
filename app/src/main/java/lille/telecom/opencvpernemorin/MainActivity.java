package lille.telecom.opencvpernemorin;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final int IMAGE_PHOTOLIBRARY = 2;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Button captureBtn;
    private ImageView imageActivityMain;
    private Button libraryBtn;
    private Button photoMatchBtn;
    private Bitmap imageBitmap; // enregistrement en attribut pour la rotation
    private Uri uriFound;
    private RetainFragment dataFragment;
    private String pathToImage;

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

        FragmentManager fm = getFragmentManager();
        dataFragment = (RetainFragment) fm.findFragmentByTag("data");

        // pour la rotation
        if(dataFragment == null){
            // création si pas de datafragment (lancement de l'activity)
            dataFragment = new RetainFragment();
            fm.beginTransaction().add(dataFragment, "data").commit();
            dataFragment.setData(this.imageBitmap);
        }else{
            // récupération du bitmap du dataFragment
            this.imageBitmap = this.dataFragment.getData();
            imageActivityMain.setImageBitmap(this.imageBitmap);
        }
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
        dataFragment.setData(this.imageBitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        this.imageBitmap = null;

        if(requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){

            // todo : nouvelle capture entraine le stop de l'appli

            // récupération de l'image
            pathToImage = uriFound.getPath();
            File f = new File(pathToImage);
            imageBitmap = decodeFile(f);

            /** redimensionnement de l'image pour que les traitements opencv passe sinon l'image est trop grande et le traitement fait exploser la mémoire **/
            // todo : ca serait mieux de le faire dans analysis avant les traitements mais je n'arrive pas à trouver le fonction bitmaptomap dans javacv car ça modifie définitivement la qualité de la photo prise
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 225, 225, false);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            File file = new File(pathToImage);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream fo = null;
            try {
                fo = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            /** fin redimensionnement **/

            // rotation car la photo est retournée lorsqu'elle arrive dans imageActivityMain
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);

            imageActivityMain.setImageBitmap(imageBitmap);

        }
        else if(requestCode == IMAGE_PHOTOLIBRARY && resultCode == RESULT_OK){

            Uri photoUri = data.getData();
            pathToImage = getRealPathFromURI(getApplicationContext(), photoUri);

            File f = new File(pathToImage);

            imageBitmap = decodeFile(f);

            /** redimensionnement de l'image pour que les traitements opencv passe sinon l'image est trop grande et le traitement fait exploser la mémoire **/
            // todo : ca serait mieux de le faire dans analysis avant les traitements mais je n'arrive pas à trouver le fonction bitmaptomap dans javacv car ça modifie définitivement la qualité de la photo prise
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 225, 225, false);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            File file = new File(pathToImage);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream fo = null;
            try {
                fo = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            /** fin redimensionnement **/

            // rotation car la photo est retournée lorsqu'elle arrive dans imageActivityMain
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);

            imageActivityMain.setImageBitmap(imageBitmap);
        }

        FragmentManager fm = getFragmentManager();
        dataFragment = (RetainFragment) fm.findFragmentByTag("data");

        if(dataFragment == null){
            dataFragment = new RetainFragment();
            fm.beginTransaction().add(dataFragment, "data").commit();
            dataFragment.setData(this.imageBitmap);
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

    protected void startCaptureActivity() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        this.uriFound = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // créé un fichier pour sauvegarder l'image
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, this.uriFound);
        startActivityForResult(captureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private void startPhotoLibraryActivity() {
        Intent libraryIntent = new Intent();
        libraryIntent.setType("image/*");
        libraryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(libraryIntent, "select location picture"), IMAGE_PHOTOLIBRARY);
    }

    private void startPhotoMatchActivity() {
        Intent photoMatchIntent = new Intent(this, AnalysisActivity.class);
        photoMatchIntent.putExtra("pathToImage", pathToImage);
        startActivity(photoMatchIntent);
    }

    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Crée un fichier pour sauvegarder une image ou une vidéo
     * @param type
     * @return
     */
    private static File getOutputMediaFile(int type){
        // todo : vérifier que la carte sd est bien montée avant

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");

        // Crée un dossier MyCameraApp s'il n'existe pas
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Créé un nom de fichier media
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    /**
     * Redimensionne une image de puis un fichier
     * @param f
     * @return Bitmap
     */
    private Bitmap decodeFile(File f) {
        try {
            // Decodage des dimensions de l'image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            try {
                BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // nouvelle dimension
            final int REQUIRED_SIZE=150;

            // Trouve la meilleure dimension possible
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Ajustement de l'image avec les nouvelles dimensions
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);

        } catch (FileNotFoundException e) {}
        return null;
    }

    /**
     * donne le chemin réél d'une uri pour accéder au photos d'un support externe
     * @param context
     * @param contentUri
     * @return
     */
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


}