package com.example.barberstaffapplication;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.barberstaffapplication.Common.Common;
import com.example.barberstaffapplication.Model.CartItem;
import com.example.barberstaffapplication.Model.Invoice;
import com.example.barberstaffapplication.Model.ShoppingItem;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DashboardActivity extends AppCompatActivity {

     HashMap<String, Integer> productNameToCount = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        getProduct();

        setTitle("Dashboard");

    }

    private void getProduct()
    {
        ArrayList<String> xValues = new ArrayList<String>();
        ArrayList<String> productName = new ArrayList<String>();

        BarChart barChart = findViewById(R.id.chart1);
        barChart.getAxisRight().setDrawLabels(false);

//        /AllSalon/Kedah/Branch/e6bTnUKZe9UhHos5PKNy/Invoices/2kuLQ8f77i82LxEIbJp1
        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selectedSalon.getSalonId())
                .collection("Invoices")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Invoice invoice = document.toObject(Invoice.class);
                            List<CartItem> shoppingItemList = invoice.getShoppingItemList();


                            for(int i=0;i< shoppingItemList.size();i++){
                                String currentProductName = shoppingItemList.get(i).getProductName();
                                Integer currentProductQty = shoppingItemList.get(i).getProductQuantity();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    productNameToCount.put(currentProductName
                                            ,productNameToCount.getOrDefault(currentProductName,0)+currentProductQty);
                                    //productNameToCount is to calculate the total quantity of an item occured in Invoice
                                    addUniqueProduct(xValues,currentProductName);

                                }

                            }

                        }
                        //HERE DISPLAY TOTAL OF QUANTITY OF EACH ITEM
                        //PASS KEY AND VALUE TO HERE
                        ArrayList<BarEntry> product = new ArrayList<>();
                        for(int i=0;i< xValues.size();i++){
                            //add String stored in xValues into productName
                            productName.add(xValues.get(i));
                            //pass product name and productQty based on productName
                            product.add(new BarEntry(i,productNameToCount.get(productName.get(i))));

                        }

                        BarDataSet barDataSet = new BarDataSet(product, "Product Name");
                        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                        barDataSet.setValueTextSize(10f);

                        BarData barData = new BarData(barDataSet);
                        barChart.setData(barData);
                        barChart.setFitBars(true);
                        barChart.getDescription().setEnabled(false);
                        barChart.invalidate();
                        barChart.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                        XAxis xAxis = barChart.getXAxis();
                        xAxis.setLabelRotationAngle(270);
                        YAxis yAxis = barChart.getAxisLeft();
                        yAxis.setAxisMaximum(0);
                        yAxis.setAxisMaximum(20);
                        yAxis.setAxisLineWidth(2f);
                        yAxis.setAxisLineColor(android.R.color.black);
                        yAxis.setLabelCount(10);
                        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(productName));
                        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                        barChart.setVisibleXRangeMaximum(10);
                        barChart.getXAxis().setGranularity(1);
                        barChart.getXAxis().setGranularityEnabled(true);
                        barChart.notifyDataSetChanged();


                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());

                    }
                    //Revise from qty of product from productNameToCount
//                    Log.d(TAG,"TESTING" + productNameToCount);

                }
            });

    }
    private void addUniqueProduct(ArrayList<String>list, String product)
    {
        if (!list.contains(product))
        {
            list.add(product);
        }
    }

}