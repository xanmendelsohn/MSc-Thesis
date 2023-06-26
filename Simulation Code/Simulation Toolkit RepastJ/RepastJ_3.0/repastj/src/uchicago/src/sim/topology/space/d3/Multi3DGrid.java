/*$$
 * Copyright (c) 1999, Trustees of the University of Chicago
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with 
 * or without modification, are permitted provided that the following 
 * conditions are met:
 *
 *	 Redistributions of source code must retain the above copyright notice,
 *	 this list of conditions and the following disclaimer.
 *
 *	 Redistributions in binary form must reproduce the above copyright notice,
 *	 this list of conditions and the following disclaimer in the documentation
 *	 and/or other materials provided with the distribution.
 *
 * Neither the name of the University of Chicago nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *$$*/
package uchicago.src.sim.topology.space.d3;

import java.util.HashSet;

import uchicago.src.sim.topology.space.Location;


/**
 * A grid object that can hold more than one object in its cells.
 * The cells themselves store their occupants without
 * any order. Use an <code>OrderedMulti2DGrid</code> if you need
 * the cell occupants to be stored in an ordered fashion. The object
 * return by getObjectAt is a <code>Multi2DLocation</code>.
 *
 * @version $Revision: 1.3 $ $Date: 2004/11/03 19:51:04 $
 */
public class Multi3DGrid extends AbstractObject3DSpace {

    /**
     * Creates this Multi2DGrid with the specified dimensions.
     *
     * @param xSize the number of columns in the grid
     * @param ySize the number of rows in the grid
     */
    public Multi3DGrid(int xSize, int ySize, int zSize) {
        super("Multi3DGrid",xSize, ySize, zSize);
    }
        /**
     * Creates this Multi2DGrid with the specified dimensions.
     * sparse specifies whether the grid will be sparsely filled or not.
     *
     * @param xSize the number of columns in the grid
     * @param ySize the number of rows in the grid
     * @param sparse whether the grid will be sparsely populated or not
     */
    public Multi3DGrid(String type, int xSize, int ySize, int zSize) {
        super(type,xSize, ySize, zSize);
    }
  
	/**
	 * Gets the Object2DLocation object at the specified coordinates.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @throws IndexOutOfBoundsException if the given coordinates are out of
	 * range (x < 0 || x >= xSize || y < 0 || y >= ySize)
	 */
	public Location getLocation(int x, int y, int z) {
		
		Location cell  = (Location) matrix[xnorm(x)][ynorm(y)][znorm(z)];
		
		if(cell == null){
			cell = new Multi3DLocation(xnorm(x), ynorm(y), znorm(z));
		}
		
		return cell;
	}

    /**
     * A grid cell whose occupants are stored as if in a bag without order.
     * Removing objects from this type of cell will be faster than from an
     * ordered cell.
     *
     * @version $Revision: 1.3 $ $Date: 2004/11/03 19:51:04 $
     */

    public class Multi3DLocation extends Abstract3DLocation implements Object3DLocation {

        protected Multi3DLocation(int x, int y, int z){
            super(x,y,z);
            elements = new HashSet();
        }
    
    }
    
}

