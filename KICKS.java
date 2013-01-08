// by Peter Li
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// v1.02 - 12/31/11
// - Implemented queued flood fill
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// v1.01 - 12/30/11
// - Bug fixes
// - Efficiency improvements
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// v1.0 - 12/29/11
// - Initial Release
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

//LEGEND
//~~~~~~
//left = 1
//top/up = 2
//right = 3
//bottom/down = 4
//...x1 = left
//...x2 = right
//...y1 = top/up
//...y2 = bottom/down

import java.awt.*;
import java.awt.font.*;
import java.text.*;
import java.util.*;
import java.io.*;

public class KICKS
{
    public static PCPC c;                                               //PCPC console object
    public static Graphics g;                                           //graphics object
    public static int cWidth, cHeight;                                  //canvas size

    //general
    public static int frameLimit = 60;                                  //frames per second
    public static double frameDelay = 1000 / frameLimit;                //delay per frame
    public static int lives;                                            //amount of lives
    public static int score;                                            //total score
    public static int roundScore;                                       //score of round
    public static double speed = 1;                                     //player speed
    public static double area;                                          //enemy area #
    public static boolean started;                                      //scanline area filling
    public static int start = 0;                                        //^
    public static int end = 0;                                          //^
    public static double playerArea;                                    //occupied area #
    public static double percentArea;                                   //occupied area %
    public static boolean boundary;                                     //on boundary status
    public static int totalArea = 196511;                               //total area constant
    public static int horizontal = 1;                                   //directional constants
    public static int vertical = 2;                                     //^
    public static boolean CArea[] [] = new boolean [560] [560];         //checked/occupied area
    public static boolean Boundary[] [] = new boolean [560] [560];      //game boundary
    public static boolean onWall;                                       //on wall status
    public static boolean onWallH;                                      //on horizontal wall status
    public static boolean onWallV;                                      //on vertical wall status
    public static boolean mLineVert, mLineHori;                         //making line status
    public static boolean GAMEOVER, NEWROUND;                           //loop control vars

    //objects
    public static double playerX, playerY;                              //player coords
    public static int lastX, lastY;                                     //last onWall position
    public static double stixX;                                         //enemy coords
    public static double stixY;                                         //^
    public static int stixR;                                            //enemy radius
    public static int stixRmax;                                         //enemy max radius
    public static double stixDir;                                       //enemy direction
    public static boolean grow;                                         //enemy radius control
    public static int walls;                                            //total wall number
    public static int twall;                                            //temporary wall
    public static int startWall;                                        //starting wall
    public static int wallx1[] = new int [1337];                        //wall coords
    public static int wally1[] = new int [1337];                        //^
    public static int wallx2[] = new int [1337];                        //^
    public static int wally2[] = new int [1337];                        //^
    public static int wallLength[] = new int [1337];                    //wall length
    public static int wallDir[] = new int [1337];                       //wall direction

    //text
    public static Font qixFont;                                         //primary qix font
    public static Font sqixFont;                                        //secondary qix font
    public static AttributedString as, as2;                             //font style
    public static Color RED = new Color (144, 32, 16);                  //qix red
    public static Color BLUE = new Color (0, 127, 127);                 //qix blue
    public static Color YELLOW = new Color (239, 239, 74);              //Yellow color
    public static int l;                                                //text length

    //timers
    public static long loopTimer;                                       //main loop
    public static long frameTimer;                                      //frames per second
    public static long now;                                             //comparison var

    //input
    public static int keyCode, keyCodeR;                                //key press/release var
    public static boolean leftArrow, upArrow, rightArrow, downArrow;    //directional keys
    public static boolean fast;                                         //wall creation
    public static int mX, mY;                                           //mouse coords
    public static boolean mClick;                                       //mouse click

    //leaderboard
    public static String name;                                          //name
    public static String names[] = new String [7];                      //name leader list
    public static int scores[] = new int [7];                           //score leader list
    public static String tScore;                                        //comparison var
    public static boolean swap;                                         //sort decision var

    public static void main (String[] args) throws Exception
    {
	c = new PCPC (700, 630);
	g = c.getGraphics ();

	//border initializer
	//top wall
	wallx1 [1] = 10;
	wallx2 [1] = 430;
	wally1 [1] = 80;
	wally2 [1] = 80;
	wallLength [1] = 420;
	wallDir [1] = horizontal;
	//bottom wall
	wallx1 [2] = 10;
	wallx2 [2] = 430;
	wally1 [2] = 550;
	wally2 [2] = 550;
	wallLength [2] = 420;
	wallDir [2] = horizontal;
	//left wall
	wallx1 [3] = 10;
	wallx2 [3] = 10;
	wally1 [3] = 80;
	wally2 [3] = 550;
	wallLength [3] = 470;
	wallDir [3] = vertical;
	//right wall
	wallx1 [4] = 430;
	wallx2 [4] = 430;
	wally1 [4] = 80;
	wally2 [4] = 550;
	wallLength [4] = 470;
	wallDir [4] = vertical;

	qixFont = Font.createFont (Font.PLAIN, new FileInputStream (new File ("qix.ttf")));
	qixFont = qixFont.deriveFont (Font.PLAIN, 90);
	as = new AttributedString ("qix");
	as.addAttribute (TextAttribute.FONT, qixFont);

	cWidth = c.getCanvasWidth ();
	cHeight = c.getCanvasHeight ();

	System.out.println ("by Peter Li");
	System.out.println ("=============================");
	System.out.println ("INSTRUCTIONS:");
	System.out.println ("=============================");
	System.out.println ("Arrow keys to move");
	System.out.println ("Z key to create");

	menu ();
    }


    //right justifies string
    public static void drawStringRight (String s, int x, int y)
    {
	l = s.length ();
	x -= l * 19;
	g.drawString (s, x, y);
    }


    public static void generateWall (int x1, int y1, int x2, int y2)
    {
	//sorts according to legend
	int tx, ty;

	if (y1 == y2)
	{
	    if (x1 > x2)
	    {
		tx = x1;
		x1 = x2;
		x2 = tx;

		ty = y1;
		y1 = y2;
		y2 = ty;
	    }
	}
	else if (x1 == x2)
	{
	    if (y1 > y2)
	    {
		tx = x1;
		x1 = x2;
		x2 = tx;

		ty = y1;
		y1 = y2;
		y2 = ty;
	    }
	}

	wallx1 [walls] = x1;
	wallx2 [walls] = x2;
	wally1 [walls] = y1;
	wally2 [walls] = y2;

	//directional properties
	if (wallx1 [walls] == wallx2 [walls])                       //vertical
	{
	    wallDir [walls] = vertical;
	    wallLength [walls] = wally2 [walls] - wally1 [walls];
	}
	else                                                        //horizontal
	{
	    wallDir [walls] = horizontal;
	    wallLength [walls] = wallx2 [walls] - wallx1 [walls];
	}
    }


    public static void reset ()
    {
	//new game reset variable defaults
	lives = 3;
	score = 1337;
	roundScore = 0;
	GAMEOVER = false;
    }


    public static void roundReset ()
    {
	//round reset variable defaults
	NEWROUND = false;
	stixX = 220;
	stixY = 300;
	stixR = 50;
	grow = true;
	stixDir = Math.toRadians (c.randInt (360));                 //randomizes enemy direction
	playerX = 220;
	playerY = 550;
	walls = 4;
	score += roundScore;                                        //adds on to score
	onWall = true;
	mLineVert = false;
	mLineHori = false;
	leftArrow = false;
	rightArrow = false;
	upArrow = false;
	downArrow = false;
	calcReset ();
	calcArea ((int) stixX, (int) stixY);
	frameTimer = System.currentTimeMillis ();
	loopTimer = System.currentTimeMillis ();
    }


    public static void setBackground ()
    {
	//background
	g.setColor (Color.BLACK);
	g.fillRect (0, 0, cWidth, cHeight);
	g.setColor (Color.WHITE);
	g.drawRect (10, 80, 420, 470);

	playerArea = totalArea - area;
	percentArea = playerArea / totalArea * 100;
	roundScore = (int) playerArea / 10;

	//sets font and text
	g.setFont (qixFont);
	g.drawString (as.getIterator (), 20, 50);

	qixFont = qixFont.deriveFont (Font.PLAIN, 30);
	drawStringRight (Integer.toString (score + roundScore), 415, 30);

	//area claimed
	g.setColor (YELLOW);
	g.drawString ("CLAIMED", 180, 40);

	sqixFont = qixFont.deriveFont (Font.PLAIN, 24);
	g.setFont (sqixFont);

	g.drawString ((int) percentArea + "%", 180, 60);
	g.drawString ("75%", 240, 60);

	//displays lives
	g.setColor (RED);
	g.fillRect (420, 15, 5, 5);
	g.fillRect (420, 25, 5, 5);
	g.fillRect (420, 35, 5, 5);
	g.setColor (Color.GRAY);
	g.fillRect (420, 45 - lives * 10, 5, 5);
    }


    public static void drawPlayer ()
    {
	//draws player
	g.setColor (Color.RED);
	g.drawRect ((int) playerX - 2, (int) playerY - 2, 4, 4);
    }


    public static void drawEnemy ()
    {
	if (grow)                                                           //increase radius
	{
	    stixR++;
	}
	else                                                                //decrease radius
	{
	    stixR--;
	}

	if (stixR <= 50 || stixR >= stixRmax)
	{
	    stixRmax = c.randInt (200);                                     //new radius

	    if (grow)
	    {
		grow = false;
	    }
	    else
	    {
		grow = true;
	    }
	}

	//enemy movement
	stixX += 3 * Math.cos (stixDir);
	stixY += 3 * Math.sin (stixDir);
	g.drawOval ((int) stixX - stixR / 2, (int) stixY - stixR / 2, stixR, stixR);
    }


    public static void drawWalls ()
    {
	//all walls
	g.setColor (Color.WHITE);
	for (int i = 1 ; i <= walls ; i++)
	{
	    g.drawLine (wallx1 [i], wally1 [i], wallx2 [i], wally2 [i]);
	}

	//line to player
	if (!onWall)
	{
	    if (fast)
	    {
		g.drawLine ((int) playerX, (int) playerY, wallx1 [twall], wally1 [twall]);
	    }
	}
    }


    public static void drawArea ()
    {
	//scanline polygon area filling
	g.setColor (BLUE);
	for (int y = 81 ; y < 550 ; y++)
	{
	    started = false;
	    for (int x = 11 ; x <= 430 ; x++)
	    {
		if (!CArea [x] [y] && !started)
		{
		    started = true;
		    start = x;
		}
		else if (CArea [x] [y] && started || started && x == 430)
		{
		    started = false;
		    end = x - 1;
		    g.drawLine (start, y, end, y);
		}
	    }
	}
    }


    public static void respawn () throws Exception
    {
	//respawns at last known location
	playerX = lastX;
	playerY = lastY;
	walls = startWall;
	wallCollision ();
	leftArrow = false;
	rightArrow = false;
	upArrow = false;
	downArrow = false;
	c.delay (500);
    }


    public static void death () throws Exception
    {
	//death sequence
	lives--;

	if (lives == 0)
	{
	    gameOver ();
	}
	else
	{
	    respawn ();
	}
    }


    public static void calcReset ()
    {
	//resets everything to false
	for (int y = 79 ; y <= 551 ; y++)
	{
	    for (int x = 9 ; x <= 431 ; x++)
	    {
		CArea [x] [y] = false;
	    }
	}

	for (int y = 80 ; y <= 550 ; y++)
	{
	    for (int x = 10 ; x <= 430 ; x++)
	    {
		Boundary [x] [y] = false;
	    }
	}

	area = 0;
    }


    //calculates created area
    //queue implemented flood fill
    public static void calcArea (int xx, int yy)
    {
	LinkedList listx = new LinkedList ();
	LinkedList listy = new LinkedList ();
	int x, y;

	listx.add (new Integer (xx));
	listy.add (new Integer (yy));

	do
	{
	    x = listx.remove (0).hashCode ();
	    y = listy.remove (0).hashCode ();

	    if (!CArea [x] [y])
	    {
		boundary = false;

		//if point is on wall
		for (int i = 1 ; i <= walls ; i++)
		{
		    if (x == wallx1 [i] && y >= wally1 [i] && y <= wally2 [i])  //vertical
		    {
			boundary = true;
			Boundary [x] [y] = true;
			break;
		    }
		    else if (y == wally1 [i] && x >= wallx1 [i] && x <= wallx2 [i]) //horizontal
		    {
			boundary = true;
			Boundary [x] [y] = true;
			break;
		    }
		}

		if (!boundary)                                                  //if not at border
		{
		    CArea [x] [y] = true;
		    area++;

		    if (!CArea [x + 1] [y])                                     //checks right side
		    {
			listx.addFirst (new Integer (x + 1));
			listy.addFirst (new Integer (y));
		    }

		    if (!CArea [x - 1] [y])                                     //left side
		    {
			listx.addFirst (new Integer (x - 1));
			listy.addFirst (new Integer (y));
		    }

		    if (!CArea [x] [y + 1])                                     //below
		    {
			listx.addFirst (new Integer (x));
			listy.addFirst (new Integer (y + 1));
		    }

		    if (!CArea [x] [y - 1])                                     //above
		    {
			listx.addFirst (new Integer (x));
			listy.addFirst (new Integer (y - 1));
		    }
		}
	    }
	}
	while (listx.size () != 0);
    }


    //calculates distance
    public static double dist (int x1, int y1, int x2, int y2)
    {
	double distance = Math.sqrt ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

	return distance;
    }


    public static void checkArea () throws Exception
    {
	//checks if group has been completed
	if (onWall)
	{
	    if (mLineHori || mLineVert)
	    {
		mLineHori = false;
		mLineVert = false;

		wallx2 [twall] = (int) playerX;
		wally2 [twall] = (int) playerY;
		walls++;

		generateWall (wallx1 [walls], wally1 [walls], wallx2 [walls], wally2 [walls]);
		calcReset ();
		calcArea ((int) stixX, (int) stixY);
	    }
	}

	//checks for win
	if (percentArea > 75)
	{
	    NEWROUND = true;
	}
    }


    //collision checking
    public static void checkCollision () throws Exception
    {
	wallCollision ();
	tempWallCollision ();
	enemyCollision ();
	enemyWallCollision ();
    }


    public static void enemyWallCollision ()
    {
	//bounces enemy if collision with wall
	if (!CArea [(int) stixX] [(int) stixY])
	{
	    stixX -= 3 * Math.cos (stixDir);
	    stixY -= 3 * Math.sin (stixDir);
	    stixDir = Math.toRadians (c.randInt (360));
	}
    }


    public static void enemyCollision () throws Exception
    {
	//if player is within radius of stix
	if (dist ((int) playerX, (int) playerY, (int) stixX, (int) stixY) < stixR / 2 && !onWall)
	{
	    death ();                                               //go to death sequence
	}
    }


    public static void tempWallCollision ()
    {
	//prevents going backwards
	//if backwards then push back in opposite direction
	if (mLineHori)                                              //for horizontal
	{
	    if (playerX > wallx1 [twall] && leftArrow)
	    {
		playerX++;
	    }
	    else if (playerX < wallx1 [twall] && rightArrow)
	    {
		playerX--;
	    }
	}
	else if (mLineVert)                                         //for vertical
	{
	    if (playerY > wally1 [twall] && upArrow)
	    {
		playerY++;
	    }
	    else if (playerY < wally1 [twall] && downArrow)
	    {
		playerY--;
	    }
	}
    }


    public static void wallCollision ()
    {
	//wall collision
	onWallH = false;
	onWallV = false;
	onWall = false;

	//pushes player in opposite direction if collision with wall
	for (int i = 1 ; i <= walls ; i++)
	{
	    if (wallDir [i] == vertical)
	    {
		if (playerX == wallx1 [i] && playerY >= wally1 [i] && playerY <= wally2 [i])
		{
		    playerX = wallx1 [i];
		    onWall = true;
		    onWallV = true;
		    fast = false;
		    break;
		}
	    }
	    if (wallDir [i] == horizontal)
	    {
		if (playerY == wally1 [i] && playerX >= wallx1 [i] && playerX <= wallx2 [i])
		{
		    playerY = wally1 [i];
		    onWall = true;
		    onWallH = true;
		    fast = false;
		    break;
		}
	    }
	}
    }


    public static void keyCheck ()
    {
	keyCode = c.getKey ();
	keyCodeR = c.getKeyR ();

	//on release
	if (keyCodeR == 37)
	{
	    leftArrow = false;
	}
	else if (keyCodeR == 38)
	{
	    upArrow = false;
	}
	else if (keyCodeR == 39)
	{
	    rightArrow = false;
	}
	else if (keyCodeR == 40)
	{
	    downArrow = false;
	}

	//on press
	if (keyCode == 37)                                              //left
	{
	    leftArrow = true;
	    upArrow = false;
	    rightArrow = false;
	    downArrow = false;
	}
	else if (keyCode == 38)                                         //up
	{
	    leftArrow = false;
	    upArrow = true;
	    rightArrow = false;
	    downArrow = false;
	}
	else if (keyCode == 39)                                         //right
	{
	    leftArrow = false;
	    upArrow = false;
	    rightArrow = true;
	    downArrow = false;
	}
	else if (keyCode == 40)                                         //down
	{
	    leftArrow = false;
	    upArrow = false;
	    rightArrow = false;
	    downArrow = true;
	}

	//on press
	if (keyCode == 90 && onWall)
	{
	    if (onWallH)
	    {
		//allows perpendicular movement if z is pressed
		if (downArrow && CArea [(int) playerX] [(int) playerY + 1] || upArrow && CArea [(int) playerX] [(int) playerY - 1])
		{
		    twall = walls + 1;
		    wallx1 [twall] = (int) playerX;
		    wally1 [twall] = (int) playerY;
		    lastX = (int) playerX;
		    lastY = (int) playerY;
		    mLineVert = true;
		    startWall = walls - 1;
		    fast = true;
		}
	    }

	    if (onWallV)
	    {
		if (leftArrow && CArea [(int) playerX - 1] [(int) playerY] || rightArrow && CArea [(int) playerX + 1] [(int) playerY])
		{
		    twall = walls + 1;
		    wallx1 [twall] = (int) playerX;
		    wally1 [twall] = (int) playerY;
		    lastX = (int) playerX;
		    lastY = (int) playerY;
		    mLineHori = true;
		    startWall = walls - 1;
		    fast = true;
		}
	    }
	}

	//on release
	if (keyCodeR == 90 && onWall)
	{
	    fast = false;
	}
    }


    public static void turnCheck ()
    {
	//if player turns in creation of a wall, then generate wall2
	if (mLineVert)                                              //vertical creation
	{
	    if (leftArrow || rightArrow)
	    {
		wallx2 [twall] = (int) playerX;
		wally2 [twall] = (int) playerY;
		walls++;

		twall = walls + 1;
		wallx1 [twall] = wallx2 [walls];
		wally1 [twall] = wally2 [walls];

		generateWall (wallx1 [walls], wally1 [walls], wallx2 [walls], wally2 [walls]);

		mLineVert = false;
		mLineHori = true;
	    }
	}
	else if (mLineHori)                                         //horizontal creation
	{
	    if (upArrow || downArrow)
	    {
		wallx2 [twall] = (int) playerX;
		wally2 [twall] = (int) playerY;
		walls++;

		twall = walls + 1;
		wallx1 [twall] = wallx2 [walls];
		wally1 [twall] = wally2 [walls];

		generateWall (wallx1 [walls], wally1 [walls], wallx2 [walls], wally2 [walls]);

		mLineHori = false;
		mLineVert = true;
	    }
	}
    }


    public static void getInput ()
    {
	keyCheck ();                                                //checks for key press
	turnCheck ();                                               //checks if wall is created

	//moves according to key press
	if (fast)
	{
	    if (leftArrow)
	    {
		playerX -= speed;
	    }
	    else if (upArrow)
	    {
		playerY -= speed;
	    }
	    else if (rightArrow)
	    {
		playerX += speed;
	    }
	    else if (downArrow)
	    {
		playerY += speed;
	    }
	}
	else
	{
	    if (leftArrow)
	    {
		//checks if player is heading towards a point on boundary
		if (Boundary [(int) playerX - 1] [(int) playerY] || Boundary [(int) playerX - 1] [(int) playerY + 1] || Boundary [(int) playerX - 1] [(int) playerY - 1])
		{
		    playerX -= speed;
		}
	    }
	    else if (upArrow)
	    {
		if (Boundary [(int) playerX - 1] [(int) playerY - 1] || Boundary [(int) playerX] [(int) playerY - 1] || Boundary [(int) playerX + 1] [(int) playerY - 1])
		{
		    playerY -= speed;
		}
	    }
	    else if (rightArrow)
	    {
		if (Boundary [(int) playerX + 1] [(int) playerY] || Boundary [(int) playerX + 1] [(int) playerY + 1] || Boundary [(int) playerX + 1] [(int) playerY - 1])
		{
		    playerX += speed;
		}
	    }
	    else if (downArrow)
	    {
		if (Boundary [(int) playerX - 1] [(int) playerY + 1] || Boundary [(int) playerX] [(int) playerY + 1] || Boundary [(int) playerX + 1] [(int) playerY + 1])
		{
		    playerY += speed;
		}
	    }
	}
    }


    public static void gameOver () throws Exception
    {
	sqixFont = new Font ("qix are for kids - large - solid", Font.PLAIN, 30);
	g.setFont (sqixFont);

	GAMEOVER = true;
	NEWROUND = true;

	//waits for name input
	while (true)
	{
	    name = c.getText ();
	    g.setColor (Color.BLACK);
	    g.fillRect (0, 0, cWidth, cHeight);
	    g.setColor (Color.WHITE);
	    g.drawString ("ENTER YOUR NAME", 120, 220);
	    g.drawString (name, cWidth / 2 - name.length () * 7, 250);
	    c.ViewUpdate ();

	    if (c.getKey () == 10)
	    {
		break;
	    }
	}

	//gets current leaderboard values
	for (int i = 1 ; i <= 5 ; i++)
	{
	    names [i] = c.readFile ("KICKS Leaderboard.kf", i * 2 - 1);
	    tScore = c.readFile ("KICKS Leaderboard.kf", i * 2);
	    scores [i] = Integer.parseInt (tScore);
	}

	names [6] = name;
	scores [6] = score + roundScore;

	//bubblesort
	for (int x = 1 ; x <= 5 ; x++)
	{
	    swap = false;
	    for (int y = 1 ; y <= 6 - x ; y++)
	    {
		if (scores [y] < scores [y + 1])
		{
		    scores [0] = scores [y];
		    names [0] = names [y];
		    scores [y] = scores [y + 1];
		    names [y] = names [y + 1];
		    scores [y + 1] = scores [0];
		    names [y + 1] = names [0];
		    swap = true;
		}
	    }
	    if (swap == false)
	    {
		break;
	    }
	}

	//outputs updated leaderboard
	c.writeFile ("", "KICKS Leaderboard.kf");                  //clears file
	for (int i = 1 ; i <= 5 ; i++)
	{
	    c.appendFile (names [i], "KICKS Leaderboard.kf");
	    c.appendFile (Integer.toString (scores [i]), "KICKS Leaderboard.kf");
	}
    }


    public static void leaderboard () throws Exception
    {
	//parses score information
	for (int i = 1 ; i <= 5 ; i++)
	{
	    names [i] = c.readFile ("KICKS Leaderboard.kf", i * 2 - 1);
	    tScore = c.readFile ("KICKS Leaderboard.kf", i * 2);
	    scores [i] = Integer.parseInt (tScore);
	}

	while (true)
	{
	    mX = c.getMouseX ();
	    mY = c.getMouseY ();
	    mClick = c.getClick ();
	    g.setColor (Color.BLACK);
	    g.fillRect (0, 0, cWidth, cHeight);
	    g.setColor (Color.WHITE);

	    for (int i = 1 ; i <= 5 ; i++)                                                      //displays scores
	    {
		g.drawString (names [i], 30, 20 * i + 30);
		g.drawString (Integer.toString (scores [i]), 300, 20 * i + 30);
	    }

	    if (mX > 30 && mX < 90 && mY > 500 && mY < 520)                                     //back button
	    {
		g.setColor (Color.YELLOW);
		g.drawString ("BACK", 30, 520);
		if (mClick)
		{
		    mClick = false;
		    break;
		}
	    }
	    else
	    {
		g.setColor (Color.WHITE);
		g.drawString ("BACK", 30, 520);
	    }

	    c.ViewUpdate ();
	}
    }


    public static void menu () throws Exception
    {
	while (true)
	{
	    //constantly receives mouse coordinates
	    mX = c.getMouseX ();
	    mY = c.getMouseY ();
	    mClick = c.getClick ();

	    g.setColor (Color.BLACK);
	    g.fillRect (0, 0, cWidth, cHeight);

	    g.setColor (Color.WHITE);
	    sqixFont = qixFont.deriveFont (Font.PLAIN, 150);

	    as2 = new AttributedString ("qix");
	    as2.addAttribute (TextAttribute.FONT, sqixFont);
	    g.drawString (as2.getIterator (), 110, 150);

	    sqixFont = qixFont.deriveFont (Font.PLAIN, 30);
	    g.setFont (sqixFont);

	    //button creation
	    if (mX > 130 && mX < 200 && mY > 230 && mY < 250)
	    {
		g.setColor (Color.YELLOW);
		g.drawString ("START", 130, 250);
		if (mClick)
		{
		    mClick = false;
		    start ();
		}
	    }
	    else
	    {
		g.setColor (Color.WHITE);
		g.drawString ("START", 130, 250);
	    }

	    if (mX > 130 && mX < 295 && mY > 355 && mY < 375)
	    {
		g.setColor (Color.YELLOW);
		g.drawString ("LEADERBOARD", 130, 375);

		if (mClick)
		{
		    mClick = false;
		    leaderboard ();
		}
	    }
	    else
	    {
		g.setColor (Color.WHITE);
		g.drawString ("LEADERBOARD", 130, 375);
	    }

	    c.ViewUpdate ();
	}
    }


    //main game loop
    public static void start () throws Exception
    {
	reset ();
	while (!GAMEOVER)
	{
	    roundReset ();
	    while (!NEWROUND)
	    {
		now = System.currentTimeMillis ();
		if (now - loopTimer > 10)
		{
		    loopTimer = now;

		    setBackground ();                                   //draws background colours, stats, etc.
		    drawArea ();                                        //draws area
		    drawPlayer ();                                      //draws player
		    drawEnemy ();                                       //draws sparx and stix
		    drawWalls ();                                       //draws walls
		    getInput ();                                        //input for controls, movement, etc.
		    checkCollision ();                                  //checks for collision with walls, enemies, etc.
		    checkArea ();                                       //checks if area has been completed

		    now = System.currentTimeMillis ();
		    if (now - frameTimer > frameDelay)                  //fps limiter
		    {
			frameTimer = now;
			c.ViewUpdate ();                                //updates canvas
		    }
		}
	    }
	    if (GAMEOVER)
	    {
		break;
	    }
	}
    }
}
