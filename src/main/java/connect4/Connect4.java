import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.JOptionPane;
import java.awt.Point;
import javax.swing.JCheckBox;


public class Connect4 implements MouseListener, MouseMotionListener
{

	private int moveCount = 0; //Holds the current number of moves
	private int playerNumber;//Holds the number of players

	//private Computer[] computers = new Computer[4]; Allows multiple computers
	private Computer computers;
	private int computerNumber;

	private JFrame gameFrame;//Frame for playing the game
	private JFrame settingsFrame;//Frame for the settings screen

	private JLabel hoverLabel;//Holds current player's piece. Moves with the mouse
	private JPanel hoverBoard;//Rectangle for hoverLabel to move in

	private JLabel currentPlayerLabel; //Shows "Player N" where N is the current user

	private JLayeredPane layeredPane;
	private JPanel mainBoard;

	private Board board;

	public static final Integer GRIDSIZE = 60;//Pixel width & height of each grid square
	public static final Color BACKGROUND = new Color(0x333333);
	public static final Color FOREGROUND = new Color(0xADADAD);


	public Connect4()
	{
		settingsFrame = new JFrame();
		settingsFrame.setLayout(new BoxLayout(settingsFrame,BoxLayout.PAGE_AXIS));

		gameFrame = new JFrame(); 
		gameFrame.setVisible(false);
		createSettingsUI();

	}

	/*- Run every time the mouse is pressed on the mainBoard. 
	Firstly determines which column has been selected based on where the mouse was clicked.
	If the mouse was clicked out of bounds or on a border then returned.
	Secondly works out the currentPlayer, and updates the board's value for the selected
	column. 
	If the updateValues function returns the currentPlayer then this player has won
	so show the win dialog. 
	If there are any computer players then call the computerMove function. -*/
	public void mousePressed(MouseEvent e)
	{
		//piece = null;

		int column = board.findCol(e.getX(),GRIDSIZE);

		if(column == -1)
		{
			//Clicked on boundary or out of bounds
			return;
		}

		int currentPlayer = (moveCount%playerNumber)+1;
		int winner = board.updateValues(column,currentPlayer,moveCount);

		if(winner == -1)
		{
			//Clicked out of bounds or in a full column
			return;
		}

		//Update the game UI based on the move performed
		updateUI(e.getX(),column);

		//If there is a win for the current player
		int finish = showWin(winner,currentPlayer);

		//Finish used to ensure the computer doesn't try and make a move after
		//the game has been won
		if(finish != -1 && computerNumber > 0)
		{
			computerMove();
		}
	}

	/*- Runs everytime the mouse is moved around the gameFrame.
	Firstly determines which column the mouse is currently in, and then
	updates the hoverLabel to appear in this column. -*/
	public void mouseMoved(MouseEvent e) 
	{		
		int column = board.findCol(e.getX(),GRIDSIZE);
		hoverLabel.setLocation(column*GRIDSIZE,0);
	}

	/*- Determines if the game has been won (winner > 0) or draw (winner == -2) -*/
	private int showWin(int winner,int currentPlayer)
	{
		if(winner == currentPlayer)
		{
			showWinnerDiag(currentPlayer);
			return -1;
		}

		if(winner == -2) //Draw
		{
			showWinnerDiag(-1);
			return -1;
		}
		return 0;
	}

	/*- Shows a dialog with a message based on who won the game or if the game was a draw.
	Then asks the user to choose between a rematch (resetting the board), going back to
	the settings screen, or exiting the game. -*/
	private void showWinnerDiag(int winner)
	{
		Object[] options = {"Exit","Game Setup",
		"Re-match"};

		String message = new String();;

		if(winner==-1)//Board full so draw
		{
			message = "Draw! What would you like to do?";
		}
		else//Display winner message
		{
			message = "Player " + winner + " wins!\n"
			+ "What would you like to do?";
		}

		int n = JOptionPane.showOptionDialog(gameFrame,
			message,
			"Game over",
			JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			null,
			options,
			options[2]);

		if(n == 0)
		{
			System.exit(1);
		}
		else if (n == 1)
		{
			showSettings();
		}
		else if(n == 2)
		{	
			reset();
		}
	}

	/*- Goes through the number of computers playing the current game.
	Firstly clones the board, and then recieves the best column to play based on the
	current board.
	Restores the board, and then updates the relevant board position's value to be
	the computer's playerID number. Finally updates the game UI. -*/
	public void computerMove()
	{
		int i = 0;
		while(i < computerNumber)
		{

			Board temp = new Board(0,0,0);
			try
			{
				temp = (Board)ObjectCloner.deepCopy(board);
			}
			catch (Exception e)
			{
				System.err.println("Error");
			}

			int column = computers.play(board,moveCount);

			System.out.println("Column: "+column);

			try
			{
				board = (Board)ObjectCloner.deepCopy(temp);
			}
			catch (Exception e)
			{
				System.err.println("Error");
			}

			int winner = board.updateValues(column,computers.getPlayer(),moveCount);

			int currentColumnHeight = board.getColHeight(column);

			updateUI(column*GRIDSIZE,column);

			i++;

			//checkWin(winner,computers[i].getPlayer());
		}
	}

	/*- Gets the relevant column's height. Then finds the JLabel at that height and 
	the x coordinate given. Sets the JLabel's icon to the current player's image icon.
	Checks if the column is full, if not then it places an 'active' square above where
	the move has taken place.
	Finally updates the move count and hover label and Player N text to be that of
	the next player. -*/
	private void updateUI(int x,int column)
	{
		int currentColumnHeight = board.getColHeight(column);

		//Sets column's next available position to current player's counter
		Component current =  mainBoard.findComponentAt(x,
			(GRIDSIZE * board.getHeight()) - (currentColumnHeight*GRIDSIZE));

		JLabel panel = (JLabel)current;
		panel.setIcon(whichPlayer());


		//Ensures active square isn't put when a column is full
		if(!(currentColumnHeight == board.getHeight()))
		{
			//Sets position above the move to be an 'active' square
			Component above =  mainBoard.findComponentAt(x,
				(GRIDSIZE * board.getHeight()) - (currentColumnHeight*(GRIDSIZE+1)));

			JLabel active = (JLabel)above;
			active.setIcon(new ImageIcon(Connect4.class.getResource("/images/squareActive.png")));
		}

		moveCount++;
		//As move count is now updated, whichPlayer will return next player's counter
		hoverLabel.setIcon(whichPlayer());
		currentPlayerLabel.setText("Player "+ ((moveCount%playerNumber)+1));
	}

	/*- Returns an ImageIcon of the CurrentPlayerNumber.png -*/
	public ImageIcon whichPlayer()
	{
		return new ImageIcon(Connect4.class.getResource("/images/"+(moveCount%playerNumber)+".png"));
	}

	/*- Sets the move count to 0. Then resets the boardValues array back to 0.
	Finally removes all components from the gameFrame and then re-adds them as new -*/
	private void reset()
	{
		moveCount = 0;

		board.newBoard();

		gameFrame.getContentPane().removeAll();

		createGameUI(board.getWidth(),board.getHeight());

		gameFrame.getContentPane().validate(); //Needed to allow new components
		gameFrame.getContentPane().repaint(); //Needed to allow new components
	}


	/*- Firstly calls the undoMove function to revert the board's values back. Then updates
	the UI to the previous state-*/
	private void undoMove()
	{
		Point lastMove = board.undoMove();
		if(lastMove == null) //If trying to pop from empty stack
		{
			return;
		}

		int x = (int)lastMove.getX();
		int y = (int)lastMove.getY();

		System.out.println(y);

		if(y != (board.getHeight()-1))//Set active square back to normal
		{
			Component current =  mainBoard.findComponentAt(x*GRIDSIZE,
				(GRIDSIZE * board.getHeight()) - ((y+2)*GRIDSIZE));
			JLabel panel = (JLabel)current;
			panel.setIcon(new ImageIcon(Connect4.class.getResource("/images/squareBG.png")));

		}

		Component current =  mainBoard.findComponentAt(x*GRIDSIZE,
			(GRIDSIZE * board.getHeight()) - ((y+1)*GRIDSIZE));
		JLabel panel = (JLabel)current;
		panel.setIcon(new ImageIcon(Connect4.class.getResource("/images/squareActive.png")));

		moveCount--;
		currentPlayerLabel.setText("Player "+ ((moveCount%playerNumber)+1));
		hoverLabel.setIcon(whichPlayer());
	}

	private void showGame()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run() {
				settingsFrame.setVisible(false);
				gameFrame.setVisible(true);
			}
		});
		
	}

	private void showSettings()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run() {
				moveCount = 0;
				gameFrame.removeAll();
				gameFrame.setVisible(false);
				settingsFrame.setVisible(true);

			}
		});
	}

	/*- Sets up a new panel the width of the gameBoard and the height of a single
	counter which will display the current user's counter above their mouse. -*/
	private void setupHover(int columns)
	{
		Dimension hoverSize = new Dimension(columns*GRIDSIZE,GRIDSIZE);
		Dimension pieceSize = new Dimension(GRIDSIZE,GRIDSIZE);


		hoverBoard = new JPanel();
		hoverBoard.setPreferredSize(hoverSize);
		hoverBoard.setLayout(null); //Used so that .setLocation is allowed
		hoverBoard.setBounds(0,0,columns*GRIDSIZE,GRIDSIZE);
		hoverBoard.addMouseMotionListener(this); //Adds the mouse motion listner
		hoverBoard.addMouseListener(this); //Adds mouse click listner
		hoverBoard.setBackground(BACKGROUND);

		gameFrame.getContentPane().add(hoverBoard);

		hoverLabel = new JLabel(whichPlayer()); //Set initial counter to player 1
		hoverLabel.setSize(pieceSize);
		hoverLabel.setOpaque(true);
		hoverLabel.setLocation(0,0); //Set initial position to 0,0
		hoverLabel.setBounds(0,0,GRIDSIZE,GRIDSIZE);

		hoverBoard.add(hoverLabel); //Add hoverLabel to the hoverBoard

	}

	/*- Firstly creates a layered pane the correct size. Adds the mouse click and motion
	listner to this pane.
	Secondly create the mainBoard, which is split up into a grid with the correct number
	of column and rows. Blank squares are then added in sequence to the grid. -*/
	private void populateGame(int columns,int rows)
	{
		Dimension boardSize = new Dimension(columns*GRIDSIZE,rows*GRIDSIZE);

		setupHover(columns);

		//  Setup Layered Pane
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(boardSize);
		layeredPane.addMouseListener(this);
		layeredPane.addMouseMotionListener(this);
		gameFrame.getContentPane().add(layeredPane);

		//  Setup a JPanel grid on top of the Layered Pane
		mainBoard = new JPanel();
		mainBoard.setLayout(new GridLayout(rows, columns)); //Split up into grid
		mainBoard.setPreferredSize(boardSize);
		mainBoard.setBounds(0,0, boardSize.width, boardSize.height);
		layeredPane.add(mainBoard, JLayeredPane.DEFAULT_LAYER);

		//Build the Board squares
		int i=0;
		int j=0;
		while(i < rows)
		{
			while(j < columns)
			{
				JPanel square = new JPanel(new BorderLayout());
				square.setOpaque(true);

				//Label to hold the square's image
				JLabel squareBG = new JLabel(new ImageIcon(Connect4.class.getResource("/images/squareBG.png")));

				if(i==rows-1)
				{
					//If first row then show a layer of 'active squares'
					squareBG.setIcon(new ImageIcon(Connect4.class.getResource("/images/squareActive.png")));	
				}

				squareBG.setSize(GRIDSIZE,GRIDSIZE);
				squareBG.setOpaque(true);
				square.add(squareBG);

				mainBoard.add(square);
				j++;
			}
			j=0;
			i++;
		}

	}

	/*- Used so that changing the style of the buttons is quick -*/
	private JButton newButton(String text)
	{
		JButton button = new JButton(text);
		button.setForeground(FOREGROUND);
		button.setBackground(BACKGROUND);
		Border line = new LineBorder(Color.BLACK);
		Border margin = new EmptyBorder(5, 15, 5, 15);
		Border compound = new CompoundBorder(line, margin);
		button.setBorder(compound);
		return button;
	}

	/*- Used to change style of all the labels -*/
	private JLabel newLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setFont(new Font("Helvetica", Font.BOLD, 15));
		label.setForeground(FOREGROUND);
		return label;
	}

	/*- Firstly dynamically determines the height of the frame based on the number of
	columns selected by the user (less columns means the button container will collapse).
	Creates each of the buttons (reset, undo and give up), and adds the button listners.
	-*/
	private void createGameUI(int columns,int rows)
	{
		int buttonSpace = 100; //Extra space below the gameFrame
		if(columns < 3)
		{
			buttonSpace = 160; //If too few columns, allow extra space
		}
		gameFrame.setSize(new Dimension(columns*GRIDSIZE,(rows+1)*GRIDSIZE+buttonSpace));

		gameFrame.setResizable(false);

		Container cp = gameFrame.getContentPane();
		cp.setLayout(new FlowLayout());
		cp.setBackground(BACKGROUND); //Set background colour

		populateGame(columns,rows); //Fill gameFrame

		currentPlayerLabel = newLabel("Player 1"); //Set current player label
		cp.add(currentPlayerLabel);


		//Adds reset button
		JButton resetButton = newButton("Reset");
		resetButton.addActionListener(new ActionListener()
		{
			//When button is clicked reset the board
			public void actionPerformed(ActionEvent e)
			{
				reset();
			}
		});
		cp.add(resetButton);

		//Adds undo button
		JButton undoButton = newButton("Undo");
		undoButton.addActionListener(new ActionListener()
		{
			//When button is clicked undo last move
			public void actionPerformed(ActionEvent e)
			{
				undoMove();
			}
		});
		cp.add(undoButton);

		//Adds give up button
		JButton giveupButton = newButton("Give up");
		giveupButton.addActionListener(new ActionListener() {
			//When button is clicked return to settings screen
			public void actionPerformed(ActionEvent e)
			{
                //Execute when button is pressed
				showSettings();
			}
		});   
		cp.add(giveupButton);

		gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		gameFrame.addWindowListener(new WindowAdapter(){
			//When the X button is clicked, return to the settings screen
			@Override
			public void windowClosing(WindowEvent e) {
				showSettings();
			}

		});


	}

	/*- Called for each spinner -*/
	private JSpinner createSpinner(int start,int min,int max)
	{
		SpinnerModel spinModel = new SpinnerNumberModel(start,min,max,1);
		JSpinner spinner = new JSpinner(spinModel);
		((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setEditable(false);
		return spinner;
	}

	/*- Sets up the settings frame. Makes the LayoutManager a FlowLayout and then adds
	the label/spinners for each of the game options. Finally adss the button/button listners -*/
	private void createSettingsUI()
	{
		settingsFrame.setSize(new Dimension(350,300));//Size of settings frame
		settingsFrame.setResizable(false);

		Container cp = settingsFrame.getContentPane();
		cp.setLayout(new FlowLayout());
		cp.setBackground(BACKGROUND);

		settingsFrame.add(Box.createRigidArea(new Dimension(500,5)));

		JLabel title = newLabel("Connect4!"); //Custom title so newLabel not used
		title.setFont(new Font("Helvetica", Font.BOLD, 24));
		title.setForeground(FOREGROUND);
		cp.add(title);

		settingsFrame.add(Box.createRigidArea(new Dimension(500,5)));

		cp.add(newLabel("Number of rows: "));
		final JSpinner rowSpinner = createSpinner(6,1,20);
		cp.add(rowSpinner);

		settingsFrame.add(Box.createRigidArea(new Dimension(500,5)));

		cp.add(newLabel("Number of columns: "));
		final JSpinner columnSpinner = createSpinner(7,1,20);
		cp.add(columnSpinner);

		settingsFrame.add(Box.createRigidArea(new Dimension(500,5)));

		cp.add(newLabel("Connections for a win: "));
		final JSpinner connectionsSpinner = createSpinner(4,2,20);
		cp.add(connectionsSpinner);

		settingsFrame.add(Box.createRigidArea(new Dimension(500,5)));

		cp.add(newLabel("Number of players (max 4): "));
		final JSpinner playerSpinner = createSpinner(2,2,4);
		cp.add(playerSpinner);

		settingsFrame.add(Box.createRigidArea(new Dimension(500,5)));

		JButton exitButton = newButton("Exit");
		exitButton.addActionListener(new ActionListener()
		{
			//When exit button is clicked exit
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		cp.add(exitButton);

		JButton computerButton = newButton("Play vs computer");
		computerButton.addActionListener(new ActionListener() {
			//When computer button pressed
			public void actionPerformed(ActionEvent e)
			{
                //Retrieve values from spinners 
				int rows = (Integer) rowSpinner.getValue();
				int columns = (Integer) columnSpinner.getValue();
				int connections = (Integer) connectionsSpinner.getValue();


				playerNumber = (Integer)playerSpinner.getValue();

				//Create computers based on how many chosen
				computerNumber = 1;
				int i = 0;
				while(i < (computerNumber))
				{
					computers = new Computer(20000,playerNumber+i);
					i++;
				}

				//Number of connections for a win cannot be greater than colum and row size
				if (connections > rows && connections > columns)
				{
					JOptionPane.showMessageDialog(null,
						"Number of connections for a win is greater than the number of columns and rows!",
						"Connections error",
						JOptionPane.ERROR_MESSAGE);
				}
				else
				{
					//Create new board based on settings
					board = new Board(columns,rows,connections);
					gameFrame = new JFrame();
					createGameUI(columns,rows);
					showGame();
				}

			}
		});
cp.add(computerButton);

JButton playButton = newButton("Play!");
playButton.addActionListener(new ActionListener() {
			//When the play button is pressed
	public void actionPerformed(ActionEvent e)
	{
                //Retrieve values from spinners
		int rows = (Integer) rowSpinner.getValue();
		int columns = (Integer) columnSpinner.getValue();
		int connections = (Integer) connectionsSpinner.getValue();

		playerNumber = (Integer)playerSpinner.getValue();

		computerNumber = 0;

				//Number of connections for a win cannot be greater than colum and row size
		if (connections > rows && connections > columns)
		{
			JOptionPane.showMessageDialog(null,
				"Number of connections for a win is greater than the number of columns and rows!",
				"Connections error",
				JOptionPane.ERROR_MESSAGE);
		}
		else
		{
					//Create new board based on settings
			board = new Board(columns,rows,connections);
			gameFrame = new JFrame();
			createGameUI(columns,rows);
			showGame();
		}

	}
});
cp.add(playButton);

		//Exit when X button clicked
settingsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
settingsFrame.setVisible(true);
}



public void mouseDragged(MouseEvent me){}
public void mouseReleased(MouseEvent e){}
public void mouseClicked(MouseEvent e) {}
public void mouseEntered(MouseEvent e) {}
public void mouseExited(MouseEvent e) {}

public static void main(String[] args)
{
	SwingUtilities.invokeLater(new Runnable()
	{
		@Override
		public void run() {
			Connect4 game = new Connect4();
		}
	});
}

}