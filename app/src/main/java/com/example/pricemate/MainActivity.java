package com.example.pricemate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    //Variables
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;

    private RecyclerView mList;
    private SearchView searchView;

    private LinearLayoutManager linearLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private List<Item> itemList;
    private RecyclerView.Adapter adapter;
    private String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();



        searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i("search view query", query);
        this.searchQuery = query;
        callApi(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.i("onQueryTextChange", newText);
        return false;
    }

    public void callApi(String searchTerm){
        mList = findViewById(R.id.main_list);

        itemList = new ArrayList<>();
        adapter = new CustomAdapter(getApplicationContext(), itemList);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(mList.getContext(), linearLayoutManager.getOrientation());

        mList.setHasFixedSize(true);
        mList.setLayoutManager(linearLayoutManager);
        mList.addItemDecoration(dividerItemDecoration);
        mList.setAdapter(adapter);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.ebay.com/buy/browse/v1/item_summary/search?q=";
        String limit = "3";

        // Request a string response from the provided URL.
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("key", "value");
        } catch (Exception e) {
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url+searchTerm+limit, parameters,new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                Log.i("onResponse", response.toString());

                try {
                    for (int i = 0; i < response.getJSONArray("itemSummaries").length(); i++) {

                        JSONObject jsonObject = response.getJSONArray("itemSummaries").getJSONObject(i);

                        Item item = new Item();
                        item.setTitle(jsonObject.getString("title"));
                        item.setCondition(jsonObject.getString("condition"));
                        item.setPrice(jsonObject.getJSONObject("price").getString("value"));
                        item.setImageUrl(jsonObject.getJSONObject("image").getString("imageUrl"));

                        itemList.add(item);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String oauthKey = "v^1.1#i^1#r^0#I^3#f^0#p^1#t^H4sIAAAAAAAAAOVYbWwURRjutteWCgUNXwb5cSygibB7s3t33btN7+TgkDvpx7XXEqhimdudbZfe7S47e5RDE5omNjGS0GAIGIyp+MMIEi0KGhP+qfGLaMQvEqOQECEEIUEIIAjObku5VtIivWATm0uafeedd573ed53ZnZBV1nF4z2xnsuVVHlxXxfoKqYobjKoKCtdNLWkeE5pEchzoPq6FnS5uktOV2OYSRtiI8KGrmHk3pRJa1h0jCE6a2qiDrGKRQ1mEBYtSUxGamtEngWiYeqWLulp2h2PhmhF8SoQAeBDKYnzS4hYtVsxm/QQLXOcAjkfQilQFRAEgYxjnEVxDVtQs0I0D3jAcDwDuCYeiF5e5DgWBHwttHsVMrGqa8SFBXTYgSs6c808rKNDhRgj0yJB6HA88mSyPhKPLq9rqvbkxQoP8pC0oJXFw5+W6TJyr4LpLBp9Gex4i8msJCGMaU94YIXhQcXILTD3AN+hmg/IVYLEK0DhoA/CYEGofFI3M9AaHYdtUWVGcVxFpFmqlRuLUcJGaj2SrMGnOhIiHnXb/xqyMK0qKjJD9PKlkTWRRIIO4+x6qC1th0zCVCVEVmESjVEmpQgBWQ5IiPHLfoC8Xu/gQgPRBmkesdIyXZNVmzTsrtOtpYigRiO54fO4IU71Wr0ZUSwbUb4ff4tDQWixRR1QMWu1a7auKEOIcDuPYyswNNuyTDWVtdBQhJEDDkUhGhqGKtMjB51aHCyfTThEt1uWIXo8nZ2dbKeX1c02Dw8A51ldW5OU2gmNNPG1e33AXx17AqM6qdhtTPxFK2cQLJtIrRIAWhsd9vGBoBAc5H04rPBI6z8MeTl7hndEoTpEQYKfVyQoCbwEUFAqRIeEB4vUY+NAKZhjMtDsQJaRhqQwJVJn2QwyVVn0+hXeG1AQI1cFFcYXVBQm5ZerGE5BCJD9LyUFA/+nRrnbUk8iyURWQWq9YHVep0At2bIyITU2b8zE+EhtTlgN21CH8FTC7zU8KwJNa6KyYjTWE2R32w13Tl7SDZTQ06qUKwADdq8XkAWvKSegaeWSKJ0mhnEliu1EJ5bI9nxMAkBDZe3GZiU949Eh2dFtU6uDeFw5RwwjnslkLZhKo3hhdvP/aCe/Y3oquetMqJyIfgNCqvLAJYV11GTxRok1EdazJrmfsfX2md2kdyCN7ICWqafTyFzFjVvo+62v3etj8PEvD4t7y71wN5WJVNtSWiUl1DrRMrsviqpwgp3GXBV5d/T7BO/4unSZo2lTbqKdQzEdW0geLTXXinu8VnuGv+SHi5w/rps6CLqp/mKKAh6wkJsP5pWVNLtKpszBqoVYFSosVts08u5qIrYD5QyomsVllNEMzyzM+6zQtxY8PPRhoaKEm5z3lQHMvT1Syk2bXckDzvl5eY5rAfNvj7q4Wa4Zka9qPlweK0eHqkuEsxfKjsN11BZQOeREUaVFrm6q6DF5Uvm1b/u/L/64/WL0txfgnoayw8+3Nmx/S7n+rvrTuSsvV556T/N9MFudtb99+pdXa6PMiR82vLpyw8LyoraOc9Mibyo74d5TM5/4eYbrzNUdrqhvbvNH+NDmRcmeyr3H+uRndp3YVqVyR06yx9e9/frWG9/Mt45ycuAN6ouWXb3fbctcqrnx181YRYx56Yjr6UvBBw7sWLyvP7T+8qGWH5/bufv6pBX6zJt0w4Kj73+2mz35x/kpB/dsfq33oqv32iO/Hzj+0FZpyb7PD/wqxh+cndk+N/Rp6bPJ2ndiVGvb9Gu9j8bPbLng+5o+v7Yn2juPrxWONVw53f+KORX/ueSTF6/3Ht63v/qXs4sH5Psbk6p+FvARAAA=";
                headers.put("Authorization", "Bearer " + oauthKey);
                return headers;
            }
        };
        queue.add(request);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //there is a login loop because it happens whenever it starts but never leaves, learn more
        //about Android life cycle to break this.
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            createSignInIntent();
        }

    }
    private void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers, Facebook and Twitter and special additional requirements
        // in gradle so don't forget to add
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.AnonymousBuilder().build());
        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.pricemate)
                .build();

        startActivityForResult(intent, RC_SIGN_IN);
    }

    public void signOut(View view){
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                        createSignInIntent();
                    }
                });
    }
    public void shoppingCart(View view){
        startActivity(new Intent(getApplicationContext(),ShoppingCart.class));
    }
}