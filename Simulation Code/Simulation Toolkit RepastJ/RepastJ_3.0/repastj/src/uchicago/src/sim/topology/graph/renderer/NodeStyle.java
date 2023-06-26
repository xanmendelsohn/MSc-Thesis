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
package uchicago.src.sim.topology.graph.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
/**
 * @author Tom Howe
 * @version $Revision: 1.3 $
 */
public abstract class NodeStyle {
	protected Font font = new Font("monospace", Font.BOLD, 9);
	protected Color labelColor = Color.yellow;
	protected Color color = Color.blue;
	protected Color borderColor = Color.white;
	protected int borderWidth = 0;
	protected int width = 8;
	protected int height = 8;
	protected boolean recalc = true;
	protected boolean allowResizing = true;
	protected boolean hollow = false;
	protected Shape shape = new Ellipse2D.Double();
	
	/**
	 * @return
	 */
	public boolean isAllowResizing() {
		return allowResizing;
	}

	/**
	 * @return
	 */
	public Color getBorderColor() {
		return borderColor;
	}

	/**
	 * @return
	 */
	public int getBorderWidth() {
		return borderWidth;
	}

	/**
	 * @return
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @return
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return
	 */
	public Color getLabelColor() {
		return labelColor;
	}

	/**
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param b
	 */
	public void setAllowResizing(boolean b) {
		allowResizing = b;
	}

	/**
	 * @param color
	 */
	public void setBorderColor(Color color) {
		borderColor = color;
	}

	/**
	 * @param i
	 */
	public void setBorderWidth(int i) {
		borderWidth = i;
	}

	/**
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @param font
	 */
	public void setFont(Font font) {
		this.font = font;
	}

	/**
	 * @param i
	 */
	public void setHeight(int i) {
		height = i;
	}

	/**
	 * @param color
	 */
	public void setLabelColor(Color color) {
		labelColor = color;
	}

	/**
	 * @param i
	 */
	public void setWidth(int i) {
		width = i;
	}
	
	public void processObject(final Object o){
		
	}
}
