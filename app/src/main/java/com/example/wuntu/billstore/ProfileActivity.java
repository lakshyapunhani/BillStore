package com.example.wuntu.billstore;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.wuntu.billstore.Utils.ProfileInformationDialog;
import com.example.wuntu.billstore.Utils.User;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileActivity extends AppCompatActivity {

    ProfileInformationDialog profileInformationDialog;

    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseFirestore db;
    DocumentReference documentReference;


    GoogleApiClient googleApiClient;

    @BindView(R.id.toolbar)
            Toolbar toolbar;

    ProgressDialog progressDialog;

    String user_name,shop_name;

    Drawer result;

    AccountHeader headerResult;

    @BindView(R.id.fab)
            FloatingActionButton floatingActionButton;


    @Override
    public void onStart()
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleApiClient.connect();
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        db = FirebaseFirestore.getInstance();
        profileInformationDialog = new ProfileInformationDialog(this);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);



        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    //progressDialog.setMessage("Loading...");
                    //progressDialog.show();
                    //progressDialog.setCancelable(false);

                    //User is signed in
                    documentReference = db.collection("Users").document(firebaseUser.getUid());

                    documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            //progressDialog.hide();
                            if (e != null)
                            {
                                Log.w("TAG", "Listen failed.", e);
                                return;
                            }
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                User user1 = documentSnapshot.toObject(User.class);
                                user_name = user1.getName();
                                shop_name = user1.getShop_name();
                                addNavigationDrawer();
                            }
                            else
                            {
                                profileInformationDialog.show();
                                profileInformationDialog.setCancelable(false);
                                addNavigationDrawer();
                            }

                        }
                    });

                    Log.d("TAG", "onAuthStateChanged:signed_in:" + firebaseUser.getUid());
                } else {
                    // User is signed out
                    Log.d("TAG", "onAuthStateChanged:signed_out");
                }
            }
        };



    }

    private void addNavigationDrawer()
    {
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.curve_shape)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles
                        (
                        new ProfileDrawerItem().withName(user_name).withEmail(shop_name).withIcon(R.drawable.ic_profile_round)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile)
                    {
                        return false;
                    }
                })
                .build();


        PrimaryDrawerItem makeBillItem = new PrimaryDrawerItem().withIdentifier(1)
                .withName("Add new Bill").withIcon(R.drawable.ic_add_bill);

        PrimaryDrawerItem logoutItem = new PrimaryDrawerItem().withIdentifier(2)
                .withName("Log Out").withIcon(R.drawable.ic_log_out);

        result = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
                .withToolbar(toolbar)
                .withTranslucentStatusBar(true)
                .withDisplayBelowStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .withSelectedItem(-1)
                .addDrawerItems(
                        makeBillItem,
                        new DividerDrawerItem(),
                        logoutItem
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        switch (position)
                        {
                            case 3:
                                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                                        new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(@NonNull Status status)
                                            {}
                                        });

                                firebaseAuth.signOut();
                                LoginManager.getInstance().logOut();

                                startActivity(new Intent(ProfileActivity.this,SignInActivity.class));
                                finish();
                                break;
                        }
                        return true;
                    }
                })
                .build();

    }


    @OnClick(R.id.fab)
    public void fab_click()
    {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.otp_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        Toast.makeText(this, "Add a new bill", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

}