package com.pheelicks.spherotones;

import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.base.ConfigureCollisionDetectionCommand;
import orbotix.robot.base.DeviceAsyncData;
import orbotix.robot.base.DeviceMessenger;
import orbotix.robot.base.DeviceMessenger.AsyncDataListener;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.sensor.Acceleration;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MusicActivity extends Activity {
	private static final String TAG = "MusicActivity";
	
	private TextView mTestLabel;
	private SampleManager sampleManager;
	private Robot mRobot;	
    private SpheroConnectionView mSpheroConnectionView;
    
    private Handler mHandler = new Handler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music);

		mTestLabel = (TextView) findViewById(R.id.test_label);
		mTestLabel.setText("Hi!");
		
		// Set up samples
		sampleManager = new SampleManager();
		sampleManager.initSounds(this);
		sampleManager.addSound(R.raw.castanet,		R.raw.castanet);
		sampleManager.addSound(R.raw.clap,			R.raw.clap);
		sampleManager.addSound(R.raw.drum_bass,		R.raw.drum_bass);
		sampleManager.addSound(R.raw.drum_el,		R.raw.drum_el);
		sampleManager.addSound(R.raw.drum_kick,		R.raw.drum_kick);
		sampleManager.addSound(R.raw.hihat_medium,	R.raw.hihat_medium);
		sampleManager.addSound(R.raw.hihat_quick,	R.raw.hihat_quick);
		sampleManager.addSound(R.raw.hihat_slow,	R.raw.hihat_slow);
		sampleManager.addSound(R.raw.maracas, 		R.raw.maracas);
		sampleManager.addSound(R.raw.snarehit, 		R.raw.snarehit);

		sampleManager.playSound(R.raw.drum_kick);
		sampleManager.playSound(R.raw.drum_kick);
		sampleManager.playSound(R.raw.drum_kick);
		sampleManager.playSound(R.raw.drum_kick);

        mSpheroConnectionView = (SpheroConnectionView)findViewById(R.id.sphero_connection_view);
        // Set the connection event listener 
        mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
        	// If the user clicked a Sphero and it failed to connect, this event will be fired
			@Override
			public void onRobotConnectionFailed(Robot robot) {}
			// If there are no Spheros paired to this device, this event will be fired
			@Override
			public void onNonePaired() {}
			// The user clicked a Sphero and it successfully paired.
			@Override
			public void onRobotConnected(Robot robot) {
				mRobot = robot;
				// Skip this next step if you want the user to be able to connect multiple Spheros
				mSpheroConnectionView.setVisibility(View.GONE);
			
				// Calling Configure Collision Detection Command right after the robot connects, will not work
				// You need to wait a second for the robot to initialize
				mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
        				// Start streaming collision detection data
        				//// First register a listener to process the data
        				DeviceMessenger.getInstance().addAsyncDataListener(mRobot,
        						mCollisionListener);

        				ConfigureCollisionDetectionCommand.sendCommand(mRobot, ConfigureCollisionDetectionCommand.DEFAULT_DETECTION_METHOD,
        						45, 45, 100, 100, 50);
                    }
                }, 1000);
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Assume that collision detection is configured and disable it.
		ConfigureCollisionDetectionCommand.sendCommand(mRobot, ConfigureCollisionDetectionCommand.DISABLE_DETECTION_METHOD, 0, 0, 0, 0, 0);
		
		// Remove async data listener
		DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mCollisionListener);
		
		// Shutdown Sphero connection view
		mSpheroConnectionView.shutdown();
		
		// Disconnect from the robot.
		RobotProvider.getDefaultProvider().removeAllControls();
	}
	
	private final AsyncDataListener mCollisionListener = new AsyncDataListener() {

		public void onDataReceived(DeviceAsyncData asyncData) {
			if (asyncData instanceof CollisionDetectedAsyncData) {
				final CollisionDetectedAsyncData collisionData = (CollisionDetectedAsyncData) asyncData;

				// Update the UI with the collision data
				Acceleration acceleration = collisionData.getImpactAcceleration();
				Log.d(TAG, "Got collision: (" + acceleration.x + ", " + acceleration.y + ", " + acceleration.z);
				Direction direction = VectorResolver.resolve3D(acceleration);
				mTestLabel.setText(direction.description);
				
				// Play sound
				if(direction == Direction.LEFT)
				{
					Log.d(TAG, "CLAP");
					sampleManager.playSound(R.raw.clap);
				}
				else if(direction == Direction.RIGHT)
				{
					sampleManager.playSound(R.raw.drum_kick);
				}
				else if(direction == Direction.FORWARD)
				{
					sampleManager.playSound(R.raw.maracas);
				}
				else if(direction == Direction.BACK)
				{
					sampleManager.playSound(R.raw.hihat_slow);
				}
			}
		}
	};
}