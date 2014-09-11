package com.coryhogan.vlsmcalulator;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.coryhogan.vlsmcalulator.network.Network;

public class ResultActivity extends ListActivity {
	ArrayAdapter<String> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		List<String> networksToString = new ArrayList<String>();
		for(Network net : MainActivity.calculatedSubnets) {
			networksToString.add(net.toString());
		}
		
		adapter = new ArrayAdapter<String>(this, R.layout.row_layout, R.id.textView1, networksToString);
		setListAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
}
