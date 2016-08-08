# Scanner
BarCode scanner 


- Wrapper for all barcode scanning activity .

# Getting result: 

 **Start barcode activity**
 
     val intent = Intent(activity, BarCodeActivity::class.java)
     var barcode = ArrayList<String>();
     barcode.add("99901106842")
     intent.putStringArrayListExtra(BarCodeActivity.BUNDLE_PREFETCH_BARCODE_LIST, barcode);
     intent.putExtra(BarCodeActivity.BUNDLE_SCAN_ITEM_COUNT, "item_count");
     startActivityForResult(intent, REQEUST_CODE_SCAN_AWB);

item_count =N , user can scan N barcode.
item_count =0  , no limit on scan count.

**Getting result**

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    
        if (RESULT_OK == resultCode) {
	         ArrayList arrayList =data.getStringArrayListExtra(BarCodeActivity.BUNDLE_BARCODE_LIST);
        }
    }
    
Arraylist contain all barcode scanned.


Barcode Validation : 
-  prefetch list .
- Verhoeff algo .

# Download

Include `jitpack.io` inside of **root** project `build.gradle`:

```groovy
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```

After that you can easily include the library in your **app** `build.gradle`:

```groovy
dependencies {
	        compile 'com.github.Nuvoex:Scanner:v1.0'
	}
```

That's it build your project.



