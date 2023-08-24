package com.svalero.cybershopapp.view;

import static com.svalero.cybershopapp.database.Constants.DATABASE_CLIENTS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.svalero.cybershopapp.MapsActivity;
import com.svalero.cybershopapp.R;
import com.svalero.cybershopapp.contract.ClientDetailsContract;
import com.svalero.cybershopapp.database.AppDatabase;
import com.svalero.cybershopapp.domain.Client;
import com.svalero.cybershopapp.presenter.ClientDetailsPresenter;

import java.sql.Date;
import java.util.Locale;

public class ClientDetailsView extends AppCompatActivity implements ClientDetailsContract.View {

    private ClientDetailsPresenter presenter;
    private MapView mapView;
    private PointAnnotationManager pointAnnotationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_details);

        presenter = new ClientDetailsPresenter(this);
        long clientId = getIntent().getLongExtra("client_id", -1);
        if (clientId != -1) {
            presenter.loadClientById(clientId);
        }

        mapView = findViewById(R.id.mapping);
    }
    @Override
    public void showClientDetails(Client client) {
        ImageView imageView = findViewById(R.id.clientPhoto);
        TextView tvName = findViewById(R.id.clientName);
        TextView tvSurname = findViewById(R.id.clientSurname);
        TextView tvNumber = findViewById(R.id.clientNumber);
        TextView tvDate = findViewById(R.id.registered);
        TextView tvStatus = findViewById(R.id.clientStatus);

        tvName.setText(client.getName());
        tvSurname.setText(client.getSurname());
        tvNumber.setText(String.valueOf(client.getNumber()));
        Date registerDate = client.getRegister_date();

        if(registerDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(client.getRegister_date());
            tvDate.setText(formattedDate);
        } else {
            tvDate.setText(R.string.UnknownRegistering);
        }

        boolean isVip = client.isVip();
        tvStatus.setText(isVip ? getString(R.string.isVIP) : getString(R.string.isNotVip));

        byte[] image = client.getImage();

        if (image != null && image.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.person);
        }

        initializePointManager();
        addClientToMap(client);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actonbar_mainmenu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (item.getItemId() == R.id.getMap) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.getPreferences){
            showLanguageSelectionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLanguageSelectionDialog() {
        String[] languages = {"Español", "English"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select language");
        builder.setItems(languages, (dialog, which) ->{
            switch (which){
                case 0:
                    setLocale("es");
                    break;
                case 1:
                    setLocale("en");
                    break;
            }
        });
        builder.create().show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources()
                .getDisplayMetrics());
        recreate();
    }

    private void addClientToMap(Client client) {
        Point clientPoint = Point.fromLngLat(client.getLongitude(), client.getLatitude());
        addMarker(clientPoint, client.getName());

        if (clientPoint != null) {
            setCameraPosition(clientPoint);
        } else {
            setCameraPosition(Point.fromLngLat(-0.8738521, 41.6396971));
        }
    }

    private void initializePointManager() {
        AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
        AnnotationConfig annotationConfig = new AnnotationConfig();
        pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, annotationConfig);
    }

    private void addMarker(Point point, String name) {
        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                .withPoint(point)
                .withTextField(name)
                .withIconImage(BitmapFactory.decodeResource(getResources(), R.mipmap.purple_marker_foreground));
        pointAnnotationManager.create(pointAnnotationOptions);
    }

    private void setCameraPosition(Point point) {
        CameraOptions cameraPosition = new CameraOptions.Builder()
                .center(point)
                .pitch(20.0)
                .zoom(15.5)
                .bearing(-17.6)
                .build();
        mapView.getMapboxMap().setCamera(cameraPosition);
    }


    @Override
    public void showMessage(String message) {

    }
}