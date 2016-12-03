package project.util;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class ConcurrentDoublyLinkedList<T> {

	public Node<T> head;
	public Node<T> tail;

	public ConcurrentDoublyLinkedList() {
		head = new Node<T>(null, Integer.MIN_VALUE);
		tail = new Node<T>(null, Integer.MAX_VALUE);
		head.next.set(tail, false);
		tail.prev.set(head, false);
	}

	class Node<U> {
		AtomicMarkableReference<Node<U>> next;
		AtomicMarkableReference<Node<U>> prev;
		U value;
		int key;

		public Node(U val, int key) {
			this.key = key;
			this.next = new AtomicMarkableReference<Node<U>>(null, false);
			this.prev = new AtomicMarkableReference<Node<U>>(null, false);
			this.value = null;
		}

		public Node(U val) {
			this.key = val.hashCode();
			this.next = new AtomicMarkableReference<Node<U>>(null, false);
			this.prev = new AtomicMarkableReference<Node<U>>(null, false);
			this.value = val;
		}
	}

	boolean insertBefore(Node<T> succ, T val) {

		if (succ == head) {
			return insertAfter(succ, val);
		}
		Node<T> node = new Node<T>(val);
		Node<T> pred = succ.prev.getReference();
		while (true) {

			while (succ.next.isMarked()) {
				succ = this.next(succ);
				if (correctPrev(pred, succ)) {
					pred = succ.prev.getReference();
				}
			}
			node.next.set(succ, false);
			node.prev.set(pred, false);
			if (pred.next.compareAndSet(succ, node, false, false))
				break;
			if (correctPrev(pred, succ)) {
				pred = succ.prev.getReference();
			}
		}
		correctPrev(pred, succ);
		return true;
	}

	boolean insertAfter(Node<T> pred, T val) {

		Node<T> node = new Node<T>(val);
		Node<T> succ;
		int key = val.hashCode();
		while (true) {
			while (true) {
				succ = this.next(pred);
				if (pred.next.isMarked()) {
					if (correctPrev(pred, succ)) {
						pred = succ.prev.getReference();
					}
					continue;
				}
				if (succ.key < key) {
					pred = succ;
				} else
					break;
			}
			if (succ.key == val.hashCode() && !succ.next.isMarked())
				return false;
			node.next.set(succ, false);
			node.prev.set(pred, false);
			if (pred.next.compareAndSet(succ, node, false, false))
				break;
		}
		correctPrev(pred, succ);
		return true;
	}

	Node<T> next(Node<T> node) {
		Node<T> succ;
		Node<T> pred;
		while (true) {
			if (node == tail)
				return tail;
			succ = node.next.getReference();

			if (succ.next.isMarked() && !node.next.isMarked()) {
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

	boolean deleteNode(Node<T> node) {
		Node<T> succ;
		Node<T> pred;

		if (node == head || node == tail)
			return false;

		while (true) {
			succ = node.next.getReference();
			if (node.next.isMarked())
				return false;

			if (node.next.compareAndSet(succ, succ, false, true) == true) {
				pred = node.prev.getReference();
				while (node.prev.isMarked() == false) {
					node.prev.compareAndSet(pred, pred, false, true);
					pred = node.prev.getReference();
				}
				correctPrev(pred, succ);
				return true;
			}
		}
	}

	boolean pushFront(T value) {
		Node<T> node = new Node<T>(value);
		Node<T> pred = head;
		Node<T> succ;

		while (true) {
			succ = pred.next.getReference();
			node.next.set(succ, false);
			node.prev.set(pred, false);
			if (pred.next.compareAndSet(succ, node, false, false)) {
				pushEnd(node, succ);
				return true;
			}
		}
	}

	boolean popBack() {
		Node<T> succ = tail;
		Node<T> node = tail.prev.getReference();

		while (true) {
			if (node.next.getReference() != succ || node.next.isMarked()) {
				if (correctPrev(node, succ))
					node = succ.prev.getReference();
				continue;
			}

			if (node == head)
				return false;

			if (node.next.compareAndSet(succ, succ, false, true)) {
				correctPrev(node.prev.getReference(), succ);
				return true;
			}
		}
	}

	void pushEnd(Node<T> node, Node<T> succ) {

		while (true) {
			if (succ.prev.isMarked() || node.next.getReference() != succ)
				return;

			if (succ.prev.compareAndSet(succ.prev.getReference(), node, false, false))
				break;
		}
		if (node.prev.isMarked())
			correctPrev(node, succ);
	}

	boolean correctPrev(Node<T> pred, Node<T> node) {
		Node<T> lastLink = null;
		Node<T> nodePred;
		Node<T> succ;
		Node<T> tmp;
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

	long size() {
		Node<T> tmp = head.next.getReference();
		int pred = head.key;
		long count = 0;
		while (tmp != tail) {
			if (tmp.next.isMarked() == false) {
				count++;
				if (tmp.key <= pred) {
					System.out.println(tmp.key + " " + pred);
				}
				pred = tmp.key;
			}
			tmp = tmp.next.getReference();
		}
		return count;
	}
}
