package com.pheelicks.spherotones;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
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
	static final int LOOP_LENGTH = 64 * SAMPLE_RATE;
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
				R.raw.drum_bass);
		try 
		{
			audioStream.read(mLoopBytes, 0, HEADER_OFFSET);
			int c = 128;
			for (int i = 1; i < c; i++) {
				int soundId;
				if (i % 8 == 0 || i % 16 == 10 || i % 16 == 11) 
				{
					soundId = R.raw.drum_kick;
				}
				else if (i % 8 == 4) 
				{
					soundId = R.raw.snarehit;
				} 
				else 
				{
					soundId = R.raw.hihat_quick;
				}

				if (soundId != -1)
				{
					audioStream = mContext.getResources().openRawResource(soundId);
					audioStream.read(mHeaderDump, 0, HEADER_OFFSET);
					audioStream.read(mLoopBytes, i * LOOP_LENGTH / c, LOOP_LENGTH / (2*c));
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
		int off = (int)(SAMPLE_SIZE * SAMPLE_RATE * (System.currentTimeMillis() - mStartTime) / 1000);
		
		InputStream audioStream = mContext.getResources().openRawResource(resourceId);
		try 
		{
			audioStream.read(mLoopBytes, off, 4096);
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
