package com.example.barberstaffapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.barberstaffapplication.Adapter.MyTimeSlotAdapter;
import com.example.barberstaffapplication.Common.Common;
import com.example.barberstaffapplication.Common.SpacesItemDecoration;
import com.example.barberstaffapplication.Interface.INotificationCountListener;
import com.example.barberstaffapplication.Interface.ITimeSlotLoadListener;
import com.example.barberstaffapplication.Model.BookingInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class StaffHomeActivity extends AppCompatActivity implements ITimeSlotLoadListener, INotificationCountListener {

    TextView txt_barber_name;

    @BindView(R.id.activity_main)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    ActionBarDrawerToggle actionBarDrawerToggle;

    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;
    AlertDialog dialog;
    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;

    TextView txt_notification_badge;

    CollectionReference notificationCollection;
    CollectionReference currentBookingDateCollection;

    EventListener<QuerySnapshot> notificationEvent;
    EventListener<QuerySnapshot> bookingEvent;

    ListenerRegistration notificationListener;
    ListenerRegistration bookingRealtimeListener;

    INotificationCountListener iNotificationCountListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        ButterKnife.bind(this);

        init();
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        if(item.getItemId() == R.id.action_new_notification)
        {
            startActivity(new Intent(StaffHomeActivity.this, NotificationActivity.class));
            txt_notification_badge.setText("");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.open,
                R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.menu_exit)
                    logOut();
                return true;
            }
        });

        View headerView = navigationView.getHeaderView(0);
        txt_barber_name = (TextView)headerView.findViewById(R.id.txt_barber_name);
        txt_barber_name.setText(Common.currentBarber.getName());

        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this)
                .build();

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                Common.simpleDateFormat.format(date.getTime()));

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));

        //Calendar
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 2);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .configure()
                .end()
                .build();
        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.bookingDate = date;
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                            Common.simpleDateFormat.format(date.getTime()));
                }
            }
        });
    }

    private void logOut() {
        //Delete all saved keys and start MainActivity
        Paper.init(this);
        Paper.book().delete(Common.SALON_KEY);
        Paper.book().delete(Common.BARBER_KEY);
        Paper.book().delete(Common.STATE_KEY);
        Paper.book().delete(Common.LOGGED_KEY);

        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout ?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent mainActivity = new Intent(StaffHomeActivity.this, MainActivity.class);
                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainActivity);
                        finish();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private void loadAvailableTimeSlotOfBarber(String barberId, String bookDate) {
        dialog.show();


        //Get information of barber
        barberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) //if barber is available
                    {
                        //get information of booking
                        //if not created, then return empty
                        CollectionReference date = FirebaseFirestore.getInstance()
                                .collection("AllSalon")
                                .document(Common.state_name)
                                .collection("Branch")
                                .document(Common.selectedSalon.getSalonId())
                                .collection("Barber")
                                .document(Common.currentBarber.getBarberId())
                                .collection(bookDate);//bookDate is date simple format with dd_MM_yyyy = 22_09_2023

                        date.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot.isEmpty()) // if dont have any appointment
                                        iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                    else {
                                        //if have appointment
                                        List<BookingInformation> timeSlots = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult())
                                            timeSlots.add(document.toObject(BookingInformation.class));
                                        iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage());
                            }
                        });
                    }
                }
            }
        });
    }

    private void init(){
        iTimeSlotLoadListener = this;
        iNotificationCountListener = this;
        initNotificationRealtimeUpdate();
        initBookingRealtimeUpdate();
    }

    private void initBookingRealtimeUpdate() {
        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selectedSalon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId());

        //Get current date
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        bookingEvent = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                //If have new booking, update adapter
                loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                        Common.simpleDateFormat.format(date.getTime()));
            }
        };
        currentBookingDateCollection = barberDoc.collection(Common.simpleDateFormat.format(date.getTime()));
        bookingRealtimeListener = currentBookingDateCollection.addSnapshotListener(bookingEvent);

    }

    private void initNotificationRealtimeUpdate() {
        notificationCollection = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selectedSalon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId())
                .collection("Notifications");

        notificationEvent = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.size() > 0)
                    loadNotification();
            }
        };
        notificationListener = notificationCollection.whereEqualTo("read",false) //Only retrieve notification "false"
                .addSnapshotListener(notificationEvent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit ?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(StaffHomeActivity.this, "Fake Function Exit", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    @Override
    public void onTimeSlotLoadSuccess(List<BookingInformation> timeSlot) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this,timeSlot);
        recycler_time_slot.setAdapter(adapter);

        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(this, ""+message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this);
        recycler_time_slot.setAdapter(adapter);

        dialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.staff_home_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.action_new_notification);

        txt_notification_badge = (TextView) menuItem.getActionView()
                .findViewById(R.id.notification_badge);

        loadNotification();

        menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(menuItem);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void loadNotification() {
        notificationCollection.whereEqualTo("read",false)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StaffHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            iNotificationCountListener.onNotificationCountSuccess(task.getResult().size());
                        }
                    }
                });
    }

    @Override
    public void onNotificationCountSuccess(int count) {
        if(count == 0)
            txt_notification_badge.setVisibility(View.INVISIBLE);
        else
        {
            txt_notification_badge.setVisibility(View.VISIBLE);
            if(count <= 9)
                txt_notification_badge.setText(String.valueOf(count));
            else
                txt_notification_badge.setText("9+");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBookingRealtimeUpdate();
        initNotificationRealtimeUpdate();
    }

    @Override
    protected void onStop() {
        if(notificationListener != null)
            notificationListener.remove();
        if(bookingRealtimeListener != null)
            bookingRealtimeListener.remove();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(notificationListener != null)
            notificationListener.remove();
        if(bookingRealtimeListener != null)
            bookingRealtimeListener.remove();
        super.onDestroy();
    }
}