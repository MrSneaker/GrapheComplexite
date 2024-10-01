/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chocosolver.graphsolver.cstrs.basic;

import org.chocosolver.graphsolver.variables.GraphEventType;
import org.chocosolver.graphsolver.variables.GraphVar;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Propagator that ensures that k loops belong to the final graph
 *
 * @author Jean-Guillaume Fages
 */
public class PropNbLoops extends Propagator<Variable> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private GraphVar g;
	private IntVar k;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropNbLoops(GraphVar graph, IntVar k) {
		super(new Variable[]{graph, k}, PropagatorPriority.LINEAR, false);
		this.g = graph;
		this.k = k;
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int min = 0;
		int max = 0;
		ISet nodes = g.getPotentialNodes();
		for (int i : nodes) {
			if (g.getMandSuccOrNeighOf(i).contains(i)) {
				min++;
				max++;
			} else if (g.getPotSuccOrNeighOf(i).contains(i)) {
				max++;
			}
		}
		k.updateLowerBound(min, this);
		k.updateUpperBound(max, this);
		if (min == max) {
			setPassive();
		} else if (k.isInstantiated()) {
			if (k.getValue() == max) {
				for (int i : nodes) {
					if (g.getPotSuccOrNeighOf(i).contains(i)) {
						g.enforceArc(i, i, this);
					}
				}
				setPassive();
			} else if (k.getValue() == min) {
				for (int i : nodes) {
					if (!g.getMandSuccOrNeighOf(i).contains(i)) {
						g.removeArc(i, i, this);
					}
				}
				setPassive();
			}
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		if (vIdx == 0) {
			return GraphEventType.REMOVE_ARC.getMask() + GraphEventType.ADD_ARC.getMask();
		} else {
			return IntEventType.boundAndInst();
		}
	}

	@Override
	public ESat isEntailed() {
		int min = 0;
		int max = 0;
		ISet env = g.getPotentialNodes();
		for (int i : env) {
			if (g.getMandSuccOrNeighOf(i).contains(i)) {
				min++;
				max++;
			} else if (g.getPotSuccOrNeighOf(i).contains(i)) {
				max++;
			}
		}
		if (k.getLB() > max || k.getUB() < min) {
			return ESat.FALSE;
		}
		if (min == max) {
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}
}
