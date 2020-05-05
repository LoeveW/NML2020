package com.example.pupildilation;

import android.graphics.Point;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.lang.Math.*;
import java.util.Vector;

import static org.opencv.imgproc.Imgproc.GaussianBlur;

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
        for(int r=rMin; r<rMax; r++){
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

        //derivatives and smoothedDerivatives should be of type Mat.
        GaussianBlur(derivatives, smoothedDerivatives, ksize, 1);

        int pos = (int) (Collections.max(smoothedDerivatives) - smoothedDerivatives.get(0)); //not sure if we need the pointers.. didn't know how to do it.

        derivative = smoothedDerivatives.get(pos);
        radius = rMin + pos;
    }
}
