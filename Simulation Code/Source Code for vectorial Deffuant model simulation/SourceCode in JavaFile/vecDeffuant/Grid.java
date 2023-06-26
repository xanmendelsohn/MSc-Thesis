package vecDeffuant;

import java.awt.*;
import java.util.*;
import uchicago.src.collection.*;
import uchicago.src.sim.space.*;
import uchicago.src.sim.util.Random;

public class Grid implements Discrete2DSpace {

   	/** Defines the neighbourhood to be all other agents with equal probability of being chosen.	*/
   	public static final int GLOBAL_UNIFORM = 2;	// N.B. VON_NEUMANN & MOORE are 0 & 1 respectively.

  	protected Object2DGrid space;
  	protected int neighbourhoodExtent;
  	protected int neighbourhoodType;

  	private Vector neighbourhood;

	/**
		Create the grid.

		@param gridWidth, gridHeight the width and height of the grid.
		@param torus if true, the grid wraps around.
		@param neighbourhoodType the type of neighbourhood: VON_NEUMANN , MOORE or GLOBAL_UNIFORM.
		@param neighbourhoodExtent the radius of the neighbourhood.
		@throws IllegalArgumentException if invalid neighbourhoodType.
	*/
	public Grid(int gridWidth, int gridHeight, boolean torus, int neighbourhoodType, int neighbourhoodExtent ) {
		space = ( torus ? new Object2DTorus(gridWidth, gridHeight) : new Object2DGrid(gridWidth, gridHeight) );
		if( neighbourhoodType < 0 || neighbourhoodType > 2)
			throw new IllegalArgumentException("Invalid neighbourhood type.");
		this.neighbourhoodType = neighbourhoodType;
		this.neighbourhoodExtent = neighbourhoodExtent;
	}

	/**
		Randomly chooses a neighbour.

		@param x, y the coordinates of the active agent.
		@return a randomly chosen neighbour of the active agent.
	*/
	public Object getNeighbour(int x, int y) {
		switch(neighbourhoodType) {
			case VON_NEUMANN:
				neighbourhood = space.getVonNeumannNeighbors(x, y, neighbourhoodExtent, neighbourhoodExtent, false);
    			return(neighbourhood.get(Random.uniform.nextIntFromTo(0, neighbourhood.size()-1)));
			case MOORE:
				neighbourhood = space.getMooreNeighbors(x, y, neighbourhoodExtent, neighbourhoodExtent, false);
    			return(neighbourhood.get(Random.uniform.nextIntFromTo(0, neighbourhood.size()-1)));
			case GLOBAL_UNIFORM:
  				int jx, jy;	// Coordinates of neighbouring site.
				// Randomly choose any site in the territory that is not the active site.
				do	{
					jx = Random.uniform.nextIntFromTo(0, space.getSizeX()-1);
					jy = Random.uniform.nextIntFromTo(0, space.getSizeY()-1);
				} while( jx == x && jy == y );
				return(space.getObjectAt(jx,jy));
		}
		return null;	// to satisfy compiler.
	}

	/**
		Used for counting regions.
	*/
	public Vector getVonNeumannNeighbors(int x, int y, boolean returnNulls) {
		return(space.getVonNeumannNeighbors(x, y, returnNulls));
	}

	// Following methods implement Discrete2DSpace interface.
	public int getSizeX() {return(space.getSizeX());}
	public int getSizeY() {return(space.getSizeY());}
	public Dimension getSize() {return(space.getSize());}
	public Object getObjectAt(int x, int y) {return(space.getObjectAt(x,y));}
	public double getValueAt(int x, int y) {return(space.getValueAt(x,y));}
	public void putObjectAt(int x, int y, Object object) {space.putObjectAt(x,y,object);}
	public void putValueAt(int x, int y, double value) {space.putValueAt(x,y,value);}
	public BaseMatrix getMatrix() {return(space.getMatrix());}
}
