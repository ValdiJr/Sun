package com.example.defensor.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dinho on 15/12/2015.
 */
public class ForecastFragment extends Fragment {

    private String longitude;
    private String latitude;
    private ArrayAdapter<String> previewArrayAdapter;
    public ForecastFragment() {
    }

    @Override
    //Infla o menu na Activity
    public void  onCreateOptionsMenu (Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);

    }
    @Override
    //método que inicia o ciclo da aplicação, setamos que o menu existe aqui, para garantir que ele estará no Fragment
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }
    //Implementa a função de escolha das opções
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh: {

                updateWeather();
                return true;
            }case R.id.action_settings_main: {

                Intent detailIntent = new Intent(getActivity(),SettingsActivity.class);
                startActivity(detailIntent);
                return true;
            }case R.id.action_map: {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sharedPostCode=sharedPref.getString(getString(R.string.key_postcode), "50000");
                Uri urigeolocation=Uri.parse("geo:0,0?");
                urigeolocation.buildUpon().appendQueryParameter("q",sharedPostCode);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(urigeolocation.buildUpon().appendQueryParameter("q",sharedPostCode).build());
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }





                Log.i("Longitude/Latitude", longitude + " / " + latitude);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
     public void updateWeather (){
         FeatchWeatherTask fwt = new FeatchWeatherTask();
         SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
         String sharedPostCode=sharedPref.getString(getString(R.string.key_postcode),"50000");
         fwt.execute(sharedPostCode);

     }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);




        previewArrayAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,
                R.id.list_item_forecast_textview);
        //Cria a View de Lista com a Lista contida no layout
        ListView listView = (ListView) rootView.findViewById(R.id.list_item_forecast);
        //Preenche a Lista com o ArrayAdapter formado antes
        listView.setAdapter(previewArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

               // Toast.makeText(getActivity(), parent.getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();
                Intent detailIntent = new Intent(getActivity(),DetailActivity.class);
                detailIntent.putExtra("detailPreview",parent.getItemAtPosition(position).toString());
                startActivity(detailIntent);
            }
        });


        return rootView;
    }







public class FeatchWeatherTask extends AsyncTask<String, Void, String[]> {

    @Override
    protected String[] doInBackground(String... params) {
        // These two need to be declared outside the try/catch
// so that they can be closed in the finally block.
        //Cria o objeto que recebe a url (URL) e faz a conexão
        HttpURLConnection urlConnection = null;
        //Cria o objeto que vai ler as linhas recebidas no GET
        BufferedReader reader = null;

// Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast cep=94043
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String temperature_unit=sharedPref.getString("key_temperature_units","metric");
            String urlString = "http://api.openweathermap.org/data/2.5/forecast/daily?q=" + params[0] + "&mode=json&units=" +
                    temperature_unit+"" +
                    "&cnt=7&APPID=2bfd6c44bcc4772a9b578fc3ce0400d8";
            URL url = new URL(urlString);
            Log.i("JSON_TAG", urlString);
            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            //especíífica o método da requisição
            urlConnection.setRequestMethod("GET");
            //abre a conexão
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                forecastJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                forecastJsonStr = null;
            }
            forecastJsonStr = buffer.toString();
            getGeoLocalization(forecastJsonStr);
            Log.i("Resultado Query JSON", forecastJsonStr);
            return getWeatherDataFromJson(forecastJsonStr, 7);

        } catch (IOException e) {
            //cria um Log e o classifica para ser visto e filtrado no logcat
            Log.e("ForecastFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            forecastJsonStr = null;
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            //se houverem conexões e leitores (readers) abertos encerra-os
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("ForecastFragment", "Error closing stream", e);
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
            //limpa o array
            previewArrayAdapter.clear();
            for (String previewByDay : result){
                //Para cada string no array insere na lista e a atualiza uilizando notyfyChange
                //Seguindo o padrão observer
                previewArrayAdapter.add(previewByDay);
            }
        }
    private void getGeoLocalization(String forecastString) throws JSONException {
        JSONObject forecastJson = new JSONObject(forecastString);
        JSONObject cityJson = forecastJson.getJSONObject("city");
        JSONObject coordJson= cityJson.getJSONObject("coord");
        longitude=coordJson.getString("lon");
        latitude=coordJson.getString("lat");

    }


    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];
        for (int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        for (String s : resultStrs) {
            //Log.v("FetcheAsycTask", "Forecast entry: " + s);
        }
        return resultStrs;

    }

}

}