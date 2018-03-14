package com.example.wuntu.billstore.Pojos;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

/**
 * Created by Dell on 24-02-2018.
 */

public class MakeBillDetails
{

    private CustomerDetails customerDetails;

    private String billTime;

    private double billAmount;

    private Map<String,ItemPojo> billItems;

    public MakeBillDetails()
    {}

    public MakeBillDetails(CustomerDetails customerDetails, String billTime, Map<String, ItemPojo> billItems,double billAmount) {
        this.customerDetails = customerDetails;
        this.billTime = billTime;
        this.billItems = billItems;
        this.billAmount = billAmount;
    }

    public double getBillAmount() {
        return billAmount;
    }

    public CustomerDetails getCustomerDetails() {
        return customerDetails;
    }

    public String getBillTime() {
        return billTime;
    }

    public Map<String, ItemPojo> getBillItems() {
        return billItems;
    }

}
