package linky.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class UtilsJson {
    public static String getJsonString(JSONObject json, Enum key) throws JSONException {
        return getJsonValue(json, key).toString();
    }

    public static JSONArray getJsonArray(JSONObject json, Enum key) throws JSONException {
        Object object = getJsonValue(json, key);
        if (object instanceof JSONArray)
            return (JSONArray) object;
        JSONArray array = new JSONArray();
        array.put(0, object);
        return array;
    }

    public static JSONObject getJsonObject(JSONObject json, Enum key) throws JSONException {
        return (JSONObject) getJsonValue(json, key);
    }

    public static Object getJsonValue(JSONObject json, Enum key) throws JSONException {
        return json.get(key.name());
    }
}
