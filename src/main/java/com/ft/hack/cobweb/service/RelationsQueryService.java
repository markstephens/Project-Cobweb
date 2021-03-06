package com.ft.hack.cobweb.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.Traverser;

import com.ft.hack.cobweb.dao.CobwebDAO;
import com.ft.hack.cobweb.domain.Datanode;
import com.ft.hack.cobweb.domain.SearchResult;

/**
 * @author anurag.kapur
 *
 */
public class RelationsQueryService {

	private static final Logger LOGGER = Logger.getLogger(RelationsQueryService.class);
	private static String NAME_KEY = "name";
	private static String TYPE_KEY = "type";
	
	public List<List> getRelations(String startingNodeName) {
		List<List> results = new ArrayList<List>();
		List<Datanode> dataNodes = new ArrayList<Datanode>();
		List<SearchResult> searchResults = new ArrayList<SearchResult>();
		FTContentSearchService searchService = new FTContentSearchService();
		
		CobwebDAO dao = new CobwebDAO();
		Traverser traverser = dao.getConnections(startingNodeName);
		Iterable<Node> nodesIterable = traverser.nodes();
		
		try {
			
			Iterator<Node> nodesIterator = nodesIterable.iterator();	
			
			//Process starting node
			Node startingNode = dao.getNode(startingNodeName);
			Datanode startingDataNode = new Datanode();
			startingDataNode.setName((String)startingNode.getProperty(NAME_KEY));
			startingDataNode.setType((String)startingNode.getProperty(TYPE_KEY));
			
			Iterator<Relationship> startingNodeRelationships = startingNode.getRelationships().iterator();
			while (startingNodeRelationships.hasNext()) {
				Datanode otherDataNode = new Datanode();
				
				Relationship relationship = (Relationship) startingNodeRelationships.next();
				Node otherNode = relationship.getOtherNode(startingNode);
				String otherNodeName = (String)otherNode.getProperty(NAME_KEY);
				otherDataNode.setName(otherNodeName);
				otherDataNode.setType((String)otherNode.getProperty(TYPE_KEY));
				startingDataNode.addAssociation(otherDataNode);
				
				List<SearchResult> intermediateSearchResults = searchService.search(startingNodeName, otherNodeName);
				for (Iterator iterator = intermediateSearchResults.iterator(); iterator.hasNext();) {
					SearchResult searchResult = (SearchResult) iterator.next();
					searchResults.add(searchResult);
				}
			}
			
			dataNodes.add(startingDataNode);
			
			// Process remaining relationships graph
			while (nodesIterator.hasNext()) {
				Datanode dataNode = new Datanode();
				Node node = (Node) nodesIterator.next();
				dataNode.setName((String)node.getProperty(NAME_KEY));
				dataNode.setType((String)node.getProperty(TYPE_KEY));
				
				Iterator<Relationship> nodeRelationships = node.getRelationships().iterator();
				while (nodeRelationships.hasNext()) {
					Datanode otherDataNode = new Datanode();
					
					Relationship relationship = (Relationship) nodeRelationships.next();
					Node otherNode = relationship.getOtherNode(node);
					otherDataNode.setName((String)otherNode.getProperty(NAME_KEY));
					otherDataNode.setType((String)otherNode.getProperty(TYPE_KEY));
					dataNode.addAssociation(otherDataNode);
				}
				dataNodes.add(dataNode);
			}
			
			//Create wrapper list
			results.add(dataNodes);
			results.add(searchResults);
		}catch(Exception e) {
			LOGGER.error(e);
		}
		
		return results;
	}
	
	public static void main(String args[]) {
		RelationsQueryService queryService = new RelationsQueryService();
		queryService.getRelations("Larry Page");
	}
}