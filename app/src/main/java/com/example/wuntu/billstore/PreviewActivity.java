package com.example.wuntu.billstore;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wuntu.billstore.EventBus.EventClearBill;
import com.example.wuntu.billstore.Pojos.CustomerDetails;
import com.example.wuntu.billstore.Pojos.ItemPojo;
import com.example.wuntu.billstore.Pojos.MakeBillDetails;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PreviewActivity extends AppCompatActivity {

    @BindView(R.id.pdflayout)
    NestedScrollView scrollView;

    public static int REQUEST_PERMISSIONS = 1;
    boolean boolean_permission;
    boolean boolean_save;
    Bitmap bitmap;
    ProgressDialog progressDialog;

    @BindView(R.id.txt_shopName)
    TextView txt_shopName;

    @BindView(R.id.txt_shopAddress)
    TextView txt_shopAddress;

    @BindView(R.id.txt_gstNumber)
    TextView txt_gstNumber;

    @BindView(R.id.txt_invoiceDate)
    TextView txt_invoiceDate;

    @BindView(R.id.txt_custName)
    TextView txt_custName;

    @BindView(R.id.txt_custAddress)
    TextView txt_custAddress;

    @BindView(R.id.txt_custGstNumber)
    TextView txt_custGstNumber;

    @BindView(R.id.tableLayoutItems)
    TableLayout tableLayoutItems;

    @BindView(R.id.invoice_total)
    TextView invoice_total;

    @BindView(R.id.btn_save)
    TextView btn_save;

    @BindView(R.id.btn_print)
    TextView btn_print;

    @BindView(R.id.note)
    TextView note;

    @BindView(R.id.title_note)
    TextView title_note;

    private ArrayList<ItemPojo> itemList;
    private String customerName = "";
    private String customerAddress = "";
    private String customerGstNumber = "";
    private String invoiceDate = "";

    HashMap<String,ItemPojo> billItems;

    private FirebaseFirestore db;
    FirebaseUser firebaseUser;

    long timestamp;
    String timestampString;

    double totalAmount = 0;

    File filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);

        billItems = new HashMap<>();
        db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseUser = firebaseAuth.getCurrentUser();

        timestamp = System.currentTimeMillis();
        timestampString = String.valueOf(timestamp);

        getIntentItems();
        getShopDetails();
        fn_permission();
        initTable();
        setViews();
    }

    private void getShopDetails()
    {
        DocumentReference profileReference = db.collection("Users").document(firebaseUser.getUid());
        profileReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists())
                {
                    Toast.makeText(PreviewActivity.this, "Profile Request Failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                txt_shopName.setText(documentSnapshot.get("shop_name").toString());
                txt_shopAddress.setText(documentSnapshot.get("shop_address").toString());
                txt_gstNumber.setText(documentSnapshot.get("shop_gst_number").toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PreviewActivity.this, "Profile Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getIntentItems()
    {
        if (getIntent() != null)
        {
            itemList = getIntent().getParcelableArrayListExtra("ItemList");
            customerName = getIntent().getStringExtra("Customer Name");
            customerAddress = getIntent().getStringExtra("Customer Address");
            customerGstNumber = getIntent().getStringExtra("Customer GST Number");
            invoiceDate = getIntent().getStringExtra("Invoice Date");
        }
    }

    private void setViews()
    {
        if (itemList.get(0).getNote().matches(""))
        {
            note.setVisibility(View.GONE);
            title_note.setVisibility(View.GONE);
        }
        else
        {
            note.setText(itemList.get(0).getNote());
        }
        String amount = String.valueOf(totalAmount);
        txt_custName.setText(customerName);
        txt_custAddress.setText(customerAddress);
        txt_custGstNumber.setText(customerGstNumber);
        txt_invoiceDate.setText(invoiceDate);
        invoice_total.setText(getResources().getString(R.string.rupee_sign) + amount);

    }

    public void initTable()
    {
        for (int i = 0; i < itemList.size(); i++)
        {
            totalAmount = totalAmount + Double.parseDouble(itemList.get(i).getTotalAmount());

            ItemPojo itemPojo = itemList.get(i);
            TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.invoice_row, tableLayoutItems, false);
            TextView itemName = row.findViewById(R.id.tv0);
            TextView costPerItem= row.findViewById(R.id.tv1);
            TextView quantity = row.findViewById(R.id.tv2);
            TextView totalAmount = row.findViewById(R.id.tv3);

            itemName.setText(itemPojo.getItemName());
            costPerItem.setText(getResources().getString(R.string.rupee_sign) + itemPojo.getCostPerItem());
            quantity.setText(itemPojo.getQuantity());
            totalAmount.setText(getResources().getString(R.string.rupee_sign) + itemPojo.getTotalAmount());

            tableLayoutItems.addView(row);
        }
    }

    @OnClick(R.id.btn_save)
    public void saveButton()
    {
        for (int i = 0;i<itemList.size();i++)
        {
            ItemPojo itemPojo = new ItemPojo(itemList.get(i).getItemName(),itemList.get(i).getCostPerItem(),itemList.get(i).getQuantity(),itemList.get(i).getItemType(),itemList.get(i).getTotalAmount(),itemList.get(i).getNote());
            billItems.put(itemList.get(i).getItemName(),itemPojo);
        }

        final CollectionReference customerReference = db.collection("Users").document(firebaseUser.getUid()).collection("Customers");
        CustomerDetails customerDetails = new CustomerDetails(customerName,customerAddress,customerGstNumber);
        final MakeBillDetails makeBillDetails = new MakeBillDetails(customerDetails, invoiceDate,billItems);

        customerReference.document(customerName).set(customerDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                customerReference.document(customerName).collection(firebaseUser.getUid())
                        .document(invoiceDate + " && " + timestampString).set(makeBillDetails)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(PreviewActivity.this, "Bill added", Toast.LENGTH_SHORT).show();
                                EventBus.getDefault().postSticky(new EventClearBill());
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PreviewActivity.this, "Bill Request Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @OnClick(R.id.btn_print)
    public void printButton()
    {
        if (boolean_permission) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please wait");
            bitmap = loadBitmapFromView(scrollView, scrollView.getWidth(), scrollView.getChildAt(0).getHeight());
            createPdf();
        } else
        {
            fn_permission();
        }
    }

    private void createPdf(){
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float height = displaymetrics.heightPixels ;
        float scrollHeight = scrollView.getChildAt(0).getHeight();
        float width = displaymetrics.widthPixels ;

        int convertHeight = (int) scrollHeight, convertWidth = (int) width;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(convertWidth, convertHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        canvas.drawPaint(paint);


        bitmap = Bitmap.createScaledBitmap(bitmap, convertWidth, convertHeight, true);

        canvas.drawBitmap(bitmap, 0, 0 , null);
        document.finishPage(page);


        // write the document content
        String targetPdf = "/sdcard/"+"test1.pdf";
        String target = Environment.getExternalStorageDirectory().getPath() + "test.pdf";
        filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            boolean_save=true;
            openPdf();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();
    }

    private void openPdf()
    {
        Intent intent;
        File file=new File(Environment.getExternalStorageDirectory()
                + File.separator + "test1.pdf");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            Uri uri = FileProvider.getUriForFile(PreviewActivity.this, getPackageName() + ".provider", file);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/pdf");
            intent = Intent.createChooser(intent, "Open File");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public static Bitmap loadBitmapFromView(View v, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);

        return b;
    }

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);

        } else {
            boolean_permission = true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                boolean_permission = true;

            } else if (permissions.length > 0)
            {
                Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();
            }
        }
    }
}