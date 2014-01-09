import java.util.Stack;
import java.awt.Point;
import java.util.Arrays;

import java.io.*;
import java.util.*;
import java.awt.*;

public class Board implements Serializable
{
	private int width;	//Number of columns
	private int height;	//Number of rows
	private int winNumber; //Number of connections for a win

	private int[][] boardValues; 
	/*- 2D array holding a 0 for an empty space, and a player ID number representing a player's
	counter in that palce -*/
	
	private int[] colHeight; //1D array holding number of counters in each column
	Stack<Point> moves = new Stack<Point>(); //Stack of (x,y) move coords


	/*- Class setter -*/
	public Board(int width, int height,int winningNumber)
	{
		this.width = width;
		this.height = height;
		this.winNumber = winningNumber;

		boardValues = new int[height][width];
		colHeight = new int[width];

		newBoard();
	}

	/*- Initialise the boardValues and colHeight arrays to contain 0 -*/
	public void newBoard()
	{
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				boardValues[j][i] = 0;
			}
			colHeight[i] = 0;
		}
	}

	/*- Getter for number of columns -*/
	public int getWidth()
	{
		return this.width;
	}

	/*- Getter for number of rows -*/
	public int getHeight()
	{
		return this.height;
	}

	/*- Getter for win number -*/
	public int getWinNumber()
	{
		return this.winNumber;
	}

	/*- Returns the column of an x-coordinate based on a column size.
	If the coordinate is invalid (or on the boundary) -1 is returned -*/
	public int findCol(int x,int columnSize)
	{
		int i = 0;

		while (i <= width)
		{
			if (x > columnSize*i && x < columnSize*(i+1))
			{
				return i;
			}
			i++;
		}
		return -1;
	}

	/*- Check to see if a given coordinate is on the board and valid.
	Returns true if coord is valid, else false -*/
	public Boolean inBound(int x, int y)
	{
		if (x < 0 || x > (this.width - 1) || y < 0 || y > (this.height - 1))
		{
			return false;
		}
		return true;
	}

	/*- Checks to see if there is an n-number (where n = winNumber) horiontal sequence
	 of the same number p (p > 0) on the board.
	 If a sequence is found, p is returned. Otherwise -1 is returned -*/
	 public int checkHorizontal()
	 {
	 	int x = 0;
	 	int y = 0;
	 	int k = 0;
	 	int current = 0;
	 	int count = 1;
	 	while (x < (this.width - 1))
	 	{
	 		while (y < (this.height - 1))
	 		{
	 			current = this.boardValues[y][x];
	 			if (current > 0)
	 			{
	 				while (k < this.winNumber)
	 				{
	 					if (inBound(x+k,y) && (boardValues[y][x+k] == current))
	 					{
	 						count++;
	 					}

	 					if (count == this.winNumber+1)
	 					{
	 						return current;
	 					}
	 					k++;
	 				}
	 				k=0;
	 				count = 1;
	 			}
	 			y++;
	 		}
	 		y = 0;
	 		x++;	
	 	}
	 	return -1;
	 }

	/*- Checks to see if there is an n-number (where n = winNumber) diagonal sequence
	of the same number p (p > 0) on the board.
	If a sequence is found, p is returned. Otherwise -1 is returned -*/
	public int checkDiagonal()
	{
		int x = 0;
		int y = 0;
		int k = 0;
		int current = 0;
		int count = 1;
		while (x < (this.width - 1))
		{
			while (y < (this.height - 1))
			{
				current = this.boardValues[y][x];
				if (current > 0)
				{
					while (k < this.winNumber)
					{
						if (inBound(x+k,y + k) && (boardValues[y+k][x + k] == current))
						{
							count++;
						}
						if (count == (this.winNumber + 1))
						{
							return current;
						}
						k++;
					}
					k = 0;
					count  = 1;
					while (k < this.winNumber)
					{
						if (inBound(x+k,y - k) && (boardValues[y-k][x+k] == current))
						{
							count++;
						}

						if (count == (this.winNumber+1))
						{
							return current;
						}
						k++;
					}
					k=0;
					count = 1;
				}
				y++;
			}
			y = 0;
			x++;	
		}
		return -1;
	}


	/*- Checks to see if there is an n-number (where n = winNumber) vertical sequence
	of the same number p (p > 0) on the board.
	If a sequence is found, p is returned. Otherwise -1 is returned -*/
	public int checkVertical()
	{
		int x = 0;
		int y = 0;
		int k = 0;
		int current = 0;
		int count = 1;
		while (x < (this.width))
		{
			while (y < (this.height))
			{
				current = boardValues[y][x];
				if (current > 0)
				{
					while (k < this.winNumber)
					{
						if (inBound(x ,y + k) && (boardValues[y+k][x] == current))
						{
							count++;
						}

						if (count == (this.winNumber+1))
						{
							return current;
						}
						k++;
					}
					k=0;
					count = 1;
				}
				y++;
			}
			y = 0;
			x++;	
		}
		return -1;
	}

	/*- Checks to see if the current player has got a sequence of the correct length by
	calling each of the check functions. Returns the currentPlayer if there is a sequence
	otherwise returns 0 -*/
	public int checkWin(int currentPlayer)
	{
		int dag = checkDiagonal();
		int ver = checkVertical();
		int hor = checkHorizontal();

		if (hor == currentPlayer || ver == currentPlayer || dag == currentPlayer)
		{
			return currentPlayer;
		}
		return 0;
	}

	/*- Pushes a new (x,y) move onto the moves stack -*/
	public void saveMove(int x, int y)
	{
		Point newPoint = new Point(x,y);
		moves.push(newPoint);
	}

	/*- Pops the latest move off the stack. Then resets the boardValues array for
	this move position back to 0 to represent a free space -*/
	public Point undoMove()
	{
		if(moves.empty())
		{
			System.out.println("Can't pop from empty stack");
			return null;
		}

		Point lastMove = moves.pop();

		boardValues[(int)lastMove.getY()][(int)lastMove.getX()] = 0;
		colHeight[(int)lastMove.getX()]--;
		return lastMove;
	}

	public int getColHeight(int column)
	{
		return colHeight[column];
	}

	public Boolean checkFull(int moveCount)
	{
		//If number of moves equals total number of possible moves
		if(moveCount == (width*height)-1)
		{
			return true;
		}
		return false;
	}

	/*- First checks if the move is in the bounds of the board. If it is, set
	the boardValues array for the new move to be the current player's ID number
	and then save the move. Finally check to see if this new move has created a win.
	Returns -1 if move invalid, -2 if the board is full, 0 if no win found, and the
	winner's number if a win is found -*/
	public int updateValues(int column,int currentPlayer,int moveCount)
	{
		int row = colHeight[column];
		colHeight[column] += 1;

		if(!inBound(column,row))
		{
			return -1;
		}

		boardValues[row][column] = currentPlayer;

		saveMove(column,row);

		int winner = checkWin(currentPlayer);

		if(checkFull(moveCount))
		{
			winner = -2;
		}

		return winner;
	}


	/*- Prints the current board value's in a grid and then the height of each column.
	Used for testing only -*/
	public void printBoard()
	{
		for(int i = 0;i < height;i++)
		{
			for(int j = 0;j < width;j++)
			{
				System.out.print(boardValues[i][j]);
			}
			System.out.println();
		}
		System.out.println();

		for(int k = 0; k < width;k++)
		{
			System.out.println(colHeight[k]);
		}
	}

	/*- Returns an array of size number of columns. The array contains a 1 if that
	column has space for a valid move, otherwise 0 -*/
	public int[] getValidMoves()
	{
		int[] moves = new int[width];

		int i = 0;
		while(i < width)
		{
			if(colHeight[i] >= height)
			{
				moves[i] = 0;
			}
			else
			{
				moves[i] = 1;
			}
			i++;
		}

		return moves;
	}

	/*- Used for testing Board class -*/
	public void testing()
	{

	}

	public static void main(String[] args)
	{
		Board testBoard = new Board(7,6,4);
		testBoard.testing();

	}

}