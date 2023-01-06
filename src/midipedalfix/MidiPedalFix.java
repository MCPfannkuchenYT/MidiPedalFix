package midipedalfix;

import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.midi.MidiSystem;

public class MidiPedalFix {

	// 125 ms between pedal is good
	// 170 ms between pedal is perfect
	// so were going with 235 ms lmao
	
	private static long pedalAt = Long.MAX_VALUE;

	public static void main(String[] args) throws Exception {
		launchVirtualMidi();
	}

	private static void launchVirtualMidi() throws Exception {
		// choose output device
		var devices = new ArrayList<String>();
		Arrays.asList(MidiSystem.getMidiDeviceInfo()).forEach(c -> {
			devices.add(c.getName());
		});
	
		// launch virtual midi
		var dev = new VirtualMidiDevice("Pianoteq 6");
		dev.setListener(c -> {
			if (pedalAt < System.currentTimeMillis()) { // (c[0] >> 4) == 0xfffffff9 && isPedal
				dev.send(new byte[] { -80, 64, 127 });
				pedalAt = Long.MAX_VALUE;
			}
			if (c[0] == -80 && c[1] == 64)
				if (c[2] == 127) {
					// pedal on
					pedalAt = System.currentTimeMillis() + 235;
					return;
				} else
					pedalAt = Long.MAX_VALUE;
			dev.send(c);
		});
		dev.open();
	}
}
