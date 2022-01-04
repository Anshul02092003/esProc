package com.scudata.dm.op;

import java.util.ArrayDeque;
import java.util.Deque;

import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.util.Variant;

/**
 * ��¼���ɵĺ���������������бȽ�
 * ���ڹ�ϣ��������ʱ��������ֶι�ϣֵ��ͬ����
 * @author RunQian
 *
 */
public class RecordTree {
	public static final boolean RED = true; // ��ɫ
	public static final boolean BLACK = false; // ��ɫ
	
	/**
	 * ������Ľڵ�
	 * @author RunQian
	 *
	 */
	public static class Node {
		Record r;
		boolean color;
		
		Node parent;
		Node left;
		Node right;
		
		public Node(Record r, boolean color) {
			this.r = r;
			this.color = color;
		}
		
		public Node(boolean color) {
			this.color = color;
		}
		
		public Node(boolean color, Node parent){
			this.color = color;
			this.parent = parent;
		}
	}
	
	private Node root;
	
	public RecordTree() {
	}
	
	public RecordTree(Record r) {
		root = new Node(r, BLACK);
	}
	
	/**
	 * �����������Ҽ�¼�ڵ㣬�Ҳ��������½��Ľڵ㣬�����Ѽ�¼�����½ڵ�
	 * @param values ����ֵ����
	 * @return
	 */
	public Node get(Object []values) {
		if (root == null) {
			return root = new Node(BLACK);
		}

		Node current = root;
		Node parent = null;
		
		while (true) {
			Object []curValues = current.r.getFieldValues();
			int cmp = Variant.compareArrays(curValues, values, values.length);
			if (cmp == 0) {
				return current;
			} else if (cmp > 0) {
				parent = current;
				current = current.left;
				
				if (current == null) {
					Node newNode = new Node(RED, parent);
					parent.left = newNode;
					
					balanceInsertion(newNode);
					//size++;
					return newNode;
				}
			} else {
				parent = current;
				current = current.right;
				
				if (current == null) {
					Node newNode = new Node(RED, parent);
					parent.right = newNode;
					
					balanceInsertion(newNode);
					//size++;
					return newNode;
				}
			}
		}
	}
	
	// ����ڵ�������ƽ��
	private void balanceInsertion(Node node) {
		Node parent;
		Node gparent;
		
		while ((parent = node.parent) != null && parent.color == RED) {
			gparent = parent.parent;
			if (gparent.left == parent) {
				Node uncle = gparent.right;
				if (uncle != null && uncle.color == RED) {
					parent.color = BLACK;
					uncle.color = BLACK;
					gparent.color = RED;
					node = gparent;
				} else {
					if (parent.right == node) {
						leftRonate(parent);
						Node temp = node;
						node = parent;
						parent = temp;
					}
					
					parent.color = BLACK;
					gparent.color = RED;
					rightRonate(gparent);
				}
			} else {
				Node uncle = gparent.left;
				if (uncle != null && uncle.color == RED) {
					parent.color = BLACK;
					uncle.color = BLACK;
					gparent.color = RED;
					node = gparent;
				} else {
					if (parent.left == node) {
						rightRonate(parent);
						Node temp = node;
						node = parent;
						parent = temp;
					}
					
					parent.color = BLACK;
					gparent.color = RED;
					leftRonate(gparent);
				}
			}
		}
		
		root.color = BLACK;
	}
	
	//��ĳ���ڵ��������
	private void leftRonate(Node x) {
		Node y = x.right;
		if (y.left != null) {
			y.left.parent = x;
		}

		x.right = y.left;
		y.left = x;
		y.parent = x.parent;

		if(x.parent != null) {
			if(x.parent.left == x) {
				x.parent.left = y;
			} else {
				x.parent.right = y;
			}
		} else {
			root = y;
		}
		
		x.parent = y;
	}
	
	//��ĳ���ڵ��������
	private void rightRonate(Node x) {
		Node y = x.left;
		if(y.right != null) {
			y.right.parent = x;
		}
		
		y.parent = x.parent;
		x.left = y.right;
		y.right = x;
		
		if(x.parent != null) {
			if(x.parent.left == x) {
				x.parent.left = y;
			} else {
				x.parent.right = y;
			}
		} else {
			root = y;
		}
		
		x.parent = y;
	}
	
	// ȡ��С�Ľڵ�
	private Node minimum(Node node) {
		while (node.left != null) {
			node = node.left;
		}
		
		return node;
	}
	
	// ȡ��һ���ڵ�
	private Node successor(Node node) {
		if (node.right != null) {
			return minimum(node.right);
		}
		
		Node parent = node.parent;
		while (parent != null && node == parent.right) {
			node = parent;
			parent = parent.parent;
		}
		
		return parent;
	}
	
	/**
	 * ������ȷ�ȡ���нڵ�ļ�¼
	 * @param out �����������ż�¼
	 */
	public void depthTraverse(Sequence out) {
		Node node = root;
		if (node == null) {
			return;
		}
		
		// ȡֵ��С�Ľڵ㣬������Ľڵ�
		node = minimum(node);
		
		do {
			out.add(node.r);
			node = successor(node);
		} while (node != null);
	}
	
	private void recursiveTraverse(Node node, Sequence out) {
		if (node.left != null) {
			recursiveTraverse(node.left, out);
		}
		
		out.add(node.r);
		
		if (node.right != null) {
			recursiveTraverse(node.right, out);
		}
	}
	
	private void breadthTraverse(Node node, Sequence out) {
		Deque<Node> deque = new ArrayDeque<Node>();
		if (node != null) {
			deque.offer(node);
		}
		
		while (!deque.isEmpty()) {
			Node tmp = deque.poll();
			out.add(tmp.r);
			if (tmp.left != null) {
				deque.offer(tmp.left);
			}
			
			if (tmp.right != null) {
				deque.offer(tmp.right);
			}
		}
	}

	/**
	 * �õݹ鷽�����ȡ���нڵ�ļ�¼
	 * @param out �����������ż�¼
	 */
	public void recursiveTraverse(Sequence out) {
		if (root != null) {
			recursiveTraverse(root, out);
		}
	}
	
	/**
	 * ������ȷ�ȡ���нڵ�ļ�¼
	 * @param out �����������ż�¼
	 */
	public void breadthTraverse(Sequence out) {
		if (root != null) {
			breadthTraverse(root, out);
		}
	}
}