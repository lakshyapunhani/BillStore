package com.fabuleux.wuntu.billstore.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fabuleux.wuntu.billstore.Pojos.ContactPojo;
import com.fabuleux.wuntu.billstore.Pojos.CustomerDetails;
import com.fabuleux.wuntu.billstore.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Dell on 12-03-2018.
 */

public class CustomerListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private ArrayList<ContactPojo> customerDetailsList;

    public CustomerListAdapter(ArrayList<ContactPojo> customerDetailsList) {
        this.customerDetailsList = customerDetailsList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.traderName)
        TextView traderName;

        @BindView(R.id.traderAddress)
        TextView traderAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_customers, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ContactPojo contactPojo = customerDetailsList.get(position);
            ((ViewHolder) holder).traderName.setText(contactPojo.getContactName());
            ((ViewHolder) holder).traderAddress.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_house_black, 0, 0, 0);
            ((ViewHolder) holder).traderAddress.setText(contactPojo.getContactAddress());
        }
    }

    @Override
    public int getItemCount() {
        return this.customerDetailsList.size();
    }
}
