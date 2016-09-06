package nuvoex.com.barcode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.zxing.BarcodeFormat;

import java.util.ArrayList;
import java.util.Arrays;

import nuvoex.com.scanner.BarCodeActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan();
            }
        });
        findViewById(R.id.rescan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rescan();
            }
        });
    }

    private void scan() {
        Intent intent = new Intent(this, BarCodeActivity.class);
        ArrayList<String> barcodes = new ArrayList<>();
        barcodes.add("1233");
        barcodes.add("4567");
        barcodes.add("7898");
        intent.putStringArrayListExtra(BarCodeActivity.BUNDLE_PREFETCH_BARCODE_LIST, barcodes);
        startActivityForResult(intent, 0);
    }

    private void rescan() {
        Intent intent = new Intent(this, BarCodeActivity.class);
        ArrayList<String> barcodes = new ArrayList<>();
        barcodes.add("4567");
        barcodes.add("7898");
        intent.putStringArrayListExtra(BarCodeActivity.BUNDLE_PREFETCH_BARCODE_LIST, barcodes);
        ArrayList<String> scanned = new ArrayList<>();
        scanned.add("1233");
        intent.putStringArrayListExtra(BarCodeActivity.BUNDLE_SCANNED_BARCODE_LIST, scanned);
        startActivityForResult(intent, 0);
    }
}
