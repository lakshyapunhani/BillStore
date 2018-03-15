package com.example.wuntu.billstore.Fragments;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wuntu.billstore.Adapters.ProductAdapter;
import com.example.wuntu.billstore.AddItemActivity;
import com.example.wuntu.billstore.Dialogs.SearchableSpinner;
import com.example.wuntu.billstore.EventBus.EventClearBill;
import com.example.wuntu.billstore.EventBus.ItemToMakeBill;
import com.example.wuntu.billstore.Pojos.CustomerDetails;
import com.example.wuntu.billstore.Pojos.ItemPojo;
import com.example.wuntu.billstore.PreviewActivity;
import com.example.wuntu.billstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

/**
 * A simple {@link Fragment} subclass.
 */
public class MakeBillFragment extends Fragment {

    @BindView(R.id.btn_preview)
    TextView btn_preview;

    @BindView(R.id.recycler_items)
    RecyclerView recycler_items;

    @BindView(R.id.invoice_date) TextView invoice_date;

    @BindView(R.id.customerSpinner)
    SearchableSpinner customerSpinner;

    @BindView(R.id.radio_existingCustomer)
    RadioButton radio_existingCustomer;

    @BindView(R.id.radio_newCustomer)
    RadioButton radio_newCustomer;

    @BindView(R.id.innerView_existingCustomer)
    LinearLayout innerView_existingCustomer;

    @BindView(R.id.innerView_newCustomer)
    LinearLayout innerView_newCustomer;

    @BindView(R.id.outerView_newCustomer)
    LinearLayout outerView_newCustomer;

    @BindView(R.id.outerView_existingCustomer)
    LinearLayout outerView_existingCustomer;

    @BindView(R.id.edt_newCustomerName)
    EditText edt_newCustomerName;

    @BindView(R.id.edt_newCustomerAddress) EditText edt_newCustomerAddress;

    @BindView(R.id.edt_newCustomerGst) EditText edt_newCustomerGst;

    LinearLayoutManager mLayoutManager;

    ProductAdapter productAdapter;

    private Context mContext;

    private ArrayList<ItemPojo> itemList;

    Calendar myCalendar = Calendar.getInstance();

    SimpleDateFormat convertDf = new SimpleDateFormat("MMMM dd, yyyy");

    boolean customerView;

    ArrayList<CustomerDetails> customersList;
    ArrayList<String> customerNameList;

    private FirebaseFirestore db;
    FirebaseUser firebaseUser;

    String newCustomerName ="",newCustomerAddress = "",newCustomerGstNumber ="";
    String invoiceDate = "";

    long timestamp;
    String timestampString;

    ArrayAdapter<String> spinnerAdapter;
    int customerSpinnerValue;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public MakeBillFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_make_bill, container, false);
        ButterKnife.bind(this,view);

        customerNameList = new ArrayList<>();
        customersList = new ArrayList<>();
        itemList = new ArrayList<>();
        productAdapter = new ProductAdapter(itemList);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseUser = firebaseAuth.getCurrentUser();

        mLayoutManager = new LinearLayoutManager(getActivity());
        recycler_items.setLayoutManager(mLayoutManager);
        recycler_items.setItemAnimator(new DefaultItemAnimator());
        recycler_items.setAdapter(productAdapter);

        long date = System.currentTimeMillis();

        String dateString = convertDf.format(date);
        invoice_date.setText(dateString);

        spinnerAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, customerNameList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        customerSpinner.setTitle("Select Customer");
        customerSpinner.setAdapter(spinnerAdapter);

        getCustomerList();

        return view;
    }

    private void getCustomerList()
    {
        CollectionReference getCustomerReference = db.collection("Users").document(firebaseUser.getUid()).collection("Customers");

        getCustomerReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null)
                {
                    Toast.makeText(mContext, "Something went wrong. Please try again", Toast.LENGTH_SHORT).show();
                    return;
                }
                customersList.clear();
                if (documentSnapshots.isEmpty())
                {
                    newCustomerViewClick();
                    return;
                }
                for (DocumentSnapshot documentSnapshot : documentSnapshots)
                {
                    CustomerDetails customerDetails = documentSnapshot.toObject(CustomerDetails.class);
                    customersList.add(customerDetails);
                    customerNameList.add(customerDetails.getCustomerName());
                }
                spinnerAdapter.notifyDataSetChanged();
            }
        });
    }

    @OnClick(R.id.layout_invoiced_items_header)
    public void headerClick()
    {
        Intent intent = new Intent(mContext,AddItemActivity.class);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(ItemToMakeBill event)
    {
        ItemPojo itemPojo = new ItemPojo(event.getItemName(),event.getCostPerItem(),event.getQuantity(),event.getItemType(),event.getTotalAmount(),event.getNote());
        itemList.add(itemPojo);
        productAdapter.notifyDataSetChanged();
        EventBus.getDefault().removeAllStickyEvents();
    };

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onClearEvent(EventClearBill event)
    {
        itemList.clear();
        productAdapter.notifyDataSetChanged();
        EventBus.getDefault().removeAllStickyEvents();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.invoice_date)
    public void onClickPickDate()
    {
        DatePickerDialog datePickerDialog = new  DatePickerDialog(mContext, R.style.DatePickerTheme, mDateListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth,
                              int selectedDay) {

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            try {
                Date reportDate = df.parse(selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay);
                invoice_date.setText((convertDf.format(reportDate)));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }

    };

    @OnClick({R.id.view_existingCustomer,R.id.radio_existingCustomer})
    public void existingCustomerViewClick()
    {
        customerView = false;
        radio_existingCustomer.setChecked(true);
        radio_newCustomer.setChecked(false);
        innerView_existingCustomer.setVisibility(View.VISIBLE);
        innerView_newCustomer.setVisibility(View.GONE);

    }

    @OnClick({R.id.view_newCustomer,R.id.radio_newCustomer})
    public void newCustomerViewClick()
    {
        customerView = true;
        radio_newCustomer.setChecked(true);
        radio_existingCustomer.setChecked(false);
        innerView_existingCustomer.setVisibility(View.GONE);
        innerView_newCustomer.setVisibility(View.VISIBLE);
    }


    @OnItemSelected(value = R.id.customerSpinner, callback = OnItemSelected.Callback.ITEM_SELECTED)
    void selectVehicle(AdapterView<?> adapterView, int newVal) {
        if (customersList.size()>0)
        {
            customerSpinnerValue = newVal;
        }
    }

    @OnClick(R.id.btn_preview)
    public void previewClick()
    {
        saveClick();
    }

    public void saveClick()
    {
        if (itemList.size() == 0)
        {
            Toast.makeText(mContext, "Please add atleast one item", Toast.LENGTH_SHORT).show();
            return;
        }

        invoiceDate = invoice_date.getText().toString().trim();
        timestamp = System.currentTimeMillis();
        timestampString = String.valueOf(timestamp);
        if (customerView)
        {
            if (edt_newCustomerName.getText().toString().trim().isEmpty())
            {
                Toast.makeText(mContext, "Please fill customer name", Toast.LENGTH_SHORT).show();
                return;
            }
            else
            {
                newCustomerName = edt_newCustomerName.getText().toString().trim();
            }
            if (edt_newCustomerAddress.getText().toString().trim().isEmpty())
            {
                Toast.makeText(mContext, "Please fill customer address", Toast.LENGTH_SHORT).show();
                return;
            }
            else
            {
                newCustomerAddress = edt_newCustomerAddress.getText().toString().trim();
            }
            if (!edt_newCustomerGst.getText().toString().trim().isEmpty())
            {
                newCustomerGstNumber = edt_newCustomerGst.getText().toString().trim();
            }
        }
        else
        {
            if (customerNameList.size() == 0)
            {
                Toast.makeText(mContext, "Please add new customer", Toast.LENGTH_SHORT).show();
            }
            newCustomerName = customersList.get(customerSpinnerValue).getCustomerName();
            newCustomerAddress = customersList.get(customerSpinnerValue).getCustomerAddress();
            if (!customersList.get(customerSpinnerValue).getCustomerGstNumber().isEmpty())
            {
                newCustomerGstNumber = customersList.get(customerSpinnerValue).getCustomerGstNumber();
            }

        }

        sendDatatoPreview();
    }

    private void sendDatatoPreview()
    {
        Intent intent = new Intent(mContext, PreviewActivity.class);
        intent.putParcelableArrayListExtra("ItemList",itemList);
        intent.putExtra("Customer Name",newCustomerName);
        intent.putExtra("Customer Address",newCustomerAddress);
        intent.putExtra("Customer GST Number",newCustomerGstNumber);
        intent.putExtra("Invoice Date",invoiceDate);
        intent.putExtra("showSave",true);
        startActivity(intent);
    }
}
