package lille.telecom.opencvpernemorin;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener{

    static final String tag = MainActivity.class.getName();
    private static final int IMAGE_CAPTURE = 1;
    private static final int IMAGE_PHOTOLIBRARY = 2;
    private Button captureBtn;
    private ImageView imageActivityMain;
    private Button libraryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        captureBtn = (Button)findViewById(R.id.captureBtn);
        captureBtn.setOnClickListener(this);
        
        libraryBtn = (Button)findViewById(R.id.photoLibraryBtn);
        libraryBtn.setOnClickListener(this);

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
            imageActivityMain.setImageBitmap(imageBitmap);
        }
        else if(requestCode == IMAGE_PHOTOLIBRARY && resultCode == RESULT_OK){
            Uri photoUri = data.getData();
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
        }

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
    }
}
