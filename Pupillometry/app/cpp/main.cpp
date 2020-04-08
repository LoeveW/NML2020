//
//  main.cpp
//  EyeFatigue
//
//  Created by Vincent on 6/2/15.
//  Copyright (c) 2015 Vincent. All rights reserved.
//



#include <iostream>
#include <opencv2/opencv.hpp>
#include <fstream>

#include <jni.h>
#include <string>

#include <opencv2/highgui/highgui.hpp>
using namespace cv;
using namespace std;

enum EdgeDetection {SobelDetection, CannyDetection};

RNG rng(12345);
CascadeClassifier faceCascade, eyeCascade;
String face_cascade_name = "/Users/vincent/opencv/data/haarcascades/haarcascade_frontalface_default.xml";
String eye_cascade_name = "/Users/vincent/opencv/data/haarcascades/haarcascade_eye_tree_eyeglasses.xml";
Mat bgr_img;


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
JNICALL
void testConnection(){
    cout << "connection test succeed";
}

int mirroredPointsIntensity(Mat inputImg, Point center, int x, int y, bool partialCircumference){
    int intensity = 0;
    
    // 1st octant
    intensity += inputImg.at<uchar>(center.y + x, center.x + y);
    // 4th octant
    intensity += inputImg.at<uchar>(center.y - x, center.x + y);
    // 5th octant
    intensity += inputImg.at<uchar>(center.y - x, center.x - y);
    // 8th octant
    intensity += inputImg.at<uchar>(center.y + x, center.x - y);
    
    if (!partialCircumference) {
        // 2nd octant
        intensity += inputImg.at<uchar>(center.y + y, center.x + x);
        // 3rd octant
        intensity += inputImg.at<uchar>(center.y + y, center.x - x);
        // 6th octant
        intensity += inputImg.at<uchar>(center.y - y, center.x - x);
        // 7th octant
        intensity += inputImg.at<uchar>(center.y - y, center.x + x);
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
int MidpointCircleAlgorithm(Mat inputImg, Point center, int r, bool partialCircumference) {
    int intensity = 0;
    int x = 0;
    int y = r;
    intensity += mirroredPointsIntensity(inputImg, center, x, y, partialCircumference);
    
    float p = float(5)/4 - r;
    
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
void partialDerivative(Mat inputImg, int x, int y, int rMin, int rMax, float sigma, int &radius, double &derivative, bool partialCircumference) {
    vector<double> lineIntegrals;
    for(int r=rMin; r<rMax; r++){
        double lineIntegral = double(MidpointCircleAlgorithm(inputImg, Point(x, y), r, partialCircumference))/(2*M_PI*r);
        lineIntegrals.push_back(lineIntegral);
    }
    
    vector<double> derivatives;
    derivatives.push_back(0);
    for(int i=1; i<lineIntegrals.size(); i++){
        double derivative = lineIntegrals[i] - lineIntegrals[i-1];
        derivatives.push_back(derivative);
    }
    
    vector<double> smoothedDerivatives;
    GaussianBlur(derivatives, smoothedDerivatives, Size(5, 1), 1);
    
    int pos = (int) (max_element(smoothedDerivatives.begin(), smoothedDerivatives.end()) - smoothedDerivatives.begin());
    
    derivative = smoothedDerivatives[pos];
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
void DaugmanOperator(Mat inputImg, int xMin, int xMax, int yMin, int yMax, int rMin, int rMax, Mat mask, Point &center, int &radius, bool partialCircumference=true){

    Mat radii = Mat::zeros(inputImg.rows, inputImg.cols, CV_32S);
    Mat derivatives = Mat::zeros(inputImg.rows, inputImg.cols, CV_64F);
    
    for(int row=yMin; row<=yMax; row++){
        for(int col=xMin; col<=xMax; col++){
            
            // No circles outside the image
            int toBottom = inputImg.rows - row;
            int toRight = inputImg.cols - col;

            int scanRange = min(min(min(col, toRight), min(row, toBottom)), rMax);
            if ( scanRange >= 10 && mask.at<uchar>(row, col)>0 ){
            
                int r;
                double derivative;
                // Use x, y to avoid confusion
                int x = col;
                int y = row;
                partialDerivative(inputImg, x, y, rMin, scanRange, 0.5, r, derivative, partialCircumference);
                radii.at<int>(row,col) = r;
                derivatives.at<double>(row,col) = derivative;
            }
        }
    }

    double minVal, maxVal;
    Point minLoc, maxLoc;
    minMaxLoc(derivatives, &minVal, &maxVal, &minLoc, &maxLoc);
    
    center = maxLoc;
    // maxLoc is using (col, row) system
    radius = radii.at<int>(maxLoc.y, maxLoc.x);
}



/**
 * Remove specular reflection using morphological operation.
 */
Mat removeReflection(Mat eyeImg) {
    
    Mat whiteImg = Mat::ones(eyeImg.size(), eyeImg.type())*255;
    Mat invertImg;
    subtract(whiteImg, eyeImg, invertImg);
    Mat element = getStructuringElement(MORPH_ELLIPSE, Size(eyeImg.rows/10, eyeImg.rows/10));
    morphologyEx(invertImg, invertImg, MORPH_CLOSE, element);
    Mat morphImg;
    subtract(whiteImg, invertImg, morphImg);
    imshow("morph", morphImg);
    return morphImg;
}


/**
 * Get the scanning mask to iris detection
 */
Mat getScanMask(Mat inputImg) {
    
    Scalar mean, stddev;
    meanStdDev(inputImg, mean, stddev);
    double thresholdVal = mean[0] - stddev[0];
    Mat scanMask;
    threshold(inputImg, scanMask, thresholdVal, 255, THRESH_BINARY_INV);
    return scanMask;
}



/**
 * Iris detection
 */
void detectIris(Mat bgr_Img, Mat &eye_bgr_img, Point &center, int &radius) {
    
    Mat gray_img;
    cvtColor(bgr_img, gray_img, CV_BGR2GRAY);
    
    eyeCascade  = CascadeClassifier(eye_cascade_name);
    vector<Rect> eyeRects;
    eyeCascade.detectMultiScale(gray_img, eyeRects, 1.1, 2, CV_HAAR_SCALE_IMAGE, Size(20,20));
    cout << "Finished Eye Detection!" << endl;
    
    vector<Point> centers;
    vector<int> radii;
    for(int i=0; i<eyeRects.size(); i++) {
        Mat eye_gray_img = gray_img(eyeRects[i]);
        Mat morphedImg = removeReflection(eye_gray_img);
        Mat irisScanMask = getScanMask(morphedImg);
        Point center;
        int radius;
        // The radius of iris is usually at least greater than 1/8-1/4 height of the eye
        DaugmanOperator(eye_gray_img, 0, eye_gray_img.cols, 0, eye_gray_img.rows, eye_gray_img.rows/8, eye_gray_img.rows/2, irisScanMask, center, radius, false);
        centers.push_back(center);
        radii.push_back(radius);
        cout << "center.x: " << center.x << " center.y: " << center.y << endl;
        cout << "radius: " << radius << endl;
        
        Mat eye_bgr_img = bgr_img(eyeRects[i]);
        circle(eye_bgr_img, center, radius, Scalar(255, 255, 255));
        imshow(to_string(i), eye_bgr_img);
    }
    
    if (eyeRects.size() > 0){
        // The circle with the largest radius is usually the iris
        long pos = max_element(radii.begin(), radii.end()) - radii.begin();
        eye_bgr_img = bgr_img(eyeRects[pos]);
        center = centers[pos];
        radius = radii[pos];
    }
    else{
        radius = 0;
    }
}



/**
 * Pupil detection
 */
void detectPupil(Mat eye_bgr_img, Point irisCenter, int irisRadius, Point &pupilCenter, int &pupilRadius) {
    
    Mat eye_gray_img;
    cvtColor(eye_bgr_img, eye_gray_img, CV_BGR2GRAY);
    imshow("Before", eye_gray_img);
    equalizeHist(eye_gray_img, eye_gray_img);
    imshow("EqualHist", eye_gray_img);
    
    Mat pupilScanMask = Mat::zeros(eye_gray_img.size(), CV_8U);
    int scanWidth = std::min(irisRadius, 5);
    for(int row=irisCenter.y-scanWidth; row<irisCenter.y+scanWidth; row++){
        for(int col=irisCenter.x-scanWidth; col<irisCenter.x+scanWidth; col++){
            pupilScanMask.at<uchar>(row,col) = 255;
        }
    }
    
    DaugmanOperator(eye_gray_img, 0, eye_gray_img.cols, 0, eye_gray_img.rows, irisRadius*0.15, irisRadius*0.6, pupilScanMask, pupilCenter, pupilRadius, true);
    
    cout << "***Pupil Detection***" << endl;
    cout << "x:" << pupilCenter.x << " y:" << pupilCenter.y << endl;
    cout << "radius:" << pupilRadius << endl;
}


/**
 * Pupil segmentation. It first detects the iris and then detects pupil inside the iris.
 * 
 * @param
 * filename: The filename of the output image. If it is null, no image will be generated.
 */
void pupilSegmentation(Mat bgr_img, int& irisRadius, int& pupilRadius, const string filename = "") {
    
    Mat eye_bgr_img;
    Point irisCenter, pupilCenter;
    
    detectIris(bgr_img, eye_bgr_img, irisCenter, irisRadius);
    
    if (irisRadius > 0){
        detectPupil(eye_bgr_img, irisCenter, irisRadius, pupilCenter, pupilRadius);
        circle(eye_bgr_img, pupilCenter, pupilRadius, Scalar(0, 255, 0));
//        imshow("Detected Pupil", eye_bgr_img);
    }
    else{
        cout << "No Iris Found!" << endl;
    }
    
    if(filename != ""){
        imwrite(filename, eye_bgr_img);
    }
    
}


/**
 * Apply pupil segmentation algorithm to all the passively captured images in the folder.
 *
 * @param
 * path: the path to the folder
 */
void pupilSegmentationForPassivelyCapturedImages(const string path) {
    
    vector<string> imageNames;
    ofstream csvFile;
    string csvFilename = path + "/passive_segmentation.csv";
    csvFile.open(csvFilename);
    csvFile << "timestamp,outer,inner" << endl;
    
    glob(path, imageNames);
    for (int i=0; i<imageNames.size(); i++){
        
        // Tokenize the filename and check if it's image captured when phone was unlocked
        char *cstr = new char[imageNames[i].size() + 1];
        strcpy(cstr, imageNames[i].c_str());
        vector<string> tokens;
        char *token = strtok(cstr, "/");
        while (token != NULL) {
            tokens.push_back(token);
            token = strtok(NULL, "/_.");
        }
        
        if (tokens.size()>=8 && tokens[4] == "snapshot" && tokens[7] == "jpg"){
            string outFilename = imageNames[i].substr(0,20) + "/PassiveSegmentation/" + tokens[3] + "/snapshotSeg_" + tokens[5] + "_" + tokens[6] + "." + tokens[7];
            cout << outFilename << endl;
            bgr_img = imread(imageNames[i]);
            
            // Rotate the image by 90 degress counter-clockwisely
            flip(bgr_img, bgr_img, 1);
            transpose(bgr_img, bgr_img);
            
            int irisRadius, pupilRadius;
            pupilSegmentation(bgr_img, irisRadius, pupilRadius, outFilename);
            
            if (csvFile.is_open()){
                csvFile << tokens[6] << "," << irisRadius << "," << pupilRadius << endl;
            }
            else{
                cout << "Unable to open the file" << endl;
            }
        }
        
    }
    
    
    cout << "Pupil segmentaiton for passively captured images is completed!" << endl;
}


/**
 * Apply pupil segmentation algorithm to all the manually captured images in the folder.
 *
 * @param
 * path: the path to the folder
 */
void pupilSegmentationForManuallyCacputredImages(const string path) {
    vector<string> imageNames;
    ofstream csvFile;
    string csvFilename = path + "/pupil_segmentation.csv";
    csvFile.open(csvFilename);
    csvFile << "timestamp,outer,inner" << endl;
    
    // Retrieve all the matching patterns
    glob(path, imageNames);
    for (int i=0; i<imageNames.size(); i++){
        
        // Tokenize the filename and check if it's pupil image
        char *cstr = new char[imageNames[i].size() + 1];
        strcpy(cstr, imageNames[i].c_str());
        vector<string> tokens;
        char *token = strtok(cstr, "/");
        while (token != NULL) {
            tokens.push_back(token);
            token = strtok(NULL, "/_.");
        }
        
        if (tokens.size()>=8 && tokens[4] == "pupil" && tokens[7] == "jpg"){
            cout << imageNames[i] << endl;
            string outFilename = imageNames[i].substr(0,20) + "/segmentation/" + tokens[3] + "/pupilSeg_" + tokens[5] + "_" + tokens[6] + "." + tokens[7];
            cout << outFilename << endl;
            bgr_img = imread(imageNames[i]);
            
            // Downsize the image if the image is taken using back-facing camera
            if (bgr_img.cols > 2000){
                resize(bgr_img, bgr_img, Size(bgr_img.cols/2.5, bgr_img.rows/2.5));
            }
            
            int irisRadius, pupilRadius;
            pupilSegmentation(bgr_img, irisRadius, pupilRadius, outFilename);
            
            if (csvFile.is_open()){
                csvFile << tokens[6] << "," << irisRadius << "," << pupilRadius << endl;
            }
            else{
                cout << "Unable to open file" << endl;
            }
        }
        
    }
    
    csvFile.close();
    cout << "Pupil Segmentation for manually captured images is completed!" << endl;
}


/**
 * Apply pupil segmentation algorithm to all the pilot study images in the folder.
 *
 * @param
 * path: the path to the folder
 */
void pupilSegmentationForPilotStudyImages(const string path) {
    
    vector<string> imageNames;
    ofstream csvFile;
    string csvFilename = path + "/passive_segmentation.csv";
    csvFile.open(csvFilename);
    csvFile << "timestamp,outer,inner" << endl;
    
    glob(path, imageNames);
    for (int i=0; i<imageNames.size(); i++){
        
        cout << imageNames[i] << endl;
        // Tokenize the filename and check if it's image captured when phone was unlocked
        char *cstr = new char[imageNames[i].size() + 1];
        strcpy(cstr, imageNames[i].c_str());
        vector<string> tokens;
        char *token = strtok(cstr, "/");
        while (token != NULL) {
            tokens.push_back(token);
            token = strtok(NULL, "/_.");
        }
        
        if (tokens.size() == 10 && tokens[9] == "jpg"){
            string outFilename = imageNames[i].substr(0,37) + "/PassiveSegmentation/" + "Vincent_IR_" + tokens[8] + "." + tokens[9];
            cout << outFilename << endl;
            bgr_img = imread(imageNames[i]);
            
            // Rotate the image by 90 degress counter-clockwisely
//            flip(bgr_img, bgr_img, 1);
//            transpose(bgr_img, bgr_img);
            
            int irisRadius, pupilRadius;
            pupilSegmentation(bgr_img, irisRadius, pupilRadius, outFilename);
            
            if (csvFile.is_open()){
                csvFile << tokens[8] << "," << irisRadius << "," << pupilRadius << endl;
            }
            else{
                cout << "Unable to open the file" << endl;
            }
        }
        
    }
    
    
    cout << "Pupil segmentaiton for passively captured images is completed!" << endl;
}


int main(int argc, const char * argv[]) {
    
    bool videoMode = false;

    if (!videoMode){
//        // Read images from matlab image folder
//        bgr_img = imread("/Users/vincent/study/P12/pupil_359125052641992_1472904156.jpg");
//
//        if(bgr_img.empty())
//        {
//            fprintf(stderr, "failed to load input image\n");
//            return -1;
//        }
//
//        // Size should be in the form of (width, height)
//        resize(bgr_img, bgr_img, Size(bgr_img.cols/2.5, bgr_img.rows/2.5));
//        
//        // Rotate image with 270 degrees if it's rotated
//        bool needRotate = false;
//        if (needRotate) {
//            flip(bgr_img, bgr_img, 1);
//            transpose(bgr_img, bgr_img);
//        }
//        
//        int irisRadius, pupilRadius;
//        pupilSegmentation(bgr_img, irisRadius, pupilRadius);
        
        
//        pupilSegmentationForPassivelyCapturedImages("/Users/vincent/study/P12");
        pupilSegmentationForPilotStudyImages("/Users/vincent/Desktop/FatiguScanner/IR_Vincent");
        
        
        waitKey(0);
        return 0;
    }
    else{
        VideoCapture cap("/Users/vincent/Desktop/EyeFatigue/EyeFatigue/IMG_9448.MOV");
//        VideoCapture cap("/Users/vincent/Desktop/EyeFatigue/EyeFatigue/test2.avi");
        
        if(!cap.isOpened()) {
            cout<<"Cannot open the video file"<<endl;
            return -1;
        }
        
        cap.get(CV_CAP_PROP_FPS);
        namedWindow("Video", CV_WINDOW_AUTOSIZE);
        Mat frame;
        double width = cap.get(CV_CAP_PROP_FRAME_WIDTH);
        double height = cap.get(CV_CAP_PROP_FRAME_HEIGHT);
        cout << width  << "," << height << endl;
        Size frameSize(static_cast<int>(width), static_cast<int>(height));
        cout << cap.get(CV_CAP_PROP_FPS) << endl;
        
        VideoWriter oVideoWriter("/Users/vincent/Desktop/EyeFatigue/EyeFatigue/out.mov",
                                 CV_FOURCC('8', 'B', 'P', 'S'),
                                 cap.get(CV_CAP_PROP_FPS),
                                 frameSize);
        
        if (!oVideoWriter.isOpened()){
            cout << "Error: Failed to write the video" << endl;
            return -1;
        }
        
        while (1) {
            cap >> frame;
            if(frame.empty()){
                break;
            }
            
            imshow("Video", frame);
            oVideoWriter << frame;
    
            if (waitKey(30)==27)
            {
                cout<<"esc key is pressed by user"<<endl;
                break;
            }
        }
        
        return 0;
    }
}
