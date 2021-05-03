package com.example.finalproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private EditText etLongitude, etLatitude, etName;
    private RecyclerView rvLocation;
    private List<Location> locationList = new ArrayList<>();
    private LocationAdapter locationAdapter;
    String URL = "content://com.example.finalproject/locations";
    String URL2 = "content://com.example.finalproject/nearBy";
    Uri locations = Uri.parse(URL);
    Uri nearBy = Uri.parse(URL2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();

        rvLocation.setLayoutManager(new LinearLayoutManager(this));
        locationAdapter = new LocationAdapter(this);
        rvLocation.setAdapter(locationAdapter);

        //ItemTouchHelper連結RecycleView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(locationAdapter));
        itemTouchHelper.attachToRecyclerView(rvLocation);

//        button add new location
        button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                String longitude = etLongitude.getText().toString().trim();
                String latitude = etLatitude.getText().toString().trim();
                String name = etName.getText().toString().trim();
                if(name.length() <= 0 || longitude.length() <=0 || latitude.length() <=0) {
                    Toast.makeText(getBaseContext(), "Name or number is invalid", Toast.LENGTH_SHORT).show();
                    return;
                }

                ContentValues values = new ContentValues();
                values.put(MyContentProvider.COL_longitude, longitude);
                values.put(MyContentProvider.COL_latitude, latitude);
                values.put(MyContentProvider.COL_name, name);
                Uri uri = getContentResolver().insert(locations, values);
                Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_SHORT).show();

//                rvLocation.setAdapter(locationAdapter);
                refresh();
            }
        });
    }

    private void findViews(){
        button = (Button) findViewById(R.id.button);
        etLongitude = (EditText) findViewById(R.id.etLongitude);
        etLatitude = (EditText) findViewById(R.id.etLatitude);
        etName = (EditText) findViewById(R.id.etName);
        rvLocation = (RecyclerView) findViewById(R.id.rvLocation);
    }

    //RecyclerView實作
    private class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> implements ItemMoveSwipeListener{
        private Context context;
        private LayoutInflater layoutInflater;

        public LocationAdapter(Context context){
            this.context=context;
            layoutInflater=LayoutInflater.from(context);
            Cursor c = getContentResolver().query(locations, null, null, null, null);
            if(c.moveToFirst()){
                do{
                    locationList.add(new Location(
                            c.getInt(c.getColumnIndex(MyContentProvider.COL_id)),
                            c.getDouble(c.getColumnIndex(MyContentProvider.COL_longitude)),
                            c.getDouble(c.getColumnIndex(MyContentProvider.COL_latitude)),
                            c.getString(c.getColumnIndex(MyContentProvider.COL_name))
                    ));
                }while(c.moveToNext());
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            private TextView tvId, tvLongitude, tvLatitude, tvName;

            public ViewHolder(View itemView){
                super(itemView);
                tvId = itemView.findViewById(R.id.tvId);
                tvLongitude = itemView.findViewById(R.id.tvLongitude);
                tvLatitude = itemView.findViewById(R.id.tvLatitude);
                tvName = itemView.findViewById(R.id.tvName);

                itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Uri query = ContentUris.withAppendedId(nearBy, locationList.get(getAdapterPosition()).getId());
                        Cursor c = getContentResolver().query(query, null, null, null, null);
                        if(c.moveToFirst()){
                            Toast.makeText(getBaseContext(), c.getString(c.getColumnIndex(MyContentProvider.COL_id)), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getBaseContext(), c.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View view){
                        Location location;
                        Uri query = ContentUris.withAppendedId(locations, locationList.get(getAdapterPosition()).getId());
                        Cursor c = getContentResolver().query(query, null, null, null, null);
                        if(c.moveToFirst()){
                            location = new Location(
                                    c.getColumnIndex(MyContentProvider.COL_id),
                                    c.getDouble(c.getColumnIndex(MyContentProvider.COL_longitude)),
                                    c.getDouble(c.getColumnIndex(MyContentProvider.COL_latitude)),
                                    c.getString(c.getColumnIndex(MyContentProvider.COL_name))
                            );
                            Uri gmmIntentUri = Uri.parse("geo:0.0?q=" + location.getLatitude() + "," + location.getLongitude());
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        }
                        return true;
                    }
                });
            }

            public TextView getTvId(){
                return tvId;
            }

            public TextView getTvLongitude(){
                return tvLongitude;
            }

            public TextView getTvLatitude(){
                return tvLatitude;
            }

            public TextView getTvName(){
                return tvName;
            }
        }

        @Override
        public int getItemCount(){
            return locationList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            View itemView = layoutInflater.inflate(
                    R.layout.recyclerview_item, viewGroup, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position){
            Location location = locationList.get(position);
            viewHolder.getTvId().setText(String.valueOf(location.getId()));
            viewHolder.getTvLongitude().setText(String.valueOf(location.getLongitude()));
            viewHolder.getTvLatitude().setText(String.valueOf(location.getLatitude()));
            viewHolder.getTvName().setText(String.valueOf(location.getName()));
        }

        //實作ItemMoveSwipeListener interface
        @Override
        public boolean onItemMove(int fromPosition, int toPosition){
            return false;
        }

        @Override
        public void onItemSwipe(final int position){
            new AlertDialog.Builder(context)
                    .setTitle("確認方塊")
                    .setMessage("確認要刪除嗎?")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            rvLocation.setAdapter(locationAdapter);
                        }
                    })
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ContentValues values = new ContentValues();
                            Uri delete = ContentUris.withAppendedId(locations, locationList.get(position).getId());
                            int count = getContentResolver().delete(delete, null, null);
                            Toast.makeText(getBaseContext(), String.valueOf(count), Toast.LENGTH_SHORT).show();
                            refresh();
                        }
                    }).show();
        }
    }

    //swipe效果
    public interface ItemMoveSwipeListener{
        boolean onItemMove(int fromPosition, int toPosition);
        void onItemSwipe(int position);
    }

    public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
        private ItemMoveSwipeListener itemMoveSwipeListener;

        public ItemTouchHelperCallback(ItemMoveSwipeListener itemMoveSwipeListener){
            this.itemMoveSwipeListener = itemMoveSwipeListener;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder){
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target){
            return itemMoveSwipeListener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction){
            itemMoveSwipeListener.onItemSwipe(viewHolder.getAdapterPosition());
        }
    }

    private void refresh(){
        finish();
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
