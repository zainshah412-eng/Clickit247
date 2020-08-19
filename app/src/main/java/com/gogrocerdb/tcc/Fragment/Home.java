package com.gogrocerdb.tcc.Fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.gogrocerdb.tcc.Adapter.DeliveredOrdersAdapter;
import com.gogrocerdb.tcc.Adapter.My_Order_Adapter;
import com.gogrocerdb.tcc.AppController;
import com.gogrocerdb.tcc.Config.BaseURL;
import com.gogrocerdb.tcc.Model.My_order_model;
import com.gogrocerdb.tcc.NetworkConnectivity.ConnectionHelper;
import com.gogrocerdb.tcc.R;
import com.gogrocerdb.tcc.util.ConnectivityReceiver;
import com.gogrocerdb.tcc.util.CustomBoldTextView;
import com.gogrocerdb.tcc.util.CustomVolleyJsonArrayRequest;
import com.gogrocerdb.tcc.util.GPSTracker;
import com.gogrocerdb.tcc.util.Session_management;
import com.google.android.gms.common.api.GoogleApiClient;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends Fragment
{
    private static String TAG = Home.class.getSimpleName();
    private CustomBoldTextView tv_today_rides;
    private CustomBoldTextView no_pending_order;
    private CustomBoldTextView no_deliverd_order;
    private List<My_order_model> my_order_modelList;
    private List<My_order_model> my_order_modelList1;
    private Session_management sessionManagement;
    String get_id;
    KProgressHUD hud;
    private static final int PERMISSIONS_REQUEST_Location = 888;
    public static final String MyPREFERENCES = "MyPrefs" ;
    GoogleApiClient mGoogleApiClient;

    private String Longitude;
    private String Latitude;
    ConnectionHelper helper;
    Boolean isInternet;
    GPSTracker gps;
    android.app.AlertDialog alert;
    public static final int REQUEST_LOCATION = 1450;

    public Home() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,
                container, false);
        sessionManagement = new Session_management(getActivity());
        get_id = sessionManagement.getUserDetails().get(BaseURL.KEY_NAME);
        tv_today_rides = (CustomBoldTextView) view.findViewById(R.id.tv_today_rides);
        no_pending_order = (CustomBoldTextView) view.findViewById(R.id.no_pending_order);
        no_deliverd_order = (CustomBoldTextView) view.findViewById(R.id.no_deliverd_order);
        gps = new GPSTracker(getActivity());
        helper = new ConnectionHelper(getActivity());
        isInternet = helper.isConnectingToInternet();

        if (sessionManagement.isLoggedIn()) {
            String getname = sessionManagement.getUserDetails().get(BaseURL.KEY_NAME);
            tv_today_rides.setText(getname);
        }


        if (ConnectivityReceiver.isConnected()) {
            makeGetOrderRequest();
        } else {
            Toast.makeText(getActivity(), "Network Issue! Please try again", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void makeGetOrderRequest() {
        showPogressdialog();
        String tag_json_obj = "json_socity_req";
        Map<String, String> params = new HashMap<String, String>();
        params.put("d_id", get_id);

        CustomVolleyJsonArrayRequest jsonObjReq = new CustomVolleyJsonArrayRequest(Request.Method.POST,
                BaseURL.GET_ORDER_URL, params, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {



                try {
                    my_order_modelList = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {

                        JSONObject obj = response.getJSONObject(i);
                        String saleid = obj.getString("sale_id");
                        String placedon = obj.getString("on_date");
                        String timefrom = obj.getString("delivery_time_from");
                        String timeto=obj.getString("delivery_time_from");
                        String item = obj.getString("total_items");
                        String ammount = obj.getString("total_amount");
                        String status = obj.getString("status");

                        String society = obj.getString("socity_name");
                        String house = obj.getString("house_no");
                        String rename = obj.getString("receiver_name");
                        String renumber = obj.getString("receiver_mobile");
                        String deliverCharges = obj.getString("delivery_charge");

                        String longitude = obj.getString("longitude");
                        String latitude = obj.getString("latitude");
                        My_order_model my_order_model = new My_order_model();
                        my_order_model.setSocityname(society);
                        my_order_model.setHouse(house);
                        my_order_model.setRecivername(rename);
                        my_order_model.setRecivermobile(renumber);
                        my_order_model.setDelivery_time_from(timefrom);
                        my_order_model.setSale_id(saleid);
                        my_order_model.setOn_date(placedon);
                        my_order_model.setDelivery_time_to(timeto);
                        my_order_model.setTotal_amount(ammount);
                        my_order_model.setStatus(status);
                        my_order_model.setTotal_items(item);
                        my_order_model.setDelivery_charge(deliverCharges);

                        my_order_model.setLatitude(latitude);
                        my_order_model.setLongitude(longitude);

                        if(status.equalsIgnoreCase("1") || status.equalsIgnoreCase("2")){
                            my_order_modelList.add(my_order_model);
                        }

                        no_pending_order.setText(String.valueOf(my_order_modelList.size()));
                        makeGetDeliveredOrderRequest();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }







            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(hud.isShowing()){
                    hud.dismiss();
                }
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.connection_time_out), Toast.LENGTH_SHORT).show();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

    }

    public void showPogressdialog(){
        hud = KProgressHUD.create(getActivity())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void makeGetDeliveredOrderRequest() {

        String tag_json_obj = "json_socity_req";
        Map<String, String> params = new HashMap<String, String>();
        params.put("d_id", get_id);

        CustomVolleyJsonArrayRequest jsonObjReq = new CustomVolleyJsonArrayRequest(Request.Method.POST,
                BaseURL.GET_ORDER_URL, params, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                if(hud.isShowing())
                {
                    hud.dismiss();
                }
                try {
                    my_order_modelList1=new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {

                        JSONObject obj = response.getJSONObject(i);
                        String saleid = obj.getString("sale_id");
                        String placedon = obj.getString("on_date");
                        String timefrom = obj.getString("delivery_time_from");
                        String timeto=obj.getString("delivery_time_from");
                        String item = obj.getString("total_items");
                        String ammount = obj.getString("total_amount");
                        String status = obj.getString("status");

                        String society = obj.getString("socity_name");
                        String house = obj.getString("house_no");
                        String rename = obj.getString("receiver_name");
                        String renumber = obj.getString("receiver_mobile");
                        String deliverCharges = obj.getString("delivery_charge");

                        String longitude = obj.getString("longitude");
                        String latitude = obj.getString("latitude");
                        My_order_model my_order_model = new My_order_model();
                        my_order_model.setSocityname(society);
                        my_order_model.setHouse(house);
                        my_order_model.setRecivername(rename);
                        my_order_model.setRecivermobile(renumber);
                        my_order_model.setDelivery_time_from(timefrom);
                        my_order_model.setSale_id(saleid);
                        my_order_model.setOn_date(placedon);
                        my_order_model.setDelivery_time_to(timeto);
                        my_order_model.setTotal_amount(ammount);
                        my_order_model.setStatus(status);
                        my_order_model.setTotal_items(item);
                        my_order_model.setDelivery_charge(deliverCharges);

                        my_order_model.setLatitude(latitude);
                        my_order_model.setLongitude(longitude);

                        if(status.equalsIgnoreCase("4")){
                            my_order_modelList1.add(my_order_model);
                        }
                        hud.dismiss();
                        no_deliverd_order.setText(String.valueOf(my_order_modelList1.size()));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(hud.isShowing()){
                    hud.dismiss();
                }
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.connection_time_out), Toast.LENGTH_SHORT).show();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

    }


}
