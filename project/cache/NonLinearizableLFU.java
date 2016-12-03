package project.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;

import project.util.IODevice;

public class NonLinearizableLFU implements Cache {
	ConcurrentHashMap<Integer, DataNode<Integer, Character>> hashMap;
	int maxSize;
	FreqNode head;
	FreqNode tail;
	FreqNode sentinal;
	DataNode<Integer, Character> dSentinal;
	AtomicInteger currentSize;

	private class FreqNode {
		int freq;
		AtomicMarkableReference<FreqNode> next;
		AtomicMarkableReference<FreqNode> prev;
		DataNode<Integer, Character> head;
		DataNode<Integer, Character> tail;

		public FreqNode(int freq) {
			this.freq = freq;
			this.next = new AtomicMarkableReference<FreqNode>(null, false);
			this.prev = new AtomicMarkableReference<FreqNode>(null, false);
			this.head = new DataNode<Integer, Character>(null, null);
			this.tail = new DataNode<Integer, Character>(null, null);
			this.head.next.set(tail, false);
			this.tail.prev.set(head, false);
		}
	}

	public class DataNode<X, Y> {
		AtomicReference<FreqNode> parent;
		AtomicMarkableReference<DataNode<X, Y>> next;
		AtomicMarkableReference<DataNode<X, Y>> prev;
		Y data;
		X key;

		public DataNode(X key, Y dataVal) {
			this.data = dataVal;
			this.key = key;
			this.next = new AtomicMarkableReference<DataNode<X, Y>>(null, false);
			this.prev = new AtomicMarkableReference<DataNode<X, Y>>(null, false);
			;
			this.parent = new AtomicReference<FreqNode>(null);
		}
	}

	public NonLinearizableLFU(int sizeVal) {
		this.maxSize = sizeVal;
		hashMap = new ConcurrentHashMap<Integer, DataNode<Integer, Character>>();
		this.head = new FreqNode(Integer.MIN_VALUE);
		this.tail = new FreqNode(Integer.MAX_VALUE);
		this.sentinal = new FreqNode(1);
		this.dSentinal = new DataNode<Integer, Character>(null, null);
		this.head.next.set(tail, false);
		this.tail.prev.set(head, false);
		this.currentSize = new AtomicInteger();
	}

	private char getFromMemory(int key) {
		return IODevice.read(key);
	}

	FreqNode next(FreqNode node) {
		FreqNode succ = null;
		FreqNode pred;
		while (true) {
			if (node == tail)
				return tail;

			succ = node.next.getReference();

			if (!node.next.isMarked() && succ.next.isMarked()) {
				while (succ.prev.isMarked() == false) {
					pred = succ.prev.getReference();
					succ.prev.compareAndSet(pred, pred, false, true);
				}
				;
				node.next.compareAndSet(succ, succ.next.getReference(), false, false);
				continue;
			}
			node = succ;
			if (succ.next.isMarked() == false)
				return node;
		}
	}

	DataNode<Integer, Character> next(DataNode<Integer, Character> node, DataNode<Integer, Character> tailNode) {
		DataNode<Integer, Character> succ;
		DataNode<Integer, Character> pred;
		while (true) {
			if (node == tailNode)
				return tailNode;
			succ = node.next.getReference();

			if (!node.next.isMarked() && succ.next.isMarked()) {
				while (succ.prev.isMarked() == false) {
					pred = succ.prev.getReference();
					succ.prev.compareAndSet(pred, pred, false, true);
				}
				;
				node.next.compareAndSet(succ, succ.next.getReference(), false, false);
				continue;
			}
			node = succ;
			if (succ.next.isMarked() == false)
				return node;
		}
	}

	FreqNode insertFreq(FreqNode pred, int freq) {
		FreqNode node = new FreqNode(freq);
		FreqNode succ;
		while (true) {
			while (true) {
				succ = this.next(pred);
				if (pred.next.isMarked()) {
					if (correctFreqPrev(pred, succ)) {
						pred = succ.prev.getReference();
					}
					continue;
				}
				if (succ.freq < freq) {
					pred = succ;
				} else
					break;
			}
			if (succ.freq == freq && !succ.next.isMarked())
				return succ;
			if (pred.freq == freq && !pred.next.isMarked())
				return pred;
			node.next.set(succ, false);
			node.prev.set(pred, false);
			if (pred.next.compareAndSet(succ, node, false, false))
				break;
		}
		correctFreqPrev(pred, succ);
		return node;
	}

	boolean pushFront(FreqNode freq, DataNode<Integer, Character> node) {
		DataNode<Integer, Character> pred = freq.head;
		DataNode<Integer, Character> succ;
		DataNode<Integer, Character> oldSucc = null;
		FreqNode parent = node.parent.get();

		while (true) {
			succ = pred.next.getReference();

			if (node.next.compareAndSet(oldSucc, succ, false, false)) {
				oldSucc = succ;
				node.prev.set(pred, false);
				if (pred.next.compareAndSet(succ, node, false, false)) {
					pushEnd(node, succ);
					node.parent.compareAndSet(parent, freq);
					return true;
				}
				if (pred.next.isMarked()) {
					while (node.next.isMarked() == false) {
						if (node.next.compareAndSet(node.next.getReference(), null, false, false))
							break;
					}

					return false;
				}
			} else {
				return false;
			}
		}
	}

	boolean deleteNode(DataNode<Integer, Character> node) {
		DataNode<Integer, Character> succ;
		DataNode<Integer, Character> pred;
		if (node.key == null) {
			return false;
		}
		while (true) {
			succ = node.next.getReference();
			if (succ == null)
				return false;
			if (node.next.isMarked()) {
				return false;
			}

			if (node.next.compareAndSet(succ, succ, false, true) == true) {
				pred = node.prev.getReference();
				while (node.prev.isMarked() == false) {
					node.prev.compareAndSet(pred, pred, false, true);
					pred = node.prev.getReference();
				}

				correctNodePrev(pred, succ);
				FreqNode parent = node.parent.get();
				next(parent.head, parent.tail);
				if (parent.head.next.compareAndSet(parent.tail, parent.tail, false, true)) {
					deleteFreq(parent);
				}
				return true;
			}
		}
	}

	boolean deleteFreq(FreqNode node) {
		FreqNode succ;
		FreqNode pred;

		if (node == head || node == tail) {
			return false;
		}
		while (true) {
			succ = node.next.getReference();
			if (node.next.isMarked()) {
				return false;
			}
			if (node.next.compareAndSet(succ, succ, false, true) == true) {
				pred = node.prev.getReference();
				while (node.prev.isMarked() == false) {
					node.prev.compareAndSet(pred, pred, false, true);
					pred = node.prev.getReference();
				}
				correctFreqPrev(pred, succ);
				return true;
			}
		}
	}

	void pushEnd(DataNode<Integer, Character> node, DataNode<Integer, Character> succ) {

		while (true) {
			if (succ.prev.isMarked() || node.next.getReference() != succ)
				return;

			if (succ.prev.compareAndSet(succ.prev.getReference(), node, false, false))
				break;
		}
		if (node.prev.isMarked())
			correctNodePrev(node, succ);
	}

	boolean popBack(FreqNode freq) {
		DataNode<Integer, Character> succ = freq.tail;
		DataNode<Integer, Character> node = freq.tail.prev.getReference();

		while (true) {

			if (node == freq.head || freq.head.next.isMarked()) {
				return false;
			}

			if (node.next.getReference() != succ || node.next.isMarked()) {
				if (correctNodePrev(node, succ))
					node = succ.prev.getReference();
				continue;
			}

			if (node.next.compareAndSet(succ, succ, false, true)) {
				hashMap.remove(node.key);
				while (node.parent.compareAndSet(node.parent.get(), sentinal) == false)
					;
				correctNodePrev(node.prev.getReference(), succ);
				next(freq.head, freq.tail);
				if (freq.head.next.compareAndSet(freq.tail, freq.tail, false, true)) {
					deleteFreq(freq);
				}
				return true;
			}
		}
	}

	boolean correctNodePrev(DataNode<Integer, Character> pred, DataNode<Integer, Character> node) {
		DataNode<Integer, Character> lastLink = dSentinal;
		DataNode<Integer, Character> nodePred;
		DataNode<Integer, Character> succ;
		DataNode<Integer, Character> tmp;

		while (true) {
			nodePred = node.prev.getReference();
			if (node.prev.isMarked())
				return false;

			if (pred == null)
				return false;
			succ = pred.next.getReference();

			if (pred.next.isMarked()) {
				if (lastLink == dSentinal) {
					pred = pred.prev.getReference();
				} else {
					while (pred.prev.isMarked() == false) {
						tmp = pred.prev.getReference();
						pred.prev.compareAndSet(tmp, tmp, false, true);
					}
					lastLink.next.compareAndSet(pred, succ, false, false);
					pred = lastLink;
					lastLink = dSentinal;
				}
				continue;
			}

			if (succ != node) {
				lastLink = pred;
				pred = succ;

				continue;
			}

			if (node.prev.compareAndSet(nodePred, pred, false, false)) {
				if (pred.prev.isMarked())
					continue;
				return true;
			}
		}
	}

	boolean correctFreqPrev(FreqNode pred, FreqNode node) {
		FreqNode lastLink = null;
		FreqNode nodePred;
		FreqNode succ;
		FreqNode tmp;
		while (true) {
			nodePred = node.prev.getReference();
			if (node.next.isMarked())
				return false;
			succ = pred.next.getReference();

			if (pred.next.isMarked()) {
				if (lastLink == null) {
					pred = pred.prev.getReference();
				} else {
					while (pred.prev.isMarked() == false) {
						tmp = pred.prev.getReference();
						pred.prev.compareAndSet(tmp, tmp, false, true);
					}
					lastLink.next.compareAndSet(pred, succ, false, false);
					pred = lastLink;
					lastLink = null;
				}
				continue;
			}

			if (succ != node) {
				lastLink = pred;
				pred = succ;
				continue;
			}

			if (node.prev.compareAndSet(nodePred, pred, false, false)) {
				if (pred.prev.isMarked())
					continue;
				return true;
			}
		}
	}

	private void evict() {
		FreqNode node;
		while (true) {
			node = head.next.getReference();
			if (node.head.next.compareAndSet(node.tail, node.tail, false, true)) {
				deleteFreq(node);
				continue;
			}

			if (popBack(node)) {
				currentSize.getAndDecrement();
				break;
			}
		}
	}
	
	@Override
	public char get(int key) {

		while (true) {

			DataNode<Integer, Character> dataNode = hashMap.get(key);
			if (dataNode == null) {
				char data = getFromMemory(key);
				DataNode<Integer, Character> node = new DataNode<Integer, Character>(key, data);
				int mySize = currentSize.getAndIncrement();
				if (mySize >= maxSize) {
					evict();
				}
				hashMap.putIfAbsent(key, node);
			}

			while (true) {
				dataNode = hashMap.get(key);

				if (dataNode == null) {
					break;
				}

				if (dataNode.next.isMarked()) {
					if (dataNode.parent.get() == sentinal) {
						break;
					}
					continue;
				}

				int freq = dataNode.parent.get() == null ? 1 : dataNode.parent.get().freq + 1;
				FreqNode parent;
				FreqNode newParent;
				DataNode<Integer, Character> newNode;

				if (freq > 1) {
					if (deleteNode(dataNode) == false) {
						continue;
					}
					newNode = new DataNode<Integer, Character>(dataNode.key, dataNode.data);
					newNode.parent.set(dataNode.parent.get());

					hashMap.put(key, newNode);
					dataNode = newNode;
				}
				parent = (freq == 1) ? head : dataNode.parent.get();
				while (true) {
					newParent = insertFreq(parent, freq);

					if (pushFront(newParent, dataNode)) {
						return dataNode.data;
					} else {
						if (newParent.head.next.isMarked()) {
							continue;
						} else {
							break;
						}
					}
				}
			}
		}
	}

	public void print() {
		FreqNode tmp = head;
		DataNode<Integer, Character> tmp2;
		while (tmp != null) {
			tmp2 = tmp.head;
			while (tmp2 != tmp.tail) {
				System.out.println(tmp.freq + " " + tmp.next.isMarked() + "->" + "<" + tmp2.key + ","
						+ tmp2.next.isMarked() + ">");
				tmp2 = tmp2.next.getReference();
			}
			System.out.println("************");
			tmp = tmp.next.getReference();
		}
	}
}
