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
@SuppressWarnings("ALL")
public class ViewSales extends FragmentActivity implements View.OnClickListener {

    private ProgressDialog progressDialog;
    private ArrayList<String> values = new ArrayList<>();
    private ArrayList<ItemReport> markers = new ArrayList<>();

    private GoogleMap googleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sales);

        //here is where GoogleMaps will create a marker depending on the sale selection
        //it creates the map and type and then controls for user access
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

        Button cancelButton = (Button) findViewById(R.id.view_sale_list_cancel_button);
        cancelButton.setOnClickListener(this);


        new ViewSaleAttempt().execute();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.view_sale_list_cancel_button) {
            Intent intention = new Intent(this, Homepage.class);
            startActivity(intention);
            overridePendingTransition(R.anim.left_in, R.anim.right_out);

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
    private class ViewSaleAttempt extends AsyncTask<String, String, String> {

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

                //gets the wishlist from the user to compare sales
                Wishlist myWishList = DatabaseInterfacer.getWishlist(myUser);
                ArrayList<Item> itemArray = myWishList.getWishlist();

                ItemReport[] reportedSales = DatabaseInterfacer.retrieveItemReports();

                //we loop through the item reports and grab the relevant ones to the user
                //then added the strings to a new list for the ListView to display
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

        @Override
        protected void onPostExecute(String file_url) {
            //DatabaseInterfacer.updateRead();
            ListView listView = (ListView) findViewById(R.id.view_sale_list);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewSales.this,
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
