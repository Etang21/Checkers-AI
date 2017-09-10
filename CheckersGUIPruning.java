//This class is the GUI representation of the board
//Allowing the player to see the board
//And make moves on the board
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class CheckersGUIPruning extends JFrame implements ActionListener
{
	private JButton[][] buttons;
	private int prevClicked; //This will be useful in the ActionListener, when you need to remember what was previously clicked.
	//prevClicked stores data in the form: first digit = row, second digit = column
	private CheckersOp data;
	private boolean isTwoHumans; //This decides whether it's a human v. human or human v. computer match. (Could've made two classes, but this was simpler).
	private boolean gameOver;
	
	public CheckersGUIPruning(String title, boolean hasTwoHumans)
	{
		super(title);

		setSize(800,800);
		setLayout(new GridLayout(8,8));
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		prevClicked = 0;
		gameOver = false;
		
		buttons = new JButton[8][8];
		for(int r=0; r<8; r++)
		{
			for(int c=0; c<8; c++)
			{
				buttons[r][c] = new JButton("O");
				add(buttons[r][c]);
				buttons[r][c].addActionListener(this);
				buttons[r][c].setActionCommand(r+""+c);
				buttons[r][c].setOpaque(true);
				buttons[r][c].setVisible(true);
				buttons[r][c].setFont(new Font("Arial", Font.BOLD, 60));
				buttons[r][c].setBackground(Color.BLACK); //This is just fo' style
					
			}
		}
		
		data = new CheckersOp();
		data.resetBoard();
		isTwoHumans = hasTwoHumans;
		drawBoard();

	}
	
	public void drawBoard() //Takes the board info and redraws the board; will run after every move
	{
		int[][] board = data.getBoard();
		for(int row=0; row<8; row++)
		{
			for(int column=0; column<8; column++)
			{
				if(board[row][column]==0)
				{
					buttons[row][column].setText("");
				}
				if(board[row][column]==1)
				{
					buttons[row][column].setForeground(Color.RED);
					buttons[row][column].setText("O");
				}
				if(board[row][column]==2)
				{
					buttons[row][column].setForeground(Color.BLACK);
					buttons[row][column].setText("O");
				}
				if(board[row][column]==3)
				{
					buttons[row][column].setForeground(Color.RED);
					buttons[row][column].setText("K");
				}
				if(board[row][column]==4)
				{
					buttons[row][column].setForeground(Color.BLACK);
					buttons[row][column].setText("K");
				}
			}
		}
		repaint();
	}
	
	public void actionPerformed(ActionEvent evt) //Interprets button presses
	{
		if(gameOver) //This locks up the screen.
			return;
		//Note: the winner check still needs some refining in here
		int currClick = Integer.parseInt(evt.getActionCommand());
		int didWork = data.makeMove(prevClicked, currClick);
		if(didWork!=0)
		{
			System.out.println("Human has moved.");
			drawBoard();
			int winner = data.checkWinner();
			
			if(isTwoHumans==true)
			{
				if(data.isRedTurn())
				{
					if(winner==2)
					{
						System.out.println("The Black Player has triumphed! Congratulations, and major props.");
						gameOver = true;
						return;
					}
				}
				else if(!data.isRedTurn()) //This means it's black's turn, so red just moved
				{
					if(winner==1)
					{
						System.out.println("The Red Player has triumphed! Way to go!");
						gameOver = true;
						return;
					}
				}
			}
			
			if(isTwoHumans==false) //Now it's the computer's move
			{
				if(winner==2) //Check for a human win
				{
					System.out.println("You beat the computer! Congratulations!");
					gameOver = true;
					return;
				}
				
				if(data.isRedTurn()) //This means it's the computer's turn
				{
					//Here we feed our wonderful AI the board, and ask it to make as many moves as needed
					//We know it must have at least one move, since we just used checkWinner() and found no winner
					//Then we'll have a while loop in here, too: while(data.currJumper!=null), keep playing
					//And finally at the end of the jump chain or at the end of the single move, the turns will have flipped within data
					//, the actionListener will be done
					//And control will pass back to the player.
										
	
					long startMillis = System.currentTimeMillis();
					CheckersAITree thinker = new CheckersAITree(data); //CheckersAITree will clone it for us
					String bestMoves = thinker.findBestMove(); //We ask our AI Tree what the best move is
					String[] arrMoves = bestMoves.split(","); //Divides up the string of moves by comma
					System.out.println("The AI moved: " + bestMoves);
					for(int i=0; i<arrMoves.length-1; i++)
					{
						data.makeMove(Integer.parseInt(arrMoves[i]),Integer.parseInt(arrMoves[i+1]));
					}
					drawBoard();
					long endMillis = System.currentTimeMillis();
					System.out.println("The AI took " + (endMillis-startMillis) + " milliseconds to find this move.");
					
					winner = data.checkWinner(); //After the computer move, we check for a Red win
					if(winner==1)
					{
						System.out.println("The Computer has Triumphed. RIP.");
						gameOver = true;
					}
				}
			}
			
		}
		prevClicked = currClick; //You'll notice that the old player's last square will get stored as the next player's prevClicked, but
		//but this is okay, because there will never be a legal move whose prevClick is a square with the opponent's piece
		this.setTitle(returnTitleString(data.turnCount));
	}
	
	private String returnTitleString(int turnCount)
	{
		String titleString = "";
		if(isTwoHumans) titleString += "Human vs. Human";
		else titleString += "Human vs. Computer";
		titleString += ", ";
		if(data.isRedTurn()) titleString += "Red's Turn";
		else titleString += "Black's Turn";
		titleString += ", Turn Count: " + turnCount;
		return titleString;
	}
	
	
	public static void main(String[] args)
	{
		CheckersGUIPruning game = new CheckersGUIPruning("First Game!", false);
		game.setVisible(true);
	}

}