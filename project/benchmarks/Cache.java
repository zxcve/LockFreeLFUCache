package project.benchmarks;

public interface Cache<K, V> {
	public void put(K key, V value);
	public V get(K key);
	public boolean contains(K key);
}
