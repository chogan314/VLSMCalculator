package com.coryhogan.vlsmcalulator;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.coryhogan.vlsmcalulator.network.IPAddress;
import com.coryhogan.vlsmcalulator.network.Network;

public class MainActivity extends Activity {
	public static final List<Network> calculatedSubnets = new ArrayList<Network>();

	private enum InputErrorType {
		IP_ADDRESS, SUBNET_MASK, HOSTS_SET, NONE
	}

	private InputErrorType inputError = InputErrorType.NONE;

	EditText ipAddressText;
	EditText subnetMaskText;
	EditText numHostsText;
	Button subnetButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ipAddressText = (EditText) findViewById(R.id.ipAddressField);
		subnetMaskText = (EditText) findViewById(R.id.subnetMaskField);
		numHostsText = (EditText) findViewById(R.id.numHostsField);
		subnetButton = (Button) findViewById(R.id.submitButton);

		subnetButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				calcSubnets();

				if(inputError == InputErrorType.NONE) {
					goToResultsActivity();
				} else {
					
					switch(inputError) {
					case HOSTS_SET:
						Toast.makeText(MainActivity.this, "Invalid input in HOSTS SET text area.", Toast.LENGTH_SHORT).show();
						break;
					case IP_ADDRESS:
						Toast.makeText(MainActivity.this, "Invalid input in IP ADDRESS text area.", Toast.LENGTH_SHORT).show();
						break;
					case SUBNET_MASK:
						Toast.makeText(MainActivity.this, "Invalid input in SUBNET MASK text area.", Toast.LENGTH_SHORT).show();
						break;
					default:
						break;

					}
				}
			}

		});
	}

	private void calcSubnets() {
		IPAddress networkIPAddress = parseIPAddress(ipAddressText.getText().toString());

		if(networkIPAddress == null) {
			inputError = InputErrorType.IP_ADDRESS;
			return;
		}

		IPAddress networkSubnetMask = parseIPAddress(subnetMaskText.getText().toString());

		if(networkSubnetMask == null) {
			inputError = InputErrorType.SUBNET_MASK;
			return;
		}

		int[] hostsSet = parseHostsSet(numHostsText.getText().toString());

		if(hostsSet == null) {
			inputError = InputErrorType.HOSTS_SET;
			return;
		}

		Network network = new Network(networkIPAddress, networkSubnetMask);
		calculatedSubnets.clear();
		calculatedSubnets.addAll(network.subnet(hostsSet));

		inputError = InputErrorType.NONE;
	}

	private IPAddress parseIPAddress(String rawText) {
		IPAddress ip = new IPAddress(rawText);
		if(ip.getAddress() == null) {
			return null;
		} else {
			return ip;
		}
	}

	private int[] parseHostsSet(String rawText) {
		String[] split = rawText.split("[ \t\n]");
		int[] intSplit = new int[split.length];

		for(int i = 0; i < split.length; i++) {
			try {
				intSplit[i] = Integer.parseInt(split[i]);
			} catch(NumberFormatException e) {
				return null;
			}
			
			if(intSplit[i] == 0) {
				return null;
			}
		}

		return intSplit;
	}

	private void goToResultsActivity() {
		Intent intent = new Intent(this, ResultActivity.class);
		startActivity(intent);
	}

}
