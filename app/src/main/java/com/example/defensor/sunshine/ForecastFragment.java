package com.example.defensor.sunshine;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dinho on 15/12/2015.
 */
public class ForecastFragment extends Fragment {


    public ForecastFragment() {
    }

    @Override
    //Infla o menu na Activity
    public void  onCreateOptionsMenu (Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment,menu);

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
            case R.id.action_refresh:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);



        //Cria uma String com previsões para preencher a lista
        String[] previewArray = {"Today - Sunney - 88/63", "Today - Sunney - 88/63"
                , "Today - Sunney - 88/63","Today - Sunney - 88/63","Today - Sunney - 88/63"
                ,"Today - Sunney - 88/63","Today - Sunney - 88/63", "Today - Sunney - 88/63"
                ,"Today - Sunney - 88/63","Today - Sunney - 88/63"};
        //Cria uma lista a partir do Array de String
        List<String> previewList = new ArrayList<String>(Arrays.asList(previewArray));
        //Define o ArrayAdapter com um Contexto, Um layout com uma lista, Uma View a ser listada, e a Lista para preencher
        ArrayAdapter<String> previewArrayAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,previewList);
        //Cria a View de Lista com a Lista contida no layout
        ListView listView = (ListView) rootView.findViewById(R.id.list_item_forecast);
        //Preenche a Lista com o ArrayAdapter formado antes
        listView.setAdapter(previewArrayAdapter);



        return rootView;
    }


}




class FeatchWeatherTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
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
            // http://openweathermap.org/API#forecast
            URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

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

        } catch (IOException e) {
            //cria um Log e o classifica para ser visto e filtrado no logcat
            Log.e("ForecastFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            forecastJsonStr = null;
        } finally{
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


}