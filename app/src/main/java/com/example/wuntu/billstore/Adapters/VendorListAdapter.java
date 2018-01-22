package com.example.wuntu.billstore.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wuntu.billstore.Pojos.VendorDetails;
import com.example.wuntu.billstore.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Dell on 1/17/2018.
 */

public class VendorListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    ArrayList<VendorDetails> vendorDetailsList;

    VendorDetails vendorDetails;

    public VendorListAdapter(ArrayList<VendorDetails> vendorDetailsList)
    {
        this.vendorDetailsList = vendorDetailsList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.traderName)
        TextView traderName;

        @BindView(R.id.traderAddress)
        TextView traderAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.traderview,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        if (holder instanceof ViewHolder)
        {
            vendorDetails = vendorDetailsList.get(position);
            ((ViewHolder)holder).traderName.setText(vendorDetails.getVendorName());
            ((ViewHolder)holder).traderAddress.setText(vendorDetails.getVendorAddress());
        }
    }

    @Override
    public int getItemCount()
    {
        return this.vendorDetailsList.size();
    }
}