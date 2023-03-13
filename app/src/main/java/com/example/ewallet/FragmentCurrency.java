package com.example.ewallet;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class FragmentCurrency extends Fragment {

    private ListView lvCurrencies;
    private RequestQueue requestQueue;
    private OnSymbolSelectedListener mListener;
    private ProgressDialog progressDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_currency, container, false);
        lvCurrencies = view.findViewById(R.id.lvCurrencies);

        lvCurrencies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                String[] parts = item.split(" - ");
                String symbol = parts[0];
                mListener.onSymbolSelected(symbol);
            }
        });



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getCurrencies();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSymbolSelectedListener) {
            mListener = (OnSymbolSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " mora da implementira OnSymbolSelectedListener");
        }
    }


    public void getCurrencies(){

        showProgressDialog();
        requestQueue = Volley.newRequestQueue(getContext());
        String url = "https://api.apilayer.com/exchangerates_data/symbols";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject symbols = jsonObject.getJSONObject("symbols");
                            Iterator<String> keys = symbols.keys();
                            List<String> currencyList = new ArrayList<>();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                currencyList.add(key + " - " + symbols.getString(key));
                            }
                            // populate the list view with currencyList
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, currencyList);
                            lvCurrencies.setAdapter(adapter);
                        } catch (JSONException e) {
                            Log.e("ExampleFragment", "Error parsing response: " + e.getMessage());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressDialog();
                        Log.e("ExampleFragment", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", "Wrs760UpMcXZruHvhrgcFsoUBkCZZ382");
                return headers;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("U toku je preuzimanje valuta. Molimo Vas da sacekate");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public interface OnSymbolSelectedListener {
        void onSymbolSelected(String symbol);
    }
}
