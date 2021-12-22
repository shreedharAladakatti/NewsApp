package com.aladakatti.hms_account;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.hmsscankit.WriterException;
import com.huawei.hms.ml.scan.HmsBuildBitmapOption;
import com.huawei.hms.ml.scan.HmsScan;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    String email, pic, name;
    TextView user_name, user_email;
    ImageView imageView;
    String content = "Add your appgallery link in this string";
    int type = HmsScan.QRCODE_SCAN_TYPE;
    int width = 200;
    int height = 200;
    ImageView iv_barCode;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();
        toolbar.setTitle("Profile");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed();
            }
        });
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            email = bundle.getString("email");
            pic = bundle.getString("pic");
            name = bundle.getString("name");
        }
        user_email.setText(email);
        user_name.setText(name);

        Glide
                .with(this)
                .load(pic)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(imageView);
        shareApp();

    }

    public void init(){
        user_name = findViewById(R.id.user_name);
        user_email = findViewById(R.id.user_email);
        imageView = findViewById(R.id.imageView);
        iv_barCode = findViewById(R.id.iv_barCode);
        toolbar = findViewById(R.id.toolbar);
    }

    public void shareApp(){
        /*
        Fore QR Code generation - https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/scan-generate-barcode-0000001050995005
        * */

        HmsBuildBitmapOption options = new HmsBuildBitmapOption
                .Creator()
                .setBitmapBackgroundColor(Color.WHITE)
                .setBitmapColor(Color.BLACK)
                .setBitmapMargin(2)
                .create();

        try {
            // If the HmsBuildBitmapOption object is not constructed, set options to null.
            Bitmap qrBitmap = ScanUtil.buildBitmap(content, type, width, height, options);
            Glide
                    .with(this)
                    .load(qrBitmap)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(iv_barCode);
        } catch (WriterException e) {
            Log.w("buildBitmap", e);
        }
    }
}