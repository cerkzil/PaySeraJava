package com.example.android.paysera;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class ConverLoader extends AsyncTaskLoader<String> {

    private static final int HTTP_CONNECTION_OKAY = 200;
    private static final int HTTP_READ_TIMEOUT = 10000;
    private static final int HTTP_CONNECT_TIMEOUT = 15000;
    private URL mUrl;

    ConverLoader(Context context, URL url) {
        super(context);
        mUrl = url;
    }

    private static String extractRates(URL url) throws IOException {

        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        String jSonResponse = null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(HTTP_READ_TIMEOUT);
            httpURLConnection.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HTTP_CONNECTION_OKAY) {
                inputStream = httpURLConnection.getInputStream();
                jSonResponse = readStream(inputStream);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return parseJSON(jSonResponse);
    }

    private static String readStream(InputStream inputStream) throws IOException {
        StringBuilder jSonResponse = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                jSonResponse.append(line);
                line = reader.readLine();
            }
        }
        return jSonResponse.toString();
    }

    private static String parseJSON(String jSonResponse) {

        String results = null;
        try {
            JSONObject root = new JSONObject(jSonResponse);
            results = root.optString("amount");
        }
        catch (JSONException e) {
            Log.e("ConversionLoader", "Problem parsing JSON results", e);
        }

        return results;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public String loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        try {
            return extractRates(mUrl);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
