package com.dennis.usaficustomer;

import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
import com.dennis.usaficustomer.Common.Common;

public class UserMainActivity extends AppCompatActivity implements HomeFragment.homeFragmentListener, FAQFragment.faqFragmentListener {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccesorsAdapter myTabAccessorAdapter;


    private GoogleApiClient googleApiClient;
    final static int REQUEST_LOCATION = 199;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private CircleImageView navProfileImage;
    private TextView navProfileUserName;

    private HomeFragment homeFragment;
    private FAQFragment faqFragment;
    private DatabaseReference mDatabaseRef;

    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference(Common.user_customer_tbl);

        //adding toolbar in main activity
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Usafi");

        //adding navigation view in drawer layout
        drawerLayout = (DrawerLayout) findViewById(R.id.user_main);
        actionBarDrawerToggle = new ActionBarDrawerToggle(UserMainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        navProfileUserName = (TextView) navView.findViewById(R.id.nav_user_fullname);

        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabAccessorAdapter = new TabsAccesorsAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabAccessorAdapter);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");


        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("fullname"))
                {
                    String fullname = dataSnapshot.child("fullname").getValue().toString();
                    navProfileUserName.setText(fullname);
                }
                if(dataSnapshot.hasChild("uploads"))
                {
                    String uploads = dataSnapshot.child("uploads").getValue().toString();
                    navProfileImage.setImageURI(Uri.parse(uploads));
                }

                else
                {
                    Toast.makeText(UserMainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //adding the left functionalities with button
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                UserMenuSelector(menuItem);

                return false;
            }
        });

        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

        enableLoc();

    }


    @Override
    public void  onInputASent(CharSequence input) {


        try {
//            FAQFragment.updateEditText(input);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    @Override
    public void onInputBSent(CharSequence input) {
        try {
//            HomeFragment.updateEditText(input);
        } catch (Exception e) {


        }

    }



    /*
    protected void onStart() {
        super.onStart();

        //current user authentication checking
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            SendUserToLoginActivity();
        }

        //checking either user has any database recorded for our two step verification
        else{
            CheckUserExistance();
        }

    }
    */

    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(UserMainActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error","Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(UserMainActivity.this, REQUEST_LOCATION);

//                                finish();
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }
    }

    private void CheckUserExistance() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        //referencing Fdatabase
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)){
                    SendUserToSetupActivity();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(UserMainActivity.this, UserSetupActivity.class);
        startActivity(setupIntent);
        finish();
    }

    private void UserMenuSelector(MenuItem menuitem) {
        switch (menuitem.getItemId()){

            case R.id.nav_home:
                ViewPager viewPager1 =  this.findViewById(R.id.main_tabs_pager);
                viewPager1.setCurrentItem(0);
                Toast.makeText(this,"Home",Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_request_pickup:
                ViewPager viewPager2 =  this.findViewById(R.id.main_tabs_pager);
                viewPager2.setCurrentItem(3);
                Toast.makeText(this,"Request pickup",Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_profile:
                ViewPager viewPager3 =  this.findViewById(R.id.main_tabs_pager);
                viewPager3.setCurrentItem(1);
                Toast.makeText(this,"Profile",Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_contract:
                ViewPager viewPager4 =  this.findViewById(R.id.main_tabs_pager);
                viewPager4.setCurrentItem(2);
                Toast.makeText(this,"Offices",Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                Toast.makeText(this,"Settings",Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_prices:
                Toast.makeText(this,"prices",Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_faq:
                Toast.makeText(this,"FAQ's",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UserMainActivity.this, FAQ2Activity.class);
                startActivity(intent);
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;




        }

    }

    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(UserMainActivity.this,UserLoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        //finish();
    }
}
