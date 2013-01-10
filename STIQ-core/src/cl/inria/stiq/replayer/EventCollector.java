/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package cl.inria.stiq.replayer;

import cl.inria.stiq.db.structure.ObjectId;

/**
 * Receives events generated by the replayer.
 * @author gpothier
 */
public abstract class EventCollector
{
	public void fieldRead(ObjectId aTarget, int aFieldSlotIndex)
	{
	}
	
	public void fieldWrite(ObjectId aTarget, int aFieldSlotIndex)
	{
	}
	
	public void arrayRead(ObjectId aTarget, int aIndex)
	{
	}
	
	public void arrayWrite(ObjectId aTarget, int aIndex)
	{
	}
	
	public void localWrite(int aSlot)
	{
		
	}
	
	public void variableRead(int aSlot)
	{
		
	}
	
	public void sync(long aTimestamp)
	{
	}
	
	public void value(ObjectId aValue)
	{
	}
	
	public void value(int aValue)
	{
	}
	
	public void value(long aValue)
	{
	}
	
	public void value(float aValue)
	{
	}
	
	public void value(double aValue)
	{
	}

	/**
	 * Called whenever a snapshot of local variables is taken.
	 */
	public void localsSnapshot(LocalsSnapshot aSnapshot)
	{
	}
	
	public void enter(int aBehaviorId, int aArgsCount)
	{
	}
	
	public void exit()
	{
	}
	
	public void exitException()
	{
	}
	
	public void registerString(ObjectId aId, String aString)
	{
	}

	/**
	 * Registers the association between a temporary id and the corresponding final id.
	 */
	public void associateIds(long aTmpId, long aRealId) 
	{
	}

}