package FSMsingle;

import java.awt.Desktop.Action;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import FSMsingle.AgentStates;
import FSMsingle.State;
import FSMsingle.FSMAgent;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import tools.pathfinder.*;


public class AgentStates {
	/*
	////////////////////
	//DIFFERENT STATES//
	////////////////////
	 * 1. SEARCH 
	 * 		- exploring for the key or door
	 * 		- searching states for key and door***
	 * 2. ATTACK
	 * 		- checks the "range" of the agent for enemies and decides to attack
	 * 3. GOAL
	 * 		- Going to door if agent has the key 
	*/
	
	
	public static class Search extends State<FSMAgent>
	{
		private static final Search instance = new Search();
		
		private Search() {}
		
		public static Search getInstance()
		{
			return instance;
		}
		
		@Override
		public void Enter(FSMAgent a) {
			System.out.println("ENTERING SEARCH STATE");
		}
		
		@Override
		public void Exit(FSMAgent a) {
			System.out.println("EXITING SEARCH STATE");
		}

		@Override
		public ACTIONS Execute(FSMAgent a) {
			
			System.out.println("EXECUTING SEARCH");
			ACTIONS ret = ACTIONS.ACTION_NIL;
			/*
			 * OUTLINE
			 * 	- if no key found --> look for key
			 * 	- if key found --> get the key
			 * 	- if have key --> search for door
			 * 	- if found key and door --> return move and switch GOAL
			 * - if key found and door found ---> goal 
			 */
			
			/*
			 * OUTLINE 2
			 * 	- go towards key
			 * 	- if run into a wall call oof
			 */
			
			//KEY POS MAY NOT BE RIGHT
			//need to add second key pos
			if (!(a.haveKey))
			{
				System.out.println("GOING TOWARDS KEY 1----------");
				ret = a.nextMove(a.attackingTiles, a.keyPos2);
				
				if (a.gotKey1(a.agentPos, a.keyPos2))
				{
					System.out.println("GOING TOWARDS KEY 2----------");
					ret = a.nextMove(a.attackingTiles, a.keyPos);
				}
				
			} else if (a.haveKey)
			{
				System.out.println("GOING TOWARDS DOOR----------");
				ret = a.nextMove(a.attackingTiles, a.doorPos);
			}
				
			
			
			
			
			
			if (a.enemiesWithinVision == 1)
			//if (a.attackTimer(a.timer))
			{
				System.out.println("CHANGING FROM SEARCH TO ATTACK");
				a.changeState(Attack.getInstance());
				return ACTIONS.ACTION_USE;
			}
			else if (a.enemiesWithinVision > 1)
			{
				System.out.println("CHANGING FROM SEARCH TO RETREAT");
				a.changeState(Retreat.getInstance());
				return ACTIONS.ACTION_USE;
			}
			
			System.out.println("STILL SEARCHING");
			return ret;
			
		}
		
	}
	
	public static class Goal extends State<FSMAgent>
	{
		private static final Goal instance = new Goal();
		
		private Goal() {}
		
		public static Goal getInstance()
		{
			return instance;
		}
		
		@Override
		public void Enter(FSMAgent a) {
			System.out.println("ENTERING GOAL STATE");
		}
		
		@Override
		public void Exit(FSMAgent a) {
			System.out.println("EXITING GOAL STATE");
		}

		@Override
		public ACTIONS Execute(FSMAgent a) {
			
			ACTIONS ret = ACTIONS.ACTION_NIL;
			/*
			 * 1. check if enemies within range -> change to attack
			 * 2. checks if agent has key
			 * 3. orientates agent (walls?)
			 * 4. moves towards door
			 * 
			 */
			
			if (a.haveKey && a.doorFound)
			{
				Vector2d doorPosition = a.doorPos;
				
				return a.nextMove(a.attackingTiles, doorPosition);
			}
			
			
			if (a.enemiesWithinVision == 1)
			//if (a.gameTick % 5 == 0)
			{
				a.changeState(Attack.getInstance());
				return ACTIONS.ACTION_USE;
			}
			
			if (a.enemiesWithinVision > 1)
			{
				a.changeState(Retreat.getInstance());
				return ACTIONS.ACTION_USE;
			}
			
			if (!(a.keyFound))
			{
				a.changeState(Search.getInstance());
				return ACTIONS.ACTION_USE;
			}
			
			System.out.println("DID NOT TRIGGER ANYTHING IN GOAL STATE");
			return ACTIONS.ACTION_USE;
		}
		
	}
	
	public static class Attack extends State<FSMAgent>
	{
		private static final Attack instance = new Attack();
		
		private Attack() {}
		
		public static Attack getInstance()
		{
			return instance;
		}
		
		@Override
		public void Enter(FSMAgent a) {
			System.out.println("ENTERING ATTACK STATE");
		}
		
		@Override
		public void Exit(FSMAgent a) {
			System.out.println("EXITING ATTACK STATE");
		}

		@Override
		public ACTIONS Execute(FSMAgent a) {
			/*
			 * NOTES:
			 * 	- DID NOT ACCOUNT FOR DIAGONAL ENEMY
			 */
			ACTIONS ret = ACTIONS.ACTION_NIL;
			Vector2d closestEnemy = null;
			
			ArrayList<Vector2d> enemyPos = a.EnemiesInRangePos(a.attackingTiles, a.enemiesPos2dnew);
			if (enemyPos.size() != 0)
			{
				closestEnemy = enemyPos.get(0);
			
				
				if (a.enemiesWithinVision == 1)
				{
					if (a.isAgentOrientationCorrect(a.agentOrientation, closestEnemy))
					{
						ret = ACTIONS.ACTION_USE;
					} 
					else
					{
						a.agentOrientation = closestEnemy;
						ret = ACTIONS.ACTION_USE;
					}
				}
				
				//start of comment
				/*
				if (a.enemyInRange && a.enemiesWithinVision == 1)
				{
					if (a.isEnemyInRange(a.attackingTiles, a.enemiesPos2d))  //within one space
					{
						//ATTACK
						// need to orientate it towards the enemy
						if (a.isAgentOrientationCorrect(a.agentOrientation, closestEnemy))
						{
							ret = ACTIONS.ACTION_USE;
						} 
						else
						{
							a.agentOrientation = closestEnemy;
							ret = ACTIONS.ACTION_USE;
						}	
					}
					else 
					{
						//move within range then attack
						//orientate it toward enemy and move towards
						if (a.isAgentOrientationCorrect(a.agentOrientation, closestEnemy))
						{
							ret = a.nextMove(a.attackingTiles, closestEnemy);
								//the move that moves it closer to enemy 
						}
						else
						{
							a.agentOrientation = closestEnemy;
							ret = a.nextMove(a.attackingTiles, closestEnemy);
							  //the move that moves it closer to enemy
						}
					}
				}
				*/
				//end comment
			}
				
				if (a.enemiesWithinVision > 1)
				{
					System.out.println("CHANGING FROM ATTACK TO RETREAT");
					a.changeState(Retreat.getInstance());
					return ACTIONS.ACTION_USE;
				}
				
				
				if (a.enemiesWithinVision == 0)
				{
					System.out.println("CHANGING FROM ATTACK TO SEARCH");
					a.changeState(Search.getInstance());
					return ACTIONS.ACTION_USE;
				}
				
			System.out.println("CONTINUING TO ATTACK");
			return ret;
			
		}
		
	}
	
	public static class Retreat extends State<FSMAgent>
	{
		private static final Retreat instance = new Retreat();
		
		private Retreat() {}
		
		public static Retreat getInstance()
		{
			return instance;
		}
		
		@Override
		public void Enter(FSMAgent a) {
			System.out.println("ENTERING RETREAT STATE");
		}
		
		@Override
		public void Exit(FSMAgent a) {
			System.out.println("EXITING RETREAT STATE");
		}

		@Override
		public ACTIONS Execute(FSMAgent a) {
			ACTIONS farthestAction = ACTIONS.ACTION_NIL;
			
			if (a.enemiesPos2dnew.size() >= 2) { 
			Vector2d enemyOnePos = a.enemiesPos2dnew.get(0);
			Vector2d enemyTwoPos = a.enemiesPos2dnew.get(1);
			//IF able to move away from closest enemy, run (Max combined distance from both enemies)
			//ELSE, attack toward enemy
			double farthestDist = a.getEuclidianDistance(a.agentPos, enemyOnePos) +
								  a.getEuclidianDistance(a.agentPos, enemyTwoPos);
			
			double currDist = farthestDist;
			Vector2d currPos = a.agentPos;
			
			//====//
			// UP //
			//====//
			
			if(currPos.y > 0 && !a.wallsPos2d.contains(new Vector2d(currPos.x, currPos.y - 1)) ) 
			{
				currPos.y -= 1;
				currDist = a.getEuclidianDistance(currPos, enemyOnePos) + a.getEuclidianDistance(currPos, enemyTwoPos);
			}
			
			if(currDist > farthestDist)
			{
				farthestDist = currDist;
				farthestAction = ACTIONS.ACTION_UP;
			}
			
			currPos = a.agentPos;
			
			//=======//
			// RIGHT //
			//=======//
			
			if(currPos.x < a.worldDimension[0] - 1 && !a.wallsPos2d.contains(new Vector2d(currPos.x + 1, currPos.y)) )
			{
				currPos.x += 1;
				currDist = a.getEuclidianDistance(currPos, enemyOnePos) + a.getEuclidianDistance(currPos, enemyTwoPos);
			}
			
			if(currDist > farthestDist)
			{
				farthestDist = currDist;
				farthestAction = ACTIONS.ACTION_RIGHT;
			}
			
			currPos = a.agentPos;
			
			//======//
			// LEFT //
			//======//
			
			if(currPos.x > 0 && !a.wallsPos2d.contains(new Vector2d(currPos.x - 1, currPos.y)) )
			{
				currPos.x -= 1;
				currDist = a.getEuclidianDistance(currPos, enemyOnePos) + a.getEuclidianDistance(currPos, enemyTwoPos);
			}
			
			if(currDist > farthestDist)
			{
				farthestDist = currDist;
				farthestAction = ACTIONS.ACTION_LEFT;
			}
			
			currPos = a.agentPos;
			
			//======//
			// DOWN //
			//======//
			
			if(currPos.y < a.worldDimension[1] - 1 && !a.wallsPos2d.contains(new Vector2d(currPos.x, currPos.y + 1)) )
			{
				currPos.y += 1;
				currDist = a.getEuclidianDistance(currPos, enemyOnePos) + a.getEuclidianDistance(currPos, enemyTwoPos);
			}
			
			if(currDist > farthestDist)
			{
				farthestDist = currDist;
				farthestAction = ACTIONS.ACTION_DOWN;
			}
			
			//==============//
			// FINAL ACTION //
			//==============//
			
			System.out.println("RETREAT FINAL ACTION IS: " + farthestAction.toString());
			
			//*************************//
			// ATTACK IF ACTION_NIL!!! //
			//*************************//
			
			if (farthestAction.equals(ACTIONS.ACTION_NIL))
            {
                farthestAction = a.correctAttack(enemyOnePos, a.agentOrientation);
            }
			
			}
			 //===============//
            // CHANGE STATES //
            //===============//
            
            if (a.enemiesWithinVision < 2 && a.enemiesWithinVision > 0)
            {
                a.changeState(Attack.getInstance());
            }
            
            if (a.haveKey && a.doorFound && a.enemiesWithinVision == 0)
            {
                a.changeState(Goal.getInstance());
            }
            
            if (a.enemiesWithinVision == 0)
            {
                a.changeState(Search.getInstance());
            }
			
			return farthestAction;
		}
		
	}
	
	
	
}
