/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.classification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Camera;
import android.media.Image;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;

import org.tensorflow.lite.examples.classification.env.Logger;
import org.tensorflow.lite.examples.classification.tflite.Classifier;
import org.tensorflow.lite.examples.classification.tflite.Classifier.Device;
import org.tensorflow.lite.examples.classification.tflite.Classifier.Model;

public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final float TEXT_SIZE_DIP = 10;
  private Bitmap rgbFrameBitmap = null;
  private Integer sensorOrientation;
  private Classifier classifier;

  private int countSideWalk = 0;
  private int countPoint = 0;
  private int countLinear = 0;
  private boolean vibeTigger = true;
  boolean rightImage = false;
  boolean leftImage = false;
  boolean rotateImage = false;















  private String detectText = ""; // 검출된 class 반환
  private boolean pointDetect = false;
  boolean RotationTigger = true;

  /** Input image size of the model along x axis. */
  private int imageSizeX;
  /** Input image size of the model along y axis. */
  private int imageSizeY;

  @Override
  protected int getLayoutId() {
    return R.layout.tfe_ic_camera_connection_fragment;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {

    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());

    recreateClassifier(getModel(), getDevice(), getNumThreads());
    if (classifier == null) {
      LOGGER.e("No classifier on preview!");
      return;
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
  }

  @Override
  protected void processImage() {
    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
    final int cropSize = Math.min(previewWidth, previewHeight);

    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            if (classifier != null) {
              final long startTime = SystemClock.uptimeMillis();
              final List<Classifier.Recognition> results =
                  classifier.recognizeImage(rgbFrameBitmap, sensorOrientation);

              Log.v("detect", "Detect:"+results);

              Message msg = handler.obtainMessage();
              handler.sendMessage(msg);

              if (results.get(0).getId().equals("not_braille")) {
                Log.v("detect", "" + countSideWalk );

                countSideWalk ++;
                countPoint = 0;
                countLinear =0;
              }
              else if (results.get(0).getId().equals("point")) {
                Log.v("detect", ""+countPoint);
                countPoint ++;
                countSideWalk =0;
                countLinear = 0;
              }
              else if (results.get(0).getId().equals("Braille")) {
                Log.v("detect", "쭉가아");
                //detectText = results.get(0).getId();
                countLinear ++;
                countSideWalk =0;
                countPoint = 0;
              }

              if (countSideWalk == 9){
                Log.v("detect", "점자블록 이탈");
                detectText = results.get(0).getId();
              }
              if (countPoint == 3){
                Log.v("detect", "점형블록");
                detectText = results.get(0).getId();
                vibeTigger = true;

              }
              if (countLinear == 2){
                Log.v("detect", "선형블록");
                rightImage =false;
                leftImage = false;
                rotateImage = false;
                right.setVisibility(View.INVISIBLE);
                left.setVisibility(View.INVISIBLE);
                rotate.setVisibility(View.INVISIBLE);

                vibeTigger = true;
                detectText = results.get(0).getId();
                if (RotationTigger && receiver.getN().equals(currentArrow)){
                  checkPoint = currentRotation;
                  for (int i =0; i < 4 ; i++){
                    BoundPoint[i] = checkPoint + 45 + i * 90;
                    if (BoundPoint[i] >= 360.0f ){
                      BoundPoint[i] -= 360.0f;
                    }
                  }
                  //Log.v("detect", "되니?");
                  RotationTigger = false;
                  //상수값을 담자
                  state = "직진하세요";
                  rightImage =false;
                  leftImage = false;
                  rotateImage = false;
                  right.setVisibility(View.INVISIBLE);
                  left.setVisibility(View.INVISIBLE);
                  rotate.setVisibility(View.INVISIBLE);
                  speech.say("직진하세요");
                }

                else if(receiver.getN().equals("왼쪽")){
                  if(currentArrow.equals("오른쪽")){
                    state = "반대 방향입니다. 뒤로 돌아주세요";
                    vibrator.vibrate(500);
                    rightImage =false;
                    leftImage = false;
                    rotateImage = true;
                    speech.say("반대 방향입니다. 뒤로 돌아주세요");
                  }
        /*else if (currentArrow.equals("직진")){
          //딜레이 줘야할듯? 일단 냅두자


        }*/
                }
                else if(receiver.getN().equals("오른쪽")){
                  if(currentArrow.equals("왼쪽")){
                    state = "반대 방향입니다. 뒤로 돌아주세요.";
                    vibrator.vibrate(500);
                    rightImage =false;
                    leftImage = false;
                    rotateImage = true;
                    speech.say("반대 방향입니다. 뒤로 돌아주세요");
                  }
                }
                else if(receiver.getN().equals("직진")){
                  if(currentArrow.equals("왼쪽")){
                    state = "직진으로 가야합니다. 오른쪽으로 돌아주세요";
                    vibrator.vibrate(500);
                    rightImage = true;
                    leftImage = false;
                    rotateImage = false;
                    speech.say("직진으로 가야합니다. 오른쪽으로 돌아주세요");
                  }
                  else if(currentArrow.equals("오른쪽")){
                    state = "직진으로 가야합니다. 왼쪽으로 돌아주세요";
                    vibrator.vibrate(500);
                    rightImage =false;
                    leftImage = true;
                    rotateImage = false;
                    speech.say("직진으로 가야합니다. 왼쪽으로 돌아주세요");
                  }
                }
              }




            }
            readyForNextImage();
          }
        });
  }

  @Override
  protected void onInferenceConfigurationChanged() {
    if (rgbFrameBitmap == null) {
      // Defer creation until we're getting camera frames.
      return;
    }
    final Device device = getDevice();
    final Model model = getModel();
    final int numThreads = getNumThreads();
    runInBackground(() -> recreateClassifier(model, device, numThreads));
  }

  private void recreateClassifier(Model model, Device device, int numThreads) {
    if (classifier != null) {
      LOGGER.d("Closing classifier.");
      classifier.close();
      classifier = null;
    }

    try {
      LOGGER.d(
          "Creating classifier (model=%s, device=%s, numThreads=%d)", model, device, numThreads);
      classifier = Classifier.create(this, model, device, numThreads);
    } catch (IOException | IllegalArgumentException e) {
      LOGGER.e(e, "Failed to create classifier.");
      runOnUiThread(
          () -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
          });
      return;
    }

    // Updates the input image size.
    imageSizeX = classifier.getImageSizeX();
    imageSizeY = classifier.getImageSizeY();
  }

  final Handler handler = new Handler(){ //여기에 ui작업
    public void handleMessage(Message msg){

      //네비게이션
      if(currentArrow.equals("직진") && pointDetect == false && detectText.equals("point")){
        pointDetect = true;
        RotationTigger = true;
        sendBroadcast(new Intent("next"));
      }
      else if(( pointDetect == true && detectText.equals("not_braille") ) || ( pointDetect == true && detectText.equals("Braille"))){
        pointDetect = false;
      }
      calsstext.setText(String.valueOf(detectText));


      //이탈방지
      if (detectText.equals("not_braille")){



        if(currentArrow.equals("왼쪽")){
          state = "점자블록을 이탈하였습니다. 오른쪽으로 돌아주세요";
          rightImage =true;
          leftImage = false;
          rotateImage = false;


          speech.say("점자블록을 이탈하였습니다. 오른쪽으로 돌아주세요");

        }
        else if(currentArrow.equals("오른쪽")){
          state = "점자블록을 이탈하였습니다. 왼쪽으로 돌아주세요";
          rightImage = false;
          leftImage = true;
          rotateImage = false;
          speech.say("점자블록을 이탈하였습니다. 왼쪽으로 돌아주세요");

        }
        else if (currentArrow.equals("직진")){
          if (pitch <= -70){
            speech.say("너무 위로 올렸습니다. 바닥을 비춰주세요");
            state = "" +pitch;
          }



        }
        //이친구 해야할듯
        if (vibeTigger){
        vibrator.vibrate(500);
        vibeTigger = false;
        }



      }
      // 이미지 변경

      if (rightImage){
        right.setVisibility(View.VISIBLE);
        left.setVisibility(View.INVISIBLE);
        rotate.setVisibility(View.INVISIBLE);

      }
      else if (leftImage){
        right.setVisibility(View.INVISIBLE);
        left.setVisibility(View.VISIBLE);
        rotate.setVisibility(View.INVISIBLE);

      }
      else if (rotateImage){
        right.setVisibility(View.INVISIBLE);
        left.setVisibility(View.INVISIBLE);
        rotate.setVisibility(View.VISIBLE);

      }






























    }
  };

  @Override
  public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

  }

  @Override
  public void onPointerCaptureChanged(boolean hasCapture) {

  }
}
