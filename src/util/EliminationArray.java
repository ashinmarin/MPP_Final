package util;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EliminationArray<T> {
	private final int duration;
	private final int capacity;
	LockFreeExchanger<T>[] exchanger;
	Random random;

	@SuppressWarnings("unchecked")
	public EliminationArray(int capacity, int duration) {
		this.exchanger = (LockFreeExchanger<T>[]) new LockFreeExchanger[capacity];
		for (int i = 0; i < capacity; i++) {
			this.exchanger[i] = new LockFreeExchanger<T>();
		}
		this.random = new Random();
		this.capacity = capacity;
		this.duration = duration;
	}

	public T visit(T value) throws TimeoutException {
		int slot = random.nextInt(this.capacity);
		return (this.exchanger[slot].exchange(value, duration, TimeUnit.MICROSECONDS));
	}

}
