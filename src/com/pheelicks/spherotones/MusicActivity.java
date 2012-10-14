package com.pheelicks.spherotones;

import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.base.CollisionDetectedAsyncData.CollisionPower;
import orbotix.robot.base.ConfigureCollisionDetectionCommand;
import orbotix.robot.base.DeviceAsyncData;
import orbotix.robot.base.DeviceMessenger;
import orbotix.robot.base.FrontLEDOutputCommand;
import orbotix.robot.base.StabilizationCommand;
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
                        // turn rear light on
                        FrontLEDOutputCommand.sendCommand(mRobot, 1.0f);

                        // turn stabilization off
                        StabilizationCommand.sendCommand(mRobot, false);

                        // Start streaming collision detection data
        				//// First register a listener to process the data
        				DeviceMessenger.getInstance().addAsyncDataListener(mRobot,
        						mCollisionListener);

        				ConfigureCollisionDetectionCommand.sendCommand(mRobot, ConfigureCollisionDetectionCommand.DEFAULT_DETECTION_METHOD,
        						15,
        						15,
        						15,
        						15,
        						20);

        				// register the async data listener
//                        DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);
  //                      // Start streaming data
    //                    requestDataStreaming();
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
				String text = "";
				Acceleration acceleration = collisionData.getImpactAcceleration();
				Log.d(TAG, "Got collision: (" + acceleration.x + ", " + acceleration.y + ", " + acceleration.z);
				Direction direction = VectorResolver.resolve3D(acceleration);
				CollisionPower cPower = collisionData.getImpactPower();
				float cSpeed = collisionData.getImpactSpeed();
				text += direction.description + "\n";
				text += "Power: " + cPower.x + ", " + cPower.y + "\n";
				text += "Speed: " + cSpeed * 1000 + "\n";
				
				// Get top power value
				float volume = cPower.x;
				if (cPower.y > volume)
					volume = cPower.y;
				
				/*
				 * power typically 
				 */
				
				// Normalize power
				volume -= 10;
				volume /= 20;
				if (volume > 2)
					volume = 2;
				
				text += "Volume: " + volume;
				
				mTestLabel.setText(text);
		
				// Play sound
				if(direction == Direction.LEFT)
				{
					sampleManager.playSound(R.raw.clap, volume);
				}
				else if(direction == Direction.RIGHT)
				{
					sampleManager.playSound(R.raw.drum_kick, volume);
				}
				else if(direction == Direction.FORWARD)
				{
					sampleManager.playSound(R.raw.maracas, volume);
				}
				else if(direction == Direction.BACK)
				{
					sampleManager.playSound(R.raw.castanet, volume);
				}
			}
		}
	};
}