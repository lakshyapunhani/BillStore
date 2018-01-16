package com.example.wuntu.billstore.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wuntu.billstore.R;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class VendorsFragment extends Fragment {


    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    public VendorsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vendors, container, false);

        return view;
    }

}
