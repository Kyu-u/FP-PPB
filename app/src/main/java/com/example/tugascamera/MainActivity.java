package com.example.tugascamera;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.example.tugascamera.ml.MobilenetV110224Quant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private ListView lv;
    ArrayList<HashMap<String, String>> contactList;
    Button b1;
    Button b2;
    Button b3;
    TextView tv;
    ImageView iv;
    Bitmap bitmap;
    String audioURL;
    RecyclerView recyclerView;
    int prediction;
    RequestQueue requestQueue;
    String[] predictions = new String[1001];
     static final int kodekamera = 222;
     static final int permcode = 101;
    static final int kodegallery = 333;

    String currentPhotoPath;
    StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        lv = (ListView) findViewById(R.id.lv);
//        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        requestQueue = VolleySingleton.getmInstance(this).getRequestQueue();
        b2 = (Button) findViewById(R.id.button2);
        b3 =  (Button) findViewById(R.id.button3);
        b1 = (Button) findViewById(R.id.button);
        iv = (ImageView) findViewById(R.id.imageView);
        tv = (TextView) findViewById(R.id.textView);

        contactList = new ArrayList<>();
        Context context = this;

//        fetchMeals();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("labels.txt"), "UTF-8"));

            // do reading, usually loop until end of file reading
            String mLine;
            int i = 0;
            while ((mLine = reader.readLine()) != null) {
                //process line
                mLine.split("\n");
                predictions[i] = mLine;
                i++;
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }


        storageReference = FirebaseStorage.getInstance().getReference();

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askCameraPermissions();
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
                try {
                    MobilenetV110224Quant model = MobilenetV110224Quant.newInstance(context);

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);
                    ByteBuffer byteBuffer = TensorImage.fromBitmap(resized).getBuffer();
                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    MobilenetV110224Quant.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                    prediction = match(outputFeature0.getFloatArray());
                    tv.setText(predictions[prediction]);
                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e) {
                    // TODO Handle the exception
                }
                new GetContacts().execute();
//                setContentView(R.layout.list_item);

            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, kodegallery);
            }
        });
    }


    public View.OnClickListener pronounce() {


        return null;
    }


    /**
     * Async task class to get json by making HTTP call
     */
    @SuppressLint("StaticFieldLeak")
    private class GetContacts extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this,
            "Json Data is downloading",Toast.LENGTH_LONG).show();

        }
        @Override
protected Void doInBackground(Void... arg0) {
    HttpHandler sh = new HttpHandler();
    // Making a request to url and getting response
            String myString = predictions[prediction].replaceAll(" ", "_").toLowerCase();
        String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + myString;
            Log.d("Myactivity", url);
//    String url = "http://api.androidhive.info/contacts/";
    String jsonStr = sh.makeServiceCall(url);
      Log.e("Main", "Response from url: " + jsonStr);
    if (jsonStr != null) {
        try {
            JSONArray mainarr = new JSONArray(jsonStr);

            JSONObject mainobj  = mainarr.getJSONObject(0);
            String name = mainobj.getString("word");
            JSONArray phonetics = mainobj.getJSONArray("phonetics");
            JSONObject p = phonetics.getJSONObject(0);
            String id = p.getString("text");
            audioURL = p.getString("audio");
            JSONArray meanings = mainobj.getJSONArray("meanings");
            JSONArray defarr;
            JSONObject meaningobj;
            JSONObject defobj;
            String m;
            HashMap<String, String> contact = new HashMap<>();
            Log.d("testing", "test1");

            for(int i = 0; i< meanings.length(); i++){
                Log.d("testing", "test2");

                JSONObject temp = meanings.getJSONObject(i);
                if(temp.getString("partOfSpeech").equalsIgnoreCase("noun")){
                    Log.d("testing", "test if");

                    meaningobj = meanings.getJSONObject(i);
                    defarr = meaningobj.getJSONArray("definitions");
                    defobj = defarr.getJSONObject(0);
                    m = defobj.getString("definition");
                    Log.d("testing", m);

                    contact.put("definition", m);
                    break;
                }
            }





                            	// tmp hash map for single contact
                // adding each child node to HashMap key => value
                contact.put("id", id);contact.put("name", name);
                // adding contact to contact list
                contactList.add(contact);
        } catch (final JSONException e) {
                    Log.e("Main", "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }});
                }
            } else {
                Log.e("Main", "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }});
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            setContentView(R.layout.list);
            Button play = (Button) findViewById(R.id.button4);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("test", "hehe");
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioAttributes(
                            new AudioAttributes
                                    .Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build());
                    try {
                        mediaPlayer.setDataSource("http:" + audioURL);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.start();
                        Toast.makeText(MainActivity.this, "Pronouncing", Toast.LENGTH_SHORT).show();

                }
            });
            Log.d("testbutton4", play.toString());
            lv = (ListView) findViewById(R.id.lv);
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, contactList,
                    R.layout.list_item, new String[]{"id","name","definition"},
                    new int[]{R.id.id,R.id.name, R.id.definition});
            lv.setAdapter(adapter);

        }}

//    private void fetchMeals() {
//        String myString = predictions[prediction].replaceAll(" ", "_").toLowerCase();
//        String url = "https://www.themealdb.com/api/json/v1/1/filter.php?i=" + myString;
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, "meals", new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    JSONArray jsonArray = response.getJSONArray(null);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void askCameraPermissions(){
        Log.d("MyActivity", "askperm: ");

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},permcode);
        }else {
            Log.d("MyActivity", "permsuccess: ");

            dispatchTakePictureIntent();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("MyActivity", "onrequestperm: ");

        if(requestCode == permcode) {
            if(grantResults.length < 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                dispatchTakePictureIntent();

            }else{
                Toast.makeText(this, "Cam Permission Required", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case (kodekamera):
//                    Bundle extras = data.getExtras();
//                    Bitmap imageBitmap = (Bitmap) extras.get("data");


                    File f = new File(currentPhotoPath);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver() , Uri.parse(String.valueOf(Uri.fromFile(f))));
                        iv.setImageBitmap(bitmap);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

//                    iv.setImageURI(Uri.fromFile(f));
                    Log.d("TAG", "image url :" + Uri.fromFile(f));

                    //Simpan ke Galeri
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    this.sendBroadcast(mediaScanIntent);


                    break;
                case (kodegallery):
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        bitmap = selectedImage;
                        iv.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                    break;
            }}
    }

    private void uploadToFirebase(String name, Uri contentUri) {
        StorageReference image = storageReference.child("images/" + name);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        Log.d("TAG", "Upload success");
//                    }
//                });
                Toast.makeText(MainActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Log.d("MyActivity", "firstif: ");

            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            Log.d("MyActivity", "beforestart: ");

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, kodekamera);
                Log.d("MyActivity", "afterstart: ");
            }
//        }
    }

    private int match(float[] arr){
        int index = 0;
        float curr = 0.0f;
        for(int i = 0; i < 1000; i++){
            if(arr[i]> curr){
                index = i;
                curr = arr[i];
            }
        }
        return index;
    }


}
