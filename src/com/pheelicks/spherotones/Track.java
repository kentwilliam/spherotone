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
		public void playSound(int trackId);
	}
	
	private class AudioSample
	{
		public int soundId;
	}
	
	public int mLoopLength = 8000;
	private Boolean mPlaying = false;
	private Runnable mTrackRunnable;
	private SparseArray<AudioSample> mTrackData;
	
	public Track()
	{
		mTrackData = new SparseArray<Track.AudioSample>();
		for(int i = 0; i < 10; i++)
		{
			AudioSample a = new AudioSample();
			a.soundId = i;
			mTrackData.put(i * 500, a);
		}
		
		int q = 1;
		q +=1;
	}
	
	public void start()
	{
		mTrackRunnable = new Runnable() {
			
			@Override
			public void run() 
			{
				int index = 0; // Start at 0 ms
				while(mPlaying)
				{
                    // Save off current time as we need to later subtract the time it took to process this code
					long startTimeStamp = System.currentTimeMillis();

					// Get sound for index
					AudioSample a = mTrackData.valueAt(index);
					if(a != null)
					{
						Log.d(TAG, "Playing sound with id " + a.soundId + " " + System.currentTimeMillis());
					}
					
					int nextIndex = index + 1 == mTrackData.size() ? 0 : index + 1;
					int delta;
					if(nextIndex == 0)
					{
						// Last sample in loop
						delta  = (mLoopLength - mTrackData.keyAt(index)) + mTrackData.keyAt(0);
					}
					else
					{
						delta = mTrackData.keyAt(nextIndex) - mTrackData.keyAt(index);
					}
					index = nextIndex;
					
					// Sleep until next sample
                    try 
                    {
						Log.d(TAG, "Sleeping for (ms) " + delta);
                    	Thread.sleep(delta - (System.currentTimeMillis() - startTimeStamp));
                    } 
                    catch (InterruptedException e) 
                    {
                    	Log.e(TAG, "Track loop was interrupted: " + e.getLocalizedMessage());
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
		
	}
}
