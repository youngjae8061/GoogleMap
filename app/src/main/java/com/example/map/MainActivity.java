package com.example.map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener{

    private GoogleMap googleMap;
    Intent intent;
    double latitude_intent, longitude_intent, latitude, longitude;
    LatLng currentPosition;
    Button btn_review, btn_sch;
    EditText edt_sch;

    GeoPoint gp;
    MarkerOptions markerOptions;
    FirebaseFirestore db; //파이어베이스 인스턴스

    // 테스트중 ===============================================================================================================================================================
    //List<String> items = new ArrayList<>();
    SearchView searchView;
    TextView resultTextView;
    TextView spot_name, review;

    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;

    List<String> searchList;

    FirebaseStorage storage = FirebaseStorage.getInstance();

    private ClusterManager<MyItem> mClusterManager;

    /*List<Review> dataList = new ArrayList<>();
    RecyclerView mRecyclerView;
    Context context;
    RecyclerAdapter adapter;
    //layout manager for recyclerview
    RecyclerView.LayoutManager layoutManager;
    */// 테스트중 ===============================================================================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        intent = getIntent();
        latitude_intent = intent.getDoubleExtra("latitude", 0);
        longitude_intent = intent.getDoubleExtra("longitude", 0);
        btn_review = (Button)findViewById(R.id.btn_review);
        btn_sch = (Button)findViewById(R.id.btn_sch);
        edt_sch = (EditText)findViewById(R.id.edt_sch);

        db = FirebaseFirestore.getInstance();

        // 테스트중 ===============================================================================================================================================================
        searchView = (SearchView)findViewById(R.id.search_view);
        resultTextView = (TextView)findViewById(R.id.textView5);
        //resultTextView.setText(getResult());
        searchList = new ArrayList<>();
        spot_name = (TextView)findViewById(R.id.textView);
        review = (TextView)findViewById(R.id.rowCountTextView);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapter(searchList);

        getData();

//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recyclerAdapter.getFilter().filter(newText);
                return false;
            }
        });

        /*//view 활성화
        mRecyclerView = findViewById(R.id.recyclerView);

        //set recyclerview properties
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        //adapter
        adapter = new RecyclerAdapter(MainActivity.this, dataList, context);


        edt_sch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });
        items = new ArrayList<>();
        */// 테스트중 ===============================================================================================================================================================


        btn_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_rv = new Intent(MainActivity.this, ReviewActivity.class);
                intent_rv.putExtra("latitude", latitude_intent);
                intent_rv.putExtra("longitude", longitude_intent);
                startActivity(intent_rv);
            }
        });

        btn_sch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleMap.clear();
                final String sch = edt_sch.getText().toString();
                if(sch.length() >0 ){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("review")
                            .whereEqualTo("spot_name", sch)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            gp = (GeoPoint)document.get("map");

                                            LatLng latLng = new LatLng(gp.getLatitude(), gp.getLongitude());
                                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));    // 화면이 바라볼 곳은 latlng이다.
                                            googleMap.moveCamera(CameraUpdateFactory.zoomTo(12));        // 화면은 15만큼 당겨라?  단계는 1~21까지 있음 숫자가 클수록 자세함
                                            markerOptions = new MarkerOptions().position(latLng).title(String.valueOf(document.get("spot_name")));
                                            googleMap.addMarker(markerOptions);


                                            mClusterManager = new ClusterManager<>(MainActivity.this, googleMap);
                                            googleMap.setOnCameraIdleListener(mClusterManager);
                                            googleMap.setOnMarkerClickListener(mClusterManager);
                                            addItems(gp);
                                        }
                                    } else {
                                        startToast("일치하는 결과가 없습니다.");
                                    }
                                }
                            });
                }else startToast("검색할 과목을 입력해주세요.");
            }
        });
    }//onCreate()


    /*// 테스트중 ===============================================================================================================================================================
    private void filter(String text) {
        List<Review> filteredList = new ArrayList<>();

        for (Review r : dataList) {
            //if (review.getSpot_name().toLowerCase().contains(text.toLowerCase())) {
            filteredList.add(r);
            //}
        }
        adapter.filterList(filteredList);
    }
    */// 테스트중 ===============================================================================================================================================================


    //review 전체 데이터 가져오기
    private void getData(){
        db.collection("review")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String a = document.getString("spot_name");
                                searchList.add(a);
                            }
                        } else {
                            //실패했을경우
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //called when there is any error while retriving
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();  //변경된 위도
        longitude = location.getLongitude();//변경된 경도
        //새로운 현재 좌표
        currentPosition = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setIndoorEnabled(true);   //실내에서작동
        googleMap.setBuildingsEnabled(true);//건물표시
        googleMap.getUiSettings().setZoomControlsEnabled(true);//UI에서 Zoom 컨트롤하겠다.

        //현재위치에서 시작
        LatLng latLng = new LatLng(latitude_intent, longitude_intent);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));    // 화면이 바라볼 곳은 latlng이다.
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(15));        // 화면은 15만큼 당겨라?  단계는 1~21까지 있음 숫자가 클수록 자세함
        //MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("홍대입구역");
        //googleMap.addMarker(markerOptions);

        //위치정보 허용했을 때
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);//UI에서 Zoom 컨트롤하겠다.
            /*
            //현재위치 관리하는 매니저
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //리스너 등록
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (android.location.LocationListener) this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, (android.location.LocationListener) this);
            //gps가 꺼져있을수도있으므로 수동위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            //최근 gps좌표를 저장
            if(lastKnownLocation != null){
                //최근 좌표가 있을경우
                latitude = lastKnownLocation.getLatitude();
                longitude = lastKnownLocation.getLongitude();
            }else{
                latitude = 37.5796212;
                longitude = 126.9748523;
            }*/



        } else {    //위치정보 허용하지 않았을 때
            checkLocationPermissionWithRationale();
        }
    }

    private void addItems(GeoPoint geo){
        /*double lat = 37.5172f, lng = 127.0473f; //서울 강남
        for(int i =0 ; i< 10; i++){
            double offset = i/60d;
            lat = lat+offset;
            lng = lng+offset;
            MyItem offsetItem = new MyItem(lat, lng);
            mClusterManager.addItem(offsetItem);
        }*/
        db.collection("review")
                .whereEqualTo("map", geo)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                startToast(String.valueOf(document.get("spot_name")));
                                GeoPoint gp1 = (GeoPoint)document.get("map");
                                double lat = gp1.getLatitude(), lng = gp1.getLongitude(); //서울 강남
                                    MyItem offsetItem = new MyItem(lat, lng);
                                    mClusterManager.addItem(offsetItem);
                            }
                        } else {
                            startToast("사진을 못가져왔어요 ㅠ");
                        }
                    }
                });
        mClusterManager.cluster();
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermissionWithRationale() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("위치정보")
                        .setMessage("이 앱을 사용하기 위해서는 위치정보에 접근이 필요합니다. 위치정보 접근을 허용하여 주세요.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void startToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
}