package com.shinymetal.bluetoothrc;

public interface BTTransmitter {

	public void write(String message);
	public String read();
}