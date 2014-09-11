package com.coryhogan.vlsmcalulator.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class Network {
	public static int RESERVED_ADDRESSES = 2;

	private IPAddress networkAddress;
	private IPAddress subnetMask;

	private boolean inUse;

	// sorts networks by increasing size
	private static final NetworkComparator networkComparator = new NetworkComparator(false);
	// sorts networks by decreasing size
	private static final NetworkComparator networkComparatorReverse = new NetworkComparator(true);

	public Network(String networkAddress, String subnetMask) {
		this.networkAddress = new IPAddress(networkAddress);
		this.subnetMask = new IPAddress(subnetMask);
	}

	public Network(BitSet networkAddress, BitSet subnetMask) {
		this.networkAddress = new IPAddress(networkAddress);
		this.subnetMask = new IPAddress(subnetMask);
	}

	public Network(IPAddress networkAddress, IPAddress subnetMask) {
		this.networkAddress = networkAddress;
		this.subnetMask = subnetMask;
	}

	private int numNetworkBits() {
		int bits = 0;
		for(int i = 0; i < IPAddress.ADDRESS_BITS; i++) {
			if(subnetMask.getAddress().get(i)) {
				bits++;
			} else {
				break;
			}
		}

		return bits;
	}

	private int numHostBits() {
		return IPAddress.ADDRESS_BITS - numNetworkBits();
	}

	// calculate a network's size based on its host bits
	private int calcNetworkSize(int hostBits) {
		return (int) Math.pow(2, hostBits);
	}

	public int networkSize() {
		return calcNetworkSize(numHostBits());
	}

	public int maxHosts() {
		return networkSize() - RESERVED_ADDRESSES;
	}

	// subnets network based on number of hosts desired
	// generates best-fit subnet using as few splits as possible
	public List<Network> subnet(int desiredHosts) {
		int desiredNetworkSize = desiredHosts + RESERVED_ADDRESSES;

		if(desiredNetworkSize > (networkSize())) {
			int maxSubnetSize = networkSize() / 2 - 2;
			System.err.println("Cannot subnet for " + desiredHosts + " hosts -- Max subnet size is " + maxSubnetSize
					+ " hosts");
			System.exit(1);
		}

		int requiredHostBits = (int) Math.ceil(Math.log(desiredNetworkSize) / Math.log(2));
		int bestFitNetworkSize = calcNetworkSize(requiredHostBits);

		return genSubnets(bestFitNetworkSize);
	}

	// recursively generates subnets for a given network size
	public List<Network> genSubnets(int bestFitNetworkSize) {
		if(bestFitNetworkSize > networkSize() / 4) {
			List<Network> subnets = genFirstOrderSubnets();
			subnets.get(0).setInUse(true);
			return subnets;
		} else {
			List<Network> subnets = genFirstOrderSubnets();
			Network chosen = subnets.get(0);
			subnets.remove(0);
			subnets.addAll(chosen.genSubnets(bestFitNetworkSize));
			return subnets;
		}
	}

	// generates the network's two first-order subnets
	public List<Network> genFirstOrderSubnets() {
		if(networkSize() == 4) {
			System.err.println("Network of size 4 does not have any subnets.");
			System.exit(1);
		}

		int subnetMaskSize = numNetworkBits() + 1;
		BitSet maskBits = new BitSet(IPAddress.ADDRESS_BITS);

		for(int i = 0; i < subnetMaskSize; i++) {
			maskBits.flip(i);
		}

		IPAddress mask = new IPAddress(maskBits);

		int subnetSize = calcNetworkSize(IPAddress.ADDRESS_BITS - subnetMaskSize);
		int numSubnets = 2;

		List<Network> subnets = new ArrayList<Network>();

		for(int i = 0; i < numSubnets; i++) {
			int subnetAddress = i * subnetSize;

			String adjustString = Integer.toBinaryString(subnetAddress);
			IPAddress address = networkAddress.add(adjustString);

			subnets.add(new Network(address, mask.copy()));
		}

		return subnets;
	}

	// subnet network based on a group of networks based on a set of groups of
	// hosts
	public List<Network> subnet(int[] desiredHostsSet) {
		Arrays.sort(desiredHostsSet);
		int len = desiredHostsSet.length;

		int[] hostsSet = new int[len];

		for(int i = 0; i < len; i++) {
			hostsSet[len - i - 1] = desiredHostsSet[i];
		}

		List<Network> subnets = genSubnets(desiredHostsSet);
		Collections.sort(subnets, networkComparatorReverse);

		return subnets;
	}

	private List<Network> genSubnets(int[] hostsSet) {
		if(hostsSet.length == 1) {
			return subnet(hostsSet[0]);
		} else {
			int numHosts = hostsSet[0];

			int[] leftoverHostsSet = new int[hostsSet.length - 1];
			for(int i = 0; i < leftoverHostsSet.length; i++) {
				leftoverHostsSet[i] = hostsSet[i + 1];
			}

			List<Network> subnets = genSubnets(leftoverHostsSet);
			Collections.sort(subnets, networkComparator);

			Network chosen = null;
			for(Network net : subnets) {
				if(net.maxHosts() >= numHosts && !net.isInUse()) {
					chosen = net;
					break;
				}
			}

			if(chosen == null) {
				System.err.println("No subnets available for given number of hosts");
				System.exit(1);
			}
			
			if(chosen.networkSize() / 2 - 2 < numHosts) {
				chosen.setInUse(true);
			} else {
				subnets.remove(chosen);
				subnets.addAll(chosen.subnet(numHosts));
			}
			
			return subnets;
		}
	}

	public IPAddress getNetworkAddress() {
		return networkAddress;
	}

	public void setNetworkAddress(IPAddress networkAddress) {
		this.networkAddress = networkAddress;
	}

	public IPAddress getSubnetMask() {
		return subnetMask;
	}

	public void setSubnetMask(IPAddress subnetMask) {
		this.subnetMask = subnetMask;
	}

	public boolean isInUse() {
		return inUse;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	@Override
	public String toString() {
		return "Network Address: " + networkAddress 
				+ "\nSubnet Mask: " + subnetMask
				+ "\nSize: " + networkSize()
				+ "\nHosts: " + maxHosts()
				+ "\nIn use: " + inUse;
	}
}
