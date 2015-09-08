package lille.telecom.opencvpernemorin;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;

/**
 * Created by E34575 on 08/09/2015.
 */
public class RetainFragment extends Fragment {

    private Bitmap data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setData(Bitmap data) {
        this.data = data;
    }

    public Bitmap getData() {
        return data;
    }



}
