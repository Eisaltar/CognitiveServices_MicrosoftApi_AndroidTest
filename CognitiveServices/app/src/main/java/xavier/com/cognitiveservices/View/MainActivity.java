package xavier.com.cognitiveservices.View;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import xavier.com.cognitiveservices.R;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class
MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static int REQUEST_IMAGE_CAPTURE = 1;
    private static int SELECT_PHOTO =2;
    private ImageView pictureDisplay;
    private Button analyzePictureButton;
    private TextView displayAnalyze;
    private Bitmap bitmap;
    private String textToDisplay;
    private static FaceServiceClient faceServiceClient = new FaceServiceRestClient("2563970663064fb6b943bc4e610f7127");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    private void init() {
        pictureDisplay = (ImageView) this.findViewById(R.id.pictureDisplay);
        displayAnalyze = (TextView) this.findViewById(R.id.descriptionDisplay);
        analyzePictureButton = (Button) this.findViewById(R.id.buttonAnalyze);
        analyzePictureButton.setOnClickListener(this);
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
        }else{
            if( id == R.id.choosePicture){
                getPicture();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void launchAnalyze( ) {
        String result= launchFaceAnalyze ();
    }

    private String launchFaceAnalyze() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>(){
                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(params[0],true,false,null);
                            if (result == null)
                            {
                                publishProgress("Detection Finished. Nothing detected");
                                return null;
                            }
                            publishProgress(
                                    String.format("Detection Finished. %d face(s) detected",
                                            result.length));
                            return result;
                        } catch (Exception e) {
                            publishProgress("Detection failed");
                            e.printStackTrace();
                            return null;
                        }
                    }
                    @Override
                    protected void onPreExecute() {
                        Toast.makeText(MainActivity.this, "début de l'analyse", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        Toast.makeText(MainActivity.this, "progress", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
                        if (result == null){
                            displayAnalyze.setText("error");
                        }else{
                            displayAnalyze.setText(result.toString());
                            bitmap = drawFaceRectangleOnBitmap(bitmap, result);
                            pictureDisplay.setImageBitmap(bitmap);

                        }

                    }

                };
        detectTask.execute(inputStream);
        return null;
    }

    private Bitmap drawFaceRectangleOnBitmap(Bitmap bitmap, Face[] result) {
        Bitmap bitmapDraw = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmapDraw);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        int stokeWidth = 2;
        paint.setStrokeWidth(stokeWidth);
        if (result != null) {
            textToDisplay = new String();
            for (Face face : result) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
                if (face.faceAttributes != null) {
                    textToDisplay += "this is a "+ face.faceAttributes.gender;
                    textToDisplay += " this person is " + face.faceAttributes.age;
                    textToDisplay += " facial hair : "+ face.faceAttributes.facialHair;
                    textToDisplay += " smile : "+face.faceAttributes.smile;
                    textToDisplay += " head pose : " + face.faceAttributes.headPose;

                    displayAnalyze.setText( textToDisplay);
                }

            }
        }
        return bitmapDraw;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.buttonAnalyze:
                launchAnalyze();
                break;
            case R.id.fab:
                launchTakingPicture();
                break;
        }

    }

    private void launchTakingPicture() {
        Intent takePictureIntent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK ){
            Bundle extras = data.getExtras();
            bitmap= (Bitmap) extras.get("data");
            pictureDisplay.setImageBitmap(bitmap);
        }else{
            if(requestCode == SELECT_PHOTO && resultCode == RESULT_OK){
                // Let's read picked image data - its URI
                Uri pickedImage = data.getData();
                // Let's read picked image path using content resolver
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
                cursor.moveToFirst();
                String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeFile(imagePath, options);

                // Do something with the bitmap
                pictureDisplay.setImageBitmap(bitmap);

                // At the end remember to close the cursor or you will end with the RuntimeException!
                cursor.close();
            }
        }
    }

    public void getPicture() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }
}
