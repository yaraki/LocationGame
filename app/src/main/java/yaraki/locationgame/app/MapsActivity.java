package yaraki.locationgame.app;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        mLocationClient = new LocationClient(this, this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.disconnect();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(false);
        uiSettings.setZoomControlsEnabled(false);
        mMap.setOnMarkerClickListener(this);
        setUpPortals();
    }

    private void setUpPortals() {
        new AsyncTask<Void, Void, ArrayList<MarkerOptions>>() {

            @Override
            protected ArrayList<MarkerOptions> doInBackground(Void... params) {
                try {
                    return download();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            private ArrayList<MarkerOptions> download() throws IOException {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("https://gist.githubusercontent.com/yaraki/6d6b1d06ec7015bdfd79/raw/portals.csv");
                    connection = (HttpURLConnection) url.openConnection();
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    ArrayList<MarkerOptions> portals = new ArrayList<MarkerOptions>();
                    while (null != (line = reader.readLine())) {
                        String[] values = TextUtils.split(line, ",");
                        portals.add(new MarkerOptions()
                                .position(new LatLng(
                                        Double.parseDouble(values[0]),
                                        Double.parseDouble(values[1])))
                                .title(values[2]));
                    }
                    return portals;
                } finally {
                    if (null != reader) {
                        reader.close();
                    }
                    if (null != connection) {
                        connection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(ArrayList<MarkerOptions> portals) {
                for (MarkerOptions portal : portals) {
                    mMap.addMarker(portal);
                }
            }

        }.execute();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);
        request.setFastestInterval(1000);
        setMapLocation(mLocationClient.getLastLocation());
        mLocationClient.requestLocationUpdates(request, this);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        setMapLocation(location);
    }

    private void setMapLocation(Location location) {
        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 17));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Toast.makeText(this, "「" + marker.getTitle() + "」をキャプチャしました!", Toast.LENGTH_SHORT).show();
        return true;
    }

}
