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
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import xavier.com.cognitiveservices.R;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class
MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static int REQUEST_IMAGE_CAPTURE = 1;
    private static int SELECT_PHOTO =2;
    private ImageView pictureDisplay;
    private Button analyzePictureButton;
    private ListView displayAnalyzeList;
    private Bitmap bitmap;
    private Adapter adapter;
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
        displayAnalyzeList = (ListView) this.findViewById(R.id.descriptionDisplayList);
        displayAnalyzeList.setVisibility(View.VISIBLE);
        ArrayList<FaceAttribute> listContent = new ArrayList<>();
        adapter = new Adapter(this,listContent);
        displayAnalyzeList.setAdapter(adapter);
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
        adapter.getContent().clear();
        launchFaceAnalyze ();
    }

    private void launchFaceAnalyze() {
        if(bitmap != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            AsyncTask<InputStream, String, Face[]> detectTask =
                    new AsyncTask<InputStream, String, Face[]>() {
                        @Override
                        protected Face[] doInBackground(InputStream... params) {
                            try {
                                publishProgress("Detecting...");
                                Face[] result = faceServiceClient.detect(params[0], true, true,new FaceServiceClient.FaceAttributeType[] {
                                        FaceServiceClient.FaceAttributeType.Age,
                                        FaceServiceClient.FaceAttributeType.Gender,
                                        FaceServiceClient.FaceAttributeType.Smile,
                                        FaceServiceClient.FaceAttributeType.FacialHair,
                                        FaceServiceClient.FaceAttributeType.HeadPose
                                });
                                if (result == null) {
                                    publishProgress("Detection Finished. Nothing detected");
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
                            if (result == null) {
                                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "debut du dessin", Toast.LENGTH_SHORT).show();
                                bitmap = drawFaceRectangleOnBitmap(bitmap, result);
                                pictureDisplay.setImageBitmap(bitmap);

                            }

                        }

                    };
            detectTask.execute(inputStream);
        }else{
            Toast.makeText(this, "choose a picture first", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap drawFaceRectangleOnBitmap(Bitmap bitmap, Face[] result) {
        Bitmap bitmapDraw = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmapDraw);
        Paint rectanglePaint = new Paint();
        rectanglePaint.setAntiAlias(true);
        rectanglePaint.setStyle(Paint.Style.STROKE);
        rectanglePaint.setColor(Color.RED);
        int stokeWidth = 3;
        rectanglePaint.setStrokeWidth(stokeWidth);
        Paint landmarkPaint = new Paint ();
        landmarkPaint.setAntiAlias(true);
        landmarkPaint.setStyle(Paint.Style.STROKE);
        landmarkPaint.setColor(Color.BLUE);
        landmarkPaint.setStrokeWidth(15);
        if (result != null) {
            for (Face face : result) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        rectanglePaint);
                if(face.faceLandmarks != null){
                    canvas.drawPoint((float) face.faceLandmarks.eyeLeftOuter.x, (float) face.faceLandmarks.eyeLeftOuter.y, landmarkPaint);
                    canvas.drawPoint((float) face.faceLandmarks.eyeRightOuter.x, (float) face.faceLandmarks.eyeRightOuter.y, landmarkPaint);
                }
                if (face.faceAttributes != null) {
                    adapter.addFaceAttribute(face.faceAttributes);
                    adapter.notifyDataSetChanged();
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
        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data");
                pictureDisplay.setImageBitmap(bitmap);
            } else {
                if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK) {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageStream);
                    if(selectedImageBitmap != null){
                        this.bitmap = selectedImageBitmap;
                        pictureDisplay.setImageBitmap(this.bitmap);
                    }else{
                        Toast.makeText(this, "error retrieving picture", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getPicture() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }
}
