package com.example.android.paysera;

import android.app.LoaderManager;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import java.text.DecimalFormat;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {

    private static final String PAYSERA_REQUEST_URL = "http://api.evp.lt/currency/commercial/exchange/";
    DecimalFormat precision;
    DecimalFormat precisionJPY;

    private double savingsUSD = 1000;
    private double savingsEUR = 0;
    private double savingsJPY = 0;

    private double commissionsUSD = 0;
    private double commissionsEUR = 0;
    private double commissionsJPY = 0;

    private String fromCurrency = "USD";
    private String toCurrency = "USD";
    private boolean enoughSavings;

    private double fromAmount = 0;
    private double commissionPay = 0;
    private int freeConversions = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        precision = new DecimalFormat("0.00");
        precisionJPY = new DecimalFormat("0");

        updateSavings();

        final Spinner fromSpinner = findViewById(R.id.from_currency_spinner);
        ArrayAdapter<CharSequence> fAdapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_spinner_item);
        fAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fAdapter);

        final Spinner toSpinner = findViewById(R.id.to_currency_spinner);
        ArrayAdapter<CharSequence> tAdapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_spinner_item);
        tAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(tAdapter);

        Button convertButton = findViewById(R.id.convert_button);
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromCurrency = fromSpinner.getSelectedItem().toString();
                toCurrency = toSpinner.getSelectedItem().toString();
                EditText amount = findViewById(R.id.amount);
                String a = amount.getText().toString();
                if (!a.equals("")) {
                    fromAmount = Double.parseDouble(a);
                    getLoaderManager().initLoader(1, null, MainActivity.this);
                }
            }
        });

    }

    public void updateSavings() {
        String set;
        TextView setView;

        setView = findViewById(R.id.USD);
        set = precision.format(savingsUSD) + " USD";
        setView.setText(set);

        setView = findViewById(R.id.EUR);
        set = precision.format(savingsEUR) + " EUR";
        setView.setText(set);

        setView = findViewById(R.id.JPY);
        set = precisionJPY.format(savingsJPY) + " JPY";
        setView.setText(set);

        setView = findViewById(R.id.comUSD);
        set = precision.format(commissionsUSD) + " USD";
        setView.setText(set);

        setView = findViewById(R.id.comEUR);
        set = precision.format(commissionsEUR) + " EUR";
        setView.setText(set);

        setView = findViewById(R.id.comJPY);
        set = precisionJPY.format(commissionsJPY) + " JPY";
        setView.setText(set);
    }

    private void convertCurrency(String data) {
        Toast toast = Toast.makeText(MainActivity.this,
                "Jūs konvertavote " + precision.format(fromAmount) + " " +
                        fromCurrency + " į " + data + " " + toCurrency +
                        ". Komisinis mokestis - " + precision.format(commissionPay) +
                        " " + fromCurrency +".", Toast.LENGTH_LONG);
        Double amount = Double.parseDouble(data);
        enoughSavings = false;
        double commissionRate = 0.007;
        commissionPay = 0;

        if (freeConversions <= 1)
        {
            commissionPay = fromAmount * commissionRate;
        }

        switch (fromCurrency) {
            case "USD":
                savingsUSD = calculate(savingsUSD);
                if(enoughSavings) {commissionsUSD += commissionPay; toast.show();}
                break;
            case "EUR":
                savingsEUR = calculate(savingsEUR);
                if(enoughSavings) {commissionsEUR += commissionPay; toast.show();}
                break;
            case "JPY":
                savingsJPY = calculate(savingsJPY);
                if(enoughSavings) {commissionsJPY += commissionPay; toast.show();}
                break;
        }


        switch (toCurrency) {
            case "USD":
                if (enoughSavings) { savingsUSD += amount; }
                break;
            case "EUR":
                if (enoughSavings) { savingsEUR += amount; }
                break;
            case "JPY":
                if (enoughSavings) { savingsJPY += amount; }
                break;
        }
        updateSavings();
    }

    private double calculate(double a){
        Toast toast = Toast.makeText(MainActivity.this, R.string.not_sufficient_funds, Toast.LENGTH_SHORT);
        if(a >= fromAmount + commissionPay) {double b = a - (fromAmount + commissionPay); freeConversions--; enoughSavings = true; return b;}
        else { toast.show(); return a;}
    }

    private URL createUrl(String a) {
        URL url;
        try {
            url = new URL(a);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        return url;
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        Uri baseUri = Uri.parse(PAYSERA_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendPath(fromAmount + "-" + fromCurrency);
        uriBuilder.appendPath(toCurrency);
        uriBuilder.appendPath("latest");
        URL url = createUrl(uriBuilder.toString());
        return new ConverLoader(this, url);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (!data.equals("0.00") && !fromCurrency.equals(toCurrency)) {
            convertCurrency(data);
        }
        getLoaderManager().destroyLoader(1);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
