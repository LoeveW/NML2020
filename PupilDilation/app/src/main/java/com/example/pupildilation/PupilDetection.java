package com.example.pupildilation;

import android.graphics.Point;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.lang.Math.*;
import java.util.Vector;

import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.morphologyEx;

public class PupilDetection {
    static {
        System.loadLibrary("openCV_java");
    }
    Random rng = new Random(12345);

    Mat bgr_img = new Mat();

    private void testConnection(){
        System.out.println("Connection succesful");
    }

    /**
     * Find the mirrored points on the other octants for Midpoint Circle Algorithm, and calculate the sum of
     * these pixels' intenstiy.
     *
     * Parameters
     * ----------
     * partialCircumference: To avoid occlusion, only calculate pixel intensity in 1st, 4th, 5th and 8th octant.
     *
     * Return
     * ------
     * intenstiy: The sum of the pixels' intensity.
     */
    private int mirroredPointsIntensity(Mat inputImg, Point center, int x, int y, boolean partialCircumference){
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

    /** Midpoint Circle Algorithm
     * An efficeint way to calculate the coordinates of the pixels, which lie on the circumference of the circle.
     *
     * Parameters
     * ----------
     * center: The center of the circle.
     * r: The radius of the circle.
     *
     *
     * Return
     * ------
     * intensity: The sum of the pixel intensity on the outline of the circle.
     *
     */
    int MidpointCircleAlgorithm(Mat inputImg, Point center, int r, boolean partialCircumference) {
        int intensity = 0;
        int x = 0;
        int y = r;
        intensity += mirroredPointsIntensity(inputImg, center, x, y, partialCircumference);

        float p;
        p = (float) (5/4 - r);

        while (x < y) {
            if (p <= 0){
                x += 1;
                p += 2*x + 3;
            }
            else{
                x += 1;
                y -= 1;
                p += 2*(x - y) + 5;
            }

            intensity += mirroredPointsIntensity(inputImg, center, x, y, partialCircumference);
        }

        return intensity;
    }

    /**
     * Given a center pixel, this function finds the radius where the maximum partial derivate along the radius occurs.
     *
     * @param
     * radius: The radius of the circle that has the greatest contrast
     * derivative: The maximum particaderivative
     *
     * @return
     * rOptimal: The radius where maximum partial derivative occurs.
     */
    void partialDerivative(Mat inputImg, int x, int y, int rMin, int rMax, float sigma, int radius, double _derivative, boolean partialCircumference) {
        ArrayList<Double> lineIntegrals = new ArrayList<>();
        double pi = Math.PI;
        for(int r = rMin; r < rMax; r++){
            double lineIntegral = (double) (MidpointCircleAlgorithm(inputImg, new Point(x, y), r, partialCircumference))/(2 * pi * r);
            lineIntegrals.add(lineIntegral);
        }

        ArrayList<Double> derivatives = new ArrayList<>();
        derivatives.add(0.0);
        double derivative;
        for(int i=1; i<lineIntegrals.size(); i++){
            derivative = lineIntegrals.get(i) - lineIntegrals.get(i-1);
            derivatives.add(derivative);
        }

        ArrayList<Double> smoothedDerivatives = new ArrayList<>();

        final Size ksize = new Size(5,1);

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
     * @param
     * center: Center of the circle
     * radius: Radius of the cirle
     * mask: Mask indiciating whether the pixel might be the potential centroid of the circle
     */
    void DaugmanOperator(Mat inputImg, int xMin, int xMax, int yMin, int yMax, int rMin, int rMax, Mat mask, Point _center, int _radius, boolean partialCircumference){
        //partialCircumference = true;
        Mat radii = Mat.zeros(inputImg.rows(), inputImg.cols(), CvType.CV_32S);
        Mat derivatives = Mat.zeros(inputImg.rows(), inputImg.cols(), CvType.CV_64F);

        for(int row = yMin; row <= yMax; row++){
            for(int col = xMin; col <= xMax; col++){

                // No circles outside the image
                int toBottom = inputImg.rows() - row;
                int toRight = inputImg.cols() - col;

                int scanRange = Math.min(Math.min(Math.min(col, toRight), Math.min(row, toBottom)), rMax);
                if ( scanRange >= 10 && mask.get(row, col)[0] > 0 ){

                    int r = 0;
                    double derivative = 0.0;
                    // Use x, y to avoid confusion
                    int x = col;
                    int y = row;
                    partialDerivative(inputImg, x, y, rMin, scanRange, 0.5f, r, derivative, partialCircumference);
                    radii.get(row, col)[0] = r;
                    derivatives.get(row,col)[0] = derivative;
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
        Mat element = getStructuringElement(MORPH_ELLIPSE, new Size(eyeImg.rows()/10, eyeImg.rows()/10));
        morphologyEx(invertImg, invertImg, Imgproc.MORPH_CLOSE, element);
        Mat morphImg = new Mat();
        Core.subtract(whiteImg, invertImg, morphImg);
//        imshow("morph", morphImg);  //find imshow JAVA alternative
        return morphImg;
    }








}
