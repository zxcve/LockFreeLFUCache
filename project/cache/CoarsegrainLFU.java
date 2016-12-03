package project.cache;

import java.util.concurrent.locks.ReentrantLock;

public class CoarsegrainLFU extends SequentialLFU {
	private final ReentrantLock lock = new ReentrantLock();

	public CoarsegrainLFU(int capacity) {
		super(capacity);
	}

	@Override
	public char get(int key) {
		lock.lock();
		try {
			return super.get(key);
		} finally {
			lock.unlock();
		}
	}
}
