package com.pheelicks.spherotones;

import java.util.List;
import java.util.Vector;

import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.base.CollisionDetectedAsyncData.CollisionPower;
import orbotix.robot.base.ConfigureCollisionDetectionCommand;
import orbotix.robot.base.DeviceAsyncData;
import orbotix.robot.base.DeviceMessenger;
import orbotix.robot.base.DeviceMessenger.AsyncDataListener;
import orbotix.robot.base.DeviceSensorsAsyncData;
import orbotix.robot.base.FrontLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.SetDataStreamingCommand;
import orbotix.robot.base.StabilizationCommand;
import orbotix.robot.sensor.Acceleration;
import orbotix.robot.sensor.AttitudeData;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MusicActivity extends Activity {
	private static final String TAG = "MusicActivity";

	private TextView mTestLabel;
	private Robot mRobot;	
	private SpheroConnectionView mSpheroConnectionView;

	private Handler mHandler = new Handler();

	private SampleManager sampleManager;
	private LoopPlayer mLoopPlayer;
	private int yaw = 0;
	
	/**
     * Data Streaming Packet Counts
     */
    private final static int TOTAL_PACKET_COUNT = 200;
    private final static int PACKET_COUNT_THRESHOLD = 50;
    private int mPacketCounter;
    
    private int[] soundColors;
    private int currentColor = 0;


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
		
		soundColors = new int[10];
		soundColors[0] = Color.BLUE;
		soundColors[1] = Color.GREEN;
		soundColors[2] = Color.RED;
		soundColors[3] = Color.WHITE;
		soundColors[4] = Color.CYAN;
		soundColors[5] = Color.YELLOW;
		soundColors[6] = Color.MAGENTA;
		soundColors[7] = Color.LTGRAY;
		soundColors[8] = 0x34b876;
		soundColors[9] = 0xd4b16f;
		currentColor = 0;
		//soundColors
		
		
		mLoopPlayer = new LoopPlayer(this);
		mLoopPlayer.start();
		//mLoopPlayer.add(R.raw.clap, 1024)


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

						//Configure
						ConfigureCollisionDetectionCommand.sendCommand(mRobot, ConfigureCollisionDetectionCommand.DEFAULT_DETECTION_METHOD,
								15,
								15,
								15,
								15,
								20);

						requestDataStreaming();
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

		// turn stabilization off
		StabilizationCommand.sendCommand(mRobot, false);
		
		// Remove async data listener
		DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mCollisionListener);

		// Shutdown Sphero connection view
		mSpheroConnectionView.shutdown();

		// Disconnect from the robot.
		RobotProvider.getDefaultProvider().removeAllControls();		
		
		mLoopPlayer.stop();
	}

	private final AsyncDataListener mCollisionListener = new AsyncDataListener() {

		String text1 = "", text2 = "";
		
		public void onDataReceived(DeviceAsyncData data) {

			if (data instanceof CollisionDetectedAsyncData) {
				text1 = "";
				final CollisionDetectedAsyncData collisionData = (CollisionDetectedAsyncData) data;

				// Update the UI with the collision data
				Acceleration acceleration = collisionData.getImpactAcceleration();
				//Log.d(TAG, "Got collision: (" + acceleration.x + ", " + acceleration.y + ", " + acceleration.z);
				Direction direction = VectorResolver.resolve3D(acceleration);
				CollisionPower cPower = collisionData.getImpactPower();
				float cSpeed = collisionData.getImpactSpeed();

				// Get top power value
				float volume = cPower.x;
				if (cPower.y > volume)
					volume = cPower.y;

				// Normalize power. It typically comes in at 20-30 and we want a volume between 0 and 2.
				volume -= 10;
				volume /= 20;
				if (volume > 2)
					volume = 2;

				sampleManager.playCurrentSample(volume); //volume
				
				mLoopPlayer.add(sampleManager.getCurrentSample(), -1);

				//sampleManager.playSound(R.raw.drum_bass, volume);
				/*
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
				}*/

				text1 += direction.description + "\n";
				text1 += "Power: " + cPower.x + ", " + cPower.y + "\n";
				text1 += "Speed: " + cSpeed * 1000 + "\n";
				text1 += "Volume: " + volume;
			}


			if(data instanceof DeviceSensorsAsyncData){
				// If we are getting close to packet limit, request more
				mPacketCounter++;
                if( mPacketCounter > (TOTAL_PACKET_COUNT - PACKET_COUNT_THRESHOLD) ) {
                    requestDataStreaming();
                }

				text2 = "";

				//get the frames in the response
				List<DeviceSensorsData> data_list = ((DeviceSensorsAsyncData)data).getAsyncData();
				if(data_list != null){

					//Iterate over each frame
					AttitudeData attitude;
					int newYaw = yaw;
					for(DeviceSensorsData datum : data_list){

						//Show attitude data
						attitude = datum.getAttitudeData();
						if(attitude != null){
							text2 += "pitch: " 	+ attitude.getAttitudeSensor().pitch + "\n";
							text2 += "roll: " 	+ attitude.getAttitudeSensor().roll  + "\n";
							text2 += "yaw: " 	+ attitude.getAttitudeSensor().yaw   + "\n";
						}

						/*
                        //Show accelerometer data
                        AccelerometerData accel = datum.getAccelerometerData();
                        if(attitude != null){
                            mAccelerometerFilteredView.setX(""+accel.getFilteredAcceleration().x);
                            mAccelerometerFilteredView.setY("" + accel.getFilteredAcceleration().y);
                            mAccelerometerFilteredView.setZ("" + accel.getFilteredAcceleration().z);
                        }*/
						newYaw = attitude.getAttitudeSensor().yaw;
					}
					if (yaw / 30 > newYaw / 30) {
						sampleManager.changeSample(1);
						yaw = newYaw;
						updateColor((currentColor + 1) % 10);
					}
					else if (yaw / 30 < newYaw / 30) {
						sampleManager.changeSample(-1);
						yaw = newYaw;
						updateColor((currentColor + 9) % 10);
					}
					//yaw = attitude.getAttitudeSensor().yaw;
				}
			}			

			mTestLabel.setText(text1 + text2);
		}
	};

	private void updateColor(int color) {
		Log.d(TAG, "Color being set to:  " + color);
		currentColor = color;
		RGBLEDOutputCommand.sendCommand(mRobot, 
				Color.red(	soundColors[currentColor]), 
				Color.green(soundColors[currentColor]), 
				Color.blue( soundColors[currentColor]));
		
	}
	
	private void requestDataStreaming() {

		if(mRobot != null){

			// Set up a bitmask containing the sensor information we want to stream
			final long mask = SetDataStreamingCommand.DATA_STREAMING_MASK_ACCELEROMETER_FILTERED_ALL |
					SetDataStreamingCommand.DATA_STREAMING_MASK_IMU_ANGLES_FILTERED_ALL;

			// Specify a divisor. The frequency of responses that will be sent is 400hz divided by this divisor.
			final int divisor = 20;

			// Specify the number of frames that will be in each response. You can use a higher number to "save up" responses
			// and send them at once with a lower frequency, but more packets per response.
			final int packet_frames = 1;

			// Reset finite packet counter
			mPacketCounter = 0;

			// Count is the number of async data packets Sphero will send you before
			// it stops.  You want to register for a finite count and then send the command
			// again once you approach the limit.  Otherwise data streaming may be left
			// on when your app crashes, putting Sphero in a bad state 
			final int response_count = TOTAL_PACKET_COUNT;

			//Send this command to Sphero to start streaming
			SetDataStreamingCommand.sendCommand(mRobot, divisor, packet_frames, mask, response_count);
		}
	}
}