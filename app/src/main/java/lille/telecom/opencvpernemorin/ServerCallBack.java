package lille.telecom.opencvpernemorin;

import org.json.JSONObject;

/**
 * Created by perne on 24/01/16.
 */


public interface ServerCallBack {
    void onSuccess(JSONObject result);
    void onSuccess(String result);
    void onSuccess(byte[] result);
}
