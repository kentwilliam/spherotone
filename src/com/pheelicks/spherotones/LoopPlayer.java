package com.pheelicks.spherotones;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class LoopPlayer {
	static final String TAG = "LoopPlayer";
	static final int SAMPLE_RATE = 44100;
	static final int SAMPLE_SIZE = 2; // bytes per sample
	static final int BUF_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE,
			AudioFormat.CHANNEL_CONFIGURATION_MONO,
			AudioFormat.ENCODING_PCM_16BIT);
	static final int LOOP_LENGTH = 8 * SAMPLE_RATE;
	static final int BEAT_COUNT = 16;
	static final int HEADER_OFFSET = 0x2C;

	byte[] mLoopBytes = new byte[LOOP_LENGTH];
	private Runnable mLoopRunnable;
	private Boolean mPlaying = false;
	private Context mContext;
	private byte[] mHeaderDump = new byte[HEADER_OFFSET];
	private long mStartTime;

	public LoopPlayer(Context context) 
	{
		mContext = context;
		InputStream audioStream = mContext.getResources().openRawResource(
				R.raw.drum_kick);
		try 
		{
			audioStream.read(mLoopBytes, 0, HEADER_OFFSET);
			for (int i = 0; i < BEAT_COUNT; i++) {
				int soundId = -1;
//				if (i % 8 == 0 || i % 16 == 10 || i % 16 == 11) 
//				{
//					soundId = R.raw.drum_kick;
//				}
//				else if (i % 8 == 4) 
//				{
//					soundId = R.raw.snarehit;
//				} 
					
					if (i % 2 == 0)
						soundId = R.raw.maracas;

					
					//if(i == 0)
					//{
					//	soundId = R.raw.drum_kick;
					//}

				if (soundId != -1)
				{
					add(soundId, i * LOOP_LENGTH / BEAT_COUNT);
				}
			}

		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
	
	public void add(int resourceId, int offset)
	{
		int off;
		if(offset == -1)
		{
			int latency = 0;
			off = ((int)(SAMPLE_SIZE * SAMPLE_RATE * (System.currentTimeMillis() - latency - mStartTime) / 1000) % LOOP_LENGTH);
		}
		else
		{
			off = offset;
		}
		
		InputStream audioStream = null;
		try
		{
			audioStream = mContext.getResources().openRawResource(resourceId);
			audioStream.read(mHeaderDump, 0, HEADER_OFFSET);

		}
		catch (NotFoundException e)
		{
			Log.e(TAG, "Could not find resource with id: " + resourceId);
			return;
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}

		// Quantize
		off = (LOOP_LENGTH / BEAT_COUNT) * (off / (LOOP_LENGTH / BEAT_COUNT));
		Log.d(TAG, "Adding sound " + resourceId + " with offset " + off);
		
		// Mix in to existing audio data
		try
		{
			for (int i = 0; true; i++) {

				int a = audioStream.read();
				if(a == -1)break;
				int b = mLoopBytes[(off + i) % LOOP_LENGTH];
				int c = (int)(a + b - (a * b)/256);
				mLoopBytes[(off + i) % LOOP_LENGTH] = (byte) c;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}	
	}

	public void start() {
		mLoopRunnable = new Runnable() {

			@Override
			public void run() 
			{
				AudioTrack audioTrack = new AudioTrack(
						AudioManager.STREAM_MUSIC, SAMPLE_RATE,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE,
						AudioTrack.MODE_STREAM);
				audioTrack.play();
				mStartTime = System.currentTimeMillis();

				byte[] buffer = new byte[BUF_SIZE];
				while (mPlaying) // loop sound
				{
					InputStream loopStream = new ByteArrayInputStream(mLoopBytes);

					long bytesWritten = 0;
					int bytesRead = 0;

					try {
						loopStream.read(buffer, 0, HEADER_OFFSET);
						
						// read until end of loopBytes
						while (mPlaying
								&& bytesWritten < mLoopBytes.length - HEADER_OFFSET) {
							bytesRead = loopStream.read(buffer, 0, BUF_SIZE);
							bytesWritten += audioTrack.write(buffer, 0, bytesRead);
						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				audioTrack.stop();
				audioTrack.release();
			}
		};

		mPlaying = true;
		Thread loopThread = new Thread(mLoopRunnable);
		loopThread.start();
	}

	public void stop() {
		mPlaying = false;
	}
}
