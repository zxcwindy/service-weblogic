package org.zxc.trie;

import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.zxc.trie.ConcurrentRadixTree1.SearchResult.Classification;

import com.googlecode.concurrenttrees.common.CharSequences;
import com.googlecode.concurrenttrees.common.LazyIterator;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.node.Node;
import com.googlecode.concurrenttrees.radix.node.NodeFactory;

public class ConcurrentRadixTree1<O> extends ConcurrentRadixTree {

	public ConcurrentRadixTree1(NodeFactory nodeFactory) {
		super(nodeFactory);
	}

	@Override
	public Iterable getKeysStartingWith(CharSequence prefix) {
		acquireReadLockIfNecessary();
		try {
			SearchResult searchResult = searchTree(prefix);
			Classification classification = searchResult.classification;
			switch (classification) {
			case EXACT_MATCH: {
				return getDescendantKeys(prefix, searchResult.nodeFound);
			}
			case KEY_ENDS_MID_EDGE: {
				// Append the remaining characters of the edge to the key.
				// For example if we searched for CO, but first matching node
				// was COFFEE,
				// the key associated with the first node should be COFFEE...
				CharSequence edgeSuffix = CharSequences.getSuffix(
						searchResult.nodeFound.getIncomingEdge(),
						searchResult.charsMatchedInNodeFound);
				prefix = CharSequences.concatenate(prefix, edgeSuffix);
				return getDescendantKeys(prefix, searchResult.nodeFound);
			}
			default: {
				// Incomplete match means key is not a prefix of any node...
				return Collections.emptySet();
			}
			}
		} finally {
			releaseReadLockIfNecessary();
		}
	}

	Iterable<CharSequence> getDescendantKeys(final CharSequence startKey,
			final Node startNode) {
		return new Iterable<CharSequence>() {
			@Override
			public Iterator<CharSequence> iterator() {
				return new LazyIterator<CharSequence>() {
					Iterator<NodeKeyPair> descendantNodes = lazyTraverseDescendants(
							startKey, startNode).iterator();
					@Override
					protected CharSequence computeNext() {
						int i = 0;
						// Traverse to the next matching node in the tree and
						// return its key and value...
						while (descendantNodes.hasNext()) {
							NodeKeyPair nodeKeyPair = descendantNodes.next();
							Object value = nodeKeyPair.node.getValue();
							if (value != null) {
								// Dealing with a node explicitly added to tree
								// (rather than an automatically-added split
								// node).

								// Call the transformKeyForResult method to
								// allow key to be transformed before returning
								// to client.
								// Used by subclasses such as ReversedRadixTree
								// implementations...
								CharSequence optionallyTransformedKey = transformKeyForResult(nodeKeyPair.key);

								// -> Convert the CharSequence to a String
								// before returning, to avoid set equality
								// issues,
								// because equals() and hashCode() is not
								// specified by the CharSequence API contract...
								return CharSequences
										.toString(optionallyTransformedKey);
							}
						}
						// Finished traversing the tree, no more matching nodes
						// to return...
						return endOfData();
					}
				};
			}
		};
	}

	SearchResult searchTree(CharSequence key) {
		Node parentNodesParent = null;
		Node parentNode = null;
		Node currentNode = root;
		int charsMatched = 0, charsMatchedInNodeFound = 0;

		final int keyLength = key.length();
		outer_loop: while (charsMatched < keyLength) {
			Node nextNode = currentNode.getOutgoingEdge(key
					.charAt(charsMatched));
			if (nextNode == null) {
				// Next node is a dead end...
				// noinspection UnnecessaryLabelOnBreakStatement
				break outer_loop;
			}

			parentNodesParent = parentNode;
			parentNode = currentNode;
			currentNode = nextNode;
			charsMatchedInNodeFound = 0;
			CharSequence currentNodeEdgeCharacters = currentNode
					.getIncomingEdge();
			for (int i = 0, numEdgeChars = currentNodeEdgeCharacters.length(); i < numEdgeChars
					&& charsMatched < keyLength; i++) {
				if (currentNodeEdgeCharacters.charAt(i) != key
						.charAt(charsMatched)) {
					// Found a difference in chars between character in key and
					// a character in current node.
					// Current node is the deepest match (inexact match)....
					break outer_loop;
				}
				charsMatched++;
				charsMatchedInNodeFound++;
				// if(charsMatchedInNodeFound > 1){
				// break outer_loop;
				// }			
			}
		}
		return new SearchResult(key, currentNode, charsMatched,
				charsMatchedInNodeFound, parentNode, parentNodesParent);
	}
	
	protected Iterable<NodeKeyPair> lazyTraverseDescendants(final CharSequence startKey, final Node startNode) {
        return new Iterable<NodeKeyPair>() {
            @Override
            public Iterator<NodeKeyPair> iterator() {
            	
                return new LazyIterator<NodeKeyPair>() {
                	
                
                    Deque<NodeKeyPair> stack = new LinkedList<NodeKeyPair>();
                    {
                    	
                        stack.push(new NodeKeyPair(startNode, startKey));
                    }

                    @Override
                    protected NodeKeyPair computeNext() {
                        if (stack.isEmpty()) {
                            return endOfData();
                        }
                        NodeKeyPair current = stack.pop();
                        List<Node> childNodes = current.node.getOutgoingEdges();
                        System.out.println("调用");
                        // -> Iterate child nodes in reverse order and so push them onto the stack in reverse order,
                        // to counteract that pushing them onto the stack alone would otherwise reverse their processing order.
                        // This ensures that we actually process nodes in ascending alphabetical order.
                        for (int i = childNodes.size(); i > 0; i--) {
                            Node child = childNodes.get(i - 1);
                            stack.push(new NodeKeyPair(child, CharSequences.concatenate(current.key, child.getIncomingEdge())));
                        }
                        return current;
                    }
                };
            }
        };
	}

	static class SearchResult {
		final CharSequence key;
		final Node nodeFound;
		final int charsMatched;
		final int charsMatchedInNodeFound;
		final Node parentNode;
		final Node parentNodesParent;
		final Classification classification;

		enum Classification {
			EXACT_MATCH, INCOMPLETE_MATCH_TO_END_OF_EDGE, INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE, KEY_ENDS_MID_EDGE, INVALID // INVALID
																															// is
																															// never
																															// used,
																															// except
																															// in
																															// unit
																															// testing
		}

		SearchResult(CharSequence key, Node nodeFound, int charsMatched,
				int charsMatchedInNodeFound, Node parentNode,
				Node parentNodesParent) {
			this.key = key;
			this.nodeFound = nodeFound;
			this.charsMatched = charsMatched;
			this.charsMatchedInNodeFound = charsMatchedInNodeFound;
			this.parentNode = parentNode;
			this.parentNodesParent = parentNodesParent;

			// Classify this search result...
			this.classification = classify(key, nodeFound, charsMatched,
					charsMatchedInNodeFound);
		}

		protected Classification classify(CharSequence key, Node nodeFound,
				int charsMatched, int charsMatchedInNodeFound) {
			if (charsMatched == key.length()) {
				if (charsMatchedInNodeFound == nodeFound.getIncomingEdge()
						.length()) {
					return Classification.EXACT_MATCH;
				} else if (charsMatchedInNodeFound < nodeFound
						.getIncomingEdge().length()) {
					return Classification.KEY_ENDS_MID_EDGE;
				}
			} else if (charsMatched < key.length()) {
				if (charsMatchedInNodeFound == nodeFound.getIncomingEdge()
						.length()) {
					return Classification.INCOMPLETE_MATCH_TO_END_OF_EDGE;
				} else if (charsMatchedInNodeFound < nodeFound
						.getIncomingEdge().length()) {
					return Classification.INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE;
				}
			}
			throw new IllegalStateException(
					"Unexpected failure to classify SearchResult: " + this);
		}

		@Override
		public String toString() {
			return "SearchResult{" + "key=" + key + ", nodeFound=" + nodeFound
					+ ", charsMatched=" + charsMatched
					+ ", charsMatchedInNodeFound=" + charsMatchedInNodeFound
					+ ", parentNode=" + parentNode + ", parentNodesParent="
					+ parentNodesParent + ", classification=" + classification
					+ '}';
		}
	}
	
	protected static class NodeKeyPair {
        public final Node node;
        public final CharSequence key;

        public NodeKeyPair(Node node, CharSequence key) {
            this.node = node;
            this.key = key;
        }
    }
	
}
