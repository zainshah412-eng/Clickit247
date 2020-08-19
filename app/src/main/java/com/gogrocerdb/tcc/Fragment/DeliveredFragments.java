package com.gogrocerdb.tcc.Fragment;


import android.app.Fragment;
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
import com.gogrocerdb.tcc.AppController;
import com.gogrocerdb.tcc.Config.BaseURL;
import com.gogrocerdb.tcc.Model.My_order_model;
import com.gogrocerdb.tcc.R;
import com.gogrocerdb.tcc.util.ConnectivityReceiver;
import com.gogrocerdb.tcc.util.CustomVolleyJsonArrayRequest;
import com.gogrocerdb.tcc.util.Session_management;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DeliveredFragments extends Fragment {
    private static String TAG = Home.class.getSimpleName();
    private RecyclerView rv_myorder;
    private List<My_order_model> my_order_modelList = new ArrayList<>();
    private Session_management sessionManagement;
    String get_id;
    KProgressHUD hud;

    RelativeLayout no_order_layout;
    LinearLayout ordersLayout;
    public DeliveredFragments() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivered_fragments,
                container, false);
        sessionManagement = new Session_management(getActivity());
        get_id = sessionManagement.getUserDetails().get(BaseURL.KEY_NAME);
        rv_myorder = (RecyclerView) view.findViewById(R.id.rv_myorder);
        no_order_layout = view.findViewById(R.id.no_order_layout);
        ordersLayout = view.findViewById(R.id.orders_layout);
        rv_myorder.setLayoutManager(new LinearLayoutManager(getActivity()));

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
                if(hud.isShowing()){
                    hud.dismiss();
                }
                try {

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
                            my_order_modelList.add(my_order_model);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                DeliveredOrdersAdapter myPendingOrderAdapter = new DeliveredOrdersAdapter(my_order_modelList);
                rv_myorder.setAdapter(myPendingOrderAdapter);
                myPendingOrderAdapter.notifyDataSetChanged();


                if (my_order_modelList.isEmpty()) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_rcord_found), Toast.LENGTH_SHORT).show();
                    ordersLayout.setVisibility(View.GONE);
                    no_order_layout.setVisibility(View.VISIBLE);
                }else {
                    ordersLayout.setVisibility(View.VISIBLE);
                    no_order_layout.setVisibility(View.GONE);
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


}
