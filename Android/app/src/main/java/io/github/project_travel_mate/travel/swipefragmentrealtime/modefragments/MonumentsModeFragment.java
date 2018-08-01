package io.github.project_travel_mate.travel.swipefragmentrealtime.modefragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.project_travel_mate.R;
import io.github.project_travel_mate.travel.swipefragmentrealtime.MapListItemAdapter;
import objects.MapItem;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static utils.Constants.HERE_API_APP_CODE;
import static utils.Constants.HERE_API_APP_ID;
import static utils.Constants.HERE_API_LINK;
import static utils.Constants.HERE_API_MODES;

public class MonumentsModeFragment extends Fragment {

    @BindView(R.id.listview)
    ListView listView;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;

    private static String mCurlat;
    private static String mCurlon;
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private List<MapItem> mMapItems =  new ArrayList<>();

    public MonumentsModeFragment() {
        //required public constructor
    }

    public static MonumentsModeFragment newInstance(String currentLat, String currentLon) {
        mCurlat = currentLat;
        mCurlon = currentLon;
        return new MonumentsModeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_each_list_real_time, container, false);
        ButterKnife.bind(this, rootView);
        getPlaces();
        return rootView;
    }

    /**
     * Gets all nearby monuments
     */
    private void getPlaces() {
        Handler handler = new Handler(Looper.getMainLooper());

        String uri = HERE_API_LINK + "?at=" + mCurlat + "," + mCurlon + "&cat=" + HERE_API_MODES.get(2)
                + "&app_id=" + HERE_API_APP_ID + "&app_code=" + HERE_API_APP_CODE;

        Log.v("EXECUTING", uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        Request request = new Request.Builder()
                .url(uri)
                .build();
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
                handler.post(() -> networkError());
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {

                final String res = Objects.requireNonNull(response.body()).string();
                handler.post(() -> {
                    try {
                        JSONObject json = new JSONObject(res);
                        json = json.getJSONObject("results");
                        JSONArray routeArray = json.getJSONArray("items");

                        for (int i = 0; i < routeArray.length(); i++) {
                            String name = routeArray.getJSONObject(i).getString("title");
                            String web = routeArray.getJSONObject(i).getString("href");
                            String number = routeArray.getJSONObject(i).getString("distance");
                            String address = routeArray.getJSONObject(i).getString("vicinity");

                            mMapItems.add(new MapItem(name, number, web, address));
                        }
                        animationView.setVisibility(View.GONE);
                        listView.setAdapter(new MapListItemAdapter(mContext, mMapItems));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        noResult();
                        Log.e("ERROR : ", e.getMessage() + " ");
                    }
                });
            }
        });
    }

    /**
     * Plays the network lost animation in the view
     */
    private void networkError() {
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation(R.raw.network_lost);
        animationView.playAnimation();
    }

    /**
     * Plays the no data found animation in the view
     */
    private void noResult() {
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation(R.raw.empty_list);
        animationView.playAnimation();
    }
}
