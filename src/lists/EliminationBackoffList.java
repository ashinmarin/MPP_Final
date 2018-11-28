package lists;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import util.EliminationArray;

public class EliminationBackoffList<T> implements lists.Set<T> {
	private Node<T> head;
	private final int capacity;
	private final int timemout;
	private Lock lock = new ReentrantLock();
	util.EliminationArray<Cell<T>> eliminationArray;

	static enum Operation {
		ADD, REMOVE
	}

	public EliminationBackoffList(int capacity, int timeout) {
		// Add sentinels to start and end
		this.head = new Node<T>(Integer.MIN_VALUE);
		head.next = new Node<T>(Integer.MAX_VALUE);

		this.capacity = capacity;
		this.timemout = timeout;
		this.eliminationArray = new EliminationArray<Cell<T>>(this.capacity, this.timemout);
	}

	/**
	 * Test whether element is present
	 * 
	 * {@link CoarseList.java contains}
	 * 
	 * @param item element to test
	 * @return true iff element is present
	 */
	public boolean contains(T item) {
		Node<T> pred, curr;
		int key = item.hashCode();
		lock.lock();
		try {
			pred = this.head;
			curr = pred.next;
			while (curr.key < key) {
				pred = curr;
				curr = curr.next;
			}
			return (key == curr.key);
		} finally { // always unlock
			lock.unlock();
		}
	}

	public boolean add(T item) {
		while (true) {
			boolean isLocked = lock.tryLock();
			if (isLocked) {
				try {
					boolean res = tryAdd(item);
					return res;
				} finally {
					lock.unlock();
				}
			} else {
				Cell<T> value = new Cell<T>(Operation.ADD, item);
				try {
					Cell<T> exchange = eliminationArray.visit(value);
					if (exchange != null && exchange.operation == Operation.REMOVE && exchange.data == item) {
						return true;
					}
				} catch (TimeoutException e) {
				}
			}
		}
	}

	boolean tryAdd(T item) {
		int key = item.hashCode();
		Node<T> pred = head;
		Node<T> curr = pred.next;
		while (curr.key < key) {
			pred = curr;
			curr = curr.next;
		}
		if (curr.key == key) {
			return false;
		}
		Node<T> node = new Node<T>(item);
		node.next = curr;
		pred.next = node;
		return true;
	}

	public boolean remove(T item) {
		while (true) {
			boolean flag = lock.tryLock();
			if (flag) {
				try {
					boolean result = tryRemove(item);
					return result;
				} finally {
					lock.unlock();
				}
			} else {
				Cell<T> value = new Cell<T>(Operation.REMOVE, item);
				try {
					Cell<T> exchange = eliminationArray.visit(value);
					if (exchange != null && exchange.operation == Operation.ADD && exchange.data == item) {
						return true;
					}
				} catch (TimeoutException e) {
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	boolean tryRemove(T item) {
		int key = item.hashCode();
		Node<T> pred = head;
		Node<T> curr = pred.next;
		while (curr.key < key) {
			pred = curr;
			curr = curr.next;
		}
		if (key == curr.key) {
			pred.next = curr.next;
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("hiding")
	private class Node<T> {
		private T item;
		int key;
		Node<T> next;

		Node(T item) {
			this.setItem(item);
			this.key = item.hashCode();
			this.next = null;
		}

		Node(int key) {
			this.setItem(null);
			this.key = key;
			this.next = null;
		}

		@SuppressWarnings("unused")
		public T getItem() {
			return item;
		}

		public void setItem(T item) {
			this.item = item;
		}
	}

	@SuppressWarnings("hiding")
	private class Cell<T> {
		int key;
		volatile T data;
		Operation operation;

		@SuppressWarnings("unused")
		public Cell(Operation operation, T data) {
			this.operation = operation;
			this.data = data;
		}
	}

}
