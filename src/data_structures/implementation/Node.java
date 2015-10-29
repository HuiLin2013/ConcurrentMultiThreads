package data_structures.implementation;


public class Node<T> 
{
	public T data;
	public Node<T> next = null;
	public int key;

	public Node(T data) {
		this.data = data;
		this.next = null;
		this.key = data.hashCode();
	}
	
	public Node() {
		this.data = null;
		this.next = null;
	}
}
