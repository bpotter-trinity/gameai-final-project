package FSMsingle;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import FSMsingle.AgentStates;
import FSMsingle.State;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public abstract class State<T> 
{
	
	public abstract void Enter(T g);
	public abstract void Exit(T g);
	public abstract ACTIONS Execute(T g);
	

}
