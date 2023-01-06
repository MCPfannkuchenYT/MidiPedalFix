package midipedalfix;

import java.util.function.Consumer;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

/**
 * Midi Controller for JavaX Midi Api
 * @author Pancake
 */
public class MidiController {

	/**
	 * Default midi message
	 * @author Pancake
	 */
	private class Message extends MidiMessage {

		/**
		 * Creates a new midi message from a byte array
		 * @param data Midi data
		 */
		protected Message(byte[] data) {
			super(data);
		}

		/**
		 * Clone the midi message
		 */
		@Override
		public Object clone() {
			return new Message(this.data);
		}

	}

	/**
	 * Name of the midi device
	 */
	private String dev;

	/**
	 * Midi output device
	 */
	private MidiDevice out;

	/**
	 * Midi input device
	 */
	private MidiDevice in;

	/**
	 * Midi output channel
	 */
	private Receiver receiver;

	/**
	 * Midi input channel
	 */
	private Transmitter transmitter;

	/**
	 * Midi device open state
	 */
	private boolean isOpened;

	/**
	 * Midi message listener
	 */
	private Consumer<MidiMessage> listener;

	/**
	 * Creates a new midi controller
	 * @param dev Controller
	 */
	public MidiController(String dev) {
		this.dev = dev;
	}

	/**
	 * Opens the midi devices
	 * @throws Exception
	 */
	public void open() throws Exception {
		if (this.isOpened) // check if the device is already opened
			return;

		/* Find midi device */
		this.out = this.findMidiDevice(false);
		this.in = this.findMidiDevice(true);

		/* Create Output and Input Channels */
		this.receiver = this.out.getReceiver();
		this.transmitter = this.in.getTransmitter();
		this.transmitter.setReceiver(new Receiver() {
			@Override
			public void close() {
			}

			@Override
			public void send(MidiMessage message, long timeStamp) {
				if (MidiController.this.listener != null)
					MidiController.this.listener.accept(message);
			}
		});

		/* Open midi devices */
		this.out.open();
		this.in.open();

		this.isOpened = true; // update midi device open state
	}

	/**
	 * Sends a midi message to the midi device
	 * @param msg Midi Message
	 */
	public void send(MidiMessage msg) {
		if (!this.isOpened) // check if the device is even opened
			return;
		this.receiver.send(msg, System.currentTimeMillis());
	}

	/**
	 * Sends a midi message to the midi device
	 * @param data Midi Message in byte array
	 */
	public void send(byte[] data) {
		this.send(new Message(data));
	}

	/**
	 * Tries to find a midi device with a given name
	 * @param name Device Name
	 * @param isOutput true: should be an output device, false: should be an input device
	 * @return Midi Device
	 * @throws Exception Midi Device not found
	 */
	private MidiDevice findMidiDevice(boolean isOutput) throws Exception {
		MidiDevice device;
		for (final Info info : MidiSystem.getMidiDeviceInfo())
			if (info.getName().contains(this.dev)) {
				device = MidiSystem.getMidiDevice(info);
				if (device.getMaxReceivers() != 0 && !isOutput || device.getMaxTransmitters() != 0 && isOutput)
					return device;
			}
		throw new Exception("Couldn't find midi device: " + this.dev);
	}

	/**
	 * Closes the devices
	 */
	public void close() {
		if (!this.isOpened) // check if the device is even opened
			return;

		this.receiver.close();
		this.out.close();
		this.in.close();

		this.isOpened = false;
	}

	/**
	 * Updates the listener for incoming midi messages
	 * @param listener
	 */
	public void setListener(Consumer<MidiMessage> listener) {
		this.listener = listener;
	}

	/**
	 * Returns the name of the device
	 * @return Device name
	 */
	public String getName() {
		return this.dev;
	}

	/**
	 * Returns the open state of this midi controller
	 * @return Midi open state
	 */
	public boolean isOpened() {
		return this.isOpened;
	}

}
