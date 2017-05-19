package com.hermann.app.whatsappnumber;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView ;
    ArrayList<String> StoreContacts ;
    ArrayList<String> myWhatsappContacts ;
    ArrayAdapter<String> arrayAdapter ;
    Cursor cursor ;
    String name, phonenumber, contactid ;
    public  static final int RequestPermissionCode  = 1 ;
    public static final int RequestPermissionCodeExternalStorage = 2;
    public static final int RequestPermissionCodeReadExternalStorage = 3;
    public static final int RequestPermissionCodeWriteContact = 4;

    Button button,chooseFile,loadFile;
    private TextView tv;
    private static final String TAG = "MEDIA";
    private static final int FILE_SELECT_CODE = 0;
    String path,filename;
    int rawContactInsertIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView)findViewById(R.id.listview1);

        button = (Button)findViewById(R.id.button3);
        chooseFile = (Button) findViewById(R.id.button1);
        loadFile = (Button)findViewById(R.id.button2);
        tv = (TextView) findViewById(R.id.textview);

        StoreContacts = new ArrayList<String>();
        myWhatsappContacts = new ArrayList<>();

        EnableRuntimePermission();
        tv.setText("");

        chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(
                            Intent.createChooser(intent, "Select a File"),
                            FILE_SELECT_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    // Potentially direct the user to the Market with a Dialog
                    Toast.makeText(MainActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(path == null){
                    // IF NO FILE SELECTED DO NOTHING
                    Toast.makeText(MainActivity.this, "Aucun fichier choisi", Toast.LENGTH_SHORT).show();
                    return;
                }
                String csv=".csv";
                if(filename.lastIndexOf(".") > 0){
                    String ext = filename.substring(filename.lastIndexOf("."));
                    if(csv.equalsIgnoreCase(ext)){
                        try{
                            FileInputStream fis = new FileInputStream(new File(path));
                            InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
                            BufferedReader bufferedReader = new BufferedReader(isr);
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                String phone = line.split(";")[0];
                                String name = line.split(";")[1];
                                ArrayList < ContentProviderOperation > ops = new ArrayList < ContentProviderOperation > ();

                                rawContactInsertIndex = ops.size();



                                try{

                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
                                    ops.add(ContentProviderOperation
                                            .newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
                                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name) // Name of the person
                                            .build());
                                    ops.add(ContentProviderOperation
                                            .newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(
                                                    ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
                                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone) // Number of the person
                                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); // Type of mobile number
                                    ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

                                }catch (Exception e){
                                    Toast.makeText(MainActivity.this, "Exception : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                            fis.close();
                            tv.append("\nLes numéros de téléphone sont enregistrés dans le téléphone !!");

                        }catch (IOException e){
                            Toast.makeText(MainActivity.this, "Exception : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    } else{
                        // IF EXTENSION IS NOT ACCEPT
                        Toast.makeText(MainActivity.this, "Extension du fichier non valide", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myWhatsappContacts.clear();
                //GetContactsIntoArrayList();
                checkExternalMedia();

                    GetWhatsAppContacts();

                arrayAdapter = new ArrayAdapter<String>(
                        MainActivity.this,
                        R.layout.contact_items_listview,
                        R.id.textView, myWhatsappContacts
                );

                listView.setAdapter(arrayAdapter);

                writeToSDFile();

                try{
                    FileInputStream fis = new FileInputStream(new File(path));
                    InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String phone = line.split(";")[0];
                        String name = line.split(";")[1];
                        deleteContact(MainActivity.this,phone,name);
                    }
                    fis.close();
                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "Exception : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                tv.append("\nLes numéros de téléphone enregistrés ont été supprimés !!");

            }
        });

    }

    public static boolean deleteContact(Context ctx, String phone, String name) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        Cursor cur = ctx.getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(name)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        ctx.getContentResolver().delete(uri, null, null);
                        return true;
                    }

                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        } finally {
            cur.close();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    tv.setText("\n");
                    try{
                        // Get the Uri of the selected file
                        Uri uri = data.getData();
                        Log.d(TAG, "File Uri: " + uri.toString());
                        // Get the path
                        path = FileUtils.getPath(MainActivity.this, uri);
                        Log.d(TAG, "File Path: " + path);
                        // Get the file instance
                        // File file = new File(path);
                        // FILE NAME
                        filename = path.substring(path.lastIndexOf(File.separator));
                        // Initiate the upload
                        tv.append("Chemin du fichier : " + path);
                        tv.append("\nNom du fichier : " + filename);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void writeToSDFile(){

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

        File root = android.os.Environment.getExternalStorageDirectory();
        tv.append("\nExternal file system root: "+root);

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        File dir = new File (root.getAbsolutePath() + "/Download");
        dir.mkdirs();
        File file = new File(dir, "numbers.csv");

        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            for(int i=0;i<myWhatsappContacts.size();i++){
                pw.println(myWhatsappContacts.get(i));
            }
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
        tv.append("\n\nFile written to "+file);
        Toast.makeText(this, "Les numéros ont été insérés dans le fichier !!", Toast.LENGTH_LONG).show();
    }

    public void GetWhatsAppContacts(){

        //This class provides applications access to the content model.
        ContentResolver cr = MainActivity.this.getContentResolver();

        //RowContacts for filter Account Types
        Cursor contactCursor = cr.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.CONTACT_ID},
                ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
                new String[]{"com.whatsapp"},
                null);

        //ArrayList for Store Whatsapp Contact
        //ArrayList<String> myWhatsappContacts = new ArrayList<>();

        if (contactCursor != null) {
            if (contactCursor.getCount() > 0) {
                if (contactCursor.moveToFirst()) {
                    do {
                        //whatsappContactId for get Number,Name,Id ect... from  ContactsContract.CommonDataKinds.Phone
                        String whatsappContactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));

                        if (whatsappContactId != null) {
                            //Get Data from ContactsContract.CommonDataKinds.Phone of Specific CONTACT_ID
                            Cursor whatsAppContactCursor = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{whatsappContactId}, null);

                            if (whatsAppContactCursor != null) {
                                whatsAppContactCursor.moveToFirst();
                                String id = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                                String name = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                String number = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                whatsAppContactCursor.close();

                                //Add Number to ArrayList
                                myWhatsappContacts.add(number+" "+ ";"+" "+name+" "+";"+" "+id);
                            }
                        }
                    } while (contactCursor.moveToNext());
                    contactCursor.close();
                }
            }
        }
    }

    public void EnableRuntimePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this,
                Manifest.permission.READ_CONTACTS))
        {

            Toast.makeText(MainActivity.this,"CONTACTS permission allows us to Access CONTACTS app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.READ_CONTACTS}, RequestPermissionCode);

        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this,
                Manifest.permission.WRITE_CONTACTS))
        {

            Toast.makeText(MainActivity.this,"CONTACTS permission allows us to Write CONTACTS ", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.WRITE_CONTACTS}, RequestPermissionCodeWriteContact);

        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {

            Toast.makeText(MainActivity.this,"Storage permission allows us to Write into External Storage app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestPermissionCodeExternalStorage);

        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE))
        {

            Toast.makeText(MainActivity.this,"Storage permission allows us to Read into External Storage app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, RequestPermissionCodeReadExternalStorage);

        }

    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this,"Permission Granted, Now your application can access CONTACTS.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot access CONTACTS.", Toast.LENGTH_LONG).show();

                }
                break;

            case RequestPermissionCodeExternalStorage:
                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this,"Permission Granted, Now your application can write into External Storage.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot write into External Storage.", Toast.LENGTH_LONG).show();

                }
                break;

            case RequestPermissionCodeReadExternalStorage:
                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this,"Permission Granted, Now your application can read into External Storage.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot read into External Storage.", Toast.LENGTH_LONG).show();

                }
                break;

            case RequestPermissionCodeWriteContact:
                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this,"Permission Granted, Now your application can write into your contacts.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot write into your contacts.", Toast.LENGTH_LONG).show();

                }
                break;


        }
    }

    /** Method to check whether external media available and writable. This is adapted from
     http://developer.android.com/guide/topics/data/data-storage.html#filesExternal */

    private void checkExternalMedia(){
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        tv.append("\n\nExternal Media: readable="
                +mExternalStorageAvailable+" writable="+mExternalStorageWriteable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
