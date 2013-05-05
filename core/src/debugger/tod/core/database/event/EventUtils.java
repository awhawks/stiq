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
package tod.core.database.event;

import java.util.HashSet;
import java.util.Set;

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;

/**
 * Provides utility methods related to events
 * @author gpothier
 */
public class EventUtils
{
	private static final IgnorableExceptions IGNORABLE_EXCEPTIONS = new IgnorableExceptions();
	
	/**
	 * Indicates if the given exception is ignorable.
	 * Ignorable exceptions include:
	 * <li>Exceptions generated by the standard classloading mechanism</li>
	 */
	public static boolean isIgnorableException (IExceptionGeneratedEvent aEvent)
	{
		return IGNORABLE_EXCEPTIONS.isIgnorableException(aEvent);
	}
	
	private static class IgnorableExceptions
	{
		private Set<String> itsIgnorableExceptions = new HashSet<String>();

		public IgnorableExceptions()
		{
			ignore("java.lang.ClassLoader", "findBootstrapClass");
			ignore("java.net.URLClassLoader$1", "run");
			ignore("java.net.URLClassLoader", "findClass");
			ignore("sun.misc.URLClassPath", "getLoader");
			ignore("sun.misc.URLClassPath$JarLoader", "getJarFile");
		}
		
		private void ignore (String aType, String aBehavior)
		{
			itsIgnorableExceptions.add (aType+"."+aBehavior);
		}
		
		public boolean isIgnorableException (IExceptionGeneratedEvent aEvent)
		{
			IBehaviorInfo theBehavior = aEvent.getOperationBehavior();
			if (theBehavior == null) return true; // TODO: this is temporary
			
			ITypeInfo theType = theBehavior.getDeclaringType();
			return itsIgnorableExceptions.contains(theType.getName()+"."+theBehavior.getName());
		}
	}
}