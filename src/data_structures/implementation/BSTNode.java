package data_structures.implementation;

public class BSTNode<T extends Comparable<T>> implements Comparable<BSTNode<T>> {

	public T data;
	private BSTNode<T> left;
	private BSTNode<T> right;
	private BSTNode<T> parent;

	/* Constructor */
	public BSTNode() {
		this.left = null;
		this.right = null;
		this.parent = null;
		this.data = null;
	}

	public BSTNode(T data) {
		this.data = data;
	}
	
	public BSTNode(T data, BSTNode<T> parent) {
		this.data = data;
		this.parent = parent;
	}

	public BSTNode(T data, BSTNode<T> left, BSTNode<T> right) {
		this.data = data;
		this.left = left;
		this.right = right;
	}

	/* Functions to set data or nodes */
	public void setData(T data) {
		this.data = data;
	}

	public void setLeft(BSTNode<T> left) {
		this.left = left;
	}

	public void setRight(BSTNode<T> right) {
		this.right = right;
	}
	
	public void setParent(BSTNode<T> parent) {
		this.parent = parent;
	}
	
	/* Functions to get data or nodes */
	public T getData() {
		return this.data;
	}

	public BSTNode<T> getLeft() {
		return this.left;
	}

	public BSTNode<T> getRight() {
		return this.right;
	}

	public BSTNode<T> getParent() {
		return parent;
	}
	

	public int compareTo(BSTNode<T> node) {
		if(node==null || node.data==null) 
	    return -1;
		
		return this.data.compareTo(node.data);
	}

}
