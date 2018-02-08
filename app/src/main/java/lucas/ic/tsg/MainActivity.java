package lucas.ic.tsg;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ok";
    private Button processar, foto, load;

    private Bitmap bmp;

    public double otsuValue;

    private String selectedImagePath = "vazio";


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    try {
                        otsuValue = otsu(bmp);
                        Toast.makeText(getApplicationContext(), "Imagem processada: " + otsuValue, Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };



    private double otsu(Bitmap bmp) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(bmp, rgba);

        Mat edges = new Mat(rgba.size(), CvType.CV_8UC4);
        Imgproc.cvtColor(rgba, edges, Imgproc.COLOR_RGBA2GRAY, 4);

        double a = Imgproc.threshold(edges, edges, 0, 255, Imgproc.THRESH_OTSU);

        Bitmap resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edges, resultBitmap);
        int nh = (int) (resultBitmap.getHeight() * (512.0 / resultBitmap.getWidth()));
        Bitmap scaled = Bitmap.createScaledBitmap(resultBitmap, 512, nh, true);
        ((ImageView) findViewById(R.id.imageView2)).setImageBitmap(scaled);



        Imgproc.threshold(edges, edges, 0, 255, Imgproc.THRESH_BINARY_INV);


        resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edges, resultBitmap);
        nh = (int) (resultBitmap.getHeight() * (512.0 / resultBitmap.getWidth()));
        scaled = Bitmap.createScaledBitmap(resultBitmap, 512, nh, true);
        ((ImageView) findViewById(R.id.imageView3)).setImageBitmap(scaled);




        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(edges, contours, new Mat(), Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.CHAIN_APPROX_NONE);

        Imgproc.drawContours(rgba, contours, 1, new Scalar(255, 0, 0), 1);


        /*
        resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edges, resultBitmap);
        nh = (int) (resultBitmap.getHeight() * (512.0 / resultBitmap.getWidth()));
        scaled = Bitmap.createScaledBitmap(resultBitmap, 512, nh, true);
*/
        resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, resultBitmap);
        nh = (int) (resultBitmap.getHeight() * (512.0 / resultBitmap.getWidth()));
        scaled = Bitmap.createScaledBitmap(resultBitmap, 512, nh, true);

       // Imgproc.drawContours(edges2, contours, 1, new Scalar(255, 0, 0), 10);



        ((ImageView) findViewById(R.id.imageView4)).setImageBitmap(scaled);




        return a;

    }






    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.teste);

        processar = (Button) findViewById(R.id.button);
        processar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                if (selectedImagePath.equals("vazio")) {
                    Toast.makeText(getApplicationContext(), "Nenhuma imagem carregada!", Toast.LENGTH_LONG).show();
                } else {

                    bmp = BitmapFactory.decodeFile(selectedImagePath);

                    if (!OpenCVLoader.initDebug()) {
                        Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, getApplicationContext(), mLoaderCallback);
                    } else {
                        Log.d(TAG, "OpenCV library found inside package. Using it!");
                        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                    }

                }
            }
        });

        foto = (Button) findViewById(R.id.button2);
        foto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(intent, 2);


            }
        });


        load = (Button) findViewById(R.id.button4);
        load.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Selecione a Imagem"), 1);


            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getRealPathFromURI_API19(getApplicationContext(), selectedImageUri);

                Bitmap source = BitmapFactory.decodeFile(selectedImagePath);

                ImageView mImg;
                mImg = (ImageView) findViewById(R.id.imageView);
                mImg.setImageBitmap(source);

                Toast.makeText(getApplicationContext(), "Imagem carregada!", Toast.LENGTH_SHORT).show();

            }

            if (requestCode == 2) {

                Bitmap photo = (Bitmap) data.getExtras().get("data");

                storeImage(photo);

                Toast.makeText(getApplicationContext(), "Foto Salva!", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";

        //Toast.makeText(getApplicationContext(), mediaStorageDir.getPath() + File.separator + mImageName, Toast.LENGTH_LONG).show();

        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(mediaStorageDir.getPath() + File.separator + mImageName);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);


        return mediaFile;
    }


}
