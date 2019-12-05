package com.michaelzapata.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

	//JAVA random object
	Random random = new Random();

	/** Textures are used TO ADD any sort of VISUAL imgage/asset - MADE WITH COLORS OR IMAGES
	 *  SpriteBatch - PUT SOMETHING ON THE SCREEN
	 *
	 *  BitmapFont -> Display text on Screen
	 *  Rectangle -> Object that will contain an image. -> X-axisPosition, Y-axis Position, width, height
	 *  Intersector.overlaps(obj, obj)//Checks if two objects, eg: Rectangle, overlap
	 */

	int gameState = 0;//1: alive, 0: Waiting to Start, anything else: Game over
	int lifes = 1;
	int collissionsCounter = 0;
	int score = 0;
	int scoringTube = 0;//Tube that is passed

	//Display text on Screen:
	BitmapFont font;

	SpriteBatch batch;
	Texture background;
	Texture gameOver;

	//BIRD
	Texture[] birdArray;
	int flapState = 0;
	int iterationCounter = 0;
	int changeFlapStateAt = 8;

	//define how QUICKLY bird will fall -> GRAVITY
	float gravity = 0.8f;
	//define the fallVelocity at which the man will fall;
	float fallVelocity = 0;
	//Represents where the man is going to be in the SCREEN - Y axis
	float birdYPosition;

	//SHAPES==========================================
	ShapeRenderer shapeRenderer;
	Circle birdCircle;
	Rectangle tubeRectangle;

	Rectangle[] topTubeRectangles;
	Rectangle[] bottomTubeRectangles;


	//=============================================================
	float midOfScreenY;
	//Velocity at which tubes move from RIGHT to left
	int xAxisVelocityMovement = 10;
	int maxNumberOfTubes = 6;
	float distanceBetweenTubes;
	int gap = 600;
	Texture topTube;
	ArrayList<Float> topTubeXPositions = new ArrayList<>();
	ArrayList<Float> topTubeYPositions = new ArrayList<>();
	Texture bottomTube;
	ArrayList<Float> bottomTubeXPositions = new ArrayList<>();
	ArrayList<Float> bottomTubeYPositions = new ArrayList<>();

	private void makeOnePairOfTopAndBottomTubes(float initXPosition){
		topTubeXPositions.add(initXPosition);
		bottomTubeXPositions.add(initXPosition);

		topTubeYPositions.add( midOfScreenY + gap/2 + randomTubeOffset() );
		bottomTubeYPositions.add(midOfScreenY - bottomTube.getHeight() - gap/2 + randomTubeOffset());
	}

	private float randomTubeOffset(){

		float midOfScreenY = Gdx.graphics.getHeight() / 2;

		//maximum amount it can move up and down
		float maxTubeOffset = midOfScreenY + gap;

		//random 0.5 and -0.5
		float randomNegPosHalf = (random.nextFloat() - 0.5f);
		//Random between One Half of the screen and the other half Y
		//Will shift up or down, depending on the offset.
		float tubeOffset = randomNegPosHalf * (Gdx.graphics.getHeight() - maxTubeOffset);
		return tubeOffset;

	}



	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("bg.png");
		gameOver = new Texture("gameover.png");

		birdArray = new Texture[2];
		birdArray[0] = new Texture("bird.png");
		birdArray[1] = new Texture("bird2.png");

		//init pipes
		topTube = new Texture("toptube.png");
		bottomTube = new Texture("bottomtube.png");

		midOfScreenY = Gdx.graphics.getHeight() / 2;


		//SHAPES----------------------------------------------------------------
		shapeRenderer = new ShapeRenderer();
		birdCircle = new Circle();
		tubeRectangle = new Rectangle();

		//------------------------------------------------------------------------

		startGame();

		//Make text on screen
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(5);//sets font SIZE
	}

	@Override
	public void render () {

		//1) we need to begin the SpriteBatch
		batch.begin();

		//put image background, startingPosition X, startingPosition Y, Image width, Image height,
		//Gdx.graphics.getHeight() -> get the height of the SCREEN
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		//---------------------------------------------------------------------------------------------------------
		float birdWidth = birdArray[flapState].getWidth();
		float birdHeight = birdArray[flapState].getHeight();
		float birdX = Gdx.graphics.getWidth() / 2 - birdWidth;

		if(gameState == 2){
			//GAME OVER
			batch.draw(gameOver, Gdx.graphics.getWidth() / 2 - gameOver.getWidth() / 2, Gdx.graphics.getHeight() / 2 - gameOver.getHeight() / 2);
		} else {
			batch.draw(birdArray[flapState], birdX, birdYPosition, birdWidth, birdHeight);
			birdCircle.set(Gdx.graphics.getWidth() / 2 - birdArray[flapState].getWidth()/2, birdYPosition + birdArray[flapState].getHeight()/2 ,  birdArray[flapState].getWidth()/2);
		}
//---------------------------------------------------------------------------------------------------------
		if(gameState == 1){
			//GAME IS ALIVE
			gameAlive();
		} else if(gameState == 0){
			//GAME IS WAITING TO START
			//If user touches the screen start game
			//add text
			font.draw(batch, "Touch to Start Game", 180, birdYPosition - 50);

			if(Gdx.input.justTouched()){
				Gdx.app.log("justTouched", "waiting touched, starting");
				gameState = 1;
			}
		} else {
			//GAME OVER
			//Reset game, Go back to Starting position:
			if(Gdx.input.justTouched()){
				resetGame();
			}

		}
//---------------------------------------------------------------------------------------------------------

		batch.end();


		//SHAPES RENDERER=====================================================================
//		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//		shapeRenderer.setColor(Color.RED);
//		//Center of circle X, Center of circle Y , Radius
//		//birdCircle.set(Gdx.graphics.getWidth() / 2 - birdArray[flapState].getWidth()/2, birdYPosition + birdArray[flapState].getHeight()/2 ,  birdArray[flapState].getWidth()/2);
//		shapeRenderer.circle(birdCircle.x, birdCircle.y, birdCircle.radius); //From birdCircle above
//
//		//for Rectangle arr
//		for (int i = 0; i < topTubeXPositions.size(); i++) {
//			//X-axis Position of the tubes
//			float currentTopTubeXPosition = topTubeXPositions.get(i);
//			float currentBottomTubeXPosition = bottomTubeXPositions.get(i);
//
//			shapeRenderer.rect(currentTopTubeXPosition, topTubeYPositions.get(i),topTube.getWidth(), topTube.getHeight());
//			shapeRenderer.rect(currentBottomTubeXPosition, bottomTubeYPositions.get(i),bottomTube.getWidth(), bottomTube.getHeight());
//		}
//		shapeRenderer.end();
		//SHAPES RENDERER END=====================================================================

	}

	private void startGame(){

        topTubeRectangles = new Rectangle[maxNumberOfTubes];
        bottomTubeRectangles = new Rectangle[maxNumberOfTubes];

		//init initial manYPosition
		birdYPosition = Gdx.graphics.getHeight() / 2;//middle

		//Make maxNumberOfTubes with initial position OFF screen , they should later move steadily to the left------------------------------------------------------------------------
		distanceBetweenTubes = Gdx.graphics.getWidth() * 3 / 4;
		for (int i = 1; i <= maxNumberOfTubes; i++) {
			float initXPosition = Gdx.graphics.getWidth() / 2 - topTube.getWidth() / 2 + i * distanceBetweenTubes;
			makeOnePairOfTopAndBottomTubes(initXPosition);
		}

	}

	private void resetGame(){
		gameState = 1;
		collissionsCounter = 0;
		score = 0;
		scoringTube = 0;
		fallVelocity = 0;
		topTubeXPositions.clear();
		topTubeYPositions.clear();
		bottomTubeXPositions.clear();
		bottomTubeYPositions.clear();
		startGame();


	}

	private void gameAlive() {
		iterationCounter++;

		//RECOGNIZE, in the loop, at which moment USER touched the screen.
		// Gdx.input.justTouched() -> will be true, in this iteration, if user touched the screen,
		// AND it will be false, in the next iteration, if user doesn't touch the screen.(true/false) state is managed by Gdx.
		if(Gdx.input.justTouched()){
			fallVelocity = -20;// bird will jump;EVERY TIME you touch the screen.
		}

		//--------------------------------------------------------------
		// initial fallVelocity will be 0;
		//fallVelocity = current fallVelocity + gravity;
		// The bigger the gravity, the faster it will fall
		fallVelocity = fallVelocity + gravity;

		//Position of man in the Y axis = current Y position - fallVelocity
		//The bigger the fall velocity -> the lower in the Y axis the man will be
		birdYPosition = birdYPosition - fallVelocity;

		//if man is at the bottom of the SCREEN (0) or Below
		// then -> keep him at position 0 (bottom of the screen)
		// REMEMBER. the bottomLeftCorner point will be the (0,0) position of the IMAGE man(not screen)
		// Therefore, this will keep the man running at the bottom of the screen.(THE whole body will be visible).
		//if man is at the bottom of the SCREEN (0) or Below
		if(birdYPosition <= 0){
			birdYPosition = 0; //keep him there.
		}

		//if man is at the top of the SCREEN (POSITION: Gdx.graphics.getHeight()) or HIGHER
		// then -> keep him at position Gdx.graphics.getHeight() (TOP of the screen)
		// REMEMBER. the bottomLeftCorner point will be the (0,0) position of the IMAGE man(not screen)

		// Therefore, we need to calculate the position before it goes over the ROOF (Gdx.graphics.getHeight()).
		int birdYPosAtTOP = Gdx.graphics.getHeight() - birdArray[flapState].getHeight();
		//if we got to the Screen or above
		if(birdYPosition >= birdYPosAtTOP){
			birdYPosition = birdYPosAtTOP;//keep him there
		}

	//SHOW PIPES---------------------------------------------------------------------------------------------------------

//		if(tubeCount < tubeFrecuency){
//			tubeCount++;
//		} else {
//			tubeCount = 0;
//			//This function will add RANDOM positions to the tubes Y positions, and the same position to the tubes X positions
//			makeOnePairOfTopAndBottomTubes();
//		}

		//We will always have "maxNumberOfTubes" tubes
		for (int i = 0; i < topTubeXPositions.size(); i++) {
			//X-axis Position of the tubes
			float currentTopTubeXPosition = topTubeXPositions.get(i);
			float currentBottomTubeXPosition = bottomTubeXPositions.get(i);

			//if currentTopTubeXPosition is at the LEFT most side, OFF screen
			if (currentTopTubeXPosition < - topTube.getWidth()) {
				//Move it back to initial position
				float moveItBackToInitial = maxNumberOfTubes * distanceBetweenTubes - topTube.getWidth();
				topTubeXPositions.set(i, moveItBackToInitial);
				bottomTubeXPositions.set(i, moveItBackToInitial);
				//Randomnize the height
				topTubeYPositions.set( i,midOfScreenY + gap/2 + randomTubeOffset() );
				bottomTubeYPositions.set(i,midOfScreenY - bottomTube.getHeight() - gap/2 + randomTubeOffset());

			} else {
				//THERFORE, we need to make sure we move the tubes;
				//UPDATE, the current tubes' position to move by an offset/separation of 10;
				float newTopTubeXPosition = currentTopTubeXPosition - xAxisVelocityMovement;
				float newBottomTubeXPosition = currentBottomTubeXPosition - xAxisVelocityMovement;
				topTubeXPositions.set(i, newTopTubeXPosition);
				bottomTubeXPositions.set(i, newBottomTubeXPosition);
			}

			//MAKE THE tubes BE PRESENT, even though, WE SET X-position to be OFF SCREEN above.
			batch.draw(topTube, currentTopTubeXPosition, topTubeYPositions.get(i));
			batch.draw(bottomTube, currentBottomTubeXPosition, bottomTubeYPositions.get(i));

			//Make the rectangle
			topTubeRectangles[i] = new Rectangle(currentTopTubeXPosition, topTubeYPositions.get(i),topTube.getWidth(), topTube.getHeight());
			bottomTubeRectangles[i] = new Rectangle(currentBottomTubeXPosition, bottomTubeYPositions.get(i),bottomTube.getWidth(), bottomTube.getHeight());

			//ADD, the Rectangle surrounding the image COIN
			//coinRectangles.add(new Rectangle(currentCoinXPosition,coinYPositions.get(i),coin.getWidth(), coin.getHeight()));
		}


	//---------------------------------------------------------------------------------------------------------
		Gdx.app.log("collissionsCounter", "before "+collissionsCounter);

		//SCORES---------------------------------------------------------------------------
		//if we passed (Gdx.graphics.getWidth() / 2) the Tubes that bird is going through (topTubeXPositions.get(scoringTube))
		if (topTubeXPositions.get(scoringTube) < Gdx.graphics.getWidth() / 2) {
			score++;//increase the score
			Gdx.app.log("Score", String.valueOf(score));
			if (scoringTube < maxNumberOfTubes - 1) {
				scoringTube++;
			} else {
				scoringTube = 0;
			}
		}

		//CHECK COLLISION
		for (int i = 0; i < topTubeXPositions.size(); i++) {
			//if manRectangle collides with any Coin Rectangle
			if (Intersector.overlaps(birdCircle, topTubeRectangles[i]) || Intersector.overlaps(birdCircle, bottomTubeRectangles[i])) {
				collissionsCounter++;
			}
		}

		//---------------------------------------------------------------------------------------------------------
		Gdx.app.log("collissionsCounter", "after "+collissionsCounter);

		//if dead: CHANGE gameState:
		if(lifes <= collissionsCounter){
			gameState = 2;
		}

		//add text
		font.draw(batch, "Lives left: " + ( lifes - collissionsCounter), 50, Gdx.graphics.getHeight() - 50);
		font.draw(batch, "Score: " + score, 50, Gdx.graphics.getHeight() - 200);

	//---------------------------------------------------------------------------------------------------------
		//ONLY FLAP EVERY 8th iteration
		if (iterationCounter == changeFlapStateAt){
			//reset iterationCounter
			iterationCounter = 0;
			if (flapState == 0) {
				flapState = 1;
			} else {
				flapState = 0;
			}
		}
	}

		@Override
	public void dispose () {
		batch.dispose();
		background.dispose();
	}
}
