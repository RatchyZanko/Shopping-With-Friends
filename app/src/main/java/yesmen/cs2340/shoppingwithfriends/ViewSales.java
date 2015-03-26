package yesmen.cs2340.shoppingwithfriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Class ViewSales extends ActionBarActivity and implements View.OnClickListener,
 * is all the code that pertains to the view friends page in android.
 *
 * @author Luka Antolic-Soban, Resse Aitken, Ratchapong Tangkijvorakul, Matty Attokaren, Sunny Patel
 * @version 1.6
 */
public class ViewSales extends FragmentActivity implements View.OnClickListener {

    private ProgressDialog progressDialog;
    private Button cancelbutton;
    ListView listView;
    private ArrayList<String> values = new ArrayList<>();
    private ArrayList<ItemReport> markers = new ArrayList<>();

    private GoogleMap googleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LatLng currentSaleMarker = new LatLng(33.75, -84.39);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sales);

        //here is where GoogleMaps will create a marker depending on the sale selection
        try {
            if (googleMap == null) {
                googleMap = ((MapFragment) getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMyLocationEnabled(true);
            UiSettings uiSettings = googleMap.getUiSettings();
            uiSettings.setZoomControlsEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        cancelbutton = (Button) findViewById(R.id.view_sale_list_cancel_button);
        cancelbutton.setOnClickListener(this);


        new ViewSaleAttempt().execute();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.view_sale_list_cancel_button) {
            Intent intention = new Intent(this, Homepage.class);
            startActivity(intention);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_sales, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Class ViewSaleAttempt extends AsyncTask, checks the attempt to view friends.
     */
    class ViewSaleAttempt extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ViewSales.this);
            progressDialog.setMessage("Refreshing Friends...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            String myUser = CurrentUser.getCurrentUser().getUsername();
            try {

                Wishlist myWishList = DatabaseInterfacer.getWishlist(myUser);
                ArrayList<Item> itemArray = myWishList.getWishlist();

                ItemReport[] reportedSales = DatabaseInterfacer.retrieveItemReports();
                for (ItemReport obj : reportedSales) {
                    for (int i = 0; i < itemArray.size(); i++) {
                        if(obj.getProductName().equals(itemArray.get(i).getName()) && obj.getPrice() <= itemArray.get(i).getPrice()) {
                            String read;
                            if (obj.getRead() == 1) {
                                read = "";
                            } else {
                                read = "New Report!";
                            }

                            values.add(read + "\n"
                                + "Product: " + obj.getProductName() + "\n"
                                + "Price: $" + obj.getPrice() + "\n"
                                + "Location: " + obj.getLocation() + "\n"
                                + "Quantity: " + obj.getQuantity() + "\n"
                                + "Sent by: " + obj.getOriginator() + "\n");
                        }

                        markers.add(obj);
                    }
                }
                DatabaseInterfacer.updateRead();
                Collections.reverse(values);

            } catch (DatabaseErrorException e) {
                return e.getMessage();
            }
            return "Success!";
        }

        /**
         * When a post is executed
         *
         * @param file_url
         */
        protected void onPostExecute(String file_url) {
            //DatabaseInterfacer.updateRead();
            listView = (ListView) findViewById(R.id.view_sale_list);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ViewSales.this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, values);
            listView.setAdapter(adapter);

            for(ItemReport obj : markers) {
                LatLng currentSaleMarker = new LatLng(obj.getLatitude(), obj.getLongitude());
                googleMap.addMarker(new MarkerOptions().
                        position(currentSaleMarker).title("Sale of " + obj.getProductName() + " - "
                                + obj.getOriginator()));
            }
            /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    // ListView Clicked item index
                    int itemPosition = position;

                    // ListView Clicked item value
                    String itemValue = (String) listView.getItemAtPosition(position);

                    // Show Alert
                    Toast.makeText(getApplicationContext(),
                            "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                            .show();

                    Intent intent = new Intent(getBaseContext(), DetailedFriend.class);
                    intent.putExtra("Username", itemValue);
                    startActivity(intent);

                }
            });*/

            // dismiss the dialog once product deleted
            progressDialog.dismiss();
            if (file_url != null) {
                Toast.makeText(ViewSales.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }
}
