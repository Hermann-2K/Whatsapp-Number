package com.hermann.app.whatsappnumber;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
    int noc = 0;
    String tmp;
    private ProgressDialog progress1;
    private ProgressDialog progress2;
    private ProgressDialog progress3;

    static String RACINE = "ZZZZZ-";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                        ChargementContact cc = new ChargementContact();
                        cc.execute();
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

                writeToSDFile();


                deleteContact(MainActivity.this);


                tv.append("\n\nLes numéros de téléphone enregistrés ont été supprimés !!");

            }
        });

    }

    public void deleteContact(final Context ctx) {
       // Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        DelContact dc = new DelContact();
        dc.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    noc = 0;
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
                        tv.append("\n\nNom du fichier : " + filename);

                        String csv=".csv";
                        if(filename.lastIndexOf(".") > 0) {
                            String ext = filename.substring(filename.lastIndexOf("."));
                            if (csv.equalsIgnoreCase(ext)) {
                                try {
                                    FileInputStream fis = new FileInputStream(new File(path));
                                    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                                    BufferedReader bufferedReader = new BufferedReader(isr);
                                    while (bufferedReader.readLine() != null) {
                                        noc++;
                                    }
                                }catch (IOException e){
                                    Toast.makeText(MainActivity.this, "Exception : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            } else{
                                // IF EXTENSION IS NOT ACCEPT
                                Toast.makeText(MainActivity.this, "Extension du fichier non valide", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        Long tsLong = System.currentTimeMillis()/1000;
                        tmp = tsLong.toString();

                        tv.append("\n\nFichier correctement enregistré !!");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void writeToSDFile(){
        SDFile sdf = new SDFile();
        sdf.execute();
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
                                String name = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                String number = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                if (name.contains(RACINE)) {
                                    //Add Number to ArrayList
                                    myWhatsappContacts.add(number+" "+ ";"+" "+name);
                                }
                                whatsAppContactCursor.close();

                            }
                        }
                    } while (contactCursor.moveToNext());
                    contactCursor.close();
                }
            }
        }
    }

    private class ChargementContact extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress1=new ProgressDialog(MainActivity.this);
            progress1.setMessage("Enregistrement des contacts dans le téléphone...");
            progress1.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress1.setCancelable(false);
            progress1.setProgress(0);
            progress1.setMax(noc);
            progress1.show();

        }

        protected void onProgressUpdate(Integer... values){
            super.onProgressUpdate();
            // Mise à jour de la ProgressBar
            progress1.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            int jumpTime = 0;
            try{
                FileInputStream fis = new FileInputStream(new File(path));
                InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
                BufferedReader bufferedReader = new BufferedReader(isr);
                String line;

                while((line = bufferedReader.readLine()) != null) {
                    try {
                        String phone = line;
                        String name = RACINE+tmp+"_"+(jumpTime+1);
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

                        jumpTime ++;
                        progress1.setProgress(jumpTime);
//                        publishProgress(jumpTime);
                        publishProgress((int) ((jumpTime / (float) noc) * 100));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                fis.close();
            }catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(progress1 != null)
            progress1.dismiss();

            tv.append("\n\nLes numéros de téléphone sont enregistrés dans les contacts du téléphone !!");
        }
    }

    private class SDFile extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress2=new ProgressDialog(MainActivity.this);
            progress2.setMessage("Chargement des numéros Whatsapp...");
            progress2.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress2.setCancelable(false);
            progress2.setProgress(0);
            progress2.show();

        }

        protected void onProgressUpdate(Integer... values){
            super.onProgressUpdate();
            // Mise à jour de la ProgressBar
            progress2.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Find the root of the external storage.
            // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal
            File root = android.os.Environment.getExternalStorageDirectory();
            //tv.append("\nExternal file system root: "+root);

            // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

            File dir = new File (root.getAbsolutePath() + "/Download");
            dir.mkdirs();
            Long tsLong = System.currentTimeMillis()/1000;

            final File file = new File(dir, "numbers-"+tsLong.toString()+".csv");

            final int totalProgressTime = myWhatsappContacts.size();
//            Toast.makeText(MainActivity.this, "Nombre de numéros Whatsapp : "+totalProgressTime, Toast.LENGTH_SHORT).show();
            try{
                final FileOutputStream f = new FileOutputStream(file);
                final PrintWriter pw = new PrintWriter(f);

                for(int i=0;i<totalProgressTime;i++) {
                    pw.println(myWhatsappContacts.get(i));
                    progress2.setProgress(i+1);
                    publishProgress(i+1);
                }
                pw.flush();
                pw.close();
                f.close();
            }catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(progress2 != null)
                progress2.dismiss();

            //tv.append("\n\nFile written to "+file);
            tv.append("\n\nLe fichier de sortie se trouve dans le dossier Downloads du téléphone");
            Toast.makeText(MainActivity.this, "Les numéros ont été insérés dans le fichier de sortie!!", Toast.LENGTH_LONG).show();
        }
    }

    private class DelContact extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress3=new ProgressDialog(MainActivity.this);
            progress3.setMessage("Suppression des numéros...");
            progress3.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress3.setCancelable(false);
            progress3.setMax(noc);
            progress3.setProgress(0);
            progress3.show();

        }

        protected void onProgressUpdate(Integer... values){
            super.onProgressUpdate();
            // Mise à jour de la ProgressBar
            progress3.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            final Cursor cur = getApplicationContext().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

                    int jumpTime = 0;

                    try {
                        if (cur.moveToFirst()) {
                            do {
                                if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).contains(RACINE)) {
                                    String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                                    getApplicationContext().getContentResolver().delete(uri, null, null);
                                }
                                jumpTime ++;
                                progress3.setProgress(jumpTime);
                               // publishProgress(jumpTime);
                                publishProgress((int) ((jumpTime / (float) noc) * 100));
                            } while (cur.moveToNext());
                        }

                    } catch (Exception e) {
                        e.getStackTrace();
                    } finally {
                        cur.close();
                    }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(progress3 != null)
                progress3.dismiss();
        }
    }

    @Override
    public void onResume(){

        super.onResume();
        if(progress1 != null && progress1.isShowing())
            progress1.show();

        if(progress2 != null && progress2.isShowing())
            progress2.show();

        if(progress3 != null && progress3.isShowing())
            progress3.show();
    }

    @Override
    public void onPause(){

        super.onPause();
        if(progress1 != null && progress1.isShowing())
            progress1.show();

        if(progress2 != null && progress2.isShowing())
            progress2.show();

        if(progress3 != null && progress3.isShowing())
            progress3.show();
    }

    @Override
    public void onStop(){

        super.onStop();
        if(progress1 != null)
            progress1.hide();

        if(progress2 != null)
            progress2.hide();

        if(progress3 != null)
            progress3.hide();
    }

    @Override
    public void onDestroy(){

        super.onDestroy();
        if(progress1 != null)
            progress1.dismiss();

        if(progress2 != null)
            progress2.dismiss();

        if(progress3 != null)
            progress3.dismiss();
    }

    public void EnableRuntimePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this,
                Manifest.permission.READ_CONTACTS))
        {

       //     Toast.makeText(MainActivity.this,"CONTACTS permission allows us to Access CONTACTS app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.READ_CONTACTS}, RequestPermissionCode);

        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this,
                Manifest.permission.WRITE_CONTACTS))
        {

        //    Toast.makeText(MainActivity.this,"CONTACTS permission allows us to Write CONTACTS ", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.WRITE_CONTACTS}, RequestPermissionCodeWriteContact);

        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {

        //    Toast.makeText(MainActivity.this,"Storage permission allows us to Write into External Storage app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestPermissionCodeExternalStorage);

        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE))
        {

         //   Toast.makeText(MainActivity.this,"Storage permission allows us to Read into External Storage app", Toast.LENGTH_LONG).show();

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

//                    Toast.makeText(MainActivity.this,"Permission Granted, Now your application can access CONTACTS.", Toast.LENGTH_LONG).show();

                } else {

  //                  Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot access CONTACTS.", Toast.LENGTH_LONG).show();

                }
                break;

            case RequestPermissionCodeExternalStorage:
                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

            //        Toast.makeText(MainActivity.this,"Permission Granted, Now your application can write into External Storage.", Toast.LENGTH_LONG).show();

                } else {

             //       Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot write into External Storage.", Toast.LENGTH_LONG).show();

                }
                break;

            case RequestPermissionCodeReadExternalStorage:
                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

              //      Toast.makeText(MainActivity.this,"Permission Granted, Now your application can read into External Storage.", Toast.LENGTH_LONG).show();

                } else {

               //     Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot read into External Storage.", Toast.LENGTH_LONG).show();

                }
                break;

            case RequestPermissionCodeWriteContact:
                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

              //      Toast.makeText(MainActivity.this,"Permission Granted, Now your application can write into your contacts.", Toast.LENGTH_LONG).show();

                } else {

               //     Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot write into your contacts.", Toast.LENGTH_LONG).show();

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
