package data_structures.implementation;

import data_structures.Sorted;
import data_structures.implementation.BSTNode;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class CoarseGrainedTree<T extends Comparable<T>> implements Sorted<T> {

	private BSTNode<T> root;

	private Lock lock;

	/* Constructor */
	public CoarseGrainedTree() {
		this.root = null;
		lock = new ReentrantLock();
	}

	public void add(T t) {

		BSTNode<T> addNode = new BSTNode<T>(t);
		if (t == null)
			return;

		lock.lock();
		try {
			if (root == null) {
				// Add the new node when root is not available
				this.root = addNode;
				return;
			}

			insert(addNode, root, null);
		}
		finally {
			lock.unlock();
		}
	}

	private boolean insert(BSTNode<T> addNode, BSTNode<T> curr, BSTNode<T> parent) {
		if (curr == null || addNode == null)
			return false;
		// When addNode is smaller than or equal to the current node,
		// it goes to the left
		if (addNode.compareTo(curr) < 0 || addNode.compareTo(curr) == 0) {
			if (curr.getLeft() == null) {
				curr.setLeft(addNode);
			    setParentNode(curr.getLeft(), curr);
			}
			else
				return insert(addNode, curr.getLeft(), curr);
		}
		// When addNode is larger than the current node,
	    // it goes to the right
		else if (addNode.compareTo(curr) > 0) {
			if (curr.getRight() == null){
				curr.setRight(addNode);
			    setParentNode(curr.getRight(), curr);
		    }
			else
				return insert(addNode, curr.getRight(), curr);
		}
		return true;
	}
	
	
	public void setParentNode (BSTNode<T> newNode, BSTNode<T> newParent)
	{
		 newNode.setParent(newParent);		 
	}
	

	public void remove(T t) {
		BSTNode<T> removeNode = new BSTNode<T>(t);
		lock.lock();
		try {
			// First check if root exists
			if (root == null)
				// The tree is empty
				// Node does not exist in the tree
				return;
            // Secondly check if root data is the one to be removed
			if (root.compareTo(removeNode) == 0) {
				BSTNode<T> dummyNode = new BSTNode<T>();
				dummyNode.setLeft(root);
				delete(removeNode, root,dummyNode);
				root = dummyNode.getLeft();
				return;
			}
			else
				delete(removeNode, root, null);
		}
		finally {
			lock.unlock();
		}
	}

	private boolean delete(BSTNode<T> removeNode, BSTNode<T> curr, BSTNode<T> parent){
		if (removeNode == null || curr == null)
			return false;
		
		if (removeNode.compareTo(curr) < 0)
		{ 
			/* when data of removeNode is less than data of current node */
			if(curr.getLeft() != null)
			// if current node has left child, run the same algorithm to find removeNode to be removed
				return delete(removeNode, curr.getLeft(), curr);
			else
			// if current node has no left child, removeNode does not exist in the BST
				return false;
		}
		else if(removeNode.compareTo(curr)>0) {
			/* when data of removeNode is greater than data of current node */
			if(curr.getRight() != null)
			 // if current node has right child, run the same algorithm to find removeNode to be removed
				return delete(removeNode, curr.getRight(), curr);
			else
		    // if current node has no right child, removeNode does not exist in the BST
				return false;
		}
		else {
			/* when data of removeNode is as same as data of current node */
			// there are three cases
			
			if(curr.getLeft() != null && curr.getRight() != null) {
				// Case 1: removeNode has two children
				// Find minimum data in the right subtree of removeNode
				// Replace data of removeNode with that minimum data
				curr.setData(searchMinValue(curr.getRight()));
				// Within the subtree of removeNode, remove the node holding that minimum data
				delete(new BSTNode<T>(curr.getData()),curr.getRight(), curr);
			}
			// Case 2: removeNode has one child 
			// Case 3: removeNode has none children
			else if (parent.getLeft() == curr) {
				parent.setLeft((curr.getLeft() != null) ? curr.getLeft():curr.getRight());
			}
			else if (parent.getRight() == curr) {
				parent.setRight((curr.getLeft() != null) ? curr.getLeft():curr.getRight());
			}

            return true;
		}
	}

	
	public T searchMinValue (BSTNode<T> node) {
		// To find the minimum data within the subtree of node 
		 if(node.getLeft() == null)
			 return node.getData();
		 else
			 return searchMinValue(node.getLeft());
	}
	
	
	public String toString() {
		BSTNode<T> node = this.root;
		if (node == null)
			return "[]";
		else
			return "[" + node.getData() + printString(node.getLeft())
				+ printString(node.getRight()) + "]";
	}

	private String printString (BSTNode<T> node) {
		if (node == null)
			return "";
		else
			return "[" + node.getData() + printString(node.getLeft())
				+ printString(node.getRight()) + "]";
	}
}
