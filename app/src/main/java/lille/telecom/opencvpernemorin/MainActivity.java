package lille.telecom.opencvpernemorin;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import org.json.JSONObject;

public class MainActivity extends Activity implements View.OnClickListener{

    static final String tag = MainActivity.class.getName();
    private Button connectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        connectBtn = (Button)findViewById(R.id.connectBtn);
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

    class GetURL extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            try {
                String indexJson = "";
                indexJson = URLReader.connect();
                Log.d("json", indexJson);

                JSONObject jsonObject = new JSONObject(indexJson);
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

    @Override
    public void onClick(View view) {
        if(view == connectBtn){
            GetURL getUrl = new GetURL();
            getUrl.execute();
        }
    }

}
