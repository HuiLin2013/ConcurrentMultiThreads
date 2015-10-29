package data_structures.implementation;

import data_structures.Sorted;
import data_structures.implementation.Node;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoarseGrainedList<T extends Comparable<T>> implements Sorted<T> {

	private Node<T> _head;
	private Lock _lock = new ReentrantLock();

	public CoarseGrainedList() {
		//constructor
		_head = new Node<T>();
		_head.key = Integer.MIN_VALUE;
		_head.next = new Node<T>();
		_head.next.key = Integer.MAX_VALUE;
	}

	public void add(T t) 
	{
		Node<T> prev, curr;
		int key = t.hashCode();
		//lock mutex
        _lock.lock();
        try
		{
			prev = _head;
			curr = _head.next;
			while (curr.key <  key)
			{
				//finding the place to add the element
				prev = curr;
				curr = curr.next;
			}
			Node<T> node = new Node<T>(t);
			node.next = curr;
			prev.next = node;
		}
		finally
		{
			_lock.unlock();
		}
	}

	public void remove(T t) {
		Node<T> prev, curr;
    	int key = t.hashCode();
		//lock the mutex
    	_lock.lock();
		try 
		{
      		prev = _head;
			curr = prev.next;
			while (curr.key < key) 
			{
				prev = curr;
				curr = curr.next;
			}
			if (key == curr.key) 
			{
				// java GC will cleanup when no one references
				prev.next = curr.next;
			} 
		}
		finally 
		{
			_lock.unlock();
		}
	}


	public String toString() {
		String ret = "[";
		Node<T> curr = _head;
		_lock.lock();
		try
		{
			while (curr.next.key != Integer.MAX_VALUE) {
				ret += curr.data;
				curr = curr.next;
				ret += ", ";
			}
		}
		finally
		{
			_lock.unlock();
		}
		return ret + "]";
	}
}
