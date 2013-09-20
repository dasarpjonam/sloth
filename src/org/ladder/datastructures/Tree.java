/**
 * Tree.java
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

/**
 * Simple n-ary tree.
 * 
 * @author jbjohns
 * @param <T>
 *            The type of data stored in the nodes of this tree.
 */
public class Tree<T> {
	
	/**
	 * The root of this tree. May be null.
	 */
	private TreeNode<T> m_root;
	
	
	/**
	 * Create an empty tree with no root.
	 */
	public Tree() {
		this(null);
	}
	

	/**
	 * Create a tree with the given root.
	 * 
	 * @param root
	 *            The root of the tree, may be null.
	 */
	public Tree(TreeNode<T> root) {
		setRoot(root);
	}
	

	/**
	 * Get the root of this tree.
	 * 
	 * @return The root of the tree, may be null.
	 */
	public TreeNode<T> getRoot() {
		return m_root;
	}
	

	/**
	 * Set the root of the tree.
	 * 
	 * @param root
	 *            The root of the tree, may be null.
	 */
	public void setRoot(TreeNode<T> root) {
		m_root = root;
	}
}
