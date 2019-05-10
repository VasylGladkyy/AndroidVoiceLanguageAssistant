package com.example.androidvoicelanguageassistant;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

import static com.example.androidvoicelanguageassistant.GlobalVars.LANGUAGE_CODES;

public class QueryUtils {

    public static final String LOG_TAG = QueryUtils.class.getName();

    private QueryUtils() {
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Пробелеми при побудові Url ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            //  В мілісекундах
            urlConnection.setReadTimeout(10000 );
            urlConnection.setConnectTimeout(15000 );
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "" + urlConnection.getResponseCode());
                Log.e(LOG_TAG, url.toString());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Проблема отримання Json результата", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static String extractFromJsonTranslation(String stringJSON) {
        String translation = "";
        if (TextUtils.isEmpty(stringJSON)) {
            return null;
        }
        try {
            JSONObject baseJsonResponse = new JSONObject(stringJSON);
            JSONArray stringArray = baseJsonResponse.getJSONArray("text");
            translation = stringArray.getString(0);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Проблема розбору отриманого Json результату", e);
        }
        return translation;
    }

    private static ArrayList<String> extractFromJsonLanguages(String stringJSON) {
        ArrayList<String> languagesList = new ArrayList<>();
        if (TextUtils.isEmpty(stringJSON)) {
            return null;
        }
        try {
            JSONObject baseJsonResponse = new JSONObject(stringJSON);
            JSONObject baseJsonResponseLangs = baseJsonResponse.optJSONObject("langs");
            Iterator<String> iter = baseJsonResponseLangs.keys();
            //LANGU.clear();
            LANGUAGE_CODES.clear();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    Object value = baseJsonResponseLangs.get(key);
                    languagesList.add(value.toString());
                    LANGUAGE_CODES.add(key);
                } catch (JSONException e) {
                    Log.e("QueryUtils", "Проблема розбору отриманого Json результату", e);
                }
            }
        } catch (JSONException e) {
            Log.e("QueryUtils", "Проблема розбору отриманого Json результату", e);
        }
        return languagesList;
    }

    public static String fetchTranslation(String requestUrl) {
        URL url = createUrl(requestUrl);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Проблема з оформлення HTTP", e);
        }
        return extractFromJsonTranslation(jsonResponse);
    }

    //  PUBLIC METHOD TO FETCH LANGUAGES
    public static ArrayList<String> fetchLanguages(String requestUrl) {
        URL url = createUrl(requestUrl);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Проблема з оформлення HTTP", e);
        }
        return extractFromJsonLanguages(jsonResponse);
    }
}
