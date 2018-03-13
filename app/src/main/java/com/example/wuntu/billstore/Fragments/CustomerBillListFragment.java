package com.example.wuntu.billstore.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wuntu.billstore.Adapters.CustomerBillListAdapter;
import com.example.wuntu.billstore.Adapters.VendorsBillListAdapter;
import com.example.wuntu.billstore.Pojos.MakeBillDetails;
import com.example.wuntu.billstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomerBillListFragment extends Fragment {


    @BindView(R.id.customerRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.customerName)
    TextView txt_vendorName;

    LinearLayoutManager mLayoutManager;

    ArrayList<MakeBillDetails> billsList;

    CustomerBillListAdapter customerBillListAdapter;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;

    Context context;

    String vendorName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_bill_list, container, false);
        ButterKnife.bind(this,view);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (getArguments() != null)
        {
            vendorName = getArguments().getString("VendorName");
            txt_vendorName.setText(vendorName);
        }

        billsList = new ArrayList<>();

        customerBillListAdapter = new CustomerBillListAdapter(billsList);

        mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(customerBillListAdapter);


        /*recyclerView.addOnItemTouchListener(
                new RecyclerViewListener(context, recyclerView, new RecyclerViewListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position)
                    {
                        BillsFragment billsFragment = new BillsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("VendorName",vendorName);
                        bundle.putString("BillDate",billsList.get(position).getBillDate());
                        bundle.putString("BillTime",billsList.get(position).getBillTime());
                        billsFragment.setArguments(bundle);
                        getFragmentManager().beginTransaction().replace(R.id.frameLayout,billsFragment).addToBackStack(null).commit();
                    }

                    @Override
                    public void onLongItemClick(View view, int position)
                    {

                    }
                })
        );*/


        CollectionReference collectionReference = db.collection("Users").document(firebaseUser.getUid()).collection("Customers")
                .document(vendorName).collection(firebaseUser.getUid());


        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null)
                {
                    Toast.makeText(context, "Bill Date Request Failed", Toast.LENGTH_SHORT).show();
                    return;
                }

                billsList.clear();
                for (DocumentSnapshot doc : documentSnapshots)
                {
                    MakeBillDetails makeBillDetails = doc.toObject(MakeBillDetails.class);
                    billsList.add(makeBillDetails);
                }
                customerBillListAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }

}