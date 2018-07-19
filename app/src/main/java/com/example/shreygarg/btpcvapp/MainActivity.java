package com.example.shreygarg.btpcvapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;

public class MainActivity extends AppCompatActivity implements OnTouchListener, CvCameraViewListener2, SeekBar.OnSeekBarChangeListener {
    private CameraBridgeViewBase mOpenCvCameraView;
    Mat latest;
    int  ctr=0;
    Size canvasSize;
    int gZoom = 1;
    Button b1,b2,b4,b8,b12,b16,bpause;
    Boolean paused = false;
    SeekBar seek;
    Switch binary,erode,dilute;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
//                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_View);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // Register the ListView  for Context menu
//        b1 = (Button)findViewById(R.id.button);
//        b2 = (Button)findViewById(R.id.button2);
//        b4 = (Button)findViewById(R.id.button4);
//        b8 = (Button)findViewById(R.id.button8);
//        b12 = (Button)findViewById(R.id.button12);
        bpause = (Button)findViewById(R.id.button3);
//        b16 = (Button)findViewById(R.id.button16);
        seek = (VerticalSlider)findViewById(R.id.verticalSeekbar);
        binary = (Switch)findViewById(R.id.switch1);
        erode = (Switch)findViewById(R.id.switch2);
        dilute = (Switch)findViewById(R.id.switch3);
        seek.setOnSeekBarChangeListener(this);



//        b1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                gZoom = 1;
//            }
//        });
//        b2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                gZoom = 3;
//            }
//        });
//        b4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                gZoom = 4;
//            }
//        });
//        b8.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                gZoom = 7;
//            }
//        });
//        b12.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                gZoom = 9;
//            }
//        });
        bpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paused = paused==false?true:false;
            }
        });
//        b16.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                gZoom = 16;
//            }
//        });

    }
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    @Override
    public void onCameraViewStarted(int width, int height) {
    }
    @Override
    public void onCameraViewStopped() {
    }
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if(paused)
            return latest;
//        Mat source = inputFrame.rgba();
        canvasSize=inputFrame.rgba().size();
//            new InterpolateImage().execute(&(inputFrame.rgba()));

        if(ctr==0) {
            try {
                int zoomingFactor = gZoom;
                if (zoomingFactor != 1) {
//                Mat destination = new Mat(source[0].rows()/zoomingFactor, source[0].cols()/zoomingFactor, source[0].type());
                    Rect roi = new Rect(inputFrame.rgba().cols() / 2, inputFrame.rgba().rows() / 2, inputFrame.rgba().cols() / zoomingFactor, inputFrame.rgba().rows() / zoomingFactor);
                    Mat smaller = new Mat(inputFrame.rgba(), roi);
                    Mat fina = new Mat(smaller.rows() * zoomingFactor, smaller.cols() * zoomingFactor, inputFrame.rgba().type());
                    Imgproc.resize(smaller, fina, fina.size(), zoomingFactor, zoomingFactor, Imgproc.INTER_CUBIC);
                    Mat bordered = new Mat(inputFrame.rgba().rows(),inputFrame.rgba().cols(),inputFrame.rgba().type());
                    int r_top = (inputFrame.rgba().rows() - fina.rows())/2;
                    int r_bot = inputFrame.rgba().rows() - fina.rows() - r_top;
                    int c_top = (inputFrame.rgba().cols() - fina.cols())/2;
                    int c_bot = inputFrame.rgba().cols() - fina.cols() - c_top;
                    Core.copyMakeBorder(fina, bordered, r_top, r_bot, c_top, c_bot, Core.BORDER_CONSTANT, Scalar.all(0));
                    if(binary.isChecked()) {
                        Imgproc.GaussianBlur(bordered, bordered, new Size(3, 3), 0);
                        Imgproc.cvtColor(bordered, bordered, Imgproc.COLOR_BGR2GRAY);
                        Imgproc.threshold(bordered, bordered, 0, 255, Imgproc.THRESH_OTSU);
//                        Imgproc.cvtColor(bordered,bordered,Imgproc.COLOR_GRAY2RGB);
                        if(erode.isChecked())
                            Imgproc.erode(bordered,bordered, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4,4)));
                        if(dilute.isChecked())
                            Imgproc.dilate(bordered,bordered, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4,4)));
//                        double orange[] = {255,127,80};
//                        for(int i=0;i<bordered.cols();i++){
//                            for(int j=0;j<bordered.rows();j++){
//                                double data[] = bordered.get(i,j);
//                                if(data[0]==0)
//                                    bordered.put(i,j,orange);
//                            }
//                        }
                    }
//                    Imgproc.threshold(bordered,bordered, 123,255, Imgproc.THRESH_BINARY);
                    latest = bordered;
                } else
                    latest = inputFrame.rgba();

            } catch (Exception e) {
                System.out.print(e.getStackTrace());
            }
        }
        ctr = 1-ctr;
        System.gc();
        return latest;
    }
    @Override
    public boolean onTouch(View v,MotionEvent event){
        return false;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Toast.makeText(getApplicationContext(),"Zoom: "+progress+"x", Toast.LENGTH_SHORT).show();
        gZoom=progress;
        if(gZoom<1)
            gZoom=1;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private class InterpolateImage extends AsyncTask<Mat, Integer, Long> {
        Mat updated;//=new Mat;
        protected Long doInBackground(Mat... source) {
            try {
                int zoomingFactor = gZoom;
                if(zoomingFactor!=1) {
//                Mat destination = new Mat(source[0].rows()/zoomingFactor, source[0].cols()/zoomingFactor, source[0].type());
                    Rect roi = new Rect(source[0].cols() / 2, source[0].rows() / 2, source[0].cols() / zoomingFactor, source[0].rows() / zoomingFactor);
                    Mat smaller = new Mat(source[0], roi);
                    Mat fina = new Mat(smaller.rows() * zoomingFactor, smaller.cols() * zoomingFactor, source[0].type());
                    Imgproc.resize(smaller, fina, fina.size(), zoomingFactor, zoomingFactor, Imgproc.INTER_CUBIC);
//
//                    smaller.release();
                    updated = fina;
                    smaller.release();
                    fina.release();
                }
                else
                    updated=source[0];
//                if(destination!=null)
//                    destination.release();
//                if(source[0]!=null)
//                    source[0].release();
            }
            catch (Exception e){
                System.out.print(e.getStackTrace());
            }
            System.gc();
            return (long)5;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            latest=updated;
            System.gc();
//            if(updated!=null)
//                updated.release();
//            if(smaller!=null)
//                smaller.release();
//            if(fina!=null)
//                fina.release();
        }
    }

}
