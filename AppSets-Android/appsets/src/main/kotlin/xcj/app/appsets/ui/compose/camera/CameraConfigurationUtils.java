/*
 * Copyright (C) 2014 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xcj.app.appsets.ui.compose.camera;

import static xcj.app.starter.android.util.PurpleLoggerKt.PurpleLogger;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility methods for configuring the Android camera.
 *
 * @author Sean Owen
 */
@SuppressWarnings("deprecation") // camera APIs
public final class CameraConfigurationUtils {

  private static final String TAG = "CameraConfiguration";

  private static final Pattern SEMICOLON = Pattern.compile(";");

  private static final int MIN_PREVIEW_PIXELS = 480 * 320; // normal screen
  private static final float MAX_EXPOSURE_COMPENSATION = 1.5f;
  private static final float MIN_EXPOSURE_COMPENSATION = 0.0f;
  private static final double MAX_ASPECT_DISTORTION = 0.15;
  private static final int MIN_FPS = 10;
  private static final int MAX_FPS = 20;
  private static final int AREA_PER_1000 = 400;

  private CameraConfigurationUtils() {
  }

  public static void setFocus(Camera.Parameters parameters,
                              boolean autoFocus,
                              boolean disableContinuous,
                              boolean safeMode) {
    List<String> supportedFocusModes = parameters.getSupportedFocusModes();
    String focusMode = null;
    if (autoFocus) {
      if (safeMode || disableContinuous) {
        focusMode = findSettableValue("focus mode",
                                       supportedFocusModes,
                                       Camera.Parameters.FOCUS_MODE_AUTO);
      } else {
        focusMode = findSettableValue("focus mode",
                                      supportedFocusModes,
                                      Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
                                      Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
                                      Camera.Parameters.FOCUS_MODE_AUTO);
      }
    }
    // Maybe selected auto-focus but not available, so fall through here:
    if (!safeMode && focusMode == null) {
      focusMode = findSettableValue("focus mode",
                                    supportedFocusModes,
                                    Camera.Parameters.FOCUS_MODE_MACRO,
                                    Camera.Parameters.FOCUS_MODE_EDOF);
    }
    if (focusMode != null) {
      if (focusMode.equals(parameters.getFocusMode())) {
        PurpleLogger.getCurrent().d(TAG, "Focus mode already set to " + focusMode, null);
      } else {
        parameters.setFocusMode(focusMode);
      }
    }
  }

  public static void setTorch(Camera.Parameters parameters, boolean on) {
    List<String> supportedFlashModes = parameters.getSupportedFlashModes();
    String flashMode;
    if (on) {
      flashMode = findSettableValue("flash mode",
                                    supportedFlashModes,
                                    Camera.Parameters.FLASH_MODE_TORCH,
                                    Camera.Parameters.FLASH_MODE_ON);
    } else {
      flashMode = findSettableValue("flash mode",
                                    supportedFlashModes,
                                    Camera.Parameters.FLASH_MODE_OFF);
    }
    if (flashMode != null) {
      if (flashMode.equals(parameters.getFlashMode())) {
        PurpleLogger.getCurrent().d(TAG, "Flash mode already set to " + flashMode, null);
      } else {
        PurpleLogger.getCurrent().d(TAG, "Setting flash mode to " + flashMode, null);
        parameters.setFlashMode(flashMode);
      }
    }
  }

  public static void setBestExposure(Camera.Parameters parameters, boolean lightOn) {
    int minExposure = parameters.getMinExposureCompensation();
    int maxExposure = parameters.getMaxExposureCompensation();
    float step = parameters.getExposureCompensationStep();
    if ((minExposure != 0 || maxExposure != 0) && step > 0.0f) {
      // Set low when light is on
      float targetCompensation = lightOn ? MIN_EXPOSURE_COMPENSATION : MAX_EXPOSURE_COMPENSATION;
      int compensationSteps = Math.round(targetCompensation / step);
      float actualCompensation = step * compensationSteps;
      // Clamp value:
      compensationSteps = Math.max(Math.min(compensationSteps, maxExposure), minExposure);
      if (parameters.getExposureCompensation() == compensationSteps) {
        PurpleLogger.getCurrent().d(TAG, "Exposure compensation already set to " + compensationSteps + " / " + actualCompensation, null);
      } else {
        PurpleLogger.getCurrent().d(TAG, "Setting exposure compensation to " + compensationSteps + " / " + actualCompensation, null);
        parameters.setExposureCompensation(compensationSteps);
      }
    } else {
      PurpleLogger.getCurrent().d(TAG, "Camera does not support exposure compensation", null);
    }
  }

  public static void setBestPreviewFPS(Camera.Parameters parameters) {
    setBestPreviewFPS(parameters, MIN_FPS, MAX_FPS);
  }

  public static void setBestPreviewFPS(Camera.Parameters parameters, int minFPS, int maxFPS) {
    List<int[]> supportedPreviewFpsRanges = parameters.getSupportedPreviewFpsRange();
    PurpleLogger.getCurrent().d(TAG, "Supported FPS ranges: " + toString(supportedPreviewFpsRanges), null);
    if (supportedPreviewFpsRanges != null && !supportedPreviewFpsRanges.isEmpty()) {
      int[] suitableFPSRange = null;
      for (int[] fpsRange : supportedPreviewFpsRanges) {
        int thisMin = fpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
        int thisMax = fpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
        if (thisMin >= minFPS * 1000 && thisMax <= maxFPS * 1000) {
          suitableFPSRange = fpsRange;
          break;
        }
      }
      if (suitableFPSRange == null) {
        PurpleLogger.getCurrent().d(TAG, "No suitable FPS range?", null);
      } else {
        int[] currentFpsRange = new int[2];
        parameters.getPreviewFpsRange(currentFpsRange);
        if (Arrays.equals(currentFpsRange, suitableFPSRange)) {
          PurpleLogger.getCurrent().d(TAG, "FPS range already set to " + Arrays.toString(suitableFPSRange), null);
        } else {
          PurpleLogger.getCurrent().d(TAG, "Setting FPS range to " + Arrays.toString(suitableFPSRange), null);
          parameters.setPreviewFpsRange(suitableFPSRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                  suitableFPSRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
        }
      }
    }
  }

  public static void setFocusArea(Camera.Parameters parameters) {
    if (parameters.getMaxNumFocusAreas() > 0) {
      PurpleLogger.getCurrent().d(TAG, "Old focus areas: " + toString(parameters.getFocusAreas()), null);
      List<Camera.Area> middleArea = buildMiddleArea();
      PurpleLogger.getCurrent().d(TAG, "Setting focus area to : " + toString(middleArea), null);
      parameters.setFocusAreas(middleArea);
    } else {
      PurpleLogger.getCurrent().d(TAG, "Device does not support focus areas", null);
    }
  }

  public static void setMetering(Camera.Parameters parameters) {
    if (parameters.getMaxNumMeteringAreas() > 0) {
      PurpleLogger.getCurrent().d(TAG, "Old metering areas: " + parameters.getMeteringAreas(), null);
      List<Camera.Area> middleArea = buildMiddleArea();
      PurpleLogger.getCurrent().d(TAG, "Setting metering area to : " + toString(middleArea), null);
      parameters.setMeteringAreas(middleArea);
    } else {
      PurpleLogger.getCurrent().d(TAG, "Device does not support metering areas", null);
    }
  }

  private static List<Camera.Area> buildMiddleArea() {
    return Collections.singletonList(
        new Camera.Area(new Rect(-AREA_PER_1000, -AREA_PER_1000, AREA_PER_1000, AREA_PER_1000), 1));
  }

  public static void setVideoStabilization(Camera.Parameters parameters) {
    if (parameters.isVideoStabilizationSupported()) {
      if (parameters.getVideoStabilization()) {
        PurpleLogger.getCurrent().d(TAG, "Video stabilization already enabled", null);
      } else {
        PurpleLogger.getCurrent().d(TAG, "Enabling video stabilization...", null);
        parameters.setVideoStabilization(true);
      }
    } else {
      PurpleLogger.getCurrent().d(TAG, "This device does not support video stabilization", null);
    }
  }

  public static void setBarcodeSceneMode(Camera.Parameters parameters) {
    if (Camera.Parameters.SCENE_MODE_BARCODE.equals(parameters.getSceneMode())) {
      PurpleLogger.getCurrent().d(TAG, "Barcode scene mode already set", null);
      return;
    }
    String sceneMode = findSettableValue("scene mode",
                                         parameters.getSupportedSceneModes(),
                                         Camera.Parameters.SCENE_MODE_BARCODE);
    if (sceneMode != null) {
      parameters.setSceneMode(sceneMode);
    }
  }

  public static void setZoom(Camera.Parameters parameters, double targetZoomRatio) {
    if (parameters.isZoomSupported()) {
      Integer zoom = indexOfClosestZoom(parameters, targetZoomRatio);
      if (zoom == null) {
        return;
      }
      if (parameters.getZoom() == zoom) {
        PurpleLogger.getCurrent().d(TAG, "Zoom is already set to " + zoom, null);
      } else {
        PurpleLogger.getCurrent().d(TAG, "Setting zoom to " + zoom, null);
        parameters.setZoom(zoom);
      }
    } else {
      PurpleLogger.getCurrent().d(TAG, "Zoom is not supported", null);
    }
  }

  private static Integer indexOfClosestZoom(Camera.Parameters parameters, double targetZoomRatio) {
    List<Integer> ratios = parameters.getZoomRatios();
    PurpleLogger.getCurrent().d(TAG, "Zoom ratios: " + ratios, null);
    int maxZoom = parameters.getMaxZoom();
    if (ratios == null || ratios.isEmpty() || ratios.size() != maxZoom + 1) {
      PurpleLogger.getCurrent().w(TAG, "Invalid zoom ratios!", null);
      return null;
    }
    double target100 = 100.0 * targetZoomRatio;
    double smallestDiff = Double.POSITIVE_INFINITY;
    int closestIndex = 0;
    for (int i = 0; i < ratios.size(); i++) {
      double diff = Math.abs(ratios.get(i) - target100);
      if (diff < smallestDiff) {
        smallestDiff = diff;
        closestIndex = i;
      }
    }
    PurpleLogger.getCurrent().d(TAG, "Chose zoom ratio of " + (ratios.get(closestIndex) / 100.0), null);
    return closestIndex;
  }

  public static void setInvertColor(Camera.Parameters parameters) {
    if (Camera.Parameters.EFFECT_NEGATIVE.equals(parameters.getColorEffect())) {
      PurpleLogger.getCurrent().d(TAG, "Negative effect already set", null);
      return;
    }
    String colorMode = findSettableValue("color effect",
                                         parameters.getSupportedColorEffects(),
                                         Camera.Parameters.EFFECT_NEGATIVE);
    if (colorMode != null) {
      parameters.setColorEffect(colorMode);
    }
  }

  public static Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution) {

    List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
    if (rawSupportedSizes == null) {
      PurpleLogger.getCurrent().w(TAG, "Device returned no supported preview sizes; using default", null);
      Camera.Size defaultSize = parameters.getPreviewSize();
      if (defaultSize == null) {
        throw new IllegalStateException("Parameters contained no preview size!");
      }
      return new Point(defaultSize.width, defaultSize.height);
    }

    if (Log.isLoggable(TAG, Log.DEBUG)) {
      StringBuilder previewSizesString = new StringBuilder();
      for (Camera.Size size : rawSupportedSizes) {
        previewSizesString.append(size.width).append('x').append(size.height).append(' ');
      }
      PurpleLogger.getCurrent().d(TAG, "Supported preview sizes: " + previewSizesString, null);
    }

    double screenAspectRatio = screenResolution.x / (double) screenResolution.y;

    // Find a suitable size, with max resolution
    int maxResolution = 0;
    Camera.Size maxResPreviewSize = null;
    for (Camera.Size size : rawSupportedSizes) {
      int realWidth = size.width;
      int realHeight = size.height;
      int resolution = realWidth * realHeight;
      if (resolution < MIN_PREVIEW_PIXELS) {
        continue;
      }

      boolean isCandidatePortrait = realWidth < realHeight;
      int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
      int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;
      double aspectRatio = maybeFlippedWidth / (double) maybeFlippedHeight;
      double distortion = Math.abs(aspectRatio - screenAspectRatio);
      if (distortion > MAX_ASPECT_DISTORTION) {
        continue;
      }

      if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
        Point exactPoint = new Point(realWidth, realHeight);
        PurpleLogger.getCurrent().d(TAG, "Found preview size exactly matching screen size: " + exactPoint, null);
        return exactPoint;
      }

      // Resolution is suitable; record the one with max resolution
      if (resolution > maxResolution) {
        maxResolution = resolution;
        maxResPreviewSize = size;
      }
    }

    // If no exact match, use largest preview size. This was not a great idea on older devices because
    // of the additional computation needed. We're likely to get here on newer Android 4+ devices, where
    // the CPU is much more powerful.
    if (maxResPreviewSize != null) {
      Point largestSize = new Point(maxResPreviewSize.width, maxResPreviewSize.height);
      PurpleLogger.getCurrent().d(TAG, "Using largest suitable preview size: " + largestSize, null);
      return largestSize;
    }

    // If there is nothing at all suitable, return current preview size
    Camera.Size defaultPreview = parameters.getPreviewSize();
    if (defaultPreview == null) {
      throw new IllegalStateException("Parameters contained no preview size!");
    }
    Point defaultSize = new Point(defaultPreview.width, defaultPreview.height);
    PurpleLogger.getCurrent().d(TAG, "No suitable preview sizes, using default: " + defaultSize, null);
    return defaultSize;
  }

  private static String findSettableValue(String name,
                                          Collection<String> supportedValues,
                                          String... desiredValues) {
    PurpleLogger.getCurrent().d(TAG, "Requesting " + name + " value from among: " + Arrays.toString(desiredValues), null);
    PurpleLogger.getCurrent().d(TAG, "Supported " + name + " values: " + supportedValues, null);
    if (supportedValues != null) {
      for (String desiredValue : desiredValues) {
        if (supportedValues.contains(desiredValue)) {
          PurpleLogger.getCurrent().d(TAG, "Can set " + name + " to: " + desiredValue, null);
          return desiredValue;
        }
      }
    }
    PurpleLogger.getCurrent().d(TAG, "No supported values match", null);
    return null;
  }

  private static String toString(Collection<int[]> arrays) {
    if (arrays == null || arrays.isEmpty()) {
      return "[]";
    }
    StringBuilder buffer = new StringBuilder();
    buffer.append('[');
    Iterator<int[]> it = arrays.iterator();
    while (it.hasNext()) {
      buffer.append(Arrays.toString(it.next()));
      if (it.hasNext()) {
        buffer.append(", ");
      }
    }
    buffer.append(']');
    return buffer.toString();
  }

  private static String toString(Iterable<Camera.Area> areas) {
    if (areas == null) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    for (Camera.Area area : areas) {
      result.append(area.rect).append(':').append(area.weight).append(' ');
    }
    return result.toString();
  }

  public static String collectStats(Camera.Parameters parameters) {
    return collectStats(parameters.flatten());
  }

  public static String collectStats(CharSequence flattenedParams) {
    StringBuilder result = new StringBuilder(1000);
    appendStat(result, "BOARD", Build.BOARD);
    appendStat(result, "BRAND", Build.BRAND);
    appendStat(result, "CPU_ABI", Build.CPU_ABI);
    appendStat(result, "DEVICE", Build.DEVICE);
    appendStat(result, "DISPLAY", Build.DISPLAY);
    appendStat(result, "FINGERPRINT", Build.FINGERPRINT);
    appendStat(result, "HOST", Build.HOST);
    appendStat(result, "ID", Build.ID);
    appendStat(result, "MANUFACTURER", Build.MANUFACTURER);
    appendStat(result, "MODEL", Build.MODEL);
    appendStat(result, "PRODUCT", Build.PRODUCT);
    appendStat(result, "TAGS", Build.TAGS);
    appendStat(result, "TIME", Build.TIME);
    appendStat(result, "TYPE", Build.TYPE);
    appendStat(result, "USER", Build.USER);
    appendStat(result, "VERSION.CODENAME", Build.VERSION.CODENAME);
    appendStat(result, "VERSION.INCREMENTAL", Build.VERSION.INCREMENTAL);
    appendStat(result, "VERSION.RELEASE", Build.VERSION.RELEASE);
    appendStat(result, "VERSION.SDK_INT", Build.VERSION.SDK_INT);

    if (flattenedParams != null) {
      String[] params = SEMICOLON.split(flattenedParams);
      Arrays.sort(params);
      for (String param : params) {
        result.append(param).append('\n');
      }
    }

    return result.toString();
  }

  private static void appendStat(StringBuilder builder, String stat, Object value) {
    builder.append(stat).append('=').append(value).append('\n');
  }

}
