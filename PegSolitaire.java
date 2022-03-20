/**
 *  @author Muhammet Emrah Kucuk 1801042100.
 *  Load function is not working.
    The 6th board is being created but not working.
    Except for these two features, everything is complete, everything is working properly.
    If the game is crashed due to FPS, turn it off and on again.
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.Random;
import java.io.*;
import java.util.Scanner;

public class PegSolitaire extends JFrame implements PegSolitaireGame, Cloneable, ActionListener
{
    private JPanel menuPanel;
    private JPanel boardPanel;
    private JPanel boardOptionPanel;

    private JRadioButton playerButton;
    private JRadioButton computerButton;
    private JRadioButton[] type;
    private JButton playButton;
    private JButton saveButton;
    private JButton loadButton;
    private JButton resetButton;
    private JButton undoButton;
    private JTextField fileNameField;
    private Vector<JButton> cellButton;

    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private final int NUM_OF_TYPE = 6;
    private int rowNumber;
    private int columnNumber;

    private int boardType;
    private Vector<Vector<Cell>> board;
    private Cell[] move;
    private boolean isFirstMove = true;
    private Vector<String> allMoves;

    /**
     * This function is constructor. Creates menu and board. It has no parameter.
     */
    public PegSolitaire()
    {
        super("PEG SOLITAIRE GAME");

        createMenuPanel();
        add(menuPanel);

        createBoardOptionPanel();
        configureWindow();
    }

    /**
     * This function configures the Window. It has no parameter and its return type is void.
     */
    public void configureWindow()
    {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setSize(WIDTH, HEIGHT);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * This function does things according to the button status.
     * @param e is a ActionEvent, button status.
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object src = e.getSource();

        if(playerButton.equals(src))
            computerButton.setSelected(false);
        else if(computerButton.equals(src))
            playerButton.setSelected(false);
        else if(playButton.equals(src))
        {
            boolean isPlayable = false;

            for(int i=0 ; i < NUM_OF_TYPE ; ++i)
                isPlayable |= type[i].isSelected();

            isPlayable &= playerButton.isSelected() || computerButton.isSelected();

            if(isPlayable)
            {
                if(getContentPane().getComponentCount() != 1)
                    remove(getContentPane().getComponent(1));

                createBoardPanel();
                add(boardPanel);
                add(boardOptionPanel);

                configureWindow();
            }
            else
                JOptionPane.showMessageDialog(null, "User and type of level must be selected!", "ERROR", JOptionPane.PLAIN_MESSAGE);
            if(computerButton.isSelected())
            {
                while(!endGame())
                {
                    System.out.println("The game continues!\n");
                    playAuto();
                    updateCellButton();
                }
                System.out.println("Game Over!\n");
                updateCellButton();

                //if(endGame())
                JOptionPane.showMessageDialog(null, "Computer score is: " + boardScore(), "GAME OVER", JOptionPane.PLAIN_MESSAGE);
            }    
        }
        else if(saveButton.equals(src))
        {
            if(fileNameField.getText().equals(""))
                saveFile("default.txt");
            else
                saveFile(fileNameField.getText());    
        }
        else if(loadButton.equals(src))
        {
            JOptionPane.showMessageDialog(null, "I wrote a function for load but it didn't work properly :(", "LOAD", JOptionPane.PLAIN_MESSAGE);
        }
        else if(resetButton.equals(src))
            playButton.doClick();
        else if(undoButton.equals(src))
            undo();
        else
        {
            boolean typeOrNot = false;

            for(int i=0 ; i < NUM_OF_TYPE ; ++i)
                if (type[i].equals(src))
                {
                    typeOrNot = true;
                    break;
                }

            if(typeOrNot) // If button is board type
            {
                for(int i = 0; i < NUM_OF_TYPE; ++i)
                    if(!type[i].equals(src))
                        type[i].setSelected(false);
            }
            else // If button is cell
            {
                for(int i=0 ; i < cellButton.size() ; ++i)
                    if(cellButton.get(i).equals(src))
                    {
                        if(isFirstMove)
                        {
                            move = new Cell[2];
                            move[0] = board.get(i / getRowNumber()).get(i % getColumnNumber());
                            isFirstMove = false;
                        }
                        else
                        {
                            move[1] = board.get(i / getRowNumber()).get(i % getColumnNumber());
                            isFirstMove = true;
                        }
                    }

                if(move[0] != null && move[1] != null && isFirstMove)
                {
                    String m = generateMoveString(move[0], move[1]);
                    
                    if(isValidMove(m))
                    {
                        allMoves.add(m);

                        if(getDirection(m) == 0)
                        {
                            moveRight(m);
                            updateCellButton();
                        }
                        else if(getDirection(m) == 1)
                        {
                            moveLeft(m);
                            updateCellButton();
                        }
                        else if(getDirection(m) == 2)
                        {
                            moveUp(m);
                            updateCellButton();
                        }
                        else
                        {
                            moveDown(m);
                            updateCellButton();
                        }
                    }
                    else
                        System.out.printf("Invalid Move!\n");

                    updateCellButton();
                    if(endGame())
                        JOptionPane.showMessageDialog(null, "Your score is: " + boardScore(), "GAME OVER", JOptionPane.PLAIN_MESSAGE);

                }
            }
        }
    }
    /**
     * This function has no parameter. It updates the buttons. Its return type is void.
     */
    public void updateCellButton()
    {
        for(int i=0 ; i < getRowNumber() ; ++i)
            for(int j=0 ; j < getColumnNumber() ; ++j)
            {
                if(!cellButton.get(i * getColumnNumber() + j).getText().equals(board.get(i).get(j).toString()))
                    cellButton.get(i * getColumnNumber() + j).setText(board.get(i).get(j).toString());
            }
    }

    /**
     * Generates string of movement. If movement is invalid, returns null
     * @return null if movement is invalid. Otherwise returns movement string like 2B UP.
     */
    public String generateMoveString(Cell first, Cell second)
    {
        if(Math.abs(first.getRow() - second.getRow()) == 2 || Math.abs(first.getColumn() - second.getColumn()) == 2)
        {
            StringBuilder sb = new StringBuilder();

            sb.append(first.getRow()).append((char)('A' + first.getColumn())).append(" ");

            if(first.getRow() == second.getRow())
            {
                if(first.getColumn() - second.getColumn() < 0)
                    sb.append("RIGHT");
                else
                    sb.append("LEFT");
            }
            else // first.getColumn = second.getColumn
            {
                if(first.getRow() - second.getRow() < 0)
                    sb.append("DOWN");
                else
                    sb.append("UP");
            }

            return sb.toString();
        }

        return null;
    }
    /**
     * This is undo function.
     * Undo feature for a single step.
     */
    private void undo()
    {
        if(allMoves.size() == 0)
        {
            JOptionPane.showMessageDialog(null, "There aren't any movement!", "ERROR", JOptionPane.PLAIN_MESSAGE);
            return;
        }

        String[] s = allMoves.get(allMoves.size() - 1).split(" ");
        int row = Character.getNumericValue(s[0].charAt(0));
        int column = (int)(s[0].charAt(1) - 'A');
        int posX=0, posY=0;

        switch (s[1])
        {
            case "LEFT" : 
                posX = -1;
                break;
            case "RIGHT" : 
                posX = 1;
                break;
            case "UP" : 
                posY = -1;
                break;
            case "DOWN" : 
                posY = 1;
                break;
        }

        board.elementAt(row).elementAt(column).setState(CellState.PEG);
        board.elementAt(row + posY).elementAt(column + posX).setState(CellState.PEG);
        board.elementAt(row + 2 * posY).elementAt(column + 2 * posX).setState(CellState.EMPTY);

        allMoves.remove(allMoves.size() - 1);
        updateCellButton();
    }
    /**
     * This function creates the menuPanel.
     */
    public void createMenuPanel()
    {
        menuPanel = new JPanel();

        playerButton = new JRadioButton("USER GAME", true);
        playerButton.addActionListener(this);
        menuPanel.add(playerButton);

        computerButton = new JRadioButton("COMPUTER GAME");
        computerButton.addActionListener(this);
        menuPanel.add(computerButton);

        type = new JRadioButton[NUM_OF_TYPE];

        for(int i=0 ; i < NUM_OF_TYPE ; ++i)
        {
            type[i] = new JRadioButton("Type " + (i + 1));
            type[i].addActionListener(this);
            menuPanel.add(type[i]);
        }
        type[0].setSelected(true);

        playButton = new JButton("PLAY");
        playButton.addActionListener(this);
        menuPanel.add(playButton);
    }
    /**
     * This function creates the boardPanel. JButton for each cell.
     */
    public void createBoardPanel()
    {
        initialize();

        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(getRowNumber(), getColumnNumber(), 10, 10));
        cellButton = new Vector<>();

        for(int i=0 ; i < getRowNumber(); ++i)
            for(int j=0 ; j < getColumnNumber() ; ++j)
            {
                JButton button = new JButton(board.elementAt(i).elementAt(j).toString());

                cellButton.add(button);
                boardPanel.add(button);
                button.addActionListener(this);
            }
    }
    /**
     * This function creates the optionPanel : Save, Load, Reset, Undo.
     */
    public void createBoardOptionPanel()
    {
        boardOptionPanel = new JPanel();
        boardOptionPanel.setLayout(new FlowLayout());

        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        boardOptionPanel.add(resetButton);

        undoButton = new JButton("Undo");
        undoButton.addActionListener(this);
        boardOptionPanel.add(undoButton);

        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        boardOptionPanel.add(saveButton);

        loadButton = new JButton("Load");
        loadButton.addActionListener(this);
        boardOptionPanel.add(loadButton);

        fileNameField = new JTextField();
        fileNameField.setPreferredSize(new Dimension (250,30));
        boardOptionPanel.add(fileNameField);
    }
    /**
     * This function initializes the board.
     */
    @Override
    public void initialize()
    {
        allMoves = new Vector<>();

        for(int i=0 ; i < NUM_OF_TYPE ; ++i)
            if(type[i].isSelected())
                boardType = i;

        selectBoard();
    }
    /**
     * This function prepares the board.
     */
    public void selectBoard()
    {
        board = new Vector<>();
        setRowNumber(ExistBoards.board[boardType].length);
        setColumnNumber(ExistBoards.board[boardType][0].length);

        for(int i=0 ; i < getRowNumber() ; ++i)
        {
            Vector<Cell> v = new Vector<>();

            for(int j=0 ; j < getColumnNumber() ; ++j)
            {
                Character chr = ExistBoards.board[boardType][i][j];

                if(chr.equals('.'))
                    v.add(new Cell(i, j, CellState.EMPTY));
                else if(chr.equals('P'))
                    v.add(new Cell(i, j, CellState.PEG));
                else if(chr.equals('W'))
                    v.add(new Cell(i, j, CellState.WALL));
                else if(chr.equals('*'))
                    v.add(new Cell(i, j, CellState.SPACE));
            }

            board.add(v);
        }
    }
    /**
     * Getter rowNumber.
     * @return row number.
     */
    public int getRowNumber() { return rowNumber; }

    /**
     * Getter columnNumber.
     * @return column number.
     */
    public int getColumnNumber() { return columnNumber; }

    /**
     * Sets the rowNumber.
     * @param rowNumber is set as the number of rows.
     */
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
    /**
     * Sets the columnNumber.
     * @param columnNumber is set as the number of columns.
     */
    public void setColumnNumber(int columnNumber) { this.columnNumber = columnNumber; }

    /**
     * Clone method.
     */
    @Override
    public PegSolitaire clone()
    {
        try
        {
            return (PegSolitaire) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new AssertionError();
        }
    }

    public class Cell
    {
        private int row;
        private int column;
        private CellState state;

        public Cell(int newRow, int newColumn, CellState newState)
        {
            row = newRow;
            column = newColumn;
            state = newState;
        }
        /**
         * Setter, sets the row.
         * @param newRow set the row.
         */
        public void setRow(int newRow) { row = newRow; }
        /**
         * Setter, sets the column.
         * @param newColumn set the column.
         */
        public void setColumn(int newColumn) { column = newColumn; }
        /**
         * Setter, sets the state of cell.
         * @param newState set the state.
         */
        public void setState(CellState newState) { state = newState; }
        /**
         * Getter for state.
         * @return the state.
         */
        public CellState getState() { return state; }
        /**
         * Getter for row.
         * @return the row.
         */
        public int getRow() { return row; }
        /**
         * Getter for column.
         * @return the column.
         */
        public int getColumn() { return column; }

        @Override
        public String toString()
        {
            if(state.equals(CellState.WALL))
                return "W";
            else if(state.equals(CellState.PEG))
                return "P";
            else if(state.equals(CellState.EMPTY))
                return ".";
            else
                return " ";
        }
    }
    /**
     * This function cheks the movement is valid or not.
     * @param input is movement.
     * @return true if movement is valid, false otherwise.
     */
    public boolean isValidMove(String input)
    {
        boolean res = false;
        int row, column;
        row = (int)input.charAt(0) - 48;
        if((int)input.charAt(1) >= 97)
            column = (int)input.charAt(1) - 97;
        else
            column = (int)input.charAt(1) - 65;
        if(getDirection(input) == 1)
        {
            if(column > 1 && board.get(row).get(column).getState() == CellState.PEG)
            {
                if(board.get(row).get(column - 2).getState() == CellState.EMPTY && board.get(row).get(column - 1).getState() == CellState.PEG)
                    res = true;
                else
                    res = false;
            }
            else
                res = false;
        }
        else if(getDirection(input) == 0)	//If direction is right.
        {
            if(column < (board.get(0).size() - 2) && board.get(row).get(column).getState() == CellState.PEG)
            {
                if(board.get(row).get(column + 2).getState() == CellState.EMPTY && board.get(row).get(column + 1).getState() == CellState.PEG)
                    res = true;
                else
                    res = false;
            }
            else
                res = false;
        }
        else if(getDirection(input) == 2)		//If direction is up.
        {
            if(row > 1 && board.get(row).get(column).getState() == CellState.PEG)
            {
                if(board.get(row - 2).get(column).getState() == CellState.EMPTY && board.get(row - 1).get(column).getState() == CellState.PEG)
                    res = true;
                else
                    res = false;
            }
            else
                res = false;
        }
        else if(getDirection(input) == 3)			//If direction is down.
        {
            if(row < (board.size() - 2) && board.get(row).get(column).getState() == CellState.PEG)
            {
                if(board.get(row + 2).get(column).getState() == CellState.EMPTY && board.get(row + 1).get(column).getState() == CellState.PEG)
                    res = true;
                else
                    res = false;
            }
            else
                res = false;
        }
        return res;
    }
    /**
     * This function determines the direction.
     * @param input is a movement.
     * @return the integer number for direction. (0:Right, 1:Left, 2:Up, 3:Down).
     */
    public int getDirection(String input)
    {
        int direction = -1;
        String[] directions = {"RIGHT", "LEFT", "UP", "DOWN"};
        if( ( (int)input.charAt(0) >= 48 + board.get(0).size() ) || (int)input.charAt(0) < 48)
            direction = -1;
        else if(input.charAt(2) != ' ')
            direction = -1;
        else if(!( ( (int)input.charAt(1) >= 65 && (int)input.charAt(1) < 65 + board.get(0).size() ) || ( (int)input.charAt(1) >= 97 && (int)input.charAt(1) < 97 + board.get(0).size() ) ))
            direction = -1;
        else
        {
            String sub = input.substring(3);
            for(int i = 0; i < directions.length; ++i)
                if(sub.equals(directions[i]))
                    direction = i;
        }
        return direction;
    }
    /**
     * This function moves right.
     * @param input is movement to move.
     */
    public void moveRight(String input)
    {
        int row, column;
        if(boardType < 6)
        {
            row = (int)input.charAt(0) - 48;
            if((int)input.charAt(1) >= 97)
                column = (int)input.charAt(1) - 97;
            else
                column = (int)input.charAt(1) - 65;
            board.get(row).get(column + 2).setState(CellState.PEG);
            board.get(row).get(column + 1).setState(CellState.EMPTY);
            board.get(row).get(column).setState(CellState.EMPTY);
        }
        else
        {
            row = (int)input.charAt(0) - 48;
            if(input.charAt(1) == 'A' || input.charAt(0) == 'a')
                column = 4 - row;
            else if(input.charAt(1) == 'B' || input.charAt(0) == 'b')
                column = 6 - row;
            else if(input.charAt(1) == 'C' || input.charAt(0) == 'c')
                column = 8 - row;
            else if(input.charAt(1) == 'D' || input.charAt(0) == 'd')
                column = 10 - row;
            else
                column = 12 - row;
            board.get(row).get(column + 4).setState(CellState.PEG);
            board.get(row).get(column + 2).setState(CellState.EMPTY);
            board.get(row).get(column).setState(CellState.EMPTY);
        }

    }
    /**
     * This function moves left.
     * @param input is movement to move.
     */
    public void moveLeft(String input)
    {
        int row, column;
        if(boardType < 6)
        {
            row = (int)input.charAt(0) - 48;
            if((int)input.charAt(1) >= 97)
                column = (int)input.charAt(1) - 97;
            else
                column = (int)input.charAt(1) - 65;
            board.get(row).get(column - 2).setState(CellState.PEG);
            board.get(row).get(column - 1).setState(CellState.EMPTY);
            board.get(row).get(column).setState(CellState.EMPTY);
        }
        else
        {
            row = (int)input.charAt(0) - 48;
            if(input.charAt(1) == 'A' || input.charAt(0) == 'a')
                column = 4 - row;
            else if(input.charAt(1) == 'B' || input.charAt(0) == 'b')
                column = 6 - row;
            else if(input.charAt(1) == 'C' || input.charAt(0) == 'c')
                column = 8 - row;
            else if(input.charAt(1) == 'D' || input.charAt(0) == 'd')
                column = 10 - row;
            else
                column = 12 - row;
            board.get(row).get(column - 4).setState(CellState.PEG);
            board.get(row).get(column - 2).setState(CellState.EMPTY);
            board.get(row).get(column).setState(CellState.EMPTY);
        }
    }
    /**
     * This function moves up.
     * @param input is movement to move.
     */
    public void moveUp(String input)
    {
        int row, column;
        if(boardType < 6)
        {
            row = (int)input.charAt(0) - 48;
            if((int)input.charAt(1) >= 97)
                column = (int)input.charAt(1) - 97;
            else
                column = (int)input.charAt(1) - 65;
            board.get(row - 2).get(column).setState(CellState.PEG);
            board.get(row - 1).get(column).setState(CellState.EMPTY);
            board.get(row).get(column).setState(CellState.EMPTY);
        }
        else
        {
            row = (int)input.charAt(0) - 48;
            if(input.charAt(1) == 'A' || input.charAt(0) == 'a')
                column = 4 - row;
            else if(input.charAt(1) == 'B' || input.charAt(0) == 'b')
                column = 6 - row;
            else if(input.charAt(1) == 'C' || input.charAt(0) == 'c')
                column = 8 - row;
            else if(input.charAt(1) == 'D' || input.charAt(0) == 'd')
                column = 10 - row;
            else
                column = 12 - row;
            if(board.get(row - 2).get(column - 2).getState() == CellState.EMPTY && board.get(row - 1).get(column - 1).getState() == CellState.PEG && board.get(row).get(column).getState() == CellState.PEG)   //Left top.
            {
                board.get(row - 2).get(column - 2).setState(CellState.PEG);
                board.get(row - 1).get(column - 1).setState(CellState.EMPTY);
                board.get(row).get(column).setState(CellState.EMPTY);
            }
            else   //Right top.
            {
                board.get(row - 2).get(column + 2).setState(CellState.PEG);
                board.get(row - 1).get(column + 1).setState(CellState.EMPTY);
                board.get(row).get(column).setState(CellState.EMPTY);
            }
        }
    }
    /**
     * This function moves down.
     * @param input is movement to move.
     */
    public void moveDown(String input)
    {
        int row, column;
        if(boardType < 6)
        {
            row = (int)input.charAt(0) - 48;
            if((int)input.charAt(1) >= 97)
                column = (int)input.charAt(1) - 97;
            else
                column = (int)input.charAt(1) - 65;
            board.get(row + 2).get(column).setState(CellState.PEG);
            board.get(row + 1).get(column).setState(CellState.EMPTY);
            board.get(row).get(column).setState(CellState.EMPTY);
        }
        else
        {
            row = (int)input.charAt(0) - 48;
            if(input.charAt(1) == 'A' || input.charAt(0) == 'a')
                column = 4 - row;
            else if(input.charAt(1) == 'B' || input.charAt(0) == 'b')
                column = 6 - row;
            else if(input.charAt(1) == 'C' || input.charAt(0) == 'c')
                column = 8 - row;
            else if(input.charAt(1) == 'D' || input.charAt(0) == 'd')
                column = 10 - row;
            else
                column = 12 - row;
            if(board.get(row + 2).get(column - 2).getState() == CellState.EMPTY && board.get(row + 1).get(column - 1).getState() == CellState.PEG && board.get(row).get(column).getState() == CellState.PEG)   //Left bottom.
            {
                board.get(row + 2).get(column - 2).setState(CellState.PEG);
                board.get(row + 1).get(column - 1).setState(CellState.EMPTY);
                board.get(row).get(column).setState(CellState.EMPTY);
            }
            else	//Right bottom.
            {
                board.get(row + 2).get(column + 2).setState(CellState.PEG);
                board.get(row + 1).get(column + 1).setState(CellState.EMPTY);
                board.get(row).get(column).setState(CellState.EMPTY);
            }
        }
    }
    /**
     * This function generates a move for computer.
     * @return string for computer move.
     */
    public String computerMove()
    {
        Random rand = new Random();
        int dirNumber = rand.nextInt(4);
        String randomMove = new String();
        randomMove += (char)(rand.nextInt(board.get(0).size()) + '1');
        randomMove += (char)(rand.nextInt(board.get(0).size()) + 'A');
        randomMove += ' ';
        if(dirNumber == 0)
            randomMove += "RIGHT";
        else if(dirNumber == 1)
            randomMove += "LEFT";
        else if(dirNumber == 2)
            randomMove += "UP";
        else
            randomMove += "DOWN";
        return randomMove;
    }
    /**
     * This function saves the game.
     * @param input is file name to save.
     */
    public void saveFile(String input)
    {
        try
        {
            FileWriter myWriter = new FileWriter(input);
            for(int i = 0; i < board.size(); ++i)
            {
                for(int j = 0; j < board.get(i).size(); ++j)
                {
                    if(board.get(i).get(j).getState() == CellState.WALL)
                        myWriter.write(" ");
                    else if(board.get(i).get(j).getState() == CellState.PEG)
                        myWriter.write("P");
                    else if(board.get(i).get(j).getState() == CellState.EMPTY)
                        myWriter.write(".");
                    else
                        myWriter.write("*");
                }
                myWriter.write("\n");
            }
            myWriter.close();
            JOptionPane.showMessageDialog(null, "The file is saved successfully!", "SAVE SUCCESSFULLY", JOptionPane.PLAIN_MESSAGE);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "The file could not be saved.!", "SAVE ERROR", JOptionPane.PLAIN_MESSAGE);
            e.printStackTrace();
        }
    }
    /**
     * This function plays one move for computer. 
     */
    public void playAuto()
    {
        String computerInput = new String();
        computerInput = computerMove();
        System.out.printf("Computer Move is: %s\n", computerInput);
        if(isValidMove(computerInput))
        {
            if(getDirection(computerInput) == 0)
                moveRight(computerInput);
            else if(getDirection(computerInput) == 1)
                moveLeft(computerInput);
            else if(getDirection(computerInput) == 2)
                moveUp(computerInput);
            else if(getDirection(computerInput) == 3)
                moveDown(computerInput);            
        }
        else
            System.err.println("Invalid!!!");
    }
    /**
     * This function plays for computer until game is over.
     */
    public void playAutoAll()
    {
        //print();
        while(!endGame())
        {
            playAuto();
        //    print();
        }
    }
    /**
     * This function calculates the score.
     * @return the number of pegs.
     */
    public int boardScore()
    {
        int pegCount = 0;
        for(int i = 0; i < board.size(); ++i)
            for(int j = 0; j < board.get(i).size(); ++j)
                if(board.get(i).get(j).getState() == CellState.PEG)
                    pegCount++;
        return pegCount;
    }
    /**
     * This function checks the game is over or not.
     * @return true if game is over, false otherwise.
     */
    public boolean endGame()
    {
        if(boardType < 6)
        {
            for(int i = 0; i < board.size(); ++i)
                for(int j = 0; j < board.get(0).size(); ++j)
                    if((j < (board.get(0).size()-2) && board.get(i).get(j).getState() == CellState.PEG && board.get(i).get(j + 1).getState() == CellState.PEG &&  board.get(i).get(j + 2).getState() == CellState.EMPTY) || (j > 1 && board.get(i).get(j).getState() == CellState.PEG && board.get(i).get(j - 1).getState() == CellState.PEG && board.get(i).get(j - 2).getState() == CellState.EMPTY) || 
                    (i < (board.size()-2) && board.get(i).get(j).getState() == CellState.PEG && board.get(i + 1).get(j).getState() == CellState.PEG && board.get(i + 2).get(j).getState() == CellState.EMPTY) ||(i > 1 && board.get(i).get(j).getState() == CellState.PEG && board.get(i - 1).get(j).getState() == CellState.PEG && board.get(i - 2).get(j).getState() == CellState.EMPTY))
                        return false;
            return true;    
        }
        else
        {
            for(int i = 0; i < (board.size()); i++)
                for(int j = 0; j < (board.get(0).size()); j++)
                    //Checks up, down, right and left.
                    if((j < (board.get(0).size()-2) && board.get(i).get(j).getState() == CellState.PEG && board.get(i).get(j + 2).getState() == CellState.PEG &&  board.get(i).get(j + 4).getState() == CellState.EMPTY) || (j > 2 && board.get(i).get(j).getState() == CellState.PEG && board.get(i).get(j - 2).getState() == CellState.PEG && board.get(i).get(j - 4).getState() == CellState.EMPTY )|| 
                    (i < (board.size()-2) && board.get(i).get(j).getState() == CellState.PEG && board.get(i + 1).get(j + 1).getState() == CellState.PEG && board.get(i + 2).get(j + 2).getState() == CellState.EMPTY) || (i < (board.size()-2) && board.get(i).get(j).getState() == CellState.PEG && board.get(i + 1).get(j - 1).getState() == CellState.PEG && board.get(i + 2).get(j - 2).getState() == CellState.EMPTY)
                    ||(i > 1 && board.get(i).get(j).getState() == CellState.PEG && board.get(i - 1).get(j - 1).getState() == CellState.PEG && board.get(i - 2).get(j - 2).getState() == CellState.EMPTY) || (i > 1 && board.get(i).get(j).getState() == CellState.PEG && board.get(i - 1).get(j + 1).getState() == CellState.PEG && board.get(i - 2).get(j + 2).getState() == CellState.EMPTY))
                        return false;
            return true;    
        }
    }
    /*
    public void loadFile(String input)
    {
        
        try(BufferedReader br = new BufferedReader(new FileReader(input)))
        {
            //BufferedReader br = new BufferedReader(new FileReader(input))
            String line; 
            int i = 0;
            while((line = br.readLine()) != null)
            {
                Vector<Cell> row = new Vector<Cell>();
                board.add(row);
                for(int j = 0; j < line.length(); ++j)
                {
                    if(line.charAt(j) == 'W')
                    {
                        Cell newCell = new Cell(i, j, CellState.WALL);
                        board.get(i).add(newCell);
                    }
                    else if(line.charAt(j) == 'P')
                    {
                        Cell newCell = new Cell(i, j, CellState.PEG);
                        board.get(i).add(newCell);
                    }
                    else if(line.charAt(j) == '.')
                    {
                        Cell newCell = new Cell(i, j, CellState.EMPTY);
                        board.get(i).add(newCell);
                    }
                    else if(line.charAt(j) == '*')
                    {
                        Cell newCell = new Cell(i, j, CellState.WALL);
                        board.get(i).add(newCell);
                    }    
                    
                }
                i += 1;
            }
        }
        catch(IOException e)
        {
            JOptionPane.showMessageDialog(null, "The file could not be loaded.!", "LOAD ERROR", JOptionPane.PLAIN_MESSAGE);
            e.printStackTrace();
        }
        
            
                
    }
    */
    
}