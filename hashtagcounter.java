/*******************
Author: Saad Afzal
University of Florida
*******************/

import java.io.*;
import java.util.HashMap;
import java.util.Map.Entry;


public class hashtagcounter {
	//globally declaring the pairwise combine hashmap
	HashMap<Integer, node> pairWise = new HashMap<Integer, node>();
	static HashMap<String, node> hashStruct = null;
	public static class node{
		
		//declaring all parameters 
		public int degree=0;			//initially node has no children
		public String key ;				//Hashtag string 
		public boolean childCut=false;	//initially Child cut value is false
		public int countValue;			//count of the Hashtag 
		public node parent=null;		//initially node is at the root level
		public node left;				//left pointer of the node
		public node right;				//right pointer of the node
		public node child=null;			//node has no children upon creation
		
		//setter method
		public void setCountValue(int val){
			countValue = val;
		}
		
		//getter method
		public int getCountValue() {
			return countValue;
		}
		
		//Constructor of the class
		public node(int newVal, String key){
			 left=this;
			 right = this;
			 countValue= newVal;
			 this.key=key;
		 }
	}
	
	//Max pointer pointing to the maximum element in the heap
	//Setting this value to be null initially
	public node maxNode = null;
	
	public void insertNode(node newNode)
	{
		if(maxNode == null)
		{
			 maxNode = newNode;
			 maxNode.parent=null;
			 maxNode.childCut = false;
			 maxNode.left = maxNode;
			 maxNode.right = maxNode;
		}
		
		else
		{
			//inserting the new node inside circular LL on right of maxNode
			node maxRight = maxNode.right;
			maxNode.right = newNode;
			newNode.left = maxNode;
			newNode.right = maxRight;
			maxRight.left = newNode;
			
			//setting parameters of new node
			newNode.parent=null;
			newNode.childCut = false;
			
			if(newNode.degree==0)
				newNode.child=null;
				
			if(newNode.countValue >= maxNode.countValue)
				maxNode=newNode;
			
			node tempMax = maxNode.left;
			node currentNode = maxNode;
			do
			{
				if(currentNode.right.countValue>maxNode.countValue)
				{
					maxNode = currentNode.right;
				}
				currentNode= currentNode.right;
			}while(currentNode != tempMax);
			
		}
		
		return;
	}
	
	
		
	//--------------------------------------------------------------------
	public void increaseKey(node x, int k)
	{
		//increasing the count value of hash tag
		x.countValue = x.countValue + k;
		node y = x.parent;
		
		if(y==null)
		{
			if(x.countValue>maxNode.countValue)
			{
				maxNode = x;
			}
		}
		
		//removing x from its parent and reinserting at root level if x > y
		if(y!=null && x.countValue>y.countValue)
		{
			cut(x,y);
			cascadeCut(y);
		}
		
		return;
	}
	
	
	
	//--------------------------------------------------------------------
	public void cut(node c, node d)
	{
		//removing x from the child list of its parent 'y'
		c.parent=null;
		
		//setting child pointer of parent to right of cut node if cut node is left most child
		//also right child of cut node should exist
		if(d.child==c && c.right !=c)
		{
			d.child=c.right;
			
			node cRight = c.right;
			node cLeft = c.left;
			cLeft.right = cRight;
			cRight.left = cLeft;	
		}
		
		//simply removing the child if not parent's leftmost child
		else if(d.child!=c && c.right !=c)
		{	
			node cRight = c.right;
			node cLeft = c.left;
			cLeft.right = cRight;
			cRight.left = cLeft;	
		}
		
		else
		{
			d.child =null;
		}
		
		c.right = c;
		c.left = c;
		
		//also decrementing degree of parent 'y' 
		d.degree--;
		
		//setting childCut value of x to be false
		c.childCut=false;
		
		//inserting x at the root level
		insertNode(c);

		return;
	}
	
	
	
	//--------------------------------------------------------------------
	public void cascadeCut(node p)
	{
		node z = p.parent;
		if(z!=null)
		{
			if(p.childCut==false)
			{
				//setting child cut value of x's parent to be true
				p.childCut=true;
			}
			
			else
			{
				//removing y from its parent 'z' since childCut of y = true
				cut(p,z);
				cascadeCut(z);
				//return;
			}	
		}
		return;
	}
	
	
	
	//--------------------------------------------------------------------
	public node removeMax()
	{
		node temp = maxNode;
		
		
		//case when there is no other node at the root level
		if(maxNode.right == maxNode)
		{
			maxNode = null;
			insertChildren(temp);
		}
		
		//when there are other nodes present at the root level
		else 
		{
			node leftsib = maxNode.left;
			node rightsib= maxNode.right;
			
			leftsib.right = rightsib;
			rightsib.left = leftsib;
			
			//temporarily making left of maxNode as new max
			node tempMax = maxNode.left;
			
			//setting maxNode to NULL since its been removed
			maxNode = null;
			maxNode = tempMax;
			
			//inserting children of maxNode in the root level
			insertChildren(temp);
			
			//finding the new max
			node currentNode = maxNode;
			do
			{
				if(currentNode.right.countValue>maxNode.countValue)
				{
					maxNode = currentNode.right;
				}
				currentNode= currentNode.right;
			}while(currentNode != tempMax);
		}

		//resetting the pairwise combine hash map
		pairWise = new HashMap<Integer, node>();
		
		//checking degrees of nodes in root level
		transition(maxNode);
		
		temp.right=temp;
		temp.left=temp;
		temp.child=null;
		temp.parent=null;
		temp.degree=0;
		return temp;
	}
	
	
	
	//--------------------------------------------------------------------
	public void transition(node one)
	{
		node currentNode = one;
	
	
		do
		{
			int pairWiseKey = currentNode.degree;
			if(pairWise.containsKey(pairWiseKey))
			{
				//removing from degree table only if node is not itself
				if(currentNode != pairWise.get(pairWiseKey))
				{
					node secondNode = pairWise.remove(pairWiseKey);
					node combinedNode = pairwiseCombine(currentNode, secondNode);
					
					//setting currentNode to bigger value out of recently combined nodes
					currentNode = combinedNode;
					
					//recursive call to itself
					transition(currentNode);
					return;		
				}
			}
			
			else
			{
				//putting node into degree table since it doesn't previously exist 
				pairWise.put(pairWiseKey, currentNode);
			}
			currentNode = currentNode.right;
		}while(currentNode !=maxNode);
		
		return;
	}
	
	
	
	
	//--------------------------------------------------------------------
	//----pairWiseCombine method
	public node pairwiseCombine(node first, node second)
	{	
		//checking count value
		if(first.countValue>second.countValue)
		{
			//unlinking second node from the root level
			node tempRightNode = second.right;
			node tempLeftNode  = second.left;
			tempLeftNode.right = tempRightNode;
			tempRightNode.left = tempLeftNode;
			
			second.right = second;
			second.left  = second;
			
			//inserting second node at the child level of the first node
			node combined = merge(first, second);
			return combined;
		}
		
		//making sure that maxNode always stays at root level when both count values are same for the nodes
		else if (second == maxNode || first == maxNode)
		{
			if(first == maxNode)
			{
				node tempRightNode = second.right;
				node tempLeftNode  = second.left;
				tempLeftNode.right = tempRightNode;
				tempRightNode.left = tempLeftNode;
				
				second.right = second;
				second.left  = second;
				
				//inserting second node at the child level of the first node
				node combined = merge(first, second);
				return combined;
			}
			
			else
			{
				node tempRightNode = first.right;
				node tempLeftNode  = first.left;
				tempLeftNode.right = tempRightNode;
				tempRightNode.left = tempLeftNode;
				
				first.right = first;
				first.left  = first;
				
				//inserting first node at the child level of the second node
				node combined = merge(second, first);
				return combined;
			}
		}
		
		else 
		{
			//unlinking first node from the root level
			node tempRightNode = first.right;
			node tempLeftNode  = first.left;
			tempLeftNode.right = tempRightNode;
			tempRightNode.left = tempLeftNode;
			
			first.right = first;
			first.left  = first;
			
			//inserting first node at the child level of the second node
			node combined = merge(second, first);
			return combined;
		}
	}
	
	
	
	
	
	//--------------------------------------------------------------------
	public node merge(node parent, node child)
	{
		if(parent.degree==0)
		{
			//simply inserting second node as single child 
			parent.child = child;
			child.parent = parent;
			child.left = child;
			child.right = child;
		}
		
		else
		{
			//inserting this new node as right child of the leftmost child of parent
			child.left = child;
			child.right = child;
			child.parent = parent;
			
			
			node rightsib = parent.child.right;
			rightsib.left = child;
			parent.child.right = child;
			child.right = rightsib;
			child.left=parent.child;
		}
		
		parent.degree++;
		
		//returning the newly combined node with new degree = old degree+1
		return parent;
	}
	
	
	//--------------------------------------------------------------------
	public void insertChildren(node removedNode)
	{
		int deg = removedNode.degree;
		node savedChild = removedNode.child;
		node rightsib;
		switch (deg)
		{
			case 0:
				//do nothing
				break;
				
			case 1:
				//insert only child into root
				insertNode(savedChild);
				break;
			
			default:
				//insert all children into root level
				for(int i=0;i<deg;i++)
				{
					node currentChild = removedNode.child;
					rightsib = currentChild.right;
					removedNode.child = rightsib;
					currentChild.right = currentChild;
					currentChild.left  = currentChild;
					insertNode(currentChild);
					
				}
		}
		
		removedNode.child = null;
		return;
	}
	

	
	//--------------------------------------------------------------------
	public static void main(String[] args) 
	{
		String nameOfFile = "input_10000.txt";
		String line = null;
		
		hashStruct = new HashMap<String, node>();
		
		hashtagcounter fibObj = new hashtagcounter();
		BufferedReader buffRdr=null;
		BufferedWriter wr=null;

        try
        	{
            	FileReader flRdr = new FileReader(nameOfFile);
            	buffRdr = new BufferedReader(flRdr);
            	wr = new BufferedWriter(new FileWriter(new File("output.txt")));
            
            	while((line = buffRdr.readLine()) != null) 
            	{
            		if(Character.valueOf(line.charAt(0))=='#')
            		{
            			String[] keyValue = line.split("\\s");
            			String key = keyValue[0].replace("#","");
            			String value = keyValue[1];
            			int convertedVal = Integer.parseInt(value);
                	
            			if(hashStruct.containsKey(key))
            			{
            				node retrivedNode= hashStruct.get(key);
            				fibObj.increaseKey(retrivedNode, convertedVal);
            			}
                	
            			else
            			{
            				node newNodeInsert = new node(convertedVal,key);
            				hashStruct.put(key, newNodeInsert);
            				fibObj.insertNode(newNodeInsert);
            			}
            		}
            		
            		
            		else if(line.equals("STOP") || line.equals("stop"))
        			{

            			buffRdr.close();     
                    	wr.close();
            			System.exit(0);
        			}
            		
            		else
            		{
            			String[] iterations = line.split("\\n");
            			int intIterations = Integer.parseInt(iterations[0]);
            			
            			//hash map for storing removed hash tags
            			HashMap<String, Integer> hashRemoved = new HashMap<String, Integer>();
            			
            			String testemp="";
            			for(int i=0;i<intIterations;i++)
            			{
            				node maxN = fibObj.removeMax();
            				//int removedVal= maxN.countValue;
            				
            				/*for (Entry<String, node> e : hashStruct.entrySet()) 
            				{
            				    String maxKey = e.getKey();
            				    Object maxValue = e.getValue();
            				    if(maxN == maxValue)
            				    {*/
            						if(i!=intIterations-1)
            						{
            							testemp+=maxN.key+", ";
            						}
            						else
            							testemp+=maxN.key;
            				    	//inserting key-value pairs of removed max nodes into hash map hashRemoved
            				    	hashRemoved.put(maxN.key, maxN.countValue);
            				    	//break;
            				    //}
            				//}	
            			}
            			wr.write(testemp+"\n");
            			
            			//Re-inserting removed nodes back into the heap
            			for (Entry<String, Integer> i : hashRemoved.entrySet()) 
        				{
        				    String maxKey = i.getKey();
        				    Integer maxValue = i.getValue();
        				    node reinsertNode = new node(maxValue,maxKey);
        				    fibObj.insertNode(reinsertNode);
        				    hashStruct.put(maxKey, reinsertNode);
        				}
            		}
            	}	
        	}
        
        //file not found exception
        catch(FileNotFoundException ex) 
        {
        	System.out.println("Error! not able to open file '" + nameOfFile + "'");                
        }
        
        //IO exception
        catch(IOException ex) 
        {
        	System.out.println("Error! Not able to read file '" + nameOfFile + "'");                  
        }
        
        //closing file handle
        finally
    	{
        	try
        	{
        	buffRdr.close();     
        	wr.close();
        	}
        	catch(IOException ex) 
            {
            	System.out.println("Error! Not able to close file '" + nameOfFile + "'");                  
            }
    	}
    }
}
