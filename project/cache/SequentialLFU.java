package project.cache;

import java.util.Hashtable;
import java.util.LinkedList;

import project.util.IODevice;

public class SequentialLFU implements Cache {

	private int capacity;
	private Hashtable<Integer, ItemNode> hashtable;
	private FrequencyNode sentinalHead;

	public SequentialLFU(int capacity) {
		this.capacity = capacity;
		hashtable = new Hashtable<Integer, ItemNode>(capacity);
		sentinalHead = new FrequencyNode(0);
		// After this sentinalHead is guaranteed to always have a non-null next
		addNewFrequencyNode(1, sentinalHead, sentinalHead.next);
	}

	@Override
	public char get(int key) {
		ItemNode item = hashtable.get(key);
		if (null == item) {
			return readHard(key);
		}
		FrequencyNode parent = item.parent;
		FrequencyNode newParent;
		if (null == parent.next || parent.next.value != parent.value + 1) {
			newParent = addNewFrequencyNode(parent.value + 1, parent, null);
		} else {
			newParent = parent.next;
		}
		newParent.items.addFirst(item);
		item.parent = newParent;

		parent.items.remove(item);
		if (0 == parent.items.size()) {
			parent.remove();
		}
		return item.value;
	}

	private void put(int key, char value) {
		if (hashtable.containsKey(key)) {
			throw new RuntimeException("Duplicate");
		}
		if (hashtable.size() == capacity) {
			evictLFUitem();
		}
		FrequencyNode freqNode = sentinalHead.next;
		if (freqNode.value != 1) {
			freqNode = addNewFrequencyNode(1, sentinalHead, sentinalHead.next);
		}
		ItemNode item = new ItemNode(key, value, freqNode);
		freqNode.items.addFirst(item);
		hashtable.put(key, item);
	}

	private char readHard(int key) {
		char read = IODevice.read(key);
		put(key, read);
		return read;
	}

	private void evictLFUitem() {
		FrequencyNode freqNode = sentinalHead.next;
		ItemNode item = freqNode.items.removeLast();
		hashtable.remove(item.key);
	}

	private FrequencyNode addNewFrequencyNode(int value, FrequencyNode prev, FrequencyNode next) {
		FrequencyNode freqNode = new FrequencyNode(value);
		freqNode.insert(prev, next);
		return freqNode;
	}

	private class FrequencyNode {
		int value;
		FrequencyNode next;
		FrequencyNode prev;
		LinkedList<ItemNode> items;

		public FrequencyNode(int value) {
			this.value = value;
			this.prev = null;
			this.next = null;
			if (value > 0) {
				items = new LinkedList<ItemNode>();
			}
		}

		public void insert(FrequencyNode prev, FrequencyNode next) {
			this.prev = prev;
			prev.next = this;

			this.next = next;
			if (next != null) {
				next.prev = this;
			}
		}

		public void remove() {
			prev.next = next;
			if (next != null) {
				next.prev = prev;
			}
		}
	}

	private class ItemNode {
		int key;
		char value;
		FrequencyNode parent;

		public ItemNode(int key, char value, FrequencyNode parent) {
			this.key = key;
			this.value = value;
			this.parent = parent;
		}
	}

	@Override
	public void print() {
		for (FrequencyNode tmp = sentinalHead.next; tmp != null; tmp = tmp.next) {
			System.out.print(tmp.value + " " + "-> ");
			for (ItemNode item : tmp.items) {
				System.out.print(item.value + " ");
			}
			System.out.println();
			System.out.println("************");
		}
	}
}
