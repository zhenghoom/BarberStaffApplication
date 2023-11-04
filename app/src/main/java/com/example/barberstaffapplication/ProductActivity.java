package com.example.barberstaffapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.barberstaffapplication.Adapter.MyShoppingItemAdapter;
import com.example.barberstaffapplication.Adapter.MyShoppingItemProductAdapter;
import com.example.barberstaffapplication.Common.SpacesItemDecoration;
import com.example.barberstaffapplication.Interface.IOnShoppingItemSelected;
import com.example.barberstaffapplication.Interface.IShoppingDataLoadListener;
import com.example.barberstaffapplication.Model.ShoppingItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends AppCompatActivity implements IShoppingDataLoadListener, IOnShoppingItemSelected {


    ChipGroup chipGroup;
    Chip chipWax,chipSpray,chip_hair_care,chip_body_care;
    CollectionReference shoppingItemRef , itemRef;

    IShoppingDataLoadListener iShoppingDataLoadListener;
    IOnShoppingItemSelected callBackToActivity;
    FloatingActionButton addButton, deleteButton;
    MaterialSpinner spinner, spinnerProductCat, spinnerProductList;
    TextView product_name, product_price, product_image_link;
    AppCompatButton btn_submit, btn_cancel, btn_delete;
    ImageView img;

    RecyclerView recycler_items;

    List<ShoppingItem> shoppingItemList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        chipGroup = findViewById(R.id.chip_group);
        chipWax = findViewById(R.id.chip_wax);
        chipSpray = findViewById(R.id.chip_spray);
        chip_hair_care = findViewById(R.id.chip_hair_care);
        chip_body_care = findViewById(R.id.chip_body_care);
        recycler_items = findViewById(R.id.recycler_items);
        addButton = findViewById(R.id.fab_add);
        deleteButton = findViewById(R.id.fab_delete);

        chipWax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedChip(chipWax);
                loadShoppingItem("Wax");
            }
        });
        chipSpray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedChip(chipSpray);
                loadShoppingItem("Spray");
            }
        });
        chip_body_care.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedChip(chip_body_care);
                loadShoppingItem("Body Care");
            }
        });
        chip_hair_care.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedChip(chip_hair_care);
                loadShoppingItem("Hair Care");
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct();
            }
        });
        
        loadShoppingItem("Wax");

        init();

        initView();
    }

    private void setSelectedChip(Chip chip) {
        //Set color
        for(int i=0;i<chipGroup.getChildCount();i++)
        {
            Chip chipItem = (Chip) chipGroup.getChildAt(i);
            if(chipItem.getId() != chip.getId()) //If not selected
            {
                chipItem.setChipBackgroundColorResource(android.R.color.darker_gray);
                chipItem.setTextColor(getResources().getColor(android.R.color.white));
            }
            else //If selected
            {
                chipItem.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
                chipItem.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }
    private void initView() {
        recycler_items.setHasFixedSize(true);
        recycler_items.setLayoutManager(new GridLayoutManager(getBaseContext(), 2));
        recycler_items.addItemDecoration(new SpacesItemDecoration(8));
    }

    private void init(){
        iShoppingDataLoadListener = this;
    }

    private void loadShoppingItem(String itemMenu) {
        shoppingItemRef = FirebaseFirestore.getInstance().collection("Shopping")
                .document(itemMenu)
                .collection("Items");
        //get data
        shoppingItemRef.get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iShoppingDataLoadListener.onShoppingDataLoadFailed(e.getMessage());
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            List<ShoppingItem> shoppingItems = new ArrayList<>();
                            for(DocumentSnapshot itemSnapshot: task.getResult())
                            {
                                ShoppingItem shoppingItem = itemSnapshot.toObject(ShoppingItem.class);
                                shoppingItem.setId(itemSnapshot.getId());
                                shoppingItems.add(shoppingItem);
                            }
                            iShoppingDataLoadListener.onShoppingDataLoadSuccess(shoppingItems);
                        }
                    }
                });
    }

    private void addProduct(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProductActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View add_product_layout = inflater.inflate(R.layout.layout_add_product,null);
        product_name = add_product_layout.findViewById(R.id.product_name);
        product_price = add_product_layout.findViewById(R.id.product_price);
        product_image_link = add_product_layout.findViewById(R.id.product_image_link);
        btn_submit = add_product_layout.findViewById(R.id.btn_submit);
        btn_cancel = add_product_layout.findViewById(R.id.btn_cancel);
        img = add_product_layout.findViewById(R.id.img);
        spinner = add_product_layout.findViewById(R.id.spinner);

        product_image_link.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (product_image_link.getText().toString().length() > 0){
                    Picasso.get().load(product_image_link.getText().toString()).into(img, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load("https://cdn.pixabay.com/photo/2017/03/28/01/42/attention-2180765_1280.png").into(img);
                        }
                    });
                }

            }
        });

        alertDialog.setView(add_product_layout);
        final AlertDialog show = alertDialog.show();
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show.dismiss();
            }
        });

        FirebaseFirestore.getInstance()
                .collection("Shopping")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> list = new ArrayList<>();
                            list.add("Product Category");
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                                list.add(documentSnapshot.getId());
                            spinner.setItems(list);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemRef = FirebaseFirestore.getInstance()
                        .collection("Shopping")
                        .document((String) spinner.getItems().get(spinner.getSelectedIndex()))
                        .collection("Items");

                ShoppingItem shoppingItem = new ShoppingItem();
                if (product_image_link.getText().toString().isEmpty() || product_name.getText().toString().isEmpty()
                        || product_price.getText().toString().isEmpty())
                {
                    Toast.makeText(ProductActivity.this, "All fields must not be empty!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    shoppingItem.setImage(product_image_link.getText().toString());
                    shoppingItem.setName(product_name.getText().toString());
                    shoppingItem.setPrice(Long.parseLong(product_price.getText().toString()));
                    itemRef.document()
                            .set(shoppingItem)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getBaseContext(), "Product Added!", Toast.LENGTH_SHORT).show();
                                    show.dismiss();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                }
            }
        });
    }

    private void deleteProduct(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProductActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View delete_product_layout = inflater.inflate(R.layout.layout_delete_product,null);
        btn_delete = delete_product_layout.findViewById(R.id.btn_delete);
        btn_cancel = delete_product_layout.findViewById(R.id.btn_cancel);
        spinnerProductCat = delete_product_layout.findViewById(R.id.spinnerProductCat);
        spinnerProductList = delete_product_layout.findViewById(R.id.spinnerProductList);
        alertDialog.setView(delete_product_layout);

        final AlertDialog show = alertDialog.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show.dismiss();
            }
        });

        FirebaseFirestore.getInstance()
                .collection("Shopping")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> list = new ArrayList<>();
                            list.add("Product Category");
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                                list.add(documentSnapshot.getId());
                            spinnerProductCat.setItems(list);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        spinnerProductCat.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                FirebaseFirestore.getInstance()
                        .collection("Shopping")
                        .document(item.toString())
                        .collection("Items")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful())
                                {

                                    List<String> shoppingItems = new ArrayList<>();
                                    shoppingItems.add("Product List");
                                    for(QueryDocumentSnapshot documentSnapshot: task.getResult())
                                    {
                                        ShoppingItem shoppingItem = documentSnapshot.toObject(ShoppingItem.class);
                                        shoppingItem.setId(documentSnapshot.getId());
                                        shoppingItemList.add(shoppingItem);
                                        shoppingItems.add((String) documentSnapshot.get("name"));
                                    }
                                    spinnerProductList.setItems(shoppingItems);
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String productCat = (String) spinnerProductCat.getItems().get(spinnerProductCat.getSelectedIndex());
                String productName = (String) spinnerProductList.getItems().get(spinnerProductList.getSelectedIndex());
                String productId = "";

                for (int i=0;i<shoppingItemList.size();i++){
                    if(shoppingItemList.get(i).getName().equals(productName))
                    {
                        productId = shoppingItemList.get(i).getId();
                    }
                }
                FirebaseFirestore.getInstance()
                        .collection("Shopping")
                        .document(productCat)
                        .collection("Items")
                        .document(productId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(ProductActivity.this, productName + " Removed!", Toast.LENGTH_SHORT).show();
                                show.dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                show.dismiss();
                            }
                        });
            }
        });

    }

    @Override
    public void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList) {
        MyShoppingItemProductAdapter adapter = new MyShoppingItemProductAdapter(getBaseContext(), shoppingItemList,this);
        recycler_items.setAdapter(adapter);
    }

    @Override
    public void onShoppingDataLoadFailed(String message) {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem) {
        callBackToActivity.onShoppingItemSelected(shoppingItem);

    }

}