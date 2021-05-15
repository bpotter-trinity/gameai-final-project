package FSMsingle;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Random;

import java.util.List;
import java.util.Collection;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import FSMsingle.AgentStates;
import FSMsingle.AgentStates.Search;
import FSMsingle.State;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import core.game.Observation;
import core.game.*;
import tools.pathfinder.*;

//GLOBAL VARS 
//	- like tiles we have seen 

public class FSMAgent extends AbstractPlayer {
	
	public State<FSMAgent> currentState = Search.getInstance();
	
	protected PathFinder pathf;
	
	/*
	public FSMAgent()
	{
		return;
	}*/
	
	public FSMAgent(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		currStateObs = so;
		timer = elapsedTimer;
		
		ArrayList<Integer> list = new ArrayList<>(0);
        list.add(0); //wall
        pathf = new PathFinder(list);
        pathf.run(so);
	}
	
	public StateObservation getStateObservation()
	{
		return this.currStateObs;
	}
	
	/*
	 * VARIABLES 
	 * 	- boolean for if enemy is within range
	 * 	- position of agent 
	 * 	- oritentation of agent
	 * 	- boolean if knows where key is 
	 * 	- boolean if knows where door is
	 * 	- int for location of key 
	 * 	- int for location of door
	 */
    
	public StateObservation prevSO = null;
	
    public StateObservation currStateObs = null; //*****//
    public ElapsedCpuTimer timer = null; //*****//
	
	public boolean enemyInRange = false;
	public int enemyCount = 0;
	
	public Vector2d agentPos = null;  //maybe change to array of 2 doubles??
	public Vector2d agentOrientation = null;
	public ArrayList<Types.ACTIONS> availableActions;
	
	public ArrayList<Vector2d> attackingTiles = new ArrayList<Vector2d>(); //need to get
	public ArrayList<Vector2d> allPositions = new ArrayList<Vector2d>(); 
	public ArrayList<Vector2d> visionTiles = new ArrayList<Vector2d>();
	public ArrayList<Vector2d> visionTilesBIG = new ArrayList<Vector2d>();
	public ArrayList<Vector2d> unknownTiles = new ArrayList<Vector2d>();
	public ArrayList<Vector2d> knownTiles = new ArrayList<Vector2d>();
	
	public boolean keyFound = false;
	public boolean doorFound = false;
	
	//enemies
	public ArrayList<Observation>[] enemiesPos;
	public ArrayList<Vector2d> enemiesPos2d = new ArrayList<Vector2d>();
	public ArrayList<Vector2d> enemiesPos2dnew = new ArrayList<Vector2d>();
	public int enemiesWithinVision = 0;
	    
    //walls
    public ArrayList<Observation>[] wallsPosObs;
    public ArrayList<Vector2d> wallsPos2d = new ArrayList<Vector2d>();
        
    //door location
    ArrayList<Observation>[] doorPosObs;
    public ArrayList<Vector2d> doorPosLst =  new ArrayList<Vector2d>();
    public Vector2d doorPos;
    
    //Key location
    ArrayList<Observation>[] keyPosObs;
    Vector2d keyPos;
    Vector2d keyPos2;
    boolean haveKey = false;
    public ArrayList<Vector2d> keyPosLst =  new ArrayList<Vector2d>();
    
    //Game settings
    public ElapsedCpuTimer gameTimer;
	public float gameScore;
	public int gameTick;
	public Types.WINNER gameWinner;
	public boolean isGameOver;
	public double[] worldDimension;
	
	
	//when to call?? 
	public void evalGS(StateObservation s, ElapsedCpuTimer elapsedTimer)
	{
		gameTick = s.getGameTick();
		gameTimer = elapsedTimer;
		
		enemyCount = 0;
		enemiesWithinVision = 0;
		enemyInRange = false;
		enemiesPos2d.clear();
		enemiesPos2dnew.clear();
		visionTiles.clear();
		visionTilesBIG.clear();
		attackingTiles.clear();
		doorPosLst.clear();
		keyPosLst.clear();
		
		ArrayList<Observation> row;
		
		//setting game variables 
		gameScore = (float) s.getGameScore();
		gameTick = s.getGameTick();
		isGameOver = s.isGameOver();
		gameWinner = s.getGameWinner();
		
		//Immovable positions (WALLS)
	    wallsPosObs = s.getImmovablePositions();
	    
	    if (wallsPosObs != null)
		{
			for(ArrayList<Observation> wall : wallsPosObs)
			{
				if (wall.size() > 0) 
				{
					//***
					wallsPos2d.add(wall.get(0).position);
					wall.remove(0);
				}
			}
		}
		
		//WORLD VARIABLES
		worldDimension = new double[2];
	    worldDimension[0] = s.getWorldDimension().getWidth();
	    worldDimension[1] = s.getWorldDimension().getHeight();
	    
	    allPositions = new ArrayList<Vector2d>();
	    double nextX;
	    double nextY;
	    for(int i = 0; i < worldDimension[0] * worldDimension[1]; i++)
	    {
	    	//========================================//
	    	// CHECK LATER TO MAKE SURE THIS IS RIGHT //
	    	//========================================//
	    	nextX = i / worldDimension[1];
	    	nextY = i % worldDimension[1];
	    	Vector2d nextVector = new Vector2d(nextX, nextY);
	    	
	    	if(!wallsPos2d.contains(nextVector))
	    	{
	    		allPositions.add(nextVector);
	    	}
	    	
	    }
	    
	    
	    
		//updating agent variables 
		agentPos = s.getAvatarPosition();
		agentOrientation = s.getAvatarOrientation();
		availableActions = s.getAvailableActions();
		
		//ATTACKING TILEs
		attackingTiles = agentAttackingTiles(agentPos);
		
		//updating NPC positions (ENEMIES) 
		enemiesPos = s.getNPCPositions();
		//double i = enemiesPos[0].get(0).position.x;
		double x = 0;
		double y = 0;
		
		Vector2d temp;
		
		//getting the vectors of the enemies
		if (enemiesPos != null)
		{
			for(ArrayList<Observation> npc : enemiesPos)
			{
				if (enemiesPos.length > 0)
				{
					/*
					x = npc.get(0).position.x / 61;
					y = npc.get(0).position.y / 61;
					temp = new Vector2d(x, y);
					enemiesPos2d.add(temp);
					*/
					enemiesPos2dnew.add(npc.get(0).position);
					enemiesPos2d.add(npc.get(0).position);
					npc.remove(0);
					enemyCount += 1;
				}
			}
		}
		
		System.out.println("ENEMIES ON THE FIELD-----------");
		System.out.println(enemiesPos2dnew.size());
		/*
		if (enemiesPos2dnew.size() > 0) {
		System.out.println("ENEMY POS 1");
		System.out.println(enemiesPos2dnew.get(0).x + "," + enemiesPos2d.get(0).y);
		}
		if (enemiesPos2dnew.size() > 1)
		{
			System.out.println("ENEMY POS 2");
			System.out.println(enemiesPos2dnew.get(1).x + "," + enemiesPos2d.get(0).y);
		}
		if (enemiesPos2dnew.size() > 2)
		{
			System.out.println("ENEMY POS 3");
			System.out.println(enemiesPos2dnew.get(2).x + "," + enemiesPos2d.get(2).y);
		}
		*/
		enemyInRange = isEnemyInRange(attackingTiles, enemiesPos2d);
		
		
		//getting door position 
		doorPosObs = s.getPortalsPositions();
		int cnt = 0;
		for (ArrayList<Observation> portal : doorPosObs) {
			if(portal.size() > 0)
            {   
				
            	//***
				doorPosLst.add(portal.get(0).position);
				doorPos = portal.get(0).position;
				portal.remove(0);
				cnt += 1;
            }
        }
		
		System.out.println("DOOR POS---------");
		System.out.println(doorPos.x + "," + doorPos.y);
		
		cnt = 0;
		//Getting key position
		keyPosObs = s.getImmovablePositions();
		//System.out.println(keyPosObs.length);
		
		for (ArrayList<Observation> key : keyPosObs) {
			cnt += 1;
            if(key.size() > 0)
            {  
            	
            	//***
            	if (cnt == 1)
            	{
            		 keyPos = key.get(0).position;
            		 key.remove(0);
            	}
            	if (cnt == 2)
            	{
            		keyPos2 = key.get(0).position;
            	}
            	
            }
        }
		
		
	
		
		
		//NEED TO DETERMINE VISION OF AGENT
		
		
		//OUTLINE
		
				//FOR ALL TILES
					//IF PATH <= 3 TILES LONG
						//IF TILE IS IN UNKNOWN TILES
							//REMOVE TILE FROM UNKNOWN AND ADD TO KNOWN (DONE)
		        			//IF TILE CONTAINS KEY (DONE)
							//IF TILE CONTAINS DOOR (DONE)
							//IF TILE IS IMMOVABLE, UPDATE IMMOVABLE LIST?
						//IF TILE CONTAINS ENEMY (DONE)
		//VISION	
		
			visionTiles = crossVision(agentPos);
			unknownTiles = allPositions;
			visionTilesBIG = crossVisionBIG(agentPos);
			
			for (Vector2d tile : visionTilesBIG)
			{
				if (!(knownTiles.contains(tile)))
				{
					knownTiles.add(tile);
					unknownTiles.remove(tile);
				}
				
				if (tile.equals(keyPos))
				{
					keyFound = true;
				}
				if (tile.equals(doorPos))
				{
					doorFound = true;
				}
				if (enemiesPos2d.contains(tile))
				{
					//enemiesWithinVision += 1;
				}
				
			}
			
			for (Vector2d tile : visionTilesBIG)
			{
				if (enemiesPos2dnew.contains(tile))
					enemiesWithinVision += 1;
			}
			
	      if (s.getImmovablePositions().length == 1)
	      {
	    	  haveKey = true;
	      }
		
	}
	
	/*
	 * //HELPER FUNCTIONS---------------------------------------------------------
	 * 		FOR THE AGENT STATES TO USE 
	 * 
	 * 	- isDoorInSight
	 * 	- isKeyInSight
	 * 	- foundDoor
	 * 	- foundKey (if we have to retreat if we found the key)
	 * 	- isEnemyInRange
	 * 	- isEnemyInSight 
	 * 	- isAPath / isAWallInWay
	 * 	- isOrientationCorrect (takes in a move)
	 */
	
	
	/*
	 * DETERMINES THE ATTACKING TILES (variable)
	 * DONE
	 */
	public boolean attackTimer(ElapsedCpuTimer timer)
	{
		double secs = timer.elapsedSeconds();
		double lower = 1;
		double upper = 6;
		
		for (int i = 1; i < 100; i++)
		{
			
			if (lower * (i * 3) < secs && secs < upper * (i * 3))
			{
				return true;
			}
			
		}
		
		return false;
	}
	
	public boolean gotKey1 (Vector2d aPos, Vector2d key1Pos)
	{
		{
	        double range = 30;
	        
	        if(aPos.x - range < key1Pos.x && aPos.x + range > key1Pos.x)
	        {
	            if(aPos.y - range < key1Pos.y && aPos.y + range > key1Pos.y)
	            {
	                return true;
	            }
	            else
	                return false;
	        }
	        else
	            return false;
	        
	        
	        /*
	        if (aPos.equals(key1Pos))
	        {
	            return true;
	        }
	        */
	        
	        //return false;
	    }
	}
	
	public ArrayList<Vector2d> agentAttackingTiles(Vector2d agentPos)
	{
		
		
		int blockSize = 61;
		int range = 2;
		ArrayList<Vector2d> ret = new ArrayList<Vector2d>();
		Vector2d point = new Vector2d(agentPos.x, agentPos.y);
		
		//LEFT
		for(int i = 1; i <= range; i++)
		{
			point = new Vector2d(agentPos.x - i * blockSize, agentPos.y);
			ret.add(point);
		}
		//RIGHT
		for(int i = 1; i < range; i++)
		{
			point = new Vector2d(agentPos.x + i * blockSize, agentPos.y);
			ret.add(point);
		}
		//UP
		for(int i = 1; i < range; i++)
		{
			point = new Vector2d(agentPos.x, agentPos.y + i * blockSize);
			ret.add(point);
		}
		//DOWN
		for(int i = 1; i < range; i++)
		{
			point = new Vector2d(agentPos.x, agentPos.y - i * blockSize);
			ret.add(point);
		}
		
		return ret;
	}
	
	/*
	 * CHECKS IF THE ENEMY IS WITHIN ATTACKING RANGE OF THE AGENT 
	 * DONE 
	 */
	public boolean isEnemyInRange(ArrayList<Vector2d> attackingTiles, ArrayList<Vector2d> enemyTiles)
	{
		for (int i = 0; i < attackingTiles.size(); i++)
		{
			for (int j = 0; j < enemyTiles.size(); j++)
			{
				if (attackingTiles.get(i).equals(enemyTiles.get(j)))
				{
					enemyInRange = true;
					return true;
				}
			}
		}
		
		
		enemyInRange = false;
		return false;
	}
	/*
	 * RETURNS THE ENEMY TILE THAT IS WITHIN ATTACKING RANGE 
	 * NOT DONE
	 * 
	 * NOTE:
	 * maybe just set a global variable and return it 
	 * 
	 * DONE
	 */
	public ArrayList<Vector2d> EnemiesInRangePos(ArrayList<Vector2d> attackingTiles, ArrayList<Vector2d> enemyTiles)
	{
		ArrayList<Vector2d> ret = new ArrayList<Vector2d>();
		
		for (int i = 0; i < attackingTiles.size(); i++)
		{
			for (int j = 0; j < enemyTiles.size(); j++)
			{
				if (attackingTiles.get(i).equals(enemyTiles.get(j)))
				{
					ret.add(enemyTiles.get(j));
					
					enemyInRange = true;
				}
			}
		}
		
		return ret;

	}
	/*
	 * CHECKS IF THERE IS AN ENEMY WITHIN AGENT SIGHT 
	 * NOT DONE
	 * 
	 * NOT NEEDED
	 */
	public boolean isEnemyInSight(ArrayList<Vector2d> visionTiles, ArrayList<Vector2d> enemyTiles)
	{
		
		return false;
	}
	/*
	 * RETURNS THE ENEMIES POSITIONS THAT ARE WITHIN AGENT SIGHT
	 * NOT DONE
	 * 
	 * NOT NEEDED
	 */
	public ArrayList<Vector2d> EnemiesInSightPos(ArrayList<Vector2d> attackingTiles, ArrayList<Vector2d> enemyTiles)
	{
		
		return null;
	}
	
	/*
	 * DETERMINES IF THE AGENT IS FACING THE CORRECT WAY
	 * 
	 * DONE
	 */
	
	public boolean isAgentOrientationCorrect(Vector2d agentOri, Vector2d correctOri)
	{
		
		if (agentOri.equals(correctOri))
			return true;
		else return false;
		
	}
	
		
	public void fixOrientationPATH(Vector2d agentOri, Vector2d correctOri)
	{
		ArrayList<Node> path = pathf.getPath(agentOri, correctOri);
		Vector2d moveTile = path.get(0).position;
		
		if (moveTile.y > agentPos.y && moveTile.x == agentPos.x)
		{
			agentOrientation = moveTile;
		}
		
		if (moveTile.y > agentPos.y && moveTile.x < agentPos.x)
		{
			agentOrientation.x = agentPos.x;
			agentOrientation.y = agentPos.y + 1;
			
		}
		
		if (moveTile.y < agentPos.y && moveTile.x == agentPos.x)
		{
			agentOrientation = moveTile;
			
		}
		
		if (moveTile.y > agentPos.y && moveTile.x > agentPos.x)
		{
			agentOrientation.x = agentPos.x;
			agentOrientation.y = agentPos.y - 1;
			
		}
		
		if (moveTile.x < agentPos.x && moveTile.y == agentPos.y)
		{
			agentOrientation = moveTile;
		}
		
		if (moveTile.y < agentPos.y && moveTile.x < agentPos.x)
		{
			agentOrientation.x = agentPos.x - 1;
			agentOrientation.y = agentPos.y;
		}
		
		if (moveTile.x > agentPos.x && moveTile.y == agentPos.y)
		{
			agentOrientation = moveTile;
		}
		
		if (moveTile.y < agentPos.y && moveTile.x > agentPos.x)
		{
			agentOrientation.x = agentPos.x + 1;
			agentOrientation.y = agentPos.y;
		}
		
		if (moveTile.equals(agentPos))
		{
			agentOrientation = moveTile;
		}
		
	}
	public ACTIONS nextMove(ArrayList<Vector2d> availableMoves, Vector2d end)
	{
		int blockSize = 61;
		availableMoves = attackingTiles;
		Vector2d moveTile = agentOrientation;
		int change = 1 * blockSize;
		
		for (int i = 0; i < availableMoves.size(); i++)
		{
			if (getEuclidianDistance(availableMoves.get(i), end) < getEuclidianDistance(moveTile, end))
			{
				moveTile = availableMoves.get(i);
			}
		}
		/*
		for (Vector2d attackTile : availableMoves)
		{
			if (getEuclidianDistance(attackTile, end) < getEuclidianDistance(moveTile, end))
			{
				moveTile = attackTile;
			}
		}
		*/
		
		
		if (moveTile.y > agentPos.y && moveTile.x == agentPos.x)
		{
			agentOrientation = moveTile;
			return ACTIONS.ACTION_UP;
		}
		else if (moveTile.y > agentPos.y && moveTile.x < agentPos.x)
		{
			agentOrientation.x = agentPos.x;
			agentOrientation.y = agentPos.y + change;
			agentOrientation = moveTile;
			return ACTIONS.ACTION_UP;
		}
		else if (moveTile.y < agentPos.y && moveTile.x == agentPos.x)
		{
			agentOrientation = moveTile;
			return ACTIONS.ACTION_DOWN;
		}
		else if (moveTile.y > agentPos.y && moveTile.x > agentPos.x)
		{
			agentOrientation.x = agentPos.x;
			agentOrientation.y = agentPos.y - change;
			agentOrientation = moveTile;
			return ACTIONS.ACTION_DOWN;
		}
		else if (moveTile.x < agentPos.x && moveTile.y == agentPos.y)
		{
			agentOrientation = moveTile;
			return ACTIONS.ACTION_LEFT;
		}
		else if (moveTile.y < agentPos.y && moveTile.x < agentPos.x)
		{
			agentOrientation.x = agentPos.x - change;
			agentOrientation.y = agentPos.y;
			agentOrientation = moveTile;
			return ACTIONS.ACTION_LEFT;
		}
		else if (moveTile.x > agentPos.x && moveTile.y == agentPos.y)
		{
			agentOrientation = moveTile;
			return ACTIONS.ACTION_RIGHT;
		}
		else if (moveTile.y < agentPos.y && moveTile.x > agentPos.x)
		{
			agentOrientation.x = agentPos.x + change;
			agentOrientation.y = agentPos.y;
			agentOrientation = moveTile;
			return ACTIONS.ACTION_RIGHT;
		}
		else if (moveTile.equals(agentPos))
		{
			agentOrientation = moveTile;
			return ACTIONS.ACTION_USE;
		}
		else
		{
			System.out.println("nextmove function did NOT work");
			return ACTIONS.ACTION_NIL;
		}
	}
	
	
	/*
	 * RETURNS THE BEST MOVE TO THE GOAL
	 * DONE
	 */
	public ACTIONS nextMovePATH(Vector2d start, Vector2d end)
	{
		
		ArrayList<Node> path = pathf.getPath(start, end);
		
		if(path == null)
		{
			System.out.println("PATH = NULL");
			return ACTIONS.ACTION_NIL;
		}
		
		Vector2d moveTile = path.get(0).position;
		
		if (moveTile.y > agentPos.y && moveTile.x == agentPos.x)
		{
			agentOrientation = moveTile;
			return ACTIONS.ACTION_UP;
		}
		else if (moveTile.y > agentPos.y && moveTile.x < agentPos.x)
		{
			agentOrientation.x = agentPos.x;
			agentOrientation.y = agentPos.y + 1;
			return ACTIONS.ACTION_UP;
		}
		else if (moveTile.y < agentPos.y && moveTile.x == agentPos.x)
		{
			agentOrientation = moveTile;
			return ACTIONS.ACTION_DOWN;
		}
		else if (moveTile.y > agentPos.y && moveTile.x > agentPos.x)
		{
			agentOrientation.x = agentPos.x;
			agentOrientation.y = agentPos.y - 1;
			return ACTIONS.ACTION_DOWN;
		}
		else if (moveTile.x < agentPos.x && moveTile.y == agentPos.y)
		{
			agentOrientation = moveTile;
			return ACTIONS.ACTION_LEFT;
		}
		else if (moveTile.y < agentPos.y && moveTile.x < agentPos.x)
		{
			agentOrientation.x = agentPos.x - 1;
			agentOrientation.y = agentPos.y;
			return ACTIONS.ACTION_LEFT;
		}
		else if (moveTile.x > agentPos.x && moveTile.y == agentPos.y)
		{
			agentOrientation = moveTile;
			return ACTIONS.ACTION_RIGHT;
		}
		else if (moveTile.y < agentPos.y && moveTile.x > agentPos.x)
		{
			agentOrientation.x = agentPos.x + 1;
			agentOrientation.y = agentPos.y;
			return ACTIONS.ACTION_RIGHT;
		}
		else if (moveTile.equals(agentPos))
		{
			agentOrientation = moveTile;
			return ACTIONS.ACTION_USE;
		}
		else
		{
			System.out.println("nextmove function did NOT work");
			return ACTIONS.ACTION_NIL;
		}
	}
	
	public ArrayList<Vector2d> crossVision(Vector2d pos)
	{
		int blockSize = 61;
		pos = agentPos;
		int visionRange = 4;
		ArrayList<Vector2d> ret = new ArrayList<Vector2d>();
		
		Vector2d point = new Vector2d(pos.x,pos.y);
		
		//LEFT
		for (int i = 1; i < visionRange; i++)
		{
			point = new Vector2d(pos.x - i * blockSize, pos.y * blockSize);
			ret.add(point);
		}
		
		//Right
		for (int i = 1; i < visionRange; i++)
		{
			point = new Vector2d(pos.x + i * blockSize, pos.y * blockSize);
			ret.add(point);
		}
		//UP
		for (int i = 1; i < visionRange; i++)
		{
			point = new Vector2d(pos.x * blockSize, pos.y + i * blockSize);
			ret.add(point);
		}
		//DOWN
		for (int i = 1; i < visionRange; i++)
		{
			point = new Vector2d(pos.x * blockSize, pos.y - i * blockSize);
			ret.add(point);
		}
		
		
		return ret;
	}
	
	public ArrayList<Vector2d> crossVisionBIG(Vector2d pos)
	{
		
	        int blockSize = 61;
	        pos = agentPos;
	        int visionRange = 3;
	        ArrayList<Vector2d> ret = new ArrayList<Vector2d>();
	        
	        Vector2d point = new Vector2d(pos.x,pos.y);
	        
	       
	        
	        for(int i = -3; i <= visionRange; i++)
	        {
	            for(int j = -3; j <= visionRange; j++)
	            {
	                if (Math.abs(i) + Math.abs(j) <= 3)
	                    ret.add( new Vector2d(pos.x + (i * blockSize), pos.y + (j * blockSize)) );
	            }
	        }
	        
		
		/*
		pos = agentPos;
		int visionRange2 = 200;
		
		
		Vector2d point2 = new Vector2d(pos.x,pos.y);
		
		//LEFT
		for (int i = 1; i < visionRange2; i++)
		{
			point = new Vector2d(pos.x - i , pos.y );
			ret.add(point2);
		}
		
		//Right
		for (int i = 1; i < visionRange2; i++)
		{
			point = new Vector2d(pos.x + i , pos.y );
			ret.add(point2);
		}
		//UP
		for (int i = 1; i < visionRange2; i++)
		{
			point = new Vector2d(pos.x , pos.y + i );
			ret.add(point2);
		}
		//DOWN
		for (int i = 1; i < visionRange2; i++)
		{
			point = new Vector2d(pos.x , pos.y - i );
			ret.add(point2);
		}
		*/
		
		return ret;
	}
	/*
	public ArrayList<Vector2d> crossVisionBIG(Vector2d pos)
	{
		int blockSize = 61;
		pos = agentPos;
		int visionRange = 250;
		ArrayList<Vector2d> ret = new ArrayList<Vector2d>();
		
		Vector2d point = new Vector2d(pos.x,pos.y);
		
		//LEFT
		for (int i = 1; i < visionRange; i++)
		{
			point = new Vector2d(pos.x - i , pos.y );
			ret.add(point);
		}
		
		//Right
		for (int i = 1; i < visionRange; i++)
		{
			point = new Vector2d(pos.x + i , pos.y );
			ret.add(point);
		}
		//UP
		for (int i = 1; i < visionRange; i++)
		{
			point = new Vector2d(pos.x , pos.y + i );
			ret.add(point);
		}
		//DOWN
		for (int i = 1; i < visionRange; i++)
		{
			point = new Vector2d(pos.x , pos.y - i );
			ret.add(point);
		}
		
		
		return ret;
	}
	*/
	public ACTIONS correctAttack(Vector2d attackTile, Vector2d agentOri)
	{
		
		if (attackTile.equals(agentOri))
		{
			return ACTIONS.ACTION_USE;
		}
		
		agentOri = attackTile;
		
		return ACTIONS.ACTION_USE;
	}
			
	public double getEuclidianDistance(Vector2d startPos, Vector2d targetPos)
    {
		int blockSize = 61;
		
        double xDist = startPos.x / blockSize - targetPos.x / blockSize;
        double yDist = startPos.y / blockSize - targetPos.y / blockSize;
        
        return Math.sqrt( xDist*xDist + yDist*yDist );
    }
	
	public boolean hitWall (Vector2d currPos, StateObservation prevState)
	{
		if (!(prevState == null))
		{
			if (currPos.equals(prevState.getAvatarPosition()))
			{
				return true;
			}
			else return false;
		} else return false;
	}
	
	public ACTIONS oof(Vector2d currPos, StateObservation prevState)
	{
		ACTIONS prevMove = prevState.getAvatarLastAction();
		ArrayList<ACTIONS> allActions = prevState.getAvailableActions();
		allActions.remove(prevMove);
		allActions.remove(ACTIONS.ACTION_USE);
		allActions.remove(ACTIONS.ACTION_ESCAPE);
		allActions.remove(ACTIONS.ACTION_NIL);
		
		ACTIONS ret = allActions.get(0);
		
		if (currPos.equals(prevState.getAvatarPosition()))
		{
			Random r = new Random();
			ret = allActions.get(r.nextInt(allActions.size()));
			
		}
		
		updateOrientationToAction(ret, agentOrientation);
		return ret;
	}
	
	public Vector2d updateOrientationToAction(ACTIONS action, Vector2d ori)
	{
		int blockSize = 61;
		int change = 1 * blockSize;
		
		if (action.equals(ACTIONS.ACTION_DOWN))
		{
			agentOrientation.y = agentPos.y - change;
		} else if (action.equals(ACTIONS.ACTION_UP))
		{
			agentOrientation.y = agentPos.y + change;
		} else if (action.equals(ACTIONS.ACTION_RIGHT))
		{
			agentOrientation.x = agentPos.x + change;
		} else if (action.equals(ACTIONS.ACTION_LEFT))
		{
			agentOrientation.x = agentPos.x - change;
		} else 
			agentOrientation = agentPos;
		
		
		return agentOrientation;
	}
	
	/*
	 * MAIN FUNCTIONS-------------------------------------------------------------
	 */
	
	public ACTIONS update() {
		
		/*
		if (currentState != null)
		{
			return currentState.Execute(this);
		}
		*/
		
		//evalGS(so);
		
		return currentState.Execute(this);
	}
	
	public void changeState(State<FSMAgent> newState)
	{
		if (currentState == null || newState == null)
		{
			System.out.println("null change State.");
		}
		
		currentState.Exit(this);
		
		currentState = newState;
		
		currentState.Enter(this);
	}
	
	
	//FSMAgent a = new FSMAgent();	
	
	//main function that returns an action...
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		// TODO Auto-generated method stub
		//maybe call buildVars function??? since called everytime need a move 
		
		evalGS(stateObs, elapsedTimer);
		//stateObs.advance(ACTIONS.ACTION_RIGHT);
		ACTIONS ret = ACTIONS.ACTION_USE;
		FSMAgent a = new FSMAgent(stateObs, elapsedTimer);
		
		/*
		if(a.getStateObservation().equals(null))
		{
			a = new FSMAgent(stateObs, elapsedTimer);
		}
		*/
		
		//ERROR TESTING
		//System.out.println("TESTING------------");
		//System.out.println(stateObs.getGameTick());
	
		
		//FSMAgent a = new FSMAgent();
		//System.out.println()
		System.out.println("enemies in vision---------");
		System.out.println(enemiesWithinVision);
		System.out.println("enemies in vision---------");
		//System.out.println(enemyCount);
		
		if (hitWall(agentPos, prevSO))
		{
			ret = oof(agentPos, prevSO);
		} else ret = update();
		
		prevSO = stateObs.copy();
		
		System.out.println(ret.toString());
		return ret;
	}

}
