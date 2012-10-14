/**
 * Based on code found here:
 * http://stackoverflow.com/a/3763993
 * http://www.droidnova.com/creating-sound-effects-in-android-part-1,570.html
 */

package com.pheelicks.spherotones;

import java.util.Iterator;
import java.util.Vector;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;

public class SampleManager {
	private static final String LOG_TAG = "SampleManager";

	private  SoundPool mSoundPool; 
	private  SparseIntArray mSoundPoolMap;
	private  int[] samples;
	private  int currentSample;
	//private Iterator sampleIterator;
	//private  int lastSampleIndex;
	//private  AudioManager  mAudioManager;
	private  Context mContext;
	private  Vector<Integer> mAvailableSounds = new Vector<Integer>();
	private  Vector<Integer> mKillSoundQueue = new Vector<Integer>();
	private  Handler mHandler = new Handler();

	public SampleManager(){}
	
	public int changeSample(int by) {
		Iterator<Integer> it = mAvailableSounds.iterator();
		int advanceToSample = -1;
		int prevSample = -1;
		while (it.hasNext()) {
			int sampleId = (Integer) it.next().intValue();
			if (by == -1 && sampleId == currentSample) {
				advanceToSample = prevSample;
				break;
			}
			else if (by == 1 && sampleId == currentSample && it.hasNext()) {
				advanceToSample = ((Integer) it.next()).intValue();
				break;
			}
			prevSample = sampleId;
		}
		// If we were at the end of the Vector, loop and select sample #1
		if (advanceToSample == -1) {
			if (by == 1) {
				it = mAvailableSounds.iterator();
				advanceToSample = ((Integer) it.next()).intValue();;
			}
			else if (by == -1) {
				advanceToSample = prevSample;
			}
		}
			
		currentSample = advanceToSample;
		//Log.d(LOG_TAG, "Advancing by " + by);
		//currentSample = currentSampleIndex + by % mSoundPoolMap.size();
		return currentSample;
	}
	
	public void setSample(int s)
	{
		currentSample = mAvailableSounds.get(s);	
	}

	public void initSounds(Context theContext) { 
		mContext = theContext;
		mSoundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0); 
		mSoundPoolMap = new SparseIntArray(); 
		samples = new int[20];
		Log.d(LOG_TAG, "SAMPLES: " + samples.length);
		//mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);       
	} 

	public void addSound(int index, int SoundID)
	{
		mAvailableSounds.add(index);
		int soundId = mSoundPool.load(mContext, SoundID, 1);
		mSoundPoolMap.put(index, soundId);
		currentSample = index;
		
		//mSoundPoolMap.
		//samples[samples.length] = soundId;
		//samples.put(mSoundPoolMap.size(), soundId);
		//currentSampleIndex = index;
	}

	public void playCurrentSample(float volume) {
		this.playSound(currentSample, volume);
	}
	
	// volume is a number between 0 and 1.
	public void playSound(int index, float volume) {
		if(mAvailableSounds.contains(index)){
			//int streamVolume = (int) (volume * 2 * mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)); 
			//Log.d(LOG_TAG, "Volume " + streamVolume);
			int soundId = mSoundPool.play(mSoundPoolMap.get(index), volume, volume, 1, 0, 1f); //mSoundPoolMap.get(index)

//			mKillSoundQueue.add(soundId);
//
//			// Play until 3 seconds have passed
//			mHandler.postDelayed(new Runnable() {
//				public void run() {
//					if(!mKillSoundQueue.isEmpty()){
//						mSoundPool.stop(mKillSoundQueue.firstElement());
//					}
//				}
//			}, 3000);
		}
	}
	
	public int getCurrentSample() {
		return currentSample;
	}

}
