import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class LFUCacheTest {

	private final Memory A = new LFUCacheTest.Memory(0x100, 'a');
	private final Memory B = new LFUCacheTest.Memory(0x101, 'b');
	private final Memory C = new LFUCacheTest.Memory(0x102, 'c');
	private final Memory D = new LFUCacheTest.Memory(0x103, 'd');
	private final Memory E = new LFUCacheTest.Memory(0x104, 'e');
	private final Memory F = new LFUCacheTest.Memory(0x105, 'f');
	private final Memory G = new LFUCacheTest.Memory(0x106, 'g');
	private final Memory H = new LFUCacheTest.Memory(0x107, 'h');
	private final Memory I = new LFUCacheTest.Memory(0x108, 'i');
	private final Memory J = new LFUCacheTest.Memory(0x109, 'j');

	private LFUCache<Integer, Character> cache;

	@Before
	public void setUp() {
		cache = new LFUCache<>(5);

	}

	@Test
	public void testPutGet() {
		cache.put(A.addr, A.val);
		cache.put(B.addr, B.val);
		cache.put(C.addr, C.val);
		assertEquals(new Character('a'), cache.get(A.addr));
		assertEquals(new Character('b'), cache.get(B.addr));
		assertEquals(new Character('c'), cache.get(C.addr));
	}

	@Test
	public void testEvict() {
		cache.put(A.addr, A.val);
		cache.put(B.addr, B.val);
		cache.put(C.addr, C.val);
		cache.put(D.addr, D.val);
		cache.get(A.addr);
		cache.get(B.addr);
		cache.get(C.addr);
		cache.get(D.addr);
		cache.put(F.addr, F.val);
		cache.put(E.addr, E.val);
		assertTrue(cache.contains(E.addr));
		assertTrue(cache.contains(A.addr));
		assertTrue(cache.contains(B.addr));
		assertTrue(cache.contains(C.addr));
		assertTrue(cache.contains(D.addr));
		assertFalse(cache.contains(F.addr));
	}

	@Test
	public void testEvict2() {
		cache.put(A.addr, A.val);
		cache.put(B.addr, B.val);
		cache.put(C.addr, C.val);
		cache.put(D.addr, D.val);
		cache.get(A.addr);
		cache.get(B.addr);
		cache.get(C.addr);
		cache.put(F.addr, F.val);
		cache.put(E.addr, E.val);
		assertTrue(cache.contains(E.addr));
		assertTrue(cache.contains(A.addr));
		assertTrue(cache.contains(B.addr));
		assertTrue(cache.contains(C.addr));
		assertTrue(cache.contains(F.addr));
		assertFalse(cache.contains(D.addr));
	}

	@Test
	public void testEvict3() {
		cache.put(A.addr, A.val);
		cache.put(B.addr, B.val);
		cache.put(C.addr, C.val);
		cache.put(D.addr, D.val);
		cache.put(E.addr, E.val);
		cache.put(F.addr, F.val);
		cache.put(G.addr, G.val);
		cache.put(H.addr, H.val);
		cache.put(I.addr, I.val);
		cache.put(J.addr, J.val);
		assertTrue(cache.contains(F.addr));
		assertTrue(cache.contains(G.addr));
		assertTrue(cache.contains(H.addr));
		assertTrue(cache.contains(I.addr));
		assertTrue(cache.contains(J.addr));
		assertFalse(cache.contains(A.addr));
		assertFalse(cache.contains(B.addr));
		assertFalse(cache.contains(C.addr));
		assertFalse(cache.contains(D.addr));
		assertFalse(cache.contains(E.addr));
	}

	@Test
	public void testEmpty() {
		try {
			cache.get(A.addr);
		} catch (RuntimeException e) {
			assertEquals("No such element", e.getMessage());
		}

	}

	@Test
	public void testDuplicate() {
		cache.put(A.addr, A.val);
		try {
			cache.put(A.addr, A.val);
		} catch (RuntimeException e) {
			assertEquals("Duplicate", e.getMessage());
		}

	}

	private class Memory {
		public final Integer addr;
		public final Character val;

		public Memory(Integer addr, Character val) {
			this.addr = addr;
			this.val = val;
		}
	}
}
