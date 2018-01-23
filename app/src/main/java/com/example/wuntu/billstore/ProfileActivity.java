package com.example.wuntu.billstore;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.wuntu.billstore.Adapters.VendorListAdapter;
import com.example.wuntu.billstore.Fragments.VendorsFragment;
import com.example.wuntu.billstore.Pojos.User;
import com.example.wuntu.billstore.Pojos.VendorDetails;
import com.example.wuntu.billstore.Utils.RecyclerViewListener;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseFirestore db;

    GoogleApiClient googleApiClient;




    //ProgressDialog progressDialog;

    String user_name,shop_name;

    Drawer result;

    AccountHeader headerResult;

    ArrayList<VendorDetails> vendorDetailsList;

    @BindView(R.id.emptyLayout)
    LinearLayout emptyLayout;

    @BindView(R.id.traderRecyclerView)
    RecyclerView traderRecyclerView;

    @BindView(R.id.profileActivity_layout) LinearLayout profileActivity_layout;

    @BindView(R.id.toolbar) Toolbar toolbar;

    VendorListAdapter vendorListAdapter;

    ArrayList<String> vendorNameList;

    //VendorsFragment vendorsFragment;


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

        vendorDetailsList = new ArrayList<>();
        vendorNameList = new ArrayList<>();

        //vendorsFragment = new VendorsFragment();

        toolbar.setTitle("Bill Store");

        setSupportActionBar(toolbar);

        vendorListAdapter = new VendorListAdapter(vendorDetailsList);



        //progressDialog = new ProgressDialog(this);
        db = FirebaseFirestore.getInstance();

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);



        firebaseAuth = FirebaseAuth.getInstance();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        traderRecyclerView.setLayoutManager(mLayoutManager);
        traderRecyclerView.setItemAnimator(new DefaultItemAnimator());
        traderRecyclerView.setAdapter(vendorListAdapter);

        traderRecyclerView.addOnItemTouchListener(
                new RecyclerViewListener(this, traderRecyclerView, new RecyclerViewListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Toast.makeText(ProfileActivity.this, "" + vendorDetailsList.get(position).getVendorName(), Toast.LENGTH_SHORT).show();
                        VendorsFragment vendorsFragment = new VendorsFragment();
                        profileActivity_layout.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().add(R.id.frameLayout,vendorsFragment).commit();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                })
        );

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {

                    //User is signed in

                    DocumentReference profileReference = db.collection("Users").document(firebaseUser.getUid());

                    CollectionReference billsReference = db.collection("Users").document(firebaseUser.getUid()).collection("Bills");

                    CollectionReference billDateReference = db.collection("Users").document(firebaseUser.getUid()).collection("Bills")
                            .document("AB Trader").collection("UID");

                    profileReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (!documentSnapshot.exists())
                            {
                                Toast.makeText(ProfileActivity.this, "Profile Request Failed", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            user_name = documentSnapshot.get("name").toString();
                            shop_name = documentSnapshot.get("shop_name").toString();

                            addNavigationDrawer();
                            //Toast.makeText(ProfileActivity.this, "Profile Request " + documentSnapshot.getData(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Profile Request Failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                    billsReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e)
                        {
                            if (e != null) {
                                Toast.makeText(ProfileActivity.this, "Bills Request Failed", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            vendorDetailsList.clear();
                            vendorNameList.clear();

                            if (documentSnapshots.isEmpty())
                            {
                                emptyLayout.setVisibility(View.VISIBLE);
                                traderRecyclerView.setVisibility(View.GONE);
                            }



                            for (DocumentSnapshot doc : documentSnapshots) {
                                if (traderRecyclerView.getVisibility() == View.GONE && emptyLayout.getVisibility() == View.VISIBLE)
                                {
                                    traderRecyclerView.setVisibility(View.VISIBLE);
                                    emptyLayout.setVisibility(View.GONE);
                                }
                                VendorDetails vendorDetails = doc.toObject(VendorDetails.class);
                                vendorDetailsList.add(vendorDetails);
                                vendorNameList.add(doc.getId());
                            }

                            vendorListAdapter.notifyDataSetChanged();
                        }
                    });


                    /*billDateReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                            if (e != null)
                            {
                                Toast.makeText(ProfileActivity.this, "Bill Date Request Failed", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            for (DocumentSnapshot doc : documentSnapshots)
                            {
                                Toast.makeText(ProfileActivity.this, "Bill Date Request " + doc.getId(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });*/

                    /*final Map<String,String> map = new HashMap<>();
                    map.put("Document","Document");

                    CollectionReference traderReference = db.collection("Users").document(firebaseUser.getUid()).collection("Bills");

                    traderReference.document("ABC Trader").set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            db.collection("Users").document(firebaseUser.getUid()).collection("Bills").document("ABC Trader")
                                    .collection(firebaseUser.getUid()).document("13May18").set(map)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(ProfileActivity.this, "Bill Added", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileActivity.this, "Bill not added", Toast.LENGTH_SHORT).show();
                                }
                            });

                            Toast.makeText(ProfileActivity.this, "Trader Request Success", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Trader Request Failure", Toast.LENGTH_SHORT).show();
                        }
                    });*/


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
                .addProfiles(
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
                .withName("Add new Bill").withIcon(R.drawable.ic_add_new_bill);

        SecondaryDrawerItem logoutItem = new SecondaryDrawerItem().withIdentifier(2)
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
                               // Toast.makeText(ProfileActivity.this, "Reached Right Place ", Toast.LENGTH_SHORT).show();
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


    @OnClick(R.id.btn_addNewBill)
    public void addNewBillClick()
    {
        Intent intent = new Intent(ProfileActivity.this,AddNewBillActivity.class);
        intent.putParcelableArrayListExtra("VENDOR_NAME_LIST",vendorDetailsList);
        startActivity(intent);
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }


}
