package lille.telecom.opencvpernemorin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.*;

public class MainActivity extends Activity implements View.OnClickListener{

    final static int IMAGE_CAPTURE = 1;
    final static String tag = MainActivity.class.getName();
    private Button captureBtn;
    private Button photoLibraryBtn;
    private ImageView imageActivityMain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Capture d'un bouton captureBtn
        captureBtn = (Button)findViewById(R.id.captureBtn);
        captureBtn.setOnClickListener(this);
        imageActivityMain = (ImageView)findViewById(R.id.imageActivityMain);
        photoLibraryBtn = (Button)findViewById(R.id.photoLibraryBtn);
        photoLibraryBtn.setOnClickListener(this);
        //fin de capture d'un boutonBtn
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void onClick(View view){
        //log lorsque l'on clic sur captureBtn
        //Log.i(tag, "click on captureButton");
        //fin de log lorsque l'on clic sur captureBtn
        if (view == captureBtn){
            startCaptureActivity();
        }
        if (view == photoLibraryBtn){
            startPhotoLibraryActivity();
        }

    }

    protected void startCaptureActivity(){
        Intent captureItent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivity(captureItent);
        startActivityForResult(captureItent,IMAGE_CAPTURE);
    }

    protected void startPhotoLibraryActivity(){

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       // Log.i(tag, "bien dans onActivityResult");
        if (requestCode == IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extra = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extra.get("data");
            imageActivityMain.setImageBitmap(imageBitmap);
        }
    }
}
