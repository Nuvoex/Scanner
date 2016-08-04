package nuvoex.com.scanner;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class BarCodeActivity extends MarshmallowSupportActivity {


    public static final String BUNDLE_BARCODE_LIST = "bundle_barcode_list";
    public static final String BUNDLE_PREFETCH_BARCODE_LIST = "bundle__prefetch_barcode_list";
    public static final String BUNDLE_SCAN_ITEM_COUNT = "bundle_scan_item_count";
    public static final String BUNDLE_SCAN_ITEM_INDICATOR = "bundle_scan_item_indicator";
    private static final int PHOTO_ACTIVITY_REQUEST_CARMERA_AND_READ_WRITE = 50;
    private static final String[] PHOTO_ACTIVITY_CAMERA_PERMISSIONS = {Manifest.permission.CAMERA};
    FrameLayout mScannerContainer;

    Button mDone;

    Toolbar mToolbar;
    EditText mBarCodeEditText;

    TextView mBarcodeCountInfo;

    View flash;

    private ZXingScannerView mScanner;

    List<String> mBarCodeList;
    private int mBarCodeCount;

    ArrayList<String> mPrefetchList;
    private String mItemIndicator;
    private MediaPlayer mediaPlayer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBarCodeCount = getIntent().getIntExtra(BUNDLE_SCAN_ITEM_COUNT, 0);
        mPrefetchList = getIntent().getStringArrayListExtra(BUNDLE_PREFETCH_BARCODE_LIST);
        mItemIndicator = getIntent().getStringExtra(BUNDLE_SCAN_ITEM_INDICATOR);
        if (mPrefetchList != null) {
            mBarCodeCount = mPrefetchList.size();
        }

        setContentView(R.layout.activity_bar_code);
        initView();

        setupToolBar();
        //init barcode list
        mBarCodeList = new ArrayList<>();

        //init barcodeScanner lib
        mScanner = new ZXingScannerView(this);
        mScanner.setAutoFocus(true);
        mScannerContainer.addView(mScanner);

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneButtonClick();
            }
        });

        mBarCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String barcode = v.getText().toString();
                if (!TextUtils.isEmpty(barcode)) {
                    mScanner.stopCameraPreview();
                    addBarcode(barcode);
                    v.setText("");
                }
                return false;
            }
        });
        updateBarcodeInfo();
        if (!hasPermissions(PHOTO_ACTIVITY_CAMERA_PERMISSIONS)) {
            checkPermissionCamera();
        }

        //init Beep sound
        mediaPlayer = MediaPlayer.create(this, R.raw.beep);
    }


    private void initView() {

        mScannerContainer = (FrameLayout) findViewById(R.id.scanner_container);
        mDone = (Button) findViewById(R.id.done_btn);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mBarCodeEditText = (EditText) findViewById(R.id.qrCodeValue);
        mBarcodeCountInfo = (TextView) findViewById(R.id.barcode_count);

        flash = findViewById(R.id.flash);
    }

    @Override
    public void onPermissionGranted(int requestCode, @Nullable FragmentPermissionCallback permissionCallback) {
    }

    private void checkPermissionCamera() {
        requestAppPermissions(PHOTO_ACTIVITY_CAMERA_PERMISSIONS, PHOTO_ACTIVITY_REQUEST_CARMERA_AND_READ_WRITE);
    }

    private void setupToolBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.title_activity_bar_code, mItemIndicator));
        actionBar.setHomeAsUpIndicator(R.drawable.close_icon);
    }

    public void openCamera() {
        mScanner.resumeCameraPreview(mResultHandle);
        mScanner.startCamera();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasPermissions(PHOTO_ACTIVITY_CAMERA_PERMISSIONS)) {
            openCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (hasPermissions(PHOTO_ACTIVITY_CAMERA_PERMISSIONS)) {
            mScanner.stopCamera();
        }

    }

    private ZXingScannerView.ResultHandler mResultHandle = new ZXingScannerView.ResultHandler() {
        @Override
        public void handleResult(Result result) {
            addBarcode(result.getText());
            showFlash();
            playBeepSound();
        }

    };

    private void playBeepSound() {
        mediaPlayer.start();
    }

    private void addBarcode(String barcode) {

        if (!validateBarcode(barcode)) {
            //show dialog
            showInvalidBarCodeDialog(barcode);
            return;
        }

        if (!mBarCodeList.contains(barcode)) {
            mBarCodeList.add(barcode);
        } else {
            Toast.makeText(BarCodeActivity.this, getString(R.string.item_already_scaned), Toast.LENGTH_SHORT).show();
        }

        if (mBarCodeCount == 0 || mBarCodeList.size() < mBarCodeCount) {
            mScanner.resumeCameraPreview(mResultHandle);
            updateBarcodeInfo();
        } else {
            scanComplete();
        }
    }

    private void showInvalidBarCodeDialog(String barcode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_invalid_barcode_title));
        builder.setMessage(getString(R.string.dialog_invalid_barcode_msg, barcode));
        builder.setPositiveButton(getString(R.string.dialog_invalid_barcode_positive_button), null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mScanner.resumeCameraPreview(mResultHandle);
            }
        });

        builder.show();

    }

    private boolean validateBarcode(String barCode) {

        //Check in data
        if (mPrefetchList != null && !mPrefetchList.contains(barCode)) {
            return false;
        }
        //verhoeff check
        return Verhoeff.validateVerhoeff(barCode);
    }

    private void updateBarcodeInfo() {
        if (mBarCodeCount == 0) {
            mBarcodeCountInfo.setText(getString(R.string.scanned_count, mBarCodeList.size()));
        } else {
            mBarcodeCountInfo.setText(getString(R.string.scanned_item_count, mBarCodeList.size() + 1, mBarCodeCount));
        }
        mBarCodeEditText.setHint(getString(R.string.qr_hint, mItemIndicator));
    }

    private void onDoneButtonClick() {
        if (mBarCodeList.size() == 0) {
            Toast.makeText(this, getString(R.string.no_item_scan), Toast.LENGTH_SHORT).show();
            return;
        }
        scanComplete();
    }

    private void scanComplete() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(BUNDLE_BARCODE_LIST, (ArrayList<String>) mBarCodeList);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showFlash() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(flash, "alpha", 1f, 0f);
        anim.setDuration(1500);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                flash.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                flash.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.start();
    }

    @Override
    public void onBackPressed() {
        if (mBarCodeList.size() > 0) {
            scanComplete();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}
