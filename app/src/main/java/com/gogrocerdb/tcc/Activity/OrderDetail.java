package com.gogrocerdb.tcc.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.franmontiel.localechanger.LocaleChanger;
import com.gogrocerdb.tcc.util.CustomBoldTextView;
import com.gogrocerdb.tcc.util.CustomTextView;
import com.gogrocerdb.tcc.util.CustomVolleyJsonRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gogrocerdb.tcc.Adapter.My_order_detail_adapter;
import com.gogrocerdb.tcc.AppController;
import com.gogrocerdb.tcc.Config.BaseURL;
import com.gogrocerdb.tcc.MainActivity;
import com.gogrocerdb.tcc.Model.My_order_detail_model;
import com.gogrocerdb.tcc.R;
import com.gogrocerdb.tcc.util.ConnectivityReceiver;
import com.gogrocerdb.tcc.util.CustomVolleyJsonArrayRequest;
import com.kaopiz.kprogresshud.KProgressHUD;

import net.gotev.uploadservice.MultipartUploadRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderDetail extends AppCompatActivity {
    public CustomTextView tv_orderno, tv_date, tv_time, tv_item, tv_tracking_date,Delivery_charges,tv_societyname,tv_recivername,tv_recivernmobile;
    public CustomTextView tv_status;
    public CustomBoldTextView tv_price;
    private String sale_id;
    RelativeLayout Mark_Delivered;
    private RecyclerView rv_detail_order;
    private List<My_order_detail_model> my_order_detail_modelList = new ArrayList<>();
    KProgressHUD hud;
    TextView delivery_text,start_navigation;
    public static final String MyPREFERENCES = "MyPrefs" ;
    private static String TAG = "OrderDetail";
    @Override
    protected void attachBaseContext(Context newBase) {
        newBase = LocaleChanger.configureBaseContext(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.orderdetail));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetail.this, MainActivity.class);
                startActivity(intent);
            }
        });


        rv_detail_order = (RecyclerView) findViewById(R.id.product_recycler);
        rv_detail_order.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv_detail_order.addItemDecoration(new DividerItemDecoration(getApplicationContext(), 0));
        tv_orderno = (CustomTextView) findViewById(R.id.tv_order_no);
        tv_status = (CustomTextView) findViewById(R.id.tv_order_status);

        tv_tracking_date = (CustomTextView) findViewById(R.id.tracking_date);
        tv_date = (CustomTextView) findViewById(R.id.tv_order_date);
        tv_time = (CustomTextView) findViewById(R.id.tv_order_time);
        tv_price = (CustomBoldTextView) findViewById(R.id.tv_order_price);
        tv_item = (CustomTextView) findViewById(R.id.tv_order_item);
        delivery_text = findViewById(R.id.delivery_text);
        Delivery_charges = findViewById(R.id.delivery_charges);
        start_navigation = findViewById(R.id.start_navigation);
        tv_societyname = findViewById(R.id.tv_societyname);
        tv_recivername = findViewById(R.id.tv_recivername);
        tv_recivernmobile = findViewById(R.id.tv_recivernmobile);


        sale_id = getIntent().getStringExtra("sale_id");
        if (ConnectivityReceiver.isConnected()) {

            makeGetOrderDetailRequest(sale_id);

        } else {
            Toast.makeText(getApplicationContext(), "Network Issue", Toast.LENGTH_SHORT).show();
        }
        String placed_on = getIntent().getStringExtra("placedon");
        String time = getIntent().getStringExtra("time");
        String item = getIntent().getStringExtra("item");
        String amount = getIntent().getStringExtra("ammount");
        String delivery_charges = getIntent().getStringExtra("delivery_charges");
        String society = getIntent().getStringExtra("socity_name");
        String receiver_name = getIntent().getStringExtra("receiver_name");
        String receiver_mobile = getIntent().getStringExtra("receiver_mobile");
        String house_no = getIntent().getStringExtra("house_no");

        final String latitude = getIntent().getStringExtra("latitude");
        final String longitude = getIntent().getStringExtra("longitude");
        final String stats = getIntent().getStringExtra("status");

        if(stats.equalsIgnoreCase("4")){
            start_navigation.setVisibility(View.GONE);
        }

        Mark_Delivered = (RelativeLayout) findViewById(R.id.btn_mark_delivered);

        start_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String geoUri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + " (" + "Store 2 Door" + ")";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                OrderDetail.this.startActivity(intent);
            }
        });


        Mark_Delivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // uploadMultipart(sale_id);

                if(stats.equalsIgnoreCase("1")){
                    startDelivery(sale_id);
                }else{
                    markDeliver(sale_id);
                }

             /*   Intent intent = new Intent(OrderDetail.this, GetSignature.class);
                intent.putExtra("sale", sale_id);
                startActivity(intent);*/

            }
        });

        if (stats.equals("0")) {
            tv_status.setText(getResources().getString(R.string.pending));
          //  relativetextstatus.setText(getResources().getString(R.string.pending));
        } else if (stats.equals("1")) {
            tv_status.setText(getResources().getString(R.string.confirm));
         //   relativetextstatus.setText(getResources().getString(R.string.confirm));

            delivery_text.setText("Start Delivery");
        } else if (stats.equals("2")) {
            tv_status.setText(getResources().getString(R.string.outfordeliverd));
            delivery_text.setText("Mark Delivered");
           // relativetextstatus.setText(getResources().getString(R.string.outfordeliverd));
        } else if (stats.equals("4")) {
            tv_status.setText(getResources().getString(R.string.delivered));
            Mark_Delivered.setVisibility(View.GONE);
         //   relativetextstatus.setText(getResources().getString(R.string.delivered));
        }

        tv_orderno.setText(sale_id);
        tv_date.setText(placed_on);
        tv_time.setText(time);
        tv_item.setText(item);
        Delivery_charges.setText(getResources().getString(R.string.currency) + " " +delivery_charges);
        tv_tracking_date.setText(placed_on);

        tv_price.setText(getResources().getString(R.string.currency) + " " + String.valueOf(Integer.parseInt(amount)+Integer.parseInt(delivery_charges)));
        tv_societyname.setText(society+" "+house_no);
        tv_recivernmobile.setText(receiver_mobile);
        tv_recivername.setText(receiver_name);

    }

    private void makeGetOrderDetailRequest(String sale_id) {

        showPogressdialog();
        String tag_json_obj = "json_order_detail_req";
        Map<String, String> params = new HashMap<String, String>();
        params.put("sale_id", sale_id);

        CustomVolleyJsonArrayRequest jsonObjReq = new CustomVolleyJsonArrayRequest(Request.Method.POST,
                BaseURL.OrderDetail, params, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                if (hud.isShowing()){
                    hud.dismiss();
                }
                Gson gson = new Gson();
                Type listType = new TypeToken<List<My_order_detail_model>>() {
                }.getType();

                my_order_detail_modelList = gson.fromJson(response.toString(), listType);

                My_order_detail_adapter adapter = new My_order_detail_adapter(my_order_detail_modelList);
                rv_detail_order.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                if (my_order_detail_modelList.isEmpty()) {
                    Toast.makeText(OrderDetail.this, getResources().getString(R.string.no_rcord_found), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (hud.isShowing()){
                    hud.dismiss();
                }
                // VolleyLog.d(TAG, "Error: " + error.getMessage());
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(OrderDetail.this, getResources().getString(R.string.connection_time_out), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

    }

    public void showPogressdialog(){
        hud = KProgressHUD.create(OrderDetail.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
    }

    public void uploadMultipart(String sale_id) {
       // String path = getPath(filePath);
        try {
            String uploadId = UUID.randomUUID().toString();
            new MultipartUploadRequest(this, BaseURL.urlUpload)
                    .addFileToUpload("", "signature") //Adding file
                    .addParameter("id", sale_id) //Adding text parameter to the request

                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload
            Intent intent = new Intent(OrderDetail.this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Upload Successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


 /*   private void markDeliver(final String sale_id) {
        showPogressdialog();
        String tag_json_obj = "json_socity_req";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest strRequest = new StringRequest(Request.Method.POST,  BaseURL.urlUpload,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (hud.isShowing()){
                            hud.dismiss();
                        }
                        Intent intent = new Intent(OrderDetail.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(OrderDetail.this, "Upload Successfully", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (hud.isShowing()){
                            hud.dismiss();
                        }
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", sale_id);
                return params;
            }
        };

        queue.add(strRequest);

    }*/






    private void markDeliver(final String sale_id) {

        showPogressdialog();
        String tag_json_obj = "json_product_req";
        Map<String, String> params = new HashMap<String, String>();


        params.put("id", sale_id);


        CustomVolleyJsonRequest jsonObjReq = new CustomVolleyJsonRequest(Request.Method.POST,
                BaseURL.urlUpload, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                if (hud.isShowing()){
                    hud.dismiss();
                }
                try {


                    if(response.getString("success").equalsIgnoreCase("0")){
                        Toast.makeText(OrderDetail.this, "Please Mark Deliver your first Order!", Toast.LENGTH_SHORT).show();
                    }else if(response.getString("success").equalsIgnoreCase("1")){
                        Intent intent = new Intent(OrderDetail.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(OrderDetail.this, "Order Delivered Successfully!", Toast.LENGTH_SHORT).show();
                    }




                } catch (JSONException e) {
                    if (hud.isShowing()){
                        hud.dismiss();
                    }
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (hud.isShowing()){
                    hud.dismiss();
                }
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(OrderDetail.this, getResources().getString(R.string.connection_time_out), Toast.LENGTH_SHORT).show();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }



    private void startDelivery(final String sale_id) {

        showPogressdialog();
        String tag_json_obj = "json_product_req";
        Map<String, String> params = new HashMap<String, String>();
        SharedPreferences userDetails = OrderDetail.this.getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        final String userId = userDetails.getString("user_id", "");
        params.put("order_id", sale_id);
        params.put("emp_id", userId);

        CustomVolleyJsonRequest jsonObjReq = new CustomVolleyJsonRequest(Request.Method.POST,
                BaseURL.startOrder, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                if (hud.isShowing()){
                    hud.dismiss();
                }
                try {

                    if(response.getString("status").equalsIgnoreCase("0")){
                        Toast.makeText(OrderDetail.this, "Please Mark Deliver your first Order!", Toast.LENGTH_SHORT).show();
                    }else if(response.getString("status").equalsIgnoreCase("1")){
                        Intent intent = new Intent(OrderDetail.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(OrderDetail.this, "Delivery Started", Toast.LENGTH_SHORT).show();
                    }




                } catch (JSONException e) {
                    if (hud.isShowing()){
                        hud.dismiss();
                    }
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (hud.isShowing()){
                    hud.dismiss();
                }
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(OrderDetail.this, getResources().getString(R.string.connection_time_out), Toast.LENGTH_SHORT).show();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }



}
