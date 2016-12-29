package nuvoex.com.scanner;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.nuvoex.library.permission.MarshmallowSupportActivity;
import com.nuvoex.library.permission.Permission;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class BarCodeActivity extends MarshmallowSupportActivity {


    public static final String BUNDLE_BARCODE_LIST = "bundle_barcode_list";
    public static final String BUNDLE_PREFETCH_BARCODE_LIST = "bundle_prefetch_barcode_list";
    public static final String BUNDLE_SCAN_ITEM_COUNT = "bundle_scan_item_count";
    public static final String BUNDLE_SCAN_ITEM_INDICATOR = "bundle_scan_item_indicator";
    public static final String BUNDLE_SCANNED_BARCODE_LIST = "bundle_scanned_barcode_list";
    public static final String BUNDLE_SKIP_CHECKSUM = "bundle_skip_checksum";
    public static final String BUNDLE_USE_INPUT_ALPHA_NUMERIC = "bundle_use_input_type_alpha_numeric";
    public static final String BUNDLE_ALLOW_EMPTY_RESULT = "bundle_allow_empty_result";
    private static final int PHOTO_ACTIVITY_REQUEST_CARMERA_AND_READ_WRITE = 50;
    private static final String[] PHOTO_ACTIVITY_CAMERA_PERMISSIONS = {Manifest.permission.CAMERA};
    FrameLayout mScannerContainer;

    Button mDone;
    Button mBtnSubmitCode;

    Toolbar mToolbar;
    EditText mBarCodeEditText;

    TextView mBarcodeCountInfo;
    LinearLayout mEditTextContainer;

    View flash;

    private ZXingScannerView mScanner;

    List<String> mBarCodeList;
    private int mBarCodeCount;

    ArrayList<String> mPrefetchList;
    ArrayList<String> mScannedList;
    private String mItemIndicator;
    private MediaPlayer mediaPlayer;

    private boolean mSkipChecksum;
    private boolean mIsInputAlphaNumeric;
    private boolean mAllowEmptyResult;

    private enum ValidationResult {
        INVALID,
        VALID,
        SCANNED
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBarCodeCount = getIntent().getIntExtra(BUNDLE_SCAN_ITEM_COUNT, 0);
        mPrefetchList = getIntent().getStringArrayListExtra(BUNDLE_PREFETCH_BARCODE_LIST);
        mScannedList = getIntent().getStringArrayListExtra(BUNDLE_SCANNED_BARCODE_LIST);
        mItemIndicator = getIntent().getStringExtra(BUNDLE_SCAN_ITEM_INDICATOR);
        if (mItemIndicator == null) {
            mItemIndicator = "";
        }
        if (mPrefetchList != null) {
            mBarCodeCount = mPrefetchList.size();
        }
        mSkipChecksum = getIntent().getBooleanExtra(BUNDLE_SKIP_CHECKSUM, false);
        mIsInputAlphaNumeric = getIntent().getBooleanExtra(BUNDLE_USE_INPUT_ALPHA_NUMERIC, false);
        mAllowEmptyResult = getIntent().getBooleanExtra(BUNDLE_ALLOW_EMPTY_RESULT, false);

        setContentView(R.layout.activity_bar_code);
        initView();

        setupToolBar();
        //init barcode list
        mBarCodeList = new ArrayList<>();

        //init barcodeScanner lib
        mScanner = new ZXingScannerView(this);
        mScanner.setAutoFocus(true);
        mScannerContainer.addView(mScanner);

        mScanner.setFormats(getRestrictedFormats());

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitBarcodes();
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

        mBarCodeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                     enableSubmitButton(!TextUtils.isEmpty(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBtnSubmitCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitClicked();
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
        mEditTextContainer = (LinearLayout) findViewById(R.id.editTextContainer);

        if(mIsInputAlphaNumeric) {
            mBarCodeEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        flash = findViewById(R.id.flash);

        mBtnSubmitCode = (Button) findViewById(R.id.currentCodeSubmitBtn);
        enableSubmitButton(false);
    }


    private void checkPermissionCamera() {
        Permission.PermissionBuilder builder = new Permission.PermissionBuilder(PHOTO_ACTIVITY_CAMERA_PERMISSIONS, PHOTO_ACTIVITY_REQUEST_CARMERA_AND_READ_WRITE, mPermissionCallBack)
                .enableDefaultSettingDialog("", getString(R.string.permission_camera_setting))
                .enableDefaultRationalDialog("", getString(R.string.permission_camera_retry));
        requestAppPermissions(builder.build());
    }

    private Permission.PermissionCallback mPermissionCallBack = new Permission.PermissionCallback() {
        @Override
        public void onPermissionGranted(int requestCode) {

        }

        @Override
        public void onPermissionDenied(int requestCode) {

        }

        @Override
        public void onPermissionAccessRemoved(int requestCode) {

        }
    };

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

        ValidationResult result = validateBarcode(barcode);
        switch (result) {
            case INVALID:
                showInvalidBarCodeDialog(barcode);
                return;
            case SCANNED:
                Toast.makeText(BarCodeActivity.this, getString(R.string.item_already_scaned), Toast.LENGTH_SHORT).show();
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

    private ValidationResult validateBarcode(String barCode) {

        //Check in data
        if (mPrefetchList != null && !mPrefetchList.contains(barCode)) {
            //Check if barcode was previously scanned
            if (mScannedList != null && mScannedList.contains(barCode)) {
                return ValidationResult.SCANNED;
            } else {
                return ValidationResult.INVALID;
            }
        }

        //verhoeff check not required
        if (mSkipChecksum) {
            return ValidationResult.VALID;
        }

        //verhoeff check
        if (Verhoeff.validateVerhoeff(barCode)) {
            return ValidationResult.VALID;
        }

        return ValidationResult.INVALID;
    }

    private void updateBarcodeInfo() {
        if (mBarCodeCount == 0) {
            mBarcodeCountInfo.setText(getString(R.string.scanned_count, mBarCodeList.size()));
        } else {
            mBarcodeCountInfo.setText(getString(R.string.scanned_item_count, mBarCodeList.size() + 1, mBarCodeCount));
        }
        mBarCodeEditText.setHint(getString(R.string.qr_hint, mItemIndicator));
    }

    private void submitBarcodes() {
        if (!mAllowEmptyResult && mBarCodeList.size() == 0) {
            if (mScannedList != null && !mScannedList.isEmpty()) {
                finish();
            } else {
                Toast.makeText(BarCodeActivity.this, getString(R.string.no_item_scan), Toast.LENGTH_SHORT).show();
            }
            return;
        }
        scanComplete();
    }

    private void onSubmitClicked() {
        String barcode = mBarCodeEditText.getText().toString();
        if (!TextUtils.isEmpty(barcode)) {
            mScanner.stopCameraPreview();
            addBarcode(barcode);
            mBarCodeEditText.setText("");
        }
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

    private void enableSubmitButton(boolean enable) {
        mBtnSubmitCode.setEnabled(enable);
        int backgroundPercent;
        if (enable) {
            mBtnSubmitCode.setAlpha(1.0f);
            backgroundPercent = 100;
        } else {
            mBtnSubmitCode.setAlpha(0.05f);
            backgroundPercent = 20;
        }
        mEditTextContainer.getBackground().setAlpha((255*backgroundPercent)/100);

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

    private List<BarcodeFormat> getRestrictedFormats () {

        // For refrence check https://github.com/zxing/zxing

        List<BarcodeFormat> list = new ArrayList<>();

        // All 1D insdustrial formats
        list.add(BarcodeFormat.CODE_39);
        list.add(BarcodeFormat.CODE_93);
        list.add(BarcodeFormat.CODE_128);
        list.add(BarcodeFormat.CODABAR);
        list.add(BarcodeFormat.ITF);
        list.add(BarcodeFormat.RSS_14);
        list.add(BarcodeFormat.RSS_EXPANDED);

        // Also QR Code support
        list.add(BarcodeFormat.QR_CODE);

        return list;
    }
}
