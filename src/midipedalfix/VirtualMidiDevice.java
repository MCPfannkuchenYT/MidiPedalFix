package midipedalfix;

import java.util.function.Consumer;

import de.tobiaserichsen.tevm.TeVirtualMIDI;

/**
 * Virtual midi device
 * @author Pancake
 */
public class VirtualMidiDevice {

	/**
	 * Name of the midi device
	 */
	private String dev;

	/**
	 * Virtual midi device
	 */
	private TeVirtualMIDI device;

	/**
	 * Midi device open state
	 */
	private boolean isOpened;

	/**
	 * Midi message listener
	 */
	private Consumer<byte[]> listener;

	/**
	 * Creates a new midi controller
	 * @param dev Controller
	 */
	public VirtualMidiDevice(String dev) {
		this.dev = dev;
	}

	/**
	 * Opens the midi devices
	 * @throws Exception
	 */
	public void open() throws Exception {
		if (this.isOpened) // check if the device is already opened
			return;

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			this.close();
		}));

		this.device = new TeVirtualMIDI(this.dev);
		new Thread(() -> {
			while (true)
				this.listener.accept(this.device.getCommand());
		}).start();

		this.isOpened = true; // update midi device open state
	}

	/**
	 * Sends a midi message to the midi device
	 * @param data Midi Message in byte array
	 */
	public void send(byte[] data) {
		this.device.sendCommand(data);
	}

	/**
	 * Closes the devices
	 */
	public void close() {
		if (!this.isOpened) // check if the device is even opened
			return;

		this.device.shutdown();

		this.isOpened = false;
	}

	/**
	 * Updates the listener for incoming midi messages
	 * @param listener
	 */
	public void setListener(Consumer<byte[]> listener) {
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
