package testpluginproject.handlers.listener;

/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import testpluginproject.model.KeyBoardClickData;
import testpluginproject.model.jsonModel.SequentialEventData;

public class KeyBoardClickListener implements KeyListener,Listener{
	
	public StringBuilder KeyBoardClickEvents;
	List<SequentialEventData> keboardClickData;
	Timer timer = new Timer();
	public KeyBoardClickListener() {
		this.keboardClickData = new ArrayList<>();
		KeyBoardClickEvents = new StringBuilder();
	}

	
	public StringBuilder getKeyBoardClickEvents() {
		return KeyBoardClickEvents;
	}


	public void setKeyBoardClickEvents(StringBuilder keyBoardClickEvents) {
		KeyBoardClickEvents = keyBoardClickEvents;
	}


	public List<SequentialEventData> getKeboardClickData() {
		return keboardClickData;
	}


	public void setKeboardClickData(List<SequentialEventData> keboardClickData) {
		this.keboardClickData = keboardClickData;
	}


	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Key Typed: "+e.getKeyChar());
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Key Pressed: "+e.getKeyCode());
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Key Released: "+e.getKeyCode());
		
	}

	@Override
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		char unicodeChar = event.character;
		if(event.keyCode==SWT.ARROW_LEFT) {
			unicodeChar = '\u2190';
		}
		else if(event.keyCode==SWT.ARROW_UP) {
			unicodeChar = '\u2191';
		}
		else if(event.keyCode==SWT.ARROW_RIGHT) {
			unicodeChar = '\u2192';
		}
		else if(event.keyCode==SWT.ARROW_DOWN) {
			unicodeChar = '\u2193';
		}
		else if(event.keyCode==SWT.ESC) {
			unicodeChar = '\u001B';
		}
		this.KeyBoardClickEvents.append(unicodeChar);
		resetKeyboardTimer();
		System.out.println("Keyboard clicked "+unicodeChar);
		System.out.println("Keyboard clicked code: "+event.keyCode);
		
	}

	public void resetKeyboardTimer() {
		// TODO Auto-generated method stub
		if(timer!=null) {
			timer.cancel();
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				processKeyboardEvents();	
				
			}
		}, 5000);
		
	}
	private void processKeyboardEvents() {
		// TODO Auto-generated method stub
		SequentialEventData seDKB = new SequentialEventData("KeyBoardClickEvent", this.KeyBoardClickEvents.toString());
		keboardClickData.add(seDKB);
		this.KeyBoardClickEvents = new StringBuilder();
		
	}

}
