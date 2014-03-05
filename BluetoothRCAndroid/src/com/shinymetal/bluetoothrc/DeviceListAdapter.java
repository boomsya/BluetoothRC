package com.shinymetal.bluetoothrc;

import java.util.Set;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DeviceListAdapter extends	BaseAdapter {
	
	private Set<BluetoothDevice> mDevices;

	public DeviceListAdapter(Set<BluetoothDevice> devices) {
		
		mDevices = devices;
	}
	
	@Override
	public int getCount() {
		
		return mDevices.size();
	}

	@Override
	public Object getItem(int position) {
		
		return mDevices.toArray()[position];
	}

	@Override
	public long getItemId(int position) {

		return position;
	}
	
	@Override
	public boolean hasStableIds() {
		
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) parent.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.device_list, null);
		}

		TextView deviceName = (TextView) convertView
				.findViewById(R.id.deviceName);
		TextView deviceAddr = (TextView) convertView
				.findViewById(R.id.deviceAddr);

		BluetoothDevice d = (BluetoothDevice) getItem(position);

		if (d != null) {
			deviceName.setText(d.getName());
			deviceAddr.setText(d.getAddress());
		}

		return convertView;
	}
}
