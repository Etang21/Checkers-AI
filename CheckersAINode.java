import java.util.*;

public class CheckersAINode 
{
	private CheckersOp board; //The current board position
	public ArrayList<CheckersAINode> children; //The next possible boards
	boolean isLeaf; //Stores if this node is a leaf (changes the value function)
	private double value;
	private String moves;
	public String bestNextMove;
	public int alpha; //Alpha is the maximum lower bound of possible solutions
	//That is, alpha is the minimum (worst) value that the maximizer (the computer AI) is assured at that node
	public int beta; //Beta is the minimum upper bound of possible solutions
	//That is, beta is the maximum value that the minimizer (the human) is assured if it follows the node
	private int currHeight;
	
	//So if we're at a minimizer (player) node, every following node will update 
	
	public CheckersAINode(CheckersOp inputBoard, String moveSequence, int inputCurrHeight)
	{
		board = new CheckersOp(inputBoard);
		children = new ArrayList<CheckersAINode>();
		moves = moveSequence;
		bestNextMove = null;
		alpha = Integer.MIN_VALUE;
		beta = Integer.MAX_VALUE;
		currHeight = inputCurrHeight;
		//Here is where you change the depth!!
		if(board.rCount<=1||board.bCount<=1) //This is where you change the depth! And it automatically searches one deeper near the end-game
		{
			if(currHeight>=7)
				isLeaf = true;
		}
		else if(board.rCount<=3||board.bCount<=3)
		{
			if(currHeight>=6)
				isLeaf = true;
		}
		else if(currHeight>=5)
			isLeaf = true;
		else
			isLeaf = false;
	}
	
	public CheckersOp getBoard()
	{
		return board;
	}
	
	public ArrayList<CheckersAINode> getChildren()
	{
		return children;
	}
	
	public String getMoves()
	{
		return moves;
	}
	
	public void buildTree(int currHeight)
	{
		//Now we build the subtree; this is a recursive function
		//For each of these, we need to run through every possible move and jump chain
		//Then we add all those possible following boards to children
		
		//Now with Alpha-Beta pruning!!!
		//For alpha-beta pruning, this evaluates alpha and beta values at every step of the way
		//And if beta < alpha, then we can stop.

		if(isLeaf)
		{
			alpha = beta = board.evaluateBoard(); //If it's the leaf, stop building and evaluate
			return;
		}
		
		int currTurn = board.turn;
		
		//This part below is where we'd optimize searching order later
		if(currTurn==1) //If it's the red pieces
		{
			for(int row=7; row>=0; row--)
			{
				for(int col=0; col<8; col++)
				{
					boolean alphaBetaConsistent = addValidSquareJumpMovesForRed(row,col);
					if(!alphaBetaConsistent)
						return;
				}
			}
			for(int row=7; row>=0; row--)
			{
				for(int col=0; col<8; col++)
				{
					boolean alphaBetaConsistent = addValidSquareMovesForRed(row,col);
					if(!alphaBetaConsistent)
						return;
				}
			}
		}
		else if(currTurn==0)
		{
			for(int row=0; row<8; row++)
			{
				for(int col=0; col<8; col++)
				{
					boolean betaGreaterThanAlpha = addValidSquareMovesForBlack(row,col);
					if(betaGreaterThanAlpha)
						return;
					
				}
			}
		}
		/*
		for(CheckersAINode kid:children)
		{
			kid.buildTree(currHeight+1);
		}
		*/
	}
	
	public boolean addValidSquareJumpMovesForRed(int row, int col)
	{
		int index = 10*row+col;
		
		//These are helper methods: find the possible moves for any given square
		if(board.getBoard()[row][col]==1)//If we found a normal red piece
		{
			//Later, we may optimize the searching order by putting the jumps first
			if(board.checkValidMove(10*row+col,10*(row+2)+col-2)==2) //Jumping down left (this is only possible when moving down-left is impossible)
			{
				//Do we need to make a TREE of all possible jumps?
				//Then we find all leaf nodes, and those are all the possible jump chains!
				//Working out jump chains (using the JumpTree):
				boolean alphaBetaConsistent = addJumperChildren(index,2,-2);
				if(!alphaBetaConsistent)
					return false;
			}
			
			if(board.checkValidMove(10*row+col,10*(row+2)+col+2)==2) //Jumping down-right
			{
				boolean alphaBetaConsistent = addJumperChildren(index,2,2);
				if(!alphaBetaConsistent)
					return false;			
			}
		}
		else if(board.getBoard()[row][col]==3) //This means we found a red king!
		{
			if(board.checkValidMove(10*row+col,10*(row+2)+col-2)==2) //Jumping down left
			{
				//Now we have to work out jump chains: just use a while loop to keep checking for chains (and/or DFS))
				//Once we've reached the end of a chain, we stop and add it to children
				boolean alphaBetaConsistent = addJumperChildren(index,2,-2);
				if(!alphaBetaConsistent)
					return false;
			}
			
			if(board.checkValidMove(10*row+col,10*(row+2)+col+2)==2) //Jumping down-right
			{
				boolean alphaBetaConsistent = addJumperChildren(index,2,2);
				if(!alphaBetaConsistent)
					return false;
			}
			
			if(board.checkValidMove(10*row+col,10*(row-2)+col-2)==2) //Jumping up-left
			{
				boolean alphaBetaConsistent = addJumperChildren(index,-2,-2);
				if(!alphaBetaConsistent)
					return false;			
			}
			
			if(board.checkValidMove(10*row+col,10*(row-2)+col+2)==2) //Jumping up-right
			{
				boolean alphaBetaConsistent = addJumperChildren(index,-2,2);
				if(!alphaBetaConsistent)
					return false;			
			}
		}
		return true; //This is if alpha-beta are still consistent after all of it
	}
	
	public boolean addValidSquareMovesForRed(int row, int col) //returns false if alphaGreaterThanBeta (can stop exploring children)
	{
		int index = 10*row+col;
		
		//These are helper methods: find the possible moves for any given square
		if(board.getBoard()[row][col]==1)//If we found a normal red piece
		{
			//Later, we may optimize the searching order by putting the jumps first
			if(board.checkValidMove(10*row+col,10*(row+1)+col-1)==1) //Moving down-left
			{
				CheckersOp tempBoard = new CheckersOp(board); //Does this work? Will my changes in tempBoard affect board?
				tempBoard.makeMove(10*row+col,10*(row+1)+col-1);
				String lastMove=(10*row+col) + "," + (10*(row+1)+col-1);
				boolean alphaBetaConsistent = updateAlphaBeta(new CheckersAINode(tempBoard, lastMove, currHeight+1));
				if(!alphaBetaConsistent)
					return false;
			}
			if(board.checkValidMove(10*row+col,10*(row+1)+col+1)==1) //moving down-right
			{
				CheckersOp tempBoard = new CheckersOp(board); //QUESTION: will my changes to tempBoard affect board? -- Now they won't!
				tempBoard.makeMove(10*row+col,10*(row+1)+col+1);
				String lastMove=(10*row+col) + "," + (10*(row+1)+col+1);
				boolean alphaBetaConsistent = updateAlphaBeta(new CheckersAINode(tempBoard, lastMove, currHeight+1));
				if(!alphaBetaConsistent)
					return false;			
			}
		}
		else if(board.getBoard()[row][col]==3) //This means we found a red king!
		{
			if(board.checkValidMove(10*row+col,10*(row+1)+col-1)==1) //Moving down-left
			{
				CheckersOp tempBoard = new CheckersOp(board); //Does this work? Will my changes in tempBoard affect board?
				tempBoard.makeMove(10*row+col,10*(row+1)+col-1);
				String lastMove=(10*row+col) + "," + (10*(row+1)+col-1);
				boolean alphaBetaConsistent = updateAlphaBeta(new CheckersAINode(tempBoard, lastMove, currHeight+1));
				if(!alphaBetaConsistent)
					return false;			
			}
			if(board.checkValidMove(10*row+col,10*(row+1)+col+1)==1) //moving down-right
			{
				CheckersOp tempBoard = new CheckersOp(board); //QUESTION: will my changes to tempBoard affect board?
				tempBoard.makeMove(10*row+col,10*(row+1)+col+1);
				String lastMove=(10*row+col) + "," + (10*(row+1)+col+1);
				boolean alphaBetaConsistent = updateAlphaBeta(new CheckersAINode(tempBoard, lastMove, currHeight+1));
				if(!alphaBetaConsistent)
					return false;			
			}
			
			if(board.checkValidMove(10*row+col,10*(row-1)+col-1)==1) //Moving up-left
			{
				CheckersOp tempBoard = new CheckersOp(board); //Does this work? Will my changes in tempBoard affect board?
				tempBoard.makeMove(10*row+col,10*(row-1)+col-1);
				String lastMove=(10*row+col) + "," + (10*(row-1)+col-1);
				boolean alphaBetaConsistent = updateAlphaBeta(new CheckersAINode(tempBoard, lastMove, currHeight+1));
				if(!alphaBetaConsistent)
					return false;			
			}
			if(board.checkValidMove(10*row+col,10*(row-1)+col+1)==1) //moving up-right
			{
				CheckersOp tempBoard = new CheckersOp(board); //QUESTION: will my changes to tempBoard affect board?
				tempBoard.makeMove(10*row+col,10*(row-1)+col+1);
				String lastMove=(10*row+col) + "," + (10*(row-1)+col+1);
				boolean alphaBetaConsistent = updateAlphaBeta(new CheckersAINode(tempBoard, lastMove, currHeight+1));
				if(!alphaBetaConsistent)
					return false;			
			}
		}
		return true; //This is if alpha-beta are still consistent after all of it
	}
	
	public boolean addValidSquareMovesForBlack(int row, int col)
	{
		int index = 10*row + col;
		if(board.getBoard()[row][col]==2)//If we found a normal black piece
		{
			//Could optimize this by shuffling the if and else checks
			if(board.checkValidMove(10*row+col,10*(row-1)+col-1)==1) //Moving up-left
			{
				CheckersOp tempBoard = new CheckersOp(board); //Does this work? Will my changes in tempBoard affect board?
				tempBoard.makeMove(10*row+col,10*(row-1)+col-1);
				String lastMove=(10*row+col) + "," + (10*(row-1)+col-1);
				boolean alphaBetaConsistent = updateAlphaBeta(new CheckersAINode(tempBoard, lastMove, currHeight+1));
				if(!alphaBetaConsistent)
					return false;			
			}
			else if(board.checkValidMove(10*row+col,10*(row-2)+col-2)==2) //Jumping  up-left
			{
				//Now we have to work out jump chains: check the piece's possible jumps
				boolean alphaBetaConsistent = addJumperChildren(index,-2,-2);
				if(!alphaBetaConsistent)
					return false;			}
			
			if(board.checkValidMove(10*row+col,10*(row-1)+col+1)==1) //moving up-right
			{
				CheckersOp tempBoard = new CheckersOp(board); //QUESTION: will my changes to tempBoard affect board?
				tempBoard.makeMove(10*row+col,10*(row-1)+col+1);
				String lastMove=(10*row+col) + "," + (10*(row-1)+col-1);
				boolean alphaBetaConsistent = updateAlphaBeta(new CheckersAINode(tempBoard, lastMove, currHeight+1));
				if(!alphaBetaConsistent)
					return false;			
			}
			else if(board.checkValidMove(10*row+col,10*(row-2)+col+2)==2) //Jumping up-right
			{
				boolean alphaBetaConsistent = addJumperChildren(index,-2,2);
				if(!alphaBetaConsistent)
					return false;			}
		}
		
		else if(board.getBoard()[row][col]==4) //This means we found a red king!
		{
			if(board.checkValidMove(10*row+col,10*(row+1)+col-1)==1) //Moving down-left
			{
				CheckersOp tempBoard = new CheckersOp(board); //Does this work? Will my changes in tempBoard affect board?
				tempBoard.makeMove(10*row+col,10*(row+1)+col-1);
				String lastMove=(10*row+col) + "," + (10*(row+1)+col-1);
				boolean alphaBetaConsistent = updateAlphaBeta(new CheckersAINode(tempBoard, lastMove, currHeight+1));
				if(!alphaBetaConsistent)
					return false;			
			}
			else if(board.checkValidMove(10*row+col,10*(row+2)+col-2)==2) //Jumping down left
			{
				//Now we have to work out jump chains: just use a while loop to keep checking for chains (and/or DFS))
				//Once we've reached the end of a chain, we stop and add it to children
				boolean alphaBetaConsistent = addJumperChildren(index,2,-2);
				if(!alphaBetaConsistent)
					return false;
			}
			
			if(board.checkValidMove(10*row+col,10*(row+1)+col+1)==1) //moving down-right
			{
				CheckersOp tempBoard = new CheckersOp(board); //QUESTION: will my changes to tempBoard affect board?
				tempBoard.makeMove(10*row+col,10*(row+1)+col+1);
				String lastMove=(10*row+col) + "," + (10*(row+1)+col+1);
				boolean alphaBetaConsistent = updateAlphaBeta(new CheckersAINode(tempBoard, lastMove, currHeight+1));
				if(!alphaBetaConsistent)
					return false;			
			}
			else if(board.checkValidMove(10*row+col,10*(row+2)+col+2)==2) //Jumping down-right
			{
				boolean alphaBetaConsistent = addJumperChildren(index,2,2);
				if(!alphaBetaConsistent)
					return false;			}
			
			if(board.checkValidMove(10*row+col,10*(row-1)+col-1)==1) //Moving up-left
			{
				CheckersOp tempBoard = new CheckersOp(board); //Does this work? Will my changes in tempBoard affect board?
				tempBoard.makeMove(10*row+col,10*(row-1)+col-1);
				String lastMove=(10*row+col) + "," + (10*(row-1)+col-1);
				boolean alphaBetaConsistent = updateAlphaBeta(new CheckersAINode(tempBoard, lastMove, currHeight+1));
				if(!alphaBetaConsistent)
					return false;			
			}
			else if(board.checkValidMove(10*row+col,10*(row-2)+col-2)==2) //Jumping up-left
			{
				boolean alphaBetaConsistent = addJumperChildren(index,-2,-2);
				if(!alphaBetaConsistent)
					return false;			}
			
			if(board.checkValidMove(10*row+col,10*(row-1)+col+1)==1) //moving up-right
			{
				CheckersOp tempBoard = new CheckersOp(board); //QUESTION: will my changes to tempBoard affect board?
				tempBoard.makeMove(10*row+col,10*(row-1)+col+1);
				String lastMove=(10*row+col) + "," + (10*(row-1)+col+1);
				boolean alphaBetaConsistent = updateAlphaBeta(new CheckersAINode(tempBoard, lastMove, currHeight+1));
				if(!alphaBetaConsistent)
					return false;			
			}
			else if(board.checkValidMove(10*row+col,10*(row-2)+col+2)==2) //Jumping up-right
			{
				//And once again, we have to work out jump chains
				boolean alphaBetaConsistent = addJumperChildren(index,-2,2);
				if(!alphaBetaConsistent)
					return false;
			}
		}
		return false;
	}
	
	private boolean addJumperChildren(int startIndex, int changeInY, int changeInX)
	{
		//Note: only create a jump chain when one jump has already been executed (the changeInY and changeInX)
		//This avoids creating way too  many unnecessary jump trees
		//So the parameters are the start, the first jump's changeInY, the first jump's changeInX, and nextIsLeaf
		//This creates a new board with that first jump already made.
		CheckersOp inputBoard = new CheckersOp(board);
		inputBoard.makeMove(startIndex, startIndex + 10*changeInY + changeInX);
		JumpTree treeOfHops = new JumpTree(inputBoard,startIndex+10*changeInY + changeInX,startIndex + "," + (startIndex+10*changeInY + changeInX)); //Could definitely have a jumper problem
		ArrayList<String> hops = treeOfHops.getAllJumpChains();
		for(String oneChainz: hops)
		{
			CheckersOp tempBoard = new CheckersOp(board);
			String[] theChain = oneChainz.split(",");
			for(int i=0; i<theChain.length-1;i++)
			{
				tempBoard.makeMove(Integer.parseInt(theChain[i]), Integer.parseInt(theChain[i+1]));
			}
			boolean alphaBetaConsistent = updateAlphaBeta(new CheckersAINode(tempBoard, oneChainz, currHeight+1));
			if(alphaBetaConsistent==false)
				return false;
		}
		return true;
	}
	
	private boolean updateAlphaBeta(CheckersAINode kid) //returns false if alpha>beta; that's when you're done exploring children
	{
		//Computer turn: 1, human turn: 0
		if(board.turn==1) //This is the computer, so trying to maximize
		{
			kid.buildTree(currHeight+1);
			if(kid.beta>=alpha) //If the minimizer's next perfect play guarantees the minimizer only something >= than the current best
			//We have to make this an equals sign so the computer doesn't just give up when it's about to lose . . .
			//Then this node's new alpha is that.
			//You'll have to beat the computer to test this!
			{
				bestNextMove = kid.moves;
				alpha = kid.beta;
				if(alpha>beta)
					return false;
			}
		}
		else //This is the person's turn, so trying to minimize
		{
			kid.buildTree(currHeight+1);
			if(kid.alpha<=beta)
			{
				bestNextMove = kid.moves;
				beta = kid.alpha;
				if(alpha>beta)
					return false;
			}
		}
		return true;
	}
	
	
	
	/*
	public double value() //We'll edit this for alpha-beta pruning soon!
	//When we use alpha-beta pruning, we'll probably just throw out all the current code and make it afresh
	{
		//Returns the maximum of the children values if it's computer's turn
		//Or returns the minimum of the children values if it's player turn
		//Or if it's the leaf node, return the current position
		if(isLeaf)
		{
			//Then we need to return the evaluation of any given position
			return board.evaluateBoard();
		}
		else
		{
			if(children.size()==0)
			{
				if(board.turn==1)
					return Integer.MIN_VALUE; //Worst possible outcome: no possible moves for the computer!
				else
					return Integer.MAX_VALUE; //This is the best possible outcome! No possible moves for the human!

			}
			else
			{ //Note that the computer is player 1, red!
				if(board.turn==1) //It's the computer's turn, return the max possible next position
				{
					double best = children.get(0).value();
					bestNextMove = children.get(0).moves;
					for(CheckersAINode possibleNext: children)
					{
						double nextStep = possibleNext.value();
						if(nextStep>best)
						{
							best = nextStep;
							bestNextMove = possibleNext.moves;
						}
					}
					return best;
				}
				else //otherwise, it's opponent's turn; return the worst (their best move)
				{
					double theirBest = children.get(0).value();
					bestNextMove = children.get(0).moves;
					for(CheckersAINode possibleNext: children)
					{
						double nextStep = possibleNext.value();
						if(nextStep<theirBest)
						{
							theirBest = nextStep;
							bestNextMove = possibleNext.moves;
						}
					}
					return theirBest;
				}
			}
		}
	}
	*/
}