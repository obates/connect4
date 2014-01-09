/*- Determines the best move to make based on simulating a number of games for
each possible move, and working out which move gave the best win ratio -*/

import java.lang.Math;
import java.util.Random;

public class Computer
{
	private int numberOfPlays;
	private int thisPlayer;

	public Computer(int dificulty,int player)
	{
		numberOfPlays = dificulty;
		thisPlayer = player;
	}

	public int getPlayer()
	{
		return thisPlayer;
	}

	/*- Gets an array which shows if a column has an allowed move. Then calls evaluateMove
	on each of these columns, it then returns the best scoring column -*/
	public int play(Board gameBoard,int moveCount)
	{
		int bestColumn = -1;
		float bestScore = -(Float.MAX_VALUE);

		int[] possibleMoves = gameBoard.getValidMoves();

		int i = 0;
		while(i < possibleMoves.length)
		{
			float currentScore = evaluateMove(gameBoard,i,moveCount);

			if(currentScore > bestScore)
			{
				bestScore = currentScore;
				bestColumn = i;
				
			}
			i++;
			System.out.println("Best move: " + bestColumn + " score: " + currentScore);
		}
		return bestColumn;
	}


	/*- Calls expandMove numberOfPlays times, firstly cloning the game board so that
	it can be reverted back after a game has been simulated. If the computer won
	the simulated game, it adds to the column score value, if it lost it takes away from the
	column score value. -*/
	public float evaluateMove(Board gameBoard,int column,int moveCount)
	{
		int value = 0; //Best value of move

		int i = 0;
		while(i < numberOfPlays)
		{
			Board clone = new Board(0,0,0);
			
			try
			{
				clone = (Board)ObjectCloner.deepCopy(gameBoard);
			}
			catch (Exception e)
			{
				System.err.println("Error");
			}

			//Clone current gameBoard so original not lost
			//try move on gameBoard

			int winner = expandMove(clone,moveCount);

			if(winner == thisPlayer)
			{
				value++;
			}
			else if(winner > 0) //winner isn't this player but not draw (-2)
			{
				value--;
			}
			i++;
		}

		return (value/(float)numberOfPlays);
	}

	/*- Repetedly chooses a random number between 0 and column number, updating the
	gameBoard with this value. When a player has won or if it is a draw, this value
	is returned. -*/
	public int expandMove(Board gameBoard,int moveCount)
	{
		int randomColumn;
		int winner = 0;
		int moves[];
		int currentPlayer;
		int playerChoice = 0;
		int currentMove = moveCount;

		Random rand = new Random();

		while(winner == 0)
		{
			if((playerChoice % 2) == 0)
			{
				currentPlayer = thisPlayer;
			}
			else
			{
				currentPlayer = thisPlayer + 1;
			}

			moves = gameBoard.getValidMoves();
			randomColumn = rand.nextInt(moves.length); 


			winner = gameBoard.updateValues(randomColumn,currentPlayer,currentMove);
			currentMove++;
			playerChoice++;
		}


		return winner;

	}

}