package project.coarsegrain;

import java.util.concurrent.locks.ReentrantLock;

import project.sequential.LFUCache;

public class CoarseGrainLFUCache<K, V> extends LFUCache<K, V> {
	private final ReentrantLock lock = new ReentrantLock();

	public CoarseGrainLFUCache(int capacity) {
		super(capacity);
	}

	@Override
	public void put(K key, V value) {
		lock.lock();
		try {
			super.put(key, value);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public V get(K key) {
		lock.lock();
		try {
			return super.get(key);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean contains(K key) {
		lock.lock();
		try {
			return super.contains(key);
		} finally {
			lock.unlock();
		}
	}
}
