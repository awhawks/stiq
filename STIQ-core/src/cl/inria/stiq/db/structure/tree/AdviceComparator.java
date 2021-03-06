/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package cl.inria.stiq.db.structure.tree;

import java.util.Comparator;

import zz.utils.tree.SimpleTreeNode;
import cl.inria.stiq.db.structure.ILocationInfo;

/**
 * Compares advices (lerxicographic order)
 * @author gpothier
 */
public class AdviceComparator implements Comparator
{
	public static AdviceComparator ADVICE = new AdviceComparator();

	private AdviceComparator()
	{
	}
	
	public int compare(Object o1, Object o2)
	{
		SimpleTreeNode<ILocationInfo> node = (SimpleTreeNode<ILocationInfo>) o1;
		
		ILocationInfo l = node.pValue().get();
		String n1 = l.getName();
			
		String n2 = (String) o2;
		
		return n1.compareTo(n2);
	}
}