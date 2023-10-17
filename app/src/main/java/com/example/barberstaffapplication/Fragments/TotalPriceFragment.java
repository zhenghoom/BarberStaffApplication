package com.example.barberstaffapplication.Fragments;

import static java.util.ResourceBundle.getBundle;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barberstaffapplication.Adapter.MyConfirmShoppingItemAdapter;
import com.example.barberstaffapplication.Common.Common;
import com.example.barberstaffapplication.Model.BarberServices;
import com.example.barberstaffapplication.Model.ShoppingItem;
import com.example.barberstaffapplication.R;
import com.example.barberstaffapplication.Retrofit.IFCMService;
import com.example.barberstaffapplication.Retrofit.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class TotalPriceFragment extends BottomSheetDialogFragment {
    Unbinder unbinder;

    @BindView(R.id.chip_group_services)
    ChipGroup chip_group_services;
    @BindView(R.id.recycler_view_shopping)
    RecyclerView recycler_view_shopping;
    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;
    @BindView(R.id.txt_barber_name)
    TextView txt_barber_name;
    @BindView(R.id.txt_time)
    TextView txt_time;
    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;
    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.btn_confirm)
    Button btn_confirm;

    HashSet<BarberServices> servicesAdded;
    List<ShoppingItem> shoppingItemList;

    IFCMService ifcmService;

    AlertDialog dialog;

    private static TotalPriceFragment instance;

    public static TotalPriceFragment getInstance(){
        return instance==null?new TotalPriceFragment():instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_total_price,container,false);

        unbinder = ButterKnife.bind(this, itemView);

        init();

        initView();

        getBundle(getArguments());

        setInformation();

        return itemView;
    }

    private void setInformation() {
        txt_salon_name.setText(Common.selectedSalon.getName());
        txt_barber_name.setText(Common.currentBarber.getName());
        txt_time.setText(Common.convertTimeSlotToString(Common.currentBookingInformation.getSlot().intValue()));
        txt_customer_name.setText(Common.currentBookingInformation.getCustomerName());
        txt_customer_phone.setText(Common.currentBookingInformation.getCustomerPhone());

        if(servicesAdded.size() > 0)
        {
            //Add to Chip
            int i = 0;
            for(BarberServices services:servicesAdded)
            {
                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.chip_item,null);
                chip.setText(services.getName());
                chip.setTag(i);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        servicesAdded.remove((int)view.getTag());
                        chip_group_services.removeView(view);

                        calculatePrice();
                    }
                });
                chip_group_services.addView(chip);

                i++;
            }
        }
        if(shoppingItemList.size() > 0)
        {
            MyConfirmShoppingItemAdapter adapter = new MyConfirmShoppingItemAdapter(getContext(),shoppingItemList);
            recycler_view_shopping.setAdapter(adapter);
        }
        calculatePrice();
    }

    private void calculatePrice() {
        double price = Common.DEFAULT_PRICE;
        for(BarberServices services:servicesAdded)
            price+=services.getPrice();
        for(ShoppingItem shoppingItem:shoppingItemList)
            price+=shoppingItem.getPrice();

        txt_total_price.setText(new StringBuilder(Common.MONEY_SIGN)
                .append(price));

    }

    private void getBundle(Bundle arguments) {
        this.servicesAdded = new Gson()
                .fromJson(arguments.getString(Common.SERVICES_ADDED),
                        new TypeToken<HashSet<BarberServices>>(){}.getType());

        this.shoppingItemList = new Gson()
                .fromJson(arguments.getString(Common.SHOPPING_LIST),
                        new TypeToken<List<ShoppingItem>>(){}.getType());
    }

    private void initView() {
        recycler_view_shopping.setHasFixedSize(true);
        recycler_view_shopping.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
    }

    private void init(){
        dialog = new SpotsDialog.Builder().setContext(getContext())
                .setCancelable(false).build();
        ifcmService = RetrofitClient.getInstance().create(IFCMService.class);
    }
}
