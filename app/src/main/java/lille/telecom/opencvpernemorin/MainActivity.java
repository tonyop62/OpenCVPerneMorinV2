package lille.telecom.opencvpernemorin;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener{

    static final String tag = MainActivity.class.getName();
    private Button connectBtn;
    private String vocabulary;

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
                String indexJson = URLReader.connect();

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
