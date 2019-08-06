package com.reactlibrary.video;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import com.reactlibrary.video.MediaController.CompressProgressListener;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoCompress {

  private static final String TAG = VideoCompress.class.getSimpleName();

  public static VideoCompressTask compressVideo(String srcPath, String destPath, String quality,
      long startTime, long endTime, CompressListener listener) {
    int finalQuality = MediaController.COMPRESS_QUALITY_LOW;

    if (quality.equals("high")) {
      finalQuality = MediaController.COMPRESS_QUALITY_HIGH;
    } else if (quality.equals("medium")) {
      finalQuality = MediaController.COMPRESS_QUALITY_MEDIUM;
    }

    final VideoCompressTask task = new VideoCompressTask(listener, finalQuality, startTime, endTime);
    task.execute(srcPath, destPath);
    return task;
  }

  public interface CompressListener {

    void onStart();

    void onSuccess();

    void onFail();

    void onProgress(float percent);
  }

  public static class VideoCompressTask extends AsyncTask<String, Float, Boolean> {

    private final CompressListener listener;
    private final int quality;
    private final long startTime;
    private final long endTime;
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);

    public VideoCompressTask(CompressListener listener, int quality, long startTime, long endTime) {
      this.listener = listener;
      this.quality = quality;
      this.startTime = startTime;
      this.endTime = endTime;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      if (listener != null) {
        listener.onStart();
      }
    }

    @Override
    protected Boolean doInBackground(String... paths) {
      return MediaController.getInstance().convertVideo(
          paths[0],
          paths[1],
          quality,
          startTime,
          endTime,
          getListener(),
          isCancelled);
    }

    @NonNull
    private CompressProgressListener getListener() {
      return new CompressProgressListener() {
        @Override
        public void onProgress(float percent) {
          publishProgress(percent);
        }
      };
    }

    @Override
    protected void onProgressUpdate(Float... percent) {
      super.onProgressUpdate(percent);
      if (listener != null) {
        listener.onProgress(percent[0]);
      }
    }

    @Override
    protected void onPostExecute(Boolean result) {
      super.onPostExecute(result);
      if (listener != null) {
        if (result) {
          listener.onSuccess();
        } else {
          listener.onFail();
        }
      }
    }

    /**
     * Overloaded cancel method which interrupts ongoing task.
     */
    public void cancel() {
      Log.w(TAG, "Canceling video compression task.");
      isCancelled.set(true);

      /* Interrupt the compression task */
      super.cancel(true);
    }
  }
}
