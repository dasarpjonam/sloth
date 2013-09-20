/**
 * TreeNode.java
 * 
 * Revision History:<br>
 * Nov 3, 2008 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package org.ladder.datastructures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Node for a simple n-ary tree (doubly linked).
 * 
 * @author jbjohns
 * @param <T>
 *            The type of data that we store in this node.
 */
public class TreeNode<T> {
	
	/**
	 * The parent of this node. Null for the root of the tree.
	 */
	private TreeNode<T> m_parentNode;
	
	/**
	 * The list of children nodes of this node. If there are no childrent, this
	 * list may either be null or empty.
	 */
	private List<TreeNode<T>> m_childNodes;
	
	/**
	 * The data that's stored at this node.
	 */
	private T m_nodeData;
	
	
	/**
	 * Construct an empty node with null parent, no children, and null data
	 */
	public TreeNode() {
		this(null, null);
	}
	

	/**
	 * Construct a node with the given parent, but no children and null data.
	 * 
	 * @param parent
	 *            The parent of this node
	 */
	public TreeNode(TreeNode<T> parent) {
		this(parent, null);
	}
	

	/**
	 * Construct a node with the given parent, but no children and null parent
	 * 
	 * @param nodeData
	 *            The data to store at this node.
	 */
	public TreeNode(T nodeData) {
		this(null, nodeData);
	}
	

	/**
	 * Construct a node with the given parent and given data, but no children.
	 * 
	 * @param parent
	 *            The parent of this node
	 * @param nodeData
	 *            The data to store at this node
	 */
	public TreeNode(TreeNode<T> parent, T nodeData) {
		m_childNodes = new ArrayList<TreeNode<T>>();
		setParentNode(parent);
		setNodeData(nodeData);
	}
	

	/**
	 * Get the node that is a parent of this node. May be null.
	 * 
	 * @return The node that is a parent of this node.
	 */
	public TreeNode<T> getParentNode() {
		return m_parentNode;
	}
	

	/**
	 * Set this node's parent node.
	 * 
	 * @param parent
	 *            The parent node
	 */
	public void setParentNode(TreeNode<T> parent) {
		m_parentNode = parent;
	}
	

	/**
	 * Get the data stored in this node
	 * 
	 * @return The data stored in this node. May be null.
	 */
	public T getNodeData() {
		return m_nodeData;
	}
	

	/**
	 * Set the data stored in this node. May be null.
	 * 
	 * @param nodeData
	 *            The data stored in this node.
	 */
	public void setNodeData(T nodeData) {
		m_nodeData = nodeData;
	}
	

	/**
	 * Get the list of child nodes for this node. May be null if there are no
	 * children.
	 * 
	 * @return Get the list of child nodes for this node
	 */
	public List<TreeNode<T>> getChildNodes() {
		return m_childNodes;
	}
	

	/**
	 * Set the list of child nodes in this node. May be null.
	 * 
	 * @param childNodes
	 *            The list of child nodes.
	 */
	public void setChildNodes(List<TreeNode<T>> childNodes) {
		m_childNodes = childNodes;
	}
	

	/**
	 * Is this data contained in this node or any child nodes? Uses .equals().
	 * Uses a depth-first approach.
	 * 
	 * @param data
	 *            The data to search for
	 * @return True if this node, or any child nodes, contain the data. If data ==
	 *         null, return false.
	 */
	public boolean containsData(T data) {
		if (data == null) {
			return false;
		}
		
		boolean contains = false;
		
		if (m_nodeData != null) {
			contains = m_nodeData.equals(data);
		}
		if (m_childNodes != null) {
			// iterate over child nodes, while we've not found the data. As soon
			// as we find the data this loop will terminate.
			for (Iterator<TreeNode<T>> childIter = m_childNodes.iterator(); childIter
			        .hasNext()
			                                                                && !contains;) {
				TreeNode<T> child = childIter.next();
				// recurse depth-first
				contains = child.containsData(data);
			}
		}
		
		return contains;
	}
}
