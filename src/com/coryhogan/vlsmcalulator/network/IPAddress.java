package com.coryhogan.vlsmcalulator.network;

import java.util.BitSet;

public class IPAddress {
	public static final int NUM_OCTETS = 4;
	public static final int OCTET_BITS = 8;
	public static final int ADDRESS_BITS = 32;

	private enum AddressFormat {
		DECIMAL, BINARY, INVALID_FORMAT
	}

	private BitSet address;
	// Formatting mode of toString()
	private AddressFormat outputFormat = AddressFormat.DECIMAL;

	public IPAddress(String address) {
		this.address = readStringAddress(address);
	}

	public IPAddress(BitSet address) {
		this.address = address;
	}

	private BitSet readStringAddress(String address) {
		String[] octetStrings = address.trim().split("[.]");

		// check if address length is valid
		if(octetStrings.length != NUM_OCTETS) {
			return null;
		}

		// assign address format
		AddressFormat addressFormat = AddressFormat.INVALID_FORMAT;
		if(octetStrings[0].length() == OCTET_BITS) {
			addressFormat = AddressFormat.BINARY;
		} else if(octetStrings[0].length() <= 3 && octetStrings[0].length() > 0) {
			addressFormat = AddressFormat.DECIMAL;
		}

		// check if address format is valid
		if(addressFormat == AddressFormat.INVALID_FORMAT) {
			return null;
		}

		// convert decimal address to binary and check if valid
		if(addressFormat == AddressFormat.DECIMAL) {
			for(int i = 0; i < NUM_OCTETS; i++) {
				int octetInt = Integer.parseInt(octetStrings[i]);

				// check if decimal octets are in valid range
				if(octetInt > 255 || octetInt < 0) {
					return null;
				}
				octetStrings[i] = Integer.toBinaryString(octetInt);
			}
			addressFormat = AddressFormat.BINARY;
		} else { // check if binary address is valid
			for(int i = 0; i < NUM_OCTETS; i++) {
				for(int j = 0; j < OCTET_BITS; j++) {
					char c = octetStrings[i].charAt(j);
					if(c != '0' && c != '1') {
						return null;
					}
				}
			}
		}

		BitSet bits = new BitSet(ADDRESS_BITS);

		for(int i = 0; i < NUM_OCTETS; i++) {
			String o = octetStrings[i];
			int offset = OCTET_BITS - o.length();

			for(int j = offset; j < OCTET_BITS; j++) {
				char c = o.charAt(j - offset);

				if(c == '1') {
					bits.flip(j + OCTET_BITS * i);
				}
			}
		}

		return bits;
	}

	public IPAddress add(String stringBits) {
		BitSet bits = new BitSet(ADDRESS_BITS);

		for(int i = 0; i < ADDRESS_BITS; i++) {
			int index = i + stringBits.length() - ADDRESS_BITS;

			if(index >= 0) {
				if(stringBits.charAt(index) == '1') {
					bits.flip(i);
				}
			}
		}

		BitSet resultBits = new BitSet(ADDRESS_BITS);

		boolean carrying = false;

		for(int i = ADDRESS_BITS - 1; i >= 0; i--) {
			if(carrying) {
				if(address.get(i) && bits.get(i)) {
					resultBits.flip(i);
					carrying = true;
				} else if(address.get(i) || bits.get(i)) {
					carrying = false;
				} else {
					resultBits.flip(i);
					carrying = false;
				}
			} else {
				if(address.get(i) && bits.get(i)) {
					carrying = true;
				} else if(address.get(i) || bits.get(i)) {
					resultBits.flip(i);
					carrying = false;
				} else {
					carrying = false;
				}
			}
		}

		return new IPAddress(resultBits);
	}

	public BitSet getAddress() {
		return address;
	}

	public void setAddress(BitSet address) {
		this.address = address;
	}

	public AddressFormat getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(AddressFormat outputFormat) {
		this.outputFormat = outputFormat;
	}

	public IPAddress copy() {
		return new IPAddress(address);
	}

	@Override
	public String toString() {
		if(outputFormat == AddressFormat.BINARY) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < ADDRESS_BITS; i++) {
				if(i % OCTET_BITS == 0 && i != 0)
					sb.append(".");

				if(address.get(i))
					sb.append(1);
				else
					sb.append(0);
			}

			return sb.toString();
		} else {
			return decimalOutput();
		}
	}

	private String decimalOutput() {
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < NUM_OCTETS; i++) {
			int octetTotal = 0;
			for(int j = 0; j < OCTET_BITS; j++) {
				if(address.get(j + OCTET_BITS * i)) {
					octetTotal += Math.pow(2, OCTET_BITS - j - 1);
				}
			}

			sb.append(octetTotal);
			if(i != NUM_OCTETS - 1) {
				sb.append(".");
			}
		}

		return sb.toString();
	}
}
