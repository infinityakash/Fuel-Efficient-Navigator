package com.fabianolibano.fen;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONHandler extends AsyncTask
{
    private StringBuilder response;
    private String urlString;

    @Override
    protected Object doInBackground(Object[] params)
    {
        if(params.length > 0)
        {
            urlString = (String)(params[0]);
        }

        try
        {
            response = new StringBuilder();
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) //ERRO
            {
                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()), 8192);
                String stringLine = null;
                while ((stringLine = input.readLine()) != null)
                {
                    response.append(stringLine);
                }
                input.close();
            }

        }
        catch (IOException e)
        {
            //Alert
        }

        return response.toString();
    }
}
