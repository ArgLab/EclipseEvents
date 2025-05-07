package com.arglab.eclipsedatacollector.core.eclipsemonitor.handlers.listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchWindow;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.KeyBoardClickData;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.jsonmodel.SequentialEventData;



public class KeyBoardClickListener implements KeyListener,Listener{
	
	public StringBuilder KeyBoardClickEvents;
	List<SequentialEventData> keyboardClickData;
	IWorkbenchWindow window;
	
	Timer timer = new Timer();
	public KeyBoardClickListener(IWorkbenchWindow window) {
		this.keyboardClickData = new ArrayList<>();
		KeyBoardClickEvents = new StringBuilder();
		this.window = window;
	}

	
	public StringBuilder getKeyBoardClickEvents() {
		return KeyBoardClickEvents;
	}


	public void setKeyBoardClickEvents(StringBuilder keyBoardClickEvents) {
		KeyBoardClickEvents = keyBoardClickEvents;
	}


	public List<SequentialEventData> getKeboardClickData() {
		return keyboardClickData;
	}


	public void setKeboardClickData(List<SequentialEventData> keboardClickData) {
		this.keyboardClickData = keboardClickData;
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
	
	public void immediateSave() {
		System.out.println("Immediate save");
		if(this.KeyBoardClickEvents.toString().length()!=0) {
			processKeyboardEvents();
		}
	}
	private void processKeyboardEvents() {
		// TODO Auto-generated method stub
		String windowName = null;
		if(window.getActivePage()!=null) {
			if(window.getActivePage().getActivePart()!=null) {
				if(window.getActivePage().getActivePart().getTitle()!=null) {
					windowName = window.getActivePage().getActivePart().getTitle();
				}
			}
		}
		
		KeyBoardClickData kcd = new KeyBoardClickData(windowName, this.KeyBoardClickEvents.toString());
		SequentialEventData seDKB = new SequentialEventData("KeyBoardClickEvent", kcd);
		keyboardClickData.add(seDKB);
		this.KeyBoardClickEvents = new StringBuilder();
		
	}

}