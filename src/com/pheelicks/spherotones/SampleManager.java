/**
 * Based on code found here:
 * http://stackoverflow.com/a/3763993
 * http://www.droidnova.com/creating-sound-effects-in-android-part-1,570.html
 */

package com.pheelicks.spherotones;

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
	private  AudioManager  mAudioManager;
	private  Context mContext;
	private  Vector<Integer> mAvailableSounds = new Vector<Integer>();
	private  Vector<Integer> mKillSoundQueue = new Vector<Integer>();
	private  Handler mHandler = new Handler();

	public SampleManager(){}

	public void initSounds(Context theContext) { 
		mContext = theContext;
		mSoundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0); 
		mSoundPoolMap = new SparseIntArray(); 
		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);       
	} 

	public void addSound(int Index, int SoundID)
	{
		mAvailableSounds.add(Index);
		mSoundPoolMap.put(Index, mSoundPool.load(mContext, SoundID, 1));

	}

	// volume is a number between 0 and 1.
	public void playSound(int index, float volume) {
		if(mAvailableSounds.contains(index)){
			//int streamVolume = (int) (volume * 2 * mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)); 
			//Log.d(LOG_TAG, "Volume " + streamVolume);
			int soundId = mSoundPool.play(mSoundPoolMap.get(index), volume, volume, 1, 0, 1f);

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
}
