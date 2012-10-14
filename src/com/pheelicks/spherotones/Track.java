package com.pheelicks.spherotones;

import android.util.Log;
import android.util.SparseArray;

public class Track 
{
	private static final String TAG = "Track";

	/**
	 * Called when sound should be played
	 */
	public interface onTriggerListener
	{
		public void playSound(int soundId);
	}
	
	private class AudioSample
	{
		public int soundId;
	}
	
	public int mLoopLength = 8000;
	private Boolean mPlaying = false;
	private Runnable mTrackRunnable;
	private SparseArray<AudioSample> mTrackData;
	private onTriggerListener mOnTriggerListener;
	private long mStartTime = 0;
	
	public Track()
	{
		mTrackData = new SparseArray<Track.AudioSample>();
		int c = 64;
		for(int i = 0; i < c; i++)
		{
			AudioSample a = new AudioSample();
			if(i % 8 == 0)
			{
				a.soundId = R.raw.drum_kick;
			}
			else if(i % 8 == 4)
			{
				a.soundId = R.raw.snarehit;
			}
			else
			{
				a.soundId = R.raw.hihat_medium;
			}

			mTrackData.put(i * mLoopLength/c, a);
		}		
	}
	
	public void setOnTriggerListener(onTriggerListener l)
	{
		mOnTriggerListener = l;
	}
	
	public void addSound(int soundId)
	{
		int sampleTime = (int)(System.currentTimeMillis() % mLoopLength);
		sampleTime = 250* (sampleTime / 250);
		AudioSample a = new AudioSample();
		a.soundId = soundId;
		mTrackData.put(sampleTime, a);
	}
	
	public void start()
	{
		mTrackRunnable = new Runnable() {
			
			@Override
			public void run() 
			{
				int index = 0; 
				int key = 0; // Start at 0 ms
				mStartTime = System.currentTimeMillis();
				int iteration = 0;
				while(mPlaying)
				{
                    // Save off current time as we need to later subtract the time it took to process this code
					long startTimeStamp = System.currentTimeMillis();

					// Obtain index for this key, we will work with that as we care about the next sample
					index = mTrackData.indexOfKey(key);
					if(index < 0)
					{
						// No sample at this key, default to first sample
						index = 0;
					}
					
					// Get sound for index
					AudioSample a = mTrackData.valueAt(index);
					if(a != null)
					{
						Log.d(TAG, "Playing sound with id " + a.soundId + " " + ((System.currentTimeMillis() - mStartTime) % 125));
						if(mOnTriggerListener != null)
						{
							mOnTriggerListener.playSound(a.soundId);
						}
					}
					
					int nextIndex = index + 1 >= mTrackData.size() ? 0 : index + 1;
					int delta;
					if(mTrackData.size() == 0)
					{
						delta = mLoopLength;
					}
					else if(nextIndex == 0)
					{
						// Last sample in loop
						iteration++;
						delta = (int)(mLoopLength * iteration + mTrackData.keyAt(nextIndex) + mStartTime - System.currentTimeMillis());

					}
					else
					{
						delta = (int)(mLoopLength * iteration + mTrackData.keyAt(nextIndex) + mStartTime - System.currentTimeMillis());
					}
					
					index = nextIndex;
					key = mTrackData.keyAt(index);
					
					// Sleep until next sample
					if (delta > 0) {
						try {
							Log.d(TAG, "Sleeping for (ms) " + delta);
							Thread.sleep(delta);
						} catch (InterruptedException e) {
							Log.e(TAG,
									"Track loop was interrupted: "
											+ e.getLocalizedMessage());
						}
					}
				}
			}
		};

		mPlaying = true;
        Thread trackThread = new Thread(mTrackRunnable);
        trackThread.start();
	}
	
	public void stop()
	{
		mPlaying = false;
	}
}
