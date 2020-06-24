package com.example.services;

import android.graphics.Bitmap;
import android.os.Environment;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;

public class PupilSeeker {
    private CascadeClassifier faceCascade;
    private CascadeClassifier eyesCascade;
    private CascadeClassifier pupilCascade;
    private Bitmap bmp;
    private Mat mat;

    private BaseLoaderCallback mLoaderCallback;
    public PupilSeeker(final Bitmap bmp) {
        if (!OpenCVLoader.initDebug())
        {
            System.out.println("Failed to INIT \n OpenCV Failure");
        }
        else
        {
            System.out.println("OpenCV INIT Succes");
        }
        this.bmp = bmp;
        mat = new Mat (bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC3);
        System.out.println("check mat size"+mat.size());

        Bitmap bmp32 = this.bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, this.mat);
        String path_xml = "res/xml";
        this.faceCascade = new CascadeClassifier("res/xml/haarcascade_frontalface_default.xml");
        this.eyesCascade = new CascadeClassifier("res/xml/haarcascade_eye.xml");

    }

    public void start(){
        System.out.println("Start Pupilseeker");
        Mat face = detect_faces(this.mat);
//        ArrayList<Mat> eye = detect_eyes(face);
//        ArrayList<Mat> pupil = detect_pupils(eye);
        System.out.println("Pupilseeker finished");
    }



//    private Mat classifyEye(Mat face) {
//        MatOfRect eyes = new MatOfRect();
//        faceCascade.detectMultiScale(face, eyes);
//        Rect rectCrop=null;
//        for (Rect rect : eyes.toArray()) {
//            Imgproc.rectangle(face, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
//                    new Scalar(0, 255, 0));
//            rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);
//        }
//        Mat image_roi = new Mat(face, rectCrop);
//        return image_roi;
//    }

//    private Mat classifyFace(Mat img) {
////        Mat gray = new Mat();
////        cvtColor(img, gray, COLOR_RGBA2GRAY, 0);
//        MatOfRect faces = new MatOfRect();
//        faceCascade.detectMultiScale(img, faces);
//        Rect rectCrop=null;
//        for (Rect rect : faces.toArray()) {
//            Imgproc.rectangle(img, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
//                    new Scalar(0, 255, 0));
//            rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);
//        }
//        Mat image_roi = new Mat(img, rectCrop);
//        return image_roi;
//    }

    private Mat detect_faces(Mat img){
        int i = 0;
        double ds_factor = 0.5;
        Mat face = new Mat();

        Size size = new Size(img.width(), img.height());
        Imgproc.resize(img, face, size, ds_factor, ds_factor, Imgproc.INTER_AREA);
        i++;
        Mat gray = new Mat();
        Imgproc.cvtColor(face, gray, COLOR_BGR2GRAY);
        MatOfRect faces = new MatOfRect();
//        faceCascade.detectMultiScale(gray, faces, 1.3, 5, 0, new Size(120,120));
        //Drawing boxes
        for (Rect rect : faces.toArray()) {
            Imgproc.rectangle(
                    img,
                    new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 0, 255),
                    3
            );

//            Imgcodecs.imwrite("C:\\Users\\Wouter\\Pictures\\faces", img.get(i));
            File folder = new File(getUrl() +"/faces");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            Imgcodecs.imwrite(getUrl() +"/faces"+getDate()+".jpg", img);
        }
        System.out.println("face detection complete");
        return face;
    }

    private ArrayList<Mat> detect_eyes(ArrayList<Mat> eye) {
        double ds_factor = 0.5;
        ArrayList<Mat> face = new ArrayList<>();
        for(int idx=0; idx < eye.size(); idx++) {
            Size size = new Size(eye.get(idx).width(), eye.get(idx).height());
            Imgproc.resize(eye.get(idx), face.get(idx), size, ds_factor, ds_factor, Imgproc.INTER_AREA);
            Mat gray = new Mat();
            Imgproc.cvtColor(face.get(idx), gray, COLOR_BGR2GRAY);
            MatOfRect faces = new MatOfRect();
            eyesCascade.detectMultiScale(gray, faces, 1.3, 1);
            //Drawing boxes
            for (Rect rect : faces.toArray()) {
                Imgproc.rectangle(
                        eye.get(idx),
                        new Point(rect.x, rect.y),
                        new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 0, 255),
                        3
                );
            }
            File folder = new File(getUrl() +"/eyes");
            if (!folder.exists()) {
                folder.mkdirs();
            }
//            Imgcodecs.imwrite("C:\\Users\\Wouter\\Pictures\\eyes", img.get(i));
            System.out.println(getUrl());
            Imgcodecs.imwrite(getUrl() +"/eyes"+getDate()+".jpg", eye.get(idx));
        }
        System.out.println("eye detection complete");
        return eye;
    }

    private ArrayList<Mat> detect_pupils(ArrayList<Mat> face) {

        return face;
    }


    private String getUrl(){
        return Environment.DIRECTORY_PICTURES;
    }

    private String getDate(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HHmmss-ddMMMMyyyy");
        return formatter.format(date);
    }
}