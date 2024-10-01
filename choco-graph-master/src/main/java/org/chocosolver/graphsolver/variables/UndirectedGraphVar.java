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

package org.chocosolver.graphsolver.variables;

import org.chocosolver.graphsolver.variables.delta.GraphDelta;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

public class UndirectedGraphVar extends GraphVar<UndirectedGraph> {

	//////////////////////////////// GRAPH PART /////////////////////////////////////////

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Creates a graph variable
	 *
	 * @param name
	 * @param solver
	 * @param LB
	 * @param UB
	 */
	public UndirectedGraphVar(String name, Model solver, UndirectedGraph LB, UndirectedGraph UB) {
		super(name, solver, LB, UB);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean removeArc(int x, int y, ICause cause) throws ContradictionException {
		assert cause != null;
		if (LB.edgeExists(x, y)) {
			this.contradiction(cause, "remove mandatory arc");
			return false;
		}
		if (UB.removeEdge(x, y)) {
			if (reactOnModification) {
				delta.add(x, GraphDelta.AR_TAIL, cause);
				delta.add(y, GraphDelta.AR_HEAD, cause);
			}
			GraphEventType e = GraphEventType.REMOVE_ARC;
			notifyPropagators(e, cause);
			return true;
		}
		return false;
	}

	@Override
	public boolean enforceArc(int x, int y, ICause cause) throws ContradictionException {
		assert cause != null;
		enforceNode(x, cause);
		enforceNode(y, cause);
		if (UB.edgeExists(x, y)) {
			if (LB.addEdge(x, y)) {
				if (reactOnModification) {
					delta.add(x, GraphDelta.AE_TAIL, cause);
					delta.add(y, GraphDelta.AE_HEAD, cause);
				}
				GraphEventType e = GraphEventType.ADD_ARC;
				notifyPropagators(e, cause);
				return true;
			}
			return false;
		}
		this.contradiction(cause, "enforce arc which is not in the domain");
		return false;
	}

	/**
	 * Get the set of neighbors of vertex 'idx' in the lower bound graph
	 * (mandatory incident edges)
	 *
	 * @param idx a vertex
	 * @return The set of neighbors of 'idx' in LB
	 */
	public ISet getMandNeighOf(int idx) {
		return getMandSuccOrNeighOf(idx);
	}

	/**
	 * Get the set of neighbors of vertex 'idx' in the upper bound graph
	 * (potential incident edges)
	 *
	 * @param idx a vertex
	 * @return The set of neighbors of 'idx' in UB
	 */
	public ISet getPotNeighOf(int idx) {
		return getPotSuccOrNeighOf(idx);
	}

	@Override
	public boolean isDirected() {
		return false;
	}
}
