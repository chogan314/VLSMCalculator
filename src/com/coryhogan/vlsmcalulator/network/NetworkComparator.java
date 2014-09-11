package com.coryhogan.vlsmcalulator.network;

import java.util.Comparator;

public class NetworkComparator implements Comparator<Network> {
	private final boolean reverse;
	
	public NetworkComparator(boolean reverse) {
		this.reverse = reverse;
	}

	@Override
	public int compare(Network network0, Network network1) {
		int network0Size = network0.networkSize();
		int network1Size = network1.networkSize();
		
		if(network0Size < network1Size) {
			return reverse ? 1 : -1;
		} else if (network0Size > network1Size) {
			return reverse ? -1 : 1;
		} else {
			return 0;
		}
	}
	
}
