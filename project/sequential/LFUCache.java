package project.sequential;

import java.util.Hashtable;
import java.util.LinkedList;

import project.benchmarks.Cache;

public class LFUCache<K, V> implements Cache<K, V> {

	private int capacity;
	private Hashtable<K, ItemNode> hashtable;
	private FrequencyNode sentinalHead;

	public LFUCache(int capacity) {
		this.capacity = capacity;
		hashtable = new Hashtable<K, ItemNode>(capacity);
		sentinalHead = new FrequencyNode(0);
		// After this sentinalHead is guaranteed to always have a non-null next
		addNewFrequencyNode(1, sentinalHead, sentinalHead.next);
	}

	@Override
	public void put(K key, V value) {
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

	@Override
	public V get(K key) {
		ItemNode item = hashtable.get(key);
		if (null == item) {
			throw new RuntimeException("No such element");
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

	@Override
	public boolean contains(K key) {
		return hashtable.containsKey(key);
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
		K key;
		V value;
		FrequencyNode parent;

		public ItemNode(K key, V value, FrequencyNode parent) {
			this.key = key;
			this.value = value;
			this.parent = parent;
		}
	}

}
