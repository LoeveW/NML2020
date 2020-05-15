package com.example.pupildilation;

import android.graphics.Point;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.lang.Math.*;
import java.util.Vector;

import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.circle;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.morphologyEx;
import static org.opencv.imgproc.Imgproc.threshold;

public class PupilDetection {
    static {
        System.loadLibrary("openCV_java");
    }

    Random rng = new Random(12345);

    Mat bgr_img = new Mat();
    String eye_cascade_name = "path to haarcascade_eye_tree_eyeglasses.xml";

    private void testConnection() {
        System.out.println("Connection succesful");
    }

    /**
     * Find the mirrored points on the other octants for Midpoint Circle Algorithm, and calculate the sum of
     * these pixels' intenstiy.
     * <p>
     * Parameters
     * ----------
     * partialCircumference: To avoid occlusion, only calculate pixel intensity in 1st, 4th, 5th and 8th octant.
     * <p>
     * Return
     * ------
     * intenstiy: The sum of the pixels' intensity.
     */
    private int mirroredPointsIntensity(Mat inputImg, Point center, int x, int y, boolean partialCircumference) {
        //so this MAT is also a problem
        int intensity = 0;

        // 1st octant
        intensity += inputImg.get(center.y + x, center.x + y)[0];
        // 4th octant
        intensity += inputImg.get(center.y - x, center.x + y)[0];
        // 5th octant
        intensity += inputImg.get(center.y - x, center.x - y)[0];
        // 8th octant
        intensity += inputImg.get(center.y + x, center.x - y)[0];

        if (!partialCircumference) {
            // 2nd octant
            intensity += inputImg.get(center.y + y, center.x + x)[0];
            // 3rd octant
            intensity += inputImg.get(center.y + y, center.x - x)[0];
            // 6th octant
            intensity += inputImg.get(center.y - y, center.x - x)[0];
            // 7th octant
            intensity += inputImg.get(center.y - y, center.x + x)[0];
        }

        return intensity;
    }

    /**
     * Midpoint Circle Algorithm
     * An efficeint way to calculate the coordinates of the pixels, which lie on the circumference of the circle.
     * <p>
     * Parameters
     * ----------
     * center: The center of the circle.
     * r: The radius of the circle.
     * <p>
     * <p>
     * Return
     * ------
     * intensity: The sum of the pixel intensity on the outline of the circle.
     */
    int MidpointCircleAlgorithm(Mat inputImg, Point center, int r, boolean partialCircumference) {
        int intensity = 0;
        int x = 0;
        int y = r;
        intensity += mirroredPointsIntensity(inputImg, center, x, y, partialCircumference);

        float p;
        p = (float) (5 / 4 - r);

        while (x < y) {
            if (p <= 0) {
                x += 1;
                p += 2 * x + 3;
            } else {
                x += 1;
                y -= 1;
                p += 2 * (x - y) + 5;
            }

            intensity += mirroredPointsIntensity(inputImg, center, x, y, partialCircumference);
        }

        return intensity;
    }

    /**
     * Given a center pixel, this function finds the radius where the maximum partial derivate along the radius occurs.
     *
     * @param radius: The radius of the circle that has the greatest contrast
     *                derivative: The maximum particaderivative
     * @return rOptimal: The radius where maximum partial derivative occurs.
     */
    void partialDerivative(Mat inputImg, int x, int y, int rMin, int rMax, float sigma, int radius, double _derivative, boolean partialCircumference) {
        ArrayList<Double> lineIntegrals = new ArrayList<>();
        double pi = Math.PI;
        for (int r = rMin; r < rMax; r++) {
            double lineIntegral = (double) (MidpointCircleAlgorithm(inputImg, new Point(x, y), r, partialCircumference)) / (2 * pi * r);
            lineIntegrals.add(lineIntegral);
        }

        ArrayList<Double> derivatives = new ArrayList<>();
        derivatives.add(0.0);
        double derivative;
        for (int i = 1; i < lineIntegrals.size(); i++) {
            derivative = lineIntegrals.get(i) - lineIntegrals.get(i - 1);
            derivatives.add(derivative);
        }

        ArrayList<Double> smoothedDerivatives = new ArrayList<>();

        final Size ksize = new Size(5, 1);

        //derivatives and smoothedDerivatives should be of type Mat??
        GaussianBlur(derivatives, smoothedDerivatives, ksize, 1);

        int pos = (int) (Collections.max(smoothedDerivatives) - smoothedDerivatives.get(0)); //not sure if we need the pointers.. didn't know how to do it.

        derivative = smoothedDerivatives.get(pos);
        radius = rMin + pos;
    }

    /**
     * Daugman's Operator
     * It scans the pixels based on the pixel map, and searchers for the circle that has the greastest intensity change
     * on its circumference.
     *
     * @param center: Center of the circle
     *                radius: Radius of the cirle
     *                mask: Mask indiciating whether the pixel might be the potential centroid of the circle
     */
    void DaugmanOperator(Mat inputImg, int xMin, int xMax, int yMin, int yMax, int rMin, int rMax, Mat mask, Point _center, int _radius, boolean partialCircumference) {
        //partialCircumference = true;
        Mat radii = Mat.zeros(inputImg.rows(), inputImg.cols(), CvType.CV_32S);
        Mat derivatives = Mat.zeros(inputImg.rows(), inputImg.cols(), CvType.CV_64F);

        for (int row = yMin; row <= yMax; row++) {
            for (int col = xMin; col <= xMax; col++) {

                // No circles outside the image
                int toBottom = inputImg.rows() - row;
                int toRight = inputImg.cols() - col;

                int scanRange = Math.min(Math.min(Math.min(col, toRight), Math.min(row, toBottom)), rMax);
                if (scanRange >= 10 && mask.get(row, col)[0] > 0) {

                    int r = 0;
                    double derivative = 0.0;
                    // Use x, y to avoid confusion
                    int x = col;
                    int y = row;
                    partialDerivative(inputImg, x, y, rMin, scanRange, 0.5f, r, derivative, partialCircumference);
                    radii.get(row, col)[0] = r;
                    derivatives.get(row, col)[0] = derivative;
                }
            }
        }

        double minVal, maxVal;
        org.opencv.core.Point minLoc, maxLoc;
//        minMaxLoc(derivatives, _minVal, _maxVal, _minLoc, _maxLoc);
        maxLoc = Core.minMaxLoc(derivatives).maxLoc;
        minLoc = Core.minMaxLoc(derivatives).minLoc;
        maxVal = Core.minMaxLoc(derivatives).maxVal;
        minVal = Core.minMaxLoc(derivatives).minVal;

        org.opencv.core.Point center = maxLoc;
        // maxLoc is using (col, row) system
        double radius = radii.get((int) maxLoc.y, (int) maxLoc.x)[0];
    }

    /**
     * Remove specular reflection using morphological operation.
     */
    Mat removeReflection(Mat eyeImg) {

        Mat _whiteImg = Mat.ones(eyeImg.size(), eyeImg.type());
        Mat whiteImg = new Mat(eyeImg.rows(), eyeImg.cols(), eyeImg.type());
        _whiteImg.convertTo(whiteImg, 255); //looks like this is fine?

        Mat invertImg = new Mat();
        Core.subtract(whiteImg, eyeImg, invertImg);
        Mat element = getStructuringElement(MORPH_ELLIPSE, new Size(eyeImg.rows() / 10, eyeImg.rows() / 10));
        morphologyEx(invertImg, invertImg, Imgproc.MORPH_CLOSE, element);
        Mat morphImg = new Mat();
        Core.subtract(whiteImg, invertImg, morphImg);
//        imshow("morph", morphImg);  //find imshow JAVA alternative
        return morphImg;
    }

    /**
     * Get the scanning mask to iris detection
     */
    Mat getScanMask(Mat inputImg) {

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(inputImg, mean, stddev);
        double thresholdVal = mean.toArray()[0] - stddev.toArray()[0];
        Mat scanMask = new Mat();
        double threshold = threshold(inputImg, scanMask, thresholdVal, 255, THRESH_BINARY_INV);
        return scanMask; //or return threshold??
    }

    /**
     * Iris detection
     */
    void detectIris(Mat bgr_Img, Mat _eye_bgr_img, Point _center, int _radius) {

        Mat gray_img = new Mat();
        cvtColor(bgr_img, gray_img, Imgproc.COLOR_BGR2GRAY);

        CascadeClassifier eyeCascade = new CascadeClassifier(eye_cascade_name);
        ArrayList<Rect> eyeRects = new ArrayList<>();
        eyeCascade.detectMultiScale(gray_img, eyeRects, 1.1, 2, Objdetect.CV_HAAR_SCALE_IMAGE, new Size(20, 20));
        System.out.println("Finished Eye Detection!");

        ArrayList<Point> centers = new ArrayList<>();
        ArrayList<Integer> radii = new ArrayList<>();
        for (int i = 0; i < eyeRects.size(); i++) {
            Mat eye_gray_img = gray_img(eyeRects.get(i)); //Method call expected?
            Mat morphedImg = removeReflection(eye_gray_img);
            Mat irisScanMask = getScanMask(morphedImg);
            org.opencv.core.Point center = new org.opencv.core.Point();
            int radius;
            // The radius of iris is usually at least greater than 1/8-1/4 height of the eye
            DaugmanOperator(eye_gray_img, 0, eye_gray_img.cols(), 0, eye_gray_img.rows(), eye_gray_img.rows() / 8, eye_gray_img.rows() / 2, irisScanMask, _center, _radius, false);
            centers.add(_center);
            radii.add(_radius);
            System.out.println("center.x: " + _center.x + "center.y:" + _center.y);
            System.out.println("radius: " + _radius);

            Mat eye_bgr_img = bgr_img(eyeRects.get(i)); //Method call expected?
            circle(eye_bgr_img, center, _radius, new Scalar(255, 255, 255));
//            imshow(to_string(i), eye_bgr_img);
        }

        if (eyeRects.size() > 0) {
            // The circle with the largest radius is usually the iris
            int pos = Collections.max(radii) - radii.get(0);
            Mat eye_bgr_img = bgr_img(eyeRects[pos]); //Mat ?
            Point center = centers.get(pos);
            _radius = radii.get(pos);
        } else {
            _radius = 0;
        }
    }

    /**
     * Pupil detection
     */
    void detectPupil(Mat eye_bgr_img, Point irisCenter, int irisRadius, Point _pupilCenter, int _pupilRadius) {

        Mat eye_gray_img;
        cvtColor(eye_bgr_img, eye_gray_img, Imgproc.COLOR_BGR2GRAY);
//        imshow("Before", eye_gray_img);
        org.opencv.imgproc.Imgproc.equalizeHist(eye_gray_img, eye_gray_img);
//        imshow("EqualHist", eye_gray_img);

        Mat pupilScanMask = Mat.zeros(eye_gray_img.size(), CvType.CV_8U);
        int scanWidth = Math.min(irisRadius, 5);
        for (int row = irisCenter.y - scanWidth; row < irisCenter.y + scanWidth; row++) {
            for (int col = irisCenter.x - scanWidth; col < irisCenter.x + scanWidth; col++) {
                pupilScanMask.get(row, col)[0] = 255;
            }
        }
        int irisRadiusOne = (int) (irisRadius * 0.15);
        int irisRadiusTwo = (int) (irisRadius * 0.6);
        DaugmanOperator(eye_gray_img, 0, eye_gray_img.cols(), 0, eye_gray_img.rows(), irisRadiusOne, irisRadiusTwo, pupilScanMask, _pupilCenter, _pupilRadius, true);

        System.out.println("***Pupil Detection***");
        System.out.println("x: " + _pupilCenter.x + "y: " + _pupilCenter.y);
        System.out.println("radius: " + _pupilRadius);
    }


    /**
     * Pupil segmentation. It first detects the iris and then detects pupil inside the iris.
     *
     * @param filename: The filename of the output image. If it is null, no image will be generated.
     */
    void pupilSegmentation(Mat bgr_img, int _irisRadius, int _pupilRadius, String filename) {
        filename = "";
        Mat eye_bgr_img = new Mat();
        Point irisCenter = new Point();
        Point pupilCenter = new Point();

        detectIris(bgr_img, eye_bgr_img, irisCenter, _irisRadius);

        if (_irisRadius > 0) {
            detectPupil(eye_bgr_img, irisCenter, _irisRadius, pupilCenter, _pupilRadius);
            circle(eye_bgr_img, pupilCenter, _pupilRadius, new Scalar(0, 255, 0)); //Wrong Point type?
//        imshow("Detected Pupil", eye_bgr_img);
        } else {
            System.out.println("No Iris Found!");
        }

        if (filename != "") {
//            imwrite(filename, eye_bgr_img);

            //need imwrite method JAVA!
        }

    }


    /**
     * Apply pupil segmentation algorithm to all the passively captured images in the folder.
     *
     * @param path: the path to the folder
     */
    void pupilSegmentationForPassivelyCapturedImages(String path) {

        ArrayList<String> imageNames = new ArrayList<>();
//        FileOutputStream fos;
        String csvFilename = path + "/passive_segmentation.csv";
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(csvFilename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("CSV file not found in pupilSegmentationForPassivelyCapturedImages(String path) method!");
        }
        ;
//        csvFile << "timestamp,outer,inner" << endl; //write csv out

//        glob(path, imageNames); //find glob() alternative
        for (int i = 0; i < imageNames.size(); i++) {
            // Tokenize the filename and check if it's image captured when phone was unlocked
            char cstr = new char[imageNames.get(i).size() + 1]; //size?
            strcpy(cstr, imageNames.get(i).c_str());
            ArrayList<String> tokens = new ArrayList<>();
            char token = strtok(cstr, "/");
            while (token != null) {
                tokens.add(token);
                token = strtok(null, "/_.");
            }

            if (tokens.size() >= 8 && tokens.get(4) == "snapshot" && tokens.get(7) == "jpg") {
                String outFilename = imageNames.get(i).substring(0, 20) + "/PassiveSegmentation/"
                        + tokens.get(3) + "/snapshotSeg_" + tokens.get(5) + "_" + tokens.get(6)
                        + "." + tokens.get(7);
                System.out.println(outFilename);
//                bgr_img = imread(imageNames[i]); //need imread method JAVA

                // Rotate the image by 90 degress counter-clockwisely
                Core.flip(bgr_img, bgr_img, 1);
                Core.transpose(bgr_img, bgr_img);

                int irisRadius = 0;
                int pupilRadius = 0;
                pupilSegmentation(bgr_img, irisRadius, pupilRadius, outFilename);

                //how to do this?
//                if (csvFile.is_open()) {
//                    csvFile << tokens.get(6) << "," << irisRadius << "," << pupilRadius << endl;
//            } else {
//                System.out.println("Unable to open the file");
//            }
            }

        }
        System.out.println("Pupil segmentaiton for passively captured images is completed!");
    }


    /**
     * Apply pupil segmentation algorithm to all the manually captured images in the folder.
     *
     * @param path: the path to the folder
     */
    void pupilSegmentationForManuallyCapturedImages(String path) {
        ArrayList<String> imageNames = new ArrayList<>();
//        FileOutputStream fos;
        String csvFilename = path + "/passive_segmentation.csv";
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(csvFilename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("CSV file not found in pupilSegmentationForManuallyCapturedImages(String path) method!");
        }
        ;
//        csvFile << "timestamp,outer,inner" << endl;

        // Retrieve all the matching patterns
//        glob(path, imageNames);
        for (int i = 0; i < imageNames.size(); i++) {

            // Tokenize the filename and check if it's pupil image
            char cstr = new char[imageNames.get(i).size() + 1];
            strcpy(cstr, imageNames.get(i).c_str()); //strcpy to JAVA?
            ArrayList<String> tokens = new ArrayList<>();
            char token = strtok(cstr, "/"); //strtok to JAVA?
            while (token != null) {
                tokens.add(token);
                token = strtok(null, "/_.");
            }

            if (tokens.size() >= 8 && tokens.get(4) == "pupil" && tokens.get(7) == "jpg") {
                System.out.println(imageNames.get(i));
                String outFilename = imageNames.get(i).substring(0, 20) + "/segmentation/" + tokens.get(3) + "/pupilSeg_" + tokens.get(5) + "_" + tokens.get(6) + "." + tokens.get(7);
                System.out.println(outFilename);
//                bgr_img = imread(imageNames[i]);

                // Downsize the image if the image is taken using back-facing camera
                if (bgr_img.cols() > 2000) {
                    org.opencv.imgproc.Imgproc.resize(bgr_img, bgr_img, new Size(bgr_img.cols() / 2.5, bgr_img.rows() / 2.5));
                }

                int irisRadius = 0;
                int pupilRadius = 0;
                pupilSegmentation(bgr_img, irisRadius, pupilRadius, outFilename);

//                if (csvFile.is_open()) {
//                    csvFile << tokens.get(6) << "," << irisRadius << "," << pupilRadius << endl;
//            } else {
//                System.out.println("Unable to open the file");
//            }
            }
//    csvFile.close();
            System.out.println("Pupil segmentation for manually captured images is completed!");
        }

        /**
         * Apply pupil segmentation algorithm to all the pilot study images in the folder.
         *
         * @param path: the path to the folder
         */
        void pupilSegmentationForPilotStudyImages (String path){

            ArrayList<String> imageNames = new ArrayList<>();
//        FileOutputStream fos;
            String csvFilename = path + "/passive_segmentation.csv";
            try {
                BufferedReader csvReader = new BufferedReader(new FileReader(csvFilename));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("CSV file not found in pupilSegmentationForPilotStudyImages(String path) method!");
            }
//        glob(path,imageNames);
            for (int i = 0; i < imageNames.size(); i++) {
                System.out.println(imageNames.get(i));
                // Tokenize the filename and check if it's image captured when phone was unlocked
                char cstr = new char[imageNames.get(i).size() + 1];
                strcpy(cstr, imageNames.get(i).c_str());
                ArrayList<String> tokens = new ArrayList<>();
                char token = strtok(cstr, "/");
                while (token != null) {
                    tokens.add(token);
                    token = strtok(null, "/_.");
                }

                if (tokens.size() == 10 && tokens.get(9) == "jpg") {
                    String outFilename = imageNames.get.substr(0, 37) + "/PassiveSegmentation/" + "Vincent_IR_" + tokens.get(8) + "." + tokens.get(9);
                    System.out.println(outFilename);
//                bgr_img = imread(imageNames.get(i));

                    // Rotate the image by 90 degress counter-clockwisely
                    //these were commented out in the original code as well --wouter
//            flip(bgr_img, bgr_img, 1);
//            transpose(bgr_img, bgr_img);

                    int irisRadius = 0;
                    int pupilRadius = 0;
                    pupilSegmentation(bgr_img, irisRadius, pupilRadius, outFilename);

//                if (csvFile.is_open()) {
//                    csvFile << tokens[8] << "," << irisRadius << "," << pupilRadius << endl;
//            } else {
//                System.out.println("Unable to open the file");
//            }
                }

            }

            System.out.println("Pupil segmentation for passively captured images is completed!");
        }


    }
}
