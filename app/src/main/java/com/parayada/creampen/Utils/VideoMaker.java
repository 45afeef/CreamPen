package com.parayada.creampen.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.parayada.creampen.Activity.MainActivity;
import com.parayada.creampen.Adapter.SliderAdapter;
import com.parayada.creampen.Model.Lesson;
import com.parayada.creampen.R;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class VideoMaker {
    private static final String TAG = "VideoMakerLog";
    private static final int PICTURE_ITEM_VIEW_TYPE = 100;
    private static final int MCQ_ITEM_VIEW_TYPE = 101;
    private static final int TEXT_ITEM_VIEW_TYPE  = 102;
    private static final int width = 1280; //1920
    private static final int height = 720; //1080


    public static void makeVideoFromLessonId(String lessonId, Context mContext,View view){
        Log.d(TAG, "makeVideo: lessonId :"+ lessonId);

        // First get lesson details from the firebase document
        FirebaseFirestore.getInstance().document("Lessons/"+lessonId)
                .get(Source.CACHE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Lesson lesson = task.getResult().toObject(Lesson.class);
                        if (lesson != null) {
                            Log.d(TAG, "makeVideoFromLessonId: Yeah we got lesson");


                            /**
                             * First make a soundless video from an array of images
                             * images are generated from slides arrayList
                             * slides will change based on time specified in changeTime arraylist
                             * get the file location of such made video to use as input video path for muxing
                             */
                            ArrayList<String> slides = lesson.getSlideArrayList();
                            ArrayList<String> changeTime = lesson.getSlideChangeList();
                            String inputAudioPath = mContext.getCacheDir().getAbsolutePath() + lessonId +".mp3";

                            // Get the duration of the audio file
                            MediaMetadataRetriever audioMetadataRetriever = new MediaMetadataRetriever();
                            audioMetadataRetriever.setDataSource(inputAudioPath);
                            long duration = Long.parseLong(audioMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

                            // Create video from a list of images and get the cache path to serve as the inputVideoPath
                            String inputVideoPath = createVideoFromSlides(mContext,slides,changeTime,duration);

                            // convertImageToVideo(lessonId);

                             mixVideoAndAudio(inputVideoPath,inputAudioPath,lesson.getTitle());

                        }
                    }
                });


    }

    private static boolean mixVideoAndAudio(String inputVideoPath, String inputAudioPath,String fileName) {
        // todo another example
        try {
            int audioTrackIndex = -1;
            int videoTrackIndex = -1;

            long duration = 0;

            //Get the audio info
            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(inputAudioPath);

            //Get the video info
            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(inputVideoPath);

            // Get the audioFormat and assign duration, audioTrackIndex from audioExtractor
            MediaFormat audioFormat = null;
            for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
                audioFormat = audioExtractor.getTrackFormat(i);
                String mimeType = audioFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("audio/")) {
                    audioTrackIndex = i;
                    duration = audioFormat.getLong(MediaFormat.KEY_DURATION);
                    break;
                }
            }

            // Get the videoFormat and assign duration, videoTrackIndex from videoExtractor
            MediaFormat videoFormat = null;
            for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
                videoFormat = videoExtractor.getTrackFormat(i);
                String mimeType = videoFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("video/")) {
                    videoTrackIndex = i;
                    break;
                }
            }

            // Return when no audio track is found
            if (audioTrackIndex < 0) return false;

            // Get or Create FileDirectory and outputFile
            File folder = new File(Environment.getExternalStorageDirectory().getPath(), "CreamPen/Downloads/Video");
            if (!folder.exists()) folder.mkdirs();
            File file = new File(folder.getAbsolutePath() + "/" + fileName + ".mp4");
            if(!file.exists()) file.createNewFile();

            // Make MediaMuxer and MediaCodec
            MediaMuxer muxer = new MediaMuxer(file.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            // Add tracks to muxer
            int writeAudioTrackIndex = muxer.addTrack(audioFormat);
            int writeVideoTrackIndex = muxer.addTrack(videoFormat);

            // start the muxer
            muxer.start();

            // create byteBuffers
            ByteBuffer audioByteBuffer = ByteBuffer.allocate(audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
            ByteBuffer videoByteBuffer = ByteBuffer.allocate(videoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));

            // create audioBufferInfo
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
            audioExtractor.selectTrack(audioTrackIndex);

            // create videoBufferInfo
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            videoExtractor.selectTrack(videoTrackIndex);

            addAudioData(muxer, duration,videoByteBuffer,videoBufferInfo,videoExtractor,videoTrackIndex,writeVideoTrackIndex);
            addAudioData(muxer, duration,audioByteBuffer,audioBufferInfo,audioExtractor,audioTrackIndex,writeAudioTrackIndex);

            // Release the Muxer,Codec and Extractor to free up memory
            audioExtractor.release();
            muxer.stop();
            muxer.release();
//


/**************************************************************************************************************************
 **************************************************************************************************************************
 **************************************************************************************************************************/

        } catch (IOException e) {
            Log.d(TAG, "IOException e: Start Here");
            e.printStackTrace();
            Log.d(TAG, "IOException e: "+ e.getMessage());
        }

        return true;
    }

    private static void addAudioData(MediaMuxer muxer, long duration, ByteBuffer audioByteBuffer, MediaCodec.BufferInfo audioBufferInfo, MediaExtractor audioExtractor, int audioTrackIndex, int writeAudioTrackIndex) {
// write audio
        while (true) {

            // break if sampleSize in less than Zero
            int readAudioSampleSize = audioExtractor.readSampleData(audioByteBuffer, 0);
            if (readAudioSampleSize < 0) break;

            // update bufferInfo
            audioBufferInfo.size = readAudioSampleSize;
            audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();

            // break if presentationTimeUs exceed total duration
            if (audioBufferInfo.presentationTimeUs > duration) {
                audioExtractor.unselectTrack(audioTrackIndex);
                break;
            }

            audioBufferInfo.offset = 0;
//                                    audioBufferInfo.flags = audioExtractor.getSampleFlags();
            audioBufferInfo.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
            muxer.writeSampleData(writeAudioTrackIndex, audioByteBuffer, audioBufferInfo);
            audioExtractor.advance();
        }
    }

    private static String createVideoFromSlides(Context mContext, ArrayList<String> slides, ArrayList<String> changeTime, long duration) {
        // Create or get the video path
        String videoPath = mContext.getCacheDir().getAbsolutePath() + "/soundlessVideo.mp4";

        try {
            File dir = new File("/storage/emulated/0/CreamPen/Downloads/Video/");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir.getAbsolutePath() + "/" + duration + ".mp4");
            if (!file.exists()) file.createNewFile();
            videoPath = file.getAbsolutePath();
        } catch (Exception e){

        }

        final String OUTPUT_MIME = MediaFormat.MIMETYPE_VIDEO_AVC;
        final int TIMEOUT_USEC = 10000;
        final int frameRate = 1;
        final int bitrate = 700000;
        final int keyFrameInternal = 1;

        int nbFrames = (int)(duration/1000 * (float)frameRate) + 1;
        MediaMuxer muxer = null;
        MediaCodec codec = null;
        boolean muxerStarted = false;
        boolean codecStarted = false;
        int videoTrackIndex = -1;


        try {

            muxer = new MediaMuxer(videoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            codec = MediaCodec.createEncoderByType(OUTPUT_MIME);

            MediaFormat mediaFormat = MediaFormat.createVideoFormat(OUTPUT_MIME, width, height);

            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, keyFrameInternal);


            codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface surface = codec.createInputSurface();
            codec.start();
            codecStarted = true;

            ByteBuffer[] outputBuffers = codec.getOutputBuffers();
            int outputBufferIndex;
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean outputDone = false;
            int nbEncoded = 0;

            Canvas canvas;
            Bitmap frame;

            while (!outputDone) {

                canvas = surface.lockCanvas(new Rect(0,0, width, height));
                frame = getBitmapFromSlide(mContext,getSlideStringForFrame(slides,changeTime,nbEncoded));
                canvas.drawBitmap(frame, 0, 0, new Paint());
                surface.unlockCanvasAndPost(canvas);

//                surface.lockCanvas(new Rect(0,0,0,0));
//                surface.unlockCanvasAndPost(canvas);

                outputBufferIndex = codec.dequeueOutputBuffer(info, TIMEOUT_USEC);
                if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    Log.d(TAG, "no output from encoder available");
                }
                else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not expected for an encoder
                    outputBuffers = codec.getOutputBuffers();
                    Log.d(TAG, "encoder output buffers changed");
                }
                else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if(muxerStarted)
                        throwException("format changed twice");
                    MediaFormat newFormat = codec.getOutputFormat();
                    videoTrackIndex = muxer.addTrack(newFormat);
                    muxer.start();
                    muxerStarted = true;
                }
                else if (outputBufferIndex < 0) {
                    throwException("unexpected result from encoder.dequeueOutputBuffer: " + outputBufferIndex);
                }
                else { // encoderStatus >=
                    Log.d(TAG,"outputBufferIndex is " + outputBufferIndex);
                    ByteBuffer encodedData = outputBuffers[outputBufferIndex];
                    if (encodedData == null) {
                        throwException("encoderOutputBuffer " + outputBufferIndex + " was null");
                    }

                    if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        // The codec config data was pulled out and fed to the muxer when we got
                        // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                        Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                        info.size = 0;
                    }

                    if (info.size != 0) {
                        if (!muxerStarted)
                            throwException("muxer hasn't started");

                        info.presentationTimeUs = computePresentationTime(nbEncoded, frameRate);
                        Log.d(TAG,"Presentation Time = " + info.presentationTimeUs);

                        if(videoTrackIndex == -1)
                            throwException("video track not set yet");

                        try {
                            Log.d(TAG, "info flag: " +info.flags);
                            Log.d(TAG, "info size: " +info.size);
                            Log.d(TAG, "info offset: " +info.offset);
                            Log.d(TAG, "endodedData limit: " +encodedData.limit());
//                            Log.d(TAG, "endodedData offset: " +encodedData.arrayOffset());
                            Log.d(TAG, "endodedData capacity: " +encodedData.capacity());
                            Log.d(TAG, "endodedData mark: " +encodedData.mark());
                            Log.d(TAG, "endodedData position: " +encodedData.position());

//                            Log.d(TAG, "info: " +info.toString());
//                            Log.d(TAG, "muxer: " +muxer.toString());

                            muxer.writeSampleData(videoTrackIndex, encodedData, info);
                        }catch (Exception e ){
                            Log.d(TAG, "convertImageToVideo: WriteSample " + e.getMessage());
                            e.printStackTrace();
                            e.getStackTrace();
                            // Just to stop looping
                            outputDone = true;
                        }

                        Log.d(TAG, "nbFrames , nbEncoded is " + nbFrames + "  -  "+ nbEncoded);
                        nbEncoded++;


                        if(nbEncoded >= nbFrames || nbEncoded > 2100 )
                            outputDone = true;
                    }

                    // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.
                    codec.releaseOutputBuffer(outputBufferIndex, false);
                }
                Log.d(TAG,"ONE cycle");
            }

            if (codec != null) {
                if (codecStarted) {codec.stop();}
                codec.release();
            }
            if (muxer != null) {
                if (muxerStarted) {muxer.stop();}
                muxer.release();
            }

        } catch (Exception e) {
            Log.e(TAG, "Encoding exception: " + e.toString());
        }

        return  videoPath;
    }

    private static String getSlideStringForFrame(ArrayList<String> slides, ArrayList<String> changeTime, int time) {
        for (int i = changeTime.size() -1 ; i > -1 ; i--) {
            String[] change = changeTime.get(i).split(",");
            if( Integer.parseInt(change[0])/1000 <= time) {
                String d = slides.get(Integer.parseInt(change[1]));
                return d;
            }
        }
        return slides.get(0);
    }

    private static String createVideoFromSlidesDemo(Context mContext, ArrayList<String> slides, ArrayList<String> changeTime, long duration) {
        // Create or get the video path
        String videoPath = mContext.getCacheDir().getAbsolutePath() + "/soundlessVideo.mp4";

        final String OUTPUT_MIME = MediaFormat.MIMETYPE_VIDEO_AVC;
        final int TIMEOUT_USEC = 10000;
        final int frameRate = 1;
        final int bitrate = 700000;
        final int keyFrameInternal = 1;
        final String filePath = "/storage/emulated/0/CreamPen/Uploads/Images/1632802780960.jpg";
        int nbFrames = (int)(duration/1000 * (float)frameRate) + 1;
        MediaMuxer muxer = null;
        MediaCodec codec = null;
        boolean muxerStarted = false;
        boolean codecStarted = false;
        int videoTrackIndex = -1;


        try {






            ///AFEEF
            // New try




            File dir = new File("/storage/emulated/0/CreamPen/Downloads/Video/");
            if(!dir.exists()) dir.mkdirs();
            File file = new File(dir.getAbsolutePath() + "/" + duration + ".mp4");
            if(!file.exists()) file.createNewFile();

            muxer = new MediaMuxer(file.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            codec = MediaCodec.createEncoderByType(OUTPUT_MIME);

            MediaFormat mediaFormat = MediaFormat.createVideoFormat(OUTPUT_MIME, width, height);

            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, keyFrameInternal);


            codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface surface = codec.createInputSurface();
            codec.start();
            codecStarted = true;

            ByteBuffer[] outputBuffers = codec.getOutputBuffers();
            int outputBufferIndex;
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean outputDone = false;
            int nbEncoded = 0;

            Bitmap frame = getBitmapFromSlide(mContext,slides.get(0));
//            Bitmap frame = getBitmapFromImage(filePath, width, height);
            Canvas canvas = surface.lockCanvas(new Rect(0,0, width, height));

            canvas.drawBitmap(frame, 0, 0, new Paint());
            surface.unlockCanvasAndPost(canvas);

            while (!outputDone) {
                canvas = surface.lockCanvas(new Rect(0,0, 0, 0));
                surface.unlockCanvasAndPost(canvas);

                outputBufferIndex = codec.dequeueOutputBuffer(info, TIMEOUT_USEC);
                if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    Log.d(TAG, "no output from encoder available");
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not expected for an encoder
                    outputBuffers = codec.getOutputBuffers();
                    Log.d(TAG, "encoder output buffers changed");
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if(muxerStarted)
                        throwException("format changed twice");
                    MediaFormat newFormat = codec.getOutputFormat();
                    videoTrackIndex = muxer.addTrack(newFormat);
                    muxer.start();
                    muxerStarted = true;
                } else if (outputBufferIndex < 0) {
                    throwException("unexpected result from encoder.dequeueOutputBuffer: " + outputBufferIndex);
                } else { // encoderStatus >=
                    Log.d(TAG,"outputBufferIndex is " + outputBufferIndex);
                    ByteBuffer encodedData = outputBuffers[outputBufferIndex];
                    if (encodedData == null) {
                        throwException("encoderOutputBuffer " + outputBufferIndex + " was null");
                    }

                    if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        // The codec config data was pulled out and fed to the muxer when we got
                        // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                        Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                        info.size = 0;
                    }

                    if (info.size != 0) {
                        if (!muxerStarted)
                            throwException("muxer hasn't started");

                        info.presentationTimeUs = computePresentationTime(nbEncoded, frameRate);
                        Log.d(TAG,"Presentation Time = " + info.presentationTimeUs);

                        if(videoTrackIndex == -1)
                            throwException("video track not set yet");

                        try {
                            Log.d(TAG, "info flag: " +info.flags);
                            Log.d(TAG, "info size: " +info.size);
                            Log.d(TAG, "info offset: " +info.offset);
                            Log.d(TAG, "endodedData limit: " +encodedData.limit());
//                            Log.d(TAG, "endodedData offset: " +encodedData.arrayOffset());
                            Log.d(TAG, "endodedData capacity: " +encodedData.capacity());
                            Log.d(TAG, "endodedData mark: " +encodedData.mark());
                            Log.d(TAG, "endodedData position: " +encodedData.position());

//                            Log.d(TAG, "info: " +info.toString());
//                            Log.d(TAG, "muxer: " +muxer.toString());

                            muxer.writeSampleData(videoTrackIndex, encodedData, info);
                        }catch (Exception e ){
                            Log.d(TAG, "convertImageToVideo: WriteSample " + e.getMessage());
                            e.printStackTrace();
                            e.getStackTrace();
                            // Just to stop looping
                            outputDone = true;
                        }

                        Log.d(TAG, "nbFrames , nbEncoded is " + nbFrames + "  -  "+ nbEncoded);
                        nbEncoded++;


                        if(nbEncoded >= nbFrames)
                            outputDone = true;
                    }

                    // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.
                    codec.releaseOutputBuffer(outputBufferIndex, false);
                }
                Log.d(TAG,"ONE cycle");
            }

            if (codec != null) {
                if (codecStarted) {codec.stop();}
                codec.release();
            }
            if (muxer != null) {
                if (muxerStarted) {muxer.stop();}
                muxer.release();
            }

        } catch (Exception e) {
            Log.e(TAG, "Encoding exception: " + e.toString());
        }







        return  videoPath;
    }

    public static void convertImageToVideo(String lessonId) {

         final String OUTPUT_MIME = MediaFormat.MIMETYPE_VIDEO_AVC;
         final int TIMEOUT_USEC = 10000;
         final int frameRate = 4;
         final int bitrate = 700000;
         final int keyFrameInternal = 1;

         final String filePath = "/storage/emulated/0/CreamPen/Uploads/Images/1632802780960.jpg";
         final int width = 1920; //1280
         final int height = 1080; //720
         final float duration = 240;

        int nbFrames = (int)(duration * (float)frameRate) + 1;

        MediaMuxer muxer = null;
        MediaCodec codec = null;
        boolean muxerStarted = false;
        boolean codecStarted = false;
        int videoTrackIndex = -1;

        try {
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(OUTPUT_MIME, width, height);

            File dir = new File("/storage/emulated/0/CreamPen/Downloads/Video/");
            if(!dir.exists()) dir.mkdirs();
            File file = new File(dir.getAbsolutePath() + "/" + lessonId + ".mp4");
            if(!file.exists()) file.createNewFile();

            muxer = new MediaMuxer(file.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            codec = MediaCodec.createEncoderByType(OUTPUT_MIME);

            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, keyFrameInternal);

            try {
                codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            }
            catch(Exception ce) {
                Log.e(TAG, ce.getMessage());
            }

            Surface surface = codec.createInputSurface();
            codec.start();
            codecStarted = true;

            ByteBuffer[] outputBuffers = codec.getOutputBuffers();
            int outputBufferIndex;
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean outputDone = false;
            int nbEncoded = 0;

            Bitmap frame = getBitmapFromImage(filePath, width, height);
            Canvas canvas = surface.lockCanvas(new Rect(0,0, width, height));

            canvas.drawBitmap(frame, 0, 0, new Paint());
            surface.unlockCanvasAndPost(canvas);

            while (!outputDone) {
                canvas = surface.lockCanvas(new Rect(0,0, 0, 0));
                surface.unlockCanvasAndPost(canvas);

                outputBufferIndex = codec.dequeueOutputBuffer(info, TIMEOUT_USEC);
                if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    Log.d(TAG, "no output from encoder available");
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not expected for an encoder
                    outputBuffers = codec.getOutputBuffers();
                    Log.d(TAG, "encoder output buffers changed");
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if(muxerStarted)
                        throwException("format changed twice");
                    MediaFormat newFormat = codec.getOutputFormat();
                    videoTrackIndex = muxer.addTrack(newFormat);
                    muxer.start();
                    muxerStarted = true;
                } else if (outputBufferIndex < 0) {
                    throwException("unexpected result from encoder.dequeueOutputBuffer: " + outputBufferIndex);
                } else { // encoderStatus >=
                    Log.d(TAG,"outputBufferIndex is " + outputBufferIndex);
                    ByteBuffer encodedData = outputBuffers[outputBufferIndex];
                    if (encodedData == null) {
                        throwException("encoderOutputBuffer " + outputBufferIndex + " was null");
                    }

                    if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        // The codec config data was pulled out and fed to the muxer when we got
                        // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                        Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                        info.size = 0;
                    }

                    if (info.size != 0) {
                        if (!muxerStarted)
                            throwException("muxer hasn't started");

                        info.presentationTimeUs = computePresentationTime(nbEncoded, frameRate);
                        Log.d(TAG,"Presentation Time = " + info.presentationTimeUs);

                        if(videoTrackIndex == -1)
                            throwException("video track not set yet");

                        try {
                            Log.d(TAG, "info flag: " +info.flags);
                            Log.d(TAG, "info size: " +info.size);
                            Log.d(TAG, "info offset: " +info.offset);
                            Log.d(TAG, "endodedData limit: " +encodedData.limit());
//                            Log.d(TAG, "endodedData offset: " +encodedData.arrayOffset());
                            Log.d(TAG, "endodedData capacity: " +encodedData.capacity());
                            Log.d(TAG, "endodedData mark: " +encodedData.mark());
                            Log.d(TAG, "endodedData position: " +encodedData.position());

//                            Log.d(TAG, "info: " +info.toString());
//                            Log.d(TAG, "muxer: " +muxer.toString());

                            muxer.writeSampleData(videoTrackIndex, encodedData, info);
                        }catch (Exception e ){
                            Log.d(TAG, "convertImageToVideo: WriteSample " + e.getMessage());
                            e.printStackTrace();
                            e.getStackTrace();
                            // Just to stop looping
                            outputDone = true;
                        }

                        Log.d(TAG, "nbFrames , nbEncoded is " + nbFrames + "  -  "+ nbEncoded);
                        nbEncoded++;


                        if(nbEncoded >= nbFrames)
                            outputDone = true;
                    }

                    // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.
                    codec.releaseOutputBuffer(outputBufferIndex, false);
                }
                Log.d(TAG,"ONE cycle");
            }

            if (codec != null) {
                if (codecStarted) {codec.stop();}
                codec.release();
            }
            if (muxer != null) {
                if (muxerStarted) {muxer.stop();}
                muxer.release();
            }

        } catch (Exception e) {
            Log.e(TAG, "Encoding exception: " + e.toString());
        }
    }

    private static long computePresentationTime(int frameIndex, int frameRate) {
        long pt = Math.abs(frameIndex * 1000000 / frameRate);
        Log.d(TAG,"Presentation Time = " + pt);
        return pt;
    }

    private static Bitmap getBitmapFromSlide(Context mContext, String slideString) {

        LayoutInflater lis = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view3 = lis.inflate(R.layout.textview, null);
        view3.setLayoutParams(new FrameLayout.LayoutParams(width,height));
        if(slideString.substring(0,3).equals("102"))
            view3.setBackgroundColor(Color.parseColor(slideString.substring(3,10)));

        TextView tv3  = view3.findViewById(R.id.textview);
        tv3.setText(Html.fromHtml(slideString.substring(10)));

        int specHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.UNSPECIFIED);
        int specWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.UNSPECIFIED);

        view3.measure(specWidth,specHeight);

        Bitmap b = Bitmap.createBitmap(view3.getMeasuredWidth(),view3.getMeasuredHeight(),Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        view3.layout(0,0,width,height);
        view3.draw(c);

        return getBitmapFromView(view3);
    }

    private static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        int width = view.getWidth();
        int height = view.getHeight();

        Bitmap returnedBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    private static Bitmap getBitmapFromImage(String inputFilePath, int width, int height) {
        Bitmap bitmap = decodeSampledBitmapFromFile(inputFilePath, width, height);

        if(bitmap.getWidth() != width || bitmap.getHeight() != height) {
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, false);
            bitmap.recycle();
            return scaled;
        }
        else {
            return bitmap;
        }
    }

    private static Bitmap decodeSampledBitmapFromFile(String filePath,
                                                      int reqWidth,
                                                      int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        // Calculate inSampleSize
        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
        return BitmapFactory.decodeFile(filePath, options);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static void throwException(String exp) {
        throw new RuntimeException(exp);
    }




}









//                            try {
//                                MediaMuxer    muxer = new MediaMuxer("temp.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//
//                                // SetUp Video/Audio Tracks.
//                                MediaFormat audioFormat = new MediaFormat();
//                                MediaFormat videoFormat = new MediaFormat();
//                                int audioTrackIndex = muxer.addTrack(audioFormat);
//                                int videoTrackIndex = muxer.addTrack(videoFormat);
//
//                                // Setup Metadata Track
//                                MediaFormat metadataFormat = new MediaFormat();
//                                metadataFormat.setString(KEY_MIME, "application/gyro");
//                                int metadataTrackIndex = muxer.addTrack(metadataFormat);
//
//                                muxer.start();
//                                while(..) {
//                                    // Allocate bytebuffer and write gyro data(x,y,z) into it.
//                                    ByteBuffer metaData = ByteBuffer.allocate(bufferSize);
//                                    metaData.putFloat(x);
//                                    metaData.putFloat(y);
//                                    metaData.putFloat(z);
//                                    MediaCodec.BufferInfo metaInfo = new MediaCodec.BufferInfo();
//                                    // Associate this metadata with the video frame by setting
//                                    // the same timestamp as the video frame.
//                                    metaInfo.presentationTimeUs = currentVideoTrackTimeUs;
//                                    metaInfo.offset = 0;
//                                    metaInfo.flags = 0;
//                                    metaInfo.size = bufferSize;
//                                    muxer.writeSampleData(metadataTrackIndex, metaData, metaInfo);
//                                };
//                                muxer.stop();
//                                muxer.release();
//
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }



//                            // todo video audio merger
//                            try {
//
//                                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "final2.mp4");
//                                file.createNewFile();
//                                outputFile = file.getAbsolutePath();
//
//                                MediaExtractor videoExtractor = new MediaExtractor();
//                                AssetFileDescriptor afdd = getAssets().openFd("Produce.MP4");
//                                videoExtractor.setDataSource(afdd.getFileDescriptor() ,afdd.getStartOffset(),afdd.getLength());
//
//                                MediaExtractor audioExtractor = new MediaExtractor();
//                                audioExtractor.setDataSource(audioFilePath);
//
//                                Log.d(TAG, "Video Extractor Track Count " + videoExtractor.getTrackCount() );
//                                Log.d(TAG, "Audio Extractor Track Count " + audioExtractor.getTrackCount() );
//
//                                MediaMuxer muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//
//                                videoExtractor.selectTrack(0);
//                                MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
//                                int videoTrack = muxer.addTrack(videoFormat);
//
//                                audioExtractor.selectTrack(0);
//                                MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
//                                int audioTrack = muxer.addTrack(audioFormat);
//
//                                Log.d(TAG, "Video Format " + videoFormat.toString() );
//                                Log.d(TAG, "Audio Format " + audioFormat.toString() );
//
//                                boolean sawEOS = false;
//                                int frameCount = 0;
//                                int offset = 100;
//                                int sampleSize = 256 * 1024;
//                                ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
//                                ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
//                                MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
//                                MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
//
//
//                                videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//                                audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//
//                                muxer.start();
//
//                                while (!sawEOS)
//                                {
//                                    videoBufferInfo.offset = offset;
//                                    videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);
//
//
//                                    if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
//                                    {
//                                        Log.d(TAG, "saw input EOS.");
//                                        sawEOS = true;
//                                        videoBufferInfo.size = 0;
//
//                                    }
//                                    else
//                                    {
//                                        videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
//                                        videoBufferInfo.flags = videoExtractor.getSampleFlags();
//                                        muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
//                                        videoExtractor.advance();
//
//
//                                        frameCount++;
//                                        Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
//                                        Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);
//
//                                    }
//                                }
//
//                                Toast.makeText(getApplicationContext() , "frame:" + frameCount , Toast.LENGTH_SHORT).show();
//
//
//
//                                boolean sawEOS2 = false;
//                                int frameCount2 =0;
//                                while (!sawEOS2)
//                                {
//                                    frameCount2++;
//
//                                    audioBufferInfo.offset = offset;
//                                    audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);
//
//                                    if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
//                                    {
//                                        Log.d(TAG, "saw input EOS.");
//                                        sawEOS2 = true;
//                                        audioBufferInfo.size = 0;
//                                    }
//                                    else
//                                    {
//                                        audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
//                                        audioBufferInfo.flags = audioExtractor.getSampleFlags();
//                                        muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
//                                        audioExtractor.advance();
//
//
//                                        Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
//                                        Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);
//
//                                    }
//                                }
//
//                                Toast.makeText(mContext , "frame:" + frameCount2 , Toast.LENGTH_SHORT).show();
//
//                                muxer.stop();
//                                muxer.release();
//
//
//                            } catch (IOException e) {
//                                Log.d(TAG, "Mixer Error 1 " + e.getMessage());
//                            } catch (Exception e) {
//                                Log.d(TAG, "Mixer Error 2 " + e.getMessage());
//                            }
//                            /////////////////////////////////////////////////////////////////////////////////////////////
