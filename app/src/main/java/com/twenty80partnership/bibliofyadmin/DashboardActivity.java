package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofyadmin.Services.ListenOrder;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ProgressDialog pd;
    private DrawerLayout drawerLayout;
    private TextView headerName, headerEmail;
    private ImageView headerPhoto;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInClient mGoogleSignInClient;
    private Button homeMenu,homeCourses,homeStationary,deliveryPins,btnTest, btnDeliveryPersons;
    LottieAnimationView thumb_up;


    private boolean val = false;
    private boolean liked;
    private Button sliderMenu;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        pd = new ProgressDialog(DashboardActivity.this);
        pd.setMessage("loading");
        pd.setCancelable(false);
        //pd.show();

        liked =false;

        thumb_up = findViewById(R.id.thumbup);
        thumb_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {




                sendNotification("d3tPPXFpQdC8IPAtEoZw9J:APA91bGguFdTiz-35NgqxOZX3EfJ_bvJkJX86DugKIAQhLRqY0mWiUJR3lSGAKnVx_ZQxG1g-z4yhJzVEitS9M-zy3RleauWCDX47rpp6CRWueB12dXsFd5jcqTl-BUykeySbimcbBGg");

                if (!liked){
                    liked = true;
                    thumb_up.playAnimation();
                    Toast.makeText(DashboardActivity.this, "Cheers!!", Toast.LENGTH_SHORT).show();
                }
                else {
                    liked = false;
                    thumb_up.cancelAnimation();
                    thumb_up.setProgress(0);
                }

            }
        });

        mAuth = FirebaseAuth.getInstance();


        Intent service = new Intent(DashboardActivity.this, ListenOrder.class);
        startService(service);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.drawer);

        View headerView = navigationView.getHeaderView(0);


        headerName = (TextView) headerView.findViewById(R.id.header_name);
        headerEmail = (TextView) headerView.findViewById(R.id.header_email);
        headerPhoto = headerView.findViewById(R.id.header_photo);
        homeMenu = findViewById(R.id.home_menu);
        homeCourses = findViewById(R.id.home_courses);
        homeStationary = findViewById(R.id.home_stationary);
        sliderMenu = findViewById(R.id.slider_menu);
        deliveryPins = findViewById(R.id.delivery_pins);
        btnTest = findViewById(R.id.btn_test);
        btnDeliveryPersons = findViewById(R.id.btn_delivery_persons);


        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))//"658054251523-u7mkfo1kkfbe4iamkes35b2borma0e6h.apps.googleusercontent.com"
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        homeMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this,HomeMenuActivity.class));
            }
        });

        sliderMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this,SliderMenuActivity.class));
            }
        });

        homeCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this,HomeCoursesActivity.class));

            }
        });

        homeStationary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, YMALMenuActivity.class));

            }
        });

        deliveryPins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this,DeliveryPinsActivity.class));
            }
        });

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this,DataAcessTestActivity.class));
            }
        });

        btnDeliveryPersons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this,DeliveryPersonsActivity.class));
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        String id = mAuth.getCurrentUser().getUid();


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null){
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUri = user.getPhotoUrl();

            String arr[] = name.split(" ", 2);
            String firstName = arr[0];
            headerName.setText(firstName);
            headerEmail.setText(email);
            Picasso.get().load(photoUri).placeholder(R.drawable.userdisplay).into(headerPhoto);
        }

    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


        if (item.getItemId() != R.id.logout)
            closeDrawer();

        switch (item.getItemId()) {
            case R.id.users:
                startActivity(new Intent(DashboardActivity.this,UsersActivity.class));
                break;
            case R.id.orders:
                startActivity(new Intent(DashboardActivity.this,OrdersActivity.class));
                break;
            case R.id.courses_listing:
                Intent intent = new Intent(DashboardActivity.this, CoursesListingActivity.class);
                intent.putExtra("mode","books");
                startActivity(intent);
                break;

            case R.id.stationary_listing:
                Intent i = new Intent(DashboardActivity.this,StationaryActivity.class);
                i.putExtra("mode","stationary");
                startActivity(i);
                break;

            case R.id.slider_menu:
                startActivity(new Intent(DashboardActivity.this,SliderMenuActivity.class));
                break;

            case R.id.logout:

                AlertDialog.Builder builder1 = new AlertDialog.Builder(DashboardActivity.this);
                builder1.setMessage("Click Yes To Logout");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mAuth.signOut();
                                mGoogleSignInClient.signOut();

                                // mGoogleSignInClient.signOut();
                                Toast.makeText(DashboardActivity.this, "Successfully Signed Out", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                                finish();
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder1.create();
                alert.show();
                break;
        }

        return true;
    }

    private void closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer();

        } else {
            if (!val) {
                Toast.makeText(DashboardActivity.this, "Press Again To Exit", Toast.LENGTH_SHORT).show();
                val = true;
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        val = false;
                    }
                }, 2000);
            } else {
                //userDataRef.removeEventListener(userDataListener);
                super.onBackPressed();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu; this adds items to the action bar if it present
       // getMenuInflater().inflate(R.menu.dashboard_menu,menu);
       // MenuItem cart=menu.findItem(R.id.cart);
        // cart.setIcon(R.drawable.search_icon);
//        cart.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                startActivity(new Intent(DashboardActivity.this,CartActivity.class));
//                return false;
//            }
//        });

        return super.onCreateOptionsMenu(menu);
    }


    private void sendNotification(final String regToken) {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json=new JSONObject();
                    JSONObject dataJson=new JSONObject();
                    dataJson.put("body","Hi this is sent from device to device");
                    dataJson.put("title","dummy title");
                    json.put("notification",dataJson);
                    json.put("to",regToken);
                    json.put("data",dataJson);
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization","key="+ "AAAAmTcXBAM:APA91bGRyAmK9vm-MTkiVnZgpI5Wt_ee_DQEGSyMKmxJY1qWDlXt0uDh_zhVm0i3nSYo3hNUDSK89SksRy04tOc4fYnP-hA6tky9owSvxdpQypqX5I3FrajnaoqtuPVaTYveoVL0Lqe0")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();

                    Log.d("notificationDe",finalResponse);
                }catch (Exception e){
                    //Log.d(TAG,e+"");
                }
                return null;
            }
        }.execute();

    }


}

